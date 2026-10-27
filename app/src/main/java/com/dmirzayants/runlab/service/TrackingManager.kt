package com.dmirzayants.runlab.service

import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

data class TrackingState(
    val isTracking: Boolean = false,
    val pathPoints: List<List<LatLng>> = emptyList(),
    val timeRunInMillis: Long = 0L
) {
    val hasStarted: Boolean get() = timeRunInMillis > 0L
}

@Singleton
class TrackingManager @Inject constructor() {

    private val _state = MutableStateFlow(TrackingState())
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    fun setTracking(isTracking: Boolean) = _state.update { it.copy(isTracking = isTracking) }

    fun startNewSegment() = _state.update { it.copy(pathPoints = it.pathPoints + listOf(emptyList())) }

    fun addPathPoint(point: LatLng) = _state.update { state ->
        val segments = state.pathPoints.ifEmpty { listOf(emptyList()) }
        state.copy(pathPoints = segments.dropLast(1) + listOf(segments.last() + point))
    }

    fun setTime(timeInMillis: Long) = _state.update { it.copy(timeRunInMillis = timeInMillis) }

    fun reset() {
        _state.value = TrackingState()
    }
}
