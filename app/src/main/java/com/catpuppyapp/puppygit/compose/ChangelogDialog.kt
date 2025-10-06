package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.screen.content.homescreen.innerpage.donateLink
import com.catpuppyapp.puppygit.utils.ActivityUtil


private const val version = "1.1.4.3v119"

private val changelog = """
- support disable log
- 支持禁用日志
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


        cancelBtnText = stringResource(R.string.ok),
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
