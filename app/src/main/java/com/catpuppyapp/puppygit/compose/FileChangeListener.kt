package com.catpuppyapp.puppygit.compose

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import com.catpuppyapp.puppygit.screen.shared.FilePath
import com.catpuppyapp.puppygit.screen.shared.FuckSafFile
import com.catpuppyapp.puppygit.utils.MyLog
import kotlinx.coroutines.Job
import kotlinx.parcelize.Parcelize


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
    path: String,

    // interval for check file change, not used for saf uri.
    // decrease this value will not help content uri listener to get new changed notify early,
    //   but welp for absolute file path got changed notify early
    intervalInMillSec: Long = 1000,

    onChange:()->Unit,
) {
    // use state to avoid lambda catching copy of values
    val file = path.let { remember(it) { mutableStateOf(FuckSafFile(context, FilePath(it))) } }

    val fileObserver = remember { mutableStateOf<Job?>(null) }

    val stopListen = {
        runCatching {
            fileObserver.value?.cancel()
        }
    }

    val startListen = {
        stopListen()

        val result = runCatching {
            fileObserver.value = file.value.createChangeListener(intervalInMillSec) {
                doActOrClearIgnoreOnce(state, onChange)
            }
        }

        if(result.isFailure) {
            MyLog.d(TAG, "start listen file change err: filePath='${file.value.path.ioPath}, err=${result.exceptionOrNull()?.stackTraceToString()}")
        }
    }

    LaunchedEffect(path) {
        startListen()
    }

    DisposableEffect(Unit) {
        onDispose {
            stopListen()
        }
    }
}
