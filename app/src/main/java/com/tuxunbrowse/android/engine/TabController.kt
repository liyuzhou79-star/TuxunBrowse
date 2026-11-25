package com.tuxunbrowse.android.engine

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.HttpAuthHandler
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.Composable
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.webkit.ScriptHandler
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.GeckoDownloadRecord
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.ReaderBackground
import com.tuxunbrowse.android.data.ReaderFontSize
import com.tuxunbrowse.android.data.ReaderPalette
import com.tuxunbrowse.android.data.readerPaletteFor
import com.tuxunbrowse.android.data.Store
import com.tuxunbrowse.android.data.hostOf
import org.mozilla.geckoview.AllowOrDeny
import org.mozilla.geckoview.ContentBlocking
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSession.PermissionDelegate
import org.mozilla.geckoview.GeckoSession.PromptDelegate
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.StorageController
import org.mozilla.geckoview.WebRequestError
import org.mozilla.geckoview.WebResponse

/** Shared Gecko runtime; created lazily, null when Gecko fails to start. */
object GeckoHolder {
    var runtime: GeckoRuntime? = null
        private set
    var available: Boolean = true
        private set

    /**
     * Create the runtime if it does not exist yet. Synchronized
     * since v0.71.0: the startup warm-up calls this from a
     * background thread while a first Firefox tab could be asking
     * for the same runtime on the main thread, and two racing
     * creations must never happen. A failed creation leaves
     * [runtime] null, so the next call simply tries again — that
     * retry is the fallback path a failed warm-up relies on.
     */
    @Synchronized
    fun ensure(context: Context): GeckoRuntime? {
        if (runtime != null) return runtime
        return try {
            runtime = GeckoRuntime.create(context.applicationContext)
            // Firefox-side tracker protection and privacy signals,
            // honouring the user's saved choices (v0.92.0).
            applyPrivacySettings(context)
            // Apply the user's saved text size (Store key "text_zoom").
            try {
                val zoom = context.getSharedPreferences(
                    "tuxunbrowse", Context.MODE_PRIVATE
                ).getInt("text_zoom", 100)
                if (zoom != 100) applyFontSizeFactor(zoom / 100f)
            } catch (t: Throwable) {
                // Text size is best-effort only.
            }
            runtime
        } catch (t: Throwable) {
            available = false
            null
        }
    }

    val hasRuntime: Boolean
        get() = runtime != null

    /**
     * Apply the user's privacy choices to the Gecko runtime
     * (v0.92.0), read straight from the shared preferences — the
     * same direct-prefs pattern as the text-zoom application in
     * [ensure], so it works at runtime creation (background
     * warm-up included) without a Store instance. The settings
     * page calls it again whenever a choice changes; a runtime
     * that does not exist yet picks the values up in [ensure].
     *
     * All three are real GeckoView 132 APIs, verified against the
     * AAR with javap: ContentBlocking.setAntiTracking takes the
     * DEFAULT (46) / STRICT (766) anti-tracking category sets —
     * STRICT adds content trackers, cryptominers, fingerprinters,
     * social-tracking protection and email trackers — and
     * GeckoRuntimeSettings carries setGlobalPrivacyControl and
     * setFingerprintingProtection. There is deliberately no DNT
     * or WebRTC line here: GeckoView 132 exposes neither (also
     * verified), so Gecko tabs cannot honour those two switches.
     */
    fun applyPrivacySettings(context: Context) {
        val rt = runtime ?: return
        try {
            val prefs = context.getSharedPreferences(
                "tuxunbrowse", Context.MODE_PRIVATE
            )
            val strict = prefs.getString("tp_level", null) == "strict"
            val gpc = prefs.getBoolean("gpc", false)
            rt.settings.contentBlocking.setAntiTracking(
                if (strict) {
                    ContentBlocking.AntiTracking.STRICT
                } else {
                    ContentBlocking.AntiTracking.DEFAULT
                }
            )
            rt.settings.setFingerprintingProtection(strict)
            rt.settings.setGlobalPrivacyControl(gpc)
        } catch (t: Throwable) {
            // Privacy settings are best-effort only.
        }
    }

    /** GeckoRuntimeSettings.setFontSizeFactor, verified in the 132 AAR. */
    fun applyFontSizeFactor(factor: Float) {
        try {
            runtime?.settings?.setAutomaticFontSizeAdjustment(false)
            runtime?.settings?.setFontSizeFactor(factor)
        } catch (t: Throwable) {
            // Best-effort only.
        }
    }

    /** StorageController.clearData, verified in the 132 AAR. */
    fun clearSiteData() {
        try {
            runtime?.storageController?.clearData(
                StorageController.ClearFlags.COOKIES or
                    StorageController.ClearFlags.DOM_STORAGES or
                    StorageController.ClearFlags.ALL_CACHES
            )
        } catch (t: Throwable) {
            // Best-effort only.
        }
    }

    /**
     * StorageController.clearDataFromHost(String, long), verified against
     * the GeckoView 132 AAR with javap. Returns false when the Gecko
     * runtime has not started yet (nothing to clear in that case).
     */
    fun clearDataFromHost(host: String): Boolean {
        val rt = runtime ?: return false
        return try {
            rt.storageController.clearDataFromHost(
                host,
                StorageController.ClearFlags.COOKIES or
                    StorageController.ClearFlags.DOM_STORAGES or
                    StorageController.ClearFlags.ALL_CACHES
            )
            true
        } catch (t: Throwable) {
            false
        }
    }
}

/** Kinds of JavaScript dialogs a Gecko page can raise. */
enum class GeckoJsPromptKind { ALERT, CONFIRM, TEXT }

/**
 * One HTTP Basic-auth challenge, normalised across both engines so
 * the app can answer it with a single dialog. Credentials travel
 * only through the answer callback — they are never stored, logged
 * or written anywhere. [message] is Gecko's own prompt text (it
 * carries the realm wording); WebView supplies [host] and [realm]
 * separately and leaves [message] empty. [onlyPassword] is set when
 * the site already knows the user name (Gecko's ONLY_PASSWORD flag).
 */
data class HttpAuthRequestInfo(
    val host: String,
    val realm: String,
    val message: String,
    val prefillUsername: String,
    val onlyPassword: Boolean
)

/**
 * A text-selection action request from a Gecko page (v0.43.0).
 * Wraps GeckoView 132's Selection object, whose shape was verified
 * against the AAR with javap: SelectionActionDelegate is an
 * interface with default methods; onShowActionRequest(session,
 * Selection) hands the app a Selection carrying public final
 * text / flags / screenRect / availableActions plus execute(String)
 * and convenience methods (copy(), selectAll(), hide(), ...). The
 * ACTION_* ids are plain Strings ("org.mozilla.geckoview.COPY" and
 * friends); FLAG_IS_COLLAPSED=1, FLAG_IS_EDITABLE=2,
 * FLAG_IS_PASSWORD=4 (constant pool read directly — the javap
 * display masked the password value). The app presents the actions
 * itself (Material 3 dialog) and answers through [execute]/[hide];
 * Gecko fires onHideAction when the selection goes away, which the
 * tab forwards so the app can drop this object.
 */
class GeckoSelectionMenu(
    private val selection: GeckoSession.SelectionActionDelegate.Selection
) {
    /** The selected text, straight from the Selection object. */
    val text: String = selection.text.orEmpty()

    val isEditable: Boolean =
        selection.flags and
            GeckoSession.SelectionActionDelegate.FLAG_IS_EDITABLE != 0

    /** Password-field selection: its text must never be previewed,
     * shared or sent to a search engine. */
    val isPassword: Boolean =
        selection.flags and
            GeckoSession.SelectionActionDelegate.FLAG_IS_PASSWORD != 0

    fun isAvailable(action: String): Boolean = try {
        selection.isActionAvailable(action)
    } catch (t: Throwable) {
        false
    }

    /** Run one ACTION_* id in the page (copy/cut/delete/select-all). */
    fun execute(action: String) {
        try {
            selection.execute(action)
        } catch (t: Throwable) {
            // A stale selection must never crash the app.
        }
    }

    /** Finish the selection on the Gecko side (ACTION_HIDE). */
    fun hide() {
        try {
            selection.hide()
        } catch (t: Throwable) {
            // Already gone — nothing to finish.
        }
    }
}

/**
 * One pending Gecko clipboard-read permission answer (v0.45.0).
 * Wraps the GeckoResult<AllowOrDeny> handed back from
 * SelectionActionDelegate.onShowClipboardPermissionRequest, whose
 * shape was verified against the GeckoView 132 AAR with javap: the
 * second parameter is a ClipboardPermission object with public final
 * uri (String, read from the message bundle's "uri" key — the JS
 * request itself only carries screenPoint, so it can be blank),
 * type (int, hard-wired to PERMISSION_CLIPBOARD_READ = 1 by the
 * bundle constructor) and screenPoint (Point). There is NO clipboard
 * content or "text available" field — the app never sees what is on
 * the clipboard, it only answers allow/deny. AllowOrDeny is an enum
 * with exactly ALLOW / DENY; GeckoResult answers asynchronously via
 * complete(value) (completeExceptionally also exists but a plain
 * DENY value is the honest refusal). The interface's default
 * implementation returns GeckoResult.deny() — before v0.45.0 the
 * request was silently denied with no prompt at all. Invoking this
 * answer more than once is a no-op, so every exit path (allow, deny,
 * dismiss, tab death) can call it without coordination.
 */
private class ClipboardPermissionAnswer(
    private val result: GeckoResult<AllowOrDeny>,
    private val onAnswered: (ClipboardPermissionAnswer) -> Unit
) : (Boolean) -> Unit {
    private var done = false

    override fun invoke(allow: Boolean) {
        if (done) return
        done = true
        onAnswered(this)
        try {
            result.complete(
                if (allow) AllowOrDeny.ALLOW else AllowOrDeny.DENY
            )
        } catch (t: Throwable) {
            // Already answered on the Gecko side; never crash.
        }
    }
}

/** Plain-language description of a WebView SSL error's primary problem. */
/**
 * Pull the common name (CN) out of an X.500 distinguished name such
 * as "CN=example.com, O=Some Org, C=US". Falls back to the whole DN
 * when there is no CN part; null/blank stays empty.
 */
private fun certCommonName(dn: String?): String {
    if (dn.isNullOrBlank()) return ""
    val cn = dn.split(",").map { it.trim() }
        .firstOrNull { it.startsWith("CN=", ignoreCase = true) }
        ?.substringAfter("=")?.trim()
    return cn?.ifBlank { dn.trim() } ?: dn.trim()
}

private fun sslErrorDescription(primaryError: Int): String = when (primaryError) {
    android.net.http.SslError.SSL_EXPIRED -> "这个网站的证书已经过期了"
    android.net.http.SslError.SSL_NOTYETVALID -> "这个网站的证书还没到生效时间"
    android.net.http.SslError.SSL_IDMISMATCH ->
        "证书上的网站名和你正在打开的网站对不上"
    android.net.http.SslError.SSL_UNTRUSTED -> "这个证书不是受信任的机构签发的"
    android.net.http.SslError.SSL_DATE_INVALID -> "这个证书的日期不对"
    android.net.http.SslError.SSL_INVALID -> "这个证书本身无效"
    else -> "这个网站的证书有问题"
}

/**
 * A network-level load failure for one tab (offline, unknown host,
 * refused connection, ...). HTTP errors such as 404/500 do not land
 * here — the server's own page is shown for those. The app renders
 * this in its own error UI instead of the engine's default blank or
 * built-in error page.
 */
data class TabLoadError(
    val failedUrl: String,
    val reason: String,
    val detail: String
)

/** Actions the engine-unavailable fallback page can offer (v0.81.0). */
enum class EngineFallbackAction { SWITCH_ENGINE, RETRY, GO_HOME }

/**
 * Which actions the engine-unavailable fallback page shows
 * (v0.81.0, pure so it is unit-tested): retrying the same engine
 * and going home always make sense; "open this site in the other
 * engine" only makes sense when there IS a site — an http(s) page.
 * Internal/blank addresses get no switch button, exactly the
 * load-error page's rule since v0.52.0.
 */
fun engineFallbackActions(url: String): List<EngineFallbackAction> {
    val actions = mutableListOf<EngineFallbackAction>()
    if (url.startsWith("http://") || url.startsWith("https://")) {
        actions.add(EngineFallbackAction.SWITCH_ENGINE)
    }
    actions.add(EngineFallbackAction.RETRY)
    actions.add(EngineFallbackAction.GO_HOME)
    return actions
}

/** Label of the fallback page's switch-engine button. */
fun otherEngineOpenLabel(engine: Engine): String = when (engine) {
    Engine.GECKO -> "用系统 WebView 打开这个网站"
    Engine.WEBVIEW -> "用火狐打开这个网站"
}

/** Plain-language reason shown on the engine-unavailable page. */
fun engineFallbackReason(engine: Engine): String = when (engine) {
    Engine.GECKO ->
        "火狐内核这次没能启动起来，网页暂时打不开。" +
            "可以先用系统 WebView 打开这个网站，或者等一下再重试。"
    Engine.WEBVIEW ->
        "这台设备上的系统 WebView 现在用不了（可能被停用、缺失或损坏），" +
            "网页暂时打不开。可以先用火狐打开这个网站，或者等一下再重试。"
}

/**
 * Tallest full-page screenshot, in pixels. A page taller than this
 * is captured only up to the cap and the UI says so honestly.
 */

/**
 * How much of a page's source the source page renders (v0.84.0).
 * The full text is kept whole in memory and is what "复制全文"
 * copies; only the DISPLAY is cut to this many leading characters,
 * with a notice stating the total — a multi-megabyte document
 * rendered as one selectable text node would jank the page.
 */
const val SOURCE_DISPLAY_LIMIT = 200_000

/** The leading part of [source] the source page displays. */
fun truncateSourceForDisplay(
    source: String,
    limit: Int = SOURCE_DISPLAY_LIMIT
): String =
    if (source.length <= limit) source else source.substring(0, limit)

/** True when [source] is longer than what the source page displays. */
fun isSourceTruncated(
    source: String,
    limit: Int = SOURCE_DISPLAY_LIMIT
): Boolean = source.length > limit

/** MIME type of a WebView web archive (.mhtml) file (v0.85.0). */
const val WEB_ARCHIVE_MIME_TYPE = "multipart/related"

/**
 * Cap on the title part of a web-archive file name (v0.85.0), so a
 * page with a paragraph for a title cannot produce an unwieldy (or
 * filesystem-rejected) name; the date suffix and extension are
 * added on top of this cap, not counted inside it.
 */

/** Resource groups the page sniffer reports, in display order. */
enum class SniffKind { FILE, IMAGE, MEDIA }

/** One downloadable resource link found on the current page. */
data class SniffedResource(
    val url: String,
    val name: String,
    val host: String,
    val kind: SniffKind
)

/**
 * Parse the sniffer script's JSON answer into typed resources.
 * Internal (not private) since v0.93.0 so unit tests can pin the
 * contract. The result is deduped by URL: the sniffer script
 * already dedupes, and this is the second line of defence — the
 * sniff screen keys its lazy list and its multi-select set by URL,
 * so a duplicate URL would break both.
 */
internal fun parseSniffResult(raw: String?): List<SniffedResource> {
    if (raw.isNullOrBlank() || raw == "null") return emptyList()
    return try {
        // evaluateJavascript hands back the page's return value JSON
        // encoded, so a JS string arrives as a quoted JSON string.
        val text = org.json.JSONTokener(raw).nextValue() as? String
            ?: return emptyList()
        val arr = org.json.JSONArray(text)
        val out = mutableListOf<SniffedResource>()
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val u = obj.optString("url")
            if (u.isBlank()) continue
            val kind = when (obj.optString("kind")) {
                "image" -> SniffKind.IMAGE
                "media" -> SniffKind.MEDIA
                else -> SniffKind.FILE
            }
            val name = obj.optString("name").ifBlank {
                u.substringBefore("?").substringBefore("#")
                    .substringAfterLast("/").ifBlank { u }
            }
            out.add(SniffedResource(u, name, hostOf(u) ?: "", kind))
        }
        out.distinctBy { it.url }
    } catch (t: Throwable) {
        emptyList()
    }
}

/**
 * Plain-language reason for a WebView main-frame error code.
 * Internal since v0.103.0 so unit tests can pin the mapping: the
 * audit that version found seven of the platform's sixteen codes
 * (auth, proxy auth, IO, file, unsafe resource) falling through to
 * the generic network line even though they are not network
 * problems. Every named code now has its own line; only
 * ERROR_UNKNOWN and future unknown codes reach the fallback.
 */
internal fun webViewErrorReason(errorCode: Int): String = when (errorCode) {
    WebViewClient.ERROR_HOST_LOOKUP ->
        "找不到这个网站，可能是网址写错了，或者现在的网络连不上它"
    WebViewClient.ERROR_CONNECT ->
        "连不上这个网站，请检查网络后再试"
    WebViewClient.ERROR_IO ->
        "数据传到一半断了，请检查网络后重试"
    WebViewClient.ERROR_TIMEOUT ->
        "这个网站半天没回应，连接超时了"
    WebViewClient.ERROR_REDIRECT_LOOP ->
        "这个网站一直在来回跳转，没法打开"
    WebViewClient.ERROR_FAILED_SSL_HANDSHAKE ->
        "和这个网站的安全连接没建起来，没法打开"
    WebViewClient.ERROR_BAD_URL ->
        "这个网址写得不对，打不开"
    WebViewClient.ERROR_UNSUPPORTED_SCHEME ->
        "这种链接本应用打不开"
    WebViewClient.ERROR_UNSUPPORTED_AUTH_SCHEME ->
        "这个网站用的登录方式本应用不支持，没法打开"
    WebViewClient.ERROR_AUTHENTICATION ->
        "这个网站没接受登录信息，没法打开"
    WebViewClient.ERROR_PROXY_AUTHENTICATION ->
        "代理服务器没接受登录信息，没法打开"
    WebViewClient.ERROR_FILE ->
        "读这个文件时出错了，没法打开"
    WebViewClient.ERROR_FILE_NOT_FOUND ->
        "找不到这个文件，它可能已经被删掉或移动了"
    WebViewClient.ERROR_UNSAFE_RESOURCE ->
        "这个网页被安全检查拦下了，没法打开"
    WebViewClient.ERROR_TOO_MANY_REQUESTS ->
        "这个网站说请求太多了，请过一会儿再试"
    else -> "网页没能打开，请检查网络后重试"
}

/**
 * Plain-language reason for a Gecko load error. WebRequestError's
 * code/category constants were verified against the GeckoView 132
 * AAR with javap (the full set: 28 codes, 7 categories). Split
 * into code/category ints since v0.103.0 so unit tests can pin
 * the mapping without constructing a WebRequestError: the audit
 * that version found the content, URI and safe-browsing codes
 * falling through to the generic "check your network" line even
 * though they are not network problems — a page blocked as
 * dangerous, or one that crashed, said the same wrong thing.
 * Every named code now has its own line, and the category
 * fallback covers all seven categories.
 */
internal fun geckoErrorReasonFor(code: Int, category: Int): String = when (code) {
    WebRequestError.ERROR_UNKNOWN_HOST ->
        "找不到这个网站，可能是网址写错了，或者现在的网络连不上它"
    WebRequestError.ERROR_OFFLINE ->
        "现在没有网络连接，请连上网络后再试"
    WebRequestError.ERROR_NET_TIMEOUT ->
        "这个网站半天没回应，连接超时了"
    WebRequestError.ERROR_CONNECTION_REFUSED ->
        "这个网站拒绝了连接，没法打开"
    WebRequestError.ERROR_NET_INTERRUPT, WebRequestError.ERROR_NET_RESET ->
        "网络连接中途断了，请重试"
    WebRequestError.ERROR_UNKNOWN_SOCKET_TYPE ->
        "连不上这个网站，请检查网络后再试"
    WebRequestError.ERROR_PORT_BLOCKED ->
        "这个网址用的端口被拦下了，没法打开"
    WebRequestError.ERROR_SECURITY_SSL, WebRequestError.ERROR_SECURITY_BAD_CERT,
    WebRequestError.ERROR_BAD_HSTS_CERT ->
        "这个网站的安全证书有问题，没法安全打开"
    WebRequestError.ERROR_HTTPS_ONLY ->
        "这个网站只接受安全连接（https），刚才的打开方式不安全，被拦下了"
    WebRequestError.ERROR_MALFORMED_URI ->
        "这个网址写得不对，打不开"
    WebRequestError.ERROR_UNKNOWN_PROTOCOL ->
        "这种链接本应用打不开"
    WebRequestError.ERROR_FILE_NOT_FOUND ->
        "找不到这个文件，它可能已经被删掉或移动了"
    WebRequestError.ERROR_FILE_ACCESS_DENIED ->
        "这个文件不让打开"
    WebRequestError.ERROR_DATA_URI_TOO_LONG ->
        "这个链接太长了，没法打开"
    WebRequestError.ERROR_REDIRECT_LOOP ->
        "这个网站一直在来回跳转，没法打开"
    WebRequestError.ERROR_CONTENT_CRASHED ->
        "这个网页崩溃了，请重新加载"
    WebRequestError.ERROR_CORRUPTED_CONTENT ->
        "这个网站传回来的内容坏了，没法打开"
    WebRequestError.ERROR_UNSAFE_CONTENT_TYPE ->
        "这个网站传回来的内容没法当网页打开"
    WebRequestError.ERROR_INVALID_CONTENT_ENCODING ->
        "这个网站传回来的内容编码不对，没法打开"
    WebRequestError.ERROR_PROXY_CONNECTION_REFUSED ->
        "代理服务器拒绝了连接，网页没能打开"
    WebRequestError.ERROR_UNKNOWN_PROXY_HOST ->
        "找不到代理服务器，网页没能打开"
    WebRequestError.ERROR_SAFEBROWSING_PHISHING_URI ->
        "这个网站被安全名单拦下了：它可能是假冒网站，不要打开"
    WebRequestError.ERROR_SAFEBROWSING_MALWARE_URI ->
        "这个网站被安全名单拦下了：它可能带恶意软件，不要打开"
    WebRequestError.ERROR_SAFEBROWSING_UNWANTED_URI,
    WebRequestError.ERROR_SAFEBROWSING_HARMFUL_URI ->
        "这个网站被安全名单拦下了：它可能有危险，不要打开"
    else -> when (category) {
        WebRequestError.ERROR_CATEGORY_NETWORK ->
            "网络连接有问题，网页没能打开，请检查网络后重试"
        WebRequestError.ERROR_CATEGORY_SECURITY ->
            "这个网站的安全有问题，没法打开"
        WebRequestError.ERROR_CATEGORY_CONTENT ->
            "这个网页的内容有问题，没法打开"
        WebRequestError.ERROR_CATEGORY_URI ->
            "这个网址有问题，没法打开"
        WebRequestError.ERROR_CATEGORY_PROXY ->
            "代理连不上，网页没能打开"
        WebRequestError.ERROR_CATEGORY_SAFEBROWSING ->
            "这个网站被安全名单拦下了，不要打开"
        else -> "网页没能打开，请检查网络后重试"
    }
}

private fun geckoErrorReason(error: WebRequestError): String =
    geckoErrorReasonFor(error.code, error.category)

private var nextTabId = 1

/**
 * Cap on simultaneously alive tab engines (v0.41.0, engine sleep).
 * The lazy-creation contract (v0.23.0) means a tab only builds its
 * engine when first shown — but once built, the engine used to stay
 * alive for as long as the tab existed, so twenty visited tabs meant
 * twenty WebViews/GeckoSessions in memory, which gets low-memory
 * devices killed or janky. From this version, at most this many
 * engines stay alive at once; past the cap, the least recently used
 * non-active tabs are put to sleep (see BrowserTab.sleepEngine).
 * Six is the balance point: enough that the tabs a user actually
 * bounces between stay instant to switch back to, small enough that
 * the worst-case engine memory stays bounded on modest hardware.
 */
const val ALIVE_ENGINE_LIMIT = 6

/**
 * One tab as the engine-sleep selection sees it (v0.78.0): a plain
 * snapshot so the victim choice is a pure function both sleep
 * passes share — the ordinary cap pass ([ALIVE_ENGINE_LIMIT]) and
 * the system memory-pressure pass (see [trimAliveLimit]).
 */
data class EngineSleepCandidate(
    val id: Int,
    val lastUsedAt: Long,
    val downloadActive: Boolean
)

/**
 * Pick which alive engines sleep so at most [limit] stay alive:
 * never the active tab, never a tab whose Gecko download stream is
 * still being copied (closing its session could cut the download
 * off), least recently used first. Download-guarded tabs may keep
 * the count over the limit for this pass; the next pass retries.
 * Pure: no Android calls, unit-tested.
 */
fun selectSleepVictims(
    alive: List<EngineSleepCandidate>,
    activeTabId: Int,
    limit: Int
): List<Int> {
    val excess = alive.size - limit
    if (excess <= 0) return emptyList()
    return alive
        .filter { it.id != activeTabId && !it.downloadActive }
        .sortedBy { it.lastUsedAt }
        .take(excess)
        .map { it.id }
}

/**
 * Memory-pressure sleep trigger (v0.78.0): the alive-engine limit
 * to enforce for a system trim-memory level, or null when the
 * level must not trigger anything. The trim levels are NOT one
 * severity scale: while the app is running they are
 * RUNNING_MODERATE 5 / RUNNING_LOW 10 / RUNNING_CRITICAL 15, and
 * once the process is cached they are UI_HIDDEN 20 / BACKGROUND
 * 40 / MODERATE 60 / COMPLETE 80. Only the two top-of-scale
 * levels — TRIM_MEMORY_RUNNING_CRITICAL (15, still running but
 * the system is about to kill background processes) and
 * TRIM_MEMORY_COMPLETE (80, cached process about to be released)
 * — squeeze the browser down to the active tab alone; every
 * moderate or lower level is ignored on purpose so a routine
 * memory tidy-up never throws away the user's warm tabs.
 */
fun trimAliveLimit(level: Int): Int? =
    if (level == android.content.ComponentCallbacks2
            .TRIM_MEMORY_RUNNING_CRITICAL ||
        level >= android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE
    ) 1 else null

/**
 * Whether removing a tab should release the app-global transient
 * UI state — the JS / auth / SSL dialogs, the shared file-picker
 * answer and the system-permission answer (v0.168.0). Those
 * pendings are app-global singletons with no tab id of their own;
 * they can only ever belong to the tab the user is looking at,
 * which is the active tab of whichever tab group is on screen.
 * There are two groups (v0.144.0): the normal tabs ([activeTabId])
 * and the fingerprint tabs ([activeFpTabId]). Tab ids come from
 * one process-wide counter, so an id matches at most one group's
 * active tab. Until this version the check compared only against
 * the normal active id, so closing the active fingerprint tab
 * never answered its pendings — its SSL / auth dialogs, file
 * picker or permission request were left waiting on a destroyed
 * WebView. [force] (whole-group teardown: close-all, profile
 * rebuild) always releases. Pure: no Android calls, unit-tested.
 */
fun shouldReleaseTransientUiForTab(
    removedTabId: Int,
    force: Boolean,
    activeTabId: Int,
    activeFpTabId: Int
): Boolean =
    force || removedTabId == activeTabId || removedTabId == activeFpTabId

/** Which tab group a newly created tab belongs to (v0.172.0). */
enum class NewTabGroup { NORMAL, FINGERPRINT }

/**
 * The group a tab created FROM [sourceTabId] must land in
 * (v0.172.0 new-tab routing audit). Every "open a new tab from
 * this tab" path — the long-press context menu, a page's new-
 * window request — decides by the SOURCE tab's group, never by
 * which shell happens to be on screen: a tab created from a
 * fingerprint tab lands in the fingerprint group (wearing the
 * current profile, outside normal history), and a tab created
 * from a normal tab lands in the normal group even while
 * fingerprint mode is open elsewhere. Tab ids come from one
 * process-wide counter, so membership in [fpTabIds] is the whole
 * decision. Until this version the long-press menu's state
 * carried no group identity at all and its new tabs always
 * landed in the normal group — the same family as the inbound
 * routing fixed in v0.153.0. Pure: no Android calls, unit-tested.
 */
fun newTabGroupForSource(
    sourceTabId: Int,
    fpTabIds: Set<Int>
): NewTabGroup =
    if (sourceTabId in fpTabIds) {
        NewTabGroup.FINGERPRINT
    } else {
        NewTabGroup.NORMAL
    }

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36"

/**
 * App-wide cap on tab thumbnails (v0.23.0). One thumbnail is
 * 360 px wide and roughly 640 px tall at ARGB_8888 — about
 * 360 × 640 × 4 B ≈ 0.9 MB of pixel memory per tab. Without a cap,
 * twenty open tabs would quietly hold ~18 MB of previews nobody is
 * looking at. The cache keeps at most [MAX_THUMBNAILS] tabs'
 * thumbnails: when a fresh capture pushes the count past the cap,
 * the least recently used tab's bitmap reference is dropped (the
 * tab grid then shows its placeholder, exactly like a tab that was
 * never captured). "Used" is tracked in access order: capturing a
 * thumbnail and selecting the tab both count as use. Plain
 * LinkedHashSet bookkeeping — no new dependency. All entry points
 * run on the main thread in practice; synchronized anyway so a
 * stray caller can never corrupt the ordering.
 */
object ThumbnailCache {
    const val MAX_THUMBNAILS = 12

    /** Tabs currently holding a thumbnail, least recently used first. */
    private val holders = LinkedHashSet<BrowserTab>()

    /** A tab just captured (or re-captured) its thumbnail. */
    @Synchronized
    fun onThumbnailSet(tab: BrowserTab) {
        holders.remove(tab)
        holders.add(tab)
        while (holders.size > MAX_THUMBNAILS) {
            val oldest = holders.first()
            holders.remove(oldest)
            // Drop only the reference, never recycle: the tab grid
            // may still be drawing this bitmap in the current frame.
            oldest.thumbnail = null
        }
    }

    /** A tab was selected: its thumbnail counts as recently used. */
    @Synchronized
    fun onThumbnailUsed(tab: BrowserTab) {
        if (tab.thumbnail == null) return
        holders.remove(tab)
        holders.add(tab)
    }

    /** A tab is going away; stop tracking it. */
    @Synchronized
    fun onTabDestroyed(tab: BrowserTab) {
        holders.remove(tab)
    }

    /**
     * A tab dropped its thumbnail without going away (v0.131.0
     * tab-sheet audit: a fresh navigation invalidates the old
     * page's preview); stop tracking it so the cap accounting
     * never counts a bitmap nobody holds.
     */
    @Synchronized
    fun onThumbnailCleared(tab: BrowserTab) {
        holders.remove(tab)
    }

    /**
     * Drop EVERY thumbnail at once (v0.127.0 perf/memory audit):
     * the system memory-pressure pass used to squeeze only the
     * engines, leaving up to [MAX_THUMBNAILS] preview bitmaps —
     * roughly 11 MB of pixels — untouched at exactly the moment
     * the system is asking for memory back. A thumbnail is pure
     * decoration: the tab grid falls back to its placeholder, and
     * the preview is recaptured on the next page finish or
     * switch-away, so dropping all of them (the active tab's too)
     * costs the user nothing but a briefly plainer tab grid.
     * References only, never recycle — a card may still be
     * drawing one in the current frame, same rule as the LRU
     * eviction in [onThumbnailSet].
     */
    @Synchronized
    fun clearAll() {
        val all = holders.toList()
        holders.clear()
        for (tab in all) {
            tab.thumbnail = null
        }
    }
}

/** One browser tab. Holds its own WebView or GeckoSession so tabs keep state. */
class BrowserTab(
    val engine: Engine,
    val incognito: Boolean,
    startUrl: String,
    val desktopMode: Boolean = false,
    private val onVisit: (title: String, url: String) -> Unit,
    private val shouldUseWebView: (String) -> Boolean = { false },
    private val onCompatRedirect: (String) -> Unit = {},
    private val isAdBlockEnabled: () -> Boolean = { true },
    // Long-press context menu (v0.42.0): one callback carrying both
    // the link URL and the image URL (either may be null) so a linked
    // image produces ONE combined menu instead of two competing ones.
    private val onPageContextMenu: (linkUrl: String?, imageUrl: String?) -> Unit =
        { _, _ -> },
    // Gecko text-selection actions (v0.43.0): the session's
    // SelectionActionDelegate forwards show/hide to the app, which
    // renders the menu. The hide callback lets the app drop a menu
    // whose selection has already gone away on its own.
    private val onGeckoSelectionShow: (GeckoSelectionMenu) -> Unit = {},
    private val onGeckoSelectionHide: () -> Unit = {},
    // Gecko clipboard-read permission (v0.45.0): a page asking to
    // read the clipboard (navigator.clipboard.readText and friends,
    // routed by Gecko through SelectionActionDelegate's clipboard
    // callbacks). The app shows its own prompt and answers through
    // the (Boolean) -> Unit callback — true only for an explicit
    // user "allow". The default answers deny at once so a page is
    // never left waiting. The dismiss callback means the engine
    // tore the request context down (the shipped
    // GeckoViewClipboardPermission JS sends it on pointerdown /
    // page teardown); the app then answers deny as well. The gone
    // callback fires when THIS tab answered its pending requests
    // itself because its engine was destroyed or slept, so the app
    // can drop a prompt whose answer already went out.
    private val onGeckoClipboardPermissionRequest: (
        String, (Boolean) -> Unit
    ) -> Unit = { _, respond -> respond(false) },
    private val onGeckoClipboardPermissionDismiss: () -> Unit = {},
    private val onGeckoClipboardPermissionGone: () -> Unit = {},
    private val textZoomProvider: () -> Int = { 100 },
    private val webDarkProvider: () -> Boolean = { false },
    // Reader-mode typesetting (v0.58.0), read live from the Store by
    // the app at reader-entry time: the font-size step, the
    // background choice, and the app's resolved darkness (theme mode
    // resolved, not the raw system value) for the FOLLOW background.
    private val readerFontSizeProvider: () -> ReaderFontSize =
        { ReaderFontSize.STANDARD },
    private val readerBackgroundProvider: () -> ReaderBackground =
        { ReaderBackground.FOLLOW },
    private val appDarkProvider: () -> Boolean = { false },
    private val onFullscreenShow: (android.view.View) -> Unit = {},
    private val onFullscreenHide: () -> Unit = {},
    private val onGeckoFullscreen: (Boolean) -> Unit = {},
    private val onRendererGone: (String) -> Unit = {},
    private val onFileChooserRequest: (
        WebChromeClient.FileChooserParams,
        ValueCallback<Array<Uri>>
    ) -> Unit = { _, callback -> callback.onReceiveValue(null) },
    private val onWebPermissionRequest: (PermissionRequest) -> Unit =
        { request -> request.deny() },
    private val onGeckoFileChooserRequest: (
        PromptDelegate.FilePrompt,
        (List<Uri>) -> Unit
    ) -> Unit = { _, respond -> respond(emptyList()) },
    private val onGeckoJsPrompt: (
        GeckoJsPromptKind, String, String, (Boolean, String?) -> Unit
    ) -> Unit = { _, _, _, respond -> respond(false, null) },
    private val onGeckoAndroidPermissions: (
        Array<String>, PermissionDelegate.Callback
    ) -> Unit = { _, callback -> callback.reject() },
    private val onGeckoContentPermission: (
        PermissionDelegate.ContentPermission
    ) -> Int = { PermissionDelegate.ContentPermission.VALUE_DENY },
    private val onGeckoMediaPermission: (
        String,
        Array<PermissionDelegate.MediaSource>?,
        Array<PermissionDelegate.MediaSource>?,
        PermissionDelegate.MediaCallback
    ) -> Unit = { _, _, _, callback -> callback.reject() },
    private val onNewWindowRequest: (String) -> Unit = {},
    private val onGeckoNewSession: (GeckoSession, String) -> Unit = { _, _ -> },
    // HTTP Basic-auth challenge (both engines, one shared dialog on
    // the app side). The answer callback gets (username, password),
    // or (null, null) for "cancel". The default cancels so a page is
    // never left waiting. Credentials are never persisted anywhere.
    private val onHttpAuth: (
        HttpAuthRequestInfo, (String?, String?) -> Unit
    ) -> Unit = { _, respond -> respond(null, null) },
    // WebView SSL certificate error: the app asks the user; the
    // answer is true only for "proceed this once". The default
    // refuses, matching the platform's safe behaviour.
    private val onSslError: (
        String, String, (Boolean) -> Unit
    ) -> Unit = { _, _, respond -> respond(false) },
    private val onUrlDownload: (
        String, String?, String?, String?
    ) -> Unit = { _, _, _, _ -> },
    // A tap on a direct file link (.apk/.zip/... by extension): both
    // engines intercept the navigation before it happens and report
    // the URL here; the app routes it through the same URL-type
    // download dispatch as the download listener. Distinct from
    // onExternalResponse on the Gecko side, which is the
    // response-type path (the server answered with a file) and keeps
    // saving the stream itself.
    private val onDirectDownload: (String) -> Unit = {},
    // Whether the ad-slot hiding stylesheet should be injected after
    // each finished WebView page (follows the ad-block switch).
    // WebView only.
    private val isAdHideSlotsEnabled: () -> Boolean = { false },
    // Fired from the exact same code path that increments the
    // per-page blocked counter, so the lifetime total in the Store
    // can never drift from the per-page numbers.
    private val onBlockedRequest: () -> Unit = {},
    // Whether the built-in page-guard script (v0.91.0, see
    // GuardScript) should be injected into this tab's WebView
    // pages. Read live from the Store by the app; consulted at
    // WebView creation and at every page start. WebView tabs
    // only — GeckoView has no script-injection path, so Gecko
    // tabs never see this. Incognito and JavaScript-disabled
    // tabs are excluded on top of this switch (see
    // guardScriptWanted).
    private val guardScriptProvider: () -> Boolean = { false },
    // The user's privacy signals (v0.92.0, see PrivacyScript):
    // Do Not Track, Global Privacy Control and WebRTC disabling.
    // Read live from the Store by the app; consulted at WebView
    // creation, at every page start and on every app-initiated
    // load (which is where the DNT / Sec-GPC headers ride).
    // WebView tabs only — Gecko honours GPC through its own
    // runtime setting and has no path for the other two.
    // Incognito tabs DO get the signals: they set nothing in
    // storage and only ever reduce what a page can learn.
    private val privacySignalsProvider: () -> PrivacySignals =
        { PrivacySignals() },
    // The fingerprint profile this tab presents (v0.144.0, see
    // FingerprintProfiles / FingerprintScript): non-null only for
    // tabs inside fingerprint mode, resolved live from the Store
    // so a profile switch re-reads at the next page start. Also
    // drives the WebView's native user-agent string at creation.
    // WebView tabs only — GeckoView 132 has no per-session
    // language / time-zone / canvas surface and fingerprint mode
    // never creates Gecko tabs (checkpoint has the API findings).
    private val fingerprintProfileProvider:
        () -> ResolvedFingerprint? = { null },
    // Whether the STRICT tracking-protection level is in force
    // (v0.92.0): widens this tab's WebView request interception to
    // AdBlocker's strict host list. Gecko tabs ignore it — their
    // level lives in the runtime's content-blocking categories.
    private val trackingStrictProvider: () -> Boolean = { false }
) {
    val id: Int = nextTabId++
    var url by mutableStateOf(startUrl)
    var title by mutableStateOf("")
    var progress by mutableIntStateOf(100)

    /**
     * True while this tab's engine is working on a page load
     * (v0.139.0 network audit). [progress] alone cannot say this:
     * it sits at 100 until the first progress callback arrives, so
     * a connection that hangs before any byte comes back looked
     * exactly like a finished page — no progress bar and, worse,
     * no way to cancel. This flag is set when a load starts (app
     * [load] / [reload] and both engines' page-start callbacks,
     * which also cover navigations a page starts itself) and
     * cleared at every point a load can end: page finish, main-
     * frame error, an app-side interception that cancels the
     * navigation, engine sleep, engine-creation failure, and
     * [stopLoading]. The toolbar menu reads it to offer
     * 「停止加载」 instead of 「重新加载」 while a load is live.
     */
    var loading by mutableStateOf(false)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    var readerActive by mutableStateOf(false)

    /** Find-in-page match counter text ("2 / 9"); WebView only. */
    var findInfo by mutableStateOf("")

    /**
     * The query the WebView's current find session was started with
     * (v0.124.0 find audit). findAllAsync always restarts the find
     * session and lands on the FIRST match, so it may only run when
     * the query actually changed; stepping through results of the
     * same query is findNext alone. Before this field existed every
     * step re-ran findAllAsync + findNext, which pinned "next" to
     * the second match and "previous" to the last one. Cleared
     * wherever the page or the find state goes away (new load,
     * clearFind, reader-mode rewrite, engine sleep), because the
     * engine-side session dies at exactly those points too.
     */
    private var lastFindQuery: String? = null

    /**
     * How many ad/tracker requests this tab's current page has had
     * blocked so far (WebView only — GeckoView exposes no per-request
     * count, so Gecko tabs keep this at zero and the UI shows no
     * number for them). Reset whenever a new page starts loading.
     * shouldInterceptRequest runs on a background thread, so the
     * increments are posted to the WebView's (main) thread.
     */
    var blockedCount by mutableIntStateOf(0)

    /**
     * Certificate facts of the current page (WebView tabs only, read
     * from the WebView's SslCertificate after a successful https
     * load): who the certificate was issued to and who issued it.
     * Empty when there is no certificate to show — the site-info
     * sheet then simply omits the row instead of inventing one.
     */
    var certIssuedTo by mutableStateOf("")
    var certIssuedBy by mutableStateOf("")

    /** Latest network-level load failure, or null when the page is fine. */
    var loadError by mutableStateOf<TabLoadError?>(null)

    /**
     * True when this tab's engine view could not be created at all
     * (v0.81.0): the system WebView is disabled / missing / broken,
     * or the Gecko runtime failed to start. Set only by
     * obtainWebView / obtainGecko when creation really fails —
     * never by a slow load — and the app shows its own fallback
     * page for the tab instead of crashing. Cleared by [load], so
     * navigating to a new address retries creation.
     */
    var engineUnavailable by mutableStateOf(false)

    /**
     * True while this tab's current page load is one the app itself
     * upgraded from http to https (v0.77.0 HTTPS first). Set by the
     * app side right after it loads an upgraded address; cleared by
     * [load] (any fresh load starts unmarked), and cleared again
     * once a load finishes successfully — what remains marked is
     * exactly "this tab is showing an error for a page WE upgraded",
     * which is when the error page offers the one-off
     * "open over http instead" escape hatch.
     */
    var httpsUpgraded by mutableStateOf(false)

    /**
     * Latest tab thumbnail (WebView tabs only). At most one bitmap is
     * ever referenced per tab — a fresh capture replaces the reference
     * and the old bitmap is left to the GC (it is NOT recycled here:
     * the tab grid may still be drawing it this frame). Closing the
     * tab drops the reference in destroy(). GeckoView has no
     * screenshot API, so Gecko tabs keep this null forever and the
     * grid says so instead of faking a preview. App-wide, at most
     * [ThumbnailCache.MAX_THUMBNAILS] tabs hold a thumbnail at once
     * (see ThumbnailCache); the least recently used ones lose theirs
     * and fall back to the same placeholder.
     */
    var thumbnail by mutableStateOf<Bitmap?>(null)

    /** Throwaway WebView holding a window.open popup until its URL is known. */
    private var popupChild: WebView? = null

    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    /**
     * Enter reader mode (WebView only): inject the extraction script
     * with the CURRENT reader typesetting settings (v0.58.0) baked
     * into its stylesheet — font size step and background/text/link
     * colours resolved from the Store at this moment. The FOLLOW
     * background resolves against the app's resolved darkness right
     * now; it does not track later theme changes while reading.
     *
     * Algorithmic darkening is switched off for this tab first: the
     * reader stylesheet already paints explicit colours, and letting
     * the platform darken them on top would double-darken the page
     * (a light reader page forced dark, a dark one darkened twice).
     * It is restored from the provider on exit / next page finish.
     */
    fun enterReaderMode(onFailed: () -> Unit = {}) {
        val view = webView
        if (view == null) {
            onFailed()
            return
        }
        setAlgorithmicDarkening(false)
        view.evaluateJavascript(
            buildReaderJs(
                readerFontSizeProvider(),
                readerPaletteFor(readerBackgroundProvider(), appDarkProvider())
            )
        ) { raw ->
            // The script answers "ok" only after it found a
            // qualifying block and rewrote the page; "fail" (or no
            // answer at all) means there is no article to extract,
            // so the tab stays on the original page: readerActive is
            // only set on success (v0.116.0 — before this the flag
            // was set unconditionally and a failed extraction still
            // switched the tab into a reader state over an
            // un-extracted or near-empty page).
            if (raw != null && raw.contains("ok")) {
                readerActive = true
                // The extraction just rewrote the document in
                // place (v0.124.0): matches and the counter were
                // computed against the ORIGINAL page, so both go.
                // A find run from here acts on the extracted
                // document, as v0.116.0 established.
                resetFindState()
                view.clearMatches()
            } else {
                setAlgorithmicDarkening(webDarkProvider())
                onFailed()
            }
        }
    }

    /**
     * Re-apply the current reader typesetting to a tab that is
     * already in reader mode (v0.58.0): the settings page calls this
     * on every reader-setting change, so changing font size or
     * background while reading takes effect at once. This is safe
     * because it does NOT re-run the extraction — it only rewrites
     * the text of the style element the extraction injected (looked
     * up by its fixed id); the extracted document is untouched.
     * No-op for tabs not in reader mode.
     */
    fun applyReaderStyle() {
        if (!readerActive) return
        val view = webView ?: return
        view.evaluateJavascript(
            buildReaderStyleJs(
                readerFontSizeProvider(),
                readerPaletteFor(readerBackgroundProvider(), appDarkProvider())
            ),
            null
        )
    }

    fun exitReaderMode() {
        readerActive = false
        // Restore the web-dark setting before the reload repaints the
        // original page (enterReaderMode turned darkening off).
        setAlgorithmicDarkening(webDarkProvider())
        reload()
    }

    /** Algorithmic darkening is an API 33+ WebSettings switch. */
    private fun setAlgorithmicDarkening(enabled: Boolean) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            try {
                webView?.settings?.setAlgorithmicDarkeningAllowed(enabled)
            } catch (t: Throwable) {
                // Best-effort only; reader mode itself must still work.
            }
        }
    }

    fun exitFullscreen() {
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        // Gecko fullscreen is a session state, not a custom view:
        // exitFullScreen() (verified present in GeckoView 132) makes
        // the content delegate report onFullScreen(false), which is
        // what clears the app's chrome-hidden state. Calling it when
        // the session is not fullscreen is a no-op.
        geckoSession?.exitFullScreen()
    }

    var webView: WebView? = null
    var geckoSession: GeckoSession? = null
    var geckoView: GeckoView? = null

    /**
     * The document-start script handler for the page-guard script
     * (v0.91.0), registered on this tab's WebView when the device
     * supports it. Null when not registered (switch off, incognito,
     * unsupported WebView, or no WebView yet). Removed again in
     * [destroy] / [sleepEngine] alongside the WebView it belongs to.
     */
    private var guardScriptHandler: ScriptHandler? = null

    /**
     * The document-start handler for the privacy-signals script
     * (v0.92.0), plus the exact payload it was registered with:
     * the payload depends on three independent switches, so a
     * changed payload at page start means remove + re-register.
     * Null when no signal is on.
     */
    private var privacyScriptHandler: ScriptHandler? = null
    private var privacyScriptPayload: String? = null

    /**
     * Fingerprint script state (v0.144.0): the live document-start
     * handler and the exact payload it was registered with, so a
     * profile switch at page start means remove + re-register.
     * Null when this is not a fingerprint tab.
     */
    private var fingerprintScriptHandler: ScriptHandler? = null
    private var fingerprintScriptPayload: String? = null

    /**
     * Lazy engine creation (v0.23.0): a tab starts out as data only
     * — url, title, engine choice and desktop-mode flag. Its engine
     * view (WebView or GeckoSession+GeckoView) is created the first
     * time the tab is actually shown as the active tab, because
     * [TabContent] is the only caller of obtainWebView/obtainGecko
     * and it is only composed for the active tab. Restored tabs
     * therefore cost no engine at startup: only the active one is
     * built and loaded; every other tab is built and loaded on its
     * first switch-to, and afterwards the engine is kept alive
     * exactly as before. [load] on a not-yet-created tab only
     * updates the data — obtain*() loads the then-current [url]
     * when the engine is eventually created, so nothing is lost.
     * Incognito tabs are never restored, so they are always created
     * on open, as before.
     */
    val engineCreated: Boolean
        get() = webView != null || geckoView != null || geckoSession != null

    /**
     * Last time this tab was the active (visible) tab, in
     * [System.currentTimeMillis] terms; set at construction and
     * refreshed by [noteUsed] every time the tab becomes active.
     * This is the LRU key for engine sleep (v0.41.0): when too many
     * engines are alive, the non-active tabs with the oldest stamps
     * sleep first. A tab the user just switched away from has a
     * fresh stamp, so it naturally sits near the back of the sleep
     * queue — it is never the first thing slept. Plain field, not
     * Compose state: nothing renders from it, the sleep pass just
     * reads it on the main thread.
     */
    var lastUsedAt: Long = System.currentTimeMillis()
        private set

    /** Mark this tab as just used (it became the active tab). */
    fun noteUsed() {
        lastUsedAt = System.currentTimeMillis()
    }

    /**
     * True while a Gecko response-type download (v0.4.0) is still
     * copying this tab's WebResponse body stream to disk on the
     * background download executor. The stream belongs to the
     * session's response: closing the session mid-copy could cut the
     * download off, so the engine-sleep pass skips tabs with this
     * flag set. Set in onExternalResponse before the copy starts,
     * cleared by saveGeckoDownload's completion callback on every
     * exit path. Volatile because the clear happens on the download
     * thread while the sleep pass reads it on the main thread.
     */
    @Volatile
    var geckoDownloadActive: Boolean = false

    /**
     * Tracker id of this tab's in-flight Gecko download copy, or a
     * negative value when none is running (v0.78.0 release audit).
     * The sleep pass only needs the boolean above, but closing the
     * tab must be able to cancel THIS tab's copy: until this version
     * destroy() closed the session and left the copy running against
     * a dead session's stream, to fail however it happened to fail.
     * Set from saveGeckoDownload's return value in onExternalResponse,
     * cleared by its completion callback.
     */
    @Volatile
    var activeGeckoDownloadId: Long = -1L

    val isHome: Boolean
        get() = url == HOME_URL

    fun load(target: String) {
        loadError = null
        // A fresh navigation is a fresh engine-creation attempt
        // (v0.81.0): if this tab was sitting on the engine-
        // unavailable fallback page, clearing the flag puts
        // TabContent back into composition, which retries obtain*.
        engineUnavailable = false
        httpsUpgraded = false
        blockedCount = 0
        certIssuedTo = ""
        certIssuedBy = ""
        url = target
        // The native home page is rendered by the app itself; the web
        // engines must never try to load its internal URL.
        if (target == HOME_URL) {
            loading = false
            return
        }
        loading = true
        when (engine) {
            Engine.WEBVIEW -> {
                // Privacy headers (v0.92.0): loads the APP initiates
                // (address bar, home shortcuts, tab restore) carry
                // the DNT / Sec-GPC headers the user switched on.
                // Navigations a page starts itself are the engine's
                // own and cannot take headers — those are covered
                // by the JS signals instead (see PrivacyScript).
                val headers = privacySignalsProvider().headers()
                if (headers.isEmpty()) {
                    webView?.loadUrl(target)
                } else {
                    webView?.loadUrl(target, headers)
                }
            }
            Engine.GECKO -> geckoSession?.loadUri(target)
        }
    }

    fun goBack() {
        when (engine) {
            Engine.WEBVIEW -> webView?.goBack()
            Engine.GECKO -> geckoSession?.goBack()
        }
    }

    fun goForward() {
        when (engine) {
            Engine.WEBVIEW -> webView?.goForward()
            Engine.GECKO -> geckoSession?.goForward()
        }
    }

    
    
    fun reload() {
        if (url != HOME_URL) loading = true
        when (engine) {
            Engine.WEBVIEW -> webView?.reload()
            Engine.GECKO -> geckoSession?.reload()
        }
    }

    /**
     * Stop the load currently in flight (v0.139.0 network audit):
     * until this version the engines' stop calls were only used
     * internally when an engine slept or was destroyed, so a page
     * stuck on a slow or hung connection could not be cancelled —
     * the only remedy was reloading, which started the wait over.
     * Whatever part of the page already arrived stays on screen
     * (both engines keep the partially rendered document); no
     * error state is raised, because the user stopping a load is
     * not a failure. Progress is closed out at 100 like the error
     * path does, so the progress bar never hangs over the stopped
     * page.
     */
    fun stopLoading() {
        when (engine) {
            Engine.WEBVIEW -> webView?.stopLoading()
            Engine.GECKO -> geckoSession?.stop()
        }
        loading = false
        progress = 100
    }

    fun findInPage(query: String, forward: Boolean) {
        if (query.isBlank()) return
        when (engine) {
            Engine.WEBVIEW -> {
                val view = webView ?: return
                if (query != lastFindQuery) {
                    // Fresh query (v0.124.0): start a new find
                    // session. findAllAsync highlights every match
                    // and makes the first one active on its own, so
                    // a forward step needs nothing more; a backward
                    // step from there wraps to the last match, which
                    // is the sensible landing spot for "previous" on
                    // a query that has not been stepped through yet.
                    lastFindQuery = query
                    view.findAllAsync(query)
                    if (!forward) view.findNext(false)
                } else {
                    // Same query: step within the live session.
                    // findNext wraps at both ends (the platform does
                    // this silently; the counter jumping from the
                    // last match back to "1 / n" is the only sign).
                    view.findNext(forward)
                }
            }
            Engine.GECKO -> {
                val flags = if (forward) {
                    GeckoSession.FINDER_FIND_FORWARD
                } else {
                    GeckoSession.FINDER_FIND_BACKWARDS
                }
                geckoSession?.finder?.find(query, flags)
            }
        }
    }

    /** Print the page (or save as PDF) through the system print service. */
    fun printPage(context: Context): Boolean {
        val view = webView ?: return false
        return try {
            val manager = context.getSystemService(
                Context.PRINT_SERVICE
            ) as android.print.PrintManager
            val jobName = title.ifBlank { "TuxunBrowse 网页" }
            manager.print(jobName, view.createPrintDocumentAdapter(jobName), null)
            true
        } catch (t: Throwable) {
            false
        }
    }

    /**
     * Capture the WebView's current visible content as a small
     * thumbnail for the tab grid, scaled to 360 px wide. Called only
     * when a page finishes loading and when the tab is switched away
     * from — never during scrolling or animations, so it cannot
     * become a per-frame cost. WebView only; Gecko tabs have no
     * screenshot API and simply keep no thumbnail.
     */
    fun captureThumbnail() {
        val view = webView ?: return
        doCaptureThumbnail(view)
    }

    /**
     * Same capture as [captureThumbnail], but deferred until after
     * the tab-switch crossfade (220 ms) has finished: the switch-away
     * call sites used to draw the outgoing WebView into a bitmap
     * synchronously in the very frame the fade starts, which could
     * stall the first transition frame. The capture is dropped if the
     * tab's view has been replaced or destroyed in the meantime.
     */
    fun captureThumbnailDeferred() {
        val view = webView ?: return
        view.postDelayed({
            if (webView === view) doCaptureThumbnail(view)
        }, 260)
    }

    private fun doCaptureThumbnail(view: WebView) {
        // Incognito tabs never capture a thumbnail (v0.27.0 privacy
        // audit): a preview bitmap of a private page sitting in the
        // in-memory cache — and shown in the tab grid to anyone
        // holding the phone — is a leak, even though it never touches
        // disk. This is the single choke point behind both capture
        // entry points (page finish and switch-away, deferred or
        // not), so the guard lives here and nowhere else is needed.
        if (incognito) return
        try {
            val w = view.width
            val h = view.height
            if (w <= 0 || h <= 0) return
            val targetW = 360
            val scale = targetW.toFloat() / w
            val targetH = (h * scale).toInt().coerceAtLeast(1)
            val bmp = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.scale(scale, scale)
            view.draw(canvas)
            thumbnail = bmp
            // Register with the app-wide cap: past 12 thumbnails the
            // least recently used tab's preview is dropped.
            ThumbnailCache.onThumbnailSet(this)
        } catch (t: Throwable) {
            // A thumbnail is best-effort decoration; a failed capture
            // must never disturb browsing.
        }
    }

    /**
     * Draw the WebView's visible area into a full-size bitmap for
     * the save/share screenshot feature. Visible area only — no
     * full-page capture. Null when there is nothing to draw.
     */
    fun captureVisibleBitmap(): Bitmap? {
        val view = webView ?: return null
        return try {
            val w = view.width
            val h = view.height
            if (w <= 0 || h <= 0) return null
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bmp))
            bmp
        } catch (t: Throwable) {
            null
        }
    }

    
    /**
     * Run the resource sniffer over the current page (WebView only):
     * injects a script that collects downloadable file links, image
     * sources and media sources, resolves them to absolute URLs and
     * de-duplicates them, then reports the typed list back.
     */
    fun collectPageResources(onResult: (List<SniffedResource>) -> Unit) {
        val view = webView
        if (view == null) {
            onResult(emptyList())
            return
        }
        try {
            view.evaluateJavascript(SNIFF_JS) { raw ->
                onResult(parseSniffResult(raw))
            }
        } catch (t: Throwable) {
            onResult(emptyList())
        }
    }

    
    fun applyTextZoom(percent: Int) {
        webView?.settings?.textZoom = percent
    }

    fun applyWebDark(enabled: Boolean) {
        // While this tab is in reader mode its page colours come from
        // the reader stylesheet, and darkening stays off (see
        // enterReaderMode); the new value takes effect when reader
        // mode exits and restores it from the provider.
        if (readerActive) return
        setAlgorithmicDarkening(enabled)
    }

    fun clearFind() {
        findInfo = ""
        lastFindQuery = null
        when (engine) {
            Engine.WEBVIEW -> webView?.clearMatches()
            Engine.GECKO -> geckoSession?.finder?.clear()
        }
    }

    /**
     * Drop the app-side find state without touching the engine
     * (v0.124.0): used where the page itself goes away or is
     * rewritten — a new load starting (either engine), reader mode
     * replacing the document, engine sleep. The engine discards its
     * own highlights at those points; what must not survive is the
     * counter text (it would describe a page that no longer exists)
     * and [lastFindQuery] (the next identical query must start a
     * fresh findAllAsync, not step a dead session).
     */
    private fun resetFindState() {
        findInfo = ""
        lastFindQuery = null
    }

    /** Mark this tab's thumbnail as recently used (tab selected). */
    fun noteThumbnailUsed() {
        ThumbnailCache.onThumbnailUsed(this)
    }

    /**
     * Drop the current thumbnail because it no longer shows what
     * this tab is on (v0.131.0 tab-sheet audit). Until this
     * version the preview survived a navigation: open the tab
     * sheet while a new page was still loading — or after a load
     * that never reached onPageFinished — and the card kept
     * showing the PREVIOUS page's picture under the new page's
     * title and host. The reference is simply dropped (never
     * recycled — the grid may still be drawing it this frame,
     * same rule as the cache's own eviction) and the fresh page
     * recaptures on finish or switch-away as usual.
     */
    fun clearThumbnail() {
        if (thumbnail == null) return
        thumbnail = null
        ThumbnailCache.onThumbnailCleared(this)
    }

    /**
     * Pending clipboard-read permission answers for this tab
     * (v0.45.0). A GeckoResult left unanswered hangs the requesting
     * page script forever, so [destroy] and [sleepEngine] drain this
     * list with "deny" before the session goes away — the page gets
     * a clean refusal instead of a frozen promise, whatever the app
     * side was doing.
     */
    private val pendingClipboardAnswers =
        mutableListOf<ClipboardPermissionAnswer>()

    /** Answer every pending clipboard-read request with "deny". */
    fun denyPendingClipboardRequests() {
        if (pendingClipboardAnswers.isEmpty()) return
        val pending = pendingClipboardAnswers.toList()
        pendingClipboardAnswers.clear()
        pending.forEach { answer ->
            try {
                answer(false)
            } catch (t: Throwable) {
                // One bad answer must not stop the others.
            }
        }
        onGeckoClipboardPermissionGone()
    }

    fun destroy() {
        denyPendingClipboardRequests()
        // An in-flight Gecko download copy belongs to this tab
        // (v0.78.0): cancel it through the tracker instead of
        // leaving it copying from a session that is about to close.
        // The copy loop unwinds through its normal cleanup (partial
        // artefact deleted, tracker entry ended, completion
        // callback clears the busy flag) — nothing dangles.
        val downloadId = activeGeckoDownloadId
        if (downloadId >= 0L) {
            GeckoDownloadTracker.requestCancel(downloadId)
        }
        // Release the thumbnail with the tab; the reference is simply
        // dropped (not recycled — the grid may still hold a frame of
        // it) and the GC reclaims the pixels.
        ThumbnailCache.onTabDestroyed(this)
        thumbnail = null
        // The guard-script handler belongs to the WebView below.
        removeGuardScriptHandler()
        removePrivacyScriptHandler()
        removeFingerprintScriptHandler()
        exitFullscreen()
        popupChild?.let {
            try {
                it.destroy()
            } catch (t: Throwable) {
                // Already gone; nothing to clean up.
            }
        }
        popupChild = null
        webView?.let {
            // Detach from the view hierarchy BEFORE destroy()
            // (v0.78.0 release audit): closeTab destroys the engine
            // while the closing tab's AndroidView is still composed
            // for this frame, and destroying a WebView that is still
            // attached is the documented leak/crash pattern — the
            // platform contract is remove-then-destroy.
            try {
                (it.parent as? android.view.ViewGroup)?.removeView(it)
            } catch (t: Throwable) {
                // Not attached (or already detached); destroy anyway.
            }
            it.stopLoading()
            it.destroy()
        }
        webView = null
        // Same detach-first rule for the Gecko view: the session
        // close below does not remove its view from the hierarchy.
        geckoView?.let {
            try {
                (it.parent as? android.view.ViewGroup)?.removeView(it)
            } catch (t: Throwable) {
                // Not attached; nothing to detach.
            }
        }
        geckoSession?.close()
        geckoSession = null
        geckoView = null
    }

    /**
     * Put this tab's engine to sleep (v0.41.0): destroy the engine
     * objects exactly like [destroy] does (WebView stopLoading +
     * destroy, GeckoSession close), but keep the tab itself — url,
     * title, engine choice, desktop-mode and incognito flags are
     * data on the tab and survive, and the thumbnail is kept too
     * (it is just a bitmap the tab grid shows; it costs a fixed,
     * already-capped amount of memory and the grid must not lose
     * the preview of a tab the user may switch back to). The tab
     * returns to the exact not-yet-created state of the v0.23.0
     * lazy contract ([engineCreated] false), so switching back to
     * it re-creates the engine through the same obtain*() path as a
     * first visit and loads the then-current [url].
     *
     * The honest trade-off, same family as the v0.34.0 rebuild
     * losses: everything that lived only inside the engine is gone
     * — scroll position, form contents the user had typed, and the
     * back/forward stack (the Gecko history snapshot is dropped
     * here too, since it describes the dead session's stack).
     * Page-level transient state (progress, find info, load error,
     * reader mode, blocked count, certificate facts) is reset for
     * the same reason: none of it describes the page that will be
     * freshly loaded on return.
     *
     * Only ever called on non-active tabs by the app's sleep pass:
     * the visible tab never sleeps, so no fullscreen or on-screen
     * state can be torn down here. Incognito tabs sleep under the
     * same rule — sleeping writes nothing anywhere, the tab's data
     * stays in memory exactly as before, so the incognito contract
     * (nothing persisted) is untouched.
     */
    fun sleepEngine() {
        if (!engineCreated) return
        denyPendingClipboardRequests()
        // The guard-script handler belongs to the WebView being
        // destroyed; a woken tab re-registers at creation.
        removeGuardScriptHandler()
        removePrivacyScriptHandler()
        removeFingerprintScriptHandler()
        customViewCallback = null
        popupChild?.let {
            try {
                it.destroy()
            } catch (t: Throwable) {
                // Already gone; nothing to clean up.
            }
        }
        popupChild = null
        webView?.let {
            try {
                it.stopLoading()
                it.destroy()
            } catch (t: Throwable) {
                // Already gone; nothing to clean up.
            }
        }
        webView = null
        try {
            geckoSession?.close()
        } catch (t: Throwable) {
            // Already gone; nothing to clean up.
        }
        geckoSession = null
        geckoView = null
        progress = 100
        loading = false
        canGoBack = false
        canGoForward = false
        readerActive = false
        resetFindState()
        loadError = null
        blockedCount = 0
        certIssuedTo = ""
        certIssuedBy = ""
    }

    /**
     * Host (activity) went to the background (v0.35.0): pause this
     * tab's engine so page media stops and JS timers/rendering idle
     * instead of running on in the background. WebView gets its
     * standard onPause(); a Gecko session is set inactive (signature
     * verified against the GeckoView 132 AAR by parsing the class
     * file: public setActive(Z)V, i.e. setActive(boolean)). Tabs
     * whose engine was never created have nothing to pause. Both
     * calls are idempotent, so an ON_PAUSE followed by ON_STOP is
     * harmless.
     */
    fun pauseEngine() {
        try {
            webView?.onPause()
        } catch (t: Throwable) {
            // Engine already gone; nothing to pause.
        }
        try {
            geckoSession?.setActive(false)
        } catch (t: Throwable) {
            // Engine already gone; nothing to pause.
        }
    }

    /**
     * Host came back to the foreground (v0.35.0): resume this tab's
     * engine. WebView tabs all resume (onResume() is cheap and safe
     * even for a tab that is not on screen); a Gecko session is only
     * reactivated when [active] says this is the visible tab — the
     * rest stay inactive, matching the tab-switch rule in
     * [applyEngineActiveState].
     */
    fun resumeEngine(active: Boolean) {
        try {
            webView?.onResume()
        } catch (t: Throwable) {
            // Engine already gone; nothing to resume.
        }
        if (engine == Engine.GECKO) applyEngineActiveState(active)
    }

    /**
     * The one place that decides whether this tab's Gecko session is
     * the active one (v0.35.0): the visible tab's session is active,
     * every other created session is inactive, so non-visible tabs
     * do not keep rendering or playing media. WebView tabs need no
     * equivalent — only the visible tab's WebView is composed, and
     * the host pause/resume pair above covers the background case.
     */
    fun applyEngineActiveState(active: Boolean) {
        if (engine != Engine.GECKO) return
        try {
            geckoSession?.setActive(active)
        } catch (t: Throwable) {
            // Engine already gone; nothing to toggle.
        }
    }

    /**
     * Create (or return) this tab's WebView. Returns null when the
     * system WebView cannot be created at all (v0.81.0): on a
     * device whose WebView provider is disabled, missing or broken,
     * the WebView constructor throws
     * WebViewFactory$MissingWebViewPackageException (a
     * RuntimeException) and the setup calls in [createWebView] can
     * throw with it. That must never crash the whole app — the tab
     * is marked [engineUnavailable] and the app shows its fallback
     * page with a switch-to-Firefox action instead. Only a real
     * creation exception lands here; a slow page load never
     * touches this path.
     */
    fun obtainWebView(context: Context): WebView? {
        webView?.let { return it }
        return try {
            createWebView(context)
        } catch (t: Throwable) {
            // A half-built view may have been assigned already;
            // drop it so the tab is cleanly uncreated and a later
            // retry starts from scratch.
            try {
                webView?.destroy()
            } catch (ignored: Throwable) {
                // Already broken; nothing more to clean up.
            }
            webView = null
            engineUnavailable = true
            // No load can be in flight without an engine (v0.139.0).
            loading = false
            null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Suppress("DEPRECATION")
    private fun createWebView(context: Context): WebView {
        val view = WebView(context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        // JavaScript stays on: pages need it to work at all.
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        // Hardened WebView switches (v0.31.0 audit), all pinned
        // explicitly rather than left to platform defaults:
        // - No file:// access at all. Nothing in the app loads local
        //   files as pages (the error page and the home page are
        //   Compose overlays, downloads go through DownloadManager /
        //   MediaStore), so the platform default (true) is pure risk.
        // - No content:// access either: no feature loads content
        //   URIs as pages (file picking hands URIs to the page's own
        //   upload callback, which is a different mechanism).
        // - The two from-file-URLs switches are deprecated but are
        //   pinned off anyway, so no file page could ever reach local
        //   files or arbitrary origins through them.
        // - Mixed content stays NEVER_ALLOW (that already was the
        //   platform default for this app; pinned so it cannot drift
        //   with an OEM WebView): an https page may not pull http
        //   subresources. Recorded trade-off: very old sites that mix
        //   http images into https pages will show broken images —
        //   accepted, the alternative weakens every secure page.
        // - Geolocation off: the app holds no location permission
        //   and has no permission prompt for it, so pages must not be
        //   able to ask the engine for a position at all.
        view.settings.allowFileAccess = false
        view.settings.allowContentAccess = false
        view.settings.allowFileAccessFromFileURLs = false
        view.settings.allowUniversalAccessFromFileURLs = false
        view.settings.mixedContentMode =
            android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        view.settings.setGeolocationEnabled(false)
        // Second hardening pass (v0.113.0 audit): four more switches
        // that used to ride on platform defaults are now pinned to
        // those same defaults explicitly, so an OEM WebView cannot
        // drift them. Every value below equals the platform default
        // on current Android, so this block changes no behaviour:
        // - JavaScript may not open windows on its own
        //   (javaScriptCanOpenWindowsAutomatically defaults to
        //   false). Window.open needs a user gesture path instead.
        // - Multiple windows stay OFF (the default). The
        //   onCreateWindow handler below is therefore not reached
        //   today: a target=_blank / window.open navigation loads
        //   as a top-level navigation in this same tab instead of
        //   opening a new tab. Turning the gate on would change
        //   that visible site behaviour, so it stays off pending a
        //   product decision (recorded in the v0.113.0 checkpoint).
        // - The legacy WebSQL database stays off (the default):
        //   domStorage above is the storage pages actually use.
        // - Password saving stays off: the setter is deprecated
        //   and a no-op on current Android (WebView no longer
        //   saves passwords itself), pinned only for the record.
        view.settings.javaScriptCanOpenWindowsAutomatically = false
        view.settings.setSupportMultipleWindows(false)
        view.settings.databaseEnabled = false
        view.settings.savePassword = false
        // Form-data saving pinned off (v0.133.0 web-capability audit):
        // unlike the four switches above this one was still riding the
        // platform default, which is ON — the WebView could keep a
        // legacy store of typed form values even though the app has no
        // password/form vault and the clear-data page already wipes
        // that store (v0.129.0). Pinning it off aligns the save side
        // with the clear side; the setter is deprecated because the
        // system Autofill Framework is the modern path, which this
        // does not touch.
        view.settings.saveFormData = false
        // Media autoplay gate (v0.123.0 fullscreen audit): also a
        // platform default pinned for the record — media playback
        // requires a user gesture (the default true), so pages
        // cannot start audible video on their own. Zero behaviour
        // change. Gecko has no wired equivalent: nothing in the
        // runtime/session settings touches autoplay, so the Gecko
        // side simply follows the engine's own default policy.
        view.settings.mediaPlaybackRequiresUserGesture = true
        view.settings.builtInZoomControls = true
        view.settings.displayZoomControls = false
        view.settings.textZoom = textZoomProvider()
        // Third-party cookies stay allowed: blocking them breaks
        // real sign-in flows (SSO redirect chains, payment pages
        // embedded in iframes) far more often than it helps.
        // First-party acceptance is pinned to the platform default
        // (true) explicitly as well (v0.129.0 site-data audit), so
        // the whole cookie policy is on the record, not on OEM
        // defaults. Zero behaviour change.
        try {
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance()
                .setAcceptThirdPartyCookies(view, true)
        } catch (t: Throwable) {
            // Cookie policy is best-effort only; browsing must still work.
        }
        // Cache mode pinned to the platform default (LOAD_DEFAULT:
        // honour the server's cache headers) explicitly, in the
        // same pin-the-defaults spirit as the v0.113.0 pass
        // (v0.129.0 site-data audit). It was previously left unset,
        // which happens to mean LOAD_DEFAULT on current Android —
        // pinned now so an OEM WebView cannot drift it. Zero
        // behaviour change.
        view.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
        // Safe Browsing, turned on explicitly through AndroidX WebKit.
        // Devices whose WebView does not support it are skipped quietly.
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.SAFE_BROWSING_ENABLE)) {
                WebSettingsCompat.setSafeBrowsingEnabled(view.settings, true)
            }
        } catch (t: Throwable) {
            // Safe Browsing is best-effort only; browsing must still work.
        }
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            view.settings.setAlgorithmicDarkeningAllowed(webDarkProvider())
        }
        // Viewport display policy (v0.40.0 audit): wide viewport +
        // overview mode are set ONCE here, unconditionally, for both
        // phone and desktop mode. Before this version only the
        // desktop branch below set them, so phone mode sat at the
        // platform defaults (both false) and old sites without a
        // viewport meta were laid out at a narrow width and squeezed
        // together instead of being shown zoomed-out as a full page
        // first, the way mainstream browsers present them. Pages
        // that DO carry a viewport meta are unaffected: the meta
        // width wins over the wide-viewport fallback either way.
        // desktopMode is a constructor val (a mode toggle recreates
        // the tab), so nothing can half-overwrite these later; the
        // desktop branch below now only swaps the user agent.
        view.settings.useWideViewPort = true
        view.settings.loadWithOverviewMode = true
        if (desktopMode) {
            view.settings.userAgentString = DESKTOP_USER_AGENT
        }
        // Fingerprint mode (v0.144.0): the profile's UA is the
        // tab's native UA too, so HTTP headers and navigator
        // reads tell the same story. Fingerprint tabs are never
        // desktop-mode tabs; if both somehow applied, the
        // profile wins by coming last.
        fingerprintProfileProvider()?.let { resolved ->
            view.settings.userAgentString = resolved.device.userAgent
        }
        view.setOnLongClickListener { v ->
            val wv = v as WebView
            val hit = wv.hitTestResult
            val extra = hit.extra
            when {
                hit.type == WebView.HitTestResult.IMAGE_TYPE &&
                    !extra.isNullOrBlank() -> {
                    onPageContextMenu(null, extra)
                    true
                }
                // A linked image: HitTestResult's extra is the LINK
                // href, not the image. Ask the WebView for the image
                // URL via the official requestImageRef message; if it
                // never answers, fall back to the link-only menu so
                // the long-press never goes silent.
                hit.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE &&
                    !extra.isNullOrBlank() -> {
                    var fired = false
                    val handler = object :
                        android.os.Handler(android.os.Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            if (fired) return
                            fired = true
                            // The answer (and the fallback below)
                            // only counts while this tab still owns
                            // this exact WebView (v0.106.0 timer
                            // audit): if the tab was slept, destroyed
                            // or rebuilt in the meantime, its view is
                            // gone and a context menu for it must
                            // never pop up over whatever the user is
                            // looking at now — same liveness guard as
                            // captureThumbnailDeferred.
                            if (webView !== wv) return
                            val img = (msg.obj as? String)
                                ?.takeIf { it.isNotBlank() }
                            onPageContextMenu(extra, img)
                        }
                    }
                    try {
                        wv.requestImageRef(handler.obtainMessage())
                    } catch (t: Throwable) {
                        if (!fired) {
                            fired = true
                            onPageContextMenu(extra, null)
                        }
                    }
                    handler.postDelayed({
                        if (!fired) {
                            fired = true
                            if (webView === wv) {
                                onPageContextMenu(extra, null)
                            }
                        }
                    }, 300)
                    true
                }
                hit.type == WebView.HitTestResult.SRC_ANCHOR_TYPE &&
                    !extra.isNullOrBlank() -> {
                    onPageContextMenu(extra, null)
                    true
                }
                // Anything else (notably long-press on plain text)
                // is NOT consumed: the WebView's own text-selection
                // action mode proceeds as the platform provides it.
                else -> false
            }
        }
        view.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                wv: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                // Load-speed audit (v0.171.0): this hook fires for
                // every subresource of every page, so the disabled
                // case must cost nothing (not even stringifying the
                // URL), and the enabled case takes the host straight
                // from the platform-parsed Uri instead of re-parsing
                // the whole URL string with java.net.URI. Matching
                // itself is the same AdBlocker core the string entry
                // point funnels through — same list, same rule.
                if (!isAdBlockEnabled()) return null
                val host = request.url?.host ?: return null
                return if (
                    AdBlocker.isBlockedHost(host, trackingStrictProvider())
                ) {
                    // Count it on the main thread: this callback runs
                    // on a background thread, the counter is UI state.
                    // The lifetime total rides the very same post, so
                    // the two counters are one count, not two.
                    wv.post {
                        blockedCount++
                        onBlockedRequest()
                    }
                    android.webkit.WebResourceResponse(
                        "text/plain", "utf-8",
                        java.io.ByteArrayInputStream(ByteArray(0))
                    )
                } else {
                    null
                }
            }

            override fun shouldOverrideUrlLoading(
                wv: WebView,
                request: WebResourceRequest
            ): Boolean {
                // A tap on a direct file link (.apk/.zip/... by
                // extension): WebView would navigate to it as if it
                // were a page and land on an error screen — the
                // DownloadListener never fires for plain link taps,
                // only for Content-Disposition / unsupported-MIME
                // responses. Intercept here instead and hand the URL
                // to the app's download dispatch.
                // False-positive guards, all required: main frame
                // only, http(s) only (other schemes keep their
                // existing handling), and only for a real user tap
                // (hasGesture) — redirect chains and script-driven
                // navigations are let through untouched, since telling
                // a "necessary" hop apart from a download is not
                // possible and breaking a login/payment redirect
                // would be far worse than missing a download.
                val target = request.url?.toString() ?: return false
                // Special links (tel:/mailto:/sms:/geo:/market:/
                // intent:): neither engine can load these itself —
                // WebView would land on an error page. A real tap is
                // handed to the shared external dispatcher (dialer,
                // mail app, maps, store, ...); without a user gesture
                // the navigation is simply blocked, so a page cannot
                // silently pop another app open on its own. An
                // http(s) fallback URL the dispatcher returns (from
                // an intent: link) is loaded right here as a normal
                // page.
                if (request.isForMainFrame && isSpecialLinkUrl(target)) {
                    if (request.hasGesture()) {
                        val fallback = dispatchSpecialLink(wv.context, target)
                        if (fallback != null) {
                            wv.post { wv.loadUrl(fallback) }
                        }
                    }
                    // The navigation is cancelled here (v0.139.0):
                    // no page load remains in flight on its account.
                    loading = false
                    return true
                }
                if (request.isForMainFrame && request.hasGesture() &&
                    (target.startsWith("http://") ||
                        target.startsWith("https://")) &&
                    isDirectDownloadUrl(target)
                ) {
                    onDirectDownload(target)
                    // Cancelled into a download (v0.139.0), same
                    // reasoning as the special-link branch above.
                    loading = false
                    return true
                }
                return false
            }

            override fun onPageStarted(
                wv: WebView,
                startedUrl: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(wv, startedUrl, favicon)
                // A load is under way (v0.139.0): also covers
                // navigations the page started itself, which never
                // pass through the app's load().
                loading = true
                // Fingerprint profile (v0.144.0): reconcile
                // first, before the other two scripts.
                syncFingerprintScript(wv)
                // Page-guard script (v0.91.0): reconcile the
                // injection channels with the user's switch.
                syncGuardScript(wv)
                // Privacy signals (v0.92.0): same reconciliation
                // for the DNT / GPC / WebRTC script.
                syncPrivacyScript(wv)
                // A fresh load is under way; any earlier failure is over.
                loadError = null
                // The old page's preview no longer describes this
                // tab (v0.131.0): drop it now instead of letting
                // the tab sheet show the previous page under the
                // new title until the new page finishes.
                clearThumbnail()
                // The blocked-request counter belongs to one page.
                blockedCount = 0
                // So does the find state (v0.124.0): the WebView
                // drops the old page's matches itself, and the
                // counter must not keep describing them.
                resetFindState()
                // The old page's certificate no longer applies.
                certIssuedTo = ""
                certIssuedBy = ""
            }

            override fun onReceivedError(
                wv: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                super.onReceivedError(wv, request, error)
                // Only main-frame failures replace the page with the
                // app's own error UI; a broken image or script must not.
                // HTTP errors (404/500) never arrive here — the server
                // answers those itself and its page is shown as-is.
                if (!request.isForMainFrame) return
                // The load is over, failed (v0.139.0 loading state).
                loading = false
                // Close out the progress
                // bar too (v0.103.0). WebView progress is driven
                // only by onProgressChanged, and a fast failure can
                // leave it parked mid-way, which would keep the bar
                // hanging over the error page forever. The Gecko
                // side already lands at 100 via onPageStop.
                progress = 100
                val failed = request.url?.toString() ?: url
                val description = error.description?.toString().orEmpty()
                loadError = TabLoadError(
                    failedUrl = failed,
                    reason = webViewErrorReason(error.errorCode),
                    detail = "错误码 ${error.errorCode}" +
                        if (description.isNotBlank()) "：$description" else ""
                )
            }

            override fun onReceivedHttpAuthRequest(
                wv: WebView,
                handler: HttpAuthHandler,
                host: String,
                realm: String
            ) {
                // The site asks for a user name and password (Basic
                // auth: router admin pages, intranet sites, ...). The
                // platform default would cancel the load outright;
                // instead the app shows its own dialog and answers the
                // handler exactly once. Credentials go straight to the
                // handler — never stored, never logged.
                onHttpAuth(
                    HttpAuthRequestInfo(
                        host = host,
                        realm = realm,
                        message = "",
                        prefillUsername = "",
                        onlyPassword = false
                    )
                ) { user, pass ->
                    try {
                        if (user != null) {
                            handler.proceed(user, pass ?: "")
                        } else {
                            handler.cancel()
                        }
                    } catch (t: Throwable) {
                        // The handler may already be answered (tab
                        // closed meanwhile); nothing left to do.
                    }
                }
            }

            override fun onReceivedSslError(
                wv: WebView,
                handler: SslErrorHandler,
                error: android.net.http.SslError
            ) {
                // The certificate is broken. Never proceed silently:
                // the app explains the problem and lets the user
                // choose. "Proceed" applies to this load only and is
                // not remembered. The default (no app answer) refuses.
                val failedUrl = error.url ?: url
                onSslError(
                    failedUrl,
                    sslErrorDescription(error.primaryError)
                ) { proceed ->
                    try {
                        if (proceed) {
                            handler.proceed()
                        } else {
                            handler.cancel()
                        }
                    } catch (t: Throwable) {
                        // Already answered; nothing left to do.
                    }
                }
            }

            override fun onRenderProcessGone(
                wv: WebView,
                detail: RenderProcessGoneDetail
            ): Boolean {
                // The system killed this tab's rendering process (usually
                // under memory pressure). Returning true keeps the app
                // alive; the app side rebuilds this tab in place with a
                // fresh WebView and reloads the page it was on. This only
                // ever fires for WebView tabs — Gecko has no such callback.
                // Crash or not makes no difference: same recovery path.
                onRendererGone(url)
                return true
            }

            override fun onPageFinished(wv: WebView, finishedUrl: String?) {
                super.onPageFinished(wv, finishedUrl)
                // The load is over (v0.139.0 loading state).
                loading = false
                // HTTPS-first marker (v0.77.0): a load that finished
                // without an error state has succeeded, so the tab
                // is no longer "showing an upgraded page that
                // failed". A failed main-frame load also reaches
                // onPageFinished, but onReceivedError has already
                // set loadError by then, so the marker survives
                // exactly for the error page's http fallback button.
                if (loadError == null) httpsUpgraded = false
                val u = finishedUrl ?: return
                if (readerActive) {
                    // A real navigation out of (or the reload ending)
                    // reader mode: hand darkening back to the setting.
                    // Entering reader mode never triggers this — it
                    // injects into the already-loaded page.
                    setAlgorithmicDarkening(webDarkProvider())
                }
                readerActive = false
                        url = u
                title = wv.title ?: ""
                canGoBack = wv.canGoBack()
                canGoForward = wv.canGoForward()
                if (!incognito && u.startsWith("http")) onVisit(title, u)
                // Certificate facts for the site-info sheet: only
                // what the platform actually reports (the CN out of
                // the two DN strings); anything missing stays empty
                // and the sheet hides the row.
                if (u.startsWith("https://")) {
                    try {
                        val cert = wv.certificate
                        certIssuedTo = certCommonName(cert?.getIssuedTo()?.getDName())
                        certIssuedBy = certCommonName(cert?.getIssuedBy()?.getDName())
                    } catch (t: Throwable) {
                        certIssuedTo = ""
                        certIssuedBy = ""
                    }
                }
                // One thumbnail capture per finished load; the other
                // capture point is the tab being switched away from.
                captureThumbnail()
                // Ad-slot hiding: hide the (often empty) ad
                // containers the request blocking left behind.
                // Silent on failure — a page that refuses the
                // injection simply keeps its slots.
                if (isAdHideSlotsEnabled()) {
                    try {
                        wv.evaluateJavascript(AD_HIDE_JS, null)
                    } catch (t: Throwable) {
                        // Injection is best-effort only.
                    }
                }
            }
        }
        view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(wv: WebView, newProgress: Int) {
                progress = newProgress
            }

            override fun onCreateWindow(
                wv: WebView,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message
            ): Boolean {
                // The page asked for a new window (target=_blank or
                // window.open). Standard Android transport: hand the
                // system a throwaway child WebView; the first real URL
                // it heads to is reported back through its client and
                // the app opens it in a real new tab through the usual
                // tab-creation path. The child itself never displays
                // anything. Gesture and non-gesture (popup) opens both
                // go through here; a new window opened from inside the
                // new tab hits this same path again, so there is no
                // separate nesting to handle.
                val child = WebView(wv.context)
                var forwarded = false
                fun forward(target: String?) {
                    if (forwarded) return
                    if (target.isNullOrBlank() || !target.startsWith("http")) return
                    forwarded = true
                    if (popupChild === child) popupChild = null
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        try {
                            child.destroy()
                        } catch (t: Throwable) {
                            // Already gone; nothing to clean up.
                        }
                    }
                    onNewWindowRequest(target)
                }
                child.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        cv: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        forward(request.url?.toString())
                        return true
                    }

                    override fun onPageStarted(
                        cv: WebView,
                        startedUrl: String?,
                        favicon: android.graphics.Bitmap?
                    ) {
                        forward(startedUrl)
                    }
                }
                popupChild = child
                val transport = resultMsg.obj as? WebView.WebViewTransport
                    ?: return false
                transport.webView = child
                resultMsg.sendToTarget()
                return true
            }

            override fun onReceivedTitle(wv: WebView, receivedTitle: String?) {
                if (!receivedTitle.isNullOrBlank()) title = receivedTitle
            }

            override fun onShowFileChooser(
                wv: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                // A web page's file button: hand the request to the app,
                // which opens the system file picker. The app must always
                // answer the callback — with the picked files, or null
                // when the user cancels — or the page waits forever.
                onFileChooserRequest(fileChooserParams, filePathCallback)
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                // A page asking for the camera and/or microphone. Only
                // those two resources are ever handed to the app (which
                // runs the system runtime-permission flow); anything
                // else the page asks for is denied outright.
                val resources = request.resources ?: emptyArray()
                val known = setOf(
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE,
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE
                )
                if (resources.isNotEmpty() && resources.all { it in known }) {
                    onWebPermissionRequest(request)
                } else {
                    request.deny()
                }
            }

            override fun onShowCustomView(
                customView: android.view.View,
                callback: CustomViewCallback
            ) {
                // Re-entry guard (v0.123.0 fullscreen audit): a page
                // that asks for fullscreen while one is already
                // showing (a second video, a re-request from the
                // same element) must be refused at once. Overwriting
                // the stored callback without answering the old one
                // breaks the WebView contract — the first element
                // would never learn its fullscreen ended, and the
                // later exit would only ever notify the newest
                // callback. Standard behaviour (Chrome does the
                // same): answer the NEW callback hidden and keep
                // the fullscreen already on screen.
                if (customViewCallback != null) {
                    try {
                        callback.onCustomViewHidden()
                    } catch (t: Throwable) {
                        // The page is gone; nothing left to answer.
                    }
                    return
                }
                customViewCallback = callback
                onFullscreenShow(customView)
            }

            override fun onHideCustomView() {
                customViewCallback = null
                onFullscreenHide()
            }
        }
        view.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
            findInfo = if (numberOfMatches == 0) {
                "没有找到"
            } else {
                "${activeMatchOrdinal + 1} / $numberOfMatches"
            }
        }
        view.setDownloadListener(
            DownloadListener { dlUrl, userAgent, contentDisposition, mimetype, _ ->
                // URL-type download: the app decides where it goes
                // (system downloader / TuxunDown / ask), per settings.
                onUrlDownload(dlUrl, userAgent, contentDisposition, mimetype)
            }
        )
        webView = view
        // Fingerprint profile (v0.144.0): registered FIRST, so
        // the profile surface is in place before the guard or
        // privacy scripts wrap anything on the page.
        registerFingerprintScriptHandler(view)
        // Page-guard script (v0.91.0): the document-start handler
        // must be registered BEFORE the first loadUrl, or the
        // first page would miss the earliest channel.
        registerGuardScriptHandler(view)
        // Privacy signals (v0.92.0): same earliest-channel rule.
        registerPrivacyScriptHandler(view)
        if (url.isNotBlank() && url != HOME_URL) {
            // The first load is app-initiated too, so the privacy
            // headers ride on it exactly like on BrowserTab.load.
            val headers = privacySignalsProvider().headers()
            if (headers.isEmpty()) {
                view.loadUrl(url)
            } else {
                view.loadUrl(url, headers)
            }
        }
        return view
    }

    /**
     * Whether the page-guard script should be injected into this
     * tab right now: the user's switch is on, this is not an
     * incognito tab (the script keeps permanent per-site statistics
     * in page storage, which contradicts the incognito contract —
     * see Store.guardScriptEnabled), and JavaScript is on (both
     * injection channels are JavaScript; with JS off there is
     * nothing for the script to hook anyway).
     */
    private fun guardScriptWanted(): Boolean =
        guardScriptProvider() && !incognito

    private fun guardDocumentStartSupported(): Boolean =
        try {
            WebViewFeature.isFeatureSupported(
                WebViewFeature.DOCUMENT_START_SCRIPT
            )
        } catch (t: Throwable) {
            false
        }

    /** Register the document-start handler on this tab's WebView. */
    private fun registerGuardScriptHandler(view: WebView) {
        if (!guardScriptWanted() || !guardDocumentStartSupported()) return
        if (guardScriptHandler != null) return
        guardScriptHandler = try {
            WebViewCompat.addDocumentStartJavaScript(
                view, GuardScript.payload(view.context), setOf("*")
            )
        } catch (t: Throwable) {
            // Registration is best-effort: the onPageStarted
            // fallback in syncGuardScript still covers the tab.
            null
        }
    }

    /** Drop the document-start handler, if one is registered. */
    private fun removeGuardScriptHandler() {
        try {
            guardScriptHandler?.remove()
        } catch (t: Throwable) {
            // The WebView may already be gone; nothing to clean up.
        }
        guardScriptHandler = null
    }

    /**
     * Reconcile page-guard injection with the switch at every page
     * start (v0.91.0). Three cases:
     *
     * - Document-start supported and the handler is registered:
     *   the script already ran for this page before any of the
     *   page's own scripts — the earliest channel there is, and
     *   this call is a no-op.
     * - Document-start supported but no handler (the switch was
     *   off when the WebView was created, or the tab just woke
     *   from sleep): register now for later navigations, and give
     *   THIS page the script through evaluateJavascript — later
     *   than document-start, but the script's own guard flag
     *   keeps it from ever running twice on one page.
     * - Document-start unsupported (older WebView): every page
     *   gets the script through evaluateJavascript here in
     *   onPageStarted, the earliest hook that remains.
     *
     * Turning the switch off removes the handler at the next page
     * start; a page that already has the script keeps it until it
     * navigates — injected code cannot be un-injected, and the
     * settings copy does not pretend otherwise.
     */
    private fun syncGuardScript(wv: WebView) {
        val want = guardScriptWanted()
        if (guardDocumentStartSupported()) {
            if (want && guardScriptHandler == null) {
                registerGuardScriptHandler(wv)
                if (guardScriptHandler != null) {
                    try {
                        wv.evaluateJavascript(
                            GuardScript.payload(wv.context), null
                        )
                    } catch (t: Throwable) {
                        // Best-effort only.
                    }
                }
            } else if (!want) {
                removeGuardScriptHandler()
            }
        } else if (want) {
            try {
                wv.evaluateJavascript(GuardScript.payload(wv.context), null)
            } catch (t: Throwable) {
                // Best-effort only.
            }
        }
    }

    /** Register the privacy-signals document-start handler. */
    private fun registerPrivacyScriptHandler(view: WebView) {
        val payload = PrivacyScript.payload(privacySignalsProvider())
            ?: return
        if (!guardDocumentStartSupported()) return
        if (privacyScriptHandler != null &&
            privacyScriptPayload == payload
        ) {
            return
        }
        removePrivacyScriptHandler()
        privacyScriptHandler = try {
            WebViewCompat.addDocumentStartJavaScript(
                view, payload, setOf("*")
            )
        } catch (t: Throwable) {
            null
        }
        privacyScriptPayload =
            if (privacyScriptHandler != null) payload else null
    }

    /** Drop the privacy-signals handler, if one is registered. */
    private fun removePrivacyScriptHandler() {
        try {
            privacyScriptHandler?.remove()
        } catch (t: Throwable) {
            // The WebView may already be gone; nothing to clean up.
        }
        privacyScriptHandler = null
        privacyScriptPayload = null
    }

    /**
     * Reconcile the privacy-signals script with the switches at
     * every page start (v0.92.0). The payload is rebuilt from the
     * live switches each time: all-off removes the handler;
     * a changed payload re-registers (and is also evaluated once
     * for the page that just started, since a registration made
     * now only binds from the next load); on WebViews without the
     * document-start channel the payload goes through
     * evaluateJavascript here, exactly like the guard script's
     * fallback. A page that already ran without a signal keeps
     * that state until it navigates — injected code cannot be
     * un-injected.
     */
    private fun syncPrivacyScript(wv: WebView) {
        val payload = PrivacyScript.payload(privacySignalsProvider())
        if (payload == null) {
            removePrivacyScriptHandler()
            return
        }
        if (guardDocumentStartSupported()) {
            if (privacyScriptHandler == null ||
                privacyScriptPayload != payload
            ) {
                registerPrivacyScriptHandler(wv)
                // A handler (re)registered at page start only
                // takes effect from the NEXT navigation — document-
                // start scripts are bound when a load begins — so
                // give THIS page its signals through the late
                // channel as well, exactly like syncGuardScript
                // does when it registers late. Running the payload
                // twice on one page is harmless: every snippet is
                // idempotent (configurable defineProperty, plain
                // reassignment of the WebRTC stub), which the
                // v0.94.0 coexistence audit verified.
                try {
                    wv.evaluateJavascript(payload, null)
                } catch (t: Throwable) {
                    // Best-effort only.
                }
            }
        } else {
            try {
                wv.evaluateJavascript(payload, null)
            } catch (t: Throwable) {
                // Best-effort only.
            }
        }
    }

    /** Register the fingerprint document-start handler. */
    private fun registerFingerprintScriptHandler(view: WebView) {
        val resolved = fingerprintProfileProvider() ?: return
        val payload = FingerprintScript.payload(resolved)
        if (!guardDocumentStartSupported()) return
        if (fingerprintScriptHandler != null &&
            fingerprintScriptPayload == payload
        ) {
            return
        }
        removeFingerprintScriptHandler()
        fingerprintScriptHandler = try {
            WebViewCompat.addDocumentStartJavaScript(
                view, payload, setOf("*")
            )
        } catch (t: Throwable) {
            null
        }
        fingerprintScriptPayload =
            if (fingerprintScriptHandler != null) payload else null
    }

    /** Drop the fingerprint handler, if one is registered. */
    private fun removeFingerprintScriptHandler() {
        try {
            fingerprintScriptHandler?.remove()
        } catch (t: Throwable) {
            // The WebView may already be gone; nothing to clean up.
        }
        fingerprintScriptHandler = null
        fingerprintScriptPayload = null
    }

    /**
     * Reconcile the fingerprint script with the live profile at
     * every page start (v0.144.0), mirroring syncPrivacyScript:
     * a non-fingerprint tab drops the handler; a changed payload
     * re-registers and is also evaluated once for the page that
     * just started (a registration made now only binds from the
     * next load); without the document-start channel the payload
     * goes through evaluateJavascript. The payload's snippets are
     * idempotent (configurable defineProperty, plain reassignment),
     * so a double run on one page is harmless.
     */
    private fun syncFingerprintScript(wv: WebView) {
        val resolved = fingerprintProfileProvider()
        if (resolved == null) {
            removeFingerprintScriptHandler()
            return
        }
        val payload = FingerprintScript.payload(resolved)
        if (guardDocumentStartSupported()) {
            if (fingerprintScriptHandler == null ||
                fingerprintScriptPayload != payload
            ) {
                registerFingerprintScriptHandler(wv)
                try {
                    wv.evaluateJavascript(payload, null)
                } catch (t: Throwable) {
                    // Best-effort only.
                }
            }
        } else {
            try {
                wv.evaluateJavascript(payload, null)
            } catch (t: Throwable) {
                // Best-effort only.
            }
        }
    }

    private fun buildGeckoSettings(): GeckoSessionSettings {
        val settingsBuilder = GeckoSessionSettings.Builder()
            .usePrivateMode(incognito)
            // JavaScript stays on: pages need it to work at all.
            .allowJavascript(true)
        if (desktopMode) {
            settingsBuilder
                .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP)
                .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_DESKTOP)
        }
        return settingsBuilder.build()
    }

    /**
     * Take over a GeckoSession created by another tab's onNewSession:
     * this (new) tab adopts it and wires up all the delegates. The app
     * calls this synchronously while onNewSession is still running, so
     * the delegates are in place before Gecko starts loading the page.
     */
    fun attachAdoptedGeckoSession(session: GeckoSession, context: Context) {
        geckoSession = session
        configureGeckoSession(session, context.applicationContext)
    }

    private fun configureGeckoSession(session: GeckoSession, appContext: Context) {
        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                s: GeckoSession,
                newUrl: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (newUrl != null) {
                    url = newUrl
                    if (!incognito && newUrl.startsWith("http")) onVisit(title, newUrl)
                }
            }

            override fun onCanGoBack(s: GeckoSession, can: Boolean) {
                canGoBack = can
            }

            override fun onCanGoForward(s: GeckoSession, can: Boolean) {
                canGoForward = can
            }

            override fun onLoadRequest(
                s: GeckoSession,
                request: GeckoSession.NavigationDelegate.LoadRequest
            ): GeckoResult<AllowOrDeny>? {
                // Per-site compatibility memory: a site the user moved to
                // WebView once is handed over to a WebView tab whenever a
                // Gecko tab tries to navigate to it.
                val target = request.uri
                if (target.startsWith("http") && shouldUseWebView(target)) {
                    onCompatRedirect(target)
                    // Denied here (v0.139.0): this session's load
                    // does not proceed; the WebView tab that takes
                    // over tracks its own loading state.
                    loading = false
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                // Navigation-level ad blocking for Gecko (subresource
                // requests are covered by content blocking instead).
                if (target.startsWith("http") && isAdBlockEnabled() &&
                    AdBlocker.isAdRequest(target)
                ) {
                    // A denied navigation ends this load attempt
                    // (v0.139.0 loading state).
                    loading = false
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                // Special links (tel:/mailto:/sms:/geo:/market:/
                // intent:): same shared dispatcher as the WebView
                // side. DENY the navigation either way — Gecko must
                // never try to load these schemes itself — and only
                // hand the link out when a real user gesture (and
                // not a redirect hop) triggered it, so a page cannot
                // silently pop another app open. An http(s) fallback
                // URL the dispatcher returns (from an intent: link)
                // is loaded in this tab as a normal page, posted to
                // the main thread so it does not re-enter the load
                // that is being denied right now.
                if (isSpecialLinkUrl(target)) {
                    if (request.hasUserGesture && !request.isRedirect) {
                        val fallback = dispatchSpecialLink(appContext, target)
                        if (fallback != null) {
                            android.os.Handler(
                                android.os.Looper.getMainLooper()
                            ).post { load(fallback) }
                        }
                    }
                    // Denied either way (v0.139.0 loading state);
                    // a fallback load re-raises it through load().
                    loading = false
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                // A tap on a direct file link: same determination
                // as the WebView side (shared extension set, user
                // gesture only, never a redirect hop — LoadRequest's
                // hasUserGesture/isRedirect fields verified against
                // the GeckoView 132 AAR). DENY the navigation and
                // hand the URL to the app's URL-type download
                // dispatch. This link-tap path is distinct from
                // onExternalResponse below, which is the
                // response-type path (the server answered with a
                // file Gecko cannot display) and keeps saving the
                // stream itself; the two never overlap.
                if (target.startsWith("http") && request.hasUserGesture &&
                    !request.isRedirect && isDirectDownloadUrl(target)
                ) {
                    onDirectDownload(target)
                    // Cancelled into a download (v0.139.0).
                    loading = false
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                return null
            }

            override fun onNewSession(
                s: GeckoSession,
                uri: String
            ): GeckoResult<GeckoSession>? {
                // The page asked for a new window (target=_blank or
                // window.open). Verified against the GeckoView 132 AAR
                // with javap: onNewSession(GeckoSession, String)
                // returns GeckoResult<GeckoSession>, and answering
                // with a new session opens the page in that session.
                // The app creates a real new tab around this session —
                // wiring its delegates synchronously through
                // attachAdoptedGeckoSession — and switches to it.
                val runtime = GeckoHolder.runtime ?: return null
                val newSession = GeckoSession(buildGeckoSettings())
                newSession.open(runtime)
                onGeckoNewSession(newSession, uri)
                return GeckoResult.fromValue(newSession)
            }

            override fun onLoadError(
                s: GeckoSession,
                // @Nullable in the 132 AAR (checked with javap -v): the
                // Kotlin override must declare it nullable to match.
                uri: String?,
                error: WebRequestError
            ): GeckoResult<String>? {
                // Network-level failure (offline, unknown host, ...).
                // Verified against the 132 AAR with javap: onLoadError
                // takes a WebRequestError with public code/category
                // fields and returns GeckoResult<String>; answering
                // null means the app handles the failure itself — here
                // the tab's own error state, which the app renders as
                // its error page. HTTP errors (404/500) never arrive
                // here; the server's own page is shown for those.
                // The load is over, failed (v0.139.0 loading state).
                loading = false
                loadError = TabLoadError(
                    failedUrl = uri ?: url,
                    reason = geckoErrorReason(error),
                    detail = "分类 ${error.category} · 错误码 ${error.code}"
                )
                return null
            }
        }
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onPageStart(s: GeckoSession, startUrl: String) {
                // A fresh load is under way; any earlier failure is over.
                loadError = null
                // Loading state (v0.139.0): also covers navigations
                // the page started itself.
                loading = true
                // Same find-state reset as the WebView side
                // (v0.124.0): a new page kills the find session.
                resetFindState()
            }

            override fun onProgressChange(s: GeckoSession, p: Int) {
                progress = p
            }

            override fun onPageStop(s: GeckoSession, success: Boolean) {
                // The load is over either way (v0.139.0).
                loading = false
                progress = 100
                // HTTPS-first marker (v0.77.0): cleared on a
                // successful stop with no error state, kept when
                // the load failed (onLoadError set loadError), so
                // the error page can offer its http fallback.
                if (success && loadError == null) httpsUpgraded = false
            }
        }
        // GeckoView 132 reports page titles on ContentDelegate, not on
        // NavigationDelegate (where older versions had them).
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(s: GeckoSession, newTitle: String?) {
                if (!newTitle.isNullOrBlank()) title = newTitle
            }

            // Long-press context menu. ContextElement's shape was
            // verified against the GeckoView 132 AAR with javap:
            // public final String linkUri / srcUri / title / altText
            // and public final int type with TYPE_NONE=0,
            // TYPE_IMAGE=1, TYPE_VIDEO=2, TYPE_AUDIO=3. Before
            // v0.42.0 only linkUri was read, so a Gecko long-press on
            // a bare image produced no menu at all.
            override fun onContextMenu(
                s: GeckoSession,
                screenX: Int,
                screenY: Int,
                element: GeckoSession.ContentDelegate.ContextElement
            ) {
                val link = element.linkUri?.takeIf { it.isNotBlank() }
                val src = element.srcUri?.takeIf { it.isNotBlank() }
                val image = if (
                    element.type ==
                    GeckoSession.ContentDelegate.ContextElement.TYPE_IMAGE
                ) src else null
                // A bare video/audio element has no link and no
                // image; present its address through the link side of
                // the combined menu (open / copy / share) rather than
                // mislabeling it as an image.
                val mediaAsLink = if (
                    link == null && image == null && src != null &&
                    (element.type ==
                        GeckoSession.ContentDelegate.ContextElement.TYPE_VIDEO ||
                        element.type ==
                        GeckoSession.ContentDelegate.ContextElement.TYPE_AUDIO)
                ) src else null
                val effectiveLink = link ?: mediaAsLink
                if (effectiveLink != null || image != null) {
                    onPageContextMenu(effectiveLink, image)
                }
            }

            override fun onFullScreen(s: GeckoSession, fullScreen: Boolean) {
                onGeckoFullscreen(fullScreen)
            }

            // Gecko hands responses it cannot display (files to download)
            // to the app as a WebResponse with a body stream. Verified
            // against the GeckoView 132 AAR: ContentDelegate has
            // onExternalResponse(GeckoSession, WebResponse) and
            // WebResponse carries uri/headers (WebMessage) plus a public
            // body InputStream that the app must consume itself.
            override fun onExternalResponse(s: GeckoSession, response: WebResponse) {
                // The body stream is copied on a background thread
                // after this returns; flag the tab as download-busy
                // until the copy finishes so the engine-sleep pass
                // (v0.41.0) never closes this session mid-copy and
                // cuts the download off. Cleared on every exit path
                // of saveGeckoDownload via its completion callback.
                geckoDownloadActive = true
                activeGeckoDownloadId = saveGeckoDownload(
                    appContext, response,
                    onFinished = {
                        geckoDownloadActive = false
                        activeGeckoDownloadId = -1L
                    },
                    onSaved = { fileName, contentUri, filePath, sizeBytes, mimeType, sourceUrl ->
                        // In-app record of the finished download
                        // (v0.57.0), so the downloads screen can list,
                        // open and delete Gecko downloads too.
                        // Incognito is hard-blocked here: the file
                        // itself is saved as usual (the user asked
                        // for it), but an incognito download leaves
                        // no stored record behind.
                        if (!incognito) {
                            try {
                                Store(appContext).addGeckoDownload(
                                    GeckoDownloadRecord(
                                        fileName = fileName,
                                        sourceUrl = sourceUrl,
                                        sizeBytes = sizeBytes,
                                        mimeType = mimeType,
                                        contentUri = contentUri,
                                        filePath = filePath,
                                        time = System.currentTimeMillis()
                                    )
                                )
                            } catch (t: Throwable) {
                                // Recording is best-effort; the file
                                // itself is already saved.
                            }
                        }
                    }
                )
            }
        }
        // SelectionActionDelegate (v0.43.0), verified against the
        // GeckoView 132 AAR with javap: GeckoSession has
        // setSelectionActionDelegate(SelectionActionDelegate); the
        // interface's methods all have default implementations, and
        // onShowActionRequest's parameters are @NonNull.
        // Before v0.43.0 no delegate was set at all, so a Gecko
        // long-press selection produced no menu whatsoever.
        // v0.45.0 adds the two clipboard callbacks, re-verified
        // against the same AAR (javap + the omni.ja modules shipped
        // inside it): onShowClipboardPermissionRequest(session,
        // ClipboardPermission) returns GeckoResult<AllowOrDeny>
        // (@Nullable return, both parameters @NonNull, @UiThread);
        // its default implementation is GeckoResult.deny(), i.e.
        // before this version a page's clipboard-read request was
        // refused silently with no prompt. The engine's own JS
        // (GeckoViewClipboardPermission.sys.mjs, confirmUserPaste)
        // keeps only ONE pending request and rejects a concurrent
        // second one itself, and sends the dismiss message when the
        // request context goes away (pointerdown / teardown) — the
        // Java layer does NOT auto-complete our result on dismiss,
        // so the dismiss override below answers deny through the
        // app, and destroy()/sleepEngine() drain any leftover
        // pending answers the same way. The result is never left
        // hanging: a hanging GeckoResult freezes the page's script.
        session.selectionActionDelegate =
            object : GeckoSession.SelectionActionDelegate {
                override fun onShowActionRequest(
                    s: GeckoSession,
                    selection: GeckoSession.SelectionActionDelegate.Selection
                ) {
                    onGeckoSelectionShow(GeckoSelectionMenu(selection))
                }

                override fun onHideAction(s: GeckoSession, reason: Int) {
                    onGeckoSelectionHide()
                }

                override fun onShowClipboardPermissionRequest(
                    s: GeckoSession,
                    perm: GeckoSession.SelectionActionDelegate
                        .ClipboardPermission
                ): GeckoResult<AllowOrDeny> {
                    val result = GeckoResult<AllowOrDeny>()
                    val answer = ClipboardPermissionAnswer(result) {
                        pendingClipboardAnswers.remove(it)
                    }
                    pendingClipboardAnswers.add(answer)
                    onGeckoClipboardPermissionRequest(
                        perm.uri.orEmpty(), answer
                    )
                    return result
                }

                override fun onDismissClipboardPermissionRequest(
                    s: GeckoSession
                ) {
                    onGeckoClipboardPermissionDismiss()
                }
            }
        // PromptDelegate, verified against the GeckoView 132 AAR with
        // javap: GeckoSession.setPromptDelegate exists; FilePrompt has
        // public type/mimeTypes/capture fields and public
        // confirm(Context, Uri) / confirm(Context, Uri[]) methods;
        // BasePrompt.dismiss() is public. All other PromptDelegate
        // methods have default implementations in 132, so only the
        // ones below are overridden — the rest keep Gecko's baseline
        // behaviour. AlertPrompt has no public confirm() in 132 (the
        // BasePrompt one is protected), so an alert's OK button
        // completes the prompt through dismiss(), which is how an
        // alert (a message with a single button) is acknowledged.
        session.promptDelegate = object : PromptDelegate {
            override fun onFilePrompt(
                s: GeckoSession,
                prompt: PromptDelegate.FilePrompt
            ): GeckoResult<PromptDelegate.PromptResponse> {
                // Same system file picker the WebView side uses; the
                // app answers with the picked files, or an empty list
                // when the user cancels.
                val result = GeckoResult<PromptDelegate.PromptResponse>()
                onGeckoFileChooserRequest(prompt) { uris ->
                    try {
                        when {
                            uris.isEmpty() -> result.complete(prompt.dismiss())
                            prompt.type == PromptDelegate.FilePrompt.Type.MULTIPLE ->
                                result.complete(
                                    prompt.confirm(appContext, uris.toTypedArray())
                                )
                            else -> result.complete(
                                prompt.confirm(appContext, uris.first())
                            )
                        }
                    } catch (t: Throwable) {
                        // The prompt may already be answered (tab closed
                        // meanwhile); never let that crash the app.
                        try {
                            result.complete(prompt.dismiss())
                        } catch (t2: Throwable) {
                            // Already answered; nothing left to do.
                        }
                    }
                }
                return result
            }

            override fun onAlertPrompt(
                s: GeckoSession,
                prompt: PromptDelegate.AlertPrompt
            ): GeckoResult<PromptDelegate.PromptResponse> {
                val result = GeckoResult<PromptDelegate.PromptResponse>()
                onGeckoJsPrompt(
                    GeckoJsPromptKind.ALERT, prompt.message ?: "", ""
                ) { _, _ ->
                    try {
                        result.complete(prompt.dismiss())
                    } catch (t: Throwable) {
                        // Already answered; nothing left to do.
                    }
                }
                return result
            }

            override fun onButtonPrompt(
                s: GeckoSession,
                prompt: PromptDelegate.ButtonPrompt
            ): GeckoResult<PromptDelegate.PromptResponse> {
                // JavaScript confirm(): OK is the positive button,
                // Cancel the negative one (ButtonPrompt.Type values
                // verified in the 132 AAR: POSITIVE=0, NEGATIVE=2).
                val result = GeckoResult<PromptDelegate.PromptResponse>()
                onGeckoJsPrompt(
                    GeckoJsPromptKind.CONFIRM, prompt.message ?: "", ""
                ) { confirmed, _ ->
                    try {
                        result.complete(
                            prompt.confirm(
                                if (confirmed) {
                                    PromptDelegate.ButtonPrompt.Type.POSITIVE
                                } else {
                                    PromptDelegate.ButtonPrompt.Type.NEGATIVE
                                }
                            )
                        )
                    } catch (t: Throwable) {
                        // Already answered; nothing left to do.
                    }
                }
                return result
            }

            override fun onTextPrompt(
                s: GeckoSession,
                prompt: PromptDelegate.TextPrompt
            ): GeckoResult<PromptDelegate.PromptResponse> {
                // JavaScript prompt(): confirming hands the typed text
                // back; cancelling dismisses, which makes prompt()
                // return null in the page, as it should.
                val result = GeckoResult<PromptDelegate.PromptResponse>()
                onGeckoJsPrompt(
                    GeckoJsPromptKind.TEXT,
                    prompt.message ?: "",
                    prompt.defaultValue ?: ""
                ) { confirmed, text ->
                    try {
                        result.complete(
                            if (confirmed) {
                                prompt.confirm(text ?: "")
                            } else {
                                prompt.dismiss()
                            }
                        )
                    } catch (t: Throwable) {
                        // Already answered; nothing left to do.
                    }
                }
                return result
            }

            override fun onAuthPrompt(
                s: GeckoSession,
                prompt: PromptDelegate.AuthPrompt
            ): GeckoResult<PromptDelegate.PromptResponse> {
                // HTTP Basic-auth challenge. Verified against the
                // GeckoView 132 AAR with javap: AuthPrompt has public
                // final message + authOptions; AuthOptions has public
                // final flags/uri/level/username/password; the Flags
                // constants are HOST=1, PROXY=2, ONLY_PASSWORD=8,
                // PREVIOUS_FAILED=16 (raw class bytes read for
                // ONLY_PASSWORD). confirm(String) answers with a
                // password only, confirm(String, String) with user
                // name + password, and dismiss() cancels. The app
                // shows the same dialog as the WebView side; the
                // credentials are never stored or logged.
                val result = GeckoResult<PromptDelegate.PromptResponse>()
                val options = prompt.authOptions
                val onlyPassword = options.flags and
                    PromptDelegate.AuthPrompt.AuthOptions.Flags.ONLY_PASSWORD != 0
                // options.uri is @Nullable in the 132 AAR.
                val authUri = options.uri ?: ""
                onHttpAuth(
                    HttpAuthRequestInfo(
                        host = hostOf(authUri) ?: authUri,
                        realm = "",
                        message = prompt.message ?: "",
                        prefillUsername = options.username ?: "",
                        onlyPassword = onlyPassword
                    )
                ) { user, pass ->
                    try {
                        result.complete(
                            when {
                                user == null -> prompt.dismiss()
                                onlyPassword -> prompt.confirm(pass ?: "")
                                else -> prompt.confirm(user ?: "", pass ?: "")
                            }
                        )
                    } catch (t: Throwable) {
                        // Already answered; nothing left to do.
                        try {
                            result.complete(prompt.dismiss())
                        } catch (t2: Throwable) {
                            // Already answered; nothing left to do.
                        }
                    }
                }
                return result
            }
        }
        // PermissionDelegate, also verified against the 132 AAR:
        // setPermissionDelegate exists; onAndroidPermissionsRequest
        // takes (session, String[], Callback with grant()/reject());
        // onContentPermissionRequest returns GeckoResult<Int> with
        // ContentPermission VALUE_ALLOW=1 / VALUE_DENY=2; media
        // (camera/microphone) requests arrive through
        // onMediaPermissionRequest with MediaSource arrays and a
        // MediaCallback (there is no PermissionRequest class in 132).
        // The app side runs the actual system-permission flow.
        session.permissionDelegate = object : PermissionDelegate {
            override fun onAndroidPermissionsRequest(
                s: GeckoSession,
                // @Nullable in the 132 AAR (checked with javap -v): the
                // Kotlin override must declare it nullable to match.
                permissions: Array<String>?,
                callback: PermissionDelegate.Callback
            ) {
                onGeckoAndroidPermissions(permissions ?: emptyArray(), callback)
            }

            override fun onContentPermissionRequest(
                s: GeckoSession,
                perm: PermissionDelegate.ContentPermission
            ): GeckoResult<Int> =
                GeckoResult.fromValue(onGeckoContentPermission(perm))

            override fun onMediaPermissionRequest(
                s: GeckoSession,
                uri: String,
                video: Array<PermissionDelegate.MediaSource>?,
                audio: Array<PermissionDelegate.MediaSource>?,
                callback: PermissionDelegate.MediaCallback
            ) {
                onGeckoMediaPermission(uri, video, audio, callback)
            }
        }
    }

    /**
     * Create (or return) this tab's GeckoView. Returns null when
     * the Gecko side cannot be created (v0.81.0, aligned with the
     * WebView side): GeckoHolder.ensure already turns a failed
     * GeckoRuntime.create into null, and any exception from the
     * session / view creation below lands in the same place — the
     * tab is marked [engineUnavailable] and the app shows its
     * fallback page (with a switch-to-WebView action) instead of
     * the bare line of text this used to fall back to, or a crash.
     */
    fun obtainGecko(context: Context): GeckoView? {
        geckoView?.let { return it }
        val view = try {
            createGeckoView(context)
        } catch (t: Throwable) {
            null
        }
        if (view == null) {
            engineUnavailable = true
            // No load can be in flight without an engine (v0.139.0).
            loading = false
        }
        return view
    }

    private fun createGeckoView(context: Context): GeckoView? {
        val appContext = context.applicationContext
        val runtime = GeckoHolder.ensure(context) ?: return null
        val adopted = geckoSession != null
        val session: GeckoSession
        if (adopted) {
            // Session handed over from another tab's onNewSession:
            // already open, delegates already wired by
            // attachAdoptedGeckoSession, and Gecko itself is loading
            // the requested page into it — do not load it again.
            session = geckoSession!!
        } else {
            session = GeckoSession(buildGeckoSettings())
            configureGeckoSession(session, appContext)
            session.open(runtime)
            geckoSession = session
        }
        val view = GeckoView(context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.setSession(session)
        geckoView = view
        if (!adopted && url.isNotBlank() && url != HOME_URL) session.loadUri(url)
        return view
    }
}

/**
 * Extract the RFC 5987 "filename*" parameter from a
 * Content-Disposition header, percent-decoded with its declared
 * charset (UTF-8 in practice). Returns null when there is no
 * filename* or it does not parse.
 *
 * This exists because URLUtil.guessFileName only understands
 * filename* on Android 15+ for apps targeting SDK 35 (the RFC 6266
 * compat change); on every older system it uses the RFC 2616
 * parser, whose pattern knows only plain "filename=" — a server
 * that sends ONLY filename* (common for non-ASCII names) would
 * lose its suggested name entirely and the download would fall
 * back to whatever the URL path happens to end with.
 */
fun rfc5987FileName(contentDisposition: String?): String? {
    if (contentDisposition.isNullOrBlank()) return null
    val marker = "filename*="
    val start = contentDisposition.indexOf(marker, ignoreCase = true)
    if (start < 0) return null
    var value = contentDisposition.substring(start + marker.length).trim()
    if (value.startsWith("\"")) {
        val end = value.indexOf('"', 1)
        if (end <= 0) return null
        value = value.substring(1, end)
    } else {
        value = value.substringBefore(";").trim()
    }
    // Shape: charset'lang'percent-encoded-value
    val q1 = value.indexOf('\'')
    val q2 = if (q1 >= 0) value.indexOf('\'', q1 + 1) else -1
    if (q1 < 0 || q2 < 0) return null
    val charsetName = value.substring(0, q1).ifBlank { "UTF-8" }
    val encoded = value.substring(q2 + 1)
    if (encoded.isBlank()) return null
    return try {
        val charset = java.nio.charset.Charset.forName(charsetName)
        val bytes = java.io.ByteArrayOutputStream(encoded.length)
        var i = 0
        while (i < encoded.length) {
            val c = encoded[i]
            if (c == '%' && i + 2 < encoded.length) {
                val hi = Character.digit(encoded[i + 1], 16)
                val lo = Character.digit(encoded[i + 2], 16)
                if (hi >= 0 && lo >= 0) {
                    bytes.write(hi * 16 + lo)
                    i += 3
                    continue
                }
            }
            bytes.write(c.code and 0xFF)
            i++
        }
        String(bytes.toByteArray(), charset).trim().ifBlank { null }
    } catch (t: Throwable) {
        null
    }
}

/**
 * Clean a server- or URL-suggested file name before it is used as
 * an on-disk name: drop any directory part (either separator —
 * the RFC 2616 parser in URLUtil only strips '/'), remove control
 * characters and quotes, trim leading/trailing dots and spaces so
 * ".." or ". ." can never survive as a name, and cap the length.
 * May return blank; callers substitute a fallback.
 */
fun sanitizeDownloadFileName(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    var name = raw.trim().replace('\\', '/').substringAfterLast('/').trim()
    name = name.filter { it.code >= 0x20 && it != '"' }
    name = name.trim().trimStart { it == '.' || it == ' ' }
        .trimEnd { it == '.' || it == ' ' }
    if (name.length > 120) {
        val dot = name.lastIndexOf('.')
        val ext = if (dot > 0 && name.length - dot <= 11) name.substring(dot) else ""
        name = name.substring(0, 120 - ext.length) + ext
    }
    // Filesystem name limits are in BYTES (255 on the storage this
    // app writes to), not characters: a 120-character CJK name is
    // 360 UTF-8 bytes and would fail every save with a too-long
    // name. Cap the UTF-8 byte length at 200 as well, keeping the
    // extension (v0.112.0 download-path audit).
    val byteLimit = 200
    if (name.toByteArray(Charsets.UTF_8).size > byteLimit) {
        val dot = name.lastIndexOf('.')
        val ext = if (dot > 0 && name.length - dot <= 11) name.substring(dot) else ""
        val extBytes = ext.toByteArray(Charsets.UTF_8).size
        var base = name.substring(0, name.length - ext.length)
        while (base.isNotEmpty() &&
            base.toByteArray(Charsets.UTF_8).size + extBytes > byteLimit
        ) {
            base = base.dropLast(1)
        }
        name = base + ext
    }
    return name
}

/** Extension (with dot) URLUtil would suggest for a MIME type. */
private fun downloadExtensionForMime(mimeType: String?): String {
    val ext = mimeType?.let {
        android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(it)
    }
    if (!ext.isNullOrBlank()) return ".$ext"
    return when {
        mimeType.equals("text/html", ignoreCase = true) -> ".html"
        mimeType?.startsWith("text/", ignoreCase = true) == true -> ".txt"
        else -> ".bin"
    }
}

/**
 * The one place download file names are decided, used by both the
 * DownloadManager path and the Gecko save path:
 * 1. An RFC 5987 filename* in the Content-Disposition wins (per
 *    RFC 6266 it outranks filename=), decoded by rfc5987FileName
 *    so it works on systems whose URLUtil predates RFC 6266.
 * 2. Otherwise URLUtil.guessFileName (URL path / filename= /
 *    MIME-derived extension; it already strips the query string,
 *    so download.php?id=123 with an APK MIME type becomes
 *    download.apk, and with no MIME type keeps download.php).
 * 3. Whatever wins is run through sanitizeDownloadFileName, and
 *    a blank result falls back to downloadfile + MIME extension
 *    instead of an empty or extension-less oddity.
 */
fun resolveDownloadFileName(
    url: String,
    contentDisposition: String?,
    mimeType: String?
): String {
    val star = sanitizeDownloadFileName(rfc5987FileName(contentDisposition))
    if (star.isNotBlank()) {
        return if (star.indexOf('.') >= 0) star
        else star + downloadExtensionForMime(mimeType)
    }
    val guessed = sanitizeDownloadFileName(
        android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType)
    )
    return guessed.ifBlank { "downloadfile" + downloadExtensionForMime(mimeType) }
}

private val geckoDownloadExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

/** One in-progress Gecko download, as the downloads screen renders it. */
data class GeckoDownloadProgress(
    val id: Long,
    val fileName: String,
    val sourceUrl: String,
    /** Content-Length of the response, or -1 when the server sent none. */
    val totalBytes: Long,
    val downloadedBytes: Long
)

/**
 * Live registry of in-progress Gecko downloads (v0.69.0). Completed
 * downloads keep living in the persisted [GeckoDownloadRecord] list
 * exactly as before; this registry only covers the copy while it is
 * running, so the downloads screen can show progress and offer a
 * cancel button. It is in-memory only: if the process dies mid-copy
 * the entry simply vanishes with it (the partial file is a pending
 * MediaStore entry the system cleans up, or an app-folder file the
 * next successful download overwrites by name-clash rules — no
 * zombie record is ever persisted). Cancellation is cooperative:
 * [requestCancel] sets a flag the copy loop checks between chunks
 * AND closes the body stream to wake a read blocked on it; the copy
 * loop then unwinds through the same cleanup path as a failure,
 * deleting the partial artefact, except the completion toast says
 * the download was cancelled.
 */
object GeckoDownloadTracker {
    val active = mutableStateListOf<GeckoDownloadProgress>()
    private val nextId = java.util.concurrent.atomic.AtomicLong(1L)
    private val cancelFlags =
        java.util.concurrent.ConcurrentHashMap<
            Long, java.util.concurrent.atomic.AtomicBoolean>()
    private val openStreams =
        java.util.concurrent.ConcurrentHashMap<Long, java.io.InputStream>()

    fun begin(
        fileName: String,
        sourceUrl: String,
        totalBytes: Long,
        body: java.io.InputStream
    ): Long {
        val id = nextId.getAndIncrement()
        cancelFlags[id] =
            java.util.concurrent.atomic.AtomicBoolean(false)
        openStreams[id] = body
        active.add(
            GeckoDownloadProgress(id, fileName, sourceUrl, totalBytes, 0L)
        )
        return id
    }

    fun updateProgress(id: Long, downloadedBytes: Long) {
        val idx = active.indexOfFirst { it.id == id }
        if (idx >= 0) {
            active[idx] = active[idx].copy(downloadedBytes = downloadedBytes)
        }
    }

    fun isCancelled(id: Long): Boolean = cancelFlags[id]?.get() == true

    fun requestCancel(id: Long) {
        cancelFlags[id]?.set(true)
        // Closing the body stream wakes a read blocked on it; the
        // copy loop's flag check covers streams whose close does not
        // interrupt a pending read.
        try {
            openStreams[id]?.close()
        } catch (t: Throwable) {
            // Best-effort; the flag check still stops the copy.
        }
    }

    fun end(id: Long) {
        active.removeAll { it.id == id }
        cancelFlags.remove(id)
        openStreams.remove(id)
    }
}

/**
 * Copy a Gecko download body to [out] in chunks (v0.69.0), reporting
 * progress to [GeckoDownloadTracker] at most every 256 KB and
 * honouring cancellation between chunks. Throws when the copy is
 * cancelled, so the caller unwinds through its normal failure
 * cleanup (partial artefact deleted) instead of finishing the file.
 * Returns the number of bytes copied.
 */
private fun copyDownloadBody(
    body: java.io.InputStream,
    out: java.io.OutputStream,
    downloadId: Long
): Long {
    val buffer = ByteArray(64 * 1024)
    var total = 0L
    var lastReport = 0L
    while (true) {
        if (GeckoDownloadTracker.isCancelled(downloadId)) {
            throw java.io.IOException("gecko download cancelled")
        }
        val read = body.read(buffer)
        if (read < 0) break
        if (read == 0) continue
        out.write(buffer, 0, read)
        total += read
        if (total - lastReport >= 256L * 1024L) {
            lastReport = total
            GeckoDownloadTracker.updateProgress(downloadId, total)
        }
    }
    GeckoDownloadTracker.updateProgress(downloadId, total)
    return total
}

/**
 * Save a Gecko "external response" (a download) to the public Downloads
 * folder. The body stream already carries the Gecko session's cookies and
 * request state, so unlike a DownloadManager re-request it also works for
 * downloads that need a login. On Android 10+ the file lands in the system
 * Downloads folder via MediaStore (no permission needed); on older systems
 * it goes to the app's own download folder, which needs no permission
 * either.
 */
fun saveGeckoDownload(
    context: Context,
    response: WebResponse,
    onFinished: () -> Unit = {},
    onSaved: ((
        fileName: String,
        contentUri: String?,
        filePath: String?,
        sizeBytes: Long,
        mimeType: String?,
        sourceUrl: String
    ) -> Unit)? = null
): Long {
    val uri = response.uri
    val headers = response.headers
    fun header(name: String): String? = headers.entries
        .firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
    val contentType = header("Content-Type")?.substringBefore(";")?.trim()
    val fileName = resolveDownloadFileName(
        uri, header("Content-Disposition"), contentType
    )
    val body = response.body
    if (body == null) {
        Toast.makeText(context, "这个文件没有可下载的内容", Toast.LENGTH_LONG).show()
        onFinished()
        return -1L
    }
    // Register with the live progress tracker before the copy starts
    // (v0.69.0), so the downloads screen can show this download —
    // and cancel it — from the first moment. The total is the
    // response Content-Length when the server sent one.
    val totalBytes = header("Content-Length")?.trim()?.toLongOrNull() ?: -1L
    val downloadId = GeckoDownloadTracker.begin(fileName, uri, totalBytes, body)
    Toast.makeText(context, "开始下载：$fileName", Toast.LENGTH_LONG).show()
    try {
        geckoDownloadExecutor.execute {
        var ok = false
        var wasCancelled = false
        // The name the file actually lands under. MediaStore never
        // overwrites an existing file: on a name clash it saves
        // under an adjusted name instead, so the completion toast
        // below reports this queried-back name, not the requested
        // one, to avoid "downloaded but can't find it" confusion.
        var savedName = fileName
        var destUri: android.net.Uri? = null
        var destFile: java.io.File? = null
        // Bytes actually copied; reported to the record callback
        // below on success (v0.57.0).
        var copiedBytes = 0L
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                    if (!contentType.isNullOrBlank()) {
                        put(android.provider.MediaStore.Downloads.MIME_TYPE, contentType)
                    }
                    put(
                        android.provider.MediaStore.Downloads.RELATIVE_PATH,
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
                }
                val dest = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                )
                destUri = dest
                if (dest != null) {
                    // A copy that produced zero bytes is a failure,
                    // not a downloaded file (redirect/error bodies
                    // can be empty); the pending entry is deleted
                    // below instead of staying behind as a 0 KB fake.
                    var copied = -1L
                    resolver.openOutputStream(dest)?.use { out ->
                        copied = copyDownloadBody(body, out, downloadId)
                    }
                    if (copied > 0) {
                        copiedBytes = copied.toLong()
                        val done = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                        }
                        resolver.update(dest, done, null, null)
                        ok = true
                        try {
                            resolver.query(
                                dest,
                                arrayOf(
                                    android.provider.MediaStore.Downloads.DISPLAY_NAME
                                ),
                                null, null, null
                            )?.use { cur ->
                                if (cur.moveToFirst()) {
                                    val actual = cur.getString(0)
                                    if (!actual.isNullOrBlank()) savedName = actual
                                }
                            }
                        } catch (t: Throwable) {
                            // Keep the requested name in the toast.
                        }
                    }
                }
            } else {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                // Never overwrite an existing file (v0.112.0
                // download-path audit): the MediaStore path above
                // and the system DownloadManager both keep the old
                // file and save under an adjusted name, and the
                // downloads screen tells the user exactly that —
                // but a plain FileOutputStream here would silently
                // truncate the old file. Pick a clash-free name the
                // same way instead.
                var file = java.io.File(dir, fileName)
                if (file.exists()) {
                    val dot = fileName.lastIndexOf('.')
                    val base = if (dot > 0) fileName.substring(0, dot) else fileName
                    val ext = if (dot > 0) fileName.substring(dot) else ""
                    var n = 1
                    while (file.exists()) {
                        file = java.io.File(dir, "$base ($n)$ext")
                        n++
                    }
                }
                destFile = file
                val copied = file.outputStream().use { out ->
                    copyDownloadBody(body, out, downloadId)
                }
                ok = copied > 0
                if (ok) {
                    copiedBytes = copied
                    savedName = file.name
                }
            }
        } catch (t: Throwable) {
            ok = false
            // A cancelled copy lands here too (the chunk loop throws
            // on the cancel flag, or the closed stream fails the
            // read); remember which, for the completion toast below.
            wasCancelled = GeckoDownloadTracker.isCancelled(downloadId)
        } finally {
            try {
                body.close()
            } catch (t: Throwable) {
                // Nothing useful to do about a failed close.
            }
            if (!ok) {
                // Never leave a half-written or 0 KB file behind and
                // call it a download: remove the partial artefact.
                try {
                    destUri?.let { context.contentResolver.delete(it, null, null) }
                } catch (t: Throwable) {
                    // Best-effort cleanup.
                }
                try {
                    destFile?.delete()
                } catch (t: Throwable) {
                    // Best-effort cleanup.
                }
            }
            // The stream copy is over, one way or the other: the
            // owning tab is no longer download-busy, so the engine
            // sleep pass may treat it like any other tab again.
            try {
                onFinished()
            } catch (t: Throwable) {
                // The callback only clears a flag; never crash here.
            }
            // The live progress entry is retired AFTER the record
            // callback below, not here (v0.86.0 mirror audit): the
            // downloads screen reloads its records when a tracker
            // entry disappears, and retiring the entry first raced
            // that reload ahead of the record write — the finished
            // download briefly existed in neither section.
        }
        if (ok) {
            // Success point (v0.57.0): everything the in-app record
            // needs is in hand here — the actual on-disk name, the
            // MediaStore entry URI (or the file path below
            // Android 10), the copied byte count, the MIME type and
            // the response URL. Failures never reach this call: a
            // failed copy has already deleted its partial artefact,
            // so there is no file a record could point at.
            try {
                onSaved?.invoke(
                    savedName,
                    destUri?.toString(),
                    destFile?.absolutePath,
                    copiedBytes,
                    contentType,
                    uri
                )
            } catch (t: Throwable) {
                // Recording must never break the download itself.
            }
        }
        // The live progress entry dies now (success, failure and
        // cancel alike): a finished download is represented by its
        // persisted record (on success, written just above) or by
        // nothing at all — never by a stale progress row.
        GeckoDownloadTracker.end(downloadId)
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(
                context,
                when {
                    ok -> "已下载：$savedName（在下载文件夹）"
                    wasCancelled -> "已取消下载：$fileName"
                    else -> "下载失败：$fileName"
                },
                Toast.LENGTH_LONG
            ).show()
        }
        }
    } catch (t: Throwable) {
        // The copy never even started (executor rejected it): the
        // tab is not download-busy after all, and neither is the
        // progress tracker. The body stream is closed here too
        // (v0.107.0 release audit): the copy task's finally block
        // owns the close once the task has started, but a rejected
        // submission never runs that task, so without this close
        // the response stream would leak.
        try {
            body.close()
        } catch (t2: Throwable) {
            // Nothing useful to do about a failed close.
        }
        GeckoDownloadTracker.end(downloadId)
        onFinished()
    }
    return downloadId
}

/**
 * Hand a URL to the system DownloadManager with the full set of
 * request headers a browser download needs:
 * - User-Agent: the one the DownloadListener reported; when the
 *   caller has none (sniffer, image save, retry), the WebView
 *   default UA for this device, fetched fresh here.
 * - Cookie: the current WebView session's cookie for this URL,
 *   fetched fresh at enqueue time (never stored, never logged).
 * - Referer: the page the download was started from, when the
 *   caller knows it. Many direct-file sites (and CDN links) refuse
 *   requests without a Referer — the missing header was the root
 *   cause of 0-byte failures fixed in v0.20.0.
 * Headers are only ever put on the request itself; they are not
 * written to disk or logs anywhere.
 */
fun startSystemDownload(
    context: Context,
    url: String?,
    userAgent: String?,
    contentDisposition: String?,
    mimetype: String?,
    referer: String? = null
) {
    if (url.isNullOrBlank()) return
    try {
        val uri = Uri.parse(url)
        // Name decided by the shared resolver: RFC 5987 filename*
        // support on all Android versions, path separators and
        // ".." cleaned out, and a downloadfile.<ext> fallback
        // instead of a blank or extension-less name.
        val fileName = resolveDownloadFileName(url, contentDisposition, mimetype)
        val request = DownloadManager.Request(uri)
            .setTitle(fileName)
            .setDescription(url)
            .setMimeType(mimetype)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        val ua = userAgent?.takeIf { it.isNotBlank() } ?: try {
            android.webkit.WebSettings.getDefaultUserAgent(context)
        } catch (t: Throwable) {
            null
        }
        if (!ua.isNullOrBlank()) request.addRequestHeader("User-Agent", ua)
        val cookie = CookieManager.getInstance().getCookie(url)
        if (!cookie.isNullOrBlank()) request.addRequestHeader("Cookie", cookie)
        if (!referer.isNullOrBlank() &&
            (referer.startsWith("http://") || referer.startsWith("https://"))
        ) {
            request.addRequestHeader("Referer", referer)
        }
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
        Toast.makeText(context, "已交给系统下载：$fileName", Toast.LENGTH_LONG).show()
    } catch (t: Throwable) {
        Toast.makeText(context, "下载启动失败，请换一个链接重试", Toast.LENGTH_LONG).show()
    }
}

private val screenshotExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

private fun screenshotFileName(): String =
    "tuxunbrowse-" + java.text.SimpleDateFormat(
        "yyyyMMdd-HHmmss", java.util.Locale.US
    ).format(java.util.Date()) + ".png"

/**
 * Insert a PNG into the system gallery under Pictures/TuxunBrowse
 * through MediaStore (Android 10+, no permission needed) and return
 * its content Uri. On older systems the file is written into the
 * app's own pictures folder and registered in MediaStore by path,
 * which is what makes its Uri shareable without a FileProvider.
 */
private fun insertScreenshotPng(
    context: Context,
    bitmap: Bitmap,
    fileName: String
): Uri? {
    val resolver = context.contentResolver
    return try {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(
                    android.provider.MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/TuxunBrowse"
                )
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ) ?: return null
            val written = resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            } ?: false
            if (!written) {
                resolver.delete(uri, null, null)
                return null
            }
            val done = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(uri, done, null, null)
            uri
        } else {
            val dir = java.io.File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: context.filesDir,
                "TuxunBrowse"
            )
            dir.mkdirs()
            val file = java.io.File(dir, fileName)
            java.io.FileOutputStream(file).use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) return null
            }
            val values = android.content.ContentValues().apply {
                put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
                @Suppress("DEPRECATION")
                put(android.provider.MediaStore.Images.Media.DATA, file.absolutePath)
            }
            resolver.insert(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            )
        }
    } catch (t: Throwable) {
        null
    }
}

/**
 * Save a page screenshot. Android 10+ lands in the system gallery
 * (Pictures/TuxunBrowse) with no permission; older systems write to
 * the app's own pictures folder. onDone receives a plain-language
 * description of where the file went, or null on failure. Takes
 * ownership of [bitmap] and recycles it when done.
 */
fun savePageScreenshot(
    context: Context,
    bitmap: Bitmap,
    onDone: (String?) -> Unit
) {
    val appContext = context.applicationContext
    screenshotExecutor.execute {
        var where: String? = null
        try {
            val name = screenshotFileName()
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                if (insertScreenshotPng(appContext, bitmap, name) != null) {
                    where = "相册的「图片 / TuxunBrowse」文件夹"
                }
            } else {
                val dir = java.io.File(
                    appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        ?: appContext.filesDir,
                    "TuxunBrowse"
                )
                dir.mkdirs()
                val file = java.io.File(dir, name)
                val ok = java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                if (ok) where = "应用目录：${file.absolutePath}"
            }
        } catch (t: Throwable) {
            where = null
        } finally {
            try {
                bitmap.recycle()
            } catch (t: Throwable) {
                // Already recycled; nothing to do.
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).post { onDone(where) }
    }
}

/**
 * Share a page screenshot through the system share sheet: the PNG
 * is inserted into MediaStore and its content Uri is shared with a
 * read grant (no FileProvider needed). Takes ownership of [bitmap]
 * and recycles it when done. Failures are reported with a toast.
 */
fun sharePageScreenshot(context: Context, bitmap: Bitmap) {
    val appContext = context.applicationContext
    screenshotExecutor.execute {
        var uri: Uri? = null
        try {
            uri = insertScreenshotPng(appContext, bitmap, screenshotFileName())
        } catch (t: Throwable) {
            uri = null
        } finally {
            try {
                bitmap.recycle()
            } catch (t: Throwable) {
                // Already recycled; nothing to do.
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            if (uri == null) {
                Toast.makeText(
                    appContext, "截图保存失败，没法分享，请重试",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                try {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_SEND
                    )
                        .setType("image/png")
                        .putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    appContext.startActivity(
                        android.content.Intent.createChooser(intent, "分享截图")
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (t: Throwable) {
                    Toast.makeText(
                        appContext, "没有能分享截图的应用",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

/**
 * The one shared set of "this link points at a downloadable file"
 * extensions. Two features read it, so it lives exactly once:
 * the page resource sniffer (its JS below is generated from these
 * sets) and the direct-download navigation interception in both
 * engines ([isDirectDownloadUrl]).
 */
val SNIFF_FILE_EXTENSIONS: Set<String> = setOf(
    "zip", "rar", "7z", "apk", "pdf",
    "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt",
    "mp4", "mp3", "mov", "mkv", "avi"
)

/** Media extensions the sniffer also collects from <link> tags. */
val SNIFF_MEDIA_EXTENSIONS: Set<String> = setOf(
    "mp4", "mp3", "mov", "mkv", "avi",
    "webm", "m4a", "wav", "ogg", "flac"
)

/** Every extension that marks a URL as a direct file download. */
val DOWNLOAD_EXTENSIONS: Set<String> =
    SNIFF_FILE_EXTENSIONS + SNIFF_MEDIA_EXTENSIONS

/**
 * True when [url]'s path (query string and fragment stripped) ends
 * in one of the shared download extensions. Used to turn a tap on a
 * direct .apk/.zip/... link into a download instead of a navigation
 * that can only fail — WebView would otherwise try to render the
 * file as a page and land on an error screen.
 */
fun isDirectDownloadUrl(url: String): Boolean {
    val clean = url.substringBefore('?').substringBefore('#')
    val name = clean.substringAfterLast('/')
    val dot = name.lastIndexOf('.')
    if (dot < 0 || dot == name.length - 1) return false
    return name.substring(dot + 1).lowercase() in DOWNLOAD_EXTENSIONS
}

/**
 * Schemes a web page can link to that neither engine can load as a
 * page: tapping one must be handed to the system (dialer, messaging,
 * mail, maps, app store, or whatever app an intent: link names)
 * instead of landing on an error page. Deliberately NOT in this set:
 * javascript:/data:/file: and friends — those stay refused outright
 * (the v0.31.0 inbound-guard口径), they are never handed out.
 */
val SPECIAL_LINK_SCHEMES: Set<String> = setOf(
    "tel", "telprompt", "sms", "smsto", "mailto", "geo", "market", "intent"
)

/** The lower-cased scheme of [url] when it is a special link, else null. */
fun specialLinkScheme(url: String): String? {
    val scheme = url.substringBefore(':', "").lowercase()
    return scheme.takeIf { it in SPECIAL_LINK_SCHEMES }
}

fun isSpecialLinkUrl(url: String): Boolean = specialLinkScheme(url) != null

/**
 * Normalize a web page's file-picker accept hint into a MIME type the
 * system document picker can actually launch with. Pages may declare
 * accept as a real MIME type (say an image type with a subtype or a
 * wildcard subtype), as a bare extension (".pdf"), or as a comma
 * list; the engines hand those raw strings through (WebView
 * FileChooserParams.acceptTypes, Gecko FilePrompt.mimeTypes). An
 * extension or a list is NOT a valid intent type — the picker either
 * finds nothing to show or fails to launch, and the request would be
 * answered as a silent cancel. Anything that is not a single
 * type-slash-subtype shape therefore falls back to the pick-anything
 * type; a valid single MIME type passes through trimmed.
 */
fun normalizePickerMime(raw: String?): String {
    val candidate = raw?.trim().orEmpty()
    if (candidate.isEmpty() || ',' in candidate) return "*/*"
    val slash = candidate.indexOf('/')
    if (slash <= 0 || slash == candidate.length - 1) return "*/*"
    if (candidate.any { it.isWhitespace() }) return "*/*"
    return candidate
}

/**
 * One query parameter of a special link, URL-decoded. These links are
 * usually opaque URIs (mailto:a@b?subject=x), where android.net.Uri
 * reports no query at all, so the query string is split by hand.
 */
private fun specialLinkQueryParam(url: String, name: String): String? {
    val query = url.substringAfter('?', "").substringBefore('#')
    if (query.isEmpty()) return null
    for (part in query.split('&')) {
        val key = part.substringBefore('=')
        if (key.equals(name, ignoreCase = true)) {
            val raw = part.substringAfter('=', "")
            if (raw.isEmpty()) return null
            return try {
                Uri.decode(raw)
            } catch (t: Throwable) {
                raw
            }
        }
    }
    return null
}

/**
 * The one shared external dispatcher for special links, used by both
 * engines' navigation interception. Callers only invoke this for a
 * navigation a real user gesture triggered; gesture-less hits are
 * blocked by the callers and never reach here.
 *
 * Returns an http(s) URL the caller should load in the current tab
 * when the link carries one (an intent: link's browser_fallback_url,
 * or an intent: link whose target is itself a web page); null in
 * every other case — the link was handed to the system, or it could
 * not be handled and the user was told so with a Toast. Every
 * hand-out is try/catch guarded: no matching app means a plain-words
 * Toast, never a crash and never the engine loading the scheme.
 */
fun dispatchSpecialLink(context: Context, url: String): String? {
    fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    fun startExternal(intent: android.content.Intent, failMessage: String) {
        try {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (t: Throwable) {
            toast(failMessage)
        }
    }
    val scheme = specialLinkScheme(url) ?: return null
    when (scheme) {
        "tel", "telprompt" -> {
            // ACTION_DIAL only pre-fills the dialer — nothing is
            // called until the user presses the call button there,
            // so no CALL_PHONE permission is needed (or declared).
            val number = url.substringAfter(':').substringBefore('?')
                .substringBefore('#')
            startExternal(
                android.content.Intent(
                    android.content.Intent.ACTION_DIAL,
                    Uri.parse("tel:$number")
                ),
                "没有找到拨号应用"
            )
        }
        "sms", "smsto" -> {
            val number = url.substringAfter(':').substringBefore('?')
                .substringBefore('#')
            val intent = android.content.Intent(
                android.content.Intent.ACTION_SENDTO,
                Uri.parse("smsto:$number")
            )
            val body = specialLinkQueryParam(url, "body")
            if (!body.isNullOrEmpty()) {
                intent.putExtra("sms_body", body)
            }
            startExternal(intent, "没有找到短信应用")
        }
        "mailto" -> {
            val to = url.substringAfter(':').substringBefore('?')
                .substringBefore('#')
            val intent = android.content.Intent(
                android.content.Intent.ACTION_SENDTO,
                Uri.parse("mailto:$to")
            )
            if (to.isNotEmpty()) {
                intent.putExtra(
                    android.content.Intent.EXTRA_EMAIL, arrayOf(to)
                )
            }
            val cc = specialLinkQueryParam(url, "cc")
            if (!cc.isNullOrEmpty()) {
                intent.putExtra(
                    android.content.Intent.EXTRA_CC, arrayOf(cc)
                )
            }
            val subject = specialLinkQueryParam(url, "subject")
            if (!subject.isNullOrEmpty()) {
                intent.putExtra(
                    android.content.Intent.EXTRA_SUBJECT, subject
                )
            }
            val body = specialLinkQueryParam(url, "body")
            if (!body.isNullOrEmpty()) {
                intent.putExtra(android.content.Intent.EXTRA_TEXT, body)
            }
            startExternal(intent, "没有找到邮箱应用")
        }
        "geo" -> {
            startExternal(
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW, Uri.parse(url)
                ),
                "没有找到地图应用"
            )
        }
        "market" -> {
            startExternal(
                android.content.Intent(
                    android.content.Intent.ACTION_VIEW, Uri.parse(url)
                ),
                "没有找到应用商店"
            )
        }
        "intent" -> {
            // Parsed with the system's own Intent.parseUri — no
            // hand-rolled intent parser. A browser_fallback_url
            // that is a web page wins and opens right here; an
            // http(s) target opens as a normal page too. Anything
            // else is another app's deep link: say so in plain
            // words and fire the parsed intent as-is.
            val parsed = try {
                android.content.Intent.parseUri(
                    url, android.content.Intent.URI_INTENT_SCHEME
                )
            } catch (t: Throwable) {
                toast("这个链接打不开")
                return null
            }
            val fallback = try {
                parsed.getStringExtra("browser_fallback_url")
            } catch (t: Throwable) {
                null
            }
            if (!fallback.isNullOrEmpty() &&
                (fallback.startsWith("http://") ||
                    fallback.startsWith("https://"))
            ) {
                return fallback
            }
            val data = parsed.data
            if (data != null &&
                (data.scheme == "http" || data.scheme == "https")
            ) {
                return data.toString()
            }
            toast("这个链接需要用其他应用打开，已为你尝试")
            startExternal(parsed, "没有找到能打开这个链接的应用")
        }
    }
    return null
}

/**
 * Page resource sniffer (WebView only): collects direct links to
 * downloadable files from anchors, image sources, media elements
 * and their <source> children, and media/file links in <link> tags.
 * Relative URLs are resolved against the page's base URI and the
 * result is de-duplicated before it leaves the page. The list is
 * capped so a pathological page cannot flood the UI.
 *
 * Collection order matters because of the cap (v0.140.0 audit):
 * files and media are collected first, images last, so on an
 * image-heavy page the cap can only ever trim images — before this
 * pass the anchors → images → media order let 300 images starve
 * every media entry out of the list. The anchor pass accepts the
 * union of the file and media extension sets: before this pass it
 * gated on the file set alone, so a direct link to a .webm/.m4a/
 * .wav/.ogg/.flac file (media-set only) was never listed even
 * though tapping the same link downloads it ([DOWNLOAD_EXTENSIONS]
 * is the same union). Images report the URL the page actually
 * loaded (currentSrc, which resolves srcset and lazy swaps) plus
 * the common lazy-load data attributes, and an image the page
 * rendered at exactly 1×1 (tracking pixel / spacer) is skipped.
 * Media elements likewise prefer currentSrc so a source assigned
 * from script is still found.
 */
private val SNIFF_JS = """
(function() {
  function abs(u) {
    if (!u) return '';
    try { return new URL(u, document.baseURI).href; } catch (e) { return ''; }
  }
  function nameOf(u) {
    try {
      var p = new URL(u).pathname;
      var n = p.substring(p.lastIndexOf('/') + 1);
      try { n = decodeURIComponent(n); } catch (e) {}
      return n || '';
    } catch (e) { return ''; }
  }
  function extOf(u) {
    var clean = (u || '').split('#')[0].split('?')[0];
    var dot = clean.lastIndexOf('.');
    if (dot < 0) return '';
    return clean.substring(dot + 1).toLowerCase();
  }
  var fileExts = [${SNIFF_FILE_EXTENSIONS.joinToString(",") { "'$it'" }}];
  var mediaExts = [${SNIFF_MEDIA_EXTENSIONS.joinToString(",") { "'$it'" }}];
  var seen = {};
  var out = [];
  function push(raw, kind) {
    var u = abs(raw);
    if (!u || (u.indexOf('http://') !== 0 && u.indexOf('https://') !== 0)) return;
    if (seen[u]) return;
    seen[u] = true;
    out.push({ url: u, kind: kind, name: nameOf(u) });
  }
  document.querySelectorAll('a[href]').forEach(function(a) {
    var href = a.getAttribute('href') || '';
    var ext = extOf(href);
    if (mediaExts.indexOf(ext) >= 0) push(href, 'media');
    else if (fileExts.indexOf(ext) >= 0) push(href, 'file');
  });
  document.querySelectorAll(
    'video, audio, video source[src], audio source[src]'
  ).forEach(function(el) {
    push(el.currentSrc || el.getAttribute('src'), 'media');
  });
  document.querySelectorAll('link[href]').forEach(function(l) {
    var href = l.getAttribute('href') || '';
    var ext = extOf(href);
    if (mediaExts.indexOf(ext) >= 0) push(href, 'media');
    else if (fileExts.indexOf(ext) >= 0) push(href, 'file');
  });
  document.querySelectorAll('img').forEach(function(img) {
    if (img.naturalWidth === 1 && img.naturalHeight === 1) return;
    push(img.currentSrc || img.getAttribute('src'), 'image');
    push(img.getAttribute('data-src'), 'image');
    push(img.getAttribute('data-original'), 'image');
  });
  return JSON.stringify(out.slice(0, 300));
})()
"""

/**
 * Reader-mode extraction + typesetting (WebView only).
 *
 * Extraction (rewritten v0.116.0; v0.6.0–v0.115.0 simply took the
 * longest-innerText element, which on real pages is the outer page
 * wrapper): every article/main/section/div candidate is scored by
 * the exact formula in ReaderScoring.kt — non-link text ×
 * (1 − link density) × tag weight — the best score sets the bar,
 * and among candidates reaching 60% of it the one with the smallest
 * total text (the tightest block holding the article) is kept.
 * The winner needs at least 200 non-link characters; when nothing
 * qualifies the script changes NOTHING and answers 'fail', so the
 * tab stays on the original page instead of opening an empty
 * reader page. The kept HTML goes under the page title (inserted
 * as a text node, never parsed as markup), then
 * scripts/styles/iframes/forms/nav/header/footer/aside and form
 * controls inside it are stripped, and the result is reflowed
 * single-column. Images and videos are kept, scaled to the column;
 * tables stay tables and scroll sideways inside the column rather
 * than crushing the text (see [readerCss]).
 *
 * The typesetting half is parameterized since v0.58.0: [font] gives
 * the body/heading sizes, [palette] the background/text/link
 * colours, both resolved from the user's reader settings at entry
 * time (see ReaderFontSize / ReaderBackground in the data layer).
 * Line height (1.75), column width (720px) and padding (16px) stay
 * fixed — never settings.
 *
 * The injected style element carries the fixed id
 * "tuxunbrowse-reader-style" so [buildReaderStyleJs] can find and
 * rewrite it later WITHOUT re-running this extraction (re-running
 * the extraction on the already-extracted document would extract
 * the extraction). The h1 keeps an inline size so it tracks the
 * font step as well.
 */
private fun readerCss(font: ReaderFontSize, palette: ReaderPalette): String =
    "html,body{background:" + palette.background + ";color:" + palette.text + "}" +
        "body{font-family:sans-serif;line-height:1.75;padding:16px;" +
        "max-width:720px;margin:0 auto;font-size:" + font.bodyPx + "px;" +
        "overflow-wrap:break-word}" +
        "a{color:" + palette.link + "}" +
        "img,video{max-width:100%;height:auto}" +
        "pre,code{white-space:pre-wrap;word-break:break-word}" +
        "table{display:block;max-width:100%;overflow-x:auto;" +
        "border-collapse:collapse}"

private fun buildReaderJs(font: ReaderFontSize, palette: ReaderPalette): String =
    """
(function() {
  if (document.getElementById('tuxunbrowse-reader-style')) return 'ok';
  var els = document.querySelectorAll('article, main, section, div');
  var feats = [];
  els.forEach(function(el) {
    var text = (el.innerText || '');
    var linkText = 0;
    el.querySelectorAll('a').forEach(function(a) {
      linkText += (a.innerText || '').length;
    });
    var tag = el.tagName.toLowerCase();
    var weight = tag === 'article' ? 1.5 : (tag === 'main' ? 1.4 : (tag === 'section' ? 1.1 : 1.0));
    var nonLink = Math.max(0, text.length - linkText);
    var density = text.length > 0 ? Math.min(1, Math.max(0, linkText / text.length)) : 1;
    feats.push({ el: el, textLen: text.length, nonLink: nonLink,
      score: nonLink * (1 - density) * weight });
  });
  var best = null;
  feats.forEach(function(f) { if (!best || f.score > best.score) best = f; });
  if (!best || best.nonLink < 200) return 'fail';
  var bar = best.score * 0.6;
  var pick = null;
  feats.forEach(function(f) {
    if (f.score < bar) return;
    if (!pick || f.textLen < pick.textLen ||
        (f.textLen === pick.textLen && f.score > pick.score)) pick = f;
  });
  if (!pick) return 'fail';
  var content = pick.el.innerHTML;
  document.body.innerHTML = content;
  var h1 = document.createElement('h1');
  h1.style.fontSize = '""" + font.headingPx + """px';
  h1.style.lineHeight = '1.4';
  h1.textContent = document.title || '';
  document.body.insertBefore(h1, document.body.firstChild);
  document.body.querySelectorAll('script,style,iframe,form,nav,header,footer,aside,button,select,textarea,input')
    .forEach(function(e) { e.remove(); });
  var st = document.createElement('style');
  st.id = 'tuxunbrowse-reader-style';
  st.textContent = '""" + readerCss(font, palette) + """';
  document.head.appendChild(st);
  window.scrollTo(0, 0);
  return 'ok';
})()
"""

/**
 * Style-only follow-up for a page already in reader mode (v0.58.0):
 * rewrite the text of the style element [buildReaderJs] injected,
 * and re-size the title it inlined. If the element is somehow gone
 * (the page changed under us), this is a silent no-op — it never
 * re-extracts.
 */
private fun buildReaderStyleJs(font: ReaderFontSize, palette: ReaderPalette): String =
    """
(function() {
  var st = document.getElementById('tuxunbrowse-reader-style');
  if (!st) return 'skip';
  st.textContent = '""" + readerCss(font, palette) + """';
  var h1 = document.querySelector('h1');
  if (h1) h1.style.fontSize = '""" + font.headingPx + """px';
  return 'ok';
})()
"""

/**
 * Ad-slot hiding (WebView only): a small stylesheet injected after
 * each page finishes, hiding elements whose markup is unambiguously
 * an ad container. The selector list is deliberately conservative —
 * AdSense/GPT markers, ad-* ids, ad banner/slot class names and ad
 * network iframes only — so real page content is never hidden by
 * accident; a missed slot is acceptable, a hidden article is not.
 * Injected once per document (guarded by the style element's id);
 * any failure is silent. Gecko tabs never see this: GeckoView has
 * no CSS injection path and its content blocking already covers
 * the tracking side.
 */
private const val AD_HIDE_JS = """
(function() {
  try {
    if (document.getElementById('tuxunbrowse-ad-hide')) return 'ok';
    var st = document.createElement('style');
    st.id = 'tuxunbrowse-ad-hide';
    st.textContent =
      'ins.adsbygoogle,[data-ad-slot],[data-ad-client],' +
      '[id^="ad-"],[id^="ads-"],[id^="google_ads"],[id*="div-gpt-ad"],' +
      '[class*="ad-banner"],[class*="ad_banner"],[class*="adsbygoogle"],' +
      '[class*="ad-slot"],[class*="ad-placeholder"],[class*="advertisement"],' +
      'iframe[src*="doubleclick"],iframe[src*="googlesyndication"],' +
      'iframe[src*="adsbygoogle"]{display:none!important}';
    (document.head || document.documentElement).appendChild(st);
    return 'ok';
  } catch (e) { return 'skip'; }
})()
"""

@Composable
fun TabContent(tab: BrowserTab, modifier: Modifier = Modifier) {
    when (tab.engine) {
        Engine.WEBVIEW -> AndroidView(
            modifier = modifier,
            factory = { ctx ->
                // Null means the engine could not be created
                // (v0.81.0): the tab is marked engineUnavailable
                // and the app-level fallback page replaces this
                // blank placeholder as soon as it recomposes.
                tab.obtainWebView(ctx) ?: android.view.View(ctx)
            },
            update = { }
        )

        Engine.GECKO -> AndroidView(
            modifier = modifier,
            factory = { ctx ->
                // Same contract as the WebView side (v0.81.0):
                // null = engine unavailable, the app-level
                // fallback page takes over. (Before this version
                // this branch showed a bare TextView with no way
                // to switch engine or retry.)
                tab.obtainGecko(ctx) ?: android.view.View(ctx)
            },
            update = { }
        )
    }
}
