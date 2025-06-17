package com.catpuppyapp.puppygit.compose

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.catpuppyapp.puppygit.etc.PathType
import com.catpuppyapp.puppygit.screen.shared.FilePath
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.parcelize.Parcelize
import java.io.File
import kotlin.coroutines.cancellation.CancellationException


private const val TAG = "FileChangeListener"

private fun doActOrClearIgnoreOnce(state: MutableState<FileChangeListenerState>, act:()->Unit) {
    if(state.value.ignoreOnceState) {
        state.apply { value = value.copy(ignoreOnceState = false) }
        MyLog.d(TAG, "#FileChangeListenerState: ignore file changed event once")
    }else {
        act()
    }
}

@Parcelize
data class FileChangeListenerState(
    internal val ignoreOnceState: Boolean = false,
) : Parcelable {
    companion object {
        fun ignoreOnce(state: MutableState<FileChangeListenerState>) = state.apply { value = value.copy(ignoreOnceState = true) }
    }
}

@Composable
fun rememberFileChangeListenerState() = rememberSaveable { mutableStateOf(FileChangeListenerState()) }


/**
 * only for absolute path like "/storage/emulate/0/something", file or dir both are should works
 */
@Composable
fun FileChangeListener(
    state: MutableState<FileChangeListenerState>,
    context: Context,
    path: FilePath,

    // interval for check file change, not used for saf uri.
    // decrease this value will not help content uri listener to get new changed notify early,
    //   but welp for absolute file path got changed notify early
    intervalInMillSec: Long = 1000,

    onChange:()->Unit,
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

                                doActOrClearIgnoreOnce(state, onChange)
                            }
                        }
                    }catch(_: CancellationException){
                        // task may be canceled normally, just ignore

                    }catch (e: Exception) {
                        MyLog.d(TAG, "listen change of file err: filePath='${filePath.value}', err=${e.stackTraceToString()}")
                    }
                }
            }else {
                contentObserver.value = object : ContentObserver(Handler()) {
                    // idk the selfChange what for? indicate changed by this ContentObserver instance itself?
                    override fun onChange(selfChange: Boolean) {
                        super.onChange(selfChange)

                        doActOrClearIgnoreOnce(state, onChange)
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
