package com.catpuppyapp.puppygit.utils

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import java.io.File

object SafAndFileCmpUtil {

    class SafAndFileCompareResult (
        val onlyInSaf:MutableList<DocumentFile>,
        val onlyInFiles:MutableList<File>,
        val modified:MutableList<SafAndFileDiffPair>
    )

    class SafAndFileDiffPair(
        val safFile: DocumentFile,
        val file: File,
        val diffType:SafAndFileDiffType
    )

    enum class SafAndFileDiffType(val code: Int) {
        /**
         * 没区别，完全一样
         */
        NONE(0),

        /**
         * 内容不同
         */
        CONTENT(1),

        /**
         * 类型不同，例如一个是目录，另一个是文件
         */
        TYPE(2)
        ;

        companion object {
            fun fromCode(code: Int): SafAndFileDiffType? {
                return SafAndFileDiffType.entries.find { it.code == code }
            }
        }
    }

    /**
     * 打开uri失败
     */
    class OpenInputStreamFailed(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause) {
        // 可以在这里添加额外的属性或方法
    }

    /**
     * 比较DocumentFile和File目录的区别，保存结果盗result
     */
    fun recursiveCompareFiles_Saf(
        contentResolver: ContentResolver,
        safFiles:Array<DocumentFile>,
        files:Array<File>,
        result:SafAndFileCompareResult
    ) {
        val safFiles = safFiles.toMutableList()
        val files = files.toMutableList()

        for(f in files) {
            //saff = saf file
            val saffIndex = safFiles.indexOfFirst { f.name == it.name }
            if(saffIndex != -1) {
                val saff = safFiles[saffIndex]

                //注：没考虑符号链接或者documentFile的isVirtual之类的
                // 比较两个文件是否一样
                if((f.isFile.not() && saff.isFile) || (f.isFile && saff.isFile.not()) ) {
                    result.modified.add(
                        SafAndFileDiffPair(
                            safFile = saff,
                            file = f,
                            diffType = SafAndFileDiffType.TYPE
                        )
                    )
                }else if(f.isFile && saff.isFile) {
                    val diffType = if(f.length() != saff.length()) {
                        SafAndFileDiffType.CONTENT
                    }else { //大小一样，逐字节比较
                        var type = SafAndFileDiffType.NONE

                        val fi = f.inputStream()
                        val saffi = contentResolver.openInputStream(saff.uri) ?: throw OpenInputStreamFailed(message = "uri = '${saff.uri}'")
                        fi.use { fiit->
                            saffi.use { saffiit->
                                while (true) {
                                    val fb = fiit.read()
                                    if(fb == -1) {
                                        break
                                    }

                                    val saffb = saffiit.read()
                                    if(saffb != fb) {
                                        type = SafAndFileDiffType.CONTENT
                                        break
                                    }
                                }
                            }
                        }

                        type
                    }

                    //不一样就添加，一样就算了
                    if(diffType != SafAndFileDiffType.NONE) {
                        result.modified.add(
                            SafAndFileDiffPair(
                                safFile = saff,
                                file = f,
                                diffType = diffType
                            )
                        )
                    }
                }else if(f.isDirectory && saff.isDirectory) {
                    recursiveCompareFiles_Saf(contentResolver, saff.listFiles() ?: arrayOf(), f.listFiles() ?: arrayOf(), result)
                }

                safFiles.removeAt(saffIndex)
            }else {  //no matched only f in files
                result.onlyInFiles.add(f)
            }
        }

        result.onlyInSaf.addAll(safFiles)
    }

}
