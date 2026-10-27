package com.dmirzayants.runlab.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileInputTest {

    @Test
    fun `accepts comma as decimal separator`() {
        assertEquals(72.5f, ProfileInput("Dan", "72,5").parsedWeight)
    }

    @Test
    fun `rejects empty, garbage and unrealistic weight`() {
        assertNull(ProfileInput("Dan", "").parsedWeight)
        assertNull(ProfileInput("Dan", "abc").parsedWeight)
        assertNull(ProfileInput("Dan", "5").parsedWeight)
    }

    @Test
    fun `blank name is invalid`() {
        assertFalse(ProfileInput("  ", "70").isValid)
        assertTrue(ProfileInput("Dan", "70").isValid)
    }

    @Test
    fun `whole weights are shown without decimals`() {
        assertEquals("80", formatWeight(80f))
        assertEquals("72.5", formatWeight(72.5f))
    }
}
