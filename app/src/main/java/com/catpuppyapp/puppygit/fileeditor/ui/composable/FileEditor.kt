package com.catpuppyapp.puppygit.fileeditor.ui.composable

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.compose.BottomBar
import com.catpuppyapp.puppygit.compose.ConfirmDialog
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.compose.MarkDownContainer
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.controller.EditorController
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.ScrollEvent
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.TextEditor
import com.catpuppyapp.puppygit.fileeditor.ui.composable.editor.FieldIcon
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.FileEditedPos
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf

private const val TAG = "FileEditor"
private const val stateKeyTag = "FileEditor"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileEditor(
    previewLoading:Boolean,
    previewScrollState:ScrollState,
    mdText:MutableState<String>,
    basePath:MutableState<String>,

    isPreviewModeOn:MutableState<Boolean>,
    quitPreviewMode:()->Unit,
    initPreviewMode:()->Unit,
    openDrawer:()->Unit,
    editorPageShowingFileName:String?,
    requestFromParent:MutableState<String>,
    fileFullPath:String,
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
    undoStack: UndoStack?
) {
    val activityContext = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val inDarkTheme = Theme.inDarkTheme

    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val showDeleteDialog = rememberSaveable { mutableStateOf(false) }
//    val editableController by rememberTextEditorController(textEditorState.value, onChanged = { onChanged(it) }, isContentChanged, editorPageIsContentSnapshoted)
    val editableController = mutableCustomStateOf(stateKeyTag, "editableController") {
        EditorController(textEditorState.value, undoStack).apply {
            setOnChangedTextListener(isContentChanged, editorPageIsContentSnapshoted, onChanged)
        }
    }


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
                editableController.value.createCancelledState()
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




    val isRtl = UIHelper.isRtlLayout()

    val onLeftToRight = { if(isPreviewModeOn.value) quitPreviewMode() else openDrawer() }
    val onRightToLeft = { if(isPreviewModeOn.value.not()) initPreviewMode() }

    val dragHandleInterval = remember { 500L } //ms
    val curTime = rememberSaveable { mutableLongStateOf(0) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                //这个会覆盖侧栏抽屉的滑动手势，所以需要处理下
                //Drag手势有 x y；水平只有x；垂直只有y
                detectHorizontalDragGestures { change, dragAmount ->
                    // debug
//                    println("dragAmount.x: ${dragAmount.x}")
//                    println("dragAmount.y: ${dragAmount.y}")

                    val curTimeInMills = System.currentTimeMillis()
                    //避免短时间滑动触发两次（抖动）
                    if(curTimeInMills - curTime.longValue > dragHandleInterval) {
                        if (dragAmount > 2) {  // left to right
                            if (isRtl) onRightToLeft() else onLeftToRight()
                        } else if (dragAmount < -2) {  // right to left
                            if (isRtl) onLeftToRight() else onRightToLeft()
                        }
                    }

                    //更新时间
                    curTime.longValue = curTimeInMills

                    // 消费事件，不知道有没有实际作用，理论上其他接受此事件的回调应检查如果已消费就不再处理
                    change.consume()
                }
            },
//            .systemBarsPadding()  //用脚手架的contentPadding就不需要这个了
    ) {
        if(isPreviewModeOn.value) {
            if(previewLoading) {
                LoadingText(
                    text = stringResource(R.string.loading),
                    contentPadding = contentPadding
                )
            }else {
                Column(
                    modifier = Modifier
                        //fillMaxSize 必须在最上面！要不然，文字不会显示在中间！
                        .fillMaxSize()
                        .padding(contentPadding)
                        .verticalScroll(previewScrollState)
                    ,
                ) {
                    MarkDownContainer(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        content = mdText.value,
                        basePathNoEndSlash = basePath.value,
                        fontSize = fontSize.intValue, //和编辑器字体大小保持一致
                    )

                    Spacer(Modifier.height(30.dp))
                }
            }
        } else {

            TextEditor(
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
                                isMultipleSelection = textEditorState.value.isMultipleSelectionMode,
                                isSelected = isSelected,
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
                    editableController.value.createCancelledState()
                }
                val iconList = listOf(
                    Icons.Filled.Delete,
                    Icons.Filled.ContentCut,
                    Icons.Filled.ContentCopy,
                    Icons.Filled.SelectAll
                )
                val iconTextList = listOf(
                    stringResource(R.string.delete),
                    stringResource(R.string.cut),
                    stringResource(R.string.copy),
                    stringResource(R.string.select_all),
                )
                val iconOnClickList = listOf(
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

                val getSelectedFilesCount = {textEditorState.value.getSelectedCount()}
                val hasLineSelected = {getSelectedFilesCount() > 0}
                val iconEnableList = listOf(
                    delete@{ readOnlyMode.not() && hasLineSelected() },  // delete
                    cut@{ hasLineSelected() },  // cut
                    copy@{ hasLineSelected() },  // copy
                    selectAll@{ true },  // select all
                )
                // BottomBar params block end



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
                    getSelectedFilesCount = getSelectedFilesCount,
                    countNumOnClickEnabled = false,
                    countNumOnClick = {}
                )
            }
        }

//        SmallFab(modifier = Modifier.align(Alignment.BottomEnd), icon = Icons.Filled.Save, iconDesc = stringResource(id = R.string.save)) {
//
//        }
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

