package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.screen.shared.FilePath

data class FileDetail(
    val fileName:String,
    val filePath: FilePath,
    val shortContent:String = "",
)
