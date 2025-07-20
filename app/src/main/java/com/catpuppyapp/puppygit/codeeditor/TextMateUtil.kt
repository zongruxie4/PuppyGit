package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextFieldState
import com.catpuppyapp.puppygit.utils.MyLog
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.styling.Span
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.util.RendererUtils
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme


private const val TAG = "TextMateInit"

object TextMateUtil {
    private var inited = false

    // colorScheme is shareable
    var colorScheme: EditorColorScheme = EditorColorScheme()
        private set

    // only need init once
    fun doInit(appContext: Context) {
        if(inited) {
            return
        }

        inited = true

        try {
            setupTextmate(appContext)


            colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())

            // 必须调用，不然没颜色
            colorScheme.applyDefault()
        }catch (e: Exception) {
            inited = false
            MyLog.e(TAG, "$TAG#doInit err: ${e.stackTraceToString()}")
        }
    }


    /**
     * Setup Textmate. Load our grammars and themes from assets
     */
    private fun setupTextmate(appContext: Context) {
        // Add assets file provider so that files in assets can be loaded
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(
                appContext.assets // use application context
            )
        )

        PLTheme.loadDefaultTextMateThemes()
        loadDefaultTextMateLanguages()
    }



    /**
     * Load default languages from JSON configuration
     *
     * @see loadDefaultLanguagesWithDSL Load by Kotlin DSL
     */
    private /*suspend*/ fun loadDefaultTextMateLanguages() /*= withContext(Dispatchers.Main)*/ {
        GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
    }


    fun setReceiverThenDoAct(
        language: Language?,
        receiver: StyleReceiver,
        act: (StyleReceiver)->Unit,
    ) {
        language?.analyzeManager?.setReceiver(receiver)
        act(receiver)
    }


    fun cleanLanguage(language: Language?) {
        if (language != null) {
            val formatter = language.getFormatter()
            formatter.setReceiver(null)
            formatter.destroy()
            language.getAnalyzeManager().setReceiver(null)
            language.getAnalyzeManager().destroy()
            language.destroy()
        }
    }


    fun forEachSpanResult(rawText: String, spans:List<Span>, foreach:(range: IntRange, spanStyle: SpanStyle) -> Unit) {
        var start = 0
        var spanIdx = 1
        while (spanIdx <= spans.size) {
            val curSpan = spans.get(spanIdx - 1)
            val nextSpan = spans.getOrNull(spanIdx++)
            val endExclusive = nextSpan?.column ?: rawText.length
            val textRange = IntRange(start, endExclusive - 1)
            // don't check, let'em throw if have any err
//                if(textRange.start < 0 || textRange.endInclusive >= rawText.length) {
//                    continue
//                }
            start = endExclusive
            val style = curSpan.style
            val foregroundColor = Color(RendererUtils.getForegroundColor(curSpan, TextMateUtil.colorScheme))
//                println("forecolor = ${RendererUtils.getForegroundColor(curSpan, colorScheme)}")

            // disable for avoid bg color conflicts when editor's merge mode on
            //   (but, actually, I never have seen this bg colors, maybe most time is transparency)
//                val backgroundColor = Color(RendererUtils.getBackgroundColor(curSpan, colorScheme))
            val fontWeight = if(TextStyle.isBold(style)) FontWeight.Bold else null
            val fontStyle = if(TextStyle.isItalics(style)) FontStyle.Italic else null

            foreach(
                textRange,
                SpanStyle(color = foregroundColor, fontStyle = fontStyle, fontWeight = fontWeight)
            )
        }
    }
}

