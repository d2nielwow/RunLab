package com.dmirzayants.runlab

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import java.util.Locale
import java.util.concurrent.TimeUnit

object TrackingUtility {

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            locationPermissions + Manifest.permission.POST_NOTIFICATIONS
        } else {
            locationPermissions
        }

    fun hasLocationPermissions(context: Context) =
        locationPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    fun calculatePolylineLength(polyline: List<LatLng>): Float {
        var distance = 0f
        val result = FloatArray(1)
        for (i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
        return distance
    }

    fun calculateDistanceInMeters(polylines: List<List<LatLng>>): Int =
        polylines.sumOf { calculatePolylineLength(it).toDouble() }.toInt()

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        val hours = TimeUnit.MILLISECONDS.toHours(ms)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60
        if (!includeMillis) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
        }
        val centis = (ms % 1000) / 10
        return String.format(Locale.US, "%02d:%02d:%02d:%02d", hours, minutes, seconds, centis)
    }
}
