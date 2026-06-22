package io.oryxen.mobile.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/** Retrofit contract mirroring the Oryxen .NET backend (`/api/v1`). */
interface OryxenApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @GET("telemetry/{plantId}")
    suspend fun telemetry(@Path("plantId") plantId: String): List<TelemetryReading>

    @POST("telemetry")
    suspend fun ingest(@Body body: TelemetryIngestRequest): TelemetryReading

    @Multipart
    @POST("ai/diagnoses")
    suspend fun createDiagnosis(
        @Part("plantId") plantId: RequestBody,
        @Part image: MultipartBody.Part,
    ): DiagnosisResponse

    @GET("ai/diagnoses/{id}")
    suspend fun getDiagnosis(@Path("id") id: String): DiagnosisResponse

    @GET("ai/plants/{plantId}/diagnoses")
    suspend fun diagnosesByPlant(@Path("plantId") plantId: String): List<DiagnosisResponse>

    @GET("plans")
    suspend fun getPlans(): List<PlanResponse>

    @POST("subscriptions/checkout")
    @Multipart
    suspend fun createCheckout(@Body body: CheckoutRequestBody): CheckoutResponse

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationResponse>

    @GET("notifications/unread/count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String)
}
