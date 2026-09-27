package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestDao {
    @Query("SELECT * FROM speed_tests ORDER BY timestamp DESC")
    fun getAllTests(): Flow<List<SpeedTestEntity>>

    @Query("SELECT * FROM speed_tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: Long): SpeedTestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: SpeedTestEntity): Long

    @Query("DELETE FROM speed_tests WHERE id = :id")
    suspend fun deleteTest(id: Long)

    @Query("DELETE FROM speed_tests")
    suspend fun clearAll()
}
