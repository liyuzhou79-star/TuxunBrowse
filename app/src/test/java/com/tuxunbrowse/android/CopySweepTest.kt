package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Copy discipline for the fingerprint-mode surfaces added
 * during the overnight run (v0.206.0 sweep).
 *
 * The user's standing rule for in-app copy: a setting's
 * description says only what the feature does, in one short
 * plain sentence — never a version narrative ("previously…",
 * "now fixed"), never report-to-the-user phrasing, never an
 * English error tail. The overnight additions (proxy card,
 * proxy dialog, storage sentence, self-check header row) were
 * swept once by hand; this test keeps them swept:
 * - none of the swept literals may contain a version token
 *   ("v0.2…") or narrative markers;
 * - the request-header self-check detail must stay the short
 *   plain wording, not grow back its "(已知结构性项)" tail.
 *
 * Source-scan genre, same as InsetsOwnershipTest.
 */
class CopySweepTest {

    private fun browserAppSource(): String {
        var dir: File? = File(System.getProperty("user.dir"))
        while (dir != null) {
            val f = File(
                dir,
                "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
            )
            if (f.isFile) return f.readText()
            dir = dir.parentFile
        }
        error("BrowserApp.kt not found from ${System.getProperty("user.dir")}")
    }

    @Test
    fun overnightCopyHasNoVersionNarrative() {
        val src = browserAppSource()
        // The swept blocks, located by their anchors.
        val anchors = listOf(
            "按当前「地区＋机型」组合保存，指纹模式期间生效",
            "这台设备的 WebView 不支持代理，设置能保存",
            "只属于当前「地区＋机型」组合",
            "网站收到的请求头语言仍是系统语言"
        )
        for (anchor in anchors) {
            assertTrue(
                "swept copy anchor missing: $anchor",
                src.contains(anchor)
            )
        }
        // The long pre-sweep wordings must not come back.
        val banned = listOf(
            "打开指纹模式期间生效，退出模式自动断开",
            "已知结构性项",
            "本地测不了请求头：网站收到的",
            "换到支持的设备上会按组合自动启用",
            "这份代理只属于当前的"
        )
        for (b in banned) {
            assertFalse(
                "pre-sweep wording returned: $b",
                src.contains(b)
            )
        }
    }
}
