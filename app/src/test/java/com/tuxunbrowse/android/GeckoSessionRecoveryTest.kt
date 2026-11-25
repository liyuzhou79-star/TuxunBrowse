package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.geckoFindInfoText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Gecko content-process death recovery + Gecko find counter,
 * pinned at the source level (v0.213.0).
 *
 * The lifecycle audit found the Gecko side had NO recovery
 * from a content-process death: GeckoView 132's
 * ContentDelegate carries onCrash / onKill (both verified
 * against the real AAR with javap; for both, the session is
 * afterwards closed and unusable), and the app overrode
 * neither — a crashed or system-killed Firefox tab sat on its
 * last frame forever, because the tab kept the dead session
 * reference and the sleep pass never touches the active tab.
 * The WebView side has had the twin recovery since
 * onRenderProcessGone was wired. The fix reports the death
 * from both callbacks into one guarded tab handler, and the
 * app side rebuilds the tab in place via the same recreateTab
 * path as an engine switch.
 *
 * The same audit closed a long-standing dual-engine gap: the
 * find bar claimed Gecko "has no counter API" (a v0.9.0-era
 * assumption) and showed a "不显示计数" label instead.
 * SessionFinder.find has returned a GeckoResult<FinderResult>
 * all along (javap-verified: found / current / total, with
 * `current` already 1-based per the toolkit Finder modules
 * inside the AAR). The counter text mapping is a pure
 * function, pinned below; the wiring is pinned at the source
 * level like the v0.209.0 popup coverage.
 */
class GeckoSessionRecoveryTest {

    private fun sourceOf(rel: String): String {
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

    private fun tabControllerSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/engine/TabController.kt"
    )

    private fun browserAppSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
    )

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
    fun findCounterTextMatchesWebViewFormat() {
        assertEquals("3 / 10", geckoFindInfoText(true, 3, 10))
        assertEquals("1 / 1", geckoFindInfoText(true, 1, 1))
        assertEquals("没有找到", geckoFindInfoText(false, 0, 0))
        // A "found" flag with a zero total is still no match.
        assertEquals("没有找到", geckoFindInfoText(true, 0, 0))
    }

    @Test
    fun bothDeathCallbacksAreOverriddenAndGuarded() {
        val src = tabControllerSource()
        assertTrue(
            "onCrash must be overridden on the Gecko content delegate",
            src.contains("override fun onCrash(s: GeckoSession)")
        )
        assertTrue(
            "onKill must be overridden on the Gecko content delegate",
            src.contains("override fun onKill(s: GeckoSession)")
        )
        // Definition + the two callback bodies.
        assertTrue(
            "both death callbacks must route into the one guarded handler",
            occurrences(src, "handleGeckoSessionDied()") >= 3
        )
        assertTrue(
            "the death report must be one-shot per session",
            src.contains("if (geckoDeathReported) return")
        )
        assertTrue(
            "a freshly configured session must reset the death flag",
            src.contains("geckoDeathReported = false")
        )
        assertTrue(
            "the tab must expose the death callback to the app side",
            src.contains("private val onGeckoSessionDied: (String) -> Unit")
        )
    }

    @Test
    fun appSideRebuildsDeadGeckoTabsInPlace() {
        val src = browserAppSource()
        assertTrue(
            "makeTab must wire the death callback into the app handler",
            src.contains("onGeckoSessionDied = { deadUrl ->")
        )
        assertTrue(
            "the app handler must exist",
            src.contains("geckoSessionDiedHandler = { tab, deadUrl ->")
        )
        assertTrue(
            "the rebuild must reuse recreateTab with the Gecko engine",
            src.contains("tab, Engine.GECKO, tab.desktopMode, force = true")
        )
    }

    @Test
    fun geckoFindConsumesTheFinderResult() {
        val src = tabControllerSource()
        assertTrue(
            "the Gecko find branch must consume find()'s GeckoResult",
            src.contains("?.accept { result ->")
        )
        assertTrue(
            "only the newest find request may write the counter",
            src.contains("seq == geckoFindSeq")
        )
        assertTrue(
            "the counter must come from the shared pure mapping",
            src.contains("geckoFindInfoText(")
        )
    }

    @Test
    fun findBarAndSettingsCardNoLongerClaimGeckoHasNoCounter() {
        val src = browserAppSource()
        assertFalse(
            "the find bar must not claim Gecko shows no counter anymore",
            src.contains("火狐内核不显示计数")
        )
        assertFalse(
            "the settings difference card must not list the find " +
                "counter as WebView-only anymore",
            src.contains("Text(\"页内查找计数\")")
        )
        assertTrue(
            "the find counter belongs in the shared-features list now",
            src.contains("Text(\"页内查找（含计数）\")")
        )
    }
}
