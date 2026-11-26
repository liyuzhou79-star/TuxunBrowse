package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FingerprintScript
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The v0.220.0 lie-detection audit: the payload's wrappers
 * and fabricated objects must be SHAPED like the natives
 * they replace — prototype placement, non-enumerable
 * slots, real names instead of .bind() copies, and noise
 * keyed by content rather than by position alone. The
 * behavioural half was verified on a Node harness running
 * the extracted payload (scratch/fp_audit_23); these tests
 * pin the source shapes so a later edit cannot quietly
 * revert to the lying forms.
 */
class FpLieDetectionTest {

    private fun payload(): String = FingerprintScript.payload(
        FingerprintProfiles.resolve("JP", "Asia/Tokyo")
    )

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

    @Test
    fun canvasNoiseIsKeyedByPixelContent() {
        val p = payload()
        assertTrue(p.contains("function noiseBytes("))
        // The hash mixes the pixel's own RGB: same value at
        // the same position draws the same delta, different
        // content draws an unrelated lattice — a position-
        // only grid can be mapped by one calibration image.
        assertTrue(p.contains("d[i] * 7 + d[i + 1] * 13"))
        assertTrue(p.contains("d[i + 2] * 17"))
    }

    @Test
    fun audioNoiseIsKeyedBySampleContent() {
        val p = payload()
        assertTrue(p.contains("Math.round(data[i] * 32768)"))
        // The gate bit comes from the same content hash, so
        // the touched-index set varies with the signal too.
        assertTrue(p.contains("(h >>> 16) % 3 === 0"))
    }

    @Test
    fun noBoundFunctionIsExposedAsAStatic() {
        val p = payload()
        // A bound copy reports name 'bound X' and an
        // anonymous native-code string — the wrappers are
        // named functions registered with fixFnAs instead.
        assertFalse(p.contains("supportedLocalesOf.bind"))
        assertFalse(p.contains("generateCertificate.bind"))
        assertTrue(p.contains("function fpSLO(orig)"))
        assertTrue(p.contains("'supportedLocalesOf', 1"))
        assertTrue(p.contains("'generateCertificate', 1"))
    }

    @Test
    fun methodsInstallOnPrototypesNotInstances() {
        val p = payload()
        assertTrue(p.contains("function setMethodOn("))
        // The five instance-own installs the audit caught,
        // now routed through the prototype installer.
        assertTrue(p.contains("setMethodOn(navigator, 'getBattery'"))
        assertTrue(p.contains("setMethodOn(speechSynthesis, 'getVoices'"))
        assertTrue(p.contains("setMethodOn(navigator.mediaDevices, 'enumerateDevices'"))
        assertTrue(p.contains("setMethodOn(navigator.mediaDevices, 'getUserMedia'"))
        assertTrue(p.contains("setMethodOn(document.fonts, 'check'"))
        // getUserMedia used to be a def() accessor handing
        // out a length-0 function; it is a real method now.
        assertFalse(p.contains("def(navigator.mediaDevices, 'getUserMedia'"))
        // The connection accessor moved to the prototype.
        assertTrue(p.contains("def(navT, 'connection', conn)"))
        assertFalse(p.contains("def(navigator, 'connection', conn)"))
    }

    @Test
    fun fabricatedObjectsRideRealPrototypes() {
        val p = payload()
        assertTrue(p.contains("Object.create(NavigatorUAData.prototype)"))
        assertTrue(p.contains("Object.create(MediaQueryList.prototype)"))
        assertTrue(p.contains("Object.create(PermissionStatus.prototype)"))
        // The plain-literal forms must not come back.
        assertFalse(p.contains("var uaData = {"))
        assertFalse(p.contains("return Promise.resolve({\n        name: name"))
    }

    @Test
    fun batteryManagerIsMemoizedAndDefBuilt() {
        val p = payload()
        // Native getBattery resolves to THE manager; the
        // payload memoizes its fabricated one.
        assertTrue(p.contains("var cachedBattery = null"))
        assertTrue(p.contains("cachedBattery = b"))
        // Plain assignment to the readonly prototype
        // attributes threw in strict mode — the object is
        // def()-built now, handlers included.
        assertTrue(p.contains("def(b, 'charging'"))
        assertTrue(p.contains("def(b, 'level'"))
        assertFalse(p.contains("b.charging = "))
        assertFalse(p.contains("b.level = "))
        assertTrue(p.contains("defHandler(b, 'onlevelchange')"))
    }

    @Test
    fun shaderPrecisionFormatIsDefBuilt() {
        val p = payload()
        assertTrue(p.contains("def(fmt, 'rangeMin'"))
        assertTrue(p.contains("def(fmt, 'precision'"))
        // The assignment form threw on the readonly
        // prototype attributes, exactly like the battery.
        assertFalse(p.contains("fmt.rangeMin = "))
        assertFalse(p.contains("fmt.precision = "))
    }

    @Test
    fun rtcInstanceCarriesNoEnumerableOwnKeys() {
        val p = payload()
        // The onicecandidate own accessor drops its
        // enumerable flag; Object.keys(pc) stays empty.
        assertTrue(p.contains(
            "Object.defineProperty(pc, 'onicecandidate', {\n" +
                "            configurable: true, enumerable: false,"
        ))
        assertTrue(p.contains(
            "Object.defineProperty(pc, 'addEventListener', {"
        ))
    }

    @Test
    fun selfCheckCarriesTheLieDetectionRows() {
        val app = browserAppSource()
        val probe = app.substringAfter("private fun fpSelfCheckProbe")
            .substringBefore("\nprivate fun")
        for (name in listOf(
            "函数形态", "伪装对象形态", "画布噪声形态",
            "电池形态", "权限对象形态"
        )) {
            assertTrue(
                "self-check row $name must exist",
                probe.contains("row('$name'")
            )
        }
        // The report waits for the two new async rows too.
        assertTrue(probe.contains("battSettled"))
        assertTrue(probe.contains("permSettled"))
        assertTrue(probe.contains(
            "voicesSettled && geoSettled && oriSettled &&"
        ))
        // The content-keyed noise row states its 'na' case
        // honestly instead of dressing it up as a pass.
        assertTrue(probe.contains("未检出噪声（保护关闭或噪声未生效）"))
    }
}
