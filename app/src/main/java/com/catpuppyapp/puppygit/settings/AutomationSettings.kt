package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable
import java.util.SortedMap
import java.util.SortedSet

@Serializable
data class AutomationSettings (
    val packageNameAndRepoIdsMap:SortedMap<String, SortedSet<String>> = sortedMapOf(),

    /**
     * show notify if server do act err
     *
     * 注意：这是系统通知，不是toast！
     * 最好能够实现：点击可启动app并前往仓库页面，定位到发生错误的仓库卡片
     */
    var showNotifyWhenErr:Boolean = true,

    /**
     * show notify if server do act success
     */
    var showNotifyWhenSuccess:Boolean = true,

    /**
     * show notify if server acting
     * 进度通知，例如正在推送、正在拉取之类的
     */
    var showNotifyWhenProgress:Boolean = true,

    )
