package com.catpuppyapp.puppygit.utils

import com.catpuppyapp.puppygit.etc.Ret
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object NetUtils {
    private const val TAG="NetUtils"


    fun checkPuppyGitHttpServiceRunning(baseUrl: String, timeoutInSec: Long=5): Ret<Unit?> {
        val targetUrl = "$baseUrl/status"
        val success = checkApiRunning(targetUrl, timeoutInSec)
        MyLog.d(TAG, "#checkPuppyGitHttpServiceRunning: test url '$targetUrl', success=$success")
        return success
    }

    /**
     * @param urlString url
     * @param timeoutInSec timeout in second
     *
     * @return if got any, even err status from server, then return true, else false
     */
    fun checkApiRunning(urlString: String, timeoutInSec: Long): Ret<Unit?> {
        val executor = Executors.newSingleThreadExecutor()
        val future: Future<Ret<Unit?>> = executor.submit<Ret<Unit?>> {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlString)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = (timeoutInSec * 1000).toInt() // 设置连接超时，乘1000把秒转换成毫秒
                connection.readTimeout = (timeoutInSec * 1000).toInt() // 设置读取超时

                val responseCode = connection.responseCode

                return@submit Ret.createSuccess(null, "success got response with status code '$responseCode'")

//                if (responseCode == HttpURLConnection.HTTP_OK) {
                      // response 200 status code
//                    return@submit Ret.createSuccess(null, "success with HTTP_OK 200 status")
//                } else {
                      // response but is not 200 status
//                }
            } catch (e: Exception) {
                MyLog.e(TAG, "#checkApiRunning: errcode=10c55e2d, err=${e.stackTraceToString()}")
                return@submit Ret.createError(null, "err: ${e.localizedMessage}")
            } finally {
                connection?.disconnect()
            }
        }

        return try {
            future.get(timeoutInSec, TimeUnit.SECONDS) // 等待结果，设置超时
        }catch (e:TimeoutException){
            MyLog.e(TAG, "#checkApiRunning: errcode=9f2d7aec, timeout maybe, err=${e.stackTraceToString()}")

            Ret.createError(null, "timeout: ${e.localizedMessage}")
        } catch (e: Exception) {
            MyLog.e(TAG, "#checkApiRunning: errcode=1c31128d, err=${e.stackTraceToString()}")
            Ret.createError(null, "err: ${e.localizedMessage}")
        } finally {
            executor.shutdown()
        }
    }

}
