package com.dmirzayants.runlab.service

import android.content.Context
import android.content.Intent
import com.dmirzayants.runlab.constants.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingServiceController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun startOrResume() = send(Constants.ACTION_START_OR_RESUME_SERVICE)
    fun pause() = send(Constants.ACTION_PAUSE_SERVICE)
    fun stop() = send(Constants.ACTION_STOP_SERVICE)

    private fun send(action: String) {
        context.startService(Intent(context, TrackingService::class.java).setAction(action))
    }
}
