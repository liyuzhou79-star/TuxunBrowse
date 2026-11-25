package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Popup-child fingerprint coverage, pinned at the source
 * level (v0.209.0).
 *
 * The subframe audit found that URL-loaded frames are
 * covered by the document-start registration itself (the
 * platform contract: the script runs in ANY frame whose
 * origin matches, and the tab registers with the wildcard
 * rule), but the window.open transport child was not: it
 * carried the storage binding only, so a popup that never
 * navigated to http (about:blank, blob:) answered with the
 * host device's voice. The fix gives the child its own
 * document-start registration plus an evaluate into its
 * main frame when a page starts without being forwarded.
 * These pins keep the wiring from silently regressing:
 *
 * - the child registration exists, with the wildcard rule;
 * - the child handler lives in its OWN slot — the tab's
 *   register/remove pair must never be called on the child
 *   (that bookkeeping belongs to the main WebView);
 * - the not-forwarded page-start path evaluates the
 *   payload into the child;
 * - forward() reports whether it forwarded (the evaluate
 *   above keys off it);
 * - the child handler is dropped at every teardown point
 *   (definition + create-window pre-clear + forward +
 *   destroy + sleep).
 */
class PopupFingerprintCoverageTest {

    private fun tabControllerSource(): String {
        val rel =
            "src/main/java/com/tuxunbrowse/android/engine/TabController.kt"
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val direct = File(dir, rel)
            if (direct.isFile) return direct.readText()
            val inApp = File(dir, "app/$rel")
            if (inApp.isFile) return inApp.readText()
            dir = dir.parentFile
        }
        error("TabController.kt not found from ${System.getProperty("user.dir")}")
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
    fun childGetsItsOwnDocumentStartRegistration() {
        val src = tabControllerSource()
        assertTrue(
            "popup child must be registered with the wildcard rule",
            src.contains("child, fpPayload, setOf(\"*\")")
        )
        assertTrue(
            "popup child handler needs its own slot",
            src.contains("private var popupChildScriptHandler")
        )
    }

    @Test
    fun tabHandlerPairIsNeverUsedOnTheChild() {
        val src = tabControllerSource()
        assertEquals(
            0,
            occurrences(src, "registerFingerprintScriptHandler(child)")
        )
        assertEquals(
            0,
            occurrences(src, "removeFingerprintScriptHandler(child)")
        )
    }

    @Test
    fun blankPopupIsHookedWhenNotForwarded() {
        val src = tabControllerSource()
        assertTrue(
            "forward() must report whether it forwarded",
            src.contains("fun forward(target: String?): Boolean")
        )
        assertTrue(
            "a not-forwarded popup page start must evaluate the payload",
            src.contains("cv.evaluateJavascript(fpPayload, null)")
        )
    }

    @Test
    fun childHandlerIsDroppedAtEveryTeardown() {
        val src = tabControllerSource()
        // Definition + pre-clear at create-window + forward +
        // destroy + sleepEngine.
        assertTrue(
            "removePopupChildScriptHandler wiring regressed",
            occurrences(src, "removePopupChildScriptHandler()") >= 5
        )
    }
}
