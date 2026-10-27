package com.dmirzayants.runlab.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.dmirzayants.runlab.constants.Constants
import com.dmirzayants.runlab.db.Migration1To2
import com.dmirzayants.runlab.db.RunDao
import com.dmirzayants.runlab.db.RunningDatabase
import com.dmirzayants.runlab.repository.RunImageStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext ctx: Context,
        imageStorage: RunImageStorage
    ): RunningDatabase = Room.databaseBuilder(
        ctx,
        RunningDatabase::class.java,
        Constants.RUNNING_DATABASE_NAME
    )
        .addMigrations(Migration1To2(imageStorage.imagesDir))
        .build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase): RunDao = db.getRunDao()

    @Singleton
    @Provides
    fun provideUserPreferencesDataStore(
        @ApplicationContext ctx: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
        migrations = listOf(
            SharedPreferencesMigration(ctx, Constants.LEGACY_SHARED_PREFERENCES_NAME)
        ),
        produceFile = { ctx.preferencesDataStoreFile(Constants.USER_PREFERENCES_NAME) }
    )
}
