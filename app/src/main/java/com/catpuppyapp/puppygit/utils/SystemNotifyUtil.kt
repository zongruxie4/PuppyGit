package com.catpuppyapp.puppygit.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.catpuppyapp.puppygit.play.pro.MainActivity
import com.catpuppyapp.puppygit.play.pro.R


object SystemNotifyUtil {
    init {
        createNotificationChannel()
    }

    private const val CHANNEL_ID = "Service"
    private const val CHANNEL_NAME = "Service"
    private const val CHANNEL_DESCRIPTION = "Show Service Notifications"

    // 创建通知渠道
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = AppModel.realAppContext
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 发送通知
    fun sendNotification(title: String?, message: String?, pendingIntent: PendingIntent?) {
        val context = AppModel.realAppContext

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.branch) // 替换为您的图标
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent) // 设置点击通知时的意图
            .setAutoCancel(true) // 点击后自动消失

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build()) // 1 是通知的 ID
    }

    fun createStartAppPendingIntent(extras:Map<String, String>): PendingIntent {
        val context = AppModel.realAppContext

        val intent = Intent(context, MainActivity::class.java) // 替换为您的主活动
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        extras.forEach { (k, v) ->
            intent.putExtra(k, v)
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}
