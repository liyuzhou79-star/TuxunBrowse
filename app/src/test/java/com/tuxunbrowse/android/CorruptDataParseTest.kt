package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.parseBookmarkFoldersJson
import com.tuxunbrowse.android.data.parseBookmarksJson
import com.tuxunbrowse.android.data.parseGeckoDownloadsJson
import com.tuxunbrowse.android.data.parseHistoryJson
import com.tuxunbrowse.android.data.parseJsonObjectList
import com.tuxunbrowse.android.data.parseReadingListJson
import com.tuxunbrowse.android.data.parseSavedTabsJson
import com.tuxunbrowse.android.data.parseSiteListJson
import com.tuxunbrowse.android.data.parseSpeedDialsJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Corruption samples for the stored-data parsers (v0.83.0 audit).
 * The contract being pinned: damaged stored content never throws,
 * damage stays inside its own key, and inside a list ONE bad
 * element is skipped while the good elements around it survive —
 * before this audit a single bad element emptied the whole list.
 */
class CorruptDataParseTest {

    @Test
    fun truncatedJsonParsesToEmptyEverywhere() {
        val truncated = "[{\"title\":\"a\",\"url\":\"https://a.com\""
        assertEquals(emptyList<Any>(), parseBookmarksJson(truncated))
        assertEquals(emptyList<Any>(), parseHistoryJson(truncated))
        assertEquals(emptyList<Any>(), parseReadingListJson(truncated))
        assertEquals(emptyList<Any>(), parseSpeedDialsJson(truncated))
        assertEquals(emptyList<Any>(), parseGeckoDownloadsJson(truncated))
        assertEquals(emptyList<String>(), parseSiteListJson(truncated))
        assertEquals(emptyList<String>(), parseBookmarkFoldersJson(truncated))
        assertTrue(parseBookmarksJson(null).isEmpty())
        assertTrue(parseBookmarksJson("").isEmpty())
        assertTrue(parseBookmarksJson("not json at all").isEmpty())
    }

    @Test
    fun wrongTopLevelTypeParsesToEmpty() {
        // An object where an array belongs, a bare string, a number.
        assertTrue(parseBookmarksJson("{\"title\":\"a\"}").isEmpty())
        assertTrue(parseHistoryJson("\"just a string\"").isEmpty())
        assertTrue(parseSiteListJson("42").isEmpty())
    }

    @Test
    fun oneBadBookmarkElementIsSkippedAndTheRestSurvive() {
        val raw = "[" +
            "{\"title\":\"好书签\",\"url\":\"https://a.com\",\"pinned\":true}," +
            "\"this element is a string, not an object\"," +
            "42," +
            "{\"title\":\"另一条\",\"url\":\"https://b.com\",\"folder\":\"资料\"}" +
            "]"
        val parsed = parseBookmarksJson(raw)
        assertEquals(2, parsed.size)
        assertEquals("好书签", parsed[0].title)
        assertTrue(parsed[0].pinned)
        assertEquals("另一条", parsed[1].title)
        assertEquals("资料", parsed[1].folder)
    }

    @Test
    fun bookmarkFieldsWithWrongTypesFallBackPerField() {
        // title stored as a number, pinned as a string: the row
        // survives, the bad fields fall back, the good URL is kept.
        val raw = "[{\"title\":123,\"url\":\"https://a.com\",\"pinned\":\"yes\"}]"
        val parsed = parseBookmarksJson(raw)
        assertEquals(1, parsed.size)
        assertEquals("https://a.com", parsed[0].url)
        assertEquals(false, parsed[0].pinned)
    }

    @Test
    fun historyTimeStoredAsTextFallsBackToZero() {
        val raw = "[" +
            "{\"title\":\"a\",\"url\":\"https://a.com\",\"time\":\"not-a-number\"}," +
            "{\"title\":\"b\",\"url\":\"https://b.com\",\"time\":123456789}" +
            "]"
        val parsed = parseHistoryJson(raw)
        assertEquals(2, parsed.size)
        assertEquals(0L, parsed[0].time)
        assertEquals(123456789L, parsed[1].time)
    }

    @Test
    fun readingListSkipsBadElementAndKeepsReadFlags() {
        val raw = "[" +
            "{\"title\":\"a\",\"url\":\"https://a.com\",\"time\":1,\"read\":true}," +
            "[\"nested\",\"array\"]," +
            "{\"title\":\"b\",\"url\":\"https://b.com\",\"time\":2}" +
            "]"
        val parsed = parseReadingListJson(raw)
        assertEquals(2, parsed.size)
        assertTrue(parsed[0].read)
        assertEquals(false, parsed[1].read)
    }

    @Test
    fun speedDialsAndGeckoDownloadsSkipBadElements() {
        val dials = parseSpeedDialsJson(
            "[{\"title\":\"a\",\"url\":\"https://a.com\"},null," +
                "{\"title\":\"b\",\"url\":\"https://b.com\"}]"
        )
        assertEquals(2, dials.size)
        val records = parseGeckoDownloadsJson(
            "[{\"name\":\"f.zip\",\"url\":\"https://a.com/f.zip\"," +
                "\"size\":\"oops\",\"time\":7}," +
                "{\"name\":\"g.zip\",\"url\":\"https://b.com/g.zip\"," +
                "\"size\":99,\"time\":8}]"
        )
        assertEquals(2, records.size)
        assertEquals(0L, records[0].sizeBytes)
        assertEquals(99L, records[1].sizeBytes)
    }

    @Test
    fun siteListSkipsNonStringElementsInsteadOfEmptying() {
        // Before the audit one numeric element threw getString and
        // the whole compat/desktop/reader list read back empty.
        val parsed = parseSiteListJson(
            "[\"example.com\",42,{\"x\":1},\"other.org\"]"
        )
        assertEquals(listOf("example.com", "other.org"), parsed)
    }

    @Test
    fun bookmarkFoldersSkipNonStringsTrimAndDedupe() {
        val parsed = parseBookmarkFoldersJson(
            "[\" 资料 \",7,\"资料\",\"\",\"工作\"]"
        )
        assertEquals(listOf("资料", "工作"), parsed)
    }

    @Test
    fun savedTabsSkipBadElementsAndKeepTheGoodOnes() {
        val raw = "{\"tabs\":[" +
            "{\"e\":\"gecko\",\"u\":\"https://a.com\",\"d\":false}," +
            "\"garbage\"," +
            "{\"e\":\"webview\",\"u\":\"https://b.com\",\"d\":true}," +
            "{\"e\":\"gecko\",\"u\":\"\",\"d\":false}" +
            "],\"active\":1}"
        val (tabs, active) = parseSavedTabsJson(raw)
        assertEquals(2, tabs.size)
        assertEquals(Engine.GECKO, tabs[0].engine)
        assertEquals(Engine.WEBVIEW, tabs[1].engine)
        assertTrue(tabs[1].desktopMode)
        assertEquals(1, active)
    }

    @Test
    fun savedTabsCorruptPayloadFallsBackToEmptySession() {
        val (tabs, active) = parseSavedTabsJson("{\"tabs\":[{\"e\":\"gecko\"")
        assertTrue(tabs.isEmpty())
        assertEquals(0, active)
        val (tabs2, active2) = parseSavedTabsJson("[1,2,3]")
        assertTrue(tabs2.isEmpty())
        assertEquals(0, active2)
        // "tabs" itself stored with the wrong type: no tabs, but
        // the active index still reads tolerantly.
        val (tabs3, _) = parseSavedTabsJson("{\"tabs\":\"oops\",\"active\":0}")
        assertTrue(tabs3.isEmpty())
    }

    @Test
    fun genericParserSkipsElementsWhoseParseThrows() {
        val parsed = parseJsonObjectList(
            "[{\"n\":1},{\"n\":\"boom\"},{\"n\":3}]"
        ) { obj ->
            val n = obj.getInt("n")
            n
        }
        assertEquals(listOf(1, 3), parsed)
    }
}
