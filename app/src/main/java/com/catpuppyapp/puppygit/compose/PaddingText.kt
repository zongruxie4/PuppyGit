package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.catpuppyapp.puppygit.style.MyStyleKt

@Composable
fun PaddingText(
    text:String,
    color: Color = Color.Unspecified,
    paddingValues: PaddingValues = PaddingValues(horizontal = MyStyleKt.defaultHorizontalPadding)
) {
    Text(
        text = text,
        color = color,
        fontWeight = FontWeight.Light,
        modifier = Modifier.padding(paddingValues)
    )
}

@Composable
fun DefaultPaddingText(text:String, color: Color = Color.Unspecified) {
    PaddingText(text, color)
}
