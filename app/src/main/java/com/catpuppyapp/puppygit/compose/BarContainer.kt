package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.dto.MenuIconBtnItem
import com.catpuppyapp.puppygit.utils.UIHelper

@Composable
fun BarContainer(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    actions:List<MenuIconBtnItem>? = null,
    onClick: (()->Unit)? = null,
    content: @Composable ()->Unit,
) {
    Row(
        modifier = modifier
            .border(BorderStroke(2.dp, UIHelper.getDividerColor())).then(
                if(onClick != null) {
                    Modifier.clickable { onClick() }
                }else {
                    Modifier
                }
            )
            .background(MaterialTheme.colorScheme.surfaceDim)
            .fillMaxWidth()
            .padding(10.dp)
        ,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        content()

        /**
         *     val icon:ImageVector,
         *     val text:String,
         *     val desc:String, // for accessbility
         *     val enabled:()->Boolean,
         *     val visible:()->Boolean,
         *     val onClick:()->Unit,
         */
        if(actions != null && actions.isNotEmpty()) {
            ScrollableRow {
                for(a in actions) {
                    if(a.visible().not()) continue

                    LongPressAbleIconBtn(
                        iconModifier = modifier.size(a.size),
                        pressedCircleSize = a.pressedCircleSize,
                        icon = a.icon,
                        tooltipText = a.text,
                        iconContentDesc = a.desc,
                        enabled = a.enabled(),
                        onClick = a.onClick
                    )
                }
            }
        }
    }
}
