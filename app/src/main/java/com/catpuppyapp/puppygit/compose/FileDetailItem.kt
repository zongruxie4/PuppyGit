package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.dto.FileDetail
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.UIHelper

@Composable
fun FileDetailItem(
    item: FileDetail,
    onLongClick:(FileDetail)->Unit,
    onClick:(FileDetail)->Unit,
){
    Column(
        modifier = Modifier
            .padding(10.dp)
            .background(UIHelper.defaultCardColor())
            .width(200.dp)

    ) {
        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(item.fileName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        MyHorizontalDivider()

        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(item.shortContent)
        }
    }
}
