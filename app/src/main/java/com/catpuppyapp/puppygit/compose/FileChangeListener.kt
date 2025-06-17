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
import com.catpuppyapp.puppygit.screen.shared.FuckSafFile


@Composable
fun FileChangeListener(
    context: Context,
    file: FuckSafFile,
    onChange:()->Unit,
) {
    // use state to avoid lambda catching copy of values
    val filePath = file.path.ioPath.let { remember(it) { mutableStateOf(it) } }
    val pathType = file.path.ioPathType.let { remember(it) { mutableStateOf(it) } }
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
