package com.catpuppyapp.puppygit.notification

// 通知id应用进程内全局唯一，同id通知会覆盖
// 我保留了1到50，作为保留id，其余值可能会在生成随机id时生成，参见 NotifyUtil.genId()
object NotifyId {
    const foregroundServiceHttp = 11
    const foregroundServiceAutomation = 12
}
