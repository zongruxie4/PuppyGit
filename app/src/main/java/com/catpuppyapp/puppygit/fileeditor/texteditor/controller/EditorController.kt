package com.catpuppyapp.puppygit.fileeditor.texteditor.controller

import androidx.compose.runtime.MutableState
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.dto.UndoStack
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextFieldState
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.SearchPos
import com.catpuppyapp.puppygit.fileeditor.texteditor.view.SearchPosResult
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.utils.isGoodIndexForStr
import java.security.InvalidParameterException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val TAG = "EditorController"
private const val stateKeyTag = "EditorController"

class EditorController(
    textEditorState: TextEditorState,
    undoStack: UndoStack
) {

    val _undoStack = undoStack
    private var isContentChanged:MutableState<Boolean>? = null
    private var editorPageIsContentSnapshoted:MutableState<Boolean>? = null
    private var onChanged: (newState:TextEditorState, trueSaveToUndoFalseRedoNullNoSave:Boolean?, clearRedoStack:Boolean) -> Unit = {newState, trueSaveToUndoFalseRedoNullNoSave, clearRedoStack ->  }

    val focusingLineIdx = textEditorState.focusingLineIdx
    private var _isMultipleSelectionMode = textEditorState.isMultipleSelectionMode
    private var _fieldsId = textEditorState.fieldsId
    val isMultipleSelectionMode get() = _isMultipleSelectionMode

    private val _fields = (textEditorState.fields).toMutableList()
    val fields get() = _fields.toList()

    private val _selectedIndices = (textEditorState.selectedIndices).toMutableList()
    val selectedIndices get() = _selectedIndices.toList()


    private val lock = ReentrantLock()

    //这为什么要选中第0行？？？？
//    init {
//        selectFieldInternal(0)
//    }


    private val targetIndexValidOrThrow = { targetIndex:Int ->
        if (targetIndex < 0 || targetIndex >= _fields.size) {
            throw IndexOutOfBoundsException("targetIndex out of range($targetIndex)")
        }
    }

    private fun genNewState():TextEditorState {
//        println("_isMultipleSelectionMode:${_isMultipleSelectionMode}")
//        println("_selectedIndices:${_selectedIndices}")
        val newState = TextEditorState.create(
            fields = _fields.toList(),
            fieldsId = _fieldsId,
            selectedIndices = _selectedIndices.toList(),
            isMultipleSelectionMode = _isMultipleSelectionMode,
            focusingLineIdx = focusingLineIdx
        )

        return newState
    }

    fun undo() {
        undoOrRedo(true)
    }

    fun redo() {
        undoOrRedo(false)
    }

    private fun undoOrRedo(trueUndoFalseRedo:Boolean) {
        lock.withLock {
            val lastState = if(trueUndoFalseRedo) _undoStack.undoStackPop() else _undoStack.redoStackPop()
            if(lastState != null) {
                isContentChanged?.value=true
                editorPageIsContentSnapshoted?.value=false
//                _fieldsId = TextEditorState.newId()

                syncState(lastState)
                //无论执行redo还是undo，都不需要清redoStack，所以这里此值传false
                val clearRedoStack = false
                // 第2个值需要取反，因为执行redo的时候期望更新undoStack，执行undo的时候期望更新redoStack
                onChanged(genNewState(), !trueUndoFalseRedo, clearRedoStack)
            }
        }
    }

    /**
     * 只要调用syncState的地方必须先对state里的数据创建拷贝，不然执行clear等操作会被源state里的数据也清掉
     * 注：不需要同步focusingIndex，那个直接共享的state
     */
    fun syncState(state: TextEditorState) {
        lock.withLock {
            _fieldsId = state.fieldsId

            _isMultipleSelectionMode = state.isMultipleSelectionMode

            _selectedIndices.clear()
            _selectedIndices.addAll(state.selectedIndices)

            _fields.clear()
            _fields.addAll(state.fields)
        }
    }

    /**
     * x 已修复） 20241203: 发现有bug，无法找到 " w " 这样字符，已经修复，在无匹配时将被查找的文件中的字符串索引向后移一位再和关键字从头匹配即可（反向查找时是向前移一位然后和关键字末尾匹配）
        @param toNext : if `toNext` is false, will search to previous
         缺陷：不支持查找包含换行符的关键字
     * @return SearchPos : if not found, will return SearchPos(-1,-1)
      */
    fun doSearch(keyword:String, toNext:Boolean, startPos: SearchPos): SearchPosResult {
        val funName="doSearch"

        fun getCurTextOfIndex(idx:Int, list:List<TextFieldState>):String{
            return list[idx].value.text.lowercase()
        }

        try {
            //搜索关键字
            //开始从当前位置查找关键字，直到找到，或者从头到尾都找不到
            val f = fields
            if(f.isEmpty() || keyword.isEmpty()) {
                return SearchPosResult.NotFound
            }

            val goodIndex = isGoodIndexForList(startPos.lineIndex, f)
            //如果行号有误，把列号重置为0或最后一行把列号重置为0或最后一列；否则使用原行号和列号（不过列号仍有可能有误
            val curPos = startPos.copy(lineIndex = if(goodIndex) startPos.lineIndex else {if(toNext) 0 else f.size-1})
            var curText = getCurTextOfIndex(curPos.lineIndex, f)
            curPos.columnIndex = if(goodIndex) startPos.columnIndex else {if(toNext) 0 else curText.length-1}
            val endPosOverThis = curPos.copy()  //越过这一行这一列，就查到开头了，结束了
            var curIndexOfKeyword= if(toNext) 0 else keyword.length-1

            curPos.columnIndex = if(isGoodIndexForStr(curPos.columnIndex, curText)){
                curPos.columnIndex
            }else{
                if(toNext){
                    0
                }else {
                    curText.length-1
                }
            }

            var char = if(curText.isEmpty()) null else curText[curPos.columnIndex]
            var charOfKeyword = keyword[curIndexOfKeyword]
            var looped = false

            val invalidIndex = -1

            // last value of matched a char
            var resetIndex = invalidIndex
            while(true) {
//                println("在搜索:lineIndex=${curPos.lineIndex}, columnIndex=${curPos.columnIndex}")  //test1791022120240812
                //matched 1 char
                if(char != null && char == charOfKeyword) {
//                    println(curPos) //test1791022120240812
                    if(toNext){
                        curIndexOfKeyword++
                    }else {
                        curIndexOfKeyword--
                    }

                    //found!
                    if(!isGoodIndexForStr(curIndexOfKeyword, keyword)) {
                        val foundLineIdx = curPos.lineIndex
                        val foundColumnIdx = if(toNext) curPos.columnIndex+1-keyword.length else curPos.columnIndex

                        var nextColumn = if(toNext) foundColumnIdx+keyword.length else foundColumnIdx-1
                        var nextLineIdx = foundLineIdx
                        //需要换行
                        if(!isGoodIndexForStr(nextColumn, curText)) {
                            nextLineIdx = if(toNext) nextLineIdx+1 else nextLineIdx-1
                            if(!isGoodIndexForList(nextLineIdx, f)) {
                                nextLineIdx = if(toNext) 0 else f.size-1
                            }

                            nextColumn = if(toNext) 0 else getCurTextOfIndex(nextLineIdx, f).length-1
                        }
//                        println("posResult:${SearchPosResult(foundPos = curPos.copy(lineIndex = foundLineIdx, columnIndex = foundColumnIdx), nextPos = SearchPos(lineIndex =  nextLineIdx, columnIndex = nextColumn))}")  //test1791022120240812
                        return SearchPosResult(
                            foundPos = curPos.copy(lineIndex = foundLineIdx, columnIndex = foundColumnIdx),
                            nextPos = SearchPos(lineIndex =  nextLineIdx, columnIndex = nextColumn)
                        )
                    }


                    //更新被搜索的字符串索引
                    if(toNext) {
                        curPos.columnIndex++
                    }else {
                        curPos.columnIndex--
                    }

                    // 第一次匹配，若匹配关键字失败，重置到当前字符的下一个字符(若反向查找，则是上一个字符)
                    if(resetIndex == invalidIndex) {
                        resetIndex = curPos.columnIndex
                    }


                }else {  // not matched!
                    // reset index of keyword
                    curIndexOfKeyword= if(toNext) 0 else keyword.length-1

                    // if last is matched, keep target text index at same place for matching to head or tail of keyword

                    //如果上次未匹配到，正常往前推进，否则重置索引指向上次匹配到的字符串之后的第一个字符
                    if(resetIndex == invalidIndex) {  //上次未匹配到
                        //更新被搜索的字符串索引
                        if(toNext) {
                            curPos.columnIndex++
                        }else {
                            curPos.columnIndex--
                        }
                    }else {  //上次匹配到过部分字符
                        //匹配过，将索引重置为下一个索引，例如：被搜索字符串：abbbbcd，关键字bbc，若在索引1开始匹配到，索引3匹配失败，那下一个被搜索的索引应该是2
                        curPos.columnIndex = resetIndex
                        //重置索引每次使用一次后，需将其置为无效索引
                        resetIndex = invalidIndex
                    }

                }

                //这里取出的字符有两种情况，要么是关键字中的下一个字符，要么是重置后的开头或末尾字符
                // 情况1： 索引仍有效，说明只匹配了部分关键字，需要继续查找
                // 情况2： 无匹配，索引已重置到关键字开头或末尾
                charOfKeyword = keyword[curIndexOfKeyword]

                //查找到当前行开头或末尾了，该换行了
                if(!isGoodIndexForStr(curPos.columnIndex, curText)) {  //需要换行
                    if(looped) {  //过了最初查找的那行且需要换行，说明查找完了，可返回了
//                        val lineOverLooped = if(toNext) curPos.lineIndex > endPosOverThis.lineIndex else curPos.lineIndex < endPosOverThis.lineIndex
//                        if(lineOverLooped) {
//                            return SearchPos.NotFound
//                        }

                        //如果循环查找，还是进入到换行的这块代码，就可以直接返回了，已经搜索过头了
                        return SearchPosResult.NotFound

                    }

                    //执行到这，looped必定为假，意味着还没查找到起点，还得继续找

                    //换行后将重置索引重新设为无效
                    resetIndex = invalidIndex

                    //换行，需要reset index of keyword，不然搜索时行末尾和下行开头会被认为是连续的而导致结果出错
                    curIndexOfKeyword= if(toNext) 0 else keyword.length-1
                    charOfKeyword = keyword[curIndexOfKeyword]


                    //换行
                    if(toNext) curPos.lineIndex++ else curPos.lineIndex--

                    //查找到文件第一行或最后一行了，该从头或从尾重新查了 （循环查找）
                    if(!isGoodIndexForList(curPos.lineIndex, f)){
                        curPos.lineIndex = if(toNext) 0 else f.size-1
                    }

                    curText = getCurTextOfIndex(curPos.lineIndex, f)

                    curPos.columnIndex = if(toNext) 0 else curText.length-1

                    //换行换到最初查找的那行了
                    if(curPos.lineIndex==endPosOverThis.lineIndex) { //隐含 `&& !looped`
                        looped = true
                    }
                }

                //注释这段代码可确保完整查找起始搜索那行以免光标在起始行的关键字中间时漏掉关键字
                //光标在关键字中间时搜索会有bug：如果启用此代码块，将会在匹配到起始搜索行的起始列时终止，导致无法匹配以下情况：例如：关键字"1234"，全文只有一行匹配文本为"1[光标]2345"，这时，向上搜索，将会无法匹配到"1234"，因为代码会在抵达光标处时返回not found，只要光标在关键字中间皆可复现此bug（例如把前面的匹配文本改为"12[光标]345"，也会not found），不过注意：因为搜索模式下更新列索引有bug所以禁用了搜索模式下更新列索引的机制，所以需要在非搜索模式把光标放到关键字中间才能复现此bug。
                //找了一轮了，在起始行，检查是否已经超过起始列，如果超了直接返回，如果没超，继续搜索，直到超了起始列（在这返回）或者超了起始行（在上面的代码返回）
//                if(looped) {
//                    //循环查找，行
//                    //这里加等号是正确的，另外：即使把等号去掉，也无法修复上面提到的光标在关键字中间时搜索会有bug的问题，因为只要光标位置离起始列的距离大于1个字符，就还是无法完整匹配整行，所以还是会有那个bug
//                    val end = if(toNext) curPos.columnIndex >= endPosOverThis.columnIndex else curPos.columnIndex <= endPosOverThis.columnIndex
//                    if(end){
//                        return SearchPosResult.NotFound
//                    }
//                }

                //把空行当作null，无匹配即可
                char = if(curText.isEmpty()) null else curText[curPos.columnIndex]
            }


        }catch (e:Exception) {
            Msg.requireShowLongDuration("err:"+e.localizedMessage)
            MyLog.e(TAG, "#$funName err: keyword=$keyword, toNext=$toNext, startPos=$startPos, err=${e.stackTraceToString()}")
            return SearchPosResult.NotFound
        }

    }

//    @Deprecated("有缺陷，无法显示光标，做不到点按某行某列的效果，所以改用 `selectField()` 了")
//    fun selectTextInLine(lineIndex:Int, textStartIndexInclusive:Int, textEndIndexExclusive:Int) {
//        doActIfIndexGood(lineIndex, _fields) { target ->
//            lock.withLock {
//                val copyTarget = target.copy(
//                    value = target.value.copy(selection = TextRange(textStartIndexInclusive, textEndIndexExclusive))
//                )
//                _fields[lineIndex] = copyTarget
//
//                onChanged(genNewState(), null)
//            }
//        }
//    }

    fun splitNewLine(targetIndex: Int, textFieldValue: TextFieldValue) {
        lock.withLock {
            targetIndexValidOrThrow(targetIndex)

            if (!textFieldValue.text.contains('\n')) {
                throw InvalidParameterException("textFieldValue doesn't contains newline")
            }

            val splitFieldValues = splitTextsByNL(textFieldValue)
            val firstSplitFieldValue = splitFieldValues.first()
            _fields[targetIndex] = _fields[targetIndex].copy(value = firstSplitFieldValue, isSelected = false)

            val newSplitFieldValues = splitFieldValues.subList(1, splitFieldValues.count())
            val newSplitFieldStates = newSplitFieldValues.map { TextFieldState(value = it, isSelected = false) }
            _fields.addAll(targetIndex + 1, newSplitFieldStates)

            val lastNewSplitFieldIndex = targetIndex + newSplitFieldValues.count()
            selectFieldInternal(lastNewSplitFieldIndex)

            isContentChanged?.value=true
            editorPageIsContentSnapshoted?.value=false
            _fieldsId = TextEditorState.newId()
            onChanged(genNewState(), true, true)
        }
    }

    fun splitAtCursor(targetIndex: Int, textFieldValue: TextFieldValue) {
        lock.withLock {
            targetIndexValidOrThrow(targetIndex)

            val splitPosition = textFieldValue.selection.start
            if (splitPosition < 0 || textFieldValue.text.length < splitPosition) {
                throw InvalidParameterException("splitPosition out of range($splitPosition)")
            }

            val firstStart = 0
            val firstEnd = if (splitPosition == 0) 0 else splitPosition
            val first = textFieldValue.text.substring(firstStart, firstEnd)

            val secondStart = if (splitPosition == 0) 0 else splitPosition
            val secondEnd = textFieldValue.text.count()
            val second = textFieldValue.text.substring(secondStart, secondEnd)

            val firstValue = textFieldValue.copy(first)
            val firstState = _fields[targetIndex].copy(value = firstValue, isSelected = false)
            _fields[targetIndex] = firstState

            val secondValue = TextFieldValue(second, TextRange.Zero)
            val secondState = TextFieldState(value = secondValue, isSelected = false)
            _fields.add(targetIndex + 1, secondState)

            selectFieldInternal(targetIndex + 1)

            isContentChanged?.value=true
            editorPageIsContentSnapshoted?.value=false
            _fieldsId = TextEditorState.newId()

            onChanged(genNewState(), true, true)
        }
    }


    fun updateField(targetIndex: Int, textFieldValue: TextFieldValue) {
        lock.withLock {
            targetIndexValidOrThrow(targetIndex)

            if (textFieldValue.text.contains('\n')) {
                throw InvalidParameterException("textFieldValue contains newline")
            }

            //检查字段内容是否改变，由于没改变也会调用此方法，所以必须判断下才能知道内容是否改变
            val contentChanged = _fields[targetIndex].value.text != textFieldValue.text  // 旧值 != 新值

            if(contentChanged) {
                isContentChanged?.value = true
                editorPageIsContentSnapshoted?.value = false
                _fieldsId = TextEditorState.newId()
            }

            //更新字段
            _fields[targetIndex] = _fields[targetIndex].copy(value = textFieldValue)

            onChanged(genNewState(), if(contentChanged) true else null, contentChanged)
        }
    }


    fun appendOrReplaceFields(targetIndex: Int, textFiledStates: List<TextFieldState>, trueAppendFalseReplace:Boolean) {
        lock.withLock {
            if(textFiledStates.isEmpty()) {
                return
            }

            if(targetIndex < 0) {
                return
            }

            //若超过size，追加到末尾
            val targetIndex = if(trueAppendFalseReplace) targetIndex+1 else targetIndex

            var targetLineSelected = false

            val targetIndexOverSize = targetIndex >= _fields.size
            //若超过有效索引则追加到末尾；否则追加到目标行（原本的目标行会后移）
            if(targetIndexOverSize) {
                _fields.addAll(textFiledStates)
            }else {
                //如果是replace，保留之前行的选择状态
                //到这才能确定索引没越界，所以在这取而不是在if else之前取
                if(trueAppendFalseReplace.not()) {
                    targetLineSelected = _fields[targetIndex].isSelected
                }

                _fields.addAll(targetIndex, textFiledStates)
            }

            //若是replace则移除之前的当前行
            if(trueAppendFalseReplace.not()) {
                //更新目标行的选择状态使其和旧行一致
                if(targetLineSelected) {
                    _fields[targetIndex] = _fields[targetIndex].copy(isSelected = true)
                }

                //粘贴之后，之前的目标行会被顶到当前要粘贴的内容后面，如果是replace，则把这行删除
                _fields.removeAt(targetIndex + textFiledStates.size)
            }

            //如果目标索引没超过原集合大小，则判断下是否需要更新选中索引列表；否则不用判断，因为超过大小的话，等于无效索引，肯定不会在已选中列表
            if(targetIndexOverSize.not()) {
                val selectedIndexOffsetValue = textFiledStates.size.let {if(trueAppendFalseReplace) it else (it-1)}
                val indexNeedOffset:(selectedIdx:Int)->Boolean = if(trueAppendFalseReplace){{ it >= targetIndex }} else {{ it > targetIndex }}

                val focusIndex = focusingLineIdx.value
                //更新聚焦行索引
                if(focusIndex != null && indexNeedOffset(focusIndex)) {
                    focusingLineIdx.value = focusIndex+selectedIndexOffsetValue
                }

                //更新已选中索引
                val selectedIndices = selectedIndices
                if(selectedIndices.isNotEmpty()) {
                    //更新选中索引集合
                    for((idx, value) in selectedIndices.withIndex()) {
                        if(indexNeedOffset(value)) {
                            _selectedIndices[idx] = value+selectedIndexOffsetValue
                        }
                    }
                }
            }



            //通知页面状态变化
            isContentChanged?.value = true
            editorPageIsContentSnapshoted?.value = false
            _fieldsId = TextEditorState.newId()

            //更新状态
            onChanged(genNewState(), true, true)
        }
    }

    fun deleteField(targetIndex: Int) {
        lock.withLock {
            targetIndexValidOrThrow(targetIndex)

            if (targetIndex == 0) {
                return
            }

            val toText = _fields[targetIndex - 1].value.text
            val fromText = _fields[targetIndex].value.text

            val concatText = toText + fromText
            val concatSelection = TextRange(toText.count())
            val concatTextFieldValue =
                TextFieldValue(text = concatText, selection = concatSelection)
            val toTextFieldState =
                _fields[targetIndex - 1].copy(value = concatTextFieldValue, isSelected = false)

            _fields[targetIndex - 1] = toTextFieldState

            _fields.removeAt(targetIndex)

            selectFieldInternal(targetIndex - 1)

            isContentChanged?.value=true
            editorPageIsContentSnapshoted?.value= false
            _fieldsId = TextEditorState.newId()

            onChanged(genNewState(), true, true)
        }
    }

    //第1个参数是行索引；第2个参数是当前行的哪个位置
    //若只传columnStartIndexInclusive，相当于定位到某一行的某一列；若传columnStartIndexInclusive 和 columnEndIndexExclusive，相当于选中某行的某一段文字
    fun selectField(
        targetIndex: Int,
        option: SelectionOption = SelectionOption.NONE,
        columnStartIndexInclusive:Int=0,
        columnEndIndexExclusive:Int=columnStartIndexInclusive,
        requireSelectLine: Boolean=true, // if true, will add targetIndex into selected indices, set false if you don't want to select this line(e.g. when you just want go to line)
    ) {
        lock.withLock {
            selectFieldInternal(
                targetIndex,
                option,
                columnStartIndexInclusive=columnStartIndexInclusive,
                columnEndIndexExclusive=columnEndIndexExclusive,
                requireSelectLine = requireSelectLine
            )

            onChanged(genNewState(), null, false)
        }
    }

    //第1个参数是行索引；第2个参数是当前行的哪个位置
    fun selectFieldSpan(targetIndex: Int) {
        lock.withLock {
            if(selectedIndices.isEmpty()) {  //如果选中列表为空，仅选中当前行
                selectFieldInternal(targetIndex, forceAdd = true)
            }else {  //选中列表不为空，执行区域选择
                val lastSelectedIndex = selectedIndices.last()
                val startIndex = Math.min(targetIndex, lastSelectedIndex)
                val endIndexExclusive = Math.max(targetIndex, lastSelectedIndex) +1

                if(startIndex >= endIndexExclusive
                    || startIndex<0 || startIndex>fields.lastIndex  // lastIndex is list.size - 1
                    || endIndexExclusive<0 || endIndexExclusive>fields.size
                ) {
                    return
                }

                for(i in startIndex..<endIndexExclusive) {
                    selectFieldInternal(i, forceAdd = true)
                }
            }

            onChanged(genNewState(), null, false)
        }
    }

    fun selectPreviousField() {
        lock.withLock {
            if (isMultipleSelectionMode) return
            val selectedIndex = selectedIndices.firstOrNull() ?: return
            if (selectedIndex == 0) return

            val previousIndex = selectedIndex - 1
            selectFieldInternal(previousIndex, SelectionOption.LAST_POSITION)
            onChanged(genNewState(), null, false)
        }
    }

    fun selectNextField() {
        lock.withLock {
            if (isMultipleSelectionMode) return
            val selectedIndex = selectedIndices.firstOrNull() ?: return
            if (selectedIndex == fields.lastIndex) return

            val nextIndex = selectedIndex + 1
            selectFieldInternal(nextIndex, SelectionOption.FIRST_POSITION)
            onChanged(genNewState(), null, false)
        }
    }
//
//    fun clearSelectedIndex(targetIndex: Int) {
//        lock.withLock {
//            if (targetIndex < 0 || fields.count() <= targetIndex) {
//                return@withLock
//            }
//
//            _fields[targetIndex] = _fields[targetIndex].copy(isSelected = false)
//            _selectedIndices.remove(targetIndex)
//            onChanged(state)
//        }
//    }
//
//    fun clearSelectedIndices() {
//        lock.withLock {
//            this.clearSelectedIndicesInternal()
//            onChanged(state)
//        }
//    }

//    fun setMultipleSelectionMode(value: Boolean) {
//        lock.withLock {
//            if (isMultipleSelectionMode && !value) {
//                this.clearSelectedIndicesInternal()
//            }
//            _isMultipleSelectionMode = value
//            onChanged(state)
//        }
//    }

    fun setOnChangedTextListener(isContentChanged: MutableState<Boolean>,editorPageIsContentSnapshoted: MutableState<Boolean>, onChanged: (newState:TextEditorState, trueSaveToUndoFalseRedoNullNoSave:Boolean?, clearRedoStack:Boolean) -> Unit) {
        this.onChanged = onChanged
        this.isContentChanged = isContentChanged
        this.editorPageIsContentSnapshoted = editorPageIsContentSnapshoted

    }

//    fun deleteAllLine() {
//        lock.withLock {
//            _fields.clear()
//            _fields.addAll(emptyList<String>().createInitTextFieldStates())
//            _selectedIndices.clear()
//
//            // I am not sure, this should needn't call it, if call when select mode on, maybe will selected first line
//            //我不确定这个是否应该调用，应该不需要，如果 调用 且 选择模式状态为开启，则会在删除所有内容后选中第1行
////            selectFieldInternal(0)
//
//            isContentChanged?.value=true
//            editorPageIsContentSnapshoted?.value= false
//            _fieldsId = TextEditorState.newId()
//
//            onChanged(genNewState())
//        }
//    }
//
//    fun deleteSelectedLines() {
//        lock.withLock {
//            val targets = selectedIndices.mapNotNull { _fields.getOrNull(it) }
//            _fields.removeAll(targets)
//            _selectedIndices.clear()
//
//            isContentChanged?.value=true
//            editorPageIsContentSnapshoted?.value= false
//            _fieldsId = TextEditorState.newId()
//
//            onChanged(genNewState())
//        }
//    }


    private fun selectFieldInternal(
        targetIndex: Int,
        option: SelectionOption = SelectionOption.NONE,
        forceAdd:Boolean=false,  // 为true：若没添加则添加，添加了则什么都不做；为false：已添加则移除，未添加则添加
        columnStartIndexInclusive:Int=0,
        columnEndIndexExclusive:Int=columnStartIndexInclusive,
        requireSelectLine:Boolean = true,
    ) {
        targetIndexValidOrThrow(targetIndex)

        val target = _fields[targetIndex]

        val selection = when (option) {
            SelectionOption.CUSTOM -> {
                TextRange(columnStartIndexInclusive, columnEndIndexExclusive)
            }
            SelectionOption.NONE -> target.value.selection
            SelectionOption.FIRST_POSITION -> TextRange.Zero
            SelectionOption.LAST_POSITION -> {
                if (target.value.text.lastIndex != -1) {
                    TextRange(target.value.text.lastIndex + 1)
                } else {
                    TextRange.Zero
                }
            }
        }


        if(isMultipleSelectionMode) {  //多选模式，添加选中行列表或从列表移除
            if(requireSelectLine) {
                if(forceAdd) {  //强制添加
                    val isSelected = _fields[targetIndex].isSelected
                    //未添加则添加，否则什么都不做
                    if(!isSelected) {
                        val copyTarget = target.copy(
                            isSelected = true,
                            value = target.value.copy(selection = selection)
                        )
                        _fields[targetIndex] = copyTarget
                        _selectedIndices.add(targetIndex)
                    }
                }else {  //切换添加和移除
                    val isSelected = !_fields[targetIndex].isSelected
                    val copyTarget = target.copy(
                        isSelected = isSelected,
                        value = target.value.copy(selection = selection)
                    )
                    _fields[targetIndex] = copyTarget

                    if (isSelected) _selectedIndices.add(targetIndex) else _selectedIndices.remove(targetIndex)
                }

            }else{ //no touch selected lines, just go to target line，这个并不是选择行，只是同一行，但选中不同的范围，原本是想用来高亮查找的关键字的，但有点毛病，目前实现成仅将光标定位到关键字前面，但无法选中范围，不过我忘了现在定位到关键字前面是不是依靠的这段代码了
                val copyTarget = target.copy(
                    value = target.value.copy(selection = selection)
                )

                _fields[targetIndex] = copyTarget
                focusingLineIdx.value = targetIndex
            }

        }else{  //非多选模式，定位光标到对应行，并选中行
            //更新行选中范围并focus行
            _fields[targetIndex] = target.copy(value = target.value.copy(selection = selection))
            focusingLineIdx.value = targetIndex



        //下面的代码会执行全量浅拷贝，性能差

            //清除选中索引
//            val copyFields = _fields.toList().map { it.copy(isSelected = false) }
//            _fields.clear()
//            _fields.addAll(copyFields)
//            _selectedIndices.clear()

            //选中当前行
//            val copyTarget = target.copy(
//                isSelected = true,  // selected target line
//                value = target.value.copy(selection = selection), // copy for new selection range
//            )

//            _fields[targetIndex] = copyTarget  // update line
//            _selectedIndices.add(targetIndex)  // add line to selected list
        }
    }

    private fun splitTextsByNL(textFieldValue: TextFieldValue): List<TextFieldValue> {
        var position = 0
        val splitTexts = textFieldValue.text.split("\n").map {
            position += it.count()
            it to position
        }

        return splitTexts.mapIndexed { index, pair ->
            if (index == 0) {
                TextFieldValue(pair.first, TextRange(pair.second))
            } else {
                TextFieldValue(pair.first, TextRange.Zero)
            }
        }
    }

    /**
     * 本函数可能会很费时间，所以加了suspend
     */
    suspend fun getKeywordCount(keyword: String): Int {
        val f = fields
        var count = 0

        f.forEach {
            val text = it.value.text
            var startIndex=0

            while (true) {
                //text为空字符串或索引加过头都会在这返回
                if(!isGoodIndexForStr(startIndex, text)) {
                    break
                }

                //执行到这，索引一定good，所以可放心indexOf
                val indexOf = text.indexOf(string=keyword, startIndex=startIndex, ignoreCase=true)

                if(indexOf == -1) {  //未找到
                    break
                }else {  //如果找到了，更新计数，更新下次查找起点
//                    println("line=${idx+1}, column=$indexOf")  //debug

                    count++
                    startIndex = indexOf+keyword.length
                }
            }
        }

        return count
    }

    /**
     * @return Pair(chars count, lines count)
     */
    fun getCharsAndLinesCount(): Pair<Int, Int> {
        val f= fields
        val lines = f.size

        var chars = 0
        f.forEach { chars+=it.value.text.length }

        return Pair(chars, lines)
    }

    /**
     * 从startIndex开始根据FindDirection查找，找到predicate返回true的行后，返回其索引和内容
     */
    fun indexAndValueOf(startIndex:Int, direction: FindDirection, predicate: (text:String) -> Boolean, includeStartIndex:Boolean): Pair<Int, String> {
        val list = fields
        var retPair = Pair(-1, "")

        try {
            val range = if(direction== FindDirection.UP) {
                val endIndex = if(includeStartIndex) startIndex else (startIndex-1)
                if(!isGoodIndexForList(endIndex, list)) {
                    throw RuntimeException("bad index range")
                }

                (0..endIndex).reversed()
            }else {
                val tempStartIndex =if(includeStartIndex) startIndex else (startIndex+1)
                if(!isGoodIndexForList(tempStartIndex, list)) {
                    throw RuntimeException("bad index range")
                }

                tempStartIndex..list.lastIndex
            }

            for(i in range) {
                val item = list[i]
                val text = item.value.text
                if(predicate(text)) {
                    retPair = Pair(i, text)
                    break
                }
            }

        }catch (_:Exception) {

        }

        return retPair

    }

    fun deleteLineByIndices(indices:List<Int>) {
        if(indices.isEmpty()) {
            return
        }

        lock.withLock {
            val newList = fields.filterIndexed {index, _ ->
                !indices.contains(index)
            }

            _fields.clear()
            _fields.addAll(newList)

            isContentChanged?.value=true
            editorPageIsContentSnapshoted?.value= false
            _fieldsId = TextEditorState.newId()

            onChanged(genNewState(), true, true)
        }
    }


    /**
     * 注：若index为无效索引，初始不会选择任何行
     */
    fun createMultipleSelectionModeState(index:Int) {
        lock.withLock {
            //用14431行的近2mb文件简单测试了下，性能还行
            //进入选择模式，数据不变，只是 MultipleSelectionMode 设为true且对应行的isSelected设为true

            //必须创建拷贝，只要调用synsState的地方都必须创建拷贝，不然会和源list冲突
            val newFields = _fields.toMutableList()
            val newSelectedIndices = mutableListOf<Int>()
            //若索引有效，选中对应行
            if(index >= 0 && index < newFields.size) {
                newFields[index] = newFields[index].copy(isSelected = true)
                newSelectedIndices.add(index)
            }

            val newState = TextEditorState.create(
                fieldsId = _fieldsId,  //这个似乎是用来判断是否需要将当前状态入撤销栈的，如果文件内容不变，不用更新此字段
                fields = newFields,  //把当前点击而开启选择行模式的那行的选中状态设为真了
                selectedIndices = newSelectedIndices,  //默认选中的索引包含当前选中行即可，因为肯定是点击某一行开启选中模式的，所以只会有一个索引
                isMultipleSelectionMode = true,
                focusingLineIdx = focusingLineIdx

            )

            syncState(newState)
            onChanged(newState, null, false)
        }

    }


    fun createCopiedState() {
        lock.withLock {
            val newState = TextEditorState.create(
    //        fields = fields.map { TextFieldState(it.id, it.value, isSelected = false)},  //对所有选中行解除选中
    //        isMultipleSelectionMode = false,  //退出选择模式
                fieldsId = _fieldsId,
                fields = fields,
                selectedIndices = selectedIndices,
                isMultipleSelectionMode = isMultipleSelectionMode,  //拷贝和删除不要退出选择模式(准确来说是不要改变是否处于选择模式，若以后以非多选模式创建CopiedState，也不会自动进入选择模式)，这样用户可间接实现剪切功能，因为选择很多行有时候很废力，所以除非用户明确按取消，否则不要自动解除选择模式
                focusingLineIdx = focusingLineIdx

            )

            syncState(newState)
            onChanged(newState, null, false)
        }

    }


    fun createSelectAllState() {
        lock.withLock {
            //TODO 这里应该可以避免拷贝吧？不过这里是引用拷贝，问题不大
            val selectedIndexList = mutableListOf<Int>()
            val selectedFieldList = mutableListOf<TextFieldState>()
            for((idx, f) in fields.withIndex()) {
                selectedIndexList.add(idx)
                selectedFieldList.add(TextFieldState(f.id,f.value, isSelected = true))
            }
            //我不太确定 data类的copy是深还是浅，但我可以确定这里不需要深拷贝，所以用下面创建浅拷贝的方法创建对象
            val newState = TextEditorState.create(
                fieldsId = _fieldsId,
                fields = selectedFieldList,
                selectedIndices = selectedIndexList,
                isMultipleSelectionMode = true,
                focusingLineIdx = focusingLineIdx

            )

            syncState(newState)
            onChanged(newState, null, false)
        }
    }


    fun createDeletedState(){
        lock.withLock {

            //把没选中的行取出来，作为新的文件内容
            val newFields = fields.filterIndexed { index, _ ->
                !selectedIndices.contains(index)
            }

            //如果选中行列表selectedIndices无重复索引且不可能有错误索引的话，
            // 可简单对比已选中行selectedIndices.size和fields.size来判断有无选中所有行，
            // 但我不确定selectedIndices是否会有重复索引和是否会有无效索引，
            // 所以这里暂时不用size判断是否选中了所有行

            //判断是否删除了所有行，如果新的文件内容列表newFields一个元素都没有，则说明全删了
            val isDeletedAll = newFields.isEmpty()

            //如果是删除所有，创建一个空状态；否则创建删除选中行后的状态
            val newState = if(isDeletedAll) {
                TextEditorState.create(
                    fieldsId = TextEditorState.newId(),
                    text = "",
                    isMultipleSelectionMode = isMultipleSelectionMode,
                )
            }else {
                //即使全删了，完全创建新状态，也不要影响选择模式，要不然有的情况自动退选择模式，有的不退，容易让人感到混乱
                TextEditorState.create(
                    fieldsId = TextEditorState.newId(),
                    fields = newFields,
                    selectedIndices = emptyList(),
                    isMultipleSelectionMode = isMultipleSelectionMode,  //一般来说此值在这会是true，不过，这里的语义是“不修改是否选择模式”，所以把这个字段传过去比直接设为true要合适
                    focusingLineIdx = focusingLineIdx

                )
            }

            isContentChanged?.value=true
            editorPageIsContentSnapshoted?.value=false
            _fieldsId = TextEditorState.newId()

            syncState(newState)
            onChanged(newState, true, true)
        }
    }

    fun quitSelectionMode(keepSelectionModeOn:Boolean = false){
        lock.withLock {
            val newState = TextEditorState.create(
                fieldsId = _fieldsId,
                fields = _fields.map { TextFieldState(id = it.id, value = it.value, isSelected = false) },
                selectedIndices = emptyList(),
                isMultipleSelectionMode = keepSelectionModeOn,
                focusingLineIdx = focusingLineIdx

            )

            syncState(newState)
            onChanged(newState, null, false)
        }
    }

    fun clearSelected(){
        quitSelectionMode(keepSelectionModeOn = true)
    }

    /**
     * 计算行索引对应的以像素为单位大概滚动位置。
     * 注：只能算个大概，如果有图片之类的，文字可能只占一行，但预览可能占很多行，这时滚动可能会有较大偏差
     */
    fun lineIdxToPx(lineIndex: Int, fontSizeInPx:Float, screenWidthInPx:Float, screenHeightInPx:Float):Float {
        // 好，最简单的情况，直接在开头，不用换算
        if(lineIndex == 0) {
            return  0f
        }

        var count = 0

        val lineHeight = UIHelper.guessLineHeight(fontSizeInPx)
        val (oneLineHowManyChars, luckyOffset) = getOneLineHowManyCharsAndLuckyOffset(lineHeight = lineHeight, screenWidthInPx = screenWidthInPx, screenHeightInPx = screenHeightInPx, indexToPx = true)
        var targetPx = luckyOffset

        val iterator = fields.iterator()
        while(iterator.hasNext()) {
            if(count++ > lineIndex) {
                break
            }

            val it = iterator.next()

            val lineChars = it.value.text.length
            //软换行行数 (soft-wrap lines)
            val softLineCount = (lineChars / oneLineHowManyChars).coerceAtLeast(1)
            //行数 乘 字体大小，粗略得到当前物理行占用多少像素
            targetPx += (softLineCount * lineHeight)
        }

        return targetPx
    }


    /**
     * 计算索引对应的大概行索引。
     * 注：只能算个大概，如果有图片之类的，文字可能只占一行，但预览可能占很多行，这时滚动可能会有较大偏差
     */
    fun pxToLineIdx(targetPx: Int, fontSizeInPx:Float, screenWidthInPx:Float, screenHeightInPx:Float):Int {
        // 好，最简单的情况，直接在开头，不用换算
        if(targetPx == 0) {
            return  0
        }

        val lineHeight = UIHelper.guessLineHeight(fontSizeInPx)
        val (oneLineHowManyChars, luckyOffset) = getOneLineHowManyCharsAndLuckyOffset(lineHeight = lineHeight, screenWidthInPx = screenWidthInPx, screenHeightInPx = screenHeightInPx, indexToPx = false)

        var pos = luckyOffset

        var targetLineIndex = 0
        val iterator = fields.iterator()
        while (iterator.hasNext()) {
            if(pos >= targetPx) {
                break
            }

            targetLineIndex++

            val it = iterator.next()

            val lineChars = it.value.text.length
            //软换行行数 (soft-wrap lines)
            val softLineCount = (lineChars / oneLineHowManyChars).coerceAtLeast(1)
            //行数 乘 字体大小，粗略得到当前物理行占用多少像素
            pos += (softLineCount * lineHeight)
        }

        return targetLineIndex
    }

    private fun getOneLineHowManyCharsAndLuckyOffset(lineHeight: Float, screenWidthInPx: Float, screenHeightInPx: Float, indexToPx:Boolean): Pair<Int, Float> {
        val oneLineHowManyChars = (screenWidthInPx / lineHeight).toInt()
        val luckyOffset = UIHelper.getLuckyOffset(indexToPx = indexToPx, screenWidthInPx = screenWidthInPx, screenHeightInPx = screenHeightInPx)
        return Pair(oneLineHowManyChars, luckyOffset)
    }

    /**
     * 如果当前行为空行，用text替换当前行；否则追加到当前行后面
     */
    fun appendTextToLastSelectedLine(text: String) {
        //若取消注释这个，复制空行再粘贴会作废，除非在复制那里处理下，ifEmpty返回个空行，
        // 但是不如在这处理，直接忽略这个条件，
        // 然后用空字符串调用.lines()会得到一个空行，就符合复制一个空行粘贴一个空行的需求了，
        // 别的地方也省得改了
//        if(text.isEmpty()) {
//            return
//        }

        //因为要粘贴到选中行后边，所以必须得有选中条目
        val selectedIndices = selectedIndices
        if(selectedIndices.isEmpty()) {
            return
        }

        //selectedIndices里存的是fields的索引，若是无效索引，直接返回
        val lastSelectedIndexOfLine = selectedIndices.last()
        if(lastSelectedIndexOfLine < 0 || lastSelectedIndexOfLine >= _fields.size) {
            MyLog.w(TAG, "#appendTextToLastSelectedLine: invalid index `$lastSelectedIndexOfLine` of `_fields`")
            return
        }


        //空字符串会拆分出一个空行，一个"\n"会拆分出两个空行
        val lines = text.lines()

        //目标若是空行则replace，否则append
        val trueAppendFalseReplace = _fields[lastSelectedIndexOfLine].value.text.isNotEmpty()

        //执行添加，到这才真正修改Editor的数据
        appendOrReplaceFields(targetIndex = lastSelectedIndexOfLine, textFiledStates = lines.map { TextFieldState(value = TextFieldValue(text = it)) }, trueAppendFalseReplace = trueAppendFalseReplace)

    }

    fun getContentOfLineIndex(lineIndex: Int): String {
        val f = _fields
        return if(lineIndex >= 0 && lineIndex < f.size) {
            f[lineIndex].value.text
        }else {
            ""
        }
    }


    enum class SelectionOption {
        FIRST_POSITION,
        LAST_POSITION,
        NONE,
        CUSTOM, // should provide start and end index when use this value
    }

    companion object {
        fun List<String>.createInitTextFieldStates(): List<TextFieldState> {
            if (this.isEmpty()) return listOf(TextFieldState(isSelected = false))
            return this.mapIndexed { _, s ->
                TextFieldState(
                    value = TextFieldValue(s, TextRange.Zero),
                    isSelected = false
                )
            }
        }
    }
}

enum class FindDirection {
    UP,
    DOWN
}
