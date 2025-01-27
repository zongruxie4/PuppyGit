package com.catpuppyapp.puppygit.notification

import com.catpuppyapp.puppygit.constants.IDS

object NormalNotify:NotifyBase(
    TAG = "NormalNotify",
    notifyId = IDS.NormalNotify,  //在你app里你这个通知id，必须唯一
    channelId="normal_notify",
    channelName = "Normal",
    channelDesc = "Show Notifications",
)
