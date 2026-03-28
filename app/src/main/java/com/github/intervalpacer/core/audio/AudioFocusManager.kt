package com.github.intervalpacer.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

/**
 * 音频焦点管理器
 * 负责 TTS 播报时的音频焦点管理，降低背景音乐音量
 */
class AudioFocusManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var audioFocusRequest: AudioFocusRequest? = null
    private var isFocusHeld = false

    /**
     * 请求音频焦点
     * 使用 TRANSIENT_MAY_DUCK 策略，让背景音乐降低音量而非完全停止
     */
    fun requestFocus(onFocusLost: (() -> Unit)? = null): Boolean {
        if (isFocusHeld) return true

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestFocusModern(onFocusLost)
        } else {
            requestFocusLegacy(onFocusLost)
        }
    }

    /**
     * 放弃音频焦点
     */
    fun abandonFocus() {
        if (!isFocusHeld) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { request ->
                audioManager.abandonAudioFocusRequest(request)
            }
            audioFocusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(focusChangeListener)
        }

        isFocusHeld = false
    }

    /**
     * 现代版本请求焦点（Android 8.0+）
     */
    private fun requestFocusModern(onFocusLost: (() -> Unit)?): Boolean {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener { focusChange ->
                handleFocusChange(focusChange, onFocusLost)
            }
            .build()

        val result = audioFocusRequest?.let { request ->
            audioManager.requestAudioFocus(request)
        } ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED

        isFocusHeld = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return isFocusHeld
    }

    /**
     * 传统版本请求焦点（Android 8.0 以下）
     */
    @Suppress("DEPRECATION")
    private fun requestFocusLegacy(onFocusLost: (() -> Unit)?): Boolean {
        val result = audioManager.requestAudioFocus(
            focusChangeListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
        )

        isFocusHeld = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return isFocusHeld
    }

    /**
     * 音频焦点变化监听器
     */
    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        handleFocusChange(focusChange, null)
    }

    /**
     * 处理焦点变化
     */
    private fun handleFocusChange(
        focusChange: Int,
        onFocusLostCallback: (() -> Unit)?
    ) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // 永久失去焦点，应该放弃焦点并停止播报
                isFocusHeld = false
                onFocusLostCallback?.invoke()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // 暂时失去焦点，应该暂停播报
                // TTS 会在恢复焦点后继续
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // 可以降低音量继续播报
                // Android 系统会自动处理
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // 重新获得焦点
                isFocusHeld = true
            }
        }
    }

    /**
     * 检查是否持有音频焦点
     */
    fun hasFocus(): Boolean = isFocusHeld
}
