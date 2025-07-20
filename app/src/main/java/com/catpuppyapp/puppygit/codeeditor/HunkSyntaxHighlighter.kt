package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.compose.ui.text.SpanStyle
import com.catpuppyapp.puppygit.git.PuppyHunkAndLines
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.forEachIndexedBetter
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private const val TAG = "HunkSyntaxHighlighter"

class HunkSyntaxHighlighter(
    val hunk: PuppyHunkAndLines,
) {
    val appContext: Context = AppModel.realAppContext
    var languageScope: PLScope = PLScope.AUTO
    val analyzeLock = ReentrantLock()
    var myLang: TextMateLanguage? = null


    // Don't call this on main thread
    @WorkerThread
    fun noMoreMemory() : Boolean {
        val disabledOrNoMore = hunk.diffItemSaver.syntaxDisabledOrNoMoreMem()

        if(disabledOrNoMore) {
            languageScope = PLScope.AUTO
        }

        return disabledOrNoMore
    }


    fun analyze(scope: PLScope = languageScope) {
        if(PLScope.scopeTypeInvalid(scope, autoAsInvalid = false)) {
            hunk.clearStyles()
            return
        }

        // only left two cases: auto or not
        val scope = if(scope == PLScope.AUTO) {
            PLScope.guessScopeType(hunk.diffItemSaver.fileName())
        } else {
            scope
        }

        if(noMoreMemory()) {
            return
        }


        analyzeLock.withLock {
            if(noMoreMemory()) {
                return
            }

            doAnalyzeNoLock(scope)
        }
    }

    private fun cleanLanguage() {
        TextMateUtil.cleanLanguage(myLang)
    }

    private fun doAnalyzeNoLock(scope: PLScope) {
        val text = hunk.linesToString()
        // even text is empty, still need create language object
//        if(text.isEmpty()) {
//            return
//        }

//        println("text: $text")

        // if no bug, should not trigger full syntax analyze a lot
        MyLog.w(TAG, "will run full syntax highlighting analyze")

        PLTheme.setTheme(Theme.inDarkTheme)

        cleanLanguage()

        // run new analyze
        val autoComplete = false
        val lang = TextMateLanguage.create(scope.scope, autoComplete)
        myLang = lang



        try {
            //闭包的receiver和给函数传参的是同一个实例
            TextMateUtil.setReceiverThenDoAct(lang, MyHunkStyleDelegate(this)) { receiver ->
                lang.analyzeManager.reset(ContentReference(Content(text)), Bundle(), receiver)
            }
        }catch (e: Exception) {
            // maybe will got NPE, if language changed to null by a new analyze
            MyLog.e(TAG, "#doAnalyzeNoLock() err: call `TextMateUtil.setReceiverThenDoAct()` err: fileRelativePath=${hunk.diffItemSaver.relativePathUnderRepo} err=${e.stackTraceToString()}")

        }
    }

    fun applyStyles(styles: Styles) {
        val spansReader = styles.spans.read()
        hunk.diffItemSaver.operateStylesMapWithWriteLock { styleMap ->
            hunk.lines.forEachIndexedBetter { idx, line ->
                val spans = spansReader.getSpansOnLine(idx)
                val lineStyles = mutableListOf<LineStylePart>()
                TextMateUtil.forEachSpanResult(line.getContentNoLineBreak(), spans) { range, style ->
                    lineStyles.add(LineStylePart(range, style))
                }

                styleMap.put(line.key, lineStyles)
            }
        }

        // diff页面只分析一次，无需增量分析，所以拿完对象就可以释放内存了
        cleanLanguage()
    }
}


data class LineStylePart(
    val range: IntRange,
    val style: SpanStyle,
)
