package com.catpuppyapp.puppygit.play.pro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.catpuppyapp.puppygit.constants.IntentCons
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ContextUtil
import com.catpuppyapp.puppygit.utils.MyLog


private const val TAG = "CodeEditorActivity"


class CodeEditorActivity : ComponentActivity() {
    companion object {
        val ACTION_OPEN_FILE = IntentCons.Action.OPEN_FILE
        const val INTENT_EXTRA_KEY_FILE_NAME = IntentCons.ExtrasKey.fileName
        const val INTENT_EXTRA_KEY_LINE_NUM = IntentCons.ExtrasKey.lineNum



        fun start(fromActivity: Activity, fileName:String, lineNum:Int) {
            val intent = Intent(ACTION_OPEN_FILE).apply {
                putExtra(INTENT_EXTRA_KEY_FILE_NAME, fileName)
                putExtra(INTENT_EXTRA_KEY_LINE_NUM, lineNum)
                setClass(fromActivity, CodeEditorActivity::class.java)
            }

            fromActivity.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val funName = "onCreate"

        super.onCreate(savedInstanceState)

        MyLog.d(TAG, "#onCreate called")





    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextUtil.getLocalizedContext(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        AppModel.handleActivityConfigurationChanged(newConfig)
    }

}


private fun getErrMsg(intent: Intent):String {
    return intent.extras?.getString(CrashActivity.INTENT_EXTRA_KEY_ERR_MSG) ?: ""
}
