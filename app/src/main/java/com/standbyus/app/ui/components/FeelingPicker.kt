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
    val resolvedEmojiGetter = emojiGetter ?: { feeling -> EmojiThemeManager.getEmoji(context, feeling.displayName) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(feelings) { feeling ->
            val isSelected = feeling == selectedFeeling

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onFeelingSelected(feeling) }
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) feeling.color
                            else feeling.color.copy(alpha = 0.2f)
                        )
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = feeling.color,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val emojiText = resolvedEmojiGetter(feeling)
                    if (isEmoji(emojiText)) {
                        Text(text = emojiText, fontSize = 26.sp)
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(emojiText).crossfade(true).build(),
                            contentDescription = feeling.displayName,
                            modifier = Modifier.size(36.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feeling.displayName,
                    fontSize = 11.sp,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
