package com.catpuppyapp.puppygit.play.pro

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.catpuppyapp.puppygit.constants.IntentCons
import com.catpuppyapp.puppygit.play.pro.base.BaseComposeActivity
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.MyLog
import io.github.rosemoe.sora.widget.CodeEditor


private const val TAG = "CodeEditorActivity"


class CodeEditorActivity : BaseComposeActivity() {
    companion object {
        val ACTION_OPEN_FILE = IntentCons.Action.OPEN_FILE
        const val INTENT_EXTRA_KEY_FILE_PATH = IntentCons.ExtrasKey.filePath
        const val INTENT_EXTRA_KEY_FILE_NAME = IntentCons.ExtrasKey.fileName
        const val INTENT_EXTRA_KEY_LINE_NUM = IntentCons.ExtrasKey.lineNum


        fun start(
            fromActivity: Activity,
            filePath:String,
            fileName:String,
            lineNum:Int
        ) {
            val intent = Intent(ACTION_OPEN_FILE).apply {
                putExtra(INTENT_EXTRA_KEY_FILE_PATH, filePath)
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

        AppModel.init_1(applicationContext, exitApp = { finish() }, initActivity = true)

        setExceptionHandler(TAG, funName)



        // 创建一个 LinearLayout 作为根布局
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        // 创建 TextView
        val textView = TextView(this).apply {
            text = "Hello, World!"
            textSize = 24f
        }

        // 创建 Button
        val button = Button(this).apply {
            text = "Click Me"
            setOnClickListener {
                textView.text = "Button Clicked!" // 更改 TextView 的文本
            }
        }

        // 将 TextView 和 Button 添加到布局中
        layout.addView(textView)
        layout.addView(button)


        val editor = CodeEditor(this)
        editor.setText("Hello, world!") // 设置文本
        editor.showSoftInput()
        editor.typefaceText = Typeface.MONOSPACE // 使用Monospace字体
        editor.nonPrintablePaintingFlags =
            CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR or CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION // Show Non-Printable Characters
        layout.addView(editor, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))


        // 设置 Activity 的内容视图为动态创建的布局
        setContentView(layout)

    }


}

