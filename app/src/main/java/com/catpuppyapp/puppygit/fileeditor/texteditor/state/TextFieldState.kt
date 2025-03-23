package com.catpuppyapp.puppygit.fileeditor.texteditor.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.utils.getRandomUUID

@Stable
data class TextFieldState(
    val id: String = getRandomUUID(),
    val value: TextFieldValue = TextFieldValue(),

    //为避免已选择list和条目list数量都过大时判断某行是否选中的性能差，所以保留这个字段，耗内存但在选中行和文件全部行数都很大时性能更好，不然判断一行是否被选中需要最大遍历两个list.size相乘的次数
    //另外，不要用此变量判断某行是否被聚焦，改用TextEditorState的focusingLineIndex去判断，
    // 不然每次聚焦一行都要先解除其他所有行的isSelected状态，这种方式需要全量浅拷贝，浪费性能
    val isSelected: Boolean = false
)
