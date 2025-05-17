package com.catpuppyapp.puppygit.soraeditor

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun CodeEditor(
    stateKeyTag:String,

    modifier: Modifier = Modifier,
    state: CodeEditorState
) {

    val context = LocalContext.current
    val editor = rememberCodeEditorState(stateKeyTag, "editor", context)

    AndroidView(
        factory = { editor.value.editor },
        modifier = modifier,
        onRelease = {
            it.release()
        }
    )
    // ...
}