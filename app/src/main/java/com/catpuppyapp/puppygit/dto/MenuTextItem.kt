package com.catpuppyapp.puppygit.dto

data class MenuTextItem (
    val text:String,
    val enabled:()->Boolean = {true},
    val visible:()->Boolean = {true},
    val onClick:()->Unit,
)
