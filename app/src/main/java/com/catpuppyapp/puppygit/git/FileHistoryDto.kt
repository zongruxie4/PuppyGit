package com.catpuppyapp.puppygit.git

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
) {
    fun authorAndCommitterAreSame():Boolean {
        return authorUsername==committerUsername && authorEmail==committerEmail
    }
}

class FileHistoryQueryResult(
    hasMore:Boolean,  // not 100% has more, but at least can try more once revwalk.next(), if return null, is really no more
    lastVersionTreeEntryOidStr:String?
)
