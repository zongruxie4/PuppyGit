package com.catpuppyapp.puppygit.codeeditor

import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange
import io.github.rosemoe.sora.lang.brackets.BracketsProvider
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import io.github.rosemoe.sora.lang.styling.Styles



class MyEditorStyleDelegate(
    val editorState: CustomStateSaveable<TextEditorState>,
    val curFieldsId: String,
    val inDarkTheme: Boolean,
    val stylesMap: MutableMap<String, StylesResult>,
): StyleReceiver {
    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?) {
        setStyles(sourceManager, styles, null)
    }

    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?, action: Runnable?) {
        if(styles == null) {
            return
        }

//        (resolved) 不行，有问题：
//        lang + editorState1 调用分析，未结束时，lang + editorState2 重新设置了receiver，然后 组合1的结果出来了，就被组合2拿到了，而组合2无法得知组合1 到底是谁
//        啊，这个问题其实可以解决，因为这里无论如何都会put，所以我可以在获取某个字段的annotatedstring的时候检查，如果有style没高亮，执行apply，就行了

        // cache可只有一个实例，需要并发安全，key为fieldsId，全局唯一
        val stylesResult = StylesResult(inDarkTheme, styles)
        stylesMap.put(curFieldsId, stylesResult)


        // if editor state and theme not changed, apply theme
        // else will postpone the apply
        // 如果编辑器状态和主题没变，应用高亮结果，否则推迟应用，直到获取字段的annotated string或重新执行code editor的analyze
        if(editorState.value.fieldsId == curFieldsId && inDarkTheme == Theme.inDarkTheme) {
            doJobThenOffLoading {
                editorState.value.applySyntaxHighlighting(curFieldsId, stylesResult)
            }
        }
    }

    //received styles is full spans, not a part
    override fun updateStyles(
        sourceManager: AnalyzeManager,
        styles: Styles,
        range: StyleUpdateRange
    ) {
        if(styles != null) {
            doJobThenOffLoading {
                editorState.value.applySyntaxHighlighting(curFieldsId, styles)
            }
        }
    }

    override fun setDiagnostics(sourceManager: AnalyzeManager, diagnostics: DiagnosticsContainer?) {

    }

    override fun updateBracketProvider(sourceManager: AnalyzeManager, provider: BracketsProvider?) {

    }
}