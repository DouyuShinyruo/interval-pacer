package com.github.intervalpacer.core.vibration

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * 震动管理器
 * 负责控制设备震动反馈
 */
class VibrationManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * 检查是否有震动权限
     */
    private fun hasVibratePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.VIBRATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 震动一次
     */
    fun vibrate(duration: Duration) {
        if (!hasVibratePermission() || !hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        duration.inWholeMilliseconds,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration.inWholeMilliseconds)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 震动模式
     */
    fun vibratePattern(pattern: VibrationPattern) {
        if (!hasVibratePermission() || !hasVibrator()) return

        try {
            val timings = pattern.getTimings()
            val amplitudes = pattern.getAmplitudes()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        timings.toLongArray(),
                        amplitudes.toIntArray(),
                        -1 // 不重复
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(timings.toLongArray(), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 取消震动
     */
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 检查设备是否支持震动
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() == true
    }

    /**
     * 阶段切换震动（短促）
     */
    fun phaseTransition() {
        vibratePattern(VibrationPattern.PHASE_TRANSITION)
    }

    /**
     * 完成震动（双击）
     */
    fun completion() {
        vibratePattern(VibrationPattern.COMPLETION)
    }

    /**
     * 倒计时震动（轻微）
     */
    fun countdown() {
        vibratePattern(VibrationPattern.COUNTDOWN)
    }
}

/**
 * 震动模式
 */
enum class VibrationPattern {
    PHASE_TRANSITION,  // 阶段切换
    COMPLETION,        // 完成
    COUNTDOWN,         // 倒计时
    ERROR;             // 错误

    /**
     * 获取震动时间序列（毫秒）
     */
    fun getTimings(): List<Long> {
        return when (this) {
            PHASE_TRANSITION -> listOf(0, 100)           // 单次短震动
            COMPLETION -> listOf(0, 100, 100, 100)      // 两次短震动
            COUNTDOWN -> listOf(0, 50)                   // 极短震动
            ERROR -> listOf(0, 200, 100, 200)            // 长短震动
        }
    }

    /**
     * 获取震动强度序列
     */
    fun getAmplitudes(): List<Int> {
        return when (this) {
            PHASE_TRANSITION -> listOf(0, 255)
            COMPLETION -> listOf(0, 255, 0, 255)
            COUNTDOWN -> listOf(0, 128)
            ERROR -> listOf(0, 255, 0, 255)
        }
    }
}
