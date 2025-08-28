package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import com.catpuppyapp.puppygit.syntaxhighlight.base.PLScope


@Composable
fun SelectSyntaxHighlightingDialog(
    plScope: PLScope,
    onCancel: () -> Unit,
    onOK: (selectedScope: PLScope) -> Unit,
) {
    SingleSelectDialog(
        currentItem = plScope,
        itemList = PLScope.SCOPES_NO_AUTO,
        text = { it.name },
        onCancel = onCancel,
        onOK = onOK
    )
}
