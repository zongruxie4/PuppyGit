package com.catpuppyapp.puppygit.screen.content.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
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
    openFile:(FileDetail)->Unit,
) {
    FlowRow (
        modifier = Modifier
            .padding(contentPadding)
        ,

        overflow = FlowRowOverflow.Visible
    ) {
        list.forEach {
            FileDetailItem(
                item = it,
                onLongClick = {

                },
                onClick = {
                    openFile(it)
                }
            )
        }
    }
}
