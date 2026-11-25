package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The UA-CH brand list algorithm (v0.210.0): Chromium
 * computes the GREASE brand's spelling, its version and
 * the order of the whole list from the major version
 * (user_agent_utils.cc). The expectations below are NOT
 * derived from our implementation — they are the brand
 * lists independently measured on real Chrome builds
 * (Clearcote Labs' per-release fingerprint measurements)
 * for five different major versions, so the algorithm has
 * to reproduce five foreign answers, not its own output.
 */
class UaBrandListTest {

    private fun joined(major: Int): String =
        FingerprintProfiles.uaBrandList(major)
            .joinToString(", ") { "${it.first};v=${it.second}" }

    @Test
    fun measuredChromeBrandListsAreReproducedExactly() {
        assertEquals(
            "Not=A?Brand;v=99, Google Chrome;v=151, " +
                "Chromium;v=151",
            joined(151)
        )
        assertEquals(
            "Chromium;v=152, Not?A_Brand;v=24, " +
                "Google Chrome;v=152",
            joined(152)
        )
        assertEquals(
            "Google Chrome;v=155, Chromium;v=155, " +
                "Not(A:Brand;v=24",
            joined(155)
        )
        assertEquals(
            "Not:A-Brand;v=8, Chromium;v=156, " +
                "Google Chrome;v=156",
            joined(156)
        )
        assertEquals(
            "Not-A.Brand;v=99, Google Chrome;v=157, " +
                "Chromium;v=157",
            joined(157)
        )
    }

    @Test
    fun fullVersionListKeepsOrderAndPadsTheGreaseVersion() {
        // Same seed → same permutation; the two product
        // brands carry the full version, the GREASE entry
        // carries its low version padded to four parts
        // (the measured Chrome 156 full list: Not:A-Brand
        // at 8.0.0.0).
        val full = FingerprintProfiles.uaFullVersionList(
            156, "156.0.8078.73"
        )
        assertEquals(
            listOf(
                "Not:A-Brand" to "8.0.0.0",
                "Chromium" to "156.0.8078.73",
                "Google Chrome" to "156.0.8078.73"
            ),
            full
        )
        // And the low-entropy list for the same major is
        // the same order with major versions throughout.
        assertEquals(
            listOf(
                "Not:A-Brand" to "8",
                "Chromium" to "156",
                "Google Chrome" to "156"
            ),
            FingerprintProfiles.uaBrandList(156)
        )
    }

    @Test
    fun everyMajorGetsThreeBrandsWithOneGrease() {
        // Structural invariants over the whole plausible
        // range (the live path follows the host engine, so
        // majors outside the measured set must still produce
        // a well-formed list): exactly three entries, the
        // two product brands at the claimed version, exactly
        // one Not…Brand GREASE entry at a low version.
        for (major in 85..220) {
            val list = FingerprintProfiles.uaBrandList(major)
            assertEquals("major $major", 3, list.size)
            val products = list.filter {
                it.first == "Chromium" ||
                    it.first == "Google Chrome"
            }
            assertEquals("major $major", 2, products.size)
            assertTrue(
                "major $major product versions",
                products.all { it.second == "$major" }
            )
            val grease = list.single {
                it.first.startsWith("Not") &&
                    it.first.endsWith("Brand")
            }
            assertTrue(
                "major $major grease version",
                grease.second in listOf("8", "99", "24")
            )
        }
    }
}
