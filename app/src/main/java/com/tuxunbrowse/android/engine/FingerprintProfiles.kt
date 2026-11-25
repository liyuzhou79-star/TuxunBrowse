package com.tuxunbrowse.android.engine

/**
 * One selectable time zone inside a fingerprint region (v0.144.0).
 * The zone drives the linked parameters the mode promises: the
 * geolocation city and coordinates, and (through the zone id) the
 * Intl/Date offset math in the injected script. [zoneName] is the
 * English long standard-time name used when the script synthesises
 * Date strings; [zoneDaylightName] is the daylight counterpart the
 * script switches to when the date's offset is the daylight one
 * (v0.145.0 — v0.144.0 always used the standard name).
 */
data class FpTimezone(
    val id: String,
    val label: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val zoneName: String,
    /** Daylight-time long name (v0.145.0); null where the zone has no DST. */
    val zoneDaylightName: String? = null
)

/**
 * One speech-synthesis voice the profile claims (v0.145.0).
 * Android Chrome's voice list comes from the device TTS engine,
 * so a profile whose pages can enumerate voices but whose list
 * names another region's engine gives itself away; the names
 * below follow the Google TTS naming style for each region.
 */
data class FpVoice(
    val name: String,
    val lang: String,
    val isDefault: Boolean = false
)

/**
 * The WebGL capability numbers a GPU reports through getParameter
 * (v0.145.0) — not just the vendor/renderer strings. Values follow
 * the public ANGLE/driver conventions for each GPU class; they are
 * pinned for self-consistency by the profile tests, but have NOT
 * been diffed against a physical unit of each device (recorded
 * honestly in the v0.145.0 checkpoint chapter). Point/line
 * minimums are always 1 on mobile GPUs, so only the maximums are
 * carried.
 */
data class FpWebGlParams(
    val maxTextureSize: Int,
    val maxCubeMapTextureSize: Int,
    val maxRenderbufferSize: Int,
    val maxViewportDim: Int,
    val aliasedPointSizeMax: Float,
    val aliasedLineWidthMax: Float,
    val maxVertexAttribs: Int,
    val maxTextureImageUnits: Int,
    val maxVertexTextureImageUnits: Int,
    val maxCombinedTextureImageUnits: Int,
    val maxFragmentUniformVectors: Int,
    val maxVertexUniformVectors: Int,
    val maxVaryingVectors: Int
)

/**
 * One device in the model library (v0.146.0): everything that
 * belongs to the HARDWARE — UA, screen geometry, GPU strings and
 * capability table, CPU/memory figures. Picking a device moves
 * all of these together, by construction: the picker never lets a
 * user combine one device's name with another's GPU, which is
 * exactly the contradiction the bounded-customisation rule bans.
 * [seedDelta] differentiates the noise realisation per device on
 * top of the region's base seed (still a constant — stable across
 * sessions for the same region+device combination).
 */
data class FpDevice(
    /** Stable id; the UA build model doubles as the id. */
    val id: String,
    val deviceModel: String,
    val uaModel: String,
    val userAgent: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val devicePixelRatio: Double,
    val hardwareConcurrency: Int,
    val deviceMemory: Int,
    val webGlVendor: String,
    val webGlRenderer: String,
    val webGl: FpWebGlParams,
    /**
     * The WebGPU adapter identity (v0.159.0): what
     * GPUAdapterInfo.vendor / .architecture report for this
     * device's GPU family in Chrome. Without developer features
     * those two strings are the ONLY adapter fields a page can
     * read, and they must name the same GPU family as the WebGL
     * renderer string or the two APIs cross-examine each other
     * into a contradiction. Dawn's family names: Qualcomm GPUs
     * report vendor "qualcomm" / architecture "adreno-7xx";
     * ARM Mali G6xx/G7xx report "arm" / "valhall".
     */
    val webGpuVendor: String,
    val webGpuArchitecture: String,
    /**
     * The stock font families this device ships (v0.174.0),
     * at family-name granularity: what a page may learn through
     * document.fonts.check() must name THIS device's shelf, not
     * the host's. The sets are the vendors' public stock facts
     * (the Android base set, plus SamsungOne on Galaxy devices
     * and Google Sans / Google Sans Text on Pixels). The font
     * FILES still render on the host — only the enumeration
     * answer follows the profile; measurement stays blurred by
     * the existing scale/jitter mitigation.
     */
    val fonts: List<String>,
    val seedDelta: Int
)

/**
 * One region profile (v0.144.0, restructured v0.146.0): what
 * belongs to the PLACE — its time zones, its language set, its
 * TTS voices, its battery/network story, and the library of
 * devices sold there. Device-bound parameters live on [FpDevice]
 * since v0.146.0 so the user can pick a model inside the region
 * without ever breaking self-consistency.
 *
 * What a profile deliberately does NOT carry: an IP address (that
 * is the user's proxy's job — the mode's copy says so), the font
 * FILES themselves (the device carries the stock family NAME set
 * for the enumeration surface since v0.174.0, but glyphs still
 * render with the host's fonts; text measurement is blurred by
 * the canvas/rect noise), and a storage partition (the system
 * WebView has ONE data directory, so fingerprint tabs share
 * cookies/storage with normal WebView tabs — recorded honestly
 * in the mode's copy and the checkpoint).
 */
data class FingerprintProfile(
    val regionId: String,
    val regionLabel: String,
    /** The region's default (first) language; also its picker head. */
    val language: String,
    /** Languages the user may pick inside this region (v0.146.0). */
    val languageOptions: List<String>,
    val noiseSeed: Int,
    val batteryLevel: Double,
    val batteryCharging: Boolean,
    val dateFormatSample: String,
    /** Speech-synthesis voices the region's TTS engine lists (v0.145.0). */
    val voices: List<FpVoice>,
    /** Widest color gamut matchMedia reports: "srgb" or "p3" (v0.145.0). */
    val colorGamut: String,
    /** Whether matchMedia (dynamic-range: high) matches (v0.145.0). */
    val dynamicRangeHigh: Boolean,
    /** navigator.connection story (v0.145.0): a healthy 4g link. */
    val connectionRtt: Int,
    val connectionDownlink: Double,
    val timezones: List<FpTimezone>,
    /** The model library for this region; the first is the default. */
    val devices: List<FpDevice>
) {
    /** The zone [id] in this region, or the region's first zone. */
    fun timezone(id: String?): FpTimezone =
        timezones.firstOrNull { it.id == id } ?: timezones.first()

    /** The device [id] in this region's library, or the default. */
    fun device(id: String?): FpDevice =
        devices.firstOrNull { it.id == id } ?: devices.first()

    /** [language] if this region offers it, else the default. */
    fun languageOrDefault(language: String?): String =
        if (language != null && language in languageOptions) {
            language
        } else {
            this.language
        }
}

/**
 * A fully resolved, self-consistent fingerprint (v0.146.0): one
 * region, one zone inside it, one device from its library, one
 * language from its set. Every cross-field agreement (UA names
 * the device, the GPU table matches the renderer string, the
 * first language is the picked language, the zone belongs to the
 * region) holds by construction — resolution can only ever pick
 * from the region's own sets, so no combination a user can save
 * contradicts itself.
 */
data class ResolvedFingerprint(
    val profile: FingerprintProfile,
    val timezone: FpTimezone,
    val device: FpDevice,
    val language: String
) {
    /** navigator.languages for [language]: primary + fallbacks. */
    val languages: List<String> =
        FingerprintProfiles.languagesFor(language)

    /** The stable noise seed for this region+device pair. */
    val seed: Int = profile.noiseSeed * 100 + device.seedDelta
}

/**
 * A user-saved combination (v0.146.0, 「我的档案」): just the four
 * ids. Applying one re-resolves through the same bounded path as
 * the picker, so a saved entry can never smuggle in a combination
 * the picker itself would refuse.
 */
data class CustomFingerprint(
    val regionId: String,
    val deviceId: String,
    val timezoneId: String,
    val language: String,
    val label: String
)

object FingerprintProfiles {

    private const val CHROME_TAIL =
        ") AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/131.0.6778.104 Mobile Safari/537.36"

    private fun ua(model: String): String =
        "Mozilla/5.0 (Linux; Android 14; $model" + CHROME_TAIL

    const val PLATFORM: String = "Linux armv8l"

    // Stock font family sets (v0.174.0). The base set is what
    // every Android device in the library ships; each vendor
    // adds its own UI families on top. Family names only — the
    // enumeration surface (document.fonts.check) answers from
    // these lists; see FpDevice.fonts.
    private val BASE_FONTS = listOf(
        "Roboto", "Roboto Condensed",
        "Noto Sans", "Noto Serif",
        "Noto Sans CJK JP", "Noto Sans CJK KR",
        "Noto Sans CJK SC", "Noto Sans CJK TC",
        "Noto Serif CJK JP", "Noto Serif CJK KR",
        "Noto Serif CJK SC", "Noto Serif CJK TC",
        "Droid Sans Mono"
    )
    private val SAMSUNG_FONTS = BASE_FONTS + listOf("SamsungOne")
    private val GOOGLE_FONTS = BASE_FONTS + listOf(
        "Google Sans", "Google Sans Text"
    )

    /**
     * Every family name the injected script treats as a KNOWN
     * system font (v0.174.0): the union of both vendors' stock
     * sets plus other Android vendors' published UI font names
     * that no device in the library ships. A fonts.check() probe
     * naming a known family is answered from the picked device's
     * own set (present → true, absent → false); a probe naming
     * anything else (a page's webfont, an obscure family) falls
     * through to the engine's real answer untouched.
     */
    val KNOWN_FONT_FAMILIES: List<String> =
        (SAMSUNG_FONTS + GOOGLE_FONTS + listOf(
            "MiSans", "HarmonyOS Sans", "OPPO Sans"
        )).distinct()

    // GPU capability tables, shared by every device carrying that
    // GPU class (v0.145.0 values, moved here in v0.146.0).
    private val GL_ADRENO_750 = FpWebGlParams(
        maxTextureSize = 16384, maxCubeMapTextureSize = 16384,
        maxRenderbufferSize = 16384, maxViewportDim = 16384,
        aliasedPointSizeMax = 1024f, aliasedLineWidthMax = 8f,
        maxVertexAttribs = 32, maxTextureImageUnits = 32,
        maxVertexTextureImageUnits = 16,
        maxCombinedTextureImageUnits = 96,
        maxFragmentUniformVectors = 224, maxVertexUniformVectors = 256,
        maxVaryingVectors = 31
    )
    private val GL_ADRENO_740 = FpWebGlParams(
        maxTextureSize = 16384, maxCubeMapTextureSize = 16384,
        maxRenderbufferSize = 16384, maxViewportDim = 16384,
        aliasedPointSizeMax = 1024f, aliasedLineWidthMax = 8f,
        maxVertexAttribs = 32, maxTextureImageUnits = 32,
        maxVertexTextureImageUnits = 16,
        maxCombinedTextureImageUnits = 96,
        maxFragmentUniformVectors = 224, maxVertexUniformVectors = 256,
        maxVaryingVectors = 31
    )
    private val GL_MALI_G715 = FpWebGlParams(
        maxTextureSize = 16384, maxCubeMapTextureSize = 16384,
        maxRenderbufferSize = 16384, maxViewportDim = 16384,
        aliasedPointSizeMax = 511f, aliasedLineWidthMax = 1f,
        maxVertexAttribs = 16, maxTextureImageUnits = 32,
        maxVertexTextureImageUnits = 16,
        maxCombinedTextureImageUnits = 64,
        maxFragmentUniformVectors = 224, maxVertexUniformVectors = 256,
        maxVaryingVectors = 16
    )
    private val GL_MALI_G68 = FpWebGlParams(
        maxTextureSize = 16384, maxCubeMapTextureSize = 16384,
        maxRenderbufferSize = 16384, maxViewportDim = 16384,
        aliasedPointSizeMax = 511f, aliasedLineWidthMax = 1f,
        maxVertexAttribs = 16, maxTextureImageUnits = 16,
        maxVertexTextureImageUnits = 16,
        maxCombinedTextureImageUnits = 48,
        maxFragmentUniformVectors = 224, maxVertexUniformVectors = 256,
        maxVaryingVectors = 16
    )

    // The model library. Devices repeat across regions where the
    // same model is sold there (regional UA build-model suffixes
    // differ where they really do: S921U in the US, S921B in
    // Singapore/Taiwan, S911B outside the US).
    private fun galaxyS24(model: String, delta: Int) = FpDevice(
        id = model, deviceModel = "Samsung Galaxy S24", uaModel = model,
        userAgent = ua(model), screenWidth = 360, screenHeight = 780,
        devicePixelRatio = 3.0, hardwareConcurrency = 8, deviceMemory = 8,
        webGlVendor = "Qualcomm", webGlRenderer = "Adreno (TM) 750",
        webGl = GL_ADRENO_750,
        webGpuVendor = "qualcomm", webGpuArchitecture = "adreno-7xx",
        fonts = SAMSUNG_FONTS,
        seedDelta = delta
    )

    private fun galaxyS23(model: String, delta: Int) = FpDevice(
        id = model, deviceModel = "Samsung Galaxy S23", uaModel = model,
        userAgent = ua(model), screenWidth = 360, screenHeight = 780,
        devicePixelRatio = 3.0, hardwareConcurrency = 8, deviceMemory = 8,
        webGlVendor = "Qualcomm", webGlRenderer = "Adreno (TM) 740",
        webGl = GL_ADRENO_740,
        webGpuVendor = "qualcomm", webGpuArchitecture = "adreno-7xx",
        fonts = SAMSUNG_FONTS,
        seedDelta = delta
    )

    private fun pixel8(delta: Int) = FpDevice(
        id = "Pixel 8", deviceModel = "Google Pixel 8",
        uaModel = "Pixel 8", userAgent = ua("Pixel 8"),
        screenWidth = 412, screenHeight = 915, devicePixelRatio = 2.625,
        // Tensor G3 is a NINE-core part (1+4+4); a Pixel 8
        // reports hardwareConcurrency 9, not 8 (corrected in
        // v0.162.0 — the core count is public spec-sheet data
        // and one of the first numbers a cross-check reads).
        hardwareConcurrency = 9, deviceMemory = 8,
        webGlVendor = "ARM",
        webGlRenderer = "ANGLE (ARM, Mali-G715s MC10, OpenGL ES 3.2)",
        webGl = GL_MALI_G715,
        webGpuVendor = "arm", webGpuArchitecture = "valhall",
        fonts = GOOGLE_FONTS,
        seedDelta = delta
    )

    private fun pixel8Pro(delta: Int) = FpDevice(
        id = "Pixel 8 Pro", deviceModel = "Google Pixel 8 Pro",
        uaModel = "Pixel 8 Pro", userAgent = ua("Pixel 8 Pro"),
        // The Pro's panel is 1344×2992 at DPR 3.0 — CSS screen
        // 448×997, NOT the base Pixel 8's 412×915 (corrected in
        // v0.162.0; it previously borrowed the smaller phone's
        // geometry, a spec-sheet check away from exposure).
        screenWidth = 448, screenHeight = 997, devicePixelRatio = 3.0,
        // 12 GB of RAM on the spec sheet, but the Device Memory
        // API buckets and caps at 8 — the page-visible value is 8.
        // Tensor G3: nine cores, like the base Pixel 8.
        hardwareConcurrency = 9, deviceMemory = 8,
        webGlVendor = "ARM",
        webGlRenderer = "ANGLE (ARM, Mali-G715s MC10, OpenGL ES 3.2)",
        webGl = GL_MALI_G715,
        webGpuVendor = "arm", webGpuArchitecture = "valhall",
        fonts = GOOGLE_FONTS,
        seedDelta = delta
    )

    private fun pixel7a(delta: Int) = FpDevice(
        id = "Pixel 7a", deviceModel = "Google Pixel 7a",
        uaModel = "Pixel 7a", userAgent = ua("Pixel 7a"),
        screenWidth = 412, screenHeight = 915, devicePixelRatio = 2.625,
        hardwareConcurrency = 8, deviceMemory = 8,
        webGlVendor = "ARM",
        webGlRenderer = "ANGLE (ARM, Mali-G710 MC10, OpenGL ES 3.2)",
        // The G710 is the G715's immediate predecessor class; the
        // capability table is carried over (recorded in the
        // v0.146.0 checkpoint chapter, not diffed on hardware).
        webGl = GL_MALI_G715,
        webGpuVendor = "arm", webGpuArchitecture = "valhall",
        fonts = GOOGLE_FONTS,
        seedDelta = delta
    )

    private fun galaxyA54(delta: Int) = FpDevice(
        id = "SM-A546B", deviceModel = "Samsung Galaxy A54",
        uaModel = "SM-A546B", userAgent = ua("SM-A546B"),
        screenWidth = 412, screenHeight = 891, devicePixelRatio = 2.625,
        hardwareConcurrency = 8, deviceMemory = 8,
        webGlVendor = "ARM",
        webGlRenderer = "ANGLE (ARM, Mali-G68, OpenGL ES 3.2)",
        webGl = GL_MALI_G68,
        webGpuVendor = "arm", webGpuArchitecture = "valhall",
        fonts = SAMSUNG_FONTS,
        seedDelta = delta
    )

    val UNITED_STATES = FingerprintProfile(
        regionId = "US",
        regionLabel = "美国",
        language = "en-US",
        languageOptions = listOf("en-US", "es-US"),
        noiseSeed = 1401,
        batteryLevel = 0.82,
        batteryCharging = false,
        dateFormatSample = "11/25/2025",
        voices = listOf(
            FpVoice("English (United States)", "en-US", isDefault = true),
            FpVoice("English (United Kingdom)", "en-GB"),
            FpVoice("Español (Estados Unidos)", "es-US")
        ),
        colorGamut = "p3",
        dynamicRangeHigh = true,
        connectionRtt = 50,
        connectionDownlink = 10.0,
        timezones = listOf(
            FpTimezone(
                "America/New_York", "东部时间", "纽约",
                40.7128, -74.0060, "Eastern Standard Time",
                "Eastern Daylight Time"
            ),
            FpTimezone(
                "America/Chicago", "中部时间", "芝加哥",
                41.8781, -87.6298, "Central Standard Time",
                "Central Daylight Time"
            ),
            FpTimezone(
                "America/Denver", "山地时间", "丹佛",
                39.7392, -104.9903, "Mountain Standard Time",
                "Mountain Daylight Time"
            ),
            FpTimezone(
                "America/Los_Angeles", "太平洋时间", "洛杉矶",
                34.0522, -118.2437, "Pacific Standard Time",
                "Pacific Daylight Time"
            )
        ),
        devices = listOf(
            galaxyS24("SM-S921U", 0),
            galaxyS23("SM-S911U", 1),
            pixel8Pro(2),
            pixel8(3)
        )
    )

    val JAPAN = FingerprintProfile(
        regionId = "JP",
        regionLabel = "日本",
        language = "ja-JP",
        languageOptions = listOf("ja-JP"),
        noiseSeed = 1402,
        batteryLevel = 0.67,
        batteryCharging = false,
        dateFormatSample = "2025/11/25",
        voices = listOf(
            FpVoice("日本語", "ja-JP", isDefault = true),
            FpVoice("English (United States)", "en-US"),
            FpVoice("中文 (普通话)", "zh-CN")
        ),
        colorGamut = "p3",
        dynamicRangeHigh = true,
        connectionRtt = 45,
        connectionDownlink = 10.0,
        timezones = listOf(
            FpTimezone(
                "Asia/Tokyo", "日本时间", "东京",
                35.6762, 139.6503, "Japan Standard Time"
            )
        ),
        devices = listOf(
            pixel8(0),
            pixel8Pro(1),
            pixel7a(2),
            galaxyS23("SM-S911B", 3)
        )
    )

    val SINGAPORE = FingerprintProfile(
        regionId = "SG",
        regionLabel = "新加坡",
        language = "en-SG",
        languageOptions = listOf("en-SG", "zh-CN", "ms-MY", "ta-IN"),
        noiseSeed = 1403,
        batteryLevel = 0.91,
        batteryCharging = true,
        dateFormatSample = "25/11/2025",
        voices = listOf(
            FpVoice("English (United States)", "en-US", isDefault = true),
            FpVoice("中文 (普通话)", "zh-CN"),
            FpVoice("Bahasa Melayu", "ms-MY"),
            FpVoice("Tamil", "ta-IN")
        ),
        colorGamut = "p3",
        dynamicRangeHigh = true,
        connectionRtt = 35,
        connectionDownlink = 10.0,
        timezones = listOf(
            FpTimezone(
                "Asia/Singapore", "新加坡时间", "新加坡",
                1.3521, 103.8198, "Singapore Standard Time"
            )
        ),
        devices = listOf(
            galaxyS23("SM-S911B", 0),
            galaxyS24("SM-S921B", 1),
            pixel8Pro(2),
            pixel8(3)
        )
    )

    val TAIWAN = FingerprintProfile(
        regionId = "TW",
        regionLabel = "台湾地区",
        language = "zh-TW",
        languageOptions = listOf("zh-TW"),
        noiseSeed = 1404,
        batteryLevel = 0.74,
        batteryCharging = false,
        dateFormatSample = "2025/11/25",
        voices = listOf(
            FpVoice("中文 (台灣)", "zh-TW", isDefault = true),
            FpVoice("English (United States)", "en-US"),
            FpVoice("中文 (普通话)", "zh-CN")
        ),
        colorGamut = "p3",
        dynamicRangeHigh = true,
        connectionRtt = 50,
        connectionDownlink = 9.2,
        timezones = listOf(
            FpTimezone(
                "Asia/Taipei", "台北时间", "台北",
                25.0330, 121.5654, "Taipei Standard Time"
            )
        ),
        devices = listOf(
            galaxyA54(0),
            galaxyS24("SM-S921B", 1),
            pixel8(2),
            pixel7a(3)
        )
    )

    /** All regions, in picker display order. */
    val all: List<FingerprintProfile> =
        listOf(UNITED_STATES, JAPAN, SINGAPORE, TAIWAN)

    /** The region [id], or the first region when unknown/null. */
    fun region(id: String?): FingerprintProfile =
        all.firstOrNull { it.regionId == id } ?: all.first()

    /**
     * navigator.languages for a primary [language]: the primary,
     * then the fallbacks a real device in that language community
     * carries — the base language and English behind non-English
     * primaries, en-US and generic en behind English variants.
     * Pinned by FingerprintProfilesTest (v0.145.0: never a lone
     * entry, the failure the real-device report showed).
     */
    fun languagesFor(language: String): List<String> {
        val out = mutableListOf(language)
        if (language == "en-US") {
            out.add("en")
            return out
        }
        if (language.startsWith("en-")) {
            out.add("en-US")
            out.add("en")
            return out
        }
        val base = language.substringBefore('-')
        if (base != language) out.add(base)
        out.add("en-US")
        out.add("en")
        return out.distinct()
    }

    /**
     * Resolve stored ids to the live combination. Every component
     * falls back INSIDE its own region's set — an unknown device
     * or a language from another region resolves to the region's
     * default, never to a cross-region mix (v0.146.0).
     */
    fun resolve(
        regionId: String?,
        timezoneId: String?,
        deviceId: String? = null,
        language: String? = null
    ): ResolvedFingerprint {
        val profile = region(regionId)
        return ResolvedFingerprint(
            profile = profile,
            timezone = profile.timezone(timezoneId),
            device = profile.device(deviceId),
            language = profile.languageOrDefault(language)
        )
    }
}
