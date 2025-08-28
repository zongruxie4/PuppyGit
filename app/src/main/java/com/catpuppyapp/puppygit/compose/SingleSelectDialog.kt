package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.utils.UIHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T> SingleSelectDialog(
    currentItem: T?,
    itemList: List<T>,
    text: (T) -> String,
    onCancel: () -> Unit,
    onOK: (selectedItem: T) -> Unit,
) {

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    PlainDialogWithPadding(onClose = onCancel) {
        LazyColumn(state = listState) {
            itemList.forEachIndexed { idx, it ->
                item {
                    SingleSelectionItem(
                        idx = idx,
                        item = it,
                        selected = currentItem == it,
                        minHeight = 60.dp,
                        text = { idx, it -> text(it) },
                        onClick = { idx, it ->
                            onCancel()
                            onOK(it)
                        },
                    )
                }
            }
        }
    }


    LaunchedEffect(Unit) {
        scope.launch {
            delay(200)
            val indexOf = itemList.indexOf(currentItem)
            if(indexOf != -1) {
                UIHelper.scrollToItem(scope, listState, indexOf-3, animation = true)
            }
        }
    }
}
