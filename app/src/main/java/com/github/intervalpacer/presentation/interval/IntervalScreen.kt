package com.github.intervalpacer.presentation.interval

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.github.intervalpacer.domain.model.Phase
import com.github.intervalpacer.domain.model.WorkoutState
import com.github.intervalpacer.presentation.ui.theme.PhaseColors
import kotlin.math.min

/**
 * 间歇训练主界面
 * 根据交互设计文档 4.2 节规范设计
 */
@Composable
fun IntervalScreen(
    timerState: WorkoutState,
    currentPhase: Phase,
    config: IntervalConfigUi,
    onConfigChange: (IntervalConfigUi) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkip: () -> Unit,
    onResetToIdle: () -> Unit,
    elapsedDuration: kotlin.time.Duration = kotlin.time.Duration.ZERO,
    completedRounds: Int = 0,
    targetRounds: Int = 0,
    onDiscard: () -> Unit = {}
) {
    var showStopConfirmDialog by remember { mutableStateOf(false) }

    // 背景色动画过渡
    val backgroundColor by animateColorAsState(
        targetValue = getPhaseBackgroundColor(currentPhase),
        animationSpec = tween(durationMillis = 500),
        label = "backgroundColor"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (timerState) {
            is WorkoutState.Idle -> IdleScreen(
                config = config,
                onConfigChange = onConfigChange,
                onStart = onStart
            )
            is WorkoutState.Active -> ActiveScreen(
                phase = currentPhase,
                progress = (timerState as? WorkoutState.Active)?.overallProgress ?: 0f,
                remainingTime = (timerState as? WorkoutState.Active)?.phaseProgress,
                totalRounds = config.rounds,
                completedRounds = (timerState as? WorkoutState.Active)?.completedRounds ?: 0,
                onPause = onPause,
                onSkip = onSkip
            )
            is WorkoutState.Paused -> PausedScreen(
                onResume = onResume,
                onStop = { showStopConfirmDialog = true }
            )
            is WorkoutState.Completed -> {
                val completedState = timerState as? WorkoutState.Completed
                CompletedScreen(
                    totalRounds = completedState?.completedRounds ?: 0,
                    totalDuration = completedState?.totalDuration ?: kotlin.time.Duration.ZERO,
                    isCompleted = completedState?.completedRounds ?: 0 >= targetRounds,
                    onBackToHome = onResetToIdle
                )
            }
            else -> {}
        }

        // 提前结束确认弹窗
        if (showStopConfirmDialog) {
            StopConfirmDialog(
                completedRounds = completedRounds,
                targetRounds = targetRounds,
                elapsedDuration = elapsedDuration,
                onSave = {
                    showStopConfirmDialog = false
                    onStop()
                },
                onDiscard = {
                    showStopConfirmDialog = false
                    onDiscard()
                },
                onDismiss = { showStopConfirmDialog = false }
            )
        }
    }
}

/**
 * 运动中界面
 * 交互设计文档 4.2 节
 */
@Composable
fun ActiveScreen(
    phase: Phase,
    progress: Float,
    remainingTime: kotlin.time.Duration?,
    totalRounds: Int,
    completedRounds: Int,
    onPause: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部控制栏：暂停按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 暂停按钮（64x64dp 关键按钮）
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clickable { onPause() }
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHighest,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u23F8",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 当前阶段名称（48pt+ 大字）
        Text(
            text = phase.getDisplayName(),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 52.sp,
                letterSpacing = (-1).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 阶段图标
        PhaseIcon(phase = phase)

        Spacer(modifier = Modifier.height(32.dp))

        // 剩余时间（超大字 36pt+，MM:SS 格式）
        val timeText = formatTime(remainingTime ?: kotlin.time.Duration.ZERO)
        Text(
            text = timeText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 72.sp,
                letterSpacing = (-2).sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 进度指示器（圆点式，显示当前组/总组数）
        ProgressDotsIndicator(
            currentIndex = completedRounds,
            total = totalRounds
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "第 ${min(completedRounds + 1, totalRounds)} 组 / 共 $totalRounds 组",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        // 跳过当前阶段按钮 - 使用 FilledTonalButton 样式
        FilledTonalButton(
            onClick = onSkip,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Text(
                text = "跳过当前阶段",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 阶段图标
 */
@Composable
private fun PhaseIcon(phase: Phase) {
    val iconEmoji = when (phase) {
        is Phase.Warmup -> "\uD83D\uDD25"
        is Phase.Run -> "\uD83C\uDFC3"
        is Phase.Walk -> "\uD83D\uDEB6"
        is Phase.Cooldown -> "\uD83D\uDCA7"
        is Phase.Completed -> "\u2705"
    }

    Text(
        text = iconEmoji,
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
        modifier = Modifier.alpha(0.9f)
    )
}

/**
 * 圆点进度指示器
 */
@Composable
private fun ProgressDotsIndicator(currentIndex: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val isSelected = index < currentIndex
            val isCurrent = index == currentIndex

            Box(
                modifier = Modifier
                    .size(
                        width = if (isCurrent) 32.dp else 16.dp,
                        height = 16.dp
                    )
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {}

            if (index < total - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

/**
 * 暂停界面
 * 交互设计文档 4.2 节
 */
@Composable
fun PausedScreen(onResume: () -> Unit, onStop: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 暂停图标
            Text(
                text = "\u23F8",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "训练暂停",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 继续训练按钮
            Button(
                onClick = onResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "继续训练",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 结束训练按钮 - 使用 error 色
            Button(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = "结束训练",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * 完成界面
 * 交互设计文档 4.2 节
 */
@Composable
fun CompletedScreen(
    totalRounds: Int,
    totalDuration: kotlin.time.Duration,
    isCompleted: Boolean = true,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 完成图标
        Text(
            text = if (isCompleted) "\uD83C\uDF89" else "\uD83D\uDE22",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isCompleted) "训练完成！" else "训练已结束",
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // 训练总结卡片 - 使用 surfaceContainerLow
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "训练总结",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // 完成组数
                    SummaryItem(
                        label = "完成组数",
                        value = "$totalRounds 组"
                    )

                    // 训练时长
                    SummaryItem(
                        label = "训练时长",
                        value = formatTotalTime(totalDuration)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(64.dp))

        // 返回主页按钮
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "返回主页",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 总结项组件
 */
@Composable
private fun SummaryItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 空闲界面（保留原有实现，但移除因为被 HomeScreen 替代）
 */
@Composable
fun IdleScreen(
    config: IntervalConfigUi,
    onConfigChange: (IntervalConfigUi) -> Unit,
    onStart: () -> Unit
) {
    // 这个界面现在由 HomeScreen 处理
    // 保留此函数以兼容现有代码
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "间歇训练",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.size(200.dp, 80.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "开始训练",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 提前结束确认弹窗
 * 交互设计文档 5.6 节
 */
@Composable
private fun StopConfirmDialog(
    completedRounds: Int,
    targetRounds: Int,
    elapsedDuration: kotlin.time.Duration,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "确定结束训练？",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "已完成 $completedRounds / $targetRounds 组",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    "运动时长 ${formatTotalTime(elapsedDuration)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // 保存并结束
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("保存并结束")
                }

                Spacer(Modifier.height(8.dp))

                // 放弃记录
                OutlinedButton(
                    onClick = onDiscard,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("放弃记录", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

/**
 * 配置对话框（保留原有实现）
 */
@Composable
fun ConfigDialog(
    config: IntervalConfigUi,
    onDismiss: () -> Unit,
    onConfirm: (IntervalConfigUi) -> Unit
) {
    var runMinutes by remember { mutableIntStateOf(config.runMinutes) }
    var runSeconds by remember { mutableIntStateOf(config.runSeconds) }
    var walkMinutes by remember { mutableIntStateOf(config.walkMinutes) }
    var walkSeconds by remember { mutableIntStateOf(config.walkSeconds) }
    var rounds by remember { mutableIntStateOf(config.rounds) }
    var runFirst by remember { mutableStateOf(config.runFirst) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(
                    "配置训练参数",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text(
                        "跑步时长",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TimePicker(runMinutes, runSeconds, { runMinutes = it }, { runSeconds = it })
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text(
                        "步行时长",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TimePicker(walkMinutes, walkSeconds, { walkMinutes = it }, { walkSeconds = it })
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text(
                        "重复组数",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    CounterPicker(rounds, { rounds = it }, "组")
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text(
                        "训练顺序",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(runFirst, { runFirst = true })
                        Spacer(Modifier.width(8.dp))
                        Text("先跑步")
                    }
                    Row(
                        Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.RadioButton(!runFirst, { runFirst = false })
                        Spacer(Modifier.width(8.dp))
                        Text("先步行")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("取消") }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        onConfirm(
                            IntervalConfigUi(
                                runMinutes,
                                runSeconds,
                                walkMinutes,
                                walkSeconds,
                                rounds,
                                runFirst
                            )
                        )
                    }) { Text("确定") }
                }
            }
        }
    }
}

/**
 * 时间选择器
 */
@Composable
private fun TimePicker(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CounterPicker(minutes, onMinutesChange, "分")
        Spacer(Modifier.width(8.dp))
        CounterPicker(seconds, onSecondsChange, "秒")
    }
}

/**
 * 计数器选择器
 */
@Composable
private fun CounterPicker(
    value: Int,
    onChange: (Int) -> Unit,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            { if (value > 0) onChange(value - 1) },
            Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.width(12.dp))

        Box(
            Modifier.size(64.dp, 48.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Button(
            { onChange(value + 1) },
            Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }

        Text(
            label,
            Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 配置UI数据类
 */
data class IntervalConfigUi(
    val runMinutes: Int = 1,
    val runSeconds: Int = 0,
    val walkMinutes: Int = 2,
    val walkSeconds: Int = 0,
    val rounds: Int = 5,
    val runFirst: Boolean = true
) {
    fun getDescription(): String {
        val runText = formatDuration(runMinutes, runSeconds, "跑")
        val walkText = formatDuration(walkMinutes, walkSeconds, "走")
        val orderText = if (runFirst) "先跑" else "先走"
        return "$orderText · $runText · $walkText · $rounds 组"
    }

    private fun formatDuration(minutes: Int, seconds: Int, label: String): String {
        return when {
            minutes > 0 && seconds > 0 -> "$label${minutes}分${seconds}秒"
            minutes > 0 -> "$label${minutes}分钟"
            seconds > 0 -> "$label${seconds}秒"
            else -> "${label}0秒"
        }
    }
}

// 辅助函数

@Composable
private fun getPhaseBackgroundColor(phase: Phase): Color {
    return when (phase) {
        is Phase.Warmup -> PhaseColors.Warmup
        is Phase.Run -> PhaseColors.Run
        is Phase.Walk -> PhaseColors.Walk
        is Phase.Cooldown -> PhaseColors.Cooldown
        is Phase.Completed -> PhaseColors.Completed
    }
}

private fun formatTime(duration: kotlin.time.Duration): String {
    val minutes = duration.inWholeMinutes.toInt()
    val seconds = (duration.inWholeSeconds % 60).toInt()
    return String.format("%02d:%02d", minutes, seconds)
}

private fun formatTotalTime(duration: kotlin.time.Duration): String {
    val minutes = duration.inWholeMinutes.toInt()
    val seconds = (duration.inWholeSeconds % 60).toInt()
    return when {
        minutes > 0 && seconds > 0 -> "${minutes}分${seconds}秒"
        minutes > 0 -> "${minutes}分钟"
        seconds > 0 -> "${seconds}秒"
        else -> "0秒"
    }
}
