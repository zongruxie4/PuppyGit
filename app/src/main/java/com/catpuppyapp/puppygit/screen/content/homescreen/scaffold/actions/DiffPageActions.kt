package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.actions

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.LongPressAbleIconBtn
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.LineNum
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dev.detailsDiffTestPassed
import com.catpuppyapp.puppygit.dev.proFeatureEnabled
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.settings.SettingsUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Msg
import com.catpuppyapp.puppygit.utils.MyLog
import com.catpuppyapp.puppygit.utils.cache.Cache
import java.io.File

private const val TAG = "DiffPageActions"

@Composable
fun DiffPageActions(
    fromTo:String,
    changeType: String,
    refreshPage: () -> Unit,
    request:MutableState<String>,
    fileFullPath:String,
    requireBetterMatchingForCompare:MutableState<Boolean>,
    copyModeOn:MutableState<Boolean>,
    copyModeSwitchable:Boolean,
    showLineNum:MutableState<Boolean>,
    showOriginType:MutableState<Boolean>,
    adjustFontSizeModeOn:MutableState<Boolean>,
    adjustLineNumSizeModeOn:MutableState<Boolean>,
    groupDiffContentByLineNum:MutableState<Boolean>,
    enableSelectCompare:MutableState<Boolean>,
    matchByWords:MutableState<Boolean>,
) {

    val navController = AppModel.singleInstanceHolder.navController
    val appContext= LocalContext.current

    val fileChangeTypeIsModified = changeType == Cons.gitStatusModified

    val dropDownMenuExpendState = rememberSaveable { mutableStateOf(false) }

//    if (fileChangeTypeIsModified && UserUtil.isPro()
//        && (dev_EnableUnTestedFeature || detailsDiffTestPassed)
//    ){
//        LongPressAbleIconBtn(
//            tooltipText = stringResource(R.string.better_but_slow_compare),
//            icon = Icons.Filled.Compare,
//            iconContentDesc = stringResource(R.string.better_but_slow_compare),
//            iconColor = UIHelper.getIconEnableColorOrNull(requireBetterMatchingForCompare.value),
//            enabled = true,
//        ) {
//            requireBetterMatchingForCompare.value = !requireBetterMatchingForCompare.value
//
//            // show msg: "better but slow compare: ON/OFF"
////            Msg.requireShow(
////                appContext.getString(R.string.better_but_slow_compare)+": "
////                + (if(requireBetterMatchingForCompare.value) appContext.getString(R.string.on_str) else appContext.getString(R.string.off_str))
////            )
//
//            SettingsUtil.update {
//                it.diff.enableBetterButSlowCompare = requireBetterMatchingForCompare.value
//            }
//
//        }
//
//    }

    if(fromTo == Cons.gitDiffFileHistoryFromTreeToTree || fromTo == Cons.gitDiffFileHistoryFromTreeToLocal) {
        LongPressAbleIconBtn(
            tooltipText = stringResource(R.string.restore),
            icon =  Icons.Filled.Restore,
            iconContentDesc = stringResource(R.string.restore),
        ) {
            request.value = PageRequest.showRestoreDialog
        }

    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.refresh),
        icon = Icons.Filled.Refresh,
        iconContentDesc = stringResource(id = R.string.refresh),
    ) {
        refreshPage()
    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.open),
        icon = Icons.Filled.FileOpen,
        iconContentDesc = stringResource(id = R.string.open),
    ) label@{
        // go editor sub page
//        showToast(appContext,filePath)
        try {
            //如果文件不存在，提示然后返回
            if(!File(fileFullPath).exists()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.file_doesnt_exist))
                return@label
            }

            //跳转到SubEditor页面
            Cache.set(Cache.Key.subPageEditor_filePathKey, fileFullPath)
            val goToLine = LineNum.lastPosition
            val initMergeMode = "0"  //冲突条目无法进入diff页面，所以能预览diff定不是冲突条目，因此跳转到editor时应将mergemode初始化为假
            val initReadOnly = "0"  //diff页面不可能显示app内置目录下的文件，所以一率可编辑

            navController.navigate(Cons.nav_SubPageEditor + "/$goToLine"+"/$initMergeMode"+"/$initReadOnly")

        }catch (e:Exception) {
            Msg.requireShowLongDuration("err:"+e.localizedMessage)
            MyLog.e(TAG, "'Open' err:"+e.stackTraceToString())
        }

    }

    LongPressAbleIconBtn(
        tooltipText = stringResource(R.string.open_as),
        icon = Icons.AutoMirrored.Filled.OpenInNew,
        iconContentDesc = stringResource(id = R.string.open_as),
    ) label@{
        try {
            if(!File(fileFullPath).exists()) {
                Msg.requireShowLongDuration(appContext.getString(R.string.file_doesnt_exist))
                return@label
            }

            //显示OpenAs弹窗
            request.value = PageRequest.showOpenAsDialog

        }catch (e:Exception) {
            Msg.requireShowLongDuration("err:"+e.localizedMessage)
            MyLog.e(TAG, "'Open As' err:"+e.stackTraceToString())
        }
    }

    //menu icon
    LongPressAbleIconBtn(
        //这种需展开的菜单，禁用内部的选项即可
//        enabled = enableAction.value,

        tooltipText = stringResource(R.string.menu),
        icon = Icons.Filled.MoreVert,
        iconContentDesc = stringResource(R.string.menu),
        onClick = {
            //切换菜单展开状态
            dropDownMenuExpendState.value = !dropDownMenuExpendState.value
        }
    )

    // menu items
    DropdownMenu(
        offset = DpOffset(x=100.dp, y=8.dp),
        expanded = dropDownMenuExpendState.value,
        onDismissRequest = { dropDownMenuExpendState.value=false }
    ) {

        DropdownMenuItem(
            text = { Text(stringResource(R.string.font_size)) },
            onClick = {
                adjustFontSizeModeOn.value = true

                dropDownMenuExpendState.value = false
            }

        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.line_num_size)) },

            onClick = {
                adjustLineNumSizeModeOn.value = true

                dropDownMenuExpendState.value = false
            }

        )

        DropdownMenuItem(
            text = { Text(stringResource(R.string.show_line_num)) },
            trailingIcon = {
                Icon(
                    imageVector = if(showLineNum.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null
                )
            },
            onClick = {
                showLineNum.value = !showLineNum.value

                SettingsUtil.update {
                    it.diff.showLineNum = showLineNum.value
                }

                dropDownMenuExpendState.value = false
            }

        )


        DropdownMenuItem(
            text = { Text(stringResource(R.string.show_change_type)) },
            trailingIcon = {
                Icon(
                    imageVector = if(showOriginType.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null
                )
            },
            onClick = {
                showOriginType.value = !showOriginType.value

                SettingsUtil.update {
                    it.diff.showOriginType = showOriginType.value
                }

                dropDownMenuExpendState.value = false
            }

        )


        DropdownMenuItem(
            text = { Text(stringResource(R.string.group_by_line)) },
            trailingIcon = {
                Icon(
                    imageVector = if(groupDiffContentByLineNum.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null
                )
            },
            onClick = {
                groupDiffContentByLineNum.value = !groupDiffContentByLineNum.value

                SettingsUtil.update {
                    it.diff.groupDiffContentByLineNum = groupDiffContentByLineNum.value
                }

                dropDownMenuExpendState.value = false
            }
        )

        if (fileChangeTypeIsModified && proFeatureEnabled(detailsDiffTestPassed)){
            DropdownMenuItem(
                text = { Text(stringResource(R.string.better_compare)) },
                trailingIcon = {
                    Icon(
                        imageVector = if(requireBetterMatchingForCompare.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                        contentDescription = null
                    )
                },
                onClick = {
                    requireBetterMatchingForCompare.value = !requireBetterMatchingForCompare.value

                    SettingsUtil.update {
                        it.diff.enableBetterButSlowCompare = requireBetterMatchingForCompare.value
                    }

                    dropDownMenuExpendState.value = false
                }
            )

            DropdownMenuItem(
                text = { Text(stringResource(R.string.match_by_words)) },
                trailingIcon = {
                    Icon(
                        imageVector = if(matchByWords.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                        contentDescription = null
                    )
                },
                onClick = {
                    matchByWords.value = !matchByWords.value

                    SettingsUtil.update {
                        it.diff.matchByWords = matchByWords.value
                    }

                    dropDownMenuExpendState.value = false
                }
            )

        }

        DropdownMenuItem(
            enabled = copyModeSwitchable,
            text = { Text(stringResource(R.string.read_only)) },
            trailingIcon = {
                Icon(
                    imageVector = if(copyModeOn.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null
                )
            },
            onClick = {
                copyModeOn.value = !copyModeOn.value

                dropDownMenuExpendState.value = false
            }

        )

        DropdownMenuItem(
            enabled = fileChangeTypeIsModified,
            text = { Text(stringResource(R.string.select_compare)) },
            trailingIcon = {
                Icon(
                    imageVector = if(enableSelectCompare.value) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = null
                )
            },
            onClick = {
                enableSelectCompare.value = !enableSelectCompare.value

                SettingsUtil.update {
                    it.diff.enableSelectCompare = enableSelectCompare.value
                }

                dropDownMenuExpendState.value = false
            }

        )


    }
}

