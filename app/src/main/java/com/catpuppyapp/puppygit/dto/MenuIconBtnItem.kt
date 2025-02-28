package com.catpuppyapp.puppygit.dto

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuIconBtnItem (
    val icon:ImageVector,
    val text:String,
    val desc:String, // for accessbility
    val enabled:()->Boolean,
    val visible:()->Boolean,
    val onClick:()->Unit,
)
