package com.catpuppyapp.puppygit.utils

import androidx.compose.ui.platform.SoftwareKeyboardController
import kotlinx.coroutines.delay

fun <T : CharSequence> T.takeIfNotBlank(): T? = if (isNotBlank()) this else null

fun <T : CharSequence> T.takeIfNotEmpty(): T? = if (isNotEmpty()) this else null


fun SoftwareKeyboardController.hideForAWhile(timeoutInMillSec: Long = 500L) {
    doJobThenOffLoading {
        // stop popup soft keyboard when open a file
        val hideKbJob = doJobThenOffLoading {
            runCatching {
                while (true) {
                    delay(1)
                    hide()
                }
            }
        }

        delay(timeoutInMillSec)

        hideKbJob?.cancel()
    }
}
