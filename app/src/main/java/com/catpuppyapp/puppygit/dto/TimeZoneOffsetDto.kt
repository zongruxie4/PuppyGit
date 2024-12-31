package com.catpuppyapp.puppygit.dto

import androidx.compose.runtime.Immutable
import java.time.ZoneOffset

// Immutable比val 更常量，不光不会重新赋值，字段内部也不会修改，由于ZoneOffset是immutable的，所以此类能做到这一点，就标记上了
// 注：此类是thread safe且Immutable的
@Immutable
data class TimeZoneOffsetDto (
    val zoneOffset:ZoneOffset,
    val offsetInMinute:Int,

    //这个秒数最多18个小时，很小，用Int即可无需用Long
    val offsetInSec:Int
)
