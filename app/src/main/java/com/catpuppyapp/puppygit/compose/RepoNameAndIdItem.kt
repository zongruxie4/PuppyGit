package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.data.entity.RepoEntity


@Composable
fun RepoNameAndIdItem(
    repoEntity: RepoEntity,
    trailIcon: (@Composable BoxScope.(initModifier:Modifier)->Unit)? = null,
    onClick:((RepoEntity) -> Unit)?
) {
    Box(
        modifier = Modifier.fillMaxWidth().then(
            if(onClick == null) Modifier else Modifier.clickable { onClick(repoEntity) }
        ).padding(horizontal = 5.dp),
    ) {
        val iconModifier = if(trailIcon == null) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(.9f)

        Column(
            modifier = iconModifier.align(Alignment.CenterStart)
        ) {

            Text(text = repoEntity.repoName)

            Text(text = repoEntity.id, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }

        if(trailIcon != null) {
            trailIcon(Modifier.fillMaxWidth(.1f).align(Alignment.CenterEnd))
        }

    }

}
