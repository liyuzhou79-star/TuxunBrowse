package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.bookmarksFromHtml
import com.tuxunbrowse.android.data.compareVersionNames
import com.tuxunbrowse.android.data.isUrlLikeInput
import com.tuxunbrowse.android.data.matchSuggestions
import com.tuxunbrowse.android.data.resolveInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure Store.kt helpers (v0.69.0): the address-bar
 * input resolver, the numeric version compare used by the update
 * check, the suggestion matcher (bookmarks before history) and the
 * Netscape bookmark-file parser. All four are pure functions with no
 * Android dependencies, so they run on the JVM in CI
 * (testDebugUnitTest).
 */
class ResolveInputTest {

    @Test
    fun blankInputGoesHome() {
        assertEquals(HOME_URL, resolveInput(""))
        assertEquals(HOME_URL, resolveInput("   "))
    }

    @Test
    fun fullHttpUrlsPassThroughUnchanged() {
        assertEquals(
            "http://example.com/a?b=1",
            resolveInput("http://example.com/a?b=1")
        )
        assertEquals(
            "https://example.com/a",
            resolveInput("https://example.com/a")
        )
    }

    @Test
    fun bareHostGetsHttpsPrefix() {
        assertEquals("https://example.com", resolveInput("example.com"))
        assertEquals(
            "https://www.example.com/path",
            resolveInput("www.example.com/path")
        )
        assertEquals("https://localhost", resolveInput("localhost"))
        assertEquals(
            "https://localhost:8080/x",
            resolveInput("localhost:8080/x")
        )
    }

    @Test
    fun inputIsTrimmedFirst() {
        assertEquals(
            "https://example.com",
            resolveInput("  example.com  ")
        )
    }

    @Test
    fun plainWordsBecomeBaiduSearchByDefault() {
        // Baidu template "https://www.baidu.com/s?wd=%s", URLEncoder
        // turns the space into '+'.
        assertEquals(
            "https://www.baidu.com/s?wd=hello+world",
            resolveInput("hello world")
        )
        // A single word without a dot is a search too, not a host.
        assertEquals(
            "https://www.baidu.com/s?wd=news",
            resolveInput("news")
        )
    }

    @Test
    fun chosenEngineIsUsedForSearches() {
        assertEquals(
            "https://www.bing.com/search?q=hello+world",
            resolveInput("hello world", SearchEngine.BING)
        )
        assertEquals(
            "https://duckduckgo.com/?q=hello+world",
            resolveInput("hello world", SearchEngine.DUCKDUCKGO)
        )
    }

    @Test
    fun customTemplateIsUsedWhenValid() {
        assertEquals(
            "https://example.org/find?q=hello+world",
            resolveInput(
                "hello world", SearchEngine.CUSTOM,
                "https://example.org/find?q=%s"
            )
        )
    }

    @Test
    fun invalidCustomTemplateFallsBackToBaidu() {
        // No %s placeholder: unusable, must not produce a broken URL.
        assertEquals(
            "https://www.baidu.com/s?wd=hello+world",
            resolveInput(
                "hello world", SearchEngine.CUSTOM,
                "https://example.org/find"
            )
        )
        // Missing template entirely.
        assertEquals(
            "https://www.baidu.com/s?wd=hello+world",
            resolveInput("hello world", SearchEngine.CUSTOM, "")
        )
    }

    @Test
    fun urlLikeMirrorMatchesResolver() {
        assertTrue(isUrlLikeInput("https://example.com"))
        assertTrue(isUrlLikeInput("example.com"))
        assertTrue(isUrlLikeInput("localhost"))
        assertFalse(isUrlLikeInput("hello world"))
        assertFalse(isUrlLikeInput("news"))
        assertFalse(isUrlLikeInput(""))
        assertFalse(isUrlLikeInput("   "))
    }
}

class CompareVersionNamesTest {

    @Test
    fun numericSegmentsNotLexicographic() {
        // A plain string compare would rank "0.9.0" above "0.17.0".
        assertTrue(compareVersionNames("0.9.0", "0.17.0") < 0)
        assertTrue(compareVersionNames("0.17.0", "0.9.0") > 0)
        assertTrue(compareVersionNames("0.68.0", "0.69.0") < 0)
        assertTrue(compareVersionNames("2.0.0", "1.99.99") > 0)
    }

    @Test
    fun equalVersionsCompareZero() {
        assertEquals(0, compareVersionNames("0.69.0", "0.69.0"))
        assertEquals(0, compareVersionNames("1.0.0", "1.0.0"))
    }

    @Test
    fun leadingVIsIgnored() {
        assertEquals(0, compareVersionNames("v0.18.0", "0.18.0"))
        assertEquals(0, compareVersionNames("V1.2.3", "1.2.3"))
        assertTrue(compareVersionNames("v1.0.1", "1.0.0") > 0)
    }

    @Test
    fun missingTrailingSegmentsCountAsZero() {
        assertEquals(0, compareVersionNames("1.2", "1.2.0"))
        assertTrue(compareVersionNames("1.2.1", "1.2") > 0)
        assertTrue(compareVersionNames("1.2", "1.2.1") < 0)
    }

    @Test
    fun dashSuffixIsIgnored() {
        assertEquals(0, compareVersionNames("1.0.0-beta", "1.0.0"))
        assertTrue(compareVersionNames("1.0.1-rc1", "1.0.0") > 0)
    }

    @Test
    fun nonNumericSegmentCountsAsZero() {
        assertEquals(0, compareVersionNames("1.x.0", "1.0.0"))
        assertTrue(compareVersionNames("1.x.1", "1.0.0") > 0)
    }
}

class MatchSuggestionsTest {

    private val bookmarks = listOf(
        Bookmark("GitHub", "https://github.com"),
        Bookmark("新闻首页", "https://news.example.com"),
        Bookmark("Example Blog", "https://blog.example.com")
    )
    private val history = listOf(
        HistoryItem("GitHub Trending", "https://github.com/trending", 3L),
        HistoryItem("Example News", "https://news.example.com/latest", 2L),
        HistoryItem("Unrelated", "https://plain.site/page", 1L)
    )

    @Test
    fun bookmarksComeBeforeHistory() {
        val out = matchSuggestions("example", bookmarks, history)
        assertEquals(
            listOf(
                "https://news.example.com",
                "https://blog.example.com",
                "https://news.example.com/latest"
            ),
            out.map { it.url }
        )
        assertTrue(out[0].bookmarked)
        assertTrue(out[1].bookmarked)
        assertFalse(out[2].bookmarked)
    }

    @Test
    fun matchingIsCaseInsensitiveOnTitleAndUrl() {
        val byTitle = matchSuggestions("github", bookmarks, history)
        assertEquals("https://github.com", byTitle.first().url)
        val byUrl = matchSuggestions("BLOG.EXAMPLE", bookmarks, history)
        assertEquals(
            listOf("https://blog.example.com"),
            byUrl.map { it.url }
        )
    }

    @Test
    fun duplicateUrlsAreKeptOnceAsBookmark() {
        // Same URL in bookmarks and history: one row, flagged as a
        // bookmark (bookmarks are considered first).
        val out = matchSuggestions(
            "github",
            listOf(Bookmark("GitHub", "https://github.com")),
            listOf(HistoryItem("GitHub", "https://github.com", 1L))
        )
        assertEquals(1, out.size)
        assertTrue(out[0].bookmarked)
    }

    @Test
    fun blankQueryReturnsNothing() {
        assertTrue(matchSuggestions("", bookmarks, history).isEmpty())
        assertTrue(matchSuggestions("   ", bookmarks, history).isEmpty())
    }

    @Test
    fun queryIsTrimmed() {
        val out = matchSuggestions("  github  ", bookmarks, history)
        assertEquals("https://github.com", out.first().url)
    }

    @Test
    fun nonHttpUrlsNeverSuggested() {
        val out = matchSuggestions(
            "example",
            listOf(Bookmark("Local file", "file:///sdcard/example.html")),
            listOf(HistoryItem("Home", "tuxun://home", 1L))
        )
        assertTrue(out.isEmpty())
    }

    @Test
    fun limitIsRespected() {
        val many = (1..12).map {
            Bookmark("Site $it", "https://site$it.example.com")
        }
        assertEquals(8, matchSuggestions("example", many, emptyList()).size)
        assertEquals(
            3,
            matchSuggestions("example", many, emptyList(), limit = 3).size
        )
    }

    @Test
    fun blankTitleFallsBackToUrl() {
        val out = matchSuggestions(
            "example",
            listOf(Bookmark("", "https://example.com")),
            emptyList()
        )
        assertEquals("https://example.com", out[0].title)
    }

    @Test
    fun noMatchReturnsEmpty() {
        assertTrue(
            matchSuggestions("zzzz", bookmarks, history).isEmpty()
        )
    }
}

class BookmarksFromHtmlTest {

    @Test
    fun parsesStandardNetscapeEntries() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><A HREF="https://example.com/" ADD_DATE="1700000000">Example</A>
                <DT><A HREF="http://news.example.org/a">News A</A>
            </DL><p>
        """.trimIndent()
        val (list, skipped) = bookmarksFromHtml(html)
        assertEquals(0, skipped)
        assertEquals(2, list.size)
        assertEquals("Example", list[0].title)
        assertEquals("https://example.com/", list[0].url)
        assertEquals("News A", list[1].title)
        assertEquals("http://news.example.org/a", list[1].url)
    }

    @Test
    fun nonHttpEntriesAreSkippedAndCounted() {
        val html = """
            <DL><p>
                <DT><A HREF="file:///etc/passwd">Local</A>
                <DT><A HREF="javascript:alert(1)">Script</A>
                <DT><A HREF="https://ok.example.com/">OK</A>
            </DL><p>
        """.trimIndent()
        val (list, skipped) = bookmarksFromHtml(html)
        assertEquals(2, skipped)
        assertEquals(1, list.size)
        assertEquals("https://ok.example.com/", list[0].url)
    }

    @Test
    fun entitiesInTitlesAndHrefsAreDecoded() {
        val html = """<DT><A HREF="https://example.com/?a=1&amp;b=2">Tom &amp; Jerry &lt;3</A>"""
        val (list, skipped) = bookmarksFromHtml(html)
        assertEquals(0, skipped)
        assertEquals("https://example.com/?a=1&b=2", list[0].url)
        assertEquals("Tom & Jerry <3", list[0].title)
    }

    @Test
    fun innerTagsInTitleAreStripped() {
        val html = """<DT><A HREF="https://example.com/">Bold <b>Title</b></A>"""
        val (list, _) = bookmarksFromHtml(html)
        assertEquals("Bold Title", list[0].title)
    }

    @Test
    fun blankTitleFallsBackToHref() {
        val html = """<DT><A HREF="https://example.com/"></A>"""
        val (list, _) = bookmarksFromHtml(html)
        assertEquals("https://example.com/", list[0].title)
    }

    @Test
    fun lowercaseTagsAndAttributesParse() {
        val html = """<dt><a href="https://lower.example.com/">lower</a>"""
        val (list, skipped) = bookmarksFromHtml(html)
        assertEquals(0, skipped)
        assertEquals(1, list.size)
        assertEquals("lower", list[0].title)
    }

    @Test
    fun emptyAndForeignInputYieldNothing() {
        val (a, ask) = bookmarksFromHtml("")
        assertTrue(a.isEmpty())
        assertEquals(0, ask)
        val (b, bsk) = bookmarksFromHtml("<html><body>no links</body></html>")
        assertTrue(b.isEmpty())
        assertEquals(0, bsk)
    }

    @Test
    fun parsedBookmarksAreUnpinned() {
        val html = """<DT><A HREF="https://example.com/">Example</A>"""
        val (list, _) = bookmarksFromHtml(html)
        assertFalse(list[0].pinned)
    }
}
