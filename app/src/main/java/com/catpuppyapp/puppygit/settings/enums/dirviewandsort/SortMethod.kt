package com.catpuppyapp.puppygit.settings.enums.dirviewandsort

enum class SortMethod(val code: Int) {
    NAME(1),
    TYPE(2),
    SIZE(3),
    LAST_MODIFIED(4);

    companion object {
        fun fromCode(code: Int): SortMethod? {
            return entries.find { it.code == code }
        }
    }
}
