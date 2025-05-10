package com.catpuppyapp.puppygit.fileeditor.ui.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.BottomBar
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.MarkDownContainer
import com.catpuppyapp.puppygit.compose.PullToRefreshBox
import com.catpuppyapp.puppygit.compose.SelectedItemDialog3
import com.catpuppyapp.puppygit.compose.SwipeIcon
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.ScrollEvent
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.TextEditor
import com.catpuppyapp.puppygit.fileeditor.ui.composable.editor.FieldIcon
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.shared.EditorPreviewNavStack
import com.catpuppyapp.puppygit.screen.shared.FilePath
import com.catpuppyapp.puppygit.settings.FileEditedPos
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.EditCache
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import kotlinx.coroutines.runBlocking
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

private const val TAG = "FileEditor"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileEditor(
    stateKeyTag:String,
    requireEditorScrollToPreviewCurPos:MutableState<Boolean>,
    requirePreviewScrollToEditorCurPos:MutableState<Boolean>,
    isSubPageMode:Boolean,
    previewNavBack:()->Unit,
    previewNavAhead:()->Unit,
    previewNavStack:CustomStateSaveable<EditorPreviewNavStack>,
    refreshPreviewPage:()->Unit,

    previewLoading:Boolean,
    mdText:MutableState<String>,
    basePath:MutableState<String>,
    previewLinkHandler:(link:String)->Boolean,

    isPreviewModeOn:MutableState<Boolean>,
    quitPreviewMode:()->Unit,
    initPreviewMode:()->Unit,

    openDrawer:()->Unit,
    editorPageShowingFileName:String?,
    requestFromParent:MutableState<String>,
    fileFullPath:FilePath,
    lastEditedPos:FileEditedPos,
    textEditorState:CustomStateSaveable<TextEditorState>,
    contentPadding:PaddingValues,
    isContentEdited:MutableState<Boolean>,
    editorLastScrollEvent:CustomStateSaveable<ScrollEvent?>,
    editorListState: LazyListState,
    editorPageIsInitDone:MutableState<Boolean>,
    editorPageIsContentSnapshoted:MutableState<Boolean>,
    goToLine:Int,
    readOnlyMode:Boolean,
    searchMode:MutableState<Boolean>,
    searchKeyword:String,
    mergeMode:Boolean,
    patchMode:Boolean,
    showLineNum:MutableState<Boolean>,
    lineNumFontSize:MutableIntState,
    fontSize:MutableIntState,
    undoStack: UndoStack
) {
    val stateKeyTag = Cache.getComponentKey(stateKeyTag, TAG)

    val activityContext = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val deviceConfiguration = LocalConfiguration.current

    val scope = rememberCoroutineScope()

    val inDarkTheme = Theme.inDarkTheme

    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val showDeleteDialog = rememberSaveable { mutableStateOf(false) }
//    val editableController by rememberTextEditorController(textEditorState.value, onChanged = { onChanged(it) }, isContentChanged, editorPageIsContentSnapshoted)

//    val bottomPadding = if (textEditorState.value.isMultipleSelectionMode) 100.dp else 0.dp  //如果使用这个padding，开启选择模式时底栏会有背景，否则没有，没有的时候就像直接浮在编辑器上
//    val bottomPadding = 0.dp

//    val contentBottomPaddingValue = with(LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }  //获取键盘高度？用来做padding以在显示键盘时能看到最后一行内容？
//    val contentPaddingValues = PaddingValues(
//        start=contentPadding.calculateStartPadding(LayoutDirection.Ltr),
//        top=contentPadding.calculateTopPadding(),
//        end=contentPadding.calculateEndPadding(LayoutDirection.Rtl),
//        bottom = contentPadding.calculateBottomPadding()+contentBottomPaddingValue+300.dp  // make bottom higher for better view
////        bottom = contentPadding.calculateBottomPadding()+contentBottomPaddingValue  // make bottom higher for better view
//    )

//    val contentPaddingValues = PaddingValues(bottom = contentBottomPaddingValue)
    val contentPaddingValues = contentPadding

    val enableSelectMode = { index:Int ->
        //隐藏键盘
        keyboardController?.hide()

        // 非行选择模式，启动行选择模式 (multiple selection mode on)
        //注：索引传-1或其他无效索引即可在不选中任何行的情况下启动选择模式，从顶栏菜单开启选择模式默认不选中任何行，所以这里传-1
        doJobThenOffLoading {
            textEditorState.value.createMultipleSelectionModeState(index)
        }
    }

    //切换行选择模式
    if(requestFromParent.value == PageRequest.editorSwitchSelectMode) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            //如果已经是选择模式，退出；否则开启选择模式
            if(textEditorState.value.isMultipleSelectionMode) {  //退出选择模式
                doJobThenOffLoading {
                    textEditorState.value.quitSelectionMode()
                }
            }else {  //开启选择模式
                enableSelectMode(-1)
            }

        }
    }

    val deleteLines = {
        doJobThenOffLoading {

            //删除选中行
            textEditorState.value.createDeletedState()

            //删除行，改变内容flag设为真
            isContentEdited.value=true
            editorPageIsContentSnapshoted.value=false
        }
    }

    if(showDeleteDialog.value) {
        ConfirmDialog(
            title = stringResource(R.string.delete_lines),
            text = replaceStringResList(stringResource(R.string.will_delete_n_lines_ask), listOf(textEditorState.value.getSelectedCount().toString())) ,
            okTextColor = MyStyleKt.TextColor.danger(),
            onCancel = { showDeleteDialog.value=false }
        ) {
            //关弹窗
            showDeleteDialog.value=false
            //删除行
            deleteLines()
            //显示通知
            Msg.requireShow(activityContext.getString(R.string.deleted))
        }
    }



//    val isRtl = UIHelper.isRtlLayout()

    //处理返回和前进
    val onLeftToRight = {
        if(isPreviewModeOn.value) {
            previewNavBack()
        } else if(isSubPageMode.not()) {
            openDrawer()
        }
    }
    val onRightToLeft = {
        if(isPreviewModeOn.value) {
            previewNavAhead()
        } else {
            initPreviewMode()

            //避免切到预览再切回来后自动弹键盘
            textEditorState.value = textEditorState.value.copy(
                focusingLineIdx = null,
            )
        }
    }

//    val dragHandleInterval = remember { 500L } //ms
//    val curTime = rememberSaveable { mutableLongStateOf(0) }


    //内容顶部padding
    val topPadding = remember { 5.dp }

    //只需要contentPadding的上和下，不然横屏会因为底部导航栏而有偏差
    val swipeIconModifier = Modifier.padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()).padding(horizontal = 10.dp)

    //左边操作总是启用，但需要确定是否显示动画
    val leftToRightAct = SwipeAction(
        icon = {
            //只有满足条件才显示图标，否则若图标根据条件变化，滑动图标松手后会短暂显示变化的图标，不好
            if(isPreviewModeOn.value) {
                SwipeIcon(
                    modifier = swipeIconModifier,
                    imageVector = runBlocking {
                        if (previewNavStack.value.backStackIsEmpty()) Icons.Filled.Edit else Icons.AutoMirrored.Filled.ArrowBackIos
                    },
                    contentDescription = null
                )
            }

        },
        background = Color.Unspecified,
        //编辑模式由于有抽屉菜单，所以这里禁用动画
        enableAnimation = isPreviewModeOn.value,
        enableAct = isSubPageMode.not() || isPreviewModeOn.value,
        enableVibration = isPreviewModeOn.value,  //在编辑器滑动出现抽屉菜单时不震动
        onSwipe = { onLeftToRight() }
    )

    val enableRightToLeftAct = isPreviewModeOn.value.not() || runBlocking { previewNavStack.value.aheadStackIsNotEmpty() }

    //右边操作不总是启用，但动画总是启用
    val rightToLeftAct = SwipeAction(
        icon = {
            if(enableRightToLeftAct) {
                SwipeIcon(
                    modifier = swipeIconModifier,
                    imageVector = if(isPreviewModeOn.value) Icons.AutoMirrored.Filled.ArrowForwardIos else Icons.Filled.RemoveRedEye,
                    contentDescription = null
                )
            }
        },
        background = Color.Unspecified,
        enableAct = enableRightToLeftAct,
        onSwipe = { onRightToLeft() },
    )


    val fontColor = remember(inDarkTheme) { UIHelper.getFontColor(inDarkTheme) }

    SwipeableActionsBox(
        startActions = listOf(leftToRightAct),
        endActions = listOf(rightToLeftAct),
    ) {
        val curPreviewScrollState = runBlocking { previewNavStack.value.getCurrentScrollState() }

        Box(modifier = Modifier.fillMaxSize()) {
            if(isPreviewModeOn.value) {
                PullToRefreshBox(
                    contentPadding = contentPadding,
//                    isRefreshing = previewLoading, //有bug，得传state，不然加载后这图标会留在原地，但要改的话里面代码得改，其他很多地方也都得改，我懒得改，索性直接禁用，用其他东西loading
                    onRefresh = { refreshPreviewPage() }
                ) {
                    Column(
                        modifier = Modifier
                            //fillMaxSize 必须在最上面！要不然，文字不会显示在中间！
                            .fillMaxSize()
                            .padding(contentPadding)
                            .verticalScroll(curPreviewScrollState)
                        ,
                    ) {
                        Spacer(Modifier.height(topPadding))

                        MarkDownContainer(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            content = mdText.value,
                            basePathNoEndSlash = basePath.value,
                            fontSize = fontSize.intValue, //和编辑器字体大小保持一致
                            fontColor = fontColor,
                            onLinkClicked = { link ->
                                previewLinkHandler(link)
                            }
                        )

                        Spacer(Modifier.height(30.dp))
                    }

                }

                LaunchedEffect(Unit) {
                    try {
                        if(requirePreviewScrollToEditorCurPos.value) {
                            //一次性变量，立刻重置
                            requirePreviewScrollToEditorCurPos.value = false

                            val fontSizeInPx = UIHelper.spToPx(sp = fontSize.intValue, density = density)
                            val screenWidthInPx = UIHelper.dpToPx(dp = deviceConfiguration.screenWidthDp, density = density)
                            val screenHeightInPx = UIHelper.dpToPx(dp = deviceConfiguration.screenHeightDp, density = density)
                            val editorCurLineIndex = editorListState.firstVisibleItemIndex

                            //计算目标滚动位置
                            val targetPos = textEditorState.value.lineIdxToPx(lineIndex=editorCurLineIndex, fontSizeInPx=fontSizeInPx, screenWidthInPx=screenWidthInPx, screenHeightInPx=screenHeightInPx)

                            //滚动
                            UIHelper.scrollTo(scope, curPreviewScrollState, targetPos.toInt())
                        }
                    }catch (e:Exception) {
                        //并非很严重的错误，debug级别吧
                        MyLog.d(TAG, "let preview scroll to current editor position failed: ${e.stackTraceToString()}")
                    }
                }
            } else {

                val bottomLineWidth = remember { 1.dp }
                val changeTypeWidth = remember { 10.dp }

                TextEditor(
                    stateKeyTag = stateKeyTag,
                    undoStack = undoStack,
                    curPreviewScrollState = curPreviewScrollState,
                    requireEditorScrollToPreviewCurPos = requireEditorScrollToPreviewCurPos,
                    editorPageShowingFileName = editorPageShowingFileName,
                    requestFromParent = requestFromParent,
                    fileFullPath = fileFullPath,
                    lastEditedPos = lastEditedPos,
                    textEditorState = textEditorState.value,
                    contentPaddingValues = contentPaddingValues,
                    lastScrollEvent =editorLastScrollEvent,
                    listState =editorListState,
                    editorPageIsInitDone = editorPageIsInitDone,
                    goToLine=goToLine,
                    readOnlyMode=readOnlyMode,
                    searchMode = searchMode,
                    searchKeyword =searchKeyword,
                    mergeMode=mergeMode,
                    patchMode=patchMode,
                    fontSize=fontSize,
                    fontColor = fontColor,
                ) { index, isSelected, currentField, focusingIdx, isMultiSelectionMode, innerTextField ->
                    // TextLine
                    Row(
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            //底线
                            .fieldBorder(
                                bottomLineWidth = bottomLineWidth,
                                color = UIHelper.getDividerColor(),
                                changeTypeLineWidth = changeTypeWidth,
                                changeTypeColor = currentField.getColorOfChangeType(inDarkTheme),
                            )
                            .fillMaxWidth()
                            .background(
                                getBackgroundColor(
                                    isSelected = isSelected,
                                    isMultiSelectionMode = isMultiSelectionMode,
                                    currentIdx = index,
                                    focusingIdx = focusingIdx,
                                    inDarkTheme = inDarkTheme,

                                )
                            )
                            .padding(end = 5.dp)
                            .then(
                                //给第一行top加点padding，不然离上面太近，难受
                                if(index == 0) Modifier.padding(top = topPadding) else Modifier
                            )
                    ) {

                        Row(
                            modifier = Modifier.combinedClickable(
                                onLongClick = {
                                    if (textEditorState.value.isMultipleSelectionMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                        doJobThenOffLoading {
                                            textEditorState.value.selectFieldSpan(targetIndex = index)
                                        }
                                    }
                                }
                            ) {
                                //如果是行选择模式，选中当前点击的行如果不是行选择模式；进入行选择模式
                                if (textEditorState.value.isMultipleSelectionMode) {
                                    //选中/取消选中 当前点击的行
                                    doJobThenOffLoading {
                                        textEditorState.value.selectField(targetIndex = index)
                                    }

                                } else { // 非行选择模式，启动行选择模式 (multiple selection mode on)
                                    enableSelectMode(index)
                                }
                            }
                        ) {
                            //充当行号修改类型指示器和行号之间的padding，不然会重叠
                            Text("", modifier = Modifier.width(if(showLineNum.value) changeTypeWidth-3.dp else changeTypeWidth))


                            //行号
                            if(showLineNum.value) {
                                Box (
                                    //让行号和选择图标居中
//                    horizontalAlignment = Alignment.CenterHorizontally
                                ){
                                    // TextLine Number
                                    Text(
                                        modifier = Modifier.align(Alignment.TopCenter),
                                        text = getLineNumber(index),
                                        color = if(inDarkTheme) MyStyleKt.TextColor.lineNum_forEditorInDarkTheme else MyStyleKt.TextColor.lineNum_forEditorInLightTheme,
                                        fontSize = lineNumFontSize.intValue.sp,
                                        fontFamily = FontFamily.Monospace,  //等宽字体，和diff页面的行号保持一致
                                        //行号居中
                                        // modifier = Modifier.align(Alignment.CenterVertically)
                                    )

                                    // TextField Menu Icon
                                    FieldIcon(
//                                    isMultipleSelection = textEditorState.value.isMultipleSelectionMode,
                                        focused = index == textEditorState.value.focusingLineIdx,

                                        //是否聚焦本行（三道横线图标）禁用了，不管了，维护这个状态太烦
//                                    focused = false,

                                        modifier = Modifier
                                            .size(12.dp)
                                            .padding(top = 1.dp)
                                            .align(Alignment.BottomCenter)
                                            .focusable(false)  //这个focusable不是我加的，是compose text editor原作者加的，不知道这个focusable(false)有什么用，是想让这个图标在用键盘导航时不聚焦吗？但实际还是能聚焦啊，所以可能和这个无关，那这个到底什么作用？


                                    )

                                }
                            }
                        }



                        // TextField
                        innerTextField(
                            Modifier
                                .weight(0.9f, true)
                                .align(Alignment.CenterVertically)
                        )


                    }
                }


                // Multiple Selection Menu
                if (textEditorState.value.isMultipleSelectionMode) {
                    //涉及到状态变量的函数一律不要用remember，
                    // 不然状态变量改变后函数不会重新生成，
                    // 例如删除按钮的enable函数依赖只读，
                    // 期望打开关闭只读影响其启用状态，若remember，
                    // 则开关将无影响，否则有影响，
                    // 当然也可把所有相关的状态放到rememberd的括号里，
                    // 例如 remember(相关状态1，状态2，等) {你的函数}，
                    // 但这样写太麻烦了

                    // BottomBar params block start
                    val quitSelectionMode = {
                        doJobThenOffLoading {
                            textEditorState.value.quitSelectionMode()
                        }

                        Unit
                    }
                    val iconList = listOf(
                        Icons.Filled.CleaningServices,  // clear line content
                        Icons.Filled.ContentPaste,
                        Icons.AutoMirrored.Filled.KeyboardReturn,  // append a line
                        Icons.Filled.Delete,
                        Icons.Filled.ContentCut,
                        Icons.Filled.ContentCopy,
                        Icons.Filled.SelectAll
                    )
                    val iconTextList = listOf(
                        stringResource(R.string.clear),
                        stringResource(R.string.paste),
                        stringResource(R.string.append_a_line),
                        stringResource(R.string.delete),
                        stringResource(R.string.cut),
                        stringResource(R.string.copy),
                        stringResource(R.string.select_all),
                    )
                    val iconOnClickList = listOf(
                        onClear@{
                            if (readOnlyMode) {
                                Msg.requireShow(activityContext.getString(R.string.readonly_cant_edit))
                                return@onClear
                            }

                            //不用确认，直接清空，若后悔可用undo撤销
                            doJobThenOffLoading {
                                textEditorState.value.clearSelectedFields()
                            }

                            Unit
                        },
                        onPaste@{
                            if (readOnlyMode) {
                                Msg.requireShow(activityContext.getString(R.string.readonly_cant_edit))
                                return@onPaste
                            }

                            val text = try {
                                clipboardManager.getText()?.text ?: ""
                            }catch (e:Exception) {
                                Msg.requireShowLongDuration("read clipboard err: ${e.localizedMessage}")
                                MyLog.e(TAG, "read clipboard err: ${e.stackTraceToString()}")

                                ""
                            }

                            EditCache.writeToFile(text)

                            doJobThenOffLoading {
                                textEditorState.value.appendTextToLastSelectedLine(text)
                            }

                            Unit
                        },
                        onAppendALine@{
                            if (readOnlyMode) {
                                Msg.requireShow(activityContext.getString(R.string.readonly_cant_edit))
                                return@onAppendALine
                            }

                            doJobThenOffLoading {
                                textEditorState.value.appendTextToLastSelectedLine("", forceAppend = true)
                            }

                            Unit
                        },
                        onDelete@{
                            if (readOnlyMode) {
                                Msg.requireShow(activityContext.getString(R.string.readonly_cant_edit))
                                return@onDelete
                            }

                            val selectedLinesNum = textEditorState.value.getSelectedCount();
                            if (selectedLinesNum < 1) {
                                Msg.requireShow(activityContext.getString(R.string.no_line_selected))
                                return@onDelete
                            }

                            showDeleteDialog.value = true
                        },
                        onCut@{
                            val selectedLinesNum = textEditorState.value.getSelectedCount();
                            if (selectedLinesNum < 1) {
                                Msg.requireShow(activityContext.getString(R.string.no_line_selected))
                                return@onCut
                            }

                            clipboardManager.setText(AnnotatedString(textEditorState.value.getSelectedText()))
                            Msg.requireShow(replaceStringResList(activityContext.getString(R.string.n_lines_copied), listOf(selectedLinesNum.toString())), )
                            deleteLines()
                        },
                        onCopy@{
                            val selectedLinesNum = textEditorState.value.getSelectedCount();
                            if (selectedLinesNum < 1) {
                                Msg.requireShow(activityContext.getString(R.string.no_line_selected))
                                return@onCopy
                            }

                            clipboardManager.setText(AnnotatedString(textEditorState.value.getSelectedText()))
                            Msg.requireShow(replaceStringResList(activityContext.getString(R.string.n_lines_copied), listOf(selectedLinesNum.toString())), )
//                            editableController.value.createCopiedState()
                        },

                        onSelectAll@{
                            doJobThenOffLoading {
                                textEditorState.value.createSelectAllState()
                            }

                            Unit
                        }
                    )

                    val selectedLines = textEditorState.value.getSelectedCount()
                    val hasLineSelected = selectedLines > 0
                    val hasLineSelectedAndNotReadOnly = hasLineSelected && readOnlyMode.not()
                    val iconEnableList = listOf(
                        onClear@{ hasLineSelectedAndNotReadOnly },  // clear
                        onPaste@{ hasLineSelectedAndNotReadOnly && clipboardManager.hasText() },  // paste，必须 "剪贴板非空 且 选中某行" 才启用
                        onAppendALine@{ hasLineSelectedAndNotReadOnly },  // append a line
                        onDelete@{ hasLineSelectedAndNotReadOnly },  // delete
                        onCut@{ hasLineSelectedAndNotReadOnly },  // cut
                        onCopy@{ hasLineSelected },  // copy
                        onSelectAll@{ true },  // select all
                    )
                    // BottomBar params block end

                    val showSelectedItemsShortDetailsDialog = rememberSaveable { mutableStateOf(false) }
                    if(showSelectedItemsShortDetailsDialog.value) {
                        SelectedItemDialog3(
                            selectedItems = textEditorState.value.selectedIndices,

                            //预览的时候带行号，拷贝的时候不带
                            text = {
                                // "行号: 行内容"
                                //it是index，index+1即行号
                                Text(text = "${it+1}: ${textEditorState.value.getContentOfLineIndex(it)}", softWrap = false, overflow = TextOverflow.Ellipsis)
                            },
                            textFormatterForCopy = { textEditorState.value.getContentOfLineIndex(it) },

                            switchItemSelected = { doJobThenOffLoading { textEditorState.value.selectField(targetIndex = it) } },
                            clearAll = { textEditorState.value.clearSelectedItemList() },
                            closeDialog = {showSelectedItemsShortDetailsDialog.value = false}
                        )
                    }

                    BottomBar(
                        quitSelectionMode=quitSelectionMode,
                        iconList=iconList,
                        iconTextList=iconTextList,
                        iconDescTextList=iconTextList,
                        iconOnClickList=iconOnClickList,
                        iconEnableList=iconEnableList,
                        moreItemTextList= listOf(),
                        moreItemOnClickList= listOf(),
                        moreItemEnableList = listOf(),
                        getSelectedFilesCount = {selectedLines},
                        countNumOnClickEnabled = true,
                        countNumOnClick = {showSelectedItemsShortDetailsDialog.value = true}
                    )
                }
            }

//        SmallFab(modifier = Modifier.align(Alignment.BottomEnd), icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)) {
//
//        }
        }
    }

}

private fun getLineNumber(index: Int): String {
//    return (index + 1).toString().padStart(3, '0')
    return (index + 1).toString()
}

@Composable
private fun getBackgroundColor(isSelected: Boolean, isMultiSelectionMode:Boolean, currentIdx:Int, focusingIdx:Int, inDarkTheme:Boolean, ): Color {
//    return if (isSelected) Color(0x806456A5) else Color.White
//    return if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
    return if (isMultiSelectionMode  &&  isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    }else if(isMultiSelectionMode.not() && currentIdx == focusingIdx) {
        //选中行颜色
        if(inDarkTheme) Color(0x4D737373) else Color(0x7AD2D2D2)
    } else {
        Color.Unspecified
    }
//    return Color.Unspecified
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
private fun Modifier.fieldBorder(
    bottomLineWidth: Dp,
    color: Color,
    changeTypeLineWidth:Dp,
    changeTypeColor: Color,
) = composed(
    factory = {
        val density = LocalDensity.current
        val bottomLineWidthPx = density.run { bottomLineWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - bottomLineWidthPx / 2

            // 底线
            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = bottomLineWidthPx
            )

            //每行左边的修改类型指示器，显示当前行是新增的还是修改的
            drawLine(
                color = changeTypeColor,
                strokeWidth = changeTypeLineWidth.toPx(),  //宽度
                //起始和结束点，单位应该是px
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
            )
        }
    }
)

