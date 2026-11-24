package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.SETTINGS_SEARCH_INDEX
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.SettingsIndexEntry
import com.tuxunbrowse.android.data.allBookmarkFolders
import com.tuxunbrowse.android.data.backupToJson
import com.tuxunbrowse.android.data.BackupSettings
import com.tuxunbrowse.android.data.bookmarksFromJson
import com.tuxunbrowse.android.data.bookmarksInFolder
import com.tuxunbrowse.android.data.bookmarksToJson
import com.tuxunbrowse.android.data.clearBookmarksFolder
import com.tuxunbrowse.android.data.derivedBookmarkFolders
import com.tuxunbrowse.android.data.filterSettingsIndex
import com.tuxunbrowse.android.data.historyRestoreIndex
import com.tuxunbrowse.android.data.normalizeBookmarkFolderName
import com.tuxunbrowse.android.data.parseBackup
import com.tuxunbrowse.android.data.renameBookmarksFolder
import com.tuxunbrowse.android.data.shouldPreheatGecko
import com.tuxunbrowse.android.data.blockedDayKey
import com.tuxunbrowse.android.data.bookmarksFromHtml
import com.tuxunbrowse.android.data.compareVersionNames
import com.tuxunbrowse.android.data.pruneBlockedDaily
import com.tuxunbrowse.android.data.recentBlockedSeries
import com.tuxunbrowse.android.data.extractFirstWebUrl
import com.tuxunbrowse.android.data.DiagnosticInfo
import com.tuxunbrowse.android.data.GECKOVIEW_VERSION
import com.tuxunbrowse.android.data.diagnosticText
import com.tuxunbrowse.android.data.displayHostFor
import com.tuxunbrowse.android.data.downgradeToHttp
import com.tuxunbrowse.android.data.upgradeToHttps
import com.tuxunbrowse.android.engine.AdBlocker
import com.tuxunbrowse.android.data.isReaderHost
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

/**
 * Per-day blocked-count helpers (v0.70.0): the ISO day key, the
 * 31-day pruning window and the zero-filled last-7-days series the
 * settings page renders.
 */
class BlockedDailyTest {

    private val today: java.time.LocalDate =
        java.time.LocalDate.of(2026, 11, 24)

    @Test
    fun dayKeyIsIsoDate() {
        assertEquals("2026-11-24", blockedDayKey(today))
        assertEquals(
            "2026-01-05",
            blockedDayKey(java.time.LocalDate.of(2026, 1, 5))
        )
    }

    @Test
    fun pruneKeepsTodayAndThirtyDaysBack() {
        val counts = mapOf(
            "2026-11-24" to 3L, // today
            "2026-10-26" to 2L, // 29 days back
            "2026-10-25" to 1L // 30 days back: last kept day
        )
        val pruned = pruneBlockedDaily(counts, today)
        assertEquals(counts, pruned)
    }

    @Test
    fun pruneDropsDayThirtyOneAndOlder() {
        val counts = mapOf(
            "2026-11-24" to 3L,
            "2026-10-24" to 9L, // 31 days back: outside the window
            "2025-01-01" to 9L
        )
        val pruned = pruneBlockedDaily(counts, today)
        assertEquals(mapOf("2026-11-24" to 3L), pruned)
    }

    @Test
    fun pruneDropsUnparseableAndFutureKeys() {
        val counts = mapOf(
            "2026-11-24" to 3L,
            "not-a-date" to 5L,
            "2026-12-01" to 5L // future relative to today
        )
        val pruned = pruneBlockedDaily(counts, today)
        assertEquals(mapOf("2026-11-24" to 3L), pruned)
    }

    @Test
    fun pruneHonoursCustomWindow() {
        val counts = mapOf(
            "2026-11-24" to 1L,
            "2026-11-18" to 1L, // 6 days back
            "2026-11-17" to 1L // 7 days back
        )
        val pruned = pruneBlockedDaily(counts, today, keepDays = 7)
        assertEquals(
            mapOf("2026-11-24" to 1L, "2026-11-18" to 1L),
            pruned
        )
    }

    @Test
    fun seriesIsSevenDaysOldestFirstEndingToday() {
        val series = recentBlockedSeries(emptyMap(), today)
        assertEquals(7, series.size)
        assertEquals("2026-11-18", series.first().first)
        assertEquals("2026-11-24", series.last().first)
    }

    @Test
    fun seriesZeroFillsMissingDaysAndCarriesCounts() {
        val counts = mapOf(
            "2026-11-24" to 7L,
            "2026-11-20" to 2L,
            "2026-11-10" to 99L // outside the 7-day window: ignored
        )
        val series = recentBlockedSeries(counts, today)
        assertEquals(
            listOf(0L, 0L, 2L, 0L, 0L, 0L, 7L),
            series.map { it.second }
        )
    }

    @Test
    fun seriesCrossesMonthBoundary() {
        val marchFirst = java.time.LocalDate.of(2026, 3, 1)
        val series = recentBlockedSeries(
            mapOf("2026-02-28" to 4L), marchFirst
        )
        assertEquals("2026-02-23", series.first().first)
        assertEquals("2026-03-01", series.last().first)
        assertEquals(4L, series[5].second)
    }
}

/** Gecko warm-up decision (v0.71.0): preheat iff either engine is Firefox. */
class ShouldPreheatGeckoTest {

    @Test
    fun bothFirefoxPreheats() {
        assertTrue(shouldPreheatGecko(Engine.GECKO, Engine.GECKO))
    }

    @Test
    fun mainFirefoxOnlyPreheats() {
        assertTrue(shouldPreheatGecko(Engine.GECKO, Engine.WEBVIEW))
    }

    @Test
    fun incognitoFirefoxOnlyPreheats() {
        assertTrue(shouldPreheatGecko(Engine.WEBVIEW, Engine.GECKO))
    }

    @Test
    fun bothWebViewNeverPreheats() {
        assertFalse(shouldPreheatGecko(Engine.WEBVIEW, Engine.WEBVIEW))
    }
}

/** History swipe-delete undo slot (v0.71.0): restore by timestamp. */
class HistoryRestoreIndexTest {

    private fun item(time: Long) =
        HistoryItem("t$time", "https://e$time.example/", time)

    private val list = listOf(item(300), item(200), item(100))

    @Test
    fun emptyListRestoresAtZero() {
        assertEquals(0, historyRestoreIndex(emptyList(), item(50)))
    }

    @Test
    fun newerThanAllGoesFirst() {
        assertEquals(0, historyRestoreIndex(list, item(400)))
    }

    @Test
    fun olderThanAllGoesLast() {
        assertEquals(3, historyRestoreIndex(list, item(50)))
    }

    @Test
    fun middleItemSlotsByTime() {
        assertEquals(1, historyRestoreIndex(list, item(250)))
        assertEquals(2, historyRestoreIndex(list, item(150)))
    }

    @Test
    fun equalTimeGoesBeforeTheExistingEqualEntry() {
        assertEquals(1, historyRestoreIndex(list, item(200)))
    }

    @Test
    fun restoredListStaysNewestFirst() {
        val restored = list.toMutableList().apply {
            val it = item(250)
            add(historyRestoreIndex(list, it), it)
        }
        assertEquals(
            listOf(300L, 250L, 200L, 100L),
            restored.map { it.time }
        )
    }
}

/** Settings-home search index matching (v0.71.0). */
class FilterSettingsIndexTest {

    @Test
    fun blankQueryMatchesNothing() {
        assertTrue(filterSettingsIndex(SETTINGS_SEARCH_INDEX, "").isEmpty())
        assertTrue(filterSettingsIndex(SETTINGS_SEARCH_INDEX, "   ").isEmpty())
    }

    @Test
    fun queryIsTrimmedBeforeMatching() {
        assertEquals(
            filterSettingsIndex(SETTINGS_SEARCH_INDEX, "深色"),
            filterSettingsIndex(SETTINGS_SEARCH_INDEX, "  深色  ")
        )
    }

    @Test
    fun darkMatchesAcrossThreePages() {
        val names = filterSettingsIndex(SETTINGS_SEARCH_INDEX, "深色")
            .map { it.name }
        assertEquals(
            listOf("界面主题 · 深色", "网页深色模式", "阅读背景 · 深色"),
            names
        )
    }

    @Test
    fun matchingIsCaseInsensitive() {
        val lower = filterSettingsIndex(SETTINGS_SEARCH_INDEX, "google")
        val upper = filterSettingsIndex(SETTINGS_SEARCH_INDEX, "GOOGLE")
        assertEquals(lower, upper)
        assertEquals(listOf("Google"), lower.map { it.name })
    }

    @Test
    fun noMatchGivesEmpty() {
        assertTrue(
            filterSettingsIndex(SETTINGS_SEARCH_INDEX, "不存在的设置项").isEmpty()
        )
    }

    @Test
    fun customIndexKeepsEntryAndPageIntact() {
        val index = listOf(
            SettingsIndexEntry("甲选项", "engine", "内核与搜索引擎"),
            SettingsIndexEntry("乙选项", "about", "关于")
        )
        val hits = filterSettingsIndex(index, "甲")
        assertEquals(1, hits.size)
        assertEquals("engine", hits[0].page)
        assertEquals("内核与搜索引擎", hits[0].pageLabel)
    }

    @Test
    fun indexCoversAllSixPagesWithValidKeys() {
        val pages = SETTINGS_SEARCH_INDEX.map { it.page }.toSet()
        assertEquals(
            setOf("engine", "appearance", "privacy", "download", "adblock", "about"),
            pages
        )
    }
}

/**
 * Bookmark folder logic (v0.72.0): derivation, the stored+derived
 * union, the page filter, rename mapping and delete-folder
 * fallback. All pure list transforms.
 */
class BookmarkFolderLogicTest {

    private fun bm(url: String, folder: String = "") =
        Bookmark("t-$url", url, folder = folder)

    @Test
    fun derivedFoldersSkipUncategorizedAndDeduplicate() {
        val list = listOf(
            bm("https://a.example", "工作"),
            bm("https://b.example"),
            bm("https://c.example", "工作"),
            bm("https://d.example", "稍后看")
        )
        assertEquals(listOf("工作", "稍后看"), derivedBookmarkFolders(list))
    }

    @Test
    fun unionKeepsStoredOrderThenDerivedOnlyNames() {
        // "空夹" holds no bookmarks: it survives only via the stored
        // list. "工作" is in both: it keeps its stored position.
        val list = listOf(
            bm("https://a.example", "工作"),
            bm("https://b.example", "新夹")
        )
        assertEquals(
            listOf("空夹", "工作", "新夹"),
            allBookmarkFolders(list, listOf("空夹", "工作"))
        )
    }

    @Test
    fun unionTrimsAndDropsBlankNames() {
        assertEquals(
            listOf("甲"),
            allBookmarkFolders(emptyList(), listOf("  甲 ", "", "甲"))
        )
        assertEquals("甲", normalizeBookmarkFolderName("  甲  "))
    }

    @Test
    fun filterNullMeansAllEmptyMeansUncategorized() {
        val list = listOf(
            bm("https://a.example", "工作"),
            bm("https://b.example"),
            bm("https://c.example", "工作")
        )
        assertEquals(list, bookmarksInFolder(list, null))
        assertEquals(
            listOf("https://b.example"),
            bookmarksInFolder(list, "").map { it.url }
        )
        assertEquals(
            listOf("https://a.example", "https://c.example"),
            bookmarksInFolder(list, "工作").map { it.url }
        )
        assertTrue(bookmarksInFolder(list, "不存在的夹").isEmpty())
    }

    @Test
    fun renameMovesEveryBookmarkInTheFolderOnly() {
        val list = listOf(
            bm("https://a.example", "工作"),
            bm("https://b.example", "生活"),
            bm("https://c.example", "工作")
        )
        val renamed = renameBookmarksFolder(list, "工作", "公司")
        assertEquals(
            listOf("公司", "生活", "公司"),
            renamed.map { it.folder }
        )
        // Input list is never mutated.
        assertEquals("工作", list[0].folder)
    }

    @Test
    fun deleteFolderFallsBackToUncategorizedAndKeepsBookmarks() {
        val list = listOf(
            bm("https://a.example", "工作"),
            bm("https://b.example", "工作"),
            bm("https://c.example")
        )
        val cleared = clearBookmarksFolder(list, "工作")
        assertEquals(3, cleared.size)
        assertTrue(cleared.all { it.folder.isEmpty() || it.url == "https://c.example" })
        assertEquals("", cleared[0].folder)
        assertEquals("", cleared[1].folder)
    }
}

/**
 * Bookmark folder JSON compatibility (v0.72.0) — the highest-risk
 * surface of the release: an old user's archive, export file or
 * backup has NO "folder" key anywhere, and every one of those
 * files must still load with every bookmark landing in
 * uncategorized and nothing dropped. Runs on the real org.json
 * (test-only dependency), not the android.jar stubs.
 */
class BookmarkFolderJsonCompatTest {

    @Test
    fun oldStoredShapeWithoutFolderParsesAsUncategorized() {
        // Exactly what bookmarksFromJson accepted before v0.72.0.
        val raw = """
            {"app":"TuxunBrowse","type":"bookmarks","items":[
              {"title":"甲站","url":"https://a.example","pinned":true},
              {"title":"乙站","url":"https://b.example","pinned":false}
            ]}
        """.trimIndent()
        val parsed = bookmarksFromJson(raw)
        assertEquals(2, parsed.size)
        assertEquals("", parsed[0].folder)
        assertEquals("", parsed[1].folder)
        assertTrue(parsed[0].pinned)
        assertEquals("甲站", parsed[0].title)
    }

    @Test
    fun bareArrayExportWithoutFolderAlsoParses() {
        val parsed = bookmarksFromJson(
            """[{"title":"甲","url":"https://a.example"}]"""
        )
        assertEquals(1, parsed.size)
        assertEquals("", parsed[0].folder)
    }

    @Test
    fun folderSurvivesExportRoundTrip() {
        val list = listOf(
            Bookmark("甲站", "https://a.example", pinned = true, folder = "工作"),
            Bookmark("乙站", "https://b.example")
        )
        val back = bookmarksFromJson(bookmarksToJson(list))
        assertEquals(list, back)
    }

    @Test
    fun oldBackupWithoutAnyFolderDataImportsClean() {
        // A backup in the exact pre-v0.72.0 shape: no "folder" on
        // the bookmarks, no "bookmarkFolders" key at all.
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[
               {"title":"甲站","url":"https://a.example","pinned":false},
               {"title":"乙站","url":"https://b.example","pinned":true}
             ],
             "history":[],"readingList":[],"speedDials":[],
             "compatSites":[],"desktopSites":[],"settings":{}}
        """.trimIndent()
        val data = parseBackup(raw)
        assertTrue(data != null)
        assertEquals(2, data!!.bookmarks.size)
        assertTrue(data.bookmarks.all { it.folder.isEmpty() })
        assertTrue(data.bookmarkFolders.isEmpty())
        assertEquals("乙站", data.bookmarks[1].title)
        assertTrue(data.bookmarks[1].pinned)
    }

    @Test
    fun newBackupRoundTripsFoldersAndEmptyFolderNames() {
        val list = listOf(
            Bookmark("甲站", "https://a.example", folder = "工作"),
            Bookmark("乙站", "https://b.example")
        )
        val json = backupToJson(
            list, emptyList(), emptyList(), emptyList(),
            BackupSettings(),
            bookmarkFolders = listOf("工作", "空夹")
        )
        val data = parseBackup(json)
        assertTrue(data != null)
        assertEquals("工作", data!!.bookmarks[0].folder)
        assertEquals("", data.bookmarks[1].folder)
        // The empty folder survives the round trip via the
        // top-level name list even though no bookmark derives it.
        assertEquals(listOf("工作", "空夹"), data.bookmarkFolders)
    }

    @Test
    fun backupFolderNamesAreSanitizedOnParse() {
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[],
             "bookmarkFolders":["  正常  ","","正常"]}
        """.trimIndent()
        val data = parseBackup(raw)
        assertTrue(data != null)
        assertEquals(listOf("正常"), data!!.bookmarkFolders)
    }
}

/**
 * v0.73.0: extracting the first web URL out of shared text. The
 * share dialog's three choices all hinge on this — a found URL is
 * opened directly / offered for the reading list, no URL means the
 * text is treated as words to search for.
 */
class ExtractFirstWebUrlTest {

    @Test
    fun nullBlankAndPlainTextHaveNoUrl() {
        assertEquals(null, extractFirstWebUrl(null))
        assertEquals(null, extractFirstWebUrl(""))
        assertEquals(null, extractFirstWebUrl("   "))
        assertEquals(null, extractFirstWebUrl("只是一段没有网址的文字"))
        assertEquals(null, extractFirstWebUrl("看看 example.com 这个站"))
    }

    @Test
    fun bareUrlIsExtractedAsIs() {
        assertEquals(
            "https://example.com/a?b=1",
            extractFirstWebUrl("https://example.com/a?b=1")
        )
        assertEquals(
            "http://example.com",
            extractFirstWebUrl("http://example.com")
        )
    }

    @Test
    fun urlBuriedInBlurbIsFound() {
        assertEquals(
            "https://news.example/story/42",
            extractFirstWebUrl("这篇文章不错：正文摘要…… https://news.example/story/42 快看")
        )
    }

    @Test
    fun firstUrlWinsWhenSeveralAreShared() {
        assertEquals(
            "https://first.example/1",
            extractFirstWebUrl("https://first.example/1 和 https://second.example/2")
        )
    }

    @Test
    fun gluedOnPunctuationIsTrimmed() {
        assertEquals(
            "https://example.com/x",
            extractFirstWebUrl("链接是这个（https://example.com/x）。")
        )
        assertEquals(
            "https://example.com/y",
            extractFirstWebUrl("看这里: https://example.com/y, 很好")
        )
    }

    @Test
    fun schemeOnlyIsNotAUrl() {
        assertEquals(null, extractFirstWebUrl("https:// 后面什么都没有"))
    }
}

/**
 * v0.73.0: the per-site reader-mode memory matches hosts with the
 * same rule as the compat/desktop lists — the site itself and its
 * subdomains match, unrelated and parent hosts do not.
 */
class ReaderHostMatchTest {

    private val sites = setOf("example.com", "news.site.cn")

    @Test
    fun exactAndSubdomainMatch() {
        assertTrue(isReaderHost("example.com", sites))
        assertTrue(isReaderHost("www.example.com", sites))
        assertTrue(isReaderHost("a.b.example.com", sites))
        assertTrue(isReaderHost("news.site.cn", sites))
    }

    @Test
    fun unrelatedParentAndLookalikeDoNotMatch() {
        assertFalse(isReaderHost("other.com", sites))
        assertFalse(isReaderHost("site.cn", sites))
        assertFalse(isReaderHost("notexample.com", sites))
        assertFalse(isReaderHost("example.com.evil.com", sites))
    }

    @Test
    fun blankInputsNeverMatch() {
        assertFalse(isReaderHost(null, sites))
        assertFalse(isReaderHost("", sites))
        assertFalse(isReaderHost("example.com", emptySet()))
    }

    @Test
    fun readerSitesSurviveBackupRoundTrip() {
        val json = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings(),
            readerSites = listOf("example.com", "news.site.cn")
        )
        val data = parseBackup(json)
        assertTrue(data != null)
        assertEquals(
            listOf("example.com", "news.site.cn"), data!!.readerSites
        )
    }

    @Test
    fun backupWithoutReaderSitesParsesAsEmpty() {
        // A backup written before v0.73.0 has no readerSites key at
        // all; it must still parse, with an empty list.
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,"bookmarks":[]}
        """.trimIndent()
        val data = parseBackup(raw)
        assertTrue(data != null)
        assertEquals(emptyList<String>(), data!!.readerSites)
    }

    @Test
    fun backupReaderSitesAreSanitizedOnParse() {
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[],
             "readerSites":[" OK.example ","https://bad.example/x","",
                            "ok.example","has space.example"]}
        """.trimIndent()
        val data = parseBackup(raw)
        assertTrue(data != null)
        assertEquals(listOf("ok.example"), data!!.readerSites)
    }
}

/**
 * v0.74.0: the backup audit found the blocked counters were the
 * only user-accumulated data missing from the full backup. These
 * tests pin the round trip, the pre-v0.74.0 shape (no blockedStats
 * key at all — the counters must come back "absent", not zero, so
 * an import leaves the current counters untouched) and the
 * parse-time pruning of the daily map.
 */
class BlockedStatsBackupTest {

    @Test
    fun blockedStatsSurviveBackupRoundTrip() {
        val today = blockedDayKey(java.time.LocalDate.now())
        val json = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings(),
            totalBlocked = 1234L,
            blockedDaily = mapOf(today to 42L)
        )
        val data = parseBackup(json)
        assertTrue(data != null)
        assertEquals(1234L, data!!.totalBlocked)
        assertEquals(mapOf(today to 42L), data.blockedDaily)
    }

    @Test
    fun oldBackupWithoutBlockedStatsParsesAsAbsent() {
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,"bookmarks":[]}
        """.trimIndent()
        val data = parseBackup(raw)
        assertTrue(data != null)
        assertEquals(null, data!!.totalBlocked)
        assertTrue(data.blockedDaily.isEmpty())
    }

    @Test
    fun backupWrittenWithoutStatsOmitsThem() {
        val json = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings()
        )
        val data = parseBackup(json)
        assertTrue(data != null)
        assertEquals(null, data!!.totalBlocked)
        assertTrue(data.blockedDaily.isEmpty())
    }

    @Test
    fun blockedDailyIsPrunedAndSanitizedOnParse() {
        val today = java.time.LocalDate.now()
        val todayKey = blockedDayKey(today)
        val staleKey = blockedDayKey(today.minusDays(60))
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[],
             "blockedStats":{"total":99,
               "daily":{"$todayKey":7,"$staleKey":500,"not-a-date":3,
                        "${blockedDayKey(today.minusDays(1))}":0}}}
        """.trimIndent()
        val data = parseBackup(raw)
        assertTrue(data != null)
        assertEquals(99L, data!!.totalBlocked)
        // Only today's positive count survives: the 60-day-old day
        // is outside the kept window, the unparseable key is
        // dropped by the prune, and the zero count is not stored.
        assertEquals(mapOf(todayKey to 7L), data.blockedDaily)
    }
}

/**
 * v0.74.0: the diagnostics text is assembled from DiagnosticInfo,
 * a type that carries ONLY version numbers, counts and a storage
 * total — no URL, title or history field exists in it, so browsing
 * content cannot reach the text by construction. These tests pin
 * the exact line set and the honest "unknown" for a missing
 * system WebView.
 */
class DiagnosticTextTest {

    private fun sampleInfo() = DiagnosticInfo(
        appVersionName = "0.74.0",
        appVersionCode = 74L,
        geckoVersion = GECKOVIEW_VERSION,
        webViewPackageName = "com.google.android.webview",
        webViewVersionName = "120.0.6099.43",
        androidRelease = "14",
        androidSdk = 34,
        storageText = "312.4 MB",
        bookmarkCount = 12,
        historyCount = 34,
        readingCount = 5,
        speedDialCount = 6,
        totalBlocked = 789L
    )

    @Test
    fun diagnosticTextHasExactlyTheExpectedLines() {
        val lines = diagnosticText(sampleInfo()).lines()
        assertEquals(11, lines.size)
        assertEquals("TuxunBrowse 诊断信息", lines[0])
        assertEquals("应用版本：0.74.0（版本号 74）", lines[1])
        assertEquals(
            "火狐内核（GeckoView）版本：$GECKOVIEW_VERSION", lines[2]
        )
        assertEquals(
            "系统 WebView：com.google.android.webview 120.0.6099.43",
            lines[3]
        )
        assertEquals("Android 版本：14（API 34）", lines[4])
        assertEquals("应用存储占用：312.4 MB", lines[5])
        assertEquals("书签：12 条", lines[6])
        assertEquals("历史记录：34 条", lines[7])
        assertEquals("阅读列表：5 条", lines[8])
        assertEquals("首页快捷方式：6 条", lines[9])
        assertEquals("广告拦截累计：789 个", lines[10])
    }

    @Test
    fun diagnosticTextContainsNoUrlLikeContent() {
        val text = diagnosticText(sampleInfo())
        assertFalse(text.contains("http://"))
        assertFalse(text.contains("https://"))
        assertFalse(text.contains("www."))
    }

    @Test
    fun missingWebViewPackageIsShownAsUnknown() {
        val info = sampleInfo().copy(
            webViewPackageName = null, webViewVersionName = null
        )
        val lines = diagnosticText(info).lines()
        assertEquals("系统 WebView：未知", lines[3])
    }
}

/**
 * The address bar's not-editing display (v0.76.0): a web page shows
 * only its host. These pin the cases the task calls out — https and
 * http, a non-default port kept, the default ports dropped, paths
 * and queries stripped, plain IP hosts, and the internal tuxun://
 * home address returning "" (the bar stays empty on the home page,
 * as it always has).
 */
class DisplayHostForTest {

    @Test
    fun httpsUrlShowsOnlyTheHost() {
        assertEquals("example.com", displayHostFor("https://example.com/"))
        assertEquals(
            "www.example.com",
            displayHostFor("https://www.example.com/some/page?x=1#frag")
        )
    }

    @Test
    fun httpUrlShowsOnlyTheHost() {
        assertEquals("example.org", displayHostFor("http://example.org/a/b"))
    }

    @Test
    fun nonDefaultPortIsKeptDefaultPortsAreDropped() {
        assertEquals(
            "example.com:8080",
            displayHostFor("http://example.com:8080/page")
        )
        assertEquals(
            "example.com:8443",
            displayHostFor("https://example.com:8443/")
        )
        assertEquals("example.com", displayHostFor("http://example.com:80/"))
        assertEquals("example.com", displayHostFor("https://example.com:443/"))
    }

    @Test
    fun ipAddressHostsShowAsIs() {
        assertEquals("192.168.1.10", displayHostFor("http://192.168.1.10/admin"))
        assertEquals(
            "192.168.1.10:9000",
            displayHostFor("http://192.168.1.10:9000/admin")
        )
        assertEquals("[::1]:8080", displayHostFor("http://[::1]:8080/"))
    }

    @Test
    fun internalAddressesReturnEmpty() {
        assertEquals("", displayHostFor(HOME_URL))
        assertEquals("", displayHostFor("tuxun://home"))
        assertEquals("", displayHostFor(""))
        assertEquals("", displayHostFor("   "))
    }

    @Test
    fun unparseableOrNonWebInputComesBackUnchanged() {
        assertEquals("not a url", displayHostFor("not a url"))
        assertEquals(
            "ftp://example.com/file",
            displayHostFor("ftp://example.com/file")
        )
    }
}

/**
 * HTTPS-first upgrade rules (v0.77.0): only http:// addresses on
 * public hosts are upgraded; IP literals, localhost and
 * local-network names never are; port/path/query survive untouched;
 * the switch being off disables everything.
 */
class HttpsUpgradeTest {

    @Test
    fun plainHttpIsUpgraded() {
        assertEquals(
            "https://example.com/",
            upgradeToHttps("http://example.com/", true)
        )
        assertEquals(
            "https://www.example.com/a/b",
            upgradeToHttps("http://www.example.com/a/b", true)
        )
    }

    @Test
    fun portPathQueryAndFragmentArePreserved() {
        assertEquals(
            "https://example.com:8080/a/b?x=1&y=2#frag",
            upgradeToHttps("http://example.com:8080/a/b?x=1&y=2#frag", true)
        )
    }

    @Test
    fun switchOffLeavesEverythingAlone() {
        assertEquals(
            "http://example.com/",
            upgradeToHttps("http://example.com/", false)
        )
    }

    @Test
    fun httpsAndOtherSchemesAreUntouched() {
        assertEquals(
            "https://example.com/",
            upgradeToHttps("https://example.com/", true)
        )
        assertEquals(
            "ftp://example.com/file",
            upgradeToHttps("ftp://example.com/file", true)
        )
        assertEquals(
            "tuxun://home",
            upgradeToHttps("tuxun://home", true)
        )
    }

    @Test
    fun ipLiteralHostsAreNotUpgraded() {
        assertEquals(
            "http://192.168.1.1/admin",
            upgradeToHttps("http://192.168.1.1/admin", true)
        )
        assertEquals(
            "http://203.0.113.9:9000/x",
            upgradeToHttps("http://203.0.113.9:9000/x", true)
        )
        assertEquals(
            "http://[::1]:8080/",
            upgradeToHttps("http://[::1]:8080/", true)
        )
        assertEquals(
            "http://[2001:db8::1]/",
            upgradeToHttps("http://[2001:db8::1]/", true)
        )
    }

    @Test
    fun localNamesAreNotUpgraded() {
        assertEquals(
            "http://localhost:3000/dev",
            upgradeToHttps("http://localhost:3000/dev", true)
        )
        assertEquals(
            "http://router.local/",
            upgradeToHttps("http://router.local/", true)
        )
        assertEquals(
            "http://nas.home.lan/files",
            upgradeToHttps("http://nas.home.lan/files", true)
        )
        assertEquals(
            "http://printer.home/",
            upgradeToHttps("http://printer.home/", true)
        )
        assertEquals(
            "http://intranet/",
            upgradeToHttps("http://intranet/", true)
        )
    }

    @Test
    fun downgradeSwapsOnlyTheScheme() {
        assertEquals(
            "http://example.com:8443/a?x=1",
            downgradeToHttp("https://example.com:8443/a?x=1")
        )
        assertEquals(
            "http://example.com/",
            downgradeToHttp("http://example.com/")
        )
        assertEquals("tuxun://home", downgradeToHttp("tuxun://home"))
    }

    @Test
    fun httpsFirstTravelsThroughBackups() {
        val json = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings(httpsFirstEnabled = false)
        )
        val parsed = parseBackup(json)
        assertEquals(false, parsed?.settings?.httpsFirstEnabled)
        // A backup written before v0.77.0 carries no such key: the
        // parsed value stays null, so applying it keeps the current
        // setting instead of inventing one.
        val oldJson = backupToJson(
            emptyList(), emptyList(), emptyList(), emptyList(),
            BackupSettings()
        )
        assertEquals(null, parseBackup(oldJson)?.settings?.httpsFirstEnabled)
    }
}

/**
 * Built-in ad/tracker blocklist hygiene (v0.77.0 expansion): the
 * list must stay duplicate-free, every entry a plain domain (no
 * scheme, path or wildcard), and ordinary sites — including their
 * subdomains — must never be blocked, while known ad hosts and
 * their subdomains are.
 */
class AdBlockerListTest {

    @Test
    fun listHasNoDuplicates() {
        assertEquals(
            AdBlocker.blockedHostList.size,
            AdBlocker.blockedHostList.toSet().size
        )
        assertEquals(
            AdBlocker.blockedHostList.size,
            AdBlocker.blockedHostCount
        )
    }

    @Test
    fun everyEntryIsAPlainDomain() {
        val domain = Regex("^[a-z0-9-]+(\\.[a-z0-9-]+)+$")
        AdBlocker.blockedHostList.forEach { entry ->
            assertTrue("bad entry: $entry", domain.matches(entry))
        }
    }

    @Test
    fun ordinarySitesAndTheirSubdomainsAreNotBlocked() {
        val ordinary = listOf(
            "baidu.com", "google.com", "github.com", "bilibili.com",
            "taobao.com", "jd.com", "zhihu.com", "weibo.com"
        )
        ordinary.forEach { site ->
            assertFalse(site, AdBlocker.isAdRequest("https://$site/"))
            assertFalse(
                "www.$site",
                AdBlocker.isAdRequest("https://www.$site/")
            )
            assertFalse(
                "m.$site",
                AdBlocker.isAdRequest("https://m.$site/")
            )
            assertFalse(
                "api.$site",
                AdBlocker.isAdRequest("https://api.$site/v1")
            )
        }
    }

    @Test
    fun knownAdHostsAndTheirSubdomainsAreBlocked() {
        assertTrue(AdBlocker.isAdRequest("https://doubleclick.net/"))
        assertTrue(
            AdBlocker.isAdRequest("https://ads.doubleclick.net/serve")
        )
        assertTrue(
            AdBlocker.isAdRequest("https://www.googlesyndication.com/pagead/js")
        )
        assertTrue(AdBlocker.isAdRequest("http://pos.baidu.com/"))
        assertTrue(AdBlocker.isAdRequest("https://gdt.qq.com/"))
        assertTrue(AdBlocker.isAdRequest("https://media.net/"))
        assertTrue(
            AdBlocker.isAdRequest("https://unityads.unity3d.com/")
        )
        assertFalse(AdBlocker.isAdRequest("https://example.com/"))
        assertFalse(AdBlocker.isAdRequest("not a url"))
    }
}
