package com.dmirzayants.runlab.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.dmirzayants.runlab.R
import kotlinx.serialization.Serializable

@Serializable
data object SetupRoute

@Serializable
data object RunsRoute

@Serializable
data object StatisticsRoute

@Serializable
data object SettingsRoute

@Serializable
data object TrackingRoute

enum class TopLevelDestination(
    val route: Any,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {
    RUNS(RunsRoute, R.string.nav_runs, R.drawable.ic_run),
    STATISTICS(StatisticsRoute, R.string.nav_statistics, R.drawable.ic_graph),
    SETTINGS(SettingsRoute, R.string.nav_settings, R.drawable.ic_settings)
}
