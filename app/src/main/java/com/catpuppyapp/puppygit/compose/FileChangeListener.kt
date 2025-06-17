package com.catpuppyapp.puppygit.compose

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.FileObserver
import android.os.Handler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.catpuppyapp.puppygit.etc.PathType
import com.catpuppyapp.puppygit.screen.shared.FilePath
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File

private const val TAG = "FileChangeListener"

/**
 * this is bad for jni and no promise for content uri, so deprecated
 */
@Composable
fun FileChangeListener_bad_for_jni(
    context: Context,
    path: FilePath,
    onChange:()->Unit,
) {
    // use state to avoid lambda catching copy of values
    val filePath = path.ioPath.let { remember(it) { mutableStateOf(it) } }
    val pathType = path.ioPathType.let { remember(it) { mutableStateOf(it) } }
    val trueAbsFalseUri = (pathType.value == PathType.ABSOLUTE).let { remember(it) { mutableStateOf(it) } }

    val fileObserver = remember { mutableStateOf<FileObserver?>(null) }
    val contentObserver = remember { mutableStateOf<ContentObserver?>(null) }

    val startListen = {
        if(trueAbsFalseUri.value) {
            fileObserver.value = object : FileObserver(filePath.value, FileObserver.MODIFY) {
                override fun onEvent(event: Int, path: String?) {
                    if (event == FileObserver.MODIFY) {
                        onChange()
                    }
                }
            }

            fileObserver.value?.startWatching()
        }else {
            contentObserver.value = object : ContentObserver(Handler()) {
                // idk the selfChange what for? indicate changed by this ContentObserver instance itself?
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    onChange()
                }
            }.apply {
                context.contentResolver.registerContentObserver(
                    Uri.parse(filePath.value),
                    true,
                    this
                )
            }
        }
    }

    val stopListen = {
        runCatching {
            fileObserver.value?.stopWatching()
            contentObserver.value?.let {
                context.contentResolver.unregisterContentObserver(it)
            }
        }
    }

    LaunchedEffect(filePath.value) {
        if(trueAbsFalseUri.value) {
            stopListen()
            startListen()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            stopListen()
        }
    }
}


/**
 * only for absolute path like "/storage/emulate/0/something", file or dir both are should works
 */
@Composable
fun FileChangeListener(
    context: Context,
    path: FilePath,

    // interval for check file change, not used for saf uri.
    // decrease this value will not help content uri listener to get new changed notify early,
    //   but welp for absolute file path got changed notify early
    intervalInMillSec: Long = 1000,

    onChange:(newFileLen:Long, newFileLastModified:Long)->Unit,
) {
    // use state to avoid lambda catching copy of values
    val filePath = path.ioPath.let { remember(it) { mutableStateOf(it) } }
    val pathType = path.ioPathType.let { remember(it) { mutableStateOf(it) } }
    val trueAbsFalseUri = (pathType.value == PathType.ABSOLUTE).let { remember(it) { mutableStateOf(it) } }

    val fileObserver = remember { mutableStateOf<Job?>(null) }
    val contentObserver = remember { mutableStateOf<ContentObserver?>(null) }

    val stopListen = {
        runCatching {
            fileObserver.value?.cancel()
        }

        runCatching {
            contentObserver.value?.let {
                context.contentResolver.unregisterContentObserver(it)
            }
        }
    }

    val startListen = {
        stopListen()

        val result = runCatching {
            if(trueAbsFalseUri.value) {
                fileObserver.value = doJobThenOffLoading {
                    try {
                        val file = File(filePath.value)
                        var oldFileLen = file.length()
                        var oldFileModified = file.lastModified()
                        while (true) {
                            delay(intervalInMillSec)

                            val newFileLen = file.length()
                            val newFileModified = file.lastModified()
                            if(oldFileLen != newFileLen || oldFileModified != newFileModified) {
                                oldFileLen = newFileLen
                                oldFileModified = newFileModified

                                onChange(newFileLen, newFileModified)
                            }
                        }
                    }catch(canceled: Exception){
                        // task may be canceled, just ignore

                    }catch (e: Exception) {
                        MyLog.d(TAG, "listen change of file err: filePath='${filePath.value}', err=${e.stackTraceToString()}")
                    }
                }
            }else {
                contentObserver.value = object : ContentObserver(Handler()) {
                    // idk the selfChange what for? indicate changed by this ContentObserver instance itself?
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)
                        onChange(0, 0)
                    }
                }.apply {
                    context.contentResolver.registerContentObserver(
                        Uri.parse(filePath.value),
                        true,
                        this
                    )
                }
            }
        }

        if(result.isFailure) {
            MyLog.d(TAG, "start listen file change err: filePath='${filePath.value}, err=${result.exceptionOrNull()?.stackTraceToString()}")
        }
    }

    LaunchedEffect(filePath.value) {
        startListen()
    }

    DisposableEffect(Unit) {
        onDispose {
            stopListen()
        }
    }
}
