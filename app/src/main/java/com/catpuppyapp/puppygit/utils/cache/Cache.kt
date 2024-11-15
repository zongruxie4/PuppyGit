package com.catpuppyapp.puppygit.utils.cache

/**
 * this is default cache instance
 */
object Cache:CacheStoreImpl(){
    const val keySeparator = ":"

    object Key {
        val changeListInnerPage_SavePatchPath = "cl_patchsavepath"
        val changeListInnerPage_RequireDoSyncAfterCommit = "cliprdsac"  //这个其实改用状态变量能达到同样的效果
        //            val changeListInnerPage_RequirePull = "cliprpul";
//            val changeListInnerPage_RequirePush = "cliprpus";
//            val changeListInnerPage_RequireSync = "cliprsyn";
        val changeListInnerPage_requireDoActFromParent = "cliprdafp";
        val repoTmpStatusPrefix = "repo_tmp_status"  // prefix+keyseparator+repoId，例如 "repo_tmp_status:a29388d9988"

        val editorPageSaveLockPrefix = "editor_save_lock"
    }

}
