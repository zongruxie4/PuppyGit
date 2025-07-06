package com.catpuppyapp.puppygit.codeeditor

//PL is "Program Language"
object PLScopes {
    // 无语法高亮
    val NONE = ""

    // init value for state
    val AUTO = "AUTO_DETECTED"

    val HTML = "text.html.basic"

    val SCOPES = listOf(
        NONE,
        HTML,

    )


    fun guessScope(fileName: String):String {
        if(fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return HTML
        }

        return NONE
    }


}
