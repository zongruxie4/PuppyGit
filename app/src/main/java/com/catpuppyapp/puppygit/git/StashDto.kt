package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.dto.ItemKey
import com.github.git24j.core.Oid

data class StashDto(
    var index: Int = -1,
    var msg: String = "",
    var stashId: Oid? = null
) : ItemKey {
    override fun getItemKey():String {
        return msg+stashId?.id.toString()+index
    }
}
