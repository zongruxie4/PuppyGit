package com.catpuppyapp.puppygit.screen.shared

import androidx.compose.runtime.mutableStateOf
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.git.StatusTypeEntrySaver

object SharedState {
    var homeChangeList_itemList = mutableListOf<StatusTypeEntrySaver>()
    var homeChangeList_indexHasItem = mutableStateOf(false)

    val homeChangeList_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
    val index_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
    val treeToTree_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
    val fileHistory_LastClickedItemKey = mutableStateOf(Cons.init_last_clicked_item_key)
}
