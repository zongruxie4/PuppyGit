package com.catpuppyapp.puppygit.dto

interface ItemKey {
    /**
     * this function promise when `instanceA.equals(instanceB)` is true, return same key
     * 本函数确保在两个实例equals的情况下返回相同key
     */
    fun getItemKey():String
}
