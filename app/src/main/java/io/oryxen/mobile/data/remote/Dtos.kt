package io.oryxen.mobile.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String, val fullName: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: String,
    val email: String,
    val fullName: String,
    val roles: List<String>,
)

@Serializable
data class TelemetryReading(
    val id: String,
    val deviceId: String,
    val plantId: String,
    val humidity: Double,
    val temperature: Double,
    val lightLevel: Double,
    val soilMoisture: Double,
    val healthScore: Int,
    val recordedAt: String,
)

@Serializable
data class TelemetryIngestRequest(
    val deviceId: String,
    val plantId: String,
    val humidity: Double,
    val temperature: Double,
    val lightLevel: Double,
    val soilMoisture: Double,
)
