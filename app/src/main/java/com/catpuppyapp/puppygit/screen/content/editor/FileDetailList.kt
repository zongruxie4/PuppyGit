package com.catpuppyapp.puppygit.screen.content.editor

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.compose.FileDetailItem
import com.catpuppyapp.puppygit.dto.FileDetail

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileDetailList(
    contentPadding: PaddingValues,
    isSubEditor:Boolean,
    list:List<FileDetail>,
    reloadList:()->Unit,
) {
    FlowRow (
        modifier = Modifier.padding(contentPadding)
    ) {
        list.forEach {
            FileDetailItem(
                item = it,
                onLongClick = {

                },
                onClick = {

                }
            )
        }
    }
}
