package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Privacy-surface pins from the v0.217.0 system-surface audit.
 *
 * Three properties established by reading the manifest and the
 * shell code, pinned so they stay true:
 *
 * 1. System backup is OFF. With allowBackup on and no exclusion
 *    rules, Android cloud backup and device-to-device transfer
 *    carry the app's whole state (history, bookmarks,
 *    fingerprint profiles, proxy book, settings) off the device
 *    by default. The app's own hand-triggered backup export is
 *    the supported migration path and is unaffected. If backup
 *    is ever re-enabled, it must come back WITH exclusion rules
 *    — this test failing is the reminder to write them.
 * 2. The exported surface stays exactly one activity. The
 *    FileProvider stays unexported (per-intent grants only) and
 *    its path roots stay app-specific: no root-path (the whole
 *    filesystem) and no external-path (the whole shared
 *    storage) may ever appear in file_paths.xml.
 * 3. The Recents snapshot protection stays wired: the shell
 *    derives "what is on screen is private" from fingerprint
 *    mode or an incognito active tab and applies
 *    setRecentsScreenshotEnabled behind the API 33 guard.
 */
class PrivacySurfaceTest {

    private fun locate(rel: String): File {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val direct = File(dir, rel)
            if (direct.isFile) return direct
            val inApp = File(dir, "app/$rel")
            if (inApp.isFile) return inApp
            dir = dir.parentFile
        }
        error("$rel not found from ${System.getProperty("user.dir")}")
    }

    private fun manifestText(): String =
        locate("src/main/AndroidManifest.xml").readText()

    private fun browserAppText(): String =
        locate(
            "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
        ).readText()

    @Test
    fun systemBackupIsOffAndNoBackupRulesDeclared() {
        val manifest = manifestText()
        assertTrue(
            "allowBackup must be false (v0.217.0 privacy audit)",
            manifest.contains("android:allowBackup=\"false\"")
        )
        assertFalse(
            manifest.contains("android:allowBackup=\"true\"")
        )
        // If either attribute ever appears, backup semantics are
        // being revisited and this audit must be redone.
        assertFalse(manifest.contains("dataExtractionRules"))
        assertFalse(manifest.contains("fullBackupContent"))
    }

    @Test
    fun onlyMainActivityIsExportedAndProviderStaysPrivate() {
        val manifest = manifestText()
        assertEquals(
            "exactly one component may be exported: MainActivity",
            1,
            manifest.split("android:exported=\"true\"").size - 1
        )
        assertTrue(
            "the FileProvider must stay unexported",
            manifest.contains("android:exported=\"false\"")
        )
        assertTrue(
            manifest.contains("android:grantUriPermissions=\"true\"")
        )
    }

    @Test
    fun fileProviderRootsStayAppSpecific() {
        val paths = locate("src/main/res/xml/file_paths.xml").readText()
        assertFalse(
            "root-path would expose the whole filesystem",
            paths.contains("root-path")
        )
        assertFalse(
            "external-path would expose the whole shared storage; " +
                "only external-files-path (app-specific) is allowed",
            paths.contains("<external-path")
        )
        assertTrue(paths.contains("<external-files-path"))
        assertTrue(paths.contains("<files-path"))
    }

    @Test
    fun recentsSnapshotProtectionStaysWired() {
        val app = browserAppText()
        assertTrue(
            "the sensitive derivation must cover both private contexts",
            app.contains(
                "fingerprintMode || activeTab()?.incognito == true"
            )
        )
        assertTrue(
            app.contains("setRecentsScreenshotEnabled(!recentsSnapshotSensitive)")
        )
        assertTrue(
            "the API 33 (TIRAMISU) guard must stay around the call",
            app.contains("android.os.Build.VERSION_CODES.TIRAMISU")
        )
        assertFalse(
            "FLAG_SECURE must not come back: it blocks the user's " +
                "own screenshots, which fingerprint testing relies on",
            app.contains("LayoutParams.FLAG_SECURE")
        )
    }
}
