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

@Serializable
data class DiagnosisResponse(
    val id: String,
    val plantId: String,
    val imageUrl: String,
    val detectedPest: String,
    val confidenceScore: Double,
    val recommendation: String,
    val status: String,
    val createdAt: String,
    val analyzedAt: String? = null,
)

@Serializable
data class PlanResponse(
    val id: String,
    val name: String,
    val price: Double,
    val currency: String,
    val billingCycleMonths: Int,
    val features: String,
    val isActive: Boolean,
)

@Serializable
data class CheckoutResponse(
    val sessionId: String,
    val checkoutUrl: String,
)

@Serializable
data class CheckoutRequestBody(val planId: String)

@Serializable
data class NotificationResponse(
    val id: String,
    val userId: String,
    val plantId: String? = null,
    val type: Int,
    val channel: Int,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val createdAt: String,
    val sentAt: String? = null,
)

@Serializable
data class UnreadCountResponse(val count: Int)
