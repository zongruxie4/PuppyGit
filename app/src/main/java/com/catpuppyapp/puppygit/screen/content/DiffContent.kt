package com.catpuppyapp.puppygit.screen.content

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.CardButton
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.DiffRow
import com.catpuppyapp.puppygit.compose.MySelectionContainer
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.LineNum
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.AppContainer
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.FlagFileName
import com.catpuppyapp.puppygit.dev.detailsDiffTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.git.CompareLinePair
import com.catpuppyapp.puppygit.git.CompareLinePairResult
import com.catpuppyapp.puppygit.git.DiffItemSaver
import com.catpuppyapp.puppygit.git.FileHistoryDto
import com.catpuppyapp.puppygit.git.PuppyHunkAndLines
import com.catpuppyapp.puppygit.git.PuppyLine
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.shared.SharedState
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.StateRequestType
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.compare.CmpUtil
import com.catpuppyapp.puppygit.utils.compare.param.StringCompareParam
import com.catpuppyapp.puppygit.utils.compare.result.IndexStringPart
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getHumanReadableSizeStr
import com.catpuppyapp.puppygit.utils.getRequestDataByState
import com.catpuppyapp.puppygit.utils.getShortUUID
import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateMapOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.catpuppyapp.puppygit.utils.withMainContext
import com.github.git24j.core.Diff
import com.github.git24j.core.Repository
import kotlinx.coroutines.channels.Channel

private const val TAG = "DiffContent"


@Composable
fun DiffContent(
    repoId: String,
    relativePathUnderRepoDecoded: String,
    fromTo: String,
    changeType: String,  //modification, new, del，之类的只有modification需要diff
    fileSize:Long,  //如果用来判断文件是否过大来决定是否加载的话，上级页面已经判断了，过大根本不会加载此组件，所以这变量可能没用，可以考虑以后显示下文件大小之类的？
    naviUp: () -> Unit,
    dbContainer: AppContainer,
    contentPadding: PaddingValues,
    treeOid1Str:String,
    treeOid2Str:String,
    needRefresh:MutableState<String>,
    listState: LazyListState,
    curRepo:CustomStateSaveable<RepoEntity>,
    requireBetterMatchingForCompare:MutableState<Boolean>,
    matchByWords:MutableState<Boolean>,
    fileFullPath:String,
    isSubmodule:Boolean,
    isDiffToLocal:Boolean,
    diffableItemList:MutableList<StatusTypeEntrySaver>,
    diffableItemListForFileHistory:List<FileHistoryDto>,
    curItemIndex:MutableIntState,
    switchItem:(StatusTypeEntrySaver, index:Int) -> Unit,
    clipboardManager:ClipboardManager,
    loadingOnParent:(String)->Unit,
    loadingOffParent:()->Unit,
    isFileAndExist:State<Boolean>,
    showLineNum:Boolean,
    showOriginType:Boolean,
    fontSize:Int,
    lineNumSize:Int,
    groupDiffContentByLineNum:Boolean,
    switchItemForFileHistory:(FileHistoryDto, index:Int)->Unit,
    enableSelectCompare:Boolean,
    lastClickedItemKey:MutableState<String>,
    pageRequest:MutableState<String>,
    showMyFileHeader:Boolean,  //显示添加删除了几行，由于有diff item对象本来就有hunk header之类的东西，所以名字加My以作区分

) {


}
