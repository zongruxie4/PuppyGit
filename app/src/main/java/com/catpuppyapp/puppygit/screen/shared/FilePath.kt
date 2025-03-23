package com.catpuppyapp.puppygit.screen.shared

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.etc.PathType
import com.catpuppyapp.puppygit.utils.FsUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
class FilePath(
    private val rawPath:String,
):Parcelable {
    @IgnoredOnParcel
    private val rawPathType = PathType.getType(rawPath)

    @IgnoredOnParcel
    var ioPath:String = rawPath

    @IgnoredOnParcel
    var ioPathType = rawPathType

    init {
        initIoPath()
    }

    private fun initIoPath() {
        ioPath = if(rawPathType == PathType.CONTENT_URI) {
            val throwResolveUriFailed = {
                throw RuntimeException("resolve uri failed")
            }

            try {
                // try resolve "content://" to real path like "/storage/emulated/0/path"
                val safUri = Uri.parse(rawPath)
                // uri.path是去掉authority（一般是包名）后的路径，例如：uri为：content://com.android.externalstorage.documents/tree/primary%3ARepos, 则uri.path为 /tree/primary:Repos
                val uriPath = safUri?.path ?: ""

                if(uriPath.isBlank()) {
                    throwResolveUriFailed()
                }

                //字符串有可能经过多次编码，所以需要循环一下，解析到无法再解析为止
                var decodedPath = Uri.decode(uriPath)
                for (i in 1..3000) { // 解码直到无法再解
                    val newPath = Uri.decode(decodedPath)
                    if (newPath == decodedPath) { //再解一次和上次一样就说明无法再解了
                        break
                    }

                    decodedPath = newPath
                }

                if (File(decodedPath).canRead()) {
                    decodedPath
                } else {
                    val uriPath = decodedPath

                    val pathOrThrow = { tryThisPath:String ->
                        if (File(tryThisPath).canRead()) {
                            tryThisPath
                        } else {
                            throwResolveUriFailed()
                        }
                    }

                    if (uriPath.startsWith(FsUtils.fileUriPathPrefix)) {
                        pathOrThrow(uriPath.removePrefix(FsUtils.fileUriPathPrefix))
                    } else {
                        val slashPrefixFileUriPath = Cons.slash + FsUtils.fileUriPathPrefix  // "/file://"，质感文件的uri解码到最后再取出uri.path返回的就是这样的路径
                        if (uriPath.startsWith(slashPrefixFileUriPath)) {
                            pathOrThrow(uriPath.removePrefix(slashPrefixFileUriPath))
                        }else {
                            throwResolveUriFailed()
                        }
                    }
                }
            }catch (_: Exception) {
                rawPath
            }
        }else if(rawPathType == PathType.FILE_URI) {
            rawPath.removePrefix(FsUtils.fileUriPathPrefix)
        }else if(rawPathType == PathType.ABSOLUTE) {
            rawPath
        }else {
            rawPath
        }

        ioPathType = PathType.getType(ioPath)
    }

    fun isEmpty():Boolean = ioPath.isEmpty()
    fun isBlank():Boolean = ioPath.isBlank()
    fun isNotEmpty():Boolean = ioPath.isNotEmpty()
    fun isNotBlank():Boolean = ioPath.isNotBlank()

    fun toFuckSafFile(context: Context):FuckSafFile {
        return FuckSafFile(context = context, path = this)
    }

    override fun toString(): String {
        return ioPath
    }
}
