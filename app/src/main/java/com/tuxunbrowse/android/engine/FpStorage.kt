package com.tuxunbrowse.android.engine

import android.webkit.WebView
import androidx.webkit.Profile
import androidx.webkit.ProfileStore
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * Storage-profile naming for fingerprint mode (v0.198.0).
 *
 * One WebView storage profile per fingerprint COMBINATION,
 * where a combination is region + device — the persona unit
 * the mode's picker and 「我的档案」both deal in. Time zone
 * and language are presentation settings of the same persona
 * (a real owner changes their phone's zone or language
 * without losing a single login), so they deliberately do
 * NOT split the jar: the plain jar count is bounded by the
 * library itself (4 regions × 4 devices = 16) plus the legacy
 * jar. Seeded random-device entries (v0.224.0) are the one
 * exception: each carries its own seed and its own jar
 * ([nameForSeeded]), bounded by the saved-entries cap.
 *
 * Pure string work, no Android calls, so the naming contract
 * is unit-testable: names use only [a-z0-9_], always carry
 * the [PREFIX], and the pre-v0.198 single jar keeps its name
 * for whichever combination owned it (see FpStorage).
 */
object FpStorageNames {

    /** The v0.196.0 single-jar name, kept as the legacy jar. */
    const val LEGACY_NAME: String = "tuxun_fp"

    /** Every fingerprint storage profile carries this prefix. */
    const val PREFIX: String = "tuxun_fp"

    /** The identity key of one combination (region + device). */
    fun keyOf(regionId: String, deviceId: String): String =
        "$regionId|$deviceId"

    /**
     * Make [raw] safe for a WebView profile name: lowercase,
     * anything outside [a-z0-9] becomes an underscore, repeats
     * collapse, edges trimmed, length capped.
     */
    fun slug(raw: String): String {
        val mapped = raw.lowercase().map { ch ->
            if (ch in 'a'..'z' || ch in '0'..'9') ch else '_'
        }.joinToString("")
        return mapped.replace(Regex("_+"), "_")
            .trim('_')
            .take(24)
            .trimEnd('_')
    }

    /**
     * The storage profile name for one combination.
     * [legacyOwnerKey] is the combination key that owned the
     * pre-v0.198 single jar (null when there is no legacy
     * jar); that combination keeps binding [LEGACY_NAME] so
     * its existing logins survive the upgrade, and every
     * other combination gets its own derived name.
     */
    fun nameFor(
        regionId: String,
        deviceId: String,
        legacyOwnerKey: String?
    ): String {
        if (legacyOwnerKey != null &&
            legacyOwnerKey == keyOf(regionId, deviceId)
        ) {
            return LEGACY_NAME
        }
        return PREFIX + "_" + slug(regionId) + "_" + slug(deviceId)
    }

    /**
     * The storage profile name for one SEEDED combination
     * (v0.224.0): a random-device entry is its own persona, so
     * its jar carries the seed as an unsigned hex suffix. The
     * shape stays inside the [isFpProfileName] family (prefix
     * + underscores), so clear-data keeps covering it, and it
     * can never collide with a plain combination name — plain
     * names are exactly prefix + two slugs, and no device id
     * slugs to a trailing "_s<hex>" segment of this form
     * because the seed segment is appended AFTER the full
     * device slug and device ids contain no such suffix
     * (pinned by unit test over the whole library).
     */
    fun nameForSeeded(
        regionId: String,
        deviceId: String,
        seed: Int
    ): String =
        PREFIX + "_" + slug(regionId) + "_" + slug(deviceId) +
            "_s" + seed.toUInt().toString(16)

    /** True for names this app created for fingerprint mode. */
    fun isFpProfileName(name: String): Boolean =
        name == LEGACY_NAME || name.startsWith(PREFIX + "_")
}

/**
 * Fingerprint mode's WebView storage profiles (v0.196.0,
 * per-combination since v0.198.0).
 *
 * Until v0.196.0 every WebView in the app shared the ONE
 * default storage profile, so fingerprint tabs' cookies and
 * site data sat in the same jar as normal tabs'. The system
 * WebView's multi-profile API (androidx.webkit, present
 * since 1.9; this app ships 1.11) offers named profiles,
 * each with its own CookieManager, WebStorage and
 * ServiceWorker scope, and a WebView can be bound to one.
 * Fingerprint tabs bind at creation to the profile named
 * for their combination — which is what "profile data
 * remains completely isolated" means in the commercial
 * anti-detect browsers, at storage level, the level this
 * API governs. Two combinations never share a jar, so the
 * same site can hold different logins under different
 * personas, and switching back finds each login intact.
 *
 * The binding has hard edges, all handled here:
 * - Support is a runtime property of the HOST WebView build
 *   (WebViewFeature.MULTI_PROFILE). Where it is missing,
 *   every call here degrades to "not bound" and the tab
 *   shares storage exactly as before — the mode's copy
 *   reads [isSupported] and says which world the user is in.
 * - setProfile must be a WebView's first call after its
 *   constructor; callers bind immediately, and any failure
 *   (late call, OEM quirk) is caught and treated as unbound
 *   rather than allowed to break tab creation.
 * - All ProfileStore calls are UI-thread calls; every entry
 *   point here is invoked from tab creation or settings UI.
 *
 * Migration and cleanup edges, on the record:
 * - The v0.196.0 jar ([FpStorageNames.LEGACY_NAME]) holds
 *   the logins the mode accumulated under the single-jar
 *   regime. Profiles cannot be renamed, so the combination
 *   active at upgrade time keeps that name (the Store
 *   records it once as the legacy owner) and its data is
 *   preserved untouched; every other combination starts
 *   with a fresh jar of its own.
 * - ProfileStore.deleteProfile cannot delete a profile
 *   this process has already loaded (it throws), and every
 *   jar the mode uses is loaded by definition — so jars are
 *   never deleted at runtime. "Clear data" wipes every
 *   fingerprint jar's contents instead; the empty jars left
 *   behind are bounded (library combinations + legacy) and
 *   carry no data.
 */
object FpStorage {

    /** The legacy single-jar name (pre-v0.198.0 installs). */
    const val PROFILE_NAME: String = FpStorageNames.LEGACY_NAME

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

    /** The named storage profile, or null when unsupported. */
    fun profileOrNull(name: String): Profile? {
        if (!isSupported()) return null
        return try {
            ProfileStore.getInstance().getOrCreateProfile(name)
        } catch (t: Throwable) {
            null
        }
    }

    /** Whether a profile with [name] already exists. */
    fun profileExists(name: String): Boolean {
        if (!isSupported()) return false
        return try {
            ProfileStore.getInstance()
                .getAllProfileNames().contains(name)
        } catch (t: Throwable) {
            false
        }
    }

    /** True when the pre-v0.198 single jar exists on disk. */
    fun legacyProfileExists(): Boolean =
        profileExists(FpStorageNames.LEGACY_NAME)

    /** Names of every storage profile fingerprint mode owns. */
    fun fpProfileNames(): List<String> {
        if (!isSupported()) return emptyList()
        return try {
            ProfileStore.getInstance().getAllProfileNames()
                .filter { FpStorageNames.isFpProfileName(it) }
        } catch (t: Throwable) {
            emptyList()
        }
    }

    /**
     * Bind a freshly constructed [view] to the storage
     * profile [name]. Returns true when the binding took.
     * Must be called before any other method on the view.
     * (The compat API binds by profile NAME; the Profile
     * object from [profileOrNull] is what cookie and storage
     * calls go through.)
     */
    fun bind(view: WebView, name: String): Boolean {
        if (!isSupported()) return false
        return try {
            // Ensure the profile exists before binding to it.
            profileOrNull(name) ?: return false
            WebViewCompat.setProfile(view, name)
            true
        } catch (t: Throwable) {
            false
        }
    }

    /** The named profile's cookie for [url], for downloads. */
    fun cookieFor(url: String, name: String): String? {
        val profile = profileOrNull(name) ?: return null
        return try {
            profile.cookieManager.getCookie(url)
        } catch (t: Throwable) {
            null
        }
    }

    /**
     * The cookie header a fingerprint tab's system download
     * may carry (v0.203.0), as a three-way answer the caller
     * passes straight to startSystemDownload:
     * - Multi-profile UNSUPPORTED: null. The tab itself lives
     *   on the shared default jar, so the downloader's own
     *   fallback to that jar reads exactly the session the tab
     *   has — consistent, because there is only one jar.
     * - Supported: the combination jar's cookie, or "" when
     *   that jar has none for the URL. The empty string is an
     *   explicit "no cookie": startSystemDownload only falls
     *   back to the default jar on a null header, so a
     *   fingerprint download can never borrow the NORMAL
     *   persona's session just because its own jar happens to
     *   have nothing for the URL (the pre-0.203 behaviour —
     *   a cross-persona leak through the download path).
     * Failing closed also covers a profile read error: no
     * cookie beats the wrong persona's cookie.
     */
    fun downloadCookie(url: String, name: String): String? {
        if (!isSupported()) return null
        return cookieFor(url, name) ?: ""
    }

    /**
     * Wipe the cookies and site storage of EVERY storage
     * profile fingerprint mode owns (legacy jar included).
     * The settings clear-data flow calls this alongside the
     * default profile's wipe, so "clear everything" keeps
     * meaning everything now that the mode has jars of its
     * own — including jars of combinations not currently
     * selected, whose logins a partial clear would silently
     * spare while claiming a full one.
     */
    fun clearData() {
        for (name in fpProfileNames()) {
            val profile = profileOrNull(name) ?: continue
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
}
