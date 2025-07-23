package com.catpuppyapp.puppygit.compose

import android.text.util.Linkify
import androidx.annotation.FontRes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.markdown.MdUtil
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkDownContainer(
    content:String,
    modifier:Modifier = Modifier,
    basePathNoEndSlash:String = "",
    style: TextStyle = LocalTextStyle.current,
    @FontRes fontResource: Int? = null,
    onLinkClicked: (String) -> Boolean = {false},
) {
    val activityContext = LocalContext.current
    val inDarkTheme = Theme.inDarkTheme

    MarkdownText(
        modifier = modifier,
        markdown = content,
        fontResource = fontResource,
        linkifyMask = Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS,
        onLinkClicked = onLinkClicked,
        linkColor = MyStyleKt.ClickableText.getColor(),
        style = style,
        isTextSelectable = true,
        syntaxHighlightColor = if(inDarkTheme) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.surfaceDim,
        coilStore = MdUtil.getCoilStore(context = activityContext, basePathNoEndSlash = basePathNoEndSlash)
    )
}
