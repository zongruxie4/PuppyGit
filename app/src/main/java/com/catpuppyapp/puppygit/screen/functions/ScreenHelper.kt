package com.catpuppyapp.puppygit.screen.functions

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ClipboardManager
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.LineNum
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.shared.FileChooserType
import com.catpuppyapp.puppygit.screen.shared.FilePath
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.generateRandomString
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.withMainContext
import kotlinx.coroutines.CoroutineScope
import java.io.File

private const val TAG = "ScreenHelper"

fun goToFileHistory(filePath: FilePath, activityContext: Context){
    goToFileHistory(filePath.toFuckSafFile(activityContext).canonicalPath, activityContext)
}

fun goToFileHistory(fileFullPath:String, activityContext: Context){
    doJobThenOffLoading job@{
        try {
            val repo = Libgit2Helper.findRepoByPath(fileFullPath)
            if(repo == null) {
                Msg.requireShow(activityContext.getString(R.string.no_repo_found))
                return@job
            }


            // file is belong a repo, but need check is under .git folder
            repo.use {
                //检查文件是否在.git目录下，若在，直接返回，此目录下的文件无历史记录
                val repoGitDirEndsWithSlash = Libgit2Helper.getRepoGitDirPathNoEndsWithSlash(it) + Cons.slash
                if(fileFullPath.startsWith(repoGitDirEndsWithSlash)) {
                    Msg.requireShowLongDuration(activityContext.getString(R.string.err_file_under_git_dir))
                    return@job
                }


                val repoWorkDirPath = Libgit2Helper.getRepoWorkdirNoEndsWithSlash(it)
                val relativePath = Libgit2Helper.getRelativePathUnderRepo(repoWorkDirPath, fileFullPath)
                if(relativePath == null) {  // this should never happen, cuz go to file history only available for file, not for dir, and this only happens when the realFullPath = repoWorkDirPath
                    Msg.requireShow(activityContext.getString(R.string.path_not_under_repo))
                    return@job
                }

                val repoDb = AppModel.dbContainer.repoRepository
                val repoFromDb = repoDb.getByFullSavePath(
                    repoWorkDirPath,
                    onlyReturnReadyRepo = false,  // if not ready, cant open at upside code, so reached here, no need more check about ready, is 100% ready
                    requireSyncRepoInfoWithGit = false,  // no need
                )

                if(repoFromDb == null) {
                    Msg.requireShowLongDuration(activityContext.getString(R.string.plz_import_repo_then_try_again))
                    return@job
                }

                goToFileHistoryByRelativePathWithMainContext(repoFromDb.id, relativePath)
            }

        }catch (e:Exception) {
            Msg.requireShowLongDuration(e.localizedMessage?:"err")
            MyLog.e(TAG, "#goToFileHistory err: ${e.stackTraceToString()}")
        }
    }
}

suspend fun goToFileHistoryByRelativePathWithMainContext(repoId:String, relativePathUnderRepo:String) {
    withMainContext {
        //go to file history page
        naviToFileHistoryByRelativePath(repoId, relativePathUnderRepo)
    }
}

fun naviToFileHistoryByRelativePath(repoId:String, relativePathUnderRepo:String) {
    Cache.set(Cache.Key.fileHistory_fileRelativePathKey, relativePathUnderRepo)
    //go to file history page
    AppModel.navController
        .navigate(
            Cons.nav_FileHistoryScreen + "/" + repoId
        )
}

fun getLoadText(loadedCount:Int, actuallyEnabledFilterMode:Boolean, activityContext:Context):String?{
    return if(loadedCount < 1){
        null
    }else if(actuallyEnabledFilterMode) {
        replaceStringResList(activityContext.getString(R.string.item_count_n), listOf(""+loadedCount))
    }else {
        replaceStringResList(activityContext.getString(R.string.loaded_n), listOf(""+loadedCount))
    }
}

fun getClipboardText(clipboardManager:ClipboardManager):String? {
    return try {
//       // or `clipboardManager.getText()?.toString()`
        clipboardManager.getText()?.text
    }catch (e:Exception) {
        MyLog.e(TAG, "#getClipboardText err: ${e.localizedMessage}")
        null
    }
}

fun openFileWithInnerSubPageEditor(filePath:String, mergeMode:Boolean, readOnly:Boolean) {
    Cache.set(Cache.Key.subPageEditor_filePathKey, filePath)
    val goToLine = LineNum.lastPosition
    val initMergeMode = if(mergeMode) "1" else "0"
    val initReadOnly = if(readOnly) "1" else "0"

    AppModel.subEditorPreviewModeOnWhenDestroy.value = false

    AppModel.navController.navigate(Cons.nav_SubPageEditor + "/$goToLine/$initMergeMode/$initReadOnly")
}


fun fromTagToCommitHistory(fullOid:String, shortName:String, repoId:String){
    //点击条目跳转到分支的提交历史记录页面
    Cache.set(Cache.Key.commitList_fullOidKey, fullOid)
    Cache.set(Cache.Key.commitList_shortBranchNameKey, shortName)  //short hash or tag name or branch name
    val useFullOid = "1"
    val isHEAD = "0"
    AppModel.navController.navigate(Cons.nav_CommitListScreen + "/" + repoId + "/" +useFullOid  + "/" + isHEAD)
}



// topbar title text double-click functions start

fun defaultTitleDoubleClick(coroutineScope: CoroutineScope, listState: LazyListState, lastPosition: MutableState<Int>)  {
    UIHelper.switchBetweenTopAndLastVisiblePosition(coroutineScope, listState, lastPosition)
}

fun defaultTitleDoubleClick(coroutineScope: CoroutineScope, listState: ScrollState, lastPosition: MutableState<Int>)  {
    UIHelper.switchBetweenTopAndLastVisiblePosition(coroutineScope, listState, lastPosition)
}

fun defaultTitleDoubleClickRequest(pageRequest: MutableState<String>) {
    pageRequest.value = PageRequest.switchBetweenTopAndLastPosition
}

// topbar title text double-click functions end

fun maybeIsGoodKeyword(keyword:String) : Boolean {
    return keyword.isNotEmpty()
}

fun filterModeActuallyEnabled(filterOn:Boolean, keyword: String):Boolean {
    return filterOn && maybeIsGoodKeyword(keyword)
}

fun <T> search(
    src:List<T>,
    match:(srcIdx:Int, srcItem:T)->Boolean,
    matchedCallback:(srcIdx:Int, srcItem:T)->Unit,
    canceled:()->Boolean
) {
    for(idx in src.indices){
        if(canceled()) {
            return
        }

        val it = src[idx]

        if(match(idx, it)) {
            matchedCallback(idx, it)
        }
    }
}

/**
 * 先遍历当前目录所有条目，然后再继续遍历当前目录的文件夹（广度优先）
 */
fun recursiveBreadthFirstSearch(
    dir: File, target:MutableList<FileItemDto>,
    match: (srcIdx:Int, srcItem: File) -> Boolean,
    matchedCallback: (srcIdx:Int, srcItem: File) -> Unit,
    canceled: () -> Boolean
) {
    if(canceled()) {
        return
    }

    val files = dir.listFiles()
    if(files == null || files.isEmpty()) {
        return
    }

    val subdirs = mutableListOf<File>()
    for(idx in files.indices) {
        if(canceled()) {
            return
        }

        val f = files[idx]

        if(match(idx, f)) {
            matchedCallback(idx, f)
        }

        if(f.isDirectory) {
            subdirs.add(f)
        }
    }

    //遍历子目录
    for(sub in subdirs) {
        recursiveBreadthFirstSearch(dir = sub, target = target, match = match, matchedCallback=matchedCallback, canceled = canceled)
    }
}

/**
 * @return canceled() 函数
 */
suspend fun initSearch(keyword: String, lastKeyword: MutableState<String>, token:MutableState<String>):()->Boolean {
    //更新上个关键字
    lastKeyword.value = keyword

    //生成新token
    val tokenForThisSession = generateNewTokenForSearch()
    //必须在主线程更新状态变量，不然可能获取到旧值，如果还有问题，改用Channel
    withMainContext {
        token.value = tokenForThisSession
    }

    //生成cancel函数并返回
    return {
        if(AppModel.devModeOn) {
            //若不相等，就是有bug，改用channel替换状态变量
            MyLog.d(TAG, "token.value==tokenForThisSession is '${token.value==tokenForThisSession}', if is false, may something wrong: token.value=${token.value}, tokenForThisSession=$tokenForThisSession")
        }

        //如果ide有 "Unused equals expression "，无视即可，ide不知道这个state变化后value会变，而curToken不会变，所以这个表达式并非常量
        //正常来说搜索前会生成新token，因此token必然非空，若为空，则代表取消搜索；若非空则与当前state变量进行比较，若不相等，代表开启了新的搜索，此次搜索已取消
        token.value.isEmpty() || tokenForThisSession != token.value
    }
}

fun generateNewTokenForSearch():String {
    return generateRandomString(18)
}


@Composable
fun <T> filterTheList(
    activityContext: Context,
    enableFilter: Boolean,
    keyword: String,
    lastKeyword: MutableState<String>,
    searching: MutableState<Boolean>,
    token: MutableState<String>,
    resetSearchVars: () -> Unit,
    match:(idx:Int, item:T)->Boolean, // 若customTask非null，此参数无效
    list: List<T>,
    filterList: MutableList<T>,

    // 开始：file history 和 commit history用这几个变量
    lastListSize: MutableIntState? = null,
    filterIdxList:MutableList<Int>? = null,
    customTask:(suspend ()->Unit)? = null,  //若此参数非null，将忽略入参match，此参数内部应该完全自定义如何匹配条目
    // 结束：file history 和 commit history用这几个变量

    // commit history用到这个参数。若此参数返回真，则会重新执行搜索，可把附加的重新启用搜索的条件放到这个参数里执行
    orCustomDoFilterCondition:()->Boolean = {false},
    // commit history用这个参数。在搜索之前执行些附加操作，一般是清列表或者更新上次列表相关的变量
    //此函数调用的时机是已通过执行搜索的判断，但在执行搜索之前
    beforeSearchCallback:(()->Unit)? = null,
) : List<T> {
    return if (enableFilter) {
        val curListSize = list.size

        if (keyword != lastKeyword.value || ( lastListSize!=null && curListSize != lastListSize.intValue) || orCustomDoFilterCondition()) {
            lastListSize?.intValue = curListSize
            filterIdxList?.clear()
            beforeSearchCallback?.invoke()

            //若自定义任务为null则运行默认任务
            doJobThenOffLoading(loadingOff = { searching.value = false }) {
                // customTask若不为null，调用；若为null，调用默认task
                (customTask ?: {
                    val canceled = initSearch(keyword = keyword, lastKeyword = lastKeyword, token = token)

                    searching.value = true

                    filterList.clear()
                    search(src = list, match = match, matchedCallback = {idx, item -> filterList.add(item)}, canceled = canceled)
                }).invoke()
            }

        }

        filterList
    } else {
        resetSearchVars()
        list
    }

}

fun newScrollState(initial:Int = 0):ScrollState = ScrollState(initial = initial)

fun navToFileChooser(type: FileChooserType) {
    AppModel.navController.navigate(Cons.nav_FileChooserScreen + "/" + type.code)
}

fun getFilesScreenTitle(currentPath:String, activityContext: Context):String {
    if(currentPath == FsUtils.rootPath) {
        return FsUtils.rootName
    }

    //不要在这判断path是否空字符串，后面的else会处理


    val trimedSlashCurPath = currentPath.trimEnd(Cons.slashChar)

    return if(trimedSlashCurPath == FsUtils.getInternalStorageRootPathNoEndsWithSeparator()) {
        activityContext.getString(R.string.internal_storage)
    }else if(trimedSlashCurPath == FsUtils.getExternalStorageRootPathNoEndsWithSeparator()) {
        activityContext.getString(R.string.external_storage)
    }else {
        runCatching { FsUtils.splitParentAndName(currentPath).second }.getOrDefault("").ifEmpty { activityContext.getString(R.string.files) }
    }
}

fun getEditorStateOnChange(
    editorPageTextEditorState:CustomStateSaveable<TextEditorState>,
    lastTextEditorState:CustomStateSaveable<TextEditorState>,
    undoStack:UndoStack
):(newState: TextEditorState, trueSaveToUndoFalseRedoNullNoSave:Boolean?, clearRedoStack:Boolean) -> Unit {

    return { newState: TextEditorState, trueSaveToUndoFalseRedoNullNoSave:Boolean?, clearRedoStack:Boolean ->
        editorPageTextEditorState.value = newState

        val lastState = lastTextEditorState.value
        // last state == null || 不等于新state的filesId，则入栈
        //这个fieldsId只是个粗略判断，即使一样也不能保证fields完全一样
        if(lastState.maybeNotEquals(newState)) {
//                    if(lastTextEditorState.value?.fields != newState.fields) {
            //true或null，存undo; false存redo。null本来是在选择行之类的场景的，没改内容，可以不存，但我后来感觉存上比较好
            val saved = if(trueSaveToUndoFalseRedoNullNoSave != false) {  // null or true
                // redo的时候，添加状态到undo，不清redo stack，平时编辑文件的时候更新undo stack需清空redo stack
                // trueSaveToUndoFalseRedoNullNoSave为null时是选择某行之类的不修改内容的状态变化，因此不用清redoStack
//                        if(trueSaveToUndoFalseRedoNullNoSave!=null && clearRedoStack) {
                //改了下调用函数时传的这个值，在不修改内容时更新状态清除clearReadStack传了false，所以不需要额外判断trueSaveToUndoFalseRedoNullNoSave是否为null了
                if(clearRedoStack) {
                    undoStack.redoStackClear()
                }

                undoStack.undoStackPush(lastState)
            }else {  // false
                undoStack.redoStackPush(lastState)
            }

            if(saved) {
                lastTextEditorState.value = newState
            }
        }
    }
}

//初始状态值随便填，只是为了帮助泛型确定类型并且避免使用null作为初始值而已，具体的值在打开文件后会重新创建
fun getInitTextEditorState():TextEditorState {
    return TextEditorState.create(
        text = "",
        fieldsId = "",
        isContentEdited = mutableStateOf(false),
        editorPageIsContentSnapshoted = mutableStateOf(false),
        isMultipleSelectionMode = false,
        focusingLineIdx = null,
        onChanged = { i1, i2, i3->}
    )
}
