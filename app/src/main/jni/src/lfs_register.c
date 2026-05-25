#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <android/log.h>

#include "git2.h"
#include "git2/sys/filter.h"

#include "lfs_register.h"



// =========================================================================
// 【硬核破解】既然 libgit2 把真实的公开 git_buf 变成了不透明的隐藏结构，
// 我们直接在本地声明一个完全对齐的镜像结构体，用来进行无视版本的内存强灌。
// =========================================================================
typedef struct {
    char   *ptr;
    size_t asize;
    size_t size;
} local_git_buf_mirror;

/**
 * 针对安卓优化的外部进程调用核心：纯 C 内存手工追加，零依赖 libgit2 的 Buffer 函数
 */
static int execute_lfs_command(
        const char *action,
        const char *filename,
        git_buf *to,
        const char *from_bytes,
        size_t from_len,
        const char *repo_path
) {
    __android_log_print(ANDROID_LOG_DEBUG, "lfs_register", "execute_lfs_command: begin");

    int in_pipe[2]; int out_pipe[2]; int err_pipe[2];
    if (pipe(in_pipe) < 0 || pipe(out_pipe) < 0 || pipe(err_pipe) < 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "lfs_register", "execute_lfs_command: pipe failed, repoPath=%s", repo_path ? repo_path : "NULL");
        return -1;
    }

    pid_t pid = fork();
    if (pid < 0) {
        close(in_pipe[0]); close(in_pipe[1]);
        close(out_pipe[0]); close(out_pipe[1]);
        close(err_pipe[0]); close(err_pipe[1]);
        __android_log_print(ANDROID_LOG_DEBUG, "lfs_register", "execute_lfs_command: fork failed, repoPath=%s", repo_path ? repo_path : "NULL");
        return -1;
    }

    if (pid == 0) {
        // ------ 子进程逻辑 ------
        dup2(in_pipe[0], STDIN_FILENO);
        dup2(out_pipe[1], STDOUT_FILENO);
        dup2(err_pipe[1], STDERR_FILENO); // 重定向标准错误到错误管道

        close(in_pipe[0]); close(in_pipe[1]);
        close(out_pipe[0]); close(out_pipe[1]);
        close(err_pipe[0]); close(err_pipe[1]);

        if (repo_path && chdir(repo_path) < 0) {
            exit(126);
        }

        char *args[] = {(char *)G_LFS_BINARY_PATH, (char *)action, "--", (char *)filename, NULL};
        execv(G_LFS_BINARY_PATH, args);
        __android_log_print(ANDROID_LOG_ERROR, "git-lfs-stderr", "G_LFS_BINARY_PATH=%s", G_LFS_BINARY_PATH);

        exit(127);
    } else {
        // ------ 父进程逻辑 ------
        close(in_pipe[0]); close(out_pipe[1]); close(err_pipe[1]); // 关闭父进程不需要的端口

        // 1. 将数据写入 git-lfs 进程
        if (from_bytes && from_len > 0) {
            if (write(in_pipe[1], from_bytes, from_len) < 0) {
                close(in_pipe[1]); close(out_pipe[0]); close(err_pipe[0]);
                return -1;
            }
        }
        close(in_pipe[1]); // 写完立刻发送 EOF

        // 2. 接收 git-lfs 的标准输出 (stdout)
        char *out_mem = NULL;
        size_t out_size = 0;
        size_t out_allocated = 0;

        char buffer[4096];
        ssize_t bytes_read;
        while ((bytes_read = read(out_pipe[0], buffer, sizeof(buffer))) > 0) {
            if (out_size + bytes_read > out_allocated) {
                size_t new_alloc = out_allocated == 0 ? 4096 : out_allocated * 2;
                while (new_alloc < out_size + bytes_read) new_alloc *= 2;

                char *new_ptr = realloc(out_mem, new_alloc);
                if (!new_ptr) {
                    free(out_mem);
                    close(out_pipe[0]); close(err_pipe[0]);
                    return -1;
                }
                out_mem = new_ptr;
                out_allocated = new_alloc;
            }
            memcpy(out_mem + out_size, buffer, bytes_read);
            out_size += bytes_read;
        }
        close(out_pipe[0]);

        // 3. 接收并打印 git-lfs 的错误输出 (stderr)
        char err_buffer[1024];
        ssize_t err_bytes_read;
        while ((err_bytes_read = read(err_pipe[0], err_buffer, sizeof(err_buffer) - 1)) > 0) {
            err_buffer[err_bytes_read] = '\0';
            __android_log_print(ANDROID_LOG_ERROR, "git-lfs-stderr", "[LFS Output]: %s", err_buffer);
        }
        close(err_pipe[0]);

        int status;
        waitpid(pid, &status, 0);

        if (WIFEXITED(status) && WEXITSTATUS(status) == 0) {
            local_git_buf_mirror *mirror = (local_git_buf_mirror *)to;
            mirror->ptr = out_mem;
            mirror->asize = out_allocated;
            mirror->size = out_size;
            return 0;
        } else {
            if (WIFEXITED(status)) {
                __android_log_print(ANDROID_LOG_ERROR, "lfs_register", "execute_lfs_command: exit code = %d, repoPath=%s", WEXITSTATUS(status), repo_path ? repo_path : "NULL");
            } else if (WIFSIGNALED(status)) {
                __android_log_print(ANDROID_LOG_ERROR, "lfs_register", "execute_lfs_command: killed by signal = %d, repoPath=%s", WTERMSIG(status), repo_path ? repo_path : "NULL");
            }
            free(out_mem);
            return -1;
        }
    }
}

/**
 * 接口层：在这里把公开的 git_buf 解包成普通的 char* 并执行命令
 */
static int lfs_filter_apply(
        git_filter     *self,
        void          **payload,
        git_buf        *to,
        const git_buf  *from,
        const git_filter_source *source)
{
    (void)self;
    const char *filename = (const char *)*payload;
    git_filter_mode_t mode = git_filter_source_mode(source);

    // =========================================================================
    // 【动态获取仓库路径】通过 source 拿到当前正在操作的 repo，再拿到工作区绝对路径
    // =========================================================================
    git_repository *repo = git_filter_source_repo(source);
    const char *repo_workdir = git_repository_workdir(repo);
    // 注意：repo_workdir 拿到的路径通常自带尾部斜杠 '/'，例如 "/sdcard/my_repo/"

    const local_git_buf_mirror *from_mirror = (const local_git_buf_mirror *)from;

    const char *action = (mode == GIT_FILTER_SMUDGE) ? "smudge" : "clean";

    // 把提取出来的 repo_workdir 作为一个新参数传给执行命令的函数
    return execute_lfs_command(action, filename, to, from_mirror->ptr, from_mirror->size, repo_workdir);
}

/**
 * 属性匹配检查：当满足 filter=lfs 属性时触发
 */
static int lfs_filter_check(git_filter *self, void **payload, const git_filter_source *src, const char **attr_values)
{
    (void)self;
    if (attr_values[0] && strcmp(attr_values[0], "lfs") == 0) {
        const char *src_path = git_filter_source_path(src);
        if (!src_path) return GIT_PASSTHROUGH;
        *payload = strdup(src_path); // 复制路径作为 payload 传给 apply
        return 0;
    }
    return GIT_PASSTHROUGH;
}

static void lfs_filter_cleanup(git_filter *self, void *payload)
{
    (void)self;
    free(payload);
}

static void lfs_filter_free(git_filter *f)
{
    free(f);
}

/**
 * 【全局公开注册接口】
 * 在你的安卓 JNI 项目初始化 Git 业务时调用（必须在 git_libgit2_init() 之后）
 * @return 0 on successful registry, error code <0 on failure
 */
int register_android_git_lfs_filter(void)
{
    chmod(G_LFS_BINARY_PATH, 0755);
    git_filter *filter = calloc(1, sizeof(git_filter));
    if (!filter) return -1;

    filter->version    = GIT_FILTER_VERSION;
    filter->attributes = "filter=lfs";
    filter->check      = lfs_filter_check;
    filter->apply      = lfs_filter_apply;  // 直接挂载 apply 模式，让 libgit2 自带公开流托管
    filter->stream     = NULL;              // 显式给 NULL，绝不碰易报错的 git_writestream
    filter->cleanup    = lfs_filter_cleanup;
    filter->shutdown   = lfs_filter_free;

    return git_filter_register("lfs", filter, GIT_FILTER_DRIVER_PRIORITY);
}

/**
 * 【全局公开注销接口】
 * 在 App 退出或不再需要 Git 服务时调用
 */
int unregister_android_git_lfs_filter(void)
{
    return git_filter_unregister("lfs");
}
