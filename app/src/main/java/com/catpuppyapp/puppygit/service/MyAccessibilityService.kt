package com.catpuppyapp.puppygit.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.accessibility.AccessibilityEvent
import android.view.inputmethod.InputMethodManager
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.notification.AutomationNotify
import com.catpuppyapp.puppygit.notification.base.ServiceNotify
import com.catpuppyapp.puppygit.notification.util.NotifyUtil
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.settings.util.AutomationUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.RepoActUtil
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


class MyAccessibilityService: AccessibilityService() {
    companion object {
        private const val TAG = "MyAccessibilityService"

        private val lock = Mutex()
        private val targetPackageTrueOpenedFalseCloseNullNeverOpenedList = mutableMapOf<String, Boolean>()
        private var lastTargetPackageName = ""  // use to check enter/leave app


        private fun createNotify(notifyId:Int):ServiceNotify {
            return ServiceNotify(AutomationNotify.create(notifyId))
        }


        private fun sendSuccessNotificationIfEnable(serviceNotify: ServiceNotify, settings: AppSettings) = { title:String?, msg:String?, startPage:Int?, startRepoId:String? ->
            if(settings.automation.showNotifyWhenSuccess) {
                serviceNotify.sendSuccessNotification(title, msg, startPage, startRepoId)
            }
        }

        private fun sendErrNotificationIfEnable(serviceNotify: ServiceNotify, settings: AppSettings)={ title:String, msg:String, startPage:Int, startRepoId:String ->
            if(settings.automation.showNotifyWhenErr) {
                serviceNotify.sendErrNotification(title, msg, startPage, startRepoId)
            }
        }

        private fun sendProgressNotificationIfEnable(serviceNotify: ServiceNotify, settings: AppSettings) = { repoNameOrId:String, progress:String ->
            if(settings.automation.showNotifyWhenProgress) {
                serviceNotify.sendProgressNotification(repoNameOrId, progress)
            }
        }


        private suspend fun pullRepoList(
            serviceNotify: ServiceNotify,
            settings: AppSettings,
            repoList:List<RepoEntity>,
        ) {
            RepoActUtil.pullRepoList(
                repoList,
                routeName = "automation pull service",
                gitUsernameFromUrl="",
                gitEmailFromUrl="",
                sendSuccessNotificationIfEnable(serviceNotify, settings),
                sendErrNotificationIfEnable(serviceNotify, settings),
                sendProgressNotificationIfEnable(serviceNotify, settings),
            )
        }


        private suspend fun pushRepoList(
            serviceNotify: ServiceNotify,
            settings: AppSettings,
            repoList:List<RepoEntity>,
        ) {
            RepoActUtil.pushRepoList(
                repoList,
                routeName = "automation push service",
                gitUsernameFromUrl="",
                gitEmailFromUrl="",
                autoCommit=true,
                force=false,
                sendSuccessNotificationIfEnable(serviceNotify, settings),
                sendErrNotificationIfEnable(serviceNotify, settings),
                sendProgressNotificationIfEnable(serviceNotify, settings),
            )
        }


    }

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

            MyLog.v(TAG, "TYPE_WINDOW_STATE_CHANGED: $packageName")

            try {
                //ignore input method package names
                if((getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.enabledInputMethodList?.find { it.packageName == packageName } != null) {
                    return
                }
            }catch (e:Exception) {
                MyLog.d(TAG, "get enabledInputMethodList err: ${e.stackTraceToString()}")
            }

            // notification expand or gesture maybe
            if("com.android.systemui" == packageName) {
                return
            }

            val settings = SettingsUtil.getSettingsSnapshot()
            val targetPackageList = AutomationUtil.getPackageNames(settings.automation)

            //如果目标app列表为空，就不需要后续判断了，直接返回
            if(targetPackageList.isEmpty()) {
                return
            }

            doJobThenOffLoading {
                val event = Unit  //覆盖外部event变量名，避免错误捕获

                lock.withLock {
                    if(targetPackageList.contains(packageName)) {  // 是我们关注的app
                        lastTargetPackageName = packageName

                        val lastTargetPackageName = Unit  // to avoid mistake using

                        MyLog.d(TAG, "target packageName '$packageName' opened, checking need pull or no....")

                        val targetOpened = targetPackageTrueOpenedFalseCloseNullNeverOpenedList[packageName] == true
                        if(!targetOpened) { // was leave, now opened; or first time opened
                            targetPackageTrueOpenedFalseCloseNullNeverOpenedList[packageName] = true
                            MyLog.d(TAG, "target packageName '$packageName' opened, need do pull")

                            val bindRepoIds = AutomationUtil.getRepoIds(settings.automation, packageName)
                            if(bindRepoIds.isEmpty()) {
                                return@withLock
                            }
                            val db = AppModel.dbContainer
                            val repoList = db.repoRepository.getAll().filter { bindRepoIds.contains(it.id) }
                            if(repoList.isEmpty()) {
                                return@withLock
                            }

                            //do pull
                            doJobThenOffLoading {
                                pullRepoList(createNotify(NotifyUtil.genId()), settings, repoList)
                            }
                        }
                    }else if(lastTargetPackageName.isNotBlank()) { //当前app不是我们关注的app，但上个是
                        val packageName = Unit  //避免在这个代码块误调用这个变量名

                        val lastOpenedTarget = lastTargetPackageName
                        lastTargetPackageName = ""

                        val lastTargetPackageName = Unit  // for avoid mistake using this variable

                        //这里需要判断，不然用户更新列表后，这里若不判断会对之前在列表但后来移除的条目多执行一次pull
                        if(targetPackageList.contains(lastOpenedTarget)) {
                            MyLog.d(TAG, "target packageName '$lastOpenedTarget' leaved, checking need push or no...")
                            //这个应该百分百为真啊？
                            val targetOpened = targetPackageTrueOpenedFalseCloseNullNeverOpenedList[lastOpenedTarget] == true
                            if(targetOpened) { // was opened, now leave
                                MyLog.d(TAG, "target packageName '$lastOpenedTarget' leaved, need do push")
                                targetPackageTrueOpenedFalseCloseNullNeverOpenedList[lastOpenedTarget] = false

                                val bindRepoIds = AutomationUtil.getRepoIds(settings.automation, lastOpenedTarget)
                                if(bindRepoIds.isEmpty()) {
                                    return@withLock
                                }
                                val db = AppModel.dbContainer
                                val repoList = db.repoRepository.getAll().filter { bindRepoIds.contains(it.id) }
                                if(repoList.isEmpty()) {
                                    return@withLock
                                }

                                // do push, one package may bind multi repos, start a coroutine do push for them
                                doJobThenOffLoading {
                                    pushRepoList(createNotify(NotifyUtil.genId()), settings, repoList)
                                }
                            }
                        }else {  //这里得设置下，如果一个条目之前在列表后来移除了，这里不更新下的话，下次打开目标app会忽略一次应该执行的pull
                            MyLog.d(TAG, "target packageName '$lastOpenedTarget' was in targetList but removed, will not do push for it")
                            targetPackageTrueOpenedFalseCloseNullNeverOpenedList[lastOpenedTarget] = false
                        }
                    }
                }
            }
        }
    }

    override fun onInterrupt() {

    }

}