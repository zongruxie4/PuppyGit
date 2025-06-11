package com.catpuppyapp.puppygit.screen.content.editor

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.FileDetailItem
import com.catpuppyapp.puppygit.dto.FileDetail
import com.catpuppyapp.puppygit.utils.AppModel


private const val itemWidth = 150
private const val itemMargin = 10
private val itemMarginDp = itemMargin.dp
// 只考虑左右需要的水平外边距
private const val oneItemRequiredMargin = itemMargin*2
private const val oneItemRequiredWidth = (itemWidth + oneItemRequiredMargin)



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

    val configuration = AppModel.getCurActivityConfig()
    val screenWidthDp = configuration.screenWidthDp

    // calculate item width and counts in each row
    val (width, maxItemsInEachRow) = remember(configuration.screenWidthDp) {
        val width = if(screenWidthDp < oneItemRequiredWidth) {
            (screenWidthDp - oneItemRequiredMargin).coerceAtLeast(screenWidthDp)
        }else {  // at least can include 2 items width with margin
            itemWidth
        }

        val actuallyOneItemWidthAndMargin = width + oneItemRequiredMargin

        val maxItemsInEachRow = screenWidthDp / actuallyOneItemWidthAndMargin

        Pair(width.dp, maxItemsInEachRow)
    }


    LazyVerticalStaggeredGrid (
        contentPadding = contentPadding,
        columns = StaggeredGridCells.Adaptive(minSize = width)
    ) {
        list.forEachIndexed { idx, it ->
            item {
                FileDetailItem(
                    width = width,
                    margin = itemMarginDp,
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
