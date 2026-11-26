package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.primaryLanguageOf
import com.tuxunbrowse.android.engine.resolveTranslateTarget
import com.tuxunbrowse.android.engine.translateErrorCodeOf
import com.tuxunbrowse.android.engine.translateErrorText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mozilla.geckoview.TranslationsController
import java.io.File

/**
 * Page translation (v0.226.0), pinned the same way as the
 * other Gecko features: the pure helpers run for real, and
 * the engine wiring is pinned at the source level.
 *
 * The feature exists because the pinned GeckoView 132 AAR's
 * TranslationsController surface was read with javap and
 * turned out complete (the checkpoint has the feasibility
 * record): RuntimeTranslation statics for engine support,
 * language lists and model management; SessionTranslation
 * for translate / restore; the session translations delegate
 * for detected languages and state. The WebView has no
 * translation API at all, so the menu handler's refusal for
 * WebView tabs and the fingerprint shell's conflict greying
 * are pinned here too — neither may silently turn into a
 * fake "working" entry.
 */
class GeckoTranslateTest {

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

    // ---- Pure helpers ----

    @Test
    fun primaryLanguageTakesTheFirstSubtag() {
        assertEquals("zh", primaryLanguageOf("zh-Hant"))
        assertEquals("en", primaryLanguageOf("en-US"))
        assertEquals("ja", primaryLanguageOf("ja"))
        assertEquals("", primaryLanguageOf(""))
    }

    @Test
    fun storedChoiceWinsWhenStillSupported() {
        assertEquals(
            "ja",
            resolveTranslateTarget(
                "ja", "en-US", listOf("en", "ja", "zh-Hans")
            )
        )
    }

    @Test
    fun storedChoiceFallsBackWhenNoLongerSupported() {
        // A stored code the engine dropped must not be used:
        // resolution continues with the system language.
        assertEquals(
            "en",
            resolveTranslateTarget(
                "xx", "en-US", listOf("en", "ja")
            )
        )
    }

    @Test
    fun systemTagMatchesExactlyThenByPrimarySubtag() {
        assertEquals(
            "zh-Hant",
            resolveTranslateTarget("", "zh-Hant", listOf("en", "zh-Hant"))
        )
        // zh-CN has no exact entry: the first zh-* entry wins
        // (zh-Hans is the mainland system language's match).
        assertEquals(
            "zh-Hans",
            resolveTranslateTarget(
                null, "zh-CN", listOf("en", "zh-Hans", "zh-Hant")
            )
        )
        assertEquals(
            "en",
            resolveTranslateTarget("", "en-GB", listOf("fr", "en"))
        )
    }

    @Test
    fun unsupportedSystemFallsBackToEnglishThenFirst() {
        assertEquals(
            "en",
            resolveTranslateTarget("", "ko-KR", listOf("ja", "en"))
        )
        assertEquals(
            "ja",
            resolveTranslateTarget("", "ko-KR", listOf("ja", "fr"))
        )
        assertNull(resolveTranslateTarget("", "ko-KR", emptyList()))
    }

    @Test
    fun errorCodesMapToPlainLanguage() {
        assertEquals(
            "这台设备的火狐内核不支持翻译", translateErrorText(-2)
        )
        assertEquals(
            "这个语言暂时翻不了", translateErrorText(-6)
        )
        assertEquals(
            "语言包没下载下来，请检查网络后重试", translateErrorText(-9)
        )
        assertEquals("语言包还没下载好", translateErrorText(-11))
        // Unknown codes get the generic retry line, never an
        // invented cause.
        assertEquals("翻译出了问题，请重试", translateErrorText(-99))
        assertEquals("翻译出了问题，请重试", translateErrorText(0))
    }

    @Test
    fun errorCodeComesOnlyFromTranslationsExceptions() {
        assertEquals(
            -9,
            translateErrorCodeOf(
                TranslationsController.TranslationsException(
                    TranslationsController.TranslationsException
                        .ERROR_MODEL_COULD_NOT_DOWNLOAD
                )
            )
        )
        assertEquals(
            -1, translateErrorCodeOf(RuntimeException("boom"))
        )
        assertEquals(-1, translateErrorCodeOf(null))
    }

    // ---- Engine wiring (source pins) ----

    @Test
    fun tabMirrorsEngineTranslationStateThroughTheDelegate() {
        val src = tabControllerSource()
        assertTrue(src.contains("setTranslationsSessionDelegate"))
        assertTrue(src.contains("onTranslationStateChange"))
        assertTrue(src.contains("detectedLanguages"))
        assertTrue(src.contains("requestedTranslationPair"))
        // No auto-prompt: the offer callback stays the
        // interface's default no-op, so translation only ever
        // starts from the menu entry.
        assertFalse(src.contains("onOfferTranslate"))
    }

    @Test
    fun translateAndRestoreGoThroughSessionTranslation() {
        val src = tabControllerSource()
        val body = segment(
            src, "fun translatePage(", "fun captureThumbnail()"
        )
        assertTrue(body.contains("session.sessionTranslation"))
        assertTrue(body.contains("translation.translate("))
        assertTrue(body.contains("TranslationPair("))
        assertTrue(body.contains(".downloadModel(true)"))
        assertTrue(body.contains("restoreOriginalPage()"))
        // A stale session's late answer must not touch the new
        // session's state.
        assertTrue(body.contains("geckoSession === session"))
    }

    @Test
    fun bridgeUsesTheRuntimeTranslationStatics() {
        val src = sourceOf(
            "src/main/java/com/tuxunbrowse/android/engine/GeckoTranslate.kt"
        )
        assertTrue(src.contains("isTranslationsEngineSupported"))
        assertTrue(src.contains("listSupportedLanguages"))
        assertTrue(src.contains("listModelDownloadStates"))
        assertTrue(src.contains("manageLanguageModel"))
        assertTrue(src.contains("checkPairDownloadSize"))
        // Fail fast without a runtime instead of hanging on a
        // result that can never complete.
        assertTrue(src.contains("GeckoHolder.hasRuntime"))
    }

    // ---- Menu / shell behaviour (source pins) ----

    @Test
    fun menuHandlerRefusesWebViewTabsHonestly() {
        val app = browserAppSource()
        val body = segment(
            app, "onTranslatePage = {", "onOpenSniffer = {"
        )
        assertTrue(body.contains("tab.isHome"))
        assertTrue(body.contains("Engine.GECKO"))
        assertTrue(
            body.contains("翻译只支持火狐内核，系统 WebView 标签请先切换内核再用")
        )
        assertTrue(body.contains("translateTab = tab"))
    }

    @Test
    fun bothShellsListTranslateInTheirMenus() {
        val app = browserAppSource()
        val normal = segment(
            app, "显示阅读模式", "安装并建立捷径"
        )
        assertTrue(normal.contains("翻译网页"))
        assertTrue(normal.contains("onTranslatePage()"))
        // Fingerprint shell: present, greyed, conflict-explained
        // (the fifth conflict item) — never a working entry and
        // never silently absent.
        val fp = segment(app, "fun fpConflictToast", "fun shareFpPage")
        assertTrue(fp.contains("可能导致指纹泄漏"))
        val fpMenu = segment(
            app, "---- Fingerprint menu", "if (showTabs)"
        )
        assertTrue(fpMenu.contains("翻译网页"))
        assertTrue(fpMenu.contains("conflict = true"))
        assertTrue(fpMenu.contains("那是档案之外的流量"))
    }

    @Test
    fun settingsHasATranslationPageWithModels() {
        val app = browserAppSource()
        assertTrue(app.contains("Triple(\"translate\", \"翻译\""))
        val page = segment(
            app, "if (targetPage == \"translate\")",
            "if (targetPage == \"about\")"
        )
        assertTrue(page.contains("翻译目标语言"))
        assertTrue(page.contains("语言模型"))
        assertTrue(page.contains("downloadLanguageModel"))
        assertTrue(page.contains("已下载"))
        // The target choice is a real Store write.
        assertTrue(app.contains("store.translateTargetLanguage = lang.code"))
        // The difference card lists translation as a Firefox
        // feature, in plain words.
        val card = segment(app, "火狐的特点", "两个内核都有")
        assertTrue(card.contains("网页翻译"))
    }

    @Test
    fun readmeStatesTranslationHonestly() {
        var dir: File? = File(System.getProperty("user.dir"))
        var readme: String? = null
        while (dir != null) {
            val f = File(dir, "README.md")
            if (f.isFile) {
                readme = f.readText()
                break
            }
            dir = dir.parentFile
        }
        val text = readme ?: error("README.md not found")
        assertTrue(text.contains("网页翻译（火狐标签）"))
        assertTrue(text.contains("系统 WebView 没有翻译引擎"))
    }
}
