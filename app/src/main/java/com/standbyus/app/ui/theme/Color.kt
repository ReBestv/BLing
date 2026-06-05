package com.standbyus.app.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Design System v3 — StandBy Us Visual Spec v1.0 =====

// ===== Primary =====
val Primary = Color(0xFFFF8E78)           // --accent-primary 主强调色
val PrimaryDark = Color(0xFFEA7662)       // 强调色深色 自动推导
val PrimaryLight = Color(0xFFFFB99F)      // 强调色浅色 自动推导
val PrimarySoft = Color(0xFFFFF0EB)       // --bg-tag 标签/徽章背景
val PrimaryBg = Color(0xFFFFF8F3)         // --bg-primary 全局页面背景

// ===== Secondary =====
val Secondary = Color(0xFFE8D5C4)         // --accent-secondary 奶茶色辅助色
val SecondaryDark = Color(0xFFD1C0B0)     // 自动推导
val SecondaryLight = Color(0xFFF0E5DB)    // 自动推导

// ===== Tertiary =====
val TertiaryColor = Color(0xFFD4E2D4)     // --accent-tertiary 薄荷灰绿

// ===== Background & Surface =====
val Background = Color(0xFFFFF8F3)        // --bg-primary
val Surface = Color(0xFFFFFFFF)           // --bg-card
val SurfaceVariant = Color(0xFFFFF0EB)    // --bg-tag

// ===== Text =====
val TextPrimary = Color(0xFF3D3029)       // --text-primary 暖棕黑
val TextSecondary = Color(0xFF8F7469)     // --text-secondary 暖灰
val TextHint = Color(0xFFC4B5AD)          // --text-tertiary 浅灰棕

// ===== Border & Shadow =====
val Border = Color(0xFFEFE2DA)            // --divider 分割线
val BorderLight = Color(0xFFF9F7F5)       // 浅边框 自动推导
val Shadow = Color(0x0F5A4A42)            // --shadow rgba(90, 74, 66, 0.06)

// ===== Semantic =====
val Danger = Color(0xFFFF8575)            // 基于珊瑚红的报错色
val OnDanger = Color(0xFFFFFFFF)
val Success = Color(0xFF6BCB77)           // 成功
val Warning = Color(0xFFFFB347)           // 警告

// ===== Dark mode =====
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2A2A2A)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFAAAAAA)
val DarkTextHint = Color(0xFF777777)
val DarkBorder = Color(0xFF333333)
val DarkBorderLight = Color(0xFF444444)

// ===== Material3 scheme aliases (兼容 Theme.kt) =====
val OnBackground = TextPrimary
val OnSurface = TextPrimary
val OnSurfaceVariant = TextSecondary
val Outline = Border
val OutlineVariant = BorderLight
val Error = Danger
val OnError = OnDanger
val Tertiary = PrimarySoft

// ===== Dark mode Material3 aliases =====
val DarkBackground = DarkBg
val DarkOnBackground = DarkTextPrimary
val DarkOnSurface = DarkTextPrimary
val DarkOnSurfaceVariant = DarkTextSecondary
val DarkOutline = DarkBorder
val DarkOutlineVariant = DarkBorderLight

// ===== Legacy aliases (兼容旧代码) =====
val PrimaryVariant = PrimaryDark
val Orange = Primary
val HoneyYellow = Color(0xFFFDCB6E)
val Charcoal = TextPrimary
val Gray = TextSecondary
val BackgroundLight = Background
val CardWhite = Surface
val DarkCard = DarkSurface
val DarkText = DarkTextPrimary
val DarkGray = DarkTextSecondary
