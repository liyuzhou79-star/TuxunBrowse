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
    val suggestionTemplate: String = ""
) {
    // Suggestion endpoints (v0.50.0, all curl-verified from the build
    // VM, no key/token needed, all answer the same OpenSearch JSON
    // shape [query, [suggestion, ...]]):
    // - Baidu: ie=UTF-8 is load-bearing — without it the endpoint
    //   answers GBK bytes and Chinese suggestions come out garbled.
    // - Bing: api.bing.com/osjson.aspx works; the www.bing.com/AS/
    //   Suggestions endpoint answered empty in testing, not used.
    // - Google: client=firefox gives the plain OpenSearch shape.
    // - DuckDuckGo: type=list gives the OpenSearch shape; the
    //   default shape is [{"phrase": ...}] objects instead.
    BAIDU(
        "baidu", "百度", "https://www.baidu.com/s?wd=%s",
        "https://suggestion.baidu.com/su?wd=%s&action=opensearch&ie=UTF-8"
    ),
    BING(
        "bing", "必应", "https://www.bing.com/search?q=%s",
        "https://api.bing.com/osjson.aspx?query=%s"
    ),
    GOOGLE(
        "google", "Google", "https://www.google.com/search?q=%s",
        "https://suggestqueries.google.com/complete/search?client=firefox&q=%s"
    ),
    DUCKDUCKGO(
        "duckduckgo", "DuckDuckGo", "https://duckduckgo.com/?q=%s",
        "https://duckduckgo.com/ac/?q=%s&type=list"
    ),

    /** User-supplied engine; its real template lives in the Store. */
    CUSTOM("custom", "自定义", "");

    fun searchUrl(query: String): String {
        // CUSTOM has no built-in template (the user's own is applied
        // by resolveInput); never format an empty template.
        val effective = template.ifBlank { BAIDU.template }
        return effective.format(java.net.URLEncoder.encode(query, "UTF-8"))
    }

    /**
     * Suggestion endpoint URL for [query], or null when this engine
     * has no suggestion endpoint (CUSTOM: there is no generic
     * suggestion protocol for a user-supplied template, so online
     * suggestions are simply unavailable there).
     */
    fun suggestionUrl(query: String): String? {
        if (suggestionTemplate.isBlank() || query.isBlank()) return null
        return suggestionTemplate.format(
            java.net.URLEncoder.encode(query, "UTF-8")
        )
    }

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
 * Bookmarks-page display sort (v0.63.0). Display-only: the stored
 * list order is never rewritten by sorting — this only decides the
 * order rows are rendered in. There is deliberately no "by time
 * added" option: [Bookmark] carries no timestamp field, and the
 * stored JSON format is not being changed to invent one.
 */
enum class BookmarkSortOrder(val id: String, val label: String) {
    DEFAULT("default", "默认顺序"),
    NAME("name", "按名称");

    companion object {
        fun fromId(id: String?): BookmarkSortOrder =
            entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * The bookmarks page's display order (v0.63.0): pinned entries
 * first (same rule as the home grid), then — in DEFAULT — the
 * stored order as-is, or — in NAME — a locale collation of the
 * titles via the platform [java.text.Collator] (on a Chinese
 * locale that collates Chinese titles by pinyin; that is the
 * platform collator's own behaviour, not a custom rule). Equal
 * titles fall back to URL so the order is fully deterministic.
 * The name sort applies inside the pinned group and inside the
 * unpinned group separately; the input list is never mutated.
 */
fun sortedBookmarksForDisplay(
    bookmarks: List<Bookmark>,
    order: BookmarkSortOrder
): List<Bookmark> {
    val pinned = bookmarks.filter { it.pinned }
    val unpinned = bookmarks.filter { !it.pinned }
    if (order == BookmarkSortOrder.DEFAULT) return pinned + unpinned
    val collator = java.text.Collator.getInstance()
    val byName = Comparator<Bookmark> { a, b ->
        val byTitle = collator.compare(a.title, b.title)
        if (byTitle != 0) byTitle else a.url.compareTo(b.url)
    }
    return pinned.sortedWith(byName) + unpinned.sortedWith(byName)
}
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

/** Debounce before an address-bar keystroke may trigger a suggestion fetch. */
const val SEARCH_SUGGESTION_DEBOUNCE_MS = 300L

/** Connect/read timeout for one suggestion fetch: short, fail silent. */
const val SEARCH_SUGGESTION_TIMEOUT_MS = 3_000

/** At most this many online suggestions are kept from one response. */
const val MAX_ONLINE_SUGGESTIONS = 8

/**
 * Cap on bookmark + online suggestion rows shown together (the
 * trailing "search this" row is extra). Bookmarks come first, so
 * online rows only fill the room the bookmarks leave.
 */
const val MAX_COMBINED_SUGGESTIONS = 10

/**
 * Parse one suggestion response. Every built-in engine answers the
 * OpenSearch shape [query, [suggestion, ...]] (v0.50.0 audit), so one
 * parser serves all of them. Anything else parses to an empty list.
 * Blank entries are dropped and duplicates removed case-insensitively.
 */
fun parseSearchSuggestions(raw: String, limit: Int = MAX_ONLINE_SUGGESTIONS): List<String> {
    if (limit <= 0) return emptyList()
    val arr = try {
        JSONArray(raw)
    } catch (e: Exception) {
        return emptyList()
    }
    val inner = arr.optJSONArray(1) ?: return emptyList()
    val out = mutableListOf<String>()
    val seen = mutableSetOf<String>()
    for (i in 0 until inner.length()) {
        if (out.size >= limit) break
        val s = inner.optString(i).trim()
        if (s.isNotEmpty() && seen.add(s.lowercase())) out.add(s)
    }
    return out
}

/**
 * Fetch online search suggestions for [query] from [engine].
 * Privacy contract (v0.50.0): this is only ever called when the user
 * has turned the "search suggestions" switch on, the active tab is
 * not incognito, the engine is a built-in one and the typed text is
 * not URL-like — all enforced by the shared
 * [shouldFetchOnlineSuggestions] gate at every call site (the
 * address bar since v0.50.0, the home search box since v0.59.0). The
 * request itself carries only the query (in the URL) and a plain
 * User-Agent: no Cookie header (HttpURLConnection sends none unless
 * a CookieHandler is installed, and this app never installs one),
 * no account information. The result is kept in memory only: the
 * query and the suggestions are never written to disk or logged.
 * Every failure is silent and returns an empty list.
 */
fun fetchSearchSuggestions(engine: SearchEngine, query: String): List<String> {
    val url = engine.suggestionUrl(query.trim()) ?: return emptyList()
    var conn: java.net.HttpURLConnection? = null
    return try {
        conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
        conn.connectTimeout = SEARCH_SUGGESTION_TIMEOUT_MS
        conn.readTimeout = SEARCH_SUGGESTION_TIMEOUT_MS
        conn.useCaches = false
        conn.setRequestProperty("User-Agent", "TuxunBrowse")
        conn.setRequestProperty("Accept", "application/json")
        if (conn.responseCode !in 200..299) {
            emptyList()
        } else {
            // All four endpoints answer UTF-8 when asked the way the
            // templates ask (Baidu needs its ie=UTF-8, see above).
            val raw = conn.inputStream.bufferedReader(
                java.nio.charset.StandardCharsets.UTF_8
            ).readText()
            parseSearchSuggestions(raw)
        }
    } catch (t: Throwable) {
        emptyList()
    } finally {
        try {
            conn?.disconnect()
        } catch (t: Throwable) {
            // Nothing to clean up beyond this; failing to disconnect
            // must never surface.
        }
    }
}

/**
 * The ONE privacy gate for online search suggestions (v0.59.0).
 * The address bar (v0.50.0) and the home search box share this exact
 * check so the two surfaces can never drift apart: a fetch may happen
 * only when EVERY condition holds — the user turned the switch on,
 * the field is focused, the active tab is NOT incognito (incognito
 * never fetches, whatever the switch says), the engine is a built-in
 * one with a suggestion endpoint (custom engines have none), the
 * typed text is non-empty, is not URL-like (it is about to be opened
 * as an address, so asking an engine about it would only leak it)
 * and does not start with a blocked scheme.
 */
fun shouldFetchOnlineSuggestions(
    enabled: Boolean,
    focused: Boolean,
    incognito: Boolean,
    engine: SearchEngine,
    query: String
): Boolean =
    enabled && focused && !incognito && engine != SearchEngine.CUSTOM &&
        query.isNotEmpty() && !isUrlLikeInput(query) &&
        !hasBlockedPageScheme(query)

/**
 * Merge step shared by the address bar and the home search box
 * (v0.59.0): given the bookmark matches and the raw online words,
 * drop online rows that merely repeat a bookmark title/URL or the
 * typed text itself, then cap bookmark + online rows together at
 * [MAX_COMBINED_SUGGESTIONS] — bookmarks come first, so online rows
 * only fill the room the bookmarks leave. The trailing "search
 * this" row is not part of this cap (each surface renders its own).
 */
fun mergedOnlineSuggestionRows(
    bookmarkSuggestions: List<Suggestion>,
    onlineSuggestions: List<String>,
    query: String
): List<String> {
    val bookmarkKeys = bookmarkSuggestions.map { it.title.lowercase() }.toSet() +
        bookmarkSuggestions.map { it.url.lowercase() }
    return onlineSuggestions.filter { s ->
        s.lowercase() !in bookmarkKeys &&
            !s.equals(query, ignoreCase = true)
    }.take(
        (MAX_COMBINED_SUGGESTIONS - bookmarkSuggestions.size).coerceAtLeast(0)
    )
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
    val readingCount: Int,
    val speedDialCount: Int,
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
        "阅读列表：${info.readingCount} 条",
        "首页快捷方式：${info.speedDialCount} 条",
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

/**
 * True when [host] is a remembered reader-mode site or a subdomain
 * of one (v0.73.0). Same shared host-matching rule as the compat
 * and desktop lists, so "this site" means the same thing for all
 * three per-site memories.
 */
fun isReaderHost(host: String?, sites: Set<String>): Boolean {
    if (host.isNullOrBlank()) return false
    return sites.any { site -> hostMatchesSite(host, site) }
}

private val SHARED_URL_REGEX = Regex("https?://\\S+")

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
 * goes straight to it and "save to reading list" is offered; when
 * it finds none, the whole text is treated as words to search for.
 * Only the FIRST URL is taken — a share is about one page.
 */
fun extractFirstWebUrl(text: String?): String? {
    if (text.isNullOrBlank()) return null
    val raw = SHARED_URL_REGEX.find(text)?.value ?: return null
    val trimmed = raw.trimEnd { it in SHARED_URL_TRAILING }
    return trimmed.takeIf { it.startsWith("http://") || it.startsWith("https://") }
        ?.takeIf { it.length > "https://".length }
}

/** How many days of per-day blocked counts are kept (v0.70.0). */
const val BLOCKED_DAILY_KEEP_DAYS = 31

/** Storage key for one day's blocked count: the ISO date, yyyy-MM-dd. */
fun blockedDayKey(date: java.time.LocalDate): String = date.toString()

/**
 * Drop per-day blocked counts outside the most recent [keepDays]
 * days ending at [today] (v0.70.0). Keys that do not parse as a
 * date, and dates in the future, are dropped too — the stored map
 * is app-private, so a bad key can only come from a corrupt write.
 */
fun pruneBlockedDaily(
    counts: Map<String, Long>,
    today: java.time.LocalDate,
    keepDays: Int = BLOCKED_DAILY_KEEP_DAYS
): Map<String, Long> {
    val cutoff = today.minusDays((keepDays - 1).toLong())
    return counts.filterKeys { key ->
        val parsed = try {
            java.time.LocalDate.parse(key)
        } catch (e: Exception) {
            null
        }
        parsed != null && !parsed.isBefore(cutoff) && !parsed.isAfter(today)
    }
}

/**
 * The last [days] days of blocked counts ending at [today], oldest
 * first (v0.70.0). Days with no recorded blocks come back as zero,
 * so the settings view can render a fixed-length series.
 */
fun recentBlockedSeries(
    counts: Map<String, Long>,
    today: java.time.LocalDate,
    days: Int = 7
): List<Pair<String, Long>> =
    (days - 1 downTo 0).map { back ->
        val key = blockedDayKey(today.minusDays(back.toLong()))
        key to (counts[key] ?: 0L)
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

/** One searchable settings entry: an option's name and its sub-page. */
data class SettingsIndexEntry(
    val name: String,
    val page: String,
    val pageLabel: String
)

/**
 * Static search index for the settings home (v0.71.0): every option
 * on the six settings sub-pages, listed by the name it shows on
 * screen, with the sub-page key it lives on. Kept in step with
 * SettingsScreen by hand — when an option is added, renamed or
 * moved between sub-pages, its entry here must follow.
 */
val SETTINGS_SEARCH_INDEX: List<SettingsIndexEntry> = listOf(
    // 内核与搜索引擎 (engine)
    SettingsIndexEntry("主内核", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("火狐内核（GeckoView）", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("系统 WebView", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("无痕内核", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("搜索引擎", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("百度", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("必应", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("Google", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("DuckDuckGo", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("自定义搜索引擎", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("获取联网搜索建议", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("兼容模式记住的网站", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("桌面模式站点", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("阅读模式记住的网站", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("清空全部桌面模式站点", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("默认浏览器", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("重新检测默认浏览器状态", "engine", "内核与搜索引擎"),
    SettingsIndexEntry("两个内核的差别", "engine", "内核与搜索引擎"),
    // 外观与字体 (appearance)
    SettingsIndexEntry("界面主题 · 跟随系统", "appearance", "外观与字体"),
    SettingsIndexEntry("界面主题 · 浅色", "appearance", "外观与字体"),
    SettingsIndexEntry("界面主题 · 深色", "appearance", "外观与字体"),
    SettingsIndexEntry("文字大小 · 小（85%）", "appearance", "外观与字体"),
    SettingsIndexEntry("文字大小 · 标准（100%）", "appearance", "外观与字体"),
    SettingsIndexEntry("文字大小 · 大（115%）", "appearance", "外观与字体"),
    SettingsIndexEntry("文字大小 · 特大（130%）", "appearance", "外观与字体"),
    SettingsIndexEntry("网页深色模式", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读字号 · 小（15px）", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读字号 · 标准（17px）", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读字号 · 大（19px）", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读字号 · 特大（21px）", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读背景 · 跟随应用", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读背景 · 浅色", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读背景 · 米色护眼", "appearance", "外观与字体"),
    SettingsIndexEntry("阅读背景 · 深色", "appearance", "外观与字体"),
    // 隐私与数据 (privacy)
    SettingsIndexEntry("启动时恢复上次的标签页", "privacy", "隐私与数据"),
    SettingsIndexEntry("历史记录 · 永久保留", "privacy", "隐私与数据"),
    SettingsIndexEntry("历史记录 · 保留 30 天", "privacy", "隐私与数据"),
    SettingsIndexEntry("历史记录 · 保留 7 天", "privacy", "隐私与数据"),
    SettingsIndexEntry("优先用 HTTPS 打开网站", "privacy", "隐私与数据"),
    SettingsIndexEntry("允许第三方 Cookie（系统 WebView 标签）", "privacy", "隐私与数据"),
    SettingsIndexEntry("无图模式（系统 WebView 标签）", "privacy", "隐私与数据"),
    SettingsIndexEntry("禁用 JavaScript（两个内核）", "privacy", "隐私与数据"),
    SettingsIndexEntry("清除浏览数据（可选细项）", "privacy", "隐私与数据"),
    SettingsIndexEntry("清除历史记录", "privacy", "隐私与数据"),
    SettingsIndexEntry("导出备份到下载文件夹", "privacy", "隐私与数据"),
    SettingsIndexEntry("从备份文件导入", "privacy", "隐私与数据"),
    // 下载 (download)
    SettingsIndexEntry("系统下载", "download", "下载"),
    SettingsIndexEntry("TuxunDown 多线程下载", "download", "下载"),
    SettingsIndexEntry("每次下载时询问", "download", "下载"),
    // 广告拦截 (adblock)
    SettingsIndexEntry("拦截广告和跟踪请求", "adblock", "广告拦截"),
    SettingsIndexEntry("隐藏页面广告位", "adblock", "广告拦截"),
    SettingsIndexEntry("清零拦截统计", "adblock", "广告拦截"),
    SettingsIndexEntry("最近 7 天", "adblock", "广告拦截"),
    // 关于 (about)
    SettingsIndexEntry("检查更新", "about", "关于"),
    SettingsIndexEntry("诊断信息", "about", "关于"),
    SettingsIndexEntry("复制诊断信息", "about", "关于"),
    SettingsIndexEntry("关于 TuxunBrowse", "about", "关于")
)

/**
 * Settings-home search (v0.71.0): the entries whose shown name
 * contains the query, case-insensitively, in index order. A blank
 * query matches nothing — the home shows its category list then,
 * not the whole index.
 */
fun filterSettingsIndex(
    index: List<SettingsIndexEntry>,
    query: String
): List<SettingsIndexEntry> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return index.filter { it.name.contains(q, ignoreCase = true) }
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

    /**
     * Bookmarks-page display sort (v0.63.0); see [BookmarkSortOrder].
     * Display preference only — the stored bookmark list itself is
     * never reordered.
     */
    var bookmarkSortOrder: BookmarkSortOrder
        get() = BookmarkSortOrder.fromId(safeString(KEY_BOOKMARK_SORT_ORDER, null))
        set(value) = prefs.edit().putString(KEY_BOOKMARK_SORT_ORDER, value.id).apply()

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

    /**
     * Online search suggestions (v0.50.0): when on, what the user
     * types in the address bar is sent to the current search engine
     * to fetch suggestion words. Default OFF — like the manual
     * update check (v0.18.0), nothing in this app touches the
     * network for suggestions until the user opts in. Incognito
     * tabs never fetch, whatever this switch says, and custom
     * search engines have no suggestion endpoint at all.
     */
    var searchSuggestionsEnabled: Boolean
        get() = safeBoolean(KEY_SEARCH_SUGGESTIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_SEARCH_SUGGESTIONS, value).apply()

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

    // Per-day blocked counts (v0.70.0): same increment point and
    // same throttled flush as the lifetime counter above — the day
    // map rides along in the same preferences write, never a write
    // of its own. Only the most recent BLOCKED_DAILY_KEEP_DAYS days
    // are kept; older days are pruned whenever the map is loaded.
    private var blockedDailyMemory: MutableMap<String, Long>? = null

    /** Per-day blocked counts (yyyy-MM-dd -> count), pruned to the kept window. */
    fun blockedDailyCounts(): Map<String, Long> = loadBlockedDaily()

    private fun loadBlockedDaily(): MutableMap<String, Long> {
        blockedDailyMemory?.let { return it }
        val loaded = mutableMapOf<String, Long>()
        val raw = safeString(KEY_BLOCKED_DAILY, null)
        if (raw != null) {
            try {
                val obj = JSONObject(raw)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    loaded[key] = obj.optLong(key, 0L)
                }
            } catch (e: Exception) {
                // Corrupt payload: start over rather than crash.
            }
        }
        val pruned = pruneBlockedDaily(loaded, java.time.LocalDate.now())
            .toMutableMap()
        blockedDailyMemory = pruned
        return pruned
    }

    /** Count one more blocked request (same call path as the per-page count). */
    fun noteBlocked() {
        val now = System.currentTimeMillis()
        val updated = totalBlocked + 1
        totalBlockedMemory = updated
        val daily = loadBlockedDaily()
        val todayKey = blockedDayKey(java.time.LocalDate.now())
        daily[todayKey] = (daily[todayKey] ?: 0L) + 1
        blockedPendingFlush++
        if (blockedPendingFlush >= BLOCKED_FLUSH_EVERY ||
            now - blockedLastFlushAt >= BLOCKED_FLUSH_MS
        ) {
            blockedPendingFlush = 0
            blockedLastFlushAt = now
            val dailyObj = JSONObject()
            daily.forEach { (day, count) -> dailyObj.put(day, count) }
            prefs.edit()
                .putLong(KEY_TOTAL_BLOCKED, updated)
                .putString(KEY_BLOCKED_DAILY, dailyObj.toString())
                .apply()
        }
    }

    /**
     * Restore the blocked counters from a backup (v0.74.0): the
     * lifetime total and the per-day map are replaced by the backed-
     * up values (a restore means "bring back what was backed up",
     * the same overwrite treatment the settings slice gets — the
     * lists merge, settings and statistics overwrite). The daily
     * map is pruned to the kept window on the way in, exactly like
     * a normal load, and both memory caches are updated so readers
     * see the restored values at once.
     */
    fun restoreBlockedStats(total: Long, daily: Map<String, Long>) {
        val cleanTotal = total.coerceAtLeast(0L)
        val cleanDaily = pruneBlockedDaily(
            daily.filterValues { it > 0 }, java.time.LocalDate.now()
        )
        totalBlockedMemory = cleanTotal
        blockedDailyMemory = cleanDaily.toMutableMap()
        blockedPendingFlush = 0
        blockedLastFlushAt = System.currentTimeMillis()
        val editor = prefs.edit().putLong(KEY_TOTAL_BLOCKED, cleanTotal)
        if (cleanDaily.isEmpty()) {
            editor.remove(KEY_BLOCKED_DAILY)
        } else {
            val dailyObj = JSONObject()
            cleanDaily.forEach { (day, count) -> dailyObj.put(day, count) }
            editor.putString(KEY_BLOCKED_DAILY, dailyObj.toString())
        }
        editor.apply()
    }

    /** Reset the lifetime blocked counter and the per-day counts, effective at once. */
    fun resetTotalBlocked() {
        totalBlockedMemory = 0L
        blockedDailyMemory = mutableMapOf()
        blockedPendingFlush = 0
        blockedLastFlushAt = System.currentTimeMillis()
        prefs.edit()
            .putLong(KEY_TOTAL_BLOCKED, 0L)
            .remove(KEY_BLOCKED_DAILY)
            .apply()
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

    // Desktop-mode sites (v0.56.0): hosts the user switched to the
    // desktop site view on. Stored exactly like the compat list —
    // a JSON array of plain host names — and matched with the same
    // shared hostMatchesSite rule.
    fun loadDesktopSites(): MutableSet<String> {
        val raw = safeString(KEY_DESKTOP_SITES, null) ?: return mutableSetOf()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }.toMutableSet()
        } catch (e: Exception) {
            mutableSetOf()
        }
    }

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

    // Reader-mode sites (v0.73.0): hosts the user asked to always
    // open in reader mode. Stored exactly like the compat/desktop
    // lists — a JSON array of plain host names — and matched with
    // the same shared hostMatchesSite rule (see isReaderHost).
    fun loadReaderSites(): MutableSet<String> {
        val raw = safeString(KEY_READER_SITES, null) ?: return mutableSetOf()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }.toMutableSet()
        } catch (e: Exception) {
            mutableSetOf()
        }
    }

    fun addReaderSite(domain: String) {
        val sites = loadReaderSites()
        if (sites.add(domain)) saveReaderSites(sites)
    }

    fun removeReaderSite(domain: String) {
        val sites = loadReaderSites()
        if (sites.remove(domain)) saveReaderSites(sites)
    }

    private fun saveReaderSites(sites: Set<String>) {
        val arr = JSONArray()
        sites.forEach { arr.put(it) }
        prefs.edit().putString(KEY_READER_SITES, arr.toString()).apply()
    }

    fun loadBookmarks(): MutableList<Bookmark> = readList(KEY_BOOKMARKS) {
        // folder (v0.72.0): optString yields "" when the key is
        // absent, so archives written before folders existed load
        // as uncategorized — no migration, no data loss.
        Bookmark(
            it.optString("title"), it.optString("url"),
            it.optBoolean("pinned"), it.optString("folder").trim()
        )
    }.toMutableList()

    fun saveBookmarks(list: List<Bookmark>) = writeList(KEY_BOOKMARKS, list) {
        JSONObject().put("title", it.title).put("url", it.url)
            .put("pinned", it.pinned).put("folder", it.folder.trim())
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
    fun loadBookmarkFolders(): List<String> {
        val raw = safeString(KEY_BOOKMARK_FOLDERS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length())
                .map { arr.optString(it).trim() }
                .filter { it.isNotEmpty() }
                .distinct()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveBookmarkFolders(list: List<String>) {
        val arr = JSONArray()
        list.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
            .forEach { arr.put(it) }
        prefs.edit().putString(KEY_BOOKMARK_FOLDERS, arr.toString()).apply()
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

    fun loadGeckoDownloads(): MutableList<GeckoDownloadRecord> =
        readList(KEY_GECKO_DOWNLOADS) {
            GeckoDownloadRecord(
                it.optString("name"),
                it.optString("url"),
                it.optLong("size"),
                it.optString("mime").ifBlank { null },
                it.optString("uri").ifBlank { null },
                it.optString("path").ifBlank { null },
                it.optLong("time")
            )
        }.toMutableList()

    fun saveGeckoDownloads(list: List<GeckoDownloadRecord>) =
        writeList(KEY_GECKO_DOWNLOADS, list) {
            JSONObject()
                .put("name", it.fileName)
                .put("url", it.sourceUrl)
                .put("size", it.sizeBytes)
                .put("mime", it.mimeType ?: "")
                .put("uri", it.contentUri ?: "")
                .put("path", it.filePath ?: "")
                .put("time", it.time)
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
        private const val KEY_HTTPS_FIRST = "https_first"
        private const val KEY_AD_HIDE_SLOTS = "ad_hide_slots"
        private const val KEY_ALLOW_THIRD_PARTY_COOKIES = "allow_third_party_cookies"
        private const val KEY_BLOCK_IMAGES = "block_images"
        private const val KEY_DISABLE_JAVASCRIPT = "disable_javascript"
        private const val KEY_SEARCH_SUGGESTIONS = "search_suggestions"
        private const val KEY_COMPAT_SITES = "compat_sites"
        private const val KEY_DESKTOP_SITES = "desktop_sites"
        private const val KEY_READER_SITES = "reader_sites"
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_BOOKMARK_FOLDERS = "bookmark_folders"
        private const val KEY_BOOKMARK_SORT_ORDER = "bookmark_sort_order"
        private const val KEY_HISTORY = "history"
        private const val KEY_READING_LIST = "reading_list"
        private const val KEY_GECKO_DOWNLOADS = "gecko_downloads"
        private const val KEY_TABS = "saved_tabs"
        private const val KEY_TEXT_ZOOM = "text_zoom"
        private const val KEY_READER_FONT_SIZE = "reader_font_size"
        private const val KEY_READER_BACKGROUND = "reader_background"
        private const val KEY_WEB_DARK = "web_dark"
        private const val KEY_STARTUP_RESTORE = "startup_restore"
        private const val KEY_DOWNLOAD_MODE = "download_mode"
        private const val KEY_HISTORY_RETENTION = "history_retention"
        private const val KEY_SPEED_DIALS = "speed_dials"
        private const val KEY_TOTAL_BLOCKED = "total_blocked"
        private const val KEY_BLOCKED_DAILY = "blocked_daily"
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
    val customSearchName: String? = null,
    val customSearchTemplate: String? = null,
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
    val adHideSlots: Boolean? = null,
    val webDarkMode: Boolean? = null,
    val allowThirdPartyCookies: Boolean? = null,
    val blockImages: Boolean? = null,
    val disableJavaScript: Boolean? = null,
    val searchSuggestionsEnabled: Boolean? = null,
    // Added in v0.58.0: reader-mode typesetting. Same optional-on-read
    // treatment as the v0.51.0 additions.
    val readerFontSize: ReaderFontSize? = null,
    val readerBackground: ReaderBackground? = null,
    // Added in v0.63.0: bookmarks-page display sort. Same
    // optional-on-read treatment as the v0.51.0 additions.
    val bookmarkSortOrder: BookmarkSortOrder? = null,
    // Added in v0.77.0: HTTPS-first switch. Same optional-on-read
    // treatment as the v0.51.0 additions.
    val httpsFirstEnabled: Boolean? = null
) {
    /** Plain-language list of what applying these settings would overwrite. */
    fun overwriteSummary(): List<String> {
        val out = mutableListOf<String>()
        if (mainEngine != null) out.add("主内核（默认用哪个内核打开网页）")
        if (incognitoEngine != null) out.add("无痕内核")
        if (searchEngine != null) out.add("搜索引擎（含自定义搜索引擎的名称和模板）")
        if (adBlockEnabled != null) out.add("广告拦截开关")
        if (adHideSlots != null) out.add("隐藏广告位开关")
        if (downloadMode != null) out.add("下载方式")
        if (historyRetention != null) out.add("历史保留期限")
        if (textZoomPercent != null) out.add("网页文字大小")
        if (startupRestore != null) out.add("启动时恢复标签页的开关")
        if (themeMode != null) out.add("界面主题（跟随系统 / 浅色 / 深色）")
        if (webDarkMode != null) out.add("网页深色模式")
        if (allowThirdPartyCookies != null) out.add("允许第三方 Cookie")
        if (blockImages != null) out.add("无图模式")
        if (disableJavaScript != null) out.add("禁用 JavaScript")
        if (searchSuggestionsEnabled != null) out.add("联网搜索建议开关")
        if (readerFontSize != null) out.add("阅读字号")
        if (readerBackground != null) out.add("阅读背景")
        if (bookmarkSortOrder != null) out.add("书签排序（默认顺序 / 按名称）")
        if (httpsFirstEnabled != null) out.add("优先用 HTTPS 打开网站")
        return out
    }
}

/** A parsed full-backup file: the four lists plus the settings slice. */
data class BackupData(
    val bookmarks: List<Bookmark>,
    val history: List<HistoryItem>,
    val readingList: List<ReadingItem>,
    val speedDials: List<SpeedDial>,
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
     * Per-site reader-mode list (v0.73.0). Same treatment as the
     * compat/desktop lists: merges (union) on import. Empty when
     * the backup predates v0.73.0. Purely additive optional field,
     * so the backup format version stays 1 (the v0.51.0 rule).
     */
    val readerSites: List<String> = emptyList(),
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
    val totalBlocked: Long? = null,
    /**
     * Per-day blocked counts (v0.74.0), yyyy-MM-dd -> count.
     * Pruned to the kept window at parse time. Empty when the
     * backup predates v0.74.0 or carries no daily counts.
     */
    val blockedDaily: Map<String, Long> = emptyMap()
)

/** Serialize a full backup (lists + main settings) to one JSON text. */
fun backupToJson(
    bookmarks: List<Bookmark>,
    history: List<HistoryItem>,
    readingList: List<ReadingItem>,
    speedDials: List<SpeedDial>,
    settings: BackupSettings,
    compatSites: List<String> = emptyList(),
    desktopSites: List<String> = emptyList(),
    bookmarkFolders: List<String> = emptyList(),
    readerSites: List<String> = emptyList(),
    totalBlocked: Long? = null,
    blockedDaily: Map<String, Long> = emptyMap()
): String {
    val bookmarkArr = JSONArray()
    bookmarks.forEach {
        bookmarkArr.put(
            JSONObject().put("title", it.title).put("url", it.url)
                .put("pinned", it.pinned).put("folder", it.folder.trim())
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
    settings.themeMode?.let { settingsObj.put("themeMode", it.id) }
    settings.adHideSlots?.let { settingsObj.put("adHideSlots", it) }
    settings.webDarkMode?.let { settingsObj.put("webDarkMode", it) }
    settings.allowThirdPartyCookies?.let {
        settingsObj.put("allowThirdPartyCookies", it)
    }
    settings.blockImages?.let { settingsObj.put("blockImages", it) }
    settings.disableJavaScript?.let { settingsObj.put("disableJavaScript", it) }
    settings.searchSuggestionsEnabled?.let {
        settingsObj.put("searchSuggestionsEnabled", it)
    }
    settings.readerFontSize?.let { settingsObj.put("readerFontSize", it.id) }
    settings.readerBackground?.let {
        settingsObj.put("readerBackground", it.id)
    }
    settings.bookmarkSortOrder?.let {
        settingsObj.put("bookmarkSortOrder", it.id)
    }
    settings.httpsFirstEnabled?.let {
        settingsObj.put("httpsFirstEnabled", it)
    }
    val compatArr = JSONArray()
    compatSites.forEach { compatArr.put(it) }
    val desktopArr = JSONArray()
    desktopSites.forEach { desktopArr.put(it) }
    val readerArr = JSONArray()
    readerSites.forEach { readerArr.put(it) }
    val folderArr = JSONArray()
    bookmarkFolders.map { it.trim() }.filter { it.isNotEmpty() }
        .distinct().forEach { folderArr.put(it) }
    val root = JSONObject()
        .put("format", BACKUP_FORMAT)
        .put("version", BACKUP_FORMAT_VERSION)
        .put("bookmarks", bookmarkArr)
        .put("history", historyArr)
        .put("readingList", readingArr)
        .put("speedDials", dialArr)
        .put("compatSites", compatArr)
        .put("desktopSites", desktopArr)
        .put("readerSites", readerArr)
        .put("bookmarkFolders", folderArr)
        .put("settings", settingsObj)
    // Blocked statistics (v0.74.0): written only when the caller
    // supplies a total, as one "blockedStats" object. Purely
    // additive — older app versions reading this file simply
    // ignore the unknown key, and the format version stays 1.
    totalBlocked?.let { total ->
        val dailyObj = JSONObject()
        blockedDaily.forEach { (day, count) -> dailyObj.put(day, count) }
        root.put(
            "blockedStats",
            JSONObject().put("total", total).put("daily", dailyObj)
        )
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
            },
            themeMode = s.optString("themeMode")
                .takeIf { it.isNotBlank() }?.let { ThemeMode.fromId(it) },
            adHideSlots = if (s.has("adHideSlots")) {
                s.optBoolean("adHideSlots")
            } else {
                null
            },
            webDarkMode = if (s.has("webDarkMode")) {
                s.optBoolean("webDarkMode")
            } else {
                null
            },
            allowThirdPartyCookies = if (s.has("allowThirdPartyCookies")) {
                s.optBoolean("allowThirdPartyCookies")
            } else {
                null
            },
            blockImages = if (s.has("blockImages")) {
                s.optBoolean("blockImages")
            } else {
                null
            },
            disableJavaScript = if (s.has("disableJavaScript")) {
                s.optBoolean("disableJavaScript")
            } else {
                null
            },
            searchSuggestionsEnabled = if (s.has("searchSuggestionsEnabled")) {
                s.optBoolean("searchSuggestionsEnabled")
            } else {
                null
            },
            readerFontSize = s.optString("readerFontSize")
                .takeIf { it.isNotBlank() }?.let { ReaderFontSize.fromId(it) },
            readerBackground = s.optString("readerBackground")
                .takeIf { it.isNotBlank() }?.let { ReaderBackground.fromId(it) },
            bookmarkSortOrder = s.optString("bookmarkSortOrder")
                .takeIf { it.isNotBlank() }?.let { BookmarkSortOrder.fromId(it) },
            httpsFirstEnabled = if (s.has("httpsFirstEnabled")) {
                s.optBoolean("httpsFirstEnabled")
            } else {
                null
            }
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
            val host = compatArr.optString(i).trim().lowercase()
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
            val host = desktopArr.optString(i).trim().lowercase()
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
            val name = folderArr.optString(i).trim()
            name.takeIf { it.isNotEmpty() && it.length <= 60 }
        }.distinct()
    }
    // Reader-mode sites (v0.73.0): same plain-host-name guard as
    // the desktop list above. Absent in older backups: empty list.
    val readerArr = obj.optJSONArray("readerSites")
    val readerSites = if (readerArr == null) {
        emptyList()
    } else {
        val count = minOf(readerArr.length(), MAX_IMPORT_ENTRIES)
        (0 until count).mapNotNull { i ->
            val host = readerArr.optString(i).trim().lowercase()
            host.takeIf {
                it.isNotEmpty() && it.length <= 253 &&
                    !it.contains("://") && !it.contains("/") &&
                    !it.contains(":") && it.none { c -> c.isWhitespace() }
            }
        }.distinct()
    }
    // Blocked statistics (v0.74.0): absent in older backups — the
    // total stays null (import then leaves the current counters
    // alone) and the daily map stays empty. A present daily map is
    // pruned to the kept window here, so a stale or hand-crafted
    // file cannot resurrect long-gone days.
    val statsObj = obj.optJSONObject("blockedStats")
    val parsedTotalBlocked: Long? =
        if (statsObj != null && statsObj.has("total")) {
            statsObj.optLong("total").coerceAtLeast(0L)
        } else {
            null
        }
    val parsedBlockedDaily: Map<String, Long> = run {
        val dailyObj = statsObj?.optJSONObject("daily")
        if (dailyObj == null) {
            emptyMap()
        } else {
            val loaded = mutableMapOf<String, Long>()
            val keys = dailyObj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val count = dailyObj.optLong(key, 0L)
                if (count > 0) loaded[key] = count
            }
            pruneBlockedDaily(loaded, java.time.LocalDate.now())
        }
    }
    return BackupData(
        bookmarks, history, reading, dials, settings, compatSites,
        desktopSites, bookmarkFolders, readerSites,
        totalBlocked = parsedTotalBlocked,
        blockedDaily = parsedBlockedDaily
    )
}

/** Serialize bookmarks for export; the format is a plain JSON object. */
fun bookmarksToJson(list: List<Bookmark>): String {
    val arr = JSONArray()
    list.forEach {
        arr.put(
            JSONObject().put("title", it.title).put("url", it.url)
                .put("pinned", it.pinned).put("folder", it.folder.trim())
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
            // folder (v0.72.0): absent in exports written before
            // folders existed — "" = uncategorized, as everywhere.
            Bookmark(
                o.optString("title").ifBlank { url }, url,
                o.optBoolean("pinned"), o.optString("folder").trim()
            )
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
