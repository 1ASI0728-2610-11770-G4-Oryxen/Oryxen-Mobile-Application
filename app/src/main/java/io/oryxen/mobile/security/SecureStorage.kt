package io.oryxen.mobile.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class SecureStorage(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS, null)
        private set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH, null)
        private set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

    var userId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        private set(value) = prefs.edit().putString(KEY_USER_ID, value).apply()

    var fullName: String?
        get() = prefs.getString(KEY_FULL_NAME, null)
        private set(value) = prefs.edit().putString(KEY_FULL_NAME, value).apply()

    var roles: List<String>
        get() = prefs.getString(KEY_ROLES, null)?.split("|") ?: emptyList()
        private set(value) = prefs.edit().putString(KEY_ROLES, value.joinToString("|")).apply()

    var currentPlantId: String?
        get() = prefs.getString(KEY_PLANT_ID, null)
        set(value) = prefs.edit().putString(KEY_PLANT_ID, value).apply()

    var deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: run {
            val generated = java.util.UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_ID, generated).apply()
            generated
        }
        private set(value) = prefs.edit().putString(KEY_DEVICE_ID, value).apply()

    val isAuthenticated: Boolean get() = accessToken != null && refreshToken != null

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: String,
        fullName: String,
        roles: List<String>,
    ) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.userId = userId
        this.fullName = fullName
        this.roles = roles
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "oryxen_secure_prefs"
        private const val KEY_ACCESS = "access_token"
        private const val KEY_REFRESH = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_ROLES = "roles"
        private const val KEY_PLANT_ID = "current_plant_id"
        private const val KEY_DEVICE_ID = "device_id"

        fun decodeUserId(jwt: String): String {
            val parts = jwt.split(".")
            if (parts.size != 3) return ""
            val payload = parts[1]
            val decoded = try {
                val json = Json { ignoreUnknownKeys = true }
                val element = json.parseToJsonElement(String(android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)))
                element.jsonObject["sub"]?.jsonPrimitive?.content ?: ""
            } catch (_: Exception) {
                ""
            }
            return decoded
        }
    }
}
