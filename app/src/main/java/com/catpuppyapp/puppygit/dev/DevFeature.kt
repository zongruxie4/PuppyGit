package com.catpuppyapp.puppygit.dev

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.PrefUtil

object DevFeature {
    private const val prefix = "Dev: "
    fun appendDevPrefix(text:String):String {
        return prefix+text
    }


    val safDiff_text = appendDevPrefix("SAF Diff")
    val inner_data_storage = appendDevPrefix("Inner Data")  // app 在 /data/data 下的私有目录
    val external_data_storage = appendDevPrefix("External Data") // app 在 外部存储路径/Android/data/data 下的私有目录


    // single diff
    const val str_singleDiff = "Single Diff"
    val singleDiff = appendDevPrefix(str_singleDiff)
    val state_singleDiff = mutableStateOf(false)

    fun updateSingleDiffValue(newValue: Boolean) {
        //更新状态变量，使用的时候就不用查配置文件了
        state_singleDiff.value = newValue

        //写入配置文件
        SettingsUtil.update {
            it.devSettings.singleDiffOn = newValue
        }
    }


    // matched all for selected compare at diff screen
    val setDiffRowToNoMatched = appendDevPrefix("No Matched")
    val setDiffRowToAllMatched = appendDevPrefix("All Matched")
    const val str_showMatchedAllAtDiff = "Show Matched All at Diff Screen"
    val state_showMatchedAllAtDiff = mutableStateOf(false)

    fun updateShowMatchedAllAtDiffScreenValue(newValue: Boolean) {
        //更新状态变量，使用的时候就不用查配置文件了
        state_showMatchedAllAtDiff.value = newValue

        //写入配置文件
        SettingsUtil.update {
            it.devSettings.showMatchedAllAtDiff = newValue
        }
    }



    // random launching text
    val showRandomLaunchingText = object : DevItem<Boolean>(text = "Show Random Launching Text", state = mutableStateOf(false)) {
        override fun update(newValue: Boolean, context: Context?) {
            //更新状态变量，使用的时候就不用查配置文件了
            state.value = newValue

            //写入配置文件
            PrefUtil.setShowRandomLaunchingText(context!!, newValue)
        }


    }

}

abstract class DevItem<T>(
    val text:String,
    val state: MutableState<T>,
) {
    // context如果需要可以传，不需要可以不传
    abstract fun update(newValue:T, context: Context? = null)
}
