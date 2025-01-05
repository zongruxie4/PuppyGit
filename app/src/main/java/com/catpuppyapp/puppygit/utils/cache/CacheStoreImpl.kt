package com.catpuppyapp.puppygit.utils.cache

import com.catpuppyapp.puppygit.utils.getShortUUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

// `open` make it inheritable
open class CacheStoreImpl(
    private val storage:MutableMap<String, Any?> = ConcurrentHashMap(),
    private val lock: Mutex = Mutex()
):CacheStore {
//    private val storage:MutableMap<String, Any?> = _storage
    //    private val storage:MutableMap<String, Any?> = mutableMapOf()

    override fun getARandomKey():String {
        val key = getShortUUID()
        return key
    }

    override fun setThenReturnKey(value: Any):String {
        val key = getARandomKey()
        set(key, value)
        return key
    }
    suspend fun syncSetThenReturnKey(value: Any):String {
        return doActWithLock{ setThenReturnKey(value) } as String
    }

    override fun set(key:String, value:Any):Any? {
//        if(debugModeOn) {
//            println("Cache.set(), key=$key, value=$value")
//        }
        return storage.put(key, value)
    }
    override fun get(key:String):Any? {
        return storage.get(key)
    }

    override fun getOrDefault(key: String, default: Any, saveDefaultWhenNoKey:Boolean): Any {
        val v = get(key)
        return if(v==null) {
            if(saveDefaultWhenNoKey) {
                set(key, default)
            }
            default
        }else {
            v
        }
    }

    override fun <T> getByType(key: String): T? {
        return get(key) as? T
    }

    override fun <T:Any> getOrDefaultByType(key: String, default: T, saveDefaultWhenNoKey:Boolean): T {
        val v = getByType<T>(key)
        return if(v==null) {
            if(saveDefaultWhenNoKey) {
                set(key, default)
            }
            default
        }else {
            v
        }
    }

    override fun del(key:String):Any? {
//        if(debugModeOn) {
//            println("Cache.del(), will del key="+ key)
//        }
        return storage.remove(key)
    }

    override fun getThenDel(key:String):Any? {
        return del(key)
    }

    override fun<T> getByTypeThenDel(key:String):T? {
//            val v = getByType<T>(key)
        val v = del(key) as? T
        return v
    }

    suspend fun syncGetThenDel(key:String):Any? {
        return doActWithLock { getThenDel(key) }
    }

    //让newKey指向oldKey的数据，然后从map删除oldKey
    override fun updateKey(oldKey:String, newKey:String, requireDelOldKey:Boolean){
        val oldVal = get(oldKey)?:return
        set(newKey, oldVal)
        if(requireDelOldKey) {
            del(oldKey)
        }
    }

    override fun clear() {
//        if(debugModeOn) {
//            println("Cache.keys="+ storage.keys)
//        }
        storage.clear()
    }

    //act 里存的应该是操作storage的一些方法，例如组合调用 get/set 之类的
    suspend fun doActWithLock(act:()->Any?):Any? {
        lock.withLock {
            return act()
        }
    }
    //这个keyPrefix是否包含keySeparator取决于调用者
    override fun clearByKeyPrefix(keyPrefix: String) {
        for(k in storage.keys) {
            if(k.startsWith(keyPrefix)) {
                del(k)

//                if(requireDel) {
//                }else{
//                    //ConcurrentMap不能设置值为null
//                    set(k, null)
//                }
            }
        }

    }

}
