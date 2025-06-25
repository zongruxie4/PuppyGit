package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme


@Composable
fun SettingsTitle(text:String){
    val inDarkTheme = Theme.inDarkTheme
    Row(modifier = Modifier
        .background(color = if (inDarkTheme) Color.DarkGray else Color.LightGray)
        .fillMaxWidth()
        .padding(start = 5.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
    }
}

@Composable
fun SettingsContent(onClick:(()->Unit)?=null, content:@Composable ()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(10.dp)
        ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        content()
    }

    MyHorizontalDivider()
}

@Composable
fun SettingsContentSelector(
    left:@Composable ColumnScope.() -> Unit,
    right:@Composable ColumnScope.() -> Unit,
) {
    SettingsContent {
        Box {
            Column(modifier = MyStyleKt.SettingsItem.selectorLeftBaseModifier.align(Alignment.CenterStart)) {
                left()
            }


            Column(modifier = MyStyleKt.SettingsItem.selectorRightBaseModifier.align(Alignment.CenterEnd)) {
                right()
            }
        }
    }
}

@Composable
fun SettingsContentSwitcher(
    left:@Composable ColumnScope.() -> Unit,
    right:@Composable ColumnScope.() -> Unit,
    onClick: (() -> Unit)? = null,
) {
    SettingsContent(onClick) {
        Box {
            Column(modifier = MyStyleKt.SettingsItem.switcherLeftBaseModifier.align(Alignment.CenterStart)) {
                left()
            }


            Column(modifier = MyStyleKt.SettingsItem.switcherRightBaseModifier.align(Alignment.CenterEnd)) {
                right()
            }
        }
    }
}
