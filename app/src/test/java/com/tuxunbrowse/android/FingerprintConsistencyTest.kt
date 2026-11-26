package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FingerprintProtection
import com.tuxunbrowse.android.engine.FingerprintScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
                    device.userAgent.contains(
                        "Chrome/" +
                            FingerprintProfiles.CHROME_FULL_VERSION
                    )
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
    fun hostChromeVersionFollowsTheHostUa() {
        // v0.176.0: the claimed Chrome version follows the
        // host engine's real one — parsed out of the default
        // UA, swapped into the profile UA as the ONLY change
        // (device model stays the profile's), and carried by
        // the UA-CH fields. Unreadable host UA -> no rewrite.
        val host = FingerprintProfiles.parseHostChrome(
            "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/140.0.7339.207 Mobile Safari/537.36"
        )
        assertEquals("140", host?.major)
        assertEquals("140.0.7339.207", host?.fullVersion)
        assertNull(FingerprintProfiles.parseHostChrome(null))
        assertNull(
            FingerprintProfiles.parseHostChrome("not a user agent")
        )
        val resolved = FingerprintProfiles.withHostChrome(
            FingerprintProfiles.resolve("US", null), host
        )
        assertTrue(
            resolved.device.userAgent
                .contains("Chrome/140.0.7339.207")
        )
        assertTrue(resolved.device.userAgent.contains("SM-S921U"))
        assertEquals("140", resolved.chromeMajor)
        assertEquals("140.0.7339.207", resolved.chromeFullVersion)
        val untouched = FingerprintProfiles.withHostChrome(
            FingerprintProfiles.resolve("US", null), null
        )
        assertEquals(
            FingerprintProfiles.CHROME_FULL_VERSION,
            untouched.chromeFullVersion
        )
    }

    @Test
    fun hostChromeVersionParseBoundaries() {
        // v0.203.0 (WebView version-matrix audit): the parse
        // contract at the edges of the host range. The major is
        // an unbounded digit run, so two-digit majors from old
        // WebViews and any future three-digit majors parse the
        // same way; a Version/ token in front (old stock-style
        // UAs) does not hide the Chrome token from find(); and
        // a Chrome token without the full four-part shape is
        // NOT a version this parser may invent numbers for —
        // it returns null and the caller keeps the table
        // fallback.
        val old = FingerprintProfiles.parseHostChrome(
            "Mozilla/5.0 (Linux; Android 10; SM-G975F) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/85.0.4183.127 Mobile Safari/537.36"
        )
        assertEquals("85", old?.major)
        assertEquals("85.0.4183.127", old?.fullVersion)
        val versionPrefixed = FingerprintProfiles.parseHostChrome(
            "Mozilla/5.0 (Linux; U; Android 8.1.0; zh-cn) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Version/4.0 Chrome/71.0.3578.99 Mobile " +
                "Safari/537.36"
        )
        assertEquals("71", versionPrefixed?.major)
        assertEquals("71.0.3578.99", versionPrefixed?.fullVersion)
        val future = FingerprintProfiles.parseHostChrome(
            "Mozilla/5.0 (Linux; Android 16) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/200.0.1000.42 Mobile Safari/537.36"
        )
        assertEquals("200", future?.major)
        assertNull(
            FingerprintProfiles.parseHostChrome(
                "Mozilla/5.0 (Linux; Android 14) " +
                    "AppleWebKit/537.36 Chrome/120.0 Mobile"
            )
        )
        assertNull(FingerprintProfiles.parseHostChrome(""))
        // The rewrite carries a two-digit host major into the
        // UA-CH fields unchanged (no padding, no truncation).
        val rewritten = FingerprintProfiles.withHostChrome(
            FingerprintProfiles.resolve("JP", null), old
        )
        assertEquals("85", rewritten.chromeMajor)
        assertEquals("85.0.4183.127", rewritten.chromeFullVersion)
        assertTrue(
            rewritten.device.userAgent.contains("Chrome/85.0.4183.127")
        )
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
    fun ruleLayerFindsTheWholeLibraryConsistent() {
        // v0.183.0: the encoded correlation rules must hold
        // over every region x device the library ships, and
        // over every resolved combination the bounded resolver
        // can produce (all languages x zones sampled via the
        // default device of each region plus each device at
        // the region default).
        for (profile in FingerprintProfiles.all) {
            for (device in profile.devices) {
                assertEquals(
                    "${profile.regionId}/${device.id}",
                    emptyList<String>(),
                    FingerprintProfiles.violations(profile, device)
                )
            }
            for (language in profile.languageOptions) {
                val resolved = FingerprintProfiles.resolve(
                    profile.regionId, null, language = language
                )
                assertEquals(
                    "${profile.regionId}/$language",
                    emptyList<String>(),
                    FingerprintProfiles.violations(resolved)
                )
            }
        }
    }

    @Test
    fun ruleLayerCatchesHandBrokenCombinations() {
        // The negative half: each hand-broken copy must be
        // caught by the rule that names its contradiction.
        val us = FingerprintProfiles.region("US")
        val pixel = us.devices.first { it.id.startsWith("Pixel") }
        val galaxy = us.devices.first { it.id.startsWith("SM-") }
        // Mali renderer wearing a Qualcomm WebGPU identity.
        assertTrue(
            FingerprintProfiles.violations(
                us, pixel.copy(webGpuVendor = "qualcomm")
            ).any { it.contains("WebGPU") }
        )
        // Galaxy with its own shelf entry stripped.
        assertTrue(
            FingerprintProfiles.violations(
                us, galaxy.copy(fonts = galaxy.fonts - "SamsungOne")
            ).any { it.contains("SamsungOne") }
        )
        // Pixel claiming the other vendor's shelf entry.
        assertTrue(
            FingerprintProfiles.violations(
                us, pixel.copy(fonts = pixel.fonts + "SamsungOne")
            ).any { it.contains("SamsungOne") }
        )
        // UA that names a different model than the device.
        assertTrue(
            FingerprintProfiles.violations(
                us,
                galaxy.copy(
                    userAgent = galaxy.userAgent.replace(
                        galaxy.uaModel, "SM-XXXXX"
                    )
                )
            ).any { it.contains("UA") }
        )
        // Caps that contradict each other.
        assertTrue(
            FingerprintProfiles.violations(
                us,
                galaxy.copy(
                    webGl = galaxy.webGl.copy(
                        maxCubeMapTextureSize =
                            galaxy.webGl.maxTextureSize + 1
                    )
                )
            ).any { it.contains("Cube-map") }
        )
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

    // ---- Protection switches (v0.190.0) ----

    @Test
    fun protectionJsonRoundTrips() {
        val p = FingerprintProtection(
            canvasNoise = false, sensors = false,
            chromeObject = false,
            timezone = false, language = false,
            geolocation = false, screen = false,
            deviceIdentity = false, connection = false
        )
        val back = FingerprintProtection.fromJson(p.toJson())
        assertEquals(p, back)
        // Corrupt or missing storage falls back to all-on: a
        // damaged pref must never silently weaken a surface.
        assertEquals(
            FingerprintProtection(),
            FingerprintProtection.fromJson(null)
        )
        assertEquals(
            FingerprintProtection(),
            FingerprintProtection.fromJson("{broken")
        )
        assertEquals(
            FingerprintProtection(),
            FingerprintProtection.fromJson("{}")
        )
    }

    @Test
    fun protectionSwitchesGateTheirOwnPayloadSections() {
        val resolved = FingerprintProfiles.resolve(
            "jp", "Asia/Tokyo", "pixel8", "ja-JP"
        )
        val allOn = FingerprintScript.payload(resolved)
        assertTrue(allOn.contains("var FP_OPT_CANVAS = true;"))
        assertTrue(allOn.contains("var FP_OPT_SENSORS = true;"))
        assertTrue(allOn.contains("var FP_OPT_CHROME = true;"))
        // The new sections exist in the default payload.
        assertTrue(allOn.contains("deviceorientation"))
        assertTrue(allOn.contains("loadTimes"))
        assertTrue(allOn.contains("notifications"))
        // Guards are wired: each gated section checks its flag.
        assertTrue(allOn.contains("if (!FP_OPT_CANVAS) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_WEBGL) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_FONTS) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_WEBRTC) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_BATTERY) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_SENSORS) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_CHROME) return;"))
        val noCanvas = FingerprintScript.payload(
            resolved,
            FingerprintProtection(canvasNoise = false)
        )
        assertTrue(noCanvas.contains("var FP_OPT_CANVAS = false;"))
        assertTrue(noCanvas.contains("var FP_OPT_WEBGL = true;"))
        // A different switch set is a different payload (the
        // cache key includes the switches).
        assertTrue(noCanvas != allOn)
        val noSensors = FingerprintScript.payload(
            resolved,
            FingerprintProtection(sensors = false)
        )
        assertTrue(noSensors.contains("var FP_OPT_SENSORS = false;"))
        assertTrue(noSensors.contains("var FP_OPT_CANVAS = true;"))
    }

    @Test
    fun coreSurfaceSwitchesGateTheirOwnPayloadSections() {
        // v0.224.0: the six always-on core surfaces became
        // real switches. Each flag exists in the default
        // payload, each gated section checks its flag, and
        // turning one off flips exactly its own flag.
        val resolved = FingerprintProfiles.resolve(
            "jp", "Asia/Tokyo", "pixel8", "ja-JP"
        )
        val allOn = FingerprintScript.payload(resolved)
        for (flag in listOf(
            "FP_OPT_TIMEZONE", "FP_OPT_LANGUAGE", "FP_OPT_GEO",
            "FP_OPT_SCREEN", "FP_OPT_IDENTITY",
            "FP_OPT_CONNECTION"
        )) {
            assertTrue(
                "default payload must carry $flag on",
                allOn.contains("var $flag = true;")
            )
        }
        // Section guards, in the established shape.
        assertTrue(allOn.contains("if (!FP_OPT_TIMEZONE) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_GEO) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_SCREEN) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_CONNECTION) return;"))
        assertTrue(allOn.contains("if (!FP_OPT_LANGUAGE) return;"))
        // The Intl section serves two switches and stands
        // down only when both are off; its DateTimeFormat
        // wrapper defaults each half under its own flag.
        assertTrue(
            allOn.contains(
                "if (!FP_OPT_TIMEZONE && !FP_OPT_LANGUAGE) return;"
            )
        )
        assertTrue(
            allOn.contains(
                "if (!opts.timeZone && FP_OPT_TIMEZONE)"
            )
        )
        // Identity is one switch for the whole navigator
        // face, UA-CH included.
        assertTrue(allOn.contains("if (FP_OPT_IDENTITY) {"))
        // The permissions section's geolocation branch
        // follows the geolocation switch, so the two
        // surfaces can never disagree about the grant.
        assertTrue(
            allOn.contains(
                "desc.name === 'geolocation' && FP_OPT_GEO"
            )
        )
        val noTimezone = FingerprintScript.payload(
            resolved,
            FingerprintProtection(timezone = false)
        )
        assertTrue(
            noTimezone.contains("var FP_OPT_TIMEZONE = false;")
        )
        assertTrue(
            noTimezone.contains("var FP_OPT_LANGUAGE = true;")
        )
        assertTrue(noTimezone != allOn)
        val noIdentity = FingerprintScript.payload(
            resolved,
            FingerprintProtection(deviceIdentity = false)
        )
        assertTrue(
            noIdentity.contains("var FP_OPT_IDENTITY = false;")
        )
        assertTrue(
            noIdentity.contains("var FP_OPT_TIMEZONE = true;")
        )
    }
}
