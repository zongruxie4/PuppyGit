package com.catpuppyapp.puppygit.screen.content


//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.MutableIntState
//import androidx.compose.runtime.MutableState
//import androidx.compose.runtime.State
//import androidx.compose.ui.platform.ClipboardManager
//import com.catpuppyapp.puppygit.data.AppContainer
//import com.catpuppyapp.puppygit.data.entity.RepoEntity
//import com.catpuppyapp.puppygit.git.FileHistoryDto
//import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
//import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
//
//private const val TAG = "DiffContent"
//
//
//@Composable
//fun DiffContent(
//    repoId: String,
//    relativePathUnderRepoDecoded: String,
//    fromTo: String,
//    changeType: String,  //modification, new, del，之类的只有modification需要diff
//    fileSize:Long,  //如果用来判断文件是否过大来决定是否加载的话，上级页面已经判断了，过大根本不会加载此组件，所以这变量可能没用，可以考虑以后显示下文件大小之类的？
//    naviUp: () -> Unit,
//    dbContainer: AppContainer,
//    contentPadding: PaddingValues,
//    treeOid1Str:String,
//    treeOid2Str:String,
//    needRefresh:MutableState<String>,
//    listState: LazyListState,
//    curRepo:CustomStateSaveable<RepoEntity>,
//    requireBetterMatchingForCompare:MutableState<Boolean>,
//    matchByWords:MutableState<Boolean>,
//    fileFullPath:String,
//    isSubmodule:Boolean,
//    isDiffToLocal:Boolean,
//    diffableItemList:MutableList<StatusTypeEntrySaver>,
//    diffableItemListForFileHistory:List<FileHistoryDto>,
//    curItemIndex:MutableIntState,
//    switchItem:(StatusTypeEntrySaver, index:Int) -> Unit,
//    clipboardManager:ClipboardManager,
//    loadingOnParent:(String)->Unit,
//    loadingOffParent:()->Unit,
//    isFileAndExist:State<Boolean>,
//    showLineNum:Boolean,
//    showOriginType:Boolean,
//    fontSize:Int,
//    lineNumSize:Int,
//    groupDiffContentByLineNum:Boolean,
//    switchItemForFileHistory:(FileHistoryDto, index:Int)->Unit,
//    enableSelectCompare:Boolean,
//    lastClickedItemKey:MutableState<String>,
//    pageRequest:MutableState<String>,
//    showMyFileHeader:Boolean,  //显示添加删除了几行，由于有diff item对象本来就有hunk header之类的东西，所以名字加My以作区分
//
//) {
//
//
//}
