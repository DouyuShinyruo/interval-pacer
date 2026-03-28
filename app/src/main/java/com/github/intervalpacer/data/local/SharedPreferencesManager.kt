package com.github.intervalpacer.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * 设置选项枚举
 */
enum class TimeReminderOption {
    NONE,               // 关闭
    ONE_MINUTE,         // 最后1分钟
    THIRTY_SECONDS      // 最后30秒
}

enum class PhasePromptStyle {
    DETAILED,   // 详细（阶段名 + 时长）
    SIMPLE,     // 简洁（仅阶段名）
    OFF         // 关闭
}

enum class DarkModeOption {
    FOLLOW_SYSTEM,  // 跟随系统
    ALWAYS_ON,      // 始终开启
    ALWAYS_OFF      // 始终关闭
}

/**
 * SharedPreferences 管理器
 * 负责设置的持久化存储
 */
class SharedPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "interval_pacer_settings"

        // 语音设置 Key
        private const val KEY_VOICE_ENABLED = "voice_enabled"
        private const val KEY_COUNTDOWN_VOICE = "countdown_voice"
        private const val KEY_TIME_REMINDER = "time_reminder"
        private const val KEY_PHASE_PROMPT_STYLE = "phase_prompt_style"
        private const val KEY_SPEECH_RATE = "speech_rate"

        // 显示设置 Key
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_DARK_MODE = "dark_mode"

        // 交互设置 Key
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_LOCK_SCREEN_NOTIFICATION = "lock_screen_notification"

        // 默认值
        private const val DEFAULT_VOICE_ENABLED = true
        private const val DEFAULT_COUNTDOWN_VOICE = true
        private const val DEFAULT_TIME_REMINDER = "one_minute"  // 对应 TimeReminderOption.ONE_MINUTE
        private const val DEFAULT_PHASE_PROMPT_STYLE = "simple"  // 对应 PhasePromptStyle.SIMPLE
        private const val DEFAULT_SPEECH_RATE = 1.0f
        private const val DEFAULT_KEEP_SCREEN_ON = true
        private const val DEFAULT_DARK_MODE = "follow_system"  // 对应 DarkModeOption.FOLLOW_SYSTEM
        private const val DEFAULT_VIBRATION_ENABLED = true
        private const val DEFAULT_LOCK_SCREEN_NOTIFICATION = true
    }

    // ========== 语音设置 ==========

    val voiceEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_ENABLED, DEFAULT_VOICE_ENABLED)

    fun saveVoiceEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VOICE_ENABLED, enabled).apply()
    }

    val countdownVoice: Boolean
        get() = prefs.getBoolean(KEY_COUNTDOWN_VOICE, DEFAULT_COUNTDOWN_VOICE)

    fun saveCountdownVoice(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_COUNTDOWN_VOICE, enabled).apply()
    }

    val timeReminder: TimeReminderOption
        get() = when (prefs.getString(KEY_TIME_REMINDER, DEFAULT_TIME_REMINDER)) {
            "none" -> TimeReminderOption.NONE
            "thirty_seconds" -> TimeReminderOption.THIRTY_SECONDS
            else -> TimeReminderOption.ONE_MINUTE
        }

    fun saveTimeReminder(option: TimeReminderOption) {
        val value = when (option) {
            TimeReminderOption.NONE -> "none"
            TimeReminderOption.THIRTY_SECONDS -> "thirty_seconds"
            TimeReminderOption.ONE_MINUTE -> "one_minute"
        }
        prefs.edit().putString(KEY_TIME_REMINDER, value).apply()
    }

    val phasePromptStyle: PhasePromptStyle
        get() = when (prefs.getString(KEY_PHASE_PROMPT_STYLE, DEFAULT_PHASE_PROMPT_STYLE)) {
            "detailed" -> PhasePromptStyle.DETAILED
            "off" -> PhasePromptStyle.OFF
            else -> PhasePromptStyle.SIMPLE
        }

    fun savePhasePromptStyle(style: PhasePromptStyle) {
        val value = when (style) {
            PhasePromptStyle.DETAILED -> "detailed"
            PhasePromptStyle.OFF -> "off"
            PhasePromptStyle.SIMPLE -> "simple"
        }
        prefs.edit().putString(KEY_PHASE_PROMPT_STYLE, value).apply()
    }

    val speechRate: Float
        get() = prefs.getFloat(KEY_SPEECH_RATE, DEFAULT_SPEECH_RATE)

    fun saveSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate.coerceIn(0.5f, 2.0f)).apply()
    }

    // ========== 显示设置 ==========

    val keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON)

    fun saveKeepScreenOn(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, enabled).apply()
    }

    val darkMode: DarkModeOption
        get() = when (prefs.getString(KEY_DARK_MODE, DEFAULT_DARK_MODE)) {
            "always_on" -> DarkModeOption.ALWAYS_ON
            "always_off" -> DarkModeOption.ALWAYS_OFF
            else -> DarkModeOption.FOLLOW_SYSTEM
        }

    fun saveDarkMode(option: DarkModeOption) {
        val value = when (option) {
            DarkModeOption.ALWAYS_ON -> "always_on"
            DarkModeOption.ALWAYS_OFF -> "always_off"
            DarkModeOption.FOLLOW_SYSTEM -> "follow_system"
        }
        prefs.edit().putString(KEY_DARK_MODE, value).apply()
    }

    // ========== 交互设置 ==========

    val vibrationEnabled: Boolean
        get() = prefs.getBoolean(KEY_VIBRATION_ENABLED, DEFAULT_VIBRATION_ENABLED)

    fun saveVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    val lockScreenNotification: Boolean
        get() = prefs.getBoolean(KEY_LOCK_SCREEN_NOTIFICATION, DEFAULT_LOCK_SCREEN_NOTIFICATION)

    fun saveLockScreenNotification(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LOCK_SCREEN_NOTIFICATION, enabled).apply()
    }

    /**
     * 清除所有设置（恢复默认）
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
