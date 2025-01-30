package com.catpuppyapp.puppygit.notification.util

import android.content.Context
import com.catpuppyapp.puppygit.constants.IntentCons
import com.catpuppyapp.puppygit.notification.AutomationNotify
import com.catpuppyapp.puppygit.notification.HttpServiceHoldNotify
import com.catpuppyapp.puppygit.notification.NormalNotify
import com.catpuppyapp.puppygit.notification.base.NotifyBase

object NotifyUtil {
    private val notifyList:List<NotifyBase> = listOf(
        NormalNotify,
        HttpServiceHoldNotify,
        AutomationNotify
    )

    /**
     * @param appContext better use application context, but activityContext or serviceContext should be fine although
     */
    fun initAllNotify(appContext: Context) {
        notifyList.forEach {
            it.init(appContext)
        }
    }

    fun sendNotificationClickGoToSpecifiedPage(notify: NotifyBase, title:String, msg:String, startPage:Int, startRepoId:String) {
        notify.sendNotification(
            null,
            title,
            msg,
            notify.createPendingIntent(
                null,
                mapOf(
                    IntentCons.ExtrasKey.startPage to startPage.toString(),
                    IntentCons.ExtrasKey.startRepoId to startRepoId
                )
            )
        )
    }
}
