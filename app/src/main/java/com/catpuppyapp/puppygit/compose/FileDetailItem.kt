package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.dto.FileDetail
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.UIHelper


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileDetailItem(
    width:Dp,
    margin:Dp,
    idx:Int,
    item: FileDetail,
    selected:Boolean,
    onLongClick:(idx:Int, FileDetail)->Unit,
    onClick:(FileDetail)->Unit,
){
    Column(
        modifier = Modifier
            .padding(margin)
            .background(if(selected) MaterialTheme.colorScheme.primaryContainer else UIHelper.defaultCardColor())
            .combinedClickable(
                onLongClick = { onLongClick(idx, item) },
            ) {
                onClick(item)
            }
            .width(width)

    ) {
//        val fontColor = UIHelper.getFontColor()
        val fontColor = Color.Unspecified

        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            ScrollableRow {
                Text(item.file.name, fontSize = MyStyleKt.Title.firstLineFontSize, color = fontColor, fontWeight = FontWeight.Bold)
            }
            ScrollableRow {
                Text(item.file.path.ioPath, fontSize = MyStyleKt.Title.secondLineFontSize, color = fontColor)
            }
        }

        MyHorizontalDivider()

        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(item.shortContent, color = fontColor)
        }
    }
}
