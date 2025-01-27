package com.catpuppyapp.puppygit.notification

import com.catpuppyapp.puppygit.constants.IDS

object HttpServiceHoldNotify:NotifyBase(
    TAG = "HttpServiceHoldNotify",
    notifyId = IDS.HttpServiceHoldNotify,  //在你app里你这个通知id，必须唯一
    channelId="service_notify",
    channelName = "Service",
    channelDesc = "Show Service Notifications",
)
