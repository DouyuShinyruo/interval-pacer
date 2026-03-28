package com.github.intervalpacer.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.intervalpacer.core.tts.TTSManager
import com.github.intervalpacer.data.local.DarkModeOption
import com.github.intervalpacer.data.local.PhasePromptStyle
import com.github.intervalpacer.data.local.SharedPreferencesManager
import com.github.intervalpacer.data.local.TimeReminderOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 设置状态
 */
data class SettingsState(
    // 语音设置
    val voiceEnabled: Boolean = true,
    val countdownVoice: Boolean = true,
    val timeReminder: TimeReminderOption = TimeReminderOption.ONE_MINUTE,
    val phasePromptStyle: PhasePromptStyle = PhasePromptStyle.SIMPLE,
    val speechRate: Float = 1.0f,

    // 显示设置
    val keepScreenOn: Boolean = true,
    val darkMode: DarkModeOption = DarkModeOption.FOLLOW_SYSTEM,

    // 交互设置
    val vibrationEnabled: Boolean = true,
    val lockScreenNotification: Boolean = true
)

/**
 * 设置 ViewModel
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = SharedPreferencesManager(application)
    private val ttsManager = TTSManager(application)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        // 应用保存的语速设置到 TTS
        applySpeechRate()
    }

    /**
     * 从 SharedPreferences 加载设置
     */
    private fun loadSettings(): SettingsState {
        return SettingsState(
            voiceEnabled = prefsManager.voiceEnabled,
            countdownVoice = prefsManager.countdownVoice,
            timeReminder = prefsManager.timeReminder,
            phasePromptStyle = prefsManager.phasePromptStyle,
            speechRate = prefsManager.speechRate,
            keepScreenOn = prefsManager.keepScreenOn,
            darkMode = prefsManager.darkMode,
            vibrationEnabled = prefsManager.vibrationEnabled,
            lockScreenNotification = prefsManager.lockScreenNotification
        )
    }

    // ========== 语音设置 ==========

    fun setVoiceEnabled(enabled: Boolean) {
        prefsManager.saveVoiceEnabled(enabled)
        updateState { it.copy(voiceEnabled = enabled) }
    }

    fun setCountdownVoice(enabled: Boolean) {
        prefsManager.saveCountdownVoice(enabled)
        updateState { it.copy(countdownVoice = enabled) }
    }

    fun setTimeReminder(option: TimeReminderOption) {
        prefsManager.saveTimeReminder(option)
        updateState { it.copy(timeReminder = option) }
    }

    fun setPhasePromptStyle(style: PhasePromptStyle) {
        prefsManager.savePhasePromptStyle(style)
        updateState { it.copy(phasePromptStyle = style) }
    }

    fun setSpeechRate(rate: Float) {
        prefsManager.saveSpeechRate(rate)
        updateState { it.copy(speechRate = rate) }
        applySpeechRate()
    }

    /**
     * 应用语速到 TTS 引擎
     */
    private fun applySpeechRate() {
        ttsManager.setSpeechRate(prefsManager.speechRate)
    }

    /**
     * 测试语音
     */
    fun testVoice() {
        if (prefsManager.voiceEnabled && ttsManager.isReady.value) {
            ttsManager.speak("语音测试", com.github.intervalpacer.core.tts.Urgency.NORMAL)
        }
    }

    // ========== 显示设置 ==========

    fun setKeepScreenOn(enabled: Boolean) {
        prefsManager.saveKeepScreenOn(enabled)
        updateState { it.copy(keepScreenOn = enabled) }
    }

    fun setDarkMode(option: DarkModeOption) {
        prefsManager.saveDarkMode(option)
        updateState { it.copy(darkMode = option) }
    }

    // ========== 交互设置 ==========

    fun setVibrationEnabled(enabled: Boolean) {
        prefsManager.saveVibrationEnabled(enabled)
        updateState { it.copy(vibrationEnabled = enabled) }
    }

    fun setLockScreenNotification(enabled: Boolean) {
        prefsManager.saveLockScreenNotification(enabled)
        updateState { it.copy(lockScreenNotification = enabled) }
    }

    /**
     * 恢复默认设置
     */
    fun resetToDefault() {
        prefsManager.clearAll()
        _settingsState.value = loadSettings()
        applySpeechRate()
    }

    /**
     * 更新状态的辅助方法
     */
    private fun updateState(transform: (SettingsState) -> SettingsState) {
        _settingsState.value = transform(_settingsState.value)
    }

    override fun onCleared() {
        super.onCleared()
        // TTSManager 由 Activity 层管理，这里不需要释放
    }
}
