package com.catpuppyapp.puppygit.utils

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object NetUtils {
    private const val TAG="NetUtils"


    fun checkPuppyGitHttpServiceRunning(baseUrl: String, timeout: Long=2):Boolean {
        val targetUrl = "$baseUrl/status"
        val success = checkApiRunning(targetUrl, timeout)
        MyLog.d(TAG, "#checkPuppyGitHttpServiceRunning: test url '$targetUrl', success=$success")
        return success
    }

    /**
     * @param urlString url
     * @param timeout timeout in second
     *
     * @return if got any, even err status from server, then return true, else false
     */
    fun checkApiRunning(urlString: String, timeout: Long): Boolean {
        val executor = Executors.newSingleThreadExecutor()
        val future: Future<Boolean> = executor.submit<Boolean> {
            var connection: HttpURLConnection? = null
            return@submit try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = (timeout * 1000).toInt() // 设置连接超时
                connection.readTimeout = (timeout * 1000).toInt() // 设置读取超时

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return@submit true
                } else {
                    return@submit true
                }
            } catch (e: Exception) {
                MyLog.e(TAG, "#checkApiRunning: errcode=10c55e2d, err=${e.stackTraceToString()}")
                return@submit false
            } finally {
                connection?.disconnect()
            }
        }

        return try {
            future.get(timeout, TimeUnit.SECONDS) // 等待结果，设置超时
        } catch (e: Exception) {
            MyLog.e(TAG, "#checkApiRunning: errcode=1c31128d, err=${e.stackTraceToString()}")
            false
        } finally {
            executor.shutdown()
        }
    }

}
