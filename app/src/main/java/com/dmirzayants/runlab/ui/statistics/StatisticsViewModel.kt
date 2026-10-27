package com.dmirzayants.runlab.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmirzayants.runlab.db.Run
import com.dmirzayants.runlab.domain.SortType
import com.dmirzayants.runlab.repository.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StatisticsUiState(
    val totalTimeInMillis: Long = 0L,
    val totalDistanceInMeters: Int = 0,
    val totalCalories: Int = 0,
    val avgSpeedKmh: Float = 0f,
    /** Oldest first, so the chart reads left to right. */
    val runsChronological: List<Run> = emptyList()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    runRepository: RunRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        runRepository.getTotalTimeInMillis(),
        runRepository.getTotalDistance(),
        runRepository.getTotalCaloriesBurned(),
        runRepository.getTotalAvgSpeed(),
        runRepository.getRuns(SortType.DATE).map { it.reversed() }
    ) { time, distance, calories, speed, runs ->
        StatisticsUiState(time, distance, calories, speed, runs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState())
}
