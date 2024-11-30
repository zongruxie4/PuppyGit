package com.catpuppyapp.puppygit.git

import com.github.git24j.core.Oid

data class StashDto(
    var index: Int = -1,
    var msg: String = "",
    var stashId: Oid? = null
) {
    fun getItemKey():String {
        return "" +hashCode()
    }
}
