package com.catpuppyapp.puppygit.screen.shared

import android.content.Context
import android.os.Parcelable
import com.catpuppyapp.puppygit.etc.PathType
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilePath(
    val originPath:String,
):Parcelable {

    @IgnoredOnParcel
    val pathType = PathType.getType(originPath)

    fun isEmpty():Boolean = originPath.isEmpty()
    fun isBlank():Boolean = originPath.isBlank()
    fun isNotEmpty():Boolean = originPath.isNotEmpty()
    fun isNotBlank():Boolean = originPath.isNotBlank()

    fun toFuckSafFile(context: Context):FuckSafFile {
        return FuckSafFile(context = context, path = this)
    }

    override fun toString(): String {
        return originPath
    }
}
