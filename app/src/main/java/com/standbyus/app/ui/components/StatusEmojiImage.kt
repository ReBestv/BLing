package com.standbyus.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.standbyus.app.ui.theme.StatusEmoji

@Composable
fun StatusEmojiImage(
    value: String?,
    feelingName: String?,
    size: Dp,
    textSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    tintColor: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val raw = value.orEmpty().trim()
    if (StatusEmoji.isRemoteImage(raw)) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(context)
                .data(raw)
                .crossfade(true)
                .build(),
            contentDescription = feelingName,
            loading = {
                Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(size * 0.35f),
                        strokeWidth = 2.dp,
                        color = tintColor.takeOrElse { Color(0xFFFFB4A2) }
                    )
                }
            },
            error = {
                Text(
                    text = StatusEmoji.textFallback(value, feelingName),
                    fontSize = textSize
                )
            },
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        Text(
            text = StatusEmoji.textFallback(value, feelingName),
            fontSize = textSize,
            modifier = modifier
        )
    }
}

private fun Color.takeOrElse(fallback: () -> Color): Color {
    return if (this == Color.Unspecified) fallback() else this
}
