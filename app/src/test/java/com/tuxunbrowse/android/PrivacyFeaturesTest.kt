package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.BackupSettings
import com.tuxunbrowse.android.data.TrackingProtectionLevel
import com.tuxunbrowse.android.data.backupToJson
import com.tuxunbrowse.android.data.parseBackup
import com.tuxunbrowse.android.engine.AdBlocker
import com.tuxunbrowse.android.engine.PrivacyScript
import com.tuxunbrowse.android.engine.PrivacySignals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Privacy section (v0.92.0): the signals script must contain
 * exactly the snippets whose switches are on, the load headers
 * must match the switches, the STRICT level must widen the WebView
 * block list without changing STANDARD behaviour, and the four new
 * settings must round-trip through a full backup.
 */
class PrivacyFeaturesTest {

    @Test
    fun payloadIsNullWhenAllSignalsOff() {
        assertNull(PrivacyScript.payload(PrivacySignals()))
    }

    @Test
    fun payloadCarriesOnlyTheEnabledSnippets() {
        val dntOnly = PrivacyScript.payload(
            PrivacySignals(doNotTrack = true)
        )!!
        assertTrue(dntOnly.contains("doNotTrack"))
        assertFalse(dntOnly.contains("globalPrivacyControl"))
        assertFalse(dntOnly.contains("RTCPeerConnection"))

        val gpcOnly = PrivacyScript.payload(
            PrivacySignals(globalPrivacyControl = true)
        )!!
        assertTrue(gpcOnly.contains("globalPrivacyControl"))
        assertFalse(gpcOnly.contains("doNotTrack"))

        val rtcOnly = PrivacyScript.payload(
            PrivacySignals(webRtcDisabled = true)
        )!!
        assertTrue(rtcOnly.contains("window.RTCPeerConnection"))
        assertTrue(rtcOnly.contains("webkitRTCPeerConnection"))
        // Capture is not the leak channel and stays untouched.
        assertFalse(rtcOnly.contains("getUserMedia"))
    }

    @Test
    fun headersFollowTheSwitches() {
        assertTrue(PrivacySignals().headers().isEmpty())
        assertEquals(
            mapOf("DNT" to "1"),
            PrivacySignals(doNotTrack = true).headers()
        )
        assertEquals(
            mapOf("Sec-GPC" to "1"),
            PrivacySignals(globalPrivacyControl = true).headers()
        )
        assertEquals(
            mapOf("DNT" to "1", "Sec-GPC" to "1"),
            PrivacySignals(
                doNotTrack = true, globalPrivacyControl = true
            ).headers()
        )
        // WebRTC off is a script-only signal: no header exists.
        assertTrue(
            PrivacySignals(webRtcDisabled = true).headers().isEmpty()
        )
    }

    @Test
    fun strictLevelWidensTheBlockList() {
        // Base-list hosts are blocked at both levels.
        assertTrue(AdBlocker.isAdRequest("https://doubleclick.net/"))
        assertTrue(
            AdBlocker.isAdRequest("https://doubleclick.net/", strict = true)
        )
        // Strict-list hosts only at STRICT.
        assertFalse(AdBlocker.isAdRequest("https://static.hotjar.com/x.js"))
        assertTrue(
            AdBlocker.isAdRequest(
                "https://static.hotjar.com/x.js", strict = true
            )
        )
        assertFalse(AdBlocker.isAdRequest("https://coinhive.com/miner.js"))
        assertTrue(
            AdBlocker.isAdRequest(
                "https://coinhive.com/miner.js", strict = true
            )
        )
        // Subdomains of strict hosts count too.
        assertTrue(
            AdBlocker.isAdRequest(
                "https://api.segment.com/v1", strict = true
            )
        )
        // Normal content is never blocked at either level.
        assertFalse(
            AdBlocker.isAdRequest("https://www.baidu.com/", strict = true)
        )
    }

    @Test
    fun strictListHasNoDuplicatesAndNoBaseOverlapSurprises() {
        assertEquals(
            AdBlocker.strictHostList.size,
            AdBlocker.strictHostList.toSet().size
        )
        assertTrue(AdBlocker.strictHostList.size >= 40)
    }

    @Test
    fun trackingProtectionLevelParsesIds() {
        assertEquals(
            TrackingProtectionLevel.STRICT,
            TrackingProtectionLevel.fromId("strict")
        )
        assertEquals(
            TrackingProtectionLevel.STANDARD,
            TrackingProtectionLevel.fromId("standard")
        )
        assertEquals(
            TrackingProtectionLevel.STANDARD,
            TrackingProtectionLevel.fromId(null)
        )
        assertEquals(
            TrackingProtectionLevel.STANDARD,
            TrackingProtectionLevel.fromId("bogus")
        )
    }

    @Test
    fun privacySettingsRoundTripThroughBackup() {
        val json = backupToJson(
            emptyList(), emptyList(),
            BackupSettings(
                trackingProtectionLevel = TrackingProtectionLevel.STRICT,
                doNotTrackEnabled = true,
                globalPrivacyControlEnabled = true,
                webRtcDisabled = true
            )
        )
        val parsed = parseBackup(json)!!
        assertEquals(
            TrackingProtectionLevel.STRICT,
            parsed.settings.trackingProtectionLevel
        )
        assertEquals(true, parsed.settings.doNotTrackEnabled)
        assertEquals(true, parsed.settings.globalPrivacyControlEnabled)
        assertEquals(true, parsed.settings.webRtcDisabled)
    }

    @Test
    fun backupWithoutPrivacyFieldsLeavesThemNull() {
        val json = backupToJson(
            emptyList(), emptyList(), BackupSettings()
        )
        val parsed = parseBackup(json)!!
        assertNull(parsed.settings.trackingProtectionLevel)
        assertNull(parsed.settings.doNotTrackEnabled)
        assertNull(parsed.settings.globalPrivacyControlEnabled)
        assertNull(parsed.settings.webRtcDisabled)
    }
}
