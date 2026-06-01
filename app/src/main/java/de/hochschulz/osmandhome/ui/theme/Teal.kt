package de.hochschulz.osmandhome.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Teal     = Color(0xFF01696F)
val TealDark = Color(0xFF004F54)

private val LightColors = lightColorScheme(
    primary           = Teal,
    onPrimary         = Color.White,
    primaryContainer  = Color(0xFFCEDCD8),
    background        = Color(0xFFF7F6F2),
    surface           = Color(0xFFF9F8F5),
    onSurface         = Color(0xFF28251D),
    onBackground      = Color(0xFF28251D),
    secondary         = Color(0xFF437A22),
    onSecondary       = Color.White,
    outline           = Color(0xFFD4D1CA),
    surfaceVariant    = Color(0xFFEDEAE5),
    onSurfaceVariant  = Color(0xFF7A7974)
)

private val DarkColors = darkColorScheme(
    primary           = Color(0xFF4F98A3),
    onPrimary         = Color(0xFF002B2E),
    primaryContainer  = Color(0xFF313B3B),
    background        = Color(0xFF171614),
    surface           = Color(0xFF1C1B19),
    onSurface         = Color(0xFFCDCCCA),
    onBackground      = Color(0xFFCDCCCA),
    secondary         = Color(0xFF6DAA45),
    onSecondary       = Color.White,
    outline           = Color(0xFF393836),
    surfaceVariant    = Color(0xFF22211F),
    onSurfaceVariant  = Color(0xFF797876)
)

@Composable
fun HaTrackerTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}