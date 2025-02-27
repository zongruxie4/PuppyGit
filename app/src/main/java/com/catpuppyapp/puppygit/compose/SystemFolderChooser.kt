package com.catpuppyapp.puppygit.compose

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.saf.SafUtil

private const val TAG = "SystemFolderChooser"

/**
 * A Folder Chooser depend System File Chooser, may not work if system removed internal file picker, in that case, can input path instead
 */
@Composable
fun SystemFolderChooser(
    safEnabled:MutableState<Boolean>,
    safPath:MutableState<String>,
    nonSafPath:MutableState<String>,
    path:MutableState<String>,
    pathTextFieldLabel:String=stringResource(R.string.path),
    pathTextFieldPlaceHolder:String=stringResource(R.string.eg_storage_emulate_0_repos),
    chosenPathCallback:(uri: Uri?)->Unit = {uri->
        if(uri != null) {
            if(AppModel.devModeOn) {
                //example of output: uri.toString() == uri.path: false, uri.toString()=content://com.android.externalstorage.documents/tree/primary%3ARepos, uri.path=/tree/primary:Repos
                // toString更完整些，是我期望的safPath，uri.path缺乏一些信息，不行
                MyLog.d(TAG, "uri.toString() == uri.path: ${uri.toString() == uri.path}, uri.toString()=${uri.toString()}, uri.path=${uri.path}")
            }

            safPath.value = SafUtil.toAppSpecifiedSafFormat(uri.toString())

            //最初是检查realPath.isNotBlank()才调用回调，但感觉检查与否意义不大，如果路径真为空，就清空也没什么
            nonSafPath.value = FsUtils.getRealPathFromUri(uri)

            path.value = if(safEnabled.value) safPath.value else nonSafPath.value

            MyLog.d(TAG, "#chooseDirLauncher: uri.toString()=${uri.toString()}, uri.path=${uri.path}, safEnabled=${safEnabled.value}, safPath=${safPath.value}, nonSafPath=${nonSafPath.value}")
        }
    }
) {

    val activityContext = LocalContext.current

    val chooseDirLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if(uri == null) {
            Msg.requireShowLongDuration("invalid uri")
        }else {
            //获取永久访问权限
            activityContext.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            //更新path
            chosenPathCallback(uri)
        }
    }


    //这里不需要能滚动，应该由使用此组件的组件考虑是否能滚动
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(.8f),
                value = path.value,
                maxLines = 6,
                onValueChange = {
                    path.value = it.trim('\n').trimEnd('/')
                },
                label = {
                    Text(pathTextFieldLabel)
                },
                placeholder = {
                    Text(pathTextFieldPlaceHolder)
                }
            )

            IconButton(
                onClick = {
                    //show folder chooser
                    chooseDirLauncher.launch(null)
                }

            ) {
                Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = stringResource(R.string.three_dots_icon_for_choose_folder))
            }

        }


        Spacer(Modifier.height(15.dp))
        Text(stringResource(R.string.if_unable_choose_a_path_just_copy_paste_instead), fontWeight = FontWeight.Light)

        Spacer(Modifier.height(15.dp))
        MyCheckBox(text = stringResource(R.string.saf_mode), value = safEnabled, onValueChange = { newValue ->
            path.value = if (newValue) {
                safPath.value
            } else {
                nonSafPath.value
            }

            safEnabled.value = newValue
        })
        CheckBoxNoteText(stringResource(R.string.saf_mode_note))
    }
}
