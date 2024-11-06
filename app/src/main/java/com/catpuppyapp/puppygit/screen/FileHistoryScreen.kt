package com.catpuppyapp.puppygit.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.BottomSheet
import com.catpuppyapp.puppygit.compose.BottomSheetItem
import com.catpuppyapp.puppygit.compose.ConfirmDialog2
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.DiffCommitsDialog
import com.catpuppyapp.puppygit.compose.FileHistoryItem
import com.catpuppyapp.puppygit.compose.FileHistoryRestoreDialog
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadMore
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MyCheckBox
import com.catpuppyapp.puppygit.compose.MyLazyColumn
import com.catpuppyapp.puppygit.compose.MySelectionContainer
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dev.commitsDiffCommitsTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.dev.resetByHashTestPassed
import com.catpuppyapp.puppygit.git.CommitDto
import com.catpuppyapp.puppygit.git.FileHistoryDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.StateRequestType
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.createAndInsertError
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getRequestDataByState
import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.GitObject
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository
import com.github.git24j.core.Revwalk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val TAG = "FileHistoryScreen"
private val stateKeyTag = "FileHistoryScreen"


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileHistoryScreen(
//    context: Context,
//    navController: NavHostController,
//    scope: CoroutineScope,
//    haptic:HapticFeedback,
//    homeTopBarScrollBehavior: TopAppBarScrollBehavior,
    repoId: String,
    fileRelativePathKey:String,  // relative path under repo
    naviUp: () -> Boolean,
) {


    //已处理这种情况，传参时传有效key，但把value设为空字符串，就解决了
//    println("fullOidKey.isEmpty()="+fullOidKey.isEmpty())  //expect true when nav from repoCard, result: is not empty yet
//    println("fullOidKey="+fullOidKey)  //expect true when nav from repoCard

    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior
    val appContext = LocalContext.current
    val navController = AppModel.singleInstanceHolder.navController
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val loadChannel = remember { Channel<Int>() }

    val fileRelativePath = rememberSaveable { Cache.getByTypeThenDel<String>(fileRelativePathKey) ?:"" }
    val lastVersionEntryOid = rememberSaveable { mutableStateOf<String?>(null) }


//    val sumPage = MockData.getCommitSum(repoId,branch)
    //获取假数据
//    val list = remember { mutableStateListOf<CommitDto>() };
//    val list = StateUtil.getCustomSaveableState(keyTag = stateKeyTag, keyDesc = "list", initValue = mutableStateListOf<CommitDto>())
    val list = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "list",
        initValue = listOf<FileHistoryDto>()
    )
//    val list = rememberSaveable(
//        stateSaver = getSaver()
//    ) {
//        mutableStateOf(getHolder(stateKeyTag, "list",  mutableListOf<CommitDto>()))
//    }
    val settings = remember { SettingsUtil.getSettingsSnapshot() }
    //page size for load more
    val pageSize = rememberSaveable{ mutableStateOf(settings.fileHistoryPageSize) }
    val rememberPageSize = rememberSaveable { mutableStateOf(false) }

    val nextCommitOid = mutableCustomStateOf<Oid>(
        keyTag = stateKeyTag,
        keyName = "nextCommitOid",
        initValue = Cons.allZeroOid
    )

    /*
        first oid in the list of this screen
     */
    val headOidOfThisScreen = mutableCustomStateOf<Oid>(
        keyTag = stateKeyTag,
        keyName = "headOidOfThisScreen",
        initValue = Cons.allZeroOid
    )

    //这个页面的滚动状态不用记住，每次点开重置也无所谓
    val listState = rememberLazyListState()
    //如果再多几个"mode"，就改用字符串判断，直接把mode含义写成常量
    val showTopBarMenu = rememberSaveable { mutableStateOf(false)}
    val showDiffCommitDialog = rememberSaveable { mutableStateOf(false)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}
//    val curCommit = rememberSaveable{ mutableStateOf(CommitDto()) }
    val curObj = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "curObj",
        initValue = FileHistoryDto()
    )
    val curRepo = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "curRepo",
        initValue = RepoEntity(id = "")
    )

    val curObjShortOid = remember(curObj.value.commitOidStr) { derivedStateOf{
        Libgit2Helper.getShortOidStrByFull(curObj.value.commitOidStr)
    }}

    // 两个用途：1点击刷新按钮后回到列表顶部 2放到刷新按钮旁边，用户滚动到底部后，想回到顶部，可点击这个按钮
    val goToTop = {
        UIHelper.scrollToItem(scope, listState, 0)
    }


    val showFilterByPathsDialog = rememberSaveable { mutableStateOf(false) }
//    val pathsForFilterByPathsDialog = mutableCustomStateListOf(stateKeyTag, "pathsForFilterByPathsDialog") { listOf<String>() }
    val pathsCacheForFilterByPathsDialog = rememberSaveable { mutableStateOf("") }  // cache the paths until user clicked the ok, then assign the value to `pathsForFilter`
    val pathsForFilter = rememberSaveable { mutableStateOf("") }

//    val requireShowToast: (String) -> Unit = Msg.requireShow


    val loadMoreLoading = rememberSaveable { mutableStateOf(false)}
    val loadMoreText = rememberSaveable { mutableStateOf("")}
    val hasMore = rememberSaveable { mutableStateOf(false)}


    val needRefresh = rememberSaveable { mutableStateOf("")}


    val loadingStrRes = stringResource(R.string.loading)
    val loadingText = rememberSaveable { mutableStateOf(loadingStrRes)}
    val showLoadingDialog = rememberSaveable { mutableStateOf(false)}

    val loadingOn = { msg:String->
        loadingText.value = msg
        showLoadingDialog.value=true
    }
    val loadingOff = {
        loadingText.value=loadingStrRes
        showLoadingDialog.value=false
    }

    if (showLoadingDialog.value) {
        LoadingDialog(loadingText.value)
    }

//    val loadingMore = StateUtil.getRememberSaveableState(initValue = false)
//    val hasMore = {
//        nextCommitOid.value != null &&
//    }

    val revwalk = mutableCustomStateOf<Revwalk?>(stateKeyTag, "revwalk", null)
    val repositoryForRevWalk = mutableCustomStateOf<Repository?>(stateKeyTag, "repositoryForRevWalk", null)
    val loadLock = mutableCustomStateOf<Mutex>(stateKeyTag, "loadLock", Mutex())

    val doLoadMore = doLoadMore@{ repoFullPath: String, oid: Oid, firstLoad: Boolean, forceReload: Boolean, loadToEnd:Boolean ->
        //第一次查询的时候是用head oid查询的，所以不会在这里返回
        //用全0oid替代null
        if (oid.isNullOrEmptyOrZero) {  //已经加载到最后一个元素了，其实正常来说，如果加载到最后一个元素，应该已经赋值给nextCommitOid了，然后加载更多按钮也会被禁用，所以多半不会再调用这个方法，这的判断只是为了以防万一
            nextCommitOid.value = oid
            return@doLoadMore
        }
        //无效仓库储存路径
        if (repoFullPath.isBlank()) {
            return@doLoadMore
        }
        //恢复数据
        if (firstLoad && list.value.isNotEmpty() && !forceReload) {
//            if(debugModeOn) {
//                //如果列表能恢复，那上次的oid应该也能恢复，问题不大
//                println("nextCommitOid.value="+nextCommitOid.value)
//            }
//            list.requireRefreshView()
            return@doLoadMore
        }

        //加载更多
        //这个用scope，似乎会随页面释放而取消任务？不知道是否需要我检查CancelException？
        doJobThenOffLoading job@{
            loadLock.value.withLock {
                loadMoreLoading.value = true
                loadMoreText.value = appContext.getString(R.string.loading)

                try {
                    if (firstLoad || forceReload || repositoryForRevWalk.value==null || revwalk.value==null) {
                        // do reset: clear list and release old repo instance
                        //如果是第一次加载或刷新页面（重新初始化页面），清下列表
                        // if is first load or refresh page, clear list
                        list.value.clear()

                        // close old repo, release resource
                        repositoryForRevWalk.value?.close()
                        repositoryForRevWalk.value = null  // if don't set to null, when assign new instance to state, implicitly call equals(), the closed repo will thrown an err


                        // do init: create new repo instance
                        val repo = Repository.open(repoFullPath)
                        //get new revwalk instance
                        val newRevwalk = Libgit2Helper.createRevwalk(repo, oid)
                        if(newRevwalk == null) {
                            val oidStr = oid.toString()
                            Msg.requireShowLongDuration(replaceStringResList(appContext.getString(R.string.create_revwalk_failed_oid), listOf(Libgit2Helper.getShortOidStrByFull(oidStr))))
                            createAndInsertError(repoId, "create Revwalk failed, oid=$oidStr")
                            return@job
                        }

//                    println("repo.equals(repositoryForRevWalk.value):${repo.equals(repositoryForRevWalk.value)}")  // expect: false, output: false

                        // the revwalk must use with the repo instance which created it, else will throw an err "signed...prefix -..." something
                        // revwalk必须与创建它的仓库一起使用，否则会报错，报什么"signed...prefix -..."之类的错误
                        repositoryForRevWalk.value = repo
                        revwalk.value = newRevwalk
                        nextCommitOid.value = newRevwalk.next() ?: Cons.allZeroOid

//                    println("oldRepoInstance == repositoryForRevWalk.value:${oldRepoInstance == repositoryForRevWalk.value}")  // expect:false, output:false
                        // release memory
//                    oldRepoInstance?.close()
                    }

                    val repo = repositoryForRevWalk.value ?: throw RuntimeException("repo for revwalk is null")

                    if(nextCommitOid.value.isNullOrEmptyOrZero) {
                        //更新变量
                        hasMore.value = false
                        loadMoreText.value = appContext.getString(R.string.end_of_the_list)
                    }else {
                        //start travel commit history
                        lastVersionEntryOid.value = Libgit2Helper.getFileHistoryList(
                            repo,
                            revwalk.value!!,
                            nextCommitOid.value,
                            repoId,
                            if(loadToEnd) Int.MAX_VALUE else pageSize.value,
                            retList = list.value,  //直接赋值给状态列表了，若性能差，可实现一个批量添加机制，比如查出50个条目添加一次，之类的
                            loadChannel = loadChannel,
                            checkChannelFrequency = settings.commitHistoryLoadMoreCheckAbortSignalFrequency,
                            lastVersionEntryOid = lastVersionEntryOid.value,
                            fileRelativePathUnderRepo = fileRelativePath
                        )

                        //update state
                        nextCommitOid.value = revwalk.value!!.next() ?: Cons.allZeroOid
                        hasMore.value = !nextCommitOid.value.isNullOrEmptyOrZero
                        loadMoreText.value = if (hasMore.value) appContext.getString(R.string.load_more) else appContext.getString(R.string.end_of_the_list)

                    }


                }catch (e:Exception) {
                    val errMsg = e.localizedMessage ?: "unknown err"
                    Msg.requireShowLongDuration(errMsg)
                    createAndInsertError(repoId, "err: $errMsg")
                    MyLog.e(TAG, "#doLoadMore: err: ${e.stackTraceToString()}")
                }finally {
                    loadMoreLoading.value = false

                }
            }
        }
    }

    val clipboardManager = LocalClipboardManager.current

    val showRestoreDialog = rememberSaveable { mutableStateOf(false)}
    if(showRestoreDialog.value) {
        FileHistoryRestoreDialog(
            targetCommitOidStr = curObj.value.commitOidStr,
            showRestoreDialog = showRestoreDialog,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            appContext = appContext,
            curRepo = curRepo,
            fileRelativePath = fileRelativePath,
            repoId = repoId
        )
    }

    val requireUserInputCommitHash = rememberSaveable { mutableStateOf(false)}
//    val forceCheckout = StateUtil.getRememberSaveableState(initValue = false)
    val showCheckoutDialog = rememberSaveable { mutableStateOf(false)}
    //当前长按commit在列表中的索引，用来更新单个条目时使用，为-1时无效，不要执行操作
    val curObjIndex = rememberSaveable{mutableIntStateOf(-1)}
//    val initCheckoutDialog = { requireUserInputHash:Boolean ->
//        checkoutSelectedOption.intValue = checkoutOptionDefault
//        requireUserInputCommitHash.value = requireUserInputHash
//        forceCheckout.value = false
//        showCheckoutDialog.value = true
//    }


    val updateCurCommitInfo = {repoFullPath:String, curCommitIdx:Int, commitOid:String, list:MutableList<CommitDto> ->
        doActIfIndexGood(curCommitIdx, list) {
            Repository.open(repoFullPath).use { repo ->
                val reQueriedCommitInfo = Libgit2Helper.getSingleCommit(repo, repoId, commitOid)
                list[curCommitIdx] = reQueriedCommitInfo
            }
        }

    }


    //filter相关，开始
    val filterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filterKeyword",
        initValue = TextFieldValue("")
    )
    val filterModeOn = rememberSaveable { mutableStateOf(false)
}
    //存储符合过滤条件的条目在源列表中的真实索引。本列表索引对应filter list条目索引，值对应原始列表索引
    val filterIdxList = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "filterIdxList",
        listOf<Int>()
    )
    val filterList = mutableCustomStateListOf(
        keyTag = stateKeyTag,
        keyName = "filterList",
        listOf<FileHistoryDto>()
    )

    //filter相关，结束


    val nameOfNewTag = rememberSaveable { mutableStateOf("")}
    val overwriteIfNameExistOfNewTag = rememberSaveable { mutableStateOf(false)}  // force
    val showDialogOfNewTag = rememberSaveable { mutableStateOf(false)}
    val hashOfNewTag = rememberSaveable { mutableStateOf( "")}
    val msgOfNewTag = rememberSaveable { mutableStateOf( "")}
//    val requireUserInputHashOfNewTag = StateUtil.getRememberSaveableState(initValue = false)
    val annotateOfNewTag = rememberSaveable { mutableStateOf(false)}
    val initNewTagDialog = { hash:String ->
        hashOfNewTag.value = hash  //把hash设置为当前选中的commit的hash

        overwriteIfNameExistOfNewTag.value = false
        showDialogOfNewTag.value = true
    }


    //初始化组件版本的checkout对话框
    val initCheckoutDialogComposableVersion = { requireUserInputHash:Boolean ->
        requireUserInputCommitHash.value = requireUserInputHash
        showCheckoutDialog.value = true
    }


    val resetOid = rememberSaveable { mutableStateOf("")}
//    val acceptHardReset = StateUtil.getRememberSaveableState(initValue = false)
    val showResetDialog = rememberSaveable { mutableStateOf(false)}
    val closeResetDialog = {
        showResetDialog.value = false
    }

    val showDetailsDialog = rememberSaveable { mutableStateOf( false)}
    val detailsString = rememberSaveable { mutableStateOf( "")}
    if(showDetailsDialog.value) {
        CopyableDialog(
            title = stringResource(id = R.string.details),
            text = detailsString.value,
            onCancel = { showDetailsDialog.value = false }
        ) {
            showDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(detailsString.value))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }

    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(settings.showNaviButtons) }

    val requireBlinkIdx = rememberSaveable{mutableIntStateOf(-1)}

//    val filterListState =mutableCustomStateOf(keyTag = stateKeyTag, keyName = "filterListState", LazyListState(0,0))
    val filterListState = rememberLazyListState()
    val enableFilterState = rememberSaveable { mutableStateOf(false)}

    val diffCommitsDialogCommit1 = rememberSaveable { mutableStateOf("")}
    val diffCommitsDialogCommit2 = rememberSaveable { mutableStateOf("")}
    if(showDiffCommitDialog.value) {
        DiffCommitsDialog(
            showDiffCommitDialog,
            diffCommitsDialogCommit1,
            diffCommitsDialogCommit2,
            curRepo.value
        )
    }


    val savePatchPath= rememberSaveable { mutableStateOf("")}
    val showSavePatchSuccessDialog = rememberSaveable { mutableStateOf(false)}

    if(showSavePatchSuccessDialog.value) {
        val path = savePatchPath.value

        CopyableDialog(
            title = stringResource(R.string.success),
            text = replaceStringResList(stringResource(R.string.export_path_ph1_you_can_go_to_files_page_found_this_file), listOf(path)),
            okBtnText = stringResource(R.string.copy_path),
            onCancel = { showSavePatchSuccessDialog.value = false }
        ) {
            showSavePatchSuccessDialog.value = false

            clipboardManager.setText(AnnotatedString(path))
            Msg.requireShow(appContext.getString(R.string.copied))
        }
    }



    val showCreatePatchDialog = rememberSaveable { mutableStateOf(false)}
    val createPatchTargetHash = rememberSaveable { mutableStateOf("")}
    val createPatchParentHash = rememberSaveable { mutableStateOf("")}
    val createPatchParentList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "createPatchParentList", listOf<String>())

    val initCreatePatchDialog = { targetFullHash:String, defaultParentFullHash:String, parentList:List<String> ->
        createPatchParentList.value.clear()
        createPatchParentList.value.addAll(parentList)

        createPatchTargetHash.value = targetFullHash
        createPatchParentHash.value = defaultParentFullHash


        showCreatePatchDialog.value = true
    }


    val invalidPageSize = -1
    val minPageSize = 1  // make sure it bigger than `invalidPageSize`

    val isInvalidPageSize = { ps:Int ->
        ps < minPageSize
    }

    val showSetPageSizeDialog = rememberSaveable { mutableStateOf(false) }
    val pageSizeForDialog = rememberSaveable { mutableStateOf(""+pageSize.value) }

    if(showSetPageSizeDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.page_size),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),

                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                        value = pageSizeForDialog.value,
                        singleLine = true,
                        onValueChange = {
                            pageSizeForDialog.value = it
                        },
                        label = {
                            Text(stringResource(R.string.page_size))
                        },
                    )

                    Spacer(Modifier.height(10.dp))

                    MyCheckBox(text= stringResource(R.string.remember), rememberPageSize)
                }
            },
            onCancel = {showSetPageSizeDialog.value=false}
        ) {
            showSetPageSizeDialog.value=false

            try {
                val newPageSize = try {
                    pageSizeForDialog.value.toInt()
                }catch (_:Exception) {
                    Msg.requireShow(appContext.getString(R.string.invalid_number))
                    invalidPageSize
                }

                if(!isInvalidPageSize(newPageSize)) {
                    pageSize.value = newPageSize

                    if(rememberPageSize.value) {
                        SettingsUtil.update {
                            it.fileHistoryPageSize = newPageSize
                        }
                    }
                }

            }catch (e:Exception) {
                MyLog.e(TAG, "#SetPageSizeDialog err: ${e.localizedMessage}")
            }
        }
    }

    val showTitleInfoDialog = remember { mutableStateOf(false) }
    val titleInfo = rememberSaveable { mutableStateOf(fileRelativePath) }
    if(showTitleInfoDialog.value) {
        ConfirmDialog2(
            title = stringResource(R.string.info),
            requireShowTextCompose = true,
            textCompose = {
                ScrollableColumn {
                    MySelectionContainer {
                        Text(titleInfo.value)
                    }

                }
            },
            onCancel = {showTitleInfoDialog.value = false},
            cancelBtnText = stringResource(R.string.close),
            showOk = false
        ) { }
    }

    val getActuallyList = {
        if(filterModeOn.value) {
            filterList.value
        }else{
            list.value
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    if(filterModeOn.value) {
                        FilterTextField(
                            filterKeyword,
                            trailingIconTooltipText= stringResource(R.string.filter_by_paths),
                            trailingIcon = Icons.AutoMirrored.Filled.List,
                            trailingIconColor = UIHelper.getIconEnableColorOrNull(pathsForFilter.value.isNotEmpty()) ?: Color.Unspecified,
                            trailingIconDesc = stringResource(R.string.a_list_icon_lor_filter_commits_by_paths),
                            trailingIconOnClick = {
                                // show filte by path dialog
                                pathsCacheForFilterByPathsDialog.value = pathsForFilter.value  // assign current working filter paths to paths cache for accept user input
                                showFilterByPathsDialog.value = true
                            }
                        )
                    }else{
                        Column(
                            modifier = Modifier.combinedClickable(
                                onDoubleClick = {
                                    //双击返回列表顶部
                                    goToTop()
                                },
                                onLongClick = {
                                    //长按显示仓库和分支信息
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                                    Msg.requireShow(repoAndBranchText)
                                    // show loaded how many items
                                    Msg.requireShow("loaded: ${list.value.size}")
                                }
                            ) { // onClick
                                showTitleInfoDialog.value = true
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = stringResource(R.string.file_history),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                            }
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                Text(
                                    text = titleInfo.value,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = MyStyleKt.Title.secondLineFontSize
                                )
                            }

                        }
                    }
                },
                navigationIcon = {
                    if(filterModeOn.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.close),
                            icon = Icons.Filled.Close,
                            iconContentDesc = stringResource(R.string.close),

                        ) {
                            filterModeOn.value = false
                        }
                    } else {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.back),
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            iconContentDesc = stringResource(R.string.back),

                            ) {
                            naviUp()
                        }
                    }
                },
                actions = {
                    if(!filterModeOn.value) {
                        Row {
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.filter),
                                icon = Icons.Filled.FilterAlt,
                                iconContentDesc = stringResource(R.string.filter),
                            ) {
                                // filter item
                                filterKeyword.value = TextFieldValue("")
                                pathsForFilter.value = ""

                                filterModeOn.value = true
                            }

                            //刷新按钮
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.refresh),
                                icon = Icons.Filled.Refresh,
                                iconContentDesc = stringResource(id = R.string.refresh),
                                enabled = true,

                            ) {
                                goToTop()
                                changeStateTriggerRefreshPage(
                                    needRefresh,
                                    StateRequestType.forceReload
                                )
                            }

                            if((proFeatureEnabled(commitsDiffCommitsTestPassed) || proFeatureEnabled(resetByHashTestPassed))) {
    //                            显示more三点菜单
                                LongPressAbleIconBtn(
                                    tooltipText = stringResource(R.string.menu),
                                    icon = Icons.Filled.MoreVert,
                                    iconContentDesc = stringResource(id = R.string.menu),
                                    enabled = true,
                                ) {
                                    showTopBarMenu.value = true
                                }

                            }
                        }

                        if(showTopBarMenu.value) {
                            Row (modifier = Modifier.padding(top = MyStyleKt.TopBar.dropDownMenuTopPaddingSize)) {
                                DropdownMenu(
                                    expanded = showTopBarMenu.value,
                                    onDismissRequest = { showTopBarMenu.value=false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.page_size)) },
                                        onClick = {
                                            pageSizeForDialog.value = ""+pageSize.value
                                            showSetPageSizeDialog.value = true

                                            //关闭顶栏菜单
                                            showTopBarMenu.value = false
                                        }
                                    )
                                }



                            }

                        }
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(pageScrolled.value) {

                GoToTopAndGoToBottomFab(
                    filterModeOn = enableFilterState.value,
                    scope = scope,
                    filterListState = filterListState,
                    listState = listState,
                    showFab = pageScrolled
                )

            }
        }
    ) { contentPadding ->

        if(loadMoreLoading.value.not() && list.value.isEmpty()) {
            Column(
                modifier = Modifier
                    //fillMaxSize 必须在最上面！要不然，文字不会显示在中间！
                    .fillMaxSize()
                    .padding(contentPadding)
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState())
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.file_hasnt_history_yet))
            }
        }else {
//        val commitLen = 10;
            if (showBottomSheet.value) {
//            var commitOid = curCommit.value.oidStr
//            if(commitOid.length > Cons.gitShortCommitHashRangeEndInclusive) {  //避免commitOid不够长导致抛异常，正常来说commitOid是40位，不会有问题，除非哪里出了问题
//                commitOid = commitOid.substring(Cons.gitShortCommitHashRange)+"..."
//            }
                BottomSheet(showBottomSheet, sheetState, curObjShortOid.value) {
                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.restore)) {
                        showRestoreDialog.value = true
                    }

                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.diff_to_prev)) label@{
                        val list = getActuallyList()
                        val indexAtDiffableList = curObjIndex.intValue
                        val previousIndex = indexAtDiffableList +1
                        if(!isGoodIndexForList(previousIndex, list)) {
                            if(hasMore.value) {
                                Msg.requireShowLongDuration(appContext.getString(R.string.plz_lode_more_then_try_again))
                            }else {
                                Msg.requireShowLongDuration(appContext.getString(R.string.no_prev_to_compare))
                            }

                            return@label
                        }


                        val underRepoPathKey = Cache.setThenReturnKey(fileRelativePath)
                        val diffableListKey = Cache.setThenReturnKey(list)

                        val previous = list[previousIndex]
                        val commit1 = previous.commitOidStr
                        val commit2 = curObj.value.commitOidStr

//                    println("commit1:"+commit1)
//                    println("commit2:"+commit2)
//                    println("fileRelativePath:"+fileRelativePath)

                        val isSubm =0
                        val isDiffToLocal = 0
                        val localAtDiffRight = 0
                        val fileSize = 0

                        //导航到diffScreen
                        navController.navigate(
                            Cons.nav_DiffScreen +
                                    "/" + repoId+
                                    //    "/" + encodeStrUri(item.relativePathUnderRepo) +
                                    "/" + Cons.gitDiffFileHistoryFromTreeToTree +
                                    "/" + Cons.gitStatusModified +
                                    "/" + fileSize +
                                    "/" + underRepoPathKey +
                                    "/" + commit1 +
                                    "/" + commit2 +
                                    "/" + isSubm +
                                    "/" + isDiffToLocal
                                    + "/" + diffableListKey
                                    + "/" + indexAtDiffableList
                                    +"/" + localAtDiffRight
                        )
                    }


                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.details)) {
                        // onClick()
//                    requireShowViewDialog(appContext.getString(R.string.view_hash), curCommit.value.oidStr)
                        val sb = StringBuilder()
                        sb.appendLine("${appContext.getString(R.string.path)}: "+curObj.value.filePathUnderRepo).appendLine()
                        sb.appendLine("${appContext.getString(R.string.commit_id)}: "+curObj.value.commitOidStr).appendLine()
                        sb.appendLine("${appContext.getString(R.string.entry_id)}: "+curObj.value.treeEntryOidStr).appendLine()
                        sb.appendLine("${appContext.getString(R.string.author)}: "+ Libgit2Helper.getFormattedUsernameAndEmail(curObj.value.authorUsername, curObj.value.authorEmail))
                        sb.appendLine()
                        sb.appendLine("${appContext.getString(R.string.committer)}: "+ Libgit2Helper.getFormattedUsernameAndEmail(curObj.value.committerUsername, curObj.value.committerEmail))
                        sb.appendLine()
                        sb.appendLine("${appContext.getString(R.string.date)}: "+curObj.value.dateTime)
                        sb.appendLine()
                        sb.appendLine("${appContext.getString(R.string.msg)}: "+curObj.value.msg)
                        sb.appendLine()


                        detailsString.value = sb.toString()
                        showDetailsDialog.value = true
                    }
//
//                if(UserUtil.isPro() && (dev_EnableUnTestedFeature || commitsDiffToLocalTestPassed)) {
//                    BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.diff_to_local)) {
//    //                    diff to local，点击跳转到tree to tree页面，然后diff
//                        //当前比较的描述信息的key，用来在界面显示这是在比较啥，值例如“和父提交比较”或者“比较两个提交”之类的
//                        val descKey = Cache.setThenReturnKey(appContext.getString(R.string.compare_to_local))
//                        //这里需要传当前commit，然后cl页面会用当前commit查出当前commit的parents
//                        val commit2 = Cons.gitLocalWorktreeCommitHash
//                        val commitForQueryParents = Cons.allZeroOidStr
//                        // url 参数： 页面导航id/repoId/treeoid1/treeoid2/desckey
//                        navController.navigate(
//                            //注意是 parentTreeOid to thisObj.treeOid，也就是 旧提交to新提交，相当于 git diff abc...def，比较的是旧版到新版，新增或删除或修改了什么，反过来的话，新增删除之类的也就反了
//                            "${Cons.nav_TreeToTreeChangeListScreen}/${curRepo.value.id}/${curObj.value.oidStr}/$commit2/$descKey/$commitForQueryParents"
//                        )
//                    }
//                }


                    //如果是filter模式，显示show in list以在列表揭示filter条目以查看前后提交（或者说上下文）
                    if(enableFilterState.value) {
                        BottomSheetItem(sheetState, showBottomSheet, stringResource(R.string.show_in_list)) {
                            filterModeOn.value = false
                            showBottomSheet.value = false

                            doJobThenOffLoading {
//                            delay(100)  // wait rendering, may unnecessary yet
                                val curItemIndex = curObjIndex.intValue  // 被长按的条目在 filterlist中的索引
                                val idxList = filterIdxList.value  //取出存储filter索引和源列表索引的 index list，条目索引对应filter list条目索引，条目值对应的是源列表的真实索引

                                doActIfIndexGood(curItemIndex, idxList) {  // it为当前被长按的条目在源列表中的真实索引
                                    UIHelper.scrollToItem(scope, listState, it)  //在源列表中定位条目
                                    requireBlinkIdx.intValue = it  //设置条目闪烁以便用户发现
                                }
                            }
                        }

                    }
                }
            }

            //根据关键字过滤条目
            val k = filterKeyword.value.text.lowercase()  //关键字
            val enableFilter = filterModeOn.value && k.isNotEmpty()
            val list = if(enableFilter){
                filterIdxList.value.clear()
                filterList.value.clear()

                val retlist = list.value.filterIndexed {idx, it ->
                    val found = it.treeEntryOidStr.lowercase().contains(k)
                            || it.authorEmail.lowercase().contains(k)
                            || it.authorUsername.lowercase().contains(k)
                            || it.committerEmail.lowercase().contains(k)
                            || it.committerUsername.lowercase().contains(k)
                            || it.dateTime.lowercase().contains(k)
                            || it.commitOidStr.lowercase().contains(k)
                            || it.msg.lowercase().contains(k)


                    filterIdxList.value.add(idx)
                    filterList.value.add(it)
                    found
                }

                retlist
            }else {
                list.value
            }

            val listState = if(enableFilter) filterListState else listState
//        if(enableFilter) {  //更新filter列表state
//            filterListState.value = listState
//        }

            //更新是否启用filter
            enableFilterState.value = enableFilter

            MyLazyColumn(
                contentPadding = contentPadding,
                list = list,
                listState = listState,
                requireForEachWithIndex = true,
                requirePaddingAtBottom = false,
                requireCustomBottom = true,
                customBottom = {
                    LoadMore(
                        pageSize=pageSize,
                        rememberPageSize=rememberPageSize,
                        showSetPageSizeDialog=showSetPageSizeDialog,
                        pageSizeForDialog=pageSizeForDialog,
                        text = loadMoreText.value,
                        enableLoadMore = !loadMoreLoading.value && hasMore.value, enableAndShowLoadToEnd = !loadMoreLoading.value && hasMore.value,
                        loadToEndOnClick = {
                            val firstLoad = false
                            val forceReload = false
                            val loadToEnd = true
                            doLoadMore(
                                curRepo.value.fullSavePath,
                                nextCommitOid.value,
                                firstLoad,
                                forceReload,
                                loadToEnd
                            )
                        }
                    ) {
                        val firstLoad = false
                        val forceReload = false
                        val loadToEnd = false
                        doLoadMore(
                            curRepo.value.fullSavePath,
                            nextCommitOid.value,
                            firstLoad,
                            forceReload,
                            loadToEnd
                        )

                    }
                }
            ) { idx, it ->
                FileHistoryItem(showBottomSheet, curObj, curObjIndex, idx, it, requireBlinkIdx) { thisObj ->
                    val underRepoPathKey = Cache.setThenReturnKey(fileRelativePath)
                    val indexAtDiffableList = idx
                    val diffableListKey = Cache.setThenReturnKey(list.toList())

                    val commit1 = it.commitOidStr
                    val commit2 = Cons.gitLocalWorktreeCommitHash

                    val isSubm =0
                    val fileSize =0
                    val isDiffToLocal = 1
                    val localAtDiffRight = 1

                    Msg.requireShow(appContext.getString(R.string.diff_to_local))

                    //导航到diffScreen
                    navController.navigate(
                        Cons.nav_DiffScreen +
                                "/" + repoId+
                                //    "/" + encodeStrUri(item.relativePathUnderRepo) +
                                "/" + Cons.gitDiffFileHistoryFromTreeToLocal +
                                "/" + Cons.gitStatusModified +
                                "/" + fileSize +
                                "/" + underRepoPathKey +
                                "/" + commit1 +
                                "/" + commit2 +
                                "/" + isSubm +
                                "/" + isDiffToLocal
                                + "/" + diffableListKey
                                + "/" + indexAtDiffableList
                                +"/" + localAtDiffRight
                    )

                }

                HorizontalDivider()
            }

            // filter mode 有可能查无条目，但是可继续加载更多，这时也应显示加载更多按钮
            if(filterModeOn.value && list.isEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(contentPadding)
                        .verticalScroll(rememberScrollState())
                    ,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(Modifier.height(50.dp))
                    Text(stringResource(R.string.no_matched_item), fontWeight = FontWeight.Light)

                    LoadMore(
                        modifier = Modifier.padding(top = 30.dp),
                        pageSize=pageSize,
                        rememberPageSize=rememberPageSize,
                        showSetPageSizeDialog=showSetPageSizeDialog,
                        pageSizeForDialog=pageSizeForDialog,
                        text = loadMoreText.value,
                        enableLoadMore = !loadMoreLoading.value && hasMore.value, enableAndShowLoadToEnd = !loadMoreLoading.value && hasMore.value,
                        loadToEndOnClick = {
                            val firstLoad = false
                            val forceReload = false
                            val loadToEnd = true
                            doLoadMore(
                                curRepo.value.fullSavePath,
                                nextCommitOid.value,
                                firstLoad,
                                forceReload,
                                loadToEnd
                            )
                        }
                    ) {
                        val firstLoad = false
                        val forceReload = false
                        val loadToEnd = false
                        doLoadMore(
                            curRepo.value.fullSavePath,
                            nextCommitOid.value,
                            firstLoad,
                            forceReload,
                            loadToEnd
                        )

                    }
                }
            }
        }

    }

    BackHandler {
        if(filterModeOn.value) {
            filterModeOn.value = false
        } else {
            naviUp()
        }
    }

    //compose创建时的副作用
    LaunchedEffect(needRefresh.value) {

        doJobThenOffLoading job@{
            //这里只用来获取是否需要forceReload的值，且这个值只需获取一次，所以getThenDel设置为true（其实多次获取也没事，只是会导致无意义查询）
            val (requestType, data) = getRequestDataByState<Any?>(
                needRefresh.value,
                getThenDel = true
            )
            val forceReload = (requestType == StateRequestType.forceReload)

            if(forceReload || curRepo.value.id.isBlank() || headOidOfThisScreen.value.isNullOrEmptyOrZero) {
                //从db查数据
                val repoDb = AppModel.singleInstanceHolder.dbContainer.repoRepository
                val repoFromDb = repoDb.getById(repoId)
                if (repoFromDb == null) {
                    MyLog.w(TAG, "#LaunchedEffect: query repo info from db error! repoId=$repoId}")
                    return@job
                }
                curRepo.value = repoFromDb
                val repoFullPath = repoFromDb.fullSavePath
                titleInfo.value = repoFromDb.repoName +": "+fileRelativePath

//            val isDetached = dbIntToBool(repoFromDb.isDetached)

                Repository.open(repoFullPath).use { repo ->
                    val head = Libgit2Helper.resolveHEAD(repo)
                    if (head == null) {
                        MyLog.w(TAG, "#LaunchedEffect: head is null! repoId=$repoId}")
                        return@job
                    }
                    val headOid = head.peel(GitObject.Type.COMMIT)?.id()
                    if (headOid == null || headOid.isNullOrEmptyOrZero) {
                        MyLog.w(
                            TAG,
                            "#LaunchedEffect: head oid is null or invalid! repoId=$repoId}, headOid=${headOid.toString()}"
                        )
                        return@job
                    }


                    headOidOfThisScreen.value = headOid
                }

                //第一次查询，指向headOid，NO！不要这么做，不然compose销毁又重建，恢复数据时，指向原本列表之后的commit就又重新指向head了，就乱了
                //不要在这给nexCommitOid和条目列表赋值！要在doLoadMore里给它们赋值！
//                nextCommitOid.value = headOid

            }

            // do first load
            val firstLoad = true
            val loadToEnd = false

            //传repoFullPath是用来打开git仓库的
            doLoadMore(curRepo.value.fullSavePath, headOidOfThisScreen.value, firstLoad, forceReload, loadToEnd)
        }
    }


    // multi DisposableEffect is ok, each DisposableEffect manage self's code block

    //compose被销毁时执行的副作用(SideEffect)
    DisposableEffect(Unit) {  // param changed or DisposableEffect destroying will run onDispose
        onDispose {
            doJobThenOffLoading {
                loadChannel.close()
            }
        }
    }

}
