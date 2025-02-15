package com.catpuppyapp.puppygit.utils

import android.content.Context

object PrefUtil {
    /**
     * 更新dev模式
     */
    fun setDevMode(context: Context, enable:Boolean) {
        PrefMan.set(context, PrefMan.Key.devModeOn, if(enable) "1" else "0")
    }

    /**
     * 获取是否启用了dev模式
     */
    fun getDevMode(context: Context):Boolean {
        return PrefMan.get(context, PrefMan.Key.devModeOn, "0") != "0"
    }
}
