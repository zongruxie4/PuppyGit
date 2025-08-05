package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.syntaxhighlight.base.PLScope
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

    PlainDialog(
        onClose = onCancel
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MyStyleKt.defaultItemPadding)
            ,
        ) {
            LazyColumn(
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
