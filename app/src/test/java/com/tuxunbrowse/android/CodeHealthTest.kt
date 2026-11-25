package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Code-health pins from the v0.211.0 sweep.
 *
 * Three properties the sweep established by reading, pinned
 * so they stay true:
 *
 * 1. No TODO/FIXME/XXX markers in the main sources — an open
 *    item lives in the checkpoint, not in a comment that
 *    nobody re-reads.
 * 2. The helpers of features deleted in v0.91.0 (the source
 *    viewer, web archives, save-all-tabs-as-bookmarks) stay
 *    deleted: their last orphaned symbols were removed in
 *    this sweep after a whole-repo caller scan found zero
 *    references in main or test code.
 * 3. The console privacy property: the app never overrides
 *    onConsoleMessage and never enables WebView remote
 *    debugging, so page console output (which can carry the
 *    page URL and arbitrary page content) has no path into
 *    the system log through this app; and the one Log call
 *    in the main sources is the startup cache-size line,
 *    which carries a byte count and nothing else.
 */
class CodeHealthTest {

    private fun mainSourceFiles(): List<File> {
        val rel = "src/main/java/com/tuxunbrowse/android"
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val direct = File(dir, rel)
            if (direct.isDirectory) {
                return direct.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            }
            val inApp = File(dir, "app/$rel")
            if (inApp.isDirectory) {
                return inApp.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .toList()
            }
            dir = dir.parentFile
        }
        error("main source tree not found from ${System.getProperty("user.dir")}")
    }

    private fun allMainSource(): String =
        mainSourceFiles().joinToString("\n") { it.readText() }

    @Test
    fun noTodoFixmeXxxMarkersInMainSources() {
        val offenders = mainSourceFiles().filter { f ->
            val text = f.readText()
            text.contains("TODO") || text.contains("FIXME") ||
                text.contains("XXX")
        }
        assertTrue(
            "markers found in: ${offenders.map { it.name }}",
            offenders.isEmpty()
        )
    }

    @Test
    fun deletedFeatureHelpersStayDeleted() {
        val src = allMainSource()
        for (symbol in listOf(
            "SOURCE_DISPLAY_LIMIT",
            "truncateSourceForDisplay",
            "isSourceTruncated",
            "WEB_ARCHIVE_MIME_TYPE",
            "TabBookmarkCandidate"
        )) {
            assertTrue(
                "$symbol reappeared in main sources",
                !src.contains(symbol)
            )
        }
    }

    @Test
    fun pageConsoleHasNoPathToTheSystemLog() {
        val src = allMainSource()
        assertTrue(
            "an onConsoleMessage override appeared",
            !src.contains("onConsoleMessage")
        )
        assertTrue(
            "WebView remote debugging got enabled",
            !src.contains("setWebContentsDebuggingEnabled")
        )
    }

    @Test
    fun theOnlyLogCallIsTheCacheSizeLine() {
        val src = allMainSource()
        var count = 0
        var from = 0
        while (true) {
            val at = src.indexOf("android.util.Log.", from)
            if (at < 0) break
            count++
            from = at + 1
        }
        assertEquals(1, count)
        assertTrue(src.contains("Auto-cleared WebView cache at startup"))
    }
}
