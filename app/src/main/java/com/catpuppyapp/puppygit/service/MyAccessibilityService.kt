package com.catpuppyapp.puppygit.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "MyAccessibilityService"

// TODO 获取用户设置的packagelist
// test
//private val targetPackageList = mutableListOf<String>("me.zhanghai.android.files", "com.catpuppyapp.puppygit.play.pro")
private val targetPackageList = mutableListOf<String>()  // query from settings
private val targetPackageTrueOpenedFalseCloseNullNeverOpenedList = mutableMapOf<String, Boolean>()
private var lastTargetPackageName = ""
private val lock = Mutex()

class MyAccessibilityService: AccessibilityService() {

    override fun onCreate() {
        super.onCreate()

        // 初始化代码
        AppModel.init_1(activityContext = this, realAppContext = applicationContext, exitApp = {}, initActivity = false)

        runBlocking {
            AppModel.init_2()
        }

        MyLog.w(TAG, "#onCreate() finished")

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if(event == null) {
            return
        }

        if(event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            //必须在外部获取，放到协程里会null
            val packageName = event.packageName.toString()

            doJobThenOffLoading {
                val event = Unit  //覆盖外部event变量名，避免错误捕获

                lock.withLock {
                    //TODO 查询 targetPackageList from user settings (or db better?)


                    //如果目标app列表为空，就不需要后续判断了，直接返回
                    if(targetPackageList.isEmpty()) {
                        return@withLock
                    }

                    if(targetPackageList.contains(packageName)) {  // 是我们关注的app
                        lastTargetPackageName = packageName

                        MyLog.d(TAG, "target packageName '$packageName' opened, checking need pull or no....")

                        val targetOpened = targetPackageTrueOpenedFalseCloseNullNeverOpenedList[packageName] == true
                        if(!targetOpened) { // was leave, now opened; or first time opened
                            targetPackageTrueOpenedFalseCloseNullNeverOpenedList[packageName] = true
                            MyLog.d(TAG, "target packageName '$packageName' opened, need do pull")
                            //TODO do pull here
                        }
                    }else if(lastTargetPackageName.isNotBlank()) { //当前app不是我们关注的app，但上个是
                        val lastOpenedTarget = lastTargetPackageName
                        lastTargetPackageName = ""
                        MyLog.d(TAG, "target packageName '$lastOpenedTarget' leaved, checking need push or no...")
                        //这个应该百分百为真啊？
                        val targetOpened = targetPackageTrueOpenedFalseCloseNullNeverOpenedList[lastOpenedTarget] == true
                        if(targetOpened) { // was opened, now leave
                            MyLog.d(TAG, "target packageName '$lastOpenedTarget' leaved, need do push")
                            targetPackageTrueOpenedFalseCloseNullNeverOpenedList[lastOpenedTarget] = false
                            // TODO push, one package may bind multi repos, start a coroutine do push for them
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {

    }

}