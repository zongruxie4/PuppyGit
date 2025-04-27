package com.catpuppyapp.puppygit.utils.cache

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import com.catpuppyapp.puppygit.utils.getShortUUID

/**
 * this is default cache instance
 */
object Cache:CacheStoreImpl(){
    const val keySeparator = ":"

    object Key {
//        val changeListInnerPage_SavePatchPath = "cl_patchsavepath"
//        val changeListInnerPage_RequireDoSyncAfterCommit = "cliprdsac"  //这个其实改用状态变量能达到同样的效果
        //            val changeListInnerPage_RequirePull = "cliprpul";
//            val changeListInnerPage_RequirePush = "cliprpus";
//            val changeListInnerPage_RequireSync = "cliprsyn";
        const val changeListInnerPage_requireDoActFromParent = "cliprdafp";
        const val repoTmpStatusPrefix = "repo_tmp_status"  // prefix+keyseparator+repoId，例如 "repo_tmp_status:a29388d9988"

        const val editorPageSaveLockPrefix = "editor_save_lock"
        const val subPagesStateKeyPrefix = "sub_pages_key_prefix$keySeparator"

//        const val diffScreen_underRepoPathKey = "diffScreen_underRepoPathKey"
//        const val diffScreen_diffableItemListKey = "diffScreen_diffableItemListKey"

//        const val fileHistory_fileRelativePathKey = "fileHistory_fileRelativePathKey"
//        const val subPageEditor_filePathKey = "subPageEditor_filePathKey"

    }

    fun clearAllSubPagesStates() {
        clearByKeyPrefix(Key.subPagesStateKeyPrefix)
    }

    @Composable
    fun getSubPageKey(stateKeyTag:String):String {
        // e.g. "sub_pages_key_prefix:DiffScreen:ak1idkjgkk"
        return rememberSaveable { Key.subPagesStateKeyPrefix+stateKeyTag+ keySeparator+ getShortUUID() }
    }

    fun combineKeys(vararg keys: String):String {
        val ret = StringBuilder()

        for (key in keys) {
            ret.append(key).append(keySeparator)
        }

        return ret.removeSuffix(keySeparator).toString()
    }
}
