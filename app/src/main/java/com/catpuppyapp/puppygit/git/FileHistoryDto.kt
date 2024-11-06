package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel

class FileHistoryDto (
    var oidStr: String="",
    var dateTime: String="",
    var author: String="",  // username
    var email: String="",
    var committerUsername:String="",
    var committerEmail:String="",
    var shortMsg:String="", //只包含第一行
    var msg: String="",  //完整commit信息
    var repoId:String="",  //数据库的repoId，用来判断当前是在操作哪个仓库
) {
    fun authorAndCommitterAreSame():Boolean {
        return author==committerUsername && email==committerEmail
    }
}
