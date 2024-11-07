package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R

@Composable
fun InfoDialog(
    showTitleInfoDialog: MutableState<Boolean>,
    content: @Composable ()->Unit,
) {
    ConfirmDialog2(
        title = stringResource(R.string.info),
        requireShowTextCompose = true,
        textCompose = {
            MySelectionContainer {
                content()
            }
        },
        onCancel = { showTitleInfoDialog.value = false },
        cancelBtnText = stringResource(R.string.close),
        showOk = false
    ) { }
}
