package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R

/**
 * 注：这个组件应该放到Scaffold里，不然背景色无法随系统主题变化(例如开了dark theme，这个背景色还是全白，会很刺眼)
 */
@Composable
fun LoadingText(
    text: String = stringResource(R.string.loading),
    contentPadding: PaddingValues,
    enableScroll: Boolean = true,
    scrollState: ScrollState = rememberScrollState(),
) {
    LoadingText(
        text = text,
        contentPadding = contentPadding,
        enableScroll = enableScroll,
        scrollState = scrollState,
        appendContent = null
    )
}

@Composable
fun LoadingText(
    text: String = stringResource(R.string.loading),
    contentPadding: PaddingValues,
    enableScroll: Boolean = true,
    scrollState: ScrollState = rememberScrollState(),
    appendContent:(@Composable ()->Unit)? = null,
) {
    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()

            //默认启用滚动，不然滚动 隐藏/显示 的顶栏无法触发 隐藏/显示
            .then(if (enableScroll) Modifier.verticalScroll(scrollState) else Modifier)
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text)
        appendContent?.invoke()  //这个代码亲测可用，和使用if判断非空然后再调用lambda一样
    }
}

@Composable
fun LoadingText(
    modifier: Modifier,
    text:String= stringResource(R.string.loading),
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = text)
    }
}

@Composable
fun LoadingText(
    text: String = stringResource(R.string.loading),
    contentPadding: PaddingValues,
    enableScroll: Boolean = true,
    scrollState: ScrollState = rememberScrollState(),

    showCancel:Boolean = false,
    cancelText:String = stringResource(R.string.cancel),
    onCancel:(()->Unit)? = null,  // 只有当showCancel为真时，才有可能被调用
) {
    LoadingText(
        text = text,
        contentPadding = contentPadding,
        enableScroll = enableScroll,
        scrollState = scrollState,
        //如果运行的是可取消的任务，显示个取消
        appendContent = if(showCancel) {
            {
                Spacer(Modifier.height(10.dp))
                ClickableText(cancelText) {
                    onCancel?.invoke()
                }
            }
        }else {
            null
        }
    )
}
