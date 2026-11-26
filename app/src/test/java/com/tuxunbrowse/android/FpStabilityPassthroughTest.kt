package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Stability + passthrough contracts (v0.223.0, round-26
 * audit).
 *
 * The stability half: the round-26 harness
 * (scratch/fp_audit_26) replayed the extracted payload
 * in fresh Node realms — three loads of one profile,
 * one of a second — and diffed 75 snapshot leaves: all
 * stable across loads, and every identity leaf differed
 * across profiles. That result rests on two source
 * facts pinned here so a future edit cannot quietly
 * reopen either: the payload rolls no dice (no
 * Math.random anywhere — every noise rule is a pure
 * hash of the profile seed and the content), and the
 * seed itself is a pure constant of the (region,
 * device) pair.
 *
 * The passthrough half: storage.estimate,
 * performance.memory, visualViewport and the four
 * chooser device APIs were audited this round and left
 * to the engine on purpose (quota is disk-derived and
 * usage is real; heap figures are V8's own; the
 * viewport mirrors the real window; the device APIs
 * do not exist in the WebView and a fabricated object
 * whose picker can never appear is its own tell). The
 * payload must not grow a fabrication for any of them;
 * the self-check rows that report these faces are
 * pinned too.
 *
 * Source-scan genre, same as FpSelfCheckAgreementTest.
 */
class FpStabilityPassthroughTest {

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

    private fun fingerprintProfilesSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/engine/" +
            "FingerprintProfiles.kt"
    )

    private fun browserAppSource(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/ui/BrowserApp.kt"
    )

    @Test
    fun payloadRollsNoDiceAndTheSeedIsAProfileConstant() {
        val script = fingerprintScriptSource()
        assertFalse(
            "payload must not re-roll anything per load",
            script.contains("Math.random")
        )
        assertTrue(
            "seed must stay a pure (region, device) constant",
            fingerprintProfilesSource().contains(
                "val seed: Int = profile.noiseSeed * 100 +" +
                    " device.seedDelta"
            )
        )
    }

    @Test
    fun auditedFacesStayPassthroughInThePayload() {
        val script = fingerprintScriptSource()
        for (token in listOf(
            "navigator.storage", "storage.estimate",
            "performance.memory", "visualViewport",
            "navigator.bluetooth", "navigator.usb",
            "navigator.serial", "navigator.hid"
        )) {
            assertFalse(
                "payload must not fabricate $token",
                script.contains(token)
            )
        }
    }

    @Test
    fun selfCheckReportsTheRound26Faces() {
        val app = browserAppSource()
        val probe = app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("The tab-count box")
        for (name in listOf(
            "指纹稳定性", "性能内存", "设备接口",
            "视觉视口", "存储估算"
        )) {
            assertTrue(
                "self-check row $name must exist",
                probe.contains("row('$name'")
            )
        }
        // The storage row is asynchronous: it owns a
        // settled flag and that flag gates the report.
        assertTrue(probe.contains("var storageSettled = false"))
        assertTrue(probe.contains("storageSettled) {"))
    }
}
