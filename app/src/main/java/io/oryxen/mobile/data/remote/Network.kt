package io.oryxen.mobile.data.remote

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** In-memory session holder. The HTTP interceptor reads the access token from here. */
object SessionManager {
    @Volatile var accessToken: String? = null
    @Volatile var refreshToken: String? = null
    @Volatile var fullName: String? = null
    @Volatile var roles: List<String> = emptyList()

    val isAuthenticated: Boolean get() = accessToken != null

    fun save(auth: AuthResponse) {
        accessToken = auth.accessToken
        refreshToken = auth.refreshToken
        fullName = auth.fullName
        roles = auth.roles
    }

    fun clear() {
        accessToken = null
        refreshToken = null
        fullName = null
        roles = emptyList()
    }
}

object ApiProvider {
    // 10.0.2.2 maps to the host machine's localhost from inside the Android emulator.
    private const val BASE_URL = "http://10.0.2.2:5170/api/v1/"

    private val json = Json { ignoreUnknownKeys = true }

    private val authInterceptor = Interceptor { chain ->
        val token = SessionManager.accessToken
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    val api: OryxenApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OryxenApi::class.java)
}
