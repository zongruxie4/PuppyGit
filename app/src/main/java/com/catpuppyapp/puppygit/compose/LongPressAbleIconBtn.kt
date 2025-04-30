package com.catpuppyapp.puppygit.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.showToast
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local

@Composable
fun LongPressAbleIconBtn(
    modifier: Modifier = Modifier,
    iconModifier:Modifier=Modifier,
    tooltipText: String,
    icon: ImageVector,
    iconContentDesc:String? = null,
    enabled:Boolean=true,
    iconColor:Color? = null,
    isInDarkTheme:Boolean = Theme.inDarkTheme,
    pressedCircleSize:Dp = 40.0.dp,
    onLongClick:(()->Unit)? = null,
    onClick: ()->Unit,
){
    val iconColor = iconColor?:LocalContentColor.current
    val activityContext = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val onLongClick:()->Unit = onLongClick ?:  {
        //空白，代表不想显示提示文案
        if(tooltipText.isNotEmpty()) {
            //震动反馈，显示tooltip提示
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showToast(AppModel.realAppContext, tooltipText, Toast.LENGTH_SHORT)
        }
    }

    LongPressAbleIconBtnToastVersion(context=activityContext, modifier=modifier,iconModifier=iconModifier,
        tooltipText=tooltipText,icon=icon,iconContentDesc=iconContentDesc,haptic=haptic,
        enabled=enabled, iconColor=iconColor, isInDarkTheme=isInDarkTheme, pressedCircleSize=pressedCircleSize,
        onLongClick=onLongClick, onClick=onClick
    )
}
//
    //没意义，废弃了
///**
// * 和另一个的区别在于这个获取文案和图标都是通过函数
// */
//@Composable
//fun LongPressAbleIconBtn(
//    modifier: Modifier = Modifier,
//    iconModifier:Modifier = Modifier,
//    tooltipText: ()->String,
//    icon: ()->ImageVector,
//    iconContentDesc:()->String? = {null},
//    enabled:()->Boolean={true},
//    iconColor:()->Color? = { null },
//    isInDarkTheme:()->Boolean = { Theme.inDarkTheme },
//    onClick: ()->Unit,
//){
//    val iconColor = iconColor()?:LocalContentColor.current
//    val appContext = AppModel.appContext
//    val haptic = AppModel.haptic
//
//    LongPressAbleIconBtnToastVersion(context=appContext, modifier=modifier,iconModifier=iconModifier,
//        tooltipText=tooltipText(),icon=icon(),iconContentDesc=iconContentDesc(),haptic=haptic,
//        enabled=enabled(), iconColor=iconColor, isInDarkTheme=isInDarkTheme(), onClick=onClick)
//}


////自己实现一个支持长按显示按钮功能的button
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun LongPressAbleIconBtnToastVersion(
    context: Context,
    modifier: Modifier = Modifier,
    iconModifier:Modifier=Modifier,
    tooltipText: String,
    icon: ImageVector,
    iconContentDesc:String?=null,
    haptic: HapticFeedback = LocalHapticFeedback.current,
    enabled:Boolean=true,
    iconColor: Color = LocalContentColor.current,
    isInDarkTheme:Boolean = Theme.inDarkTheme,
    pressedCircleSize: Dp,
    onLongClick:()->Unit,
    onClick: ()->Unit,
) {

    Box(
        modifier = modifier
            // inline的时候如果加这个最小可交互size，会带padding，间距会过大，不好
//            .minimumInteractiveComponentSize()

            //圆角
            .size(pressedCircleSize)  //参见 IconButton 的Box的size
            .background(color = Color.Transparent)
            .graphicsLayer {
                clip = true
                shape = CircleShape
            }
            //长按震动反馈，点按执行操作
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )

        ,
        contentAlignment = Alignment.Center
    ) {
//        CompositionLocalProvider( content = content)

            Icon(
                modifier=iconModifier,
                tint= if(enabled) iconColor else UIHelper.getDisableBtnColor(isInDarkTheme),
                imageVector = icon,
                contentDescription = iconContentDesc
            )

    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun LongPressAbleIconBtnNormal(
//    modifier: Modifier = Modifier,
//    iconModifier:Modifier=Modifier,
//    tooltipText: String?,
//    icon: ImageVector,
//    iconContentDesc:String,
//    enabled:Boolean=true,
//    onClick: ()->Unit,
//) {
//
//    IconButton(
//            modifier=modifier,
//            enabled = enabled,
//            onClick = onClick,
//        ){
//            Icon(
//                modifier=iconModifier,
//                imageVector = icon,
//                contentDescription = iconContentDesc
//            )
//    }
//}


/*  支持长按显示功能提示的tooltip，但是，有输入框聚焦bug，先弃用，以后jetpackcompose更新再试试
////自己实现一个支持长按显示按钮功能的button
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LongPressAbleIconBtn(
    context: Context,
    modifier: Modifier = Modifier,
    iconModifier:Modifier=Modifier,
    tooltipText: String?,
    icon: ImageVector,
    iconContentDesc:String,
    scope: CoroutineScope,
    haptic: HapticFeedback = LocalHapticFeedback.current,
    enabled:Boolean=true,
    onClick: ()->Unit,
) {

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            //圆角
            .size(40.0.dp)  //参见 IconButton 的Box的size
            .background(color = Color.Transparent)
            .graphicsLayer {
                clip = true
                shape = CircleShape
            }
            //长按震动反馈，点按执行操作
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = {
                    //震动反馈，显示tooltip提示
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )

        ,
        contentAlignment = Alignment.Center
    ) {
//        CompositionLocalProvider( content = content)
//
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                if(tooltipText!=null) {
                    PlainTooltip {
                        Text(tooltipText)
                    }
                }
            },
            state = rememberTooltipState()
        ) {
            Icon(
                modifier=iconModifier,
                tint= if(enabled) LocalContentColor.current else Color.LightGray,
                imageVector = icon,
                contentDescription = iconContentDesc
            )
        }
    }
}
*/