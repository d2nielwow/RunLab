package com.dmirzayants.runlab.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): Flow<List<Run>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): Flow<List<Run>>

    @Query("SELECT COALESCE(SUM(timeInMillis), 0) FROM running_table")
    fun getTotalTimeInMillis(): Flow<Long>

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM running_table")
    fun getTotalCaloriesBurned(): Flow<Int>

    @Query("SELECT COALESCE(SUM(distanceInMeters), 0) FROM running_table")
    fun getTotalDistance(): Flow<Int>

    @Query("SELECT COALESCE(AVG(avgSpeedInKMH), 0) FROM running_table")
    fun getTotalAvgSpeed(): Flow<Float>
}
