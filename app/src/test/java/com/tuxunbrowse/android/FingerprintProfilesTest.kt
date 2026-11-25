package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Self-consistency of the four preset fingerprint profiles
 * (v0.144.0): the whole point of a preset is that its parts agree
 * with each other — the UA names the profile's device, the first
 * language is the profile language, every zone belongs to its
 * region and carries plausible city coordinates. These tests pin
 * that agreement so a future profile edit cannot quietly break it.
 */
class FingerprintProfilesTest {

    @Test
    fun exactlyTheFourRegionsInPickerOrder() {
        assertEquals(
            listOf("US", "JP", "SG", "TW"),
            FingerprintProfiles.all.map { it.regionId }
        )
    }

    @Test
    fun unitedStatesCarriesTheFourBigZones() {
        val us = FingerprintProfiles.UNITED_STATES
        assertTrue(us.timezones.size >= 4)
        assertEquals(
            listOf(
                "America/New_York", "America/Chicago",
                "America/Denver", "America/Los_Angeles"
            ),
            us.timezones.map { it.id }
        )
    }

    @Test
    fun everyProfileAgreesWithItself() {
        for (profile in FingerprintProfiles.all) {
            // The UA names the same build model UA-CH reports.
            assertTrue(
                "${profile.regionId} UA lacks its model",
                profile.userAgent.contains(profile.uaModel)
            )
            assertTrue(profile.userAgent.contains("Android 14"))
            // navigator.language is the head of navigator.languages.
            assertEquals(profile.language, profile.languages.first())
            // Screen geometry is a real phone geometry.
            assertTrue(profile.screenWidth in 300..500)
            assertTrue(profile.screenHeight in 600..1000)
            assertTrue(profile.devicePixelRatio in 1.5..4.0)
            assertTrue(profile.hardwareConcurrency in 4..16)
            assertTrue(profile.deviceMemory in 2..8)
            assertTrue(profile.batteryLevel in 0.0..1.0)
            assertTrue(profile.timezones.isNotEmpty())
            // Zone ids are unique inside the region, IANA-shaped,
            // and the coordinates sit on land in range.
            assertEquals(
                profile.timezones.size,
                profile.timezones.map { it.id }.toSet().size
            )
            for (zone in profile.timezones) {
                assertTrue(zone.id.contains('/'))
                assertTrue(zone.latitude in -90.0..90.0)
                assertTrue(zone.longitude in -180.0..180.0)
                assertTrue(zone.city.isNotBlank())
            }
        }
    }

    @Test
    fun resolveFallsBackToFirstRegionAndZone() {
        val unknown = FingerprintProfiles.resolve("XX", "Nowhere/Place")
        assertEquals(
            FingerprintProfiles.all.first().regionId,
            unknown.profile.regionId
        )
        assertEquals(
            FingerprintProfiles.all.first().timezones.first().id,
            unknown.timezone.id
        )
        val us = FingerprintProfiles.resolve("US", "America/Chicago")
        assertEquals("US", us.profile.regionId)
        assertEquals("芝加哥", us.timezone.city)
        val nulls = FingerprintProfiles.resolve(null, null)
        assertEquals("US", nulls.profile.regionId)
    }
}
