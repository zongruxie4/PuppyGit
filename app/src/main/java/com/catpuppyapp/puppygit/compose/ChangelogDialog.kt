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
import com.catpuppyapp.puppygit.utils.AppModel


private val changelog = """
- support call http api in sync way
- support auto dismiss automation service notification (pr #132, thx @jiesou)
- 支持以同步方式调用http api
- 支持自动忽略自动化服务的通知消息 (pr #132, thx @jiesou)
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

        title = AppModel.getAppVersionNameAndCode(),
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
