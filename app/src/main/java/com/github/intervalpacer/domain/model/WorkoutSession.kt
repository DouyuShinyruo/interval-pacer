package com.github.intervalpacer.domain.model

import kotlin.time.Duration
import kotlin.time.Instant as KotlinInstant
import kotlin.time.Duration.Companion.milliseconds

/**
 * 训练会话状态
 */
sealed class WorkoutState {
    /** 空闲状态 */
    data object Idle : WorkoutState()

    /** 准备中 */
    data object Preparing : WorkoutState()

    /** 训练中 */
    data class Active(
        val currentPhase: Phase,
        val phaseProgress: Duration,
        val overallProgress: Float, // 0.0 to 1.0
        val completedRounds: Int
    ) : WorkoutState()

    /** 暂停 */
    data class Paused(
        val lastState: Active
    ) : WorkoutState()

    /** 已完成 */
    data class Completed(
        val totalDuration: Duration,
        val completedRounds: Int
    ) : WorkoutState()
}

/**
 * 训练阶段
 */
sealed class Phase {
    /** 热身 */
    data object Warmup : Phase()

    /** 跑步 */
    data object Run : Phase()

    /** 走路/休息 */
    data object Walk : Phase()

    /** 冷身 */
    data object Cooldown : Phase()

    /** 已完成 */
    data object Completed : Phase()

    /**
     * 获取阶段显示名称
     */
    fun getDisplayName(): String = when (this) {
        is Warmup -> "热身"
        is Run -> "跑步"
        is Walk -> "步行"
        is Cooldown -> "冷身"
        is Completed -> "完成"
    }

    /**
     * 获取阶段颜色（用于UI）
     */
    fun getColor(): PhaseColor = when (this) {
        is Warmup -> PhaseColor.YELLOW
        is Run -> PhaseColor.ORANGE
        is Walk -> PhaseColor.GREEN
        is Cooldown -> PhaseColor.BLUE
        is Completed -> PhaseColor.GRAY
    }
}

/**
 * 阶段颜色
 */
enum class PhaseColor {
    YELLOW,  // 热身
    ORANGE,  // 跑步（高强度）
    GREEN,   // 步行（低强度）
    BLUE,    // 冷身
    GRAY     // 完成
}

/**
 * 训练会话
 */
@OptIn(kotlin.time.ExperimentalTime::class)
data class WorkoutSession(
    val id: String,
    val type: WorkoutType,
    val config: IntervalConfig,
    val startTime: KotlinInstant,
    val endTime: KotlinInstant? = null,
    val state: WorkoutState = WorkoutState.Idle
) {
    /**
     * 获取实际训练时长
     */
    fun getActualDuration(): Duration {
        // 使用系统时间计算时长
        val end = endTime?.toEpochMilliseconds() ?: System.currentTimeMillis()
        val start = startTime.toEpochMilliseconds()
        return (end - start).milliseconds
    }

    /**
     * 是否已完成
     */
    fun isCompleted(): Boolean = state is WorkoutState.Completed
}

/**
 * 训练类型
 */
enum class WorkoutType {
    /** 走跑间歇 */
    INTERVAL_RUN,

    /** 力量训练 */
    STRENGTH_TRAINING
}
