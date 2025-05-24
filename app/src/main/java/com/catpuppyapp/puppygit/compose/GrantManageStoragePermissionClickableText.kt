package com.catpuppyapp.puppygit.compose

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.ActivityUtil

private val color = MyStyleKt.TextColor.danger()

@Composable
fun GrantManageStoragePermissionClickableText(activityContext: Context) {
    Row(
        modifier = Modifier
            .padding(bottom = 15.dp)
            .padding(horizontal = MyStyleKt.defaultHorizontalPadding)
            .clickable {
                // grant permission for read/write external storage
                ActivityUtil.getManageStoragePermissionOrShowFailedMsg(activityContext)
            }
        ,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.please_grant_permission_before_you_add_a_storage_path),
            overflow = TextOverflow.Visible,
            fontWeight = FontWeight.Light,
            color = color,
        )

        InLineIcon(
            icon = Icons.AutoMirrored.Filled.OpenInNew,
            tooltipText = "",
            iconColor = color
        )
    }
}
