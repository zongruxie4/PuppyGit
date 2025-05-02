package com.catpuppyapp.puppygit.style

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.dto.DeviceWidthHeight
import com.catpuppyapp.puppygit.ui.theme.Theme

object MyStyleKt{
    val defaultHorizontalPadding = 10.dp
    val defaultIconSize = 40.dp

    object ClickableText{
        @Composable
        fun getStyle() = LocalTextStyle.current;  // 默认
//        fun getStyle() = TextStyle(textDecoration = TextDecoration.Underline);  // 加下划线

        val color = Color(0xFF0096FF)
        val errColor = Color(0xFFFF5733)
        val minClickableSize = 25.dp
//        val modifier = Modifier.padding(start = 1.dp,top=15.dp, bottom = 0.dp, end=1.dp)
        //defaultMinSize为了确保基本的可点击范围，避免分支名字很小点不到的情况发生
        val modifier = Modifier.padding(start = 3.dp,top=0.dp, bottom = 0.dp, end=1.dp).defaultMinSize(minClickableSize)
        val modifierNoPadding = Modifier.defaultMinSize(minClickableSize)
//        val fontSize = 15.sp
        val textAlign = TextAlign.Center
    }
    object NormalText{
//        val modifier = Modifier.padding(start = 1.dp,top=15.dp, bottom = 0.dp, end=1.dp)
        //defaultMinSize为了确保基本的可点击范围，避免分支名字很小点不到的情况发生
        val modifier = Modifier.padding(start = 3.dp,top=0.dp, bottom = 0.dp, end=1.dp).defaultMinSize(25.dp)
//        val fontSize = 15.sp
    }

    object ChangeListItemColor {
        /**
         * item change type color
         */
        val changeTypeAdded = Color(0xFF117C21)
        val changeTypeAdded_darkTheme = Color(0xFF78ab78)
        val changeTypeModified = Color(0xFF2B6FC2)
        val changeTypeModified_darkTheme = Color(0xFF5A9AD9)
        val changeTypeDeleted = Color(0xFF5E5E5E)
        val changeTypeDeleted_darkTheme = Color(0xFF8A8A8A)
        val changeTypeConflict = Color(0xFFBE4040)
        val changeTypeConflict_darkTheme = Color(0xFFBE4040)

        /**
         * DiffScreen background color
         */
        val added = Color(0xFF78ab78)
        val added_darkTheme = Color(0xFF204820)
        val modified = Color(0xFF5A9AD9)
        val modified_darkTheme = Color(0xFF183653)
        val deleted = Color(0xFF8A8A8A)
        val deleted_darkTheme = Color(0xFF4F4F4F)
        val conflict = Color(0xFFBE4040)
        val conflict_darkTheme = Color(0xFF692525)
//        val conflict = Color(0xFF913FA8)
//        val conflict_darkTheme = Color(0xFF621E75)

    }

    object IconColor {
        val enable = Color(0xFF0F479B)
        val disable = Color.LightGray
        val disable_DarkTheme = Color(0xFF505050)
        val normal = Color(0xFF5F5F5F)
    }

    object TextColor {
        val enable = Color.Unspecified
        val disable = Color.LightGray
        val disable_DarkTheme = Color(0xFF505050)
        val highlighting_green =Color(0xFF1FAB26)

        //Editor font color
        val lineNum_forEditorInLightTheme = Color.DarkGray
        val lineNum_forEditorInDarkTheme = Color(0xFF535353)

        //DiffContent font color
        val lineNum_forDiffInLightTheme = Color.DarkGray
        val lineNum_forDiffInDarkTheme = Color(0xFF757575)

        val fontColor = Color.Unspecified
        val darkThemeFontColor = Color.Gray

        fun error() = if(Theme.inDarkTheme) Color(0xFFF53737) else Color.Red.copy(alpha = .8f)
//        fun error() = Color.Red.copy(alpha = .8f)

        fun danger() = error()
    }

    object TextSize {
        val default = 16.sp
        val lineNumSize = 10.sp
    }

    object Editor {
//        val fontSize = 16.sp
//        val bottomBarHeight = 80.dp  //这是以前你个底栏高度，后来改用和其他页面一样的了 由 BottomBar.height 控制

        //高亮文本的背景颜色
        val highlightingBgColor = Color(0xFFFFEB3B)
    }

    //只能在compose下获取这个颜色
//    bgColor = MaterialTheme.colorScheme.primaryContainer,  //标题栏背景色
//    titleColor = MaterialTheme.colorScheme.primary,  //标题栏文字颜色

    object BottomBar{
        val height=60.dp

        //使用BottomBar的那个页面，需要padding出这个高度，否则列表内容会被BottomBar盖住
        val outsideContentPadding = height+20.dp
    }


    object Fab {
        fun getFabModifierForEditor(isMultipleSelectionMode:Boolean, isPortrait:Boolean):Modifier {
            return addNavPaddingIfNeed(isPortrait, Modifier.imePadding().then(if(isMultipleSelectionMode) Modifier.padding(bottom = BottomBar.height) else Modifier))
        }

        fun getFabModifier(isPortrait:Boolean, deviceWidthHeight: DeviceWidthHeight):Modifier {
            //貌似Fab自带一点Padding，所以这里直接用BottomBar的高度即可，不需要再额外加padding
            // end 20dp 是为了避免浮动按钮盖住条目的三个点菜单按钮（例如Files页面，每个条目后面都有个3点菜单）
            return addNavPaddingIfNeed(isPortrait, Modifier.padding(bottom = BottomBar.height, end = (deviceWidthHeight.width * 0.1f).dp))
        }

        //若是横屏，添加导航栏padding，不然浮动按钮会被导航栏盖住
        private fun addNavPaddingIfNeed(isPortrait:Boolean, modifier:Modifier = Modifier):Modifier {
            return if(isPortrait) {
                modifier
            }else {
                modifier.navigationBarsPadding()
            }
        }
    }


    object RadioOptions{
        val minHeight=30.dp
        val middleHeight=35.dp
    }

    object Title {
        val lineHeight = 20.dp

        val firstLineFontSize = 18.sp
        val firstLineFontSizeSmall = 15.sp

        //标题大字下面那行小字的字体大小
        val secondLineFontSize = 12.sp

        //可点击title的最小尺寸
        //注：应放到点击函数之后，不然虽然会变宽但多出的部分点击无效
        val clickableTitleMinWidth = 40.dp
//        val clickableTitleMinWidth = 50.dp
    }

    object SettingsItem {

        val itemFontSize = 20.sp
        val itemDescFontSize = 15.sp
        val switcherIconSize = 60.dp
        val selectorWidth = MyStyleKt.DropDownMenu.minWidth.dp

        val itemLeftWidthForSwitcher = .8f
        val itemLeftWidthForSelector = .6f

    }

    object Padding {
        val PageBottom=500.dp
    }

    object TextSelectionColor {
        val customTextSelectionColors = TextSelectionColors(
            handleColor = Color(0xff4b6cc6),  //光标拖手颜色
            backgroundColor = Color(0xFF6A86D1),  //选中文本背景颜色
        )
        val customTextSelectionColors_darkMode = TextSelectionColors(
            handleColor = Color(0xFF1F3368),  //光标拖手颜色
            backgroundColor = Color(0xFF23376F),  //选中文本背景颜色
        )

        //隐藏光标拖手（拖柄）。注意：只是透明了，但实际仍存在且可点击和拖动
        val customTextSelectionColors_hideCursorHandle = TextSelectionColors(
            handleColor = Color.Transparent,  //光标拖手颜色
            backgroundColor = Color.Transparent,  //选中文本背景颜色
        )
    }

    object Icon {
        val size = 25.dp
        val modifier = Modifier.size(size)
    }


    object CheckoutBox {
//        val height = 56.dp
        val height = 40.dp

    }


    object TopBar {
        val dropDownMenuTopPaddingSize = 70.dp
    }

    object BottomSheet{
        val skipPartiallyExpanded = true
    }

     object DropDownMenu {
         // unit is dp, but for more flexibility
         // 单位是dp，但为了灵活性，这里就不写dp了，只写数字
         const val minWidth = 160

         val selectedItemColor = TextColor.highlighting_green
    }

}
