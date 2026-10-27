package com.dmirzayants.runlab.ui.tracking

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmirzayants.runlab.TrackingUtility
import com.dmirzayants.runlab.db.Run
import com.dmirzayants.runlab.domain.CaloriesCalculator
import com.dmirzayants.runlab.prefs.UserPreferencesRepository
import com.dmirzayants.runlab.repository.RunRepository
import com.dmirzayants.runlab.service.TrackingManager
import com.dmirzayants.runlab.service.TrackingServiceController
import com.dmirzayants.runlab.service.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    trackingManager: TrackingManager,
    private val serviceController: TrackingServiceController,
    private val runRepository: RunRepository,
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    val trackingState: StateFlow<TrackingState> = trackingManager.state

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun toggleRun() {
        if (trackingState.value.isTracking) serviceController.pause() else serviceController.startOrResume()
    }

    fun cancelRun() = serviceController.stop()

    /** Pauses tracking so the numbers stop changing while the map snapshot is taken. */
    fun prepareFinish() {
        if (trackingState.value.isTracking) serviceController.pause()
        _isSaving.value = true
    }

    fun finishRun(snapshot: Bitmap?, onSaved: () -> Unit) {
        val state = trackingState.value
        viewModelScope.launch {
            val weight = userPreferences.userProfile.first().weightKg
            val distance = TrackingUtility.calculateDistanceInMeters(state.pathPoints)
            val run = Run(
                timestamp = System.currentTimeMillis(),
                avgSpeedInKMH = CaloriesCalculator.avgSpeedKmh(distance, state.timeRunInMillis),
                distanceInMeters = distance,
                timeInMillis = state.timeRunInMillis,
                caloriesBurned = CaloriesCalculator.caloriesBurned(distance, state.timeRunInMillis, weight)
            )
            runRepository.saveRun(snapshot, run)
            serviceController.stop()
            _isSaving.value = false
            onSaved()
        }
    }
}
