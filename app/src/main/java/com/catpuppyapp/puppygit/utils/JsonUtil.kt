package com.catpuppyapp.puppygit.utils

import kotlinx.serialization.json.Json

object JsonUtil {

    //ignoreUnknownKey：s忽略对象里没有的key；encodeDefaults：保存默认值，不然只有当类的字段值和其默认值不一样时，才会被保存到配置文件，用于配置文件时建议不要保存默认值，这样节省文件大小，并方便开发者控制默认值。
//    val j = Json{ ignoreUnknownKeys = true; encodeDefaults=true}
    val j = Json{ ignoreUnknownKeys = true; encodeDefaults=false }
}
