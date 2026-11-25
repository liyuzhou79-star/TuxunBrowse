package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Old-conclusion review, second batch (v0.214.0), pinned at the
 * source level like the v0.213.0 recovery test.
 *
 * Three Gecko "has no API" notes from the project's first weeks
 * were re-checked against the pinned GeckoView 132 AAR (method
 * tables read out of the real classes.jar, not from memory):
 *
 *  - "Gecko tabs have no screenshot API" (v0.10/v0.14 era) —
 *    WRONG: GeckoView.capturePixels() exists and returns a
 *    GeckoResult<Bitmap> of the composited frame. Gecko tabs now
 *    get tab-grid thumbnails and the visible-area page
 *    screenshot, through the same incognito guard as WebView.
 *  - "Gecko blocking has no count" (v0.19 era) — WRONG:
 *    GeckoSession.setContentBlockingDelegate exists, and
 *    ContentBlocking.Delegate.onContentBlocked reports each
 *    decision with BlockEvent.isBlocking(). Blocked events now
 *    feed the same per-page counter and lifetime total the
 *    WebView interceptor feeds.
 *
 * The user-visible copies that stated the old conclusions
 * (screenshot toast, site-info sheet, settings totals, the
 * dual-engine difference card, README) were updated in the same
 * version; the last test pins that they do not creep back.
 */
class GeckoCaptureAndBlockCountTest {

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
    fun geckoThumbnailGoesThroughCapturePixels() {
        val src = tabControllerSource()
        assertTrue(src.contains("fun captureGeckoThumbnail"))
        assertTrue(src.contains("capturePixels()"))
        // The thumbnail is scaled to the same 360 px grid width
        // and registered with the same app-wide LRU cap.
        val body = segment(
            src, "fun captureGeckoThumbnail", "fun captureThumbnailDeferred"
        )
        assertTrue(body.contains("createScaledBitmap"))
        assertTrue(body.contains("ThumbnailCache.onThumbnailSet"))
    }

    @Test
    fun geckoThumbnailKeepsTheIncognitoGuard() {
        val src = tabControllerSource()
        // captureThumbnail() must refuse incognito tabs BEFORE
        // reaching the Gecko capture, exactly like the WebView
        // choke point refuses them: a private page's preview
        // bitmap is a leak in any engine.
        val body = segment(
            src, "fun captureThumbnail()", "private fun captureGeckoThumbnail"
        )
        assertTrue(body.contains("if (incognito) return"))
        assertTrue(body.contains("captureGeckoThumbnail(geckoView"))
    }

    @Test
    fun geckoPageStopCapturesThumbnail() {
        val src = tabControllerSource()
        val body = segment(
            src, "override fun onPageStop", "session.contentBlockingDelegate"
        )
        assertTrue(body.contains("captureThumbnail()"))
    }

    @Test
    fun contentBlockingDelegateCountsBlockedEvents() {
        val src = tabControllerSource()
        val body = segment(
            src, "session.contentBlockingDelegate", "session.contentDelegate"
        )
        assertTrue(body.contains("onContentBlocked"))
        assertTrue(body.contains("event.isBlocking"))
        // Same counter and same lifetime-total hook as the
        // WebView interceptor.
        assertTrue(body.contains("blockedCount++"))
        assertTrue(body.contains("onBlockedRequest()"))
    }

    @Test
    fun screenshotFlowUsesTheAsyncCapture() {
        val tab = tabControllerSource()
        assertTrue(tab.contains("fun captureVisibleBitmapAsync"))
        val app = browserAppSource()
        assertTrue(app.contains("captureVisibleBitmapAsync"))
    }

    @Test
    fun oldConclusionCopyDoesNotCreepBack() {
        val app = browserAppSource()
        assertFalse(app.contains("火狐标签不支持网页截图"))
        assertFalse(app.contains("没有个数统计"))
        assertFalse(app.contains("火狐标签不统计"))
        assertFalse(app.contains("WebView 标签统计"))
        // The difference card: the three closed gaps must sit in
        // the shared section, not the WebView-only one.
        val webViewOnly = segment(app, "只有系统 WebView 有", "火狐的特点")
        assertFalse(webViewOnly.contains("网页截图"))
        assertFalse(webViewOnly.contains("标签预览图"))
        assertFalse(webViewOnly.contains("本页拦截计数"))
        val shared = segment(app, "两个内核都有", "if (targetPage == \"privacy\")")
        assertTrue(shared.contains("网页截图"))
        assertTrue(shared.contains("标签预览图"))
        assertTrue(shared.contains("本页拦截计数"))
    }
}
