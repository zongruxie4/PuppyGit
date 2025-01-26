package com.catpuppyapp.puppygit.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.catpuppyapp.puppygit.play.pro.MainActivity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.MyLog


object ServiceNotify {
    private const val TAG = "ServiceNotify"
    const val notifyId = 1  //在你app里你这个通知id，必须唯一
    private val inited = mutableStateOf(false)

    private lateinit var appContext:Context

    private const val CHANNEL_ID = "Service"
    private const val CHANNEL_NAME = "Service"
    private const val CHANNEL_DESCRIPTION = "Show Service Notifications"

    /**
     * @param context 建议传 applicationContext
     */
    fun init(context: Context) {
        if(inited.value) {
            return
        }

        inited.value = true

        appContext = context

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // 发送通知
    fun sendNotification(context: Context?, title: String?, message: String?, pendingIntent: PendingIntent?) {
        val context = context ?: appContext

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.branch) // 替换为您的图标
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent) // 设置点击通知时的意图
            .setAutoCancel(true) // 点击后自动消失

        NotificationManagerCompat.from(context).apply {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                MyLog.e(TAG, "#sendNotification: send notification failed, permission 'POST_NOTIFICATIONS' not granted")
                return
            }

            //notify
            notify(notifyId, builder.build())
        }

        // untested code, another way to send notification
//        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.notify(notifyId, builder.build())
    }

    fun createPendingIntent(context: Context?, extras:Map<String, String>): PendingIntent {
        val context = context ?: appContext

        val intent = Intent(context, MainActivity::class.java) // 替换为您的主活动
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        extras.forEach { (k, v) ->
            intent.putExtra(k, v)
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

}
