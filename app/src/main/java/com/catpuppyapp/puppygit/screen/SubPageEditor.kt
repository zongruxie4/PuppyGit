package com.catpuppyapp.puppygit.screen

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.compose.LoadingDialog
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.compose.SmallFab
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dto.FileSimpleDto
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.ScrollEvent
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.EditorInnerPage
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions.EditorPageActions
import com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title.EditorTitle
import com.catpuppyapp.puppygit.screen.shared.SharedState
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//for debug
private const val TAG = "SubPageEditor"
private const val stateKeyTag = "SubPageEditor"


//子页面版本editor
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubPageEditor(
//    context: Context,
//    navController: NavController,
//    drawerState: DrawerState,
//    scope: CoroutineScope,
//    scrollBehavior: TopAppBarScrollBehavior,
//    currentPage: MutableIntState,
//    repoPageListState: LazyListState,
//    filePageListState: LazyListState,
//    haptic: HapticFeedback,
    goToLine:Int,  //大于0，打开文件定位到对应行，小于0，打开文件定位上次编辑行（之前的逻辑不变
    initMergeMode:Boolean,
    initReadOnly:Boolean,
    editorPageLastFilePath:MutableState<String>,
    naviUp:()->Unit
) {
//    val isTimeNaviUp = rememberSaveable { mutableStateOf(false) }
//    if(isTimeNaviUp.value) {
//        naviUp()
//    }

    val naviUp = {
        AppModel.lastEditFile.value = ""
        AppModel.lastEditFileWhenDestroy.value = ""

        naviUp()

        Unit
    }


    val navController = AppModel.navController
    val homeTopBarScrollBehavior = AppModel.homeTopBarScrollBehavior
//    val appContext = AppModel.appContext  //这个获取不了Activity!
    val activityContext = LocalContext.current  //这个能获取到

    val allRepoParentDir = AppModel.allRepoParentDir


//    val changeListRefreshRequiredByParentPage= StateUtil.getRememberSaveableState(initValue = "")
//    val changeListRequireRefreshFromParentPage = {
//        //TODO 显示个loading遮罩啥的
//        changeStateTriggerRefreshPage(changeListRefreshRequiredByParentPage)
//    }
//    val changeListCurRepo = rememberSaveable{ mutableStateOf(RepoEntity()) }


    // canonicalPath
//    val editorPageRequireOpenFilePath = StateUtil.getRememberSaveableState(initValue = (Cache.getByType<String>(filePathKey))?:"")
//    val needRefreshFilesPage = rememberSaveable { mutableStateOf(false) }

    //这个变量有在Activity销毁时保存其值的机制，所以仅需从Cache获取一次，后续不管恢复成功还是失败都无所谓，成功就用它的值，失败就用Activity销毁时保存的值。
    // 注意：这里用getByTypeThenDel()是对的，因为此变量有特殊处理，在Activity销毁时会保存其值，其他页面state变量，如果没特殊处理，应该使用get而不是getThenDel以避免rememberSaveable发生恢复状态变量失败的bug时app报错
    // 注意：虽然rememberSaveable有bug(有时无法在旋转屏幕或其他显示配置改变后正常恢复页面的state变量值)，但就算就算此值在文件打开时恢复失败变成空字符串也无所谓，因为在Activity销毁时保存其值的变量仍会存储最后打开文件路径。
    val editorPageShowingFilePath = rememberSaveable { mutableStateOf((Cache.getByTypeThenDel<String>(Cache.Key.subPageEditor_filePathKey))?:"")} //当前展示的文件的canonicalPath
    val editorPageShowingFileIsReady = rememberSaveable { mutableStateOf(false)} //当前展示的文件是否已经加载完毕
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
    val needRefreshEditorPage = rememberSaveable { mutableStateOf("")}
    val editorPageIsSaving = rememberSaveable { mutableStateOf(false)}
    val editorPageIsEdited = rememberSaveable { mutableStateOf(false)}
    val showReloadDialog = rememberSaveable { mutableStateOf(false)}
    val editorPageShowingFileDto = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "editorPageShowingFileDto",FileSimpleDto() )
    val editorPageSnapshotedFileInfo = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "editorPageSnapshotedFileInfo",FileSimpleDto() )

    val editorPageLastScrollEvent = mutableCustomStateOf<ScrollEvent?>(stateKeyTag, "editorPageLastScrollEvent") { null }  //这个用remember就行，没必要在显示配置改变时还保留这个滚动状态，如果显示配置改变，直接设为null，从配置文件读取滚动位置重定位更好
    val editorPageLazyListState = rememberLazyListState()
    val editorPageIsInitDone = rememberSaveable{mutableStateOf(false)}  //这个也用remember就行，无需在配置改变时保存此状态，直接重置成false就行
    val editorPageIsContentSnapshoted = rememberSaveable{mutableStateOf(false)}  //是否已对当前内容创建了快照
    val editorPageSearchMode = rememberSaveable{mutableStateOf(false)}
    val editorPageSearchKeyword = mutableCustomStateOf(keyTag = stateKeyTag, keyName = "editorPageSearchKeyword", TextFieldValue("") )
    val editorReadOnlyMode = rememberSaveable{mutableStateOf(initReadOnly)}

    //如果用户pro且功能测试通过，允许使用url传来的初始值，否则一律false
    val editorPageMergeMode = rememberSaveable{mutableStateOf(initMergeMode)}

    val editorPageRequestFromParent = rememberSaveable { mutableStateOf("")}


    val editorPreviewScrollState = rememberScrollState()
    val editorIsPreviewModeOn = rememberSaveable { mutableStateOf(false) }
    val editorMdText = rememberSaveable { mutableStateOf("") }
    val editorBasePath = rememberSaveable { mutableStateOf("") }
    val editorPreviewPath = rememberSaveable { mutableStateOf("") }
    val editorPreviewNavStack = mutableCustomStateOf(stateKeyTag, "editorPreviewNavStack") { SharedState.subEditorPreviewNavStack }

    val editorQuitPreviewMode = {
        AppModel.subEditorPreviewModeOnWhenDestroy.value = false

        editorBasePath.value = ""
        editorMdText.value = ""
        editorIsPreviewModeOn.value = false
    }
    val editorInitPreviewMode = {
        //请求执行一次保存，不然有可能切换
        editorPageRequestFromParent.value = PageRequest.requireInitPreviewFromSubEditor
    }


    val settingsTmp = remember { SettingsUtil.getSettingsSnapshot() }  //避免状态变量里的设置项过旧，重新获取一个
    val editorShowLineNum = rememberSaveable{mutableStateOf(settingsTmp.editor.showLineNum)}
    val editorLineNumFontSize = rememberSaveable { mutableIntStateOf(settingsTmp.editor.lineNumFontSize)}
    val editorFontSize = rememberSaveable { mutableIntStateOf(settingsTmp.editor.fontSize)}
    val editorAdjustFontSizeMode = rememberSaveable{mutableStateOf(false)}
    val editorAdjustLineNumFontSizeMode = rememberSaveable{mutableStateOf(false)}
    val editorLastSavedLineNumFontSize = rememberSaveable { mutableIntStateOf(editorLineNumFontSize.intValue) } //用来检查，如果没变，就不执行保存，避免写入硬盘
    val editorLastSavedFontSize = rememberSaveable { mutableIntStateOf(editorFontSize.intValue)}
    val editorOpenFileErr = rememberSaveable{mutableStateOf(false)}

    val editorShowUndoRedo = rememberSaveable{mutableStateOf(settingsTmp.editor.showUndoRedo)}
    val editorUndoStack = mutableCustomStateOf<UndoStack?>(stateKeyTag, "editorUndoStack") { null }

    val showCloseDialog = rememberSaveable { mutableStateOf(false)}

    val closeDialogCallback = mutableCustomStateOf<(Boolean)->Unit>(
        keyTag = stateKeyTag,
        keyName = "closeDialogCallback",
        initValue = { requireSave:Boolean -> Unit}
    )

    val initLoadingText = activityContext.getString(R.string.loading)
    val loadingText = rememberSaveable { mutableStateOf(initLoadingText)}
    val isLoading = rememberSaveable { mutableStateOf(false)}

    val loadingOn = {msg:String ->
        loadingText.value=msg
        isLoading.value=true
//        Msg.requireShow(msg)
//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }
    val loadingOff = {
        isLoading.value=false
        loadingText.value=initLoadingText
//        changeStateTriggerRefreshPage(needRefreshEditorPage)
    }

    if(isLoading.value) {
        LoadingDialog(loadingText.value)
    }

    val doSave:suspend ()->Unit = FsUtils.getDoSaveForEditor(
        editorPageShowingFilePath = editorPageShowingFilePath,
        editorPageLoadingOn = loadingOn,
        editorPageLoadingOff = loadingOff,
        appContext = activityContext,
        editorPageIsSaving = editorPageIsSaving,
        needRefreshEditorPage = needRefreshEditorPage,
        editorPageTextEditorState = editorPageTextEditorState,
        pageTag = TAG,
        editorPageIsEdited = editorPageIsEdited,
        requestFromParent = editorPageRequestFromParent,
        editorPageFileDto = editorPageShowingFileDto,
        isSubPageMode = true,
        isContentSnapshoted =editorPageIsContentSnapshoted,
        snapshotedFileInfo = editorPageSnapshotedFileInfo
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
                    //这页面不用来打开外部文件，正常来说不会出现file uri，不过虽然可以通过最近文件列表打开一个uri路径，但这个处理起来有点繁琐，算了，不管了，又不是不能用

                    EditorTitle(
                        previewNavStack = editorPreviewNavStack.value,
                        previewingPath = editorPreviewPath.value,
                        isPreviewModeOn = editorIsPreviewModeOn.value,
                        editorPageShowingFileName = null,  //若打开 uri 此变量是文件名，但此页面不用来打开uri，所以也不需要指定uri关联的文件名
                        editorPageShowingFilePath = editorPageShowingFilePath,
                        editorPageRequestFromParent = editorPageRequestFromParent,
                        editorSearchMode = editorPageSearchMode.value,
                        editorSearchKeyword = editorPageSearchKeyword,
                        editorPageMergeMode = editorPageMergeMode.value,
                        readOnly = editorReadOnlyMode.value,
                        editorOpenFileErr = editorOpenFileErr.value
                    )

                },
                navigationIcon = {
                    if(editorIsPreviewModeOn.value || editorPageSearchMode.value || editorAdjustFontSizeMode.value || editorAdjustLineNumFontSizeMode.value) {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.close),
                            icon =  Icons.Filled.Close,
                            iconContentDesc = stringResource(R.string.close),
                        ) {
                            if(editorIsPreviewModeOn.value) {
                                editorQuitPreviewMode()
                            }else if(editorPageSearchMode.value){
                                editorPageSearchMode.value = false
                            }else if(editorAdjustFontSizeMode.value) {
                                editorPageRequestFromParent.value = PageRequest.requireSaveFontSizeAndQuitAdjust
                            }else if(editorAdjustLineNumFontSizeMode.value) {
                                editorPageRequestFromParent.value = PageRequest.requireSaveLineNumFontSizeAndQuitAdjust
                            }
                        }
                    }else {
                        LongPressAbleIconBtn(
                            tooltipText = stringResource(R.string.back),
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            iconContentDesc = stringResource(R.string.back),
                        ) {
                            doJobThenOffLoading {
//                            isTimeNaviUp.value=false
                                //未保存，先保存，再点击，再返回
                                if(editorPageIsEdited.value && !editorReadOnlyMode.value) {
//                                doSave()这个得改一下，不要在外部保存
                                    editorPageRequestFromParent.value = PageRequest.requireSave
                                    return@doJobThenOffLoading
                                }

                                //返回
//                            isTimeNaviUp.value=true
                                withContext(Dispatchers.Main) {
                                    naviUp()
                                }

//                            changeStateTriggerRefreshPage(needRefreshEditorPage)  //都离开页面了，刷新鸡毛啊

                            }
                        }
                    }

                },
                actions = {
                    if(!editorOpenFileErr.value) {
                        EditorPageActions(
                            isPreviewModeOn = editorIsPreviewModeOn.value,
                            previewScrollState = editorPreviewScrollState,
                            editorPageShowingFilePath = editorPageShowingFilePath,
    //                        editorPageRequireOpenFilePath,
                            editorPageShowingFileIsReady = editorPageShowingFileIsReady,
                            needRefreshEditorPage = needRefreshEditorPage,
                            editorPageTextEditorState = editorPageTextEditorState,
    //                        editorPageShowSaveDoneToast,
                            isSaving = editorPageIsSaving,
                            isEdited = editorPageIsEdited,
                            showReloadDialog = showReloadDialog,
                            showCloseDialog = showCloseDialog,
                            closeDialogCallback=closeDialogCallback,
    //                        isLoading = isLoading,
                            doSave = doSave,
                            loadingOn = loadingOn,
                            loadingOff = loadingOff,
                            editorPageRequest = editorPageRequestFromParent,
                            editorPageSearchMode = editorPageSearchMode,
                            editorPageMergeMode=editorPageMergeMode,
                            readOnlyMode = editorReadOnlyMode,
                            editorSearchKeyword = editorPageSearchKeyword.value.text,
                            isSubPageMode=true,

                            fontSize=editorFontSize,
                            lineNumFontSize=editorLineNumFontSize,
                            adjustFontSizeMode=editorAdjustFontSizeMode,
                            adjustLineNumFontSizeMode=editorAdjustLineNumFontSizeMode,
                            showLineNum = editorShowLineNum,
                            undoStack = editorUndoStack.value,
                            showUndoRedo = editorShowUndoRedo
                        )
                    }
                },
                scrollBehavior = homeTopBarScrollBehavior,
            )
        },
        floatingActionButton = {
            if(editorPageShowingFileIsReady.value && editorPageShowingFilePath.value.isNotBlank() && editorPageIsEdited.value && !editorPageIsSaving.value && !editorReadOnlyMode.value) {
                SmallFab(
                    modifier= MyStyleKt.Fab.getFabModifierForEditor(editorPageTextEditorState.value.isMultipleSelectionMode),
                    icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)
                ) {
                    editorPageRequestFromParent.value = PageRequest.requireSave
                }
            }
        }
    ) { contentPadding ->
        EditorInnerPage(
            previewPath = editorPreviewPath,
            previewNavStack = editorPreviewNavStack,
            isPreviewModeOn = editorIsPreviewModeOn,
            previewScrollState = editorPreviewScrollState,
            mdText = editorMdText,
            basePath = editorBasePath,
            quitPreviewMode = editorQuitPreviewMode,
            initPreviewMode = editorInitPreviewMode,

            editorPageShowingFileName = null,
            contentPadding = contentPadding,

            //editor作为子页面时其实不需要这个变量，只是调用的组件需要，又没默认值，所以姑且创建一个
            currentHomeScreen = remember { mutableIntStateOf(Cons.selectedItem_Repos)},

//            editorPageRequireOpenFilePath=editorPageRequireOpenFilePath,
            editorPageShowingFilePath=editorPageShowingFilePath,
            editorPageShowingFileIsReady=editorPageShowingFileIsReady,
            editorPageTextEditorState=editorPageTextEditorState,
            lastTextEditorState=editorPageLastTextEditorState,
//            editorPageShowSaveDoneToast=editorPageShowSaveDoneToast,
            needRefreshEditorPage=needRefreshEditorPage,
            isSaving = editorPageIsSaving,
            isEdited = editorPageIsEdited,
            showReloadDialog=showReloadDialog,
            isSubPageMode=true,
            showCloseDialog=showCloseDialog,
            closeDialogCallback=closeDialogCallback,
//            isLoading = isLoading,
            loadingOn = loadingOn,
            loadingOff = loadingOff,
            saveOnDispose = false,  //这时父页面（当前页面）负责保存，不需要子页面保存，所以传false
            doSave = doSave,  //doSave还是要传的，虽然销毁组件时的保存关闭了，但是返回时的保存依然要用doSave
            naviUp=naviUp,
            requestFromParent = editorPageRequestFromParent,
            editorPageShowingFileDto = editorPageShowingFileDto,
            lastFilePath = editorPageLastFilePath,
            editorLastScrollEvent = editorPageLastScrollEvent,
            editorListState = editorPageLazyListState,
            editorPageIsInitDone = editorPageIsInitDone,
            editorPageIsContentSnapshoted = editorPageIsContentSnapshoted,
            goToFilesPage = {},  //子页面不支持在Files显示文件，所以传空函数即可，应该由子页面的调用者(应该是个顶级页面)来实现在Files页面显示文件，子页面只负责编辑（暂时先这样，实际上就算想支持也不行，因为子页面无法跳转到顶级的Files页面）
            goToLine=goToLine,
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
            openDrawer = {}, //非顶级页面按返回键不需要打开抽屉
            editorOpenFileErr = editorOpenFileErr,
            undoStack = editorUndoStack

        )
    }




//    //compose创建时的副作用
////    LaunchedEffect(currentPage.intValue) {
//    LaunchedEffect(Unit) {
//    }
//
//
//    //compose被销毁时执行的副作用
//    DisposableEffect(Unit) {
////        ("DisposableEffect: entered main")
//        onDispose {
////            ("DisposableEffect: exited main")
//        }
//    }

}



