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
import com.catpuppyapp.puppygit.utils.isGoodIndexForList
import com.catpuppyapp.puppygit.utils.isGoodIndexForStr
import java.security.InvalidParameterException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val TAG = "EditorController"
private const val stateKeyTag = "EditorController"

class EditorController(
    textEditorState: TextEditorState,
    undoStack: UndoStack?
) {
    val _undoStack = undoStack
    private var isContentChanged:MutableState<Boolean>? = null
    private var editorPageIsContentSnapshoted:MutableState<Boolean>? = null
    private var onChanged: (newState:TextEditorState, trueSaveToUndoFalseRedoNullNoSave:Boolean?, clearRedoStack:Boolean) -> Unit = {newState, trueSaveToUndoFalseRedoNullNoSave, clearRedoStack ->  }


    private var _isMultipleSelectionMode = textEditorState.isMultipleSelectionMode
    private var _fieldsId = textEditorState.fieldsId
    val isMultipleSelectionMode get() = _isMultipleSelectionMode

    private val _fields = (textEditorState.fields).toMutableList()
    val fields get() = _fields.toList()

    private val _selectedIndices = (textEditorState.selectedIndices).toMutableList()
    val selectedIndices get() = _selectedIndices.toList()


    private val lock = ReentrantLock()

    init {
        selectFieldInternal(0)
    }

    private fun genNewState():TextEditorState {
//        println("_isMultipleSelectionMode:${_isMultipleSelectionMode}")
//        println("_selectedIndices:${_selectedIndices}")
        val newState = TextEditorState.create(
            fields = _fields.toList(),
            fieldsId = _fieldsId,
            selectedIndices = _selectedIndices.toList(),
            isMultipleSelectionMode = _isMultipleSelectionMode,
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
            val lastState = if(trueUndoFalseRedo) _undoStack?.undoStackPop() else _undoStack?.redoStackPop()
            if(lastState != null) {
                syncState(lastState)
                val clearRedoStack = false
                // 第2个值需要取反，因为执行redo的时候期望更新undostack，执行undo的时候期望更新redostack
                onChanged(genNewState(), !trueUndoFalseRedo, clearRedoStack)
            }
        }
    }

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
            if (targetIndex < 0 || fields.count() <= targetIndex) {
                throw InvalidParameterException("targetIndex out of range($targetIndex)")
            }

            if (!textFieldValue.text.contains('\n')) {
                throw InvalidParameterException("textFieldValue doesn't contains newline")
            }

            val splitFieldValues = textFieldValue.splitTextsByNL()
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
            if (targetIndex < 0 || fields.count() <= targetIndex) {
                throw InvalidParameterException("targetIndex out of range($targetIndex)")
            }

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
            if (targetIndex < 0 || fields.count() <= targetIndex) {
                throw InvalidParameterException("targetIndex out of range($targetIndex)")
            }

            if (textFieldValue.text.contains('\n')) {
                throw InvalidParameterException("textFieldValue contains newline")
            }

            if(isContentChanged?.value == false) {
                //这里不会发生npe，如果左值为null，等号右边的表达式不会被执行
                isContentChanged?.value = _fields[targetIndex].value.text != textFieldValue.text  // 旧值 != 新值
            }

            if(isContentChanged?.value == true) {
                editorPageIsContentSnapshoted?.value= false

                _fieldsId = TextEditorState.newId()
            }

            _fields[targetIndex] = _fields[targetIndex].copy(value = textFieldValue)
            onChanged(genNewState(), true, true)
        }
    }

    fun deleteField(targetIndex: Int) {
        lock.withLock {
            if (targetIndex < 0 || fields.count() <= targetIndex) {
                throw InvalidParameterException("targetIndex out of range($targetIndex)")
            }

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

            onChanged(genNewState(), null, true)
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

            onChanged(genNewState(), null, true)
        }
    }

    fun selectPreviousField() {
        lock.withLock {
            if (isMultipleSelectionMode) return
            val selectedIndex = selectedIndices.firstOrNull() ?: return
            if (selectedIndex == 0) return

            val previousIndex = selectedIndex - 1
            selectFieldInternal(previousIndex, SelectionOption.LAST_POSITION)
            onChanged(genNewState(), null, true)
        }
    }

    fun selectNextField() {
        lock.withLock {
            if (isMultipleSelectionMode) return
            val selectedIndex = selectedIndices.firstOrNull() ?: return
            if (selectedIndex == fields.lastIndex) return

            val nextIndex = selectedIndex + 1
            selectFieldInternal(nextIndex, SelectionOption.FIRST_POSITION)
            onChanged(genNewState(), null, true)
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

    private fun clearSelectedIndicesInternal() {
        val copyFields = _fields.toList().map { it.copy(isSelected = false) }
        _fields.clear()
        _fields.addAll(copyFields)
        _selectedIndices.clear()
    }

    private fun selectFieldInternal(
        targetIndex: Int,
        option: SelectionOption = SelectionOption.NONE,
        forceAdd:Boolean=false,  // 为true：若没添加则添加，添加了则什么都不做；为false：已添加则移除，未添加则添加
        columnStartIndexInclusive:Int=0,
        columnEndIndexExclusive:Int=columnStartIndexInclusive,
        requireSelectLine:Boolean = true,
    ) {
        if (targetIndex < 0 || fields.count() <= targetIndex) {
            throw InvalidParameterException("targetIndex out of range($targetIndex)")
        }

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

            }else{ //no touch selected lines, just go to target line
                val copyTarget = target.copy(
                    value = target.value.copy(selection = selection)
                )

                _fields[targetIndex] = copyTarget
            }

        }else{  //非多选模式，定位光标到对应行，并选中行
            val copyTarget = target.copy(
                isSelected = true,  // selected target line
                value = target.value.copy(selection = selection), // copy for new selection range
            )
            this.clearSelectedIndicesInternal()  // clear selected lines
            _fields[targetIndex] = copyTarget  // update line
            _selectedIndices.add(targetIndex)  // add line to selected list
        }
    }

    private fun TextFieldValue.splitTextsByNL(): List<TextFieldValue> {
        var position = 0
        val splitTexts = this.text.split("\n").map {
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

            //关闭所有条目的选中状态
            val newFieldsList = fields.mapIndexed{idx,it ->
                it.copy(isSelected = idx==index)  //为当前点击的条目开启选中状态。注：若index为-1或其他无效索引值，则不会选中任何行。
            }

            val newState = TextEditorState.create(
                fieldsId = _fieldsId,
                fields = newFieldsList,  //把当前点击而开启选择行模式的那行的选中状态设为真了
                selectedIndices = if(isGoodIndexForList(index, newFieldsList)) listOf(index) else listOf(),  //默认选中的索引包含当前选中行即可，因为肯定是点击某一行开启选中模式的，所以只会有一个索引
                isMultipleSelectionMode = true,
            )

            syncState(newState)
            onChanged(newState, true, true)
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
            )

            syncState(newState)
            onChanged(newState, true, true)
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
            )

            syncState(newState)
            onChanged(newState, true, true)
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
                )
            }


            syncState(newState)
            onChanged(newState, true, true)
        }
    }

    fun createCancelledState(){
        lock.withLock {
            val newState = TextEditorState.create(
                fieldsId = _fieldsId,
                fields = fields.map { TextFieldState(id = it.id, value = it.value, isSelected = false) },
                selectedIndices = emptyList(),
                isMultipleSelectionMode = false,
            )

            syncState(newState)
            onChanged(newState, true, true)
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
