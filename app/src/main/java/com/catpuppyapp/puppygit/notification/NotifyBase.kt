package com.catpuppyapp.puppygit.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.catpuppyapp.puppygit.play.pro.MainActivity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.MyLog


open class NotifyBase(
    private val TAG:String, // used for log
    private val notifyId:Int,  //在你app里你这个通知id，必须唯一
    private val channelId:String,
    private val channelName:String,
    private val channelDesc:String,
    private val createAction:((context:Context) -> Action)? = null
) {
    /**
     * 用来避免重复执行init出错
     */
    private val inited:MutableState<Boolean> = mutableStateOf(false)

    /**
     * applicationContext，不要用activityContext，
     * init时会更新此变量为非null值
     *
     */
    private var appContext:Context? = null

    /**
     * 必须先init，否则执行其他方法可能报错并且无效
     * @param context 建议传 applicationContext
     */
    fun init(context: Context) {
        if(inited.value) {
            return
        }

        inited.value = true

        appContext = context

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        // see: https://developer.android.com/develop/ui/views/notifications/build-notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = channelDesc

            // Register the channel with the system.
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            MyLog.w(TAG, "notify channel '$channelId' registered")
        }

        MyLog.w(TAG, "notification '$notifyId' inited")
    }

    // 发送通知
    fun sendNotification(context: Context?, title: String?, message: String?, pendingIntent: PendingIntent?) {
        val context = context ?: appContext!!

        val builder = getNotificationBuilder(context, title, message, pendingIntent) // 点击后自动消失

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

    fun getNotificationBuilder(context: Context?, title: String?, message: String?, pendingIntent: PendingIntent?): NotificationCompat.Builder {
        val context = context ?: appContext!!

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.branch) // 替换为您的图标
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent) // 设置点击通知时的意图
            .setAutoCancel(true) // 点击后通知将自动消失，除非你是foreground service启动的通知

        if(createAction != null) {
            val action = createAction.invoke(context)
            builder.addAction(action.iconId, action.text, action.pendingIntent)
        }

        return builder
    }

    fun createPendingIntent(context: Context?, extras:Map<String, String>): PendingIntent {
        val context = context ?: appContext!!

        val intent = Intent(context, MainActivity::class.java) // 替换为您的主活动
        //创建个新Activity并清掉之前的Activity，不然可能存在多个Activity，有点混乱
        //这个 or 是 bitwise，相当于 '1 | 0' 的 '|'
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        //添加参数
        extras.forEach { (k, v) ->
            intent.putExtra(k, v)
        }

        //flag作用是如果pendingIntent已经存在，则取消之前的然后创建个新的，没验证，可能是
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

}
