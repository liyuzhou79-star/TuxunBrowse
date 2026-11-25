package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FpProxyBook
import com.tuxunbrowse.android.engine.FpProxyConfig
import com.tuxunbrowse.android.engine.FpStorageNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.198.0: the per-combination contracts. Storage profile
 * names and the proxy book are both keyed by region + device;
 * the contracts that matter are the ones a mix-up would break
 * silently — two personas sharing a jar or a proxy — so
 * distinctness, the legacy owner's preserved name, and the
 * book's per-key independence are all pinned here.
 */
class FpPerCombinationTest {

    @Test
    fun storageNamesAreSafeAndDistinct() {
        // Device ids carry spaces and dashes ("Pixel 8",
        // "SM-S921U"); names must come out as [a-z0-9_] only.
        val jpPixel = FpStorageNames.nameFor("JP", "Pixel 8", null)
        assertEquals("tuxun_fp_jp_pixel_8", jpPixel)
        val usS24 = FpStorageNames.nameFor("US", "SM-S921U", null)
        assertEquals("tuxun_fp_us_sm_s921u", usS24)

        // Every library combination gets a different jar.
        val combos = listOf(
            "US" to "SM-S921U", "US" to "Pixel 8",
            "JP" to "Pixel 8", "JP" to "Pixel 8 Pro",
            "SG" to "SM-S911B", "TW" to "SM-A546B"
        )
        val names = combos.map {
            FpStorageNames.nameFor(it.first, it.second, null)
        }
        assertEquals(combos.size, names.toSet().size)
        assertTrue(names.all { FpStorageNames.isFpProfileName(it) })
    }

    @Test
    fun legacyOwnerKeepsTheOldJarName() {
        val owner = FpStorageNames.keyOf("JP", "Pixel 8")
        // The owning combination keeps binding the v0.196.0
        // jar, so its logins survive the upgrade.
        assertEquals(
            FpStorageNames.LEGACY_NAME,
            FpStorageNames.nameFor("JP", "Pixel 8", owner)
        )
        // Any other combination gets its own derived jar even
        // though a legacy owner exists.
        assertNotEquals(
            FpStorageNames.LEGACY_NAME,
            FpStorageNames.nameFor("US", "SM-S921U", owner)
        )
        // No legacy jar anywhere: nobody binds the old name.
        assertNotEquals(
            FpStorageNames.LEGACY_NAME,
            FpStorageNames.nameFor("JP", "Pixel 8", null)
        )
        assertNotEquals(
            FpStorageNames.LEGACY_NAME,
            FpStorageNames.nameFor("JP", "Pixel 8", "")
        )
    }

    @Test
    fun fpProfileNameFilter() {
        assertTrue(FpStorageNames.isFpProfileName("tuxun_fp"))
        assertTrue(
            FpStorageNames.isFpProfileName("tuxun_fp_jp_pixel_8")
        )
        // The WebView default profile and anything foreign are
        // not ours to wipe.
        assertFalse(FpStorageNames.isFpProfileName("Default"))
        assertFalse(FpStorageNames.isFpProfileName("tuxun_fpx"))
        assertFalse(FpStorageNames.isFpProfileName(""))
    }

    @Test
    fun slugCapsAndCleans() {
        assertEquals("pixel_8", FpStorageNames.slug("Pixel 8"))
        assertEquals("sm_s921u", FpStorageNames.slug("SM-S921U"))
        assertEquals("a_b", FpStorageNames.slug(" a--b "))
        assertTrue(FpStorageNames.slug("x".repeat(40)).length <= 24)
    }

    @Test
    fun proxyBookRoundTripsPerCombination() {
        val jpKey = FpStorageNames.keyOf("JP", "Pixel 8")
        val usKey = FpStorageNames.keyOf("US", "SM-S921U")
        val book = mapOf(
            jpKey to FpProxyConfig(
                enabled = true, scheme = "socks5",
                host = "127.0.0.1", port = 1080
            ),
            usKey to FpProxyConfig(
                enabled = false, scheme = "http",
                host = "proxy.example.com", port = 8080
            )
        )
        val decoded = FpProxyBook.decode(FpProxyBook.encode(book))
        assertEquals(book, decoded)
        // Independence: editing one combination's entry leaves
        // the other's untouched.
        val edited = decoded.toMutableMap()
        edited[jpKey] = FpProxyConfig()
        assertEquals(book[usKey], edited[usKey])
        assertEquals(FpProxyConfig(), edited[jpKey])
    }

    @Test
    fun proxyBookBrokenInputReadsEmpty() {
        assertTrue(FpProxyBook.decode(null).isEmpty())
        assertTrue(FpProxyBook.decode("").isEmpty())
        assertTrue(FpProxyBook.decode("{not json").isEmpty())
        assertTrue(FpProxyBook.decode("[1,2]").isEmpty())
        assertEquals(
            emptyMap<String, FpProxyConfig>(),
            FpProxyBook.decode("{}")
        )
    }

    @Test
    fun everyLibraryCombinationGetsItsOwnJar() {
        // v0.204.0: the six-sample distinctness in
        // storageNamesAreSafeAndDistinct, extended to the
        // WHOLE library — the per-combination jar contract is
        // only worth anything if it holds for every persona
        // the picker can produce, including devices a future
        // version adds to a region.
        val combos = FingerprintProfiles.all.flatMap { region ->
            region.devices.map { region.regionId to it.id }
        }
        assertEquals(16, combos.size)
        val noLegacy = combos.map {
            FpStorageNames.nameFor(it.first, it.second, null)
        }
        assertEquals(
            "every combination must bind a different jar",
            combos.size, noLegacy.toSet().size
        )
        assertTrue(noLegacy.all {
            FpStorageNames.isFpProfileName(it) &&
                it != FpStorageNames.LEGACY_NAME
        })
        // With ANY one combination owning the legacy jar, the
        // full set stays distinct and the owner — and only the
        // owner — keeps the old name.
        for (owner in combos) {
            val ownerKey = FpStorageNames.keyOf(owner.first, owner.second)
            val names = combos.map {
                FpStorageNames.nameFor(it.first, it.second, ownerKey)
            }
            assertEquals(
                "legacy owner $ownerKey must not collide jars",
                combos.size, names.toSet().size
            )
            combos.zip(names).forEach { (combo, name) ->
                if (combo == owner) {
                    assertEquals(FpStorageNames.LEGACY_NAME, name)
                } else {
                    assertNotEquals(FpStorageNames.LEGACY_NAME, name)
                }
            }
        }
    }
}
