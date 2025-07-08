package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getRandomUUID
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
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


private const val TAG = "MyCodeEditor"
//private val highlightMapShared: MutableMap<String, Map<String, AnnotatedStringResult>> = ConcurrentMap()
//private val stylesMapShared: MutableMap<String, StylesResult> = ConcurrentMap()


class MyCodeEditor(
    val appContext: Context,
    val plScope: MutableState<String>,
    val editorState: CustomStateSaveable<TextEditorState>,
    //除非样式分析执行特别慢，否则队列不会满，也不会阻塞线程，如果真的特别慢，或许文件特别大，最好禁用语法高亮
    // except styles analyze very slow, else, the channel queue will not fullfill and will not block thread，if really very slow, may have large file, better disable syntax highlighting
    val stylesUpdateRequestChannel: Channel<StylesUpdateRequest> = Channel(2000),
    var colorScheme: EditorColorScheme = EditorColorScheme()
) { // TODO 测试不继承CodeEditor是否会报错？
//): CodeEditor(appContext) {

    var languageScope:String = ""
    var myLang: TextMateLanguage? = null
    //{fieldsId: syntaxHighlightId: AnnotatedString}
    val highlightMap: MutableMap<String, Map<String, AnnotatedStringResult>> = ConcurrentMap()
    val stylesMap: MutableMap<String, StylesResult> = ConcurrentMap()
//    val editorStateMap: MutableMap<String, TextEditorState> = ConcurrentMap()

    val stylesRequestLock = ReentrantLock(true)

    fun genNewStyleDelegate() = MyEditorStyleDelegate(this, Theme.inDarkTheme, stylesMap)
//    var editor: CodeEditor? = null

    init {
        try {

            setupTextmate(appContext)

            colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())

            colorScheme.applyDefault()

            // for clear when Activity destroy
            AppModel.editorCache.add(this)

            analyze()
        }catch (e: Exception) {
            MyLog.e(TAG, "#init err: ${e.stackTraceToString()}")
        }
    }

//    override fun release() {
//        super.release()
//        clearCache()
//    }

    fun release() {
        clearCache()
        clearStylesChannel()
    }

    fun clearCache() {
        highlightMap.clear()
        stylesMap.clear()
    }

    fun obtainCachedStyles(): StylesResult? {
        println("outter filesId: ${editorState.value.fieldsId}")
        val cachedStyles = stylesMap.get(editorState.value.fieldsId)
        return if(cachedStyles != null && cachedStyles.inDarkTheme == Theme.inDarkTheme) {
            cachedStyles
        }else {
            // when switched theme, maybe need remove another themes cached styles, if not clear is ok too,
            //   but, whatever, they will be cleared when exit app
            // 如果切换了app主题，可能需要清下之前主题缓存的styles，不过不清也没事，不清的好处是如果用户来回切换主题，不会反复执行代码高亮分支，清的好处是可能有助于释放内存，
            //  但不管在这清不清，都无所谓，反正退出app时一定会清
//            stylesMap.remove(editorState.fieldsId)


            null
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun sendUpdateStylesRequest(stylesUpdateRequest: StylesUpdateRequest) {
        stylesRequestLock.withLock {
            stylesUpdateRequest.act()
            stylesUpdateRequestChannel.trySend(stylesUpdateRequest)
        }
    }

    fun analyze(force: Boolean = false) {
        val plScope = plScope.value
        // no highlights or not supported
        if(SettingsUtil.isEditorSyntaxHighlightEnabled().not() || PLScopes.scopeInvalid(plScope)) {
            release()
            return
        }

        val scopeChanged = plScope != languageScope
        languageScope = plScope


        val editorState = editorState.value

        // invalid state, maybe just created, but never used
        if(editorState.fieldsId.isBlank()) {
            return
        }

        // has cached
        // 检查是否有cached styles，有则直接应用
        if(!force && !scopeChanged) {
            val cachedStyles = obtainCachedStyles()
            if(cachedStyles != null) {
                // 会在 style receiver收到之后立刻apply，所以这里正常不需要再apply了，但内部会检测，如果已经applied，则不会重复applied，所以这里调用也无妨
                doJobThenOffLoading {
                    editorState.applySyntaxHighlighting(editorState.fieldsId, cachedStyles)
                }
                return
            }
        }




        // do analyze
        //执行分析
        // 用editorState.getAllText()获取已\n结尾的文件，这里不要直接读取文件，避免 /r/n，可能导致解析出的索引与editor state实际使用的不匹配
        val text = editorState.getAllText()
        if(text.isEmpty()) {
            return
        }

//        println("text: $text")

        MyLog.d(TAG, "will run full syntax highlighting analyze")
        PLTheme.applyTheme(Theme.inDarkTheme)

        this.let {
            clearStylesChannel()
            sendUpdateStylesRequest(StylesUpdateRequest(ignoreThis = true, editorState, {}))
            sendUpdateStylesRequest(StylesUpdateRequest(ignoreThis = false, editorState, {}))


            val autoComplete = false
            val lang = if(myLang == null || scopeChanged) {
                TextMateLanguage.create(plScope, autoComplete).let { myLang = it; it }
            }else {
                myLang!!
            }

            lang.isAutoCompleteEnabled = false
            lang.tabSize = 0

            lang.analyzeManager.setReceiver(genNewStyleDelegate())
            lang.analyzeManager.reset(ContentReference(Content(text)), Bundle())

//            it.setEditorLanguage(lang)
//            it.setText(text)

        }
    }

    private fun clearStylesChannel() {
        stylesRequestLock.withLock {
            while (stylesUpdateRequestChannel.tryReceive().isSuccess) {}
        }
    }

//    override fun setEditorLanguage(lang: Language?) {
//        var lang = lang
//        if (lang == null) {
//            lang = EmptyLanguage()
//        }
//
//        // Destroy old one
//        val old: Language? = editorLanguage
//        if (old != null) {
//            val formatter = old.getFormatter()
//            formatter.setReceiver(null)
//            formatter.destroy()
//            old.getAnalyzeManager().setReceiver(null)
//            old.getAnalyzeManager().destroy()
//            old.destroy()
//        }
//
//        myLang = lang
//        this.diagnostics = null
//
//        // Setup new one
//        if (text != null) {
//            lang.getAnalyzeManager().reset(ContentReference(text), extraArguments)
//        }
//
//        if (snippetController != null) {
//            snippetController.stopSnippet()
//        }
//        renderContext.invalidateRenderNodes()
//        invalidate()
//    }




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
    val from: StylesResultFrom,
    val uniqueId: String = getRandomUUID(),
)

enum class StylesResultFrom {
    CODE_EDITOR,
    TEXT_STATE,
}


class AnnotatedStringResult(
    val inDarkTheme: Boolean,
    val annotatedString: AnnotatedString
)

class StylesUpdateRequest(
    // 很多时候需要对同一个state先执行删除，再执行新增，分别会调用两次增量更新，这时，忽略前面的操作，只响应最后一个
    val ignoreThis: Boolean,
    val targetEditorState: TextEditorState,
    val act:()->Unit,
)
