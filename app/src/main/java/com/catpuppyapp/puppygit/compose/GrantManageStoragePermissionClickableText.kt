package com.catpuppyapp.puppygit.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.getStoragePermission


@Composable
fun GrantManageStoragePermissionClickableText(activityContext: Context) {
    Row(modifier = Modifier
        .padding(bottom = 15.dp)
        .padding(horizontal = MyStyleKt.defaultHorizontalPadding)) {
        ClickableText(
            text = stringResource(R.string.please_grant_permission_before_you_add_a_storage_path),
            overflow = TextOverflow.Visible,
            fontWeight = FontWeight.Light,
            modifier = MyStyleKt.ClickableText.modifierNoPadding.clickable {
                val activity = activityContext as? Activity;

                // grant permission for read/write external storage
                if (activity == null) {
                    Msg.requireShowLongDuration(activityContext.getString(R.string.please_go_to_system_settings_allow_manage_storage))
                } else {
                    activity.getStoragePermission()
                }
            },
        )
    }
}
