package com.standbyus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.standbyus.app.data.model.Feeling
import com.standbyus.app.ui.theme.EmojiThemeManager
import com.standbyus.app.ui.theme.StandByUsLightColors
import com.standbyus.app.ui.theme.StandByUsSpacing

private fun isEmoji(text: String) = !text.startsWith("http")

@Composable
fun FeelingPicker(
    feelings: List<Feeling> = Feeling.entries,
    selectedFeeling: Feeling,
    onFeelingSelected: (Feeling) -> Unit,
    modifier: Modifier = Modifier,
    emojiGetter: ((Feeling) -> String)? = null
) {
    val context = LocalContext.current
    val resolvedEmojiGetter = emojiGetter ?: { feeling ->
        EmojiThemeManager.getEmoji(context, feeling.displayName)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(StandByUsSpacing.md),
        verticalArrangement = Arrangement.spacedBy(StandByUsSpacing.md),
        contentPadding = PaddingValues(StandByUsSpacing.sm)
    ) {
        items(feelings) { feeling ->
            val isSelected = feeling == selectedFeeling

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onFeelingSelected(feeling) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) StandByUsLightColors.accent
                            else StandByUsLightColors.accentBg
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) StandByUsLightColors.accent
                                    else MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val emoji = resolvedEmojiGetter(feeling)
                    if (isEmoji(emoji)) {
                        Text(text = emoji, fontSize = 20.sp)
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(emoji)
                                .crossfade(true)
                                .build(),
                            contentDescription = feeling.displayName,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
