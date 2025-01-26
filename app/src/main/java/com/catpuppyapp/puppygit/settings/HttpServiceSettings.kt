package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class HttpServiceSettings (
    /**
     * App启动时自动启动服务
     */
    var launchOnAppStartup:Boolean = false,

    var listenHost:String = "127.0.0.1",

    var listenPort:Int = 52520,

    /**
     * token, use to verify the requester
     *
     * note: if token empty, will allow all request
     *
     * code: will only do empty check with this field, means it can be include blank char, but doesn't recommend
     * 代码：对这个字段只执行空值检查，意味着它可包含空格，但不建议
     */
    var token:String = "default_puppygit_token",

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
    var showNotifyWhenSuccess:Boolean = false,

    /**
     * no token require if ip in the white list
     */
    var ipWhiteList:List<String> = listOf("127.0.0.1"),

    /**
     * never do nothing and never response if ip in the list
     */
    var ipBlackList:List<String> = listOf()
)
