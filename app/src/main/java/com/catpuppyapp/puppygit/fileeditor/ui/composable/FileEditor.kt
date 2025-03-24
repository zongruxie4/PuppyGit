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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
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
import androidx.compose.runtime.State
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
import com.catpuppyapp.puppygit.compose.SelectedItemDialog3
import com.catpuppyapp.puppygit.compose.SwipeIcon
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.controller.EditorController
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
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import kotlinx.coroutines.runBlocking
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

private const val TAG = "FileEditor"
private const val stateKeyTag = "FileEditor"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileEditor(
    requireEditorScrollToPreviewCurPos:MutableState<Boolean>,
    requirePreviewScrollToEditorCurPos:MutableState<Boolean>,
    isSubPageMode:Boolean,
    previewNavBack:()->Unit,
    previewNavAhead:()->Unit,
    previewNavStack:CustomStateSaveable<EditorPreviewNavStack>,

    previewLoading:Boolean,
    mdText:MutableState<String>,
    basePath:MutableState<String>,
    previewLinkHandler:(link:String)->Boolean,

    isPreviewModeOn:MutableState<Boolean>,
    quitPreviewMode:()->Unit,
    initPreviewMode:()->Unit,

    editableController:State<EditorController>,
    openDrawer:()->Unit,
    editorPageShowingFileName:String?,
    requestFromParent:MutableState<String>,
    fileFullPath:FilePath,
    lastEditedPos:FileEditedPos,
    textEditorState:CustomStateSaveable<TextEditorState>,
    onChanged:(newState:TextEditorState, trueSaveToUndoFalseRedoNullNoSave:Boolean?, clearRedoStack:Boolean)->Unit,
    contentPadding:PaddingValues,
    isContentChanged:MutableState<Boolean>,
    editorLastScrollEvent:CustomStateSaveable<ScrollEvent?>,
    editorListState: LazyListState,
    editorPageIsInitDone:MutableState<Boolean>,
    editorPageIsContentSnapshoted:MutableState<Boolean>,
    goToLine:Int,
    readOnlyMode:Boolean,
    searchMode:MutableState<Boolean>,
    searchKeyword:String,
    mergeMode:Boolean,
    showLineNum:MutableState<Boolean>,
    lineNumFontSize:MutableIntState,
    fontSize:MutableIntState,
    undoStack: UndoStack
) {
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
        editableController.value.createMultipleSelectionModeState(index)
    }

    //切换行选择模式
    if(requestFromParent.value == PageRequest.editorSwitchSelectMode) {
        PageRequest.clearStateThenDoAct(requestFromParent) {
            //如果已经是选择模式，退出；否则开启选择模式
            if(textEditorState.value.isMultipleSelectionMode) {  //退出选择模式
                editableController.value.quitSelectionMode()
            }else {  //开启选择模式
                enableSelectMode(-1)
            }

        }
    }

    val deleteLines = {
        //删除选中行
        editableController.value.createDeletedState()

        //删除行，改变内容flag设为真
        isContentChanged.value=true
        editorPageIsContentSnapshoted.value=false
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


    SwipeableActionsBox(
        startActions = listOf(leftToRightAct),
        endActions = listOf(rightToLeftAct),
    ) {
        val curPreviewScrollState = runBlocking { previewNavStack.value.getCurrentScrollState() }

        Box(modifier = Modifier.fillMaxSize()) {
            if(isPreviewModeOn.value) {
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
                        onLinkClicked = { link ->
                            previewLinkHandler(link)
                        }
                    )

                    Spacer(Modifier.height(30.dp))
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
                            val targetPos = editableController.value.lineIdxToPx(lineIndex=editorCurLineIndex, fontSizeInPx=fontSizeInPx, screenWidthInPx=screenWidthInPx, screenHeightInPx=screenHeightInPx)

                            //滚动
                            UIHelper.scrollTo(scope, curPreviewScrollState, targetPos.toInt())
                        }
                    }catch (e:Exception) {
                        //并非很严重的错误，debug级别吧
                        MyLog.d(TAG, "let preview scroll to current editor position failed: ${e.stackTraceToString()}")
                    }
                }
            } else {

                TextEditor(
                    curPreviewScrollState = curPreviewScrollState,
                    requireEditorScrollToPreviewCurPos = requireEditorScrollToPreviewCurPos,
                    editorPageShowingFileName = editorPageShowingFileName,
                    requestFromParent = requestFromParent,
                    fileFullPath = fileFullPath,
                    lastEditedPos = lastEditedPos,
                    textEditorState = textEditorState.value,
                    editableController = editableController.value,
//            onChanged = onChanged,
                    contentPaddingValues = contentPaddingValues,
                    lastScrollEvent =editorLastScrollEvent,
                    listState =editorListState,
                    editorPageIsInitDone = editorPageIsInitDone,
                    goToLine=goToLine,
                    readOnlyMode=readOnlyMode,
                    searchMode = searchMode,
                    searchKeyword =searchKeyword,
                    mergeMode=mergeMode,
                    fontSize=fontSize,

//            modifier = Modifier.padding(bottom = bottomPadding)
//            modifier = Modifier.fillMaxSize()
                ) { index, isSelected, innerTextField ->
                    // TextLine
                    Row(
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                getBackgroundColor(
                                    isSelected,
                                    textEditorState.value.isMultipleSelectionMode
                                )
                            )
                            .padding(start = (if (showLineNum.value) 2.dp else 5.dp), end = 5.dp)
                            .then(
                                //给第一行top加点padding，不然离上面太近，难受
                                if(index == 0) Modifier.padding(top = topPadding) else Modifier
                            )
                            .bottomBorder(
                                strokeWidth = 1.dp,
                                color = if (inDarkTheme) Color.DarkGray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.2f)
                            )
                    ) {
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
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(top = 1.dp)
                                        .align(Alignment.BottomCenter)
                                        .focusable(false)  //不知道这个focusable(false)有什么用
                                        .combinedClickable(
                                            onLongClick = {
                                                if (textEditorState.value.isMultipleSelectionMode) {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                                    editableController.value.selectFieldSpan(targetIndex = index)
                                                }
                                            }
                                        ) {
                                            //如果是行选择模式，选中当前点击的行如果不是行选择模式；进入行选择模式
                                            if (textEditorState.value.isMultipleSelectionMode) {
                                                //选中/取消选中 当前点击的行
                                                editableController.value.selectField(targetIndex = index)

                                            } else { // 非行选择模式，启动行选择模式 (multiple selection mode on)
                                                enableSelectMode(index)
                                            }
                                        }

                                )

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
                        editableController.value.quitSelectionMode()
                    }
                    val iconList = listOf(
                        Icons.Filled.CleaningServices,  // clear line content
                        Icons.Filled.ContentPaste,
                        Icons.Filled.Delete,
                        Icons.Filled.ContentCut,
                        Icons.Filled.ContentCopy,
                        Icons.Filled.SelectAll
                    )
                    val iconTextList = listOf(
                        stringResource(R.string.clear),
                        stringResource(R.string.paste),
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
                            editableController.value.clearSelectedFields()
                        },
                        onPaste@{
                            if (readOnlyMode) {
                                Msg.requireShow(activityContext.getString(R.string.readonly_cant_edit))
                                return@onPaste
                            }

                            editableController.value.appendTextToLastSelectedLine(clipboardManager.getText()?.text ?: "")
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
                            editableController.value.createCopiedState()
                        },

                        onSelectAll@{
                            editableController.value.createSelectAllState()
                        }
                    )

                    val selectedLines = textEditorState.value.getSelectedCount()
                    val hasLineSelected = selectedLines > 0
                    val hasLineSelectedAndNotReadOnly = hasLineSelected && readOnlyMode.not()
                    val iconEnableList = listOf(
                        onClear@{ hasLineSelectedAndNotReadOnly },  // clear
                        onPaste@{ hasLineSelectedAndNotReadOnly && clipboardManager.hasText() },  // paste，必须 "剪贴板非空 且 选中某行" 才启用
                        onDelete@{ hasLineSelectedAndNotReadOnly },  // delete
                        onCut@{ hasLineSelectedAndNotReadOnly },  // cut
                        onCopy@{ hasLineSelected },  // copy
                        onSelectAll@{ true },  // select all
                    )
                    // BottomBar params block end

                    val showSelectedItemsShortDetailsDialog = rememberSaveable { mutableStateOf(false) }
                    if(showSelectedItemsShortDetailsDialog.value) {
                        SelectedItemDialog3(
                            selectedItems = editableController.value.selectedIndices,

                            //预览的时候带行号，拷贝的时候不带
                            text = {
                                // "行号: 行内容"
                                //it是index，index+1即行号
                                Text(text = "${it+1}: ${editableController.value.getContentOfLineIndex(it)}", softWrap = false, overflow = TextOverflow.Ellipsis)
                            },
                            textFormatterForCopy = { editableController.value.getContentOfLineIndex(it) },

                            switchItemSelected = { editableController.value.selectField(targetIndex = it) },
                            clearAll = { editableController.value.clearSelectedItemList() },
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
                        enableMoreIcon=false,
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
private fun getBackgroundColor(isSelected: Boolean, isMultiSelectionMode:Boolean): Color {
//    return if (isSelected) Color(0x806456A5) else Color.White
//    return if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Unspecified
    return if (isMultiSelectionMode  &&  isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Unspecified
    }
//    return Color.Unspecified
}

@SuppressLint("ModifierFactoryUnreferencedReceiver")
private fun Modifier.bottomBorder(strokeWidth: Dp, color: Color) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }

        Modifier.drawBehind {
            val width = size.width
            val height = size.height - strokeWidthPx / 2

            drawLine(
                color = color,
                start = Offset(x = 0f, y = height),
                end = Offset(x = width, y = height),
                strokeWidth = strokeWidthPx
            )
        }
    }
)

