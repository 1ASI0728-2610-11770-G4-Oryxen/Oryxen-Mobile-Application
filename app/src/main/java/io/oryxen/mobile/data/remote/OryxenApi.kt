package io.oryxen.mobile.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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
}
