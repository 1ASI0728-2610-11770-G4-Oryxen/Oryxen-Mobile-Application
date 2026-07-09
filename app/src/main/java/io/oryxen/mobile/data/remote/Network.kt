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
    val session = Session(secureStorage)

    class Session(private val store: SecureStorage) {
        val fullName: String? get() = store.fullName
        val email: String? get() = store.email
        val roles: List<String> get() = store.roles
        val isAuthenticated: Boolean get() = store.isAuthenticated

        fun saveAccessToken(token: String) {
            val userId = SecureStorage.decodeUserId(token)
            store.saveTokens(
                accessToken = token,
                refreshToken = store.refreshToken ?: "",
                userId = userId,
                fullName = store.fullName ?: "",
                email = store.email ?: "",
                roles = store.roles,
            )
        }

        fun clear() = store.clear()
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
