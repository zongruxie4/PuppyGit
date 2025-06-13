package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.style.MyStyleKt


@Composable
fun TwoLineTextsAndIcons(
    text1:String,
    text2:String,
    trailIcons: @Composable () -> Unit
) {
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
            SelectionRow(
                Modifier.horizontalScroll(rememberScrollState())
            ) {
                Text(text = text1, fontSize = MyStyleKt.Title.firstLineFontSize, fontWeight = FontWeight.Bold)
            }

            SelectionRow(
                Modifier.horizontalScroll(rememberScrollState())
            ) {
                Text(text = text2, fontSize = MyStyleKt.Title.secondLineFontSize, fontWeight = FontWeight.Light)
            }
        }

        trailIcons()

    }
}
