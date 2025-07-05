package com.catpuppyapp.puppygit.codeeditor

// it was had "CodeEditorState", so named this to "2"
data class CodeEditorState2(
    val theme: String = PLTheme.AUTO,
    val languageScopeName: String = PLScopes.NONE,
    val autoCompleteEnabled: Boolean = false,
)
