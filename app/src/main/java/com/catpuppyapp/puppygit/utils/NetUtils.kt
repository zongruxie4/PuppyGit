package com.catpuppyapp.puppygit.utils

import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * THIS FUNCTION NOT TESTED
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
            return@submit false
        } finally {
            connection?.disconnect()
        }
    }

    return try {
        future.get(timeout, TimeUnit.SECONDS) // 等待结果，设置超时
    } catch (e: Exception) {
        false
    } finally {
        executor.shutdown()
    }
}
