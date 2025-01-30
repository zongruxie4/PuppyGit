package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.utils.UIHelper


/**
 * 默认双击标题回到列表顶部的title
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollableTitle(
    text:String,
    listState: ScrollState
) {
    val scope = rememberCoroutineScope()

    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.combinedClickable(onDoubleClick = {
            // double click go to top
            UIHelper.scrollTo(scope, listState, 0)
        }) {  }
    )
}

/**
 * 默认双击标题回到列表顶部的title
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScrollableTitle(
    text:String,
    listState: LazyListState
) {
    val scope = rememberCoroutineScope()

    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.combinedClickable(onDoubleClick = {
            // double click go to top
            UIHelper.scrollToItem(scope, listState, 0)
        }) {  }
    )
}
