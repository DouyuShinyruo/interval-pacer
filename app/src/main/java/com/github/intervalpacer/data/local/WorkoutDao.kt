package com.github.intervalpacer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: WorkoutRecordEntity): Long

    @Query("SELECT * FROM workout_records ORDER BY start_time DESC")
    fun getAllRecords(): Flow<List<WorkoutRecordEntity>>

    @Query("SELECT * FROM workout_records ORDER BY start_time DESC LIMIT :limit OFFSET :offset")
    suspend fun getRecordsPaged(limit: Int, offset: Int): List<WorkoutRecordEntity>

    @Query("SELECT * FROM workout_records WHERE workout_type = :type ORDER BY start_time DESC")
    fun getRecordsByType(type: String): Flow<List<WorkoutRecordEntity>>

    @Query("SELECT * FROM workout_records WHERE id = :id")
    suspend fun getRecordById(id: Long): WorkoutRecordEntity?

    @Query("SELECT COUNT(*) FROM workout_records")
    suspend fun getRecordCount(): Int

    @Query("DELETE FROM workout_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM workout_records")
    suspend fun deleteAll()

    @Query("SELECT * FROM workout_records WHERE start_time >= :startTime AND start_time <= :endTime ORDER BY start_time DESC")
    fun getRecordsByDateRange(startTime: Long, endTime: Long): Flow<List<WorkoutRecordEntity>>

    @Query("SELECT SUM(actual_duration_ms) FROM workout_records")
    suspend fun getTotalDurationMs(): Long

    @Query("SELECT COUNT(*) FROM workout_records WHERE workout_type = :type")
    suspend fun getCountByType(type: String): Int
}
