package com.standbyus.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// 视觉规范 v1.0 的 Header 专属 Token
private val HeaderBgColor = Color(0xFFFFFFFF)
private val DividerColor = Color(0xFFF0EAE6)
private val TextPrimaryColor = Color(0xFF5A4A42)

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
            .background(HeaderBgColor)
            .height(48.dp) // 极简高度
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
            .padding(horizontal = 4.dp), // 留出一点边距
        contentAlignment = Alignment.Center
    ) {
        // 左侧返回按钮，强制统一使用规范的样式和 #5A4A42
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = TextPrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // 绝对水平、垂直居中的标题，文字再次放大
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimaryColor
        )

        // 右侧操作区图标插槽，调用方需注意图标应为 #9E8E86 等规范
        if (rightIcon != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp)
            ) {
                rightIcon()
            }
        }
    }
}
