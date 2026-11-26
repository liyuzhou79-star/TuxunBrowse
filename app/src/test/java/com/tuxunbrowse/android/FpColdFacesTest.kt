package com.tuxunbrowse.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Cold API faces, pinned at the source level (v0.222.0,
 * FP-LIE-26..31).
 *
 * Six faces a page can read without any permission were
 * audited against the profile story:
 * - The speech-voices DELIVERY FORM (FP-LIE-26): Chrome
 *   serves the catalogue asynchronously — a cold page's
 *   first getVoices() returns [] and voiceschanged
 *   delivers the list. The payload used to serve the full
 *   list from the first synchronous call while ALSO
 *   firing the event 50ms later, a shape no real Chrome
 *   cold page produces. The list is now gated behind a
 *   ready flag the delivery timer flips BEFORE it
 *   dispatches the event, so a handler reading inside
 *   voiceschanged already sees the list.
 * - The sensor cadence (FP-LIE-31): the synthetic stream
 *   ticked at 100ms, twice Chromium's nominal sensor
 *   rate — kInertialSensorIntervalMillis = 50 in the
 *   Chromium source, the browser-side minimal interval
 *   between sensor data updates with the renderer pump
 *   delay matched to it. Tick, devicemotion interval
 *   field, and the wall-clock shape of the wobble (the
 *   divisors and drift step halved with the tick) are
 *   pinned together so the cadence cannot drift back
 *   without the story moving with it.
 * - Four faces stay PASSTHROUGH by design (FP-LIE-27
 *   RTC codec capabilities, FP-LIE-28 media-device
 *   constraints, FP-LIE-29 media capabilities,
 *   FP-LIE-30 timers): fabricating any of them would
 *   contradict a sibling surface the same page can read
 *   (the SDP offer, the constraint set's engine-build
 *   nature, canPlayType / isTypeSupported, the shared
 *   Chromium timer clamp). Their ABSENCE from the
 *   payload is the invariant, pinned here so a future
 *   edit cannot quietly start fabricating one.
 * - The self-check gained a row per face (three-state:
 *   graded where a public baseline exists — timer step,
 *   sensor cadence, voices arrival order — and honestly
 *   'na' where none does).
 *
 * Source-scan genre, same as FpSelfCheckAgreementTest.
 */
class FpColdFacesTest {

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

    private fun payload(): String = sourceOf(
        "src/main/java/com/tuxunbrowse/android/engine/" +
            "FingerprintScript.kt"
    )

    private fun probe(): String {
        val app = sourceOf(
            "src/main/java/com/tuxunbrowse/android/ui/" +
                "BrowserApp.kt"
        )
        return app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("The tab-count box")
    }

    @Test
    fun voicesAreGatedBehindTheDeliveryEvent() {
        val p = payload()
        assertTrue(
            "getVoices must answer [] until the delivery " +
                "timer flips the ready flag",
            p.contains("return voicesReady ? list.slice() : [];")
        )
        // The flag flips before the event is dispatched,
        // so a handler reading inside voiceschanged sees
        // the list — the real delivery's ordering.
        val flip = p.indexOf("voicesReady = true;")
        val dispatch = p.indexOf(
            "speechSynthesis.dispatchEvent(evt);")
        assertTrue("ready flip must exist", flip >= 0)
        assertTrue("voiceschanged dispatch must exist",
            dispatch >= 0)
        assertTrue(
            "ready flag must flip before voiceschanged " +
                "is dispatched",
            flip < dispatch
        )
    }

    @Test
    fun sensorCadenceMatchesChromiumNominalRate() {
        val p = payload()
        assertTrue(
            "devicemotion interval field must say the " +
                "Chromium nominal 50",
            p.contains("interval: 50")
        )
        assertFalse(
            "the old 100ms interval field must not return",
            p.contains("interval: 100")
        )
        // The wobble divisors and the compass drift step
        // halved with the tick: the motion story's
        // wall-clock shape is unchanged, only the
        // cadence moved to the nominal rate.
        assertTrue(p.contains("Math.sin(tick / 18)"))
        assertTrue(p.contains("Math.cos(tick / 26)"))
        assertTrue(p.contains("tick * 0.01"))
    }

    @Test
    fun passthroughFacesStayOutOfThePayload() {
        val p = payload()
        // FP-LIE-29: decoder answers come from the host
        // media stack; the payload must not grow a
        // mediaCapabilities fabrication.
        assertFalse(p.contains("mediaCapabilities"))
        // FP-LIE-27: codec capability lists stay the
        // host engine's (they agree with the SDP offer
        // by construction); no getCapabilities wrapper.
        assertFalse(p.contains("getCapabilities"))
        assertFalse(p.contains("RTCRtpSender"))
        // FP-LIE-28: the constraint table is an engine-
        // build property, not a per-model surface.
        assertFalse(p.contains("getSupportedConstraints"))
    }

    @Test
    fun selfCheckGradesTheColdFaces() {
        val probe = probe()
        // One row per audited face.
        for (name in listOf(
            "媒体能力", "RTC 编解码能力", "媒体设备约束",
            "计时精度", "语音到达顺序", "传感器节拍"
        )) {
            assertTrue(
                "self-check must carry the $name row",
                probe.contains("row('$name'")
            )
        }
        // The async rows own settled flags and the flags
        // gate the report, or the run reports early
        // without them / hangs to the watchdog.
        for (flag in listOf(
            "capsSettled", "voiceOrderSettled",
            "sensorBeatSettled"
        )) {
            assertTrue(
                "settled flag $flag must exist",
                probe.contains("var $flag = false")
            )
            assertTrue(
                "settled flag $flag must gate the report",
                probe.contains("&& $flag") ||
                    probe.contains("$flag &&") ||
                    probe.contains("$flag)")
            )
        }
        // The voices arrival order is graded from the
        // cold read captured at the top of the probe.
        assertTrue(probe.contains("voiceColdCount"))
        assertTrue(probe.contains("voiceEventSeen"))
    }
}
