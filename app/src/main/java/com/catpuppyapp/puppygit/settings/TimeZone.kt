package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class TimeZone (
    /**
     * 为true则App跟随系统时区，否则使用 `commitTimeZone_OffsetInMinutes` 指定的偏移量，由于github默认显示提交时间时跟随用户时区，所以我这默认值也设成true，为true应该比较符合直觉
     */
    var followSystem:Boolean = true,


    /**
     * 时区偏移量，正数或负数或0，留空以使用提交中携带的时区偏移量，非空使用其指定的分钟数
     * 应通过 `str.trim().toInt()` 解析此字段并在解析后使用 `Utils.isValidOffsetInMinutes()` 检测其值是否在有效区间
     */
    var offsetInMinutes:String="",

)
