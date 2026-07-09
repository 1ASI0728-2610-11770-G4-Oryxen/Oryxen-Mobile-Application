package io.oryxen.mobile.domain

import org.junit.Assert.assertEquals
import org.junit.Test

/** Health-score banding must mirror the backend PlantHealthCalculator thresholds. */
class HealthStatusTest {

    @Test
    fun `scores below 30 are critical`() {
        assertEquals(HealthStatus.CRITICAL, healthStatusOf(0))
        assertEquals(HealthStatus.CRITICAL, healthStatusOf(29))
    }

    @Test
    fun `scores 30-59 are warning`() {
        assertEquals(HealthStatus.WARNING, healthStatusOf(30))
        assertEquals(HealthStatus.WARNING, healthStatusOf(59))
    }

    @Test
    fun `scores 60-79 are good`() {
        assertEquals(HealthStatus.GOOD, healthStatusOf(60))
        assertEquals(HealthStatus.GOOD, healthStatusOf(79))
    }

    @Test
    fun `scores 80 and above are optimal`() {
        assertEquals(HealthStatus.OPTIMAL, healthStatusOf(80))
        assertEquals(HealthStatus.OPTIMAL, healthStatusOf(100))
    }
}
