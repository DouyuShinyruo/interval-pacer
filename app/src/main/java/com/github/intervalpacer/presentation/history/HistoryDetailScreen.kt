package com.github.intervalpacer.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.WorkoutType
import com.github.intervalpacer.data.model.StrengthConfig
import com.github.intervalpacer.presentation.history.components.formatDuration
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun HistoryDetailScreen(
    recordId: String,
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    val records by viewModel.allRecords.collectAsState(initial = emptyList())
    val record = records.find { it.id == recordId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("训练详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        if (record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "记录未找到",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Workout Type Header
                Text(
                    text = when (record.type) {
                        WorkoutType.INTERVAL_RUN -> "间歇训练"
                        WorkoutType.STRENGTH_TRAINING -> "力量训练"
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Stats Section
                DetailCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatRow(label = "训练时长", value = formatDuration(record.totalDuration))
                        StatRow(
                            label = "开始时间",
                            value = formatStartTime(record.startTime)
                        )
                        StatRow(
                            label = "完成进度",
                            value = "${record.completedRounds}/${record.targetRounds} ${if (record.type == WorkoutType.INTERVAL_RUN) "轮" else "组"}"
                        )

                        // Completion Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (record.isCompleted) "已完成" else "未完成",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (record.isCompleted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }
                            )
                            if (record.isCompleted) {
                                Spacer(modifier = Modifier.size(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Config Details Section
                Text(
                    text = "训练配置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when (record.type) {
                    WorkoutType.INTERVAL_RUN -> {
                        val config = record.intervalConfig
                        if (config != null) {
                            IntervalConfigDetails(config)
                        }
                    }
                    WorkoutType.STRENGTH_TRAINING -> {
                        val config = record.strengthConfig
                        if (config != null) {
                            StrengthConfigDetails(config)
                        }
                    }
                }

                // Delete Button
                OutlinedButton(
                    onClick = {
                        viewModel.deleteRecord(recordId)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除记录", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun IntervalConfigDetails(config: IntervalConfig) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ConfigCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfigItem(label = "跑步时长", value = formatDuration(config.runDuration))
                ConfigItem(label = "步行时长", value = formatDuration(config.walkDuration))
                ConfigItem(label = "重复次数", value = "${config.repeatCount}轮")
            }
        }

        if (config.warmupDuration > Duration.ZERO) {
            ConfigCard {
                ConfigItem(label = "热身时长", value = formatDuration(config.warmupDuration))
            }
        }

        if (config.cooldownDuration > Duration.ZERO) {
            ConfigCard {
                ConfigItem(label = "冷身时长", value = formatDuration(config.cooldownDuration))
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun StrengthConfigDetails(config: StrengthConfig) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ConfigCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (config.exerciseName.isNotEmpty()) {
                    ConfigItem(label = "动作名称", value = config.exerciseName)
                }
                ConfigItem(label = "组数", value = "${config.sets}组")
                if (config.repsPerSet != null) {
                    ConfigItem(label = "每组次数", value = "${config.repsPerSet}次")
                }
                ConfigItem(label = "组间休息", value = formatDuration(config.restDuration))
            }
        }
    }
}

@Composable
private fun ConfigCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ConfigItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalTime::class)
private fun formatStartTime(instant: kotlin.time.Instant): String {
    val localDateTime = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(instant.toEpochMilliseconds()),
        java.time.ZoneId.systemDefault()
    )
    return "${localDateTime.year}年${localDateTime.monthValue}月${localDateTime.dayOfMonth}日 " +
            String.format("%02d:%02d", localDateTime.hour, localDateTime.minute)
}
