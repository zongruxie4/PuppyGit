package com.catpuppyapp.puppygit.screen.shared

enum class MainActivityLifeCycle(val code: String) {
    NONE("NONE"),
    ON_PAUSE("ON_PAUSE"),
    ON_RESUME("ON_RESUME"),

    ;


    companion object {
        fun fromCode(code: String): MainActivityLifeCycle? {
            return entries.find { it.code == code }
        }
    }
}

fun doActIfIsExpectLifeCycle(expectLifeCycle: MainActivityLifeCycle, act:()->Unit) {
    if(SharedState.currentMainActivityLifeCycle.value == expectLifeCycle.code) {
        //判断完立刻重置，确保一个事件只消费一次
        SharedState.currentMainActivityLifeCycle.value = MainActivityLifeCycle.NONE.code

        act()
    }
}
