package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import com.catpuppyapp.puppygit.utils.EncodingUtil


@Composable
fun SelectEncodingDialog(
    currentCharset: String?,
    onCancel: () -> Unit,
    onOK: (selectedCharset: String) -> Unit,
) {
    SingleSelectDialog(
        currentItem = currentCharset,
        itemList = EncodingUtil.supportedCharsetList,
        text = { it },
        onCancel = onCancel,
        onOK = onOK
    )
}
