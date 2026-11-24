package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.engine.EngineFallbackAction
import com.tuxunbrowse.android.engine.engineFallbackActions
import com.tuxunbrowse.android.engine.engineFallbackReason
import com.tuxunbrowse.android.engine.otherEngineOpenLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Engine-unavailable fallback (v0.81.0): which buttons the page
 * offers is a pure decision — retry and go-home always, the
 * switch-engine button only when there is a real http(s) site to
 * reopen in the other engine. The labels must name the OTHER
 * engine, following the failing tab's engine, so after a failed
 * switch the button points back the way it came.
 */
class EngineFallbackTest {

    @Test
    fun httpsPageOffersAllThreeActionsSwitchFirst() {
        assertEquals(
            listOf(
                EngineFallbackAction.SWITCH_ENGINE,
                EngineFallbackAction.RETRY,
                EngineFallbackAction.GO_HOME
            ),
            engineFallbackActions("https://example.com/some/page?x=1")
        )
    }

    @Test
    fun httpPageOffersSwitchToo() {
        assertTrue(
            EngineFallbackAction.SWITCH_ENGINE in
                engineFallbackActions("http://example.com/")
        )
    }

    @Test
    fun homeUrlHasNoSwitchAction() {
        assertEquals(
            listOf(
                EngineFallbackAction.RETRY,
                EngineFallbackAction.GO_HOME
            ),
            engineFallbackActions(HOME_URL)
        )
    }

    @Test
    fun blankAndNonWebUrlsHaveNoSwitchAction() {
        listOf("", "about:blank", "file:///sdcard/a.html").forEach { url ->
            val actions = engineFallbackActions(url)
            assertFalse(
                "switch must not be offered for '$url'",
                EngineFallbackAction.SWITCH_ENGINE in actions
            )
            assertTrue(EngineFallbackAction.RETRY in actions)
            assertTrue(EngineFallbackAction.GO_HOME in actions)
        }
    }

    @Test
    fun switchLabelNamesTheOtherEngine() {
        // The failing engine is Gecko -> the button offers WebView.
        assertEquals(
            "用系统 WebView 打开这个网站",
            otherEngineOpenLabel(Engine.GECKO)
        )
        // The failing engine is WebView -> the button offers Firefox.
        assertEquals(
            "用火狐打开这个网站",
            otherEngineOpenLabel(Engine.WEBVIEW)
        )
    }

    @Test
    fun reasonNamesTheFailingEngineAndTheWayOut() {
        val geckoReason = engineFallbackReason(Engine.GECKO)
        assertTrue(geckoReason.contains("火狐内核"))
        assertTrue(geckoReason.contains("系统 WebView"))
        val webViewReason = engineFallbackReason(Engine.WEBVIEW)
        assertTrue(webViewReason.contains("系统 WebView"))
        assertTrue(webViewReason.contains("火狐"))
    }
}
