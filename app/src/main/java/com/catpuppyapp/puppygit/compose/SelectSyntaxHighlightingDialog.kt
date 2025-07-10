package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import com.catpuppyapp.puppygit.codeeditor.PLScope


@Composable
fun SelectSyntaxHighlightingDialog(
    plScope: PLScope,
    onCancel: () -> Unit,
    onOK: (selectedScope: PLScope) -> Unit,
) {

    val selectedPlScope = rememberSaveable { mutableStateOf(plScope) }

    ConfirmDialog2(
        requireShowTextCompose = true,
        textCompose = {
            ScrollableColumn {
                SingleSelection(
                    itemList = PLScope.SCOPES_NO_AUTO,
                    selected = { idx, it -> selectedPlScope.value == it },
                    text = { idx, it -> it.scope },
                    onClick = { idx, it -> selectedPlScope.value = it },
                )
            }
        },

        onCancel = onCancel,
    ) {
        onOK(selectedPlScope.value)
    }

}
