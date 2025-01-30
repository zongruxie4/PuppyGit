package com.catpuppyapp.puppygit.notification

import com.catpuppyapp.puppygit.constants.IDS
import com.catpuppyapp.puppygit.notification.base.NotifyBase

/**
 * 执行自动化行为的通知，就是app打开、关闭时发送通知的渠道
 */
object AutomationNotify: NotifyBase(
    TAG = "AutomationNotify",
    notifyId = IDS.AutomationNotify,  //在你app里你这个通知id，必须唯一
    channelId="automation_notify",
    channelName = "Automation",
    channelDesc = "Show Automation Notifications",
)
