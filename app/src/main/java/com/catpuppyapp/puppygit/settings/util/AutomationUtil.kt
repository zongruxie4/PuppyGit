package com.catpuppyapp.puppygit.settings.util

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import com.catpuppyapp.puppygit.dto.AppInfo
import com.catpuppyapp.puppygit.service.MyAccessibilityService
import com.catpuppyapp.puppygit.settings.AutomationSettings
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.getInstalledAppList


object AutomationUtil {
    private const val TAG = "AutomationSettingsUtil"


    fun getPackageNames(automationSettings: AutomationSettings):Set<String> {
        return automationSettings.packageNameAndRepoIdsMap.keys
    }

    fun getRepoIds(automationSettings: AutomationSettings, packageName:String):List<String> {
        return automationSettings.packageNameAndRepoIdsMap.get(packageName) ?: listOf()
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

    /**
     * @return Pair(selectedList, unselectedList)
     */
    fun getSelectedAndUnSelectedAppList(context: Context, automationSettings: AutomationSettings):Pair<List<AppInfo>, List<AppInfo>> {
        val installedAppList = getInstalledAppList(context)
        val userAddedAppList = getPackageNames(automationSettings)
        val selectedList = mutableListOf<AppInfo>()
        val unselectedList = mutableListOf<AppInfo>()

        installedAppList.forEach { installed ->
            if(userAddedAppList.contains(installed.packageName)) {
                installed.isSelected = true
                selectedList.add(installed)
            }else {
                installed.isSelected = false
                unselectedList.add(installed)
            }
        }


        // remove uninstalled apps
        val existedApps = selectedList.map { it.packageName }
        SettingsUtil.update { s ->
            val map = s.automation.packageNameAndRepoIdsMap
            map.keys.forEach { k ->
                if(!existedApps.contains(k)) {
                    map.remove(k)
                }
            }
        }

        return Pair(selectedList, unselectedList)
    }


}
