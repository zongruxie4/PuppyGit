package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.codeeditor.PLScope


@Composable
fun SelectSyntaxHighlightingDialog(
    plScope: PLScope,
    onCancel: () -> Unit,
    onOK: (selectedScope: PLScope) -> Unit,
) {

    PlainDialog(onClose = onCancel) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 20.dp, horizontal = 10.dp)
        ) {
            SingleSelection(
                itemList = PLScope.SCOPES_NO_AUTO,
                selected = { idx, it -> plScope == it },
                text = { idx, it -> it.name },
                onClick = { idx, it ->
                    onCancel()
                    onOK(it)
                },
                minHeight = 60.dp
            )
        }
    }
}
