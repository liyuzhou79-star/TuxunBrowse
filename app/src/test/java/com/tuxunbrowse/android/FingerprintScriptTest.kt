package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FingerprintScript
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
