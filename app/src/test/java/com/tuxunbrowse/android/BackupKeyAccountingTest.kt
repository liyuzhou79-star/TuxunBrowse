package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Backup key accounting (v0.205.0): every persisted key in
 * Store has exactly one disposition, pinned here —
 *
 * - BACKUP: carried by the full backup (settings slice or a
 *   merged list) and restored by applyBackup.
 * - OWN_EXPORT: not in the full backup, but has its own
 *   file export/import （我的档案）.
 * - DEVICE_LOCAL: deliberately out — device/mode setup, not
 *   user data a backup should carry to another machine
 *   (the fp_* family rationale, in Store since v0.196.0;
 *   download_origins joins it: system download ids are
 *   meaningless off this device).
 * - SESSION: live session state, meaningless in a backup.
 * - LEGACY: read only as a migration source.
 *
 * The audit behind this test (v0.205.0) walked the export
 * call site, backupToJson, parseBackup and applyBackup
 * field by field: the 18 settings, the four lists and the
 * blocked total are written, parsed and applied
 * symmetrically; the three fingerprint families the task
 * named （我的档案 / 逐组合代理簿 / 保护开关） are out by
 * documented design, the first with its own export path.
 * A future key added to Store without a disposition fails
 * here until it is classified — a field can no longer be
 * silently lost by a backup again.
 */
class BackupKeyAccountingTest {

    private enum class Disposition {
        BACKUP, OWN_EXPORT, DEVICE_LOCAL, SESSION, LEGACY
    }

    private val classified: Map<String, Disposition> = mapOf(
        // Settings slice of the backup.
        "main_engine" to Disposition.BACKUP,
        "incognito_engine" to Disposition.BACKUP,
        "search_engine" to Disposition.BACKUP,
        "theme_mode" to Disposition.BACKUP,
        "ad_block" to Disposition.BACKUP,
        "https_first" to Disposition.BACKUP,
        "guard_script" to Disposition.BACKUP,
        "tp_level" to Disposition.BACKUP,
        "dnt" to Disposition.BACKUP,
        "gpc" to Disposition.BACKUP,
        "webrtc_off" to Disposition.BACKUP,
        "text_zoom" to Disposition.BACKUP,
        "reader_font_size" to Disposition.BACKUP,
        "reader_background" to Disposition.BACKUP,
        "web_dark" to Disposition.BACKUP,
        "startup_restore" to Disposition.BACKUP,
        "download_mode" to Disposition.BACKUP,
        "history_retention" to Disposition.BACKUP,
        // Lists + blocked total.
        "bookmarks" to Disposition.BACKUP,
        "bookmark_folders" to Disposition.BACKUP,
        "history" to Disposition.BACKUP,
        "compat_sites" to Disposition.BACKUP,
        "desktop_sites" to Disposition.BACKUP,
        "total_blocked" to Disposition.BACKUP,
        // 我的档案： its own file export/import (v0.197.0).
        "fp_custom" to Disposition.OWN_EXPORT,
        // Device-local mode setup (documented fp_* rationale).
        "fp_region" to Disposition.DEVICE_LOCAL,
        "fp_timezone" to Disposition.DEVICE_LOCAL,
        "fp_device" to Disposition.DEVICE_LOCAL,
        "fp_language" to Disposition.DEVICE_LOCAL,
        "fp_protect" to Disposition.DEVICE_LOCAL,
        "fp_proxy" to Disposition.DEVICE_LOCAL,
        "fp_proxies" to Disposition.DEVICE_LOCAL,
        "fp_storage_legacy_owner" to Disposition.DEVICE_LOCAL,
        "download_origins" to Disposition.DEVICE_LOCAL,
        // Session state.
        "saved_tabs" to Disposition.SESSION,
        "saved_fp_tabs" to Disposition.SESSION,
        "gecko_downloads" to Disposition.SESSION,
        "wv_incognito_dirty" to Disposition.SESSION,
        // Migration sources only.
        "reading_list" to Disposition.LEGACY,
        "speed_dials" to Disposition.LEGACY
    )

    private fun storeSource(): String {
        val rel = "src/main/java/com/tuxunbrowse/android/data/Store.kt"
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val direct = File(dir, rel)
            if (direct.isFile) return direct.readText()
            val inApp = File(dir, "app/$rel")
            if (inApp.isFile) return inApp.readText()
            dir = dir.parentFile
        }
        error("Store.kt not found from ${System.getProperty("user.dir")}")
    }

    @Test
    fun everyPersistedKeyHasExactlyOneDisposition() {
        val src = storeSource()
        val found = Regex("""KEY_[A-Z_]+ =\s*"([a-z_0-9]+)"""")
            .findAll(src)
            .map { it.groupValues[1] }
            .toSet()
        assertEquals(
            "a Store key is unclassified (or a classified key " +
                "vanished): classify it in BackupKeyAccountingTest",
            classified.keys, found
        )
    }

    @Test
    fun excludedKeysNeverEnterTheBackupWriterOrParser() {
        val src = storeSource()
        val writer = src.substringAfter("fun backupToJson")
            .substringBefore("fun parseBackup")
        val parser = src.substringAfter("fun parseBackup")
            .substringBefore("fun bookmarksToJson")
        val excluded = classified.filterValues {
            it != Disposition.BACKUP
        }.keys
        for (key in excluded) {
            assertFalse(
                "excluded key $key leaked into backupToJson",
                writer.contains("\"$key\"")
            )
            assertFalse(
                "excluded key $key leaked into parseBackup",
                parser.contains("\"$key\"")
            )
        }
    }

    @Test
    fun backupSettingsFieldsAreWrittenAndParsedSymmetrically() {
        val src = storeSource()
        val writer = src.substringAfter("fun backupToJson")
            .substringBefore("fun parseBackup")
        val parser = src.substringAfter("fun parseBackup")
            .substringBefore("fun bookmarksToJson")
        // The 18 settings-slice JSON names: each must be
        // written by backupToJson and read by parseBackup —
        // a field present on only one side is a silent loss.
        val fields = listOf(
            "mainEngine", "incognitoEngine", "searchEngine",
            "adBlockEnabled", "downloadMode", "historyRetention",
            "textZoomPercent", "startupRestore", "themeMode",
            "webDarkMode", "readerFontSize", "readerBackground",
            "httpsFirstEnabled", "guardScriptEnabled",
            "trackingProtectionLevel", "doNotTrackEnabled",
            "globalPrivacyControlEnabled", "webRtcDisabled"
        )
        for (field in fields) {
            assertTrue(
                "$field not written by backupToJson",
                writer.contains("\"$field\"")
            )
            assertTrue(
                "$field not parsed by parseBackup",
                parser.contains("\"$field\"")
            )
        }
    }
}
