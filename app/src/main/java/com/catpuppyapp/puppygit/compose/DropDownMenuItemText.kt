package com.catpuppyapp.puppygit.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.catpuppyapp.puppygit.style.MyStyleKt


@Composable
fun DropDownMenuItemText(text:String, selected:Boolean) {
    Text(
        text = text,
        color = if(selected) MyStyleKt.DropDownMenu.selectedItemColor() else Color.Unspecified,
        fontWeight = if(selected) FontWeight.ExtraBold else null,  //注意默认是 null，不是 Normal，实测默认的字体比Normal粗
    )
}
