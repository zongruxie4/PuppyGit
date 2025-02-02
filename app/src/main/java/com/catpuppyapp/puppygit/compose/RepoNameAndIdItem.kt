package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
    onClick:(RepoEntity) -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(repoEntity) }.padding(horizontal = 5.dp),

        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = repoEntity.repoName)
            Text(text = repoEntity.id, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)

        }


    }

}

