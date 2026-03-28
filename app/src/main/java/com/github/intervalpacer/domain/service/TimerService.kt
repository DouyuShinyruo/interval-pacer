package com.github.intervalpacer.domain.service

import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.Phase
import com.github.intervalpacer.domain.model.WorkoutState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * 计时器核心服务
 * 负责处理计时逻辑和状态更新
 */
class TimerService(
    private val scope: CoroutineScope,
    private val phaseManager: PhaseManager = PhaseManager()
) {
    private var timerJob: Job? = null
    private var currentConfig: IntervalConfig? = null
    private var currentPhaseIndex: Int = 0
    private var completedRounds: Int = 0
    private var phaseStartTime: Long = 0

    private val _timerState = MutableStateFlow<WorkoutState>(WorkoutState.Idle)
    val timerState: StateFlow<WorkoutState> = _timerState.asStateFlow()

    private val _currentPhase = MutableStateFlow<Phase>(Phase.Warmup)
    val currentPhase: StateFlow<Phase> = _currentPhase.asStateFlow()

    /**
     * 开始计时
     */
    fun start(config: IntervalConfig) {
        currentConfig = config
        currentPhaseIndex = 0
        completedRounds = 0

        // 初始化阶段序列
        val phases = phaseManager.initializePhases(config)
        if (phases.isEmpty()) return

        val firstPhase = phases.first()
        _currentPhase.value = firstPhase
        phaseStartTime = System.currentTimeMillis()

        _timerState.value = WorkoutState.Active(
            currentPhase = firstPhase,
            phaseProgress = Duration.ZERO,
            overallProgress = 0f,
            completedRounds = 0
        )

        startCountdown(phaseManager.getPhaseDuration(firstPhase, config))
    }

    /**
     * 暂停计时
     */
    fun pause() {
        timerJob?.cancel()
        val currentState = _timerState.value
        if (currentState is WorkoutState.Active) {
            _timerState.value = WorkoutState.Paused(currentState)
        }
    }

    /**
     * 恢复计时
     */
    fun resume() {
        val currentState = _timerState.value
        if (currentState is WorkoutState.Paused) {
            _timerState.value = currentState.lastState
            val remainingTime = currentState.lastState.phaseProgress
            phaseStartTime = System.currentTimeMillis() - remainingTime.inWholeMilliseconds
            startCountdown(phaseManager.getPhaseDuration(currentState.lastState.currentPhase, currentConfig!!))
        }
    }

    /**
     * 停止计时
     */
    fun stop() {
        timerJob?.cancel()
        phaseManager.reset()

        val duration = calculateTotalDuration()

        _timerState.value = WorkoutState.Completed(
            totalDuration = duration,
            completedRounds = completedRounds
        )
    }

    /**
     * 重置到空闲状态（用于返回主页）
     */
    fun reset() {
        timerJob?.cancel()
        phaseManager.reset()
        _timerState.value = WorkoutState.Idle
        _currentPhase.value = Phase.Warmup
        currentConfig = null
        currentPhaseIndex = 0
        completedRounds = 0
    }

    /**
     * 跳过当前阶段
     */
    fun skipToNextPhase() {
        timerJob?.cancel()
        transitionToNextPhase()
    }

    /**
     * 开始倒计时
     */
    private fun startCountdown(duration: Duration) {
        timerJob = scope.launch {
            var remainingTime = duration
            val totalPhaseDuration = duration

            while (remainingTime > Duration.ZERO && _timerState.value is WorkoutState.Active) {
                // 计算已过时间
                val elapsed = totalPhaseDuration - remainingTime
                val overallProgress = calculateOverallProgress(elapsed, totalPhaseDuration)

                // 更新状态
                val currentState = _timerState.value as? WorkoutState.Active ?: return@launch
                _timerState.value = currentState.copy(
                    phaseProgress = remainingTime,
                    overallProgress = overallProgress,
                    completedRounds = completedRounds
                )

                // 检查是否需要播报语音
                checkAnnouncements(currentState.currentPhase, remainingTime.inWholeSeconds.toInt())

                // 等待1秒
                delay(1.seconds)
                remainingTime -= 1.seconds
            }

            // 阶段完成，转换到下一个阶段
            if (_timerState.value is WorkoutState.Active) {
                transitionToNextPhase()
            }
        }
    }

    /**
     * 转换到下一个阶段
     */
    private fun transitionToNextPhase() {
        val config = currentConfig ?: return
        val currentPhase = _currentPhase.value

        // 更新完成轮数（Walk 完成标志一组 Run+Walk 完成）
        if (currentPhase is Phase.Walk) {
            completedRounds++
        }

        // 获取下一个阶段（使用索引查找，避免 data object 的 indexOf 问题）
        val nextPhase = phaseManager.getNextPhase(currentPhaseIndex)
        if (nextPhase == null || nextPhase is Phase.Completed) {
            // 训练完成
            val totalDuration = calculateTotalDuration()
            _timerState.value = WorkoutState.Completed(
                totalDuration = totalDuration,
                completedRounds = completedRounds
            )
            return
        }

        // 切换到下一个阶段
        currentPhaseIndex++
        _currentPhase.value = nextPhase
        phaseStartTime = System.currentTimeMillis()

        val phaseDuration = phaseManager.getPhaseDuration(nextPhase, config)
        _timerState.value = WorkoutState.Active(
            currentPhase = nextPhase,
            phaseProgress = phaseDuration,
            overallProgress = calculateOverallProgress(Duration.ZERO, phaseDuration),
            completedRounds = completedRounds
        )

        startCountdown(phaseDuration)
    }

    /**
     * 计算整体进度
     */
    private fun calculateOverallProgress(elapsed: Duration, phaseDuration: Duration): Float {
        val config = currentConfig ?: return 0f
        val totalPhases = phaseManager.getTotalPhases(config)
        val currentPhaseProgress = elapsed.inWholeMilliseconds.toFloat() / phaseDuration.inWholeMilliseconds.toFloat()
        return (currentPhaseIndex.toFloat() + currentPhaseProgress) / totalPhases.toFloat()
    }

    /**
     * 计算总训练时长
     */
    private fun calculateTotalDuration(): Duration {
        return currentConfig?.getFullDuration() ?: Duration.ZERO
    }

    /**
     * 检查是否需要播报语音
     */
    private fun checkAnnouncements(phase: Phase, secondsRemaining: Int) {
        val config = currentConfig ?: return
        val announcementType = phaseManager.shouldAnnounceAt(phase, secondsRemaining, config)

        // 这里会触发语音播报，实际实现由TTSManager处理
        // 可以通过回调或Flow来通知TTSManager
        _announcementFlow.tryEmit(
            AnnouncementRequest(
                type = announcementType,
                phase = phase,
                secondsRemaining = secondsRemaining
            )
        )
    }

    // 语音播报请求流
    private val _announcementFlow = MutableStateFlow<AnnouncementRequest?>(null)
    val announcementFlow: StateFlow<AnnouncementRequest?> = _announcementFlow.asStateFlow()
}

/**
 * 语音播报请求
 */
data class AnnouncementRequest(
    val type: AnnouncementType,
    val phase: Phase,
    val secondsRemaining: Int
)
