package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class DevSettings (
    var singleDiffOn: Boolean = false,

    /**
     * Diff页面，行选项（select compare那个菜单），显示`匹配所有/不匹配所有`选项
     */
    var showMatchedAllAtDiff: Boolean = false,

    /**
     * 若为true，使用之前加载changelist的方法，多jni，少数据拷贝，循环一次；
     * 若为false，使用少jni调用，多数据拷贝，多循环一次的方法。
     *
     * 内存占用和性能都没严格测试，不好说，不过除非git status条目特别多，几万个之类的，否则差别应该不大。
     */
    var legacyChangeListLoadMethod: Boolean = false,
)
