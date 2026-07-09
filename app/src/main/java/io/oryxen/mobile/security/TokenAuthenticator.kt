package io.oryxen.mobile.security

import io.oryxen.mobile.data.remote.OryxenApi
import io.oryxen.mobile.data.remote.RefreshRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class TokenAuthenticator(
    private val secureStorage: SecureStorage,
) : Authenticator {

    private val json = Json { ignoreUnknownKeys = true }

    private val refreshClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val refreshApi: OryxenApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(refreshClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OryxenApi::class.java)

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.url.encodedPath.endsWith("/auth/refresh")) {
            return null
        }

        synchronized(this) {
            val storedAccessToken = secureStorage.accessToken
            val storedRefreshToken = secureStorage.refreshToken

            val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (failedToken != null && failedToken != storedAccessToken && storedAccessToken != null) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $storedAccessToken")
                    .build()
            }

            if (storedRefreshToken == null) {
                secureStorage.clear()
                SessionEventBus.emit(SessionEvent.Expired)
                return null
            }

            return try {
                runBlocking {
                    val refreshResponse = refreshApi.refresh(RefreshRequest(storedRefreshToken))

                    val userId = SecureStorage.decodeUserId(refreshResponse.accessToken)
                    secureStorage.saveTokens(
                        accessToken = refreshResponse.accessToken,
                        refreshToken = refreshResponse.refreshToken,
                        userId = userId,
                        fullName = refreshResponse.fullName,
                        email = refreshResponse.email,
                        roles = refreshResponse.roles,
                    )

                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${refreshResponse.accessToken}")
                        .build()
                }
            } catch (_: Exception) {
                secureStorage.clear()
                SessionEventBus.emit(SessionEvent.Expired)
                null
            }
        }
    }

    companion object {
        const val BASE_URL = "http://10.0.2.2:5170/api/v1/"
    }
}
