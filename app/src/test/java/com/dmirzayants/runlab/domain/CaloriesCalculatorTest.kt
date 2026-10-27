package com.dmirzayants.runlab.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CaloriesCalculatorTest {

    @Test
    fun `met matches compendium table points`() {
        assertEquals(9.8, CaloriesCalculator.metForSpeed(9.7), 1e-9)
        assertEquals(8.3, CaloriesCalculator.metForSpeed(8.0), 1e-9)
    }

    @Test
    fun `met is interpolated between points`() {
        // halfway between 9.7 km/h (9.8) and 10.8 km/h (10.5)
        assertEquals(10.15, CaloriesCalculator.metForSpeed(10.25), 1e-9)
    }

    @Test
    fun `met is clamped outside the table`() {
        assertEquals(1.3, CaloriesCalculator.metForSpeed(-1.0), 1e-9)
        assertEquals(23.0, CaloriesCalculator.metForSpeed(40.0), 1e-9)
    }

    @Test
    fun `one hour at 9_7 kmh for 70 kg`() {
        // 9.8 MET * 70 kg * 1 h
        assertEquals(686, CaloriesCalculator.caloriesBurned(9_700, 3_600_000L, 70f))
    }

    @Test
    fun `faster pace burns more for the same distance per hour`() {
        val slow = CaloriesCalculator.caloriesBurned(5_000, 3_600_000L, 70f)
        val fast = CaloriesCalculator.caloriesBurned(12_000, 3_600_000L, 70f)
        assert(fast > slow)
    }

    @Test
    fun `zero time or weight gives zero`() {
        assertEquals(0, CaloriesCalculator.caloriesBurned(1_000, 0L, 70f))
        assertEquals(0, CaloriesCalculator.caloriesBurned(1_000, 60_000L, 0f))
    }

    @Test
    fun `average speed is rounded to one decimal`() {
        assertEquals(10.0f, CaloriesCalculator.avgSpeedKmh(5_000, 30 * 60_000L))
        assertEquals(0f, CaloriesCalculator.avgSpeedKmh(5_000, 0L))
        assertEquals(8.6f, CaloriesCalculator.avgSpeedKmh(4_321, 30 * 60_000L))
    }
}
