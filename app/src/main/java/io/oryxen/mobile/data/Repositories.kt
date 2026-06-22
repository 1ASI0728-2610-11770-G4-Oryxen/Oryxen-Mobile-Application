package io.oryxen.mobile.data

import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.data.remote.AuthResponse
import io.oryxen.mobile.data.remote.CheckoutResponse
import io.oryxen.mobile.data.remote.DashboardResponse
import io.oryxen.mobile.data.remote.DiagnosisResponse
import io.oryxen.mobile.data.remote.NotificationResponse
import io.oryxen.mobile.data.remote.UnreadCountResponse
import io.oryxen.mobile.data.remote.LoginRequest
import io.oryxen.mobile.data.remote.PlanResponse
import io.oryxen.mobile.data.remote.PlantTrendResponse
import io.oryxen.mobile.data.remote.RegisterRequest
import io.oryxen.mobile.data.remote.TelemetryIngestRequest
import io.oryxen.mobile.data.remote.TelemetryReading
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import io.oryxen.mobile.data.remote.CommunityCommentResponse
import io.oryxen.mobile.data.remote.CommunityLikeResponse
import io.oryxen.mobile.data.remote.CommunityPostResponse
import io.oryxen.mobile.data.remote.CreateCommentRequest
import io.oryxen.mobile.security.SecureStorage

object AuthRepository {
    suspend fun login(email: String, password: String): AuthResponse {
        val provider = ApiProvider.instance
        val auth = provider.api.login(LoginRequest(email.trim(), password))
        val userId = SecureStorage.decodeUserId(auth.accessToken)
        provider.secureStorage.saveTokens(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId = userId,
            fullName = auth.fullName,
            roles = auth.roles,
        )
        return auth
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResponse {
        val provider = ApiProvider.instance
        val auth = provider.api.register(RegisterRequest(email.trim(), password, fullName.trim()))
        val userId = SecureStorage.decodeUserId(auth.accessToken)
        provider.secureStorage.saveTokens(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId = userId,
            fullName = auth.fullName,
            roles = auth.roles,
        )
        return auth
    }
}

object TelemetryRepository {
    private val provider get() = ApiProvider.instance

    suspend fun forPlant(plantId: String): List<TelemetryReading> =
        provider.api.telemetry(plantId)

    suspend fun sendSample(plantId: String): TelemetryReading =
        provider.api.ingest(
            TelemetryIngestRequest(
                deviceId = provider.secureStorage.deviceId,
                plantId = plantId,
                humidity = kotlin.random.Random.nextInt(35, 71).toDouble(),
                temperature = kotlin.random.Random.nextInt(16, 31).toDouble(),
                lightLevel = kotlin.random.Random.nextInt(200, 1201).toDouble(),
                soilMoisture = kotlin.random.Random.nextInt(15, 81).toDouble(),
            ),
        )
}

object DiagnosisRepository {
    private val provider get() = ApiProvider.instance

    suspend fun forPlant(plantId: String): List<DiagnosisResponse> =
        provider.api.diagnosesByPlant(plantId)
}

object BillingRepository {
    private val provider get() = ApiProvider.instance

    suspend fun getPlans(): List<PlanResponse> =
        provider.api.getPlans()

    suspend fun createCheckout(planId: String): CheckoutResponse =
        provider.api.createCheckout(CheckoutRequestBody(planId))

    suspend fun getCurrentSubscription(): SubscriptionResponse =
        provider.api.getCurrentSubscription()
}

object NotificationRepository {
    private val provider get() = ApiProvider.instance

    suspend fun getAll(): List<NotificationResponse> =
        provider.api.getNotifications()

    suspend fun getUnreadCount(): UnreadCountResponse =
        provider.api.getUnreadCount()

    suspend fun markRead(id: String) =
        provider.api.markNotificationRead(id)
}

object AnalyticsRepository {
    private val provider get() = ApiProvider.instance

    suspend fun getDashboard(): DashboardResponse =
        provider.api.getDashboard()

    suspend fun getPlantTrends(plantId: String): PlantTrendResponse =
        provider.api.getPlantTrends(plantId)
}

object CommunityRepository {
    private val provider get() = ApiProvider.instance

    suspend fun getFeed(): List<CommunityPostResponse> =
        provider.api.getCommunityFeed()

    suspend fun createPost(title: String, content: String, imageBytes: ByteArray?, fileName: String?): CommunityPostResponse {
        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())
        val imagePart = imageBytes?.let {
            val body = it.toRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", fileName, body)
        }
        return provider.api.createCommunityPost(titlePart, contentPart, imagePart)
    }

    suspend fun addComment(postId: String, content: String): CommunityCommentResponse =
        provider.api.addCommunityComment(postId, CreateCommentRequest(content))

    suspend fun toggleLike(postId: String): CommunityLikeResponse =
        provider.api.toggleCommunityLike(postId)
}
