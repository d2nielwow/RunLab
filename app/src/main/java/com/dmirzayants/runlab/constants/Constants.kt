package com.dmirzayants.runlab.constants

object Constants {

    const val RUNNING_DATABASE_NAME = "running_db"
    const val RUN_IMAGES_DIR = "run_images"

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_SCREEN = "ACTION_SHOW_TRACKING_SCREEN"

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L
    const val POLYLINE_WIDTH = 8f
    const val MAP_ZOOM = 15f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_ID = 1
    const val TIMER_UPDATE_INTERVAL = 50L

    const val USER_PREFERENCES_NAME = "user_preferences"
    // SharedPreferences file used before the move to DataStore; migrated on first read.
    const val LEGACY_SHARED_PREFERENCES_NAME = "sharedPref"
    const val DEFAULT_WEIGHT = 80f
}
