package com.catpuppyapp.puppygit.soraeditor

import android.content.Context
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.widget.CodeEditor


object CodeEditorUtil {

    fun createCodeEditor(
        context: Context,
        content:Content,
    ): CodeEditor {
        return CodeEditor(context).apply { setText(content) }
    }

    fun updateContent(
        editor: CodeEditor,
        newContent: Content,
    ) {
        editor.setText(newContent)
    }

}
