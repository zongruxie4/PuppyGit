package com.catpuppyapp.puppygit.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.catpuppyapp.puppygit.constants.IDS
import com.catpuppyapp.puppygit.notification.HttpServiceHoldNotify.createPendingIntentWithAct
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.service.http.server.HttpService

object HttpServiceHoldNotify:NotifyBase(
    TAG = "HttpServiceHoldNotify",
    notifyId = IDS.HttpServiceHoldNotify,  //在你app里你这个通知id，必须唯一
    channelId="service_notify",
    channelName = "Service",
    channelDesc = "Show Service Notifications",
    actionList = { context ->
        listOf(
            // "stop" action, click stop service
            Action(
                iconId = R.drawable.baseline_close_24,
                text = context.getString(R.string.stop),
                pendingIntent = createPendingIntentWithAct(context, HttpService.command_stop)
            ),

            // copy addr
            Action(
                iconId = R.drawable.baseline_content_copy_24,
                text = context.getString(R.string.copy),
                pendingIntent = createPendingIntentWithAct(context, HttpService.command_copy_addr)
            )
        )
    }
){

    fun createPendingIntentWithAct(context: Context, action:String): PendingIntent {
        val intent = Intent(context, HttpService::class.java) // 替换为您的主活动

        intent.action = action

        //flag作用是如果pendingIntent已经存在，则取消之前的然后创建个新的，没验证，可能是
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

}
