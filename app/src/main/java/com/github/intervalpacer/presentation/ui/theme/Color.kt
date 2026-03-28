package com.github.intervalpacer.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// Material 3 Dark Theme Color Tokens
// 基于 Material Theme Builder 生成的深色主题配色
// ============================================================

// Primary - 橙色系（主要按钮、高亮、强调）
val md_theme_dark_primary = Color(0xFFFFB4AB)
val md_theme_dark_onPrimary = Color(0xFF690005)
val md_theme_dark_primaryContainer = Color(0xFF93350B)
val md_theme_dark_onPrimaryContainer = Color(0xFFFFDAD4)

// Secondary - 棕灰色系（次要按钮、标签、辅助信息）
val md_theme_dark_secondary = Color(0xFFE5BDB)
val md_theme_dark_onSecondary = Color(0xFF4A4540)
val md_theme_dark_secondaryContainer = Color(0xFF635B54)
val md_theme_dark_onSecondaryContainer = Color(0xFFFFDAD4)

// Tertiary - 绿色系（步行阶段、成功状态）
val md_theme_dark_tertiary = Color(0xFFA8D4A0)
val md_theme_dark_onTertiary = Color(0xFF0F3F0C)
val md_theme_dark_tertiaryContainer = Color(0xFF275C20)
val md_theme_dark_onTertiaryContainer = Color(0xFFC4F0BB)

// Error
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)

// Surface - 背景层级
val md_theme_dark_background = Color(0xFF1C1412)
val md_theme_dark_onBackground = Color(0xFFF5DFDA)
val md_theme_dark_surface = Color(0xFF1C1412)
val md_theme_dark_onSurface = Color(0xFFF5DFDA)

val md_theme_dark_surfaceVariant = Color(0xFF524345)
val md_theme_dark_onSurfaceVariant = Color(0xFFD8C1BF)

val md_theme_dark_surfaceContainerLowest = Color(0xFF130F0D)
val md_theme_dark_surfaceContainerLow = Color(0xFF1C1412)
val md_theme_dark_surfaceContainer = Color(0xFF251E1B)
val md_theme_dark_surfaceContainerHigh = Color(0xFF302825)
val md_theme_dark_surfaceContainerHighest = Color(0xFF3B3330)

// Outline - 分割线、边框
val md_theme_dark_outline = Color(0xFF8B7375)
val md_theme_dark_outlineVariant = Color(0xFF524345)

// Scrim - 遮罩层
val md_theme_dark_scrim = Color(0xFF000000)

// ============================================================
// 阶段专属颜色（运动中背景动画和阶段指示）
// 这些颜色仅在运动进行中使用，不替代 Primary/Secondary 角色
// ============================================================
object PhaseColors {
    val Warmup = Color(0xFFFFAB40)     // 热身 - 琥珀色
    val Run = Color(0xFFFF7043)        // 跑步 - 深橙色
    val Walk = Color(0xFF66BB6A)       // 步行 - 绿色
    val Cooldown = Color(0xFF42A5F5)   // 冷身 - 蓝色
    val Completed = Color(0xFF4DB6AC)  // 完成 - 青色
}

// ============================================================
// 向后兼容别名（逐步移除）
// ============================================================
@Deprecated("Use md_theme_dark_primary or PhaseColors.Run", ReplaceWith("PhaseColors.Run"))
val PrimaryOrange = PhaseColors.Run

@Deprecated("Use md_theme_dark_tertiary or PhaseColors.Walk", ReplaceWith("PhaseColors.Walk"))
val PrimaryGreen = PhaseColors.Walk

@Deprecated("Use PhaseColors.Warmup", ReplaceWith("PhaseColors.Warmup"))
val PrimaryYellow = PhaseColors.Warmup

@Deprecated("Use PhaseColors.Cooldown", ReplaceWith("PhaseColors.Cooldown"))
val PrimaryBlue = PhaseColors.Cooldown

@Deprecated("Use PhaseColors.Completed", ReplaceWith("PhaseColors.Completed"))
val PrimaryGray = PhaseColors.Completed

@Deprecated("Use md_theme_dark_background", ReplaceWith("md_theme_dark_background"))
val DarkBackground = md_theme_dark_background

@Deprecated("Use md_theme_dark_surface", ReplaceWith("md_theme_dark_surface"))
val DarkSurface = md_theme_dark_surface

@Deprecated("Use md_theme_dark_surfaceVariant", ReplaceWith("md_theme_dark_surfaceVariant"))
val DarkSurfaceVariant = md_theme_dark_surfaceVariant

@Deprecated("Use md_theme_dark_surfaceContainer", ReplaceWith("md_theme_dark_surfaceContainer"))
val DarkSurfaceContainer = md_theme_dark_surfaceContainer

@Deprecated("Use md_theme_dark_surfaceContainerHigh", ReplaceWith("md_theme_dark_surfaceContainerHigh"))
val DarkSurfaceContainerHigh = md_theme_dark_surfaceContainerHigh

@Deprecated("Use md_theme_dark_surfaceContainerHighest", ReplaceWith("md_theme_dark_surfaceContainerHighest"))
val DarkSurfaceContainerHighest = md_theme_dark_surfaceContainerHighest

@Deprecated("Use md_theme_dark_onBackground", ReplaceWith("md_theme_dark_onBackground"))
val OnDarkBackground = md_theme_dark_onBackground

@Deprecated("Use md_theme_dark_onSurface", ReplaceWith("md_theme_dark_onSurface"))
val OnDarkSurface = md_theme_dark_onSurface

@Deprecated("Use md_theme_dark_onSurfaceVariant", ReplaceWith("md_theme_dark_onSurfaceVariant"))
val OnDarkSurfaceVariant = md_theme_dark_onSurfaceVariant
