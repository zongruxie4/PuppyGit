package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CopyScrollableColumn(modifier: Modifier=Modifier, content:@Composable ()->Unit) {
    MySelectionContainer {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            content()
        }
    }
}
