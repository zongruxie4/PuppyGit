package com.catpuppyapp.puppygit.compose


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.Msg


@Composable
fun <T> SelectedItemDialog(
    detailStr:String,
    selectedItems:List<T>,
    formatter:(T)->String,
    switchItemSelected:(T)->Unit,  //用switch而不是单纯的remove是为了日后可实现撤销删除方便
    clearAll:()->Unit,
    closeDialog:()->Unit,
    onCancel:()->Unit = closeDialog
) {
    val clipboardManager = LocalClipboardManager.current
    val activityContext = LocalContext.current

    CopyableDialog(
        title = stringResource(id = R.string.selected_str),
//            text = selectedItemsShortDetailsStr.value,
        requireShowTextCompose = true,
        textCompose = {
            ScrollableColumn {
                Row(
                    modifier = Modifier.height(60.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    ClickableText(stringResource(R.string.clear)) {
                        closeDialog()
                        clearAll()
                    }
                }
                selectedItems.forEach {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(text = formatter(it), modifier = Modifier.fillMaxWidth(.8f).align(Alignment.CenterStart))

                        IconButton(
                            modifier = Modifier.fillMaxWidth(.2f).align(Alignment.CenterEnd),
                            onClick = { switchItemSelected(it) }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = stringResource(R.string.trash_bin_icon_for_delete_item)
                            )
                        }
                    }

                    HorizontalDivider()
                }

            }
        },
        onCancel = onCancel
    ) {
        closeDialog()
        clipboardManager.setText(AnnotatedString(detailStr))
        Msg.requireShow(activityContext.getString(R.string.copied))
    }
}
