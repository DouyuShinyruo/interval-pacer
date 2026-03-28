package com.github.intervalpacer.presentation.interval

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.intervalpacer.IntervalPacerApp
import com.github.intervalpacer.core.tts.TTSManager
import com.github.intervalpacer.data.local.SharedPreferencesManager
import com.github.intervalpacer.core.tts.VoicePromptGenerator
import com.github.intervalpacer.core.vibration.VibrationManager
import com.github.intervalpacer.data.model.StrengthConfig
import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.Phase
import com.github.intervalpacer.domain.model.WorkoutState
import com.github.intervalpacer.domain.model.WorkoutType
import com.github.intervalpacer.domain.repository.HistoryRepository
import com.github.intervalpacer.domain.service.AnnouncementRequest
import com.github.intervalpacer.domain.service.TimerService
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class IntervalViewModel(application: Application) : AndroidViewModel(application) {

    private val timerService = TimerService(viewModelScope)
    private val ttsManager = TTSManager(application)
    private val vibrationManager = VibrationManager(application)
    private val prefsManager = SharedPreferencesManager(application)
    private val voicePromptGenerator = VoicePromptGenerator()
    private val historyRepository: HistoryRepository =
        (application as IntervalPacerApp).historyRepository

    val timerState: StateFlow<WorkoutState> = timerService.timerState
    val currentPhase: StateFlow<Phase> = timerService.currentPhase

    private var trainingStartTime: Long = 0L
    val startTime: Long get() = trainingStartTime
    private var currentIntervalConfig: IntervalConfig? = null

    // 默认配置：新手预设
    var config: IntervalConfigUi = IntervalConfigUi(
        runMinutes = 1,
        runSeconds = 0,
        walkMinutes = 2,
        walkSeconds = 0,
        rounds = 5,
        runFirst = true
    )
        private set

    init {
        // 读取震动设置
        vibrationManager.enabled = prefsManager.vibrationEnabled

        // 监听语音播报请求
        viewModelScope.launch {
            timerService.announcementFlow.collect { request ->
                request?.let { handleAnnouncement(it) }
            }
        }

        // 监听训练完成，自动保存记录
        viewModelScope.launch {
            timerService.timerState.collect { state ->
                if (state is WorkoutState.Completed) {
                    saveWorkoutRecord(state)
                }
            }
        }
    }

    /**
     * 开始训练
     */
    fun startWorkout() {
        try {
            // 将 UI 配置转换为 IntervalConfig
            val runDuration = config.runMinutes.minutes + config.runSeconds.seconds
            val walkDuration = config.walkMinutes.minutes + config.walkSeconds.seconds

            val intervalConfig = IntervalConfig(
                runDuration = runDuration,
                walkDuration = walkDuration,
                repeatCount = config.rounds,
                enableVoicePrompt = prefsManager.voiceEnabled,
                enableVibration = prefsManager.vibrationEnabled,
                runFirst = config.runFirst
            )

            currentIntervalConfig = intervalConfig
            trainingStartTime = System.currentTimeMillis()

            timerService.start(intervalConfig)

            // 播报开始提示（仅在 TTS 准备好时）
            if (ttsManager.isReady.value) {
                ttsManager.speak("开始训练", com.github.intervalpacer.core.tts.Urgency.HIGH)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 暂停训练
     */
    fun pauseWorkout() {
        try {
            timerService.pause()
            vibrationManager.cancel()
            if (ttsManager.isReady.value) {
                ttsManager.announcePause()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 恢复训练
     */
    fun resumeWorkout() {
        try {
            timerService.resume()
            val phase = currentPhase.value
            if (ttsManager.isReady.value) {
                ttsManager.announceResume(phase)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 停止训练
     */
    fun stopWorkout() {
        try {
            timerService.stop()
            vibrationManager.cancel()
            ttsManager.stop()

            val state = timerState.value
            if (state is WorkoutState.Completed && ttsManager.isReady.value) {
                ttsManager.announceCompleted(state.completedRounds)
                vibrationManager.completion()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 跳过当前阶段
     */
    fun skipPhase() {
        timerService.skipToNextPhase()
        vibrationManager.phaseTransition()
    }

    /**
     * 更新配置
     */
    fun updateConfig(newConfig: IntervalConfigUi) {
        this.config = newConfig
    }

    /**
     * 重置到空闲状态（返回主页）
     */
    fun resetToIdle() {
        timerService.reset()
        ttsManager.stop()
        vibrationManager.cancel()
    }

    /**
     * 保存训练记录
     */
    private fun saveWorkoutRecord(state: WorkoutState.Completed) {
        val intervalConfig = currentIntervalConfig ?: return
        val startTime = if (trainingStartTime > 0L) {
            Instant.fromEpochMilliseconds(trainingStartTime)
        } else return

        val record = WorkoutRecord(
            id = "",
            type = WorkoutType.INTERVAL_RUN,
            intervalConfig = intervalConfig,
            strengthConfig = null,
            startTime = startTime,
            endTime = Clock.System.now(),
            totalDuration = state.totalDuration,
            completedRounds = state.completedRounds,
            targetRounds = intervalConfig.repeatCount,
            isCompleted = state.completedRounds >= intervalConfig.repeatCount
        )

        viewModelScope.launch {
            historyRepository.saveRecord(record)
        }
    }

    /**
     * 处理语音播报请求
     */
    private fun handleAnnouncement(request: AnnouncementRequest) {
        try {
            if (!ttsManager.isReady.value) return

            val promptText = voicePromptGenerator.generatePrompt(request)
            promptText?.let { text ->
                val urgency = when (request.type) {
                    com.github.intervalpacer.domain.service.AnnouncementType.PHASE_START,
                    com.github.intervalpacer.domain.service.AnnouncementType.COUNTDOWN -> com.github.intervalpacer.core.tts.Urgency.HIGH
                    else -> com.github.intervalpacer.core.tts.Urgency.NORMAL
                }

                ttsManager.speak(text, urgency)

                // 阶段开始时震动
                if (request.type == com.github.intervalpacer.domain.service.AnnouncementType.PHASE_START) {
                    vibrationManager.phaseTransition()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.release()
        vibrationManager.cancel()
    }
}
