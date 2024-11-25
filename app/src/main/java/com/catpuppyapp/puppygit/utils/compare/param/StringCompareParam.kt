package com.catpuppyapp.puppygit.utils.compare.param

class StringCompareParam(
    val chars:String,
): CompareParam<String> {
    override fun getLen(): Int {
        return chars.length
    }

    override fun getChar(index:Int): Char {
        return chars.get(index)
    }

    override fun isEmpty(): Boolean {
        return chars.isEmpty()
    }

    override fun isOnlyLineSeparator(): Boolean {
        return getLen()==1 && getChar(0)=='\n'
    }

    override fun hasEndOfNewLine(): Boolean {
        return chars.endsWith('\n')
    }

    override fun getTextNoEndOfNewLine(copyEvenNoNewLine:Boolean): CompareParam<String> {
        return if(hasEndOfNewLine()) {
            StringCompareParam(chars.substring(0, getLen()-1))
        }else {
            StringCompareParam(if(copyEvenNoNewLine) String(chars.toByteArray()) else chars)
        }
    }
}
