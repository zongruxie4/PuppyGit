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
import androidx.compose.ui.focus.onFocusChanged
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
    onUpdateText: (TextFieldValue, textChanged: Boolean?) -> Unit,
    onContainNewLine: (TextFieldValue) -> Unit,
    onFocus: (TextFieldValue) -> Unit,
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

            //存在选中文本时，显示光标拖手和背景颜色（handle）
//            needShowCursorHandle.value = newState.selection.start != newState.selection.end

//            // no change, in my tests, never happened, so compare them doesn't make sense, and waste cpu
//            if (lastTextField == newState) return@ovc
//            // test
//            if (lastTextField == newState) throw RuntimeException("no changed, still called onValueChange")

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


//                println("text: ${newState.text}")
//                println("newState.annotatedString == lastTextField.annotatedString: ${newState.annotatedString == lastTextField.annotatedString}")

                val newState = if(textChanged) {
//                  // copy style for new text if possible,
                    //   this will use more memory and cpu, but can be reduce syntax highlighting flicker(only for current editing line)
                    //   usually the effect is acceptable, except when users try edit a huge file or huge line, or both
                    // 为新文本拷贝旧文本的样式，这样可减缓甚至完全避免增量更新样式时闪烁（仅针对当前编辑行，其他行没辙，可能还是会闪），但是，会耗费更多内存和cpu，为了达到效果，
                    //   带来的后果一般可接受，除非用户编辑大文件或包含很多文本的行，我发现中文比英语更简洁，两行表达的内容超过了英语的3行，哈哈
                    val newTextLen = newState.text.length
                    // 这是 `TextRange`，不是`IntRange`，左闭右开，所以`end`是可以等于`text.length`的，`SpanStyle`是不可变的，所以不用深拷贝
                    // this is `TextRange`, not `IntRange`, so the `end` can be equals to `text.length`
                    // `SpanStyle` is immutable, so no need to do deep copy.
                    val validSpans = lastTextField.annotatedString.spanStyles.filter { it.start >= 0 && it.end <= newTextLen }

                    if(validSpans.isEmpty()) {
                        newState
                    }else {
                        newState.annotatedString.let {
                            newState.copy(
                                annotatedString = AnnotatedString(
                                    text = it.text,
                                    spanStyles = validSpans,
                                    paragraphStyles = it.paragraphStyles
                                )
                            )
                        }
                    }
                } else {
                    // copy to avoid lost highlighting styles when text no changes,
                    //   if styles still lost, try use `textFieldState.value` as `lastTextField`
                    // must copy all fields of newState except its `annotatedString`,
                    //   else maybe lost styles or ime will have weird behavior(due to
                    //   ime may use `composition` field to draw underline for text,
                    //   if not update that field, will cause bug for ime)
                    newState.copy(annotatedString = lastTextField.annotatedString)
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



