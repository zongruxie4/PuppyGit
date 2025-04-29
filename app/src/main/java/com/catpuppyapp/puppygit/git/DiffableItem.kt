package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.dto.ItemKey

interface DiffableItem:ItemKey {
    fun getRelativePath():String

    // Cons.gitItemType... 那几个值，dir file subm
    fun getItemType():Int

    fun getChangeType():String

    fun isChangeListItem():Boolean

    fun isFileHistoryItem():Boolean

    // FileHistory专有条目
    fun getEntryId():String
    // FileHistory专有条目
    fun getCommitId():String

    fun getSizeInBytes():Long

    fun getFileName():String

    //FileHistory专有字段
    fun getShortCommitId():String
}
