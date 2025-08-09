package com.catpuppyapp.puppygit.dto

interface MyFileItem {
    fun itemName():String
    fun itemPath():String

    fun itemIsDir():Boolean
}
