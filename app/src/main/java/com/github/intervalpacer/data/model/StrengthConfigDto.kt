package com.github.intervalpacer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StrengthConfigDto(
    val exerciseName: String = "",
    val totalSets: Int = 5,
    val restDurationSeconds: Long = 90
)
