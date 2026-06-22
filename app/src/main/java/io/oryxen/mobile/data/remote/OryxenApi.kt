package io.oryxen.mobile.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/** Retrofit contract mirroring the Oryxen .NET backend (`/api/v1`). */
interface OryxenApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

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
    suspend fun createCheckout(@Body body: CheckoutRequestBody): CheckoutResponse

    @GET("subscriptions/current")
    suspend fun getCurrentSubscription(): SubscriptionResponse

    @GET("notifications")
    suspend fun getNotifications(): List<NotificationResponse>

    @GET("notifications/unread/count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @PATCH("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String)

    @GET("analytics/dashboard")
    suspend fun getDashboard(): DashboardResponse

    @GET("analytics/plants/{plantId}/trends")
    suspend fun getPlantTrends(@Path("plantId") plantId: String): PlantTrendResponse

    @GET("community/feed")
    suspend fun getCommunityFeed(): List<CommunityPostResponse>

    @Multipart
    @POST("community/posts")
    suspend fun createCommunityPost(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?,
    ): CommunityPostResponse

    @POST("community/posts/{id}/comments")
    suspend fun addCommunityComment(
        @Path("id") postId: String,
        @Body body: CreateCommentRequest,
    ): CommunityCommentResponse

    @POST("community/posts/{id}/likes")
    suspend fun toggleCommunityLike(@Path("id") postId: String): CommunityLikeResponse
}
