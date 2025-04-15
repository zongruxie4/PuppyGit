package com.catpuppyapp.puppygit.utils

import java.io.File

object DirSearchUtil {

    /**
     * 虚假的广度优先搜索，针对顶级目录和普通过滤功能性能一样（所以凑合用了一段时间，感觉也够用），
     * 但后续针对子目录的搜索并非广度优先，
     * 实际会深入到一个子目录的最底层后才会查找
     * 其他的子目录（同时每个子目录也同样会先对目录内所有文件和文件夹进行匹配，
     * 再分别深入其子目录，这个过程是递归的），所以并非真正的广度优先搜索。
     *
     * 这个算是针对深度优先搜索做了一些优化使其针对顶层目录匹配的效率和单层级目录的过滤功能性能一样。
     */
    fun recursiveFakeBreadthFirstSearch(
        dir: File,
        match: (srcIdx:Int, srcItem: File) -> Boolean,
        matchedCallback: (srcIdx:Int, srcItem: File) -> Unit,
        canceled: () -> Boolean
    ) {
        if(canceled()) {
            return
        }

        val files = dir.listFiles()
        if(files == null || files.isEmpty()) {
            return
        }

        val subdirs = mutableListOf<File>()
        for((idx, f) in files.withIndex()) {
            if(canceled()) {
                return
            }

            if(match(idx, f)) {
                matchedCallback(idx, f)
            }

            if(f.isDirectory) {
                subdirs.add(f)
            }
        }

        //TODO 这里可改成基于协程的，性能应该至少能翻1倍，但要控制协程数量，4个左右就可以，不要太多，不然物理cpu核心占满，创建更多协程也没意义
        //遍历子目录
        for(sub in subdirs) {
            recursiveFakeBreadthFirstSearch(dir = sub, match = match, matchedCallback=matchedCallback, canceled = canceled)
        }
    }

    /**
     * 真正的广度优先搜索，会一层一层查找，先第一层，再第2层，再第3层，直到最后一层目录。
     *
     * 针对顶级目录的搜索和普通单层的过滤功能性能一样。
     */
    fun realBreadthFirstSearch(
        dir: File,
        match: (srcIdx:Int, srcItem: File) -> Boolean,
        matchedCallback: (srcIdx:Int, srcItem: File) -> Unit,
        canceled: () -> Boolean
    ) {
        if(canceled()) {
            return
        }

        //忽略当前被搜索的目录，例如你在目录"PuppyGit"搜索关键字"PuppyGit"，这时并不希望匹配到当前目录本身
        val ignorePath = dir.canonicalPath
        val subdirs = mutableListOf(dir)

        while (subdirs.isNotEmpty()) {
            val subdirsCopy = subdirs.toList()
            subdirs.clear()

            for((idx, f) in subdirsCopy.withIndex()) {
                if(canceled()) {
                    return
                }

                if(f.canonicalPath != ignorePath && match(idx, f)) {
                    matchedCallback(idx, f)
                }

                if(f.isDirectory) {
                    val files = f.listFiles()
                    if(files == null || files.isEmpty()) {
                        continue
                    }

                    subdirs.addAll(files)
                }
            }
        }

    }

}
