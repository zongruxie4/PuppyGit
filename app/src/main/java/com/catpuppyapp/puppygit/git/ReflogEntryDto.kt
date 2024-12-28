package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.dto.ItemKey
import com.github.git24j.core.Oid

data class ReflogEntryDto(
    var username:String="",
    var email:String="",
    var date:String="",
    var actuallyUsingTimeZoneOffsetInMinutes:Int=0,
    var originTimeZoneOffsetInMinutes:Int=0,
    var idNew: Oid?=null,
    var idOld:Oid?=null,
    var msg:String=""
) : ItemKey {

    override fun getItemKey():String {
        return username+email+date+idNew+idOld+msg
    }
}
