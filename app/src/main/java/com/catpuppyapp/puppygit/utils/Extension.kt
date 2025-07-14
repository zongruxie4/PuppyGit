package com.catpuppyapp.puppygit.utils

import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.delay

fun <T : CharSequence> T.takeIfNotBlank(): T? = if (isNotBlank()) this else null

fun <T : CharSequence> T.takeIfNotEmpty(): T? = if (isNotEmpty()) this else null


// I am not sure, maybe this will cause a NPE? check the log file "API31_12_20250617_TextInputServiceAndroid NPE.logcat"
fun SoftwareKeyboardController.hideForAWhile(timeoutInMillSec: Long = 200L) {
    doJobThenOffLoading {
        // stop popup soft keyboard when open a file
        val hideKbJob = doJobThenOffLoading {
            runCatching {
                while (true) {
                    // too short may cause NPE
                    delay(30)
                    hide()
                }
            }
        }

        delay(timeoutInMillSec)

        hideKbJob?.cancel()
    }
}

//fun SoftwareKeyboardController.hideWhenTimeout(timeoutInMillSec: Long = 200L) {
//    doJobThenOffLoading {
//        runCatching {
//            delay(timeoutInMillSec)
//
//            hide()
//        }
//    }
//}

// used to indicate a copy is called by a cut action
fun String.appendCutSuffix() = "$this (CUT)"

fun String.countSub(sub: String) = this.split(sub).size - 1
