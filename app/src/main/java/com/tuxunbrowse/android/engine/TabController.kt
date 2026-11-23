package com.tuxunbrowse.android.engine

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.DownloadListener
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
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

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
            runtime
        } catch (t: Throwable) {
            available = false
            null
        }
    }
}

private var nextTabId = 1

/** One browser tab. Holds its own WebView or GeckoSession so tabs keep state. */
class BrowserTab(
    val engine: Engine,
    val incognito: Boolean,
    startUrl: String,
    private val onVisit: (title: String, url: String) -> Unit
) {
    val id: Int = nextTabId++
    var url by mutableStateOf(startUrl)
    var title by mutableStateOf("")
    var progress by mutableIntStateOf(100)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)

    var webView: WebView? = null
    var geckoSession: GeckoSession? = null
    var geckoView: GeckoView? = null

    fun load(target: String) {
        url = target
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

    fun destroy() {
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
        view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(wv: WebView, finishedUrl: String?) {
                super.onPageFinished(wv, finishedUrl)
                val u = finishedUrl ?: return
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
        }
        view.setDownloadListener(
            DownloadListener { dlUrl, userAgent, contentDisposition, mimetype, _ ->
                startSystemDownload(context, dlUrl, userAgent, contentDisposition, mimetype)
            }
        )
        webView = view
        if (url.isNotBlank()) view.loadUrl(url)
        return view
    }

    fun obtainGecko(context: Context): GeckoView? {
        geckoView?.let { return it }
        val runtime = GeckoHolder.ensure(context) ?: return null
        val settings = GeckoSessionSettings.Builder()
            .usePrivateMode(incognito)
            .build()
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

            override fun onTitleChange(s: GeckoSession, newTitle: String?) {
                if (!newTitle.isNullOrBlank()) title = newTitle
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
            ): GeckoResult<GeckoSession.NavigationDelegate.AllowOrDeny>? = null
        }
        session.progressDelegate = object : GeckoSession.ProgressDelegate {
            override fun onProgressChange(s: GeckoSession, p: Int) {
                progress = p
            }

            override fun onPageStop(s: GeckoSession, success: Boolean) {
                progress = 100
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
        if (url.isNotBlank()) session.loadUri(url)
        return view
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
