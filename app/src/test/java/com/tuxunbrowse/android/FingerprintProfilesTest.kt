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
    fun languageListsCarryRealisticFallbacks() {
        // v0.145.0: a lone navigator.languages entry is a tell —
        // real devices list fallbacks behind the primary language.
        for (profile in FingerprintProfiles.all) {
            assertTrue(
                "${profile.regionId} languages too thin",
                profile.languages.size >= 2
            )
        }
        assertEquals(
            listOf("ja-JP", "ja", "en-US", "en"),
            FingerprintProfiles.JAPAN.languages
        )
        assertEquals(
            listOf("zh-TW", "zh", "en-US", "en"),
            FingerprintProfiles.TAIWAN.languages
        )
    }

    @Test
    fun voicesAndPanelAndNetworkStoriesAgreeWithTheProfile() {
        // v0.145.0 additions: voices, WebGL caps, panel, network.
        for (profile in FingerprintProfiles.all) {
            assertTrue(profile.voices.isNotEmpty())
            assertEquals(
                "${profile.regionId} needs exactly one default voice",
                1, profile.voices.count { it.isDefault }
            )
            val defaultVoice = profile.voices.first { it.isDefault }
            assertEquals(
                profile.language.substringBefore('-'),
                defaultVoice.lang.substringBefore('-')
            )
            assertTrue(profile.webGl.maxTextureSize in setOf(8192, 16384))
            assertTrue(profile.webGl.maxViewportDim >= 8192)
            assertTrue(profile.webGl.aliasedPointSizeMax > 1f)
            assertTrue(profile.webGl.aliasedLineWidthMax >= 1f)
            assertTrue(profile.webGl.maxVertexAttribs in 16..32)
            assertTrue(profile.webGl.maxTextureImageUnits in 16..32)
            assertTrue(profile.colorGamut in setOf("srgb", "p3"))
            assertTrue(profile.connectionRtt in 1..300)
            assertTrue(profile.connectionDownlink in 1.0..50.0)
        }
        // Adreno GPUs draw wide lines; Mali GPUs only width 1 —
        // the renderer string and the caps must tell one story.
        assertTrue(FingerprintProfiles.UNITED_STATES.webGl.aliasedLineWidthMax > 1f)
        assertEquals(1f, FingerprintProfiles.JAPAN.webGl.aliasedLineWidthMax)
        assertEquals(1f, FingerprintProfiles.TAIWAN.webGl.aliasedLineWidthMax)
    }

    @Test
    fun daylightNamesExistOnlyWhereTheZoneObservesDst() {
        for (zone in FingerprintProfiles.UNITED_STATES.timezones) {
            assertTrue(zone.zoneDaylightName!!.contains("Daylight"))
        }
        for (profile in listOf(
            FingerprintProfiles.JAPAN,
            FingerprintProfiles.SINGAPORE,
            FingerprintProfiles.TAIWAN
        )) {
            for (zone in profile.timezones) {
                assertEquals(null, zone.zoneDaylightName)
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
