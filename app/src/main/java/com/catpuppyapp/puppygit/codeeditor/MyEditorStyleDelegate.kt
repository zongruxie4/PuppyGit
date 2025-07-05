package com.catpuppyapp.puppygit.codeeditor

import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.utils.cache.EditorStylesCache
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.brackets.BracketsProvider
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import io.github.rosemoe.sora.lang.styling.Styles

class MyEditorStyleDelegate(
    val editorState: CustomStateSaveable<TextEditorState>,

): StyleReceiver {
    val curFieldsId = editorState.value.fieldsId

    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?) {

    }

    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?, action: Runnable?) {
        if(styles == null) {
            return
        }

        // cache可只有一个实例，需要并发安全，key为filesId，全局唯一
        EditorStylesCache.set(curFieldsId, styles)

        if(editorState.value.fieldsId == curFieldsId) {
            editorState.value.applySyntaxHighlighting(curFieldsId, styles)
        }
    }

    override fun setDiagnostics(sourceManager: AnalyzeManager, diagnostics: DiagnosticsContainer?) {

    }

    override fun updateBracketProvider(sourceManager: AnalyzeManager, provider: BracketsProvider?) {

    }
}