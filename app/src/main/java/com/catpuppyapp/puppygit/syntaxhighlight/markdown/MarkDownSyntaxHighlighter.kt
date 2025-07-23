package com.catpuppyapp.puppygit.syntaxhighlight.markdown

import android.content.Context
import android.os.Bundle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.catpuppyapp.puppygit.syntaxhighlight.MarkDownStyleReceiver
import com.catpuppyapp.puppygit.syntaxhighlight.PLScope
import com.catpuppyapp.puppygit.syntaxhighlight.TextMateUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.forEachIndexedBetter
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private const val TAG = "MarkDownSyntaxHighlighter"

class MarkDownSyntaxHighlighter(
    val text: String,
    val onFinished: (AnnotatedString) -> Unit,
) {
    val appContext: Context = AppModel.realAppContext
    val analyzeLock = ReentrantLock()
    var myLang: TextMateLanguage? = null
    val scope = PLScope.MARKDOWN

    val lines = text.lines()

    fun analyze(scope: PLScope) {
        analyzeLock.withLock {
            doAnalyzeNoLock(scope)
        }
    }

    fun release() {
        TextMateUtil.cleanLanguage(myLang)
    }

    private fun doAnalyzeNoLock(scope: PLScope) {
        // if no bug, should not trigger full syntax analyze a lot
        MyLog.w(TAG, "will run full syntax highlighting analyze(at MarkDownSyntaxHighlighter)")


        release()

        // run new analyze
        val autoComplete = false
        val lang = TextMateLanguage.create(scope.scope, autoComplete)
        myLang = lang

        try {
            //闭包的receiver和给函数传参的是同一个实例
            TextMateUtil.setReceiverThenDoAct(lang, MarkDownStyleReceiver(this)) { receiver ->
                lang.analyzeManager.reset(ContentReference(Content(text)), Bundle(), receiver)
            }
        }catch (e: Exception) {
            // maybe will got NPE, if language changed to null by a new analyze
            MyLog.e(TAG, "#doAnalyzeNoLock() err: call `TextMateUtil.setReceiverThenDoAct()` err: ${e.stackTraceToString()}")

        }
    }


}

