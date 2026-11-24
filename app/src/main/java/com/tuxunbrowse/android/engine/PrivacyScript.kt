package com.tuxunbrowse.android.engine

/**
 * The user's privacy-signal choices (v0.92.0), read live from the
 * Store by the app and handed to every tab. All three signals are
 * WebView-side mechanisms (plus request headers on app-initiated
 * loads); the Gecko side honours GPC through a native runtime
 * setting instead and cannot do the other two at all — see the
 * Store properties' docs.
 */
data class PrivacySignals(
    val doNotTrack: Boolean = false,
    val globalPrivacyControl: Boolean = false,
    val webRtcDisabled: Boolean = false
) {
    /** True when at least one signal needs the injection channel. */
    val needsScript: Boolean
        get() = doNotTrack || globalPrivacyControl || webRtcDisabled

    /** True when app-initiated loads should carry extra headers. */
    val needsHeaders: Boolean
        get() = doNotTrack || globalPrivacyControl

    /** Request headers for app-initiated WebView loads. */
    fun headers(): Map<String, String> {
        val out = mutableMapOf<String, String>()
        if (doNotTrack) out["DNT"] = "1"
        if (globalPrivacyControl) out["Sec-GPC"] = "1"
        return out
    }
}

/**
 * Document-start script carrying the privacy signals (v0.92.0).
 *
 * Why a script at all: the system WebView offers no API to attach
 * a header to every request a page makes, and `DNT` is on the
 * Fetch spec's forbidden-header list, so page JavaScript cannot
 * set it either. What sites actually read for these two signals
 * in practice is the JavaScript surface — `navigator.doNotTrack`
 * for DNT and `navigator.globalPrivacyControl` for GPC (the
 * latter is part of the GPC specification itself) — and that
 * surface a document-start script CAN set, before any page script
 * runs. The real `DNT: 1` / `Sec-GPC: 1` headers are added on top
 * for the loads the app itself initiates (see BrowserTab.load).
 *
 * WebRTC disabling replaces `RTCPeerConnection` (and its webkit
 * alias) with a stub whose constructor throws: a page then gets
 * no ICE candidates at all, so the real IP address behind a VPN
 * cannot leak through peer connections. Camera/microphone capture
 * (getUserMedia) is deliberately untouched — it has its own
 * permission flow and is not the IP-leak channel.
 *
 * Every snippet is defineProperty/assignment wrapped in try/catch:
 * a page (or a future WebView) that refuses one definition must
 * not keep the other signals from applying.
 */
object PrivacyScript {

    private const val DNT_SNIPPET = """
        try {
            Object.defineProperty(window.navigator, 'doNotTrack', {
                get: function () { return '1'; },
                configurable: true
            });
        } catch (e) { /* signal is best-effort */ }
"""

    private const val GPC_SNIPPET = """
        try {
            Object.defineProperty(window.navigator, 'globalPrivacyControl', {
                get: function () { return true; },
                configurable: true
            });
        } catch (e) { /* signal is best-effort */ }
"""

    private const val WEBRTC_SNIPPET = """
        try {
            var BlockedRTC = function () {
                throw new Error('WebRTC is disabled by the browser settings');
            };
            BlockedRTC.generateCertificate = function () {
                return Promise.reject(
                    new Error('WebRTC is disabled by the browser settings')
                );
            };
            window.RTCPeerConnection = BlockedRTC;
            window.webkitRTCPeerConnection = BlockedRTC;
        } catch (e) { /* signal is best-effort */ }
"""

    /**
     * The injectable payload for [signals], or null when nothing
     * is on (the caller then registers no handler at all). Pure
     * assembly, split out so unit tests can pin which snippet each
     * flag contributes.
     */
    fun payload(signals: PrivacySignals): String? {
        if (!signals.needsScript) return null
        val out = StringBuilder("(function () {\n    'use strict';\n")
        if (signals.doNotTrack) out.append(DNT_SNIPPET)
        if (signals.globalPrivacyControl) out.append(GPC_SNIPPET)
        if (signals.webRtcDisabled) out.append(WEBRTC_SNIPPET)
        out.append("})();\n")
        return out.toString()
    }
}
