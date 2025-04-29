package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.dto.ItemKey

/**
 * 函数名前加前缀是为了避免 getFiled() 这类名字和jvm默认的 getter 冲突导致报错
 */
interface DiffableItem:ItemKey {
    fun base_getRelativePath():String

    // Cons.gitItemType... 那几个值，dir file subm
    fun base_getItemType():Int

    fun base_getChangeType():String

    fun base_isChangeListItem():Boolean

    fun base_isFileHistoryItem():Boolean

    // FileHistory专有条目
    fun base_getEntryId():String
    // FileHistory专有条目
    fun base_getCommitId():String

    fun base_getSizeInBytes():Long

    fun base_getFileName():String

    //FileHistory专有字段
    fun base_getShortCommitId():String
}
