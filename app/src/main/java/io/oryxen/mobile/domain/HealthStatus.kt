package io.oryxen.mobile.domain

import androidx.compose.ui.graphics.Color

/** Qualitative plant status derived from the backend Health Score (0-100). */
enum class HealthStatus(val label: String, val color: Color) {
    CRITICAL("Critical", Color(0xFFD32F2F)),
    WARNING("Warning", Color(0xFFF59E0B)),
    GOOD("Good", Color(0xFF3A9D5D)),
    OPTIMAL("Optimal", Color(0xFF1AA37A)),
}

fun healthStatusOf(score: Int): HealthStatus = when {
    score < 30 -> HealthStatus.CRITICAL
    score < 60 -> HealthStatus.WARNING
    score < 80 -> HealthStatus.GOOD
    else -> HealthStatus.OPTIMAL
}
