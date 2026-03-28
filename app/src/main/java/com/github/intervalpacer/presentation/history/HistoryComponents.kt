package com.github.intervalpacer.presentation.history.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.intervalpacer.data.model.StrengthConfig
import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.model.WorkoutType
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "暂无训练记录",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "完成首次训练后，记录将显示在这里",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DateGroupHeader(dateHeader: String) {
    Text(
        text = dateHeader,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun WorkoutCard(
    record: WorkoutRecord,
    onClick: () -> Unit
) {
    val leftColor = when (record.type) {
        WorkoutType.INTERVAL_RUN -> MaterialTheme.colorScheme.primary
        WorkoutType.STRENGTH_TRAINING -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val typeIcon: ImageVector = when (record.type) {
        WorkoutType.INTERVAL_RUN -> Icons.Outlined.DirectionsRun
        WorkoutType.STRENGTH_TRAINING -> Icons.Outlined.FitnessCenter
    }

    val typeName = when (record.type) {
        WorkoutType.INTERVAL_RUN -> "间歇训练"
        WorkoutType.STRENGTH_TRAINING -> "力量训练"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left color bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(leftColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Main content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = typeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(record.startTime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatConfigSummary(record),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "时长 ${formatDuration(record.totalDuration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right side completion badge
            WorkoutCompletionBadge(record)
        }
    }
}

@Composable
fun WorkoutCompletionBadge(record: WorkoutRecord) {
    if (record.isCompleted) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "已完成",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { record.getCompletionPercentage() },
                modifier = Modifier
                    .width(32.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                text = "${record.completedRounds}/${record.targetRounds}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatConfigSummary(record: WorkoutRecord): String {
    return when (record.type) {
        WorkoutType.INTERVAL_RUN -> {
            val config = record.intervalConfig ?: return "配置丢失"
            "跑${formatDuration(config.runDuration)}/走${formatDuration(config.walkDuration)} × ${config.repeatCount}组"
        }
        WorkoutType.STRENGTH_TRAINING -> {
            val config = record.strengthConfig ?: return "配置丢失"
            val exerciseName = if (config.exerciseName.isNotEmpty()) config.exerciseName else "力量训练"
            "$exerciseName · ${config.sets}组 · 休息${formatDuration(config.restDuration)}"
        }
    }
}

@OptIn(ExperimentalTime::class)
fun formatDuration(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    val seconds = duration.inWholeSeconds % 60
    return if (minutes > 0) {
        "${minutes}分${seconds}秒"
    } else {
        "${seconds}秒"
    }
}

@OptIn(ExperimentalTime::class)
fun formatTime(instant: kotlin.time.Instant): String {
    val localDate = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(instant.toEpochMilliseconds()),
        java.time.ZoneId.systemDefault()
    )
    return String.format("%02d:%02d", localDate.hour, localDate.minute)
}

@OptIn(ExperimentalTime::class)
fun groupRecordsByDate(records: List<WorkoutRecord>): Map<String, List<WorkoutRecord>> {
    val todayStart = java.time.LocalDate.now()
        .atStartOfDay(java.time.ZoneId.systemDefault())
        .toEpochSecond() * 1000
    val yesterdayStart = todayStart - 24 * 60 * 60 * 1000

    return records
        .sortedByDescending { it.startTime.toEpochMilliseconds() }
        .groupBy { record ->
            val recordTime = record.startTime.toEpochMilliseconds()
            when {
                recordTime >= todayStart -> "今天"
                recordTime >= yesterdayStart -> "昨天"
                else -> {
                    val localDate = java.time.LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(recordTime),
                        java.time.ZoneId.systemDefault()
                    )
                    "${localDate.monthValue}月${localDate.dayOfMonth}日"
                }
            }
        }
}
