package com.catpuppyapp.puppygit.utils.saf

import com.catpuppyapp.puppygit.utils.createDirIfNonexists
import java.io.File

object SafUtil {
    /**
     * saf和ppgit内部目录的缓冲区，其下每个目录都应该关联一个saf目录
     */
    const val safDirName = "saf"

    var safDir:File? = null

    /**
     * 非saf模式的路径必然是/开头的，只是在显示时区分internal://和external://，但saf则不同，存储的时候会加上saf前缀以和普通路径区分
     */
    const val safPathPrefix = "Saf:/"

    fun init(puppyGitDataDir: File) {
        safDir = createDirIfNonexists(puppyGitDataDir, safDirName)
    }


}
