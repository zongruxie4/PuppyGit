package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.style.MyStyleKt

@Composable
fun CheckBoxNoteRow(content: @Composable RowScope.()->Unit) {
    Row(modifier = Modifier.padding(horizontal = MyStyleKt.defaultHorizontalPadding)) {
        content()
    }
}
