package com.github.intervalpacer.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.github.intervalpacer.domain.model.Phase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * TTS语音管理器
 * 负责语音合成和播报
 */
class TTSManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var speechRate: Float = 1.0f
    private var pitch: Float = 1.0f
    private var currentLocale: Locale = Locale.CHINA

    init {
        // 初始化TTS
        tts = TextToSpeech(context, this)
    }

    /**
     * TTS初始化回调
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 设置默认语言为中文
            val result = tts?.setLanguage(currentLocale)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // 中文不支持，尝试英文
                currentLocale = Locale.ENGLISH
                tts?.setLanguage(currentLocale)
            }

            // 设置播报参数
            tts?.setSpeechRate(speechRate)
            tts?.setPitch(pitch)

            // 设置监听器
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })

            _isReady.value = true
        } else {
            _isReady.value = false
        }
    }

    /**
     * 播报语音
     */
    fun speak(text: String, urgency: Urgency = Urgency.NORMAL) {
        if (!_isReady.value) return
        if (text.isBlank()) return

        tts?.let { engine ->
            // 根据紧急程度调整播报方式
            when (urgency) {
                Urgency.HIGH -> {
                    // 高优先级，清空队列立即播报
                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
                }
                Urgency.NORMAL -> {
                    // 正常优先级，加入队列
                    engine.speak(text, TextToSpeech.QUEUE_ADD, null, "tts_${System.currentTimeMillis()}")
                }
                Urgency.LOW -> {
                    // 低优先级，不影响当前播报
                    if (!_isSpeaking.value) {
                        engine.speak(text, TextToSpeech.QUEUE_ADD, null, "tts_${System.currentTimeMillis()}")
                    }
                }
            }
        }
    }

    /**
     * 停止播报
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * 设置语速
     */
    fun setSpeechRate(rate: Float) {
        this.speechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(this.speechRate)
    }

    /**
     * 设置音调
     */
    fun setPitch(rate: Float) {
        this.pitch = rate.coerceIn(0.5f, 2.0f)
        tts?.setPitch(this.pitch)
    }

    /**
     * 设置语言
     */
    fun setLanguage(locale: Locale): Boolean {
        val result = tts?.setLanguage(locale)
        val success = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED

        if (success) {
            currentLocale = locale
        }

        return success
    }

    /**
     * 检查语言是否可用
     */
    fun isLanguageAvailable(locale: Locale): Boolean {
        return tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE
    }

    /**
     * 释放资源
     */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
        _isSpeaking.value = false
    }

    /**
     * 播报阶段开始
     */
    fun announcePhaseStart(phaseName: String, duration: String = "") {
        val text = if (duration.isNotEmpty()) {
            "$phaseName，$duration"
        } else {
            phaseName
        }
        speak(text, Urgency.HIGH)
    }

    /**
     * 播报倒计时
     */
    fun announceCountdown(seconds: Int) {
        speak("$seconds", Urgency.HIGH)
    }

    /**
     * 播报剩余时间
     */
    fun announceRemaining(timeText: String) {
        speak("还有$timeText", Urgency.NORMAL)
    }

    /**
     * 播报完成
     */
    fun announceCompleted(totalRounds: Int) {
        speak("恭喜完成，共${totalRounds}组", Urgency.HIGH)
    }

    /**
     * 播报暂停提示
     */
    fun announcePause() {
        speak("运动已暂停", Urgency.NORMAL)
    }

    /**
     * 播报恢复提示
     */
    fun announceResume(phase: Phase) {
        val phaseName = when (phase) {
            is Phase.Warmup -> "热身"
            is Phase.Run -> "跑步"
            is Phase.Walk -> "步行"
            is Phase.Cooldown -> "冷身"
            is Phase.Completed -> "完成"
            else -> "训练"
        }
        speak("继续$phaseName，3，2，1", Urgency.HIGH)
    }
}

/**
 * 播报紧急程度
 */
enum class Urgency {
    HIGH,    // 高优先级，立即播报
    NORMAL,  // 正常优先级
    LOW      // 低优先级
}
