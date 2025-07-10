package com.catpuppyapp.puppygit.codeeditor

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// it was had "CodeEditorState", so named this to "2"
@Parcelize
data class CodeEditorState2(
    val theme: String = "",
    val languageScopeName: PLScope = PLScope.NONE,
    val autoCompleteEnabled: Boolean = false,
): Parcelable
