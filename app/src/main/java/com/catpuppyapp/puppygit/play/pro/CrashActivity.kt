package com.catpuppyapp.puppygit.play.pro

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.catpuppyapp.puppygit.ui.theme.PuppyGitAndroidTheme
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.ContextUtil
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.PrefMan


private const val TAG = "CrashActivity"


class CrashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val funName = "onCreate"

        super.onCreate(savedInstanceState)

        MyLog.d(TAG, "onCreate called")

        val errMsg = getErrMsg(intent)


        setContent {
            //这页面只使用主题，不更新主题，所以不需要创建state变量
            val theme = ""+PrefMan.getInt(applicationContext, PrefMan.Key.theme, Theme.defaultThemeValue)
            PuppyGitAndroidTheme(
                theme = theme,
            ) {
                MainCompose(errMsg)
            }
        }

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextUtil.getLocalizedContext(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}


@Composable
private fun MainCompose(errMsg: String) {
    val stateKeyTag = "MainCompose"
    val funName = "MainCompose"

    val activityContext = LocalContext.current
    val loadingText = rememberSaveable { mutableStateOf(activityContext.getString(R.string.launching))}

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        Text(errMsg)
    }

}

private const val errMsgKey = "errMsg"
fun startCrashActivity(context: Context, errMsg:String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.putExtra(errMsgKey, errMsg)
    intent.setClass(context, CrashActivity::class.java)

    context.startActivity(intent)
}

private fun getErrMsg(intent: Intent):String {
    return intent.extras?.getString(errMsgKey) ?: ""
}
