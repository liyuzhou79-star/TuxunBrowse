package com.tuxunbrowse.android

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Incognito-isolation pins from the v0.218.0 incognito audit.
 *
 * The audit walked every surface an incognito tab touches and
 * classified it: truly isolated, intentionally shared and
 * disclosed in the UI copy, or leaking. Two leaks were found and
 * fixed in this version; this file pins both fixes plus the
 * older choke points the audit re-verified, so none of them can
 * silently regress:
 *
 * 1. The lifetime blocked-request counter is persisted (and
 *    rides the backup). Incognito tabs used to feed it through
 *    the same callback as normal tabs, so activity volume from
 *    private sessions survived them as a stored number. The
 *    wiring in makeTab now skips the stored counter for
 *    incognito tabs; the per-tab count (in memory, shown in
 *    the site-info sheet) still runs.
 * 2. The per-site compat memory could be written from an
 *    incognito tab: the engine-switch dialog's "remember this
 *    site" promise and the site-info sheet's compat toggle
 *    both landed the incognito tab's host in the normal-mode
 *    list, where it stayed (and backed up) after the private
 *    session ended. The desktop-mode memory has refused
 *    incognito writes since v0.56.0; the compat paths now
 *    follow the same rule — the switch itself still happens,
 *    the memory rows are hidden in the dialog, and the
 *    site-info toggle explains instead of writing.
 * 3. Re-verified choke points: both engines guard the history
 *    callback with the incognito flag, the recently-closed
 *    stack skips incognito tabs, and the thumbnail capture
 *    choke point refuses incognito tabs in both engines.
 */
class IncognitoIsolationTest {

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

    private fun browserAppText(): String =
        locate(
            "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
        ).readText()

    private fun tabControllerText(): String =
        locate(
            "src/main/java/com/tuxunbrowse/android/engine/TabController.kt"
        ).readText()

    @Test
    fun lifetimeBlockedCounterSkipsIncognitoTabs() {
        val app = browserAppText()
        assertTrue(
            "the persisted lifetime counter must not be fed " +
                "by incognito tabs",
            app.contains(
                "onBlockedRequest = { if (!incognito) store.noteBlocked() }"
            )
        )
    }

    @Test
    fun engineSwitchNeverWritesCompatMemoryFromIncognito() {
        val app = browserAppText()
        assertTrue(
            "the remember-site write in switchTabEngine must " +
                "be gated on the tab not being incognito",
            app.contains(
                "if (rememberSite && host != null && !tab.incognito)"
            )
        )
        assertTrue(
            "the dialog's remember row must be hidden for " +
                "incognito tabs (the promise would not be kept)",
            app.contains(
                "activeTab.engine == Engine.GECKO &&\n" +
                    "                        !activeTab.incognito"
            )
        )
        assertTrue(
            "the dialog's forget row must be hidden for " +
                "incognito tabs too",
            app.contains(
                "currentHostIsCompat && !activeTab.incognito"
            )
        )
    }

    @Test
    fun siteInfoCompatToggleExplainsInsteadOfWritingFromIncognito() {
        val app = browserAppText()
        assertTrue(
            "toggleSiteCompat must refuse from an incognito " +
                "tab with the explanatory toast",
            app.contains("无痕标签里不改兼容记忆")
        )
        // The refusal must sit before the toggle's first list
        // write inside the function body.
        val fnStart = app.indexOf("fun toggleSiteCompat()")
        assertTrue(fnStart >= 0)
        val body = app.substring(fnStart)
        val guardAt = body.indexOf("if (tab.incognito)")
        val writeAt = body.indexOf("addCompatSite(host)")
        assertTrue("incognito guard missing in toggleSiteCompat", guardAt >= 0)
        assertTrue("compat write missing in toggleSiteCompat", writeAt >= 0)
        assertTrue(
            "the incognito guard must precede any compat write",
            guardAt < writeAt
        )
    }

    @Test
    fun historyCallbackStaysGuardedInBothEngines() {
        val tc = tabControllerText()
        assertTrue(
            "WebView history callback must stay incognito-guarded",
            tc.contains(
                "if (!incognito && u.startsWith(\"http\")) onVisit(title, u)"
            )
        )
        assertTrue(
            "Gecko history callback must stay incognito-guarded",
            tc.contains(
                "if (!incognito && newUrl.startsWith(\"http\")) " +
                    "onVisit(title, newUrl)"
            )
        )
    }

    @Test
    fun closedStackAndThumbnailsStayIncognitoFree() {
        val app = browserAppText()
        assertTrue(
            "pushClosedTab must keep skipping incognito tabs",
            app.contains("fun pushClosedTab(tab: BrowserTab) {\n" +
                "        if (tab.incognito) return")
        )
        val tc = tabControllerText()
        assertTrue(
            "the thumbnail choke point must keep refusing " +
                "incognito tabs",
            tc.contains(
                "private fun doCaptureThumbnail(view: WebView) {"
            ) && tc.contains("if (incognito) return")
        )
    }
}
