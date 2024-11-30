package com.catpuppyapp.puppygit.utils.compare.param

class StringCompareParam(
    private val chars:String,
    private val length:Int,
): CompareParam<String>(chars, length) {
    override fun getTextNoEndOfNewLine(): CompareParam<String> {
        return if(hasEndOfNewLine()) {
            StringCompareParam(chars, length - 1)
        }else {
            this
        }
    }
}
