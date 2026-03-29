package com.github.intervalpacer.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.intervalpacer.presentation.interval.IntervalConfigUi
import com.github.intervalpacer.presentation.interval.ConfigDialog
import com.github.intervalpacer.presentation.settraining.SetTrainingConfigUi
import com.github.intervalpacer.presentation.ui.components.TimeSelectorRow
import com.github.intervalpacer.presentation.ui.components.WheelTimePickerSheet
import com.github.intervalpacer.presentation.ui.components.formatPresetTime
import com.github.intervalpacer.presentation.ui.components.restTimePresets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 主界面（Home）
 * 交互设计文档 4.1 节
 */
@Composable
fun HomeScreen(
    intervalConfig: IntervalConfigUi,
    strengthConfig: SetTrainingConfigUi,
    onIntervalConfigChange: (IntervalConfigUi) -> Unit,
    onStrengthConfigChange: (SetTrainingConfigUi) -> Unit,
    initialMode: String = "interval",
    lastUsedTime: Long = 0L,
    onNavigateToInterval: () -> Unit = {},
    onNavigateToSetTraining: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    var selectedMode by remember {
        mutableStateOf(
            if (initialMode == "strength") TrainingMode.STRENGTH else TrainingMode.INTERVAL
        )
    }
    var showIntervalConfigDialog by remember { mutableStateOf(false) }
    var showStrengthConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标题区域
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "IntervalPacer",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 上次使用时间
        val lastUsedText = formatLastUsedTime(lastUsedTime)
        if (lastUsedText.isNotEmpty()) {
            Text(
                text = "上次：$lastUsedText",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "运动时机控制专家",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 模式选择
        SectionLabel("选择训练模式")

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModeCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PlayArrow,
                title = "走跑间歇",
                description = "跑走结合，科学训练",
                color = Color.Unspecified,
                isSelected = selectedMode == TrainingMode.INTERVAL,
                onClick = { selectedMode = TrainingMode.INTERVAL }
            )

            ModeCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                title = "健身计时",
                description = "组数管理，精准休息",
                color = Color.Unspecified,
                isSelected = selectedMode == TrainingMode.STRENGTH,
                onClick = { selectedMode = TrainingMode.STRENGTH }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 当前配置显示
        when (selectedMode) {
            TrainingMode.INTERVAL -> {
                ConfigDisplayCard(
                    config = intervalConfig,
                    onEditClick = { showIntervalConfigDialog = true }
                )
            }
            TrainingMode.STRENGTH -> {
                StrengthConfigDisplayCard(
                    config = strengthConfig,
                    onEditClick = { showStrengthConfigDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 底部操作区
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 历史记录按钮
            FilledTonalButton(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "历史",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 开始训练按钮
            Button(
                onClick = {
                    when (selectedMode) {
                        TrainingMode.INTERVAL -> onNavigateToInterval()
                        TrainingMode.STRENGTH -> onNavigateToSetTraining()
                    }
                },
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = when (selectedMode) {
                        TrainingMode.INTERVAL -> "开始间歇训练"
                        TrainingMode.STRENGTH -> "开始力量训练"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // 间歇训练配置对话框
    if (showIntervalConfigDialog) {
        ConfigDialog(
            config = intervalConfig,
            onDismiss = { showIntervalConfigDialog = false },
            onConfirm = { newConfig ->
                onIntervalConfigChange(newConfig)
                showIntervalConfigDialog = false
            }
        )
    }

    // 力量训练配置对话框
    if (showStrengthConfigDialog) {
        StrengthConfigDialog(
            config = strengthConfig,
            onDismiss = { showStrengthConfigDialog = false },
            onConfirm = { newConfig ->
                onStrengthConfigChange(newConfig)
                showStrengthConfigDialog = false
            }
        )
    }
}

/**
 * 段落标题
 */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * 模式选择卡片
 */
@Suppress("UNUSED_PARAMETER")
@Composable
private fun ModeCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            )
        },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 间歇训练配置显示卡片
 */
@Composable
private fun ConfigDisplayCard(
    config: IntervalConfigUi,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "当前配置",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = config.getDescription(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 编辑按钮
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 配置详情
            ConfigDetailRow("跑步时长", formatDuration(config.runMinutes, config.runSeconds))
            ConfigDetailRow("步行时长", formatDuration(config.walkMinutes, config.walkSeconds))
            ConfigDetailRow("重复组数", "${config.rounds} 组")
        }
    }
}

/**
 * 力量训练配置显示卡片
 */
@Composable
private fun StrengthConfigDisplayCard(
    config: SetTrainingConfigUi,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "当前配置",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = config.getSummary(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 编辑按钮
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 配置详情
            if (config.exerciseName.isNotEmpty()) {
                ConfigDetailRow("动作名称", config.exerciseName)
            }
            ConfigDetailRow("总组数", "${config.totalSets} 组")
            ConfigDetailRow("组间休息", config.getRestDescription())
        }
    }
}

/**
 * 力量训练配置对话框
 */
@Composable
private fun StrengthConfigDialog(
    config: SetTrainingConfigUi,
    onDismiss: () -> Unit,
    onConfirm: (SetTrainingConfigUi) -> Unit
) {
    var exerciseName by remember { mutableStateOf(config.exerciseName) }
    var totalSets by remember { mutableIntStateOf(config.totalSets) }
    var restMinutes by remember { mutableIntStateOf(config.restMinutes) }
    var restSeconds by remember { mutableIntStateOf(config.restSeconds) }
    var showRestTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
            ) {
                Text(
                    "配置力量训练",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                // 动作名称
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { exerciseName = it },
                    label = { Text("动作名称（可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(Modifier.height(16.dp))

                // 组数
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text("总组数", style = MaterialTheme.typography.bodyLarge)
                    HomeCounterPicker(totalSets, { totalSets = it }, "组")
                }

                Spacer(Modifier.height(16.dp))

                // 组间休息
                TimeSelectorRow(
                    label = "组间休息",
                    currentMinutes = restMinutes,
                    currentSeconds = restSeconds,
                    onClick = { showRestTimePicker = true }
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.End) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) { Text("取消") }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        onConfirm(SetTrainingConfigUi(exerciseName, totalSets, restMinutes, restSeconds))
                    }) { Text("确定") }
                }
            }
        }
    }

    // 组间休息滚轮选择器
    if (showRestTimePicker) {
        WheelTimePickerSheet(
            title = "组间休息",
            presets = restTimePresets,
            initialMinutes = restMinutes,
            initialSeconds = restSeconds,
            onDismiss = { showRestTimePicker = false },
            onConfirm = { min, sec -> restMinutes = min; restSeconds = sec; showRestTimePicker = false }
        )
    }
}
@Composable
private fun HomeCounterPicker(
    value: Int,
    onChange: (Int) -> Unit,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            { if (value > 0) onChange(value - 1) },
            Modifier.size(48.dp),
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }

        Spacer(Modifier.width(12.dp))

        Box(
            Modifier.size(64.dp, 48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
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
            contentPadding = PaddingValues(0.dp)
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

@Composable
private fun ConfigDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(minutes: Int, seconds: Int): String {
    return when {
        minutes > 0 && seconds > 0 -> "${minutes}分${seconds}秒"
        minutes > 0 -> "${minutes}分钟"
        seconds > 0 -> "${seconds}秒"
        else -> "0秒"
    }
}

private fun formatLastUsedTime(timestamp: Long): String {
    if (timestamp <= 0) return ""
    val now = System.currentTimeMillis()
    val diffMs = now - timestamp
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(timestamp))
    return when {
        diffDays == 0L -> "今天 $timeStr"
        diffDays == 1L -> "昨天 $timeStr"
        diffDays == 2L -> "两天前"
        else -> "${diffDays}天前"
    }
}

private enum class TrainingMode {
    INTERVAL,
    STRENGTH
}
