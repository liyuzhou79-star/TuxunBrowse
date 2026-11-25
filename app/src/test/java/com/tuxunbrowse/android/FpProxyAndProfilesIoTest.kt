package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.CustomFingerprint
import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FpProxyConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.197.0: the proxy config's parse/validation contract and
 * the saved-combination export/import parser. Both sit on
 * user-supplied text (a stored pref, a picked file), so the
 * contract is: broken input degrades to empty/unset, never to
 * a half-valid value that would route traffic or resolve a
 * combination somewhere surprising.
 */
class FpProxyAndProfilesIoTest {

    @Test
    fun proxyConfigParsesAndValidates() {
        val unset = FpProxyConfig.fromJson(null)
        assertFalse(unset.enabled)
        assertFalse(unset.isUsable)
        assertEquals("未设置", unset.summary)

        val broken = FpProxyConfig.fromJson("{not json")
        assertEquals(FpProxyConfig(), broken)

        val good = FpProxyConfig.fromJson(
            """{"enabled":true,"scheme":"socks5",""" +
                """"host":"127.0.0.1","port":1080}"""
        )
        assertTrue(good.isUsable)
        assertEquals("socks5://127.0.0.1:1080", good.rule)

        // An unknown scheme falls back to http rather than
        // reaching the proxy layer as-is.
        val oddScheme = FpProxyConfig.fromJson(
            """{"enabled":true,"scheme":"gopher",""" +
                """"host":"h","port":70}"""
        )
        assertEquals("http", oddScheme.scheme)

        // Enabled with no usable host/port is not usable.
        assertFalse(
            FpProxyConfig(enabled = true, host = "", port = 8080)
                .isUsable
        )
        assertFalse(
            FpProxyConfig(enabled = true, host = "h", port = 0)
                .isUsable
        )
        assertFalse(
            FpProxyConfig(enabled = true, host = "h", port = 70000)
                .isUsable
        )
        // A complete but disabled config is not usable either.
        assertFalse(
            FpProxyConfig(
                enabled = false, host = "h", port = 8080
            ).isUsable
        )
    }

    @Test
    fun proxyConfigRoundTrips() {
        val config = FpProxyConfig(
            enabled = true, scheme = "https",
            host = "proxy.example.com", port = 8443
        )
        assertEquals(config, FpProxyConfig.fromJson(config.toJson()))
    }

    @Test
    fun customProfilesExportImportRoundTrips() {
        val entries = listOf(
            CustomFingerprint(
                "JP", "Pixel 8", "Asia/Tokyo", "ja-JP",
                "日本 · Pixel 8 · 东京"
            ),
            CustomFingerprint(
                "US", "SM-S921U", "America/Chicago", "en-US",
                "美国 · Galaxy S24 · 芝加哥"
            )
        )
        val parsed = FingerprintProfiles.parseCustomProfiles(
            FingerprintProfiles.customProfilesToJson(entries)
        )
        assertEquals(entries, parsed)
    }

    @Test
    fun customProfilesImportFiltersAndDedupes() {
        // Bare-array shape is accepted; an unknown region is
        // dropped; a repeated combination collapses.
        val raw = """
            [
              {"r":"JP","d":"Pixel 8","t":"Asia/Tokyo","l":"ja-JP","label":"a"},
              {"r":"JP","d":"Pixel 8","t":"Asia/Tokyo","l":"ja-JP","label":"dup"},
              {"r":"XX","d":"Pixel 8","t":"Asia/Tokyo","l":"ja-JP","label":"bad"}
            ]
        """.trimIndent()
        val parsed = FingerprintProfiles.parseCustomProfiles(raw)
        assertEquals(1, parsed.size)
        assertEquals("a", parsed[0].label)

        assertTrue(
            FingerprintProfiles.parseCustomProfiles(null).isEmpty()
        )
        assertTrue(
            FingerprintProfiles.parseCustomProfiles("junk").isEmpty()
        )
        assertTrue(
            FingerprintProfiles
                .parseCustomProfiles("""{"kind":"other"}""")
                .isEmpty()
        )
    }
}
