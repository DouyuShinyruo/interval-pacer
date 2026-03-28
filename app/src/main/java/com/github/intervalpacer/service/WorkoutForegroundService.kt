package com.github.intervalpacer.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.github.intervalpacer.core.notification.NotificationController
import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.Phase
import com.github.intervalpacer.domain.model.WorkoutState
import com.github.intervalpacer.domain.service.TimerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * 运动前台服务
 * 负责在后台保持训练进程运行，并显示持久通知
 */
class WorkoutForegroundService : Service() {

    companion object {
        const val ACTION_START = "com.github.intervalpacer.action.START"
        const val ACTION_PAUSE = "com.github.intervalpacer.action.PAUSE"
        const val ACTION_RESUME = "com.github.intervalpacer.action.RESUME"
        const val ACTION_STOP = "com.github.intervalpacer.action.STOP"
        const val ACTION_SKIP = "com.github.intervalpacer.action.SKIP"

        const val EXTRA_CONFIG_RUN_DURATION = "run_duration"
        const val EXTRA_CONFIG_WALK_DURATION = "walk_duration"
        const val EXTRA_CONFIG_REPEAT_COUNT = "repeat_count"
        const val EXTRA_CONFIG_WARMUP_DURATION = "warmup_duration"
        const val EXTRA_CONFIG_COOLDOWN_DURATION = "cooldown_duration"
        const val EXTRA_CONFIG_RUN_FIRST = "run_first"

        const val NOTIFICATION_ID = 1001
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var timerService: TimerService
    private lateinit var notificationController: NotificationController

    private var stateObserverJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        timerService = TimerService(serviceScope)
        notificationController = NotificationController(this)

        // 监听训练状态变化，更新通知
        observeTrainingState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
            ACTION_SKIP -> handleSkip()
        }

        // 确保服务被杀后重启
        return START_STICKY
    }

    /**
     * 处理开始训练命令
     */
    private fun handleStart(intent: Intent) {
        val config = parseConfig(intent)
        val notification = notificationController.createForegroundNotification()

        startForeground(NOTIFICATION_ID, notification)
        timerService.start(config)
    }

    /**
     * 处理暂停命令
     */
    private fun handlePause() {
        timerService.pause()
        val currentPhase = timerService.currentPhase.value
        notificationController.updatePausedNotification(currentPhase.getDisplayName())
    }

    /**
     * 处理恢复命令
     */
    private fun handleResume() {
        timerService.resume()
    }

    /**
     * 处理停止命令
     */
    private fun handleStop() {
        timerService.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    /**
     * 处理跳过阶段命令
     */
    private fun handleSkip() {
        timerService.skipToNextPhase()
    }

    /**
     * 从 Intent 解析训练配置
     */
    private fun parseConfig(intent: Intent): IntervalConfig {
        val runDuration = Duration.parse(intent.getStringExtra(EXTRA_CONFIG_RUN_DURATION) ?: "1m")
        val walkDuration = Duration.parse(intent.getStringExtra(EXTRA_CONFIG_WALK_DURATION) ?: "2m")
        val repeatCount = intent.getIntExtra(EXTRA_CONFIG_REPEAT_COUNT, 5)
        val warmupDuration = Duration.parse(intent.getStringExtra(EXTRA_CONFIG_WARMUP_DURATION) ?: "0m")
        val cooldownDuration = Duration.parse(intent.getStringExtra(EXTRA_CONFIG_COOLDOWN_DURATION) ?: "0m")
        val runFirst = intent.getBooleanExtra(EXTRA_CONFIG_RUN_FIRST, true)

        return IntervalConfig(
            runDuration = runDuration,
            walkDuration = walkDuration,
            repeatCount = repeatCount,
            warmupDuration = warmupDuration,
            cooldownDuration = cooldownDuration,
            runFirst = runFirst
        )
    }

    /**
     * 监听训练状态变化，更新通知
     */
    private fun observeTrainingState() {
        stateObserverJob = serviceScope.launch {
            timerService.timerState.collect { state ->
                when (state) {
                    is WorkoutState.Active -> {
                        val phase = timerService.currentPhase.value
                        notificationController.updateNotification(
                            phaseName = phase.getDisplayName(),
                            remainingTime = state.phaseProgress,
                            completedRounds = state.completedRounds
                        )
                    }
                    is WorkoutState.Completed -> {
                        notificationController.updateCompletedNotification(
                            totalRounds = state.completedRounds,
                            totalDuration = state.totalDuration
                        )
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                    is WorkoutState.Paused -> {
                        // 通知已在 handlePause 中更新
                    }
                    is WorkoutState.Idle, is WorkoutState.Preparing -> {
                        // 不更新通知
                    }
                }
            }
        }
    }

    /**
     * 获取训练状态流（供 Activity 绑定）
     */
    fun getTimerState(): StateFlow<WorkoutState> = timerService.timerState

    /**
     * 获取当前阶段流（供 Activity 绑定）
     */
    fun getCurrentPhase(): StateFlow<Phase> = timerService.currentPhase

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stateObserverJob?.cancel()
        serviceScope.cancel()
        notificationController.cancelNotification()
        super.onDestroy()
    }
}
