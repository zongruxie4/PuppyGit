package com.catpuppyapp.puppygit.notification

import com.catpuppyapp.puppygit.constants.IntentCons

object NotifyUtil {

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
