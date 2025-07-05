package com.catpuppyapp.puppygit.codeeditor

//PL is "Program Language"
object PLScopes {
    // 无语法高亮
    val NONE = ""

    val HTML = "text.html.basic"

    val SCOPES = listOf(
        HTML,

    )


    fun guessScope(fileName: String):String {
        if(fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return HTML
        }

        return NONE
    }


}
