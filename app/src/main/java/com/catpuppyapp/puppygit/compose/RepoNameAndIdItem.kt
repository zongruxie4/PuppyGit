package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.catpuppyapp.puppygit.data.entity.RepoEntity


@Composable
fun RepoNameAndIdItem(
    repoEntity: RepoEntity,
    trailIcons: @Composable ()->Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically,
    ) {
        TwoLineTextsAndIcons(repoEntity.repoName, repoEntity.id, trailIcons)
    }

}

