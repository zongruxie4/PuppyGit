package com.catpuppyapp.puppygit.service.http.server

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.os.Build
import android.os.IBinder
import com.catpuppyapp.puppygit.constants.IDS
import com.catpuppyapp.puppygit.notification.HttpServiceHoldNotify
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import kotlinx.coroutines.runBlocking


private const val TAG = "HttpService"
private const val serviceId = IDS.HttpService  //这id必须唯一，最好和notifyid也不一样

class HttpService : Service() {
    companion object {
        const val command_stop = "STOP"

        fun start(appContext: Context) {
            if(HttpServer.isServerRunning()) {
                MyLog.w(TAG, "HttpServer already running, start canceled")
                return
            }

            val serviceIntent = Intent(appContext, HttpService::class.java)
            appContext.startForegroundService(serviceIntent)
            MyLog.d(TAG, "HttpService started")
        }

        //没运行service调用此方法应该不会报错，chatgpt说的，我没测试
        fun stop(appContext: Context) {
            val serviceIntent = Intent(appContext, HttpService::class.java)
            appContext.stopService(serviceIntent)
            MyLog.d(TAG, "HttpService stopped")
        }
    }

    override fun onCreate() {
        super.onCreate()

        // 初始化代码
        AppModel.init_1(activityContext = this, realAppContext = applicationContext, exitApp = {}, initActivity = false)

        runBlocking {
            AppModel.init_2()
        }

        MyLog.w(TAG, "http service onCreate() finished")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null && command_stop == intent.action){
            // stop
            stop(AppModel.realAppContext)

            MyLog.w(TAG, "http service onStartCommand() stop finished")
        }else {
            // start
            val settings = SettingsUtil.getSettingsSnapshot()

            // 启动前台服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // UPSIDE_DOWN_CAKE is sdk 34
                startForeground(serviceId, getNotification(settings), FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            }else {
                startForeground(serviceId, getNotification(settings))
            }


            doJobThenOffLoading {
                HttpServer.doActWithLock {
                    startServer(settings)
                }
            }

            MyLog.w(TAG, "http service onStartCommand() start finished")

        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // 不需要绑定
    }

    override fun onDestroy() {
        super.onDestroy()
        HttpServer.stopServer()
        stopForeground(STOP_FOREGROUND_REMOVE)
        MyLog.w(TAG, "http service onDestroy() finished")
    }

    private fun getNotification(settings: AppSettings): Notification {
        val builder = HttpServiceHoldNotify.getNotificationBuilder(
            this,
            "PuppyGit Service",
            "Listen on: http://${settings.httpService.listenHost}:${settings.httpService.listenPort}",
            HttpServiceHoldNotify.createPendingIntent(null, mapOf()), //启动app但不指定页面
        )

        return builder.build()
    }
}
