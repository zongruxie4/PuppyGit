package com.catpuppyapp.puppygit.screen.shared

import androidx.compose.foundation.ScrollState
import com.catpuppyapp.puppygit.datastruct.Stack
import com.catpuppyapp.puppygit.screen.functions.newScrollState
import com.catpuppyapp.puppygit.utils.MyLog
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

private const val TAG = "EditorPreviewNavStack"

class EditorPreviewNavStackItem (val path: String = "", val scrollState: ScrollState = newScrollState())

/**
 * push 和 ahead/back 应该先后调用，不然会有页面未显示，比如在页面z push了a、b、c，然后ahead，那会从z直接到c，但返回时却会返回到b，再返回a，再返回z，会比较反逻辑
 */
class EditorPreviewNavStack internal constructor(var root:String) {
    //虽然这个类很多操作都加了lock，但是，没有会长时间阻塞的操作，所以若没必要不需要开协程，直接用runBlocking就行
    private val lock = Mutex()

    var editingPath = root
    var previewingPath = root
    var rootNavStackItem = EditorPreviewNavStackItem(root, newScrollState())

    private val backStack = Stack<EditorPreviewNavStackItem>()
    private val aheadStack = Stack<EditorPreviewNavStackItem>()

    suspend fun reset(newRoot: String) {
        //除lock外都重置，因为共享的都是同一个变量，所以不能重置lock
        lock.withLock {
            root = newRoot
            editingPath = newRoot
            previewingPath = newRoot
            rootNavStackItem = EditorPreviewNavStackItem(newRoot, newScrollState())
            backStack.clear()
            aheadStack.clear()
        }
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

            val curFirst = aheadStack.getFirst()?.path
            if(curFirst != path) {  //和现有路由列表不同，需要清栈
                aheadStack.clear()
            }

            aheadStack.push(EditorPreviewNavStackItem(path))

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
    suspend fun ahead():EditorPreviewNavStackItem? {
        lock.withLock {
            val target = aheadStack.pop() ?: return null;

            backStack.push(target)

            return target
        }
    }

    suspend fun back():EditorPreviewNavStackItem? {
        lock.withLock {
            val target = backStack.pop() ?: return null;

            aheadStack.push(target)

            return target
        }
    }

    suspend fun getCurrent():EditorPreviewNavStackItem {
        lock.withLock {
            return getCurrentNoLock()
        }
    }

    private suspend fun getCurrentNoLock():EditorPreviewNavStackItem {
        return backStack.getFirst() ?: rootNavStackItem
    }


    suspend fun getCurrentScrollState():ScrollState {
        lock.withLock {
            return getCurrentNoLock().scrollState
        }
    }

    suspend fun backStackIsNotEmpty():Boolean {
        lock.withLock {
            return backStack.isNotEmpty()
        }
    }

    suspend fun backStackOrAheadStackIsNotEmpty():Boolean {
        lock.withLock {
            return backStack.isNotEmpty() || aheadStack.isNotEmpty()
        }
    }

}
