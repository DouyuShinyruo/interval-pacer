package com.github.intervalpacer.domain.model

import kotlin.time.Duration

/**
 * 力量训练配置
 */
data class SetTrainingConfig(
    val exerciseName: String = "",
    val totalSets: Int = 5,
    val restDuration: Duration = Duration.parse("90s"),
    val enableVoicePrompt: Boolean = true,
    val enableVibration: Boolean = true,
    val enableAutoStart: Boolean = false
)
