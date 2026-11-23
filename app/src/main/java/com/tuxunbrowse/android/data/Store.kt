package com.tuxunbrowse.android.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

enum class Engine(val id: String, val label: String) {
    GECKO("gecko", "火狐内核（GeckoView）"),
    WEBVIEW("webview", "系统 WebView");

    companion object {
        fun fromId(id: String?): Engine = entries.firstOrNull { it.id == id } ?: GECKO
    }
}

enum class SearchEngine(val id: String, val label: String, val template: String) {
    BAIDU("baidu", "百度", "https://www.baidu.com/s?wd=%s"),
    BING("bing", "必应", "https://www.bing.com/search?q=%s"),
    GOOGLE("google", "Google", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s"),

    /** User-supplied engine; its real template lives in the Store. */
    CUSTOM("custom", "自定义", "");

    fun searchUrl(query: String): String {
        // CUSTOM has no built-in template (the user's own is applied
        // by resolveInput); never format an empty template.
        val effective = template.ifBlank { BAIDU.template }
        return effective.format(java.net.URLEncoder.encode(query, "UTF-8"))
    }

    companion object {
        fun fromId(id: String?): SearchEngine =
            entries.firstOrNull { it.id == id } ?: BAIDU
    }
}

data class Bookmark(val title: String, val url: String, val pinned: Boolean = false)
data class HistoryItem(val title: String, val url: String, val time: Long)

/** One "read it later" entry: a saved page, with its read/unread state. */
data class ReadingItem(
    val title: String,
    val url: String,
    val time: Long,
    val read: Boolean = false
)

/**
 * One home-page speed dial: a user-defined shortcut, stored apart
 * from bookmarks (its own list, its own JSON key).
 */
data class SpeedDial(val title: String, val url: String)

/** One address-bar suggestion row, from a bookmark or a history entry. */
data class Suggestion(val title: String, val url: String, val bookmarked: Boolean)

/**
 * Address-bar suggestions: bookmarks first, then history, matched by a
 * case-insensitive substring on title or URL, de-duplicated by URL.
 */
fun matchSuggestions(
    query: String,
    bookmarks: List<Bookmark>,
    history: List<HistoryItem>,
    limit: Int = 8
): List<Suggestion> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    val out = mutableListOf<Suggestion>()
    val seen = mutableSetOf<String>()
    fun consider(title: String, url: String, bookmarked: Boolean) {
        if (out.size >= limit || !url.startsWith("http")) return
        if (title.lowercase().contains(q) || url.lowercase().contains(q)) {
            if (seen.add(url)) {
                out.add(Suggestion(title.ifBlank { url }, url, bookmarked))
            }
        }
    }
    bookmarks.forEach { consider(it.title, it.url, true) }
    history.forEach { consider(it.title, it.url, false) }
    return out
}

/** A normal (never incognito) tab saved so the session survives a restart. */
data class SavedTab(val engine: Engine, val url: String, val desktopMode: Boolean)

/** Internal URL of the native home page. Never handed to a web engine. */
const val HOME_URL = "tuxun://home"

/**
 * Inbound-URL guard (v0.31.0 security audit). Anything that arrives
 * from OUTSIDE the app and is about to be opened as a page — a link
 * handed over by another app, the clipboard hint, a release page URL
 * from the update check, an imported bookmark, a saved speed dial —
 * may only be http://, https://, or the app's own internal home URL.
 * file:, javascript:, data:, content:, intent: and every other scheme
 * are refused at this one gate (see BrowserApp.openUrl), never opened.
 * In-page navigation is NOT routed through here: pages linking among
 * themselves are the engines' own business (shouldOverrideUrlLoading /
 * onLoadRequest), and subresource loads (a data: image inside a page,
 * say) never pass through openUrl at all.
 */
fun isAllowedPageUrl(url: String): Boolean =
    url == HOME_URL ||
        url.startsWith("http://") ||
        url.startsWith("https://")

/** Schemes that must never be opened as a page, however they arrive. */
private val BLOCKED_PAGE_SCHEMES = listOf(
    "javascript:", "data:", "file:", "content:", "intent:",
    "about:", "view-source:", "blob:"
)

/**
 * True when typed or pasted text starts with one of the blocked
 * schemes above. Used at the places that turn raw text into a URL
 * (address bar, home search, speed-dial and bookmark editors) so the
 * refusal happens with a plain-language message instead of the text
 * being silently reinterpreted as a search or a host name.
 */
fun hasBlockedPageScheme(input: String): Boolean {
    val text = input.trim().lowercase()
    return BLOCKED_PAGE_SCHEMES.any { text.startsWith(it) }
}

/** Package name of the TuxunDown multi-thread downloader app. */
const val TUXUNDOWN_PACKAGE = "com.tuxundown.android"

/** Where a URL-type download goes: the system downloader, TuxunDown, or ask. */
enum class DownloadMode(val id: String, val label: String) {
    SYSTEM("system", "系统下载"),
    TUXUNDOWN("tuxundown", "TuxunDown 多线程下载"),
    ASK("ask", "每次下载时询问");

    companion object {
        fun fromId(id: String?): DownloadMode =
            entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

/** App UI theme: follow the system, always light, or always dark. */
enum class ThemeMode(val id: String, val label: String) {
    SYSTEM("system", "跟随系统"),
    LIGHT("light", "浅色"),
    DARK("dark", "深色");

    companion object {
        fun fromId(id: String?): ThemeMode =
            entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

/**
 * Total size in bytes of a file or directory tree, or null when the
 * path does not exist (the UI shows "—" instead of inventing a number).
 */
fun dirSizeBytes(file: java.io.File?): Long? {
    if (file == null || !file.exists()) return null
    if (file.isFile) return file.length()
    var total = 0L
    val stack = ArrayDeque<java.io.File>()
    stack.add(file)
    while (stack.isNotEmpty()) {
        val cur = stack.removeLast()
        val children = try {
            cur.listFiles()
        } catch (t: Throwable) {
            null
        } ?: continue
        for (child in children) {
            if (child.isDirectory) stack.add(child) else total += child.length()
        }
    }
    return total
}

/** Plain-language size text: "—" for unknown, otherwise B/KB/MB/GB. */
fun formatStorageSize(bytes: Long?): String {
    if (bytes == null) return "—"
    return when {
        bytes < 1024L -> "$bytes B"
        bytes < 1024L * 1024L -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024L * 1024L * 1024L ->
            String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

/**
 * Auto cache management (v0.40.0): when the WebView cache measured
 * at startup exceeds this many bytes, it is cleared automatically.
 * 150 MB, as a named constant so the threshold has exactly one home.
 */
const val WEBVIEW_CACHE_AUTO_CLEAR_THRESHOLD_BYTES: Long = 150L * 1024L * 1024L

/**
 * Size in bytes of the WebView cache only (not cookies, storage or
 * history): the app cacheDir (where the WebView HTTP cache lives)
 * plus any cache-named directory under app_webview, found with the
 * same directory names the v0.20.0 storage card uses. The app_webview
 * walk is depth-limited (3 levels) so a startup call stays cheap;
 * each cache directory found is measured with the full recursive
 * [dirSizeBytes], exactly like the storage card measures.
 */
fun webViewCacheSizeBytes(cacheDir: java.io.File?, dataDir: java.io.File): Long {
    var total = dirSizeBytes(cacheDir) ?: 0L
    val webviewDir = java.io.File(dataDir, "app_webview")
    if (webviewDir.exists()) {
        val stack = ArrayDeque<Pair<java.io.File, Int>>()
        stack.add(webviewDir to 0)
        while (stack.isNotEmpty()) {
            val (cur, depth) = stack.removeLast()
            val children = try {
                cur.listFiles()
            } catch (t: Throwable) {
                null
            } ?: continue
            for (child in children) {
                if (!child.isDirectory) continue
                if (child.name.contains("cache", ignoreCase = true)) {
                    total += dirSizeBytes(child) ?: 0L
                } else if (depth < 3) {
                    stack.add(child to depth + 1)
                }
            }
        }
    }
    return total
}

/** How long normal (never incognito) history is kept. days = 0 means forever. */
enum class HistoryRetention(val id: String, val label: String, val days: Int) {
    FOREVER("forever", "永久保留", 0),
    DAYS_30("days30", "保留 30 天", 30),
    DAYS_7("days7", "保留 7 天", 7);

    companion object {
        fun fromId(id: String?): HistoryRetention =
            entries.firstOrNull { it.id == id } ?: FOREVER
    }
}

fun hostOf(url: String): String? = try {
    java.net.URI(url).host?.lowercase()
} catch (e: Exception) {
    null
}

/** True when [host] is a remembered compat site or a subdomain of one. */
fun isCompatHost(host: String?, sites: Set<String>): Boolean {
    if (host.isNullOrBlank()) return false
    return sites.any { site -> host == site || host.endsWith(".$site") }
}

/** Local persistence: engine choices, search engine, compat sites, bookmarks, history. */
class Store(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("tuxunbrowse", Context.MODE_PRIVATE)

    // SharedPreferences throws ClassCastException when a stored value
    // has the wrong type (damaged or half-migrated data). Every read
    // in this class goes through these guards so corrupted data falls
    // back to the default instead of crashing — several of these
    // getters run during startup, where a crash would be fatal.
    private fun safeString(key: String, def: String?): String? = try {
        prefs.getString(key, def)
    } catch (e: Exception) {
        def
    }

    private fun safeBoolean(key: String, def: Boolean): Boolean = try {
        prefs.getBoolean(key, def)
    } catch (e: Exception) {
        def
    }

    private fun safeInt(key: String, def: Int): Int = try {
        prefs.getInt(key, def)
    } catch (e: Exception) {
        def
    }

    private fun safeLong(key: String, def: Long): Long = try {
        prefs.getLong(key, def)
    } catch (e: Exception) {
        def
    }

    val mainEngineChosen: Boolean
        get() = prefs.contains(KEY_MAIN_ENGINE)

    var mainEngine: Engine
        get() = Engine.fromId(safeString(KEY_MAIN_ENGINE, null))
        set(value) = prefs.edit().putString(KEY_MAIN_ENGINE, value.id).apply()

    var incognitoEngine: Engine
        get() = Engine.fromId(safeString(KEY_INCOGNITO_ENGINE, Engine.GECKO.id))
        set(value) = prefs.edit().putString(KEY_INCOGNITO_ENGINE, value.id).apply()

    val incognitoEngineChosen: Boolean
        get() = prefs.contains(KEY_INCOGNITO_ENGINE)

    var searchEngine: SearchEngine
        get() = SearchEngine.fromId(safeString(KEY_SEARCH_ENGINE, null))
        set(value) = prefs.edit().putString(KEY_SEARCH_ENGINE, value.id).apply()

    /** App UI theme mode (app chrome only; web content is separate). */
    var themeMode: ThemeMode
        get() = ThemeMode.fromId(safeString(KEY_THEME_MODE, null))
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value.id).apply()

    /** Display name of the user's custom search engine (CUSTOM only). */
    var customSearchName: String
        get() = safeString(KEY_CUSTOM_SEARCH_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SEARCH_NAME, value).apply()

    /** URL template of the custom search engine; must contain %s. */
    var customSearchTemplate: String
        get() = safeString(KEY_CUSTOM_SEARCH_TEMPLATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SEARCH_TEMPLATE, value).apply()

    var adBlockEnabled: Boolean
        get() = safeBoolean(KEY_AD_BLOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_AD_BLOCK, value).apply()

    /**
     * Sub-option of ad blocking: also hide the (often empty) ad slots
     * left on WebView pages by injecting a small stylesheet. Only
     * meaningful while [adBlockEnabled] is on; the settings UI greys
     * it out otherwise. WebView tabs only — Gecko has no CSS
     * injection path and its content blocking already covers the
     * tracking side.
     */
    var adHideSlots: Boolean
        get() = safeBoolean(KEY_AD_HIDE_SLOTS, true)
        set(value) = prefs.edit().putBoolean(KEY_AD_HIDE_SLOTS, value).apply()

    /** Web page text size in percent: 85 / 100 / 115 / 130. */
    var textZoomPercent: Int
        get() = safeInt(KEY_TEXT_ZOOM, 100)
        set(value) = prefs.edit().putInt(KEY_TEXT_ZOOM, value).apply()

    /** Ask web pages to render dark (WebView algorithmic darkening). */
    var webDarkMode: Boolean
        get() = safeBoolean(KEY_WEB_DARK, false)
        set(value) = prefs.edit().putBoolean(KEY_WEB_DARK, value).apply()

    /**
     * Allow third-party cookies in system WebView tabs (v0.39.0).
     * Default ON: in a browser, blocking third-party cookies breaks
     * real sign-in flows (SSO redirect chains, payment pages embedded
     * in iframes) far more often than it helps; desktop browsers
     * allow them by default and mobile browsers mostly allow or
     * smart-allow them. Turning it off is more tracking-resistant,
     * but some sites will then fail to sign in. WebView tabs only —
     * Gecko tabs are governed by the kernel's own anti-tracking.
     */
    var allowThirdPartyCookies: Boolean
        get() = safeBoolean(KEY_ALLOW_THIRD_PARTY_COOKIES, true)
        set(value) = prefs.edit().putBoolean(KEY_ALLOW_THIRD_PARTY_COOKIES, value).apply()

    /**
     * No-image mode (v0.46.0): when on, system WebView tabs do not
     * load images at all (loadsImagesAutomatically=false plus
     * blockNetworkImage=true in obtainWebView), which saves data
     * and makes pages open faster on slow networks. Default OFF:
     * most users expect pages to look complete, and images are
     * content, not clutter, until the user says otherwise. WebView
     * tabs only — GeckoView 132 exposes no image-blocking setting
     * to apps (audited against the AAR: no such key in
     * GeckoSessionSettings, no such pref in GeckoRuntimeSettings,
     * no image category in ContentBlocking), so Gecko tabs keep
     * loading images and the settings text says so plainly.
     */
    var blockImages: Boolean
        get() = safeBoolean(KEY_BLOCK_IMAGES, false)
        set(value) = prefs.edit().putBoolean(KEY_BLOCK_IMAGES, value).apply()

    /**
     * Disable JavaScript (v0.47.0): when on, web pages' scripts do
     * not run in either engine (WebView javaScriptEnabled=false;
     * Gecko GeckoSessionSettings.allowJavascript(false), both
     * AAR/platform audited). Default OFF — JavaScript stays on,
     * exactly as before this switch existed (the v0.31.0 audit
     * pinned javaScriptEnabled=true as browser-essential; this
     * switch is the user's own opt-in, not a new default). Pages
     * that lean on scripts for sign-in, buttons or video will
     * break with it on, and the settings text says so plainly.
     */
    var disableJavaScript: Boolean
        get() = safeBoolean(KEY_DISABLE_JAVASCRIPT, false)
        set(value) = prefs.edit().putBoolean(KEY_DISABLE_JAVASCRIPT, value).apply()

    /** Restore the previous session's tabs on launch (vs always home). */
    var startupRestore: Boolean
        get() = safeBoolean(KEY_STARTUP_RESTORE, true)
        set(value) = prefs.edit().putBoolean(KEY_STARTUP_RESTORE, value).apply()

    /** Where URL-type downloads go. Gecko tab downloads never use this. */
    var downloadMode: DownloadMode
        get() = DownloadMode.fromId(safeString(KEY_DOWNLOAD_MODE, null))
        set(value) = prefs.edit().putString(KEY_DOWNLOAD_MODE, value.id).apply()

    /** How long normal history entries are kept before cleanup on launch. */
    var historyRetention: HistoryRetention
        get() = HistoryRetention.fromId(safeString(KEY_HISTORY_RETENTION, null))
        set(value) = prefs.edit().putString(KEY_HISTORY_RETENTION, value.id).apply()

    // ---- Lifetime blocked-request counter (WebView tabs only) ----
    // shouldInterceptRequest can fire dozens of times per page, so
    // hits are accumulated in memory and flushed to preferences only
    // every BLOCKED_FLUSH_EVERY hits or when a few seconds have
    // passed — never one preferences write per blocked request.
    private var totalBlockedMemory: Long = -1L
    private var blockedPendingFlush: Int = 0
    private var blockedLastFlushAt: Long = 0L

    /** Lifetime total of blocked requests (WebView tabs only). */
    val totalBlocked: Long
        get() {
            if (totalBlockedMemory < 0) {
                totalBlockedMemory = safeLong(KEY_TOTAL_BLOCKED, 0L)
            }
            return totalBlockedMemory
        }

    /** Count one more blocked request (same call path as the per-page count). */
    fun noteBlocked() {
        val now = System.currentTimeMillis()
        val updated = totalBlocked + 1
        totalBlockedMemory = updated
        blockedPendingFlush++
        if (blockedPendingFlush >= BLOCKED_FLUSH_EVERY ||
            now - blockedLastFlushAt >= BLOCKED_FLUSH_MS
        ) {
            blockedPendingFlush = 0
            blockedLastFlushAt = now
            prefs.edit().putLong(KEY_TOTAL_BLOCKED, updated).apply()
        }
    }

    /** Reset the lifetime blocked counter to zero, effective at once. */
    fun resetTotalBlocked() {
        totalBlockedMemory = 0L
        blockedPendingFlush = 0
        blockedLastFlushAt = System.currentTimeMillis()
        prefs.edit().putLong(KEY_TOTAL_BLOCKED, 0L).apply()
    }

    fun loadCompatSites(): MutableSet<String> {
        val raw = safeString(KEY_COMPAT_SITES, null) ?: return mutableSetOf()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }.toMutableSet()
        } catch (e: Exception) {
            mutableSetOf()
        }
    }

    fun addCompatSite(domain: String) {
        val sites = loadCompatSites()
        if (sites.add(domain)) saveCompatSites(sites)
    }

    fun removeCompatSite(domain: String) {
        val sites = loadCompatSites()
        if (sites.remove(domain)) saveCompatSites(sites)
    }

    private fun saveCompatSites(sites: Set<String>) {
        val arr = JSONArray()
        sites.forEach { arr.put(it) }
        prefs.edit().putString(KEY_COMPAT_SITES, arr.toString()).apply()
    }

    fun loadBookmarks(): MutableList<Bookmark> = readList(KEY_BOOKMARKS) {
        Bookmark(it.optString("title"), it.optString("url"), it.optBoolean("pinned"))
    }.toMutableList()

    fun saveBookmarks(list: List<Bookmark>) = writeList(KEY_BOOKMARKS, list) {
        JSONObject().put("title", it.title).put("url", it.url).put("pinned", it.pinned)
    }

    fun loadHistory(): MutableList<HistoryItem> = readList(KEY_HISTORY) {
        HistoryItem(it.optString("title"), it.optString("url"), it.optLong("time"))
    }.toMutableList()

    fun saveHistory(list: List<HistoryItem>) = writeList(KEY_HISTORY, list) {
        JSONObject().put("title", it.title).put("url", it.url).put("time", it.time)
    }

    fun clearHistory() = prefs.edit().remove(KEY_HISTORY).apply()

    fun loadReadingList(): MutableList<ReadingItem> = readList(KEY_READING_LIST) {
        ReadingItem(
            it.optString("title"),
            it.optString("url"),
            it.optLong("time"),
            it.optBoolean("read")
        )
    }.toMutableList()

    fun saveReadingList(list: List<ReadingItem>) = writeList(KEY_READING_LIST, list) {
        JSONObject()
            .put("title", it.title)
            .put("url", it.url)
            .put("time", it.time)
            .put("read", it.read)
    }

    fun loadSpeedDials(): MutableList<SpeedDial> = readList(KEY_SPEED_DIALS) {
        SpeedDial(it.optString("title"), it.optString("url"))
    }.toMutableList()

    fun saveSpeedDials(list: List<SpeedDial>) = writeList(KEY_SPEED_DIALS, list) {
        JSONObject().put("title", it.title).put("url", it.url)
    }

    fun saveTabs(list: List<SavedTab>, activeIndex: Int) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("e", it.engine.id)
                    .put("u", it.url)
                    .put("d", it.desktopMode)
            )
        }
        val obj = JSONObject().put("tabs", arr).put("active", activeIndex)
        prefs.edit().putString(KEY_TABS, obj.toString()).apply()
    }

    fun loadTabs(): Pair<List<SavedTab>, Int> {
        val raw = safeString(KEY_TABS, null) ?: return emptyList<SavedTab>() to 0
        return try {
            val obj = JSONObject(raw)
            val arr = obj.optJSONArray("tabs") ?: JSONArray()
            val list = (0 until arr.length()).map {
                val o = arr.getJSONObject(it)
                SavedTab(
                    Engine.fromId(o.optString("e")),
                    o.optString("u"),
                    o.optBoolean("d")
                )
            }.filter { it.url.isNotBlank() }
            list to obj.optInt("active", 0)
        } catch (e: Exception) {
            emptyList<SavedTab>() to 0
        }
    }

    private fun <T> readList(key: String, parse: (JSONObject) -> T): List<T> {
        val raw = safeString(key, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { parse(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun <T> writeList(key: String, list: List<T>, toJson: (T) -> JSONObject) {
        val arr = JSONArray()
        list.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(key, arr.toString()).apply()
    }

    companion object {
        private const val KEY_MAIN_ENGINE = "main_engine"
        private const val KEY_INCOGNITO_ENGINE = "incognito_engine"
        private const val KEY_SEARCH_ENGINE = "search_engine"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_CUSTOM_SEARCH_NAME = "custom_search_name"
        private const val KEY_CUSTOM_SEARCH_TEMPLATE = "custom_search_template"
        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_AD_HIDE_SLOTS = "ad_hide_slots"
        private const val KEY_ALLOW_THIRD_PARTY_COOKIES = "allow_third_party_cookies"
        private const val KEY_BLOCK_IMAGES = "block_images"
        private const val KEY_DISABLE_JAVASCRIPT = "disable_javascript"
        private const val KEY_COMPAT_SITES = "compat_sites"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
        private const val KEY_READING_LIST = "reading_list"
        private const val KEY_TABS = "saved_tabs"
        private const val KEY_TEXT_ZOOM = "text_zoom"
        private const val KEY_WEB_DARK = "web_dark"
        private const val KEY_STARTUP_RESTORE = "startup_restore"
        private const val KEY_DOWNLOAD_MODE = "download_mode"
        private const val KEY_HISTORY_RETENTION = "history_retention"
        private const val KEY_SPEED_DIALS = "speed_dials"
        private const val KEY_TOTAL_BLOCKED = "total_blocked"
        private const val BLOCKED_FLUSH_EVERY = 20
        private const val BLOCKED_FLUSH_MS = 5_000L
    }
}

/**
 * Hard cap on how many entries one imported file may contribute per
 * list. A bookmark/backup file is untrusted input: without a cap a
 * crafted or corrupted file with hundreds of thousands of entries
 * could exhaust memory while parsing and merging. 5,000 per list is
 * far above any real user's data (the app itself caps history at
 * 500, the reading list at 200 and speed dials at 12).
 */
const val MAX_IMPORT_ENTRIES = 5000

/**
 * Hard cap, in characters, on an imported file's text. The import
 * code reads the whole file into memory before parsing, so the read
 * itself must stop somewhere: 8M characters (~16 MB as UTF-16) is
 * far above a real export (5,000 short entries are well under 1 MB)
 * and small enough to never endanger the heap.
 */
const val MAX_IMPORT_FILE_CHARS = 8_000_000

/** Format marker written into every full-backup file. */
const val BACKUP_FORMAT = "tuxunbrowse-backup"

/** Backup format version (bump when the layout changes). */
const val BACKUP_FORMAT_VERSION = 1

/** The settings slice of a full backup. All fields optional on read. */
data class BackupSettings(
    val mainEngine: Engine? = null,
    val incognitoEngine: Engine? = null,
    val searchEngine: SearchEngine? = null,
    val customSearchName: String? = null,
    val customSearchTemplate: String? = null,
    val adBlockEnabled: Boolean? = null,
    val downloadMode: DownloadMode? = null,
    val historyRetention: HistoryRetention? = null,
    val textZoomPercent: Int? = null,
    val startupRestore: Boolean? = null
) {
    /** Plain-language list of what applying these settings would overwrite. */
    fun overwriteSummary(): List<String> {
        val out = mutableListOf<String>()
        if (mainEngine != null) out.add("主内核（默认用哪个内核打开网页）")
        if (incognitoEngine != null) out.add("无痕内核")
        if (searchEngine != null) out.add("搜索引擎（含自定义搜索引擎的名称和模板）")
        if (adBlockEnabled != null) out.add("广告拦截开关")
        if (downloadMode != null) out.add("下载方式")
        if (historyRetention != null) out.add("历史保留期限")
        if (textZoomPercent != null) out.add("网页文字大小")
        if (startupRestore != null) out.add("启动时恢复标签页的开关")
        return out
    }
}

/** A parsed full-backup file: the four lists plus the settings slice. */
data class BackupData(
    val bookmarks: List<Bookmark>,
    val history: List<HistoryItem>,
    val readingList: List<ReadingItem>,
    val speedDials: List<SpeedDial>,
    val settings: BackupSettings
)

/** Serialize a full backup (lists + main settings) to one JSON text. */
fun backupToJson(
    bookmarks: List<Bookmark>,
    history: List<HistoryItem>,
    readingList: List<ReadingItem>,
    speedDials: List<SpeedDial>,
    settings: BackupSettings
): String {
    val bookmarkArr = JSONArray()
    bookmarks.forEach {
        bookmarkArr.put(
            JSONObject().put("title", it.title).put("url", it.url)
                .put("pinned", it.pinned)
        )
    }
    val historyArr = JSONArray()
    history.forEach {
        historyArr.put(
            JSONObject().put("title", it.title).put("url", it.url)
                .put("time", it.time)
        )
    }
    val readingArr = JSONArray()
    readingList.forEach {
        readingArr.put(
            JSONObject().put("title", it.title).put("url", it.url)
                .put("time", it.time).put("read", it.read)
        )
    }
    val dialArr = JSONArray()
    speedDials.forEach {
        dialArr.put(JSONObject().put("title", it.title).put("url", it.url))
    }
    val settingsObj = JSONObject()
    settings.mainEngine?.let { settingsObj.put("mainEngine", it.id) }
    settings.incognitoEngine?.let { settingsObj.put("incognitoEngine", it.id) }
    settings.searchEngine?.let { settingsObj.put("searchEngine", it.id) }
    settings.customSearchName?.let { settingsObj.put("customSearchName", it) }
    settings.customSearchTemplate?.let {
        settingsObj.put("customSearchTemplate", it)
    }
    settings.adBlockEnabled?.let { settingsObj.put("adBlockEnabled", it) }
    settings.downloadMode?.let { settingsObj.put("downloadMode", it.id) }
    settings.historyRetention?.let { settingsObj.put("historyRetention", it.id) }
    settings.textZoomPercent?.let { settingsObj.put("textZoomPercent", it) }
    settings.startupRestore?.let { settingsObj.put("startupRestore", it) }
    return JSONObject()
        .put("format", BACKUP_FORMAT)
        .put("version", BACKUP_FORMAT_VERSION)
        .put("bookmarks", bookmarkArr)
        .put("history", historyArr)
        .put("readingList", readingArr)
        .put("speedDials", dialArr)
        .put("settings", settingsObj)
        .toString(2)
}

/**
 * Parse a full-backup file. Returns null when the text is not JSON or
 * does not carry this app's backup format marker — the caller must
 * then refuse to touch any data. Unknown/missing pieces inside a
 * valid backup simply come back empty/absent.
 */
fun parseBackup(raw: String): BackupData? {
    val obj = try {
        JSONObject(raw.trim())
    } catch (e: Exception) {
        return null
    }
    if (obj.optString("format") != BACKUP_FORMAT) return null
    fun <T> readArr(name: String, parse: (JSONObject) -> T?): List<T> {
        val arr = obj.optJSONArray(name) ?: return emptyList()
        val count = minOf(arr.length(), MAX_IMPORT_ENTRIES)
        return (0 until count).mapNotNull {
            arr.optJSONObject(it)?.let(parse)
        }
    }
    // Inbound data (v0.31.0): a backup file comes from outside the
    // app, so its URLs follow the same http/https-only rule as every
    // other outside link (see isAllowedPageUrl).
    fun webUrl(url: String): String? =
        url.takeIf {
            it.startsWith("http://") || it.startsWith("https://")
        }
    val bookmarks = readArr("bookmarks") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        Bookmark(
            o.optString("title").ifBlank { url }, url, o.optBoolean("pinned")
        )
    }
    val history = readArr("history") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        HistoryItem(
            o.optString("title").ifBlank { url }, url, o.optLong("time")
        )
    }
    val reading = readArr("readingList") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        ReadingItem(
            o.optString("title").ifBlank { url }, url,
            o.optLong("time"), o.optBoolean("read")
        )
    }
    val dials = readArr("speedDials") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        SpeedDial(
            o.optString("title").ifBlank { url }, url
        )
    }
    val s = obj.optJSONObject("settings")
    fun engineOf(key: String): Engine? =
        s?.optString(key)?.takeIf { it.isNotBlank() }?.let { Engine.fromId(it) }
    val settings = if (s == null) {
        BackupSettings()
    } else {
        BackupSettings(
            mainEngine = engineOf("mainEngine"),
            incognitoEngine = engineOf("incognitoEngine"),
            searchEngine = s.optString("searchEngine")
                .takeIf { it.isNotBlank() }?.let { SearchEngine.fromId(it) },
            customSearchName = if (s.has("customSearchName")) {
                s.optString("customSearchName")
            } else {
                null
            },
            customSearchTemplate = if (s.has("customSearchTemplate")) {
                s.optString("customSearchTemplate")
            } else {
                null
            },
            adBlockEnabled = if (s.has("adBlockEnabled")) {
                s.optBoolean("adBlockEnabled")
            } else {
                null
            },
            downloadMode = s.optString("downloadMode")
                .takeIf { it.isNotBlank() }?.let { DownloadMode.fromId(it) },
            historyRetention = s.optString("historyRetention")
                .takeIf { it.isNotBlank() }?.let { HistoryRetention.fromId(it) },
            textZoomPercent = if (s.has("textZoomPercent")) {
                s.optInt("textZoomPercent")
            } else {
                null
            },
            startupRestore = if (s.has("startupRestore")) {
                s.optBoolean("startupRestore")
            } else {
                null
            }
        )
    }
    return BackupData(bookmarks, history, reading, dials, settings)
}

/** Serialize bookmarks for export; the format is a plain JSON object. */
fun bookmarksToJson(list: List<Bookmark>): String {
    val arr = JSONArray()
    list.forEach {
        arr.put(
            JSONObject().put("title", it.title).put("url", it.url).put("pinned", it.pinned)
        )
    }
    return JSONObject()
        .put("app", "TuxunBrowse")
        .put("type", "bookmarks")
        .put("items", arr)
        .toString(2)
}

/** Parse an exported bookmarks file (our object format or a bare array). */
fun bookmarksFromJson(raw: String): List<Bookmark> {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return emptyList()
    val arr = try {
        if (trimmed.startsWith("[")) {
            JSONArray(trimmed)
        } else {
            JSONObject(trimmed).optJSONArray("items") ?: JSONArray()
        }
    } catch (e: Exception) {
        return emptyList()
    }
    val count = minOf(arr.length(), MAX_IMPORT_ENTRIES)
    return (0 until count).mapNotNull {
        val o = arr.optJSONObject(it) ?: return@mapNotNull null
        val url = o.optString("url")
        // Inbound data (v0.31.0): only web URLs may be imported as
        // bookmarks — anything else (file:, javascript:, ...) is
        // dropped here rather than stored and refused at open time.
        if (url.isBlank() ||
            !(url.startsWith("http://") || url.startsWith("https://"))
        ) {
            null
        } else {
            Bookmark(o.optString("title").ifBlank { url }, url, o.optBoolean("pinned"))
        }
    }
}

/** A custom search template is usable only with %s over http(s). */
fun isValidCustomSearchTemplate(template: String): Boolean {
    val t = template.trim()
    return t.contains("%s") &&
        (t.startsWith("http://") || t.startsWith("https://"))
}

/** Build a search URL from a custom template (%s = encoded query). */
private fun customSearchUrl(template: String, query: String): String =
    template.trim().replace(
        "%s", java.net.URLEncoder.encode(query, "UTF-8")
    )

/** Decode the handful of HTML entities bookmark titles commonly carry. */
private fun decodeHtmlEntities(text: String): String {
    var out = text
    // &amp; must be decoded last so "&amp;lt;" stays the literal "&lt;".
    out = out.replace("&lt;", "<")
    out = out.replace("&gt;", ">")
    out = out.replace("&quot;", "\"")
    out = out.replace("&apos;", "'")
    out = out.replace("&#39;", "'")
    out = out.replace("&nbsp;", " ")
    out = out.replace("&amp;", "&")
    return out
}

private val HTML_BOOKMARK_REGEX = Regex(
    "<A\\s[^>]*?HREF\\s*=\\s*\"([^\"]*)\"[^>]*>(.*?)</A>",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
)

/**
 * Parse a standard Netscape bookmark file (the HTML format every
 * desktop browser exports): `<DT><A HREF="...">title</A>` entries.
 * Returns the bookmarks found plus how many link entries were
 * skipped because their address was blank or not http/https.
 * (v0.31.0: file: entries are skipped too — imported bookmarks are
 * inbound data and follow the same http/https-only rule as every
 * other outside link; see isAllowedPageUrl.)
 * De-duplication against existing bookmarks is the caller's job.
 */
fun bookmarksFromHtml(raw: String): Pair<List<Bookmark>, Int> {
    val out = mutableListOf<Bookmark>()
    var skipped = 0
    for (match in HTML_BOOKMARK_REGEX.findAll(raw)) {
        if (out.size >= MAX_IMPORT_ENTRIES) break
        val href = decodeHtmlEntities(match.groupValues[1].trim())
        val scheme = href.substringBefore("://", "").lowercase()
        if (href.isBlank() || (scheme != "http" && scheme != "https")) {
            skipped++
            continue
        }
        val title = decodeHtmlEntities(
            match.groupValues[2].replace(Regex("<[^>]+>"), "").trim()
        )
        out.add(Bookmark(title.ifBlank { href }, href))
    }
    return out to skipped
}

/** Turn whatever the user typed into a URL: full URL, bare host, or a search. */
fun resolveInput(
    input: String,
    searchEngine: SearchEngine = SearchEngine.BAIDU,
    customTemplate: String = ""
): String {
    val text = input.trim()
    if (text.isEmpty()) return HOME_URL
    // Only http(s) URLs pass through as-is (v0.31.0): the old about:
    // and file: pass-throughs are gone — file: pages are refused like
    // every other non-web scheme (see isAllowedPageUrl), and about:
    // pages were never a feature of this browser.
    if (text.startsWith("http://") || text.startsWith("https://")) return text
    val looksLikeHost = !text.contains(" ") &&
        (text.contains(".") || text.startsWith("localhost"))
    return if (looksLikeHost) {
        "https://$text"
    } else if (searchEngine == SearchEngine.CUSTOM) {
        // A missing or invalid custom template falls back to Baidu
        // rather than producing a broken URL.
        if (isValidCustomSearchTemplate(customTemplate)) {
            customSearchUrl(customTemplate, text)
        } else {
            SearchEngine.BAIDU.searchUrl(text)
        }
    } else {
        searchEngine.searchUrl(text)
    }
}

/**
 * True when [resolveInput] would treat the text as a URL to open rather
 * than words to search for. Mirrors resolveInput's own checks exactly, so
 * the address bar can decide whether a "search this" row is needed.
 */
fun isUrlLikeInput(input: String): Boolean {
    val text = input.trim()
    if (text.isEmpty()) return false
    if (text.startsWith("http://") || text.startsWith("https://")) return true
    return !text.contains(" ") &&
        (text.contains(".") || text.startsWith("localhost"))
}

/**
 * Split a version name (or a release tag like "v0.17.0") into its
 * numeric segments. A leading "v"/"V" is dropped, anything after a
 * "-" suffix is ignored, and a segment that is not a plain number
 * counts as 0. Pure function — no Android dependencies.
 */
fun parseVersionNumbers(version: String): List<Int> {
    val core = version.trim()
        .removePrefix("v").removePrefix("V")
        .substringBefore("-").trim()
    if (core.isEmpty()) return emptyList()
    return core.split(".").map { segment ->
        segment.trim().takeWhile { it.isDigit() }.toIntOrNull() ?: 0
    }
}

/**
 * Compare two version names segment by segment, numerically (so
 * 0.9.0 < 0.17.0 — a plain string compare would get that wrong).
 * Missing trailing segments count as 0, so "1.2" == "1.2.0".
 * Returns negative when [a] is older, 0 when equal, positive when
 * [a] is newer. Pure function.
 *
 * Self-check cases (verified when this was written):
 * compareVersionNames("0.9.0", "0.17.0") < 0,
 * compareVersionNames("v0.18.0", "0.18.0") == 0,
 * compareVersionNames("0.17.0", "0.9.0") > 0,
 * compareVersionNames("1.2", "1.2.0") == 0,
 * compareVersionNames("v1.0.1", "1.0.0") > 0.
 */
fun compareVersionNames(a: String, b: String): Int {
    val pa = parseVersionNumbers(a)
    val pb = parseVersionNumbers(b)
    val n = maxOf(pa.size, pb.size)
    for (i in 0 until n) {
        val x = pa.getOrElse(i) { 0 }
        val y = pb.getOrElse(i) { 0 }
        if (x != y) return x.compareTo(y)
    }
    return 0
}

/** True when [tag] looks like a release tag: "v" + numeric segments. */
fun isReleaseTag(tag: String): Boolean {
    val t = tag.trim()
    if (!t.startsWith("v") && !t.startsWith("V")) return false
    val parts = parseVersionNumbers(t)
    return parts.isNotEmpty() &&
        t.removePrefix("v").removePrefix("V").substringBefore("-")
            .split(".").all { seg -> seg.isNotEmpty() && seg.all { it.isDigit() } }
}

/**
 * From (tag_name, html_url) pairs of a repo's releases, pick the one
 * with the highest vX.Y.Z version. Non-release tags are ignored.
 * Returns null when nothing qualifies. Pure function.
 */
fun pickLatestRelease(
    releases: List<Pair<String, String>>
): Pair<String, String>? =
    releases.filter { isReleaseTag(it.first) }
        .maxWithOrNull { x, y -> compareVersionNames(x.first, y.first) }
