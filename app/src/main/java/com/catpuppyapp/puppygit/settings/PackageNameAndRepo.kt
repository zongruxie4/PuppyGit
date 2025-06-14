package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class PackageNameAndRepo(
    val appPackageName:String = "",
    val repoId:String = "",
) {
    fun toKey() = "$appPackageName:$repoId"
}
