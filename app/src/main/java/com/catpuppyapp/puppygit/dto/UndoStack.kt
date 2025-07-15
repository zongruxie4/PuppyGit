package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.codeeditor.MyCodeEditor
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.getSecFromTime
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val TAG = "UndoStack"

// small size, big interval = less mem use but less steps
// big size, small interval = more mem use but more steps
// size越小，间隔越大，越省内存记录的步骤越粗略;
// size越大，间隔越小，越费内存，记录的步骤越精细
private const val defaultSizeLimit = 15
private const val defaultSaveIntervalInSec = 20

class UndoStack(
    /**
    用来标记这是哪个文件的stack
     */
    var filePath:String,
    /**
     * 记多少步
     */
    var sizeLimit: Int = defaultSizeLimit,

    /**
     * 保存间隔，秒数，为0则不限只要状态变化就立即存一版
     */
    var undoSaveIntervalInSec:Int = defaultSaveIntervalInSec,

    /**
     * utc秒数，上次保存时间，用来和时间间隔配合实现在几秒内只保存一次，若为0，无视时间间隔，立即存一版本，然后更新时间为当前秒数
     */
    var undoLastSaveAt: Long = 0L,


    private var undoStack:LinkedList<TextEditorState> = LinkedList(),
    private var redoStack:LinkedList<TextEditorState> = LinkedList(),
    private var undoLock: ReentrantLock = ReentrantLock(true),
    private var redoLock: ReentrantLock = ReentrantLock(true),
    var codeEditor: MyCodeEditor? = null,
) {

    fun reset(filePath:String, force:Boolean) {
        cleanUnusedStyles()

        if(force.not() && filePath == this.filePath) {
            return
        }

        this.filePath = filePath
        sizeLimit = defaultSizeLimit
        undoSaveIntervalInSec = defaultSaveIntervalInSec
        undoLastSaveAt = 0L
        undoStack = LinkedList()
        redoStack = LinkedList()
        undoLock = ReentrantLock(true)
        redoLock = ReentrantLock(true)
    }

    private fun cleanUnusedStyles() {
        codeEditor?.let { codeEditor ->
            if(codeEditor.stylesMap.isNotEmpty()) {
                undoLock.withLock {
                    redoLock.withLock {
                        for (i in codeEditor.stylesMap) {
                            if(!containsNoLock(i.value.fieldsId)) {
                                codeEditor.cleanStylesByFieldsId(i.value.fieldsId)
                            }
                        }
                    }
                }
            }
        }
    }

    fun copyFrom(other:UndoStack) {
        filePath = other.filePath
        sizeLimit = other.sizeLimit
        undoSaveIntervalInSec = other.undoSaveIntervalInSec
        undoLastSaveAt = other.undoLastSaveAt
        undoStack = other.undoStack
        redoStack = other.redoStack
        undoLock = other.undoLock
        redoLock = other.redoLock
    }

    fun undoStackIsEmpty():Boolean {
        return undoStack.isEmpty()
    }

    fun redoStackIsEmpty():Boolean {
        return redoStack.isEmpty()
    }

    fun undoStackSize():Int {
        return undoStack.size
    }

    fun redoStackSize():Int {
        return redoStack.size
    }

    /**
     * @return true saved, false not saved
     */
    fun undoStackPush(state: TextEditorState):Boolean {
        return undoLock.withLock {
            undoStackPushNoLock(state)
        }
    }

    private fun undoStackPushNoLock(state: TextEditorState):Boolean {
        val headState = peek(undoStack)
        // first time save or switched multi selection mode, save without interval check
        val selectModeChanged = headState == null || headState.isMultipleSelectionMode != state.isMultipleSelectionMode

        val now = getSecFromTime()

        val snapshotLastSaveAt = undoLastSaveAt
        //在时间间隔内只存一版
        if(selectModeChanged || undoSaveIntervalInSec == 0 || snapshotLastSaveAt == 0L || (now - snapshotLastSaveAt) > undoSaveIntervalInSec) {
            push(undoStack, state)
            undoLastSaveAt = now

            //若超过数量限制移除第一个
            if(undoStack.size.let { it > 0 && it > sizeLimit }) {
                codeEditor?.cleanStylesByFieldsId(undoStack.removeAt(0).fieldsId)
            }

            return true
        }

        // not into stack, clear it's styles
        codeEditor?.cleanStylesByFieldsId(state.fieldsId)

        return false
    }

    fun undoStackPop(): TextEditorState? {
        return undoLock.withLock {
            undoStackPopNoLock()
        }
    }

    private fun undoStackPopNoLock(): TextEditorState? {
        return pop(undoStack)
    }

    /**
     * @return true saved, false not saved
     */
    fun redoStackPush(state: TextEditorState):Boolean {
        redoLock.withLock {
            push(redoStack, state)
            return true
        }
    }

    fun redoStackPop(): TextEditorState? {
        redoLock.withLock {
            undoLock.withLock {
                //为使弹出的状态可立刻被undo stack存上，所以将上次存储时间清0
                undoLastSaveAt = 0
            }

//            remainOnceRedoStackCount()

//            val last = pop(redoStack)
//            redoLastPop.value = last
//            return last

            // 这里只需pop redoStack，不需push undoStack，
            // editor state会在执行pop后触发一次onChanged，
            // 会在其中执行push undoStack，配合上面的存储时间清0，就可立刻将pop的redo状态入undo栈
            return pop(redoStack)
        }
    }

    fun redoStackClear() {
        redoLock.withLock {
            for (i in redoStack) {
                codeEditor?.cleanStylesByFieldsId(i.fieldsId)
            }

            redoStack.clear()
        }
    }

    private fun push(stack: MutableList<TextEditorState>, state: TextEditorState) {
        try {
            // add to tail
            stack.add(state)
        }catch (e:Exception) {
            MyLog.e(TAG, "#push, err: ${e.stackTraceToString()}")
        }
    }

//    @Deprecated("弃用，感觉不需要做太多判断，应该调用者自己判断是否需要入栈")
//    private fun pushDeprecated(stack: MutableList<TextEditorState>, state: TextEditorState) {
//        try {
//            //第2个判断是个粗略判断，避免在只有一个条目且内容相等时重复添加内容到栈
//            if(stack.isEmpty() || !(state.fields.size==1 && stack.last().fields.size == 1 && state.fields.first()==stack.last().fields.first()) || stack.last().fieldsId != state.fieldsId) {
////            if(stack.isEmpty() || state.fields != peek(stack)?.fields) {
//                stack.add(state)
//
//                if(stack.size > sizeLimit) {
//                    stack.removeAt(0)
//                }
//            }
//        }catch (e:Exception) {
//            MyLog.e(TAG, "#push, err: ${e.stackTraceToString()}")
//        }
//    }

    private fun pop(stack: MutableList<TextEditorState>): TextEditorState? {
        return try {
            stack.removeAt(stack.size - 1)
        }catch (e:Exception) {
            null
        }
    }

    private fun peek(stack: MutableList<TextEditorState>): TextEditorState? {
        return try {
            stack.get(stack.size - 1)
        }catch (e:Exception) {
            null
        }
    }

    fun updateUndoHeadIfNeed(latestState: TextEditorState) {
        if(undoStack.isEmpty()) {
            return
        }

        undoStackPopThenPush(latestState)
    }

    private fun undoStackPopThenPush(state: TextEditorState) {
        undoLock.withLock {
            undoStackPopNoLock()
            undoStackPushNoLock(state)
        }
    }

    fun contains(fieldsId: String):Boolean {
        return undoLock.withLock {
            redoLock.withLock {
                containsNoLock(fieldsId)
            }
        }
    }

    private fun containsNoLock(fieldsId: String):Boolean {
        return undoContainsNoLock(fieldsId) || redoContainsNoLock(fieldsId)
    }

    private fun undoContainsNoLock(fieldsId: String) : Boolean {
        return undoStack.find { it.fieldsId == fieldsId } != null
    }

    private fun redoContainsNoLock(fieldsId:String) : Boolean {
        return redoStack.find { it.fieldsId == fieldsId } != null
    }

}
