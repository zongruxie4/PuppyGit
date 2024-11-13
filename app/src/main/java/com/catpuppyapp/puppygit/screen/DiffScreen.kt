package com.catpuppyapp.puppygit.screen

import FontSizeAdjuster
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.CopyableDialog
import com.catpuppyapp.puppygit.compose.FileHistoryRestoreDialog
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.MySelectionContainer
import com.catpuppyapp.puppygit.compose.OpenAsDialog
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.FileHistoryDto
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.DiffContent
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.DiffPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.DiffScreenTitle
import com.catpuppyapp.puppygit.settings.SettingsCons
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.getFileNameFromCanonicalPath
import com.catpuppyapp.puppygit.utils.getFormattedLastModifiedTimeOfFile
import com.catpuppyapp.puppygit.utils.getHumanReadableSizeStr
import com.catpuppyapp.puppygit.utils.getParentPathEndsWithSeparator
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import java.io.File

private val stateKeyTag = "DiffScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiffScreen(
    repoId: String,
    fromTo:String,
    changeType:String,  //modification, new, del，之类的只有modification需要diff
    fileSize:Long,
    underRepoPathKey:String,
    treeOid1Str:String,
    treeOid2Str:String,
    isSubmodule:Boolean,
    isDiffToLocal:Boolean,  // actually is diff with local, whether local at left or right, this gonna be true
    diffableItemListKey:String, //可预览diff的条目集合
    curItemIndexAtDiffableItemList:Int,
    localAtDiffRight:Boolean,
    naviUp: () -> Boolean,
) {

//    val isWorkTree = fromTo == Cons.gitDiffFromIndexToWorktree
    //废弃，改用diffContent里获取diffItem时动态计算了
//    val fileSizeOverLimit = isFileSizeOverLimit(fileSize)
    val dbContainer = AppModel.singleInstanceHolder.dbContainer
    val homeTopBarScrollBehavior = AppModel.singleInstanceHolder.homeTopBarScrollBehavior

    val activityContext = AppModel.singleInstanceHolder.activityContext

    val isFileHistoryTreeToLocal = fromTo == Cons.gitDiffFileHistoryFromTreeToLocal
    val isFileHistoryTreeToTree = fromTo == Cons.gitDiffFileHistoryFromTreeToTree

    val scope = rememberCoroutineScope()
    val settings = remember { SettingsUtil.getSettingsSnapshot() }

    val clipboardManager = LocalClipboardManager.current

    val treeOid1Str = rememberSaveable { mutableStateOf(
        if(fromTo == Cons.gitDiffFromIndexToWorktree) {
            Cons.gitIndexCommitHash
        }else if(fromTo == Cons.gitDiffFromHeadToIndex) {
            Cons.gitHeadCommitHash
        }else{
            treeOid1Str
        }
    ) }
    val treeOid2Str = rememberSaveable { mutableStateOf(
        if(fromTo == Cons.gitDiffFromIndexToWorktree) {
            Cons.gitLocalWorktreeCommitHash
        }else if(fromTo == Cons.gitDiffFromHeadToIndex) {
            Cons.gitIndexCommitHash
        }else {
            treeOid2Str
        }
    ) }

    //这个值存到状态变量里之后就不用管了，与页面共存亡即可，如果旋转屏幕也没事，返回rememberSaveable可恢复
//    val relativePathUnderRepoDecoded = (Cache.Map.getThenDel(Cache.Map.Key.diffScreen_UnderRepoPath) as? String)?:""
    val relativePathUnderRepoState = rememberSaveable { mutableStateOf((Cache.getByTypeThenDel<String>(underRepoPathKey)) ?: "")}

    val diffableItemList = mutableCustomStateListOf(stateKeyTag, "diffableItemList") {
        if(isFileHistoryTreeToLocal || isFileHistoryTreeToTree) {
            listOf()
        } else {
            (Cache.getByTypeThenDel<List<StatusTypeEntrySaver>>(diffableItemListKey)) ?: listOf()
        }
    }
    val diffableItemListForFileHistory = mutableCustomStateListOf(stateKeyTag, "diffableItemListForFileHistory") {
        if(isFileHistoryTreeToLocal || isFileHistoryTreeToTree) {
            (Cache.getByTypeThenDel<List<FileHistoryDto>>(diffableItemListKey)) ?: listOf()
        }else {
            listOf()
        }
    }
    val curItemIndex = rememberSaveable { mutableIntStateOf(curItemIndexAtDiffableItemList) }
    val changeType = rememberSaveable { mutableStateOf(changeType) }
    val fileSize = rememberSaveable { mutableLongStateOf(fileSize) }
    val isSubmodule = rememberSaveable { mutableStateOf(isSubmodule) }

    val enableSelectCompare = rememberSaveable { mutableStateOf(changeType.value == Cons.gitStatusModified && settings.diff.enableSelectCompare) }


//    val curRepo = rememberSaveable { mutableStateOf(RepoEntity()) }
    val curRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "curRepo", initValue = RepoEntity())
    val fileNameOnly = remember{ derivedStateOf {  getFileNameFromCanonicalPath(relativePathUnderRepoState.value)} }
    val fileParentPathOnly = remember{ derivedStateOf {getParentPathEndsWithSeparator(relativePathUnderRepoState.value)}}

    //考虑将这个功能做成开关，所以用状态变量存其值
    //ps: 这个值要么做成可在设置页面关闭（当然，其他与预览diff不相关的页面也行，总之别做成只能在正在执行O(nm)的diff页面开关就行），要么就默认app启动后重置为关闭，绝对不能做成只能在预览diff的页面开关，不然万一O(nm)算法太慢卡死导致这个东西关不了就尴尬了
    //20240618:目前临时开启O(nm)算法的机制是在预览diff页面三击屏幕，但app启动时会重置为关闭，日后需要添加相关设置项以方便用户使用
    val requireBetterMatchingForCompare = rememberSaveable { mutableStateOf(settings.diff.enableBetterButSlowCompare) }
    val adjustFontSizeModeOn = rememberSaveable { mutableStateOf(false) }
    val adjustLineNumSizeModeOn = rememberSaveable { mutableStateOf(false) }
    val showLineNum = rememberSaveable { mutableStateOf(settings.diff.showLineNum) }
    val showOriginType = rememberSaveable { mutableStateOf(settings.diff.showOriginType) }
    val fontSize = rememberSaveable { mutableIntStateOf(settings.diff.fontSize) }
    val lineNumFontSize = rememberSaveable { mutableIntStateOf(settings.diff.lineNumFontSize) }
    val groupDiffContentByLineNum = rememberSaveable { mutableStateOf(settings.diff.groupDiffContentByLineNum) }

    // this loading not shown as default, only show when executing action,
    //  use DiffContent's loading state indicating is loading diff content or not
    val loadingForAction= rememberSaveable { mutableStateOf(false)}
    val loadingText = rememberSaveable { mutableStateOf("")}
    val loadingOn = { text:String ->
        loadingText.value = text
        loadingForAction.value = true
    }
    val loadingOff = {
        loadingForAction.value = false
        loadingText.value = ""
    }

    val needRefresh = rememberSaveable { mutableStateOf("")}

    val request = rememberSaveable { mutableStateOf("")}

    val listState = rememberLazyListState()
    val fileFullPath = remember{ derivedStateOf{curRepo.value.fullSavePath + File.separator + relativePathUnderRepoState.value}}

    val isFileAndExist = remember(fileFullPath.value) { derivedStateOf {
        val f= File(fileFullPath.value)
        f.exists() && f.isFile
    } }

    // now is "read-only" mode
    val readOnlySwitchable = remember(localAtDiffRight, fileFullPath.value) { derivedStateOf {
        localAtDiffRight && isFileAndExist.value
    }}

    // cant switch = force readonly, else init value set to off
    // 不能切换等于强制只读，否则初始值设为关闭只读模式
    val readOnlyModeOn = rememberSaveable(readOnlySwitchable.value) { mutableStateOf(readOnlySwitchable.value.not()) }

    val enableLineTapMenu = remember(isSubmodule.value, changeType.value, fileFullPath.value, localAtDiffRight, readOnlyModeOn.value) {
        derivedStateOf {
            // only check when local as diff right(xxx..local)
            if(localAtDiffRight.not() || readOnlyModeOn.value || isSubmodule.value || (changeType.value != Cons.gitStatusNew && changeType.value != Cons.gitStatusModified)){
                false
            }else{
                val f= File(fileFullPath.value)
                f.exists() && f.isFile
            }
        }
    }



    val showBackFromExternalAppAskReloadDialog = rememberSaveable { mutableStateOf(false)}
    if(showBackFromExternalAppAskReloadDialog.value) {
        ConfirmDialog(
            title = stringResource(id = R.string.reload_file),
            text = stringResource(R.string.back_editor_from_external_app_ask_reload),
            okBtnText = stringResource(id = R.string.reload),
            onCancel = { showBackFromExternalAppAskReloadDialog.value=false }
        ) {
            //onOk
            showBackFromExternalAppAskReloadDialog.value=false
            changeStateTriggerRefreshPage(needRefresh)
        }
    }


    val showOpenAsDialog = rememberSaveable { mutableStateOf(false)}
    val openAsDialogFilePath = rememberSaveable { mutableStateOf("")}
//    val showOpenInEditor = StateUtil.getRememberSaveableState(initValue = false)
    if(showOpenAsDialog.value) {
        OpenAsDialog(fileName=fileNameOnly.value, filePath = openAsDialogFilePath.value,
            openSuccessCallback = {
                //只有在worktree的diff页面才有必要显示弹窗，在index页面没必要显示，在diff commit的页面更没必要显示，因为若修改，肯定是修改worktree的文件，你在index页面就算重载也看不到修改后的内容，所以没必要提示
                if(fromTo == Cons.gitDiffFromIndexToWorktree) {
                    //如果请求外部打开成功，不管用户有无选择app（想实现成选择才询问是否重新加载，但无法判断）都询问是否重载文件
                    showBackFromExternalAppAskReloadDialog.value=true  // 显示询问是否重载的弹窗
                }
            }
        ) {
            //onClose
            showOpenAsDialog.value=false
        }
    }

    val detailsString = rememberSaveable { mutableStateOf("")}
    val showDetailsDialog = rememberSaveable { mutableStateOf(false)}
    if(showDetailsDialog.value){
        CopyableDialog(
            title = stringResource(id = R.string.details),
            text = detailsString.value,
            onCancel = { showDetailsDialog.value = false }
        ) {
            showDetailsDialog.value = false
            clipboardManager.setText(AnnotatedString(detailsString.value))
            Msg.requireShow(activityContext.getString(R.string.copied))
        }
    }

    val showRestoreDialog = rememberSaveable { mutableStateOf(false)}
    if(showRestoreDialog.value) {
        FileHistoryRestoreDialog(
            // only show restore for history
            targetCommitOidStr = if(isFileHistoryTreeToLocal){
                treeOid1Str.value
            }else { // isFileHistoryTreeToTree
                treeOid2Str.value
            },
            showRestoreDialog = showRestoreDialog,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            activityContext = activityContext,
            curRepo = curRepo,
            fileRelativePath = relativePathUnderRepoState.value,
            repoId = repoId,
            onSuccess = {
                if(isFileHistoryTreeToLocal) {
                    changeStateTriggerRefreshPage(needRefresh)
                }
            }
        )
    }


    if(request.value == PageRequest.showOpenAsDialog) {
        PageRequest.clearStateThenDoAct(request) {
            openAsDialogFilePath.value = fileFullPath.value
            showOpenAsDialog.value=true
        }
    }

    if(request.value == PageRequest.showRestoreDialog) {
        PageRequest.clearStateThenDoAct(request) {
            showRestoreDialog.value = true
        }
    }

    if(request.value == PageRequest.showDetails) {
        PageRequest.clearStateThenDoAct(request) {
            val sb = StringBuilder()
            if(treeOid1Str.value != Cons.allZeroOidStr || treeOid2Str.value!=Cons.allZeroOidStr){
                sb.append(activityContext.getString(R.string.comparing_label)+": ").appendLine("${Libgit2Helper.getShortOidStrByFull(treeOid1Str.value)}..${Libgit2Helper.getShortOidStrByFull(treeOid2Str.value)}").appendLine()
            }
            sb.append(activityContext.getString(R.string.name)+": ").appendLine(fileNameOnly.value).appendLine()
            if(isFileHistoryTreeToLocal){
                sb.append(activityContext.getString(R.string.commit_id)+": ").appendLine(treeOid1Str.value).appendLine()
                // at here: curItemIndex is on FileHistory, which item got clicked
                sb.append(activityContext.getString(R.string.entry_id)+": ").appendLine(diffableItemListForFileHistory.value[curItemIndex.intValue].treeEntryOidStr).appendLine()
            }else if(isFileHistoryTreeToTree){
                sb.append(activityContext.getString(R.string.commit_id)+": ").appendLine(treeOid2Str.value).appendLine()
                // at here: curItemIndex is on FileHistory, which item got long pressed
                sb.append(activityContext.getString(R.string.entry_id)+": ").appendLine(diffableItemListForFileHistory.value[curItemIndex.intValue].treeEntryOidStr).appendLine()
            }else {
                sb.append(activityContext.getString(R.string.change_type)+": ").appendLine(changeType.value).appendLine()
            }


            sb.append(activityContext.getString(R.string.path)+": ").appendLine(relativePathUnderRepoState.value).appendLine()

            sb.append(activityContext.getString(R.string.full_path)+": ").appendLine(fileFullPath.value)

            val file = File(fileFullPath.value)
            if(file.exists()) {
                sb.appendLine()
                if(file.isFile) {
                    sb.append(activityContext.getString(R.string.size)+": ").appendLine(getHumanReadableSizeStr(file.length())).appendLine()
                }
                sb.append(activityContext.getString(R.string.last_modified)+": ").appendLine(getFormattedLastModifiedTimeOfFile(file))
            }

            detailsString.value = sb.toString()
            showDetailsDialog.value=true
        }
    }




    // 向下滚动监听，开始
    val pageScrolled = remember { mutableStateOf(settings.showNaviButtons) }
//    val firstVisible = remember { derivedStateOf { listState.value } }
//    ScrollListener(
//        nowAt = firstVisible.value,
//        onScrollUp = {scrollingDown.value = false}
//    ) { // onScrollDown
//        scrollingDown.value = true
//    }
//
//    val lastAt = remember { mutableIntStateOf(0) }
//    val lastIsScrollDown = remember { mutableStateOf(false) }
//    val forUpdateScrollState = remember {
//        derivedStateOf {
//            val nowAt = listState.value
//
//            val scrolledDown = nowAt > lastAt.intValue  // scroll down
////            val scrolledUp = nowAt < lastAt.intValue
//
//            val scrolled = nowAt != lastAt.intValue  // scrolled
//            lastAt.intValue = nowAt
//
//            // only update state when this scroll down and last is not scroll down, or this is scroll up and last is not scroll up
//            if(scrolled && ((lastIsScrollDown.value && !scrolledDown) || (!lastIsScrollDown.value && scrolledDown))) {
//                pageScrolled.value = true
//            }
//
//            lastIsScrollDown.value = scrolledDown
//        }
//    }.value
    // 向下滚动监听，结束

    val switchItem = {newItem:StatusTypeEntrySaver, newItemIndex:Int->
        changeType.value = newItem.changeType ?:""
        isSubmodule.value = newItem.itemType == Cons.gitItemTypeSubmodule
        fileSize.longValue = newItem.fileSizeInBytes
        relativePathUnderRepoState.value = newItem.relativePathUnderRepo

        curItemIndex.intValue = newItemIndex

        changeStateTriggerRefreshPage(needRefresh)
    }

    val switchItemForFileHistory = {newItem:FileHistoryDto, newItemIndex:Int->
        curItemIndex.intValue = newItemIndex
        treeOid1Str.value = newItem.commitOidStr
        changeStateTriggerRefreshPage(needRefresh)
    }

    val saveFontSizeAndQuitAdjust = {
        adjustFontSizeModeOn.value = false

        SettingsUtil.update {
            it.diff.fontSize = fontSize.intValue
        }

        Unit
    }
    val saveLineNumFontSizeAndQuitAdjust = {
        adjustLineNumSizeModeOn.value = false

        SettingsUtil.update {
            it.diff.lineNumFontSize = lineNumFontSize.intValue
        }

        Unit
    }

//    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true) }

    BackHandler(
        onBack = getBackHandler(
            naviUp = naviUp,
            adjustFontSizeMode=adjustFontSizeModeOn,
            adjustLineNumFontSizeMode=adjustLineNumSizeModeOn,
            saveFontSizeAndQuitAdjust = saveFontSizeAndQuitAdjust,
            saveLineNumFontSizeAndQuitAdjust = saveLineNumFontSizeAndQuitAdjust,
        )
    )


    Scaffold(
        modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    DiffScreenTitle(
                        fileName = fileNameOnly.value,
                        filePath = fileParentPathOnly.value,
                        fileRelativePathUnderRepoState = relativePathUnderRepoState,
                        listState,
                        scope,
                        request,
                        changeType.value
                    )
                },
                navigationIcon = {
                    if(adjustFontSizeModeOn.value || adjustLineNumSizeModeOn.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.close),
                            icon = Icons.Filled.Close,
                            iconContentDesc = stringResource(R.string.close),

                        ) {
                            if(adjustFontSizeModeOn.value) {
                                saveFontSizeAndQuitAdjust()
                            }else {
                                saveLineNumFontSizeAndQuitAdjust()
                            }
                        }
                    }else {
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
                    if(adjustFontSizeModeOn.value) {
                        FontSizeAdjuster(fontSize = fontSize, resetValue = SettingsCons.defaultFontSize)
                    }else if(adjustLineNumSizeModeOn.value){
                        FontSizeAdjuster(fontSize = lineNumFontSize, resetValue = SettingsCons.defaultLineNumFontSize)
                    }else {
                        DiffPageActions(
                            fromTo=fromTo,
                            changeType=changeType.value,
                            refreshPage = { changeStateTriggerRefreshPage(needRefresh) },
                            request = request,
                            fileFullPath = fileFullPath.value,
                            requireBetterMatchingForCompare = requireBetterMatchingForCompare,
                            copyModeOn = readOnlyModeOn,
                            copyModeSwitchable = readOnlySwitchable.value,
                            showLineNum=showLineNum,
                            showOriginType=showOriginType,
                            adjustFontSizeModeOn = adjustFontSizeModeOn,
                            adjustLineNumSizeModeOn = adjustLineNumSizeModeOn,
                            groupDiffContentByLineNum=groupDiffContentByLineNum,
                            enableSelectCompare=enableSelectCompare
                        )

                    }

                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(pageScrolled.value) {
                GoToTopAndGoToBottomFab(
                    scope = scope,
                    listState = listState,
                    showFab = pageScrolled
                )
            }
        }
    ) { contentPadding ->

        if(loadingForAction.value) {
            LoadingDialog(loadingText.value)
        }

//        if(fileSizeOverLimit) {  // 文件过大不加载
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
////                    .verticalScroll(rememberScrollState())
//                    .padding(contentPadding)
//                ,
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center,
//            ) {
//                Row {
//                    Text(text = stringResource(R.string.file_size_over_limit)+"("+Cons.editorFileSizeMaxLimitForHumanReadable+")")
//                }
//            }
//        }else {  //文件大小ok

        //改成统一在DiffContent里检查实际diff需要获取的内容的大小了，和文件大小有所不同，有时候文件大小很大，但需要diff的内容大小实际很小，这时其实可以diff，性能不会太差

        // if diff to local, enable edit menu and disable copy(because may cause app crashed), else disable edit menu but enable copy(no complex layout enable copy is ok)
        if(localAtDiffRight && readOnlyModeOn.value.not() && enableSelectCompare.value.not()){
            DiffContent(repoId=repoId,relativePathUnderRepoDecoded=relativePathUnderRepoState.value,
                fromTo=fromTo,changeType=changeType.value,fileSize=fileSize.longValue, naviUp=naviUp, dbContainer=dbContainer,
                contentPadding = contentPadding, treeOid1Str = treeOid1Str.value, treeOid2Str = treeOid2Str.value,
                needRefresh = needRefresh, listState = listState, curRepo=curRepo,
                requireBetterMatchingForCompare = requireBetterMatchingForCompare, fileFullPath = fileFullPath.value,
                isSubmodule=isSubmodule.value, isDiffToLocal = isDiffToLocal,
                diffableItemList= diffableItemList.value,diffableItemListForFileHistory=diffableItemListForFileHistory.value,
                curItemIndex=curItemIndex, switchItem=switchItem, clipboardManager=clipboardManager,
                loadingOnParent=loadingOn, loadingOffParent=loadingOff,isFileAndExist=enableLineTapMenu,
                showLineNum=showLineNum.value, showOriginType=showOriginType.value,
                fontSize=fontSize.intValue, lineNumSize=lineNumFontSize.intValue,
                groupDiffContentByLineNum=groupDiffContentByLineNum.value,switchItemForFileHistory=switchItemForFileHistory,
                enableSelectCompare = enableSelectCompare.value
            )
        }else {
            MySelectionContainer {
                DiffContent(repoId=repoId,relativePathUnderRepoDecoded=relativePathUnderRepoState.value,
                    fromTo=fromTo,changeType=changeType.value,fileSize=fileSize.longValue, naviUp=naviUp, dbContainer=dbContainer,
                    contentPadding = contentPadding, treeOid1Str = treeOid1Str.value, treeOid2Str = treeOid2Str.value,
                    needRefresh = needRefresh, listState = listState, curRepo=curRepo,
                    requireBetterMatchingForCompare = requireBetterMatchingForCompare, fileFullPath = fileFullPath.value,
                    isSubmodule=isSubmodule.value, isDiffToLocal = isDiffToLocal,
                    diffableItemList= diffableItemList.value,diffableItemListForFileHistory=diffableItemListForFileHistory.value,
                    curItemIndex=curItemIndex, switchItem=switchItem, clipboardManager=clipboardManager,
                    loadingOnParent=loadingOn, loadingOffParent=loadingOff, isFileAndExist=enableLineTapMenu,
                    showLineNum=showLineNum.value, showOriginType=showOriginType.value,
                    fontSize=fontSize.intValue, lineNumSize=lineNumFontSize.intValue,
                    groupDiffContentByLineNum=groupDiffContentByLineNum.value,switchItemForFileHistory=switchItemForFileHistory,
                    enableSelectCompare = enableSelectCompare.value



                )
            }
        }


//        }
    }



//    LaunchedEffect(Unit) {
//    }
}


@Composable
private fun getBackHandler(
    naviUp:()->Boolean,
    adjustFontSizeMode: MutableState<Boolean>,
    adjustLineNumFontSizeMode: MutableState<Boolean>,
    saveFontSizeAndQuitAdjust:()->Unit,
    saveLineNumFontSizeAndQuitAdjust:()->Unit,
): () -> Unit {
    val backHandlerOnBack = {
        if(adjustFontSizeMode.value) {
            saveFontSizeAndQuitAdjust()
        }else if(adjustLineNumFontSizeMode.value) {
            saveLineNumFontSizeAndQuitAdjust()
        }else {
            naviUp()
        }

        Unit
    }

    return backHandlerOnBack
}
