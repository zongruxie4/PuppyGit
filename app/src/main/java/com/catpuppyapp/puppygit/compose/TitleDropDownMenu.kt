package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> TitleDropDownMenu(
    dropDownMenuExpendState: MutableState<Boolean>,
    curSelectItem:T,
    contentDescription:String = stringResource(R.string.switch_item),
    menuItemFormatter:(T)->String,
    titleFirstLineFormatter:(T)->String,
    titleSecondLineFormatter:(T)->String,
    itemList: List<T>,
    onLongClick:(T)->Unit,
    itemClick: (T) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val closeDropDownMenu = {
        dropDownMenuExpendState.value = false
    }

    val switchDropDownMenuShowHide = {
        dropDownMenuExpendState.value = !dropDownMenuExpendState.value
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onLongClick = {  //长按显示仓库名和分支名
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    onLongClick(curSelectItem)
                }
            ) { // onClick
                switchDropDownMenuShowHide()  //切换下拉菜单显示隐藏
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(.8f)
                .align(Alignment.CenterStart)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                //限制下宽度，不然仓库名太长就看不到箭头按钮了，用户可能就不知道能点击仓库名切换仓库了
            ) {
                Text(
                    text = titleFirstLineFormatter(curSelectItem),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = MyStyleKt.Title.firstLineFontSize,
                )

            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
            ) {

                Text(
                    //  判断仓库是否处于detached，然后显示在这里(例如： "abc1234(detached)" )
                    // "main|StateT" or "main", eg, when merging show: "main|Merging", when 仓库状态正常时 show: "main"；如果是detached HEAD状态，则显示“提交号(Detached)|状态“，例如：abc2344(Detached) 或 abc2344(Detached)|Merging
                    text = titleSecondLineFormatter(curSelectItem),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = MyStyleKt.Title.secondLineFontSize,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(.2f)
                .align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = if (dropDownMenuExpendState.value) Icons.Filled.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowLeft,
                contentDescription = contentDescription,
            )
        }
    }
    DropdownMenu(
        expanded = dropDownMenuExpendState.value,
        onDismissRequest = { closeDropDownMenu() }
    ) {
        for (i in itemList.toList()) {
            //列出条目
            DropdownMenuItem(
                text = { Text(menuItemFormatter(i)) },
                onClick = {
                    itemClick(i)
                    closeDropDownMenu()
                }
            )
        }
    }
}
