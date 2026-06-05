package com.standbyus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val HeaderBgStart = Color(0xFFFFFAF6)
private val HeaderBgEnd = Color(0xFFFFF3EC)
private val DividerColor = Color(0xFFEFE2DA)
private val TextPrimaryColor = Color(0xFF3D3029)
private val IconContainerColor = Color(0xB3FFFFFF)
private val IconTintColor = Color(0xFF8F7469)

@Composable
fun AppHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null,
    showDivider: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(HeaderBgStart, HeaderBgEnd)
                )
            )
            .height(50.dp)
            .drawBehind {
                if (showDivider) {
                    drawLine(
                        color = DividerColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(36.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(IconContainerColor)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = IconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = TextPrimaryColor
        )

        if (rightIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(36.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(IconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                rightIcon()
            }
        }
    }
}
