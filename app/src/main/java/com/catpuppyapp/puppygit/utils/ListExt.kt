package com.catpuppyapp.puppygit.utils

/**
 * due to default `forEachIndexed` of `List` very easy got `ConcurrentModificationException`,
 * so better use this instead of, it can't promise 100% safe, but less chance got `ConcurrentModificationException`
 */
inline fun <T> List<T>.forEachIndexedBetter(
    foreach: (idx:Int, T)->Unit
) {
    for(idx in indices) {
        val value = getOrNull(idx) ?: continue
        foreach(idx, value)
    }
}
