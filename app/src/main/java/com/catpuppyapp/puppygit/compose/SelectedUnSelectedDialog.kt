package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.state.CustomStateSaveable

@Composable
fun <T> SelectedUnSelectedDialog(
    title: String,
    loading:Boolean,
    selectedItemList:MutableList<T>,
    unSelectedItemList:MutableList<T>,
    filterKeyWord: CustomStateSaveable<TextFieldValue>,
    selectedItemFormatter:@Composable (T)->Unit,
    unselectedItemFormatter:@Composable (T)->Unit,
    cancel:()->Unit,
    save:()->Unit
){
    ConfirmDialog2(
        title = title,
        requireShowTextCompose = true,
        textCompose = {

        },
        onCancel = cancel,
        onOk = save
    )
}

@Composable
fun <T> SelectedUnSelectedList(
    loading:Boolean,
    selectedItemList:MutableList<T>,
    unSelectedItemList:MutableList<T>,
    filterKeyWord: CustomStateSaveable<TextFieldValue>,
    selectedItemFormatter:@Composable (T)->Unit,
    unselectedItemFormatter:@Composable (T)->Unit,
) {
    MySelectionContainer {
        if(loading) {
            LoadingText(stringResource(R.string.loading), PaddingValues(30.dp), enableScroll = false)
        }

        if(loading.not()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                    FilterTextField(
                        filterKeyWord,
                    )
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item {
                        SettingsTitle(stringResource(R.string.selected_str))
                    }

                    selectedItemList.forEach {
                        item {
                            selectedItemFormatter(it)
                        }
                    }

                    item {
                        SettingsTitle(stringResource(R.string.unselected))
                    }

                    unSelectedItemList.forEach {
                        item {
                            unselectedItemFormatter(it)
                        }
                    }
                }
            }
        }
    }
}

