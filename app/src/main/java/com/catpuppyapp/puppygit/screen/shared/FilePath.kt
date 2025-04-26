package com.catpuppyapp.puppygit.screen.shared

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.etc.PathType
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.MyLog
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File


private const val TAG = "FilePath"

private val knownSystemFilesManagerUris = listOf(
    "content://com.android.externalstorage.documents/tree/primary",
    "content://com.android.externalstorage.documents/document/primary",

    )

private val knownUris = listOf(
    "content://net.gsantner.markor.provider/external_files/",
    "content://com.blacksquircle.ui.provider/root/",
    "content://com.raival.compose.file.explorer.provider/storage_root/",

    )

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

    // parcel的类最好别用lambda，有可能会出错
    private fun throwResolveUriFailed():String {
        throw RuntimeException("resolve uri failed")

        // for pass compile，不返回通不过编译
        ""
    }


    //这里必须用fun不能用lambda，否则会出错，判断不准，具体原因没测试，总之改成fun就行了，或者把lambda放到parcel类外应该也行（未测试）
    private fun pathOrThrow(tryThisPath :String):String {
        val file = File(tryThisPath)
        return if (file.canRead()) {file.canRead()
            file.canonicalPath
        } else {
            throwResolveUriFailed()
        }
    }

    private fun readableCanonicalPathOrDefault(file:File, default:String):String {
        return file.let {
            if(it.canRead()) {
                it.canonicalPath
            }else {
                default
            }
        }
    }

    private fun initIoPath() {
        ioPath = if(rawPathType == PathType.CONTENT_URI) {
            var decodedPathAtOut:String = ""

            try {
                // try resolve "content://" to real path like "/storage/emulated/0/path"
                val safUri = Uri.parse(rawPath)
                MyLog.d(TAG, "in try: rawUri: $safUri, rawUriPath=${safUri.path}")

                // uri.path是去掉authority（一般是包名）后的路径，例如：uri为：content://com.android.externalstorage.documents/tree/primary%3ARepos, 则uri.path为 /tree/primary:Repos
                val uriPath = safUri?.path ?: ""

                if(uriPath.isBlank()) {
                    throwResolveUriFailed()
                }


                val decodedPath = decodeTheFuckingUriPath(uriPath)
                decodedPathAtOut = decodedPath  //为了在catch里使用
                MyLog.d(TAG, "in try: decodePath=$decodedPath")

                //先直接尝试解码后的路径能否读取，若不能，检查是否是 file:// 或 /file:// 开头的路径，若不是，抛异常，然后尝试匹配已知的uri，若匹配上，解析成绝对路径，否则返回rawPath
                val file = File(decodedPath)
                if (file.canRead()) {
                    file.canonicalPath
                } else {
                    val uriPath = decodedPath

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
                try {
                    MyLog.d(TAG, "in catch: rawPath=$rawPath, decoded uri.path: $decodedPathAtOut")

                    tryResolveKnownUriToRealPath(rawPath)
                }catch (_:Exception) {
                    rawPath
                }
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

    private fun tryResolveKnownUriToRealPath(uriStr:String): String {
        //这里是不是用上面try里解码后的path更好？不过后面的uri都是没编码的，解码其实多此一举...而且由于编码符号可能是文件名，所以解码反而有可能出错，虽然概率很低

        //安卓系统自带的文件选择器选择/storage/emulated/0下的文件就会是这个路径
        //这里不能以 / 结尾去匹配，因为primary后面可能是冒号或者%百分号开头的编码之类的玩意

        //安卓系统的自带文件管理器路径
        val maybeCanGetPathFromUri = knownSystemFilesManagerUris.any { uriStr.startsWith(it) }

        return if(maybeCanGetPathFromUri) {
            readableCanonicalPathOrDefault(File(FsUtils.getRealPathFromUri(Uri.parse(rawPath))), rawPath)
        } else {
            //注意：匹配的时候尽量以 / 结尾，避免文件名包含前缀而匹配错误
            //注意：匹配的时候尽量以 / 结尾，避免文件名包含前缀而匹配错误
            //注意：匹配的时候尽量以 / 结尾，避免文件名包含前缀而匹配错误
            //注意：匹配的时候尽量以 / 结尾，避免文件名包含前缀而匹配错误

            var resolvedPath = ""
            for (uriPrefix in knownUris) {
                // try resolve markor uri to real path
                val indexOfPrefix = uriStr.indexOf(uriPrefix)
                // indexOf == 0, means `include` and `starsWith`, both are `true`
                if(indexOfPrefix == 0) {
                    // length-1 是为了保留之前的 "/"，如果不减1，还得自己在前面prepend一个 "/"
                    resolvedPath = readableCanonicalPathOrDefault(File(uriStr.substring(uriPrefix.length-1)), "")
                    if(resolvedPath.isNotBlank()) {
                        break
                    }
                }
            }

            resolvedPath.ifBlank { rawPath }
        }
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

    companion object {
        //字符串有可能经过多次编码，所以需要循环一下，解析到无法再解析为止
        //不一定解码uri.path，完整的uri.toString()也行
        fun decodeTheFuckingUriPath(uriPath:String):String {
            var decodedPath = Uri.decode(uriPath)

            for (i in 1..3000) { // 解码直到无法再解
                val newPath = Uri.decode(decodedPath)
                if (newPath == decodedPath) { //再解一次和上次一样就说明无法再解了
                    break
                }

                decodedPath = newPath
            }

            return decodedPath
        }
    }
}
