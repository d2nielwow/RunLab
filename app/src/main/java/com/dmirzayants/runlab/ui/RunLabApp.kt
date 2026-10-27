package com.dmirzayants.runlab.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dmirzayants.runlab.R
import com.dmirzayants.runlab.prefs.UserProfile
import com.dmirzayants.runlab.ui.navigation.RunsRoute
import com.dmirzayants.runlab.ui.navigation.SettingsRoute
import com.dmirzayants.runlab.ui.navigation.SetupRoute
import com.dmirzayants.runlab.ui.navigation.StatisticsRoute
import com.dmirzayants.runlab.ui.navigation.TopLevelDestination
import com.dmirzayants.runlab.ui.navigation.TrackingRoute
import com.dmirzayants.runlab.ui.profile.SettingsScreen
import com.dmirzayants.runlab.ui.profile.SetupScreen
import com.dmirzayants.runlab.ui.runs.RunsScreen
import com.dmirzayants.runlab.ui.statistics.StatisticsScreen
import com.dmirzayants.runlab.ui.tracking.TrackingScreen
import kotlinx.coroutines.launch

/**
 * @param openTrackingRequested true when the activity was opened from the tracking notification.
 */
@Composable
fun RunLabApp(
    profile: UserProfile,
    openTrackingRequested: Boolean,
    onOpenTrackingHandled: () -> Unit
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val runSavedMessage = stringResource(R.string.tracking_run_saved)

    // Decided once: changing it later would rebuild the graph.
    val startDestination: Any = remember { if (profile.isOnboarded) RunsRoute else SetupRoute }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = TopLevelDestination.entries.any { dest ->
        currentDestination?.hierarchy?.any { it.hasRoute(dest.route::class) } == true
    }

    LaunchedEffect(openTrackingRequested) {
        if (openTrackingRequested) {
            navController.navigate(TrackingRoute) { launchSingleTop = true }
            onOpenTrackingHandled()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                RunLabBottomBar(navController = navController, currentDestinationHas = { route ->
                    currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true
                })
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable<SetupRoute> {
                SetupScreen(onSetupFinished = {
                    navController.navigate(RunsRoute) {
                        popUpTo(SetupRoute) { inclusive = true }
                    }
                })
            }
            composable<RunsRoute> {
                RunsScreen(
                    userName = profile.name,
                    onStartRun = { navController.navigate(TrackingRoute) { launchSingleTop = true } }
                )
            }
            composable<StatisticsRoute> {
                StatisticsScreen(userName = profile.name)
            }
            composable<SettingsRoute> {
                SettingsScreen(snackbarHostState = snackbarHostState)
            }
            composable<TrackingRoute> {
                val backToRuns: () -> Unit = {
                    navController.navigate(RunsRoute) {
                        popUpTo(RunsRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                }
                TrackingScreen(
                    userName = profile.name,
                    onRunCancelled = backToRuns,
                    onRunSaved = {
                        backToRuns()
                        scope.launch { snackbarHostState.showSnackbar(runSavedMessage) }
                    }
                )
            }
        }
    }
}

@Composable
private fun RunLabBottomBar(
    navController: NavHostController,
    currentDestinationHas: (Any) -> Boolean
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentDestinationHas(destination.route),
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                label = { Text(stringResource(destination.label)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}
