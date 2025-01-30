package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.dto.AppInfo


@Composable
fun AppItem(
    appInfo:AppInfo,
    trailIcon: (@Composable BoxScope.(initModifier:Modifier)->Unit)? = null,
    onClick:(AppInfo) -> Unit
) {

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        val iconModifier = if(trailIcon == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(.8f)

        Column(
            modifier = iconModifier.align(Alignment.CenterStart)
        ) {
            Text(text = appInfo.appName)
            Text(text = appInfo.packageName, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }

        if(trailIcon != null) {
            trailIcon(Modifier.fillMaxWidth(.2f).align(Alignment.CenterEnd))
        }

    }

}
