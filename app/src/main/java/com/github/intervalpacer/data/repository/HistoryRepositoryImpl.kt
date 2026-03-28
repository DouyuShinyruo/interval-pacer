package com.github.intervalpacer.data.repository

import android.util.Log
import com.github.intervalpacer.data.local.AppDatabase
import com.github.intervalpacer.data.local.toDomainModel
import com.github.intervalpacer.data.local.toEntity
import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.model.WorkoutType
import com.github.intervalpacer.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val database: AppDatabase
) : HistoryRepository {

    companion object {
        private const val TAG = "HistoryRepositoryImpl"
    }

    private val workoutDao = database.workoutDao()

    override fun getAllRecords(): Flow<List<WorkoutRecord>> {
        return workoutDao.getAllRecords()
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun saveRecord(record: WorkoutRecord): Boolean {
        return try {
            val entity = record.toEntity()
            workoutDao.insert(entity)
            Log.d(TAG, "Saved workout record: ${record.id}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save workout record: ${record.id}", e)
            false
        }
    }

    override fun getRecordsByType(type: WorkoutType): Flow<List<WorkoutRecord>> {
        return workoutDao.getRecordsByType(type.name)
            .map { entities -> entities.map { it.toDomainModel() } }
    }

    override suspend fun deleteRecord(id: String) {
        try {
            val idLong = id.toLongOrNull() ?: return
            workoutDao.deleteById(idLong)
            Log.d(TAG, "Deleted workout record: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete workout record: $id", e)
        }
    }

    override suspend fun clearAllRecords() {
        try {
            workoutDao.deleteAll()
            Log.d(TAG, "Cleared all workout records")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear workout records", e)
        }
    }

    override suspend fun getRecordCount(): Int {
        return try {
            workoutDao.getRecordCount()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get record count", e)
            0
        }
    }
}
