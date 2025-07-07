package com.catpuppyapp.puppygit.codeeditor

//PL is "Program Language"
object PLScopes {
    // 无语法高亮
    // 可用 EmptyLanguage()
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

    fun scopeInvalid(plScope: String) = plScope == NONE || !SCOPES.contains(plScope)


}
