package com.catpuppyapp.puppygit.dev

object DevFeature {
    private const val prefix = "Dev: "

    val safDiff_text = appendDevPrefix("SAF Diff")
    val inner_data_storage = appendDevPrefix("Inner Data")  // app 在 /data/data 下的私有目录
    val external_data_storage = appendDevPrefix("External Data") // app 在 外部存储路径/Android/data/data 下的私有目录
    val setDiffRowToNoMatched = appendDevPrefix("No Matched")
    val setDiffRowToAllMatched = appendDevPrefix("All Matched")

    fun appendDevPrefix(text:String):String {
        return prefix+text
    }
}
