package com.catpuppyapp.puppygit.notification

import com.catpuppyapp.puppygit.notification.base.NotifyBase

class AutomationServiceHoldNotify private constructor(
    override val notifyId:Int
): NotifyBase(
    TAG = "AutomationServiceHoldNotify",
    channelId="automation_service_hold_notify",
    channelName = "Automation Foreground Service",
    channelDesc = "Automation foreground service for keep app long-live",
    actionList = { context ->
        listOf()
    }
){

    companion object {
        fun create(notifyId:Int):NotifyBase {
            return AutomationServiceHoldNotify(notifyId)
        }
    }

}
