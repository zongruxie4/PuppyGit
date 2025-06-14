package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.catpuppyapp.puppygit.data.entity.RepoEntity


@Composable
fun RepoNameAndIdItem(
    repoEntity: RepoEntity,
    trailIconWidth: Dp,
    trailIcons: @Composable BoxScope.(containerModifier:Modifier)->Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),

        verticalAlignment = Alignment.CenterVertically,
    ) {
        TwoLineTextsAndIcons(repoEntity.repoName, repoEntity.id, trailIconWidth, trailIcons)
    }

}

