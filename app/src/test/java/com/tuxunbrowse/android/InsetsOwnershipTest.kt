package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Inset ownership, pinned at the source level (v0.193.0).
 *
 * The rule (see ToolbarSpec): every system inset has
 * exactly ONE consumer per shell. v0.191.0 broke it twice
 * in opposite directions — it deleted the normal toolbar
 * row's only status-bar padding (the row is a hand-built
 * Row in the Scaffold topBar slot, not a TopAppBar, so
 * nothing else pads it) and the row fused with the status
 * bar on the real device; and it folded the navigation
 * inset into the bottom bars' heights with the icon row
 * centred across body+inset, sinking the icons toward
 * the gesture bar. Both times the "fix" was found by
 * counting consumers, not by tuning heights — so the
 * counts are what this test pins.
 *
 * The scan reads BrowserApp.kt from the module source
 * tree (unit tests run with the module dir as working
 * dir; the walk-up fallback covers other runners).
 */
class InsetsOwnershipTest {

    private fun browserAppSource(): String {
        val rel =
            "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val direct = File(dir, rel)
            if (direct.isFile) return direct.readText()
            val inApp = File(dir, "app/$rel")
            if (inApp.isFile) return inApp.readText()
            dir = dir.parentFile
        }
        error("BrowserApp.kt not found from ${System.getProperty("user.dir")}")
    }

    private fun occurrences(haystack: String, needle: String): Int {
        var count = 0
        var from = 0
        while (true) {
            val at = haystack.indexOf(needle, from)
            if (at < 0) return count
            count++
            from = at + needle.length
        }
    }

    @Test
    fun statusBarInsetHasExactlyOnePaddingConsumer() {
        val src = browserAppSource()
        // The normal shell's toolbar row is the single
        // owner. The fingerprint shell must NOT add one:
        // its Scaffold content padding owns the inset
        // there (a second one was the v0.191.0 double-
        // inset identity strip). Code occurrences only —
        // the needle includes the call's opening paren, so
        // prose mentions in comments do not count.
        assertEquals(
            1,
            occurrences(
                src,
                ".windowInsetsPadding(WindowInsets.statusBars)"
            )
        )
    }

    @Test
    fun navigationBarInsetIsPaddedOutsideBothBottomBars() {
        val src = browserAppSource()
        // One per shell, both in the padded-outside-the-
        // body form on the bar Surface.
        assertEquals(
            2,
            occurrences(
                src,
                "WindowInsets.navigationBars.only("
            )
        )
        // The v0.191.0 fold-it-into-the-height construction
        // must never come back: bar height is the body
        // constant alone, never body + a live inset read.
        assertEquals(
            0,
            occurrences(
                src,
                "BOTTOM_BAR_HEIGHT_DP.dp +"
            )
        )
        assertTrue(
            src.contains(
                ".height(ToolbarSpec.BOTTOM_BAR_HEIGHT_DP.dp)"
            )
        )
    }

    @Test
    fun identityStripNeverPadsItsOwnStatusInset() {
        val src = browserAppSource()
        val fpStart = src.indexOf("private fun FingerprintShell(")
        assertTrue(fpStart >= 0)
        val fp = src.substring(fpStart)
        // Inside the fingerprint shell, the only inset
        // padding allowed is the bottom bar's navigation
        // one (asserted above); a statusBars padding here
        // would double the Scaffold content padding again.
        assertEquals(
            0,
            occurrences(
                fp,
                ".windowInsetsPadding(WindowInsets.statusBars)"
            )
        )
    }
}
