package com.catpuppyapp.puppygit.compose

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
import com.catpuppyapp.puppygit.utils.ActivityUtil


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
                // grant permission for read/write external storage
                ActivityUtil.getManageStoragePermissionOrShowFailedMsg(activityContext)
            },
        )
    }
}
