package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Launcher-icon pins from v0.219.0, when the user-picked final
 * artwork (geometric two-tone: violet + electric blue browser
 * window frame with fingerprint arcs) replaced the placeholder
 * line-drawn globe vector.
 *
 * 1. The adaptive icons keep their two-layer shape: the sampled
 *    artwork ground colour as background, per-density bitmap
 *    foregrounds as foreground. The placeholder vector must not
 *    come back — a full-colour bitmap cannot be a tinted vector,
 *    and the home brand block was built around that vector.
 * 2. The background colour stays the artwork's own ground
 *    (#4001A6, median-sampled from the final artwork), so the
 *    foreground bitmap's ground and the adaptive background are
 *    indistinguishable where a launcher mask reveals the layer
 *    beneath.
 * 3. In-app display of the icon uses the drawable-nodpi bitmap
 *    copy (R.drawable.app_icon). painterResource on
 *    R.mipmap.ic_launcher resolves to the adaptive-icon XML,
 *    which painterResource cannot load — it crashes the first
 *    frame on Android 8+ (standing lesson, AGENTS.md).
 */
class LauncherIconTest {

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

    private fun exists(rel: String): Boolean = try {
        locate(rel); true
    } catch (e: IllegalStateException) {
        false
    }

    @Test
    fun adaptiveIconsUseColourBackgroundAndBitmapForeground() {
        for (name in listOf("ic_launcher.xml", "ic_launcher_round.xml")) {
            val text = locate("src/main/res/mipmap-anydpi-v26/$name").readText()
            assertTrue(
                "$name must keep the colour background layer",
                text.contains("@color/ic_launcher_background")
            )
            assertTrue(
                "$name must use the bitmap foreground",
                text.contains("@mipmap/ic_launcher_foreground")
            )
            assertFalse(
                "$name must not reference the deleted vector",
                text.contains("@drawable/ic_launcher_foreground")
            )
        }
    }

    @Test
    fun backgroundColourIsTheArtworkGround() {
        val colors = locate("src/main/res/values/colors.xml").readText()
        assertTrue(
            "ic_launcher_background must stay the sampled artwork ground",
            colors.contains(
                "<color name=\"ic_launcher_background\">#4001A6</color>"
            )
        )
    }

    @Test
    fun foregroundBitmapsExistAtAllDensitiesAndPlaceholderIsGone() {
        for (dens in listOf("mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi")) {
            assertTrue(
                "mipmap-$dens foreground bitmap missing",
                exists("src/main/res/mipmap-$dens/ic_launcher_foreground.png")
            )
        }
        assertFalse(
            "the placeholder vector must not come back",
            exists("src/main/res/drawable/ic_launcher_foreground.xml")
        )
    }

    @Test
    fun inAppDisplayUsesTheNodpiBitmapCopy() {
        assertTrue(
            "drawable-nodpi bitmap copy missing",
            exists("src/main/res/drawable-nodpi/app_icon.png")
        )
        val shell = locate(
            "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
        ).readText()
        assertTrue(
            "the home brand block must render the bitmap copy",
            shell.contains("R.drawable.app_icon")
        )
        assertFalse(
            "in-app code must never load the adaptive XML as a painter",
            shell.contains("R.mipmap.ic_launcher")
        )
        assertFalse(
            "the deleted vector must not be referenced",
            shell.contains("ic_launcher_foreground")
        )
    }
}
