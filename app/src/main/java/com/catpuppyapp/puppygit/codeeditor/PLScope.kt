package com.catpuppyapp.puppygit.codeeditor

import androidx.compose.runtime.MutableState
import com.catpuppyapp.puppygit.settings.SettingsUtil

//PL is "Program Language"

enum class PLScope(val scope: String) {

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
    GROOVY("source.groovy"),
    GIT_IGNORE("source.ignore"),
    GIT_REBASE("text.git-rebase"),
    HTML("text.html.basic"),
    INI("source.ini"),
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
    TSX("source.tsx"),
    XML("text.xml"),
    YAML("text.yaml"),

    ;


    companion object {

        val SCOPES = PLScope.entries.map { it.scope }
        val SCOPES_NO_AUTO = PLScope.entries.filter { it != AUTO }

        private fun guessScopeType(fileName: String) : PLScope {
            val fileName = fileName.lowercase()

            if(fileName.endsWith(".txt") || fileName.endsWith(".text")) {
                return NONE
            }

            if(fileName.endsWith(".gitignore")) {
                return GIT_IGNORE
            }

            if(fileName.endsWith(".ts")) {
                return TS
            }

            if(fileName.endsWith(".xml")) {
                return XML
            }

            if(fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
                return YAML
            }

            if(fileName.endsWith(".markdown") || fileName.endsWith(".md") || fileName.endsWith(".mdown")) {
                return MARKDOWN
            }

            if(fileName.endsWith(".html") || fileName.endsWith(".htm")) {
                return HTML
            }

            if(fileName.endsWith(".json")) {
                return JSON
            }

            if(fileName.endsWith(".java")) {
                return JAVA
            }

            if(fileName.endsWith(".js") || fileName.endsWith(".javascript")) {
                return JAVASCRIPT
            }

            if(fileName.endsWith(".jsx")) {
                return JSX
            }

            if(fileName.endsWith(".tsx")) {
                return TSX
            }

            if(fileName.endsWith(".ini") || fileName == "config" || fileName.endsWith(".gitconfig") || fileName.endsWith(".gitmodules")) {
                return INI
            }

            if(fileName.endsWith(".lua")) {
                return LUA
            }

            if(fileName.endsWith(".kotlin") || fileName.endsWith(".kt") || fileName.endsWith(".kts")) {
                return KOTLIN
            }

            if(fileName.endsWith(".go")) {
                return GO
            }

            if(fileName.endsWith(".c")) {
                return C
            }

            if(fileName.endsWith(".cpp") || fileName.endsWith(".cc") || fileName.endsWith(".cxx") || fileName.endsWith(".c++")) {
                return CPP
            }


            if(fileName.endsWith(".diff") || fileName.endsWith(".patch")) {
                return DIFF
            }


            if(fileName.endsWith(".bat")) {
                return BAT
            }

            if(fileName.endsWith(".cs")) {
                return CSHARP
            }

            if(fileName.endsWith(".dart")) {
                return DART
            }

            if(fileName.endsWith(".php")) {
                return PHP
            }

            if(fileName.endsWith(".py") || fileName.endsWith(".python")) {
                return PYTHON
            }

            if(fileName.endsWith(".rust")) {
                return RUST
            }

            if(fileName.endsWith(".gradle") || fileName.endsWith(".groovy")) {
                return GROOVY
            }

            if(fileName == "dockerfile" || fileName.endsWith(".dockerfile")) {
                return DOCKER_FILE
            }

            if(fileName.endsWith(".sh")) {
                return SHELL
            }

            if(fileName.endsWith(".makefile")) {
                return MAKE_FILE
            }

            if(fileName.endsWith(".css")) {
                return CSS
            }

            if(fileName.endsWith(".less")) {
                return LESS
            }


            if(fileName.endsWith(".ps1")) {
                return POWER_SHELL
            }

            if(fileName.endsWith(".rb") || fileName.endsWith(".ruby")) {
                return RUBY
            }


            if(fileName.endsWith(".perl")) {
                return PERL
            }

            if(fileName.endsWith(".scss") || fileName.endsWith(".sass")) {
                return SCSS
            }

            if(fileName.endsWith(".sql")) {
                return SQL
            }

            if(fileName.endsWith(".swift")) {
                return SWIFT
            }

            if(fileName.endsWith(".mdmath") || fileName.endsWith(".mdm") || fileName.endsWith(".mmd")) {
                return MARKDOWN_MATH
            }

            if(fileName.endsWith(".rebase") || fileName.endsWith(".git-rebase") || fileName.endsWith(".gitrebase")) {
                return GIT_REBASE
            }

            return NONE
        }

        private fun guessScope(fileName: String) = guessScopeType(fileName).scope

        fun scopeInvalid(plScope: String?) :Boolean {
            return plScope == null
                    || plScope == NONE.scope
                    || plScope == AUTO.scope
                    || plScope.isBlank()
                    || !SCOPES.contains(plScope)
        }


        fun updatePlScopeIfNeeded(plScope: MutableState<PLScope>, fileName: String) {
            // if was detected language or selected by user, then will not update program language scope again
            if(plScope.value == AUTO) {
                plScope.value = if(SettingsUtil.isEditorSyntaxHighlightEnabled()) {
                    PLScope.guessScopeType(fileName)
                }else {
                    NONE
                }
            }
        }

        fun resetPlScope(plScope: MutableState<PLScope>) {
            plScope.value = AUTO
        }

    }
}
