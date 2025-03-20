package com.catpuppyapp.puppygit.screen.shared

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.catpuppyapp.puppygit.etc.PathType
import com.catpuppyapp.puppygit.utils.FsUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * fuck the saf api, saf should full-compatible with File api and support jni, else it just shit
 */
class FuckSafFile(val path: FilePath, val context: Context) {
    var isSaf:Boolean = false
    var file:File? = null
    var docFile:DocumentFile? = null
    var uri:Uri? = null

    init {
        initFile()
    }

    fun lastModified():Long {
        return (if (isSaf) docFile?.lastModified() else file?.lastModified()) ?: 0L
    }

    fun size():Long {
        return (if (isSaf) docFile?.length() else file?.length()) ?: 0L
    }

    fun initFile(){
        val pathType = path.pathType
        val originPath = path.originPath

        if(pathType == PathType.ABSOLUTE) {
            file = File(originPath)
            isSaf = false
        }else if(pathType == PathType.CONTENT_URI) {
            uri = Uri.parse(originPath)
            docFile = FsUtils.getDocumentFileFromUri(context, uri!!)
            isSaf = true
        }else if(pathType == PathType.FILE_URI) {
            val realPath = originPath.removePrefix(FsUtils.fileUriPathPrefix)
            file = File(realPath)
            isSaf = false
        }
    }

    fun inputStream():InputStream {
        return if(isSaf) context.contentResolver.openInputStream(uri!!)!! else file!!.inputStream()
    }

    fun outputStream():OutputStream {
        return if(isSaf) context.contentResolver.openOutputStream(uri!!)!! else file!!.outputStream()
    }
}
