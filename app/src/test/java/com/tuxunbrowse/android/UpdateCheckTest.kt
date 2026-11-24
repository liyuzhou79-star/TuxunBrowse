package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.UpdateFetchResult
import com.tuxunbrowse.android.data.compareVersionNames
import com.tuxunbrowse.android.data.isReleaseTag
import com.tuxunbrowse.android.data.parseVersionNumbers
import com.tuxunbrowse.android.data.pickLatestRelease
import com.tuxunbrowse.android.data.releasePageUrl
import com.tuxunbrowse.android.data.updateFetchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.119.0 update-check audit: the version comparison, the
 * release picking, the download-page URL and the outcome
 * classification are pinned with the boundary samples the audit
 * named — above all 0.99.0 vs 0.100.0, the pair a string compare
 * (or a list-order pick) would rank the wrong way round.
 */
class CompareVersionBoundaryTest {

    @Test
    fun threeDigitSegmentBeatsTwoDigit() {
        // String order says "0.99.0" > "0.100.0"; numeric order
        // must say the opposite.
        assertTrue(compareVersionNames("0.99.0", "0.100.0") < 0)
        assertTrue(compareVersionNames("0.100.0", "0.99.0") > 0)
        assertTrue(compareVersionNames("v0.99.0", "v0.100.0") < 0)
    }

    @Test
    fun currentVersionEqualsItself() {
        assertEquals(0, compareVersionNames("0.119.0", "0.119.0"))
        assertEquals(0, compareVersionNames("v0.119.0", "0.119.0"))
    }

    @Test
    fun fourSegmentVersions() {
        assertTrue(compareVersionNames("1.2.3.4", "1.2.3") > 0)
        assertTrue(compareVersionNames("1.2.3", "1.2.3.4") < 0)
        assertEquals(0, compareVersionNames("1.2.3.0", "1.2.3"))
    }

    @Test
    fun emptyAndBlankInputs() {
        assertEquals(emptyList<Int>(), parseVersionNumbers(""))
        assertEquals(emptyList<Int>(), parseVersionNumbers("   "))
        assertEquals(0, compareVersionNames("", "0.0.0"))
        assertTrue(compareVersionNames("", "0.1.0") < 0)
        assertTrue(compareVersionNames("0.1.0", "") > 0)
    }

    @Test
    fun malformedSegmentsNeverCrash() {
        assertEquals(listOf(0), parseVersionNumbers("abc"))
        assertEquals(listOf(1, 0, 2), parseVersionNumbers("1..2"))
        assertEquals(0, compareVersionNames("v", "0.0.0"))
        // A segment too big for Int counts as 0 instead of
        // throwing — pinned so the behaviour is deliberate.
        assertEquals(listOf(0, 0), parseVersionNumbers("999999999999.0"))
    }

    @Test
    fun suffixAfterDashIgnoredAtBoundary() {
        assertEquals(0, compareVersionNames("0.119.0-beta", "0.119.0"))
        assertTrue(compareVersionNames("0.119.0", "0.118.9-rc1") > 0)
    }
}

class PickLatestReleaseTest {

    @Test
    fun picksNumericMaxNotListOrderOrStringMax() {
        val picked = pickLatestRelease(
            listOf(
                "v0.99.0" to "https://example.com/99",
                "v0.118.0" to "https://example.com/118",
                "v0.100.0" to "https://example.com/100"
            )
        )
        assertEquals("v0.118.0" to "https://example.com/118", picked)
    }

    @Test
    fun hundredBeatsNinetyNine() {
        val picked = pickLatestRelease(
            listOf(
                "v0.99.0" to "u99",
                "v0.100.0" to "u100"
            )
        )
        assertEquals("v0.100.0", picked?.first)
    }

    @Test
    fun nonReleaseTagsAreIgnored() {
        val picked = pickLatestRelease(
            listOf(
                "nightly" to "u1",
                "release-9.9.9" to "u2",
                "0.200.0" to "u3",
                "v0.119.0" to "u4"
            )
        )
        assertEquals("v0.119.0" to "u4", picked)
    }

    @Test
    fun nothingQualifiesReturnsNull() {
        assertNull(pickLatestRelease(emptyList()))
        assertNull(
            pickLatestRelease(listOf("nightly" to "u", "" to "u2"))
        )
    }

    @Test
    fun releaseTagShape() {
        assertTrue(isReleaseTag("v0.119.0"))
        assertTrue(isReleaseTag("V1.0.0"))
        assertTrue(isReleaseTag("v1.2"))
        assertFalse(isReleaseTag("0.119.0"))
        assertFalse(isReleaseTag("v"))
        assertFalse(isReleaseTag(""))
        assertFalse(isReleaseTag("v1..2"))
        assertFalse(isReleaseTag("v1.2.x"))
        assertFalse(isReleaseTag("release-1.0.0"))
    }
}

class ReleasePageUrlTest {

    @Test
    fun apiUrlIsUsedVerbatim() {
        assertEquals(
            "https://github.com/liyuzhou79-star/TuxunBrowse/releases/tag/v0.118.0",
            releasePageUrl(
                "v0.119.0",
                "https://github.com/liyuzhou79-star/TuxunBrowse/releases/tag/v0.118.0"
            )
        )
    }

    @Test
    fun blankApiUrlFallsBackToTagShape() {
        assertEquals(
            "https://github.com/liyuzhou79-star/TuxunBrowse/releases/tag/v0.119.0",
            releasePageUrl("v0.119.0", "")
        )
        assertEquals(
            "https://github.com/liyuzhou79-star/TuxunBrowse/releases/tag/v0.119.0",
            releasePageUrl("v0.119.0", "   ")
        )
    }

    @Test
    fun unusableTagYieldsNoUrl() {
        assertEquals("", releasePageUrl("nightly", ""))
        assertEquals("", releasePageUrl("", ""))
    }
}

class UpdateFetchResultTest {

    @Test
    fun successWithReleaseIsFound() {
        val r = updateFetchResult(200, "v0.119.0" to "https://example.com/p")
        assertTrue(r is UpdateFetchResult.Found)
        assertEquals("v0.119.0", (r as UpdateFetchResult.Found).tag)
        assertEquals("https://example.com/p", r.pageUrl)
    }

    @Test
    fun foundFallsBackToAssembledPageUrl() {
        val r = updateFetchResult(200, "v0.119.0" to "")
        assertTrue(r is UpdateFetchResult.Found)
        assertEquals(
            "https://github.com/liyuzhou79-star/TuxunBrowse/releases/tag/v0.119.0",
            (r as UpdateFetchResult.Found).pageUrl
        )
    }

    @Test
    fun successWithoutReleaseIsNoReleases() {
        assertEquals(
            UpdateFetchResult.NoReleases, updateFetchResult(200, null)
        )
    }

    @Test
    fun rateLimitIsNotANetworkFailure() {
        assertEquals(
            UpdateFetchResult.RateLimited, updateFetchResult(403, null)
        )
        assertEquals(
            UpdateFetchResult.RateLimited, updateFetchResult(429, null)
        )
    }

    @Test
    fun otherFailuresAreFailed() {
        assertEquals(
            UpdateFetchResult.Failed, updateFetchResult(null, null)
        )
        assertEquals(
            UpdateFetchResult.Failed, updateFetchResult(404, null)
        )
        assertEquals(
            UpdateFetchResult.Failed, updateFetchResult(500, null)
        )
    }
}
