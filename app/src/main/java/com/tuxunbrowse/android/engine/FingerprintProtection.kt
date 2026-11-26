package com.tuxunbrowse.android.engine

import org.json.JSONObject

/**
 * Per-surface protection switches for fingerprint mode
 * (v0.190.0). Every surface the payload disguises can be
 * turned off individually; everything defaults ON, and a
 * surface that is off simply stops being disguised — the
 * page then reads the host's real values for that one
 * surface while the rest of the profile keeps its story.
 *
 * The set is deliberately the surfaces that EXIST: canvas
 * noise, the WebGL/WebGPU disguise, audio noise, the font
 * shelf, the WebRTC relay-only form, the sensor stream, the
 * battery story and the window.chrome object. There is no
 * switch for surfaces the mode never had (the Accept-Language
 * header, storage isolation): a toggle for those would be a
 * fake switch.
 *
 * v0.224.0 adds the six core surfaces that used to be always
 * on: timezone & time, language & regional formats,
 * geolocation, screen & orientation, device identity and
 * network info. They were split only where the split stays
 * self-consistent: the UA string, UA-CH, platform and the
 * hardware numbers are ONE switch (deviceIdentity) because
 * answering half of them from the profile and half from the
 * host would contradict itself inside a single read; the
 * speech voice list rides the language switch because the
 * voices ARE a language surface; matchMedia's display
 * queries ride the screen switch because they describe the
 * same panel. Surfaces that stay always on because they
 * cannot be split without contradiction are the noise seeds
 * themselves and the Intl/Date plumbing shared by the
 * timezone and language switches (each constructor still
 * gates its own half).
 *
 * The switches ride into [FingerprintScript.payload] as
 * FP_OPT_* flags, and the consistency tests pin that turning
 * one off changes exactly its own flag. The self-check page
 * reads the same state and reports a switched-off surface as
 * 已关闭 (off), never as an anomaly.
 */
data class FingerprintProtection(
    val canvasNoise: Boolean = true,
    val webglDisguise: Boolean = true,
    val audioNoise: Boolean = true,
    val fontSet: Boolean = true,
    val webrtcRelay: Boolean = true,
    val sensors: Boolean = true,
    val battery: Boolean = true,
    val chromeObject: Boolean = true,
    /** Date/Intl timezone answers (v0.224.0). */
    val timezone: Boolean = true,
    /** Intl formats, navigator.languages, speech voices. */
    val language: Boolean = true,
    /** Geolocation answers. */
    val geolocation: Boolean = true,
    /** Screen geometry, DPR, matchMedia display queries. */
    val screen: Boolean = true,
    /** UA, UA-CH, platform, cores/memory — one switch. */
    val deviceIdentity: Boolean = true,
    /** navigator.connection story. */
    val connection: Boolean = true
) {
    fun toJson(): String = JSONObject()
        .put("canvas", canvasNoise)
        .put("webgl", webglDisguise)
        .put("audio", audioNoise)
        .put("fonts", fontSet)
        .put("webrtc", webrtcRelay)
        .put("sensors", sensors)
        .put("battery", battery)
        .put("chrome", chromeObject)
        .put("tz", timezone)
        .put("lang", language)
        .put("geo", geolocation)
        .put("screen", screen)
        .put("identity", deviceIdentity)
        .put("conn", connection)
        .toString()

    companion object {
        /**
         * Parse a stored value; anything missing or malformed
         * falls back to the all-on default per key, so a
         * corrupt store can never silently weaken a surface.
         */
        fun fromJson(raw: String?): FingerprintProtection {
            if (raw.isNullOrBlank()) return FingerprintProtection()
            return try {
                val o = JSONObject(raw)
                FingerprintProtection(
                    canvasNoise = o.optBoolean("canvas", true),
                    webglDisguise = o.optBoolean("webgl", true),
                    audioNoise = o.optBoolean("audio", true),
                    fontSet = o.optBoolean("fonts", true),
                    webrtcRelay = o.optBoolean("webrtc", true),
                    sensors = o.optBoolean("sensors", true),
                    battery = o.optBoolean("battery", true),
                    chromeObject = o.optBoolean("chrome", true),
                    timezone = o.optBoolean("tz", true),
                    language = o.optBoolean("lang", true),
                    geolocation = o.optBoolean("geo", true),
                    screen = o.optBoolean("screen", true),
                    deviceIdentity = o.optBoolean("identity", true),
                    connection = o.optBoolean("conn", true)
                )
            } catch (e: Exception) {
                FingerprintProtection()
            }
        }
    }
}
