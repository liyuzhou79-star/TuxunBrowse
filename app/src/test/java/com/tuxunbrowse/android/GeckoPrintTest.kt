package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Old-conclusion review, second batch (v0.216.0), pinned at the
 * source level like the v0.213.0/v0.214.0 Gecko tests.
 *
 * The first weeks' note "Gecko tabs cannot print" was
 * re-checked against the pinned GeckoView 132 AAR (method
 * tables and the official sources jar, not memory): it does
 * not hold. GeckoSession.saveAsPdf() returns a
 * GeckoResult<InputStream> of the engine-rendered PDF, and
 * the stock GeckoViewPrintDocumentAdapter feeds that stream
 * to the system print manager. Gecko tabs now print through
 * the same menu entry as WebView tabs; the only user-visible
 * difference left is the internal path, which the settings
 * difference card states.
 *
 * The copies that stated the old conclusion (the print
 * handler's Gecko toast, the difference card's WebView-only
 * list, the README line) were updated in the same version;
 * the last tests pin that they do not creep back.
 */
class GeckoPrintTest {

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

    private fun readmeSource(): String {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val f = File(dir, "README.md")
            if (f.isFile) return f.readText()
            dir = dir.parentFile
        }
        error("README.md not found from ${System.getProperty("user.dir")}")
    }

    private fun segment(
        src: String, startMarker: String, endMarker: String
    ): String {
        val start = src.indexOf(startMarker)
        assertTrue("start marker missing: $startMarker", start >= 0)
        val end = src.indexOf(endMarker, start)
        assertTrue("end marker missing: $endMarker", end > start)
        return src.substring(start, end)
    }

    @Test
    fun geckoPrintGoesThroughSaveAsPdfAndStockAdapter() {
        val src = tabControllerSource()
        assertTrue(
            src.contains(
                "import org.mozilla.geckoview." +
                    "GeckoViewPrintDocumentAdapter"
            )
        )
        val body = segment(
            src, "fun printPage(", "fun captureThumbnail()"
        )
        assertTrue(body.contains("session.saveAsPdf()"))
        assertTrue(body.contains("GeckoViewPrintDocumentAdapter("))
        // Same job-name rule as the WebView path.
        assertTrue(body.contains("title.ifBlank"))
        // The async failure callback is part of the contract:
        // a Gecko print that fails after acceptance must reach
        // the caller's retry toast, not vanish.
        assertTrue(body.contains("onAsyncFailure"))
        // A stale result (tab destroyed/slept/rebuilt while the
        // engine rendered) must be dropped, not printed.
        assertTrue(body.contains("geckoSession !== session"))
    }

    @Test
    fun webViewPrintPathIsUnchanged() {
        val src = tabControllerSource()
        val body = segment(
            src, "fun printPage(", "fun captureThumbnail()"
        )
        assertTrue(body.contains("createPrintDocumentAdapter"))
    }

    @Test
    fun printHandlerHasNoGeckoRefusalLeft() {
        val app = browserAppSource()
        assertFalse(app.contains("火狐标签不能打印"))
        val body = segment(
            app, "onPrintPage = {", "onOpenSniffer = {"
        )
        // Home is still checked first, and the handler passes
        // the async failure callback through.
        assertTrue(body.contains("tab.isHome"))
        assertTrue(body.contains("tab.printPage(context, showRetry)"))
        assertTrue(body.contains("打印没能打开，请重试"))
    }

    @Test
    fun differenceCardListsPrintAsShared() {
        val app = browserAppSource()
        val webViewOnly = segment(app, "只有系统 WebView 有", "火狐的特点")
        assertFalse(webViewOnly.contains("打印 / 存为 PDF"))
        val shared = segment(
            app, "两个内核都有", "if (targetPage == \"privacy\")"
        )
        assertTrue(shared.contains("打印 / 存为 PDF"))
        // The shared entry states the different internal path.
        assertTrue(shared.contains("火狐由内核先生成 PDF"))
    }

    @Test
    fun readmeNoLongerClaimsWebViewOnlyPrint() {
        val readme = readmeSource()
        assertFalse(readme.contains("打印只支持系统 WebView"))
        assertFalse(readme.contains("火狐标签点按会说明原因"))
    }
}
