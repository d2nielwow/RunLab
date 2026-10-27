package com.dmirzayants.runlab.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Run::class],
    version = 2,
    exportSchema = false
)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDao
}
