package com.catpuppyapp.puppygit.dev

object DevFeature {
    private const val prefix = "Dev: "

    val safDiff_text = appendDevPrefix("SAF Diff")

    fun appendDevPrefix(text:String):String {
        return prefix+text
    }
}
