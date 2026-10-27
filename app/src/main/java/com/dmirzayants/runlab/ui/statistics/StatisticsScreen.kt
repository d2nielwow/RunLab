package com.dmirzayants.runlab.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dmirzayants.runlab.R
import com.dmirzayants.runlab.TrackingUtility
import com.dmirzayants.runlab.db.Run
import com.dmirzayants.runlab.ui.common.RunLabTopBar
import com.dmirzayants.runlab.ui.common.caloriesText
import com.dmirzayants.runlab.ui.common.distanceText
import com.dmirzayants.runlab.ui.common.formatDate
import com.dmirzayants.runlab.ui.common.speedText
import com.dmirzayants.runlab.ui.theme.Accent
import com.dmirzayants.runlab.ui.theme.Blue900
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnModel
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent

@Composable
fun StatisticsScreen(
    userName: String,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { RunLabTopBar(userName = userName) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    value = TrackingUtility.getFormattedStopWatchTime(state.totalTimeInMillis),
                    label = stringResource(R.string.stats_total_time),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = distanceText(state.totalDistanceInMeters),
                    label = stringResource(R.string.stats_total_distance),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    value = caloriesText(state.totalCalories),
                    label = stringResource(R.string.stats_total_calories),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = speedText(state.avgSpeedKmh),
                    label = stringResource(R.string.stats_avg_speed),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = stringResource(R.string.stats_chart_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (state.runsChronological.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.stats_no_runs))
                }
            } else {
                AvgSpeedChart(runs = state.runsChronological)
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AvgSpeedChart(runs: List<Run>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(runs) {
        modelProducer.runTransaction {
            columnModel { series(runs.map { it.avgSpeedInKMH }) }
        }
    }

    // Marker labels are plain strings, so they are prepared here with the current locale.
    val resources = LocalResources.current
    val markerLabels = remember(runs, resources) {
        runs.map { run ->
            listOf(
                formatDate(run.timestamp),
                TrackingUtility.getFormattedStopWatchTime(run.timeInMillis),
                resources.getString(R.string.format_speed_kmh, run.avgSpeedInKMH),
                resources.getString(R.string.format_distance_km, run.distanceInMeters / 1000f),
                resources.getString(R.string.format_calories, run.caloriesBurned)
            ).joinToString("\n")
        }
    }
    val markerFormatter = remember(markerLabels) {
        DefaultCartesianMarker.ValueFormatter { _, targets ->
            markerLabels.getOrNull(targets.first().x.toInt()).orEmpty()
        }
    }

    val axisText = TextStyle(color = Color.White, fontSize = 12.sp)
    val axisLine = rememberAxisLineComponent(fill = Fill(Color.White))
    val marker = rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            style = TextStyle(color = Color.White, fontSize = 14.sp),
            lineCount = 5,
            padding = Insets(horizontal = 12.dp, vertical = 8.dp),
            background = rememberShapeComponent(fill = Fill(Blue900), shape = RoundedCornerShape(12.dp))
        ),
        valueFormatter = markerFormatter
    )

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(
                        fill = Fill(Accent),
                        thickness = 16.dp,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                line = axisLine,
                label = rememberAxisLabelComponent(style = axisText),
                guideline = null
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                line = axisLine,
                label = null,
                guideline = null
            ),
            marker = marker
        ),
        modelProducer = modelProducer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
