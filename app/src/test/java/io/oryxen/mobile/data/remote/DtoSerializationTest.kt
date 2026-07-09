package io.oryxen.mobile.data.remote

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Wire-contract tests: the DTOs must decode exactly what the .NET backend serializes
 * (camelCase, optional fields absent) and encode what its request contracts expect.
 */
class DtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `auth response decodes the backend payload`() {
        val payload = """
            {
              "accessToken": "jwt-access",
              "refreshToken": "opaque-refresh",
              "accessTokenExpiresAt": "2026-07-09T12:30:00Z",
              "email": "farmer@oryxen.io",
              "fullName": "Abraham Estrada",
              "roles": ["FARMER"]
            }
        """.trimIndent()

        val auth = json.decodeFromString<AuthResponse>(payload)

        assertEquals("jwt-access", auth.accessToken)
        assertEquals(listOf("FARMER"), auth.roles)
    }

    @Test
    fun `plant response tolerates extra backend fields and missing optionals`() {
        // The backend also sends bio/status/metrics/wateringLogs; unknown keys must be ignored.
        val payload = """
            {
              "id": "aaaaaaaa-0000-0000-0000-000000000001",
              "userId": "bbbbbbbb-0000-0000-0000-000000000002",
              "name": "Aloe Vera",
              "type": "Succulent",
              "bio": "desert plant",
              "imgUrl": "https://cdn/img.png",
              "status": "Healthy",
              "metrics": [],
              "wateringLogs": [],
              "createdAt": "2026-07-01T00:00:00Z"
            }
        """.trimIndent()

        val plant = json.decodeFromString<PlantResponse>(payload)

        assertEquals("Aloe Vera", plant.name)
        assertNull(plant.location)
        assertEquals("Healthy", plant.status)
    }

    @Test
    fun `chat request encodes message and optional context`() {
        val body = json.encodeToString(ChatRequest(message = "How often to water?", context = "Plant: Aloe"))

        assertEquals(
            """{"message":"How often to water?","context":"Plant: Aloe"}""",
            body,
        )
    }

    @Test
    fun `chat response decodes the backend reply`() {
        val payload = """{"reply":"Twice a week.","provider":"gemini","generatedAt":"2026-07-09T12:00:00Z"}"""

        val chat = json.decodeFromString<ChatResponse>(payload)

        assertEquals("Twice a week.", chat.reply)
        assertEquals("gemini", chat.provider)
    }

    @Test
    fun `assign sensor request matches the backend contract`() {
        val body = json.encodeToString(AssignSensorRequest(deviceId = "SL-123456"))

        assertEquals("""{"deviceId":"SL-123456"}""", body)
    }
}
