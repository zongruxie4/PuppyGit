package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.functions.maybeIsGoodKeyword
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable

@Composable
fun <T> SelectedUnSelectedDialog(
    title:String,
    loading:Boolean,
    selectedTitleText:String,
    unselectedTitleText:String,
    selectedItemList:MutableList<T>,
    unselectedItemList:MutableList<T>,
    filterKeyWord: CustomStateSaveable<TextFieldValue>,
    selectedItemFormatter:@Composable (T)->Unit,
    unselectedItemFormatter:@Composable (T)->Unit,
    filterSelectedItemList: (keyword:String)->List<T>,
    filterUnselectedItemList: (keyword:String)->List<T>,
    cancel:()->Unit,
){
    ConfirmDialog3(
        title = title,
        requireShowTextCompose = true,
        textCompose = {
            SelectedUnSelectedList(
                loading = loading,
                selectedTitleText = selectedTitleText,
                unselectedTitleText = unselectedTitleText,
                selectedItemList = selectedItemList,
                unselectedItemList = unselectedItemList,
                filterKeyWord = filterKeyWord,
                selectedItemFormatter = selectedItemFormatter,
                unselectedItemFormatter = unselectedItemFormatter,
                filterSelectedItemList=filterSelectedItemList,
                filterUnselectedItemList=filterUnselectedItemList,
            )
        },
        cancelBtnText = stringResource(R.string.close),
        onCancel = cancel,
        showOk = false,
        onOk = {}
    )
}

@Composable
fun <T> SelectedUnSelectedList(
    loading:Boolean,
    selectedTitleText:String,
    unselectedTitleText:String,
    selectedItemList:MutableList<T>,
    unselectedItemList:MutableList<T>,
    filterKeyWord: CustomStateSaveable<TextFieldValue>,
    selectedItemFormatter:@Composable (T)->Unit,
    unselectedItemFormatter:@Composable (T)->Unit,

    filterSelectedItemList: (keyword:String)->List<T>,
    filterUnselectedItemList: (keyword:String)->List<T>,
) {
    MySelectionContainer {
        if(loading) {
            LoadingText(modifier = Modifier.height(30.dp).fillMaxWidth(), stringResource(R.string.loading))
        }

        if(loading.not()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                val keyWordIsEmpty = filterKeyWord.value.text.isEmpty()

                //注意：这个过滤没开协程，直接在渲染线程过滤的，目前这个组件用来过滤仓库，我估计用户不会克隆超过100个仓库，性能损耗很小，没必要开协程处理
                //普通的过滤，加不加清空无所谓，一按返回就清空了，但这个常驻显示，得加个清空按钮
                FilterTextField(
                    filterKeyWord = filterKeyWord,
                    trailingIconTooltipText = stringResource(R.string.clear),
                    trailingIcon = if(keyWordIsEmpty) null else Icons.Filled.Close,
                    trailingIconDesc = stringResource(R.string.clear),
                    trailingIconOnClick = { filterKeyWord.value = TextFieldValue("") }
                )

                Spacer(Modifier.height(10.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        SettingsTitle(selectedTitleText+"(${selectedItemList.size})")
                    }


                    //根据关键字过滤条目
                    val k = filterKeyWord.value.text.lowercase()  //关键字
                    val enableFilter = maybeIsGoodKeyword(k)
                    val filteredSelectedList = if(enableFilter){
                        filterSelectedItemList(k)
                    }else {
                        selectedItemList
                    }

                    val filteredUnselectedList = if(enableFilter){
                        filterUnselectedItemList(k)
                    }else {
                        unselectedItemList
                    }


                    if(filteredSelectedList.isEmpty()) {
                        item {
                            ItemListIsEmpty()
                        }
                    }else {
                        filteredSelectedList.forEach {
                            item {
                                selectedItemFormatter(it)
                            }
                        }
                    }

                    item {
                        SettingsTitle(unselectedTitleText+"(${unselectedItemList.size})")
                    }

                    if(filteredUnselectedList.isEmpty()) {
                        item { ItemListIsEmpty() }
                    }else {
                        filteredUnselectedList.forEach {
                            item {
                                unselectedItemFormatter(it)
                            }
                        }
                    }
                }
            }
        }
    }
}

