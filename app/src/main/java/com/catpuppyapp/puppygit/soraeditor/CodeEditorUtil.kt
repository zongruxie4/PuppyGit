package com.catpuppyapp.puppygit.soraeditor

import android.content.Context
import io.github.rosemoe.sora.event.ClickEvent
import io.github.rosemoe.sora.event.EventReceiver
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor


object CodeEditorUtil {

    fun createCodeEditor(
        context: Context,
        content:Content,
        onClick:EventReceiver<ClickEvent>,
    ): CodeEditor {
        return CodeEditor(context).apply {
            setText(content)
            subscribeEvent(ClickEvent::class.java, onClick)
        }
    }

    fun updateContent(
        editor: CodeEditor,
        newContent: Content,
    ) {
        editor.setText(newContent)
    }

}
