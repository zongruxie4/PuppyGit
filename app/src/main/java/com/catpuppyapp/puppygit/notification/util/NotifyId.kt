package com.catpuppyapp.puppygit.notification.util

import kotlin.random.Random

// 通知id应用进程内全局唯一，同id通知会覆盖
// 保留[1, 100]，作为保留id，其余值可能会在生成随机id时生成，参见 `randomId()`
object NotifyId {
    // reversed, no use
    const val reversedId1 = 1;

    // > 80 is foreground service notify id （我规定的，不是系统规定的）
    const val foregroundServiceHttp = 81
    const val foregroundServiceAutomation = 82

    // random id
    const val randomNotifyIdStartAt = 101;  // included
    fun randomId():Int {
        //I want less than `randomNotifyIdStartAt` as reserved notification Id, so don't use it
        return Random.nextInt(randomNotifyIdStartAt, Int.MAX_VALUE)
    }
}
