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
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.PrefMan
import com.catpuppyapp.puppygit.utils.copyTextToClipboard
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.genHttpHostPortStr
import kotlinx.coroutines.runBlocking


private const val TAG = "HttpService"
private const val serviceId = IDS.HttpService  //这id必须唯一，最好和notifyid也不一样

class HttpService : Service() {
    companion object {
        const val command_stop = "STOP"
        const val command_copy_addr = "COPY_ADDR"

        fun launchOnSystemStartUpEnabled(context: Context):Boolean {
            return PrefMan.get(context, PrefMan.Key.launchServiceOnSystemStartup, "0") == "1"
        }

        fun setLaunchOnSystemStartUp(context: Context, enable:Boolean) {
            PrefMan.set(context, PrefMan.Key.launchServiceOnSystemStartup, if(enable) "1" else "0")
        }

        /**
         * 调这个方法不需要HttpServer.doActWithLock()，其内部会自己调用
         */
        fun start(appContext: Context) {
            if(HttpServer.isServerRunning()) {
                MyLog.w(TAG, "HttpServer already running, start canceled")
            }else {
                val serviceIntent = Intent(appContext, HttpService::class.java)
                appContext.startForegroundService(serviceIntent)
                MyLog.d(TAG, "HttpService started")
            }

            TileHttpService.sendUpdateTileRequest(appContext, true)
        }

        //没运行service调用此方法应该不会报错，chatgpt说的，我没测试
        fun stop(appContext: Context) {
            val serviceIntent = Intent(appContext, HttpService::class.java)
            appContext.stopService(serviceIntent)
            MyLog.d(TAG, "HttpService stopped")

            TileHttpService.sendUpdateTileRequest(appContext, false)
        }


        //service启动Activity似乎需要弹窗权限，算了，点击通知启动也很方便
//        fun launchApp() {
//            val context = AppModel.realAppContext
//
//            val intent = Intent(context, MainActivity::class.java) // 替换为您的主活动
//            //创建个新Activity并清掉之前的Activity，不然可能存在多个Activity，有点混乱
//            //这个 or 是 bitwise，相当于 '1 | 0' 的 '|'
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//
////            intent.putExtra(IntentCons.ExtrasKey.startPage, Cons.selectedItem_Repos)
////            intent.putExtra(IntentCons.ExtrasKey.startRepoId, )
//
//           context.startActivity(intent)
//        }


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
        val action = intent?.action
        if(action == command_stop){
            // stop
            stop(AppModel.realAppContext)

            MyLog.w(TAG, "http service onStartCommand() stop finished")
        }else if(action == command_copy_addr){
            val settings = SettingsUtil.getSettingsSnapshot()
            copyTextToClipboard(
                context = applicationContext,
                label = "PuppyGit Http Addr",
                text = genHttpHostPortStr(settings.httpService.listenHost, settings.httpService.listenPort)
            )

            //这toast不一定显示
            Msg.requireShow(applicationContext.getString(R.string.copied))
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

            //严格来说start并没finished，只是调用了，可能得等会HttpServer.isServerRunning()才能检测到true
            MyLog.w(TAG, "http service onStartCommand() start finished")

        }

        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null // 不需要绑定
    }

    override fun onDestroy() {
        super.onDestroy()

        doJobThenOffLoading {
            HttpServer.doActWithLock {
                stopServer()
            }
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        MyLog.w(TAG, "http service onDestroy() finished")
    }

    private fun getNotification(settings: AppSettings): Notification {
        val builder = HttpServiceHoldNotify.getNotificationBuilder(
            this,
            "PuppyGit Service",
            "Listen on: ${genHttpHostPortStr(settings.httpService.listenHost, settings.httpService.listenPort)}",
            HttpServiceHoldNotify.createPendingIntent(null, mapOf()), //启动app但不指定页面
        )

        return builder.build()
    }
}
