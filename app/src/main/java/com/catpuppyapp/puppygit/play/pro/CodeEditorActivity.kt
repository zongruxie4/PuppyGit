package com.catpuppyapp.puppygit.play.pro

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.catpuppyapp.puppygit.constants.IntentCons
import com.catpuppyapp.puppygit.play.pro.base.BaseActivity
import com.catpuppyapp.puppygit.utils.MyLog


private const val TAG = "CodeEditorActivity"


class CodeEditorActivity : BaseActivity() {
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


}

