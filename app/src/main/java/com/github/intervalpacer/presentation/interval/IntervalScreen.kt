package com.github.intervalpacer.presentation.interval

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.PaddingValues
import com.github.intervalpacer.domain.model.Phase
import com.github.intervalpacer.domain.model.WorkoutState
import com.github.intervalpacer.presentation.ui.theme.PhaseColors

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
    onResetToIdle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getPhaseBackgroundColor(currentPhase))
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
                completedRounds = (timerState as? WorkoutState.Active)?.completedRounds ?: 0,
                onPause = onPause,
                onSkip = onSkip,
                onStop = onStop
            )
            is WorkoutState.Paused -> PausedScreen(
                onResume = onResume,
                onStop = onStop
            )
            is WorkoutState.Completed -> CompletedScreen(
                totalRounds = (timerState as? WorkoutState.Completed)?.completedRounds ?: 0,
                totalDuration = (timerState as? WorkoutState.Completed)?.totalDuration ?: kotlin.time.Duration.ZERO,
                onBackToHome = onResetToIdle
            )
            else -> {}
        }
    }
}

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

@Composable
fun IdleScreen(
    config: IntervalConfigUi,
    onConfigChange: (IntervalConfigUi) -> Unit,
    onStart: () -> Unit
) {
    var showConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "间歇训练",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            onClick = { showConfigDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = config.getDescription(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "点击修改配置",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.size(200.dp, 80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "开始训练",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showConfigDialog) {
        ConfigDialog(
            config = config,
            onDismiss = { showConfigDialog = false },
            onConfirm = { newConfig ->
                onConfigChange(newConfig)
                showConfigDialog = false
            }
        )
    }
}

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
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                Text("配置训练参数", style = MaterialTheme.typography.headlineMedium)

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("跑步时长", style = MaterialTheme.typography.bodyLarge)
                    TimePicker(runMinutes, runSeconds, { runMinutes = it }, { runSeconds = it })
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("步行时长", style = MaterialTheme.typography.bodyLarge)
                    TimePicker(walkMinutes, walkSeconds, { walkMinutes = it }, { walkSeconds = it })
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("重复组数", style = MaterialTheme.typography.bodyLarge)
                    CounterPicker(rounds, { rounds = it }, "组")
                }

                Spacer(Modifier.height(16.dp))

                Column {
                    Text("训练顺序", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(runFirst, { runFirst = true })
                        Spacer(Modifier.width(8.dp))
                        Text("先跑步")
                    }
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(!runFirst, { runFirst = false })
                        Spacer(Modifier.width(8.dp))
                        Text("先步行")
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    Button(onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) { Text("取消") }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        onConfirm(IntervalConfigUi(runMinutes, runSeconds, walkMinutes, walkSeconds, rounds, runFirst))
                    }) { Text("确定") }
                }
            }
        }
    }
}

@Composable
private fun TimePicker(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CounterPicker(minutes, onMinutesChange, "分")
    }
}

@Composable
private fun CounterPicker(
    value: Int,
    onChange: (Int) -> Unit,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            { if (value > 0) onChange(value - 1) },
            Modifier.size(40.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.width(8.dp))

        Box(
            Modifier.size(50.dp, 36.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            Alignment.Center
        ) {
            Text("$value", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(8.dp))

        Button(
            { onChange(value + 1) },
            Modifier.size(40.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }

        Text(label, Modifier.padding(start = 4.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ActiveScreen(
    phase: Phase,
    progress: Float,
    remainingTime: kotlin.time.Duration?,
    completedRounds: Int,
    onPause: () -> Unit,
    onSkip: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onPause, modifier = Modifier.size(80.dp, 48.dp)) {
                Text("暂停")
            }
            Button(onClick = onStop, modifier = Modifier.size(80.dp, 48.dp)) {
                Text("停止")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = phase.getDisplayName(),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        val timeText = formatTime(remainingTime ?: kotlin.time.Duration.ZERO)
        Text(
            text = timeText,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(8.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "已完成 $completedRounds 组",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("跳过当前阶段", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun PausedScreen(onResume: () -> Unit, onStop: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "训练暂停",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onResume,
            modifier = Modifier.fillMaxWidth().height(80.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("继续训练", style = MaterialTheme.typography.headlineLarge)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStop,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("结束训练", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun CompletedScreen(
    totalRounds: Int,
    totalDuration: kotlin.time.Duration,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉 训练完成！",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "训练总结",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "完成组数",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$totalRounds 组",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "训练时长",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatTotalTime(totalDuration),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "记录已自动保存",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "返回主页",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
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
