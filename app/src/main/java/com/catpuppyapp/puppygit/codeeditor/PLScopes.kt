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
    MAKE_FILE("source.makefile"),
    MARKDOWN("text.html.markdown"),
    MARKDOWN_MATH("text.html.markdown.math"),
    PERL("source.perl"),
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

            if(fileName.endsWith(".js") || fileName.endsWith(".javascript")) {
                return JAVASCRIPT.scope
            }

            if(fileName.endsWith(".jsx")) {
                return JSX.scope
            }

            if(fileName.endsWith(".json")) {
                return JSON.scope
            }

            if(fileName.endsWith(".less")) {
                return LESS.scope
            }

            if(fileName.endsWith(".lua")) {
                return LUA.scope
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

            if(fileName.endsWith(".c")) {
                return C.scope
            }


            if(fileName.endsWith(".diff") || fileName.endsWith(".patch")) {
                return DIFF.scope
            }


            if(fileName.endsWith(".bat")) {
                return BAT.scope
            }

            if(fileName.endsWith(".cs")) {
                return CSHARP.scope
            }

            if(fileName.endsWith(".css")) {
                return CSS.scope
            }

            if(fileName.endsWith(".dart")) {
                return DART.scope
            }

            if(fileName == "dockerfile" || fileName.endsWith(".dockerfile")) {
                return DOCKER_FILE.scope
            }

            if(fileName.endsWith(".mdmath") || fileName.endsWith(".mdm") || fileName.endsWith(".mmd")) {
                return MARKDOWN_MATH.scope
            }


            if(fileName.endsWith(".perl")) {
                return PERL.scope
            }

            if(fileName.endsWith(".makefile")) {
                return MAKE_FILE.scope
            }

            if(fileName.endsWith(".php")) {
                return PHP.scope
            }

            if(fileName.endsWith(".ps1")) {
                return POWER_SHELL.scope
            }

            if(fileName.endsWith(".py") || fileName.endsWith(".python")) {
                return PYTHON.scope
            }

            if(fileName.endsWith(".rb") || fileName.endsWith(".ruby")) {
                return RUBY.scope
            }

            if(fileName.endsWith(".rust")) {
                return RUST.scope
            }

            if(fileName.endsWith(".scss") || fileName.endsWith(".sass")) {
                return SCSS.scope
            }

            if(fileName.endsWith(".sh")) {
                return SHELL.scope
            }

            if(fileName.endsWith(".sql")) {
                return SQL.scope
            }

            if(fileName.endsWith(".swift")) {
                return SWIFT.scope
            }

            if(fileName.endsWith(".ts")) {
                return TS.scope
            }

            if(fileName.endsWith(".xml")) {
                return XML.scope
            }

            if(fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                return YAML.scope
            }



            return NONE.scope
        }

        fun scopeInvalid(plScope: String) = plScope == NONE.scope || plScope.isBlank() || !SCOPES.contains(plScope)


    }
}
