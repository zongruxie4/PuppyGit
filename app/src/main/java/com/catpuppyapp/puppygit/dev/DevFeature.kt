package com.catpuppyapp.puppygit.dev

import androidx.compose.runtime.mutableStateOf
import com.catpuppyapp.puppygit.settings.SettingsUtil

object DevFeature {
    private const val prefix = "Dev: "

    val safDiff_text = appendDevPrefix("SAF Diff")
    val inner_data_storage = appendDevPrefix("Inner Data")  // app 在 /data/data 下的私有目录
    val external_data_storage = appendDevPrefix("External Data") // app 在 外部存储路径/Android/data/data 下的私有目录
    val setDiffRowToNoMatched = appendDevPrefix("No Matched")
    val setDiffRowToAllMatched = appendDevPrefix("All Matched")
    const val singleDiffNoPrefix = "Single Diff"
    val singleDiff = appendDevPrefix(singleDiffNoPrefix)



    val singleDiffState = mutableStateOf(false)

    fun updateSingleDiffValue(newValue: Boolean) {
        //更新状态变量，使用的时候就不用查配置文件了
        singleDiffState.value = newValue

        //写入配置文件
        SettingsUtil.update {
            it.devSettings.singleDiffOn = newValue
        }
    }

    fun appendDevPrefix(text:String):String {
        return prefix+text
    }
}
