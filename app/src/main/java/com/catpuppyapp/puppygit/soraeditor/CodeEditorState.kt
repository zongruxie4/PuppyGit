package com.catpuppyapp.puppygit.soraeditor

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor

data class CodeEditorState(
    val context: Context,
    val content: Content = Content(),
    val editor: CodeEditor = createCodeEditor(context, content),
) {
    fun createNewStateWithNewContent(newContent:Content): CodeEditorState {
        return copy(editor = editor.apply { setText(newContent) }, content = newContent)
    }
}

@Composable
fun rememberCodeEditorState(
    stateKeyTag:String,
    stateKeyName:String,

    context: Context,
) = mutableCustomStateOf(stateKeyTag, stateKeyName) {
    CodeEditorState(context)
}

private fun createCodeEditor(
    context: Context,
    content:Content,
): CodeEditor {
    return CodeEditor(context).apply { setText(content) }
}

