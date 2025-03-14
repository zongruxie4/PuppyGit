package com.catpuppyapp.puppygit.compose

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.markdown.MdUtil
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkDownContainer(
    modifier:Modifier = Modifier,
    content:String,
    basePathNoEndSlash:String,
    fontSize:Int,
) {
    val activityContext = LocalContext.current
    val inDarkTheme = Theme.inDarkTheme

    MarkdownText(
        modifier = modifier,
        markdown = content,
        linkColor = MyStyleKt.ClickableText.color,
        style = LocalTextStyle.current.copy(fontSize = fontSize.sp),
        isTextSelectable = true,
        syntaxHighlightColor = if(inDarkTheme) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.surfaceDim,
        coilStore = MdUtil.getCoilStore(context = activityContext, basePathNoEndSlash = basePathNoEndSlash)
    )
}
