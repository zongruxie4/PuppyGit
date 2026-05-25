package com.catpuppyapp.puppygit.utils
import java.io.File
import kotlin.concurrent.thread

object LfsUtil {
    private const val TAG = "LfsUtil"

    fun runGitLfs(repoPath: String) {
        try {
            val process = ProcessBuilder(FsUtils.getLfsBinPath(), "fetch")
                .directory(File(repoPath))
                // 注意：这里不要加 redirectErrorStream 了
                .start()

            // 1. 异步读取标准输出 (stdout)
            thread {
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        MyLog.d(TAG, "[STDOUT]: $line") // 替换为你的 Log.d
                    }
                }
            }

            // 2. 异步读取标准错误 (stderr)
            thread {
                process.errorStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        MyLog.e(TAG, "[STDERR]: $line") // 替换为你的 Log.e
                    }
                }
            }

            // 3. 主线程同步等待子进程结束
            val exitCode = process.waitFor()
            MyLog.d(TAG, "Git-LFS 执行完毕，退出码: $exitCode")

        } catch (e: Exception) {
            throw e
        }
    }
}
