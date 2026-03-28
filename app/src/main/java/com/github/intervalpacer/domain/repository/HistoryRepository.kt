package com.github.intervalpacer.domain.repository

import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.model.WorkoutType
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {

    fun getAllRecords(): Flow<List<WorkoutRecord>>

    suspend fun saveRecord(record: WorkoutRecord): Boolean

    fun getRecordsByType(type: WorkoutType): Flow<List<WorkoutRecord>>

    suspend fun deleteRecord(id: String)

    suspend fun clearAllRecords()

    suspend fun getRecordCount(): Int
}
