package com.github.intervalpacer.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IntervalConfigDto(
    val runDurationSeconds: Long,
    val walkDurationSeconds: Long,
    val repeatCount: Int,
    val warmupDurationSeconds: Long = 0,
    val cooldownDurationSeconds: Long = 0,
    val runFirst: Boolean = true
)
