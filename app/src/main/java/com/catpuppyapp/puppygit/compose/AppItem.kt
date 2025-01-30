package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.catpuppyapp.puppygit.dto.AppInfo


@Composable
fun AppItem(
    appInfo:AppInfo,
    trailIcon: (@Composable BoxScope.(initModifier:Modifier)->Unit)? = null,
    onClick:((AppInfo) -> Unit)?
) {

    Box(
        modifier = Modifier.fillMaxWidth().then(
            if(onClick == null) Modifier else Modifier.clickable { onClick(appInfo) }
        ),
    ) {
        val iconModifier = if(trailIcon == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(.9f)

        Row(
            modifier = iconModifier.align(Alignment.CenterStart)
        ) {

            Image(
                bitmap = appInfo.appIcon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 10.dp)
            )

            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = appInfo.appName)

                Text(text = appInfo.packageName, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
            }
        }

        if(trailIcon != null) {
            trailIcon(Modifier.fillMaxWidth(.1f).align(Alignment.CenterEnd))
        }

    }

}
