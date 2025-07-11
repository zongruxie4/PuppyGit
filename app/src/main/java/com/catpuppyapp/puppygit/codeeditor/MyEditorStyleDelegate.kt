package com.catpuppyapp.puppygit.codeeditor

import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange
import io.github.rosemoe.sora.lang.brackets.BracketsProvider
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import io.github.rosemoe.sora.lang.styling.Styles


class MyEditorStyleDelegate(
    val codeEditor: MyCodeEditor,
    val inDarkTheme: Boolean,
    val stylesMap: MutableMap<String, StylesResult>,
    val editorState: TextEditorState?,
    val languageScope: PLScope,
): StyleReceiver {
    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?) {
        setStyles(sourceManager, styles, null)
    }

    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?, action: Runnable?) {
//        val requester = codeEditor.stylesUpdateRequestChannel.tryReceive().getOrNull()
//        if(requester == null || requester.ignoreThis) {
//            return
//        }

        if(editorState == null || editorState.fieldsId.isBlank()) {
            return
        }

        if(styles == null) {
            return
        }



//        (resolved) 不行，有问题：
//        lang + editorState1 调用分析，未结束时，lang + editorState2 重新设置了receiver，然后 组合1的结果出来了，就被组合2拿到了，而组合2无法得知组合1 到底是谁
//        啊，这个问题其实可以解决，因为这里无论如何都会put，所以我可以在获取某个字段的annotatedstring的时候检查，如果有style没高亮，执行apply，就行了

        // cache只有一个实例，需要并发安全，key为fieldsId，全局唯一
        val stylesResult = StylesResult(inDarkTheme, styles, StylesResultFrom.CODE_EDITOR, fieldsId = editorState.fieldsId, languageScope = languageScope)
        codeEditor.latestStyles = stylesResult
        if(inDarkTheme == Theme.inDarkTheme) {
//            val targetEditorState = requester.targetEditorState
//            val targetEditorState = editorState
            val copiedStyles = stylesResult.copyWithDeepCopyStyles()
            stylesMap.put(editorState.fieldsId, copiedStyles)
            doJobThenOffLoading {
                // apply copied for editor state to avoid styles changed when applying
                editorState.applySyntaxHighlighting(copiedStyles)
            }
        }
    }

    //received styles is full spans, not a part
    override fun updateStyles(
        sourceManager: AnalyzeManager,
        styles: Styles,
        range: StyleUpdateRange
    ) {
        setStyles(sourceManager, styles)
    }

    override fun setDiagnostics(sourceManager: AnalyzeManager, diagnostics: DiagnosticsContainer?) {

    }

    override fun updateBracketProvider(sourceManager: AnalyzeManager, provider: BracketsProvider?) {

    }
}