package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun knownHardwareFactsHoldForEveryDevice() {
        // v0.162.0: spec-sheet facts a detector can check
        // against public data — Tensor G3 (Pixel 8 / 8 Pro) is
        // a nine-core part, and the 8 Pro's higher-resolution
        // panel gives it a different CSS screen from the base
        // Pixel 8. Pinning them here keeps a future library
        // edit from silently reintroducing the wrong numbers.
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                when (device.id) {
                    "Pixel 8", "Pixel 8 Pro" -> assertEquals(
                        "${device.id} cores",
                        9, device.hardwareConcurrency
                    )
                    "Pixel 7a" -> assertEquals(
                        "Pixel 7a cores",
                        8, device.hardwareConcurrency
                    )
                }
                if (device.id == "Pixel 8 Pro") {
                    assertEquals(448, device.screenWidth)
                    assertEquals(997, device.screenHeight)
                    assertEquals(3.0, device.devicePixelRatio, 0.0)
                }
                if (device.id == "Pixel 8") {
                    assertEquals(412, device.screenWidth)
                    assertEquals(915, device.screenHeight)
                }
            }
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
    fun fontSetsFollowTheDeviceVendor() {
        // v0.174.0: the font enumeration shelf belongs to the
        // DEVICE vendor, not the host and not the GPU family
        // (the Galaxy A54 pairs a Mali GPU with Samsung's shelf,
        // so vendor is read off the model id, never the GPU).
        // Galaxy devices carry SamsungOne and not Google Sans;
        // Pixels the reverse; both carry the Android base set;
        // and the known set the script intercepts with covers
        // every device's whole shelf.
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                assertTrue(device.fonts.contains("Roboto"))
                if (device.id.startsWith("SM-")) {
                    assertTrue(device.fonts.contains("SamsungOne"))
                    assertFalse(device.fonts.contains("Google Sans"))
                } else if (device.id.startsWith("Pixel")) {
                    assertTrue(device.fonts.contains("Google Sans"))
                    assertTrue(
                        device.fonts.contains("Google Sans Text")
                    )
                    assertFalse(device.fonts.contains("SamsungOne"))
                } else {
                    throw AssertionError(
                        "unclassified device: ${device.id}"
                    )
                }
                assertTrue(
                    FingerprintProfiles.KNOWN_FONT_FAMILIES
                        .containsAll(device.fonts)
                )
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
