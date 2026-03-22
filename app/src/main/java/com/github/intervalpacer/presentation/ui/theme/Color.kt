package com.github.intervalpacer.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// 主色调
val PrimaryOrange = Color(0xFFFF6B35)      // 跑步阶段（高强度）
val PrimaryGreen = Color(0xFF4CAF50)       // 步行阶段（低强度）
val PrimaryYellow = Color(0xFFFFC107)      // 热身
val PrimaryBlue = Color(0xFF2196F3)        // 冷身
val PrimaryGray = Color(0xFF9E9E9E)        // 完成

// 背景色
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2D2D2D)

// 文字色
val OnDarkBackground = Color(0xFFFFFFFF)
val OnDarkSurface = Color(0xFFE0E0E0)
val OnDarkSurfaceVariant = Color(0xFFB0B0B0)

// 语义化颜色
object PhaseColors {
    val Warmup = PrimaryYellow
    val Run = PrimaryOrange
    val Walk = PrimaryGreen
    val Cooldown = PrimaryBlue
    val Completed = PrimaryGray
}
