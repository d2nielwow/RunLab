package com.dmirzayants.runlab.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.dmirzayants.runlab.R
import com.dmirzayants.runlab.TrackingUtility
import com.dmirzayants.runlab.constants.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var trackingManager: TrackingManager

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private var isFirstRun = true
    private var serviceKilled = false

    private var timerJob: Job? = null
    private var timeRun = 0L
    private var timeStarted = 0L

    override fun onCreate() {
        super.onCreate()
        trackingManager.reset()

        lifecycleScope.launch {
            trackingManager.state
                .map { it.isTracking }
                .distinctUntilChanged()
                .collect {
                    updateLocationTracking(it)
                    updateNotificationTrackingState(it)
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_OR_RESUME_SERVICE -> {
                if (isFirstRun) {
                    // Cleared first: the isTracking collector skips notification updates while it is set.
                    isFirstRun = false
                    startForegroundService()
                } else {
                    Timber.d("Resuming service...")
                    startTimer()
                }
            }

            Constants.ACTION_PAUSE_SERVICE -> {
                Timber.d("Paused service")
                pauseTimer()
            }

            Constants.ACTION_STOP_SERVICE -> {
                Timber.d("Stopped service")
                killService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        trackingManager.startNewSegment()
        trackingManager.setTracking(true)
        timeStarted = System.currentTimeMillis()
        timerJob = lifecycleScope.launch {
            while (isActive) {
                trackingManager.setTime(timeRun + System.currentTimeMillis() - timeStarted)
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
        }
    }

    private fun pauseTimer() {
        if (timerJob?.isActive != true) return
        timerJob?.cancel()
        timeRun += System.currentTimeMillis() - timeStarted
        trackingManager.setTime(timeRun)
        trackingManager.setTracking(false)
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        if (serviceKilled || isFirstRun) return
        val actionText = getString(if (isTracking) R.string.action_pause else R.string.action_resume)
        val action =
            if (isTracking) Constants.ACTION_PAUSE_SERVICE else Constants.ACTION_START_OR_RESUME_SERVICE
        val pendingIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TrackingService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        baseNotificationBuilder
            .clearActions()
            .addAction(R.drawable.ic_pause_black_24dp, actionText, pendingIntent)
        notificationManager.notify(Constants.NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    Constants.LOCATION_UPDATE_INTERVAL
                )
                    .setMinUpdateIntervalMillis(Constants.FASTEST_LOCATION_INTERVAL)
                    .build()
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            if (!trackingManager.state.value.isTracking) return
            for (location in result.locations) {
                trackingManager.addPathPoint(LatLng(location.latitude, location.longitude))
            }
        }
    }

    private fun killService() {
        serviceKilled = true
        timerJob?.cancel()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        trackingManager.reset()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startForegroundService() {
        createNotificationChannel()

        // Must be called within a few seconds of startService(); on Android 14+ the type is mandatory.
        ServiceCompat.startForeground(
            this,
            Constants.NOTIFICATION_ID,
            baseNotificationBuilder.build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                0
            }
        )

        startTimer()

        lifecycleScope.launch {
            trackingManager.state
                .map { it.timeRunInMillis / 1000L }
                .distinctUntilChanged()
                .drop(1)
                .collect { seconds ->
                    if (!serviceKilled) {
                        baseNotificationBuilder.setContentText(
                            TrackingUtility.getFormattedStopWatchTime(seconds * 1000L)
                        )
                        notificationManager.notify(
                            Constants.NOTIFICATION_ID,
                            baseNotificationBuilder.build()
                        )
                    }
                }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_tracking),
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }
}
