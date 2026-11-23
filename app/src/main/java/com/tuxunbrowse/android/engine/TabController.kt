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
import android.webkit.PermissionRequest
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.Composable
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
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

    fun ensure(context: Context): GeckoRuntime? {
        if (runtime != null) return runtime
        return try {
            runtime = GeckoRuntime.create(context.applicationContext)
            // Firefox-side ad/tracker protection: GeckoView's own content
            // blocking with its default anti-tracking categories. Guarded:
            // if this ever fails, browsing must still work.
            try {
                runtime?.settings?.contentBlocking
                    ?.setAntiTracking(ContentBlocking.AntiTracking.DEFAULT)
            } catch (t: Throwable) {
                // Content blocking is best-effort only.
            }
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

/** Resource groups the page sniffer reports, in display order. */
enum class SniffKind { FILE, IMAGE, MEDIA }

/**
 * Tallest full-page screenshot, in pixels. A page taller than this
 * is captured only up to the cap and the UI says so honestly.
 */
const val MAX_FULL_PAGE_HEIGHT_PX = 12000

/** Outcome of a full-page capture attempt. */
data class FullPageCapture(
    val bitmap: Bitmap?,
    val truncated: Boolean,
    val outOfMemory: Boolean
)

/** One downloadable resource link found on the current page. */
data class SniffedResource(
    val url: String,
    val name: String,
    val host: String,
    val kind: SniffKind
)

/** Parse the sniffer script's JSON answer into typed resources. */
private fun parseSniffResult(raw: String?): List<SniffedResource> {
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
        out
    } catch (t: Throwable) {
        emptyList()
    }
}

/** Plain-language reason for a WebView main-frame error code. */
private fun webViewErrorReason(errorCode: Int): String = when (errorCode) {
    WebViewClient.ERROR_HOST_LOOKUP ->
        "找不到这个网站，可能是网址写错了，或者现在的网络连不上它"
    WebViewClient.ERROR_CONNECT ->
        "连不上这个网站，请检查网络后再试"
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
    WebViewClient.ERROR_TOO_MANY_REQUESTS ->
        "这个网站说请求太多了，请过一会儿再试"
    else -> "网页没能打开，请检查网络后重试"
}

/**
 * Plain-language reason for a Gecko load error. WebRequestError's
 * code/category constants were verified against the GeckoView 132
 * AAR with javap.
 */
private fun geckoErrorReason(error: WebRequestError): String = when (error.code) {
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
    WebRequestError.ERROR_SECURITY_SSL, WebRequestError.ERROR_SECURITY_BAD_CERT,
    WebRequestError.ERROR_BAD_HSTS_CERT ->
        "这个网站的安全证书有问题，没法安全打开"
    WebRequestError.ERROR_MALFORMED_URI ->
        "这个网址写得不对，打不开"
    WebRequestError.ERROR_REDIRECT_LOOP ->
        "这个网站一直在来回跳转，没法打开"
    else -> when (error.category) {
        WebRequestError.ERROR_CATEGORY_NETWORK ->
            "网络连接有问题，网页没能打开，请检查网络后重试"
        WebRequestError.ERROR_CATEGORY_SECURITY ->
            "这个网站的安全有问题，没法打开"
        WebRequestError.ERROR_CATEGORY_PROXY ->
            "代理连不上，网页没能打开"
        else -> "网页没能打开，请检查网络后重试"
    }
}

private var nextTabId = 1

private const val DESKTOP_USER_AGENT =
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/120.0.0.0 Safari/537.36"

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
    private val onLinkContext: (String) -> Unit = {},
    private val onImageContext: (String) -> Unit = {},
    private val textZoomProvider: () -> Int = { 100 },
    private val webDarkProvider: () -> Boolean = { false },
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
    private val onGeckoNewSession: (GeckoSession, String) -> Unit = { _, _ -> }
) {
    val id: Int = nextTabId++
    var url by mutableStateOf(startUrl)
    var title by mutableStateOf("")
    var progress by mutableIntStateOf(100)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    var readerActive by mutableStateOf(false)

    /** Find-in-page match counter text ("2 / 9"); WebView only. */
    var findInfo by mutableStateOf("")

    /**
     * How many ad/tracker requests this tab's current page has had
     * blocked so far (WebView only — GeckoView exposes no per-request
     * count, so Gecko tabs keep this at zero and the UI shows no
     * number for them). Reset whenever a new page starts loading.
     * shouldInterceptRequest runs on a background thread, so the
     * increments are posted to the WebView's (main) thread.
     */
    var blockedCount by mutableIntStateOf(0)

    /** Latest network-level load failure, or null when the page is fine. */
    var loadError by mutableStateOf<TabLoadError?>(null)

    /**
     * Latest tab thumbnail (WebView tabs only). At most one bitmap is
     * ever referenced per tab — a fresh capture replaces the reference
     * and the old bitmap is left to the GC (it is NOT recycled here:
     * the tab grid may still be drawing it this frame). Closing the
     * tab drops the reference in destroy(). GeckoView has no
     * screenshot API, so Gecko tabs keep this null forever and the
     * grid says so instead of faking a preview.
     */
    var thumbnail by mutableStateOf<Bitmap?>(null)

    /** Throwaway WebView holding a window.open popup until its URL is known. */
    private var popupChild: WebView? = null

    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    fun enterReaderMode() {
        val view = webView ?: return
        view.evaluateJavascript(READER_JS, null)
        readerActive = true
    }

    fun exitReaderMode() {
        readerActive = false
        reload()
    }

    fun exitFullscreen() {
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }

    var webView: WebView? = null
    var geckoSession: GeckoSession? = null
    var geckoView: GeckoView? = null

    val isHome: Boolean
        get() = url == HOME_URL

    fun load(target: String) {
        loadError = null
        blockedCount = 0
        url = target
        // The native home page is rendered by the app itself; the web
        // engines must never try to load its internal URL.
        if (target == HOME_URL) return
        when (engine) {
            Engine.WEBVIEW -> webView?.loadUrl(target)
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
        when (engine) {
            Engine.WEBVIEW -> webView?.reload()
            Engine.GECKO -> geckoSession?.reload()
        }
    }

    fun findInPage(query: String, forward: Boolean) {
        if (query.isBlank()) return
        when (engine) {
            Engine.WEBVIEW -> {
                val view = webView ?: return
                view.findAllAsync(query)
                view.findNext(forward)
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
     * Draw the whole page into one tall bitmap (WebView only): the
     * full content height (contentHeight × scale), capped at
     * [MAX_FULL_PAGE_HEIGHT_PX] so a very long page cannot exhaust
     * memory — when the cap bites, [FullPageCapture.truncated] says
     * so and the UI tells the user only the first part was captured.
     * An out-of-memory failure is reported, never thrown.
     */
    fun captureFullPageBitmap(): FullPageCapture {
        val view = webView ?: return FullPageCapture(null, false, false)
        return try {
            val width = view.width
            if (width <= 0) return FullPageCapture(null, false, false)
            val fullHeight = (view.contentHeight * view.scale).toInt()
            if (fullHeight <= 0) return FullPageCapture(null, false, false)
            val truncated = fullHeight > MAX_FULL_PAGE_HEIGHT_PX
            val height = fullHeight.coerceAtMost(MAX_FULL_PAGE_HEIGHT_PX)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bmp))
            FullPageCapture(bmp, truncated, false)
        } catch (oom: OutOfMemoryError) {
            FullPageCapture(null, false, true)
        } catch (t: Throwable) {
            FullPageCapture(null, false, false)
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
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            webView?.settings?.setAlgorithmicDarkeningAllowed(enabled)
        }
    }

    fun clearFind() {
        findInfo = ""
        when (engine) {
            Engine.WEBVIEW -> webView?.clearMatches()
            Engine.GECKO -> geckoSession?.finder?.clear()
        }
    }

    fun destroy() {
        // Release the thumbnail with the tab; the reference is simply
        // dropped (not recycled — the grid may still hold a frame of
        // it) and the GC reclaims the pixels.
        thumbnail = null
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
            it.stopLoading()
            it.destroy()
        }
        webView = null
        geckoSession?.close()
        geckoSession = null
        geckoView = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun obtainWebView(context: Context): WebView {
        webView?.let { return it }
        val view = WebView(context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.settings.javaScriptEnabled = true
        view.settings.domStorageEnabled = true
        view.settings.loadsImagesAutomatically = true
        view.settings.builtInZoomControls = true
        view.settings.displayZoomControls = false
        view.settings.textZoom = textZoomProvider()
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
        if (desktopMode) {
            view.settings.userAgentString = DESKTOP_USER_AGENT
            view.settings.useWideViewPort = true
            view.settings.loadWithOverviewMode = true
        }
        view.setOnLongClickListener { v ->
            val hit = (v as WebView).hitTestResult
            val extra = hit.extra
            when {
                hit.type == WebView.HitTestResult.IMAGE_TYPE &&
                    !extra.isNullOrBlank() -> {
                    onImageContext(extra)
                    true
                }
                (hit.type == WebView.HitTestResult.SRC_ANCHOR_TYPE ||
                    hit.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) &&
                    !extra.isNullOrBlank() -> {
                    onLinkContext(extra)
                    true
                }
                else -> false
            }
        }
        view.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                wv: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? {
                val target = request.url?.toString() ?: return null
                return if (isAdBlockEnabled() && AdBlocker.isAdRequest(target)) {
                    // Count it on the main thread: this callback runs
                    // on a background thread, the counter is UI state.
                    wv.post { blockedCount++ }
                    android.webkit.WebResourceResponse(
                        "text/plain", "utf-8",
                        java.io.ByteArrayInputStream(ByteArray(0))
                    )
                } else {
                    null
                }
            }

            override fun onPageStarted(
                wv: WebView,
                startedUrl: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(wv, startedUrl, favicon)
                // A fresh load is under way; any earlier failure is over.
                loadError = null
                // The blocked-request counter belongs to one page.
                blockedCount = 0
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
                val failed = request.url?.toString() ?: url
                val description = error.description?.toString().orEmpty()
                loadError = TabLoadError(
                    failedUrl = failed,
                    reason = webViewErrorReason(error.errorCode),
                    detail = "错误码 ${error.errorCode}" +
                        if (description.isNotBlank()) "：$description" else ""
                )
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
                val u = finishedUrl ?: return
                readerActive = false
                url = u
                title = wv.title ?: ""
                canGoBack = wv.canGoBack()
                canGoForward = wv.canGoForward()
                if (!incognito && u.startsWith("http")) onVisit(title, u)
                // One thumbnail capture per finished load; the other
                // capture point is the tab being switched away from.
                captureThumbnail()
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
                startSystemDownload(context, dlUrl, userAgent, contentDisposition, mimetype)
            }
        )
        webView = view
        if (url.isNotBlank() && url != HOME_URL) view.loadUrl(url)
        return view
    }

    private fun buildGeckoSettings(): GeckoSessionSettings {
        val settingsBuilder = GeckoSessionSettings.Builder()
            .usePrivateMode(incognito)
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
                    return GeckoResult.fromValue(AllowOrDeny.DENY)
                }
                // Navigation-level ad blocking for Gecko (subresource
                // requests are covered by content blocking instead).
                if (target.startsWith("http") && isAdBlockEnabled() &&
                    AdBlocker.isAdRequest(target)
                ) {
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
            }

            override fun onProgressChange(s: GeckoSession, p: Int) {
                progress = p
            }

            override fun onPageStop(s: GeckoSession, success: Boolean) {
                progress = 100
            }
        }
        // GeckoView 132 reports page titles on ContentDelegate, not on
        // NavigationDelegate (where older versions had them).
        session.contentDelegate = object : GeckoSession.ContentDelegate {
            override fun onTitleChange(s: GeckoSession, newTitle: String?) {
                if (!newTitle.isNullOrBlank()) title = newTitle
            }

            override fun onContextMenu(
                s: GeckoSession,
                screenX: Int,
                screenY: Int,
                element: GeckoSession.ContentDelegate.ContextElement
            ) {
                val link = element.linkUri
                if (!link.isNullOrBlank()) onLinkContext(link)
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
                saveGeckoDownload(appContext, response)
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

    fun obtainGecko(context: Context): GeckoView? {
        geckoView?.let { return it }
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

private val geckoDownloadExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

/**
 * Save a Gecko "external response" (a download) to the public Downloads
 * folder. The body stream already carries the Gecko session's cookies and
 * request state, so unlike a DownloadManager re-request it also works for
 * downloads that need a login. On Android 10+ the file lands in the system
 * Downloads folder via MediaStore (no permission needed); on older systems
 * it goes to the app's own download folder, which needs no permission
 * either.
 */
fun saveGeckoDownload(context: Context, response: WebResponse) {
    val uri = response.uri
    val headers = response.headers
    fun header(name: String): String? = headers.entries
        .firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
    val contentType = header("Content-Type")?.substringBefore(";")?.trim()
    val fileName = android.webkit.URLUtil.guessFileName(
        uri, header("Content-Disposition"), contentType
    )
    val body = response.body
    if (body == null) {
        Toast.makeText(context, "这个文件没有可下载的内容", Toast.LENGTH_LONG).show()
        return
    }
    Toast.makeText(context, "开始下载：$fileName", Toast.LENGTH_LONG).show()
    geckoDownloadExecutor.execute {
        var ok = false
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
                if (dest != null) {
                    resolver.openOutputStream(dest)?.use { out -> body.copyTo(out) }
                    val done = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                    }
                    resolver.update(dest, done, null, null)
                    ok = true
                }
            } else {
                val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                java.io.File(dir, fileName).outputStream().use { out -> body.copyTo(out) }
                ok = true
            }
        } catch (t: Throwable) {
            ok = false
        } finally {
            try {
                body.close()
            } catch (t: Throwable) {
                // Nothing useful to do about a failed close.
            }
        }
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast.makeText(
                context,
                if (ok) "已下载：$fileName（在系统下载文件夹）" else "下载失败：$fileName",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

fun startSystemDownload(
    context: Context,
    url: String?,
    userAgent: String?,
    contentDisposition: String?,
    mimetype: String?
) {
    if (url.isNullOrBlank()) return
    try {
        val uri = Uri.parse(url)
        val fileName = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimetype)
        val request = DownloadManager.Request(uri)
            .setTitle(fileName)
            .setDescription(url)
            .setMimeType(mimetype)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        if (!userAgent.isNullOrBlank()) request.addRequestHeader("User-Agent", userAgent)
        val cookie = CookieManager.getInstance().getCookie(url)
        if (!cookie.isNullOrBlank()) request.addRequestHeader("Cookie", cookie)
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
 * Page resource sniffer (WebView only): collects direct links to
 * downloadable files from anchors, image sources, media elements
 * and their <source> children, and media/file links in <link> tags.
 * Relative URLs are resolved against the page's base URI and the
 * result is de-duplicated before it leaves the page. The list is
 * capped so a pathological page cannot flood the UI.
 */
private const val SNIFF_JS = """
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
  var fileExts = ['zip','rar','7z','apk','pdf','doc','docx','xls','xlsx',
    'ppt','pptx','txt','mp4','mp3','mov','mkv','avi'];
  var mediaExts = ['mp4','mp3','mov','mkv','avi','webm','m4a','wav','ogg','flac'];
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
    if (fileExts.indexOf(ext) >= 0) {
      push(href, mediaExts.indexOf(ext) >= 0 ? 'media' : 'file');
    }
  });
  document.querySelectorAll('img[src]').forEach(function(img) {
    push(img.getAttribute('src'), 'image');
  });
  document.querySelectorAll(
    'video[src], audio[src], video source[src], audio source[src]'
  ).forEach(function(el) {
    push(el.getAttribute('src'), 'media');
  });
  document.querySelectorAll('link[href]').forEach(function(l) {
    var href = l.getAttribute('href') || '';
    var ext = extOf(href);
    if (mediaExts.indexOf(ext) >= 0) push(href, 'media');
    else if (fileExts.indexOf(ext) >= 0) push(href, 'file');
  });
  return JSON.stringify(out.slice(0, 300));
})()
"""

/**
 * Reader mode script (WebView only): pick the element with the most
 * text, keep its HTML, strip scripts/nav/forms, and restyle the page
 * as a clean single column. Reloading the page exits reader mode.
 */
private const val READER_JS = """
(function() {
  var candidates = document.querySelectorAll('article, main, section, div');
  var best = null, bestLen = 0;
  candidates.forEach(function(el) {
    var t = (el.innerText || '').length;
    if (t > bestLen) { bestLen = t; best = el; }
  });
  var pageTitle = document.title || '';
  var content = best ? best.innerHTML : document.body.innerHTML;
  document.body.innerHTML =
    '<h1 style="font-size:22px;line-height:1.4">' + pageTitle + '</h1>' + content;
  document.body.querySelectorAll('script,style,iframe,form,nav,header,footer,aside')
    .forEach(function(e) { e.remove(); });
  var st = document.createElement('style');
  st.textContent =
    'body{font-family:sans-serif;line-height:1.75;padding:16px;' +
    'max-width:720px;margin:0 auto;font-size:17px}' +
    'img,video{max-width:100%;height:auto}' +
    'pre,code{white-space:pre-wrap;word-break:break-word}';
  document.head.appendChild(st);
  window.scrollTo(0, 0);
  return 'ok';
})()
"""

@Composable
fun TabContent(tab: BrowserTab, modifier: Modifier = Modifier) {
    when (tab.engine) {
        Engine.WEBVIEW -> AndroidView(
            modifier = modifier,
            factory = { ctx -> tab.obtainWebView(ctx) },
            update = { }
        )

        Engine.GECKO -> AndroidView(
            modifier = modifier,
            factory = { ctx ->
                tab.obtainGecko(ctx) ?: android.widget.TextView(ctx).apply {
                    text = "火狐内核这次没能启动，请到设置里改用系统 WebView 内核，或重启应用再试。"
                }
            },
            update = { }
        )
    }
}
