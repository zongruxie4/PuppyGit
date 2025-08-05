package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PlainDialog(
    modifier: Modifier = Modifier.fillMaxWidth(),
    shape: Shape = RoundedCornerShape(16.dp),
    onClose: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = modifier,
            shape = shape,
        ) {
            content()
        }
    }
}
