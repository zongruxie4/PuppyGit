package com.catpuppyapp.puppygit.compose

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.functions.navToFileChooser
import com.catpuppyapp.puppygit.screen.shared.FileChooserType
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.getStoragePermission


/**
 * App Internal File Chooser, no-depend system file chooser, also no saf support
 */
@Composable
fun InternalFileChooser(
    activityContext: Context,  //这个就得从外部传，在弹窗内获取的Context，无法转换为Activity
    path:MutableState<String>,
    chooserType: FileChooserType = FileChooserType.SINGLE_DIR,  //默认选dir，如果想选文件，可传对应类型
    pathTextFieldLabel:String=stringResource(R.string.path),
    pathTextFieldPlaceHolder:String=stringResource(R.string.eg_storage_emulate_0_repos),
) {
    Row(modifier = Modifier.padding(bottom = 15.dp).padding(horizontal = MyStyleKt.defaultHorizontalPadding)) {
        ClickableText (
            text = stringResource(R.string.please_grant_permission_before_you_add_a_storage_path),
            overflow = TextOverflow.Visible,
            fontWeight = FontWeight.Light,
            modifier = MyStyleKt.ClickableText.modifierNoPadding.clickable {
                val activity = activityContext as? Activity;

                // grant permission for read/write external storage
                if (activity == null) {
                    Msg.requireShowLongDuration(activityContext.getString(R.string.please_go_to_system_settings_allow_manage_storage))
                }else {
                    activity.getStoragePermission()
                }
            },
        )
    }

    TextField(
        modifier = Modifier.fillMaxWidth().padding(horizontal = MyStyleKt.defaultHorizontalPadding),
        value = path.value,
        maxLines = 6,
        onValueChange = {
            path.value = it
        },
        label = {
            Text(pathTextFieldLabel)
        },
        placeholder = {
            Text(pathTextFieldPlaceHolder)
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    navToFileChooser(chooserType)
                }

            ) {
                Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = stringResource(R.string.three_dots_icon_for_choose_folder))
            }
        }
    )

}
