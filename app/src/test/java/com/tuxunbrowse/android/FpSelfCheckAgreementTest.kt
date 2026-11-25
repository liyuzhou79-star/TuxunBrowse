package com.tuxunbrowse.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    /**
     * Off-state agreement (v0.205.0 audit): a protection
     * switch turned off must read as 已关闭 (off) in the
     * self-check, never as an anomaly — and ONLY the rows
     * whose surfaces a switch actually gates may have an off
     * state. The payload gates exactly three probed surfaces
     * by switch (WebGL/WebGPU via webglDisguise, the font
     * shelf via fontSet, and — since v0.210.0 — the
     * orientation pair via sensors); the v0.201.0 rows
     * （网络信息 / 设备列表 / 语音列表） are ungated in the
     * payload — no FP_OPT flag exists for them — so their
     * rows must not grow an off state either, or the page
     * would claim a switch exists that does not. 请求头语言
     * stays permanently 'na' by design.
     */
    @Test
    fun offStateExistsExactlyOnTheSwitchGatedRows() {
        val app = browserAppSource()
        val probe = app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("The tab-count box")
        // Four rows report off through the ternary shape:
        // WebGL 厂商 / WebGL 型号 / WebGPU 厂商 (webglOff)
        // and 字体清单 (fontOff). The fifth off-aware row —
        // 方向事件 (sensorsOff, v0.210.0) — reports through
        // an if-branch, so the ternary count stays four and
        // the flag SET is the invariant that grows.
        assertEquals(
            "exactly the switch-gated rows may report 'off'",
            4, probe.split("? 'off'").size - 1
        )
        assertEquals(
            setOf("webglOff", "fontOff", "sensorsOff"),
            Regex("""EXP\.(\w+Off)""").findAll(probe)
                .map { it.groupValues[1] }.toSet()
        )
        assertTrue(probe.contains("row('方向事件', 'off'"))
        assertTrue(probe.contains("EXP.sensorsOff"))
        // The EXP builder writes the flags, from the same
        // protection state the payload was built with.
        val builder = app.substringAfter("fun runSelfCheck")
            .substringBefore("val probe = fpSelfCheckProbe")
        assertTrue(builder.contains(".put(\"webglOff\", !protection.webglDisguise)"))
        assertTrue(builder.contains(".put(\"fontOff\", !protection.fontSet)"))
        assertTrue(builder.contains(".put(\"sensorsOff\", !protection.sensors)"))
        // The ungated v0.201.0 rows are all present and none
        // of them references an off flag (checked above by
        // the identifier set); the header-language row is
        // the permanent 'na'.
        for (name in listOf("网络信息", "设备列表", "语音列表")) {
            assertTrue(
                "ungated row $name must exist",
                probe.contains("row('$name'")
            )
        }
        assertTrue(probe.contains("row('请求头语言', 'na'"))
    }

    @Test
    fun geolocationProbeReadsTheSameCoordinatesThePayloadClaims() {
        // v0.208.0: the 定位形态 probe row compares the
        // delivered position against EXP.lat / EXP.lon; the
        // EXP builder must write those from the SAME resolved
        // zone the payload was built with (zone.latitude /
        // zone.longitude), or the row grades against a second
        // hand-copied table.
        val app = browserAppSource()
        val probe = app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("\nprivate fun")
        assertTrue(probe.contains("row('定位形态'"))
        assertTrue(probe.contains("EXP.lat"))
        assertTrue(probe.contains("EXP.lon"))
        assertTrue(probe.contains("[object GeolocationPosition]"))
        assertTrue(probe.contains("[object GeolocationCoordinates]"))
        val builder = app.substringAfter("fun runSelfCheck")
            .substringBefore("val probe = fpSelfCheckProbe")
        assertTrue(builder.contains(".put(\"lat\", zone.latitude)"))
        assertTrue(builder.contains(".put(\"lon\", zone.longitude)"))
        // The v0.208.0 sync rows ride along: orientation pair,
        // UA derivative, clock agreement.
        for (name in listOf("屏幕方向", "UA 派生值", "时钟一致性")) {
            assertTrue(
                "row $name must exist",
                probe.contains("row('$name'")
            )
        }
        // The report waits for the geolocation row too (and,
        // since v0.210.0, for the orientation row as well:
        // six settled flags in the conjunction).
        assertTrue(probe.contains("geoSettled"))
        assertTrue(probe.contains("voicesSettled && geoSettled"))
    }

    @Test
    fun brandListRowGradesAgainstTheSameAlgorithm() {
        // v0.210.0: the probe's UA 品牌列表 row compares the
        // page's brands against EXP.brands, and the builder
        // computes EXP.brands with FingerprintProfiles'
        // seeded algorithm — the same function the payload
        // builds its list from. A hand-kept expected triple
        // on either side breaks the single source.
        val app = browserAppSource()
        val probe = app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("The tab-count box")
        val builder = app.substringAfter("fun runSelfCheck")
            .substringBefore("val probe = fpSelfCheckProbe")
        assertTrue(probe.contains("row('UA 品牌列表'"))
        assertTrue(probe.contains("EXP.brands"))
        assertTrue(builder.contains("uaBrandList"))
        assertFalse(builder.contains("Not=A?Brand"))
        assertFalse(probe.contains("Not=A?Brand"))
    }

    @Test
    fun orientationRowSettlesTheReport() {
        // v0.210.0: the orientation row is the probe's sixth
        // asynchronous surface — it owns a settled flag and
        // that flag gates the report, or the run either
        // reports early without the row or hangs to the
        // watchdog.
        val app = browserAppSource()
        val probe = app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("The tab-count box")
        assertTrue(probe.contains("var oriSettled = false"))
        assertTrue(probe.contains("geoSettled && oriSettled"))
        assertTrue(probe.contains("row('方向事件'"))
        assertTrue(probe.contains("deviceorientationabsolute"))
    }

    @Test
    fun selfCheckRunIsCreationGuardedAndWatchdogged() {
        // v0.208.0: runSelfCheck used to construct its WebView
        // bare — the one unguarded construction left — and the
        // report callback was the only exit from the running
        // state. Both ends are closed now; pin them so a
        // future edit cannot quietly reopen either.
        val app = browserAppSource()
        val run = app.substringAfter("fun runSelfCheck")
            .substringBefore("var ipResult")
        assertTrue(run.contains("catch (t: Throwable)"))
        assertTrue(run.contains("这台设备的系统 WebView 建不出来"))
        assertTrue(run.contains("postDelayed"))
        assertTrue(run.contains("自查超时没有回话"))
        assertTrue(run.contains("checkWebView === wv"))
    }
}
