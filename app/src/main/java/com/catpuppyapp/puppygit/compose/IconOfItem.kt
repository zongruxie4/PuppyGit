package com.catpuppyapp.puppygit.compose

import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFileName
import com.catpuppyapp.puppygit.utils.mime.iconRes
import java.io.File


@Composable
fun IconOfItem(
    fileName:String,
    filePath:String,
    context: Context,
    contentDescription:String?,

    //非图片类型，才会使用图标颜色
    iconColor: Color = LocalContentColor.current,

    //若为null，将根据mime类型获取对应图标
    defaultIconWhenLoadFailed: ImageVector? = null,

) {
    //如果是图片，显示缩略图，否则显示图标
    val mime = MimeType.guessFromFileName(fileName)

    if(mime.type == "image" && File(filePath).let{ it.exists() && it.isFile }) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(filePath)
                .size(100, 100)
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier.size(50.dp)
        )
    }else {
        Icon(
            imageVector = defaultIconWhenLoadFailed ?: mime.iconRes,
            contentDescription = contentDescription,
            tint = iconColor
        )
    }
}
