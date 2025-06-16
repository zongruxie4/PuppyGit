package com.catpuppyapp.puppygit.dto

data class LineNumParseResult(
    // line num and column number, both are start from 1, subtract 1 to trans them to index
    val lineNum:Int = 1,
    val columnNum:Int = 1,
    val isRelative: Boolean = false,
) {
    fun lineNumToIndex(curLineIndex:Int, maxLineIndex:Int):Int {
        return lineNum.let {
            (if(isRelative) curLineIndex + it  else (it - 1)).coerceAtMost(maxLineIndex).coerceAtLeast(0)
        }
    }

    fun columnNumToIndex(): Int {
        return columnNum - 1
    }
}
