package com.tuxunbrowse.android.engine

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
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
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
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
    ) -> Unit = { _, _, _, callback -> callback.reject() }
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
        exitFullscreen()
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
                    android.webkit.WebResourceResponse(
                        "text/plain", "utf-8",
                        java.io.ByteArrayInputStream(ByteArray(0))
                    )
                } else {
                    null
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
                val u = finishedUrl ?: return
                readerActive = false
                url = u
                title = wv.title ?: ""
                canGoBack = wv.canGoBack()
                canGoForward = wv.canGoForward()
                if (!incognito && u.startsWith("http")) onVisit(title, u)
            }
        }
        view.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(wv: WebView, newProgress: Int) {
                progress = newProgress
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

    fun obtainGecko(context: Context): GeckoView? {
        geckoView?.let { return it }
        val appContext = context.applicationContext
        val runtime = GeckoHolder.ensure(context) ?: return null
        val settingsBuilder = GeckoSessionSettings.Builder()
            .usePrivateMode(incognito)
        if (desktopMode) {
            settingsBuilder
                .userAgentMode(GeckoSessionSettings.USER_AGENT_MODE_DESKTOP)
                .viewportMode(GeckoSessionSettings.VIEWPORT_MODE_DESKTOP)
        }
        val settings = settingsBuilder.build()
        val session = GeckoSession(settings)
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
        }
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
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
                permissions: Array<String>,
                callback: PermissionDelegate.Callback
            ) {
                onGeckoAndroidPermissions(permissions, callback)
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
        session.open(runtime)
        geckoSession = session
        val view = GeckoView(context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.setSession(session)
        geckoView = view
        if (url.isNotBlank() && url != HOME_URL) session.loadUri(url)
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
