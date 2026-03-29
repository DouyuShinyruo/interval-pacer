package com.github.intervalpacer.presentation.settraining

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.intervalpacer.IntervalPacerApp
import com.github.intervalpacer.core.tts.TTSManager
import com.github.intervalpacer.core.tts.Urgency
import com.github.intervalpacer.core.tts.VoicePromptGenerator
import com.github.intervalpacer.core.vibration.VibrationManager
import com.github.intervalpacer.data.local.SharedPreferencesManager
import com.github.intervalpacer.data.model.StrengthConfig
import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.model.SetTrainingConfig
import com.github.intervalpacer.domain.model.WorkoutType
import com.github.intervalpacer.domain.repository.HistoryRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toDuration

/**
 * 力量训练状态
 */
sealed class SetTrainingState {
    data object Idle : SetTrainingState()
    data class Exercising(
        val currentSet: Int,
        val setDuration: Duration,
        val completedSets: Int
    ) : SetTrainingState()
    data class Resting(
        val completedSet: Int,
        val restRemaining: Duration,
        val totalRest: Duration,
        val nextSet: Int
    ) : SetTrainingState()
    data class Paused(
        val lastState: SetTrainingState
    ) : SetTrainingState()
    data class Completed(
        val totalSets: Int,
        val totalDuration: Duration
    ) : SetTrainingState()
}

@OptIn(ExperimentalTime::class)
class SetTrainingViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsManager = TTSManager(application)
    private val vibrationManager = VibrationManager(application)
    private val prefsManager = SharedPreferencesManager(application)
    private val voicePromptGenerator = VoicePromptGenerator()
    private val historyRepository: HistoryRepository =
        (application as IntervalPacerApp).historyRepository

    private val _trainingState = MutableStateFlow<SetTrainingState>(SetTrainingState.Idle)
    val trainingState: StateFlow<SetTrainingState> = _trainingState.asStateFlow()

    var config: SetTrainingConfigUi = SetTrainingConfigUi()
        private set

    private var currentConfig: SetTrainingConfig? = null
    private var completedSets: Int = 0
    private var setStartTime: Long = 0L
    private var trainingStartTime: Long = 0L
    private var restJob: Job? = null
    private var setTimerJob: Job? = null

    val startTime: Long get() = trainingStartTime

    init {
        vibrationManager.enabled = prefsManager.vibrationEnabled
    }

    /**
     * 开始训练
     */
    fun startTraining() {
        val config = SetTrainingConfig(
            exerciseName = config.exerciseName,
            totalSets = config.totalSets,
            restDuration = config.restMinutes.minutes + config.restSeconds.seconds,
            enableVoicePrompt = prefsManager.voiceEnabled,
            enableVibration = prefsManager.vibrationEnabled
        )
        currentConfig = config
        completedSets = 0
        trainingStartTime = System.currentTimeMillis()

        // 开始第一组
        startSet(1)
    }

    /**
     * 开始某一组
     */
    private fun startSet(setNumber: Int) {
        setStartTime = System.currentTimeMillis()
        vibrationManager.phaseTransition()

        _trainingState.value = SetTrainingState.Exercising(
            currentSet = setNumber,
            setDuration = Duration.ZERO,
            completedSets = completedSets
        )

        // 语音播报
        if (ttsManager.isReady.value && currentConfig?.enableVoicePrompt == true) {
            ttsManager.speak("第${setNumber}组，开始", Urgency.HIGH)
        }

        // 启动计时
        setTimerJob = viewModelScope.launch {
            var elapsed = Duration.ZERO
            while (_trainingState.value is SetTrainingState.Exercising) {
                val currentState = _trainingState.value as? SetTrainingState.Exercising ?: break
                _trainingState.value = currentState.copy(setDuration = elapsed)
                delay(1.seconds)
                elapsed += 1.seconds
            }
        }
    }

    /**
     * 完成本组（手动触发）
     */
    fun completeSet() {
        setTimerJob?.cancel()
        completedSets++
        vibrationManager.completion()

        if (ttsManager.isReady.value && currentConfig?.enableVoicePrompt == true) {
            val restText = voicePromptGenerator.formatDuration(currentConfig?.restDuration ?: 90.seconds)
            ttsManager.speak("组完成，休息${restText}", Urgency.HIGH)
        }

        // 如果还有下一组，进入休息
        if (completedSets < (currentConfig?.totalSets ?: config.totalSets)) {
            startRest(completedSets, completedSets + 1)
        } else {
            // 全部完成
            finishTraining()
        }
    }

    /**
     * 开始组间休息
     */
    private fun startRest(completedSet: Int, nextSet: Int) {
        val restDuration = currentConfig?.restDuration ?: 90.seconds

        _trainingState.value = SetTrainingState.Resting(
            completedSet = completedSet,
            restRemaining = restDuration,
            totalRest = restDuration,
            nextSet = nextSet
        )

        restJob = viewModelScope.launch {
            var remaining = restDuration
            while (remaining > Duration.ZERO && _trainingState.value is SetTrainingState.Resting) {
                delay(1.seconds)
                remaining -= 1.seconds
                val currentState = _trainingState.value as? SetTrainingState.Resting ?: break
                _trainingState.value = currentState.copy(restRemaining = remaining)
            }

            // 休息结束
            if (_trainingState.value is SetTrainingState.Resting) {
                vibrationManager.phaseTransition()
                if (ttsManager.isReady.value && currentConfig?.enableVoicePrompt == true) {
                    ttsManager.speak("休息结束，准备第${nextSet}组", Urgency.HIGH)
                }
                startSet(nextSet)
            }
        }
    }

    /**
     * 跳过休息
     */
    fun skipRest() {
        restJob?.cancel()
        val currentState = _trainingState.value as? SetTrainingState.Resting ?: return
        vibrationManager.phaseTransition()
        startSet(currentState.nextSet)
    }

    /**
     * 暂停训练
     */
    fun pauseTraining() {
        setTimerJob?.cancel()
        restJob?.cancel()
        val currentState = _trainingState.value
        if (currentState is SetTrainingState.Exercising || currentState is SetTrainingState.Resting) {
            _trainingState.value = SetTrainingState.Paused(currentState)
            ttsManager.stop()
            vibrationManager.cancel()
        }
    }

    /**
     * 恢复训练
     */
    fun resumeTraining() {
        val currentState = _trainingState.value as? SetTrainingState.Paused ?: return
        when (val lastState = currentState.lastState) {
            is SetTrainingState.Exercising -> startSet(lastState.currentSet)
            is SetTrainingState.Resting -> startRest(lastState.completedSet, lastState.nextSet)
            else -> {}
        }
    }

    /**
     * 完成训练（所有组数完成后自动调用）
     */
    private fun finishTraining() {
        setTimerJob?.cancel()
        restJob?.cancel()
        val totalDuration = ((System.currentTimeMillis() - trainingStartTime) / 1000.0).seconds
        val endTime = Clock.System.now()

        _trainingState.value = SetTrainingState.Completed(
            totalSets = completedSets,
            totalDuration = totalDuration
        )

        // 保存训练记录
        saveSetTrainingRecord(totalDuration, endTime)

        if (ttsManager.isReady.value && completedSets > 0) {
            ttsManager.announceCompleted(completedSets)
        }
        vibrationManager.cancel()
    }

    /**
     * 停止训练（手动提前结束）
     */
    fun stopTraining() {
        finishTraining()
    }

    /**
     * 保存力量训练记录
     */
    private fun saveSetTrainingRecord(
        totalDuration: Duration,
        endTime: Instant
    ) {
        val cfg = currentConfig ?: return
        val startTime = Instant.fromEpochMilliseconds(trainingStartTime)

        val record = WorkoutRecord(
            id = "",
            type = WorkoutType.STRENGTH_TRAINING,
            intervalConfig = null,
            strengthConfig = StrengthConfig(
                sets = cfg.totalSets,
                repsPerSet = null,
                restDuration = cfg.restDuration,
                exerciseName = cfg.exerciseName
            ),
            startTime = startTime,
            endTime = endTime,
            totalDuration = totalDuration,
            completedRounds = completedSets,
            targetRounds = cfg.totalSets,
            isCompleted = completedSets >= cfg.totalSets
        )

        viewModelScope.launch {
            historyRepository.saveRecord(record)
        }
    }

    /**
     * 重置到空闲状态
     */
    fun resetToIdle() {
        setTimerJob?.cancel()
        restJob?.cancel()
        _trainingState.value = SetTrainingState.Idle
        ttsManager.stop()
        vibrationManager.cancel()
        completedSets = 0
        currentConfig = null
    }

    /**
     * 更新配置
     */
    fun updateConfig(newConfig: SetTrainingConfigUi) {
        this.config = newConfig
    }

    override fun onCleared() {
        super.onCleared()
        setTimerJob?.cancel()
        restJob?.cancel()
        ttsManager.release()
        vibrationManager.cancel()
    }
}

/**
 * UI 配置数据类
 */
data class SetTrainingConfigUi(
    val exerciseName: String = "",
    val totalSets: Int = 5,
    val restMinutes: Int = 1,
    val restSeconds: Int = 30
) {
    fun getRestDescription(): String {
        return when {
            restMinutes > 0 && restSeconds > 0 -> "休息${restMinutes}分${restSeconds}秒"
            restMinutes > 0 -> "休息${restMinutes}分钟"
            restSeconds > 0 -> "休息${restSeconds}秒"
            else -> "无休息"
        }
    }

    fun getSummary(): String {
        val name = if (exerciseName.isNotEmpty()) exerciseName else "力量训练"
        return "$name · $totalSets 组 · ${getRestDescription()}"
    }
}
