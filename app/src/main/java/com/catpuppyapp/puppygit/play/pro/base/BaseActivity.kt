package com.catpuppyapp.puppygit.play.pro.base

import android.content.Context
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ContextUtil

open class BaseActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextUtil.getLocalizedContext(newBase))
    }

    override fun onDestroy() {
        super.onDestroy()

        AppModel.destroyer()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        AppModel.handleActivityConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()

        AppModel.updateExitApp { finish() }
    }
}
