package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.dbIntToBool

@Composable
fun RepoInfoDialog(
    curRepo: RepoEntity,
    showTitleInfoDialog: MutableState<Boolean>,
    prependContent:@Composable (()->Unit)? = null,
    appendContent:@Composable (()->Unit)? = null
) {
    InfoDialog(showTitleInfoDialog) {
        ScrollableColumn {
            if(prependContent != null) {
                prependContent()
                RepoInfoDialogItemSpacer()
            }

            Row {
                Text(stringResource(id = R.string.repo) + ": " + curRepo.repoName)
            }
            RepoInfoDialogItemSpacer()

            if (dbIntToBool(curRepo.isDetached)) {
                Row {
                    Text(stringResource(R.string.branch) + ": " + Cons.gitDetachedHead)
                }
            } else {
                Row {
                    Text(stringResource(R.string.branch) + ": " + (curRepo.branch))
                }

                if (curRepo.upstreamBranch.isNotBlank()) {
                    RepoInfoDialogItemSpacer()

                    Row {
                        Text(stringResource(R.string.upstream) + ": " + (curRepo.upstreamBranch))
                    }
                }
            }

            RepoInfoDialogItemSpacer()
            Row {
                Text(stringResource(R.string.repo_state) + ": " + (curRepo.gitRepoState?.name?:""))
            }

            if(appendContent != null) {
                RepoInfoDialogItemSpacer()
                appendContent()
            }

        }
    }
}

@Composable
fun RepoInfoDialogItemSpacer() {
    Spacer(Modifier.height(10.dp))
}
