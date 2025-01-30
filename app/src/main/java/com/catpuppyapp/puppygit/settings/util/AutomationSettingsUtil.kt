package com.catpuppyapp.puppygit.settings.util

import com.catpuppyapp.puppygit.settings.AutomationSettings

object AutomationSettingsUtil {
    fun getPackageNames(automationSettings: AutomationSettings):Set<String> {
        return automationSettings.packageNameAndRepoIdsMap.keys
    }

    fun getRepoIds(automationSettings: AutomationSettings, packageName:String):Set<String> {
        return automationSettings.packageNameAndRepoIdsMap.get(packageName) ?: sortedSetOf()
    }

}
