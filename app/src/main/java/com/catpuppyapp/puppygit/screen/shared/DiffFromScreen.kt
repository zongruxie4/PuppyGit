package com.catpuppyapp.puppygit.screen.shared

enum class DiffFromScreen(val code: String) {
    HOME_CHANGELIST("1"),
    INDEX("2"),
    TREE_TO_TREE("3"),
    FILE_HISTORY("4");

    companion object {
        fun fromCode(code: String): DiffFromScreen? {
            return entries.find { it.code == code }
        }
    }
}
