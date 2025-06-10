package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun TwoLineTextsAndIcons(text1:String, text2:String, trailIcons: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .padding(5.dp)
            .padding(end = 5.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .padding(end = 2.dp)
                .fillMaxWidth(.7f),
            verticalArrangement = Arrangement.Center,
        ) {
            MySelectionContainer {
                Text(text = text1)
            }

            MySelectionContainer {
                Text(text = text2, style = MaterialTheme.typography.bodySmall)
            }
        }

        trailIcons()

    }
}
