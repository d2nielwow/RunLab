package com.dmirzayants.runlab.domain

import kotlin.math.roundToInt

/**
 * Energy expenditure estimate: kcal = MET × body weight (kg) × duration (h).
 *
 * MET values by speed are taken from the 2011 Compendium of Physical Activities
 * (walking codes 17xxx, running codes 12xxx) and linearly interpolated between points.
 * The old formula `km × weight` ignored pace entirely.
 */
object CaloriesCalculator {

    // speed km/h to MET
    private val metTable = listOf(
        0.0 to 1.3,   // standing
        3.2 to 2.8,
        4.0 to 3.0,
        4.8 to 3.5,
        5.6 to 4.3,
        6.4 to 5.0,   // brisk walk
        8.0 to 8.3,   // running from here on
        8.4 to 9.0,
        9.7 to 9.8,
        10.8 to 10.5,
        11.3 to 11.0,
        12.1 to 11.5,
        12.9 to 11.8,
        13.8 to 12.3,
        14.5 to 12.8,
        16.1 to 14.5,
        17.7 to 16.0,
        19.3 to 19.0,
        20.9 to 19.8,
        22.5 to 23.0
    )

    fun metForSpeed(speedKmh: Double): Double {
        if (speedKmh <= metTable.first().first) return metTable.first().second
        if (speedKmh >= metTable.last().first) return metTable.last().second
        val upperIndex = metTable.indexOfFirst { it.first >= speedKmh }
        val (x0, y0) = metTable[upperIndex - 1]
        val (x1, y1) = metTable[upperIndex]
        return y0 + (speedKmh - x0) / (x1 - x0) * (y1 - y0)
    }

    fun caloriesBurned(distanceInMeters: Int, timeInMillis: Long, weightKg: Float): Int {
        if (timeInMillis <= 0L || weightKg <= 0f) return 0
        val hours = timeInMillis / 3_600_000.0
        val speedKmh = distanceInMeters / 1000.0 / hours
        return (metForSpeed(speedKmh) * weightKg * hours).roundToInt()
    }

    fun avgSpeedKmh(distanceInMeters: Int, timeInMillis: Long): Float {
        if (timeInMillis <= 0L) return 0f
        val hours = timeInMillis / 3_600_000.0
        return ((distanceInMeters / 1000.0 / hours) * 10).roundToInt() / 10f
    }
}
