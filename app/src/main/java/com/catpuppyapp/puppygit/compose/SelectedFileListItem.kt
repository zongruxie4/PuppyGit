package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt

private val trailIconSize = MyStyleKt.trailIconSize

@Composable
fun SelectedFileListItem(
    list: List<FileItemDto>,
    removeItem: (FileItemDto)->Unit,
    goToParentAndScrollToItem: (FileItemDto)->Unit,
    clearAll:()->Unit,
    closeDialog:()->Unit,
    textFormatterForCopy:(FileItemDto)->String,
) {

    SelectedItemDialog3(
        selectedItems = list,
        text = {},
        customText = {
            val splitSpacerWidth = MyStyleKt.trailIconSplitSpacerWidth

            TwoLineTextsAndIcons(
                text1 = it.name,
                text2 = it.fullPath,
                trailIconWidth = trailIconSize * 2 + splitSpacerWidth
            ) { containerModifier ->
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
                        imageVector = if(it.isDir) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = if(it.isDir) stringResource(R.string.folder) else stringResource(R.string.file)
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
        },
        customTrailIcon = {},
        textFormatterForCopy = textFormatterForCopy,
        switchItemSelected = removeItem,
        clearAll = clearAll,
        closeDialog = closeDialog,
    )

}
