package com.github.intervalpacer.data.model

import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.WorkoutType
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * 训练记录实体（用于持久化）
 */
@OptIn(kotlin.time.ExperimentalTime::class)
data class WorkoutRecord(
    val id: String,
    val type: WorkoutType,
    val intervalConfig: IntervalConfig? = null, // 间歇训练配置
    val strengthConfig: StrengthConfig? = null, // 力量训练配置
    val startTime: Instant,
    val endTime: Instant,
    val totalDuration: Duration,
    val completedRounds: Int,
    val targetRounds: Int,
    val isCompleted: Boolean
) {
    /**
     * 计算完成度百分比
     */
    fun getCompletionPercentage(): Float {
        return if (targetRounds > 0) {
            completedRounds.toFloat() / targetRounds.toFloat()
        } else {
            0f
        }
    }
}

/**
 * 力量训练配置
 */
@OptIn(kotlin.time.ExperimentalTime::class)
data class StrengthConfig(
    val sets: Int,              // 组数
    val repsPerSet: Int?,       // 每组次数（可选）
    val restDuration: Duration, // 组间休息时长
    val exerciseName: String = "" // 动作名称
)
