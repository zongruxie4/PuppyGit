package com.catpuppyapp.puppygit.git

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.dto.ItemKey
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.formatMinutesToUtc
import com.catpuppyapp.puppygit.utils.readTimeZoneOffsetInMinutesFromSettingsOrDefault
import java.io.File

class FileHistoryDto (
//    var fileName:String="",
    var filePathUnderRepo:String="",
//    var fileFullPath:String="",
    var treeEntryOidStr:String="",
    var commitOidStr: String="",
    var dateTime: String="",
    var originTimeOffsetInMinutes:Int = 0,  // commit的时间偏移量，单位分钟 (btw 实际上是 commit使用的是commit对象中的committer signature中的时间和偏移量)
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


    fun getActuallyUsingTimeZoneUtcFormat(settings: AppSettings): String {
        val minuteOffset = readTimeZoneOffsetInMinutesFromSettingsOrDefault(settings, originTimeOffsetInMinutes)

        return formatMinutesToUtc(minuteOffset)
    }


    override fun getItemKey():String {
        return commitOidStr
    }



    fun toDiffableItem():DiffableItem {
        return DiffableItem(
            repoIdFromDb = repoId,
            relativePath = filePathUnderRepo,
            itemType = Cons.gitItemTypeFile,
            changeType = Cons.gitStatusModified,
            isChangeListItem = false,
            isFileHistoryItem = true,

            // FileHistory专有条目
            entryId = treeEntryOidStr,
            // FileHistory专有条目
            commitId = commitOidStr,

            sizeInBytes = 0L,
            shortCommitId = getCachedCommitShortOidStr(),
        )
    }

}
