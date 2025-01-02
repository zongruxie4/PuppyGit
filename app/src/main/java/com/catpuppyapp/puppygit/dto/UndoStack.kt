package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.fileeditor.texteditor.state.TextEditorState
import com.catpuppyapp.puppygit.utils.MyLog
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
    val sizeLimit: Int = 50,

    private val undoStack:MutableList<TextEditorState> = LinkedList(),
    private val redoStack:MutableList<TextEditorState> = LinkedList(),
    private val undoLock: ReentrantLock = ReentrantLock(true),
    private val redoLock: ReentrantLock = ReentrantLock(true),
) {
    fun undoStackIsEmpty():Boolean {
        return undoStack.isEmpty()
    }

    fun redoStackIsEmpty():Boolean {
        return redoStack.isEmpty()
    }
    fun undoStackPush(state: TextEditorState) {
        undoLock.withLock {
            push(undoStack, state)
        }
    }

    fun undoStackPop(): TextEditorState? {
        undoLock.withLock {
            return pop(undoStack)
        }
    }

    fun redoStackPush(state: TextEditorState) {
        redoLock.withLock {
            push(redoStack, state)
        }
    }

    fun redoStackPop(): TextEditorState? {
        redoLock.withLock {
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
            //第2个判断是个粗略判断，避免在只有一个条目且内容相等时重复添加内容到栈
//            if(stack.isEmpty() || !(state.fields.size==1 && stack.last().fields.size == 1 && state.fields.first()==stack.last().fields.first()) || stack.last().fieldsId != state.fieldsId) {
            if(stack.isEmpty() || state.fields != peek(stack)?.fields) {
                stack.add(state)

                if(stack.size > sizeLimit) {
                    stack.removeAt(0)
                }
            }
        }catch (e:Exception) {
            MyLog.e(TAG, "#push, err: ${e.stackTraceToString()}")
        }
    }

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
