package com.catpuppyapp.puppygit.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.catpuppyapp.puppygit.utils.markdown.MdUtil
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun MarkDownContainer(
    modifier:Modifier = Modifier,
    content:String,
    basePathNoEndSlash:String,
) {
    val activityContext = LocalContext.current

    MarkdownText(
        modifier = modifier,
        markdown = content,
        coilStore = MdUtil.getCoilStore(context = activityContext, basePathNoEndSlash = basePathNoEndSlash)
    )
}
