#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/wait.h>

// 引入标准的 libgit2 头文件
#include "git2.h"
#include "git2/sys/filter.h"

// =========================================================================
// 【可变配置项】请在这里填入你在安卓系统上的 git-lfs 可执行文件的真实绝对路径
// 例如安卓应用私有数据目录: "/data/data/com.your.app/files/bin/git-lfs"
// =========================================================================
static const char *G_LFS_BINARY_PATH = "/path/to/your/android/git-lfs";

static git_filter *create_lfs_filter(void);

/**
 * 【主程序调用接口】
 * 在你的安卓项目初始化 Git 业务时（例如执行 git_libgit2_init() 之后），
 * 调用此函数来全局注册 LFS 过滤器。
 * * @return 0 成功，小于 0 失败
 */
int git_lfs_filter_register(void)
{
    // GIT_FILTER_DRIVER_PRIORITY (200) 是大文件过滤器的标准优先级
    return git_filter_register("lfs", create_lfs_filter(), GIT_FILTER_DRIVER_PRIORITY);
}

/**
 * 【主程序调用接口】
 * 在程序退出或不再需要 Git 服务时，调用此函数注销过滤器，释放全局资源。
 */
void git_lfs_filter_unregister(void)
{
    git_filter_unregister("lfs");
}

/**
 * 1. 检查阶段：当 libgit2 解析到 .gitattributes 中满足 filter=lfs 的文件时触发
 */
static int lfs_filter_check(
        git_filter  *self,
        void **payload,
        const git_filter_source *src,
        const char **attr_values)
{
    (void)self;

    // 当属性精准匹配到 "lfs" 时（即属性配置为 filter=lfs）
    if (attr_values[0] && strcmp(attr_values[0], "lfs") == 0) {
        // 提取当前正在处理的文件路径（对应 Git 里的 %f 参数）
        // 并通过 payload 传递给接下来的 apply 阶段
        const char *src_path = git_filter_source_path(src);
        if (!src_path) return GIT_PASSTHROUGH;

        char *filename = strdup(src_path);
        if (!filename) return GIT_ERROR; // 内存分配失败

        *payload = filename;
        return 0; // 0 代表确认应用此过滤器
    }

    return GIT_PASSTHROUGH;
}

/**
 * 核心执行：安卓(Linux)底层双向管道通信，调用外部 git-lfs 进程
 */
static int execute_lfs_command(const char *action, const char *filename, git_str *to, const git_str *from)
{
    int in_pipe[2];  // 写入子进程的 stdin
    int out_pipe[2]; // 读取子进程的 stdout

    if (pipe(in_pipe) < 0 || pipe(out_pipe) < 0) {
        return -1;
    }

    pid_t pid = fork();
    if (pid < 0) {
        close(in_pipe[0]); close(in_pipe[1]);
        close(out_pipe[0]); close(out_pipe[1]);
        return -1;
    }

    if (pid == 0) {
        // ------ 子进程逻辑 ------
        dup2(in_pipe[0], STDIN_FILENO);   // 将管道输入重定向为标准输入
        dup2(out_pipe[1], STDOUT_FILENO); // 将管道输出重定向为标准输出

        close(in_pipe[1]);
        close(out_pipe[0]);

        // 构造 execv 参数，等价于在 Shell 执行: git-lfs <smudge/clean> -- <filename>
        char *args[] = {
                (char *)G_LFS_BINARY_PATH,
                (char *)action,
                "--",
                (char *)filename,
                NULL
        };

        execv(G_LFS_BINARY_PATH, args);

        // 如果 execv 失败（例如权限不足或路径错误），直接退出子进程，避免影响主进程
        exit(127);
    } else {
        // ------ 父进程逻辑 ------
        close(in_pipe[0]);
        close(out_pipe[1]);

        // 1. 将旧数据写入 git-lfs 进程
        if (from && from->size > 0) {
            // 注意：如果文件极大（如数百兆），在极其严格的生产环境，
            // 应该开线程或用 select/poll 异步读写，以防单次 write 塞满管道产生阻塞。
            // 这里采用单次写入，能应付绝大多数 LFS 指针与中小文件转换。
            if (write(in_pipe[1], from->ptr, from->size) < 0) {
                close(in_pipe[1]);
                close(out_pipe[0]);
                return -1;
            }
        }
        close(in_pipe[1]); // 写完必须立刻关闭！发送 EOF 告诉 git-lfs 数据已传完

        // 2. 从 git-lfs 进程接收转换后的新数据
        char buffer[4096];
        ssize_t bytes_read;
        while ((bytes_read = read(out_pipe[0], buffer, sizeof(buffer))) > 0) {
            // 将读取到的字节动态存入 libgit2 提供的 `git_str` 缓存区
            if (git_str_put(to, buffer, bytes_read) < 0) {
                close(out_pipe[0]);
                return -1;
            }
        }
        close(out_pipe[0]);

        // 3. 回收子进程，避免僵尸进程
        int status;
        waitpid(pid, &status, 0);
        return (WIFEXITED(status) && WEXITSTATUS(status) == 0) ? 0 : -1;
    }
}

/**
 * 2. 核心应用阶段：根据当前 Git 的操作方向执行 clean 或 smudge
 */
static int lfs_filter_apply(
        git_filter     *self,
        void          **payload,
        git_str        *to,
        const git_str  *from,
        const git_filter_source *source)
{
    (void)self;
    const char *filename = *payload;
    git_filter_mode_t mode = git_filter_source_mode(source);

    if (mode == GIT_FILTER_SMUDGE) {
        // 检出/下载时：指针文件 -> 真实大文件
        return execute_lfs_command("smudge", filename, to, from);
    } else if (mode == GIT_FILTER_CLEAN) {
        // 提交/打包时：真实大文件 -> 指针文件
        return execute_lfs_command("clean", filename, to, from);
    }

    return GIT_PASSTHROUGH;
}

/**
 * 3. 流初始化：libgit2 默认的缓冲流逻辑
 */
static int lfs_filter_stream(
        git_writestream **out,
        git_filter *self,
        void **payload,
        const git_filter_source *src,
        git_writestream *next)
{
    return git_filter_buffered_stream_new(out,
                                          self, lfs_filter_apply, NULL, payload, src, next);
}

/**
 * 4. 单次清理：每个文件过滤完成后，释放 check 阶段申请的文件名内存
 */
static void lfs_filter_cleanup(git_filter *self, void *payload)
{
    (void)self;
    free(payload);
}

/**
 * 5. 全局销毁：注销过滤器时释放过滤器结构体自身
 */
static void lfs_filter_free(git_filter *f)
{
    free(f);
}

/**
 * 创建并装配 git_filter 结构体
 */
static git_filter *create_lfs_filter(void)
{
    // 使用 calloc 分配纯 C 结构体内存
    git_filter *filter = calloc(1, sizeof(git_filter));
    if (!filter) return NULL;

    filter->version    = GIT_FILTER_VERSION;
    filter->attributes = "filter=lfs"; // 显式匹配属性
    filter->check      = lfs_filter_check;
    filter->stream     = lfs_filter_stream;
    filter->cleanup    = lfs_filter_cleanup;
    filter->shutdown   = lfs_filter_free;

    return filter;
}
