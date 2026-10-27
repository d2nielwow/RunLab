package com.dmirzayants.runlab.ui.tracking

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dmirzayants.runlab.R
import com.dmirzayants.runlab.TrackingUtility
import com.dmirzayants.runlab.constants.Constants
import com.dmirzayants.runlab.ui.common.RunLabTopBar
import com.dmirzayants.runlab.ui.common.distanceText
import com.dmirzayants.runlab.ui.theme.Blue800
import com.dmirzayants.runlab.ui.theme.RouteRed
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun TrackingScreen(
    userName: String,
    onRunCancelled: () -> Unit,
    onRunSaved: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel()
) {
    val state by viewModel.trackingState.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }

    // The timer recomposes every 50 ms; only recompute the distance when the path changes.
    val distanceInMeters = remember(state.pathPoints) {
        TrackingUtility.calculateDistanceInMeters(state.pathPoints)
    }

    // Follow the runner while not in the middle of saving.
    val lastPoint = state.pathPoints.lastOrNull()?.lastOrNull()
    LaunchedEffect(lastPoint) {
        if (lastPoint != null && !isSaving) {
            try {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(lastPoint, Constants.MAP_ZOOM)
                )
            } catch (_: CancellationException) {
                // Superseded by a newer location or by the finish animation.
            }
        }
    }

    fun finishRun() {
        viewModel.prepareFinish()
        scope.launch {
            val points = state.pathPoints.flatten()
            if (points.isNotEmpty()) {
                val bounds = LatLngBounds.Builder().apply { points.forEach(::include) }.build()
                val padding = with(density) { 48.dp.roundToPx() }
                cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                delay(SNAPSHOT_RENDER_DELAY_MS) // let the tiles for the new camera render
            }
            // The SDK may never call back (e.g. map not rendered); save the run without an image then.
            val bitmap = withTimeoutOrNull(SNAPSHOT_TIMEOUT_MS) { googleMap?.awaitSnapshot() }
            viewModel.finishRun(bitmap, onRunSaved)
        }
    }

    Scaffold(
        topBar = {
            RunLabTopBar(userName = userName) {
                if (state.hasStarted && !isSaving) {
                    IconButton(onClick = { showCancelDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close_white),
                            contentDescription = stringResource(R.string.tracking_cancel_run)
                        )
                    }
                }
            }
        }
    ) { padding ->
        BoxWithConstraints(modifier = Modifier.padding(padding).fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            val map: @Composable (Modifier) -> Unit = { modifier ->
                GoogleMap(
                    modifier = modifier,
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = TrackingUtility.hasLocationPermissions(context)
                    ),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false)
                ) {
                    MapEffect(Unit) { googleMap = it }
                    state.pathPoints.forEach { segment ->
                        if (segment.size > 1) {
                            Polyline(points = segment, color = RouteRed, width = Constants.POLYLINE_WIDTH)
                        }
                    }
                }
            }
            val controls: @Composable (Modifier) -> Unit = { modifier ->
                TrackingControls(
                    timeInMillis = state.timeRunInMillis,
                    distanceInMeters = distanceInMeters,
                    isTracking = state.isTracking,
                    hasStarted = state.hasStarted,
                    isSaving = isSaving,
                    onToggle = viewModel::toggleRun,
                    onFinish = ::finishRun,
                    modifier = modifier
                )
            }

            if (isLandscape) {
                Row(Modifier.fillMaxSize()) {
                    controls(Modifier.weight(1f).fillMaxSize())
                    map(Modifier.weight(1f).fillMaxSize())
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    map(Modifier.weight(1f).fillMaxWidth())
                    controls(Modifier.fillMaxWidth())
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null) },
            title = { Text(stringResource(R.string.tracking_cancel_title)) },
            text = { Text(stringResource(R.string.tracking_cancel_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    viewModel.cancelRun()
                    onRunCancelled()
                }) { Text(stringResource(R.string.action_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.action_no))
                }
            }
        )
    }
}

@Composable
private fun TrackingControls(
    timeInMillis: Long,
    distanceInMeters: Int,
    isTracking: Boolean,
    hasStarted: Boolean,
    isSaving: Boolean,
    onToggle: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(Blue800).padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = TrackingUtility.getFormattedStopWatchTime(timeInMillis, includeMillis = true),
                style = MaterialTheme.typography.displayMedium,
                fontFamily = FontFamily.Monospace
            )
            Text(text = distanceText(distanceInMeters), style = MaterialTheme.typography.titleLarge)

            if (isSaving) {
                CircularProgressIndicator()
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onToggle) {
                        Text(
                            stringResource(if (isTracking) R.string.tracking_stop else R.string.tracking_start)
                        )
                    }
                    if (!isTracking && hasStarted) {
                        OutlinedButton(onClick = onFinish) {
                            Text(stringResource(R.string.tracking_finish))
                        }
                    }
                }
            }
        }
    }
}

private suspend fun GoogleMap.awaitSnapshot(): Bitmap? = suspendCancellableCoroutine { cont ->
    snapshot { bitmap -> if (cont.isActive) cont.resume(bitmap) }
}

private const val SNAPSHOT_RENDER_DELAY_MS = 700L
private const val SNAPSHOT_TIMEOUT_MS = 3_000L
