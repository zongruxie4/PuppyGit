package com.catpuppyapp.puppygit.codeeditor

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.catpuppyapp.puppygit.utils.getRandomUUID
import io.github.rosemoe.sora.lang.styling.Styles
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock

class HunkSyntaxHighlighter(
    // low mem count应属于页面，所有hunk共享同一low mem count
    val getLowMemoryCount: suspend () -> Unit,
    val updateLowMemCount: suspend (Int) -> Unit,

    val stylesMapLock: ReentrantReadWriteLock = ReentrantReadWriteLock(),
    val stylesMap: SnapshotStateMap<String, HunkStylesResult> = mutableStateMapOf(),
    val plScope: MutableState<PLScope> = mutableStateOf(PLScope.AUTO),
) {
    var languageScope: PLScope = PLScope.NONE

}


data class HunkStylesResult(
    val inDarkTheme: Boolean,
    val styles: Styles,
    val from: StylesResultFrom,
    val uniqueId: String = getRandomUUID(),
    val hunkId:String,
    val languageScope: PLScope,
    val applied: AtomicBoolean = AtomicBoolean(false)
) {


}
