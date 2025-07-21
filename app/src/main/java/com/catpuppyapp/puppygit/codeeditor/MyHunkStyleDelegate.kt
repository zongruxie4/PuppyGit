package com.catpuppyapp.puppygit.codeeditor

import io.github.rosemoe.sora.lang.styling.Styles

private const val TAG = "MyHunkStyleDelegate"

class MyHunkStyleDelegate(
    val highlighter: HunkSyntaxHighlighter,
) : MyStyleReceiver(TAG, highlighter.myLang?.analyzeManager) {

    override fun handleStyles(styles: Styles) {
        highlighter.applyStyles(styles)
    }

}
