package com.catpuppyapp.puppygit.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.ConfirmDialog3
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.GoToTopAndGoToBottomFab
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.ScrollableColumn
import com.catpuppyapp.puppygit.compose.ScrollableRow
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.IntentCons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.constants.SingleSendHandleMethod
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.dto.FileSimpleDto
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.ScrollEvent
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.AboutInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.ChangeListInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.EditorInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.FilesInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.RepoInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.ServiceInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.SettingsInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.SubscriptionPage
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.ChangeListPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.EditorPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.FilesPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.RefreshActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.RepoPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.SubscriptionActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.drawer.drawerContent
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.ChangeListTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.EditorTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.FilesTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.ReposTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.ScrollableTitle
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.SimpleTitle
import com.catpuppyapp.puppygit.screen.functions.ChangeListFunctions
import com.catpuppyapp.puppygit.screen.shared.SharedState
import com.catpuppyapp.puppygit.settings.SettingsCons
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Repository.StateT
import kotlinx.coroutines.launch


private const val TAG = "HomeScreen"
private const val stateKeyTag = "HomeScreen"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
//    context: Context,
//    navController: NavController,
    drawerState: DrawerState,
//    scope: CoroutineScope,
//    scrollBehavior: TopAppBarScrollBehavior,
    currentHomeScreen: MutableIntState,
    repoPageListState: LazyListState,
    editorPageLastFilePath: MutableState<String>,

//    filePageListState: LazyListState,
//    haptic: HapticFeedback,
) {

    val exitApp = AppModel.exitApp
    val navController = AppModel.navController
    val scope = rememberCoroutineScope()
    val homeTopBarScrollBehavior = AppModel.homeTopBarScrollBehavior
//    val appContext = AppModel.appContext  //这个获取不了Activity!
    val activityContext = LocalContext.current  //这个能获取到
    val activity = ActivityUtil.getCurrentActivity()

//    val settingsTmp = remember { SettingsUtil.getSettingsSnapshot() }   //避免状态变量里的设置项过旧，重新获取一个

    val allRepoParentDir = AppModel.allRepoParentDir

    val settingsSnapshot = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "settingsSnapshot", initValue = SettingsUtil.getSettingsSnapshot())
    val showWelcomeToNewUser = rememberSaveable { mutableStateOf(false)}
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = MyStyleKt.BottomSheet.skipPartiallyExpanded)
    val showBottomSheet = rememberSaveable { mutableStateOf(false)}

    //替换成我的cusntomstateSaver，然后把所有实现parcellzier的类都取消实现parcellzier，改成用我的saver
//    val curRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
    val repoPageCurRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "repoPageCurRepo", initValue = RepoEntity(id=""))  //id=空，表示无效仓库
    //使用前检查，大于等于0才是有效索引
    val repoPageCurRepoIndex = rememberSaveable { mutableIntStateOf(-1)}

//    val repoPageRepoList = StateUtil.getCustomSaveableState(
//        keyTag = stateKeyTag,
//        keyDesc = "repoPageRepoList",
//        initValue =mutableListOf<RepoEntity>()
//    )

    val repoPageRepoList = mutableCustomStateListOf(stateKeyTag, "repoPageRepoList", listOf<RepoEntity>())

    val changeListRefreshRequiredByParentPage= rememberSaveable { mutableStateOf("WorktreeToIndex_ChangeList_refresh_init_value_5hpn")}

    //参数repoId用来实现如果在仓库a执行操作，然后切换了仓库，则仓库a的仓库执行完后不会刷新页面
    val changeListRequireRefreshFromParentPage = { whichRepoRequestRefresh:RepoEntity ->
        ChangeListFunctions.changeListDoRefresh(changeListRefreshRequiredByParentPage, whichRepoRequestRefresh)
    }
//    val changeListCurRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }
    val changeListCurRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "changeListCurRepo", initValue = RepoEntity(id=""))  //id=空，表示无效仓库
    val changeListIsShowRepoList = rememberSaveable { mutableStateOf(false)}
    val changeListShowRepoList = {
        changeListIsShowRepoList.value=true
    }
    val changeListIsFileSelectionMode = rememberSaveable { mutableStateOf(false)}
    val changeListPageNoRepo = rememberSaveable { mutableStateOf(false)}
    val changeListPageHasNoConflictItems = rememberSaveable { mutableStateOf(false)}
    val changeListPageRebaseCurOfAll = rememberSaveable { mutableStateOf("")}
    val changeListNaviTarget = rememberSaveable { mutableStateOf(Cons.ChangeListNaviTarget_InitValue)}

    val changeListPageFilterKeyWord = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "changeListPageFilterKeyWord",
        initValue = TextFieldValue("")
    )
    val changeListPageFilterModeOn = rememberSaveable { mutableStateOf(false) }

    val repoPageFilterKeyWord =mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "repoPageFilterKeyWord",
        initValue = TextFieldValue("")
    )
    val repoPageFilterModeOn = rememberSaveable { mutableStateOf(false) }
    val repoPageShowImportRepoDialog = rememberSaveable { mutableStateOf(false) }
    val repoPageGoToId = rememberSaveable { mutableStateOf("") }


    val subscriptionPageNeedRefresh = rememberSaveable { mutableStateOf("") }
    val needRefreshHome = rememberSaveable { mutableStateOf("") }

    val swapForChangeListPage = rememberSaveable { mutableStateOf(false) }


//    val editorPageRequireOpenFilePath = StateUtil.getRememberSaveableState(initValue = "") // canonicalPath
//    val needRefreshFilesPage = rememberSaveable { mutableStateOf(false) }
    val needRefreshFilesPage = rememberSaveable { mutableStateOf("") }

    val needRefreshSettingsPage = rememberSaveable { mutableStateOf("") }
    val refreshSettingsPage = { changeStateTriggerRefreshPage(needRefreshSettingsPage) }

    val needRefreshServicePage = rememberSaveable { mutableStateOf("") }
    val refreshServicePage = { changeStateTriggerRefreshPage(needRefreshServicePage) }

    val needRefreshAutomationPage = rememberSaveable { mutableStateOf("") }
    val refreshAutomationPage = { changeStateTriggerRefreshPage(needRefreshAutomationPage) }



    val settingsListState = rememberScrollState()
    val serviceListState = rememberScrollState()


    val filesPageIsFileSelectionMode = rememberSaveable { mutableStateOf(false)}
    val filesPageIsPasteMode = rememberSaveable { mutableStateOf(false)}
    val filesPageSelectedItems = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filesPageSelectedItems", initValue = listOf<FileItemDto>())

    val reposPageIsSelectionMode = rememberSaveable { mutableStateOf(false)}
    val reposPageSelectedItems = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "reposPageSelectedItems", initValue = listOf<RepoEntity>())
    val reposPageUnshallowItems = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "reposPageUnshallowItems", initValue = listOf<RepoEntity>())
    val reposPageDeleteItems = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "reposPageDeleteItems", initValue = listOf<RepoEntity>())
    val reposPageUserInfoRepoList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "reposPageUserInfoRepoList", initValue = listOf<RepoEntity>())
    val reposPageUpstreamRemoteOptionsList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "reposPageUpstreamRemoteOptionsList", initValue = listOf<String>())

    // 指定一个待刷新的仓库列表，若为空，则刷新全部仓库
    val reposPageSpecifiedRefreshRepoList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "reposPageSpecifiedRefreshRepoList", initValue = listOf<RepoEntity>())


    //这个filter有点重量级，比较适合做成全局搜索之类的功能
    val filesPageFilterMode = rememberSaveable{mutableIntStateOf(0)}  //0关闭，1正在搜索，显示输入框，2显示搜索结果
    val filesPageFilterKeyword = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filesPageFilterKeyword",
        initValue = TextFieldValue("")
    )
    val filesPageFilterTextFieldFocusRequester = remember { FocusRequester() }
    val filesPageFilterOn = {
        filesPageFilterMode.intValue = 1

        //若不为空，选中关键字
        val text = filesPageFilterKeyword.value.text
        if(text.isNotEmpty()) {
            filesPageFilterKeyword.value = filesPageFilterKeyword.value.copy(
                //这个TextRange，左闭右开，话说kotlin这一会左闭右闭一会左闭右开，有点难受
                selection = TextRange(0, text.length)
            )
        }
    }
    val filesPageFilterOff = {
        filesPageFilterMode.intValue = 0
        changeStateTriggerRefreshPage(needRefreshFilesPage)
    }
    val filesPageGetFilterMode = {
        filesPageFilterMode.intValue
    }
    val filesPageDoFilter= doFilter@{ keyWord:String ->
        //传参的话，优先使用，参数若为空，检查状态变量是否有数据，若有使用，若还是空，中止操作
        var needUpdateFieldState = true  //如果不是从状态变量获取的关键字，则更新状态变量为关键字

        var key = keyWord
        if(key.isEmpty()) {  //注意是empty，不要用blank，不然不能搜索空格，但文件名可能包含空格
            key = filesPageFilterKeyword.value.text
            if(key.isEmpty()) {
                Msg.requireShow(activityContext.getString(R.string.keyword_is_empty))
                return@doFilter
            }

            needUpdateFieldState = false  //这时，key本身就是从state获取的，所以不用再更新state了
        }

        if(needUpdateFieldState){
            filesPageFilterKeyword.value = TextFieldValue(key)  //设置关键字
        }

        filesPageFilterMode.intValue=2  // 设置搜索模式为显示结果
        changeStateTriggerRefreshPage(needRefreshFilesPage)  //刷新页面
    }

    val filesPageScrolled = rememberSaveable { mutableStateOf(settingsSnapshot.value.showNaviButtons)}
    val filesPageListState = mutableCustomStateOf(stateKeyTag, "filesPageListState", initValue = LazyListState(0,0))

    val filesPageSimpleFilterOn = rememberSaveable { mutableStateOf(false)}
    val filesPageSimpleFilterKeyWord = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "filesPageSimpleFilterKeyWord",
        initValue = TextFieldValue("")
    )

    val filesPageCurrentPath = rememberSaveable { mutableStateOf("")}
    val filesPageLastPathByPressBack = rememberSaveable { mutableStateOf("")}
    val showCreateFileOrFolderDialog = rememberSaveable { mutableStateOf(false)}
    val filesPageCurPathFileItemDto = mutableCustomStateOf(stateKeyTag, "filesPageCurPathFileItemDto") { FileItemDto() }
    val filesPageCurrentPathBreadCrumbList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filesPageCurrentPathBreadCrumbList", initValue = listOf<FileItemDto>())

    val showSetGlobalGitUsernameAndEmailDialog = rememberSaveable { mutableStateOf(false)}

    //判定过滤模式是否真开启的状态变量，例如虽然filterModeOn为真，但是，如果没输入任何关键字，过滤模式其实还是没开，这时这个变量会为假，但filterModeOn为真
    val repoPageEnableFilterState = rememberSaveable { mutableStateOf(false)}
    val filesPageEnableFilterState = rememberSaveable { mutableStateOf(false)}
    val changeListPageEnableFilterState = rememberSaveable { mutableStateOf(false)}
    val reposPageFilterList = mutableCustomStateListOf(stateKeyTag, "reposPageFilterList", listOf<RepoEntity>() )
    val filesPageFilterList = mutableCustomStateListOf(stateKeyTag, "filesPageFilterList", listOf<FileItemDto>())
    val changeListFilterList = mutableCustomStateListOf(stateKeyTag,"changeListFilterList", listOf<StatusTypeEntrySaver>())

    val changeListLastClickedItemKey = rememberSaveable{ SharedState.homeChangeList_LastClickedItemKey }

//    val changelistFilterListState = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "changelistFilterListState", LazyListState(0,0))
    val changelistFilterListState = rememberLazyListState()

//    val filesFilterListState = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "filesFilterListState", LazyListState(0,0));
//    val repoFilterListState = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "repoFilterListState", LazyListState(0,0))
    val filesFilterListState = rememberLazyListState()
    val repoFilterListState = rememberLazyListState()

    //当前展示的文件的canonicalPath
    val editorPageShowingFilePath = rememberSaveable { mutableStateOf("")}
    //当前展示的文件是否已经加载完毕
    val editorPageShowingFileIsReady = rememberSaveable { mutableStateOf(false)}
    //TextEditor用的变量
    val editorPageTextEditorState = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "editorPageTextEditorState",
        initValue = TextEditorState.create(text = "", fieldsId = "")
    )
    val editorPageLastTextEditorState = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "editorPageLastTextEditorState",
        initValue = TextEditorState.create(text = "", fieldsId = "")
    )
//    val editorPageShowSaveDoneToast = rememberSaveable { mutableStateOf(false)}
//    val needRefreshEditorPage = rememberSaveable { mutableStateOf(false) }
    val needRefreshEditorPage = rememberSaveable { mutableStateOf("")}
    val editorPageIsSaving = rememberSaveable { mutableStateOf(false)}
    val editorPageIsEdited = rememberSaveable { mutableStateOf( false)}
    val showReloadDialog = rememberSaveable { mutableStateOf( false)}

    val changeListHasIndexItems = rememberSaveable { mutableStateOf( false)}
//    val changeListRequirePull = rememberSaveable { mutableStateOf(false)}
//    val changeListRequirePush = rememberSaveable { mutableStateOf(false)}
    val changeListRequireDoActFromParent = rememberSaveable { mutableStateOf( false)}
    val changeListRequireDoActFromParentShowTextWhenDoingAct = rememberSaveable { mutableStateOf("")}
    val changeListEnableAction = rememberSaveable { mutableStateOf( true)}
    val changeListCurRepoState = rememberSaveable{mutableIntStateOf(StateT.NONE.bit)}  //初始状态是NONE，后面会在ChangeListInnerPage检查并更新状态，只要一创建innerpage或刷新（重新执行init），就会更新此状态
    val changeListPageFromTo = Cons.gitDiffFromIndexToWorktree
    val changeListPageItemList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "changeListPageItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changeListPageItemListState = rememberLazyListState()
    val changeListPageSelectedItemList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "changeListPageSelectedItemList", initValue = listOf<StatusTypeEntrySaver>())
    val changelistNewestPageId = rememberSaveable { mutableStateOf("") }

    val changeListPageDropDownMenuItemOnClick={item:RepoEntity->
        //如果切换仓库，清空选中项列表
        if(changeListCurRepo.value.id != item.id) {
            changeListPageSelectedItemList.value.clear()
//            changeListPageSelectedItemList.requireRefreshView()
        }
        changeListCurRepo.value=item
        changeListRequireRefreshFromParentPage(item)
    }

    val editorPageShowingFileName = rememberSaveable { mutableStateOf<String?>(null) }
    val filesPageRequireImportFile = rememberSaveable { mutableStateOf( false)}
    val intentConsumed = rememberSaveable { mutableStateOf(false)}  //此变量用来确保导入模式只启动一次，避免以导入模式进入app后，进入子页面再返回再次以导入模式进入Files页面
    val filesPageRequireImportUriList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filesPageRequireImportUriList", initValue = listOf<Uri>())
    val filesPageCurrentPathFileList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "filesPageCurrentPathFileList", initValue = listOf<FileItemDto>()) //路径字符串，用路径分隔符分隔后的list
    val filesPageRequestFromParent = rememberSaveable { mutableStateOf("")}
    val filesPageCheckOnly = rememberSaveable { mutableStateOf(false)}
    val filesPageSelectedRepo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "filesPageSelectedRepo", RepoEntity(id="") )

    val howToDealWithSingleSend = rememberSaveable { mutableStateOf(SingleSendHandleMethod.NEED_ASK.code) }
    val showAskHandleSingleSendMethod = rememberSaveable { mutableStateOf(false)}
    val cancelAskHandleSingleSendMethod = {
        //设为已经消费，避免重入
        intentConsumed.value = true
        //关闭弹窗
        showAskHandleSingleSendMethod.value = false
    }

    val okAskHandleSingleSendMethod = { handleMethod:SingleSendHandleMethod ->
        howToDealWithSingleSend.value = handleMethod.code
        showAskHandleSingleSendMethod.value = false
        changeStateTriggerRefreshPage(needRefreshHome)
    }

    if(showAskHandleSingleSendMethod.value) {
        ConfirmDialog3(
            title = stringResource(R.string.ask),
            text = stringResource(R.string.do_you_want_to_edit_the_file_or_import_it),
            onCancel = cancelAskHandleSingleSendMethod,
            customOk = {
                ScrollableRow {
                    TextButton(
                        onClick = {
                            okAskHandleSingleSendMethod(SingleSendHandleMethod.EDIT)
                        }
                    ) {
                        Text(stringResource(id = R.string.edit))
                    }

                    TextButton(
                        onClick = {
                            okAskHandleSingleSendMethod(SingleSendHandleMethod.IMPORT)
                        }
                    ) {
                        Text(stringResource(id = R.string.import_str))
                    }
                }
            }
        ) { }

    }

    val initDone = rememberSaveable { mutableStateOf(false)}
    val editorPageShowCloseDialog = rememberSaveable { mutableStateOf(false)}

    val editorPageCloseDialogCallback = mutableCustomStateOf(
        keyTag = stateKeyTag,
        keyName = "editorPageCloseDialogCallback",
        initValue = { requireSave:Boolean -> }
    )
    val initLoadingText = activityContext.getString(R.string.loading)
    val loadingText = rememberSaveable { mutableStateOf(initLoadingText)}

    val editorPageIsLoading = rememberSaveable { mutableStateOf(false)}
    if(editorPageIsLoading.value) {
        LoadingDialog(loadingText.value)
    }
    val editorPageLoadingOn = {msg:String ->
        loadingText.value = msg
        editorPageIsLoading.value=true
//        Msg.requireShow(msg)
        //这里不需要请求这个东西，否则会无限闪屏
//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }
    val editorPageLoadingOff = {
        editorPageIsLoading.value=false
        loadingText.value = initLoadingText

//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }

    val editorPageRequestFromParent = rememberSaveable { mutableStateOf("")}
    val editorPageShowingFileDto = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "editorPageShowingFileDto",FileSimpleDto() )
    val editorPageSnapshotedFileInfo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "editorPageSnapshotedFileInfo",FileSimpleDto())
    val editorPageLastScrollEvent = mutableCustomStateOf<ScrollEvent?>(keyTag = stateKeyTag, keyName = "editorPageLastScrollEvent") { null }  //这个用remember就行，没必要在显示配置改变时还保留这个滚动状态，如果显示配置改变，直接设为null，从配置文件读取滚动位置重定位更好
    val editorPageLazyListState = rememberLazyListState()
    val editorPageIsInitDone = rememberSaveable{mutableStateOf(false)}  //这个也用remember就行，无需在配置改变时保存此状态，直接重置成false就行
    val editorPageIsContentSnapshoted = rememberSaveable{mutableStateOf(false)}  //是否已对当前内容创建了快照
    val editorPageSearchMode = rememberSaveable{mutableStateOf(false)}
    val editorPageSearchKeyword = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "editorPageSearchKeyword", TextFieldValue(""))
    val editorPageMergeMode = rememberSaveable{mutableStateOf(false)}
    val editorReadOnlyMode = rememberSaveable{mutableStateOf(false)}

    val editorShowLineNum = rememberSaveable{mutableStateOf(settingsSnapshot.value.editor.showLineNum)}
    val editorLineNumFontSize = rememberSaveable { mutableIntStateOf( settingsSnapshot.value.editor.lineNumFontSize)}
    val editorLastSavedLineNumFontSize = rememberSaveable { mutableIntStateOf( editorLineNumFontSize.intValue) } //用来检查，如果没变，就不执行保存，避免写入硬盘
    val editorFontSize = rememberSaveable { mutableIntStateOf( settingsSnapshot.value.editor.fontSize)}
    val editorLastSavedFontSize = rememberSaveable { mutableIntStateOf(editorFontSize.intValue)}
    val editorAdjustFontSizeMode = rememberSaveable{mutableStateOf(false)}
    val editorAdjustLineNumFontSizeMode = rememberSaveable{mutableStateOf(false)}
    val editorOpenFileErr = rememberSaveable{mutableStateOf(false)}
    val editorShowUndoRedo = rememberSaveable{mutableStateOf(settingsSnapshot.value.editor.showUndoRedo)}
    val editorUndoStack = mutableCustomStateOf<UndoStack?>(stateKeyTag, "editorUndoStack") { null }


    //给Files页面点击打开文件用的
    //第2个参数是期望值，只有当文件路径不属于app内置禁止edit的目录时才会使用那个值，否则强制开启readonly模式
    val requireInnerEditorOpenFileWithFileName = { fullPath:String, expectReadOnly:Boolean, fileName:String? ->
        //文件名后来加的，用来从外部打开content uri file uri那类路径时使用提取的文件名，不然content://开头的那种uri可能会拿到错误文件名
        editorPageShowingFileName.value = fileName

        editorPageShowingFileIsReady.value=false
        editorPageShowingFilePath.value = fullPath
        editorPageShowingFileDto.value.fullPath = ""
        currentHomeScreen.intValue = Cons.selectedItem_Editor

        editorPageMergeMode.value = false  //这个页面不负责打开ChangeList页面的diff条目，所以MergeMode状态直接初始化为关即可，用户需要用的时候打开文件后再手动开即可
        //如果路径是app内置禁止修改的目录，则强制开启readonly，否则使用入参值
//        editorReadOnlyMode.value = if(FsUtils.isReadOnlyDir(fullPath)) true else expectReadOnly
        editorReadOnlyMode.value = expectReadOnly

        changeStateTriggerRefreshPage(needRefreshEditorPage)  //这个其实可有可无，因为一切换页面，组件会重建，必然会执行一次LaunchedEffect，也就起到了刷新的作用
    }

    val requireInnerEditorOpenFile = { fullPath:String, expectReadOnly:Boolean ->
        val fileName:String? = null
        //传null会从文件路径解析文件名
        requireInnerEditorOpenFileWithFileName(fullPath, expectReadOnly, fileName)
    }


    val needRefreshRepoPage = rememberSaveable { mutableStateOf("")}
    val doSave: suspend () -> Unit = FsUtils.getDoSaveForEditor(
        editorPageShowingFilePath = editorPageShowingFilePath,
        editorPageLoadingOn = editorPageLoadingOn,
        editorPageLoadingOff = editorPageLoadingOff,
        appContext = activityContext,
        editorPageIsSaving = editorPageIsSaving,
        needRefreshEditorPage = needRefreshEditorPage,
        editorPageTextEditorState = editorPageTextEditorState,
        pageTag = TAG,
        editorPageIsEdited = editorPageIsEdited,
        requestFromParent = editorPageRequestFromParent,
        editorPageFileDto = editorPageShowingFileDto,
        isSubPageMode = false,
        isContentSnapshoted = editorPageIsContentSnapshoted,
        snapshotedFileInfo = editorPageSnapshotedFileInfo
    )


    val goToRepoPage = { targetIdIfHave:String ->
        //跳转到Repos页面不用关过滤模式，因为做了额外处理，
        // 会根据是否开过滤模式决定查哪个列表，并且如果过滤后的列表无条目，
        // 会强制从原始列表查询，如果查询到，将退出过滤模式并显示条目
        // 如果后续发现bug，也强制关过滤模式，
        // 理论上来说是这样，但是，强制关过滤模式似乎也没什么问题，
        // 而且和其他页面的跳转逻辑一致，所以还是关
        repoPageFilterModeOn.value = false  //过滤输入框是否显示
        repoPageEnableFilterState.value = false  //过滤模式是否真的被应用（显示输入框并且有关键字）

        //目标仓库id，如果有的话
        repoPageGoToId.value = targetIdIfHave
        //跳转页面
        currentHomeScreen.intValue = Cons.selectedItem_Repos
        //可有可无的刷新
        changeStateTriggerRefreshPage(needRefreshRepoPage)
    }

    val goToFilesPage = {path:String ->
        //先把过滤模式关球了
        filesPageSimpleFilterOn.value = false  //过滤输入框是否显示
        filesPageEnableFilterState.value = false  //过滤模式是否真的被应用（显示输入框并且有关键字）

        //设置路径
        filesPageCurrentPath.value = path
        //跳转页面
        currentHomeScreen.intValue = Cons.selectedItem_Files

        //可有可无的刷新，因为如果从其他页面跳转，不管needRefreshFilesPage值为什么，都肯定执行一次FilesInnerPage的LaunchedEffect
        changeStateTriggerRefreshPage(needRefreshFilesPage)
    }

    val goToChangeListPage = { repoWillShowInChangeListPage: RepoEntity ->
        //先关过滤模式
        changeListPageFilterModeOn.value = false  //过滤输入框是否显示
        changeListPageEnableFilterState.value = false  //过滤模式是否真的被应用（显示输入框并且有关键字）

        //设置目标仓库
        changeListCurRepo.value = repoWillShowInChangeListPage
        //跳转页面
        currentHomeScreen.intValue = Cons.selectedItem_ChangeList

        changeListRequireRefreshFromParentPage(repoWillShowInChangeListPage)
    }



    val changelistPageScrolled = rememberSaveable { mutableStateOf(settingsSnapshot.value.showNaviButtons) }
    val repoPageScrolled = rememberSaveable { mutableStateOf(settingsSnapshot.value.showNaviButtons) }

    // two usages: 1. re query repo list when click title;  2. after imported submodules at ChangeList page
    val needReQueryRepoListForChangeListTitle = rememberSaveable { mutableStateOf("")}
    val changeListRepoList = mutableCustomStateListOf(keyTag = stateKeyTag, keyName = "changeListRepoList", initValue = listOf<RepoEntity>())


    //用不到这段代码了，当初用这个是因为有些地方不能用Toast，后来发现直接withMainContext就可在任何地方用Toast了
    //这一堆判断只是为了确保这代码能被运行
    //不加会漏消息，妈的这个狗屁组件
//    if (needRefreshEditorPage.value != needRefreshFilesPage.value
//        || changeListRefreshRequiredByParentPage.value != needRefreshRepoPage.value
//    ) {
//        SideEffect {
//            MsgQueue.showAndClearAllMsg()
//        }
//    }

    val closeWelcome = {
        //关闭弹窗
        showWelcomeToNewUser.value=false

//        //更新设置项
//        val settingsWillSave = SettingsUtil.getSettingsSnapshot()
//        settingsWillSave.firstUse = false
//        SettingsUtil.updateSettings(settingsWillSave)  //更新配置文件
//
//        //更新设置项状态变量
//        settingsSnapshot.value=settingsWillSave  //更新当前页面的设置项为最新

        //更新配置文件并更新页面存储的app配置状态变量
        settingsSnapshot.value = SettingsUtil.update(true) {
            it.firstUse = false
        }!!
    }
    //显示欢迎弹窗
    if(showWelcomeToNewUser.value) {
        AlertDialog(
            title = {
                Text(stringResource(id = R.string.welcome))
            },
            text = {
                ScrollableColumn {
                    Row {
                        Text(text = stringResource(id = R.string.welcome)+"!")
                    }

                    Row (modifier = Modifier.padding(top = 10.dp)){
                        Text(text = stringResource(id = R.string.tips)+":"+ stringResource(R.string.try_long_press_icon_get_hints))
                    }

                    // whatever, the master password actually 没什么卵用，每次都要输麻烦，若存，就不绝对安全，而且很多时候没必要啊，数据库在用户手机本地，app空间内，一般不会泄漏，不建议设了，虽然设了还是比不设好一点点
//                    Row (modifier = Modifier.padding(top = 15.dp)){
//                        Text(text = stringResource(R.string.recommend_go_to_settings_screen_set_a_master_password), color = MyStyleKt.TextColor.danger())
//                    }
                }
            },
            //点击弹框外区域的时候触发此方法，一般设为和OnCancel一样的行为即可
            onDismissRequest = {closeWelcome()},
            dismissButton = {},  //禁用取消按钮，一个按钮就够了
            confirmButton = {
                TextButton(
                    enabled = true,
                    onClick = {
                        //执行用户传入的callback
                        closeWelcome()
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.ok),
                    )
                }
            },

            )

    }

//    if(editorPageShowSaveDoneToast.value) {
//        showToast(context = appContext, appContext.getString(R.string.file_saved), Toast.LENGTH_SHORT)
//        editorPageShowSaveDoneToast.value=false
//    }
    val drawTextList = listOf(
        stringResource(id = R.string.repos),
        stringResource(id = R.string.files),
        stringResource(id = R.string.editor),
        stringResource(id = R.string.changelist),
        stringResource(id = R.string.service),
        stringResource(id = R.string.automation),
        stringResource(id = R.string.settings),
        stringResource(id = R.string.about),
//        stringResource(id = R.string.subscription),
    )
    val drawIdList = listOf(
        Cons.selectedItem_Repos,
        Cons.selectedItem_Files,
        Cons.selectedItem_Editor,
        Cons.selectedItem_ChangeList,
        Cons.selectedItem_Service,
        Cons.selectedItem_Automation,
        Cons.selectedItem_Settings,
        Cons.selectedItem_About,
//        Cons.selectedItem_Subscription,
    )
    val drawIconList = listOf(
        Icons.Filled.Inventory,
        Icons.Filled.Folder,
        Icons.Filled.EditNote,
        Icons.Filled.Difference,
        Icons.Filled.Cloud,
        Icons.Filled.AutoFixHigh,
        Icons.Filled.Settings,
        Icons.Filled.Info,
//        Icons.Filled.Subscriptions
    )

    val refreshPageList = listOf(
        refreshRepoPage@{ changeStateTriggerRefreshPage(needRefreshRepoPage) },
        refreshFilesPage@{ changeStateTriggerRefreshPage(needRefreshFilesPage) },
        refreshEditorPage@{ editorPageShowingFileIsReady.value=false; changeStateTriggerRefreshPage(needRefreshEditorPage) },
        refreshChangeListPage@{changeListRequireRefreshFromParentPage(changeListCurRepo.value)},
        refreshServicePage@{ refreshServicePage() },
        refreshAutomationPage@{ refreshAutomationPage() },
        refreshSettingsPage@{ refreshSettingsPage() },
        refreshAboutPage@{}, //About页面静态的，不需要刷新
//        {},  //Subscription页面
    )

    val openDrawer = {  //打开侧栏(抽屉)
        scope.launch {
            drawerState.apply {
                if (isClosed) open()
            }
        }

        Unit
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                //侧栏菜单展开占屏幕宽度的比例
                //抽屉会过大或过小，然后闪烁一下变成目标宽度，会闪烁，不太好
//                modifier= if(drawerState.isOpen) Modifier.fillMaxWidth(.8F) else Modifier,
                //之前是250dp，显示不全广告，改成320了，正好能显示全
                modifier= Modifier
                    .fillMaxHeight()
                    .widthIn(max = 320.dp)
                    .verticalScroll(rememberScrollState())
                ,
                drawerShape = RectangleShape,
                content = drawerContent(
                    currentHomeScreen = currentHomeScreen,
                    scope = scope,
                    drawerState = drawerState,
                    drawerItemShape = RectangleShape,
                    drawTextList = drawTextList,
                    drawIdList = drawIdList,
                    drawIconList = drawIconList,
                    refreshPageList = refreshPageList,
                    showExit = true
                )
            )
        },
    ) {

        Scaffold(
            modifier = Modifier.nestedScroll(homeTopBarScrollBehavior.nestedScrollConnection)
            ,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        //TODO 把app标题放到抽屉里，最好再有个长方形的背景图
                        if(currentHomeScreen.intValue == Cons.selectedItem_Repos){
                            if(repoPageFilterModeOn.value) {
                                FilterTextField(
                                    repoPageFilterKeyWord,
                                )
                            }else {
                                ReposTitle(repoPageListState, scope, repoPageRepoList.value.size)
                            }
                        } else if(currentHomeScreen.intValue == Cons.selectedItem_Files){
                            FilesTitle(filesPageCurrentPath, allRepoParentDir, needRefreshFilesPage, filesPageGetFilterMode,
                                filesPageFilterKeyword, filesPageFilterOn,filesPageDoFilter, filesPageRequestFromParent,
                                filesPageFilterTextFieldFocusRequester, filesPageSimpleFilterOn.value, filesPageSimpleFilterKeyWord,
                                filesPageCurPathFileItemDto.value
                            )
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_Editor) {
                            EditorTitle(editorPageShowingFileName.value, editorPageShowingFilePath,editorPageRequestFromParent, editorPageSearchMode.value, editorPageSearchKeyword, editorPageMergeMode.value, editorReadOnlyMode.value, editorOpenFileErr.value)
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_ChangeList) {
                            if(changeListPageFilterModeOn.value) {
                                FilterTextField(
                                    changeListPageFilterKeyWord,
                                )
                            }else{
                                ChangeListTitle(
                                    changeListCurRepo = changeListCurRepo,
                                    dropDownMenuItemOnClick = changeListPageDropDownMenuItemOnClick,
                                    repoState = changeListCurRepoState,
                                    isSelectionMode = changeListIsFileSelectionMode,
                                    listState = changeListPageItemListState,
                                    scope = scope,
                                    enableAction = changeListEnableAction.value,
                                    repoList = changeListRepoList,
                                    needReQueryRepoList=needReQueryRepoListForChangeListTitle,
                                )
                            }
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_Settings) {
                            ScrollableTitle(text = stringResource(R.string.settings), listState = settingsListState)
                        } else if (currentHomeScreen.intValue == Cons.selectedItem_About) {
                            SimpleTitle(stringResource(R.string.about))
                        } else if(currentHomeScreen.intValue == Cons.selectedItem_Subscription) {
                            SimpleTitle(stringResource(R.string.subscription))
                        } else if(currentHomeScreen.intValue == Cons.selectedItem_Service){
                            ScrollableTitle(text = stringResource(R.string.service), listState = serviceListState)
                        } else {
                            SimpleTitle()
                        }
                    },
                    navigationIcon = {
                        //如果是Files页面且开启过滤模式，则显示关闭按钮，否则显示菜单按钮
                        if(currentHomeScreen.intValue == Cons.selectedItem_Files
                            && (filesPageGetFilterMode() != 0 || filesPageSimpleFilterOn.value)
                        ) {
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
//                                filesPageFilterOff()
                                filesPageSimpleFilterOn.value = false
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList && changeListPageFilterModeOn.value){
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
                                changeListPageFilterModeOn.value=false
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Repos && repoPageFilterModeOn.value){
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
                                repoPageFilterModeOn.value=false
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Editor
                            && (editorPageSearchMode.value || editorAdjustFontSizeMode.value || editorAdjustLineNumFontSizeMode.value)
                        ){
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.close),
                                icon =  Icons.Filled.Close,
                                iconContentDesc = stringResource(R.string.close),

                            ) {
                                if(editorPageSearchMode.value) {
                                    editorPageSearchMode.value = false
                                }else if(editorAdjustFontSizeMode.value) {
                                    editorPageRequestFromParent.value = PageRequest.requireSaveFontSizeAndQuitAdjust
                                }else if(editorAdjustLineNumFontSizeMode.value) {
                                    editorPageRequestFromParent.value = PageRequest.requireSaveLineNumFontSizeAndQuitAdjust
                                }
                            }
                        }else {
                            LongPressAbleIconBtn(
                                tooltipText = stringResource(R.string.menu),
                                icon = Icons.Filled.Menu,
                                iconContentDesc = stringResource(R.string.menu),
                            ) {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }

                        }

                    },
                    actions = {
                        if(currentHomeScreen.intValue == Cons.selectedItem_Repos) {
                            if(!repoPageFilterModeOn.value){
                                RepoPageActions(navController, repoPageCurRepo, showSetGlobalGitUsernameAndEmailDialog, needRefreshRepoPage,
                                    repoPageFilterModeOn, repoPageFilterKeyWord,
                                    showImportRepoDialog = repoPageShowImportRepoDialog
                                )
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Files) {
                            FilesPageActions(showCreateFileOrFolderDialog,
                                refreshPage = {
                                    changeStateTriggerRefreshPage(needRefreshFilesPage)
                                },
                                filterOn = filesPageFilterOn,
                                filesPageGetFilterMode,
                                filesPageDoFilter,
                                filesPageRequestFromParent,
                                filesPageSimpleFilterOn,
                                filesPageSimpleFilterKeyWord
                            )

                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Editor && !editorOpenFileErr.value) {
                            EditorPageActions(
                                editorPageShowingFilePath,
//                                editorPageRequireOpenFilePath,
                                editorPageShowingFileIsReady,
                                needRefreshEditorPage,
                                editorPageTextEditorState,
//                                editorPageShowSaveDoneToast,
                                isSaving = editorPageIsSaving,
                                isEdited = editorPageIsEdited,
                                showReloadDialog = showReloadDialog,
                                showCloseDialog=editorPageShowCloseDialog,
                                closeDialogCallback = editorPageCloseDialogCallback,
//                                isLoading = editorPageIsLoading,
                                doSave = doSave,
                                loadingOn = editorPageLoadingOn,
                                loadingOff = editorPageLoadingOff,
                                editorPageRequest = editorPageRequestFromParent,
                                editorPageSearchMode=editorPageSearchMode,
                                editorPageMergeMode=editorPageMergeMode,
                                readOnlyMode = editorReadOnlyMode,
                                editorSearchKeyword = editorPageSearchKeyword.value.text,
                                isSubPageMode = false,

                                fontSize=editorFontSize,
                                lineNumFontSize=editorLineNumFontSize,
                                adjustFontSizeMode=editorAdjustFontSizeMode,
                                adjustLineNumFontSizeMode=editorAdjustLineNumFontSizeMode,
                                showLineNum = editorShowLineNum,
                                undoStack = editorUndoStack.value,
                                showUndoRedo = editorShowUndoRedo
                            )
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList) {
                            if(!changeListPageFilterModeOn.value){
                                ChangeListPageActions(
                                    changeListCurRepo,
                                    changeListRequireRefreshFromParentPage,
                                    changeListHasIndexItems,
    //                                requirePull = changeListRequirePull,
    //                                requirePush = changeListRequirePush,
                                    changeListRequireDoActFromParent,
                                    changeListRequireDoActFromParentShowTextWhenDoingAct,
                                    changeListEnableAction,
                                    changeListCurRepoState,
                                    fromTo = changeListPageFromTo,
                                    changeListPageItemListState,
                                    scope,
                                    changeListPageNoRepo=changeListPageNoRepo,
                                    hasNoConflictItems = changeListPageHasNoConflictItems.value,
                                    changeListPageFilterModeOn= changeListPageFilterModeOn,
                                    changeListPageFilterKeyWord=changeListPageFilterKeyWord,
                                    rebaseCurOfAll = changeListPageRebaseCurOfAll.value,
                                    naviTarget = changeListNaviTarget
                                )

                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Settings) {
//                            RefreshActions(refreshPage=refreshSettingsPage)
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Subscription) {
                            SubscriptionActions { // refreshPage
                                changeStateTriggerRefreshPage(subscriptionPageNeedRefresh)
                            }
                        }else if(currentHomeScreen.intValue == Cons.selectedItem_Service) {
                            RefreshActions(refreshServicePage)
                        }
                    },
                    scrollBehavior = homeTopBarScrollBehavior,
                )
            },
            floatingActionButton = {
                if(currentHomeScreen.intValue == Cons.selectedItem_Editor && (editorPageShowingFileIsReady.value && editorPageShowingFilePath.value.isNotBlank() && editorPageIsEdited.value && !editorPageIsSaving.value && !editorReadOnlyMode.value)) {
                    SmallFab(modifier = MyStyleKt.Fab.getFabModifierForEditor(editorPageTextEditorState.value.isMultipleSelectionMode),
                        icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)
                    ) {
                        editorPageRequestFromParent.value = PageRequest.requireSave
                    }
                }else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList && changelistPageScrolled.value) {

                    GoToTopAndGoToBottomFab(
                        filterModeOn = changeListPageFilterModeOn.value,
                        scope = scope,
                        filterListState = changelistFilterListState,
                        listState = changeListPageItemListState,
                        showFab = changelistPageScrolled
                    )

                }else if(currentHomeScreen.intValue == Cons.selectedItem_Repos && repoPageScrolled.value) {
                    GoToTopAndGoToBottomFab(
                        filterModeOn = repoPageFilterModeOn.value,
                        scope = scope,
                        filterListState = repoFilterListState,
                        listState = repoPageListState,
                        showFab = repoPageScrolled
                    )

                }else if(currentHomeScreen.intValue == Cons.selectedItem_Files && filesPageScrolled.value) {
                    GoToTopAndGoToBottomFab(
                        filterModeOn = filesPageSimpleFilterOn.value,
                        scope = scope,
                        filterListState = filesFilterListState,
                        listState = filesPageListState.value,
                        showFab = filesPageScrolled
                    )
                }
            }
        ) { contentPadding ->
            if(currentHomeScreen.intValue == Cons.selectedItem_Repos) {
//                changeStateTriggerRefreshPage(needRefreshRepoPage)
                RepoInnerPage(
                    showBottomSheet = showBottomSheet,
                    sheetState = sheetState,
                    curRepo = repoPageCurRepo,
                    curRepoIndex = repoPageCurRepoIndex,
                    contentPadding = contentPadding,
                    repoPageListState = repoPageListState,
                    showSetGlobalGitUsernameAndEmailDialog = showSetGlobalGitUsernameAndEmailDialog,
                    needRefreshRepoPage = needRefreshRepoPage,
//                    changeListCurRepo=changeListCurRepo,
//                    currentHomeScreen=currentHomeScreen,
//                    changeListNeedRefresh=changeListRefreshRequiredByParentPage,
                    repoList = repoPageRepoList,
//                    filesPageCurrentPath=filesPageCurrentPath,
//                    filesPageNeedRefresh=needRefreshFilesPage,
                    goToFilesPage = goToFilesPage,
                    goToChangeListPage = goToChangeListPage,
                    repoPageScrolled=repoPageScrolled,
                    repoPageFilterModeOn=repoPageFilterModeOn,
                    repoPageFilterKeyWord= repoPageFilterKeyWord,
                    filterListState = repoFilterListState,
                    openDrawer = openDrawer,
                    showImportRepoDialog = repoPageShowImportRepoDialog,
                    goToThisRepoId = repoPageGoToId,
                    enableFilterState = repoPageEnableFilterState,
                    filterList = reposPageFilterList,
                    isSelectionMode = reposPageIsSelectionMode,
                    selectedItems = reposPageSelectedItems,
                    unshallowList = reposPageUnshallowItems,
                    deleteList = reposPageDeleteItems,
                    userInfoRepoList = reposPageUserInfoRepoList,
                    upstreamRemoteOptionsList = reposPageUpstreamRemoteOptionsList,
                    specifiedRefreshRepoList = reposPageSpecifiedRefreshRepoList
                )

            }
            else if(currentHomeScreen.intValue== Cons.selectedItem_Files) {
//                changeStateTriggerRefreshPage(needRefreshFilesPage)
                FilesInnerPage(
                    contentPadding = contentPadding,
//                    filePageListState = filePageListState,
                    currentHomeScreen=currentHomeScreen,
                    editorPageShowingFilePath = editorPageShowingFilePath,
                    editorPageShowingFileIsReady = editorPageShowingFileIsReady,
                    needRefreshFilesPage = needRefreshFilesPage,
                    currentPath=filesPageCurrentPath,
                    showCreateFileOrFolderDialog=showCreateFileOrFolderDialog,
                    requireImportFile=filesPageRequireImportFile,
                    requireImportUriList=filesPageRequireImportUriList,
                    filesPageGetFilterMode=filesPageGetFilterMode,
                    filesPageFilterKeyword=filesPageFilterKeyword,
                    filesPageFilterModeOff = filesPageFilterOff,
                    currentPathFileList = filesPageCurrentPathFileList,
                    filesPageRequestFromParent = filesPageRequestFromParent,
                    requireInnerEditorOpenFile = requireInnerEditorOpenFile,
                    filesPageSimpleFilterOn = filesPageSimpleFilterOn,
                    filesPageSimpleFilterKeyWord = filesPageSimpleFilterKeyWord,
                    filesPageScrolled = filesPageScrolled,
                    curListState = filesPageListState,
                    filterListState = filesFilterListState,
                    openDrawer = openDrawer,
                    isFileSelectionMode= filesPageIsFileSelectionMode,
                    isPasteMode = filesPageIsPasteMode,
                    selectedItems = filesPageSelectedItems,
                    checkOnly = filesPageCheckOnly,
                    selectedRepo = filesPageSelectedRepo,
                    goToRepoPage=goToRepoPage,
                    goToChangeListPage=goToChangeListPage,
                    lastPathByPressBack=filesPageLastPathByPressBack,
                    curPathFileItemDto=filesPageCurPathFileItemDto,
                    currentPathBreadCrumbList=filesPageCurrentPathBreadCrumbList,
                    enableFilterState = filesPageEnableFilterState,
                    filterList = filesPageFilterList
                )
            }
            else if(currentHomeScreen.intValue == Cons.selectedItem_Editor) {
//                changeStateTriggerRefreshPage(needRefreshEditorPage)

                EditorInnerPage(
                    editorPageShowingFileName=editorPageShowingFileName.value,
                    contentPadding = contentPadding,
                    currentHomeScreen = currentHomeScreen,
//                    editorPageRequireOpenFilePath=editorPageRequireOpenFilePath,
                    editorPageShowingFilePath=editorPageShowingFilePath,
                    editorPageShowingFileIsReady=editorPageShowingFileIsReady,
                    editorPageTextEditorState=editorPageTextEditorState,
                    lastTextEditorState = editorPageLastTextEditorState,
//                    editorPageShowSaveDoneToast=editorPageShowSaveDoneToast,
                    needRefreshEditorPage=needRefreshEditorPage,
                    isSaving = editorPageIsSaving,
                    isEdited = editorPageIsEdited,
                    showReloadDialog=showReloadDialog,
                    isSubPageMode = false,
                    showCloseDialog = editorPageShowCloseDialog,
                    closeDialogCallback = editorPageCloseDialogCallback,
//                    isLoading = editorPageIsLoading,
                    loadingOn = editorPageLoadingOn,
                    loadingOff = editorPageLoadingOff,
                    saveOnDispose = true,  //销毁时保存处理起来比切换抽屉处理要好实现一些，所以在这里请求销毁时保存，但如果是子页面之类比较好处理的情况，这个值应该传false，由父页面负责保存
                    doSave=doSave,
                    naviUp = {},  //当前页面是一级页面，不需要传naviUp所有逻辑都包含在backhandler里了；二级页面(子页面)才需要传naviUp，用来在按返回箭头时保存再返回
                    requestFromParent = editorPageRequestFromParent,
                    editorPageShowingFileDto,
                    editorPageLastFilePath,
                    editorPageLastScrollEvent,
                    editorPageLazyListState,
                    editorPageIsInitDone,
                    editorPageIsContentSnapshoted,
                    goToFilesPage,
                    drawerState,
                    editorSearchMode = editorPageSearchMode,
                    editorSearchKeyword = editorPageSearchKeyword,
                    readOnlyMode = editorReadOnlyMode,
                    editorMergeMode = editorPageMergeMode,
                    editorShowLineNum=editorShowLineNum,
                    editorLineNumFontSize=editorLineNumFontSize,
                    editorFontSize=editorFontSize,
                    editorAdjustLineNumFontSizeMode = editorAdjustLineNumFontSizeMode,
                    editorAdjustFontSizeMode = editorAdjustFontSizeMode,
                    editorLastSavedLineNumFontSize = editorLastSavedLineNumFontSize,
                    editorLastSavedFontSize = editorLastSavedFontSize,
                    openDrawer = openDrawer,
                    editorOpenFileErr = editorOpenFileErr,
                    undoStack = editorUndoStack

                )

            }
            else if(currentHomeScreen.intValue == Cons.selectedItem_ChangeList) {
//                val commit1OidStr = rememberSaveable { mutableStateOf("") }
//                val commitParentList = remember { mutableStateListOf<String>() }
//                changeListRequireRefreshFromParentPage()

                //从抽屉菜单打开的changelist是对比worktree和index的文件，所以from是worktree
                ChangeListInnerPage(
                    contentPadding,
                    fromTo = changeListPageFromTo,
                    changeListCurRepo,
                    changeListIsFileSelectionMode,

                    changeListRefreshRequiredByParentPage.value,
                    changeListRequireRefreshFromParentPage,
                    changeListHasIndexItems,
//                    requirePullFromParentPage = changeListRequirePull,
//                    requirePushFromParentPage = changeListRequirePush,
                    changeListRequireDoActFromParent,
                    changeListRequireDoActFromParentShowTextWhenDoingAct,
                    changeListEnableAction,
                    changeListCurRepoState,
                    naviUp = {},  //主页来的，不用naviUp只用exitApp，所以这里随便填就行
                    itemList = changeListPageItemList,
                    itemListState = changeListPageItemListState,
                    selectedItemList = changeListPageSelectedItemList,
//                    commit1OidStr=commit1OidStr,
//                    commitParentList=commitParentList,
                    changeListPageNoRepo=changeListPageNoRepo,
                    hasNoConflictItems = changeListPageHasNoConflictItems,
                    goToFilesPage = goToFilesPage,
                    changelistPageScrolled=changelistPageScrolled,
                    changeListPageFilterModeOn= changeListPageFilterModeOn,
                    changeListPageFilterKeyWord=changeListPageFilterKeyWord,
                    filterListState = changelistFilterListState,
                    swap=swapForChangeListPage.value,
                    commitForQueryParents = "",
                    rebaseCurOfAll=changeListPageRebaseCurOfAll,
                    openDrawer = openDrawer,
                    goToRepoPage = goToRepoPage,
                    changeListRepoList= changeListRepoList,
                    goToChangeListPage=goToChangeListPage,
                    needReQueryRepoList = needReQueryRepoListForChangeListTitle,
                    newestPageId = changelistNewestPageId,
                    naviTarget = changeListNaviTarget,
                    enableFilterState = changeListPageEnableFilterState,
                    filterList = changeListFilterList,
                    lastClickedItemKey = changeListLastClickedItemKey
                    // index..worktree, need not pass params, because `fromTo` already implicit `indexToWorktree` or `headToIndex`
//                    commit1OidStr = Cons.gitIndexCommitHash,
//                    commit2OidStr = Cons.gitLocalWorktreeCommitHash
//                    refreshRepoPage = { changeStateTriggerRefreshPage(needRefreshRepoPage) }
                )

                //改用dropdwonmenu了
//                if(changeListIsShowRepoList.value) {
//                    RepoListDialog(curSelectedRepo = changeListCurRepo,
//                        itemOnClick={item:RepoEntity->
//
//
//                        },
//                        onClose={changeListIsShowRepoList.value=false})
//                }
            }else if(currentHomeScreen.intValue == Cons.selectedItem_Settings) {
                SettingsInnerPage(
                    contentPadding = contentPadding,
                    needRefreshPage = needRefreshSettingsPage,
//                    appContext = appContext,
                    openDrawer = openDrawer,
                    exitApp = exitApp,
                    listState = settingsListState,
                )
            }else if(currentHomeScreen.intValue == Cons.selectedItem_About) {
                //About页面是静态的，无需刷新
                AboutInnerPage(contentPadding, openDrawer = openDrawer)
            }else if(currentHomeScreen.intValue == Cons.selectedItem_Subscription) {
                SubscriptionPage(contentPadding = contentPadding, needRefresh = subscriptionPageNeedRefresh, openDrawer = openDrawer)
            }else if(currentHomeScreen.intValue == Cons.selectedItem_Service) {
                ServiceInnerPage(
                    contentPadding = contentPadding,
                    needRefreshPage = needRefreshServicePage,
                    openDrawer = openDrawer,
                    exitApp = exitApp,
                    listState = serviceListState,
                )
            }
        }
    }

    //第一个变量用来控制第一次加载页面时，不会更新配置文件，不然就覆盖成currentPage状态变量的初始值了，
    // 第2个变量让负责渲染页面的线程知道我要用currentPage那个变量，以确保currentPage变化时能执行下面的代码块
    //ps: currentPage.intValue > 0可替换成别的，只要用到currentPage这个状态变量且永远为真即可。
    //ps: initDone 的值应该在初始化完之后就不要再更新了，一直为true即可，这样每次重新渲染页面都检查currentPage是否变化，如果变化就会保存到配置文件了
    if(initDone.value && currentHomeScreen.intValue != Cons.selectedItem_Never && currentHomeScreen.intValue != Cons.selectedItem_Exit) {
        SideEffect {
            //检查下，如果页面变了，并且配置文件里设置为“记住上次退出页面”，就更新配置文件
            if(settingsSnapshot.value.startPageMode == SettingsCons.startPageMode_rememberLastQuit) {  // is remember last quit mode
                val curHomeScreenVal = currentHomeScreen.intValue
//                if(debugModeOn) {
//                    println("curPageVal before update:"+curHomeScreenVal)
//                    println("settingsSnapshot.value.lastQuitPage:"+settingsSnapshot.value.lastQuitHomeScreen)
//                }
                if(curHomeScreenVal != settingsSnapshot.value.lastQuitHomeScreen) {  //currentHomeScreen not same with lastQuitPage, need update
                    //先更新下状态值，避免重复进入此代码块 (或许不能完全避免，不过问题不大，顶多重复更新下配置文件)
                    settingsSnapshot.value.lastQuitHomeScreen = curHomeScreenVal

                    //保存最后切换的页面，然后返回最新配置文件快照
                    settingsSnapshot.value = SettingsUtil.update(requireReturnSnapshotOfUpdatedSettings = true) {
                        it.lastQuitHomeScreen = curHomeScreenVal
                    }!!
                }
            }
        }

    }

    //compose创建时的副作用
//    LaunchedEffect(currentPage.intValue) {
    LaunchedEffect(needRefreshHome.value) {
        //test
//        delay(30*1000)
//        throw RuntimeException("test save when exception")  // passed, it can save when exception threw, even in android 8, still worked like a charm
        //test

        doJobThenOffLoading {
            try {
                //检查是否初次使用，若是，显示欢迎弹窗
                if (settingsSnapshot.value.firstUse) {
                    showWelcomeToNewUser.value = true
                }
                //恢复上次退出页面
                val startPageMode = settingsSnapshot.value.startPageMode
                if (startPageMode == SettingsCons.startPageMode_rememberLastQuit) {
                    //从配置文件恢复上次退出页面
                    currentHomeScreen.intValue = settingsSnapshot.value.lastQuitHomeScreen
                    //设置初始化完成，之后就会通过更新页面值的代码来在页面值变化时更新配置文件中的值了
                    initDone.value = true
                }


                //检查是否存在intent，如果存在，则切换到导入模式
                if (activity != null) {
                    val intent = activity.intent
                    if (intent != null) {
                        val extras = intent.extras

                        //importListConsumed 确保每个Activity的文件列表只被消费一次(20240706: 修复以导入模式启动app再进入子页面再返回会再次触发导入模式的bug)
                        if (extras != null) {
                            //if need import files, go to Files, else go to page which matched to the page id in the extras
                            //if need import files, go to Files, else go to page which matched to the page id in the extras


                            if (!intentConsumed.value) {  //如果列表已经消费过一次，不重新导入，有可能通过系统分享菜单启动app，然后切换到后台，然后再从后台启动app会发生这种情况，但用户有可能已经在上次进入app后点击进入其他页面，所以这时再启动导入模式是不合逻辑的

                                //一进入代码块立即设为已消费，确保intent只被消费一次，不然你通过导入模式进来，一进二级页面，这变量没更新，
                                // 会重新读取intent中的数据再次启动导入模式，其他使用intent的场景也有这个问题，
                                // 比如点击service通知进入changelist页面，然后再切换到repos页面，
                                // 再点进随便一个二级页面，再返回，又会重入此代码块，又回到changelist...
                                intentConsumed.value = true


                                //检查是否需要启动导入模式（用户可通过系统分享菜单文件到app以启动此模式）
                                var importFiles = false  // after jump to Files, it will be set to true, else keep false
                                //20240706: fixed: if import mode in app then go to any sub page then back, will duplicate import files list
                                //20240706: 修复以导入模式启动app后跳转到任意子页面再返回，导入文件列表重复的bug
                                filesPageRequireImportUriList.value.clear()

                                //获取单文件，对应 action SEND
                                val uri = try {
                                    extras.getParcelable<Uri>(Intent.EXTRA_STREAM)  //如果没条目，会警告并打印异常信息，然后返回null
                                } catch (e: Exception) {
                                    null
                                }

                                //导入单文件，需要弹窗询问一下，用户想导入文件还是想编辑文件
                                if (uri != null) {
                                    if(howToDealWithSingleSend.value == SingleSendHandleMethod.NEED_ASK.code) {
                                        //设为假，因为肯定要再入一次这个代码块
                                        intentConsumed.value = false
                                        showAskHandleSingleSendMethod.value = true
                                        return@doJobThenOffLoading
                                    }else if(howToDealWithSingleSend.value == SingleSendHandleMethod.EDIT.code) {
                                        val (path, filename) = FsUtils.getFilePathAndRealNameFromUriOrCanonicalPath(activityContext, uri)
                                        if(path.isBlank()) {
                                            //path如果为空，依然跳转到editor页面
                                            Msg.requireShowLongDuration(activityContext.getString(R.string.file_path_invalid))
                                        }

                                        val expectReadOnly = false
                                        requireInnerEditorOpenFileWithFileName(path, expectReadOnly, filename)

                                        return@doJobThenOffLoading
                                    }else if(howToDealWithSingleSend.value == SingleSendHandleMethod.IMPORT.code) {
                                        //导入文件，添加到列表，然后继续执行后面的代码块就行
                                        filesPageRequireImportUriList.value.add(uri)
                                    }
                                }


                                //获取多文件，对应 action SEND_MULTIPLE
                                val uriList = try {
                                    extras.getParcelableArrayList<Uri>(Intent.EXTRA_STREAM) ?: listOf()
                                } catch (e: Exception) {
                                    listOf()
                                }
                                if (uriList.isNotEmpty()) {
                                    filesPageRequireImportUriList.value.addAll(uriList)
                                }

                                if (filesPageRequireImportUriList.value.isNotEmpty()) {
                                    //请求Files页面导入文件
                                    filesPageRequireImportFile.value = true
                                    currentHomeScreen.intValue = Cons.selectedItem_Files  //跳转到Files页面

                                    importFiles = true
                                }


                                //导入文件完了，下面检查是否跳转页面


                                //导入模式启动，页面已经跳转到Files，后面不用检查了
                                if(importFiles) {
                                    return@doJobThenOffLoading
                                }

                                //如果没有导入文件，检查是否需要跳转页面

                                //如果intent携带了启动页面和仓库，使用
                                val startPage = extras.getString(IntentCons.ExtrasKey.startPage) ?: ""
                                val startRepoId = extras.getString(IntentCons.ExtrasKey.startRepoId) ?: ""

                                if (startPage.isNotBlank()) {
                                    if (startPage == Cons.selectedItem_ChangeList.toString()) {
                                        var startRepo = changeListCurRepo.value
                                        if (startRepoId.isNotBlank()) {  //参数中携带的目标仓库id，查一下子，有就有，没就拉倒
                                            startRepo = AppModel.dbContainer.repoRepository.let { it.getById(startRepoId) ?: it.getByName(startRepoId) ?: startRepo }
                                        }

                                        goToChangeListPage(startRepo)
                                    }else if(startPage == Cons.selectedItem_Repos.toString()) {
                                        goToRepoPage(startRepoId)
                                    }
                                    // else if go to other page maybe
                                }
                            }

                        }
                    }
                }
            } catch (e: Exception) {
                MyLog.e(TAG, "#LaunchedEffect: init home err: " + e.stackTraceToString())
                Msg.requireShowLongDuration("init home err: " + e.localizedMessage)
            }

        }
    }

}



