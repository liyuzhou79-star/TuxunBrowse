package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.customFingerprintAdmission
import com.tuxunbrowse.android.engine.CustomFingerprint
import com.tuxunbrowse.android.engine.FingerprintIdentity
import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FpStorageNames
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Device-fingerprint identity + random-device candidates
 * (v0.224.0). The identity shown on the fingerprint home is
 * a deterministic derivation of the resolved combination:
 * same combination, same identity, forever; any change of
 * seed, device, zone or language is a different identity.
 * The Chrome version is NOT an input — the claim follows
 * the host WebView, and a host update must not rename the
 * persona.
 */
class FingerprintIdentityTest {

    private fun allResolved() =
        FingerprintProfiles.all.flatMap { profile ->
            profile.devices.map { device ->
                FingerprintProfiles.resolve(
                    profile.regionId,
                    profile.timezones.first().id,
                    device.id,
                    profile.language
                )
            }
        }

    @Test
    fun identityIsDeterministicAndUniquePerCombination() {
        val resolved = allResolved()
        assertEquals(16, resolved.size)
        val ids = resolved.map { FingerprintIdentity.idOf(it) }
        // Deterministic: a second derivation agrees everywhere.
        assertEquals(
            ids,
            resolved.map { FingerprintIdentity.idOf(it) }
        )
        // Unique: no two library combinations share an identity.
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all {
            it.length == FingerprintIdentity.ID_LENGTH &&
                it.all { c -> c in '0'..'9' || c in 'a'..'f' }
        })
    }

    @Test
    fun identityIgnoresTheChromeVersionClaim() {
        val base = FingerprintProfiles.resolve(
            "JP", "Asia/Tokyo", "Pixel 8", "ja-JP"
        )
        val otherChrome = base.copy(
            chromeMajor = "999",
            chromeFullVersion = "999.0.0.0"
        )
        assertEquals(
            FingerprintIdentity.idOf(base),
            FingerprintIdentity.idOf(otherChrome)
        )
    }

    @Test
    fun seedOverrideChangesIdentityAndSticks() {
        val base = FingerprintProfiles.resolve(
            "JP", "Asia/Tokyo", "Pixel 8", "ja-JP"
        )
        assertEquals(
            base.profile.noiseSeed * 100 + base.device.seedDelta,
            base.seed
        )
        val seeded = base.copy(seedOverride = 424242)
        assertEquals(424242, seeded.seed)
        assertFalse(
            FingerprintIdentity.idOf(base) ==
                FingerprintIdentity.idOf(seeded)
        )
        // The same override reproduces the same identity —
        // the seed is stored with the entry, never re-rolled.
        assertEquals(
            FingerprintIdentity.idOf(seeded),
            FingerprintIdentity.idOf(
                base.copy(seedOverride = 424242)
            )
        )
        // Zone and language are identity inputs too (the US
        // region offers two of each, so both can move inside
        // one region without leaving its sets).
        val usBase = FingerprintProfiles.resolve(
            "US", "America/New_York", "SM-S921U", "en-US"
        )
        assertFalse(
            FingerprintIdentity.idOf(usBase) ==
                FingerprintIdentity.idOf(
                    FingerprintProfiles.resolve(
                        "US", "America/Los_Angeles",
                        "SM-S921U", "en-US"
                    )
                )
        )
        assertFalse(
            FingerprintIdentity.idOf(usBase) ==
                FingerprintIdentity.idOf(
                    FingerprintProfiles.resolve(
                        "US", "America/New_York",
                        "SM-S921U", "es-US"
                    )
                )
        )
    }

    @Test
    fun maskedFormShowsOnlyThePrefix() {
        val id = FingerprintIdentity.idOf(
            FingerprintProfiles.resolve(
                "US", "America/New_York", "SM-S921U", "en-US"
            )
        )
        val masked = FingerprintIdentity.masked(id)
        assertTrue(masked.startsWith(id.take(6)))
        assertEquals(12, masked.length)
        assertFalse(masked.contains(id.substring(6, 12)))
        assertEquals(id.take(6), FingerprintIdentity.prefixOf(id))
    }

    @Test
    fun candidatesAreDistinctFromEachOtherAndCurrent() {
        val profile = FingerprintProfiles.region("JP")
        val current = FingerprintProfiles.resolve(
            "JP", "Asia/Tokyo", "Pixel 8", "ja-JP"
        )
        var state = 987654321
        val nextRandom = {
            // A small LCG: deterministic, full-range output.
            state = (state * 1103515245 + 12345) and 0x7fffffff
            state
        }
        val candidates = FingerprintIdentity.randomCandidates(
            profile, "Asia/Tokyo", "ja-JP", current, nextRandom
        )
        assertEquals(3, candidates.size)
        val ids = candidates.map { entry ->
            FingerprintIdentity.idOf(
                FingerprintProfiles.resolve(
                    entry.regionId, entry.timezoneId,
                    entry.deviceId, entry.language
                ).copy(seedOverride = entry.seed)
            )
        }
        assertEquals(3, ids.toSet().size)
        assertFalse(FingerprintIdentity.idOf(current) in ids)
        assertTrue(candidates.all {
            it.regionId == "JP" && it.seed != 0 &&
                it.label.startsWith("随机设备 · ")
        })
        // The label's preview is the candidate's own prefix.
        candidates.forEachIndexed { i, entry ->
            assertTrue(
                entry.label.endsWith(
                    FingerprintIdentity.prefixOf(ids[i])
                )
            )
        }
    }

    @Test
    fun candidatesTerminateOnADegenerateRandomSource() {
        val profile = FingerprintProfiles.region("TW")
        val current = FingerprintProfiles.resolve(
            "TW", "Asia/Taipei",
            profile.devices.first().id, profile.language
        )
        // Always-zero randomness: same device, same seed roll
        // every time — the nudge path must still produce three
        // distinct candidates instead of looping or repeating.
        val candidates = FingerprintIdentity.randomCandidates(
            profile, "Asia/Taipei", profile.language, current,
            nextRandom = { 0 }
        )
        assertEquals(3, candidates.size)
        assertEquals(
            3, candidates.map { it.seed }.toSet().size
        )
    }

    @Test
    fun seededStorageNamesNeverCollideWithPlainOnes() {
        val combos = FingerprintProfiles.all.flatMap { profile ->
            profile.devices.map { profile.regionId to it.id }
        }
        assertEquals(16, combos.size)
        val plain = mutableSetOf(FpStorageNames.LEGACY_NAME)
        for ((regionId, deviceId) in combos) {
            plain += FpStorageNames.nameFor(regionId, deviceId, null)
            plain += FpStorageNames.nameFor(
                regionId, deviceId,
                FpStorageNames.keyOf(regionId, deviceId)
            )
        }
        val seeds = listOf(1, 2, 42, 140101, Int.MAX_VALUE, -1)
        val seeded = mutableSetOf<String>()
        for ((regionId, deviceId) in combos) {
            for (seed in seeds) {
                val name = FpStorageNames.nameForSeeded(
                    regionId, deviceId, seed
                )
                assertTrue(FpStorageNames.isFpProfileName(name))
                seeded += name
            }
        }
        // Every (combination, seed) pair is its own jar, and
        // no seeded jar shadows a plain or legacy one.
        assertEquals(combos.size * seeds.size, seeded.size)
        assertTrue(plain.intersect(seeded).isEmpty())
    }

    @Test
    fun admissionTreatsTheSeedAsPartOfTheCombination() {
        val plain = CustomFingerprint(
            "JP", "Pixel 8", "Asia/Tokyo", "ja-JP", "a"
        )
        val seeded = plain.copy(label = "b", seed = 777)
        // Same ids, different seed: a different device, admitted.
        assertTrue(
            customFingerprintAdmission(listOf(plain), seeded)
        )
        // Same ids AND seed under another label: a duplicate.
        assertFalse(
            customFingerprintAdmission(
                listOf(seeded), seeded.copy(label = "other")
            )
        )
        assertFalse(
            customFingerprintAdmission(listOf(plain), plain)
        )
    }

    @Test
    fun customProfileJsonRoundTripsTheSeed() {
        val entries = listOf(
            CustomFingerprint(
                "JP", "Pixel 8", "Asia/Tokyo", "ja-JP", "plain"
            ),
            CustomFingerprint(
                "JP", "Pixel 8", "Asia/Tokyo", "ja-JP",
                "随机设备 · Pixel 8 · abc123", seed = 777
            )
        )
        val parsed = FingerprintProfiles.parseCustomProfiles(
            FingerprintProfiles.customProfilesToJson(entries)
        )
        assertEquals(entries, parsed)
        // A pre-v0.224.0 file has no "s" key: seed reads as 0,
        // the entry keeps working as a plain combination.
        val legacy = FingerprintProfiles.parseCustomProfiles(
            """{"app":"TuxunBrowse","kind":"fingerprint-profiles",""" +
                """"version":1,"profiles":[{"r":"JP",""" +
                """"d":"Pixel 8","t":"Asia/Tokyo","l":"ja-JP",""" +
                """"label":"old"}]}"""
        )
        assertEquals(1, legacy.size)
        assertEquals(0, legacy.first().seed)
    }
}
