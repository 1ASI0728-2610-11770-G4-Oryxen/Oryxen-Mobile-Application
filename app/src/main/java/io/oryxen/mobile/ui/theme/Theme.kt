package io.oryxen.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val OryxenGreen = Color(0xFF8AC73D)
val OryxenAccent = Color(0xFF002933)
val OryxenBackground = Color(0xFFF7F7ED)

private val OryxenColors = lightColorScheme(
    primary = OryxenGreen,
    onPrimary = Color.White,
    secondary = OryxenAccent,
    onSecondary = Color.White,
    background = OryxenBackground,
    onBackground = OryxenAccent,
    surface = Color.White,
    onSurface = OryxenAccent,
)

@Composable
fun OryxenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OryxenColors,
        typography = Typography(),
        content = content,
    )
}
