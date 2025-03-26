package com.catpuppyapp.puppygit.fileeditor.texteditor.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue

@Stable
class TextFieldState(
    //这个id本来是作为LazyColumn的item key的，可能和触发重组有关，但实际上我很多地方都忘了在修改这个对象后更换此id，所以最后取消用这个值在做key了，不知道compose怎么判断是否需要重组，不过能用同时不卡就行了，有问题再处理
//    val id: String = getRandomUUID(),

    val value: TextFieldValue = TextFieldValue(),

    //为避免已选择list和条目list数量都过大时判断某行是否选中的性能差，所以保留这个字段，耗内存但在选中行和文件全部行数都很大时性能更好，不然判断一行是否被选中需要最大遍历两个list.size相乘的次数
    //另外，不要用此变量判断某行是否被聚焦，改用TextEditorState的focusingLineIndex去判断，
    // 不然每次聚焦一行都要先解除其他所有行的isSelected状态，这种方式需要全量浅拷贝，浪费性能
    val isSelected: Boolean = false
) {
    fun copy(
//        id: String = this.id,
        value: TextFieldValue = this.value,
        isSelected: Boolean = this.isSelected
    ) = TextFieldState(
//        id = id,
        value = value,
        isSelected = isSelected
    )

    override fun toString(): String {
        return "TextFieldState(value=$value, isSelected=$isSelected)"
    }


    //不太确定：非常极端的情况下，如果一个文件不换行，就一行，有非常多内容（可能几M），那这样比较不如直接比较地址？
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        // or `other !is TextFieldState`
        if (javaClass != other?.javaClass) return false

        other as TextFieldState

        if (value != other.value) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + isSelected.hashCode()
        return result
    }

}
