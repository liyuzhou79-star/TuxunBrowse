package com.tuxunbrowse.android.engine

/**
 * One selectable time zone inside a fingerprint region (v0.144.0).
 * The zone drives the linked parameters the mode promises: the
 * geolocation city and coordinates, and (through the zone id) the
 * Intl/Date offset math in the injected script. [zoneName] is the
 * English long name used when the script synthesises Date strings;
 * it is the standard-time name — daylight time reuses the same
 * string with the daylight offset, a simplification recorded in
 * the v0.144.0 checkpoint chapter.
 */
data class FpTimezone(
    val id: String,
    val label: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val zoneName: String
)

/**
 * One preset fingerprint profile (v0.144.0): a region, one
 * mainstream device sold in that region, and every browser-side
 * parameter that has to agree with both — user agent, platform,
 * language set, screen geometry, CPU/memory figures, WebGL
 * strings, the noise seed and the battery story. The values are
 * fixed in code (not fetched, not randomised per install) so a
 * profile is stable and self-consistent; the self-consistency
 * rules are pinned by FingerprintProfilesTest.
 *
 * What a profile deliberately does NOT carry: an IP address (that
 * is the user's proxy's job — the mode's copy says so), a font
 * list (Android exposes no per-app font set to spoof; text
 * measurement is only blurred by the canvas/rect noise), and a
 * storage partition (the system WebView has ONE data directory,
 * so fingerprint tabs share cookies/storage with normal WebView
 * tabs — recorded honestly in the mode's copy and the checkpoint).
 */
data class FingerprintProfile(
    val regionId: String,
    val regionLabel: String,
    val deviceModel: String,
    /** The build model string inside the UA and UA-CH (SM-…, Pixel 8). */
    val uaModel: String,
    val userAgent: String,
    val platform: String,
    val language: String,
    val languages: List<String>,
    val screenWidth: Int,
    val screenHeight: Int,
    val devicePixelRatio: Double,
    val hardwareConcurrency: Int,
    val deviceMemory: Int,
    val webGlVendor: String,
    val webGlRenderer: String,
    val noiseSeed: Int,
    val batteryLevel: Double,
    val batteryCharging: Boolean,
    val dateFormatSample: String,
    val timezones: List<FpTimezone>
) {
    /** The zone [id] in this region, or the region's first zone. */
    fun timezone(id: String?): FpTimezone =
        timezones.firstOrNull { it.id == id } ?: timezones.first()
}

/** A profile plus the one zone the user picked inside it. */
data class ResolvedFingerprint(
    val profile: FingerprintProfile,
    val timezone: FpTimezone
)

object FingerprintProfiles {

    private const val CHROME_TAIL =
        ") AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/131.0.6778.104 Mobile Safari/537.36"

    val UNITED_STATES = FingerprintProfile(
        regionId = "US",
        regionLabel = "美国",
        deviceModel = "Samsung Galaxy S24",
        uaModel = "SM-S921U",
        userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S921U" + CHROME_TAIL,
        platform = "Linux armv8l",
        language = "en-US",
        languages = listOf("en-US", "en"),
        screenWidth = 360,
        screenHeight = 780,
        devicePixelRatio = 3.0,
        hardwareConcurrency = 8,
        deviceMemory = 8,
        webGlVendor = "Qualcomm",
        webGlRenderer = "Adreno (TM) 750",
        noiseSeed = 1401,
        batteryLevel = 0.82,
        batteryCharging = false,
        dateFormatSample = "11/25/2025",
        timezones = listOf(
            FpTimezone(
                "America/New_York", "东部时间", "纽约",
                40.7128, -74.0060, "Eastern Standard Time"
            ),
            FpTimezone(
                "America/Chicago", "中部时间", "芝加哥",
                41.8781, -87.6298, "Central Standard Time"
            ),
            FpTimezone(
                "America/Denver", "山地时间", "丹佛",
                39.7392, -104.9903, "Mountain Standard Time"
            ),
            FpTimezone(
                "America/Los_Angeles", "太平洋时间", "洛杉矶",
                34.0522, -118.2437, "Pacific Standard Time"
            )
        )
    )

    val JAPAN = FingerprintProfile(
        regionId = "JP",
        regionLabel = "日本",
        deviceModel = "Google Pixel 8",
        uaModel = "Pixel 8",
        userAgent = "Mozilla/5.0 (Linux; Android 14; Pixel 8" + CHROME_TAIL,
        platform = "Linux armv8l",
        language = "ja-JP",
        languages = listOf("ja-JP", "ja", "en-US", "en"),
        screenWidth = 412,
        screenHeight = 915,
        devicePixelRatio = 2.625,
        hardwareConcurrency = 8,
        deviceMemory = 8,
        webGlVendor = "ARM",
        webGlRenderer = "ANGLE (ARM, Mali-G715s MC10, OpenGL ES 3.2)",
        noiseSeed = 1402,
        batteryLevel = 0.67,
        batteryCharging = false,
        dateFormatSample = "2025/11/25",
        timezones = listOf(
            FpTimezone(
                "Asia/Tokyo", "日本时间", "东京",
                35.6762, 139.6503, "Japan Standard Time"
            )
        )
    )

    val SINGAPORE = FingerprintProfile(
        regionId = "SG",
        regionLabel = "新加坡",
        deviceModel = "Samsung Galaxy S23",
        uaModel = "SM-S911B",
        userAgent = "Mozilla/5.0 (Linux; Android 14; SM-S911B" + CHROME_TAIL,
        platform = "Linux armv8l",
        language = "en-SG",
        languages = listOf("en-SG", "en-US", "en"),
        screenWidth = 360,
        screenHeight = 780,
        devicePixelRatio = 3.0,
        hardwareConcurrency = 8,
        deviceMemory = 8,
        webGlVendor = "Qualcomm",
        webGlRenderer = "Adreno (TM) 740",
        noiseSeed = 1403,
        batteryLevel = 0.91,
        batteryCharging = true,
        dateFormatSample = "25/11/2025",
        timezones = listOf(
            FpTimezone(
                "Asia/Singapore", "新加坡时间", "新加坡",
                1.3521, 103.8198, "Singapore Standard Time"
            )
        )
    )

    val TAIWAN = FingerprintProfile(
        regionId = "TW",
        regionLabel = "台湾地区",
        deviceModel = "Samsung Galaxy A54",
        uaModel = "SM-A546B",
        userAgent = "Mozilla/5.0 (Linux; Android 14; SM-A546B" + CHROME_TAIL,
        platform = "Linux armv8l",
        language = "zh-TW",
        languages = listOf("zh-TW", "zh", "en-US", "en"),
        screenWidth = 412,
        screenHeight = 891,
        devicePixelRatio = 2.625,
        hardwareConcurrency = 8,
        deviceMemory = 8,
        webGlVendor = "ARM",
        webGlRenderer = "ANGLE (ARM, Mali-G68, OpenGL ES 3.2)",
        noiseSeed = 1404,
        batteryLevel = 0.74,
        batteryCharging = false,
        dateFormatSample = "2025/11/25",
        timezones = listOf(
            FpTimezone(
                "Asia/Taipei", "台北时间", "台北",
                25.0330, 121.5654, "Taipei Standard Time"
            )
        )
    )

    /** All regions, in picker display order. */
    val all: List<FingerprintProfile> =
        listOf(UNITED_STATES, JAPAN, SINGAPORE, TAIWAN)

    /** The region [id], or the first region when unknown/null. */
    fun region(id: String?): FingerprintProfile =
        all.firstOrNull { it.regionId == id } ?: all.first()

    /** Resolve stored ids to the live profile + zone pair. */
    fun resolve(regionId: String?, timezoneId: String?): ResolvedFingerprint {
        val profile = region(regionId)
        return ResolvedFingerprint(profile, profile.timezone(timezoneId))
    }
}
