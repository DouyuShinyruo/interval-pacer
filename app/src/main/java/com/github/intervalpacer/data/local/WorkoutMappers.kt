package com.github.intervalpacer.data.local

import com.github.intervalpacer.data.model.IntervalConfigDto
import com.github.intervalpacer.data.model.StrengthConfigDto
import com.github.intervalpacer.data.model.StrengthConfig
import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.model.IntervalConfig
import com.github.intervalpacer.domain.model.WorkoutType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val json = Json { ignoreUnknownKeys = true }

@OptIn(ExperimentalTime::class)
fun WorkoutRecordEntity.toDomainModel(): WorkoutRecord {
    val workoutType = try {
        WorkoutType.valueOf(workout_type)
    } catch (e: IllegalArgumentException) {
        WorkoutType.INTERVAL_RUN // fallback
    }

    val config = try {
        when (workoutType) {
            WorkoutType.INTERVAL_RUN -> {
                val dto = json.decodeFromString<IntervalConfigDto>(config_json)
                IntervalConfig(
                    runDuration = dto.runDurationSeconds.seconds,
                    walkDuration = dto.walkDurationSeconds.seconds,
                    repeatCount = dto.repeatCount,
                    warmupDuration = dto.warmupDurationSeconds.seconds,
                    cooldownDuration = dto.cooldownDurationSeconds.seconds,
                    runFirst = dto.runFirst
                )
            }
            WorkoutType.STRENGTH_TRAINING -> {
                val dto = json.decodeFromString<StrengthConfigDto>(config_json)
                StrengthConfig(
                    sets = dto.totalSets,
                    repsPerSet = null,
                    restDuration = dto.restDurationSeconds.seconds,
                    exerciseName = dto.exerciseName
                )
            }
        }
    } catch (e: Exception) {
        null
    }

    return WorkoutRecord(
        id = id.toString(),
        type = workoutType,
        intervalConfig = if (workoutType == WorkoutType.INTERVAL_RUN) config as? IntervalConfig else null,
        strengthConfig = if (workoutType == WorkoutType.STRENGTH_TRAINING) config as? StrengthConfig else null,
        startTime = Instant.fromEpochMilliseconds(start_time),
        endTime = Instant.fromEpochMilliseconds(end_time),
        totalDuration = actual_duration_ms.milliseconds,
        completedRounds = completed_rounds,
        targetRounds = target_rounds,
        isCompleted = is_completed
    )
}

@OptIn(ExperimentalTime::class)
fun WorkoutRecord.toEntity(): WorkoutRecordEntity {
    val configJson = when (type) {
        WorkoutType.INTERVAL_RUN -> intervalConfig?.let { config ->
            json.encodeToString(
                IntervalConfigDto(
                    runDurationSeconds = config.runDuration.inWholeSeconds,
                    walkDurationSeconds = config.walkDuration.inWholeSeconds,
                    repeatCount = config.repeatCount,
                    warmupDurationSeconds = config.warmupDuration.inWholeSeconds,
                    cooldownDurationSeconds = config.cooldownDuration.inWholeSeconds,
                    runFirst = config.runFirst
                )
            )
        } ?: ""

        WorkoutType.STRENGTH_TRAINING -> strengthConfig?.let { config ->
            json.encodeToString(
                StrengthConfigDto(
                    exerciseName = config.exerciseName,
                    totalSets = config.sets,
                    restDurationSeconds = config.restDuration.inWholeSeconds
                )
            )
        } ?: ""
    }

    return WorkoutRecordEntity(
        id = id.toLongOrNull() ?: 0,
        workout_type = type.name,
        start_time = startTime.toEpochMilliseconds(),
        end_time = endTime.toEpochMilliseconds(),
        actual_duration_ms = totalDuration.inWholeMilliseconds,
        completed_rounds = completedRounds,
        target_rounds = targetRounds,
        is_completed = isCompleted,
        config_json = configJson
    )
}
