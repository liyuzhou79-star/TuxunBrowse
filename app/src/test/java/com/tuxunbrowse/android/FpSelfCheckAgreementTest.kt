package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Self-check ↔ payload agreement, pinned at the source
 * level (v0.204.0).
 *
 * The fingerprint self-check page (v0.184.0, deepened in
 * v0.201.0) judges the live page against expectations, and
 * two of those expectations duplicate facts that also live
 * in the injected payload:
 * - The media-devices row expects exactly the sanitised
 *   device list the payload answers enumerateDevices with
 *   (one mic, one camera, one speaker, empty labels). The
 *   list is a literal in FingerprintScript.kt; the
 *   expectation is a literal in the probe in
 *   BrowserApp.kt. If one side is ever edited without the
 *   other, the row would cry wolf (or stay silent) on every
 *   device — so the two literals are reconciled here.
 * - The voices row takes its expectation (count + language
 *   multiset) from the resolved profile's own voice list —
 *   the same list the payload injects. That single-source
 *   wiring is pinned too: the EXP builder must read
 *   profile.voices, and the payload builder must read the
 *   profile's voices, never a second hand-kept copy.
 *
 * Source-scan genre, same as InsetsOwnershipTest: the
 * scan reads the module source tree (unit tests run with
 * the module dir as working dir; the walk-up fallback
 * covers other runners).
 */
class FpSelfCheckAgreementTest {

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

    private fun fingerprintScriptSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/engine/" +
            "FingerprintScript.kt"
    )

    private fun browserAppSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
    )

    @Test
    fun spoofedDeviceListMatchesTheProbeExpectation() {
        val script = fingerprintScriptSource()
        // The payload's enumerateDevices answer: the device()
        // calls inside the resolved array.
        val kinds = Regex("""device\('([a-z]+)',""")
            .findAll(script)
            .map { it.groupValues[1] }
            .toList()
        assertEquals(
            "payload must answer exactly one mic, one camera, " +
                "one speaker",
            listOf("audioinput", "videoinput", "audiooutput"),
            kinds
        )
        // The probe's expectation: the count and the sorted
        // kind set, as literals in fpSelfCheckProbe.
        val app = browserAppSource()
        val expectedKinds = kinds.sorted().joinToString(",")
        assertTrue(
            "probe must expect the payload's kind set",
            app.contains("kinds === '$expectedKinds'")
        )
        assertTrue(
            "probe must expect the payload's device count",
            app.contains("devs.length === ${kinds.size}")
        )
        // Every spoofed device carries an empty label — the
        // probe fails the row on any label text (host leak).
        assertTrue(
            "payload devices must keep empty labels",
            script.contains("def(d, 'label', '');")
        )
    }

    @Test
    fun voicesExpectationAndPayloadShareOneSource() {
        val app = browserAppSource()
        assertTrue(
            "EXP voice expectation must read the profile's list",
            app.contains(
                ".put(\"voiceCount\", profile.voices.size)"
            )
        )
        assertTrue(
            "EXP voice languages must read the profile's list",
            app.contains("profile.voices.map { it.lang }")
        )
        val script = fingerprintScriptSource()
        assertTrue(
            "payload voices must come from the profile's list",
            script.contains("p.voices.joinToString")
        )
    }
}
