package com.github.intervalpacer.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_records",
    indices = [
        Index(value = ["start_time"]),
        Index(value = ["workout_type"])
    ]
)
data class WorkoutRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workout_type: String,
    val start_time: Long,
    val end_time: Long,
    val actual_duration_ms: Long,
    val completed_rounds: Int,
    val target_rounds: Int,
    val is_completed: Boolean,
    val config_json: String
)
