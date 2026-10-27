package com.dmirzayants.runlab

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingUtilityTest {

    @Test
    fun `formats hours minutes seconds`() {
        assertEquals("01:02:03", TrackingUtility.getFormattedStopWatchTime(3_723_000L))
    }

    @Test
    fun `formats centiseconds when requested`() {
        assertEquals("00:00:05:43", TrackingUtility.getFormattedStopWatchTime(5_432L, includeMillis = true))
    }

    @Test
    fun `zero time`() {
        assertEquals("00:00:00", TrackingUtility.getFormattedStopWatchTime(0L))
    }
}
