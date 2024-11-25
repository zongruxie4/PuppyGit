package com.catpuppyapp.puppygit.utils.compare.param

interface CompareParam<T> {
    fun getLen() : Int
    fun getChar(index:Int) : Char
    fun isEmpty():Boolean
    fun isOnlyLineSeparator():Boolean

    /**
     * at the end of text, has '\n' or not
     */
    fun hasEndOfNewLine():Boolean

    /**
     * @param copyEvenNoNewLine if true, even text no '\n' at the end, till return a copy, else, return this
     */
    fun getTextNoEndOfNewLine(copyEvenNoNewLine:Boolean=false):CompareParam<T>
}
