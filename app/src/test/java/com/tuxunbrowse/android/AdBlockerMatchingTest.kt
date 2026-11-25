package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.AdBlocker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Boundary semantics of the ad-block host matcher (v0.130.0 pipeline
 * audit). The matcher is host-suffix based: a listed domain blocks
 * itself and its subdomains, nothing else. These cases pin the
 * edges an attacker or an accident could otherwise slip through —
 * glued-on prefixes, listed names parked under an attacker's
 * domain, trailing root dots, case, ports, userinfo, IP literals,
 * malformed URLs and the strict list's on/off boundary.
 */
class AdBlockerMatchingTest {

    @Test
    fun listedApexAndSubdomainsAreBlocked() {
        assertTrue(AdBlocker.isAdRequest("https://doubleclick.net/"))
        assertTrue(AdBlocker.isAdRequest("https://ads.doubleclick.net/x.js"))
        assertTrue(AdBlocker.isAdRequest("https://a.b.doubleclick.net/"))
    }

    @Test
    fun gluedPrefixNamesAreNotBlocked() {
        // "notdoubleclick.net" is a different registrable name, not
        // a subdomain of doubleclick.net.
        assertFalse(AdBlocker.isAdRequest("https://notdoubleclick.net/"))
        assertFalse(AdBlocker.isAdRequest("https://evil-doubleclick.net/"))
        assertFalse(AdBlocker.isAdRequest("https://fakegoogle-analytics.com/"))
    }

    @Test
    fun listedNameUnderAttackerDomainIsNotBlocked() {
        assertFalse(AdBlocker.isAdRequest("https://doubleclick.net.evil.com/"))
        assertFalse(AdBlocker.isAdRequest("https://ads.doubleclick.net.evil.com/x"))
    }

    @Test
    fun trailingRootDotDoesNotEvade() {
        assertTrue(AdBlocker.isAdRequest("https://doubleclick.net./"))
        assertTrue(AdBlocker.isAdRequest("https://ads.doubleclick.net./x.js"))
    }

    @Test
    fun casePortAndUserinfoDoNotEvade() {
        assertTrue(AdBlocker.isAdRequest("https://ADS.DOUBLECLICK.NET/x"))
        assertTrue(AdBlocker.isAdRequest("https://ads.doubleclick.net:8443/x"))
        assertTrue(AdBlocker.isAdRequest("https://user:pass@ads.doubleclick.net/x"))
    }

    @Test
    fun listedNameInUserinfoDoesNotBlockRealHost() {
        // The host here is evil.com; doubleclick.net is only the
        // userinfo segment and must not trigger a block.
        assertFalse(AdBlocker.isAdRequest("https://doubleclick.net@evil.com/"))
    }

    @Test
    fun ipLiteralsAreNeverBlocked() {
        assertFalse(AdBlocker.isAdRequest("http://127.0.0.1/ads.js"))
        assertFalse(AdBlocker.isAdRequest("http://192.168.0.1/"))
        assertFalse(AdBlocker.isAdRequest("http://[::1]:8080/x"))
        assertFalse(AdBlocker.isAdRequest("http://[2001:db8::1]/x"))
    }

    @Test
    fun malformedOrHostlessUrlsPassWithoutCrashing() {
        assertFalse(AdBlocker.isAdRequest(""))
        assertFalse(AdBlocker.isAdRequest("not a url"))
        assertFalse(AdBlocker.isAdRequest("https://"))
        assertFalse(AdBlocker.isAdRequest("://doubleclick.net"))
        assertFalse(AdBlocker.isAdRequest("/relative/path.js"))
        // No scheme: java.net.URI finds no host, so the request
        // passes — real engine requests always carry a scheme.
        assertFalse(AdBlocker.isAdRequest("doubleclick.net/banner.js"))
        assertFalse(AdBlocker.isAdRequest("about:blank"))
        assertFalse(AdBlocker.isAdRequest("data:text/html,hi"))
    }

    @Test
    fun strictHostsOnlyBlockAtStrictLevel() {
        assertFalse(AdBlocker.isAdRequest("https://coinhive.com/miner.js"))
        assertTrue(AdBlocker.isAdRequest("https://coinhive.com/miner.js", strict = true))
        assertTrue(
            AdBlocker.isAdRequest("https://cdn.coinhive.com/miner.js", strict = true)
        )
        assertTrue(
            AdBlocker.isAdRequest("https://static.hotjar.com/c.js", strict = true)
        )
        assertFalse(AdBlocker.isAdRequest("https://static.hotjar.com/c.js"))
    }

    @Test
    fun strictLevelKeepsBaseListAndBoundaries() {
        assertTrue(AdBlocker.isAdRequest("https://ads.doubleclick.net/", strict = true))
        assertFalse(AdBlocker.isAdRequest("https://notcoinhive.com/", strict = true))
        assertFalse(
            AdBlocker.isAdRequest("https://coinhive.com.evil.com/", strict = true)
        )
        // Withdrawn in the v0.94.0 false-positive review: the
        // vendor's own parent domain must stay reachable even at
        // strict level.
        assertFalse(AdBlocker.isAdRequest("https://hotjar.com/", strict = true))
        assertFalse(AdBlocker.isAdRequest("https://app.hotjar.com/", strict = true))
    }

    @Test
    fun ordinarySitesStayReachableAtBothLevels() {
        assertFalse(AdBlocker.isAdRequest("https://example.com/", strict = true))
        assertFalse(AdBlocker.isAdRequest("https://www.baidu.com/", strict = true))
        assertFalse(AdBlocker.isAdRequest("https://github.com/", strict = true))
    }

    // ---- isBlockedHost: the host-direct entry the WebView request
    // hook uses (v0.171.0). It must agree with isAdRequest on every
    // shape the platform's Uri parser can hand it, because the two
    // entry points now share one matching core.

    @Test
    fun hostEntryMatchesListedApexAndSubdomains() {
        assertTrue(AdBlocker.isBlockedHost("doubleclick.net"))
        assertTrue(AdBlocker.isBlockedHost("ads.doubleclick.net"))
        assertTrue(AdBlocker.isBlockedHost("a.b.doubleclick.net"))
        assertFalse(AdBlocker.isBlockedHost("notdoubleclick.net"))
        assertFalse(AdBlocker.isBlockedHost("doubleclick.net.evil.com"))
        assertFalse(AdBlocker.isBlockedHost("example.com"))
    }

    @Test
    fun hostEntryNormalisesCaseAndTrailingDot() {
        // Uri.getHost() does not promise lowercase, and a fully-
        // qualified host may keep its root dot: both must land on
        // the same verdict as the string entry point.
        assertTrue(AdBlocker.isBlockedHost("ADS.DoubleClick.NET"))
        assertTrue(AdBlocker.isBlockedHost("ads.doubleclick.net."))
        assertTrue(AdBlocker.isBlockedHost("DoubleClick.Net."))
    }

    @Test
    fun hostEntryRejectsNullAndEmpty() {
        assertFalse(AdBlocker.isBlockedHost(null))
        assertFalse(AdBlocker.isBlockedHost(""))
        assertFalse(AdBlocker.isBlockedHost("."))
    }

    @Test
    fun hostEntryHonoursStrictBoundary() {
        assertFalse(AdBlocker.isBlockedHost("cdn.coinhive.com"))
        assertTrue(AdBlocker.isBlockedHost("cdn.coinhive.com", strict = true))
        assertTrue(AdBlocker.isBlockedHost("static.hotjar.com", strict = true))
        assertFalse(AdBlocker.isBlockedHost("hotjar.com", strict = true))
    }

    @Test
    fun hostEntryAgreesWithUrlEntry() {
        // One matching core, two doors: for each pair the verdict
        // must be identical, or the WebView hook and the Gecko
        // navigation check would drift apart.
        val cases = listOf(
            "doubleclick.net" to "https://doubleclick.net/",
            "ads.doubleclick.net" to "https://ads.doubleclick.net/x.js",
            "google-analytics.com" to "https://www.google-analytics.com/g.js",
            "example.com" to "https://example.com/",
            "sub.51.la" to "https://sub.51.la/stat.js"
        )
        for ((host, url) in cases) {
            assertTrue(
                "host/URL verdict drift for $host",
                AdBlocker.isBlockedHost(host) == AdBlocker.isAdRequest(url)
            )
            assertTrue(
                "strict host/URL verdict drift for $host",
                AdBlocker.isBlockedHost(host, strict = true) ==
                    AdBlocker.isAdRequest(url, strict = true)
            )
        }
    }
}
