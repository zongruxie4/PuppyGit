package com.catpuppyapp.puppygit.syntaxhighlight.base

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.catpuppyapp.puppygit.utils.MyLog
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.styling.Span
import io.github.rosemoe.sora.lang.styling.TextStyle
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.util.RendererUtils
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import org.eclipse.tm4e.core.registry.IThemeSource


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
                .apply {
                    // 必须调用，不然没颜色
                    applyDefault()
                }
        }catch (e: Exception) {
            inited = false
            MyLog.e(TAG, "$TAG#doInit err: ${e.stackTraceToString()}")
        }
    }

    // will call this method after app theme changed,
    //   so if we forget apply theme before analyzing,
    //   it still will use the right color theme
    fun updateTheme(inDarkTheme:Boolean) {
        PLTheme.setTheme(inDarkTheme)
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

        loadDefaultTextMateThemes()
        loadDefaultTextMateLanguages()
    }

    /**
     * Load default textmate themes
     */
    /*suspend*/ fun loadDefaultTextMateThemes() /*= withContext(Dispatchers.IO)*/ {
        val themes = PLTheme.THEMES
        val themeRegistry = ThemeRegistry.getInstance()
        themes.forEach { name ->
            val path = "textmate/$name.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(path), path, null
                    ), name
                ).apply {
                    isDark = name != PLTheme.THEME_LIGHT
                }
            )
        }
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
            val foregroundColor = Color(RendererUtils.getForegroundColor(curSpan, colorScheme))
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

