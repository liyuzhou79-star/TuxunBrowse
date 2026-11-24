package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.GeckoDownloadRecord
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.SavedTab
import com.tuxunbrowse.android.data.bookmarkJsonObject
import com.tuxunbrowse.android.data.bookmarksFromJson
import com.tuxunbrowse.android.data.bookmarksToJson
import com.tuxunbrowse.android.data.geckoDownloadJsonObject
import com.tuxunbrowse.android.data.historyJsonObject
import com.tuxunbrowse.android.data.parseBookmarksJson
import com.tuxunbrowse.android.data.parseGeckoDownloadsJson
import com.tuxunbrowse.android.data.parseHistoryJson
import com.tuxunbrowse.android.data.parseSavedTabsJson
import com.tuxunbrowse.android.data.restoredActiveIndex
import com.tuxunbrowse.android.data.savedTabsJson
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Full-field round trips for the stored data objects (v0.109.0
 * field-loss audit, second pass). Each test builds an object with
 * EVERY field at a non-default value, runs it through the exact
 * production writer (the same builder functions the Store writes
 * with) and the exact production reader, and asserts field-by-field
 * equality — so a field that is written under one key and read
 * under another, or dropped on either side, fails here instead of
 * silently losing user data on the device.
 */
class FieldRoundTripTest {

    private fun roundTripArray(build: () -> org.json.JSONObject): String =
        JSONArray().put(build()).toString()

    @Test
    fun bookmarkRoundTripsEveryField() {
        val original = Bookmark(
            title = "示例标题",
            url = "https://example.com/some/page?q=1",
            pinned = true,
            folder = "工作资料"
        )
        val parsed = parseBookmarksJson(
            roundTripArray { bookmarkJsonObject(original) }
        )
        assertEquals(listOf(original), parsed)
    }

    @Test
    fun bookmarkExportRoundTripsEveryField() {
        val original = Bookmark(
            title = "导出的书签",
            url = "https://example.org/a",
            pinned = true,
            folder = "稍后读"
        )
        val parsed = bookmarksFromJson(bookmarksToJson(listOf(original)))
        assertEquals(listOf(original), parsed)
    }

    @Test
    fun historyItemRoundTripsEveryFieldWithFullLongTime() {
        // A millisecond timestamp far beyond the Int range: an
        // Int-typed read would truncate it.
        val original = HistoryItem(
            title = "历史标题",
            url = "https://example.net/history",
            time = 1_764_000_000_123L
        )
        val parsed = parseHistoryJson(
            roundTripArray { historyJsonObject(original) }
        )
        assertEquals(listOf(original), parsed)
    }

    @Test
    fun geckoDownloadRecordRoundTripsEveryField() {
        // Size beyond the Int range on purpose (a >4 GB download):
        // the byte count is a Long on both sides.
        val original = GeckoDownloadRecord(
            fileName = "big-file (1).zip",
            sourceUrl = "https://files.example.com/big-file.zip",
            sizeBytes = 5_000_000_001L,
            mimeType = "application/zip",
            contentUri = "content://media/external/downloads/4242",
            filePath = null,
            time = 1_764_000_111_222L
        )
        val parsed = parseGeckoDownloadsJson(
            roundTripArray { geckoDownloadJsonObject(original) }
        )
        assertEquals(listOf(original), parsed)
    }

    @Test
    fun archiveRecordRoundTripsWithPathLocatorAndNoUri() {
        // The v0.85.0 web-archive shape sharing this table: the
        // archive MIME type, a file path instead of a content Uri.
        val original = GeckoDownloadRecord(
            fileName = "页面存档.mhtml",
            sourceUrl = "https://example.com/article",
            sizeBytes = 123_456L,
            mimeType = "multipart/related",
            contentUri = null,
            filePath = "/storage/emulated/0/Download/页面存档.mhtml",
            time = 1_764_000_222_333L
        )
        val parsed = parseGeckoDownloadsJson(
            roundTripArray { geckoDownloadJsonObject(original) }
        )
        assertEquals(listOf(original), parsed)
    }

    @Test
    fun geckoDownloadRecordNullMimeRoundTripsAsNull() {
        val original = GeckoDownloadRecord(
            fileName = "no-mime.bin",
            sourceUrl = "https://example.com/no-mime.bin",
            sizeBytes = 7L,
            mimeType = null,
            contentUri = null,
            filePath = "/data/app/downloads/no-mime.bin",
            time = 99L
        )
        val parsed = parseGeckoDownloadsJson(
            roundTripArray { geckoDownloadJsonObject(original) }
        )
        assertEquals(listOf(original), parsed)
    }

    @Test
    fun savedTabsRoundTripEveryFieldAndActiveIndex() {
        val original = listOf(
            SavedTab(Engine.GECKO, "https://a.example.com", desktopMode = true),
            SavedTab(Engine.WEBVIEW, "https://b.example.com", desktopMode = false),
            SavedTab(Engine.WEBVIEW, "tuxun://home", desktopMode = true)
        )
        val (tabs, active) = parseSavedTabsJson(savedTabsJson(original, 2))
        assertEquals(original, tabs)
        assertEquals(2, active)
    }

    @Test
    fun restoredActiveIndexIsIdentityWhenNothingIsDropped() {
        val saved = listOf(
            SavedTab(Engine.GECKO, "https://a.example.com", false),
            SavedTab(Engine.WEBVIEW, "https://b.example.com", false),
            SavedTab(Engine.GECKO, "https://c.example.com", false)
        )
        for (i in saved.indices) {
            assertEquals(i, restoredActiveIndex(saved, i) { true })
        }
        // Out-of-range stored indices clamp, as the old raw read did.
        assertEquals(2, restoredActiveIndex(saved, 99) { true })
        assertEquals(0, restoredActiveIndex(saved, -3) { true })
    }

    @Test
    fun restoredActiveIndexShiftsWhenAnEarlierTabIsDropped() {
        val saved = listOf(
            SavedTab(Engine.GECKO, "about:blank", false),
            SavedTab(Engine.WEBVIEW, "https://b.example.com", false),
            SavedTab(Engine.GECKO, "https://c.example.com", false)
        )
        val keep: (SavedTab) -> Boolean = { it.url.startsWith("http") }
        // The dropped first entry must not push the active tab one
        // position to the right in the restored list.
        assertEquals(0, restoredActiveIndex(saved, 1, keep))
        assertEquals(1, restoredActiveIndex(saved, 2, keep))
    }

    @Test
    fun restoredActiveIndexFallsBackWhenTheActiveTabItselfIsDropped() {
        val saved = listOf(
            SavedTab(Engine.GECKO, "https://a.example.com", false),
            SavedTab(Engine.WEBVIEW, "about:blank", false),
            SavedTab(Engine.GECKO, "https://c.example.com", false)
        )
        val keep: (SavedTab) -> Boolean = { it.url.startsWith("http") }
        // Active entry dropped: the kept entry just before it wins.
        assertEquals(0, restoredActiveIndex(saved, 1, keep))
        // Active entry dropped with nothing kept before it: the
        // first kept entry is the fallback.
        val saved2 = listOf(
            SavedTab(Engine.WEBVIEW, "about:blank", false),
            SavedTab(Engine.GECKO, "https://c.example.com", false)
        )
        assertEquals(0, restoredActiveIndex(saved2, 0, keep))
        assertEquals(0, restoredActiveIndex(emptyList(), 0, keep))
    }
}
