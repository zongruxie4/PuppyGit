package com.catpuppyapp.puppygit.compose

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFileName
import com.catpuppyapp.puppygit.utils.mime.iconRes


@Composable
fun IconOfItem(
    fileName:String,
    filePath:String,
    context: Context,
    iconColor: Color = LocalContentColor.current
) {
    //如果是图片，显示缩略图，否则显示图标
    val mime = MimeType.guessFromFileName(fileName)

    if(mime.type == "image") {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(filePath)
                .size(100, 100)
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
    }else {
        Icon(
//                    imageVector = if (item.isFile) Icons.Outlined.InsertDriveFile else Icons.Filled.Folder,
            imageVector = mime.iconRes,
            contentDescription = stringResource(R.string.file_or_folder_icon),
            tint = iconColor
        )
    }
}
