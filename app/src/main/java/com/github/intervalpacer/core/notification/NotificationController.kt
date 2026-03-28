package com.github.intervalpacer.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.github.intervalpacer.R
import com.github.intervalpacer.presentation.ui.MainActivity
import kotlin.time.Duration

/**
 * 通知控制器
 * 负责管理通知渠道和训练状态通知显示
 */
class NotificationController(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "workout_channel"
        private const val CHANNEL_NAME = "运动训练"
        private const val NOTIFICATION_ID = 1001
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    /**
     * 创建通知渠道（Android 8.0+ 需要）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示当前训练状态和进度"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建前台服务通知
     */
    fun createForegroundNotification(): Notification {
        val pendingIntent = createPendingIntent()

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("间歇训练")
            .setContentText("准备开始训练...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    /**
     * 更新通知内容
     */
    fun updateNotification(
        phaseName: String,
        remainingTime: Duration,
        completedRounds: Int
    ) {
        val timeText = formatDuration(remainingTime)
        val contentText = "$phaseName · 剩余 $timeText · 已完成 $completedRounds 组"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("间歇训练进行中")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(createPendingIntent())
            .setOngoing(true)
            .setSilent(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 更新通知为暂停状态
     */
    fun updatePausedNotification(phaseName: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("训练已暂停")
            .setContentText("当前阶段：$phaseName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(createPendingIntent())
            .setOngoing(true)
            .setSilent(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 更新通知为完成状态
     */
    fun updateCompletedNotification(
        totalRounds: Int,
        totalDuration: Duration
    ) {
        val durationText = formatDuration(totalDuration)
        val contentText = "完成 $totalRounds 组 · 用时 $durationText"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("训练完成！")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(createPendingIntent())
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 取消通知
     */
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * 创建返回应用的 PendingIntent
     */
    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    /**
     * 格式化时长显示
     */
    private fun formatDuration(duration: Duration): String {
        val minutes = duration.inWholeMinutes.toInt()
        val seconds = (duration.inWholeSeconds % 60).toInt()

        return when {
            minutes > 0 && seconds > 0 -> "${minutes}分${seconds}秒"
            minutes > 0 -> "${minutes}分钟"
            seconds > 0 -> "${seconds}秒"
            else -> "0秒"
        }
    }
}
