package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.style.MyStyleKt

@Composable
fun ClickableText(text:String, onClick:()->Unit) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontWeight = FontWeight.Light,
        style = MyStyleKt.ClickableText.style,
        color = MyStyleKt.ClickableText.color,
        fontSize = 16.sp,
        modifier = MyStyleKt.ClickableText.modifierNoPadding.clickable {
            onClick()
        },
    )
}


@Composable
fun ClickableText(
    text:String,
    maxLines:Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    fontWeight: FontWeight? = null,
    modifier: Modifier
) {
    Text(
        text = text,
        maxLines = maxLines,
        overflow = overflow,
        fontWeight = fontWeight,
        style = MyStyleKt.ClickableText.style,
        color = MyStyleKt.ClickableText.color,
        fontSize = 16.sp,
        modifier = modifier,
    )
}
