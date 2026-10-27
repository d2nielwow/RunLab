package com.dmirzayants.runlab.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dmirzayants.runlab.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

fun formatDate(timestamp: Long): String =
    Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(dateFormatter)

@Composable
fun distanceText(meters: Int) = stringResource(R.string.format_distance_km, meters / 1000f)

@Composable
fun speedText(kmh: Float) = stringResource(R.string.format_speed_kmh, kmh)

@Composable
fun caloriesText(kcal: Int) = stringResource(R.string.format_calories, kcal)
