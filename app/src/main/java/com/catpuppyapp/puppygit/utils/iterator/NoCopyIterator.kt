package com.catpuppyapp.puppygit.utils.iterator

/**
 * no copy, no tread safe iterator, but faster
 * 无拷贝，线程不安全的iterator，但是快
 * @author Bandeapart1964 of catpuppyapp
 */
open class NoCopyIterator<T>(
    val srcList:MutableList<T>
):MutableIterator<T> {

    /**
     * indicate the iteration order is reversed or not
     */
    open val isReversed = false

    protected open var currentIndex:Int = 0

    open fun reset(){
        currentIndex = 0
    }

    open fun srcIsEmpty():Boolean {
        return srcList.isEmpty()
    }


    override fun hasNext(): Boolean {
        return currentIndex >= 0 && currentIndex < srcList.size
    }

    override fun next(): T {
        return srcList[currentIndex++]
    }

    override fun remove() {
        srcList.removeAt(--currentIndex)
    }
}

class ReverseNoCopyIterator<T>(
    srcList:MutableList<T>
): NoCopyIterator<T>(srcList) {

    override val isReversed = true

    override var currentIndex:Int = srcList.size - 1

    override fun reset(){
        currentIndex = srcList.size - 1
    }

    override fun srcIsEmpty():Boolean {
        return srcList.isEmpty()
    }

    override fun next(): T {
        return srcList[currentIndex--]
    }

    override fun remove() {
        srcList.removeAt(++currentIndex)
    }
}
