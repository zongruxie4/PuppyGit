package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.dto.FileDetail
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.UIHelper

private const val itemWidth = 200
private const val itemMargin = 10


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileDetailItem(
    item: FileDetail,
    selected:Boolean,
    onLongClick:(FileDetail)->Unit,
    onClick:(FileDetail)->Unit,
){
    val configuration = AppModel.getCurActivityConfig()
    val width = remember(configuration.screenWidthDp) {
        configuration.screenWidthDp.let { screenWidth ->
            // if not enough even show 2 items in a row,
            //  set width to screen width,
            //  else set width to 200dp
            if(screenWidth < (itemWidth*2 + itemMargin*2)) {
                screenWidth
            }else {  // at least can include 2 items width with margin
                itemWidth
            }
        }.dp
    }

    Column(
        modifier = Modifier
            .padding(itemMargin.dp)
            .background(if(selected) MaterialTheme.colorScheme.primaryContainer else UIHelper.defaultCardColor())
            .combinedClickable(
                onLongClick = { onLongClick(item) },
            ) {
                onClick(item)
            }
            .width(width)

    ) {
        Column(
            modifier = Modifier.padding(5.dp)
        ) {
            ScrollableRow {
                Text(item.file.name, fontSize = MyStyleKt.Title.firstLineFontSize)
            }
            ScrollableRow {
                Text(item.file.path.ioPath, fontSize = MyStyleKt.Title.secondLineFontSize)
            }
        }

        MyHorizontalDivider()

        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(item.shortContent)
        }
    }
}
