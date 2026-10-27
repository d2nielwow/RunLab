package com.dmirzayants.runlab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Blue50 = Color(0xFFE3F2FD)
val Blue500 = Color(0xFF2196F3)
val Blue700 = Color(0xFF1976D2)
val Blue800 = Color(0xFF1565C0)
val Blue900 = Color(0xFF0D47A1)
val Accent = Color(0xFFFFE600)
val ErrorRed = Color(0xFFFF8A80)
val RouteRed = Color(0xFFE53935)

// The app has always been light-on-dark-blue, so there is a single (dark) scheme.
private val RunLabColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.Black,
    primaryContainer = Accent,
    onPrimaryContainer = Color.Black,
    secondary = Blue500,
    onSecondary = Blue50,
    secondaryContainer = Blue800,
    onSecondaryContainer = Color.White,
    background = Blue700,
    onBackground = Blue50,
    surface = Blue900,
    onSurface = Color.White,
    surfaceVariant = Blue800,
    onSurfaceVariant = Blue50,
    surfaceContainer = Blue900,
    surfaceContainerLow = Blue800,
    surfaceContainerHigh = Blue800,
    surfaceContainerHighest = Blue800,
    outline = Blue50.copy(alpha = 0.6f),
    error = ErrorRed,
    onError = Color.Black
)

@Composable
fun RunLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = RunLabColors, content = content)
}
