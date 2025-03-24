package com.catpuppyapp.puppygit.fileeditor.texteditor.state

import androidx.compose.runtime.Immutable
import com.catpuppyapp.puppygit.etc.Ret
import com.catpuppyapp.puppygit.fileeditor.texteditor.controller.EditorController.Companion.createInitTextFieldStates
import com.catpuppyapp.puppygit.screen.shared.FuckSafFile
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.doActIfIndexGood
import com.catpuppyapp.puppygit.utils.generateRandomString
import java.io.OutputStream

private const val lb = "\n"

//不实现equals，直接比较指针地址，反而性能可能更好，不过状态更新可能并不准确，例如fields没更改的情况也会触发页面刷新
@Immutable
class TextEditorState private constructor(

    /**
     the `fieldsId` only about fields, same fieldsId should has same fields，but isn't enforce (但不强制要求)
     */
    val fieldsId: String,
    val fields: List<TextFieldState>,

    val selectedIndices: List<Int>,
    val isMultipleSelectionMode: Boolean,

    var focusingLineIdx: Int?,

) {
    /**
     * note: if you want to save lines to file, recommend to use `dumpLines` instead
     */
    fun getAllText(): String {
        val sb = StringBuilder()
        fields.forEach { sb.append(it.value.text).append(lb) }

        //移除遍历时多添加的末尾换行符，然后返回
        return sb.removeSuffix(lb).toString()

        //below code very slow when file over 1MB，主要原因是字符串拼接，次要是隐含多次循环
//        return fields.map { it.value.text }.foldIndexed("") { index, acc, s ->
//            if (index == 0) acc + s else acc + lb + s
//        }
    }

    fun dumpLinesAndGetRet(output: OutputStream, lineBreak:String=lb): Ret<Unit?> {
        try {
            dumpLines(output, lineBreak)
            return Ret.createSuccess(null)
        }catch (e:Exception) {
            return Ret.createError(null, e.localizedMessage ?: "dump lines err", exception = e)
        }
    }

    fun dumpLines(output: OutputStream, lineBreak:String=lb) {
        val lastIndex = fields.size - 1
        output.bufferedWriter().use { out ->
            for(i in fields.indices) {
                out.write(fields[i].value.text)

                if(i != lastIndex) {
                    out.write(lineBreak)
                }
            }
        }
    }

    fun getSelectedText(): String {
        // 把索引排序，然后取出文本，拼接，返回
        val sb = StringBuilder()
        selectedIndices.toSortedSet().forEach { selectedLineIndex->
            doActIfIndexGood(selectedLineIndex, fields) { field ->
                sb.append(field.value.text).append(lb)
            }
        }

        return sb.removeSuffix(lb).toString()

        //废弃：保留末尾多出来的换行符，就是要让它多一个，不然复制多行时粘贴后会定位到最后一行开头，反直觉，要解决这个问题需要改掉整个行处理机制，太麻烦了，所以暂时这样规避下，其实这样倒合理，在粘贴内容到一行的中间部位时，感觉比之前还合理
//        return sb.toString()

    }

    //获取选择行记数（获取选择了多少行）
    fun getSelectedCount():Int{
//        return selectedIndices.toSet().filter{ it>=0 }.size  //toSet()是为了去重，我不确定是否一定没重复，去下保险；filter {it>=0} 是为了避免里面有-1，我记得初始值好像是往selectedIndices里塞个-1。
        return selectedIndices.size  //toSet()是为了去重，我不确定是否一定没重复，去下保险；filter {it>=0} 是为了避免里面有-1，我记得初始值好像是往selectedIndices里塞个-1。
    }

    fun contentIsEmpty(): Boolean {
        return fields.isEmpty() || (fields.size == 1 && fields[0].value.text.isEmpty())
    }

    /**
     * 此函数不比较fields数组，缺点是返回结果并不准确(真或假都不靠谱)，优点是快
     */
    fun maybeNotEquals(other: TextEditorState):Boolean {
        return this.fieldsId != other.fieldsId || this.isMultipleSelectionMode != other.isMultipleSelectionMode || this.selectedIndices != other.selectedIndices
    }

    companion object {
        fun create(text: String, fieldsId: String, isMultipleSelectionMode:Boolean = false, focusingLineIdx:Int? = null): TextEditorState {
            return create(
                lines = text.lines(),
                fieldsId= fieldsId,
                isMultipleSelectionMode = isMultipleSelectionMode,
                focusingLineIdx = focusingLineIdx

            )
        }

        fun create(lines: List<String>, fieldsId: String, isMultipleSelectionMode:Boolean = false, focusingLineIdx:Int? = null): TextEditorState {
            return create(
                fields = lines.createInitTextFieldStates(),
                fieldsId= fieldsId,
                selectedIndices = listOf(),
                isMultipleSelectionMode = isMultipleSelectionMode,
                focusingLineIdx = focusingLineIdx

            )
        }

        fun create(file: FuckSafFile, fieldsId: String, isMultipleSelectionMode:Boolean = false, focusingLineIdx:Int? = null): TextEditorState {
            //这里`addNewLineIfFileEmpty`必须传true，以确保和String.lines()行为一致，不然若文件末尾有空行，读取出来会少一行
            return create(
                lines = FsUtils.readLinesFromFile(file, addNewLineIfFileEmpty = true),
                fieldsId= fieldsId,
                isMultipleSelectionMode = isMultipleSelectionMode,
                focusingLineIdx = focusingLineIdx
            )
        }

        fun create(
            fields: List<TextFieldState>,
            fieldsId: String,
            selectedIndices: List<Int>,
            isMultipleSelectionMode: Boolean,
            focusingLineIdx: Int?
        ): TextEditorState {
            return TextEditorState(
                fieldsId= fieldsId,
                fields = fields,
                selectedIndices = selectedIndices,
                isMultipleSelectionMode = isMultipleSelectionMode,
                focusingLineIdx = focusingLineIdx
            )
        }

        fun newId():String {
            return generateRandomString(20)
        }
    }
}
