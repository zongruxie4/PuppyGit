package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.donateLink
import com.catpuppyapp.puppygit.utils.ActivityUtil


private const val version = "1.1.4.2v118"

private val changelog = """
    - Make text of dialog copyable 
    - Add changelog dialog
    - 使弹窗文字可拷贝
    - 添加更新日志弹窗
""".trimIndent()


private const val donateText = "❤ Donate to support develop! ❤"


@Composable
fun ChangelogDialog(
    onClose: () -> Unit,
) {
    val activityContext = LocalContext.current

    CopyableDialog2(
        // hide ok btn
        okCompose = {},
        onOk = {},


        onCancel = onClose,

        title = version,
        requireShowTextCompose = true,
        textCompose = {
            ScrollableColumn {
                MultiLineClickableText(donateText) {
                    ActivityUtil.openUrl(activityContext, donateLink)
                }

                Spacer(Modifier.height(15.dp))

                Text(changelog)
            }
        }
    )
}
