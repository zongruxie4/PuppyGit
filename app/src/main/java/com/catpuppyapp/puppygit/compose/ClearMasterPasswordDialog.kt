package com.catpuppyapp.puppygit.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading

@Composable
fun ClearMasterPasswordDialog(
    onCancel:()->Unit,
    onOk:()->Unit
) {
    ConfirmDialog2(
        title = stringResource(R.string.clear_master_password),
        requireShowTextCompose = true,
        textCompose = {
            MySelectionContainer {
                ScrollableColumn {
                    Text(stringResource(R.string.clear_master_password_confirm))
                }
            }
        },
        okBtnText = stringResource(R.string.clear),
        okTextColor = MyStyleKt.TextColor.danger(),
        onCancel = onCancel
    ) {
        doJobThenOffLoading {
            SettingsUtil.update {
                it.masterPasswordHash = ""
            }

            AppModel.singleInstanceHolder.masterPassword.value = ""

            onOk()
        }
    }
}
