package com.catpuppyapp.puppygit.play.pro

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.platform.createLifecycleAwareWindowRecomposer
import androidx.core.view.WindowCompat
import com.catpuppyapp.puppygit.compose.LoadingText
import com.catpuppyapp.puppygit.compose.SshUnknownHostDialog
import com.catpuppyapp.puppygit.jni.SshAskUserUnknownHostRequest
import com.catpuppyapp.puppygit.screen.AppScreenNavigator
import com.catpuppyapp.puppygit.screen.RequireMasterPasswordScreen
import com.catpuppyapp.puppygit.screen.functions.KnownHostRequestStateMan
import com.catpuppyapp.puppygit.screen.shared.IntentHandler
import com.catpuppyapp.puppygit.ui.theme.PuppyGitAndroidTheme
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.user.UserUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ContextUtil
import com.catpuppyapp.puppygit.utils.Lg2HomeUtils
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.PrefMan
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.showToast
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay


private const val TAG = "MainActivity"

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

////
//fun Context.setAppLocale(language: String): Context {
//    val locale = Locale(language)
//    Locale.setDefault(locale)
//    val config = resources.configuration
//    config.setLocale(locale)
//    config.setLayoutDirection(locale)
//    return createConfigurationContext(config)
//}

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val funName = "onCreate"

        //20240519 上午: start: 尝试解决谷歌自动测试时的bug，什么gms err之类的
        //20240519 下午：更新：注释了这段代码，再上传，没报错。
        //20240519: 好像和这个无关，？参见未解决的问题文档，搜“play console测试莫名其妙报错 gms相关 原因不明 20240519”
//        val threadPolicy = StrictMode.ThreadPolicy.Builder()
//            .permitDiskReads()
//            .permitCustomSlowCalls()
////            .permitDiskWrites() // If you also want to ignore DiskWrites, Set this line too.
//            .build();
//        StrictMode.setThreadPolicy(threadPolicy);
        //20240519: end: 尝试解决谷歌自动测试时的bug，什么gms err之类的

        super.onCreate(savedInstanceState)

        MyLog.d(TAG, "onCreate called")

        //如果是初次创建Activity，onNewIntent不会被调用，只能在这里设置一下，要不然有可能漏外部传来的intent（分享文件、编辑文件）
        IntentHandler.setNewIntent(intent)

        //打印广告id，需要google play service
//        doJobThenOffLoading {
//            val adClient = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
//            // AdLimit为true代表用户退出个性化广告，即限制广告跟踪
//            MyLog.d(TAG, "AdId:${adClient.id}, AdLimit:${adClient.isLimitAdTrackingEnabled}")
//        }

//        println("applicationContext.filesDir:"+applicationContext.filesDir)
//        println("applicationContext.dataDir:"+applicationContext.dataDir)

//        println("Environment.getExternalStorageDirectory()="+Environment.getExternalStorageDirectory())  // /storage/emulated/0
//        println("applicationContext.filesDir="+applicationContext.filesDir)  // /data/user/0/com.catpuppyapp.puppygit/files
//        println("applicationContext.getExternalFilesDir(null)="+applicationContext.getExternalFilesDir(null))  // /storage/emulated/0/Android/data/com.catpuppyapp.puppygit/files
//        println("getShortUuid():::"+getShortUuid())

        // applicationContext, life time with app process, save reference has low risk of memory leak,
        // but some cases can't get properly resources (such as you switched language, but use applicationContext.getString() still get english),
//      //  AppModel.init_1(applicationContext = applicationContext, exitApp = {finish()})

        // baseContext, life time with activity, can get properly resources, but save reference to static field will increase risk of memory leak
//        AppModel.init_1(activityContext = baseContext, realAppContext = applicationContext, exitApp = {finish()})
        AppModel.init_1(realAppContext = applicationContext, exitApp = {finish()}, initActivity = true)

        //for make imePadding() work
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // for catch exception, block start。( refer: https://stackoverflow.com/questions/76061623/how-to-restart-looper-when-exception-throwed-in-jetpack-compose
        val recomposer = window.decorView.createLifecycleAwareWindowRecomposer(
            CoroutineExceptionHandler { coroutineContext, throwable ->
                try {
    //                throwable.printStackTrace();
                    MyLog.e(TAG, "#$funName err: "+throwable.stackTraceToString())

                    //出错提示下用户就行，经我测试，画面会冻结，但数据不会丢，问题不大
                    showToast(applicationContext, getString(R.string.err_restart_app_may_resolve), Toast.LENGTH_LONG)  //测试了下，能显示Toast

                    //不重新创建Activity的话，页面会freeze，按什么都没响应，不过系统导航键还是可用的
                    //重新创建不一定成功，有可能会返回桌面
//                    ActivityUtil.restartActivityByIntent(this)

                    // 不重建Activity，直接退出
                    finish()

                    // 如果想显示错误弹窗，参见文档 “下一步-20240120.txt” 中的："compose错误处理 compose出错弹窗实现 20240505"

                }catch (e:Exception) {
                    e.printStackTrace()  //再出异常，管不了了，随便吧，打印下就行
                }
            }, lifecycle)

        // set window use our recomposer
        window.decorView.compositionContext = recomposer
        // for catch exception, block end

//        // val settings = SettingsUtil.getSettingsSnapshot()


        setContent {
            val theme = rememberSaveable { mutableStateOf(""+PrefMan.getInt(applicationContext, PrefMan.Key.theme, Theme.defaultThemeValue)) }
            AppModel.theme = theme
            PuppyGitAndroidTheme(
                theme = theme.value,
            ) {
                MainCompose()
                //                Greeting(baseContext)
            }
        }

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextUtil.getLocalizedContext(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()

        AppModel.destroyer()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        MyLog.d(TAG, "#onNewIntent() called")
        IntentHandler.setNewIntent(intent)
    }

}


@Composable
fun MainCompose() {
    val stateKeyTag = "MainCompose"
    val funName = "MainCompose"

    val activityContext = LocalContext.current
    val loadingText = rememberSaveable { mutableStateOf(activityContext.getString(R.string.launching))}

    val sshCertRequestListenerChannel = remember { Channel<Int>() }
    val isInitDone = rememberSaveable { mutableStateOf(false) };
    val requireMasterPassword = rememberSaveable { mutableStateOf(false) };


    //start: init user and billing state, 这代码位置别随便挪，必须早于调用Billing.init()，不然相关的集合无法得到更新
    val isProState = rememberSaveable { mutableStateOf(false) }
    UserUtil.updateUserStateToRememberXXXForPage(newIsProState = isProState)

    //end: init user and billing state

    val sshAskUserUnknownHostRequestList = mutableCustomStateListOf(stateKeyTag, "sshAskUserUnknownHostRequestList", listOf<SshAskUserUnknownHostRequest>())
    val currentSshAskUserUnknownHostRequest = mutableCustomStateOf<SshAskUserUnknownHostRequest?>(stateKeyTag, "currentSshAskUserUnknownHostRequest", null)

    val iTrustTheHost = rememberSaveable { mutableStateOf(false) }
    val showSshDialog = rememberSaveable { mutableStateOf(false) }
    val closeSshDialog ={
        showSshDialog.value=false
    }
    val allowOrRejectSshDialogCallback={
        //设置当前条目为null以接受新条目
        currentSshAskUserUnknownHostRequest.value = null
        // 设置“我信任此主机”勾选框为未勾选
        iTrustTheHost.value = false
    }
    if(showSshDialog.value) {
        SshUnknownHostDialog(
            currentSshAskUserUnknownHostRequest = currentSshAskUserUnknownHostRequest,
            iTrustTheHost = iTrustTheHost,
            closeSshDialog = closeSshDialog,
            allowOrRejectSshDialogCallback = allowOrRejectSshDialogCallback,
            appContext = activityContext
        )
    }




    //初始化完成显示app界面，否则显示loading界面
    if(isInitDone.value) {
        if(requireMasterPassword.value) {
            RequireMasterPasswordScreen(requireMasterPassword)
        }else {
            AppScreenNavigator()
        }
    }else {
        //这个东西太阴间了，除非是真的需要确保阻止用户操作，例如保存文件，否则尽量别用这个
//        LoadingDialog(loadingText.value)
//        LoadingDialog()

        //TODO 把文字loading替换成有App Logo 的精美画面
        //这里用Scaffold是因为其会根据系统是否暗黑模式调整背景色，就不需要我自己判断了
        Scaffold { contentPadding ->
            LoadingText(contentPadding = contentPadding, text = loadingText.value)
        }
    }

    //compose创建时的副作用
    LaunchedEffect(Unit) {
//        println("LaunchedEffect传Unit只会执行一次，由于maincompose是app其他compose的根基，不会被反复创建销毁，所以maincompose里的launchedEffect只会执行一次，可以用来执行读取配置文件之类的初始化操作")
        try {
//        读取配置文件，初始化状态之类的操作，初始化时显示一个loading页面，完成后更新状态变量，接着加载app页面
            //初始化完成之后，设置变量，显示应用界面
            doJobThenOffLoading {
                isInitDone.value = false

                //test passed
//                assert(!MyLog.isInited)
                //test

                AppModel.init_2()

                requireMasterPassword.value = AppModel.requireMasterPassword()

                //如果无需主密码，直接在这检查是否需要迁移密码，一般迁移密码发生在加密解密器升级之后，可能换了实现，之类的
                if(requireMasterPassword.value.not()) {
                    loadingText.value = activityContext.getString(R.string.checking_creds_migration)
                    AppModel.dbContainer.credentialRepository.migrateEncryptVerIfNeed(AppModel.masterPassword.value)
                }

                //test passed
//                assert(MyLog.isInited)
                //test

                isInitDone.value = true

            }

            KnownHostRequestStateMan.init(sshAskUserUnknownHostRequestList.value)

            doJobThenOffLoading {
                while (true) {
                    if(sshCertRequestListenerChannel.tryReceive().isClosed) {
                        break
                    }

                    if(showSshDialog.value.not() && currentSshAskUserUnknownHostRequest.value==null) {
                        val item = KnownHostRequestStateMan.getFirstThenRemove()
                        if(item != null) {
                            if(Lg2HomeUtils.itemInUserKnownHostsFile(item.sshCert).not()) {
                                currentSshAskUserUnknownHostRequest.value = item
                                iTrustTheHost.value = false
                                showSshDialog.value = true
                            }
                        }
                    }

                    delay(1000)
                }
            }

        } catch (e: Exception) {
            MyLog.e(TAG, "#$funName err: "+e.stackTraceToString())
        }

        //test passed
//        delay(30*1000)
//        AppModel.exitApp()  //测试exitApp()，Editor未保存的数据是否会保存，结论：会
//        appContext.findActivity()?.recreate()  // 测试重建是否会保存数据，结论：会
//        throw RuntimeException("throw exception test")
        //test
    }

    //compose被销毁时执行的副作用
    DisposableEffect(Unit) {
//        ("DisposableEffect: entered main")
        onDispose {
//            ("DisposableEffect: exited main")
            sshCertRequestListenerChannel.close()
        }
    }

}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    PuppyGitAndroidTheme {
//        Greeting("Android")
//    }
//}

//一个演示方法
//@Composable
//fun MainScreen(navController: NavController) {
//    LaunchedEffect(Unit) {
//        println("LaunchedEffect: entered main")
//        var i = 0
//        // Just an example of coroutines usage
//        // don't use this way to track screen disappearance
//        // DisposableEffect is better for this
//        try {
//            while (true) {
//                delay(1000)
//                println("LaunchedEffect: ${i++} sec passed")
//            }
//        } catch (cancel: Exception) {
//            println("LaunchedEffect: job cancelled")
//        }
//    }
//    DisposableEffect(Unit) {
//        println("DisposableEffect: entered main")
//        onDispose {
//            println("DisposableEffect: exited main")
//        }
//    }
//}
