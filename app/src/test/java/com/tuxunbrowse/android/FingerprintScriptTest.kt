package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FingerprintScript
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The fingerprint payload (v0.144.0) carries the resolved
 * profile's own values into the page — and nothing from another
 * profile. Pure string assembly, so the checks are exact.
 */
class FingerprintScriptTest {

    @Test
    fun payloadCarriesTheResolvedProfile() {
        val payload = FingerprintScript.payload(
            FingerprintProfiles.resolve("US", "America/New_York")
        )
        assertTrue(payload.contains("America/New_York"))
        assertTrue(payload.contains("Eastern Standard Time"))
        assertTrue(payload.contains("SM-S921U"))
        assertTrue(payload.contains("Adreno (TM) 750"))
        assertTrue(payload.contains("en-US"))
        assertTrue(payload.contains("40.7128"))
        assertTrue(payload.contains("-74.006"))
        // The surface pieces are all in place.
        assertTrue(payload.contains("getTimezoneOffset"))
        assertTrue(payload.contains("getCurrentPosition"))
        assertTrue(payload.contains("getImageData"))
        assertTrue(payload.contains("0x9246"))
        // v0.149.0: the framebuffer readback carries noise too.
        assertTrue(payload.contains("readPixels"))
        // v0.150.0: colour depth and touch points are pinned too.
        assertTrue(payload.contains("colorDepth"))
        assertTrue(payload.contains("maxTouchPoints"))
        // v0.152.0: audio noise applies once per buffer (WeakMap).
        assertTrue(payload.contains("WeakMap"))
        assertTrue(payload.contains("getChannelData"))
        assertTrue(payload.contains("getBoundingClientRect"))
        assertTrue(payload.contains("getBattery"))
        assertTrue(payload.contains("RTCPeerConnection"))
        assertTrue(payload.contains("userAgentData"))
        // Another region's identity must not leak in.
        assertFalse(payload.contains("Asia/Tokyo"))
        assertFalse(payload.contains("Pixel 8"))
    }

    @Test
    fun payloadFollowsThePickedZoneInsideTheRegion() {
        val payload = FingerprintScript.payload(
            FingerprintProfiles.resolve("US", "America/Los_Angeles")
        )
        assertTrue(payload.contains("America/Los_Angeles"))
        assertTrue(payload.contains("34.0522"))
        assertFalse(payload.contains("America/New_York"))
    }

    @Test
    fun payloadCarriesJapanInJapanese() {
        val payload = FingerprintScript.payload(
            FingerprintProfiles.resolve("JP", null)
        )
        assertTrue(payload.contains("Asia/Tokyo"))
        assertTrue(payload.contains("ja-JP"))
        assertTrue(payload.contains("Mali-G715s"))
        assertTrue(payload.contains("35.6762"))
    }

    @Test
    fun payloadIsStableForTheSameProfile() {
        // v0.145.0: a real fingerprint browser's profile presents
        // the SAME fingerprint across sessions — the payload is a
        // pure function of the profile (seed is the profile's own
        // constant) and must contain no per-build randomness.
        val resolved = FingerprintProfiles.resolve("JP", null)
        assertEquals(
            FingerprintScript.payload(resolved),
            FingerprintScript.payload(resolved)
        )
        assertFalse(
            FingerprintScript.payload(resolved).contains("Math.random")
        )
    }

    @Test
    fun payloadCarriesTheV1450Surfaces() {
        val payload = FingerprintScript.payload(
            FingerprintProfiles.resolve("JP", null)
        )
        // Voice list names the region's engine.
        assertTrue(payload.contains("getVoices"))
        assertTrue(payload.contains("日本語"))
        // WebGL capability table, not only the strings.
        assertTrue(payload.contains("3379"))
        assertTrue(payload.contains("16384"))
        // Panel, network and device-list stories.
        assertTrue(payload.contains("matchMedia"))
        assertTrue(payload.contains("color-gamut"))
        assertTrue(payload.contains("connection"))
        assertTrue(payload.contains("enumerateDevices"))
        // Font-metric scaling and UA-CH form factors.
        assertTrue(payload.contains("measureText"))
        assertTrue(payload.contains("formFactors"))
        // Japan observes no DST: no daylight name in the payload.
        assertTrue(payload.contains("DAYLIGHT_NAME = null"))
    }

    @Test
    fun payloadCarriesDaylightNamesForTheStates() {
        val payload = FingerprintScript.payload(
            FingerprintProfiles.resolve("US", "America/New_York")
        )
        assertTrue(payload.contains("Eastern Daylight Time"))
        // Taiwan's smaller GPU carries its own combined-unit cap.
        val tw = FingerprintScript.payload(
            FingerprintProfiles.resolve("TW", null)
        )
        assertTrue(tw.contains("35661: 48"))
    }

    @Test
    fun payloadFollowsThePickedDevice() {
        // v0.146.0: the model library pick drives the whole
        // hardware story — a Pixel picked inside the US region
        // reports the Pixel's UA, GPU strings and caps, and none
        // of the default Galaxy's identity leaks through.
        val payload = FingerprintScript.payload(
            FingerprintProfiles.resolve(
                "US", null, deviceId = "Pixel 8 Pro"
            )
        )
        assertTrue(payload.contains("Pixel 8 Pro"))
        assertTrue(payload.contains("Mali-G715s"))
        assertFalse(payload.contains("SM-S921U"))
        assertFalse(payload.contains("Adreno"))
        // …and the languages follow a picked in-region language.
        val spanish = FingerprintScript.payload(
            FingerprintProfiles.resolve(
                "US", null, language = "es-US"
            )
        )
        assertTrue(spanish.contains("'es-US','es','en-US','en'"))
    }

    @Test
    fun chargingStoryMatchesTheProfile() {
        val sg = FingerprintScript.payload(
            FingerprintProfiles.resolve("SG", null)
        )
        assertTrue(sg.contains("charging: true"))
        assertTrue(sg.contains("chargingTime: 0"))
        val us = FingerprintScript.payload(
            FingerprintProfiles.resolve("US", null)
        )
        assertTrue(us.contains("charging: false"))
        assertTrue(us.contains("chargingTime: Infinity"))
    }
}
