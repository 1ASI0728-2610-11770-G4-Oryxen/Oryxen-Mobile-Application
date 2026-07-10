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
import io.oryxen.mobile.data.remote.AssignSensorRequest
import io.oryxen.mobile.data.remote.ChatRequest
import io.oryxen.mobile.data.remote.ChatResponse
import io.oryxen.mobile.data.remote.CheckoutRequestBody
import io.oryxen.mobile.data.remote.CreateCommentRequest
import io.oryxen.mobile.data.remote.CreatePlantRequest
import io.oryxen.mobile.data.remote.PlantResponse
import io.oryxen.mobile.data.remote.SubscriptionResponse
import io.oryxen.mobile.data.remote.UpdatePlantRequest
import io.oryxen.mobile.data.remote.WateringResponse
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
            email = auth.email,
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
            email = auth.email,
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

    suspend fun createDiagnosis(plantId: String, imageBytes: ByteArray, fileName: String = "diagnosis.jpg"): DiagnosisResponse {
        val plantIdPart = plantId.toRequestBody("text/plain".toMediaTypeOrNull())
        val body = imageBytes.toRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", fileName, body)
        return provider.api.createDiagnosis(plantIdPart, imagePart)
    }
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
    private val api get() = ApiProvider.instance.api

    suspend fun getDashboard(): DashboardResponse {
        return api.getDashboard()
    }

    suspend fun getPlantTrends(plantId: String): PlantTrendResponse {
        return api.getPlantTrends(plantId)
    }
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

object PlantRepository {
    private val provider get() = ApiProvider.instance

    suspend fun getAll(): List<PlantResponse> =
        provider.api.getPlants(provider.secureStorage.userId ?: "")

    suspend fun getById(id: String): PlantResponse =
        provider.api.getPlant(id)

    suspend fun create(name: String, type: String, location: String?): PlantResponse =
        provider.api.createPlant(CreatePlantRequest(name.trim(), type.trim(), location?.trim()))

    suspend fun update(id: String, name: String, type: String, location: String?): PlantResponse =
        provider.api.updatePlant(id, UpdatePlantRequest(name.trim(), type.trim(), location?.trim()))

    suspend fun delete(id: String) =
        provider.api.deletePlant(id)

    suspend fun assignSensor(id: String, deviceId: String): PlantResponse =
        provider.api.assignSensor(id, AssignSensorRequest(deviceId))

    suspend fun water(id: String): WateringResponse =
        provider.api.waterPlant(id)
}

object ChatRepository {
    private val provider get() = ApiProvider.instance

    /** Sends a user message to the server-side Gemini assistant (POST /ai/chat). */
    suspend fun send(message: String, context: String? = null): ChatResponse =
        provider.api.chat(ChatRequest(message.trim(), context))
}
