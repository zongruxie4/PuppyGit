package com.catpuppyapp.puppygit.git

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dto.ItemKey
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.getFileNameFromCanonicalPath
import com.catpuppyapp.puppygit.utils.getParentPathEndsWithSeparator
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.channels.Channel
import java.io.File

/**
 * 函数名前加前缀是为了避免 getFiled() 这类名字和jvm默认的 getter 冲突导致报错
 */
data class DiffableItem(
    val repoIdFromDb:String="",
    val relativePath:String = "",
    val itemType:Int = Cons.gitItemTypeFile,

    // modified/new/deleted之类的，
    // 不要把默认值改成有效的修改类型，
    // 因为源头StatusTypeEntrySaver里居然默认值是null！当时没考虑好，
    // 该弄成默认空字符串就对了，
    // 总之这个默认值设为null应该比设为一个有效的修改类型兼容性更强些，
    // 因为以前的代码可能有做若null则空字符串的判断
    val changeType:String = "",

    val isChangeListItem:Boolean = false,
    val isFileHistoryItem:Boolean = false,

    // FileHistory专有条目
    val entryId:String = "",
    // FileHistory专有条目
    val commitId:String = "",

    val sizeInBytes:Long = 0L,
    val shortCommitId:String = "",


    //页面状态用的变量
    val loading:Boolean = true,
    val loadChannel:Channel<Int> = Channel(),
    //严格来说是diff result才对，但当时命名的时候没想到，现在已经很多地方用这名字，积重难返了
    val diffItemSaver: DiffItemSaver = DiffItemSaver(),
    val stringPairMap: SnapshotStateMap<String, CompareLinePairResult> = mutableStateMapOf(),
    val compareLinePair:CompareLinePair = CompareLinePair(),
    val submoduleIsDirty:Boolean = false,
    val errMsg: String = "",
):ItemKey {
    override fun getItemKey(): String {
        // else的是 isFileHistoryItem
        return if(isChangeListItem) repoIdFromDb+ relativePath+changeType+itemType else commitId
    }

    fun getFileNameOnly():String {
        return getFileNameFromCanonicalPath(relativePath)
    }

    fun getFileParentPathOnly():String {
        return getParentPathEndsWithSeparator(relativePath)
    }

    fun getFileFullPath(repoWorkDirPath:String):String {
        return File(repoWorkDirPath, relativePath).canonicalPath
    }

    fun copyForLoading():DiffableItem {
        return copy(loading = true, stringPairMap = mutableStateMapOf(), compareLinePair = CompareLinePair(), submoduleIsDirty = false, errMsg = "", loadChannel = Channel())
    }

    fun closeLoadChannel() {
        runCatching { loadChannel.close() }
    }

    protected fun finalize() {
        // finalization logic
        closeLoadChannel()
    }
}
