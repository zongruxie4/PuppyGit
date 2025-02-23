package com.catpuppyapp.puppygit.screen.functions

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.ClipboardManager
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.LineNum
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.withMainContext
import kotlinx.coroutines.CoroutineScope

private const val TAG = "ScreenHelper"

fun goToFileHistory(fileFullPath:String, appContext: Context){
    doJobThenOffLoading job@{
        try {
            val repo = Libgit2Helper.findRepoByPath(fileFullPath)
            if(repo == null) {
                Msg.requireShow(appContext.getString(R.string.no_repo_found))
                return@job
            }


            // file is belong a repo, but need check is under .git folder
            repo.use {
                //检查文件是否在.git目录下，若在，直接返回，此目录下的文件无历史记录
                val repoGitDirEndsWithSlash = Libgit2Helper.getRepoGitDirPathNoEndsWithSlash(it) + Cons.slash
                if(fileFullPath.startsWith(repoGitDirEndsWithSlash)) {
                    Msg.requireShowLongDuration(appContext.getString(R.string.err_file_under_git_dir))
                    return@job
                }


                val repoWorkDirPath = Libgit2Helper.getRepoWorkdirNoEndsWithSlash(it)
                val relativePath = Libgit2Helper.getRelativePathUnderRepo(repoWorkDirPath, fileFullPath)
                if(relativePath == null) {  // this should never happen, cuz go to file history only available for file, not for dir, and this only happens when the realFullPath = repoWorkDirPath
                    Msg.requireShow(appContext.getString(R.string.path_not_under_repo))
                    return@job
                }

                val repoDb = AppModel.dbContainer.repoRepository
                val repoFromDb = repoDb.getByFullSavePath(
                    repoWorkDirPath,
                    onlyReturnReadyRepo = false,  // if not ready, cant open at upside code, so reached here, no need more check about ready, is 100% ready
                    requireSyncRepoInfoWithGit = false,  // no need
                )

                if(repoFromDb == null) {
                    Msg.requireShowLongDuration(appContext.getString(R.string.plz_import_repo_then_try_again))
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

fun getLoadText(loadedCount:Int, actuallyEnabledFilterMode:Boolean, appContext:Context):String?{
    return if(loadedCount < 1){
        null
    }else if(actuallyEnabledFilterMode) {
        replaceStringResList(appContext.getString(R.string.item_count_n), listOf(""+loadedCount))
    }else {
        replaceStringResList(appContext.getString(R.string.loaded_n), listOf(""+loadedCount))
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

    AppModel.navController
        .navigate(Cons.nav_SubPageEditor + "/$goToLine/$initMergeMode/$initReadOnly")
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

