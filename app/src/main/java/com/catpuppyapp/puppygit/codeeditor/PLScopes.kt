package com.catpuppyapp.puppygit.codeeditor

//PL is "Program Language"

enum class PLScopes(val scope: String) {

    // init value for state
    AUTO("AUTO_DETECTED"),


    // 无语法高亮
    // 可不解析或用 EmptyLanguage()
    NONE(""),

    // order by a-z
    BAT("source.batchfile"),
    C("source.c"),
    CPP("source.cpp"),
    CSHARP("source.cs"),
    CSS("source.css"),
    DART("source.dart"),
    DIFF("source.diff"),
    DOCKER_FILE("source.dockerfile"),
    GO("source.go"),
    HTML("text.html.basic"),
    JAVA("source.java"),
    JAVASCRIPT("source.js"),
    JSX("source.js.jsx"),
    JSON("source.json"),
    KOTLIN("source.kotlin"),
    LESS("source.css.less"),
    LUA("source.lua"),
    MARKDOWN("text.html.markdown"),
    MARKDOWN_MATH("text.html.markdown.math"),
    PERL("source.perl"),
    MAKE_FILE("source.makefile"),
    PHP("source.php"),
    POWER_SHELL("source.powershell"),
    PYTHON("source.python"),
    RUBY("source.ruby"),
    RUST("source.rust"),
    SCSS("source.css.scss"),
    SHELL("source.shell"),
    SQL("source.sql"),
    SWIFT("source.swift"),
    TS("source.ts"),
    XML("text.xml"),
    YAML("text.yaml"),


    ;
    companion object {

        val SCOPES = PLScopes.entries.map { it.scope }


        fun guessScope(fileName: String):String {
            val fileName = fileName.lowercase()


            if(fileName.endsWith(".markdown") || fileName.endsWith(".md") || fileName.endsWith(".mdown")) {
                return MARKDOWN.scope
            }

            if(fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                return HTML.scope
            }

            if(fileName.endsWith(".java")) {
                return JAVA.scope
            }

            if(fileName.endsWith(".kotlin") || fileName.endsWith(".kt")) {
                return KOTLIN.scope
            }

            if(fileName.endsWith(".go")) {
                return GO.scope
            }

            if(fileName.endsWith(".cpp") || fileName.endsWith(".cc") || fileName.endsWith(".cxx") || fileName.endsWith(".c++")) {
                return CPP.scope
            }


            if(fileName.endsWith(".diff") || fileName.endsWith(".patch")) {
                return DIFF.scope
            }


            if(fileName.endsWith(".bat")) {
                return BAT.scope
            }

            if(fileName.endsWith(".c")) {
                return C.scope
            }

            if(fileName.endsWith(".cs")) {
                return CSHARP.scope
            }

            if(fileName.endsWith(".mdmath") || fileName.endsWith(".mdm")) {
                return MARKDOWN_MATH.scope
            }



            return NONE.scope
        }

        fun scopeInvalid(plScope: String) = plScope == NONE.scope || plScope.isBlank() || !SCOPES.contains(plScope)


    }
}
