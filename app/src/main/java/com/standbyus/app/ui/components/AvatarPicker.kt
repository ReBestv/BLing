package com.standbyus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ——— Design tokens ———
private val Primary = Color(0xFFFFB4A2)
private val TextSecondary = Color(0xFF9E8E86)
private val UnselectedBg = Color(0xFFF5F0ED)

data class AvatarCategory(
    val name: String,
    val emojis: List<String>
)

val avatarCategories = listOf(
    AvatarCategory("动物系", listOf("🐱", "🐶", "🐰", "🐻", "🐼", "🦊", "🐨", "🐸", "🐷", "🐹")),
    AvatarCategory("趣味系", listOf("👻", "👾", "🤖", "💩", "🤡", "💀")),
    AvatarCategory("治愈系", listOf("⭐", "🌙", "☀️", "🌸", "🍀", "🌈")),
    AvatarCategory("酷系",   listOf("😎", "🕶️", "👑", "🔥"))
)

val allAvatarEmojis: List<String> = avatarCategories.flatMap { it.emojis }

@Composable
fun AvatarPicker(
    selectedEmoji: String,
    onAvatarSelected: (String) -> Unit,
    columns: Int = 4,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        avatarCategories.forEach { category ->
            item(key = "header_${category.name}") {
                Text(
                    text = category.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }

            // Chunk into rows
            val rows = category.emojis.chunked(columns)
            rows.forEach { rowEmojis ->
                item(key = "row_${category.name}_${rowEmojis.hashCode()}") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowEmojis.forEach { emoji ->
                            val isSelected = emoji == selectedEmoji

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f)
                                        else UnselectedBg,
                                        CircleShape
                                    )
                                    .then(
                                        if (isSelected)
                                            Modifier.border(2.dp, Primary, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { onAvatarSelected(emoji) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
