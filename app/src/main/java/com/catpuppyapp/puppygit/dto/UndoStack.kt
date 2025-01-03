package com.catpuppyapp.puppygit.dto

import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.mutableLongStateOf
import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.getSecFromTime
import kotlinx.coroutines.delay
import java.util.LinkedList
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private const val TAG = "UndoStack"

data class UndoStack(
    /**
    用来标记这是哪个文件的stack
     */
    val filePath:String,
    /**
     * 记多少步
     */
    val sizeLimit: Int = 100,

    /**
     * 保存间隔，秒数，为0则不限只要状态变化就立即存一版
     */
//    val undoSaveIntervalInSec:Int = 5,
    //5秒的用户体验并不好，有可能漏选，用0秒了
    val undoSaveIntervalInSec:Int = 0,

    /**
     * utc秒数，上次保存时间，用来和时间间隔配合实现在几秒内只保存一次，若为0，无视时间间隔，立即存一版本，然后更新时间为当前秒数
     */
    var undoLastSaveAt:MutableLongState = mutableLongStateOf(0),


    private val undoStack:LinkedList<TextEditorState> = LinkedList(),
    private val redoStack:LinkedList<TextEditorState> = LinkedList(),
    private val undoLock: ReentrantLock = ReentrantLock(true),
    private val redoLock: ReentrantLock = ReentrantLock(true),
) {
//    private var redoLastPop:MutableState<TextEditorState?> = mutableStateOf(null)
//
//    private val remainUndoLock:ReentrantLock = ReentrantLock(true)
//    private var remainRedoStackCount:Int = 0
//
//    fun remainOnceRedoStackCount():Int {
//        remainUndoLock.withLock {
//            remainRedoStackCount++
//            return remainRedoStackCount
//        }
//    }
//
//    /**
//     * @return 返回新的记数
//     */
//    private fun consumeOnceRedoStackCount():Int {
//        remainUndoLock.withLock {
//            remainRedoStackCount--
//            //好像小于0没什么必要，所以归0
//            if(remainRedoStackCount < 0) {
//                remainRedoStackCount = 0
//            }
//            return remainRedoStackCount
//        }
//    }

    fun undoStackIsEmpty():Boolean {
        return undoStack.isEmpty()
    }

    fun redoStackIsEmpty():Boolean {
        return redoStack.isEmpty()
    }

    /**
     * @return true saved, false not saved
     */
    fun undoStackPush(state: TextEditorState):Boolean {
        undoLock.withLock {
            val now = getSecFromTime()
            val snapshotLastSaveAt = undoLastSaveAt.longValue
            //在时间间隔内只存一版
            if(undoSaveIntervalInSec == 0 || snapshotLastSaveAt == 0L || (now - snapshotLastSaveAt) > undoSaveIntervalInSec) {
                push(undoStack, state)
                undoLastSaveAt.longValue = now

                return true
            }

            return false
        }
    }

    fun undoStackPop(): TextEditorState? {
        undoLock.withLock {
            return pop(undoStack)
        }
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
                //为使弹窗的状态可立刻被undo stack存上，所以将上次存储时间清0
                undoLastSaveAt.longValue = 0
            }

//            remainOnceRedoStackCount()

//            val last = pop(redoStack)
//            redoLastPop.value = last
//            return last

            return pop(redoStack)
        }
    }

    fun redoStackClear() {
        redoLock.withLock {
            redoStack.clear()
        }
    }

    private fun push(stack: MutableList<TextEditorState>, state: TextEditorState) {
        try {
            // add to tail
            stack.add(state)

            //超过限制移除head first
            if(stack.size > sizeLimit) {
                stack.removeAt(0)
            }
        }catch (e:Exception) {
            MyLog.e(TAG, "#push, err: ${e.stackTraceToString()}")
        }
    }
//
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
}
