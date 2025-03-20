package com.catpuppyapp.puppygit.screen.shared

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


object FileDisplayType{
    const val FILE = 0 shl 1

    const val DIR = 0 shl 2
}

/**
 * 根据dispalyType匹配文件或文件夹；若 pattern非空，将根据pattern匹配文件名，否则显示所有文件
 */
@Parcelize
data class FileDisplayFilter(
    val displayTypeFlags: Int = FileDisplayType.FILE or FileDisplayType.DIR,
    val pattern:String = "",
) : Parcelable {


}
