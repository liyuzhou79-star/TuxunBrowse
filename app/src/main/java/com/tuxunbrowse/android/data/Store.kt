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

enum class SearchEngine(
    val id: String,
    val label: String,
    val template: String,
) {
    // Suggestion endpoints (v0.50.0, all curl-verified from the build
    // VM, no key/token needed, all answer the same OpenSearch JSON
    // shape [query, [suggestion, ...]]):
    // - Baidu: ie=UTF-8 is load-bearing — without it the endpoint
    //   answers GBK bytes and Chinese suggestions come out garbled.
    // - Bing: api.bing.com/osjson.aspx works; the www.bing.com/AS/
    //   Suggestions endpoint answered empty in testing, not used.
    BAIDU(
        "baidu", "百度", "https://www.baidu.com/s?wd=%s"
    ),
    BING(
        "bing", "必应", "https://www.bing.com/search?q=%s"
    ),
    GOOGLE(
        "google", "Google", "https://www.google.com/search?q=%s"
    ),
    DUCKDUCKGO(
        "duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s"
    );

    fun searchUrl(query: String): String =
        template.format(java.net.URLEncoder.encode(query, "UTF-8"))

    companion object {
        fun fromId(id: String?): SearchEngine =
            entries.firstOrNull { it.id == id } ?: BAIDU
    }
}

data class Bookmark(
    val title: String,
    val url: String,
    val pinned: Boolean = false,
    /**
     * Bookmark folder name (v0.72.0). The empty string means
     * "uncategorized" — there is no separate uncategorized bucket in
     * storage, an absent/blank folder IS the uncategorized state.
     * Every JSON reader below reads this with optString + trim, so
     * files written before folders existed (no "folder" key at all)
     * parse as uncategorized and lose nothing.
     */
    val folder: String = ""
)

/** Folder names are compared trimmed; storage keeps them trimmed. */
fun normalizeBookmarkFolderName(raw: String): String = raw.trim()

/**
 * The folders that actually hold bookmarks, in first-appearance
 * order (v0.72.0). Derived from the bookmark list itself, so it can
 * never disagree with the data.
 */
fun derivedBookmarkFolders(bookmarks: List<Bookmark>): List<String> =
    bookmarks.map { it.folder.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

/**
 * The full folder list the UI offers (v0.72.0): the union of the
 * separately stored folder names (which is what lets an EMPTY
 * folder survive — it holds no bookmarks, so derivation alone
 * would forget it) and the folders derived from the bookmarks.
 * Stored names keep their stored order first; derived-only names
 * follow in first-appearance order.
 */
fun allBookmarkFolders(
    bookmarks: List<Bookmark>,
    storedFolders: List<String>
): List<String> {
    val out = mutableListOf<String>()
    (storedFolders + derivedBookmarkFolders(bookmarks)).forEach { name ->
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && trimmed !in out) out.add(trimmed)
    }
    return out
}

/**
 * Folder filter for the bookmarks page (v0.72.0): a null filter
 * means "all bookmarks", the empty string means "uncategorized
 * only", any other value selects exactly that folder.
 */
fun bookmarksInFolder(
    bookmarks: List<Bookmark>,
    folder: String?
): List<Bookmark> = when (folder) {
    null -> bookmarks
    else -> bookmarks.filter { it.folder.trim() == folder }
}

/** Rename a folder: every bookmark in it moves to the new name. */
fun renameBookmarksFolder(
    bookmarks: List<Bookmark>,
    oldName: String,
    newName: String
): List<Bookmark> = bookmarks.map {
    if (it.folder.trim() == oldName) it.copy(folder = newName.trim()) else it
}

/**
 * Delete a folder: the folder goes away, its bookmarks fall back
 * to uncategorized — deleting a folder never deletes a bookmark.
 */
fun clearBookmarksFolder(
    bookmarks: List<Bookmark>,
    name: String
): List<Bookmark> = bookmarks.map {
    if (it.folder.trim() == name) it.copy(folder = "") else it
}

/**
 * One open tab as the "save all tabs as bookmarks" planner sees it
 * (v0.87.0): plain values, so the classification below is
 * unit-testable without a live BrowserTab.
 */
data class TabBookmarkCandidate(
    val title: String,
    val url: String,
    val incognito: Boolean
)


data class HistoryItem(val title: String, val url: String, val time: Long)

/**
 * Time ranges offered when clearing history (v0.79.0). A range is
 * a window ending NOW: clearing removes every entry whose
 * timestamp falls inside the window and keeps everything older.
 * [windowMillis] is null for ALL, which has no lower bound.
 */
enum class HistoryClearRange(val label: String, val windowMillis: Long?) {
    LAST_HOUR("最近一小时", 3_600_000L),
    LAST_24_HOURS("最近 24 小时", 86_400_000L),
    ALL("全部时间", null)
}

/**
 * The cutoff timestamp for a ranged history clear (v0.79.0): an
 * entry is removed iff its time is >= the returned cutoff, so an
 * entry timestamped exactly AT the cutoff is inside the window
 * and is removed. ALL returns [Long.MIN_VALUE], which every real
 * timestamp satisfies.
 */
fun historyClearCutoff(range: HistoryClearRange, nowMillis: Long): Long =
    range.windowMillis?.let { nowMillis - it } ?: Long.MIN_VALUE

/** Whether an entry timestamp falls inside a clear window. */
fun isInClearRange(timeMillis: Long, cutoff: Long): Boolean = timeMillis >= cutoff

/** How many entries a clear with [cutoff] would remove. */
fun countHistoryInClearRange(items: List<HistoryItem>, cutoff: Long): Int =
    items.count { isInClearRange(it.time, cutoff) }

/** The entries that SURVIVE a clear with [cutoff], order kept. */
fun keptHistoryAfterClear(items: List<HistoryItem>, cutoff: Long): List<HistoryItem> =
    items.filter { !isInClearRange(it.time, cutoff) }

/**
 * The four stored lists the startup effect parses off the main
 * thread (v0.28.0, folders added v0.79.0). Bundled into one value
 * so the background load is a single call.
 */
data class StartupLists(
    val bookmarks: List<Bookmark>,
    val history: List<HistoryItem>,
    val bookmarkFolders: List<String>
)

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

/**
 * One in-app record of a completed Gecko (Firefox) tab download
 * (v0.57.0). Gecko downloads are saved by the app itself straight
 * into the system Downloads folder and never pass through the
 * system DownloadManager, so without this record the downloads
 * screen could not list, open or delete them. Every field is a
 * value the save path provably has at the moment the copy finishes
 * (audited in the v0.57.0 checkpoint): the actual on-disk name
 * (queried back from MediaStore, so a clash-renamed file is
 * recorded under its real name), the response URL, the copied byte
 * count, the response MIME type, and exactly one locator —
 * [contentUri] on Android 10+ (the MediaStore entry the app itself
 * inserted) or [filePath] below that (the app download folder).
 * Only successful saves are recorded: a failed copy deletes its
 * partial artefact, so there is no file a record could point at.
 * Incognito Gecko downloads are never recorded (the save itself
 * still happens; the record is a stored trace, and incognito
 * leaves none). Records are deliberately NOT part of backups: they
 * reference files on this device only.
 *
 * Since v0.85.0 the same record shape also carries WebView web
 * archives (.mhtml, mimeType multipart/related, sourceUrl = the
 * archived page): the downloads screen's in-app section lists,
 * opens and deletes both kinds through this one list, which is
 * why that section is headed 「应用内保存」 rather than naming one
 * engine. The incognito rule is the same for archives.
 */
data class GeckoDownloadRecord(
    val fileName: String,
    val sourceUrl: String,
    val sizeBytes: Long,
    val mimeType: String?,
    val contentUri: String?,
    val filePath: String?,
    val time: Long
)

/**
 * Cap on stored Gecko download records. In line with the other
 * list caps (history 500, reading list 200): 200 completed
 * downloads is far beyond what the downloads screen can usefully
 * present, and the oldest records are dropped past it.
 */
const val MAX_GECKO_DOWNLOAD_RECORDS = 200

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

/**
 * Range of the FIRST case-insensitive occurrence of [query] inside
 * [text], for highlighting the matched part of a suggestion row
 * (v0.68.0). The query is trimmed first; a blank query, blank text or
 * no occurrence all return null, in which case the row renders its
 * text unchanged. Pure and side-effect free so it can be unit-tested
 * on the JVM.
 */
fun matchRange(text: String, query: String): IntRange? {
    val q = query.trim()
    if (q.isEmpty() || text.isEmpty()) return null
    val start = text.indexOf(q, ignoreCase = true)
    if (start < 0) return null
    return start until start + q.length
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
fun isAllowedPageUrl(url: String): Boolean {
    if (url == HOME_URL) return true
    // Schemes are case-insensitive (RFC 3986): a link arriving
    // as "HTTP://…" from another app or a QR code is a valid
    // http link, and refusing it would be a false refusal
    // (v0.156.0). Only the scheme prefix is compared, folded.
    val scheme = url.take(8).lowercase()
    return scheme.startsWith("http://") || scheme.startsWith("https://")
}

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

/** Where a URL-type download goes: the system downloader or TuxunDown. */
enum class DownloadMode(val id: String, val label: String) {
    SYSTEM("system", "系统下载"),
    TUXUNDOWN("tuxundown", "TuxunDown 多线程下载");

    companion object {
        fun fromId(id: String?): DownloadMode =
            entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

/**
 * Reader-mode body text size (v0.58.0). Deliberately a separate
 * setting from the web text zoom: text zoom scales every web page in
 * both engines, this only sizes the re-typeset reader page (WebView
 * tabs only). The four steps mirror the text-zoom steps' spirit
 * (small / standard / large / extra large) but are stored as CSS px
 * for the reader stylesheet, under their own key.
 */
enum class ReaderFontSize(val id: String, val label: String, val bodyPx: Int, val headingPx: Int) {
    SMALL("small", "小", 15, 20),
    STANDARD("standard", "标准", 17, 22),
    LARGE("large", "大", 19, 24),
    EXTRA_LARGE("extra_large", "特大", 21, 26);

    companion object {
        fun fromId(id: String?): ReaderFontSize =
            entries.firstOrNull { it.id == id } ?: STANDARD
    }
}

/**
 * Reader-mode page background (v0.58.0). FOLLOW resolves against the
 * app's own theme at the moment reader mode is entered (the theme
 * mode after the three-way setting is resolved, not the raw system
 * value). BEIGE is a warm paper tone for long reading sessions:
 * Material 3 has no beige role, so its value is a warm paper colour
 * at the same lightness as the light scheme's surface, recorded here
 * rather than tuned ad hoc in the injected CSS.
 */
enum class ReaderBackground(val id: String, val label: String) {
    FOLLOW("follow", "跟随应用"),
    LIGHT("light", "浅色"),
    BEIGE("beige", "米色护眼"),
    DARK("dark", "深色");

    companion object {
        fun fromId(id: String?): ReaderBackground =
            entries.firstOrNull { it.id == id } ?: FOLLOW
    }
}

/** Concrete colours the reader stylesheet paints with. */
data class ReaderPalette(val background: String, val text: String, val link: String)

/**
 * Resolve a [ReaderBackground] choice to concrete colours. LIGHT and
 * DARK use the app theme's own Material 3 surface / onSurface /
 * primary values (see ui/Theme.kt: light surface #F7F8FA on #1A1C1E
 * with primary #0B57D0; dark surface #111417 on #E3E7EC with primary
 * #A8C7FA), so the reader page matches the app around it. FOLLOW
 * picks the light or dark pair by [appDark] — the app's resolved
 * darkness at reader-entry time.
 */
fun readerPaletteFor(background: ReaderBackground, appDark: Boolean): ReaderPalette =
    when (background) {
        ReaderBackground.LIGHT ->
            ReaderPalette("#F7F8FA", "#1A1C1E", "#0B57D0")
        ReaderBackground.DARK ->
            ReaderPalette("#111417", "#E3E7EC", "#A8C7FA")
        ReaderBackground.BEIGE ->
            ReaderPalette("#F4ECD8", "#2E2A1F", "#0B57D0")
        ReaderBackground.FOLLOW ->
            if (appDark) {
                ReaderPalette("#111417", "#E3E7EC", "#A8C7FA")
            } else {
                ReaderPalette("#F7F8FA", "#1A1C1E", "#0B57D0")
            }
    }

/**
 * The app's resolved darkness for a [ThemeMode]: LIGHT is never
 * dark, DARK is always dark, SYSTEM reads the device configuration
 * (the same uiMode night mask isSystemInDarkTheme resolves from).
 */
fun isAppDarkTheme(context: Context, mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM ->
        (context.resources.configuration.uiMode and
            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
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

/**
 * The GeckoView version this build ships (v0.74.0). It is the exact
 * version string of the org.mozilla.geckoview dependency in
 * app/build.gradle.kts, named here so the diagnostics card and the
 * build file have one agreed value to point at — when the
 * dependency is bumped, this constant must be bumped with it.
 */
const val GECKOVIEW_VERSION = "132.0.20241110192737"

/**
 * Everything the diagnostics card on the About page shows
 * (v0.74.0). Deliberately ONLY non-private fields: version numbers,
 * counts and a storage total. There is no URL, title or history
 * content field in this type at all, so the assembled text cannot
 * leak browsing content by construction — [diagnosticText] has no
 * parameter through which such data could even reach it.
 */
data class DiagnosticInfo(
    val appVersionName: String,
    val appVersionCode: Long,
    val geckoVersion: String,
    val webViewPackageName: String?,
    val webViewVersionName: String?,
    val androidRelease: String,
    val androidSdk: Int,
    val storageText: String,
    val bookmarkCount: Int,
    val historyCount: Int,
    val totalBlocked: Long
)

/**
 * Assemble the diagnostics card's rows as one plain text
 * (v0.74.0): exactly the lines the card shows, in the same order,
 * so "copy diagnostics" copies what the user just read — nothing
 * more. An unknown system WebView is stated as unknown, never
 * invented.
 */
fun diagnosticText(info: DiagnosticInfo): String {
    val webViewText = if (info.webViewPackageName.isNullOrBlank()) {
        "未知"
    } else {
        val version = info.webViewVersionName
            ?.takeIf { it.isNotBlank() } ?: "未知"
        "${info.webViewPackageName} $version"
    }
    return listOf(
        "TuxunBrowse 诊断信息",
        "应用版本：${info.appVersionName}（版本号 ${info.appVersionCode}）",
        "火狐内核（GeckoView）版本：${info.geckoVersion}",
        "系统 WebView：$webViewText",
        "Android 版本：${info.androidRelease}（API ${info.androidSdk}）",
        "应用存储占用：${info.storageText}",
        "书签：${info.bookmarkCount} 条",
        "历史记录：${info.historyCount} 条",
        "广告拦截累计：${info.totalBlocked} 个"
    ).joinToString("\n")
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

/**
 * Enhanced tracking protection level (v0.92.0), modelled on
 * Firefox's standard / strict choice. Both levels share the base
 * block list and the engines' built-in anti-tracking; STRICT adds
 * the strict host list on the WebView side and, on the Gecko side,
 * the kernel's STRICT category set (which adds content trackers,
 * cryptominers and fingerprinters) plus fingerprinting protection.
 */
enum class TrackingProtectionLevel(val id: String, val label: String) {
    STANDARD("standard", "标准"),
    STRICT("strict", "严格");

    companion object {
        fun fromId(id: String?): TrackingProtectionLevel =
            entries.firstOrNull { it.id == id } ?: STANDARD
    }
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

/**
 * Cap for a pinned home-screen shortcut's short label (v0.86.0).
 * The platform itself accepts longer labels, but launchers render
 * one or two short lines under the icon and ellipsize the rest
 * unpredictably; capping here keeps the label the app chose, not
 * the launcher's cut. 30 characters fits a typical page title's
 * meaningful head in both Chinese and Latin scripts.
 */
const val PIN_SHORTCUT_LABEL_LIMIT = 30

/**
 * Stable id for the pinned shortcut of [url] (v0.86.0): the same
 * page always yields the same id (so re-adding a page updates its
 * one shortcut instead of piling up duplicates), and two different
 * pages practically never collide — the id is "web-" plus the
 * first 16 hex digits of the URL's SHA-256.
 */
fun pinnedShortcutId(url: String): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256")
        .digest(url.toByteArray(Charsets.UTF_8))
    val hex = digest.joinToString("") { "%02x".format(it) }
    return "web-" + hex.take(16)
}

/**
 * The short label a pinned shortcut for a page shows (v0.86.0):
 * the page title with whitespace collapsed, capped at
 * [PIN_SHORTCUT_LABEL_LIMIT] characters. A page with no usable
 * title falls back to its host, and a URL with no parseable host
 * falls back to the URL itself (still capped) — the label is
 * never blank, because a shortcut with an empty label is useless
 * on a home screen.
 */
fun pinnedShortcutLabel(title: String, url: String): String {
    val cleanTitle = title.trim().replace(Regex("\\s+"), " ")
    val base = when {
        cleanTitle.isNotEmpty() -> cleanTitle
        else -> hostOf(url) ?: url.trim()
    }
    return if (base.length > PIN_SHORTCUT_LABEL_LIMIT) {
        base.substring(0, PIN_SHORTCUT_LABEL_LIMIT)
    } else {
        base
    }
}

/**
 * The letter drawn on a pinned shortcut's generated icon
 * (v0.86.0): the first character of the shortcut's label — the
 * same "title's first letter" device the home page's speed-dial
 * tiles use, so the two shortcut surfaces look related.
 */
fun pinnedShortcutInitial(title: String, url: String): String =
    pinnedShortcutLabel(title, url).firstOrNull()?.toString() ?: "?"

/**
 * What the address bar shows for [url] while it is NOT being edited
 * (v0.76.0): just the host for web pages — no scheme, no path, no
 * query — because the full URL is noise at a glance and the domain
 * is the part that tells the user where they are. A non-default
 * port stays (it is part of "where"), the default ports (80 for
 * http, 443 for https) are dropped. Internal `tuxun://` addresses
 * return "" — the home page's bar is empty with its placeholder
 * showing, exactly as before this display rule existed, and no
 * internal address is ever dressed up as a domain. Anything that
 * is not a parseable http(s) URL comes back unchanged, so an odd
 * address is shown as-is rather than mangled. Tapping the bar
 * switches back to the full URL for editing (BrowserScreen).
 */
fun displayHostFor(url: String): String {
    val text = url.trim()
    if (text.isEmpty()) return ""
    if (text.startsWith("tuxun://")) return ""
    val uri = try {
        java.net.URI(text)
    } catch (e: Exception) {
        return text
    }
    val scheme = uri.scheme?.lowercase()
    if (scheme != "http" && scheme != "https") return text
    val defaultPort = if (scheme == "http") 80 else 443
    val host = uri.host?.lowercase()
    if (host == null) {
        // URI could not parse a server-style host (some literal
        // forms): fall back to the raw authority, minus any
        // userinfo and minus a default port.
        val authority = uri.authority
            ?.substringAfterLast("@")?.lowercase() ?: return text
        return authority.removeSuffix(":$defaultPort")
    }
    // An IPv6 host may come back already bracketed or bare,
    // depending on the runtime; normalize to exactly one pair.
    val bareHost = host.removeSurrounding("[", "]")
    val displayHost = if (bareHost.contains(":")) "[$bareHost]" else bareHost
    val port = uri.port
    return if (port == -1 || port == defaultPort) {
        displayHost
    } else {
        "$displayHost:$port"
    }
}

private val IPV4_HOST_REGEX = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")

/** Suffixes treated as intranet / local-network names (v0.77.0). */
private val LOCAL_HOST_SUFFIXES = listOf(
    ".local", ".lan", ".home", ".internal", ".intranet", ".corp"
)

/**
 * HTTPS-first upgrade (v0.77.0): when [enabled] and [url] is an
 * http:// page address on a public host, return the same address
 * with the scheme swapped to https:// — port, path, query and
 * fragment are preserved byte for byte. Everything else comes back
 * unchanged:
 * - the switch is off, or the URL is not http:// at all;
 * - the host is an IP literal (IPv4 or IPv6) — intranet devices and
 *   direct-IP services routinely have no certificate;
 * - the host is localhost, a single-label name, or carries a
 *   local-network suffix (.local/.lan/…) — same reasoning;
 * - the host cannot be parsed at all — never mangle an address the
 *   app does not understand.
 * Callers apply this ONLY to the four inbound paths (typed address
 * bar input, home search opens, the share sheet's "open" choice and
 * external VIEW links); navigation generated inside a page never
 * passes through it.
 */
fun upgradeToHttps(url: String, enabled: Boolean): String {
    if (!enabled) return url
    if (!url.startsWith("http://")) return url
    val host = hostOf(url) ?: return url
    if (IPV4_HOST_REGEX.matches(host) || host.contains(':')) return url
    if (host == "localhost" || !host.contains('.')) return url
    if (LOCAL_HOST_SUFFIXES.any { host.endsWith(it) }) return url
    return "https://" + url.substring("http://".length)
}

/**
 * The one-off escape hatch paired with [upgradeToHttps] (v0.77.0):
 * the same address over plain http, for the error page's "open
 * over http instead" button. Applied to that single load only —
 * nothing is remembered and the HTTPS-first switch stays as it is.
 */
fun downgradeToHttp(url: String): String =
    if (url.startsWith("https://")) {
        "http://" + url.substring("https://".length)
    } else {
        url
    }

/**
 * The project's one host-matching rule: [host] belongs to [site]
 * when it is the site itself or a subdomain of it. Every per-site
 * feature (compat memory, desktop-mode memory, per-site data
 * clearing, per-site history deletion) goes through this so
 * "this site" means the same thing everywhere.
 */
fun hostMatchesSite(host: String?, site: String): Boolean {
    if (host.isNullOrBlank() || site.isBlank()) return false
    return host == site || host.endsWith(".$site")
}

/** True when [host] is a remembered compat site or a subdomain of one. */
fun isCompatHost(host: String?, sites: Set<String>): Boolean {
    if (host.isNullOrBlank()) return false
    return sites.any { site -> hostMatchesSite(host, site) }
}

/** True when [host] is a remembered desktop-mode site or a subdomain of one. */
fun isDesktopHost(host: String?, sites: Set<String>): Boolean {
    if (host.isNullOrBlank()) return false
    return sites.any { site -> hostMatchesSite(host, site) }
}

private val SHARED_URL_REGEX =
    Regex("https?://\\S+", RegexOption.IGNORE_CASE)

/**
 * Trailing characters that share-sheet text commonly glues onto the
 * end of a URL (sentence punctuation, closing brackets and quotes,
 * in both ASCII and CJK forms). They are trimmed off the end of an
 * extracted URL; a real URL path never ends in these in practice,
 * and keeping them would break the link.
 */
private const val SHARED_URL_TRAILING = ".,;:!?)]}>，。；：！？、）】」』\"'"

/**
 * The first http(s) URL inside an arbitrary shared text (v0.73.0),
 * or null when the text carries none. Other apps share a page as
 * "title + blurb + URL" more often than as a bare URL, so the share
 * dialog works off this extraction: when it finds a URL, "open"
 * goes straight to it and "save as bookmark" is offered; when
 * it finds none, the whole text is treated as words to search for.
 * Only the FIRST URL is taken — a share is about one page.
 */
fun extractFirstWebUrl(text: String?): String? {
    if (text.isNullOrBlank()) return null
    val raw = SHARED_URL_REGEX.find(text)?.value ?: return null
    val trimmed = raw.trimEnd { it in SHARED_URL_TRAILING }
    return trimmed.takeIf {
        val scheme = it.take(8).lowercase()
        scheme.startsWith("http://") || scheme.startsWith("https://")
    }?.takeIf { it.length > "https://".length }
}

/**
 * Whether the Gecko runtime should be warmed up in the background
 * right after the browser UI first renders (v0.71.0). The runtime
 * is worth creating early only when some tab can actually use it:
 * the main engine or the incognito engine being Firefox. When both
 * are the system WebView the runtime is never created at all, so
 * preheating would only waste memory.
 */
fun shouldPreheatGecko(
    mainEngine: Engine,
    incognitoEngine: Engine
): Boolean =
    mainEngine == Engine.GECKO || incognitoEngine == Engine.GECKO

/**
 * Where a restored history entry belongs in the newest-first
 * history list (v0.71.0): the index of the first entry that is not
 * newer than [item], or the end of the list when every entry is
 * newer. Used by the history page's swipe-to-delete undo, so the
 * entry goes back by its own timestamp — never by a remembered
 * position, which the search/site filters could have shifted.
 */
fun historyRestoreIndex(list: List<HistoryItem>, item: HistoryItem): Int {
    val idx = list.indexOfFirst { it.time <= item.time }
    return if (idx < 0) list.size else idx
}

/**
 * Parse one stored JSON array of objects, element by element
 * (v0.83.0 corruption audit). The contract every list loader in
 * [Store] now shares:
 * - a null/blank payload, or one that is not a JSON array at all
 *   (truncated write, wrong type), parses to an empty list — the
 *   damage stays inside this one key;
 * - an element that is not a JSON object, or whose own parse
 *   throws, is SKIPPED — the surviving elements still parse, so
 *   one bad row can no longer empty the whole list;
 * - field reads inside [parse] use the opt* family, so a field
 *   stored with the wrong type falls back per-field instead of
 *   throwing the row (or the list) away.
 * Pure (org.json only) so the corruption samples are unit-tested.
 */
fun <T> parseJsonObjectList(raw: String?, parse: (JSONObject) -> T): List<T> {
    if (raw.isNullOrBlank()) return emptyList()
    val arr = try {
        JSONArray(raw)
    } catch (e: Exception) {
        return emptyList()
    }
    val out = mutableListOf<T>()
    for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        try {
            out.add(parse(obj))
        } catch (e: Exception) {
            // One corrupt element: skip it, keep the rest.
        }
    }
    return out
}

/** Stored bookmarks (see [parseJsonObjectList] for the contract). */
fun parseBookmarksJson(raw: String?): List<Bookmark> =
    parseJsonObjectList(raw) {
        Bookmark(
            it.optString("title"), it.optString("url"),
            it.optBoolean("pinned"), it.optString("folder").trim()
        )
    }

/** Stored history entries. */
fun parseHistoryJson(raw: String?): List<HistoryItem> =
    parseJsonObjectList(raw) {
        HistoryItem(it.optString("title"), it.optString("url"), it.optLong("time"))
    }

/** Stored reading-list entries. */
fun parseReadingListJson(raw: String?): List<ReadingItem> =
    parseJsonObjectList(raw) {
        ReadingItem(
            it.optString("title"),
            it.optString("url"),
            it.optLong("time"),
            it.optBoolean("read")
        )
    }

/** Stored home-page speed dials. */
fun parseSpeedDialsJson(raw: String?): List<SpeedDial> =
    parseJsonObjectList(raw) {
        SpeedDial(it.optString("title"), it.optString("url"))
    }

/** Stored Gecko download records. */
fun parseGeckoDownloadsJson(raw: String?): List<GeckoDownloadRecord> =
    parseJsonObjectList(raw) {
        GeckoDownloadRecord(
            it.optString("name"),
            it.optString("url"),
            it.optLong("size"),
            it.optString("mime").ifBlank { null },
            it.optString("uri").ifBlank { null },
            it.optString("path").ifBlank { null },
            it.optLong("time")
        )
    }

/**
 * One of the per-site string lists (compat / desktop / reader).
 * Only genuine string elements count: a number or object element
 * is corruption, skipped rather than coerced into a fake host
 * name. Order is kept; de-duplication is the caller's business
 * (the loaders pour the result into a set).
 */
fun parseSiteListJson(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()
    val arr = try {
        JSONArray(raw)
    } catch (e: Exception) {
        return emptyList()
    }
    val out = mutableListOf<String>()
    for (i in 0 until arr.length()) {
        val value = arr.opt(i) as? String ?: continue
        out.add(value)
    }
    return out
}

/** Stored bookmark folder names: trimmed, non-blank, de-duplicated. */
fun parseBookmarkFoldersJson(raw: String?): List<String> =
    parseSiteListJson(raw)
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

/**
 * The saved-tab archive: one object with a "tabs" array and an
 * "active" index. A payload that is not a JSON object, or whose
 * "tabs" is not an array, falls back to (empty, 0); a bad element
 * inside the array is skipped like everywhere else, and tabs with
 * a blank URL are dropped (the pre-audit rule, kept).
 */
fun parseSavedTabsJson(raw: String?): Pair<List<SavedTab>, Int> {
    if (raw.isNullOrBlank()) return emptyList<SavedTab>() to 0
    val obj = try {
        JSONObject(raw)
    } catch (e: Exception) {
        return emptyList<SavedTab>() to 0
    }
    val tabsRaw = obj.optJSONArray("tabs")?.toString()
    val list = parseJsonObjectList(tabsRaw) {
        SavedTab(
            Engine.fromId(it.optString("e")),
            it.optString("u"),
            it.optBoolean("d")
        )
    }.filter { it.url.isNotBlank() }
    return list to obj.optInt("active", 0)
}

/**
 * The one JSON object a bookmark is written as (v0.109.0 field
 * audit). Every writer — the local store, the full backup and the
 * standalone bookmark export — builds its bookmark entries through
 * this single function, and every reader reads the same four keys
 * ("title" / "url" / "pinned" / "folder"), so the write side and
 * the read side cannot drift apart field by field. The folder is
 * stored trimmed, matching every reader's trim.
 */
fun bookmarkJsonObject(bookmark: Bookmark): JSONObject =
    JSONObject().put("title", bookmark.title).put("url", bookmark.url)
        .put("pinned", bookmark.pinned).put("folder", bookmark.folder.trim())

/** The one JSON object a history entry is written as. See [bookmarkJsonObject]. */
fun historyJsonObject(item: HistoryItem): JSONObject =
    JSONObject().put("title", item.title).put("url", item.url)
        .put("time", item.time)

/**
 * The one JSON object a Gecko download / web-archive record is
 * written as. See [bookmarkJsonObject]. Nullable fields are
 * written as "" and read back as null (the readers' ifBlank rule),
 * so null is the only "absent" value a record can carry.
 */
fun geckoDownloadJsonObject(record: GeckoDownloadRecord): JSONObject =
    JSONObject()
        .put("name", record.fileName)
        .put("url", record.sourceUrl)
        .put("size", record.sizeBytes)
        .put("mime", record.mimeType ?: "")
        .put("uri", record.contentUri ?: "")
        .put("path", record.filePath ?: "")
        .put("time", record.time)

/** The saved-tab archive payload: a "tabs" array plus the "active" index. */
fun savedTabsJson(list: List<SavedTab>, activeIndex: Int): String {
    val arr = JSONArray()
    list.forEach {
        arr.put(
            JSONObject()
                .put("e", it.engine.id)
                .put("u", it.url)
                .put("d", it.desktopMode)
        )
    }
    return JSONObject().put("tabs", arr).put("active", activeIndex).toString()
}

/**
 * Where the saved active tab lands in the RESTORED tab list
 * (v0.109.0 field audit). The stored "active" index counts
 * positions in the saved list, but the restore pass first drops
 * the entries [keep] rejects (non-web URLs), so applying the raw
 * index to the filtered list would activate the wrong tab
 * whenever an entry before the active one was dropped. This maps
 * the index through the same filter: the position of the saved
 * active entry among the kept ones. When the active entry itself
 * was dropped, the last kept entry before it is chosen (or the
 * first kept one when there is none before it). With nothing
 * dropped this is exactly the clamped saved index, so archives
 * that restore whole behave exactly as before.
 */
fun restoredActiveIndex(
    savedTabs: List<SavedTab>,
    savedActive: Int,
    keep: (SavedTab) -> Boolean
): Int {
    if (savedTabs.isEmpty()) return 0
    val target = savedActive.coerceIn(0, savedTabs.lastIndex)
    var keptBefore = 0
    for (i in 0 until target) {
        if (keep(savedTabs[i])) keptBefore++
    }
    return if (keep(savedTabs[target])) {
        keptBefore
    } else {
        maxOf(0, keptBefore - 1)
    }
}

/** Local persistence: engine choices, search engine, compat sites, bookmarks, history. */
class Store(private val context: Context) {
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

    /** URL template of the custom search engine; must contain %s. */

    var adBlockEnabled: Boolean
        get() = safeBoolean(KEY_AD_BLOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_AD_BLOCK, value).apply()

    /**
     * HTTPS first (v0.77.0): open http:// addresses from the four
     * inbound paths as https:// instead. Default ON. See
     * [upgradeToHttps] for the exact rule (IP literals and local
     * names are never upgraded). Incognito tabs follow the same
     * rule — upgrading writes nothing anywhere.
     */
    var httpsFirstEnabled: Boolean
        get() = safeBoolean(KEY_HTTPS_FIRST, true)
        set(value) = prefs.edit().putBoolean(KEY_HTTPS_FIRST, value).apply()

    /**
     * The built-in page-guard script (v0.91.0): a user-supplied
     * all-in-one page script (ad purge, redirect guard, privacy
     * shield, speed-up, reading unlock) injected into WebView tabs
     * at page start. Default ON; this switch is its only control —
     * the script's own floating UI is suppressed. WebView tabs
     * only: GeckoView has no equivalent injection path, so Gecko
     * tabs keep relying on the native block list and tracking
     * protection. Incognito WebView tabs are never injected: the
     * script keeps permanent per-site statistics in page storage,
     * which contradicts the incognito contract.
     */
    var guardScriptEnabled: Boolean
        get() = safeBoolean(KEY_GUARD_SCRIPT, true)
        set(value) = prefs.edit().putBoolean(KEY_GUARD_SCRIPT, value).apply()

    /**
     * Enhanced tracking protection level (v0.92.0). Default
     * STANDARD, which is exactly the pre-v0.92.0 behaviour (base
     * block list + Gecko anti-tracking DEFAULT). STRICT widens
     * both engines — see [TrackingProtectionLevel].
     */
    var trackingProtectionLevel: TrackingProtectionLevel
        get() = TrackingProtectionLevel.fromId(safeString(KEY_TP_LEVEL, null))
        set(value) = prefs.edit().putString(KEY_TP_LEVEL, value.id).apply()

    /**
     * Do Not Track (v0.92.0), default OFF. WebView tabs only:
     * app-initiated loads carry a `DNT: 1` header and pages see
     * `navigator.doNotTrack == "1"` through the privacy-signals
     * script. GeckoView 132 exposes no DNT API at all (verified
     * against the AAR), so Gecko tabs cannot send it — the
     * settings copy says so instead of pretending otherwise.
     */
    var doNotTrackEnabled: Boolean
        get() = safeBoolean(KEY_DNT, false)
        set(value) = prefs.edit().putBoolean(KEY_DNT, value).apply()

    /**
     * Global Privacy Control (v0.92.0), default OFF: tell sites
     * not to sell or share personal data. Both engines have a real
     * path — Gecko has a native runtime setting (it sends the
     * `Sec-GPC: 1` header and sets `navigator.globalPrivacyControl`
     * itself); WebView tabs get the header on app-initiated loads
     * plus the same JS property via the privacy-signals script.
     */
    var globalPrivacyControlEnabled: Boolean
        get() = safeBoolean(KEY_GPC, false)
        set(value) = prefs.edit().putBoolean(KEY_GPC, value).apply()

    /**
     * Disable WebRTC (v0.92.0), default OFF: stops pages from
     * opening peer connections, which can reveal the real IP
     * address behind a VPN. WebView tabs only, via the
     * privacy-signals script replacing `RTCPeerConnection` with a
     * stub that always fails. GeckoView 132 exposes no WebRTC
     * switch and no script-injection path, so Gecko tabs cannot
     * honour it — the settings copy says so.
     */
    var webRtcDisabled: Boolean
        get() = safeBoolean(KEY_WEBRTC_OFF, false)
        set(value) = prefs.edit().putBoolean(KEY_WEBRTC_OFF, value).apply()

    /**
     * Fingerprint mode's chosen region id (v0.144.0), one of
     * FingerprintProfiles' ids ("US"/"JP"/"SG"/"TW"); unknown or
     * unset resolves to the first profile. Deliberately NOT part
     * of the backup slice in this version — the backup key set is
     * pinned by audit tests and the profile is two taps to re-pick.
     */
    var fingerprintRegionId: String
        get() = safeString(KEY_FP_REGION, null) ?: ""
        set(value) = prefs.edit().putString(KEY_FP_REGION, value).apply()

    /** Fingerprint mode's chosen IANA time zone id inside the region. */
    var fingerprintTimezoneId: String
        get() = safeString(KEY_FP_TIMEZONE, null) ?: ""
        set(value) = prefs.edit().putString(KEY_FP_TIMEZONE, value).apply()

    /** Fingerprint mode's chosen device id inside the region (v0.146.0). */
    var fingerprintDeviceId: String
        get() = safeString(KEY_FP_DEVICE, null) ?: ""
        set(value) = prefs.edit().putString(KEY_FP_DEVICE, value).apply()

    /** Fingerprint mode's chosen language inside the region (v0.146.0). */
    var fingerprintLanguage: String
        get() = safeString(KEY_FP_LANGUAGE, null) ?: ""
        set(value) = prefs.edit().putString(KEY_FP_LANGUAGE, value).apply()

    /**
     * The host WebView's own Chrome version (v0.176.0), read
     * once per process — the system WebView's version cannot
     * change under a running app. Null when unreadable; the
     * fingerprint claim then keeps the table's fallback.
     */
    private val hostChrome:
        com.tuxunbrowse.android.engine.FingerprintProfiles.HostChrome?
            by lazy {
                com.tuxunbrowse.android.engine.FingerprintProfiles
                    .parseHostChrome(
                        try {
                            android.webkit.WebSettings
                                .getDefaultUserAgent(context)
                        } catch (t: Throwable) {
                            null
                        }
                    )
            }

    /** The live fingerprint combination the mode runs with. */
    fun resolvedFingerprint():
        com.tuxunbrowse.android.engine.ResolvedFingerprint =
        com.tuxunbrowse.android.engine.FingerprintProfiles
            .withHostChrome(
                com.tuxunbrowse.android.engine.FingerprintProfiles
                    .resolve(
                        fingerprintRegionId, fingerprintTimezoneId,
                        fingerprintDeviceId, fingerprintLanguage
                    ),
                hostChrome
            )

    /**
     * The user's saved fingerprint combinations (v0.146.0,
     * 「我的档案」): four ids plus a display label each, JSON in
     * one pref. Applying one goes through the same bounded
     * resolve as the picker, so a stale entry (a device a later
     * version dropped) degrades to the region default instead of
     * producing an impossible mix. Same not-in-backup rationale
     * as the fp_* keys above.
     */
    fun customFingerprints():
        List<com.tuxunbrowse.android.engine.CustomFingerprint> =
        parseJsonObjectList(safeString(KEY_FP_CUSTOM, null)) { obj ->
            com.tuxunbrowse.android.engine.CustomFingerprint(
                regionId = obj.optString("r"),
                deviceId = obj.optString("d"),
                timezoneId = obj.optString("t"),
                language = obj.optString("l"),
                label = obj.optString("label")
            )
        }

    /** Save [entry] unless an identical combination is saved. */
    fun addCustomFingerprint(
        entry: com.tuxunbrowse.android.engine.CustomFingerprint
    ) {
        val current = customFingerprints()
        if (current.any {
                it.regionId == entry.regionId &&
                    it.deviceId == entry.deviceId &&
                    it.timezoneId == entry.timezoneId &&
                    it.language == entry.language
            }
        ) {
            return
        }
        val arr = JSONArray()
        for (item in current + entry) {
            arr.put(
                JSONObject()
                    .put("r", item.regionId)
                    .put("d", item.deviceId)
                    .put("t", item.timezoneId)
                    .put("l", item.language)
                    .put("label", item.label)
            )
        }
        prefs.edit().putString(KEY_FP_CUSTOM, arr.toString()).apply()
    }

    /** Drop the saved combination with the same four ids. */
    fun removeCustomFingerprint(
        entry: com.tuxunbrowse.android.engine.CustomFingerprint
    ) {
        val kept = customFingerprints().filterNot {
            it.regionId == entry.regionId &&
                it.deviceId == entry.deviceId &&
                it.timezoneId == entry.timezoneId &&
                it.language == entry.language
        }
        val arr = JSONArray()
        for (item in kept) {
            arr.put(
                JSONObject()
                    .put("r", item.regionId)
                    .put("d", item.deviceId)
                    .put("t", item.timezoneId)
                    .put("l", item.language)
                    .put("label", item.label)
            )
        }
        prefs.edit().putString(KEY_FP_CUSTOM, arr.toString()).apply()
    }

    /** Web page text size in percent: 85 / 100 / 115 / 130. */
    var textZoomPercent: Int
        get() = safeInt(KEY_TEXT_ZOOM, 100)
        set(value) = prefs.edit().putInt(KEY_TEXT_ZOOM, value).apply()

    /**
     * Reader-mode body text size (v0.58.0). Own key on purpose: the
     * web text zoom above scales all pages in both engines, this one
     * only sizes the reader page and must not move when text zoom
     * changes (or vice versa).
     */
    var readerFontSize: ReaderFontSize
        get() = ReaderFontSize.fromId(safeString(KEY_READER_FONT_SIZE, null))
        set(value) = prefs.edit().putString(KEY_READER_FONT_SIZE, value.id).apply()

    /** Reader-mode background choice (v0.58.0); see [ReaderBackground]. */
    var readerBackground: ReaderBackground
        get() = ReaderBackground.fromId(safeString(KEY_READER_BACKGROUND, null))
        set(value) = prefs.edit().putString(KEY_READER_BACKGROUND, value.id).apply()

    /** Ask web pages to render dark (WebView algorithmic darkening). */
    var webDarkMode: Boolean
        get() = safeBoolean(KEY_WEB_DARK, false)
        set(value) = prefs.edit().putBoolean(KEY_WEB_DARK, value).apply()

    /** Restore the previous session's tabs on launch (vs always home). */
    var startupRestore: Boolean
        get() = safeBoolean(KEY_STARTUP_RESTORE, true)
        set(value) = prefs.edit().putBoolean(KEY_STARTUP_RESTORE, value).apply()

    /**
     * Set while an incognito WebView tab exists (v0.128.0 process-
     * death audit). The close-time cookie-jar wipe only runs when
     * the last incognito WebView tab goes away through a normal
     * path; if the process dies first, the jar — already flushed
     * to disk on pause — would keep the incognito session's
     * cookies for the next launch. This flag lets the next start
     * finish that wipe before anything reads the jar. Cleared by
     * the wipe itself, on both the close-time and startup paths.
     */
    var webViewIncognitoDirty: Boolean
        get() = safeBoolean(KEY_WV_INCOGNITO_DIRTY, false)
        set(value) = prefs.edit()
            .putBoolean(KEY_WV_INCOGNITO_DIRTY, value).apply()

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
    //
    // Threading (v0.139.0 network audit): every access to the three
    // fields below is guarded by [blockedCounterLock]. The count
    // path itself is posted to the main thread today, but the
    // restore path runs on a background IO thread and the reset /
    // flush paths run wherever the UI or lifecycle calls them —
    // the old unguarded read-modify-write in noteBlocked could
    // lose hits (or resurrect a restored/reset value) whenever two
    // of those paths overlapped. The lock is uncontended in the
    // common case and held only for field updates plus an async
    // preferences apply, so the per-request cost stays negligible.
    private val blockedCounterLock = Any()
    private var totalBlockedMemory: Long = -1L
    private var blockedPendingFlush: Int = 0
    private var blockedLastFlushAt: Long = 0L

    /** Lifetime total of blocked requests (WebView tabs only). */
    val totalBlocked: Long
        get() = synchronized(blockedCounterLock) {
            if (totalBlockedMemory < 0) {
                totalBlockedMemory = safeLong(KEY_TOTAL_BLOCKED, 0L)
            }
            totalBlockedMemory
        }

    /** Count one more blocked request (same call path as the per-page count). */
    fun noteBlocked() {
        synchronized(blockedCounterLock) {
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
    }

    /**
     * Restore the blocked counter from a backup: the lifetime total
     * is replaced by the backed-up value (a restore means "bring
     * back what was backed up", the same overwrite treatment the
     * settings slice gets — the lists merge, settings and
     * statistics overwrite). The memory cache is updated so readers
     * see the restored value at once.
     */
    fun restoreBlockedStats(total: Long) {
        synchronized(blockedCounterLock) {
            val cleanTotal = total.coerceAtLeast(0L)
            totalBlockedMemory = cleanTotal
            blockedPendingFlush = 0
            blockedLastFlushAt = System.currentTimeMillis()
            prefs.edit().putLong(KEY_TOTAL_BLOCKED, cleanTotal).apply()
        }
    }

    /**
     * Write out any blocked hits still sitting in the memory
     * buffer (v0.128.0 process-death audit). Without this, a
     * process death could lose up to BLOCKED_FLUSH_EVERY - 1
     * unflushed hits; the app calls this when it goes to the
     * background, which is where process death normally follows.
     */
    fun flushBlockedCounter() {
        synchronized(blockedCounterLock) {
            if (blockedPendingFlush <= 0) return
            blockedPendingFlush = 0
            blockedLastFlushAt = System.currentTimeMillis()
            prefs.edit().putLong(KEY_TOTAL_BLOCKED, totalBlocked).apply()
        }
    }

    /** Reset the lifetime blocked counter, effective at once. */
    fun resetTotalBlocked() {
        synchronized(blockedCounterLock) {
            totalBlockedMemory = 0L
            blockedPendingFlush = 0
            blockedLastFlushAt = System.currentTimeMillis()
            prefs.edit().putLong(KEY_TOTAL_BLOCKED, 0L).apply()
        }
    }

    fun loadCompatSites(): MutableSet<String> =
        parseSiteListJson(safeString(KEY_COMPAT_SITES, null)).toMutableSet()

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

    // Desktop-mode sites (v0.56.0): hosts the user switched to the
    // desktop site view on. Stored exactly like the compat list —
    // a JSON array of plain host names — and matched with the same
    // shared hostMatchesSite rule.
    fun loadDesktopSites(): MutableSet<String> =
        parseSiteListJson(safeString(KEY_DESKTOP_SITES, null)).toMutableSet()

    fun addDesktopSite(domain: String) {
        val sites = loadDesktopSites()
        if (sites.add(domain)) saveDesktopSites(sites)
    }

    fun removeDesktopSite(domain: String) {
        val sites = loadDesktopSites()
        if (sites.remove(domain)) saveDesktopSites(sites)
    }

    private fun saveDesktopSites(sites: Set<String>) {
        val arr = JSONArray()
        sites.forEach { arr.put(it) }
        prefs.edit().putString(KEY_DESKTOP_SITES, arr.toString()).apply()
    }

    // folder (v0.72.0): optString yields "" when the key is
    // absent, so archives written before folders existed load
    // as uncategorized — no migration, no data loss.
    fun loadBookmarks(): MutableList<Bookmark> =
        parseBookmarksJson(safeString(KEY_BOOKMARKS, null)).toMutableList()

    fun saveBookmarks(list: List<Bookmark>) = writeList(KEY_BOOKMARKS, list) {
        bookmarkJsonObject(it)
    }

    /**
     * One-time migration for the two homepage lists removed in
     * v0.91.0 (reading list and speed dials): their entries become
     * ordinary bookmarks — speed dials pinned (they were the
     * homepage shortcuts), reading-list entries in a folder named
     * 阅读列表 — and the two legacy keys are deleted, so this runs
     * at most once. URLs already bookmarked are not duplicated.
     * Called from the app's startup pass, off the main thread.
     *
     * Returns true only when legacy entries were actually merged
     * (the caller then reloads the bookmark list). Startup audit
     * (v0.95.0): once the migration has run, both legacy keys are
     * gone, and that settled state is now detected by the two raw
     * reads alone — no JSON parsing, and no remove/apply round
     * trip on every cold start the way the old shape did (it also
     * made the caller re-parse the whole bookmark list each time
     * for a merge that could not have happened).
     */
    fun migrateRemovedListsToBookmarks(): Boolean {
        val readingRaw = safeString(KEY_READING_LIST, null)
        val dialsRaw = safeString(KEY_SPEED_DIALS, null)
        if (readingRaw == null && dialsRaw == null) return false
        val legacyReading = parseReadingListJson(readingRaw)
        val legacyDials = parseSpeedDialsJson(dialsRaw)
        if (legacyReading.isEmpty() && legacyDials.isEmpty()) {
            prefs.edit()
                .remove(KEY_READING_LIST).remove(KEY_SPEED_DIALS)
                .apply()
            return false
        }
        val bookmarks = loadBookmarks()
        val known = bookmarks.map { it.url }.toMutableSet()
        legacyDials.forEach { dial ->
            if (known.add(dial.url)) {
                bookmarks.add(
                    Bookmark(dial.title, dial.url, pinned = true)
                )
            }
        }
        legacyReading.forEach { item ->
            if (known.add(item.url)) {
                bookmarks.add(
                    Bookmark(item.title, item.url, folder = "阅读列表")
                )
            }
        }
        saveBookmarks(bookmarks)
        prefs.edit()
            .remove(KEY_READING_LIST).remove(KEY_SPEED_DIALS)
            .apply()
        return true
    }

    /**
     * The stored folder-name list (v0.72.0). This list exists for
     * exactly one reason: an empty folder holds no bookmarks, so it
     * cannot be derived from them — without a stored list it would
     * vanish the moment its last bookmark leaves. The list the UI
     * shows is the UNION of this and the derived names (see
     * allBookmarkFolders), so the two can never disagree about a
     * folder that holds bookmarks.
     */
    fun loadBookmarkFolders(): List<String> =
        parseBookmarkFoldersJson(safeString(KEY_BOOKMARK_FOLDERS, null))

    fun saveBookmarkFolders(list: List<String>) {
        val arr = JSONArray()
        list.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            .forEach { arr.put(it) }
        prefs.edit().putString(KEY_BOOKMARK_FOLDERS, arr.toString()).apply()
    }

    /** All startup lists in one call; call off the main thread. */
    fun loadStartupLists(): StartupLists = StartupLists(
        bookmarks = loadBookmarks(),
        history = loadHistory(),
        bookmarkFolders = loadBookmarkFolders()
    )

    fun loadHistory(): MutableList<HistoryItem> =
        parseHistoryJson(safeString(KEY_HISTORY, null)).toMutableList()

    fun saveHistory(list: List<HistoryItem>) = writeList(KEY_HISTORY, list) {
        historyJsonObject(it)
    }

    fun clearHistory() = prefs.edit().remove(KEY_HISTORY).apply()

    fun loadGeckoDownloads(): MutableList<GeckoDownloadRecord> =
        parseGeckoDownloadsJson(
            safeString(KEY_GECKO_DOWNLOADS, null)
        ).toMutableList()

    fun saveGeckoDownloads(list: List<GeckoDownloadRecord>) =
        writeList(KEY_GECKO_DOWNLOADS, list) {
            geckoDownloadJsonObject(it)
        }

    /** Newest first; past the cap the oldest records fall off. */
    fun addGeckoDownload(record: GeckoDownloadRecord) {
        val list = loadGeckoDownloads()
        list.add(0, record)
        saveGeckoDownloads(list.take(MAX_GECKO_DOWNLOAD_RECORDS))
    }

    fun removeGeckoDownload(record: GeckoDownloadRecord) {
        val list = loadGeckoDownloads()
        if (list.remove(record)) saveGeckoDownloads(list)
    }

    fun saveTabs(list: List<SavedTab>, activeIndex: Int) {
        prefs.edit()
            .putString(KEY_TABS, savedTabsJson(list, activeIndex)).apply()
    }

    fun loadTabs(): Pair<List<SavedTab>, Int> =
        parseSavedTabsJson(safeString(KEY_TABS, null))

    /**
     * Fingerprint-mode session archive (v0.161.0): the fp tab
     * group gets the same process-death treatment as the normal
     * session — same JSON shape, separate key. Fingerprint tabs
     * still never write browsing history; this archive only
     * remembers which pages were open in the mode, exactly like
     * the normal session archive does for normal tabs.
     */
    fun saveFpTabs(list: List<SavedTab>, activeIndex: Int) {
        prefs.edit()
            .putString(KEY_FP_TABS, savedTabsJson(list, activeIndex))
            .apply()
    }

    fun loadFpTabs(): Pair<List<SavedTab>, Int> =
        parseSavedTabsJson(safeString(KEY_FP_TABS, null))

    /**
     * Whether a fingerprint session archive exists at all
     * (v0.165.0 startup audit). The startup restore checks this
     * single string read first: a user who never entered
     * fingerprint mode has no archive, and their cold start then
     * skips the restore's background round-trip entirely instead
     * of paying it — and delaying startupDone, which the
     * external-link and share consumers also wait on — only to
     * parse an absent key into an empty list. When an archive
     * DOES exist the restore runs exactly as before.
     */
    val hasFpTabsArchive: Boolean
        get() = safeString(KEY_FP_TABS, null) != null

    // List reads all go through parseJsonObjectList (v0.83.0):
    // per-element tolerance, so one corrupt row can no longer
    // empty a whole list the way the old getJSONObject-based
    // reader did. A failed read falls back in memory only —
    // nothing is written back until the user actually changes
    // that list, so the stored bytes survive a corrupt read.

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
        private const val KEY_AD_BLOCK = "ad_block"
        private const val KEY_HTTPS_FIRST = "https_first"
        private const val KEY_GUARD_SCRIPT = "guard_script"
        private const val KEY_TP_LEVEL = "tp_level"
        private const val KEY_DNT = "dnt"
        private const val KEY_GPC = "gpc"
        private const val KEY_WEBRTC_OFF = "webrtc_off"
        private const val KEY_FP_REGION = "fp_region"
        private const val KEY_FP_TIMEZONE = "fp_timezone"
        private const val KEY_FP_DEVICE = "fp_device"
        private const val KEY_FP_LANGUAGE = "fp_language"
        private const val KEY_FP_CUSTOM = "fp_custom"
        private const val KEY_COMPAT_SITES = "compat_sites"
        private const val KEY_DESKTOP_SITES = "desktop_sites"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_BOOKMARK_FOLDERS = "bookmark_folders"
        private const val KEY_HISTORY = "history"
        private const val KEY_READING_LIST = "reading_list"
        private const val KEY_GECKO_DOWNLOADS = "gecko_downloads"
        private const val KEY_TABS = "saved_tabs"
        private const val KEY_FP_TABS = "saved_fp_tabs"
        private const val KEY_TEXT_ZOOM = "text_zoom"
        private const val KEY_READER_FONT_SIZE = "reader_font_size"
        private const val KEY_READER_BACKGROUND = "reader_background"
        private const val KEY_WEB_DARK = "web_dark"
        private const val KEY_STARTUP_RESTORE = "startup_restore"
        private const val KEY_DOWNLOAD_MODE = "download_mode"
        private const val KEY_HISTORY_RETENTION = "history_retention"
        private const val KEY_SPEED_DIALS = "speed_dials"
        private const val KEY_TOTAL_BLOCKED = "total_blocked"
        private const val KEY_WV_INCOGNITO_DIRTY = "wv_incognito_dirty"
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

/**
 * Backup format version. Stays 1 in v0.51.0 even though the backup
 * gained fields: every addition is an optional field the parser
 * reads tolerantly (absent = keep the current value), so a backup
 * written by v0.19.0 still parses exactly as before, and an older
 * app version reading a newer backup simply ignores the fields it
 * does not know. Bump this only when a change would make an old
 * file parse differently, not for additive fields. (v0.72.0's
 * bookmark folders follow the same rule: the per-bookmark "folder"
 * field and the top-level "bookmarkFolders" list are both additive
 * and read tolerantly, so the version stays 1 and pre-v0.72.0
 * backups import exactly as before. The v0.74.0 "blockedStats"
 * object follows the same rule again: additive, read tolerantly,
 * absent = leave the current counters alone.)
 */
const val BACKUP_FORMAT_VERSION = 1

/** The settings slice of a full backup. All fields optional on read. */
data class BackupSettings(
    val mainEngine: Engine? = null,
    val incognitoEngine: Engine? = null,
    val searchEngine: SearchEngine? = null,
    val adBlockEnabled: Boolean? = null,
    val downloadMode: DownloadMode? = null,
    val historyRetention: HistoryRetention? = null,
    val textZoomPercent: Int? = null,
    val startupRestore: Boolean? = null,
    // Added in v0.51.0: the settings that were introduced after the
    // v0.19.0 backup slice was frozen and never made it in. All
    // optional on read, exactly like the original fields — a backup
    // from before v0.51.0 simply leaves the current values alone.
    val themeMode: ThemeMode? = null,
    val webDarkMode: Boolean? = null,
    // Added in v0.58.0: reader-mode typesetting. Same optional-on-read
    // treatment as the v0.51.0 additions.
    val readerFontSize: ReaderFontSize? = null,
    val readerBackground: ReaderBackground? = null,
    // Added in v0.77.0: HTTPS-first switch. Same optional-on-read
    // treatment as the v0.51.0 additions.
    val httpsFirstEnabled: Boolean? = null,
    // Added in v0.91.0: built-in page-guard script switch. Same
    // optional-on-read treatment as the v0.51.0 additions.
    val guardScriptEnabled: Boolean? = null,
    // Added in v0.92.0: the privacy section (tracking protection
    // level, Do Not Track, Global Privacy Control, WebRTC off).
    // Same optional-on-read treatment as the v0.51.0 additions.
    val trackingProtectionLevel: TrackingProtectionLevel? = null,
    val doNotTrackEnabled: Boolean? = null,
    val globalPrivacyControlEnabled: Boolean? = null,
    val webRtcDisabled: Boolean? = null
) {
    /** Plain-language list of what applying these settings would overwrite. */
    fun overwriteSummary(): List<String> {
        val out = mutableListOf<String>()
        if (mainEngine != null) out.add("主内核（默认用哪个内核打开网页）")
        if (incognitoEngine != null) out.add("无痕内核")
        if (searchEngine != null) out.add("搜索引擎")
        if (adBlockEnabled != null) out.add("广告拦截开关")
        if (downloadMode != null) out.add("下载方式")
        if (historyRetention != null) out.add("历史保留期限")
        if (textZoomPercent != null) out.add("网页文字大小")
        if (startupRestore != null) out.add("启动时恢复标签页的开关")
        if (themeMode != null) out.add("界面主题（跟随系统 / 浅色 / 深色）")
        if (webDarkMode != null) out.add("网页深色模式")
        if (readerFontSize != null) out.add("阅读字号")
        if (readerBackground != null) out.add("阅读背景")
        if (httpsFirstEnabled != null) out.add("优先用 HTTPS 打开网站")
        if (guardScriptEnabled != null) out.add("内置净化脚本开关")
        if (trackingProtectionLevel != null) out.add("增强跟踪保护等级")
        if (doNotTrackEnabled != null) out.add("不要追踪开关")
        if (globalPrivacyControlEnabled != null) out.add("不出售或分享资料开关")
        if (webRtcDisabled != null) out.add("停用 WebRTC 开关")
        return out
    }
}

/** A parsed full-backup file: the four lists plus the settings slice. */
data class BackupData(
    val bookmarks: List<Bookmark>,
    val history: List<HistoryItem>,
    val settings: BackupSettings,
    /**
     * Per-site compat list (v0.51.0). Kept beside the four lists,
     * not inside the settings slice, because it behaves like them:
     * on import it merges (union) instead of overwriting, so sites
     * the user marked on this device are never deleted by a restore.
     * Empty when the backup predates v0.51.0.
     */
    val compatSites: List<String> = emptyList(),
    /**
     * Per-site desktop-mode list (v0.56.0). Same treatment as the
     * compat list: a user asset collected site by site, so it sits
     * beside the lists and merges (union) on import. Empty when the
     * backup predates v0.56.0.
     */
    val desktopSites: List<String> = emptyList(),
    /**
     * Bookmark folder names (v0.72.0), including empty folders that
     * no bookmark derives. Merges (union) on import, like the two
     * site lists. Empty when the backup predates v0.72.0.
     */
    val bookmarkFolders: List<String> = emptyList(),
    /**
     * Lifetime blocked-request total (v0.74.0 backup audit). The
     * v0.74.0 audit of every persisted key found the two blocked
     * counters were the only user-accumulated data missing from
     * the backup: they cannot be rebuilt (a new device starts at
     * zero), so they belong in it. Null when the backup predates
     * v0.74.0 — the current counters are then left untouched.
     * On import the backed-up values OVERWRITE the current ones
     * (restore semantics, like the settings slice).
     */
    val totalBlocked: Long? = null
)

/** Serialize a full backup (lists + main settings) to one JSON text. */
fun backupToJson(
    bookmarks: List<Bookmark>,
    history: List<HistoryItem>,
    settings: BackupSettings,
    compatSites: List<String> = emptyList(),
    desktopSites: List<String> = emptyList(),
    bookmarkFolders: List<String> = emptyList(),
    totalBlocked: Long? = null
): String {
    val bookmarkArr = JSONArray()
    bookmarks.forEach { bookmarkArr.put(bookmarkJsonObject(it)) }
    val historyArr = JSONArray()
    history.forEach { historyArr.put(historyJsonObject(it)) }
    val settingsObj = JSONObject()
    settings.mainEngine?.let { settingsObj.put("mainEngine", it.id) }
    settings.incognitoEngine?.let { settingsObj.put("incognitoEngine", it.id) }
    settings.searchEngine?.let { settingsObj.put("searchEngine", it.id) }
    settings.adBlockEnabled?.let { settingsObj.put("adBlockEnabled", it) }
    settings.downloadMode?.let { settingsObj.put("downloadMode", it.id) }
    settings.historyRetention?.let { settingsObj.put("historyRetention", it.id) }
    settings.textZoomPercent?.let { settingsObj.put("textZoomPercent", it) }
    settings.startupRestore?.let { settingsObj.put("startupRestore", it) }
    settings.themeMode?.let { settingsObj.put("themeMode", it.id) }
    settings.webDarkMode?.let { settingsObj.put("webDarkMode", it) }
    settings.readerFontSize?.let { settingsObj.put("readerFontSize", it.id) }
    settings.readerBackground?.let {
        settingsObj.put("readerBackground", it.id)
    }
    settings.httpsFirstEnabled?.let {
        settingsObj.put("httpsFirstEnabled", it)
    }
    settings.guardScriptEnabled?.let {
        settingsObj.put("guardScriptEnabled", it)
    }
    settings.trackingProtectionLevel?.let {
        settingsObj.put("trackingProtectionLevel", it.id)
    }
    settings.doNotTrackEnabled?.let {
        settingsObj.put("doNotTrackEnabled", it)
    }
    settings.globalPrivacyControlEnabled?.let {
        settingsObj.put("globalPrivacyControlEnabled", it)
    }
    settings.webRtcDisabled?.let {
        settingsObj.put("webRtcDisabled", it)
    }
    val compatArr = JSONArray()
    compatSites.forEach { compatArr.put(it) }
    val desktopArr = JSONArray()
    desktopSites.forEach { desktopArr.put(it) }
    val folderArr = JSONArray()
    bookmarkFolders.map { it.trim() }.filter { it.isNotEmpty() }
        .distinct().forEach { folderArr.put(it) }
    val root = JSONObject()
        .put("format", BACKUP_FORMAT)
        .put("version", BACKUP_FORMAT_VERSION)
        .put("bookmarks", bookmarkArr)
        .put("history", historyArr)
        .put("compatSites", compatArr)
        .put("desktopSites", desktopArr)
        .put("bookmarkFolders", folderArr)
        .put("settings", settingsObj)
    // Blocked statistics (v0.74.0): written only when the caller
    // supplies a total, as one "blockedStats" object. Purely
    // additive — older app versions reading this file simply
    // ignore the unknown key, and the format version stays 1.
    totalBlocked?.let { total ->
        root.put("blockedStats", JSONObject().put("total", total))
    }
    return root.toString(2)
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
        // folder (v0.72.0): absent in older backups — optString
        // yields "", which IS uncategorized, so old files import
        // exactly as before.
        Bookmark(
            o.optString("title").ifBlank { url }, url,
            o.optBoolean("pinned"), o.optString("folder").trim()
        )
    }
    val history = readArr("history") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        HistoryItem(
            o.optString("title").ifBlank { url }, url, o.optLong("time")
        )
    }
    // Backups written before v0.91.0 may carry the two removed
    // homepage lists (reading list, speed dials). They fold into
    // the bookmarks, mirroring the on-device migration: speed
    // dials pinned, reading entries in a 阅读列表 folder, URLs
    // already bookmarked not duplicated.
    val legacyReading = readArr("readingList") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        ReadingItem(
            o.optString("title").ifBlank { url }, url,
            o.optLong("time"), o.optBoolean("read")
        )
    }
    val legacyDials = readArr("speedDials") { o ->
        val url = webUrl(o.optString("url")) ?: return@readArr null
        SpeedDial(
            o.optString("title").ifBlank { url }, url
        )
    }
    val mergedBookmarks = bookmarks.toMutableList()
    val knownBookmarkUrls = bookmarks.map { it.url }.toMutableSet()
    legacyDials.forEach { dial ->
        if (knownBookmarkUrls.add(dial.url)) {
            mergedBookmarks.add(
                Bookmark(dial.title, dial.url, pinned = true)
            )
        }
    }
    legacyReading.forEach { item ->
        if (knownBookmarkUrls.add(item.url)) {
            mergedBookmarks.add(
                Bookmark(item.title, item.url, folder = "阅读列表")
            )
        }
    }
    val s = obj.optJSONObject("settings")
    // Strictly typed settings reads (v0.101.0 backup audit). A
    // backup is untrusted input, and the lenient opt* accessors
    // misread wrong-typed values in ways that DESTROY data on
    // apply: optInt on a string yields 0 (a "115" written as text
    // would apply a 0% text zoom), optBoolean on a number yields
    // false, and the fromId fallbacks turn an unknown id into a
    // non-null default that then OVERWRITES the current setting
    // (an old backup whose search engine was the since-removed
    // "custom" would silently reset the current engine to the
    // default). So: enum fields accept only a string that names
    // a current entry, booleans accept only real JSON booleans,
    // and the text zoom accepts only a number in a sane range —
    // anything else parses as absent and leaves the current
    // value alone, the same absent-field semantics every real
    // backup relies on. backupToJson only ever writes these
    // exact types, so real files are unaffected.
    fun strOf(key: String): String? =
        (s?.opt(key) as? String)?.takeIf { it.isNotBlank() }
    fun boolOf(key: String): Boolean? = s?.opt(key) as? Boolean
    fun engineOf(key: String): Engine? =
        strOf(key)?.let { id -> Engine.entries.firstOrNull { it.id == id } }
    val settings = if (s == null) {
        BackupSettings()
    } else {
        BackupSettings(
            mainEngine = engineOf("mainEngine"),
            incognitoEngine = engineOf("incognitoEngine"),
            searchEngine = strOf("searchEngine")?.let { id ->
                SearchEngine.entries.firstOrNull { it.id == id }
            },
            adBlockEnabled = boolOf("adBlockEnabled"),
            downloadMode = strOf("downloadMode")?.let { id ->
                DownloadMode.entries.firstOrNull { it.id == id }
            },
            historyRetention = strOf("historyRetention")?.let { id ->
                HistoryRetention.entries.firstOrNull { it.id == id }
            },
            textZoomPercent = (s.opt("textZoomPercent") as? Number)
                ?.toInt()?.takeIf { it in 50..200 },
            startupRestore = boolOf("startupRestore"),
            themeMode = strOf("themeMode")?.let { id ->
                ThemeMode.entries.firstOrNull { it.id == id }
            },
            webDarkMode = boolOf("webDarkMode"),
            readerFontSize = strOf("readerFontSize")?.let { id ->
                ReaderFontSize.entries.firstOrNull { it.id == id }
            },
            readerBackground = strOf("readerBackground")?.let { id ->
                ReaderBackground.entries.firstOrNull { it.id == id }
            },
            httpsFirstEnabled = boolOf("httpsFirstEnabled"),
            guardScriptEnabled = boolOf("guardScriptEnabled"),
            trackingProtectionLevel = strOf("trackingProtectionLevel")
                ?.let { id ->
                    TrackingProtectionLevel.entries
                        .firstOrNull { it.id == id }
                },
            doNotTrackEnabled = boolOf("doNotTrackEnabled"),
            globalPrivacyControlEnabled =
                boolOf("globalPrivacyControlEnabled"),
            webRtcDisabled = boolOf("webRtcDisabled")
        )
    }
    // Compat sites (v0.51.0): plain host names only — anything with
    // a scheme, a path, whitespace or an implausible length is
    // dropped, never stored. Absent in older backups: empty list.
    val compatArr = obj.optJSONArray("compatSites")
    val compatSites = if (compatArr == null) {
        emptyList()
    } else {
        val count = minOf(compatArr.length(), MAX_IMPORT_ENTRIES)
        (0 until count).mapNotNull { i ->
            // Real strings only (v0.101.0, same rule as the
            // on-device site lists): optString would stringify a
            // number into a plausible-looking host name.
            val host = (compatArr.opt(i) as? String)
                ?.trim()?.lowercase() ?: return@mapNotNull null
            host.takeIf {
                it.isNotEmpty() && it.length <= 253 &&
                    !it.contains("://") && !it.contains("/") &&
                    !it.contains(":") && it.none { c -> c.isWhitespace() }
            }
        }.distinct()
    }
    // Desktop-mode sites (v0.56.0): same plain-host-name guard as
    // the compat list above. Absent in older backups: empty list.
    val desktopArr = obj.optJSONArray("desktopSites")
    val desktopSites = if (desktopArr == null) {
        emptyList()
    } else {
        val count = minOf(desktopArr.length(), MAX_IMPORT_ENTRIES)
        (0 until count).mapNotNull { i ->
            val host = (desktopArr.opt(i) as? String)
                ?.trim()?.lowercase() ?: return@mapNotNull null
            host.takeIf {
                it.isNotEmpty() && it.length <= 253 &&
                    !it.contains("://") && !it.contains("/") &&
                    !it.contains(":") && it.none { c -> c.isWhitespace() }
            }
        }.distinct()
    }
    // Bookmark folder names (v0.72.0): plain names only — blank or
    // implausibly long entries are dropped, never stored. Absent in
    // older backups: empty list (folders still derive from the
    // imported bookmarks themselves).
    val folderArr = obj.optJSONArray("bookmarkFolders")
    val bookmarkFolders = if (folderArr == null) {
        emptyList()
    } else {
        val count = minOf(folderArr.length(), MAX_IMPORT_ENTRIES)
        (0 until count).mapNotNull { i ->
            val name = (folderArr.opt(i) as? String)?.trim()
                ?: return@mapNotNull null
            name.takeIf { it.isNotEmpty() && it.length <= 60 }
        }.distinct()
    }
    // Blocked statistics (v0.74.0): absent in older backups — the
    // total stays null and the import leaves the current counter
    // alone. (The per-day map some pre-v0.91.0 backups carry is
    // an unknown key now — the daily view was removed in v0.91.0 —
    // and is ignored like every other unknown key.)
    val statsObj = obj.optJSONObject("blockedStats")
    // Only a real JSON number counts (v0.101.0): optLong on a
    // wrong-typed value yields 0, which on apply would wipe the
    // current counter to zero — a wrong-typed total must parse
    // as absent and leave the counter alone.
    val parsedTotalBlocked: Long? =
        (statsObj?.opt("total") as? Number)?.toLong()?.coerceAtLeast(0L)
    return BackupData(
        mergedBookmarks, history, settings, compatSites,
        desktopSites, bookmarkFolders,
        totalBlocked = parsedTotalBlocked
    )
}

/** Serialize bookmarks for export; the format is a plain JSON object. */
fun bookmarksToJson(list: List<Bookmark>): String {
    val arr = JSONArray()
    list.forEach { arr.put(bookmarkJsonObject(it)) }
    return JSONObject()
        .put("app", "TuxunBrowse")
        .put("type", "bookmarks")
        .put("items", arr)
        .toString(2)
}

/** Escape text for HTML output: & first, so escapes are not re-escaped. */
private fun escapeHtmlText(text: String): String {
    var out = text
    out = out.replace("&", "&amp;")
    out = out.replace("<", "&lt;")
    out = out.replace(">", "&gt;")
    out = out.replace("\"", "&quot;")
    return out
}

/**
 * Export bookmarks in the standard Netscape HTML format (v0.90.0),
 * the mirror image of [bookmarksFromHtml]: uncategorized entries at
 * the root of the list, then one folder section per folder in
 * first-appearance order. Other browsers can import this file
 * directly. The pinned flag has no HTML representation and is not
 * carried — the JSON export remains the lossless one. The output
 * round-trips through [bookmarksFromHtml] with titles, URLs and
 * folders intact.
 */
fun bookmarksToHtml(list: List<Bookmark>): String {
    val sb = StringBuilder()
    sb.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n")
    sb.append(
        "<META HTTP-EQUIV=\"Content-Type\" " +
            "CONTENT=\"text/html; charset=UTF-8\">\n"
    )
    sb.append("<TITLE>Bookmarks</TITLE>\n")
    sb.append("<H1>Bookmarks</H1>\n")
    sb.append("<DL><p>\n")
    fun entry(b: Bookmark, indent: String) {
        sb.append(indent)
            .append("<DT><A HREF=\"")
            .append(escapeHtmlText(b.url))
            .append("\">")
            .append(escapeHtmlText(b.title))
            .append("</A>\n")
    }
    for (b in list) {
        if (b.folder.isBlank()) entry(b, "    ")
    }
    val folderOrder = mutableListOf<String>()
    for (b in list) {
        val f = b.folder.trim()
        if (f.isNotEmpty() && f !in folderOrder) folderOrder.add(f)
    }
    for (f in folderOrder) {
        sb.append("    <DT><H3>").append(escapeHtmlText(f)).append("</H3>\n")
        sb.append("    <DL><p>\n")
        for (b in list) {
            if (b.folder.trim() == f) entry(b, "        ")
        }
        sb.append("    </DL><p>\n")
    }
    sb.append("</DL><p>\n")
    return sb.toString()
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
            // folder (v0.72.0): absent in exports written before
            // folders existed — "" = uncategorized, as everywhere.
            Bookmark(
                o.optString("title").ifBlank { url }, url,
                o.optBoolean("pinned"), o.optString("folder").trim()
            )
        }
    }
}

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

/**
 * One pass over a Netscape bookmark file needs three token kinds:
 * list open/close (the folder structure lives in how `<DL>` lists
 * nest), folder headings (`<H3>`), and the links themselves.
 * Group 1 is the closing slash of a `<DL>` token, group 2 a folder
 * name, groups 3/4 a link's href and title; exactly one alternative
 * participates per match.
 */
private val HTML_TOKEN_REGEX = Regex(
    "<(/?)DL\\b[^>]*>|<H3\\b[^>]*>(.*?)</H3>|" +
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
 *
 * Folder structure (v0.89.0): a `<H3>` heading names the folder
 * whose entries live in the `<DL>` list that follows it, and lists
 * nest. Each bookmark takes the innermost folder it sits in (this
 * app's folders are one flat level, so a nested export folder lands
 * under its deepest heading); entries outside any folder list keep
 * the empty folder, i.e. uncategorized. A heading whose list never
 * opens names nothing. Folder display needs no extra bookkeeping:
 * allBookmarkFolders derives folder names from the bookmarks
 * themselves, exactly like the JSON import path.
 * De-duplication against existing bookmarks is the caller's job.
 */
fun bookmarksFromHtml(raw: String): Pair<List<Bookmark>, Int> {
    val out = mutableListOf<Bookmark>()
    var skipped = 0
    val folderStack = mutableListOf<String>()
    var pendingFolder: String? = null
    for (match in HTML_TOKEN_REGEX.findAll(raw)) {
        val groups = match.groups
        when {
            groups[3] != null -> {
                if (out.size >= MAX_IMPORT_ENTRIES) break
                val href = decodeHtmlEntities(groups[3]!!.value.trim())
                val scheme = href.substringBefore("://", "").lowercase()
                if (href.isBlank() || (scheme != "http" && scheme != "https")) {
                    skipped++
                    continue
                }
                val title = decodeHtmlEntities(
                    groups[4]!!.value.replace(Regex("<[^>]+>"), "").trim()
                )
                out.add(
                    Bookmark(
                        title.ifBlank { href }, href,
                        folder = folderStack.lastOrNull().orEmpty()
                    )
                )
            }
            groups[2] != null -> {
                pendingFolder = normalizeBookmarkFolderName(
                    decodeHtmlEntities(
                        groups[2]!!.value.replace(Regex("<[^>]+>"), "").trim()
                    )
                )
            }
            groups[1] != null -> {
                if (groups[1]!!.value == "/") {
                    if (folderStack.isNotEmpty()) {
                        folderStack.removeAt(folderStack.lastIndex)
                    }
                } else {
                    folderStack.add(pendingFolder.orEmpty())
                }
                pendingFolder = null
            }
        }
    }
    return out to skipped
}

/** Turn whatever the user typed into a URL: full URL, bare host, or a search. */
fun resolveInput(
    input: String,
    searchEngine: SearchEngine = SearchEngine.BAIDU
): String {
    // Tabs and line breaks are removed before anything else
    // (v0.138.0 input audit): a URL copied from a wrapped line or
    // pasted out of a multi-line note arrives with \n / \t inside
    // it, and leaving them in produced a broken host name or
    // sent a wrapped URL off to the search engine instead.
    // This mirrors what the URL standard itself does with tabs and
    // newlines. Ordinary spaces stay — they still mark the input
    // as words to search for below.
    val text = input
        .filterNot { it == '\t' || it == '\r' || it == '\n' }
        .trim()
    if (text.isEmpty()) return HOME_URL
    // Only http(s) URLs pass through as-is (v0.31.0): the old about:
    // and file: pass-throughs are gone — file: pages are refused like
    // every other non-web scheme (see isAllowedPageUrl), and about:
    // pages were never a feature of this browser. The scheme check is
    // case-insensitive (v0.114.0): "HTTP://EXAMPLE.COM" is an http
    // URL, and the old case-sensitive check mangled it into
    // "https://HTTP://…" instead of passing it through.
    val lower = text.lowercase()
    if (lower.startsWith("http://") || lower.startsWith("https://")) return text
    val looksLikeHost = !text.contains(" ") &&
        (text.contains(".") || lower.startsWith("localhost"))
    return if (looksLikeHost) {
        "https://$text"
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
    // Same tab/newline removal as resolveInput (v0.138.0), so the
    // mirror never disagrees with the resolver about pasted text.
    val text = input
        .filterNot { it == '\t' || it == '\r' || it == '\n' }
        .trim()
    if (text.isEmpty()) return false
    // Mirror resolveInput exactly, including its case-insensitive
    // scheme and localhost checks (v0.114.0).
    val lower = text.lowercase()
    if (lower.startsWith("http://") || lower.startsWith("https://")) return true
    return !text.contains(" ") &&
        (text.contains(".") || lower.startsWith("localhost"))
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

/**
 * The page a release is downloaded from (v0.119.0 update-check
 * audit). The html_url the API returns is authoritative and is
 * used verbatim when present; only when it is missing/blank is
 * the URL assembled from the tag, in the exact shape the repo's
 * release pages use (".../releases/tag/vX.Y.Z"). A tag that is
 * not a release tag yields "" — the caller treats that as "no
 * page to open" instead of guessing. Pure function.
 */
fun releasePageUrl(tag: String, htmlUrl: String): String {
    val fromApi = htmlUrl.trim()
    if (fromApi.isNotBlank()) return fromApi
    return if (isReleaseTag(tag)) {
        "https://github.com/liyuzhou79-star/TuxunBrowse/releases/tag/" +
            tag.trim()
    } else {
        ""
    }
}

/**
 * The four outcomes of one manual update check (v0.119.0 audit).
 * Before this split, every failure — no network, a timeout, an
 * HTTP error, GitHub rate limiting, even a successful response
 * with no usable release in it — collapsed into the single
 * toast "检查失败，请检查网络", which is wrong advice for most
 * of them. The outcomes are kept distinct so the UI can say
 * what actually happened and what to do next.
 */
sealed interface UpdateFetchResult {
    /** A release list came back and [tag] is its highest version. */
    data class Found(val tag: String, val pageUrl: String) :
        UpdateFetchResult

    /** The list came back fine but held no usable release. */
    data object NoReleases : UpdateFetchResult

    /** GitHub refused with 403/429: too many requests. */
    data object RateLimited : UpdateFetchResult

    /** No answer at all, or an HTTP error that is not limiting. */
    data object Failed : UpdateFetchResult
}

/**
 * Classify one finished update-check request from its HTTP
 * status ([httpCode], null when the request never got a
 * response) and the picked latest release ([latest], null when
 * the response held none). Rate limiting is checked first so a
 * 403/429 is never misreported as a network problem. Pure
 * function.
 */
fun updateFetchResult(
    httpCode: Int?,
    latest: Pair<String, String>?
): UpdateFetchResult = when {
    httpCode == 403 || httpCode == 429 ->
        UpdateFetchResult.RateLimited
    httpCode == null || httpCode !in 200..299 ->
        UpdateFetchResult.Failed
    latest == null -> UpdateFetchResult.NoReleases
    else -> UpdateFetchResult.Found(
        latest.first,
        releasePageUrl(latest.first, latest.second)
    )
}
