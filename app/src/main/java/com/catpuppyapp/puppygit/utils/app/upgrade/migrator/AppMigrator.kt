package com.catpuppyapp.puppygit.utils.app.upgrade.migrator

import androidx.room.withTransaction
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.fileopenhistory.FileOpenHistory
import com.catpuppyapp.puppygit.utils.fileopenhistory.FileOpenHistoryMan

private const val TAG = "AppMigrator"
//app升级如果更新了字段之类的有时候需要处理一些东西，比如添加了分支的上游分支字段，旧版没这个字段，得更新下
object AppMigrator {
    /**
     * 本次迁移执行的操作：更新 数据库 和 FileOpenHistory 中 所有时间字段 为 UTC时间
     *
     * 注意：这个版本的迁移不管成功失败一律当作成功，确保只执行一次，因为即使失败，用户也会继续使用应用，这就导致数据库中有两个时区的时间，如果再执行迁移，就会错乱
     * 依赖：执行到这时，必须确保AppModel中系统时区相关字段已正确设置，否则迁移后的时间可能会乱
     *
     * @return migrate success or failed
     */
    suspend fun fromVer47():Boolean {
        // 更新FileOpenHistory内所有日期字段为UTC时间
        val systemDefaultTimeOffsetInSec = (AppModel.systemTimeZoneOffsetInMinutes.intValue * 60).toLong()

        try {
            FileOpenHistoryMan.subtractTimeOffset(systemDefaultTimeOffsetInSec)

            MyLog.w(TAG, "#fromVer47 migrate `FileOpenHistory` success")
        }catch (e:Exception) {
            MyLog.e(TAG, "#fromVer47 migrate `FileOpenHistory` failed: ${e.stackTraceToString()}")

        }

        //更新数据库中所有时间字段为UTC时间
        try {
            AppModel.dbContainer.db.withTransaction {
                AppModel.dbContainer.credentialRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
                AppModel.dbContainer.domainCredentialRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
                AppModel.dbContainer.errorRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
                AppModel.dbContainer.remoteRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
                AppModel.dbContainer.repoRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
                AppModel.dbContainer.settingsRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
                AppModel.dbContainer.storageDirRepository.subtractTimeOffset(systemDefaultTimeOffsetInSec)
            }


            MyLog.w(TAG, "#fromVer47 migrate `DataBase` success")

        }catch (e:Exception) {
            MyLog.e(TAG, "#fromVer47 migrate `DataBase` failed: ${e.stackTraceToString()}")
        }


        return true
    }

}
