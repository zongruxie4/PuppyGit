package com.catpuppyapp.puppygit.utils.saf

import android.net.Uri
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
//    const val safPathPrefix = "Saf:/"

//    fun toAppSpecifiedSafFormat(originPath:String):String {
//        return "$safPathPrefix$originPath"
//    }

//    fun getOriginPathFromAppSpecifiedSafPath(appSpecifiedSafPath:String):String {
//        return appSpecifiedSafPath.removePrefix(safPathPrefix)
//    }

    fun init(puppyGitDataDir: File) {
        safDir = createDirIfNonexists(puppyGitDataDir, safDirName)
    }

    /**
     * 把uri转换成适合存到db的格式，虽然只是简单调用toString()，但为了以后要有什么变化，修改方便，所以，还是单独写个函数处理
     */
    fun uriToDbSupportedFormat(uri: Uri):String {
        return uri.toString()
    }

}
