package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.BackupSettings
import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.DownloadMode
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.HistoryRetention
import com.tuxunbrowse.android.data.ReaderBackground
import com.tuxunbrowse.android.data.ReaderFontSize
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.ThemeMode
import com.tuxunbrowse.android.data.TrackingProtectionLevel
import com.tuxunbrowse.android.data.backupToJson
import com.tuxunbrowse.android.data.parseBackup
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v0.101.0 backup/restore integrity audit: the whole settings
 * slice round-trips, pre-v0.91.0 backups (which carry keys for
 * features deleted in v0.91.0) import tolerantly, and
 * wrong-typed values parse as ABSENT instead of being misread
 * into destructive values (0% text zoom, a zeroed blocked
 * counter, a default engine overwriting the current one).
 */
class FullBackupRoundTripAuditTest {

    @Test
    fun everySettingsFieldSurvivesTheRoundTrip() {
        val settings = BackupSettings(
            mainEngine = Engine.WEBVIEW,
            incognitoEngine = Engine.WEBVIEW,
            searchEngine = SearchEngine.GOOGLE,
            adBlockEnabled = false,
            downloadMode = DownloadMode.TUXUNDOWN,
            historyRetention = HistoryRetention.DAYS_7,
            textZoomPercent = 130,
            startupRestore = false,
            themeMode = ThemeMode.DARK,
            webDarkMode = true,
            readerFontSize = ReaderFontSize.LARGE,
            readerBackground = ReaderBackground.BEIGE,
            httpsFirstEnabled = false,
            guardScriptEnabled = false,
            trackingProtectionLevel = TrackingProtectionLevel.STRICT,
            doNotTrackEnabled = true,
            globalPrivacyControlEnabled = true,
            webRtcDisabled = true,
            translateTargetLanguage = "zh-Hans"
        )
        val json = backupToJson(
            listOf(Bookmark("甲站", "https://a.example", pinned = true, folder = "工作")),
            listOf(HistoryItem("甲站", "https://a.example", 42L)),
            settings,
            compatSites = listOf("compat.example"),
            desktopSites = listOf("desk.example"),
            bookmarkFolders = listOf("工作", "空夹"),
            totalBlocked = 777L
        )
        val data = parseBackup(json)!!
        assertEquals(settings, data.settings)
        assertEquals(19, data.settings.overwriteSummary().size)
        assertEquals(listOf("compat.example"), data.compatSites)
        assertEquals(listOf("desk.example"), data.desktopSites)
        assertEquals(listOf("工作", "空夹"), data.bookmarkFolders)
        assertEquals(777L, data.totalBlocked)
        assertEquals(1, data.bookmarks.size)
        assertEquals(1, data.history.size)
    }

    @Test
    fun exportedBackupCarriesEveryTopLevelKey() {
        val json = backupToJson(
            emptyList(), emptyList(),
            BackupSettings(mainEngine = Engine.GECKO),
            totalBlocked = 1L
        )
        val root = JSONObject(json)
        listOf(
            "format", "version", "bookmarks", "history",
            "compatSites", "desktopSites", "bookmarkFolders",
            "settings", "blockedStats"
        ).forEach { key -> assertTrue("missing key: $key", root.has(key)) }
    }
}

class OldBackupCompatAuditTest {

    @Test
    fun preRemovalBackupWithDeletedFeatureKeysImportsClean() {
        // A backup in the pre-v0.91.0 shape: settings for the
        // custom search engine, the UA preset, third-party
        // cookies, image blocking, the JavaScript switch and the
        // search-suggestions switch (all deleted in v0.91.0),
        // plus top-level reader sites and per-day blocked stats.
        // Every deleted-feature key must be ignored, never crash,
        // and the still-live fields must come through untouched.
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[{"title":"甲站","url":"https://a.example"}],
             "history":[],
             "readerSites":["reader.example"],
             "blockedStats":{"total":55,"daily":{"2026-11-20":7}},
             "settings":{
               "searchEngine":"custom",
               "customSearchName":"我的引擎",
               "customSearchTemplate":"https://s.example/?q=%s",
               "uaPreset":"desktop",
               "allowThirdPartyCookies":false,
               "blockImages":true,
               "disableJavaScript":true,
               "adHideSlots":false,
               "searchSuggestionsEnabled":true,
               "adBlockEnabled":false,
               "textZoomPercent":115,
               "trackingProtectionLevel":"strict"
             }}
        """.trimIndent()
        val data = parseBackup(raw)!!
        assertEquals(1, data.bookmarks.size)
        assertEquals(55L, data.totalBlocked)
        // The removed custom engine does NOT fold into a forced
        // default: the field parses as absent so the import
        // leaves the current engine choice alone.
        assertNull(data.settings.searchEngine)
        assertEquals(false, data.settings.adBlockEnabled)
        assertEquals(115, data.settings.textZoomPercent)
        assertEquals(
            TrackingProtectionLevel.STRICT,
            data.settings.trackingProtectionLevel
        )
    }

    @Test
    fun legacyFoldDedupesAgainstBookmarksAndItself() {
        // Same dedupe rule as the on-device migration: a URL
        // already bookmarked is not duplicated (and a dial does
        // not upgrade an existing bookmark to pinned), a URL in
        // both legacy lists folds once (the dial wins, pinned),
        // and a repeated legacy URL folds once.
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[{"title":"已有","url":"https://a.example"}],
             "readingList":[
               {"title":"读A","url":"https://a.example","time":1,"read":false},
               {"title":"读B","url":"https://b.example","time":2,"read":true},
               {"title":"读B二","url":"https://b.example","time":3,"read":false},
               {"title":"坏条","url":"ftp://x.example","time":4,"read":false},
               "junk",42
             ],
             "speedDials":[
               {"title":"捷A","url":"https://a.example"},
               {"title":"捷C","url":"https://c.example"},
               {"title":"捷B","url":"https://b.example"}
             ]}
        """.trimIndent()
        val data = parseBackup(raw)!!
        assertEquals(3, data.bookmarks.size)
        val byUrl = data.bookmarks.associateBy { it.url }
        // Pre-existing bookmark wins as-is (not pinned, no folder).
        assertEquals("已有", byUrl["https://a.example"]!!.title)
        assertEquals(false, byUrl["https://a.example"]!!.pinned)
        assertEquals("", byUrl["https://a.example"]!!.folder)
        // b.example appears in both lists: the dial folds first.
        assertEquals("捷B", byUrl["https://b.example"]!!.title)
        assertTrue(byUrl["https://b.example"]!!.pinned)
        assertTrue(byUrl["https://c.example"]!!.pinned)
    }

    @Test
    fun badListEntriesAreSkippedNotFatal() {
        val raw = """
            {"format":"tuxunbrowse-backup","version":1,
             "bookmarks":[
               "junk",
               {"title":"无网址"},
               {"title":"数字网址","url":123},
               {"title":"脚本网址","url":"javascript:alert(1)"},
               {"title":"好站","url":"https://good.example"}
             ],
             "history":[
               {"title":"坏时间","url":"https://h.example","time":"昨天"},
               7
             ],
             "compatSites":["ok.example","https://full.url/x",123,""],
             "bookmarkFolders":["正常",123,""]}
        """.trimIndent()
        val data = parseBackup(raw)!!
        assertEquals(1, data.bookmarks.size)
        assertEquals("https://good.example", data.bookmarks[0].url)
        // A wrong-typed history time falls back to 0, entry kept.
        assertEquals(1, data.history.size)
        assertEquals(0L, data.history[0].time)
        assertEquals(listOf("ok.example"), data.compatSites)
        assertEquals(listOf("正常"), data.bookmarkFolders)
    }

    @Test
    fun fileWithoutTheFormatMarkerIsRefused() {
        assertNull(parseBackup("""{"bookmarks":[]}"""))
        assertNull(parseBackup("""{"format":"other-app","bookmarks":[]}"""))
        assertNull(parseBackup("not json at all"))
        assertNull(parseBackup(""))
    }
}

class BackupWrongTypeAuditTest {

    private fun settingsOf(settingsJson: String) =
        parseBackup(
            """{"format":"tuxunbrowse-backup","version":1,
               "bookmarks":[],"settings":$settingsJson}"""
        )!!.settings

    @Test
    fun unknownEnumIdsParseAsAbsent() {
        val s = settingsOf(
            """{"mainEngine":"netscape","incognitoEngine":"netscape",
               "searchEngine":"altavista","downloadMode":"carrier-pigeon",
               "historyRetention":"until-notice","themeMode":"neon",
               "readerFontSize":"huge","readerBackground":"plaid",
               "trackingProtectionLevel":"paranoid"}"""
        )
        assertNull(s.mainEngine)
        assertNull(s.incognitoEngine)
        assertNull(s.searchEngine)
        assertNull(s.downloadMode)
        assertNull(s.historyRetention)
        assertNull(s.themeMode)
        assertNull(s.readerFontSize)
        assertNull(s.readerBackground)
        assertNull(s.trackingProtectionLevel)
        assertTrue(s.overwriteSummary().isEmpty())
    }

    @Test
    fun wrongTypedBooleansParseAsAbsent() {
        val s = settingsOf(
            """{"adBlockEnabled":1,"webDarkMode":"true",
               "startupRestore":0,"doNotTrackEnabled":"yes"}"""
        )
        assertNull(s.adBlockEnabled)
        assertNull(s.webDarkMode)
        assertNull(s.startupRestore)
        assertNull(s.doNotTrackEnabled)
    }

    @Test
    fun wrongTypedOrOutOfRangeTextZoomParsesAsAbsent() {
        assertNull(settingsOf("""{"textZoomPercent":"115"}""").textZoomPercent)
        assertNull(settingsOf("""{"textZoomPercent":0}""").textZoomPercent)
        assertNull(settingsOf("""{"textZoomPercent":100000}""").textZoomPercent)
        assertEquals(
            85, settingsOf("""{"textZoomPercent":85}""").textZoomPercent
        )
        assertEquals(
            130, settingsOf("""{"textZoomPercent":130}""").textZoomPercent
        )
    }

    @Test
    fun wrongTypedBlockedTotalParsesAsAbsent() {
        val bad = parseBackup(
            """{"format":"tuxunbrowse-backup","version":1,"bookmarks":[],
               "blockedStats":{"total":"很多"}}"""
        )!!
        assertNull(bad.totalBlocked)
        val negative = parseBackup(
            """{"format":"tuxunbrowse-backup","version":1,"bookmarks":[],
               "blockedStats":{"total":-5}}"""
        )!!
        assertEquals(0L, negative.totalBlocked)
    }
}
