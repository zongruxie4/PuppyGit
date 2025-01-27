package com.catpuppyapp.puppygit.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.catpuppyapp.puppygit.constants.IDS
import com.catpuppyapp.puppygit.notification.HttpServiceHoldNotify.createStopPendingIntent
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.service.http.server.HttpService

object HttpServiceHoldNotify:NotifyBase(
    TAG = "HttpServiceHoldNotify",
    notifyId = IDS.HttpServiceHoldNotify,  //在你app里你这个通知id，必须唯一
    channelId="service_notify",
    channelName = "Service",
    channelDesc = "Show Service Notifications",
    createAction = {context ->
        // "stop" action, click stop service
        Action(
            iconId = R.drawable.baseline_close_24,
            text = context.getString(R.string.stop),
            pendingIntent = createStopPendingIntent(context)
        )
    }
){

    fun createStopPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, HttpService::class.java) // 替换为您的主活动

        intent.action = HttpService.command_stop

        //flag作用是如果pendingIntent已经存在，则取消之前的然后创建个新的，没验证，可能是
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

}
