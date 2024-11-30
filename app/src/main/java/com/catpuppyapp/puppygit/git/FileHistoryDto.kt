package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.dto.ItemKey
import com.catpuppyapp.puppygit.utils.Libgit2Helper

class FileHistoryDto (
//    var fileName:String="",
    var filePathUnderRepo:String="",
//    var fileFullPath:String="",
    var treeEntryOidStr:String="",
    var commitOidStr: String="",
    var dateTime: String="",
    var authorUsername: String="",  // username
    var authorEmail: String="",
    var committerUsername:String="",
    var committerEmail:String="",
    var shortMsg:String="", //只包含第一行
    var msg: String="",  //完整commit信息
    var repoId:String="",  //数据库的repoId，用来判断当前是在操作哪个仓库
): ItemKey {

    private var commitShortOidStr:String?=null
    private var treeEntryShortOidStr:String?=null

    fun authorAndCommitterAreSame():Boolean {
        return authorUsername==committerUsername && authorEmail==committerEmail
    }

    fun getCachedCommitShortOidStr():String {
        if(commitShortOidStr==null) {
            commitShortOidStr = Libgit2Helper.getShortOidStrByFull(commitOidStr)
        }
        return commitShortOidStr ?:""
    }
    fun getCachedTreeEntryShortOidStr():String {
        if(treeEntryShortOidStr==null) {
            treeEntryShortOidStr = Libgit2Helper.getShortOidStrByFull(treeEntryOidStr)
        }
        return treeEntryShortOidStr ?:""
    }

    override fun getItemKey():String {
        return commitOidStr
    }
}
