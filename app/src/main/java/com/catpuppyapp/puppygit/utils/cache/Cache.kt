package com.catpuppyapp.puppygit.utils.cache

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

        const val commitList_fullOidKey = "commitList_fullOidKey"
        const val commitList_shortBranchNameKey = "commitList_shortBranchNameKey"

        const val diffScreen_underRepoPathKey = "diffScreen_underRepoPathKey"
        const val diffScreen_diffableItemListKey = "diffScreen_diffableItemListKey"

        const val fileHistory_fileRelativePathKey = "fileHistory_fileRelativePathKey"
        const val subPageEditor_filePathKey = "subPageEditor_filePathKey"

    }

}
