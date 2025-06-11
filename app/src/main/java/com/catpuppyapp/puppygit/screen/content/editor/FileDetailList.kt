package com.catpuppyapp.puppygit.screen.content.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.compose.FileDetailItem
import com.catpuppyapp.puppygit.compose.FullScreenScrollableColumn
import com.catpuppyapp.puppygit.dto.FileDetail

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileDetailList(
    contentPadding: PaddingValues,
    isSubEditor:Boolean,
    list:List<FileDetail>,
    reloadList:()->Unit,
    onClick:(FileDetail)->Unit,
    itemOnLongClick:(idx:Int, FileDetail)->Unit,
    isItemSelected: (FileDetail) -> Boolean,
) {
    FullScreenScrollableColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
    ) {
        FlowRow (
            overflow = FlowRowOverflow.Visible
        ) {
            list.forEachIndexed {idx, it ->
                FileDetailItem(
                    idx = idx,
                    item = it,
                    onLongClick = itemOnLongClick,
                    onClick = onClick,
                    selected = isItemSelected(it)
                )
            }
        }
    }
}
