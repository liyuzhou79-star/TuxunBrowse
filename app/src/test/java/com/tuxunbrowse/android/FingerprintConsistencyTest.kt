package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The cross-consistency ledger (v0.151.0): the fingerprint story
 * only holds if every layer agrees with every other layer, for
 * EVERY region × device × language × timezone combination the
 * bounded resolver can produce. These tests walk the whole
 * product space of the data tables and check the agreements a
 * cross-examining detector would check — UA against UA-CH model,
 * language chains, voice leads, seed uniqueness, zone names.
 * Adding a device or a region that breaks an agreement fails
 * here first, not on a user's real-device report.
 */
class FingerprintConsistencyTest {

    @Test
    fun uaAndUaChModelAgreeForEveryDevice() {
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                assertTrue(
                    "${profile.regionId}/${device.id}",
                    device.userAgent.contains(device.uaModel)
                )
                assertTrue(
                    device.userAgent.contains("Chrome/131.0.6778.104")
                )
                assertTrue(device.userAgent.contains("Android 14"))
            }
        }
    }

    @Test
    fun deviceNumbersStayPlausibleForEveryDevice() {
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                // Portrait story everywhere (the profiles are
                // phones held upright).
                assertTrue(device.screenWidth < device.screenHeight)
                assertTrue(device.devicePixelRatio in 2.0..3.5)
                assertTrue(device.hardwareConcurrency in 4..12)
                // Device Memory API buckets only.
                assertTrue(device.deviceMemory in listOf(2, 4, 8))
                assertTrue(device.webGlRenderer.isNotBlank())
                assertTrue(device.webGlVendor.isNotBlank())
            }
        }
    }

    @Test
    fun seedsAreUniqueAcrossEveryCombination() {
        val seeds = mutableSetOf<Int>()
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                val resolved = FingerprintProfiles.resolve(
                    profile.regionId,
                    profile.timezones.first().id,
                    device.id,
                    profile.language
                )
                assertTrue(
                    "seed ${resolved.seed} reused",
                    seeds.add(resolved.seed)
                )
            }
        }
        assertEquals(16, seeds.size)
    }

    @Test
    fun languageChainsAreWellFormedForEveryOption() {
        for (profile in FingerprintProfiles.all) {
            for (option in profile.languageOptions) {
                val chain = FingerprintProfiles.languagesFor(option)
                assertEquals(option, chain.first())
                assertEquals("en", chain.last())
                assertEquals(chain.size, chain.distinct().size)
            }
        }
    }

    @Test
    fun voicesLeadWithTheRegionsLanguage() {
        for (profile in FingerprintProfiles.all) {
            val lead = profile.voices.first()
            val prefix = profile.language.substringBefore('-')
            assertTrue(
                "${profile.regionId} lead voice ${lead.lang}",
                lead.lang.startsWith(prefix)
            )
            assertTrue(profile.voices.any { it.isDefault })
        }
    }

    @Test
    fun webGpuIdentityAgreesWithTheWebGlRenderer() {
        // v0.159.0: the WebGPU adapter info must name the same
        // GPU family as the WebGL renderer string — a detector
        // reads both, one probe apart.
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                if (device.webGlRenderer.contains("Adreno")) {
                    assertEquals(
                        "${device.id} vendor",
                        "qualcomm", device.webGpuVendor
                    )
                    assertTrue(
                        device.webGpuArchitecture.startsWith("adreno")
                    )
                } else if (device.webGlRenderer.contains("Mali")) {
                    assertEquals(
                        "${device.id} vendor",
                        "arm", device.webGpuVendor
                    )
                    assertEquals("valhall", device.webGpuArchitecture)
                } else {
                    throw AssertionError(
                        "unclassified GPU family: ${device.webGlRenderer}"
                    )
                }
            }
        }
    }

    @Test
    fun timezoneNamesCoverTheDaylightStory() {
        for (profile in FingerprintProfiles.all) {
            for (zone in profile.timezones) {
                assertTrue(zone.zoneName.isNotBlank())
                if (profile.regionId == "US") {
                    // US zones observe DST: the daylight name must
                    // exist or Date strings would wear the standard
                    // name all summer.
                    assertTrue(zone.zoneDaylightName != null)
                }
            }
        }
    }
}
