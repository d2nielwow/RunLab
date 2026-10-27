package com.dmirzayants.runlab.repository

import android.graphics.Bitmap
import com.dmirzayants.runlab.db.Run
import com.dmirzayants.runlab.db.RunDao
import com.dmirzayants.runlab.domain.SortType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepository @Inject constructor(
    private val runDao: RunDao,
    private val imageStorage: RunImageStorage
) {
    fun getRuns(sortType: SortType): Flow<List<Run>> = when (sortType) {
        SortType.DATE -> runDao.getAllRunsSortedByDate()
        SortType.RUNNING_TIME -> runDao.getAllRunsSortedByTimeInMillis()
        SortType.DISTANCE -> runDao.getAllRunsSortedByDistance()
        SortType.AVG_SPEED -> runDao.getAllRunsSortedByAvgSpeed()
        SortType.CALORIES_BURNED -> runDao.getAllRunsSortedByCaloriesBurned()
    }

    suspend fun saveRun(snapshot: Bitmap?, run: Run) {
        val imagePath = snapshot?.let { imageStorage.save(it, run.timestamp) }
        runDao.insertRun(run.copy(imagePath = imagePath))
    }

    suspend fun deleteRun(run: Run) {
        runDao.deleteRun(run)
        imageStorage.delete(run.imagePath)
    }

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()
    fun getTotalDistance() = runDao.getTotalDistance()
    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()
    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()
}
