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
    val DIFF = "source.diff"
    val GO = "source.go"
    val HTML = "text.html.basic"
    val JAVA = "source.java"
    val KOTLIN = "source.kotlin"
    val MARKDOWN = "text.html.markdown"

    val SCOPES = listOf(
        NONE,
        CPP,
        DIFF,
        GO,
        HTML,
        JAVA,
        KOTLIN,
        MARKDOWN
    )


    fun guessScope(fileName: String):String {
        val fileName = fileName.lowercase()


        if(fileName.endsWith(".markdown") || fileName.endsWith(".md") || fileName.endsWith(".mdown")) {
            return MARKDOWN
        }

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

        if(fileName.endsWith(".cpp") || fileName.endsWith(".cc") || fileName.endsWith(".cxx") || fileName.endsWith(".c++")) {
            return CPP
        }


        if(fileName.endsWith(".diff") || fileName.endsWith(".patch")) {
            return DIFF
        }



        return NONE
    }

    fun scopeInvalid(plScope: String) = plScope == NONE || !SCOPES.contains(plScope)


}
