package com.catpuppyapp.puppygit.fileeditor.texteditor.state

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import com.catpuppyapp.puppygit.utils.getRandomUUID

@Stable
data class TextFieldState(
    val id: String = getRandomUUID(),
    val value: TextFieldValue = TextFieldValue(),
    val isSelected: Boolean = false
)
