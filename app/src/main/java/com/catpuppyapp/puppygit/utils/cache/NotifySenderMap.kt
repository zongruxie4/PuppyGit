package com.catpuppyapp.puppygit.utils.cache

/**
 * this is default cache instance
 */
object NotifySenderMap:CacheStoreImpl(){
    fun genKey(repoId:String, sessionId:String):String {
        return "$repoId-$sessionId"
    }
}
