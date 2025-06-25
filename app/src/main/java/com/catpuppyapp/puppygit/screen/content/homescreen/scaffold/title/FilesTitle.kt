package com.catpuppyapp.puppygit.screen.content.homescreen.scaffold.title

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.DropDownMenuItemText
import com.catpuppyapp.puppygit.compose.FilterTextField
import com.catpuppyapp.puppygit.compose.IconOfRepoState
import com.catpuppyapp.puppygit.compose.ScrollableRow
import com.catpuppyapp.puppygit.compose.SettingsTitle
import com.catpuppyapp.puppygit.compose.TitleDropDownMenu
import com.catpuppyapp.puppygit.constants.PageRequest
import com.catpuppyapp.puppygit.dto.FileItemDto
import com.catpuppyapp.puppygit.dto.NameAndPath
import com.catpuppyapp.puppygit.dto.NameAndPathList
import com.catpuppyapp.puppygit.dto.NameAndPathType
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.functions.defaultTitleDoubleClickRequest
import com.catpuppyapp.puppygit.screen.functions.getFilesScreenTitle
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.FsUtils
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.cache.Cache
import com.catpuppyapp.puppygit.utils.changeStateTriggerRefreshPage
import com.catpuppyapp.puppygit.utils.dbIntToBool
import com.catpuppyapp.puppygit.utils.doJobThenOffLoading
import com.catpuppyapp.puppygit.utils.replaceStringResList
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateListOf
import com.catpuppyapp.puppygit.utils.state.mutableCustomStateOf
import com.github.git24j.core.Repository
import kotlinx.coroutines.sync.Mutex
import java.io.File

private const val TAG = "FilesTitle"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesTitle(
    stateKeyTag: String,

    currentPath: ()->String,
    goToPath:(String)->Unit,
    allRepoParentDir: File,
    needRefreshFilesPage: MutableState<String>,
    filesPageGetFilterMode: ()->Int,
    filterKeyWord:CustomStateSaveable<TextFieldValue>,
    filterModeOn:()->Unit,
    doFilter:(String)->Unit,
    requestFromParent:MutableState<String>,
    filterKeywordFocusRequester: FocusRequester,
    filesPageSimpleFilterOn: Boolean,
    filesPageSimpleFilterKeyWord:CustomStateSaveable<TextFieldValue>,
    curPathItemDto: FileItemDto,
    searching:Boolean
) {
    val stateKeyTag = Cache.getComponentKey(stateKeyTag, TAG)

    val haptic = LocalHapticFeedback.current
    val activityContext = LocalContext.current
    val inDarkTheme = Theme.inDarkTheme

    if(filesPageSimpleFilterOn) {
        FilterTextField(filterKeyWord = filesPageSimpleFilterKeyWord, loading = searching)
    } else {  //filesPageGetFilterMode()==0 , 搜索模式关闭
        val dropDownMenuExpandState = rememberSaveable { mutableStateOf(false) }
        val storages = mutableCustomStateOf(stateKeyTag, "storages") { NameAndPathList() }

        val switchDropDownMenu = {
            if(dropDownMenuExpandState.value) {  // need close
                dropDownMenuExpandState.value = false
            } else {  // need open
                doJobThenOffLoading {
                    NameAndPath.getListForFilesManager(activityContext, storages.value.list, storages.value.updateLock)
                }

                dropDownMenuExpandState.value = true
            }
        }

        val enableAction = true
        val getTitleColor = {
            UIHelper.getTitleColor(enabled = enableAction)
        }

        TitleDropDownMenu(
            dropDownMenuExpandState = dropDownMenuExpandState,
            // never have any item be selected, it just need display the list
            curSelectedItem = NameAndPath(),
            itemList = storages.value.list,
            titleClickEnabled = true,
            switchDropDownMenuShowHide = switchDropDownMenu,
            titleFirstLine = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getFilesScreenTitle(currentPath(), activityContext),
//                    style=MyStyleKt.clickableText.style,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = MyStyleKt.Title.firstLineFontSize,
                        //如果是在合并（或者有冲突），仓库名变成红色，否则变成默认颜色
                        color = getTitleColor()
                    )
                }
            },
            titleSecondLine = {
                Text(
                    text = replaceStringResList(stringResource(R.string.folder_n_file_m), listOf(""+curPathItemDto.folderCount, ""+curPathItemDto.fileCount)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = MyStyleKt.Title.secondLineFontSize,
                    color = getTitleColor()
                )
            },
            titleRightIcon = {
                Icon(
                    imageVector = if (dropDownMenuExpandState.value) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowLeft,
                    contentDescription = null,
                    tint = if (enableAction) LocalContentColor.current else UIHelper.getDisableBtnColor(inDarkTheme)
                )
            },
            isItemSelected = { false },
            menuItem = { element, selected ->
                var basePadding = PaddingValues(10.dp)
                val firstItemBasePadding = PaddingValues(top = 26.dp, bottom = 10.dp, start = 10.dp, end = 10.dp)
                if(element.type == NameAndPathType.FIRST_APP_ACCESSIBLE_STORAGES) {
                    SettingsTitle(stringResource(R.string.storage))
                    basePadding = firstItemBasePadding
                } else if(element.type == NameAndPathType.FIRST_REPOS_STORAGE_PATH) {
                    SettingsTitle(stringResource(R.string.storage_paths))
                    basePadding = firstItemBasePadding
                }else if(element.type == NameAndPathType.FIRST_REPO_WORKDIR_PATH) {
                    SettingsTitle(stringResource(R.string.repos))
                    basePadding = firstItemBasePadding
                }

                DropDownMenuItemText(
                    text1 = element.name,

                    //仓库状态若不是NONE，则显示 (其实等于 仓库状态等于null的仓库并不会显示在这里，查询的时候就过滤掉了，不过为了逻辑完整，还是保留null判断
                    text2 = FsUtils.getPathWithInternalOrExternalPrefix(fullPath = element.path),
                    basePadding = basePadding
                )
            },
            titleOnLongClick = { requestFromParent.value = PageRequest.requireShowPathDetails },
            itemOnClick = {
                goToPath(it.path)
            },
        )

//        Column(
//            modifier = Modifier
//            .combinedClickable(
//                onDoubleClick = {
//                    defaultTitleDoubleClickRequest(requestFromParent)
//                },
//                onLongClick = {
////                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//
//                    //长按标题回到仓库根目录
//                    goToPath(allRepoParentDir.canonicalPath)
//                }
//            ) {  //onClick
//                requestFromParent.value = PageRequest.requireShowPathDetails
//            },
//        ) {
//            ScrollableRow {
//                Text(
//                    text = getFilesScreenTitle(currentPath(), activityContext),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    fontSize = MyStyleKt.Title.firstLineFontSize,
//                )
//            }
//
//            ScrollableRow {
//                Text(
//                    text = replaceStringResList(stringResource(R.string.folder_n_file_m), listOf(""+curPathItemDto.folderCount, ""+curPathItemDto.fileCount)),
//                    fontSize = MyStyleKt.Title.secondLineFontSize
//                )
//            }
//        }

    }

    // this code maybe invalid for now?
    LaunchedEffect(filesPageGetFilterMode()) {
        try {
            if(filesPageGetFilterMode() == 1) {
                filterKeywordFocusRequester.requestFocus()
            }

        }catch (_:Exception) {
            //顶多聚焦失败，没啥好记的
        }
    }
}
