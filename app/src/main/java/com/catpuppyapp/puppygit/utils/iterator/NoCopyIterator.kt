package com.catpuppyapp.puppygit.utils.iterator

/**
 * no copy, no tread safe iterator
 * 无拷贝，线程不安全的iterator
 * @author Bandeapart1964 of catpuppyapp
 */
class NoCopyIterator<T>(
    val srcList:MutableList<T>
):MutableIterator<T> {

    var count:Int=0

    fun reset(){
        count = 0
    }

    fun srcIsEmpty():Boolean {
        return srcList.isEmpty()
    }

    override fun hasNext(): Boolean {
        return count < srcList.size
    }

    override fun next(): T {
        return srcList[count++]
    }

    override fun remove() {
        srcList.removeAt(--count)
    }
}
