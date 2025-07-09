package com.catpuppyapp.puppygit.codeeditor

//PL is "Program Language"
object PLScopes {
    // 无语法高亮
    // 可用 EmptyLanguage()
    val NONE = ""

    // init value for state
    val AUTO = "AUTO_DETECTED"

    // order by a-z
    val CPP = "source.cpp"
    val GO = "source.go"
    val HTML = "text.html.basic"
    val JAVA = "source.java"
    val KOTLIN = "source.kotlin"

    val SCOPES = listOf(
        NONE,
        CPP,
        GO,
        HTML,
        JAVA,
        KOTLIN,

    )


    fun guessScope(fileName: String):String {
        val fileName = fileName.lowercase()

        if(fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return HTML
        }

        if(fileName.endsWith(".java")) {
            return JAVA
        }

        if(fileName.endsWith(".kotlin") || fileName.endsWith(".kt")) {
            return KOTLIN
        }

        if(fileName.endsWith(".go")) {
            return GO
        }

        if(fileName.endsWith(".cpp") || fileName.endsWith(".cc") || fileName.endsWith(".cxx")) {
            return CPP
        }

        return NONE
    }

    fun scopeInvalid(plScope: String) = plScope == NONE || !SCOPES.contains(plScope)


}
