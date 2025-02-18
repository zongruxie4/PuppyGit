package com.catpuppyapp.puppygit.compose


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading


@Composable
fun <T> SelectedItemDialog(
    selectedItems:List<T>,
    formatter:(T)->String,
    switchItemSelected:(T)->Unit,  //用switch而不是单纯的remove是为了日后可实现撤销删除方便
    clearAll:()->Unit,
    closeDialog:()->Unit,
    onCancel:()->Unit = closeDialog
) {
    val clipboardManager = LocalClipboardManager.current
    val activityContext = LocalContext.current


    CopyableDialog2(
        title = stringResource(id = R.string.selected_str),
//            text = selectedItemsShortDetailsStr.value,
        requireShowTextCompose = true,
        textCompose = {
            ScrollableColumn {
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
        cancelCompose = {
            ScrollableRow {
                //清空按钮
                if(selectedItems.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            closeDialog()
                            clearAll()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.clear),
                            color = MyStyleKt.TextColor.danger(),
                        )
                    }
                }

                //关闭按钮
                TextButton(
                    onClick = onCancel
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        color = Color.Unspecified,
                    )
                }
            }
        },
        onCancel = onCancel,

        //仅有条目时才可复制
        okBtnEnabled = selectedItems.isNotEmpty(),
    ) {
        closeDialog()

        doJobThenOffLoading {
            val lb = Cons.lineBreak
            val sb = StringBuilder()
            selectedItems.forEach {
                sb.append(formatter(it)).append(lb)
            }

            clipboardManager.setText(AnnotatedString(sb.removeSuffix(lb).toString()))
            Msg.requireShow(activityContext.getString(R.string.copied))
        }
    }
}
