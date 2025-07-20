package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import com.catpuppyapp.puppygit.utils.MyLog
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
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

}

