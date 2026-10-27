package com.dmirzayants.runlab.ui.runs

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dmirzayants.runlab.R
import com.dmirzayants.runlab.TrackingUtility
import com.dmirzayants.runlab.db.Run
import com.dmirzayants.runlab.domain.SortType
import com.dmirzayants.runlab.ui.common.RunLabTopBar
import com.dmirzayants.runlab.ui.common.caloriesText
import com.dmirzayants.runlab.ui.common.distanceText
import com.dmirzayants.runlab.ui.common.formatDate
import com.dmirzayants.runlab.ui.common.speedText
import java.io.File

private val SortType.label: Int
    get() = when (this) {
        SortType.DATE -> R.string.sort_date
        SortType.RUNNING_TIME -> R.string.sort_running_time
        SortType.DISTANCE -> R.string.sort_distance
        SortType.AVG_SPEED -> R.string.sort_avg_speed
        SortType.CALORIES_BURNED -> R.string.sort_calories
    }

@Composable
fun RunsScreen(
    userName: String,
    onStartRun: () -> Unit,
    viewModel: RunsViewModel = hiltViewModel()
) {
    val runs by viewModel.runs.collectAsStateWithLifecycle()
    val sortType by viewModel.sortType.collectAsStateWithLifecycle()
    var runToDelete by remember { mutableStateOf<Run?>(null) }

    val requestPermissionsAndStart = rememberLocationPermissionRequester(onGranted = onStartRun)

    Scaffold(
        topBar = { RunLabTopBar(userName = userName) },
        floatingActionButton = {
            FloatingActionButton(onClick = requestPermissionsAndStart) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_black),
                    contentDescription = stringResource(R.string.runs_start_run)
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            SortSelector(
                selected = sortType,
                onSelected = viewModel::sortRuns,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.End)
            )

            val currentRuns = runs
            when {
                currentRuns == null -> Unit
                currentRuns.isEmpty() -> EmptyRuns()
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentRuns, key = { it.id }) { run ->
                        RunCard(run = run, onLongClick = { runToDelete = run })
                    }
                }
            }
        }
    }

    runToDelete?.let { run ->
        AlertDialog(
            onDismissRequest = { runToDelete = null },
            icon = { Icon(painterResource(R.drawable.ic_delete), contentDescription = null) },
            title = { Text(stringResource(R.string.run_delete_title)) },
            text = { Text(stringResource(R.string.run_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRun(run)
                    runToDelete = null
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { runToDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun rememberLocationPermissionRequester(onGranted: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showRationale by rememberSaveable { mutableStateOf(false) }
    var askedOnEntry by rememberSaveable { mutableStateOf(false) }
    var startAfterGrant by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (TrackingUtility.hasLocationPermissions(context)) {
            if (startAfterGrant) onGranted()
        } else {
            showRationale = true
        }
        startAfterGrant = false
    }

    LaunchedEffect(Unit) {
        if (!askedOnEntry && !TrackingUtility.hasLocationPermissions(context)) {
            askedOnEntry = true
            launcher.launch(TrackingUtility.requiredPermissions)
        }
    }

    if (showRationale) {
        val permanentlyDenied = activity != null && TrackingUtility.locationPermissions.none {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.permission_title)) },
            text = { Text(stringResource(R.string.permission_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    if (permanentlyDenied) {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    } else {
                        launcher.launch(TrackingUtility.requiredPermissions)
                    }
                }) {
                    Text(
                        stringResource(
                            if (permanentlyDenied) R.string.permission_open_settings
                            else R.string.permission_grant
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    return {
        if (TrackingUtility.hasLocationPermissions(context)) {
            onGranted()
        } else {
            startAfterGrant = true
            launcher.launch(TrackingUtility.requiredPermissions)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelector(
    selected: SortType,
    onSelected: (SortType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = stringResource(selected.label),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.runs_sort_by)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(stringResource(type.label)) },
                    onClick = {
                        onSelected(type)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RunCard(run: Run, onLongClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onLongClick)
    ) {
        run.imagePath?.let { path ->
            AsyncImage(
                model = File(path),
                contentDescription = stringResource(R.string.run_map_snapshot),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RunValue(formatDate(run.timestamp))
            RunValue(TrackingUtility.getFormattedStopWatchTime(run.timeInMillis))
            RunValue(distanceText(run.distanceInMeters))
            RunValue(speedText(run.avgSpeedInKMH))
            RunValue(caloriesText(run.caloriesBurned))
        }
    }
}

@Composable
private fun RunValue(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun EmptyRuns() {
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.runs_empty),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}
