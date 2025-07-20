package com.catpuppyapp.puppygit.codeeditor

import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange
import io.github.rosemoe.sora.lang.brackets.BracketsProvider
import io.github.rosemoe.sora.lang.diagnostic.DiagnosticsContainer
import io.github.rosemoe.sora.lang.styling.Styles

private const val TAG = "MyHunkStyleDelegate"

class MyHunkStyleDelegate(
    val highlighter: HunkSyntaxHighlighter,
): StyleReceiver {
    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?) {
        setStyles(sourceManager, styles, null)
    }

    override fun setStyles(sourceManager: AnalyzeManager, styles: Styles?, action: Runnable?) {

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
