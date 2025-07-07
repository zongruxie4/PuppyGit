package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import androidx.annotation.UiThread
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.StyleUpdateRange
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
import kotlinx.coroutines.channels.Channel


private const val TAG = "MyCodeEditor"
//private val highlightMapShared: MutableMap<String, Map<String, AnnotatedStringResult>> = ConcurrentMap()
//private val stylesMapShared: MutableMap<String, StylesResult> = ConcurrentMap()


class MyCodeEditor(
    val appContext: Context,
    val plScope: MutableState<String>,
    val editorState: CustomStateSaveable<TextEditorState>,
    val styleRequestChannel: Channel<TextEditorState> = Channel(1000)
): CodeEditor(appContext) {

    //{fieldsId: syntaxHighlightId: AnnotatedString}
    val highlightMap: MutableMap<String, Map<String, AnnotatedStringResult>> = ConcurrentMap()
    val stylesMap: MutableMap<String, StylesResult> = ConcurrentMap()
//    val editorStateMap: MutableMap<String, TextEditorState> = ConcurrentMap()

    fun genNewStyleDelegate() = MyEditorStyleDelegate(this, Theme.inDarkTheme, stylesMap)
//    var editor: CodeEditor? = null

    init {
        try {

            setupTextmate(appContext)

            this.apply {
                colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
            }

            // for clear when Activity destroy
            AppModel.editorCache.add(this)

            analyze()
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

    fun obtainCachedStyles():Styles? {
        val cachedStyles = stylesMap.get(editorState.value.fieldsId)
        return if(cachedStyles != null && cachedStyles.inDarkTheme == Theme.inDarkTheme) {
            cachedStyles.styles
        }else {
            // when switched theme, maybe need remove another themes cached styles, if not clear is ok too,
            //   but, whatever, they will be cleared when exit app
            // 如果切换了app主题，可能需要清下之前主题缓存的styles，不过不清也没事，不清的好处是如果用户来回切换主题，不会反复执行代码高亮分支，清的好处是可能有助于释放内存，
            //  但不管在这清不清，都无所谓，反正退出app时一定会清
//            stylesMap.remove(editorState.fieldsId)


            null
        }
    }

    fun analyze() {
        if(SettingsUtil.isEditorSyntaxHighlightEnabled().not()) {
            return
        }

        val plScope = plScope.value
        // no highlights or not supported
        if(PLScopes.scopeInvalid(plScope)) {
            return
        }


        val editorState = editorState.value

        // invalid state, maybe just created, but never used
        if(editorState.fieldsId.isBlank()) {
            return
        }

        // has cached
        // 检查是否有cached styles，有则直接应用
        val cachedStyles = obtainCachedStyles()
        if(cachedStyles != null) {
            // 会在 style receiver收到之后立刻apply，所以这里不需要再apply了，如果有缓存就代表已经apply过了
//            doJobThenOffLoading {
//                editorState.applySyntaxHighlighting(editorState.fieldsId, cachedStyles)
//            }
            return
        }




        // do analyze
        //执行分析
        // 用editorState.getAllText()获取已\n结尾的文件，这里不要直接读取文件，避免 /r/n，可能导致解析出的索引与editor state实际使用的不匹配
        val text = editorState.getAllText()
        if(text.isEmpty()) {
            return
        }

        PLTheme.applyTheme(Theme.inDarkTheme)

        this.let {
            it.setText(text)

            val autoComplete = false
            val lang = TextMateLanguage.create(
                plScope, autoComplete
            )

            lang.analyzeManager.setReceiver(genNewStyleDelegate())

            it.setEditorLanguage(lang)
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
        if (text != null) {
            lang.getAnalyzeManager().reset(ContentReference(text), extraArguments)
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
    fun putSyntaxHighlight(fieldsId:String, highlights:Map<String, AnnotatedStringResult>) {
        highlightMap.put(fieldsId, highlights)
    }
    // 这里不用 get 而是用 obtain，是为了避免和默认的getter 名冲突
    fun obtainSyntaxHighlight(fieldsId:String):Map<String, AnnotatedStringResult>? {
        return highlightMap.get(fieldsId)
    }


    // TODO 添加修改行和删除行的函数，外部调用 code editor重新执行语法分析，然后应用style到调用的那个state
}

class StylesResult(
    val inDarkTheme: Boolean,
    val styles: Styles,
)

class AnnotatedStringResult(
    val inDarkTheme: Boolean,
    val annotatedString: AnnotatedString
)
