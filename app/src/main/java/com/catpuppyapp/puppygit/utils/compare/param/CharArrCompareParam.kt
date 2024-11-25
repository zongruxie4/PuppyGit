package com.catpuppyapp.puppygit.utils.compare.param

class CharArrCompareParam(
    val chars:CharArray,
): CompareParam<CharArray> {
    override fun getLen(): Int {
        return chars.size
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
        return chars.last() == '\n'
    }

    override fun getTextNoEndOfNewLine(copyEvenNoNewLine:Boolean): CompareParam<CharArray> {
        return if(hasEndOfNewLine()) {
            CharArrCompareParam(chars.copyOf(getLen()-1))
        }else {
            CharArrCompareParam(if(copyEvenNoNewLine) chars.copyOf() else chars)
        }
    }
}
