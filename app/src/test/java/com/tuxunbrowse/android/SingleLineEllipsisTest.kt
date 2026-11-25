package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Final UI/copy sweep (v0.215.0): single-line text must clip
 * gracefully.
 *
 * A Compose `Text` with `maxLines = 1` and no explicit overflow
 * uses TextOverflow.Clip — overlong text is cut mid-glyph with
 * no indication anything is missing. Most of the app already
 * pairs every single-line Text with TextOverflow.Ellipsis; the
 * sweep found the remaining eleven in the two suggestion lists
 * (bookmark titles and URLs are arbitrary-length), the home
 * search placeholder, the engine-switch chip, the clipboard
 * chip and the address-bar placeholder pill, and gave them the
 * same treatment. This test pins the invariant for both UI
 * files that hold single-line Texts: every `maxLines = 1`
 * line must have an Ellipsis within a few lines of it.
 *
 * The same sweep closed a stale copy left behind by v0.214.0:
 * the tab grid still carried a Gecko-only "no preview" card
 * branch (and a comment claiming GeckoView has no screenshot
 * API) even though Gecko tabs have captured real thumbnails
 * via capturePixels since that version. The branch is gone —
 * a Gecko tab without a capture yet now shows the same
 * generic placeholder as a WebView tab — and the second test
 * pins that the stale wording does not creep back.
 */
class SingleLineEllipsisTest {

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

    private fun browserAppSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
    )

    private fun pillSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/ui/PillTextField.kt"
    )

    private fun unclippedSingleLines(src: String): List<Int> {
        val lines = src.split("\n")
        val bad = mutableListOf<Int>()
        for (i in lines.indices) {
            if ("maxLines = 1" in lines[i]) {
                val window = lines.subList(
                    maxOf(0, i - 4),
                    minOf(lines.size, i + 7)
                ).joinToString("\n")
                if ("Ellipsis" !in window) bad += i + 1
            }
        }
        return bad
    }

    @Test
    fun everySingleLineTextInBrowserAppEllipsizes() {
        val bad = unclippedSingleLines(browserAppSource())
        assertTrue(
            "single-line Texts without Ellipsis at lines $bad",
            bad.isEmpty()
        )
    }

    @Test
    fun addressPlaceholderPillEllipsizes() {
        val bad = unclippedSingleLines(pillSource())
        assertTrue(
            "single-line Texts without Ellipsis at lines $bad",
            bad.isEmpty()
        )
    }

    @Test
    fun staleGeckoNoPreviewCopyDoesNotCreepBack() {
        val app = browserAppSource()
        assertFalse(app.contains("火狐标签暂无预览"))
        assertFalse(app.contains("no screenshot API"))
        // The generic placeholder both engines fall back to
        // before their first capture must stay.
        assertTrue(app.contains("contentDescription = \"暂无预览\""))
    }
}
