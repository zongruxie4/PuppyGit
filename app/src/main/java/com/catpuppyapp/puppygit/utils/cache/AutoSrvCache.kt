package com.catpuppyapp.puppygit.utils.cache


object AutoSrvCache:CacheStoreImpl(){
    object Key {
        const val curPackageName = "cur_package_name"

    }

    fun setCurPackageName(packageName:String) {
        set(Key.curPackageName, packageName)
    }

    fun getCurPackageName():String {
        return getOrDefaultByType<String>(Key.curPackageName, default = {""})
    }
}
