#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/stat.h>

// 引入标准的公开 libgit2 头文件
#include "git2.h"
#include "git2/sys/filter.h"

// =========================================================================
// 【配置项】请在这里填入你在安卓系统上的 git-lfs 可执行文件的真实绝对路径
// =========================================================================
// 默认值，初始化lfs过滤器时会修改
static char *G_LFS_BINARY_PATH = "/data/data/com.catpuppyapp.puppygit.play.pro/files/bin/git-lfs";

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
    int in_pipe[2];  int out_pipe[2];
    if (pipe(in_pipe) < 0 || pipe(out_pipe) < 0) return -1;

    pid_t pid = fork();
    if (pid < 0) {
        close(in_pipe[0]); close(in_pipe[1]); close(out_pipe[0]); close(out_pipe[1]);
        return -1;
    }

    if (pid == 0) {
        // ------ 子进程逻辑 ------
        dup2(in_pipe[0], STDIN_FILENO);
        dup2(out_pipe[1], STDOUT_FILENO);
        close(in_pipe[1]); close(out_pipe[0]);

        // =========================================================================
        // 【动态工作目录切换】如果成功拿到仓库路径，就在 execv 之前切过去
        // =========================================================================
        if (repo_path && chdir(repo_path) < 0) {
            exit(126);
        }

        char *args[] = {(char *)G_LFS_BINARY_PATH, (char *)action, "--", (char *)filename, NULL};
        execv(G_LFS_BINARY_PATH, args);
        exit(127);
    } else {
        // ------ 父进程逻辑 ------
        close(in_pipe[0]); close(out_pipe[1]);

        // 1. 将数据写入 git-lfs 进程
        if (from_bytes && from_len > 0) {
            if (write(in_pipe[1], from_bytes, from_len) < 0) {
                close(in_pipe[1]); close(out_pipe[0]);
                return -1;
            }
        }
        close(in_pipe[1]); // 写完立刻关闭，向 git-lfs 发送 EOF

        // 2. 用我们自己的纯 C 变量接住 git-lfs 的标准输出
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
                    close(out_pipe[0]);
                    return -1;
                }
                out_mem = new_ptr;
                out_allocated = new_alloc;
            }
            memcpy(out_mem + out_size, buffer, bytes_read);
            out_size += bytes_read;
        }
        close(out_pipe[0]);

        int status;
        waitpid(pid, &status, 0);

        if (WIFEXITED(status) && WEXITSTATUS(status) == 0) {
            // =========================================================================
            // 通过强转成我们自己的镜像结构体指针，直接给隐藏的 to 变量赋值
            // 绕过所有 to->asize 不存在、git_buf_set 找不到的阴间报错
            // =========================================================================
            local_git_buf_mirror *mirror = (local_git_buf_mirror *)to;
            mirror->ptr = out_mem;
            mirror->asize = out_allocated;
            mirror->size = out_size;
            return 0;
        } else {
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
void unregister_android_git_lfs_filter(void)
{
    git_filter_unregister("lfs");
}
