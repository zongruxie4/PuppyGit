package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class DevSettings (
    var singleDiffOn: Boolean = false,

    /**
     * Diff页面，行选项（select compare那个菜单），显示`匹配所有/不匹配所有`选项
     */
    var showMatchedAllAtDiff: Boolean = false,

)
