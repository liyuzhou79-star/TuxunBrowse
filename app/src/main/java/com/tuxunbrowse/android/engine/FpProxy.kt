package com.tuxunbrowse.android.engine

import androidx.webkit.ProxyConfig
import androidx.webkit.ProxyController
import androidx.webkit.WebViewFeature
import org.json.JSONObject

/**
 * Fingerprint mode's proxy setting (v0.197.0; one saved
 * config per combination since v0.198.0, see FpProxyBook),
 * in the shape the anti-detect browsers treat as a profile's
 * basic equipment — Octo and GoLogin both bind a proxy to
 * the identity so the IP story and the browser story come
 * from the same place.
 *
 * What the platform actually offers, honestly bounded:
 * - The WebView proxy override (androidx.webkit
 *   ProxyController) is APP-WIDE: while it is set, every
 *   WebView in the app uses it — the fingerprint tabs and the
 *   normal WebView tabs alike. GeckoView does not read it at
 *   all. There is no per-tab or per-profile proxy granularity
 *   in the WebView API.
 * - The mode therefore applies the override when fingerprint
 *   mode is entered and clears it when the mode is exited —
 *   the only window in which fingerprint tabs are on screen.
 *   The mode's proxy card says exactly this, including that a
 *   normal WebView tab used in that window would share it.
 * - Proxy authentication is not offered: the WebView proxy
 *   API has no credential channel (page-level auth prompts do
 *   not cover proxy auth), so the form takes host and port
 *   only and says so.
 *
 * The config is a device-side mode setting like the other
 * fp_* keys: it stays out of backups.
 */
data class FpProxyConfig(
    val enabled: Boolean = false,
    val scheme: String = "http",
    val host: String = "",
    val port: Int = 0
) {
    /** True when the form's contents can actually be applied. */
    val isUsable: Boolean
        get() = enabled && host.isNotBlank() &&
            port in 1..65535 && scheme in SCHEMES

    /** The Chromium proxy rule the override is built from. */
    val rule: String
        get() = "$scheme://$host:$port"

    /** Short human summary for the mode's proxy card. */
    val summary: String
        get() = if (host.isBlank() || port !in 1..65535) {
            "未设置"
        } else {
            "$scheme://$host:$port" + if (enabled) "" else "（已停用）"
        }

    fun toJson(): String = JSONObject()
        .put("enabled", enabled)
        .put("scheme", scheme)
        .put("host", host)
        .put("port", port)
        .toString()

    companion object {
        val SCHEMES: List<String> = listOf("http", "https", "socks5")

        /** Parse a stored config; anything broken reads as unset. */
        fun fromJson(raw: String?): FpProxyConfig {
            if (raw.isNullOrBlank()) return FpProxyConfig()
            return try {
                val obj = JSONObject(raw)
                val scheme = obj.optString("scheme", "http")
                FpProxyConfig(
                    enabled = obj.optBoolean("enabled", false),
                    scheme = if (scheme in SCHEMES) scheme else "http",
                    host = obj.optString("host", "").trim(),
                    port = obj.optInt("port", 0)
                )
            } catch (t: Throwable) {
                FpProxyConfig()
            }
        }
    }
}

/**
 * The per-combination proxy book (v0.198.0): one
 * [FpProxyConfig] per fingerprint combination, keyed by the
 * same region+device combination key the storage profiles
 * use (FpStorageNames.keyOf), serialised as one JSON object
 * of key → config-JSON. Commercial anti-detect browsers bind
 * a proxy to each profile; the WebView proxy override itself
 * is still app-wide while the mode is open (see FpProxy's
 * header), so what is per-combination here is the SAVED
 * configuration — switching combinations applies that
 * combination's saved proxy, exactly as it swaps its storage
 * jar. Pure functions over strings so the book's contract
 * (broken input degrades to an empty book, never a half-read
 * one) is unit-testable.
 */
object FpProxyBook {

    /** Serialise the book; unset combinations are absent. */
    fun encode(book: Map<String, FpProxyConfig>): String {
        val obj = JSONObject()
        for ((key, config) in book) {
            obj.put(key, config.toJson())
        }
        return obj.toString()
    }

    /** Parse a stored book; anything broken reads as empty. */
    fun decode(raw: String?): Map<String, FpProxyConfig> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val out = LinkedHashMap<String, FpProxyConfig>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                out[key] = FpProxyConfig.fromJson(
                    obj.optString(key, "")
                )
            }
            out
        } catch (t: Throwable) {
            emptyMap()
        }
    }
}

/** Applying and clearing the app-wide WebView proxy override. */
object FpProxyControl {

    /** Whether the host WebView offers the proxy override API. */
    fun isSupported(): Boolean = try {
        WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)
    } catch (t: Throwable) {
        false
    }

    /**
     * Route all WebView traffic through [config]'s proxy.
     * Best-effort: an unsupported host or an unusable config
     * leaves routing untouched.
     */
    fun apply(config: FpProxyConfig) {
        if (!isSupported() || !config.isUsable) return
        try {
            val proxyConfig = ProxyConfig.Builder()
                .addProxyRule(config.rule)
                .build()
            ProxyController.getInstance().setProxyOverride(
                proxyConfig,
                { runnable -> runnable.run() },
                Runnable {}
            )
        } catch (t: Throwable) {
            // Routing must never break tab creation or mode entry.
        }
    }

    /** Drop the override; WebViews go back to direct routing. */
    fun clear() {
        if (!isSupported()) return
        try {
            ProxyController.getInstance().clearProxyOverride(
                { runnable -> runnable.run() },
                Runnable {}
            )
        } catch (t: Throwable) {
            // Best-effort, like apply.
        }
    }
}
