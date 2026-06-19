package io.oryxen.mobile.data

import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.data.remote.AuthResponse
import io.oryxen.mobile.data.remote.LoginRequest
import io.oryxen.mobile.data.remote.RegisterRequest
import io.oryxen.mobile.data.remote.SessionManager
import io.oryxen.mobile.data.remote.TelemetryIngestRequest
import io.oryxen.mobile.data.remote.TelemetryReading
import kotlin.random.Random

object AuthRepository {
    suspend fun login(email: String, password: String): AuthResponse {
        val auth = ApiProvider.api.login(LoginRequest(email.trim(), password))
        SessionManager.save(auth)
        return auth
    }

    suspend fun register(fullName: String, email: String, password: String): AuthResponse {
        val auth = ApiProvider.api.register(RegisterRequest(email.trim(), password, fullName.trim()))
        SessionManager.save(auth)
        return auth
    }
}

object TelemetryRepository {
    suspend fun forPlant(plantId: String): List<TelemetryReading> =
        ApiProvider.api.telemetry(plantId)

    /** Pushes a randomized Sensor Lite reading so the dashboard has live data to show. */
    suspend fun sendSample(plantId: String): TelemetryReading =
        ApiProvider.api.ingest(
            TelemetryIngestRequest(
                deviceId = "SL-MOBILE-DEMO",
                plantId = plantId,
                humidity = Random.nextInt(35, 71).toDouble(),
                temperature = Random.nextInt(16, 31).toDouble(),
                lightLevel = Random.nextInt(200, 1201).toDouble(),
                soilMoisture = Random.nextInt(15, 81).toDouble(),
            ),
        )
}
