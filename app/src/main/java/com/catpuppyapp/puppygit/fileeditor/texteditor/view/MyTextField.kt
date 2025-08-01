package com.catpuppyapp.puppygit.fileeditor.texteditor.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
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
    onUpdateText: (TextFieldValue) -> Unit,
    onContainNewLine: (TextFieldValue) -> Unit,
//    onFocus: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
//    needShowCursorHandle:MutableState<Boolean>,
    fontSize:Int,
    fontColor:Color,
) {
    val inDarkTheme = Theme.inDarkTheme
    val textStyle = LocalTextStyle.current

    val currentTextField = textFieldState.value.let { remember(it) { mutableStateOf(it) } }
    val focusRequester = remember { FocusRequester() }


    // NOTE: if the `value` is not equals to `BasicTextField` held value, then the ime state will reset
    BasicTextField(
        value = currentTextField.value,
        readOnly = readOnly,
        enabled = enabled,
        onValueChange = ovc@{ newState ->
            val lastTextField = currentTextField.value

            // used for some checks
            val textChanged = lastTextField.text.length != newState.text.length || lastTextField.text != newState.text

            // scroll if invisible
            // when input some chars but target line invisible, will scroll to that line
            if(textChanged) {
                scrollIfInvisible()
            }



            if (newState.text.contains('\n')) {
                // if contains new line, wait new list update the state
                onContainNewLine(newState)
            } else {
                currentTextField.value = newState

                onUpdateText(newState)
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
//            .onFocusChanged {
//                if (it.isFocused) {
//                    onFocus(currentTextField.value)
//                }
//            }
    )



    if(focusThisLine) {
        LaunchedEffect(Unit) {
            runCatching {
                focusRequester.requestFocus()
            }
        }
    }

}
