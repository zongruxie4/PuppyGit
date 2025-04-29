package com.catpuppyapp.puppygit.dto

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuIconBtnItem<T> (
    val icon:ImageVector,
    val text:String,
    val desc:String, // for accessbility
    val enabled:(T)->Boolean,
    val visible:(T)->Boolean,
    val onClick:(T)->Unit,
)
