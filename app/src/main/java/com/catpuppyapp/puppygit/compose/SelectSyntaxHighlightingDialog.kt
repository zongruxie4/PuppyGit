package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.syntaxhighlight.PLScope
import com.catpuppyapp.puppygit.utils.UIHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val itemList = PLScope.SCOPES_NO_AUTO

@Composable
fun SelectSyntaxHighlightingDialog(
    plScope: PLScope,
    onCancel: () -> Unit,
    onOK: (selectedScope: PLScope) -> Unit,
) {

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    PlainDialog(onClose = onCancel) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 10.dp),
            state = listState
        ) {
            itemList.forEachIndexed { idx, it ->
                item {
                    SingleSelectionItem(
                        idx = idx,
                        item = it,
                        selected = plScope == it,
                        minHeight = 60.dp,
                        text = { idx, it -> it.name },
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
            val indexOf = itemList.indexOf(plScope)
            if(indexOf != -1) {
                UIHelper.scrollToItem(scope, listState, indexOf-3, animation = true)
            }
        }
    }
}
