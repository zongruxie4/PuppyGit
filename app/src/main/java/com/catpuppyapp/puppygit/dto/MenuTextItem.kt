package com.catpuppyapp.puppygit.dto

data class MenuTextItem (
    val text:String,
    val enabled:()->Boolean,
    val visible:()->Boolean,
    val onClick:()->Unit,
)
