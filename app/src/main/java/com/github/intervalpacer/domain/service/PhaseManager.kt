package com.github.intervalpacer.domain.service

import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.Phase

/**
 * 阶段状态管理器
 * 负责管理训练阶段的转换逻辑
 */
class PhaseManager {

    private val phaseSequence: MutableList<Phase> = mutableListOf()

    /**
     * 初始化阶段序列
     */
    fun initializePhases(config: IntervalConfig): List<Phase> {
        phaseSequence.clear()

        // 添加热身（如果有）
        if (config.warmupDuration > kotlin.time.Duration.ZERO) {
            phaseSequence.add(Phase.Warmup)
        }

        // 添加主要的跑走间歇循环（根据 runFirst 决定顺序）
        repeat(config.repeatCount) { round ->
            if (config.runFirst) {
                phaseSequence.add(Phase.Run)
                phaseSequence.add(Phase.Walk)
            } else {
                phaseSequence.add(Phase.Walk)
                phaseSequence.add(Phase.Run)
            }
        }

        // 添加冷身（如果有）
        if (config.cooldownDuration > kotlin.time.Duration.ZERO) {
            phaseSequence.add(Phase.Cooldown)
        }

        // 最后添加完成阶段
        phaseSequence.add(Phase.Completed)

        return phaseSequence.toList()
    }

    /**
     * 获取下一个阶段
     */
    fun getNextPhase(currentPhase: Phase, config: IntervalConfig): Phase? {
        if (phaseSequence.isEmpty()) {
            initializePhases(config)
        }

        val currentIndex = phaseSequence.indexOf(currentPhase)
        return if (currentIndex >= 0 && currentIndex < phaseSequence.size - 1) {
            phaseSequence[currentIndex + 1]
        } else {
            Phase.Completed
        }
    }

    /**
     * 获取当前阶段的时长
     */
    fun getPhaseDuration(phase: Phase, config: IntervalConfig): kotlin.time.Duration {
        return when (phase) {
            is Phase.Warmup -> config.warmupDuration
            is Phase.Run -> config.runDuration
            is Phase.Walk -> config.walkDuration
            is Phase.Cooldown -> config.cooldownDuration
            is Phase.Completed -> kotlin.time.Duration.ZERO
        }
    }

    /**
     * 获取当前阶段在序列中的索引
     */
    fun getPhaseIndex(phase: Phase, config: IntervalConfig): Int {
        if (phaseSequence.isEmpty()) {
            initializePhases(config)
        }
        return phaseSequence.indexOf(phase)
    }

    /**
     * 获取总阶段数
     */
    fun getTotalPhases(config: IntervalConfig): Int {
        if (phaseSequence.isEmpty()) {
            initializePhases(config)
        }
        return phaseSequence.size
    }

    /**
     * 判断是否应该在特定时间播报语音
     */
    fun shouldAnnounceAt(
        phase: Phase,
        secondsRemaining: Int,
        config: IntervalConfig
    ): AnnouncementType {
        val phaseDuration = getPhaseDuration(phase, config).inWholeSeconds
        val elapsed = phaseDuration - secondsRemaining

        return when {
            // 阶段刚开始（前3秒）- 播报阶段名称
            elapsed <= 3 && secondsRemaining > 0 -> AnnouncementType.PHASE_START

            // 最后1分钟 - 如果阶段超过2分钟
            secondsRemaining == 60 && phaseDuration > 120 -> AnnouncementType.ONE_MINUTE_REMAINING

            // 最后30秒 - 如果阶段超过1分钟
            secondsRemaining == 30 && phaseDuration > 60 -> AnnouncementType.THIRTY_SECONDS_REMAINING

            // 最后10秒开始倒数
            secondsRemaining in 1..10 -> AnnouncementType.COUNTDOWN

            // 其他情况不播报
            else -> AnnouncementType.NONE
        }
    }

    /**
     * 重置阶段序列
     */
    fun reset() {
        phaseSequence.clear()
    }
}

/**
 * 语音播报类型
 */
enum class AnnouncementType {
    NONE,                   // 不播报
    PHASE_START,            // 阶段开始
    ONE_MINUTE_REMAINING,   // 剩余1分钟
    THIRTY_SECONDS_REMAINING, // 剩余30秒
    COUNTDOWN               // 倒数（10-1）
}
