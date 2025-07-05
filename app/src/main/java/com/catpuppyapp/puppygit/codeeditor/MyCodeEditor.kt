package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.cache.EditorStylesCache
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.CodeEditor


class MyCodeEditor(
    appContext: Context,
    val myEditorStyleDelegate: MyEditorStyleDelegate
): CodeEditor(appContext) {
    private val TAG = "MyCodeEditor"

//    var editor: CodeEditor? = null

    fun init(appContext: Context, inDarkTheme: Boolean) {
        try {
            // order is important
            this.apply {
                colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            }

            PLTheme.init(inDarkTheme)

            setupTextmate(appContext)

        }catch (e: Exception) {
            MyLog.e(TAG, "#init err: ${e.stackTraceToString()}")
        }
    }

    fun analyze(plScope:String, editorState: TextEditorState) {
        if(plScope == PLScopes.NONE || PLScopes.SCOPES.contains(plScope).not()) {
            return
        }

        // 检查是否有cached styles，有则直接应用
        val cachedStyles = EditorStylesCache.getByType<Styles>(editorState.fieldsId)
        if(cachedStyles != null) {
            editorState.applySyntaxHighlighting(editorState.fieldsId, cachedStyles)
            return
        }

        //执行分析
        // 用editorState.getAllText()获取已\n结尾的文件，这里不要直接读取文件，避免 /r/n，可能导致解析出的索引与editor state实际使用的不匹配
        val text = editorState.getAllText()
        this.let {
            it.setEditorLanguage(
                TextMateLanguage.create(
                    plScope, false
                )
            )
            it.setText(text)
        }
    }

    override fun setEditorLanguage(lang: Language?) {
        var lang = lang
        if (lang == null) {
            lang = EmptyLanguage()
        }

        // Destroy old one
        val old: Language? = editorLanguage
        if (old != null) {
            val formatter = old.getFormatter()
            formatter.setReceiver(null)
            formatter.destroy()
            old.getAnalyzeManager().setReceiver(null)
            old.getAnalyzeManager().destroy()
            old.destroy()
        }

        this.diagnostics = null

        // Setup new one
        val mgr = lang.getAnalyzeManager()
        mgr.setReceiver(myEditorStyleDelegate)
        if (text != null) {
            mgr.reset(ContentReference(text), extraArguments)
        }

        if (snippetController != null) {
            snippetController.stopSnippet()
        }
        renderContext.invalidateRenderNodes()
        invalidate()
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

}
