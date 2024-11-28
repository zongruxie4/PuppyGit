package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R

@Composable
fun OpenAsAskReloadDialog(
    onCancel:()->Unit,
    doReload:()->Unit,
) {
    ConfirmDialog2(
        title = stringResource(R.string.reload_file),
        text = stringResource(R.string.back_editor_from_external_app_ask_reload),
        okBtnText = stringResource(R.string.reload),
        cancelBtnText = stringResource(R.string.cancel),
        onCancel = onCancel,
        onOk = doReload
    )
}
