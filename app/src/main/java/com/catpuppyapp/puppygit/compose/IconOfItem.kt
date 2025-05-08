package com.catpuppyapp.puppygit.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.catpuppyapp.puppygit.utils.apkIconOrNull
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFile
import com.catpuppyapp.puppygit.utils.mime.iconRes
import java.io.File

private const val iconSizeInPx = 100
private val iconModifierSize = 50.dp
private val defaultIconModifier = Modifier.size(iconModifierSize)


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
    modifier: Modifier = defaultIconModifier,

) {
    val file = remember(filePath) { File(filePath) }

    //如果是图片，显示缩略图，否则显示图标
    // guessFromFile()会判断是否是文件夹，guessFromFileName()则不会，为了能为文件夹正常显示图标，这里应用用guessFromFile()
    val mime = MimeType.guessFromFile(file)

    //图片类型
    if(mime.type == "image" && file.let{ it.exists() && it.isFile }) {
        ShowThumbnail(context, filePath, contentDescription, mime.iconRes, iconColor, modifier)

        return
    }

    if(mime == MimeType.APK) {
        val apkIcon = apkIconOrNull(context, filePath, iconSizeInPx)
        if(apkIcon != null) {
            Image(
                apkIcon,
                contentDescription = contentDescription,
                modifier = modifier,
            )

            return
        }
    }



    ShowIcon(defaultIconWhenLoadFailed ?: mime.iconRes, contentDescription, iconColor, modifier)

}

@Composable
private fun ShowThumbnail(
    context:Context,
    filePath:String,
    contentDescription: String?,
    fallbackIcon: ImageVector,
    fallbackIconColor: Color,
    modifier: Modifier
) {
    val loadErr = remember { mutableStateOf(false) }

    if(loadErr.value) {
        ShowIcon(fallbackIcon, contentDescription, fallbackIconColor, modifier)
    }else {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(filePath)
                .size(iconSizeInPx)
                .decoderFactory(SvgDecoder.Factory())
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,  //.clip(RectangleShape)，想弄成正方形，但没卵用，算了
            onError = {
                loadErr.value = true
            },
            onLoading = {
                loadErr.value = false
            },
            onSuccess = {
                loadErr.value = false
            }
        )
    }
}
