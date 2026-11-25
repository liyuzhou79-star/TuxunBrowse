package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.hasBlockedPageScheme
import com.tuxunbrowse.android.data.hostMatchesSite
import com.tuxunbrowse.android.data.hostOf
import com.tuxunbrowse.android.data.isCompatHost
import com.tuxunbrowse.android.data.isDesktopHost
import com.tuxunbrowse.android.data.isUrlLikeInput
import com.tuxunbrowse.android.data.matchSuggestions
import com.tuxunbrowse.android.data.resolveInput
import com.tuxunbrowse.android.engine.rfc5987FileName
import com.tuxunbrowse.android.engine.sanitizeDownloadFileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Boundary tests for the high-risk pure decision functions
 * (v0.114.0 core pure-function audit). PureFunctionsTest already
 * pins the everyday cases; this file pins the edges it did not
 * cover: ports / IPs / CJK domains / local suffixes in the input
 * resolver, the resolver-vs-mirror consistency sweep, the blocked-
 * scheme gate the resolver relies on, host parsing (userinfo,
 * IPv6, trailing dot), per-site host matching against suffix
 * spoofing, suggestion-matcher limits and de-duplication inside a
 * single source, and the download-name caps at their exact byte
 * boundaries.
 */
class ResolveInputBoundaryTest {

    @Test
    fun bareHostWithPortPathAndQueryGetsHttpsPrefix() {
        assertEquals(
            "https://example.com:8080",
            resolveInput("example.com:8080")
        )
        assertEquals(
            "https://example.com:8080/a/b?q=1&r=2#frag",
            resolveInput("example.com:8080/a/b?q=1&r=2#frag")
        )
    }

    @Test
    fun ipv4LiteralsAreHosts() {
        assertEquals("https://192.168.1.1", resolveInput("192.168.1.1"))
        assertEquals(
            "https://192.168.1.1:8080/admin",
            resolveInput("192.168.1.1:8080/admin")
        )
        assertEquals("https://127.0.0.1:9000", resolveInput("127.0.0.1:9000"))
    }

    @Test
    fun chineseDomainsAreHosts() {
        assertEquals("https://例子.中国", resolveInput("例子.中国"))
        assertEquals("https://中文.com/路径", resolveInput("中文.com/路径"))
    }

    @Test
    fun localSuffixHostsGetHttpsPrefix() {
        assertEquals("https://router.local", resolveInput("router.local"))
        assertEquals(
            "https://nas.home.lan/files",
            resolveInput("nas.home.lan/files")
        )
    }

    @Test
    fun singleLabelWithoutDotIsStillASearch() {
        // Only "localhost" is special-cased; any other dotless word
        // is a search, even an intranet-sounding one.
        assertEquals(
            SearchEngine.BAIDU.searchUrl("intranet"),
            resolveInput("intranet")
        )
        assertEquals(
            SearchEngine.BAIDU.searchUrl("router"),
            resolveInput("router")
        )
    }

    @Test
    fun uppercaseSchemePassesThroughUnchanged() {
        // v0.114.0 fix: the scheme check used to be case-sensitive,
        // so this input was mangled into "https://HTTP://…".
        assertEquals(
            "HTTP://example.com/a",
            resolveInput("HTTP://example.com/a")
        )
        assertEquals(
            "HTTPS://EXAMPLE.COM",
            resolveInput("HTTPS://EXAMPLE.COM")
        )
        assertEquals(
            "Http://example.com",
            resolveInput("Http://example.com")
        )
        assertTrue(isUrlLikeInput("HTTP://example.com/a"))
    }

    @Test
    fun uppercaseLocalhostIsAHost() {
        // Same v0.114.0 fix: the localhost check follows the same
        // case-insensitive rule in both mirror functions.
        assertEquals("https://LOCALHOST", resolveInput("LOCALHOST"))
        assertEquals(
            "https://Localhost:8080/x",
            resolveInput("Localhost:8080/x")
        )
        assertTrue(isUrlLikeInput("LOCALHOST:8080"))
    }

    @Test
    fun trailingDotHostIsKeptAsTyped() {
        assertEquals("https://example.com.", resolveInput("example.com."))
        assertEquals(
            "https://user@example.com",
            resolveInput("user@example.com")
        )
    }

    @Test
    fun wordsWithSpaceAreSearches() {
        assertEquals(
            SearchEngine.BAIDU.searchUrl("a b.com"),
            resolveInput("a b.com")
        )
    }

    @Test
    fun tabsAndNewlinesAreRemovedBeforeResolving() {
        // v0.138.0: tabs / CR / LF are stripped up front (the URL
        // standard's own rule), so a URL copied from a wrapped line
        // still resolves as that URL instead of a broken host or a
        // search. The mirror function applies the same removal.
        assertEquals(
            "https://example.com/path",
            resolveInput("https://exa\nmple.com/path")
        )
        assertEquals(
            "https://example.com",
            resolveInput("example.com\r\n")
        )
        assertEquals(
            "https://example.com",
            resolveInput("exa\tmple.com")
        )
        assertEquals(
            SearchEngine.BAIDU.searchUrl("helloworld"),
            resolveInput("hello\tworld")
        )
        assertEquals(
            SearchEngine.BAIDU.searchUrl("two words"),
            resolveInput("two\n words")
        )
        assertTrue(isUrlLikeInput("example.com\n"))
        assertTrue(isUrlLikeInput("https://exa\nmple.com"))
        assertFalse(isUrlLikeInput("two\nlines of text"))
    }

    @Test
    fun blockedSchemesAreRefusedAtTheGate() {
        // Every caller refuses these BEFORE resolveInput runs; the
        // gate itself is case-insensitive and trims first.
        assertTrue(hasBlockedPageScheme("javascript:alert(1)"))
        assertTrue(hasBlockedPageScheme("JavaScript:alert(1)"))
        assertTrue(hasBlockedPageScheme("  javascript:alert(1)"))
        assertTrue(hasBlockedPageScheme("data:text/html;base64,AAAA"))
        assertTrue(hasBlockedPageScheme("file:///sdcard/x.html"))
        assertTrue(hasBlockedPageScheme("content://com.example/x"))
        assertTrue(hasBlockedPageScheme("intent://example.com/#Intent;end"))
        assertTrue(hasBlockedPageScheme("about:blank"))
        assertTrue(hasBlockedPageScheme("view-source:https://example.com"))
        assertTrue(hasBlockedPageScheme("blob:https://example.com/1"))
    }

    @Test
    fun blockedSchemeLookalikesAreNotRefused() {
        // The gate matches the scheme WITH its colon, so ordinary
        // hosts and words that merely start with the same letters
        // pass through to the resolver.
        assertFalse(hasBlockedPageScheme("database.com"))
        assertFalse(hasBlockedPageScheme("files.example.com"))
        assertFalse(hasBlockedPageScheme("content.example.com"))
        assertFalse(hasBlockedPageScheme("javascript tutorial"))
        assertFalse(hasBlockedPageScheme("example.com"))
    }

    @Test
    fun resolverNeverEmitsABlockedScheme() {
        // Defense in depth: even without the gate, resolveInput's
        // output for blocked-scheme text is a search or an
        // https-prefixed string — never the scheme itself. (The
        // gate is still what refuses file: text with a plain
        // message instead of this reinterpretation.)
        for (raw in listOf(
            "javascript:alert(document.domain)",
            "data:text/html;base64,PHNjcmlwdD4=",
            "file:///sdcard/secret.html"
        )) {
            val out = resolveInput(raw)
            assertFalse(out.startsWith("javascript:"))
            assertFalse(out.startsWith("data:"))
            assertFalse(out.startsWith("file:"))
            assertTrue(hasBlockedPageScheme(raw))
        }
    }

    @Test
    fun urlLikeMirrorAgreesWithResolverAcrossCorpus() {
        // The historical risk is the two functions drifting apart:
        // for every corpus input, isUrlLikeInput must say "URL"
        // exactly when resolveInput produces a URL (not the home
        // page and not a search URL).
        val corpus = listOf(
            "", "   ", "example.com", "example.com:8080/a?q=1",
            "192.168.0.1", "127.0.0.1:9000", "[::1]", "localhost",
            "LOCALHOST:8080", "HTTP://a.com", "https://a.com/x",
            "hello world", "hello.world", "a b.com", "例子.中国",
            "news", "intranet", "example.com.", ".com",
            "user@example.com", "javascript:alert(1)",
            "file:///a.html", "router.local", "a.b c", "中文.com",
            "example.com\n", "https://exa\nmple.com"
        )
        for (raw in corpus) {
            val resolved = resolveInput(raw)
            val resolverSaysUrl = resolved != HOME_URL &&
                resolved != SearchEngine.BAIDU.searchUrl(raw.trim())
            assertEquals(
                "mirror drift for input: $raw",
                resolverSaysUrl,
                isUrlLikeInput(raw)
            )
        }
    }
}

class HostOfBoundaryTest {

    @Test
    fun userInfoAndPortAreStripped() {
        assertEquals(
            "example.com",
            hostOf("https://user:pass@example.com:8080/a?b=1")
        )
        assertEquals(
            "example.com",
            hostOf("https://user@example.com/")
        )
    }

    @Test
    fun hostIsLowercased() {
        assertEquals("example.com", hostOf("https://EXAMPLE.COM/Path"))
        assertEquals(
            "www.example.com",
            hostOf("http://WWW.Example.COM:80/")
        )
    }

    @Test
    fun ipv6LiteralHostIsReturned() {
        val host = hostOf("http://[::1]:8080/")
        assertTrue(host != null && host.contains("::1"))
        val host6 = hostOf("https://[2001:db8::1]/a")
        assertTrue(host6 != null && host6.contains("2001:db8::1"))
    }

    @Test
    fun trailingDotIsKept() {
        // java.net.URI keeps the trailing dot and hostOf does not
        // strip it; per-site matching therefore treats the dotted
        // form as a different host (pinned in CompatHostBoundary).
        assertEquals("example.com.", hostOf("https://example.com./"))
    }

    @Test
    fun schemelessRelativeAndBlankHaveNoHost() {
        assertNull(hostOf("example.com/path"))
        assertNull(hostOf("/just/a/path"))
        assertNull(hostOf(""))
    }

    @Test
    fun homeUrlHostIsHome() {
        assertEquals("home", hostOf(HOME_URL))
    }

    @Test
    fun malformedUrlsReturnNull() {
        assertNull(hostOf("https://exa mple.com/"))
        assertNull(hostOf("http://[::1"))
    }
}

class CompatHostBoundaryTest {

    private val sites = setOf("example.com", "bank.cn")

    @Test
    fun exactAndSubdomainHostsMatch() {
        assertTrue(isCompatHost("example.com", sites))
        assertTrue(isCompatHost("www.example.com", sites))
        assertTrue(isCompatHost("a.b.example.com", sites))
        assertTrue(isCompatHost("m.bank.cn", sites))
    }

    @Test
    fun suffixSpoofingDoesNotMatch() {
        // The dot boundary in hostMatchesSite is what stops these.
        assertFalse(isCompatHost("evil-example.com", sites))
        assertFalse(isCompatHost("notexample.com", sites))
        assertFalse(isCompatHost("example.com.evil.com", sites))
        assertFalse(isCompatHost("examplexcom", sites))
        assertFalse(isCompatHost("fakebank.cn", sites))
    }

    @Test
    fun nullBlankAndEmptyInputsNeverMatch() {
        assertFalse(isCompatHost(null, sites))
        assertFalse(isCompatHost("", sites))
        assertFalse(isCompatHost("example.com", emptySet()))
        assertFalse(hostMatchesSite("example.com", ""))
        assertFalse(hostMatchesSite(null, "example.com"))
    }

    @Test
    fun trailingDotHostDoesNotMatch() {
        // Pinned current rule: hostOf keeps the trailing dot, so
        // the dotted form does not match the remembered site.
        assertFalse(isCompatHost("example.com.", sites))
    }

    @Test
    fun desktopHostUsesTheSameRule() {
        assertTrue(isDesktopHost("www.example.com", sites))
        assertTrue(isDesktopHost("example.com", sites))
        assertFalse(isDesktopHost("evil-example.com", sites))
        assertFalse(isDesktopHost(null, sites))
    }
}

class MatchSuggestionsBoundaryTest {

    @Test
    fun zeroLimitReturnsNothing() {
        val out = matchSuggestions(
            "example",
            listOf(Bookmark("Example", "https://example.com")),
            emptyList(),
            limit = 0
        )
        assertTrue(out.isEmpty())
    }

    @Test
    fun duplicatesWithinBookmarksAreKeptOnce() {
        val out = matchSuggestions(
            "example",
            listOf(
                Bookmark("Example", "https://example.com"),
                Bookmark("Example Again", "https://example.com")
            ),
            emptyList()
        )
        assertEquals(1, out.size)
        assertEquals("Example", out[0].title)
    }

    @Test
    fun duplicatesWithinHistoryAreKeptOnce() {
        val out = matchSuggestions(
            "example",
            emptyList(),
            listOf(
                HistoryItem("Example", "https://example.com", 2L),
                HistoryItem("Example", "https://example.com", 1L)
            )
        )
        assertEquals(1, out.size)
        assertFalse(out[0].bookmarked)
    }

    @Test
    fun historyBlankTitleFallsBackToUrl() {
        val out = matchSuggestions(
            "example",
            emptyList(),
            listOf(HistoryItem("", "https://example.com/page", 1L))
        )
        assertEquals("https://example.com/page", out[0].title)
    }

    @Test
    fun queryCanMatchHistoryTitleOnly() {
        val out = matchSuggestions(
            "trending",
            emptyList(),
            listOf(
                HistoryItem("Trending Now", "https://site.test/a", 1L)
            )
        )
        assertEquals(listOf("https://site.test/a"), out.map { it.url })
    }

    @Test
    fun uppercaseSchemeUrlsAreNotSuggested() {
        // Pinned current rule: the http gate is a literal lowercase
        // prefix check. Stored bookmark/history URLs always carry a
        // lowercase scheme (the resolver writes them), so this only
        // affects hand-corrupted stored data.
        val out = matchSuggestions(
            "example",
            listOf(Bookmark("Example", "HTTPS://EXAMPLE.COM")),
            emptyList()
        )
        assertTrue(out.isEmpty())
    }
}

class DownloadNameBoundaryTest {

    @Test
    fun sanitizePureExtensionLosesItsDot() {
        // Leading dots are trimmed, so a "name" that is only an
        // extension degrades to the bare extension text; the
        // resolver's blank/fallback logic never sees a dot-file.
        assertEquals("apk", sanitizeDownloadFileName(".apk"))
        assertEquals("hidden.txt", sanitizeDownloadFileName(".hidden.txt"))
    }

    @Test
    fun sanitizeTrailingDotAndEmptyTailSegments() {
        assertEquals("file", sanitizeDownloadFileName("file."))
        assertEquals("", sanitizeDownloadFileName("a/b/c/"))
        assertEquals("", sanitizeDownloadFileName("dir\\"))
    }

    @Test
    fun sanitizeMultiDotLongNameKeepsOnlyLastExtension() {
        val out = sanitizeDownloadFileName("a".repeat(150) + ".tar.gz")
        assertEquals(120, out.length)
        assertTrue(out.endsWith(".gz"))
        assertFalse(out.contains(".tar"))
    }

    @Test
    fun sanitizeOverlongExtensionIsNotKept() {
        // An "extension" longer than 10 characters is not treated
        // as one: the name is cut plain at the character cap.
        val out = sanitizeDownloadFileName(
            "b".repeat(130) + "." + "x".repeat(15)
        )
        assertEquals(120, out.length)
        assertEquals("b".repeat(120), out)
    }

    @Test
    fun sanitizeByteCapBoundaryIsExactForCjk() {
        // 65 CJK chars * 3 bytes + ".apk" = 199 bytes: just inside
        // the 200-byte cap, passes through unchanged.
        val inside = "图".repeat(65) + ".apk"
        assertEquals(inside, sanitizeDownloadFileName(inside))
        // 70 CJK chars = 214 bytes: cut back to the same 65 chars.
        val out = sanitizeDownloadFileName("图".repeat(70) + ".apk")
        assertEquals(inside, out)
        assertEquals(199, out.toByteArray(Charsets.UTF_8).size)
    }

    @Test
    fun sanitizeByteCapWithoutExtension() {
        val out = sanitizeDownloadFileName("图".repeat(80))
        assertEquals("图".repeat(66), out)
        assertEquals(198, out.toByteArray(Charsets.UTF_8).size)
    }

    @Test
    fun sanitizeExactlyAtCharCapIsUnchanged() {
        val atCap = "a".repeat(116) + ".apk"
        assertEquals(120, atCap.length)
        assertEquals(atCap, sanitizeDownloadFileName(atCap))
    }

    @Test
    fun rfc5987DecodesIso88591Charset() {
        val cd = "attachment; filename*=ISO-8859-1''caf%E9.txt"
        assertEquals("café.txt", rfc5987FileName(cd))
    }

    @Test
    fun rfc5987SkipsLanguageTag() {
        val cd = "attachment; filename*=UTF-8'en'%E2%82%ACrates.txt"
        assertEquals("€rates.txt", rfc5987FileName(cd))
    }

    @Test
    fun rfc5987UnknownCharsetReturnsNull() {
        assertNull(
            rfc5987FileName("attachment; filename*=X-BOGUS''abc.txt")
        )
    }

    @Test
    fun rfc5987MalformedPercentSequencesPassThroughLiterally() {
        assertEquals(
            "%ZZ.apk",
            rfc5987FileName("attachment; filename*=UTF-8''%ZZ.apk")
        )
        assertEquals(
            "abc%2",
            rfc5987FileName("attachment; filename*=UTF-8''abc%2")
        )
    }

    @Test
    fun rfc5987EncodedSlashIsStrippedBySanitizer() {
        // The RFC 5987 layer decodes %2F to a real slash; the
        // sanitizer is the layer that must strip the path part.
        val decoded = rfc5987FileName(
            "attachment; filename*=UTF-8''%2Ftmp%2Fevil.apk"
        )
        assertEquals("/tmp/evil.apk", decoded)
        assertEquals("evil.apk", sanitizeDownloadFileName(decoded))
    }
}
