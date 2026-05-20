package com.standbyus.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow

/**
 * StandBy Us — Design Tokens v1.0
 * Generated 2026-05-12 from index.html prototype
 * Direction: Warm + Playful (珊瑚橙 #FF7E67)
 */

// ============================================================
// COLOR — Light Theme
// ============================================================
object StandByUsLightColors {
    // Background
    val bg            = Color(0xFFFFF7F2)
    val surface       = Color(0xFFFFFAF6)
    val surfaceRaised = Color(0xFFFFFCF9)
    val overlay       = Color(0x662B2826) // rgba(43,40,38,0.4)

    // Foreground
    val fg           = Color(0xFF252220)
    val fgSecondary  = Color(0xFF5B5552)
    val muted        = Color(0xFF807975)
    val placeholder  = Color(0xFFA29B97)

    // Accent — Coral
    val accent       = Color(0xFFFF7E67)
    val accentHover  = Color(0xFFEA6E58)
    val accentPress  = Color(0xFFD55F4A)
    val accentSoft   = Color(0xFFFFD4CA)
    val accentBg     = Color(0xFFFFF2EF)
    val accentGlow   = Color(0x59FF7D66) // rgba(255,125,102,0.35)

    // Border
    val border       = Color(0xFFE8E1DD)
    val borderLight  = Color(0xFFF2EDEA)

    // Semantic
    val success      = Color(0xFF17A34A)
    val successBg    = Color(0xFFDCF5E5)
    val warn         = Color(0xFFEAB308)
    val warnBg       = Color(0xFFFDF3CB)
    val danger       = Color(0xFFDC2626)
    val dangerBg     = Color(0xFFFDE0DD)
}

// ============================================================
// COLOR — Dark Theme (深夜陪伴)
// ============================================================
object StandByUsDarkColors {
    val bg            = Color(0xFF1E1C1A)
    val surface       = Color(0xFF2A2724)
    val surfaceRaised = Color(0xFF332F2C)
    val overlay       = Color(0x99000000) // rgba(0,0,0,0.6)

    val fg           = Color(0xFFEDEBE8)
    val fgSecondary  = Color(0xFFB3AFAB)
    val muted        = Color(0xFF8A8580)
    val placeholder  = Color(0xFF6C6763)

    val accent       = Color(0xFFFF9B86)
    val accentHover  = Color(0xFFFFA590)
    val accentPress  = Color(0xFFFF8A70)
    val accentSoft   = Color(0xFFA88075)
    val accentBg     = Color(0xFF3D2D28)
    val accentGlow   = Color(0x73FF7D66) // rgba(255,125,102,0.45)

    val border       = Color(0xFF3D3936)
    val borderLight  = Color(0xFF332F2C)

    // Semantic (same as light for readability)
    val success      = Color(0xFF17A34A)
    val successBg    = Color(0xFF1A3D2E)
    val warn         = Color(0xFFEAB308)
    val warnBg       = Color(0xFF3D371A)
    val danger       = Color(0xFFDC2626)
    val dangerBg     = Color(0xFF3D1A1A)
}

// ============================================================
// TYPOGRAPHY
// ============================================================
object StandByUsTypography {
    // Font families
    val displayFont = FontFamily.Serif     // Tiempos Headline → system serif fallback
    val bodyFont    = FontFamily.SansSerif // Söhne → system sans-serif
    val monoFont    = FontFamily.Monospace

    // Type scale (1.2 ratio)
    val xs    = 11.sp   // Status bar, labels, captions
    val sm    = 13.sp   // Nav text, secondary info
    val base  = 15.sp   // Body (default)
    val md    = 17.sp   // Card titles, list items
    val lg    = 20.sp   // App bar title
    val xl    = 24.sp   // Page title
    val xxl   = 32.sp   // Section heading
    val xxxl  = 40.sp   // Hero emotion text
}

// ============================================================
// SPACING — 4dp grid
// ============================================================
object StandByUsSpacing {
    val xs    = 4.dp
    val sm    = 8.dp
    val md    = 12.dp
    val base  = 16.dp   // Card padding
    val lg    = 20.dp   // Section gap
    val xl    = 24.dp
    val xxl   = 32.dp   // Page top/bottom
    val xxxl  = 40.dp   // Major section divider
    val huge  = 48.dp
}

// ============================================================
// BORDER RADIUS
// ============================================================
object StandByUsShapes {
    val sm     = RoundedCornerShape(8.dp)    // Small buttons, labels, inputs
    val medium = RoundedCornerShape(12.dp)   // Cards, nav buttons
    val lg     = RoundedCornerShape(16.dp)   // Large buttons, image containers
    val xl     = RoundedCornerShape(24.dp)   // Mood card, hero areas
    val xxl    = RoundedCornerShape(32.dp)   // Rare
    val full   = RoundedCornerShape(50)      // Pills, avatars, mood circles
}

// ============================================================
// SHADOW / ELEVATION — warm-tinted
// ============================================================
object StandByUsElevation {
    val l1 = Shadow(
        color = Color(0x0D252220),
        offset = Offset(0f, 1f),
        blurRadius = 2f
    )
    val l2 = Shadow(
        color = Color(0x12252220),
        offset = Offset(0f, 2f),
        blurRadius = 8f
    )
    val l3 = Shadow(
        color = Color(0x17252220),
        offset = Offset(0f, 4f),
        blurRadius = 16f
    )
    val l4 = Shadow(
        color = Color(0x1C252220),
        offset = Offset(0f, 8f),
        blurRadius = 32f
    )
}

// ============================================================
// MOTION — Duration & Easing
// ============================================================
object StandByUsMotion {
    const val DUR_FAST   = 150
    const val DUR_BASE   = 250
    const val DUR_SLOW   = 400
    const val DUR_BREATH = 3000  // breathing glow cycle

    // Compose equivalents:
    // ease-out   → FastOutSlowInEasing
    // ease-in-out → LinearEasing (for symmetric transitions, or use a custom cubic)
    // ease-spring → CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
}

// ============================================================
// MOOD PALETTE — 16 emotions
// ============================================================
data class MoodColor(
    val emoji: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val label: String
)

object StandByUsMoods {
    val all = listOf(
        MoodColor("😊",   Color(0xFFFFD180), Color(0xFFFFAB40), "开心/幸福"),
        MoodColor("🥰",   Color(0xFFF8BBD0), Color(0xFFF48FB1), "爱/心动"),
        MoodColor("🤩",   Color(0xFFFFE082), Color(0xFFFFD54F), "兴奋"),
        MoodColor("😌",   Color(0xFFB2DFDB), Color(0xFF80CBC4), "平静"),
        MoodColor("😇",   Color(0xFFC5E1A5), Color(0xFFAED581), "满足"),
        MoodColor("🤗",   Color(0xFFFFCCBC), Color(0xFFFFAB91), "期待"),
        MoodColor("🥺",   Color(0xFFB3E5FC), Color(0xFF81D4FA), "思念"),
        MoodColor("😤",   Color(0xFFE1BEE7), Color(0xFFCE93D8), "小情绪"),
        MoodColor("😕",   Color(0xFFFFF9C4), Color(0xFFFFF176), "困惑"),
        MoodColor("😮‍💨", Color(0xFFD7CCC8), Color(0xFFBCAAA4), "疲惫"),
        MoodColor("😢",   Color(0xFFBBDEFB), Color(0xFF90CAF9), "伤心"),
        MoodColor("😠",   Color(0xFFFFCDD2), Color(0xFFEF9A9A), "生气"),
        MoodColor("😰",   Color(0xFFE0E0E0), Color(0xFFBDBDBD), "焦虑"),
        MoodColor("🫶",   Color(0xFFF0F4C3), Color(0xFFE6EE9C), "感恩"),
        MoodColor("🤒",   Color(0xFFDCEDC8), Color(0xFFC5E1A5), "生病"),
        MoodColor("💪",   Color(0xFFFFE0B2), Color(0xFFFFCC80), "忙碌"),
    )
}


