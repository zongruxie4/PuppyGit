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
    appendContent:@Composable (()->Unit)? = null
) {
    InfoDialog(showTitleInfoDialog) {
        ScrollableColumn {
            Row {
                Text(stringResource(id = R.string.repo) + ": " + curRepo.repoName)
            }
            Spacer(Modifier.height(10.dp))

            if (dbIntToBool(curRepo.isDetached)) {
                Row {
                    Text(stringResource(R.string.branch) + ": " + Cons.gitDetachedHead)
                }
            } else {
                Row {
                    Text(stringResource(R.string.branch) + ": " + (curRepo.branch))
                }

                if (curRepo.upstreamBranch.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))

                    Row {
                        Text(stringResource(R.string.upstream) + ": " + (curRepo.upstreamBranch))
                    }
                }
            }

            if(appendContent != null) {
                Spacer(Modifier.height(10.dp))
                appendContent()
            }

        }
    }
}
