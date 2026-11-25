package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Self-consistency of the four preset fingerprint profiles
 * (v0.144.0): the whole point of a preset is that its parts agree
 * with each other — the UA names the profile's device, the first
 * language is the profile language, every zone belongs to its
 * region and carries plausible city coordinates. These tests pin
 * that agreement so a future profile edit cannot quietly break it.
 *
 * v0.146.0: devices moved into a per-region model library, and
 * resolution is BOUNDED — a device id or language from another
 * region can never resolve into this one. The tests pin that too.
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
            // The region's default language is one it offers, and
            // it heads the languages list resolution builds.
            assertTrue(profile.language in profile.languageOptions)
            val resolved = FingerprintProfiles.resolve(profile.regionId, null)
            assertEquals(profile.language, resolved.language)
            assertEquals(
                profile.language, resolved.languages.first()
            )
            assertTrue(profile.batteryLevel in 0.0..1.0)
            assertTrue(profile.timezones.isNotEmpty())
            assertTrue(profile.devices.isNotEmpty())
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
            // Every device in the library agrees with itself:
            // the UA names the same build model UA-CH reports,
            // and the screen geometry is a real phone geometry.
            assertEquals(
                profile.devices.size,
                profile.devices.map { it.id }.toSet().size
            )
            for (device in profile.devices) {
                assertTrue(
                    "${profile.regionId}/${device.id} UA lacks its model",
                    device.userAgent.contains(device.uaModel)
                )
                assertTrue(device.userAgent.contains("Android 14"))
                assertTrue(device.screenWidth in 300..500)
                assertTrue(device.screenHeight in 600..1000)
                assertTrue(device.devicePixelRatio in 1.5..4.0)
                assertTrue(device.hardwareConcurrency in 4..16)
                assertTrue(device.deviceMemory in 2..8)
            }
        }
    }

    @Test
    fun languageListsCarryRealisticFallbacks() {
        // v0.145.0: a lone navigator.languages entry is a tell —
        // real devices list fallbacks behind the primary language.
        for (profile in FingerprintProfiles.all) {
            val resolved = FingerprintProfiles.resolve(profile.regionId, null)
            assertTrue(
                "${profile.regionId} languages too thin",
                resolved.languages.size >= 2
            )
        }
        assertEquals(
            listOf("ja-JP", "ja", "en-US", "en"),
            FingerprintProfiles.languagesFor("ja-JP")
        )
        assertEquals(
            listOf("zh-TW", "zh", "en-US", "en"),
            FingerprintProfiles.languagesFor("zh-TW")
        )
        assertEquals(
            listOf("en-SG", "en-US", "en"),
            FingerprintProfiles.languagesFor("en-SG")
        )
        assertEquals(
            listOf("en-US", "en"),
            FingerprintProfiles.languagesFor("en-US")
        )
    }

    @Test
    fun voicesAndPanelAndNetworkStoriesAgreeWithTheProfile() {
        // v0.145.0 additions: voices, panel, network (region
        // level) and the WebGL caps (device level since v0.146.0).
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
            assertTrue(profile.colorGamut in setOf("srgb", "p3"))
            assertTrue(profile.connectionRtt in 1..300)
            assertTrue(profile.connectionDownlink in 1.0..50.0)
            for (device in profile.devices) {
                assertTrue(device.webGl.maxTextureSize in setOf(8192, 16384))
                assertTrue(device.webGl.maxViewportDim >= 8192)
                assertTrue(device.webGl.aliasedPointSizeMax > 1f)
                assertTrue(device.webGl.aliasedLineWidthMax >= 1f)
                assertTrue(device.webGl.maxVertexAttribs in 16..32)
                assertTrue(device.webGl.maxTextureImageUnits in 16..32)
                // The renderer string and the line-width cap must
                // tell one story: Adreno draws wide lines, Mali
                // only ever reports width 1.
                if (device.webGlRenderer.contains("Adreno")) {
                    assertTrue(device.webGl.aliasedLineWidthMax > 1f)
                }
                if (device.webGlRenderer.contains("Mali")) {
                    assertEquals(1f, device.webGl.aliasedLineWidthMax)
                }
            }
        }
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
    fun resolutionIsBoundedToTheRegionsOwnSets() {
        // v0.146.0's core rule: no combination the resolver emits
        // may contradict itself. A US-only device id and a
        // Taiwan-only language asked of Japan both fall back to
        // Japan's own defaults — never to a cross-region mix.
        val jp = FingerprintProfiles.resolve(
            "JP", null, deviceId = "SM-S921U", language = "zh-TW"
        )
        assertEquals(
            FingerprintProfiles.JAPAN.devices.first().id, jp.device.id
        )
        assertEquals("ja-JP", jp.language)
        // …while a legal in-region pick goes through untouched.
        val us = FingerprintProfiles.resolve(
            "US", "America/Chicago",
            deviceId = "Pixel 8 Pro", language = "es-US"
        )
        assertEquals("Pixel 8 Pro", us.device.id)
        assertEquals("es-US", us.language)
        assertEquals(
            listOf("es-US", "es", "en-US", "en"), us.languages
        )
        // The noise seed follows the combination: stable for the
        // same one, different across devices.
        val usAgain = FingerprintProfiles.resolve(
            "US", "America/Chicago",
            deviceId = "Pixel 8 Pro", language = "es-US"
        )
        assertEquals(us.seed, usAgain.seed)
        assertNotEquals(
            us.seed,
            FingerprintProfiles.resolve("US", null).seed
        )
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
        assertEquals(
            FingerprintProfiles.all.first().devices.first().id,
            unknown.device.id
        )
        val us = FingerprintProfiles.resolve("US", "America/Chicago")
        assertEquals("US", us.profile.regionId)
        assertEquals("芝加哥", us.timezone.city)
        val nulls = FingerprintProfiles.resolve(null, null)
        assertEquals("US", nulls.profile.regionId)
    }
}
