package com.catpuppyapp.puppygit.screen.content.homescreen.innerpage

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.compose.AppIcon
import com.catpuppyapp.puppygit.compose.AppIconMonoChrome
import com.catpuppyapp.puppygit.compose.SpacerRow
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.ActivityUtil
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.ComposeHelper
import com.catpuppyapp.puppygit.utils.UIHelper
import com.catpuppyapp.puppygit.utils.baseVerticalScrollablePageModifier
import com.catpuppyapp.puppygit.utils.forEachBetter
import com.catpuppyapp.puppygit.utils.forEachIndexedBetter


const val authorMail = "luckyclover33xx@gmail.com"
const val authorMailLink = "mailto:$authorMail"

const val madeBy = "Made by Bandeapart1964 of catpuppyapp"
const val madeByLink = "https://github.com/Bandeapart1964"

const val sourceCodeLink = "https://github.com/catpuppyapp/PuppyGit"
const val privacyPolicyLink = "$sourceCodeLink/blob/main/PrivacyPolicy.md"
const val discussionLink = "$sourceCodeLink/discussions"
const val reportBugsLink = "$sourceCodeLink/issues/new"
const val donateLink = "$sourceCodeLink/blob/main/donate.md"
const val faqLink = "$sourceCodeLink/blob/main/FAQ.md"
const val httpServiceApiUrl = "$sourceCodeLink/blob/main/http_service_api.md"
const val automationDocUrl = "$sourceCodeLink/blob/main/automation_doc.md"


var versionCode: Int = AppModel.getAppVersionCode()
var versionName: String = AppModel.getAppVersionName()

private data class OpenSource(
    val projectName:String,
    val projectLink:String,
    val licenseLink:String,
)

private data class Contributor(
    val name:String,
    val link:String,
    val desc:String,
)

private data class Link(
    val title: String,
    val link: String,
)

private val openSourceList = listOf(
    OpenSource(projectName = "libgit2", projectLink = "https://github.com/libgit2/libgit2", licenseLink = "https://raw.githubusercontent.com/libgit2/libgit2/main/COPYING"),
    OpenSource(projectName = "git24j", projectLink = "https://github.com/git24j/git24j", licenseLink = "https://raw.githubusercontent.com/git24j/git24j/master/LICENSE"),
    OpenSource(projectName = "text-editor-compose", projectLink = "https://github.com/kaleidot725/text-editor-compose", licenseLink = "https://raw.githubusercontent.com/kaleidot725/text-editor-compose/main/LICENSE"),
    OpenSource(projectName = "OpenSSL", projectLink = "https://github.com/openssl/openssl", licenseLink = "https://raw.githubusercontent.com/openssl/openssl/master/LICENSE.txt"),
    OpenSource(projectName = "libssh2", projectLink = "https://github.com/libssh2/libssh2", licenseLink = "https://raw.githubusercontent.com/libssh2/libssh2/refs/heads/master/COPYING"),
    OpenSource(projectName = "compose-markdown", projectLink = "https://github.com/jeziellago/compose-markdown", licenseLink = "https://github.com/jeziellago/compose-markdown/blob/main/LICENSE"),
    OpenSource(projectName = "swipe", projectLink = "https://github.com/saket/swipe", licenseLink = "https://github.com/saket/swipe/blob/trunk/LICENSE.txt"),
    OpenSource(projectName = "sora-editor", projectLink = "https://github.com/Rosemoe/sora-editor", licenseLink = "https://github.com/Rosemoe/sora-editor/blob/main/LICENSE"),
)


private val contributorList = listOf(
    Contributor(name = "triksterr", link = "https://github.com/triksterr", desc = "Russian translator"),
    Contributor(name = "mikropsoft", link = "https://github.com/mikropsoft", desc = "Turkish translator"),
    Contributor(name = "Hussain96o", link = "https://github.com/Hussain96o", desc = "Arabic translator"),
    Contributor(name = "sebastien46", link = "https://github.com/sebastien46", desc = "Monochrome app icon"),
    Contributor(name = "kamilhussen24", link = "https://github.com/kamilhussen24", desc = "Bangla translator"),
)

@Composable
fun AboutInnerPage(
    listState:ScrollState,
    contentPadding: PaddingValues,
    openDrawer:() -> Unit,
){

    val activityContext = LocalContext.current
    val exitApp = AppModel.exitApp

    val links = listOf(
        Link(title = stringResource(R.string.source_code), link = sourceCodeLink),
        Link(title = stringResource(R.string.discussions), link = discussionLink),
        Link(title = stringResource(R.string.report_bugs), link = reportBugsLink),
        Link(title = stringResource(R.string.contact_author), link = authorMailLink),
        Link(title = "💖"+stringResource(R.string.donate)+"💖", link = donateLink),
        Link(title = stringResource(R.string.faq), link = faqLink),
        Link(title = stringResource(R.string.privacy_policy), link = privacyPolicyLink),
    )


    //back handler block start
    val isBackHandlerEnable = rememberSaveable { mutableStateOf(true) }
    val backHandlerOnBack = ComposeHelper.getDoubleClickBackHandler(context = activityContext, openDrawer = openDrawer, exitApp= exitApp)
    //注册BackHandler，拦截返回键，实现双击返回和返回上级目录
    BackHandler(enabled = isBackHandlerEnable.value, onBack = {backHandlerOnBack()})
    //back handler block end

    val appLogoEasterEggOn = rememberSaveable { mutableStateOf(false) }
    val appLogoEasterEggIconColor = remember { mutableStateOf(Color.Magenta) }

    val animatedIconColor by animateColorAsState(
        targetValue = if (appLogoEasterEggOn.value) appLogoEasterEggIconColor.value else MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "IconColorAnimation"
    )

    Column(
        modifier = Modifier
            .baseVerticalScrollablePageModifier(contentPadding, listState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        // App Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App Icon with Easter Egg
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            if(appLogoEasterEggOn.value) {
                                appLogoEasterEggIconColor.value = UIHelper.getRandomColor()
                            } else {
                                appLogoEasterEggOn.value = true
                            }
                        },
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if(appLogoEasterEggOn.value) {
                            AppIconMonoChrome(tint = animatedIconColor)
                        } else {
                            AppIcon()
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // App Name and Version
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$versionName ($versionCode)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(16.dp))

                // Made By
                TextButton(
                    onClick = {
                        ActivityUtil.openUrl(activityContext, madeByLink)
                    }
                ) {
                    Text(
                        text = madeBy,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Quick Links Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Quick Links",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                links.forEachBetter {
                    QuickLinkItem(
                        icon = Icons.Outlined.Code,
                        title = it.title,
                        onClick = { ActivityUtil.openUrl(activityContext, it.link) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Open Source Section
        SectionCard(
            title = stringResource(id = R.string.powered_by_open_source),
            icon = Icons.Outlined.Code
        ) {
            openSourceList.forEachIndexedBetter { index, openSource ->
                OpenSourceItem(
                    openSource = openSource,
                    isLast = index == openSourceList.size - 1,
                    onProjectClick = { ActivityUtil.openUrl(activityContext, openSource.projectLink) },
                    onLicenseClick = { ActivityUtil.openUrl(activityContext, openSource.licenseLink) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Contributors Section
        SectionCard(
            title = "Contributors",
            icon = Icons.Outlined.Favorite
        ) {
            contributorList.forEachIndexedBetter { index, contributor ->
                ContributorItem(
                    contributor = contributor,
                    isLast = index == contributorList.size - 1,
                    onClick = { ActivityUtil.openUrl(activityContext, contributor.link) }
                )
            }
        }

        SpacerRow()
    }
}

@Composable
private fun QuickLinkItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Outlined.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            content()
        }
    }
}

@Composable
private fun OpenSourceItem(
    openSource: OpenSource,
    isLast: Boolean,
    onProjectClick: () -> Unit,
    onLicenseClick: () -> Unit
) {
    Column {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onProjectClick() },
            color = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text(
                    text = openSource.projectName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.clickable { onLicenseClick() },
                    color = Color.Transparent
                ) {
                    Text(
                        text = "(${stringResource(R.string.license)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!isLast) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ContributorItem(
    contributor: Contributor,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                text = contributor.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = contributor.desc,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        )
    }
}