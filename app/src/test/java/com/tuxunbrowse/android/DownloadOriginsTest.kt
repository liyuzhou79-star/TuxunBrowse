package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.DownloadOrigin
import com.tuxunbrowse.android.engine.DownloadOrigins
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Download-origin side table (v0.205.0): a failed download
 * born in fingerprint mode must retry with its own
 * combination's cookie jar, not the default persona's. The
 * table's pure contract (round trip, broken input, pruning,
 * the retry decision) is pinned here; the wiring — the tab
 * download paths record, the downloads screen's retry reads
 * — is pinned at the source level below, in the
 * InsetsOwnershipTest style.
 */
class DownloadOriginsTest {

    private val jp = DownloadOrigin("fp_jp_pixel", "日本 · Pixel 8")
    private val us = DownloadOrigin("fp_us_s24", "美国 · Galaxy S24")

    @Test
    fun tableRoundTrips() {
        val table = mapOf(11L to jp, 27L to us)
        assertEquals(table, DownloadOrigins.decode(DownloadOrigins.encode(table)))
    }

    @Test
    fun emptyAndBrokenInputReadsAsEmptyTable() {
        assertTrue(DownloadOrigins.decode(null).isEmpty())
        assertTrue(DownloadOrigins.decode("").isEmpty())
        assertTrue(DownloadOrigins.decode("not json").isEmpty())
        assertTrue(DownloadOrigins.decode("[1,2]").isEmpty())
        // A row without a jar name is not an origin at all.
        assertTrue(
            DownloadOrigins.decode("""{"5":{"label":"x"}}""").isEmpty()
        )
        // A non-numeric id is skipped, the good row survives.
        val mixed = DownloadOrigins.decode(
            """{"abc":{"jar":"j","label":"l"},"7":{"jar":"k","label":"m"}}"""
        )
        assertEquals(mapOf(7L to DownloadOrigin("k", "m")), mixed)
    }

    @Test
    fun blankJarOriginsAreNeverWritten() {
        val encoded = DownloadOrigins.encode(
            mapOf(1L to DownloadOrigin("", "label"), 2L to jp)
        )
        assertEquals(mapOf(2L to jp), DownloadOrigins.decode(encoded))
    }

    @Test
    fun pruneDropsDeadIdsAndKeepsLiveOnes() {
        val table = mapOf(1L to jp, 2L to us, 3L to jp)
        assertEquals(
            mapOf(2L to us),
            DownloadOrigins.prune(table, setOf(2L, 99L))
        )
        assertEquals(table, DownloadOrigins.prune(table, setOf(1L, 2L, 3L)))
    }

    @Test
    fun capDropsTheOldestIds() {
        val big = (1L..(DownloadOrigins.MAX_ENTRIES + 5)).associateWith { jp }
        val capped = DownloadOrigins.cap(big)
        assertEquals(DownloadOrigins.MAX_ENTRIES, capped.size)
        // The five smallest (oldest) ids are gone, the newest stay.
        assertNull(capped[1L])
        assertNull(capped[5L])
        assertEquals(jp, capped[6L])
        assertEquals(jp, capped[(DownloadOrigins.MAX_ENTRIES + 5).toLong()])
    }

    @Test
    fun retryDecisionFollowsTheOrigin() {
        // No origin (or a jar-less one) keeps the plain
        // default-jar retry; a real origin routes the retry
        // through the fingerprint jar.
        assertFalse(DownloadOrigins.usesFingerprintJar(null))
        assertFalse(DownloadOrigins.usesFingerprintJar(DownloadOrigin("", "x")))
        assertTrue(DownloadOrigins.usesFingerprintJar(jp))
    }

    // ---- Source-level wiring pins ----

    private fun source(rel: String): String {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val direct = File(dir, rel)
            if (direct.isFile) return direct.readText()
            val inApp = File(dir, "app/$rel")
            if (inApp.isFile) return inApp.readText()
            dir = dir.parentFile
        }
        error("$rel not found from ${System.getProperty("user.dir")}")
    }

    @Test
    fun retryReadsTheOriginJarAndStartReturnsTheId() {
        val app = source(
            "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
        )
        val retry = app.substringAfter("fun retryDownload")
            .substringBefore("fun openDownloadedFile")
        // The retry consults the record's origin and, for a
        // fingerprint-born record, takes the cookie through
        // FpStorage.downloadCookie for THAT origin's jar.
        assertTrue(retry.contains("entry.origin"))
        assertTrue(retry.contains("FpStorage"))
        assertTrue(retry.contains("downloadCookie(source, it.storageName)"))
        // And the retried record inherits the origin.
        assertTrue(retry.contains("recordDownloadOrigin(newId, origin)"))
        // The recorder helper exists once and both fingerprint
        // download entry points (download listener, direct-link
        // intercept) call it: three occurrences of the name in
        // total — declaration plus two call sites.
        assertEquals(
            3,
            app.split("recordDownloadOriginIfFingerprint(").size - 1
        )
        val engine = source(
            "src/main/java/com/tuxunbrowse/android/engine/TabController.kt"
        )
        val start = engine.substringAfter("fun startSystemDownload")
            .substringBefore("private val screenshotExecutor")
        assertTrue(start.contains("): Long?"))
        assertTrue(start.contains("return downloadId"))
    }
}
