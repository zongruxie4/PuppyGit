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
- upgrade dependencies: ligit2 to 1.9.2, openssl to 3.6.1
- optimize english translation (pr #125, thx @clach04)
- optimize code of `PullToRefreshBox` (pr #128, thx @msmt2018)
- align c libs to 16KB page size
- fix Automation service bug (issue #130)
- 升级依赖: ligit2 to 1.9.2, openssl to 3.6.1
- 优化英语翻译 (pr #125, thx @clach04)
- 优化 `PullToRefreshBox` 的代码 (pr #128, thx @msmt2018)
- 对齐c库为16KB页大小
- 修复自动化服务bug (issue #130)
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
