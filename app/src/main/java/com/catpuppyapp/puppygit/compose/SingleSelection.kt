package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * 左边一个圆圈的那种单选列表，适合条目不太多的选项，条目太多建议用dropdown list
 */
@Composable
fun <T> SingleSelection(
    itemList:List<T>,
    selected:(idx:Int, T)->Boolean,
    text:(idx:Int, T)->String,
    onClick:(idx:Int, T)->Unit,
) {
    for ((idx, item) in itemList.withIndex()) {
        val selected = selected(idx, item)

        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min=40.dp)
                .selectable(
                    selected = selected,
                    onClick = {
                        onClick(idx, item)
                    },
                    role = Role.RadioButton
                )
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null // null recommended for accessibility with screenreaders
            )
            ScrollableRow {
                Text(
                    text = text(idx, item),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }

}
