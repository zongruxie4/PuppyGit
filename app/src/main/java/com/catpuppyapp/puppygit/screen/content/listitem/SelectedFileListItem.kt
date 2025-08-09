package com.catpuppyapp.puppygit.screen.content.listitem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.compose.SelectedItemDialog3
import com.catpuppyapp.puppygit.compose.SizeIcon
import com.catpuppyapp.puppygit.compose.TwoLineTextsAndIcons
import com.catpuppyapp.puppygit.dto.MyFileItem
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt

private val trailIconSize = MyStyleKt.trailIconSize

@Composable
fun SelectedFileListItem(
    list: List<MyFileItem>,
    removeItem: (MyFileItem)->Unit,
    goToParentAndScrollToItem: (MyFileItem)->Unit,
    clearAll:()->Unit,
    closeDialog:()->Unit,
    textFormatterForCopy:(MyFileItem)->String = { it.itemName() + "\n" + it.itemPath() + "\n\n" },
) {

    SelectedItemDialog3(
        selectedItems = list,
        text = {},
        customText = {
            val splitSpacerWidth = MyStyleKt.trailIconSplitSpacerWidth

            TwoLineTextsAndIcons(
                text1 = it.itemName(),
                text2 = it.itemPath(),
                trailIconWidth = trailIconSize * 2 + splitSpacerWidth,
                trailIcons = { containerModifier ->
                    Row(
                        modifier = containerModifier
                            .fillMaxWidth()
                        ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {

                        SizeIcon(
                            size = trailIconSize,
                            modifier = Modifier.clickable {
                                goToParentAndScrollToItem(it)
                            },
                            imageVector = if(it.itemIsDir()) Icons.Outlined.Folder else Icons.AutoMirrored.Outlined.InsertDriveFile,
                            contentDescription = if(it.itemIsDir()) stringResource(R.string.folder) else stringResource(R.string.file)
                        )

                        Spacer(modifier = Modifier.width(splitSpacerWidth))

                        SizeIcon(
                            size = trailIconSize,
                            modifier = Modifier.clickable {
                                removeItem(it)
                            },
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            )
        },
        customTrailIcon = {},
        textFormatterForCopy = textFormatterForCopy,
        switchItemSelected = removeItem,
        clearAll = clearAll,
        closeDialog = closeDialog,
    )

}
