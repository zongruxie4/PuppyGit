package com.catpuppyapp.puppygit.utils.cache

import com.catpuppyapp.puppygit.git.CommitDto
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * 缓存仓库的提交
 */
object CommitCache {
    private val cache = mutableMapOf<String, MutableMap<String, CommitDto>>()
    private val lock = Mutex()

    //每个仓库最多存多少个提交
    private const val EACH_REPO_CACHE_SIZE = 100

    suspend fun cacheIt(repoId:String, commitFullHash:String, commitDto: CommitDto) {
        lock.withLock {
            val cacheOfRepo = getCacheMapOfRepo(repoId)
            if(cacheOfRepo.size < EACH_REPO_CACHE_SIZE) {
                cacheOfRepo.put(commitFullHash, commitDto)
            }
        }
    }

    suspend fun getCachedDataOrNull(repoId: String, commitFullHash: String): CommitDto? {
        lock.withLock {
            val cacheOfRepo = getCacheMapOfRepo(repoId)
            return cacheOfRepo.get(commitFullHash)
        }
    }

    suspend fun clear() {
        lock.withLock {
            cache.clear()
        }
    }

    private fun getCacheMapOfRepo(repoId:String) = cache.getOrPut(repoId) { mutableMapOf() }

}
