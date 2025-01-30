package com.catpuppyapp.puppygit.settings.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.catpuppyapp.puppygit.service.MyAccessibilityService
import com.catpuppyapp.puppygit.settings.AutomationSettings
import com.catpuppyapp.puppygit.utils.MyLog


object AutomationSettingsUtil {
    private const val TAG = "AutomationSettingsUtil"


    fun getPackageNames(automationSettings: AutomationSettings):Set<String> {
        return automationSettings.packageNameAndRepoIdsMap.keys
    }

    fun getRepoIds(automationSettings: AutomationSettings, packageName:String):Set<String> {
        return automationSettings.packageNameAndRepoIdsMap.get(packageName) ?: sortedSetOf()
    }


    /**
     * 检查App无障碍服务是否启用
     * @return `null` unknown state, `true` enabled, `false` disabled
     *
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean? {
        return try {
            val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
            val componentName = ComponentName(context, MyAccessibilityService::class.java).flattenToString()

            enabledServices.contains(componentName)
        }catch (e:Exception) {
            MyLog.e(TAG, "#isAccessibilityServiceEnabled() err: ${e.stackTraceToString()}")
            null
        }
    }
}
