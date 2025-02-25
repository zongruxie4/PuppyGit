package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R


@Composable
fun InLineIcon(
    icon: ImageVector,
    tooltipText: String,
    iconContentDesc: String = tooltipText,
    iconModifier: Modifier = Modifier.size(16.dp),
    pressedCircleSize: Dp = 24.dp,
    onClick: () -> Unit
) {
    LongPressAbleIconBtn(
        tooltipText = tooltipText,
        icon = icon,
        iconContentDesc = iconContentDesc,
        iconModifier = iconModifier,
        pressedCircleSize = pressedCircleSize
    ) {
        onClick()
    }
}


@Composable
fun InLineCommitHistoryIcon(
    onClick: () -> Unit
) {
    InLineHistoryIcon(
        tooltipText = stringResource(R.string.commit_history),
    ){
        onClick()
    }
}

@Composable
fun InLineCopyIcon(
    onClick: () -> Unit
) {
    InLineIcon(
        icon = Icons.Filled.ContentCopy,
        tooltipText = stringResource(R.string.copy),
    ){
        onClick()
    }
}

@Composable
private fun InLineHistoryIcon(
    tooltipText: String,
    onClick: () -> Unit
) {
    InLineIcon(
        icon = Icons.Filled.History,
        tooltipText = tooltipText,
    ){
        onClick()
    }
}
