package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.dto.ItemKey
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.github.git24j.core.Oid

data class StashDto(
    var index: Int = -1,
    var msg: String = "",
    var stashId: Oid? = null
) : ItemKey {

    private var shortStashId:String?=null

    fun getCachedShortStashId():String {
        if(shortStashId == null) {
            shortStashId = Libgit2Helper.getShortOidStrByFull(stashId.toString())
        }

        return shortStashId ?: stashId.toString()
    }

    override fun getItemKey():String {
        return "$index: $stashId"
    }
}
