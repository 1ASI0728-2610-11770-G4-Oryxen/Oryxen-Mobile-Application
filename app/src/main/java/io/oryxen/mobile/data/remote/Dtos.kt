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
data class SubscriptionResponse(
    val id: String,
    val userId: String,
    val plan: String,
    val status: String,
    val startedAt: String,
    val expiresAt: String? = null,
    val nextBillingDate: String? = null,
    val canceledAt: String? = null,
)

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

@Serializable
data class DashboardResponse(
    val totalPlants: Int,
    val healthyPlants: Int,
    val warningPlants: Int,
    val criticalPlants: Int,
    val avgHumidity: Double,
    val avgTemperature: Double,
    val avgSoilMoisture: Double,
    val avgLightLevel: Double,
    val avgHealthScore: Double,
    val totalReadings: Int,
    val plantSummaries: List<PlantHealthSummaryDto>,
)

@Serializable
data class PlantHealthSummaryDto(
    val plantId: String,
    val plantName: String,
    val plantType: String,
    val status: String,
    val avgHealthScore: Double,
    val avgSoilMoisture: Double,
    val readingCount: Int,
    val lastReadingAt: String? = null,
)

@Serializable
data class TrendPointDto(
    val label: String,
    val avgHealthScore: Double,
    val avgSoilMoisture: Double,
    val avgTemperature: Double,
    val avgHumidity: Double,
    val readingCount: Int,
)

@Serializable
data class PlantTrendResponse(
    val plantId: String,
    val plantName: String,
    val daily: List<TrendPointDto>,
    val weekly: List<TrendPointDto>,
    val monthly: List<TrendPointDto>,
)

@Serializable
data class CommunityPostResponse(
    val id: String,
    val userId: String,
    val authorName: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val likesCount: Int,
    val likedByCurrentUser: Boolean,
    val createdAt: String,
    val comments: List<CommunityCommentResponse>,
)

@Serializable
data class CommunityCommentResponse(
    val id: String,
    val postId: String,
    val userId: String,
    val authorName: String,
    val content: String,
    val createdAt: String,
)

@Serializable
data class CommunityLikeResponse(
    val postId: String,
    val likesCount: Int,
    val likedByCurrentUser: Boolean,
)

@Serializable
data class CreateCommentRequest(val content: String)
