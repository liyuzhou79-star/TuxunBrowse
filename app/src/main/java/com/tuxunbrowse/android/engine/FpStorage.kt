package com.tuxunbrowse.android.engine

import android.webkit.WebView
import androidx.webkit.Profile
import androidx.webkit.ProfileStore
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * Fingerprint mode's own WebView storage profile (v0.196.0).
 *
 * Until this version every WebView in the app shared the ONE
 * default storage profile, so fingerprint tabs' cookies and
 * site data sat in the same jar as normal tabs' — the mode's
 * home copy said so plainly. The system WebView's multi-profile
 * API (androidx.webkit, present since 1.9; this app ships
 * 1.11) offers named profiles, each with its own CookieManager,
 * WebStorage and ServiceWorker scope, and a WebView can be
 * bound to one. Fingerprint tabs now bind to [PROFILE_NAME]
 * at creation, which is what "profile data remains completely
 * isolated" means in the commercial anti-detect browsers —
 * at storage level, which is the level this API governs.
 *
 * The binding has hard edges, all handled here:
 * - Support is a runtime property of the HOST WebView build
 *   (WebViewFeature.MULTI_PROFILE). Where it is missing, every
 *   call here degrades to "not bound" and the tab shares
 *   storage exactly as before — the mode's copy reads
 *   [isSupported] and says which world the user is in.
 * - setProfile must be a WebView's first call after its
 *   constructor; callers bind immediately, and any failure
 *   (late call, OEM quirk) is caught and treated as unbound
 *   rather than allowed to break tab creation.
 * - All ProfileStore calls are UI-thread calls; every entry
 *   point here is invoked from tab creation or settings UI.
 *
 * One profile for the whole mode, not one per region/device:
 * the mode presents ONE alternate identity at a time (switching
 * profile rebuilds the tab group), so a single storage jar for
 * the mode matches the product shape and keeps the clear-data
 * story simple. Per-combination jars are a possible later step,
 * not a silent promise.
 */
object FpStorage {

    const val PROFILE_NAME: String = "tuxun_fp"

    @Volatile
    private var supportedCache: Boolean? = null

    /** Whether the host WebView offers the multi-profile API. */
    fun isSupported(): Boolean {
        supportedCache?.let { return it }
        val supported = try {
            WebViewFeature.isFeatureSupported(
                WebViewFeature.MULTI_PROFILE
            )
        } catch (t: Throwable) {
            false
        }
        supportedCache = supported
        return supported
    }

    /** The mode's storage profile, or null when unsupported. */
    fun profileOrNull(): Profile? {
        if (!isSupported()) return null
        return try {
            ProfileStore.getInstance()
                .getOrCreateProfile(PROFILE_NAME)
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Bind a freshly constructed [view] to the mode's profile.
     * Returns true when the binding took. Must be called before
     * any other method on the view.
     */
    fun bind(view: WebView): Boolean {
        val profile = profileOrNull() ?: return false
        return try {
            WebViewCompat.setProfile(view, profile)
            true
        } catch (t: Throwable) {
            false
        }
    }

    /** The mode profile's cookie for [url], for download calls. */
    fun cookieFor(url: String): String? {
        val profile = profileOrNull() ?: return null
        return try {
            profile.cookieManager.getCookie(url)
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * Wipe the mode profile's cookies and site storage. The
     * settings clear-data flow calls this alongside the default
     * profile's wipe, so "clear everything" keeps meaning
     * everything now that the mode has a jar of its own.
     */
    fun clearData() {
        val profile = profileOrNull() ?: return
        try {
            profile.cookieManager.removeAllCookies(null)
            profile.cookieManager.flush()
        } catch (t: Throwable) {
            // Best-effort, like the default profile's wipe.
        }
        try {
            profile.webStorage.deleteAllData()
        } catch (t: Throwable) {
            // Best-effort, like the default profile's wipe.
        }
    }
}
