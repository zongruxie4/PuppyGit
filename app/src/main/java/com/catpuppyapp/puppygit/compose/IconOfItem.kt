package com.catpuppyapp.puppygit.compose

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.catpuppyapp.puppygit.ui.theme.Theme
import com.catpuppyapp.puppygit.utils.apkIconOrNull
import com.catpuppyapp.puppygit.utils.mime.MimeType
import com.catpuppyapp.puppygit.utils.mime.guessFromFile
import com.catpuppyapp.puppygit.utils.mime.iconRes
import java.io.File

private const val iconSizeInPx = 100
private val iconModifierSize = 50.dp


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
    val file = remember(filePath) { File(filePath) }

    //如果是图片，显示缩略图，否则显示图标
    // guessFromFile()会判断是否是文件夹，guessFromFileName()则不会，为了能为文件夹正常显示图标，这里应用用guessFromFile()
    val mime = MimeType.guessFromFile(file)

    //图片类型
    if(mime.type == "image" && file.let{ it.exists() && it.isFile }) {
        ShowThumbnail(context, filePath, contentDescription, mime.iconRes)

        return
    }

    if(mime == MimeType.APK) {
        val apkIcon = apkIconOrNull(context, filePath, iconSizeInPx)
        if(apkIcon != null) {
            Image(
                apkIcon,
                contentDescription = contentDescription,
            )

            return
        }
    }


    Icon(
        imageVector = defaultIconWhenLoadFailed ?: mime.iconRes,
        contentDescription = contentDescription,
        tint = iconColor
    )
}

@Composable
private fun ShowThumbnail(context:Context, filePath:String, contentDescription: String?, loadErrShowThisIcon: ImageVector) {
    val fallback = rememberVectorPainter(loadErrShowThisIcon)
    val inDarkTheme = remember { Theme.inDarkTheme }

    // 另一种加载出错显示后备图片的方法是不给asyncImage设后备图片，或设成透明，然后用Box在AsyncImage前面显示后备图片，由于Box是浮动的，所以若目标图片没成功加载，就会看到后备图片

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(filePath)
            .size(iconSizeInPx)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = contentDescription,
        modifier = Modifier.size(iconModifierSize).background(if(inDarkTheme) Color.DarkGray else Color.White),  //.clip(RectangleShape)，想弄成正方形，但没卵用，算了
        error = fallback,
        placeholder = fallback,
        fallback = fallback,

        //加载出错时给图片着色，这样不行，会影响正常加载的图片的颜色
//        colorFilter = ColorFilter.tint(LocalContentColor.current)
    )
}
