package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.BackupSettings
import com.tuxunbrowse.android.data.IPHONE_USER_AGENT
import com.tuxunbrowse.android.data.UaPreset
import com.tuxunbrowse.android.data.backupToJson
import com.tuxunbrowse.android.data.parseBackup
import com.tuxunbrowse.android.data.presetUserAgent
import com.tuxunbrowse.android.data.swapUaPlatformSegment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * UA presets (v0.80.0): the Android / desktop strings must be
 * built ONLY from fragments of the device's own default UA (real
 * Android version, model and Chrome version) — the tests pin the
 * exact assembly against a realistic WebView default UA, plus
 * the no-Chrome-token fallback and the backup round trip.
 */
class UserAgentPresetTest {

    // A realistic system WebView default UA (Android 14 device).
    private val defaultUa =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UP1A.231005.007; wv) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 " +
            "Chrome/120.0.6099.43 Mobile Safari/537.36"

    @Test
    fun systemPresetNeverOverrides() {
        assertNull(presetUserAgent(UaPreset.SYSTEM, defaultUa))
        assertNull(presetUserAgent(UaPreset.SYSTEM, null))
    }

    @Test
    fun androidPhoneKeepsRealDeviceFragments() {
        assertEquals(
            "Mozilla/5.0 (Linux; Android 14; Pixel 8 Build/UP1A.231005.007) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.6099.43 Mobile Safari/537.36",
            presetUserAgent(UaPreset.ANDROID_PHONE, defaultUa)
        )
    }

    @Test
    fun desktopSwapsPlatformAndDropsMobile() {
        assertEquals(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/120.0.6099.43 Safari/537.36",
            presetUserAgent(UaPreset.DESKTOP, defaultUa)
        )
    }

    @Test
    fun iphoneIsTheStandardTemplateAndNeedsNoDefault() {
        assertEquals(IPHONE_USER_AGENT, presetUserAgent(UaPreset.IPHONE, defaultUa))
        assertEquals(IPHONE_USER_AGENT, presetUserAgent(UaPreset.IPHONE, null))
        assertTrue(IPHONE_USER_AGENT.startsWith("Mozilla/5.0 (iPhone; CPU iPhone OS"))
        assertTrue(IPHONE_USER_AGENT.contains("Safari/"))
    }

    @Test
    fun blankDefaultMeansNoOverrideForDeviceBuiltPresets() {
        assertNull(presetUserAgent(UaPreset.ANDROID_PHONE, null))
        assertNull(presetUserAgent(UaPreset.ANDROID_PHONE, "  "))
        assertNull(presetUserAgent(UaPreset.DESKTOP, null))
        assertNull(presetUserAgent(UaPreset.DESKTOP, ""))
    }

    @Test
    fun noChromeTokenFallsBackToPlatformSwapOnly() {
        val noChrome =
            "Mozilla/5.0 (Linux; Android 13; SomePhone; wv) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36"
        assertEquals(
            "Mozilla/5.0 (Linux; Android 13; SomePhone) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36",
            presetUserAgent(UaPreset.ANDROID_PHONE, noChrome)
        )
        assertEquals(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) Safari/537.36",
            presetUserAgent(UaPreset.DESKTOP, noChrome)
        )
    }

    @Test
    fun swapPlatformSegmentBasics() {
        assertEquals(
            "Mozilla/5.0 (X) AppleWebKit/1",
            swapUaPlatformSegment("Mozilla/5.0 (A; B) AppleWebKit/1", "X")
        )
        assertEquals("no parens here", swapUaPlatformSegment("no parens here", "X"))
    }

    @Test
    fun fromIdRoundTripAndUnknown() {
        UaPreset.entries.forEach { preset ->
            assertEquals(preset, UaPreset.fromId(preset.id))
        }
        assertEquals(UaPreset.SYSTEM, UaPreset.fromId("nonsense"))
        assertEquals(UaPreset.SYSTEM, UaPreset.fromId(null))
    }

    @Test
    fun backupRoundTripCarriesPreset() {
        val json = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings(uaPreset = UaPreset.IPHONE)
        )
        val data = parseBackup(json)
        assertEquals(UaPreset.IPHONE, data?.settings?.uaPreset)
        assertTrue(
            data?.settings?.overwriteSummary()
                ?.contains("用户代理（UA）预设") == true
        )
    }

    @Test
    fun oldBackupWithoutPresetKeyLeavesItNull() {
        val json = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings()
        )
        assertNull(parseBackup(json)?.settings?.uaPreset)
    }
}
