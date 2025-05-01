package com.catpuppyapp.puppygit.dto

import androidx.compose.ui.graphics.vector.ImageVector

data class MenuIconBtnItem (
    val icon:ImageVector,
    val text:String,
    val desc:String = text, // for accessbility
    val enabled:()->Boolean={true},
    val visible:()->Boolean={true},
    val onClick:()->Unit,
)
