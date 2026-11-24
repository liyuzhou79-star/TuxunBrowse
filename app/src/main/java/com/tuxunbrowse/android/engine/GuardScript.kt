package com.tuxunbrowse.android.engine

import android.content.Context

/**
 * The built-in page-guard user script (v0.91.0).
 *
 * The script body ships verbatim as the asset `page_guard.js`
 * (Via Liquid Glass Pro 7.0.0, supplied by the user): six engines
 * in one — ad purge, malicious-redirect guard, anti-tracking /
 * anti-fingerprint, load speed-up, reading unlock, and a night
 * mode. It is a document-start style script: its hooks (fetch /
 * XHR interception, canvas/WebGL fingerprint noise, redirect
 * guards) only work if it runs before the page's own scripts, so
 * [BrowserTab] injects it through the earliest channels the
 * platform offers (see TabController's guard-script wiring).
 *
 * Two wrappers are added around the verbatim body:
 *
 * - A prelude that seeds the script's own localStorage config:
 *   the five always-on engines are pinned on, the script's own
 *   night mode stays off (the app has its own web-dark setting),
 *   and any stale per-site record for this host is dropped, so a
 *   site can never be silently opted out or darkened by a record
 *   the user cannot see. The app's single settings switch is the
 *   only control; nothing inside the page can change this.
 *
 * - An epilogue that hides the script's floating pill / panel UI.
 *   The script mounts that UI inside a shadow root under
 *   `#__via_lg_v65_host__` and re-mounts the host if it is ever
 *   removed, so removal would just bring it back. The shadow root
 *   is open, though: a `display: none !important` stylesheet
 *   appended into it wins over the script's own non-important
 *   inline display changes and keeps the pill, panel, toast and
 *   element-picker bar permanently invisible while every engine
 *   keeps running underneath.
 *
 * The payload is built once per process and cached: the body is
 * ~150 KB and every WebView tab would otherwise re-read and
 * re-concatenate it.
 */
object GuardScript {

    private const val ASSET_NAME = "page_guard.js"

    /** localStorage key of the script's global config (its own). */
    private const val PRELUDE = """
(function () {
    try {
        localStorage.setItem(
            '__via_lg_v65_global_cfg__',
            JSON.stringify({
                masterAdPurge: true,
                masterRedirectGuard: true,
                masterPrivacyShield: true,
                masterSpeedBoost: true,
                masterReadUnlock: true,
                darkMode: false,
                darkAutoSystem: false
            })
        );
        localStorage.removeItem(
            '__via_lg_v65_site_cfg__' + location.hostname
        );
    } catch (e) { /* storage unavailable: defaults apply */ }
})();
"""

    /** Shadow-root stylesheet that keeps the script's UI hidden. */
    private const val EPILOGUE = """
(function () {
    function hideGuardUi() {
        var host = document.getElementById('__via_lg_v65_host__');
        if (!host || !host.shadowRoot) return;
        if (host.shadowRoot.getElementById('__tuxun_hide__')) return;
        var st = document.createElement('style');
        st.id = '__tuxun_hide__';
        st.textContent =
            '.lg-pill-wrap,.lg-panel,.lg-toast,.lg-picker-bar,.lg-backdrop' +
            '{display:none !important;}';
        host.shadowRoot.appendChild(st);
    }
    hideGuardUi();
    var tries = 0;
    var timer = setInterval(function () {
        hideGuardUi();
        if (++tries > 40) clearInterval(timer);
    }, 250);
})();
"""

    @Volatile
    private var cachedPayload: String? = null

    /** The full injectable payload: prelude + verbatim body + epilogue. */
    fun payload(context: Context): String {
        cachedPayload?.let { return it }
        val body = context.applicationContext.assets.open(ASSET_NAME)
            .bufferedReader(Charsets.UTF_8).use { it.readText() }
        val built = wrap(body)
        cachedPayload = built
        return built
    }

    /** Pure assembly, split out so unit tests can pin the ordering. */
    fun wrap(scriptBody: String): String =
        PRELUDE + "\n" + scriptBody + "\n" + EPILOGUE
}
