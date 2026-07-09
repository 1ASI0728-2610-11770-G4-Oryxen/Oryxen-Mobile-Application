package io.oryxen.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.darkColorScheme
import androidx.compose.foundation.isSystemInDarkTheme

// ── Brand Palette ───────────────────────────────────────────────────────
val OryxenGreen      = Color(0xFF2E7D32)   // Primary – deep green from wireflow
val OryxenGreenLight = Color(0xFF4CAF50)   // lighter variant for accents
val OryxenGreenSoft  = Color(0xFFE8F5E9)   // very light green for tinted surfaces
val OryxenAccent     = Color(0xFF1B2B1F)   // near-black green for text
val OryxenBackground = Color(0xFFF9FBF7)   // warm white background
val OryxenGray       = Color(0xFF6B7280)   // secondary text
val OryxenGrayLight  = Color(0xFFF3F4F6)   // input background

private val OryxenColors = lightColorScheme(
    primary          = OryxenGreen,
    onPrimary        = Color.White,
    primaryContainer = OryxenGreenSoft,
    onPrimaryContainer = OryxenGreen,
    secondary        = OryxenGreenLight,
    onSecondary      = Color.White,
    background       = OryxenBackground,
    onBackground     = OryxenAccent,
    surface          = Color.White,
    onSurface        = OryxenAccent,
    surfaceVariant   = OryxenGrayLight,
    onSurfaceVariant = OryxenGray,
    outline          = Color(0xFFD1D5DB),
    outlineVariant   = Color(0xFFE5E7EB),
    error            = Color(0xFFDC2626),
    onError          = Color.White,
)

// ── Typography ──────────────────────────────────────────────────────────
private val OryxenTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

// ── Dark Colors ─────────────────────────────────────────────────────────
private val OryxenDarkColors = darkColorScheme(
    primary          = OryxenGreen,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFF1B2B1F),
    onPrimaryContainer = OryxenGreenLight,
    secondary        = OryxenGreenLight,
    onSecondary      = Color.White,
    background       = Color(0xFF121212),
    onBackground     = Color(0xFFE5E7EB),
    surface          = Color(0xFF1E1E1E),
    onSurface        = Color(0xFFE5E7EB),
    surfaceVariant   = Color(0xFF2D2D2D),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline          = Color(0xFF4B5563),
    outlineVariant   = Color(0xFF374151),
    error            = Color(0xFFEF4444),
    onError          = Color.White,
)

// ── Shapes ──────────────────────────────────────────────────────────────
private val OryxenShapes = Shapes(
    small  = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large  = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun OryxenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) OryxenDarkColors else OryxenColors

    MaterialTheme(
        colorScheme = colors,
        typography = OryxenTypography,
        shapes = OryxenShapes,
        content = content,
    )
}
