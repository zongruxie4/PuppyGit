package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.donateLink
import com.catpuppyapp.puppygit.style.MyStyleKt


private val changelog = """
    1.1.4.2v118:
    - Make text of dialog copyable 
    - Add changelog dialog
    - 使弹窗文字可拷贝
    - 添加更新日志弹窗
""".trimIndent()

@Composable
fun ChangelogDialog(
    onClose: () -> Unit,
) {
    CopyableDialog2(
        // hide ok btn
        okCompose = {},
        onOk = {},


        onCancel = onClose,

        title = "Changelog",
        requireShowTextCompose = true,
        textCompose = {
            ScrollableColumn {
                Text(
                    text = buildAnnotatedString {
                        withLink(LinkAnnotation.Url(donateLink)) {
                            withStyle(style = SpanStyle(fontWeight = MyStyleKt.TextItem.defaultFontWeight(), color = MyStyleKt.ClickableText.getColor(), fontSize = MyStyleKt.ClickableText.fontSize)) {
                                append("❤ Donate to support develop! ❤")
                            }
                        }
                    }
                )

                Spacer(Modifier.height(15.dp))

                Text(changelog)
            }
        }
    )
}
