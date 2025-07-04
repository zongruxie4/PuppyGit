package com.catpuppyapp.puppygit.soraeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor


data class CodeEditorState(
    var editor: CodeEditor? = null,
    val initialContent: Content = Content(),
    //if true, when hard kb available, will not show soft kb; set to false to show soft kb in any case
    val isDisableSoftKbdIfHardKbdAvailable: Boolean = true,
    val softWrap: Boolean = true,
) {
    val content = mutableStateOf(initialContent)
}

@Composable
fun rememberCodeEditorState(
    initialContent: Content = Content()
) = remember {
    CodeEditorState(
        initialContent = initialContent
    )
}
