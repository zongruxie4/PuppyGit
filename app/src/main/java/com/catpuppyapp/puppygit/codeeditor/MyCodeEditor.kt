package com.catpuppyapp.puppygit.codeeditor

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.screen.shared.FilePath
import com.catpuppyapp.puppygit.screen.shared.FuckSafFile
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.appAvailHeapSizeInMb
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getRandomUUID
import com.catpuppyapp.puppygit.utils.isLocked
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.StyleReceiver
import io.github.rosemoe.sora.lang.styling.Styles
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.absoluteValue


// if app available memory lower than `lowestMemInMb` in `lowestMemLimitCount` times,
//  will disable syntax highlighting and free related memory,
//  else, app may crash by OOM
// 如果app可用内存连续`lowestMemLimitCount`次小于`lowestMemInMb`，将禁用语法高亮并释放相关内存，
//   否则，app可能会因OOM而崩溃，会导致用户正在编辑的文件数据丢失，不只是未保存内容丢失，
//   源文件可能会在写入时进程被杀，导致写入终止，所以源文件可能损坏或丢失比预期更多的内容。
private const val lowestMemInMb = 30
private const val lowestMemLimitCount = 3


private const val TAG = "MyCodeEditor"
//private val highlightMapShared: MutableMap<String, Map<String, AnnotatedStringResult>> = ConcurrentMap()
//private val stylesMapShared: MutableMap<String, StylesResult> = ConcurrentMap()


class MyCodeEditor(
    val appContext: Context,
    // this will bind code editor
    val editorState: CustomStateSaveable<TextEditorState>,
    // code editor will bind this at init block
    val undoStack: CustomStateSaveable<UndoStack>,
    val plScope: MutableState<PLScope>,
) {
    private var lowMemCount:Int = 0
    private var file: FuckSafFile = FuckSafFile(appContext, FilePath(""))

    var colorScheme: EditorColorScheme = EditorColorScheme()
        private set

    internal var latestStyles: StylesResult? = null


    var languageScope: PLScope = PLScope.NONE
        private set

    var myLang: TextMateLanguage? = null

    //{fieldsId: syntaxHighlightId: AnnotatedString}
    // a fieldsId+syntaxHighlightId can located a AnnotatedString
    // 一个fieldsId+syntaxHighlightId定位一个AnnotatedString
    val highlightMap: MutableMap<String, Map<String, AnnotatedStringResult>> = ConcurrentMap()
    //{fieldsId: StylesResult}
    val stylesMap: MutableMap<String, StylesResult> = ConcurrentMap()
//    val editorStateMap: MutableMap<String, TextEditorState> = ConcurrentMap()

    private val stylesRequestLock = ReentrantLock(true)
    private val analyzeLock = ReentrantLock(true)
//    val stylesApplyLock = ReentrantLock(true)

    private val delayAnalyzingTaskLock = Mutex()

    val textEditorStateOnChangeLock = Mutex()

    private fun genNewStyleDelegate(editorState: TextEditorState?) = MyEditorStyleDelegate(this, Theme.inDarkTheme, stylesMap, editorState, languageScope)

    companion object {
        private var inited = false

        // only need init once
        fun doInit(appContext: Context) {
            if(inited) {
                return
            }

            inited = true

            try {
                setupTextmate(appContext)
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



        private fun setReceiverThenDoAct(
            language: Language?,
            receiver: MyEditorStyleDelegate,
            act: (StyleReceiver)->Unit,
        ) {
            try {
                language?.analyzeManager?.setReceiver(receiver)
                act(receiver)
            }catch (e: Exception) {
                // maybe will got NPE, if language changed to null by a new analyze
                MyLog.e(TAG, "#setReceiverThenDoAct() err: targetFieldsId=${receiver.editorState?.fieldsId}, err=${e.stackTraceToString()}")

            }
        }

    }

    init {
        doInit(appContext)

        undoStack?.value?.codeEditor = this

        colorScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())

        // 必须调用，不然没颜色
        colorScheme.applyDefault()

        // for clear when Activity destroy
        AppModel.editorCache.add(this)

        analyze()
    }


    fun noMoreMemory() : Boolean {
        if(appAvailHeapSizeInMb() < lowestMemInMb) {
            lowMemCount++
        }else {
            lowMemCount = 0
        }

        return if(lowMemCount >= lowestMemLimitCount) {
            resetAllPlScopes()

            release()
            Msg.requireShowLongDuration("Syntax highlighting disabled: No more memory!")

            true
        }else {
            false
        }
    }


    private fun resetAllPlScopes() {
        // state
        resetPlScope()

        // current Language used pl scope
        languageScope = PLScope.NONE
    }


    // reset plScope state
    // 重置 plScope state，影响编辑器语法高亮弹窗选中的语言
    fun resetPlScope() {
        PLScope.resetPlScope(plScope)
    }

    fun updatePlScopeThenAnalyze() {
        PLScope.updatePlScopeIfNeeded(plScope, file.name)
        // call this is safe, if have cached styles, will not re-analyze
        analyze()
    }

//    fun updatePlScope(newScope: PLScope) {
//        plScope.value = newScope
//    }
//
//    fun currentPlScope() = plScope.value

    fun release() {
        cleanLanguage()
        highlightMap.clear()
        stylesMap.clear()
    }


    fun reset(newFile: FuckSafFile, force: Boolean) {
        lowMemCount = 0

        if(force.not() && newFile.path.ioPath == file.path.ioPath) {
            return
        }

        resetPlScope()
        resetFile(newFile)
        release()
    }

    private fun resetFile(newFile: FuckSafFile) {
        file = newFile
    }

    fun obtainCachedStyles(editorState: TextEditorState): StylesResult? {
        val targetFieldsId = editorState.fieldsId
        val cachedStyles = stylesMap.get(targetFieldsId)
        return if(isGoodStyles(cachedStyles, editorState)) {
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

    private fun plScopeStateInvalid() = PLScope.scopeInvalid(plScope.value.scope)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun sendUpdateStylesRequest(stylesUpdateRequest: StylesUpdateRequest, language: Language? = myLang) {
        if(plScopeStateInvalid()) {
            return
        }

        stylesRequestLock.withLock {
            if(plScopeStateInvalid()) {
                return
            }

            if(noMoreMemory()) {
                return
            }

            val targetEditorState = if(stylesUpdateRequest.ignoreThis) {
                null
            }else {
                stylesUpdateRequest.targetEditorState
            }


            // updated: 修改了 sora editor相关代码，现在在执行操作前先设置styles receiver就可确保大概率收到新样式的是当前操作执行前设置的receiver
            // x 这个可能导致问题，想象一下：为textEditorState实例1设置了receiver，然后执行分析，
            //   收到结果前，又为textEditorState实例2设置了receiver，
            //   这时，textEditorState实例2就可能会收到1的分析结果，这个分析结果就会错误绑定到实例2，实例1就没样式了
            //   但是，及时如此，也比用channel靠谱。
            //   最好的解决方案：应该让receiver把执行分析时关联的text editor state实例绑定上，或者，只创建一个editor state实例，
            //   后者可以实现，但需要修改撤销机制，因为目前的撤销机制是直接存储整个editor实例，如果改成只存fields，就行了，
            //   不对，好像还是无法解决哪个styles的结果和哪个editor state实例关联的问题。。。。。。。
            setReceiverThenDoAct(language, genNewStyleDelegate(targetEditorState), stylesUpdateRequest.act)
        }
    }

    fun getLatestStylesResultIfMatch(editorState: TextEditorState) = if(isGoodStyles(latestStyles, editorState)) latestStyles else null

    // bug: if user input very fast, and the analyzing very slow, will trigger
    //   full text analyzing till user stop input and wait the
    //   last analyzing finished, usually happened when user open a large file.
    // 有缺陷，如果用户输入很快，软件响应很慢，增量更新会跟不上，然后无限触发全量更新，直到用户停下来，等待一次完整分析结束，
    //   这种情况一般会在用户打开大文件时发生
    fun analyze(
        editorState: TextEditorState? = this.editorState?.value,
        plScope: PLScope = this.plScope.value,
    ) {
        if(editorState == null) {
            return
        }

        if(plScopeStateInvalid()) {
            return
        }

        if(noMoreMemory()) {
            return
        }


        analyzeLock.withLock {
            doAnalyzeNoLock(editorState, plScope)
        }
    }

    private fun doAnalyzeNoLock(
        editorState: TextEditorState,
        plScope: PLScope,
    ) {
        val scopeChanged = plScope != languageScope
        languageScope = plScope

        // no highlights or not supported
//        if(SettingsUtil.isEditorSyntaxHighlightEnabled().not() || PLScope.scopeInvalid(plScope.scope)) {
        if(PLScope.scopeInvalid(plScope.scope)) {
            release()
            return
        }

        // invalid state, maybe just created, but never used
        if(editorState.fieldsId.isBlank()) {
            return
        }



        // has cached
        // 检查是否有cached styles，有则直接应用
        if(scopeChanged) {
            release()
        } else {
            val cachedStyles = obtainCachedStyles(editorState)
            if(cachedStyles != null) {
                // 会在 style receiver收到之后立刻apply，所以这里正常不需要再apply了，但内部会检测，如果已经applied，则不会重复applied，所以这里调用也无妨
                doJobThenOffLoading {
                    editorState.applySyntaxHighlighting(cachedStyles)
                }
                return
            }
        }



        // do analyze
        //执行分析
        // 用editorState.getAllText()获取已\n结尾的文件，这里不要直接读取文件，避免 /r/n，可能导致解析出的索引与editor state实际使用的不匹配
        val text = editorState.getAllText()
        // even text is empty, still need create language object
//        if(text.isEmpty()) {
//            return
//        }

//        println("text: $text")

        // if no bug, should not trigger full syntax analyze a lot
        MyLog.w(TAG, "will run full syntax highlighting analyze")

        PLTheme.applyTheme(Theme.inDarkTheme)

        this.let {
//            clearStylesChannel()
//            sendUpdateStylesRequest(StylesUpdateRequest(ignoreThis = true, editorState, {}))
//            sendUpdateStylesRequest(StylesUpdateRequest(ignoreThis = false, editorState, {}))



            // Destroy old one
            cleanLanguage()


            // run new analyze
            val autoComplete = false
            val lang = TextMateLanguage.create(plScope.scope, autoComplete)
            myLang = lang

            //BEGIN: don't enable these lines, may cause parse err
            // x wrong) whatever, we don't use it's indent enter pressed feature
            // x 理解错了好像) 这些值用来计算缩进的，也可能会按回车有关，比如回车时自动补全注释星号，但我这里不用，所以无所谓，能关则关，减少执行无意义操作
//            lang.isAutoCompleteEnabled = false  // enable this is fine, and can avoid store identifiers for auto complete (reduce mem use and improve performance), but, already disabled it when create Language, so don't need disable again at here
//            lang.tabSize = 0  // enable this maybe will cause err when open git config file or other file which have tab size not equals to 0 spaces? I am not sure.
            //END: don't enable these lines, may cause parse err

            // must set receiver, then do act, else the result will sent to unrelated editor state
            sendUpdateStylesRequest(
                stylesUpdateRequest = StylesUpdateRequest(
                    ignoreThis = false,
                    targetEditorState = editorState,
                    act = { styleReceiver -> lang.analyzeManager.reset(ContentReference(Content(text)), Bundle(), styleReceiver) }
                ),
                language = lang
            )
//            sendUpdateStylesRequest(StylesUpdateRequest(ignoreThis = false, editorState, {}))

//            it.setEditorLanguage(lang)
//            it.setText(text)

        }
    }

    private fun cleanLanguage() {
        latestStyles = null
        val old: Language? = myLang
        myLang = null

        if (old != null) {
            val formatter = old.getFormatter()
            formatter.setReceiver(null)
            formatter.destroy()
            old.getAnalyzeManager().setReceiver(null)
            old.getAnalyzeManager().destroy()
            old.destroy()
        }
    }

//    private fun clearStylesChannel() {
////        stylesRequestLock.withLock {
//////            while (stylesUpdateRequestChannel.tryReceive().isSuccess) {}
////        }
//    }

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







    // map key String is field's `syntaxHighlightId`, 让highlights map在外部，方便切换语法高亮方案时清空
    fun putSyntaxHighlight(fieldsId:String, highlights:Map<String, AnnotatedStringResult>) {
        highlightMap.put(fieldsId, highlights)
    }
    // 这里不用 get 而是用 obtain，是为了避免和默认的getter 名冲突
    fun obtainSyntaxHighlight(fieldsId:String):Map<String, AnnotatedStringResult>? {
        return highlightMap.get(fieldsId)
    }

    fun releaseAndRemoveSelfFromCache() {
        release()
        AppModel.editorCache.remove(this)
    }

    // user stop input after `delayInSec`, will start a syntax highlighting analyze
    // set checkTimes to control check how many times, that should not too large, else may have many tasks, that's bad
    fun startAnalyzeWhenUserStopInputForAWhile(initState: TextEditorState, delayInSec: Int = 2, checkTimes: Int = 5) {
        if(plScopeStateInvalid()) {
            return
        }

        doJobThenOffLoading task@{
            if(isLocked(delayAnalyzingTaskLock)) {
                return@task
            }

            delayAnalyzingTaskLock.withLock {
                var initState = initState
                var count = 0
                while (count++ < checkTimes) {
                    delay(delayInSec * 1000L)
                    if(plScopeStateInvalid()) {
                        break
                    }

                    if(editorState != null && editorState.value.fieldsId.let { it.isNotBlank() && it == initState.fieldsId }) {
                        analyze(editorState.value)
                        break
                    }else {
                        initState = editorState?.value ?: break
                    }
                }
            }
        }
    }

    fun cleanStylesByFieldsIdList(fieldsIdList: List<String>) {
        for (fieldsId in fieldsIdList) {
            cleanStylesByFieldsId(fieldsId)
        }
    }

    fun cleanStylesByFieldsId(fieldsId: String) {
        stylesMap.remove(fieldsId)
        highlightMap.remove(fieldsId)
    }


}

data class StylesResult(
    val inDarkTheme: Boolean,
    val styles: Styles,
    val from: StylesResultFrom,
    val uniqueId: String = getRandomUUID(),
    val fieldsId:String,
    val languageScope: PLScope,
    val applied: AtomicBoolean = AtomicBoolean(false)
) {
    fun copyForEditorState(newFieldsId: String) = copy(
        styles = styles.copy(),
        from = StylesResultFrom.TEXT_EDITOR_STATE,
        uniqueId = getRandomUUID(),
        fieldsId = newFieldsId,
        applied = AtomicBoolean(false)
    )

    fun copyWithDeepCopyStyles() = copy(styles = this.styles.copy())
}

enum class StylesResultFrom {
    CODE_EDITOR,
    TEXT_EDITOR_STATE,
}


class AnnotatedStringResult(
    val inDarkTheme: Boolean,
    val annotatedString: AnnotatedString
)

class StylesUpdateRequest(
    // 很多时候需要对同一个state先执行删除，再执行新增，分别会调用两次增量更新，这时，忽略前面的操作，只响应最后一个
    val ignoreThis: Boolean,
    val targetEditorState: TextEditorState,
    val act:(StyleReceiver)->Unit,
)

fun MyCodeEditor?.scopeInvalid() = this == null || PLScope.scopeInvalid(languageScope.scope)


fun MyCodeEditor?.scopeMatched(scope: String?) = this != null && scope != null && languageScope.scope == scope && !this.scopeInvalid()

fun MyCodeEditor?.isGoodStyles(stylesResult: StylesResult?, editorState: TextEditorState):Boolean {
    return !(this == null || stylesResult == null || stylesResult.fieldsId != editorState.fieldsId
            || stylesResult.inDarkTheme != Theme.inDarkTheme

            // this check is a guess-check, maybe something wrong, or may not, but,
            // usually lineCount and fields.size's difference count should less than 3,
            // and, if have some deadly bug, it will fault when apply,
            // then a re-analyzing will start when TextEditorState aware it
            || (stylesResult.styles.spans.lineCount - editorState.fields.size).absoluteValue > 5

            || !this.scopeMatched(stylesResult.languageScope.scope)
    )
}
