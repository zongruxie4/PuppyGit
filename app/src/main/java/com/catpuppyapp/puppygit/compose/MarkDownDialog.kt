package com.catpuppyapp.puppygit.compose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.catpuppyapp.puppygit.syntaxhighlight.PLFont
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.syntaxhighlight.markdown.MarkDownSyntaxHighlighter

@Composable
fun MarkDownDialog(
    title: String,
    text: String,
    previewModeOn: MutableState<Boolean>,
    close: () -> Unit,
    copy: () -> Unit,
) {
    val switchBetweenRawAndRenderedContent = {
        previewModeOn.value = !previewModeOn.value
    }
    val highlightedMarkdownText = remember { mutableStateOf<AnnotatedString?>(null) }

    val annotatedText = remember { AnnotatedString(text) }

    val rawOrHighlightedText = { highlightedMarkdownText.value ?:  annotatedText}

    ConfirmDialog3(
        title = title,
        requireShowTextCompose = true,
        textCompose = {
            ScrollableColumn {
                // use code font for both, if want to view text with system font, can use details dialog instead
                if(previewModeOn.value) {
                    MarkDownContainer(
                        content = text,
                        fontResource = PLFont.codeFontResId,

                        // false to let default link handler take it
                        onLinkClicked = { false },
                    )
                }else {
                    Text(
                        text = rawOrHighlightedText(),
                        fontFamily = PLFont.codeFont
                    )
                }
            }
        },

        customCancel = {
            ScrollableRow {
                IconButton(
                    onClick = switchBetweenRawAndRenderedContent
                ) {
                    Icon(
                        imageVector = if(previewModeOn.value) Icons.Filled.RemoveRedEye else Icons.AutoMirrored.Filled.Notes,
                        contentDescription = "switch preview mode and text mode"
                    )
                }

                TextButton(
                    onClick = close
                ) {
                    Text(
                        text = stringResource(R.string.close),
                    )
                }
            }

        },
        customOk = {
            TextButton(
                onClick = {
                    copy()
                    close()
                }
            ) {
                Text(
                    text = stringResource(R.string.copy),
                )
            }
        },
        onDismiss = close,
    )

    LaunchedEffect(previewModeOn.value) {
        if(!previewModeOn.value && highlightedMarkdownText.value == null) {
            MarkDownSyntaxHighlighter(text) {
                highlightedMarkdownText.value = it
            }
        }
    }
}
