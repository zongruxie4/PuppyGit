package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.catpuppyapp.puppygit.dto.AppInfo


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    appInfo:AppInfo,
    haptic: HapticFeedback,
    // item onLongClick，不是尾部图标的onClick
    onLongClick:((AppInfo) -> Unit)?,
    onClick:(AppInfo) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            onLongClick = {
                if(onLongClick != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick(appInfo)
                }
            }
        ){
            onClick(appInfo)
        },

        verticalAlignment = Alignment.CenterVertically,
    ) {

        Image(
            bitmap = appInfo.appIcon.toBitmap().asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .padding(start = 10.dp)
        )

        Column(
            modifier = Modifier.padding(5.dp).padding(end = 5.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = appInfo.appName)
            Text(text = appInfo.packageName, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)

        }


    }

}
