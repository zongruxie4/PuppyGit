package com.catpuppyapp.puppygit.fileeditor.texteditor.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.MyTextFieldState
import com.catpuppyapp.puppygit.syntaxhighlight.base.PLFont
import com.catpuppyapp.puppygit.ui.theme.Theme


//private const val TAG = "MyTextField"


@Composable
internal fun MyTextField(
    scrollIfInvisible:()->Unit,
    readOnly:Boolean,
    focusThisLine:Boolean,
    textFieldState: MyTextFieldState,
    enabled: Boolean,
    onUpdateText: (TextFieldValue, textChanged: Boolean?) -> Unit,
    onContainNewLine: (TextFieldValue) -> Unit,
    onFocus: () -> Unit,
    modifier: Modifier = Modifier,
    needShowCursorHandle:MutableState<Boolean>,
    fontSize:Int,
    fontColor:Color,
) {
    val inDarkTheme = Theme.inDarkTheme
    val textStyle = LocalTextStyle.current


    val currentTextField = textFieldState.value.let { remember(it) { mutableStateOf(it) } }
    val focusRequester = remember { FocusRequester() }


    // if the `value` is not equals to `BasicTextField` held value, then the ime state will reset
    BasicTextField(
        value = currentTextField.value,
        readOnly = readOnly,
        enabled = enabled,
        onValueChange = { newState ->
            val lastTextField = currentTextField.value


            //存在选中文本时，显示光标拖手和背景颜色（handle）
            needShowCursorHandle.value = newState.selection.start != newState.selection.end

            // no change
            if (lastTextField == newState) return@BasicTextField

            // used for some checks
            val textChanged = lastTextField.text.length != newState.text.length || lastTextField.text != newState.text

            // scroll if invisible
            // when input some chars but target line invisible, will scroll to that line
            if(textChanged) {
                scrollIfInvisible()
            }

            if (newState.text.contains('\n')) {
                onContainNewLine(newState)
            } else {
                // only update state when no linebreak,
                //   if has line break, still update, will got unexpected line break,
                //   for avoid it, just wait the text field list updated it, nothing need to do here

                val newState = if(textChanged) {
                    newState
                } else {
                    // copy to avoid lost highlighting styles when text no changes,
                    // if styles still lost, try use `textFieldState.value` as `lastTextField`
                    lastTextField.copy(selection = newState.selection)
                }

                currentTextField.value = newState

                onUpdateText(newState, textChanged)
            }
        },
        //字体样式:字体颜色、字体大小、背景颜色等
        textStyle = textStyle.copy(
            fontSize = fontSize.sp,
            color = fontColor,
//            background = bgColor,
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
    )



    if(focusThisLine) {
        LaunchedEffect(Unit) {
            runCatching {
                focusRequester.requestFocus()
            }
        }
    }

}



