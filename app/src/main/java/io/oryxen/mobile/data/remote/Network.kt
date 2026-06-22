package io.oryxen.mobile.data.remote

import android.content.Context
import io.oryxen.mobile.security.SecureStorage
import io.oryxen.mobile.security.TokenAuthenticator
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ApiProvider(context: Context) {
    val secureStorage = SecureStorage(context)

    // Backwards-compatible session facade for existing screen code
    val session = object {
        val fullName: String? get() = secureStorage.fullName
        val roles: List<String> get() = secureStorage.roles
        val isAuthenticated: Boolean get() = secureStorage.isAuthenticated

        fun saveAccessToken(token: String) {
            val userId = SecureStorage.decodeUserId(token)
            secureStorage.saveTokens(
                accessToken = token,
                refreshToken = secureStorage.refreshToken ?: "",
                userId = userId,
                fullName = secureStorage.fullName ?: "",
                roles = secureStorage.roles,
            )
        }

        fun clear() = secureStorage.clear()
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val authHeaderInterceptor = Interceptor { chain ->
        val token = secureStorage.accessToken
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authHeaderInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .authenticator(TokenAuthenticator(secureStorage))
        .build()

    val api: OryxenApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(OryxenApi::class.java)

    companion object {
        const val BASE_URL = "http://10.0.2.2:5170/api/v1/"

        lateinit var instance: ApiProvider
            private set

        fun init(context: Context) {
            instance = ApiProvider(context.applicationContext)
        }
    }
}
