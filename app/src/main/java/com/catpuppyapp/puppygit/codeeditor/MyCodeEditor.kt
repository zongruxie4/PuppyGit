package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.forEachBetter
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
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
import io.ktor.util.collections.ConcurrentMap


private const val TAG = "MyCodeEditor"

class MyCodeEditor(
    val appContext: Context,
    val inDarkTheme: Boolean,
    val plScope: MutableState<String>,
    val editorState: CustomStateSaveable<TextEditorState>,
): CodeEditor(appContext) {

    //{fieldsId: syntaxHighlightId: AnnotatedString}
    val highlightMap: MutableMap<String, Map<String, AnnotatedString>> = ConcurrentMap()
    val stylesMap: MutableMap<String, Styles> = ConcurrentMap()
    val myEditorStyleDelegate: MyEditorStyleDelegate = MyEditorStyleDelegate(editorState, stylesMap)

//    var editor: CodeEditor? = null

    init {
        try {
            // order is important
            this.apply {
                colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            }

            PLTheme.init(inDarkTheme)

            setupTextmate(appContext)

            AppModel.editorCache.add(this)

        }catch (e: Exception) {
            MyLog.e(TAG, "#init err: ${e.stackTraceToString()}")
        }
    }

    override fun release() {
        super.release()
        clearCache()
    }

    fun clearCache() {
        highlightMap.clear()
        stylesMap.clear()
    }

    fun analyze(editorState: TextEditorState) {
        if(SettingsUtil.isEditorSyntaxHighlightEnabled().not()) {
            return
        }

        val plScope = plScope.value
        // no highlights or not supported
        if(plScope == PLScopes.NONE || PLScopes.SCOPES.contains(plScope).not()) {
            return
        }

        // 检查是否有cached styles，有则直接应用
        val cachedStyles = stylesMap.get(editorState.fieldsId)
        if(cachedStyles != null) {
            doJobThenOffLoading {
                editorState.applySyntaxHighlighting(editorState.fieldsId, cachedStyles)
            }
            return
        }

        //执行分析
        // 用editorState.getAllText()获取已\n结尾的文件，这里不要直接读取文件，避免 /r/n，可能导致解析出的索引与editor state实际使用的不匹配
        val text = editorState.getAllText()
        if(text.isEmpty()) {
            return
        }

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
        // avoid throw NPE when parent class `CodeEditor.initialize()` running
        if(myEditorStyleDelegate != null) {
            myEditorStyleDelegate.reset()
            mgr.setReceiver(myEditorStyleDelegate)
        }

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




    // map key String is field's `syntaxHighlightId`, 让highlights map在外部，方便切换语法高亮方案时清空
    fun putSyntaxHighlight(fieldsId:String, highlights:Map<String, AnnotatedString>) {
        highlightMap.put(fieldsId, highlights)
    }
    // 这里不用 get 而是用 obtain，是为了避免和默认的getter 名冲突
    fun obtainSyntaxHighlight(fieldsId:String):Map<String, AnnotatedString>? {
        return highlightMap.get(fieldsId)
    }


    // TODO 添加修改行和删除行的函数，外部调用 code editor重新执行语法分析，然后应用style到调用的那个state
}
