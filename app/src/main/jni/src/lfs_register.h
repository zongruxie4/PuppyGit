#ifndef PUPPYGIT_ANDROID_LFS_REGISTER_H
#define PUPPYGIT_ANDROID_LFS_REGISTER_H

// =========================================================================
// 【配置项】请在这里填入你在安卓系统上的 git-lfs 可执行文件的真实绝对路径
// =========================================================================
// 默认值，初始化lfs过滤器时会修改
static char *G_LFS_BINARY_PATH = "/data/data/com.catpuppyapp.puppygit.play.pro/files/bin/git-lfs";

int register_android_git_lfs_filter(void);
int unregister_android_git_lfs_filter(void);

#endif //PUPPYGIT_ANDROID_LFS_REGISTER_H
