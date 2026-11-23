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

data class Bookmark(val title: String, val url: String)
data class HistoryItem(val title: String, val url: String, val time: Long)

/** Local persistence: engine choices, bookmarks and history in SharedPreferences. */
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

    fun loadBookmarks(): MutableList<Bookmark> = readList(KEY_BOOKMARKS) {
        Bookmark(it.optString("title"), it.optString("url"))
    }.toMutableList()

    fun saveBookmarks(list: List<Bookmark>) = writeList(KEY_BOOKMARKS, list) {
        JSONObject().put("title", it.title).put("url", it.url)
    }

    fun loadHistory(): MutableList<HistoryItem> = readList(KEY_HISTORY) {
        HistoryItem(it.optString("title"), it.optString("url"), it.optLong("time"))
    }.toMutableList()

    fun saveHistory(list: List<HistoryItem>) = writeList(KEY_HISTORY, list) {
        JSONObject().put("title", it.title).put("url", it.url).put("time", it.time)
    }

    fun clearHistory() = prefs.edit().remove(KEY_HISTORY).apply()

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
        private const val KEY_BOOKMARKS = "bookmarks"
        private const val KEY_HISTORY = "history"
    }
}

/** Turn whatever the user typed into a URL: full URL, bare host, or a search. */
fun resolveInput(input: String): String {
    val text = input.trim()
    if (text.isEmpty()) return "about:blank"
    if (text.startsWith("http://") || text.startsWith("https://") ||
        text.startsWith("about:") || text.startsWith("file:")
    ) return text
    val looksLikeHost = !text.contains(" ") &&
        (text.contains(".") || text.startsWith("localhost"))
    return if (looksLikeHost) {
        "https://$text"
    } else {
        "https://duckduckgo.com/?q=" + java.net.URLEncoder.encode(text, "UTF-8")
    }
}
