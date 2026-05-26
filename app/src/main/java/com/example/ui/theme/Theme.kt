package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SolarPrimary = Color(0xFFFFB300) // Amber Yellow
val SolarSecondary = Color(0xFF00E5FF) // Electric cyan
val SolarSkyBlue = Color(0xFF0284C7) // Celestial Sky Blue
val BackgroundDark = Color(0xFF0B111E) // Slate Dark night
val SurfaceDark = Color(0xFF152033) // Card color
val SurfaceVariantDark = Color(0xFF1F2F4B) // Card border/elevated state
val BorderDark = Color(0xFF2E456E) // Stroke color
val AccentOrange = Color(0xFFF97316) // Energetic Spark Orange

private val DarkColorScheme = darkColorScheme(
    primary = SolarPrimary,
    secondary = SolarSecondary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = SurfaceVariantDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // A dark atmospheric cosmic slate aesthetic matches the precision of solar sizing perfectly
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
