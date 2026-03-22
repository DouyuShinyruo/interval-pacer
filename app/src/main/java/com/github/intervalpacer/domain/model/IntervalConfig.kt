package com.github.intervalpacer.domain.model

import kotlin.time.Duration

/**
 * 间歇训练配置
 * @param runDuration 跑步时长
 * @param walkDuration 走路/休息时长
 * @param repeatCount 重复次数
 * @param warmupDuration 热身时长（可选）
 * @param cooldownDuration 冷身时长（可选）
 * @param enableVoicePrompt 是否启用语音提示
 * @param enableVibration 是否启用震动
 * @param runFirst 是否先跑步（true=先跑后走，false=先走后跑）
 */
data class IntervalConfig(
    val runDuration: Duration,
    val walkDuration: Duration,
    val repeatCount: Int,
    val warmupDuration: Duration = Duration.ZERO,
    val cooldownDuration: Duration = Duration.ZERO,
    val enableVoicePrompt: Boolean = true,
    val enableVibration: Boolean = true,
    val runFirst: Boolean = true
) {
    companion object {
        /**
         * 新手预设：跑1分钟，走2分钟，重复5次
         */
        fun beginnerPreset() = IntervalConfig(
            runDuration = Duration.parse("1m"),
            walkDuration = Duration.parse("2m"),
            repeatCount = 5
        )

        /**
         * 进阶预设：跑3分钟，走2分钟，重复6次
         */
        fun intermediatePreset() = IntervalConfig(
            runDuration = Duration.parse("3m"),
            walkDuration = Duration.parse("2m"),
            repeatCount = 6
        )
    }

    /**
     * 计算总训练时长（不包括热身和冷身）
     */
    fun getTotalDuration(): Duration {
        return (runDuration + walkDuration) * repeatCount
    }

    /**
     * 计算完整的训练时长（包括热身和冷身）
     */
    fun getFullDuration(): Duration {
        return warmupDuration + getTotalDuration() + cooldownDuration
    }
}
