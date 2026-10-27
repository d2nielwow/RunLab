package com.dmirzayants.runlab.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A finished run. The map snapshot is stored as a file in internal storage and only its
 * absolute path is kept here: storing bitmaps as BLOBs bloats the database and can exceed
 * the 2 MB CursorWindow limit.
 */
@Entity(tableName = "running_table")
data class Run(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imagePath: String? = null,
    val timestamp: Long = 0L,
    val avgSpeedInKMH: Float = 0f,
    val distanceInMeters: Int = 0,
    val timeInMillis: Long = 0L,
    val caloriesBurned: Int = 0
)
