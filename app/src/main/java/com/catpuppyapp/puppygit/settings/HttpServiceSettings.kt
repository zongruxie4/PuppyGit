package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class HttpServiceSettings (
    /**
     * 总开关
     */
    var enable:Boolean = false,

    /**
     * 开机自启
     */
    var launchAfterSystemStarted:Boolean = false,

    var listenHost:String = "127.0.0.1",

    var listenPort:Int = 52520,

    /**
     * token, use to verify the requester
     */
    var token:String = "default_puppygit_token",

    @Deprecated("no need this, should http requester handle response by itself")
    var showNotify:Boolean = false,

    /**
     * no token require if ip in the white list
     */
    var ipWhiteList:List<String> = listOf("127.0.0.1"),

    /**
     * never do nothing and never response if ip in the list
     */
    var ipBlackList:List<String> = listOf()
)
