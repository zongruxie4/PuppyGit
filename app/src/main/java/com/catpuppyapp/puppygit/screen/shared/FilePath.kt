package com.catpuppyapp.puppygit.screen.shared

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

}
