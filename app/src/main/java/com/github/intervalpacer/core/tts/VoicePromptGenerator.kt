package com.github.intervalpacer.core.tts

import com.github.intervalpacer.domain.model.Phase
import com.github.intervalpacer.domain.service.AnnouncementRequest
import com.github.intervalpacer.domain.service.AnnouncementType

/**
 * 语音提示生成器
 * 根据不同场景生成合适的语音内容
 */
class VoicePromptGenerator {

    /**
     * 生成语音提示文本
     */
    fun generatePrompt(request: AnnouncementRequest): String? {
        return when (request.type) {
            AnnouncementType.NONE -> null
            AnnouncementType.PHASE_START -> generatePhaseStartPrompt(request.phase)
            AnnouncementType.ONE_MINUTE_REMAINING -> "还有1分钟"
            AnnouncementType.THIRTY_SECONDS_REMAINING -> "30秒"
            AnnouncementType.COUNTDOWN -> request.secondsRemaining.toString()
        }
    }

    /**
     * 生成阶段开始提示
     */
    private fun generatePhaseStartPrompt(phase: Phase): String {
        return when (phase) {
            is Phase.Warmup -> "热身"
            is Phase.Run -> "跑步"
            is Phase.Walk -> "步行"
            is Phase.Cooldown -> "冷身"
            is Phase.Completed -> "完成"
        }
    }

    /**
     * 生成带时长的阶段提示
     */
    fun generatePhaseStartWithDuration(phase: Phase, duration: kotlin.time.Duration): String {
        val phaseName = generatePhaseStartPrompt(phase)
        val timeText = formatDuration(duration)
        return "$phaseName，$timeText"
    }

    /**
     * 格式化时长为易读文本
     */
    fun formatDuration(duration: kotlin.time.Duration): String {
        val minutes = duration.inWholeMinutes.toInt()
        val seconds = (duration.inWholeSeconds % 60).toInt()

        return when {
            minutes > 0 && seconds > 0 -> "${minutes}分${seconds}秒"
            minutes > 0 -> "${minutes}分钟"
            seconds > 0 -> "${seconds}秒"
            else -> ""
        }
    }

    /**
     * 生成倒计时序列（3-2-1）
     */
    fun generateCountdownSequence(): List<String> {
        return listOf("3", "2", "1")
    }

    /**
     * 生成完成提示
     */
    fun generateCompletionPrompt(completedRounds: Int): String {
        return "恭喜完成，共${completedRounds}组"
    }

    /**
     * 生成暂停提示
     */
    fun generatePausePrompt(): String {
        return "运动已暂停"
    }

    /**
     * 生成恢复提示
     */
    fun generateResumePrompt(phase: Phase): String {
        val phaseName = generatePhaseStartPrompt(phase)
        return "继续$phaseName，3，2，1"
    }
}
