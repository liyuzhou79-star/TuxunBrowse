package com.tuxunbrowse.android.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

enum class Engine(val id: String, val label: String) {
    GECKO("gecko", "火狐 GeckoView"),
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

    val mainEngineChosen: Boolean
        get() = prefs.contains(KEY_MAIN_ENGINE)

    var mainEngine: Engine
        get() = Engine.fromId(prefs.getString(KEY_MAIN_ENGINE, null))
        set(value) = prefs.edit().putString(KEY_MAIN_ENGINE, value.id).apply()

    var incognitoEngine: Engine
        get() = Engine.fromId(prefs.getString(KEY_INCOGNITO_ENGINE, Engine.GECKO.id))
        set(value) = prefs.edit().putString(KEY_INCOGNITO_ENGINE, value.id).apply()

    val incognitoEngineChosen: Boolean
        get() = prefs.contains(KEY_INCOGNITO_ENGINE)

    var searchEngine: SearchEngine
        get() = SearchEngine.fromId(prefs.getString(KEY_SEARCH_ENGINE, null))
        set(value) = prefs.edit().putString(KEY_SEARCH_ENGINE, value.id).apply()

    /** Display name of the user's custom search engine (CUSTOM only). */
    var customSearchName: String
        get() = prefs.getString(KEY_CUSTOM_SEARCH_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SEARCH_NAME, value).apply()

    /** URL template of the custom search engine; must contain %s. */
    var customSearchTemplate: String
        get() = prefs.getString(KEY_CUSTOM_SEARCH_TEMPLATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SEARCH_TEMPLATE, value).apply()

    var adBlockEnabled: Boolean
        get() = prefs.getBoolean(KEY_AD_BLOCK, true)
        set(value) = prefs.edit().putBoolean(KEY_AD_BLOCK, value).apply()

    /** Web page text size in percent: 85 / 100 / 115 / 130. */
    var textZoomPercent: Int
        get() = prefs.getInt(KEY_TEXT_ZOOM, 100)
        set(value) = prefs.edit().putInt(KEY_TEXT_ZOOM, value).apply()

    /** Ask web pages to render dark (WebView algorithmic darkening). */
    var webDarkMode: Boolean
        get() = prefs.getBoolean(KEY_WEB_DARK, false)
        set(value) = prefs.edit().putBoolean(KEY_WEB_DARK, value).apply()

    /** Restore the previous session's tabs on launch (vs always home). */
    var startupRestore: Boolean
        get() = prefs.getBoolean(KEY_STARTUP_RESTORE, true)
        set(value) = prefs.edit().putBoolean(KEY_STARTUP_RESTORE, value).apply()

    /** Where URL-type downloads go. Gecko tab downloads never use this. */
    var downloadMode: DownloadMode
        get() = DownloadMode.fromId(prefs.getString(KEY_DOWNLOAD_MODE, null))
        set(value) = prefs.edit().putString(KEY_DOWNLOAD_MODE, value.id).apply()

    /** How long normal history entries are kept before cleanup on launch. */
    var historyRetention: HistoryRetention
        get() = HistoryRetention.fromId(prefs.getString(KEY_HISTORY_RETENTION, null))
        set(value) = prefs.edit().putString(KEY_HISTORY_RETENTION, value.id).apply()

    fun loadCompatSites(): MutableSet<String> {
        val raw = prefs.getString(KEY_COMPAT_SITES, null) ?: return mutableSetOf()
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
        val raw = prefs.getString(KEY_TABS, null) ?: return emptyList<SavedTab>() to 0
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
        val raw = prefs.getString(key, null) ?: return emptyList()
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
        private const val KEY_CUSTOM_SEARCH_NAME = "custom_search_name"
        private const val KEY_CUSTOM_SEARCH_TEMPLATE = "custom_search_template"
        private const val KEY_AD_BLOCK = "ad_block"
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
    }
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
    return (0 until arr.length()).mapNotNull {
        val o = arr.optJSONObject(it) ?: return@mapNotNull null
        val url = o.optString("url")
        if (url.isBlank()) {
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
 * skipped because their address was blank or not http/https/file.
 * De-duplication against existing bookmarks is the caller's job.
 */
fun bookmarksFromHtml(raw: String): Pair<List<Bookmark>, Int> {
    val out = mutableListOf<Bookmark>()
    var skipped = 0
    for (match in HTML_BOOKMARK_REGEX.findAll(raw)) {
        val href = decodeHtmlEntities(match.groupValues[1].trim())
        val scheme = href.substringBefore("://", "").lowercase()
        val isFile = href.startsWith("file:", ignoreCase = true)
        if (href.isBlank() || (!isFile && scheme != "http" && scheme != "https")) {
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
    if (text.startsWith("http://") || text.startsWith("https://") ||
        text.startsWith("about:") || text.startsWith("file:")
    ) return text
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
    if (text.startsWith("http://") || text.startsWith("https://") ||
        text.startsWith("about:") || text.startsWith("file:")
    ) return true
    return !text.contains(" ") &&
        (text.contains(".") || text.startsWith("localhost"))
}
