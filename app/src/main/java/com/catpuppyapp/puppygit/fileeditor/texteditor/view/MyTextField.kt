package com.catpuppyapp.puppygit.fileeditor.texteditor.view

import android.view.KeyEvent.KEYCODE_DEL
import android.view.KeyEvent.KEYCODE_DPAD_DOWN
import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_DPAD_UP
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.syntaxhighlight.base.PLFont
import com.catpuppyapp.puppygit.compose.DisableSoftKeyboard
import com.catpuppyapp.puppygit.dev.bug_Editor_WrongUpdateEditColumnIdx_Fixed
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextFieldState
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.MyLog


private const val TAG = "MyTextField"


@Composable
internal fun MyTextField(
    idx: Int,
    scrollIfInvisible:()->Unit,
    disableSoftKb:Boolean,
    readOnly:Boolean,
    focusThisLine:Boolean,
    textFieldState: TextFieldState,
    enabled: Boolean,
    onUpdateText: (TextFieldValue) -> Unit,
    onContainNewLine: (TextFieldValue) -> Unit,
    onAddNewLine: (TextFieldValue) -> Unit,
    onDeleteNewLine: () -> Unit,
    onFocus: () -> Unit,
    onUpFocus: () -> Unit,
    onDownFocus: () -> Unit,
    onLeftPressed: (current: TextFieldState, idx:Int, toHeadOrTail:Boolean) -> Unit,
    onRightPressed: (TextFieldState, Int, toHeadOrTail:Boolean) -> Unit,
    modifier: Modifier = Modifier,
    needShowCursorHandle:MutableState<Boolean>,
    lastEditedColumnIndexState:MutableIntState,
    searchMode:Boolean,   //因为更新光标有bug，会在搜索时错误更新编辑列导致搜索卡在原地，所以才传这个参数，否则用不到
    mergeMode:Boolean,
    fontSize:Int,
    fontColor:Color,
    bgColor:Color = Color.Unspecified,
) {
    val focusRequester = remember { FocusRequester() }
    val textStyle = LocalTextStyle.current

    //写法1
//    val currentTextField = remember { mutableStateOf(textFieldState) }  //重组不会重新执行
//    currentTextField.value = textFieldState  //重组会重新执行，这样通过value就能获取到最新值了（当然，第一次执行这个也会执行，所以和上面的mutableState()加起来等于执行了两次赋值，但state值相同不会触发更新，所以重复执行无所谓

    //写法2，这种写法等同于写法1，之所以使用currentTextFiled的地方能获取到重组后最新的值是因为本质上by是state.value的语法糖，所以使用by后的值等于 获取 state.value ，所以value一旦更新，调用state.value就能获取到最新值
    val currentTextField by rememberUpdatedState(newValue = textFieldState.value)

    val inDarkTheme = Theme.inDarkTheme

    DisableSoftKeyboard(disableSoftKb) {
        BasicTextField(
            value = currentTextField,
            readOnly = readOnly,
            enabled = enabled,
            onValueChange = {
//            println("start:${it.selection.start}, end:${it.selection.end}")  //test2024081116726433
                //更新最后编辑列
                //废弃，因为检查是否更新了文本浪费性能，反正启动也不定位到列，干脆不更新了，不过维护此值对性能影响不大，所以没删这段代码) 注：search且没更新文本，不更新光标，因为更新光标有bug，例如我搜索后定位到2行3列，但这里可能会自动变成3行4列，然后导致搜索不跳转，很傻逼
//            if((it.selection.start == it.selection.end) && (!searchMode || it.text!=textFieldState.value.text)) {  // start == end 说明不是选中状态而是点击某列，这时更新最后编辑列

                //目前仅在非searchMode时更新最后编辑列，日后修复更新列错误的bug后，改下flag变量即可，这的逻辑不用改。
                //ps: mergeMode也要用到搜索，所以也需要判断
                if((it.selection.start == it.selection.end) && ((!searchMode && !mergeMode) || bug_Editor_WrongUpdateEditColumnIdx_Fixed)) {  // start == end 说明不是选中状态而是点击某列，这时更新最后编辑列
                    lastEditedColumnIndexState.intValue = it.selection.start
                }

                //存在选中文本时，显示光标拖手和背景颜色（handle
                needShowCursorHandle.value = it.selection.start != it.selection.end

                // no change
                if (currentTextField == it) return@BasicTextField

                // scroll if invisible
                if(currentTextField.selection.start != it.selection.start
                    || currentTextField.selection.end != it.selection.end
                    || currentTextField.text != it.text
                ) {
                    scrollIfInvisible()
                }

                if (it.text.contains('\n')) onContainNewLine(it) else onUpdateText(it)
            },
            //字体样式:字体颜色、字体大小、背景颜色等
            textStyle = textStyle.copy(
                fontSize = fontSize.sp,
                color = fontColor,
                background = bgColor,
                fontFamily = PLFont.editorCodeFont(),
            ),
            //光标颜色
            cursorBrush = SolidColor(if(inDarkTheme) Color.LightGray else Color.Black),

            modifier = modifier
                .fillMaxWidth()
//            .wrapContentHeight()
                .padding(start = 2.dp)
//            .focusTarget()  //如果加这个，按一次返回会先解除focus，然后才会退出，操作有些繁琐，我感觉不加比较好
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        onFocus()
                    }
                }
                .onPreviewKeyEvent opke@{ event ->
                    //只响应按下key不响应松开
                    if (event.type != KeyEventType.KeyDown) return@opke false




//                    val value = textFieldState.value
                    val selection = currentTextField.selection

                    if (onPreviewDelKeyEvent(event, selection) { onDeleteNewLine() }) {
                        return@opke true
                    }

                    if (onPreviewDownKeyEvent(event) { onDownFocus() }) {
                        return@opke true
                    }

                    if (onPreviewUpKeyEvent(event) { onUpFocus() }) {
                        return@opke true
                    }

                    if (onPreviewLeftKeyEvent(event, textFieldState) { onLeftPressed(textFieldState, idx, false) }) {
                        return@opke true
                    }

                    if (onPreviewRightKeyEvent(event, textFieldState) { onRightPressed(textFieldState, idx, false) }) {
                        return@opke true
                    }

                    if (onPreviewHomeKeyEvent(event, textFieldState) { onLeftPressed(textFieldState, idx, true) }) {
                        return@opke true
                    }

                    if (onPreviewEndKeyEvent(event, textFieldState) { onRightPressed(textFieldState, idx, true) }) {
                        return@opke true
                    }

                    // even disable this, when new line '\n' added to text filed, will still split new line, so this is unnecessary
//                val b4 = onPreviewEnterKeyEvent(event) { onAddNewLine(currentTextField.copy(text = insertNewLineAtCursor(value))) }
//                if (b4) return@onPreviewKeyEvent true

                    // if enable, will can't use tab to indent, it will go to next field
//                val b5 = onPreviewTabKeyEvent(event) { onDownFocus() }
//                if (b5) return@onPreviewKeyEvent true

                    return@opke false
                }
        )
    }



    if(focusThisLine) {
        LaunchedEffect(Unit) {
            runCatching {
                focusRequester.requestFocus()
            }
        }
    }

}



private fun onPreviewHomeKeyEvent(event: KeyEvent, textFieldState: TextFieldState, invoke: () -> Unit): Boolean {
    return onPreviewHomeOrEndKeyEvent(event, textFieldState, trueHomeFalseEnd = true, invoke)
}

private fun onPreviewEndKeyEvent(event: KeyEvent, textFieldState: TextFieldState, invoke: () -> Unit): Boolean {
    return onPreviewHomeOrEndKeyEvent(event, textFieldState, trueHomeFalseEnd = false, invoke)
}

private fun onPreviewLeftKeyEvent(event: KeyEvent, textFieldState: TextFieldState, invoke: () -> Unit): Boolean {
    return onPreviewLeftOrRightKeyEvent(event, textFieldState, trueLeftFalseRight = true, invoke)
}

private fun onPreviewRightKeyEvent(event: KeyEvent, textFieldState: TextFieldState, invoke: () -> Unit): Boolean {
    return onPreviewLeftOrRightKeyEvent(event, textFieldState, trueLeftFalseRight = false, invoke)
}

private fun onPreviewUpKeyEvent(event: KeyEvent, invoke: () -> Unit): Boolean {
    return onPreviewUpOrDownKeyEvent(event, trueUpFalseDown = true, invoke)
}

private fun onPreviewDownKeyEvent(event: KeyEvent, invoke: () -> Unit): Boolean {
    return onPreviewUpOrDownKeyEvent(event, trueUpFalseDown = false, invoke)
}



private fun onPreviewDelKeyEvent(
    event: KeyEvent,
    selection: TextRange,
    invoke: () -> Unit
): Boolean {
    //不是删除不响应
    val isDelKey = event.nativeKeyEvent.keyCode == KEYCODE_DEL
    if (!isDelKey) return false

    //没删除到行开头不响应，这时由TextField负责更新数据，
    // 若删除到行开头则需要在TextFiled外部将当前TextField从列表移除，所以需要外部处理
    val isEmpty = selection == TextRange.Zero
    if (!isEmpty) return false

    //处理事件
    invoke()
    return true
}

private fun onPreviewUpOrDownKeyEvent(
    event: KeyEvent,

    trueUpFalseDown: Boolean,
    invoke: () -> Unit,
): Boolean {
    if(event.isCtrlPressed || event.isShiftPressed || event.isAltPressed || event.isMetaPressed) return false

    val expectedKey = if (trueUpFalseDown) KEYCODE_DPAD_UP else KEYCODE_DPAD_DOWN
    val isExpectedKey = event.nativeKeyEvent.keyCode == expectedKey
    if (!isExpectedKey) return false

//    val atStartOfLine = selection == TextRange.Zero
//    if (!atStartOfLine) return false

    invoke()
    return true
}

private fun onPreviewLeftOrRightKeyEvent(
    event: KeyEvent,
    field: TextFieldState,
    trueLeftFalseRight: Boolean,
    invoke: () -> Unit,
): Boolean {
    if(event.isCtrlPressed || event.isShiftPressed || event.isAltPressed || event.isMetaPressed || field.value.selection.collapsed.not()) return false

    val expectedKey = if(trueLeftFalseRight) KEYCODE_DPAD_LEFT else KEYCODE_DPAD_RIGHT
    val isExceptedKey = event.nativeKeyEvent.keyCode == expectedKey
    if (!isExceptedKey) return false


    invoke()

    return true
}


private fun onPreviewHomeOrEndKeyEvent(
    event: KeyEvent,
    field: TextFieldState,
    trueHomeFalseEnd: Boolean,
    invoke: () -> Unit,
): Boolean {
    if(event.isShiftPressed || event.isAltPressed || event.isMetaPressed || field.value.selection.collapsed.not()) return false

    val expectedKey = if(trueHomeFalseEnd) Key.MoveHome else Key.MoveEnd
    val isExpectedKey = event.key == expectedKey
    if (!isExpectedKey) return false

    val pos = field.value.selection.start
    if((event.isCtrlPressed && (pos != 0 && pos != field.value.text.length))) return false

    invoke()
    return true
}



private fun insertNewLineAtCursor(textFieldValue: TextFieldValue):String {
    val splitPosition = textFieldValue.selection.start  //光标位置，有可能在行末尾，这时和text.length相等，并不会越界
    val maxOfPosition = textFieldValue.text.length

    //这个情况不应该发生
    if (splitPosition < 0 || splitPosition > maxOfPosition) {  //第2个判断没错，就是大于最大位置，不是大于等于，没写错，光标位置有可能在行末尾，这时其索引和text.length相等，所以只有大于才有问题
        val errMsg = "splitPosition '$splitPosition' out of range '[0, $maxOfPosition]'"
        MyLog.e(TAG, "#getNewTextOfLine: $errMsg")
        throw RuntimeException(errMsg)
    }

    // 在光标位置插入个换行符然后返回就行了
    return textFieldValue.text.let {
        val sb = StringBuilder()
        sb.append(it.substring(0, splitPosition))
        sb.append("\n")

        //这里不需要判断，string.substring() 可用的startIndex最大值即为string.length，
        // 若是length，会返回空字符串，与期望一致
        sb.append(it.substring(splitPosition))

        sb.toString()
    }

}

