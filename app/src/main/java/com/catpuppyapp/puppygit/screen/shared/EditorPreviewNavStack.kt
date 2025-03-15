package com.catpuppyapp.puppygit.screen.shared

import androidx.compose.foundation.ScrollState
import com.catpuppyapp.puppygit.datastruct.Stack
import com.catpuppyapp.puppygit.screen.functions.newScrollState
import com.catpuppyapp.puppygit.utils.MyLog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "EditorPreviewNavStack"

class EditorPreviewNavStack internal constructor(var root:String) {
    private val lock = Mutex()

    var editingPath = root
    var previewingPath = root
    private val backStack = Stack<String>()
    private val aheadStack = Stack<String>()
    private val pathAndScrollStateMap = ConcurrentHashMap<String, ScrollState>()

    suspend fun reset(newRoot: String) {
        //除lock外都重置，因为共享的都是同一个变量，所以不能重置lock
        root = newRoot
        editingPath = newRoot
        previewingPath = newRoot
        backStack.clear()
        aheadStack.clear()
        pathAndScrollStateMap.clear()
    }

    suspend fun push(path:String):Boolean {
        lock.withLock {
            //创建文件对象
            val f = try {
                File(path)
            }catch (e:Exception) {
                MyLog.e(TAG, "push path: create File err! path=$path, err=${e.stackTraceToString()}")
                return false
            }

            //无法读取文件
            try {
                if(f.canRead().not()) {
                    return false
                }
            }catch (e:Exception) {
                MyLog.e(TAG, "push path: can't read file! path=$path, err=${e.stackTraceToString()}")
                return false
            }

            val path = f.canonicalPath

            val next = aheadStack.getFirst()
            if(next==null || next!=path) {  //和现有路由列表不同，需要清栈
                aheadStack.clear()
                aheadStack.push(path)
            }

            return true
        }
    }

    suspend fun backToHome() {
        lock.withLock {
            val size = backStack.size()
            if(size > 1) {
                for(i in 0..size-2) {
                    backStack.pop()?.let { aheadStack.push(it) }
                }
            }
        }
    }

    /**
     * ahead()前需要先push目标path
     */
    suspend fun ahead():Pair<String, ScrollState>? {
        lock.withLock {
            val nextPath = aheadStack.pop() ?: return null;

            backStack.push(nextPath)

            return Pair(nextPath, getScrollState(nextPath))
        }
    }

    suspend fun back():Pair<String, ScrollState>? {
        lock.withLock {
            val lastPath = backStack.pop() ?: return null;

            aheadStack.push(lastPath)

            return Pair(lastPath, getScrollState(lastPath))
        }
    }

    suspend fun getFirst():Pair<String, ScrollState> {
        lock.withLock {
            val first = backStack.getFirst() ?: root
            return Pair(first, getScrollState(first))
        }
    }


    fun getScrollState(path:String):ScrollState {
        val scrollState = pathAndScrollStateMap[path]

        return if(scrollState != null) {
            scrollState
        }else {
            val scrollState = newScrollState()
            pathAndScrollStateMap[path] = scrollState
            scrollState
        }
    }

    fun backIsNotEmpty():Boolean {
        return backStack.isNotEmpty()
    }

    fun ofThisPath(path: String): Boolean {
        return path == root
    }

}
