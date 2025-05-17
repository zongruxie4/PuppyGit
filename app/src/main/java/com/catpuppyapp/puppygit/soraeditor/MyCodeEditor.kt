package com.catpuppyapp.puppygit.soraeditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.viewinterop.AndroidView
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import io.github.rosemoe.sora.text.Content


/**
 * 警告
 *
 * 如果CodeEditor的底部有可组合项，请使用Modifier.weight(1f)而不是Modifier.fillMaxSize()。否则CodeEditor将会占满整个容器。
 */
@Composable
fun MyCodeEditor(
    stateKeyTag:String,

    modifier: Modifier = Modifier,
    content: CustomStateSaveable<Content>
) {

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current


    val editor = remember {
        CodeEditorUtil.createCodeEditor(
            context,
            content.value,
            onClick = { event, unsub ->
//                event.editor.showSoftInput()
//                keyboardController?.show()
//                println("keyboardController == null: ${keyboardController == null}")
//                println("event.editor.isSoftKeyboardEnabled: ${event.editor.isSoftKeyboardEnabled}")
            }
        )
    }

    AndroidView(
        factory = { editor },
        modifier = modifier,
        onRelease = {
            it.release()
        }
    )

    LaunchedEffect(content.value) {
        CodeEditorUtil.updateContent(editor, content.value)
    }
}
