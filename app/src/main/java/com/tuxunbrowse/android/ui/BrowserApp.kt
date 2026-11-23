package com.tuxunbrowse.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image as FoundationImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.DownloadMode
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.HistoryRetention
import com.tuxunbrowse.android.data.TUXUNDOWN_PACKAGE
import com.tuxunbrowse.android.data.ReadingItem
import com.tuxunbrowse.android.data.SpeedDial
import com.tuxunbrowse.android.data.compareVersionNames
import com.tuxunbrowse.android.data.pickLatestRelease
import com.tuxunbrowse.android.data.SavedTab
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.Store
import com.tuxunbrowse.android.data.ThemeMode
import com.tuxunbrowse.android.data.dirSizeBytes
import com.tuxunbrowse.android.data.formatStorageSize
import com.tuxunbrowse.android.data.WEBVIEW_CACHE_AUTO_CLEAR_THRESHOLD_BYTES
import com.tuxunbrowse.android.data.webViewCacheSizeBytes
import com.tuxunbrowse.android.data.BackupData
import com.tuxunbrowse.android.data.BackupSettings
import com.tuxunbrowse.android.data.backupToJson
import com.tuxunbrowse.android.data.parseBackup
import com.tuxunbrowse.android.data.bookmarksFromHtml
import com.tuxunbrowse.android.data.bookmarksFromJson
import com.tuxunbrowse.android.data.bookmarksToJson
import com.tuxunbrowse.android.data.MAX_IMPORT_FILE_CHARS
import com.tuxunbrowse.android.data.isValidCustomSearchTemplate
import com.tuxunbrowse.android.data.hostOf
import com.tuxunbrowse.android.data.isCompatHost
import com.tuxunbrowse.android.data.hasBlockedPageScheme
import com.tuxunbrowse.android.data.isAllowedPageUrl
import com.tuxunbrowse.android.data.isUrlLikeInput
import com.tuxunbrowse.android.data.matchSuggestions
import com.tuxunbrowse.android.data.resolveInput
import com.tuxunbrowse.android.engine.ALIVE_ENGINE_LIMIT
import com.tuxunbrowse.android.engine.AdBlocker
import com.tuxunbrowse.android.engine.BrowserTab
import com.tuxunbrowse.android.engine.GeckoHolder
import com.tuxunbrowse.android.engine.MAX_FULL_PAGE_HEIGHT_PX
import com.tuxunbrowse.android.engine.NavHistoryEntry
import com.tuxunbrowse.android.engine.SniffKind
import com.tuxunbrowse.android.engine.SniffedResource
import com.tuxunbrowse.android.engine.TabLoadError
import com.tuxunbrowse.android.engine.GeckoJsPromptKind
import com.tuxunbrowse.android.engine.HttpAuthRequestInfo
import com.tuxunbrowse.android.engine.TabContent
import com.tuxunbrowse.android.engine.savePageScreenshot
import com.tuxunbrowse.android.engine.sharePageScreenshot
import com.tuxunbrowse.android.engine.resolveDownloadFileName
import com.tuxunbrowse.android.engine.startSystemDownload
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.mozilla.geckoview.GeckoSession

private enum class Screen { PICKER, BROWSER, BOOKMARKS, HISTORY, SETTINGS, DOWNLOADS, CLEAR_DATA, SNIFF, READING_LIST }

/**
 * One recently closed tab, kept in memory only (never on disk) so it
 * can be reopened from the tab sheet. Incognito tabs are NEVER put
 * on this stack — incognito leaves no trace, by definition.
 */
private data class ClosedTabInfo(
    val title: String,
    val engine: Engine,
    val url: String,
    val desktopMode: Boolean
)

/** How many recently closed tabs the session stack remembers. */
private const val MAX_RECENT_CLOSED = 10

/** A pending "which downloader?" question (download mode = ask). */
private data class AskDownloadRequest(
    val message: String,
    val onSystem: () -> Unit,
    val onTuxunDown: () -> Unit
)

/** A JavaScript dialog (alert/confirm/prompt) waiting for the user. */
private data class JsPromptState(
    val kind: GeckoJsPromptKind,
    val message: String,
    val defaultValue: String,
    val respond: (Boolean, String?) -> Unit
)

/**
 * An HTTP Basic-auth challenge waiting for the user. The credentials
 * typed into the dialog exist only in the dialog's own state and in
 * this answer callback — they are never stored or logged.
 */
private data class AuthPromptState(
    val info: HttpAuthRequestInfo,
    val respond: (String?, String?) -> Unit
)

/**
 * Read an imported file's full text, stopping at
 * [MAX_IMPORT_FILE_CHARS]. Import files are untrusted input: reading
 * one unbounded with readText() lets an oversized file exhaust
 * memory before any parser gets a chance to reject it. Throws on
 * oversize or I/O failure — callers catch and report in plain
 * language without touching existing data.
 */
private fun readImportFileText(
    context: android.content.Context,
    uri: android.net.Uri
): String {
    val reader = context.contentResolver.openInputStream(uri)?.bufferedReader()
        ?: throw java.io.IOException("cannot open import file")
    val sb = StringBuilder()
    reader.use { r ->
        val buf = CharArray(8192)
        while (true) {
            val n = r.read(buf)
            if (n < 0) break
            if (sb.length + n > MAX_IMPORT_FILE_CHARS) {
                throw java.io.IOException("import file too large")
            }
            sb.append(buf, 0, n)
        }
    }
    return sb.toString()
}

/**
 * Copy plain text to the system clipboard. Returns false when the
 * clipboard service is unavailable or refuses — callers must not
 * claim "copied" in that case.
 */
private fun copyTextToClipboard(
    context: android.content.Context,
    label: String,
    text: String
): Boolean = try {
    val clipboard = context.getSystemService(
        android.content.Context.CLIPBOARD_SERVICE
    ) as android.content.ClipboardManager
    clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
    true
} catch (t: Throwable) {
    false
}

/**
 * A long-press context menu waiting to be shown (v0.42.0). Carries
 * the SOURCE tab's engine and incognito flag, captured when the
 * long-press fired: "open in new tab" must open the same kind of tab
 * (an incognito long-press must never spawn a normal tab), which the
 * pre-0.42.0 menus got wrong — they always opened a normal tab on
 * the main engine.
 */
private data class PageContextMenuState(
    val linkUrl: String?,
    val imageUrl: String?,
    val engine: Engine,
    val incognito: Boolean
)

/** A WebView SSL certificate error waiting for the user's decision. */
private data class SslPromptState(
    val url: String,
    val description: String,
    val respond: (Boolean) -> Unit
)

@OptIn(FlowPreview::class)
@Composable
fun BrowserApp(
    initialUrl: String? = null,
    onInitialUrlConsumed: () -> Unit = {},
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val store = remember { Store(context) }
    var screen by remember {
        mutableStateOf(if (store.mainEngineChosen) Screen.BROWSER else Screen.PICKER)
    }
    val tabs = remember { mutableStateListOf<BrowserTab>() }
    var activeTabId by remember { mutableStateOf(-1) }
    // Startup loading split (v0.28.0 audit): the three big JSON lists
    // start out EMPTY here and are filled in by the startup effect
    // below, which parses them on a background dispatcher before it
    // restores any tab. That ordering is what makes the split safe:
    // nothing can read or mutate these lists before the fill —
    // visits are recorded only by tab callbacks, bookmark/read-later
    // actions need an active tab, and tabs are created only later in
    // that same effect. Parsing them inside remember{} instead ran
    // the whole lot (history alone is capped at 500 entries) on the
    // main thread during the very first composition.
    val bookmarks = remember { mutableStateListOf<Bookmark>() }
    val history = remember { mutableStateListOf<HistoryItem>() }
    val readingList = remember { mutableStateListOf<ReadingItem>() }
    // Speed dials stay synchronous on purpose: the list is hard-capped
    // at 12 tiny entries (a couple of KB of JSON at most) and the home
    // page shows them, so there is nothing worth deferring.
    val speedDials = remember {
        mutableStateListOf<SpeedDial>().apply { addAll(store.loadSpeedDials()) }
    }
    // Compat sites also stay synchronous on purpose: the very first
    // restored tab must already know them (effectiveEngine / the
    // shouldUseWebView check run when that tab is created and first
    // loads), and the set is a handful of domain strings.
    val compatSites = remember {
        mutableStateListOf<String>().apply { addAll(store.loadCompatSites()) }
    }
    var pageContextMenu by remember {
        mutableStateOf<PageContextMenuState?>(null)
    }
    val recentClosedTabs = remember { mutableStateListOf<ClosedTabInfo>() }
    // The search engine as Compose state: the store is the source of
    // truth on disk, this mirror is what the address bar and the home
    // page read, so a change made on either the settings page or the
    // home-page chip applies everywhere at once.
    var searchEngineState by remember { mutableStateOf(store.searchEngine) }
    LaunchedEffect(screen) {
        searchEngineState = store.searchEngine
    }
    // Bumped whenever something outside the settings page rewrites
    // the stored settings wholesale (today: a backup import). The
    // settings page keys its store-mirroring states on this tick, so
    // an import applied while the settings page is open re-reads the
    // store instead of showing the pre-import values until the user
    // leaves and re-enters (the v0.19.0 known limitation, fixed in
    // v0.33.0).
    var settingsRefreshTick by remember { mutableStateOf(0) }
    var startupDone by remember { mutableStateOf(false) }
    var fullscreenView by remember { mutableStateOf<android.view.View?>(null) }
    var geckoFullscreen by remember { mutableStateOf(false) }
    var jsPromptRequest by remember { mutableStateOf<JsPromptState?>(null) }
    var authPromptRequest by remember { mutableStateOf<AuthPromptState?>(null) }
    var sslPromptRequest by remember { mutableStateOf<SslPromptState?>(null) }
    // The tab whose page resources the sniffer screen is showing.
    var sniffTabId by remember { mutableStateOf(-1) }
    // The tab waiting on the screenshot save/share dialog.
    var screenshotTab by remember { mutableStateOf<BrowserTab?>(null) }
    // A download waiting on the user's system-vs-TuxunDown choice.
    var askDownload by remember { mutableStateOf<AskDownloadRequest?>(null) }

    /**
     * Hand link text to TuxunDown through its only public entry: an
     * ACTION_SEND share with the link(s) in EXTRA_TEXT. Returns false
     * when TuxunDown is not installed or refuses the intent.
     */
    fun sendToTuxunDown(text: String): Boolean {
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(android.content.Intent.EXTRA_TEXT, text)
                .setPackage(TUXUNDOWN_PACKAGE)
                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context.packageManager.resolveActivity(intent, 0) == null) {
                return false
            }
            context.startActivity(intent)
            true
        } catch (t: Throwable) {
            false
        }
    }

    /** Send to TuxunDown; when it is missing, say so and fall back. */
    fun tuxunDownOrFallback(text: String, systemFallback: () -> Unit) {
        if (!sendToTuxunDown(text)) {
            android.widget.Toast.makeText(
                context, "没有装 TuxunDown，已改用系统下载",
                android.widget.Toast.LENGTH_LONG
            ).show()
            systemFallback()
        }
    }

    /**
     * The one dispatch point for URL-type downloads (WebView download
     * listener, sniffer items). Gecko tab downloads never come here —
     * the app saves those itself. [systemFallback] is the caller's
     * existing system-download call, unchanged.
     */
    fun downloadWithMode(
        url: String,
        filenameHint: String?,
        fromIncognito: Boolean = false,
        systemFallback: () -> Unit
    ) {
        if (url.isBlank()) return
        // Incognito honesty (v0.27.0): a system download is recorded
        // by the system's DownloadManager and stays visible in the
        // system download list (and this app's Downloads page) after
        // the incognito tab is gone. That is the user's own action,
        // not a silent leak — but it must be said once, at the moment
        // the system download actually starts. Wrapping the fallback
        // means the reminder fires only when a system download really
        // happens (never for TuxunDown shares, never in normal tabs).
        val effectiveFallback: () -> Unit = if (fromIncognito) {
            {
                android.widget.Toast.makeText(
                    context,
                    "无痕提醒：下载记录会留在系统下载管理里，关掉无痕标签也不会自动删掉",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                systemFallback()
            }
        } else {
            systemFallback
        }
        when (store.downloadMode) {
            DownloadMode.SYSTEM -> effectiveFallback()
            DownloadMode.TUXUNDOWN -> tuxunDownOrFallback(url, effectiveFallback)
            DownloadMode.ASK -> askDownload = AskDownloadRequest(
                message = filenameHint?.takeIf { it.isNotBlank() } ?: url,
                onSystem = effectiveFallback,
                onTuxunDown = { tuxunDownOrFallback(url, effectiveFallback) }
            )
        }
    }

    /**
     * Batch version of [downloadWithMode] (sniffer "download all"):
     * never asks or shares per item — the whole batch is one share
     * with one link per line, and ask mode asks exactly once.
     */
    fun downloadBatchWithMode(
        urls: List<String>,
        fromIncognito: Boolean = false,
        systemFallback: () -> Unit
    ) {
        val clean = urls.filter { it.isNotBlank() }
        if (clean.isEmpty()) return
        val joined = clean.joinToString("\n")
        // Same incognito honesty as downloadWithMode: one reminder
        // for the whole batch, only if a system download happens.
        val effectiveFallback: () -> Unit = if (fromIncognito) {
            {
                android.widget.Toast.makeText(
                    context,
                    "无痕提醒：下载记录会留在系统下载管理里，关掉无痕标签也不会自动删掉",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                systemFallback()
            }
        } else {
            systemFallback
        }
        when (store.downloadMode) {
            DownloadMode.SYSTEM -> effectiveFallback()
            DownloadMode.TUXUNDOWN -> tuxunDownOrFallback(joined, effectiveFallback)
            DownloadMode.ASK -> askDownload = AskDownloadRequest(
                message = "共 ${clean.size} 个链接",
                onSystem = effectiveFallback,
                onTuxunDown = { tuxunDownOrFallback(joined, effectiveFallback) }
            )
        }
    }

    fun recordVisit(title: String, url: String) {
        if (history.firstOrNull()?.url == url) return
        history.add(0, HistoryItem(title.ifBlank { url }, url, System.currentTimeMillis()))
        if (history.size > 500) {
            val trimmed = history.take(500)
            history.clear()
            history.addAll(trimmed)
        }
        store.saveHistory(history)
    }

    fun activeTab(): BrowserTab? = tabs.firstOrNull { it.id == activeTabId }

    /**
     * Engine sleep pass (v0.41.0): keep at most [ALIVE_ENGINE_LIMIT]
     * tab engines alive. When the alive count is over the cap, the
     * excess is slept away starting from the least recently used
     * NON-ACTIVE tabs (see BrowserTab.lastUsedAt / sleepEngine).
     * The active tab never sleeps, and a tab whose Gecko download
     * stream is still being copied to disk is skipped for this pass
     * (closing its session could cut the download off; it becomes
     * eligible again on the next pass after the copy finishes). If
     * downloads keep the count over the cap for now, that is fine —
     * the pass runs again on every tab switch. Incognito tabs are
     * treated exactly like normal ones: sleeping only destroys the
     * in-memory engine, nothing is written anywhere. Sleeping is
     * silent in the UI on purpose — no badge on the tab cards; a
     * slept tab simply reloads its page when switched back to.
     */
    fun enforceEngineSleepLimit() {
        val alive = tabs.filter { it.engineCreated }
        if (alive.size <= ALIVE_ENGINE_LIMIT) return
        val excess = alive.size - ALIVE_ENGINE_LIMIT
        val candidates = alive
            .filter { it.id != activeTabId && !it.geckoDownloadActive }
            .sortedBy { it.lastUsedAt }
        var slept = 0
        for (candidate in candidates) {
            if (slept >= excess) break
            candidate.sleepEngine()
            slept++
        }
    }

    // Gecko tabs report remembered compat sites through this handler; it is
    // wired up below once the tab-replacement helper exists.
    var compatRedirectHandler: (String) -> Unit = {}

    // WebView tabs report a killed rendering process through this handler;
    // wired up below next to compatRedirectHandler. The dead tab and the
    // URL it was showing are passed so the tab can be rebuilt in place.
    var renderProcessGoneHandler: (BrowserTab, String) -> Unit = { _, _ -> }

    // WebView pages ask for files through this handler; wired up below
    // once the activity-result launcher exists. The default answers null
    // (cancelled) so a page is never left waiting.
    var fileChooserHandler: (
        android.webkit.WebChromeClient.FileChooserParams,
        android.webkit.ValueCallback<Array<android.net.Uri>>
    ) -> Unit = { _, callback -> callback.onReceiveValue(null) }

    // Gecko pages ask for files through PromptDelegate.FilePrompt; the
    // handler is wired up below next to the WebView file chooser so both
    // engines share the one system file picker. The default answers
    // "cancelled" (empty list) so a page is never left waiting.
    var geckoFileChooserHandler: (
        GeckoSession.PromptDelegate.FilePrompt,
        (List<android.net.Uri>) -> Unit
    ) -> Unit = { _, respond -> respond(emptyList()) }

    // Gecko JavaScript dialogs (alert/confirm/prompt) land here; wired
    // up below. The default cancels so a page is never left waiting.
    var geckoJsPromptHandler: (
        GeckoJsPromptKind, String, String, (Boolean, String?) -> Unit
    ) -> Unit = { _, _, _, respond -> respond(false, null) }

    // HTTP Basic-auth challenges from both engines land here; wired
    // up below next to the JS prompt handler. The default cancels so
    // a page is never left waiting.
    var httpAuthHandler: (
        HttpAuthRequestInfo, (String?, String?) -> Unit
    ) -> Unit = { _, respond -> respond(null, null) }

    // WebView SSL certificate errors land here; wired up below. The
    // default refuses, matching the platform's safe behaviour.
    var sslErrorHandler: (
        String, String, (Boolean) -> Unit
    ) -> Unit = { _, _, respond -> respond(false) }

    // Web pages asking for the camera/microphone (WebView side) land
    // here; wired up below next to the system-permission launcher. The
    // default denies so a page is never left waiting.
    var webPermissionHandler: (android.webkit.PermissionRequest) -> Unit =
        { request -> request.deny() }

    // The same for Gecko tabs, split by GeckoView's three callbacks;
    // wired up below. Defaults refuse everything.
    var geckoAndroidPermHandler: (
        Array<String>, GeckoSession.PermissionDelegate.Callback
    ) -> Unit = { _, callback -> callback.reject() }
    var geckoContentPermHandler: (
        GeckoSession.PermissionDelegate.ContentPermission
    ) -> Int = { GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY }
    var geckoMediaPermHandler: (
        String,
        Array<GeckoSession.PermissionDelegate.MediaSource>?,
        Array<GeckoSession.PermissionDelegate.MediaSource>?,
        GeckoSession.PermissionDelegate.MediaCallback
    ) -> Unit = { _, _, _, callback -> callback.reject() }

    // Pages asking for a new window (target=_blank / window.open) land
    // here: WebView tabs report just the target URL; Gecko tabs hand
    // over the new session GeckoView asked us to create. Wired up below
    // next to compatRedirectHandler. Defaults do nothing.
    var newWindowHandler: (BrowserTab, String) -> Unit = { _, _ -> }
    var geckoNewSessionHandler: (
        BrowserTab, GeckoSession, String
    ) -> Unit = { _, _, _ -> }

    fun makeTab(
        engine: Engine,
        incognito: Boolean,
        url: String,
        desktopMode: Boolean = false
    ): BrowserTab {
        // The render-process-gone callback needs to know which tab died,
        // but the tab does not exist yet while it is being constructed —
        // so the lambda reads this holder, filled in right after.
        var created: BrowserTab? = null
        val tab = BrowserTab(
            engine = engine,
            incognito = incognito,
            startUrl = url,
            desktopMode = desktopMode,
            onVisit = { t, u -> recordVisit(t, u) },
            shouldUseWebView = { target ->
                engine == Engine.GECKO && isCompatHost(hostOf(target), compatSites.toSet())
            },
            onCompatRedirect = { target -> compatRedirectHandler(target) },
            isAdBlockEnabled = { store.adBlockEnabled },
            onPageContextMenu = { link, image ->
                val src = created
                pageContextMenu = PageContextMenuState(
                    linkUrl = link,
                    imageUrl = image,
                    engine = src?.engine ?: engine,
                    incognito = src?.incognito ?: incognito
                )
            },
            textZoomProvider = { store.textZoomPercent },
            webDarkProvider = { store.webDarkMode },
            thirdPartyCookiesProvider = { store.allowThirdPartyCookies },
            onFullscreenShow = { view -> fullscreenView = view },
            onFullscreenHide = { fullscreenView = null },
            onGeckoFullscreen = { geckoFullscreen = it },
            onRendererGone = { deadUrl ->
                created?.let { renderProcessGoneHandler(it, deadUrl) }
            },
            onFileChooserRequest = { params, callback ->
                fileChooserHandler(params, callback)
            },
            onWebPermissionRequest = { request -> webPermissionHandler(request) },
            onGeckoFileChooserRequest = { prompt, respond ->
                geckoFileChooserHandler(prompt, respond)
            },
            onGeckoJsPrompt = { kind, message, defaultValue, respond ->
                geckoJsPromptHandler(kind, message, defaultValue, respond)
            },
            onHttpAuth = { info, respond -> httpAuthHandler(info, respond) },
            onSslError = { failedUrl, description, respond ->
                sslErrorHandler(failedUrl, description, respond)
            },
            onGeckoAndroidPermissions = { perms, callback ->
                geckoAndroidPermHandler(perms, callback)
            },
            onGeckoContentPermission = { perm -> geckoContentPermHandler(perm) },
            onGeckoMediaPermission = { uri, video, audio, callback ->
                geckoMediaPermHandler(uri, video, audio, callback)
            },
            onNewWindowRequest = { target ->
                created?.let { newWindowHandler(it, target) }
            },
            onGeckoNewSession = { session, uri ->
                created?.let { geckoNewSessionHandler(it, session, uri) }
            },
            onUrlDownload = { dlUrl, ua, cd, mt ->
                downloadWithMode(
                    dlUrl, null,
                    fromIncognito = created?.incognito == true
                ) {
                    startSystemDownload(
                        context, dlUrl, ua, cd, mt,
                        referer = created?.url
                    )
                }
            },
            onDirectDownload = { target ->
                // A tapped direct file link, intercepted by either
                // engine before navigating. URL-type dispatch, same
                // as the download listener: startSystemDownload
                // fills in the current session's Cookie and a
                // fallback User-Agent itself; the Referer is the
                // page the tap happened on (the tab has not moved).
                val fileName = resolveDownloadFileName(target, null, null)
                downloadWithMode(
                    target, fileName,
                    fromIncognito = created?.incognito == true
                ) {
                    startSystemDownload(
                        context, target, null, null, null,
                        referer = created?.url
                    )
                }
                if (store.downloadMode != DownloadMode.ASK) {
                    android.widget.Toast.makeText(
                        context, "已开始下载 $fileName",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            isAdHideSlotsEnabled = {
                store.adBlockEnabled && store.adHideSlots
            },
            onBlockedRequest = { store.noteBlocked() }
        )
        created = tab
        return tab
    }

    fun effectiveEngine(engine: Engine, url: String): Engine =
        if (engine == Engine.GECKO && url.startsWith("http") &&
            isCompatHost(hostOf(url), compatSites.toSet())
        ) Engine.WEBVIEW else engine

    /**
     * Wipe the shared WebView cookie jar and web storage once no
     * incognito WebView tab is left (v0.27.0 audit). The system
     * WebView keeps ONE cookie jar for the whole app, so an incognito
     * WebView tab's cookies live in the same jar normal tabs read —
     * the moment the last incognito WebView tab goes away (closed,
     * batch-closed, or destroyed by an engine switch), the jar must
     * be wiped or those cookies leak into normal browsing. Incognito
     * Gecko tabs do NOT keep the jar alive: they are truly private
     * (usePrivateMode) and never touch it. Callers invoke this only
     * when an incognito tab was actually involved. (Side effect, as
     * documented since v0.2.0: normal WebView tabs get signed out —
     * the jar is shared, there is no per-tab alternative.)
     */
    fun wipeWebViewDataIfIncognitoWebViewGone() {
        if (tabs.none { it.incognito && it.engine == Engine.WEBVIEW }) {
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()
            android.webkit.WebStorage.getInstance().deleteAllData()
        }
    }

    fun newTab(
        engine: Engine,
        incognito: Boolean,
        url: String = HOME_URL,
        desktopMode: Boolean = false
    ): BrowserTab {
        // The tab being switched away from gets its one switch-away
        // thumbnail capture (the other capture point is page finish).
        // Deferred: drawing the bitmap in the same frame the switch
        // crossfade starts could stall the transition's first frame.
        activeTab()?.captureThumbnailDeferred()
        val tab = makeTab(effectiveEngine(engine, url), incognito, url, desktopMode)
        tabs.add(tab)
        activeTabId = tab.id
        return tab
    }

    fun recreateTab(
        tab: BrowserTab,
        newEngine: Engine,
        desktopMode: Boolean,
        force: Boolean = false
    ): BrowserTab {
        val idx = tabs.indexOf(tab)
        if (idx < 0) return tab
        if (!force && tab.engine == newEngine && tab.desktopMode == desktopMode) return tab
        val replacement = makeTab(newEngine, tab.incognito, tab.url, desktopMode)
        tabs[idx] = replacement
        tab.destroy()
        // Engine switch / recreation destroys the old tab without
        // going through closeTab: if it was an incognito WebView tab
        // and its replacement is not one (switched to Gecko), the
        // shared cookie jar may now be unowned — wipe it under the
        // same rule as closing (v0.27.0). A WebView-to-WebView
        // recreation keeps the replacement as the jar's owner, so
        // the helper no-ops and the session survives.
        if (tab.incognito && tab.engine == Engine.WEBVIEW) {
            wipeWebViewDataIfIncognitoWebViewGone()
        }
        if (activeTabId == tab.id) activeTabId = replacement.id
        return replacement
    }

    fun replaceTabEngine(tab: BrowserTab, newEngine: Engine): BrowserTab =
        recreateTab(tab, newEngine, tab.desktopMode)

    compatRedirectHandler = { target ->
        val tab = activeTab()
        if (tab != null && tab.engine == Engine.GECKO) {
            val replacement = replaceTabEngine(tab, Engine.WEBVIEW)
            if (replacement.url != target) replacement.load(target)
        }
    }

    // A page asked for a new window: open the target in a real new tab
    // (same engine, same incognito flag as the page's tab) through the
    // usual tab-creation path and switch to it.
    newWindowHandler = { source, target ->
        newTab(source.engine, source.incognito, target, source.desktopMode)
        screen = Screen.BROWSER
    }

    // Same for Gecko tabs: the source tab's onNewSession already
    // created and opened the new session; build the new tab around it.
    // attachAdoptedGeckoSession wires the new tab's delegates into the
    // session synchronously, before Gecko starts loading the page.
    geckoNewSessionHandler = { source, session, uri ->
        val tab = makeTab(source.engine, source.incognito, uri, source.desktopMode)
        tab.attachAdoptedGeckoSession(session, context)
        tabs.add(tab)
        activeTabId = tab.id
        screen = Screen.BROWSER
    }

    // A WebView tab's rendering process was killed by the system: rebuild
    // the tab in place (same slot, same engine, same incognito flag) via
    // the usual tab-recreation path and reload the page it was on. Works
    // for incognito tabs too — only the URL is carried over, nothing is
    // written to disk that was not already there.
    renderProcessGoneHandler = { tab, deadUrl ->
        if (tab.engine == Engine.WEBVIEW && tabs.contains(tab)) {
            // Posted, not run inline: this fires from inside the dead
            // WebView's own client callback, and rebuilding destroys that
            // WebView — safer once the callback has returned.
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (!tabs.contains(tab)) return@post
                val target = deadUrl.ifBlank { tab.url }
                val replacement = recreateTab(
                    tab, Engine.WEBVIEW, tab.desktopMode, force = true
                )
                if (target.isNotBlank() && target != HOME_URL &&
                    replacement.url != target
                ) {
                    replacement.load(target)
                }
                android.widget.Toast.makeText(
                    context, "页面进程被系统结束，已自动恢复",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    fun addCompatSite(domain: String) {
        store.addCompatSite(domain)
        if (domain !in compatSites) compatSites.add(domain)
    }

    fun removeCompatSite(domain: String) {
        store.removeCompatSite(domain)
        compatSites.remove(domain)
    }

    fun switchTabEngine(tab: BrowserTab, newEngine: Engine, rememberSite: Boolean) {
        if (tab.engine == newEngine) return
        val host = hostOf(tab.url)
        if (rememberSite && host != null) {
            when (newEngine) {
                Engine.WEBVIEW -> addCompatSite(host)
                Engine.GECKO -> removeCompatSite(host)
            }
        }
        replaceTabEngine(tab, newEngine)
    }

    /**
     * Push a closing tab onto the recently-closed stack (newest
     * first, capped at [MAX_RECENT_CLOSED], oldest dropped).
     * Incognito tabs are skipped unconditionally.
     */
    fun pushClosedTab(tab: BrowserTab) {
        if (tab.incognito) return
        recentClosedTabs.add(
            0,
            ClosedTabInfo(
                tab.title.ifBlank { tab.url },
                tab.engine, tab.url, tab.desktopMode
            )
        )
        while (recentClosedTabs.size > MAX_RECENT_CLOSED) {
            recentClosedTabs.removeAt(recentClosedTabs.lastIndex)
        }
    }

    fun closeTab(tab: BrowserTab) {
        val idx = tabs.indexOf(tab)
        val wasIncognito = tab.incognito
        pushClosedTab(tab)
        tab.destroy()
        tabs.remove(tab)
        if (wasIncognito) {
            // Wipe once no incognito WebView tab is left — including
            // the case where incognito Gecko tabs remain open: they
            // never owned the shared WebView jar, so waiting for ALL
            // incognito tabs to close would leak (v0.27.0).
            wipeWebViewDataIfIncognitoWebViewGone()
        }
        if (tabs.isEmpty()) {
            newTab(store.mainEngine, false)
        } else if (activeTabId == tab.id) {
            activeTabId = tabs[minOf(idx, tabs.size - 1)].id
        }
    }

    /** Reopen one entry of the recently-closed stack and drop it. */
    fun reopenClosedTab(info: ClosedTabInfo) {
        recentClosedTabs.remove(info)
        newTab(info.engine, false, info.url, info.desktopMode)
        screen = Screen.BROWSER
    }

    fun closeAllTabs() {
        val hadIncognito = tabs.any { it.incognito }
        // Every normal tab goes onto the recently-closed stack
        // (incognito tabs are skipped inside pushClosedTab).
        tabs.toList().forEach { pushClosedTab(it) }
        tabs.forEach { it.destroy() }
        tabs.clear()
        if (hadIncognito) {
            // Same rule as closing the last incognito tab one by one:
            // with every tab gone the helper's condition always holds.
            wipeWebViewDataIfIncognitoWebViewGone()
        }
        newTab(store.mainEngine, false)
    }

    /**
     * Close every tab except the active one. Normal tabs land on the
     * recently-closed stack (up to its cap) so they can be reopened
     * one by one; the sheet still asks for confirmation first because
     * a large batch can overflow the stack and incognito tabs are
     * never recorded.
     */
    fun closeOtherTabs() {
        val keep = activeTab() ?: return
        val others = tabs.filter { it.id != keep.id }
        if (others.isEmpty()) return
        val closedIncognito = others.any { it.incognito }
        others.forEach { pushClosedTab(it) }
        others.forEach { it.destroy() }
        tabs.removeAll(others)
        if (closedIncognito) {
            // Same rule as closing the last incognito tab one by
            // one — remaining incognito Gecko tabs do not keep the
            // shared WebView jar alive (v0.27.0).
            wipeWebViewDataIfIncognitoWebViewGone()
        }
        android.widget.Toast.makeText(
            context, "已关闭 ${others.size} 个标签页",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    // Session restore: reopen the tabs that were open when the app was
    // last closed (normal tabs only — incognito tabs are never saved).
    // Runs once, before the persistence effect below is allowed to save
    // anything, so a fresh start can never wipe the saved session first.
    LaunchedEffect(Unit) {
        // Background fill of the three big lists (v0.28.0): parse the
        // stored JSON off the main thread, then publish the results
        // into the state lists above. This must stay the FIRST step of
        // this effect — the retention cleanup below reads history, and
        // tab restore further down is what makes user actions on these
        // lists possible at all, so the fill always lands first.
        val loadedLists = kotlinx.coroutines.withContext(
            kotlinx.coroutines.Dispatchers.Default
        ) {
            Triple(
                store.loadBookmarks(),
                store.loadHistory(),
                store.loadReadingList()
            )
        }
        bookmarks.addAll(loadedLists.first)
        history.addAll(loadedLists.second)
        readingList.addAll(loadedLists.third)
        // History retention cleanup: drop normal-history entries older
        // than the user's chosen window. Runs inside this effect (off
        // the startup critical path, filtering on a background
        // dispatcher); incognito visits are never recorded, so they
        // are untouched by definition. It runs after the history fill
        // above, so it always sees the full stored list (v0.16.0).
        val retentionDays = store.historyRetention.days
        if (retentionDays > 0 && history.isNotEmpty()) {
            val cutoff = System.currentTimeMillis() - retentionDays * 86_400_000L
            val snapshot = history.toList()
            val kept = kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.Default
            ) {
                snapshot.filter { it.time >= cutoff }
            }
            if (kept.size != history.size) {
                history.clear()
                history.addAll(kept)
                store.saveHistory(history)
            }
        }
        if (store.mainEngineChosen && tabs.isEmpty()) {
            // The saved-session JSON is parsed off the main thread
            // too (v0.28.0); restore still happens only after this
            // returns, and startupDone is still set only at the very
            // end, so the persistence effect below can never save
            // before restore has run (the v0.4.0 guard is intact).
            val (saved, savedActive) = kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.Default
            ) {
                store.loadTabs()
            }
            val usable = if (store.startupRestore) {
                saved.filter { it.url.startsWith("http") || it.url == HOME_URL }
            } else {
                emptyList()
            }
            if (usable.isNotEmpty()) {
                // Lazy restore (v0.23.0): makeTab only builds the
                // tab's data (url / engine / desktop mode). No engine
                // view is created here — TabContent is composed only
                // for the active tab, so only that one obtains its
                // WebView/GeckoSession and loads; every other
                // restored tab is built and loaded on its first
                // switch-to (see BrowserTab.engineCreated). Startup
                // cost is therefore one page load, however many tabs
                // were open last time.
                usable.forEach { st ->
                    tabs.add(
                        makeTab(
                            effectiveEngine(st.engine, st.url),
                            false, st.url, st.desktopMode
                        )
                    )
                }
                activeTabId = tabs[savedActive.coerceIn(0, tabs.lastIndex)].id
            } else {
                newTab(store.mainEngine, false)
            }
        }
        startupDone = true
        // Auto cache management (v0.40.0), deliberately LAST in this
        // effect: startup is fully done, and the size walk runs on
        // the IO dispatcher so it never blocks a frame. Only the
        // WebView cache is measured (cacheDir + cache-named dirs
        // under app_webview, the v0.20.0 storage-card directories).
        // Over the threshold, a throwaway WebView's clearCache(true)
        // clears the whole app WebView cache — that call touches
        // cache only, never cookies, web storage or history, so
        // sign-ins survive. A temporary WebView is used so the clear
        // does not depend on any tab having been created yet. No
        // toast: the outcome is logged, and the policy is written
        // out under the storage card on the clear-data page. The
        // Gecko cache is the kernel's own business (it has its own
        // size cap) and is not touched here.
        try {
            val cacheBytes = kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.IO
            ) {
                webViewCacheSizeBytes(
                    context.cacheDir,
                    java.io.File(context.applicationInfo.dataDir)
                )
            }
            if (cacheBytes > WEBVIEW_CACHE_AUTO_CLEAR_THRESHOLD_BYTES) {
                val tempWebView = android.webkit.WebView(context)
                try {
                    tempWebView.clearCache(true)
                } finally {
                    tempWebView.destroy()
                }
                android.util.Log.i(
                    "TuxunBrowse",
                    "Auto-cleared WebView cache at startup: " +
                        "$cacheBytes bytes was over the " +
                        "$WEBVIEW_CACHE_AUTO_CLEAR_THRESHOLD_BYTES byte limit"
                )
            }
        } catch (t: Throwable) {
            // Cache housekeeping must never break startup.
        }
    }

    // Keep the saved session in sync with the live tab list.
    // Debounced (v0.28.0): tab state changes on every navigation step
    // (each redirect / URL change re-emits), and every emission used
    // to serialize the whole session JSON on the main thread and push
    // it through SharedPreferences. Saving only the state that is
    // still current 400 ms after the last change keeps the archive
    // just as complete — intermediate URLs within one navigation were
    // never worth persisting — at a fraction of the writes. Trade-off
    // on record: if the process dies inside that 400 ms window, the
    // archive can lag the very last navigation by one step.
    LaunchedEffect(startupDone) {
        if (!startupDone) return@LaunchedEffect
        snapshotFlow {
            val normal = tabs.filter { !it.incognito }
            normal.map { Triple(it.engine, it.url, it.desktopMode) } to
                normal.indexOfFirst { it.id == activeTabId }
        }.debounce(400L).collect { (list, idx) ->
            store.saveTabs(
                list.map { SavedTab(it.first, it.second, it.third) },
                idx.coerceAtLeast(0)
            )
        }
    }

    // Bookmark import: system file picker. Two formats are accepted —
    // this app's own JSON export, and the standard Netscape HTML file
    // every desktop browser exports — told apart by sniffing the
    // content, not the file name. Merged in with duplicates (same
    // URL, against existing bookmarks and within the file) skipped.
    val importBookmarksLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val raw = readImportFileText(context, uri)
            val head = raw.trimStart()
            val isHtml = head.startsWith("<!DOCTYPE NETSCAPE", ignoreCase = true) ||
                head.startsWith("<DL", ignoreCase = true)
            var skipped = 0
            val parsed = if (isHtml) {
                val (list, bad) = bookmarksFromHtml(raw)
                skipped += bad
                list
            } else {
                bookmarksFromJson(raw)
            }
            // De-duplicate within the file itself as well.
            val imported = parsed.distinctBy { it.url }
            skipped += parsed.size - imported.size
            var added = 0
            imported.forEach { bm ->
                if (bookmarks.none { it.url == bm.url }) {
                    bookmarks.add(bm)
                    added++
                } else {
                    skipped++
                }
            }
            if (added > 0) store.saveBookmarks(bookmarks)
            android.widget.Toast.makeText(
                context,
                if (imported.isEmpty() && skipped == 0) {
                    "这个文件里没有可导入的书签"
                } else {
                    "导入了 $added 条书签，跳过 $skipped 条（重复或不支持的）"
                },
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "文件有问题，没导入任何书签，请换一个文件重试",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // ---- Full backup / restore (settings page "备份与恢复") ----
    // A parsed backup waiting for the user's overwrite confirmation.
    var pendingBackup by remember { mutableStateOf<BackupData?>(null) }

    fun exportBackup() {
        val json = backupToJson(
            bookmarks, history, readingList, speedDials,
            BackupSettings(
                mainEngine = store.mainEngine,
                incognitoEngine = store.incognitoEngine,
                searchEngine = store.searchEngine,
                customSearchName = store.customSearchName,
                customSearchTemplate = store.customSearchTemplate,
                adBlockEnabled = store.adBlockEnabled,
                downloadMode = store.downloadMode,
                historyRetention = store.historyRetention,
                textZoomPercent = store.textZoomPercent,
                startupRestore = store.startupRestore
            )
        )
        val fileName = "tuxunbrowse-backup.json"
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(
                        android.provider.MediaStore.Downloads.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                }
                val dest = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                ) ?: throw IllegalStateException("insert failed")
                resolver.openOutputStream(dest)?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                }
            } else {
                val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                java.io.File(dir, fileName).writeText(json, Charsets.UTF_8)
            }
            android.widget.Toast.makeText(
                context, "备份已导出到下载文件夹：$fileName",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "导出失败，请重试", android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Merge a confirmed backup into the live data: lists merge with
     * de-duplication (bookmarks/reading list/speed dials by URL,
     * history by URL + time), settings overwrite. Returns the
     * per-list added counts for the result toast.
     */
    fun applyBackup(data: BackupData): String {
        var addedBookmarks = 0
        data.bookmarks.distinctBy { it.url }.forEach { bm ->
            if (bookmarks.none { it.url == bm.url }) {
                bookmarks.add(bm)
                addedBookmarks++
            }
        }
        if (addedBookmarks > 0) store.saveBookmarks(bookmarks)
        var addedHistory = 0
        val historyKeys = history.map { it.url + "|" + it.time }.toHashSet()
        data.history.forEach { item ->
            if (historyKeys.add(item.url + "|" + item.time)) {
                history.add(item)
                addedHistory++
            }
        }
        if (addedHistory > 0) {
            val sorted = history.sortedByDescending { it.time }.take(500)
            history.clear()
            history.addAll(sorted)
            store.saveHistory(history)
        }
        var addedReading = 0
        data.readingList.distinctBy { it.url }.forEach { item ->
            if (readingList.none { it.url == item.url }) {
                readingList.add(item)
                addedReading++
            }
        }
        if (addedReading > 0) {
            while (readingList.size > 200) {
                readingList.removeAt(readingList.lastIndex)
            }
            store.saveReadingList(readingList)
        }
        var addedDials = 0
        data.speedDials.distinctBy { it.url }.forEach { dial ->
            if (speedDials.size < 12 && speedDials.none { it.url == dial.url }) {
                speedDials.add(dial)
                addedDials++
            }
        }
        if (addedDials > 0) store.saveSpeedDials(speedDials)
        val s = data.settings
        s.mainEngine?.let { store.mainEngine = it }
        s.incognitoEngine?.let { store.incognitoEngine = it }
        s.searchEngine?.let { store.searchEngine = it }
        s.customSearchName?.let { store.customSearchName = it }
        s.customSearchTemplate?.let { store.customSearchTemplate = it }
        s.adBlockEnabled?.let { store.adBlockEnabled = it }
        s.downloadMode?.let { store.downloadMode = it }
        s.historyRetention?.let { store.historyRetention = it }
        s.startupRestore?.let { store.startupRestore = it }
        s.textZoomPercent?.let { percent ->
            store.textZoomPercent = percent
            tabs.forEach { it.applyTextZoom(percent) }
            GeckoHolder.applyFontSizeFactor(percent / 100f)
        }
        if (data.settings.overwriteSummary().isNotEmpty()) {
            // The store was just rewritten behind the settings
            // page's back: sync this level's search-engine mirror at
            // the source (same two-way sync the settings/home
            // pickers use) and bump the tick the settings page keys
            // its own mirrors on, so every page shows the imported
            // values at once instead of after a re-entry.
            searchEngineState = store.searchEngine
            settingsRefreshTick++
        }
        return "书签 +$addedBookmarks 条、历史 +$addedHistory 条、" +
            "阅读列表 +$addedReading 条、快捷方式 +$addedDials 条"
    }

    // Backup import: system file picker. The file is validated by
    // its format marker first — anything else is refused in plain
    // language and no data is touched. A valid file still changes
    // nothing until the user confirms the overwrite dialog.
    val importBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val raw = readImportFileText(context, uri)
            val parsed = parseBackup(raw)
            if (parsed == null) {
                android.widget.Toast.makeText(
                    context,
                    "这个文件不是 TuxunBrowse 的备份文件，数据没有动",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                pendingBackup = parsed
            }
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "文件有问题，没导入，请换一个备份文件重试",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    // Backup import confirmation: lists merge (duplicates skipped),
    // but settings are overwritten — so the dialog names exactly
    // which settings would change before anything is written.
    val backupToConfirm = pendingBackup
    if (backupToConfirm != null) {
        val overwrite = backupToConfirm.settings.overwriteSummary()
        AlertDialog(
            onDismissRequest = { pendingBackup = null },
            title = { Text("导入这个备份？") },
            text = {
                // The overwrite list can run to a dozen lines; on a
                // short screen a plain Text would push the buttons
                // off the bottom, so the body scrolls on its own.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "备份里的书签 ${backupToConfirm.bookmarks.size} 条、" +
                            "历史 ${backupToConfirm.history.size} 条、" +
                            "阅读列表 ${backupToConfirm.readingList.size} 条、" +
                            "快捷方式 ${backupToConfirm.speedDials.size} 条会合并进来，" +
                            "和现有的重复的会跳过，不会删掉现有的任何东西。" +
                            if (overwrite.isNotEmpty()) {
                                "\n\n下面这些设置会被备份里的值覆盖：\n" +
                                    overwrite.joinToString("\n") { "· $it" }
                            } else {
                                ""
                            }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    pendingBackup = null
                    val counts = applyBackup(backupToConfirm)
                    android.widget.Toast.makeText(
                        context,
                        "导入完成：$counts" +
                            if (overwrite.isNotEmpty()) "，设置已按备份更新" else "",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }) { Text("导入") }
            },
            dismissButton = {
                TextButton(onClick = { pendingBackup = null }) { Text("取消") }
            }
        )
    }

    /**
     * Clear one site's data: Gecko's per-host clear plus this app's
     * own history entries for the host (subdomains included). The
     * system WebView has no per-site API, so WebView data is NOT
     * touched here — callers say so in their copy.
     */
    fun clearSiteData(host: String) {
        GeckoHolder.clearDataFromHost(host)
        val before = history.size
        history.removeAll {
            val h = hostOf(it.url)
            h == host || (h != null && h.endsWith(".$host"))
        }
        if (history.size != before) store.saveHistory(history)
    }

    /**
     * The site-info sheet's compat toggle: flip this host's compat
     * memory and move the current tab to the matching engine through
     * the usual tab-replacement path.
     */
    fun toggleSiteCompat() {
        val tab = activeTab() ?: return
        val host = hostOf(tab.url) ?: return
        if (isCompatHost(host, compatSites.toSet())) {
            removeCompatSite(host)
            if (tab.engine == Engine.WEBVIEW) {
                replaceTabEngine(tab, Engine.GECKO)
            }
            android.widget.Toast.makeText(
                context, "已取消 $host 的兼容模式",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        } else {
            addCompatSite(host)
            if (tab.engine == Engine.GECKO) {
                replaceTabEngine(tab, Engine.WEBVIEW)
            }
            android.widget.Toast.makeText(
                context, "已记住：$host 以后用兼容模式打开",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Web page file uploads, both engines, one shared system picker:
    // whichever engine asked last leaves its answer function in
    // pendingFileAnswer, and the picker's result goes to it. A new
    // request first answers the pending one with "cancelled" (empty
    // list) so no page is ever left waiting. WebView tabs arrive via
    // fileChooserHandler, Gecko tabs via geckoFileChooserHandler
    // (PromptDelegate.FilePrompt in TabController).
    var pendingFileAnswer by remember {
        mutableStateOf<((List<android.net.Uri>) -> Unit)?>(null)
    }
    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        pendingFileAnswer?.invoke(uris)
        pendingFileAnswer = null
    }
    fun launchFilePicker(mime: String, answer: (List<android.net.Uri>) -> Unit) {
        pendingFileAnswer?.invoke(emptyList())
        pendingFileAnswer = answer
        try {
            fileChooserLauncher.launch(arrayOf(mime))
        } catch (t: Throwable) {
            pendingFileAnswer = null
            answer(emptyList())
        }
    }
    fileChooserHandler = { params, callback ->
        val mime = params.acceptTypes?.firstOrNull { it.isNotBlank() } ?: "*/*"
        launchFilePicker(mime) { uris ->
            callback.onReceiveValue(
                if (uris.isEmpty()) null else uris.toTypedArray()
            )
        }
    }
    geckoFileChooserHandler = { prompt, respond ->
        val mime = prompt.mimeTypes?.firstOrNull { it.isNotBlank() } ?: "*/*"
        launchFilePicker(mime, respond)
    }

    // Gecko JavaScript dialogs: one at a time. A second prompt arriving
    // while a dialog is up is cancelled at once rather than queued, so
    // no page waits forever and no answer callback leaks.
    geckoJsPromptHandler = { kind, message, defaultValue, respond ->
        if (jsPromptRequest != null || authPromptRequest != null ||
            sslPromptRequest != null
        ) {
            respond(false, null)
        } else {
            jsPromptRequest = JsPromptState(kind, message, defaultValue, respond)
        }
    }

    // HTTP Basic-auth challenges (both engines): one dialog at a time,
    // sharing the stage with the JS prompt and SSL dialogs — a
    // challenge that arrives while any of them is up is cancelled at
    // once rather than stacked or queued. The credentials the user
    // types live only in the dialog state and this callback.
    httpAuthHandler = { info, respond ->
        if (authPromptRequest != null || jsPromptRequest != null ||
            sslPromptRequest != null
        ) {
            respond(null, null)
        } else {
            authPromptRequest = AuthPromptState(info, respond)
        }
    }

    // WebView SSL errors: same one-dialog rule. A second error while
    // one is already waiting is refused (cancelled) at once instead
    // of stacking another dialog.
    sslErrorHandler = { failedUrl, description, respond ->
        if (sslPromptRequest != null || authPromptRequest != null ||
            jsPromptRequest != null
        ) {
            respond(false)
        } else {
            sslPromptRequest = SslPromptState(failedUrl, description, respond)
        }
    }

    // ---- Web page camera / microphone permissions (both engines) ----
    // The system is only ever asked when a page itself asks first, and
    // a page only gets the camera/microphone for the parts the user
    // actually granted. One system request at a time: a request that
    // arrives while the system dialog is up is refused at once (with a
    // plain-language toast) instead of queueing behind it.
    var pendingSysPermAnswer by remember {
        mutableStateOf<((Set<String>) -> Unit)?>(null)
    }
    var pendingSysPerms by remember { mutableStateOf<List<String>>(emptyList()) }
    val sysPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        val answer = pendingSysPermAnswer
        val requested = pendingSysPerms
        pendingSysPermAnswer = null
        pendingSysPerms = emptyList()
        val granted = requested.filter {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, it
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }.toSet()
        answer?.invoke(granted)
    }
    fun requestSysPermissions(
        perms: Array<String>,
        answer: (Set<String>) -> Unit
    ) {
        val missing = perms.filter {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        when {
            missing.isEmpty() -> answer(perms.toSet())
            pendingSysPermAnswer != null -> {
                android.widget.Toast.makeText(
                    context, "上一个权限申请还没处理完，这个先拒绝了，请重试",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                answer(emptySet())
            }
            else -> {
                pendingSysPermAnswer = answer
                pendingSysPerms = missing
                try {
                    sysPermLauncher.launch(missing.toTypedArray())
                } catch (t: Throwable) {
                    pendingSysPermAnswer = null
                    pendingSysPerms = emptyList()
                    answer(emptySet())
                }
            }
        }
    }

    // Recreation safety net for pending page callbacks (v0.34.0
    // audit): every pending answer below lives in remember{} state —
    // the file picker's answer, the system-permission answer and the
    // JS / auth / SSL dialogs. If this composition is disposed while
    // one of them is still waiting (an activity recreation from a
    // configuration change this app does not handle itself, e.g. a
    // SIM or hardware-keyboard change), disposing is NOT the same as
    // dismissing: none of the dialogs' answer paths run, and the page
    // side would be left waiting on a callback nobody will ever call.
    // Answer everything still pending here, each with its "cancel"
    // value, so nothing dangles. Every engine-side answer wrapper
    // already guards against double answers with try/catch, and the
    // engines themselves are being torn down by the same recreation,
    // so a late cancel is harmless. Normal tab closing never reaches
    // this: BrowserApp only leaves the composition when the activity
    // itself goes away.
    DisposableEffect(Unit) {
        onDispose {
            try {
                pendingFileAnswer?.invoke(emptyList())
            } catch (t: Throwable) {
                // Engine already gone; nothing left to answer.
            }
            pendingFileAnswer = null
            try {
                pendingSysPermAnswer?.invoke(emptySet())
            } catch (t: Throwable) {
                // Engine already gone; nothing left to answer.
            }
            pendingSysPermAnswer = null
            pendingSysPerms = emptyList()
            try {
                jsPromptRequest?.respond?.invoke(false, null)
            } catch (t: Throwable) {
                // Engine already gone; nothing left to answer.
            }
            jsPromptRequest = null
            try {
                authPromptRequest?.respond?.invoke(null, null)
            } catch (t: Throwable) {
                // Engine already gone; nothing left to answer.
            }
            authPromptRequest = null
            try {
                sslPromptRequest?.respond?.invoke(false)
            } catch (t: Throwable) {
                // Engine already gone; nothing left to answer.
            }
            sslPromptRequest = null
            // Engine teardown on the way out (v0.35.0): this
            // composition only leaves when the activity itself is
            // destroyed (the user swipes the app away, or a
            // recreation this app does not handle via configChanges),
            // and until now nothing on that path ever called a tab's
            // destroy() — tab engines were only destroyed when a tab
            // was closed or recreated in place, and on activity death
            // they were simply abandoned to the system. Destroy each
            // created engine properly here (WebView.destroy /
            // GeckoSession.close). GeckoRuntime is a process-wide
            // singleton and is deliberately NOT shut down: closing
            // it would break a later start in the same process.
            tabs.forEach { tab ->
                try {
                    tab.destroy()
                } catch (t: Throwable) {
                    // One bad tab must not stop the others.
                }
            }
        }
    }

    // Host lifecycle wiring (v0.35.0). Before this version nothing
    // told the engines when the app went to the background:
    // MainActivity had no onPause/onResume/onStop overrides,
    // WebView.onPause/onResume were never called, and no
    // GeckoSession.setActive existed anywhere — page media kept
    // playing audibly in the background, JS timers and rendering
    // kept running, and every created Gecko session stayed
    // "active" forever, visible tab or not. This observer is the
    // one wiring point: ON_PAUSE/ON_STOP pauses every created
    // engine (the mainstream-browser rule: a backgrounded browser
    // does not keep playing media); ON_RESUME resumes them —
    // WebView tabs all resume, a Gecko session is reactivated
    // only when it is the visible tab. Downloads handed to the
    // system DownloadManager run in a system process and are NOT
    // affected by any of this pausing.
    val hostActivity = context as? androidx.activity.ComponentActivity
    DisposableEffect(hostActivity) {
        val activity = hostActivity
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    // Cookie flush (v0.39.0), on this same v0.35.0
                    // wiring: WebView cookies are persisted
                    // asynchronously by CookieManager, and before this
                    // version nothing ever flushed outside the two
                    // wipe paths — if the app was swiped away or
                    // killed, session cookies set since the last
                    // internal persist could be lost and sites signed
                    // the user out. ON_PAUSE is the right moment:
                    // every path out of the foreground (switching
                    // apps, locking the screen, being swiped away)
                    // passes through it, and flush() is cheap.
                    // Ordering vs the incognito wipe (v0.27.0): the
                    // wipe runs synchronously at tab-close time,
                    // while the app is still in the foreground, and
                    // itself calls removeAllCookies + flush right
                    // there — so by the time any ON_PAUSE arrives,
                    // the jar is already cleared AND that cleared
                    // state is already persisted. This flush can only
                    // persist the jar as it currently is; it cannot
                    // resurrect cookies the wipe already removed.
                    try {
                        android.webkit.CookieManager.getInstance().flush()
                    } catch (t: Throwable) {
                        // Cookie persistence is best-effort only.
                    }
                    tabs.forEach { it.pauseEngine() }
                }
                Lifecycle.Event.ON_RESUME ->
                    tabs.forEach { it.resumeEngine(it.id == activeTabId) }
                else -> Unit
            }
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose { activity?.lifecycle?.removeObserver(observer) }
    }

    // Gecko active-tab rule (v0.35.0): exactly one created Gecko
    // session is active — the visible tab's; every other created
    // session is inactive. Keyed on activeTabId and run after
    // composition, when a newly selected tab's session has been
    // created by TabContent, so first-switch tabs are covered too.
    // (Before this, tab switches never touched setActive at all.)
    LaunchedEffect(activeTabId) {
        // Stamp the newly active tab as most recently used BEFORE
        // the sleep pass reads the stamps (v0.41.0).
        activeTab()?.noteUsed()
        tabs.forEach { it.applyEngineActiveState(it.id == activeTabId) }
        // Engine sleep pass (v0.41.0): keyed on activeTabId, so it
        // runs after every tab creation and every tab switch — by
        // the time this effect runs, the newly active tab's engine
        // has been created by TabContent during composition, so the
        // alive count it sees is the real one.
        enforceEngineSleepLimit()
    }

    webPermissionHandler = { request ->
        val resources = request.resources?.toList() ?: emptyList()
        val needed = buildList {
            if (android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE in resources) {
                add(android.Manifest.permission.CAMERA)
            }
            if (android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE in resources) {
                add(android.Manifest.permission.RECORD_AUDIO)
            }
        }
        requestSysPermissions(needed.toTypedArray()) { granted ->
            val grantedResources = resources.filter { res ->
                when (res) {
                    android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                        android.Manifest.permission.CAMERA in granted
                    android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                        android.Manifest.permission.RECORD_AUDIO in granted
                    else -> false
                }
            }
            when {
                grantedResources.isEmpty() -> {
                    request.deny()
                    android.widget.Toast.makeText(
                        context, "没有拿到摄像头或麦克风权限，网页的这个功能用不了",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                grantedResources.size < resources.size -> {
                    request.grant(grantedResources.toTypedArray())
                    android.widget.Toast.makeText(
                        context, "只拿到了部分权限，网页的这个功能可能用不全",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
                else -> request.grant(grantedResources.toTypedArray())
            }
        }
    }
    geckoAndroidPermHandler = { perms, callback ->
        requestSysPermissions(perms) { granted ->
            if (perms.all { it in granted }) {
                callback.grant()
            } else {
                callback.reject()
            }
        }
    }
    geckoContentPermHandler = { perm ->
        when (perm.permission) {
            GeckoSession.PermissionDelegate.PERMISSION_GEOLOCATION ->
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context, android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
                } else {
                    GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
                }
            GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_INAUDIBLE,
            GeckoSession.PermissionDelegate.PERMISSION_AUTOPLAY_AUDIBLE,
            GeckoSession.PermissionDelegate.PERMISSION_MEDIA_KEY_SYSTEM_ACCESS ->
                GeckoSession.PermissionDelegate.ContentPermission.VALUE_ALLOW
            else -> GeckoSession.PermissionDelegate.ContentPermission.VALUE_DENY
        }
    }
    geckoMediaPermHandler = { _, video, audio, callback ->
        val needed = buildList {
            if (!video.isNullOrEmpty()) add(android.Manifest.permission.CAMERA)
            if (!audio.isNullOrEmpty()) add(android.Manifest.permission.RECORD_AUDIO)
        }
        if (needed.isEmpty()) {
            callback.reject()
        } else {
            requestSysPermissions(needed.toTypedArray()) { granted ->
                val videoSource = video?.firstOrNull()
                    ?.takeIf { android.Manifest.permission.CAMERA in granted }
                val audioSource = audio?.firstOrNull()
                    ?.takeIf { android.Manifest.permission.RECORD_AUDIO in granted }
                if (videoSource == null && audioSource == null) {
                    callback.reject()
                    android.widget.Toast.makeText(
                        context, "没有拿到摄像头或麦克风权限，网页的这个功能用不了",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } else {
                    callback.grant(videoSource, audioSource)
                }
            }
        }
    }

    fun exportBookmarks() {
        if (bookmarks.isEmpty()) {
            android.widget.Toast.makeText(
                context, "还没有书签可以导出", android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val json = bookmarksToJson(bookmarks)
        val fileName = "tuxunbrowse-bookmarks.json"
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(
                        android.provider.MediaStore.Downloads.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                }
                val dest = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                ) ?: throw IllegalStateException("insert failed")
                resolver.openOutputStream(dest)?.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                }
            } else {
                val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                java.io.File(dir, fileName)
                    .writeText(json, Charsets.UTF_8)
            }
            android.widget.Toast.makeText(
                context, "书签已导出到下载文件夹：$fileName",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "导出失败，请重试", android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    fun openUrl(url: String) {
        // The one inbound gate (v0.31.0): every "open this as a page"
        // request that arrives from outside a web page funnels through
        // here — links handed over by other apps, the clipboard hint,
        // the update check's release page, bookmarks, history, the
        // reading list, speed dials, address-bar suggestions. Only
        // http/https and the app's own home page may pass; file:,
        // javascript:, data:, content:, intent: and friends are
        // refused with a plain-language message. In-page navigation
        // never comes through here (the engines handle it), so pages
        // linking among themselves — and subresources like data:
        // images inside a page — are unaffected.
        if (!isAllowedPageUrl(url)) {
            android.widget.Toast.makeText(
                context,
                "这个链接打不开：只支持打开 http 或 https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        var tab = activeTab() ?: newTab(store.mainEngine, false, url)
        if (tab.engine == Engine.GECKO &&
            effectiveEngine(Engine.GECKO, url) == Engine.WEBVIEW
        ) {
            tab = replaceTabEngine(tab, Engine.WEBVIEW)
        }
        if (tab.url != url) tab.load(url)
        screen = Screen.BROWSER
    }

    // A link opened from another app: show it once the session is ready.
    LaunchedEffect(initialUrl, startupDone) {
        if (startupDone && initialUrl != null) {
            openUrl(initialUrl)
            onInitialUrlConsumed()
        }
    }

    BackHandler(enabled = fullscreenView != null) {
        activeTab()?.exitFullscreen()
        fullscreenView = null
    }

    // Page transitions follow Material 3 motion: a short fade
    // everywhere, plus a slight horizontal slide between the
    // Compose-only sub-pages. Going to or from the browser itself is
    // fade-only — sliding a native web view (above all Gecko's
    // surface) would look broken. Durations stay in the 180–250 ms
    // range with the standard FastOutSlowIn easing.
    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            if (initialState == Screen.BROWSER || targetState == Screen.BROWSER) {
                fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(tween(180, easing = FastOutSlowInEasing))
            } else {
                (fadeIn(tween(250, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        tween(250, easing = FastOutSlowInEasing)
                    ) { it / 10 }) togetherWith
                    fadeOut(tween(200, easing = FastOutSlowInEasing))
            }
        },
        label = "screenTransition"
    ) { targetScreen ->
        when (targetScreen) {
        Screen.PICKER -> EnginePickerScreen(
            title = "选择浏览器内核",
            subtitle = "TuxunBrowse 有两个内核，打开网页的方式不同。这个选择以后随时能在设置里改。",
            initial = store.mainEngine,
            confirmLabel = "开始浏览",
            onConfirm = { engine ->
                store.mainEngine = engine
                if (tabs.isEmpty()) newTab(engine, false)
                screen = Screen.BROWSER
            }
        )

        Screen.BROWSER -> {
            val current = activeTab()
            BrowserScreen(
                tabs = tabs,
                activeTab = current,
                isBookmarked = current?.let { t -> bookmarks.any { it.url == t.url } } ?: false,
                currentHostIsCompat = current?.let {
                    isCompatHost(hostOf(it.url), compatSites.toSet())
                } ?: false,
                onSelectTab = { target ->
                    val outgoing = activeTab()
                    if (outgoing != null && outgoing.id != target.id) {
                        outgoing.captureThumbnailDeferred()
                    }
                    // Selecting a tab counts as using its thumbnail
                    // for the app-wide LRU cap (v0.23.0).
                    target.noteThumbnailUsed()
                    activeTabId = target.id
                },
                onTabsOpened = { activeTab()?.captureThumbnailDeferred() },
                onCloseTab = { closeTab(it) },
                onCloseAllTabs = { closeAllTabs() },
                onCloseOtherTabs = { closeOtherTabs() },
                recentClosed = recentClosedTabs,
                onReopenClosed = { reopenClosedTab(it) },
                onNewTab = { newTab(store.mainEngine, false) },
                onNewIncognitoTab = { engine -> newTab(engine, true) },
                incognitoEngine = store.incognitoEngine,
                onIncognitoEngineChosen = { store.incognitoEngine = it },
                onToggleBookmark = {
                    val tab = activeTab() ?: return@BrowserScreen
                    val existing = bookmarks.firstOrNull { it.url == tab.url }
                    if (existing != null) {
                        bookmarks.remove(existing)
                    } else if (tab.url.startsWith("http")) {
                        bookmarks.add(0, Bookmark(tab.title.ifBlank { tab.url }, tab.url))
                    }
                    store.saveBookmarks(bookmarks)
                },
                onNavigate = { text ->
                    // Typed input with a non-web scheme (file:,
                    // javascript:, data:, ...) is refused outright
                    // (v0.31.0) instead of being reinterpreted as a
                    // search or a host name.
                    if (hasBlockedPageScheme(text)) {
                        android.widget.Toast.makeText(
                            context,
                            "这个链接打不开：只支持打开 http 或 https 开头的网址",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        activeTab()?.load(
                            resolveInput(text, store.searchEngine, store.customSearchTemplate)
                        )
                    }
                },
                bookmarks = bookmarks,
                searchEngine = searchEngineState,
                customSearchName = store.customSearchName,
                onOpenSuggestion = { url -> openUrl(url) },
                onGoHome = { activeTab()?.load(HOME_URL) },
                onReloadPage = { activeTab()?.reload() },
                onSwitchEngine = { engine, rememberSite ->
                    val tab = activeTab() ?: return@BrowserScreen
                    switchTabEngine(tab, engine, rememberSite)
                },
                onToggleSiteCompat = { toggleSiteCompat() },
                onClearSiteData = { host -> clearSiteData(host) },
                activeDesktopMode = current?.desktopMode ?: false,
                onToggleDesktopMode = { enabled ->
                    val tab = activeTab() ?: return@BrowserScreen
                    recreateTab(tab, tab.engine, enabled)
                },
                onFind = { query, forward -> activeTab()?.findInPage(query, forward) },
                onClearFind = { activeTab()?.clearFind() },
                fullscreenView = fullscreenView,
                chromeHidden = geckoFullscreen,
                onExitFullscreen = {
                    activeTab()?.exitFullscreen()
                    fullscreenView = null
                },
                onToggleReaderMode = {
                    val tab = activeTab() ?: return@BrowserScreen
                    when {
                        tab.isHome -> android.widget.Toast.makeText(
                            context, "先打开一个网页再用阅读模式",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        tab.engine == Engine.GECKO -> android.widget.Toast.makeText(
                            context,
                            "阅读模式暂时只支持系统 WebView 标签，火狐标签可以先切换内核再开",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        tab.readerActive -> tab.exitReaderMode()
                        else -> tab.enterReaderMode()
                    }
                },
                onPrintPage = {
                    val tab = activeTab() ?: return@BrowserScreen
                    when {
                        tab.engine == Engine.GECKO -> android.widget.Toast.makeText(
                            context,
                            "火狐标签暂时不能打印，可以先切换到系统 WebView 再打印或存为 PDF",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        tab.isHome -> android.widget.Toast.makeText(
                            context, "先打开一个网页再打印",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        !tab.printPage(context) -> android.widget.Toast.makeText(
                            context, "打印没能打开，请重试",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onOpenSniffer = {
                    val tab = activeTab()
                    when {
                        tab == null || tab.isHome -> android.widget.Toast.makeText(
                            context, "先打开一个网页再嗅探",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        tab.engine == Engine.GECKO -> android.widget.Toast.makeText(
                            context,
                            "火狐标签暂不支持嗅探，请切换到系统 WebView 打开本页再试",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        else -> {
                            sniffTabId = tab.id
                            screen = Screen.SNIFF
                        }
                    }
                },
                onScreenshot = {
                    val tab = activeTab()
                    when {
                        tab == null || tab.isHome -> android.widget.Toast.makeText(
                            context, "先打开一个网页再截图",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        tab.engine == Engine.GECKO -> android.widget.Toast.makeText(
                            context,
                            "火狐标签暂不支持网页截图，请切换到系统 WebView 打开本页再试",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        else -> screenshotTab = tab
                    }
                },
                onOpenDownloads = { screen = Screen.DOWNLOADS },
                onOpenBookmarks = { screen = Screen.BOOKMARKS },
                onOpenHistory = { screen = Screen.HISTORY },
                onOpenSettings = { screen = Screen.SETTINGS },
                adBlockEnabled = store.adBlockEnabled,
                onAddToReadingList = {
                    val tab = activeTab() ?: return@BrowserScreen
                    when {
                        tab.isHome || !tab.url.startsWith("http") ->
                            android.widget.Toast.makeText(
                                context, "先打开一个网页再加入阅读列表",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        readingList.any { it.url == tab.url } ->
                            android.widget.Toast.makeText(
                                context, "这篇文章已经在阅读列表里了",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        else -> {
                            readingList.add(
                                0,
                                ReadingItem(
                                    tab.title.ifBlank { tab.url },
                                    tab.url,
                                    System.currentTimeMillis(),
                                    false
                                )
                            )
                            // Cap the list at 200 entries; the oldest
                            // ones fall off the end.
                            while (readingList.size > 200) {
                                readingList.removeAt(readingList.lastIndex)
                            }
                            store.saveReadingList(readingList)
                            android.widget.Toast.makeText(
                                context, "已加入阅读列表",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onOpenReadingList = { screen = Screen.READING_LIST },
                homeContent = {
                    HomeContent(
                        bookmarks = bookmarks,
                        speedDials = speedDials,
                        onOpenSpeedDial = { openUrl(it.url) },
                        onAddSpeedDial = {
                            speedDials.add(it)
                            store.saveSpeedDials(speedDials)
                        },
                        onEditSpeedDial = { old, newItem ->
                            val idx = speedDials.indexOf(old)
                            if (idx >= 0) {
                                speedDials[idx] = newItem
                                store.saveSpeedDials(speedDials)
                            }
                        },
                        onDeleteSpeedDial = {
                            speedDials.remove(it)
                            store.saveSpeedDials(speedDials)
                        },
                        searchEngine = searchEngineState,
                        customSearchName = store.customSearchName,
                        customSearchTemplate = store.customSearchTemplate,
                        onSearchEngineSelected = { engine ->
                            store.searchEngine = engine
                            searchEngineState = engine
                        },
                        onSaveCustomSearch = { name, template ->
                            store.customSearchName = name
                            store.customSearchTemplate = template
                            store.searchEngine = SearchEngine.CUSTOM
                            searchEngineState = SearchEngine.CUSTOM
                        },
                        onSearchUrl = { url -> openUrl(url) },
                        onOpenBookmark = { openUrl(it.url) },
                        onDeleteBookmark = {
                            bookmarks.remove(it)
                            store.saveBookmarks(bookmarks)
                        },
                        onTogglePinBookmark = { bm ->
                            val idx = bookmarks.indexOf(bm)
                            if (idx >= 0) {
                                bookmarks[idx] = bm.copy(pinned = !bm.pinned)
                                store.saveBookmarks(bookmarks)
                            }
                        },
                        onOpenAllBookmarks = { screen = Screen.BOOKMARKS }
                    )
                }
            )
        }

        Screen.BOOKMARKS -> BookmarksScreen(
            bookmarks = bookmarks,
            onBack = { screen = Screen.BROWSER },
            onOpen = { openUrl(it.url) },
            onDelete = {
                bookmarks.remove(it)
                store.saveBookmarks(bookmarks)
            },
            onEdit = { old, newTitle, newUrl ->
                val idx = bookmarks.indexOf(old)
                if (idx >= 0) {
                    bookmarks[idx] = Bookmark(newTitle, newUrl)
                    store.saveBookmarks(bookmarks)
                }
            },
            onExport = { exportBookmarks() },
            onImport = {
                importBookmarksLauncher.launch(
                    arrayOf("application/json", "text/plain", "text/html")
                )
            },
            onDeleteMany = { selected ->
                bookmarks.removeAll(selected)
                store.saveBookmarks(bookmarks)
            }
        )

        Screen.HISTORY -> HistoryScreen(
            history = history,
            onBack = { screen = Screen.BROWSER },
            onOpen = { openUrl(it.url) },
            onDelete = {
                history.remove(it)
                store.saveHistory(history)
            },
            onClear = {
                history.clear()
                store.clearHistory()
            }
        )

        Screen.SETTINGS -> SettingsScreen(
            store = store,
            refreshTick = settingsRefreshTick,
            onOpenReleaseUrl = { url -> openUrl(url) },
            onBack = { screen = Screen.BROWSER },
            onClearHistory = {
                history.clear()
                store.clearHistory()
            },
            onRemoveCompatSite = { removeCompatSite(it) },
            onOpenClearData = { screen = Screen.CLEAR_DATA },
            onExportBackup = { exportBackup() },
            onImportBackup = {
                importBackupLauncher.launch(arrayOf("application/json", "text/plain"))
            },
            onThemeModeChange = { mode -> onThemeModeChanged(mode) },
            onSearchEngineChange = { searchEngineState = store.searchEngine },
            onTextZoomChange = { percent ->
                tabs.forEach { it.applyTextZoom(percent) }
                GeckoHolder.applyFontSizeFactor(percent / 100f)
            },
            onWebDarkChange = { enabled ->
                tabs.forEach { it.applyWebDark(enabled) }
            },
            onThirdPartyCookiesChange = { enabled ->
                tabs.forEach { it.applyThirdPartyCookies(enabled) }
            }
        )

        Screen.CLEAR_DATA -> ClearDataScreen(
            geckoStarted = GeckoHolder.hasRuntime,
            onBack = { screen = Screen.SETTINGS },
            onClear = { clearHistorySel, clearWebView, clearGecko ->
                if (clearHistorySel) {
                    history.clear()
                    store.clearHistory()
                }
                if (clearWebView) {
                    android.webkit.CookieManager.getInstance().removeAllCookies(null)
                    android.webkit.CookieManager.getInstance().flush()
                    android.webkit.WebStorage.getInstance().deleteAllData()
                }
                if (clearGecko) {
                    GeckoHolder.clearSiteData()
                }
            },
            onClearSite = { host -> clearSiteData(host) }
        )

        Screen.DOWNLOADS -> DownloadsScreen(
            onBack = { screen = Screen.BROWSER },
            retryReferer = activeTab()?.url
        )

        Screen.SNIFF -> SniffScreen(
            tab = tabs.firstOrNull { it.id == sniffTabId },
            onBack = { screen = Screen.BROWSER },
            onDownload = { res ->
                downloadWithMode(
                    res.url, res.name,
                    fromIncognito =
                        tabs.firstOrNull { t -> t.id == sniffTabId }?.incognito == true
                ) {
                    startSystemDownload(
                        context, res.url, null, null, null,
                        referer = tabs.firstOrNull { t -> t.id == sniffTabId }?.url
                    )
                }
            },
            onDownloadAll = { list ->
                downloadBatchWithMode(
                    list.map { it.url },
                    fromIncognito =
                        tabs.firstOrNull { t -> t.id == sniffTabId }?.incognito == true
                ) {
                    list.forEach {
                        startSystemDownload(
                            context, it.url, null, null, null,
                            referer = tabs.firstOrNull { t -> t.id == sniffTabId }?.url
                        )
                    }
                }
            }
        )

        Screen.READING_LIST -> ReadingListScreen(
            items = readingList,
            onBack = { screen = Screen.BROWSER },
            onOpen = { item ->
                // Opening an entry marks it read, then follows the
                // usual openUrl path (current tab, default engine
                // rules and compat memory included).
                val idx = readingList.indexOf(item)
                if (idx >= 0 && !readingList[idx].read) {
                    readingList[idx] = readingList[idx].copy(read = true)
                    store.saveReadingList(readingList)
                }
                openUrl(item.url)
            },
            onDelete = {
                readingList.remove(it)
                store.saveReadingList(readingList)
            },
            onClearRead = {
                readingList.removeAll { it.read }
                store.saveReadingList(readingList)
            }
        )
        }
    }

    val ask = askDownload
    if (ask != null) {
        AlertDialog(
            onDismissRequest = { askDownload = null },
            title = { Text("用哪种方式下载？") },
            text = {
                Text(
                    ask.message + "\n\n系统下载用手机自带的下载器；" +
                        "TuxunDown 是多线程下载器，下大文件更快，需要另外安装这个 App，" +
                        "没装会自动改用系统下载。"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    askDownload = null
                    ask.onTuxunDown()
                }) { Text("TuxunDown 下载") }
            },
            dismissButton = {
                TextButton(onClick = {
                    askDownload = null
                    ask.onSystem()
                }) { Text("系统下载") }
            }
        )
    }

    // Long-press context menu (v0.42.0): ONE combined dialog for
    // link and/or image, fed by both engines. The address is shown
    // (capped at 3 lines with an ellipsis, so a huge URL cannot blow
    // up the layout) so the user sees exactly what would be opened.
    // "Open in new tab" reuses the source tab's engine + incognito
    // flag through the existing newTab path. Non-http(s) targets are
    // never opened (v0.31.0 / v0.38.0 rule): they only get copy,
    // with an honest note.
    val ctxMenu = pageContextMenu
    if (ctxMenu != null) {
        val link = ctxMenu.linkUrl
        val image = ctxMenu.imageUrl
        val linkOpenable = link != null &&
            (link.startsWith("http://") || link.startsWith("https://"))
        val imageOpenable = image != null &&
            (image.startsWith("http://") || image.startsWith("https://"))
        AlertDialog(
            onDismissRequest = { pageContextMenu = null },
            title = {
                Text(
                    when {
                        link != null && image != null -> "链接与图片"
                        link != null -> "链接"
                        else -> "图片"
                    }
                )
            },
            text = {
                LazyColumn {
                    if (link != null) {
                        item {
                            Text(
                                link,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        if (linkOpenable) {
                            item {
                                ListItem(
                                    headlineContent = {
                                        Text("在新标签页打开")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pageContextMenu = null
                                            newTab(
                                                ctxMenu.engine,
                                                ctxMenu.incognito,
                                                link
                                            )
                                            screen = Screen.BROWSER
                                        }
                                )
                            }
                        } else {
                            item {
                                Text(
                                    "这个链接不是普通网页地址，不能在新标签页打开，只能复制。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        item {
                            ListItem(
                                headlineContent = { Text("复制链接") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val ok = copyTextToClipboard(
                                            context, "link", link
                                        )
                                        android.widget.Toast.makeText(
                                            context,
                                            if (ok) "链接已复制"
                                            else "复制失败，请重试",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        pageContextMenu = null
                                    }
                            )
                        }
                        if (linkOpenable) {
                            item {
                                ListItem(
                                    headlineContent = { Text("分享链接") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pageContextMenu = null
                                            try {
                                                val intent =
                                                    android.content.Intent(
                                                        android.content.Intent
                                                            .ACTION_SEND
                                                    )
                                                        .setType("text/plain")
                                                        .putExtra(
                                                            android.content.Intent
                                                                .EXTRA_TEXT,
                                                            link
                                                        )
                                                context.startActivity(
                                                    android.content.Intent
                                                        .createChooser(
                                                            intent, "分享链接"
                                                        )
                                                )
                                            } catch (t: Throwable) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "没有能分享链接的应用",
                                                    android.widget.Toast
                                                        .LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                )
                            }
                        }
                    }
                    if (link != null && image != null) {
                        item { HorizontalDivider() }
                    }
                    if (image != null) {
                        if (image != link) {
                            item {
                                Text(
                                    image,
                                    style = MaterialTheme.typography
                                        .bodyMedium,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                )
                            }
                        }
                        if (imageOpenable) {
                            item {
                                ListItem(
                                    headlineContent = {
                                        Text("在新标签页打开图片")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pageContextMenu = null
                                            newTab(
                                                ctxMenu.engine,
                                                ctxMenu.incognito,
                                                image
                                            )
                                            screen = Screen.BROWSER
                                        }
                                )
                            }
                            item {
                                ListItem(
                                    headlineContent = { Text("保存图片") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            pageContextMenu = null
                                            // Same incognito honesty as
                                            // the download dispatch: this
                                            // calls the system downloader
                                            // directly.
                                            if (ctxMenu.incognito) {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    "无痕提醒：下载记录会留在系统下载管理里，关掉无痕标签也不会自动删掉",
                                                    android.widget.Toast
                                                        .LENGTH_LONG
                                                ).show()
                                            }
                                            startSystemDownload(
                                                context, image, null, null,
                                                null,
                                                referer = activeTab()?.url
                                            )
                                        }
                                )
                            }
                        } else {
                            item {
                                Text(
                                    "这张图片是网页内嵌的数据，不能打开或保存，只能复制地址。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme
                                        .onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        item {
                            ListItem(
                                headlineContent = { Text("复制图片地址") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val ok = copyTextToClipboard(
                                            context, "image", image
                                        )
                                        android.widget.Toast.makeText(
                                            context,
                                            if (ok) "图片地址已复制"
                                            else "复制失败，请重试",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        pageContextMenu = null
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pageContextMenu = null }) {
                    Text("取消")
                }
            }
        )
    }

    // JavaScript dialog from a Gecko page (alert / confirm / prompt),
    // shown with a real Material 3 AlertDialog. Every exit path answers
    // the page exactly once: alert acknowledges, confirm reports the
    // button, prompt hands back the typed text or cancels.
    val jsPrompt = jsPromptRequest
    if (jsPrompt != null) {
        var textInput by remember(jsPrompt) { mutableStateOf(jsPrompt.defaultValue) }
        fun answerJsPrompt(confirmed: Boolean, text: String?) {
            jsPromptRequest = null
            jsPrompt.respond(confirmed, text)
        }
        // A text prompt opens with its input already focused and the
        // keyboard up, ready to type into.
        val promptFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            if (jsPrompt.kind == GeckoJsPromptKind.TEXT) {
                promptFocusRequester.requestFocus()
            }
        }
        AlertDialog(
            onDismissRequest = {
                answerJsPrompt(jsPrompt.kind == GeckoJsPromptKind.ALERT, null)
            },
            title = { Text("网页提示") },
            text = {
                LazyColumn {
                    item { Text(jsPrompt.message) }
                    if (jsPrompt.kind == GeckoJsPromptKind.TEXT) {
                        item {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .focusRequester(promptFocusRequester),
                                singleLine = true
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (jsPrompt.kind == GeckoJsPromptKind.TEXT) {
                        answerJsPrompt(true, textInput)
                    } else {
                        answerJsPrompt(true, null)
                    }
                }) {
                    Text(if (jsPrompt.kind == GeckoJsPromptKind.ALERT) "好" else "确定")
                }
            },
            dismissButton = {
                if (jsPrompt.kind != GeckoJsPromptKind.ALERT) {
                    TextButton(onClick = { answerJsPrompt(false, null) }) {
                        Text("取消")
                    }
                }
            }
        )
    }

    // HTTP Basic-auth dialog (both engines share this one). The user
    // name and password exist only in this dialog's state and the
    // answer callback — nothing is saved to disk, history or logs.
    val authPrompt = authPromptRequest
    if (authPrompt != null) {
        val info = authPrompt.info
        var authUser by remember(authPrompt) {
            mutableStateOf(info.prefillUsername)
        }
        var authPass by remember(authPrompt) { mutableStateOf("") }
        fun answerAuth(user: String?, pass: String?) {
            authPromptRequest = null
            authPrompt.respond(user, pass)
        }
        // The dialog opens focused on its first input (the user name,
        // or the password when the site only asks for one) with the
        // keyboard up; the password can be peeked at with the eye
        // button instead of typing blind.
        var authPassVisible by remember(authPrompt) { mutableStateOf(false) }
        val authFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { authFocusRequester.requestFocus() }
        AlertDialog(
            onDismissRequest = { answerAuth(null, null) },
            title = { Text("这个网站需要登录") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            when {
                                info.message.isNotBlank() -> info.message
                                info.realm.isNotBlank() ->
                                    "网站 ${info.host}（${info.realm}）" +
                                        "需要账号密码才能打开。"
                                else ->
                                    "网站 ${info.host} 需要账号密码才能打开。"
                            }
                        )
                    }
                    if (!info.onlyPassword) {
                        item {
                            OutlinedTextField(
                                value = authUser,
                                onValueChange = { authUser = it },
                                label = { Text("用户名") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .focusRequester(authFocusRequester),
                                singleLine = true
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = authPass,
                            onValueChange = { authPass = it },
                            label = { Text("密码") },
                            modifier = if (info.onlyPassword) {
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .focusRequester(authFocusRequester)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            },
                            singleLine = true,
                            visualTransformation = if (authPassVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            trailingIcon = {
                                IconButton(
                                    onClick = { authPassVisible = !authPassVisible }
                                ) {
                                    Icon(
                                        if (authPassVisible) {
                                            Icons.Filled.VisibilityOff
                                        } else {
                                            Icons.Filled.Visibility
                                        },
                                        contentDescription = if (authPassVisible) {
                                            "隐藏密码"
                                        } else {
                                            "显示密码"
                                        }
                                    )
                                }
                            }
                        )
                    }
                    item {
                        Text(
                            "账号密码只会发给这个网站，本应用不会保存。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    answerAuth(
                        if (info.onlyPassword) info.prefillUsername else authUser,
                        authPass
                    )
                }) {
                    Text("登录")
                }
            },
            dismissButton = {
                TextButton(onClick = { answerAuth(null, null) }) {
                    Text("取消")
                }
            }
        )
    }

    // SSL certificate error dialog (WebView tabs). "返回" is the
    // primary, safe choice; proceeding applies to this one load only
    // and is never remembered.
    val sslPrompt = sslPromptRequest
    if (sslPrompt != null) {
        fun answerSsl(proceed: Boolean) {
            sslPromptRequest = null
            sslPrompt.respond(proceed)
        }
        AlertDialog(
            onDismissRequest = { answerSsl(false) },
            title = { Text("这个网站的证书有问题") },
            text = {
                LazyColumn {
                    item { Text(sslPrompt.description + "。") }
                    item {
                        Text(
                            "网址：${sslPrompt.url}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item {
                        Text(
                            "证书有问题的网站可能不安全，输在里面的账号密码" +
                                "有被别人看走的风险。就算这次选了继续，" +
                                "也只对这一次打开有效，下次还会再问。",
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { answerSsl(false) }) { Text("返回") }
            },
            dismissButton = {
                TextButton(onClick = { answerSsl(true) }) { Text("仍然继续") }
            }
        )
    }

    // Page screenshot (WebView tabs only — Gecko tabs are told
    // honestly at the menu entry that it is not supported there).
    // Two kinds: the visible screen, or a full-page long capture
    // capped at MAX_FULL_PAGE_HEIGHT_PX — the dialog says what the
    // cap means, and a truncated capture is reported after saving.
    val shotTab = screenshotTab
    if (shotTab != null) {
        var fullPage by remember(shotTab) { mutableStateOf(false) }
        fun shotToast(message: String) {
            android.widget.Toast.makeText(
                context, message, android.widget.Toast.LENGTH_LONG
            ).show()
        }
        fun runShot(share: Boolean) {
            screenshotTab = null
            if (!fullPage) {
                val bmp = shotTab.captureVisibleBitmap()
                when {
                    bmp == null -> shotToast("截图失败，请重试")
                    share -> sharePageScreenshot(context, bmp)
                    else -> savePageScreenshot(context, bmp) { where ->
                        shotToast(
                            if (where != null) {
                                "截图已保存到$where"
                            } else {
                                "保存失败，请重试"
                            }
                        )
                    }
                }
                return
            }
            val result = shotTab.captureFullPageBitmap()
            val bmp = result.bitmap
            when {
                result.outOfMemory -> shotToast(
                    "这个页面太长太大，手机内存不够截成长图，可以改用当前屏幕截图"
                )
                bmp == null -> shotToast("截图失败，请重试")
                share -> {
                    sharePageScreenshot(context, bmp)
                    if (result.truncated) {
                        shotToast("页面太长，只截了前半部分")
                    }
                }
                else -> savePageScreenshot(context, bmp) { where ->
                    shotToast(
                        when {
                            where == null -> "保存失败，请重试"
                            result.truncated ->
                                "页面太长，只截了前半部分，已保存到$where"
                            else -> "长截图已保存到$where"
                        }
                    )
                }
            }
        }
        AlertDialog(
            onDismissRequest = { screenshotTab = null },
            title = { Text("网页截图") },
            text = {
                LazyColumn {
                    item {
                        Text("选一种截图方式，截好后可以存到相册，也可以直接分享。")
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("当前屏幕截图") },
                            supportingContent = {
                                Text("只截现在屏幕里能看到的这一屏")
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = !fullPage,
                                    onClick = { fullPage = false }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { fullPage = false }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("整页长截图") },
                            supportingContent = {
                                Text(
                                    "把整个网页从上到下截成一张长图，最长 " +
                                        "$MAX_FULL_PAGE_HEIGHT_PX 像素；" +
                                        "页面更长时只截前半部分，截完会提醒你"
                                )
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = fullPage,
                                    onClick = { fullPage = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { fullPage = true }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { runShot(share = false) }) { Text("保存到相册") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { runShot(share = true) }) { Text("分享截图") }
                    TextButton(onClick = { screenshotTab = null }) { Text("取消") }
                }
            }
        )
    }
}

/**
 * The app's own home page: a native Material 3 screen with a big search
 * box, a bookmark shortcut grid and recent history. It never loads an
 * external start page.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    bookmarks: List<Bookmark>,
    speedDials: List<SpeedDial> = emptyList(),
    onOpenSpeedDial: (SpeedDial) -> Unit = {},
    onAddSpeedDial: (SpeedDial) -> Unit = {},
    onEditSpeedDial: (SpeedDial, SpeedDial) -> Unit = { _, _ -> },
    onDeleteSpeedDial: (SpeedDial) -> Unit = {},
    searchEngine: SearchEngine,
    customSearchName: String = "",
    customSearchTemplate: String = "",
    onSearchEngineSelected: (SearchEngine) -> Unit = {},
    onSaveCustomSearch: (String, String) -> Unit = { _, _ -> },
    onSearchUrl: (String) -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onTogglePinBookmark: (Bookmark) -> Unit,
    onOpenAllBookmarks: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Bookmark?>(null) }
    var actionTarget by remember { mutableStateOf<Bookmark?>(null) }
    // Speed-dial editor/menu state (add when dialEditing == null).
    val homeContext = LocalContext.current
    var dialActionTarget by remember { mutableStateOf<SpeedDial?>(null) }
    var dialPendingDelete by remember { mutableStateOf<SpeedDial?>(null) }
    var dialDialogOpen by remember { mutableStateOf(false) }
    // Home-page search-engine quick switch: same data source as the
    // settings page (the store, via the callbacks above); this local
    // mirror is what the home search box reads, so a pick here takes
    // effect on the home page at once.
    var currentEngine by remember { mutableStateOf(searchEngine) }
    var currentCustomName by remember { mutableStateOf(customSearchName) }
    var currentCustomTemplate by remember { mutableStateOf(customSearchTemplate) }
    var showEnginePicker by remember { mutableStateOf(false) }
    var showHomeCustomDialog by remember { mutableStateOf(false) }
    var homeCustomName by remember { mutableStateOf("") }
    var homeCustomTemplate by remember { mutableStateOf("") }
    var homeCustomError by remember { mutableStateOf<String?>(null) }
    fun engineShortName(): String =
        if (currentEngine == SearchEngine.CUSTOM) {
            currentCustomName.ifBlank { "自定义" }
        } else {
            currentEngine.label
        }
    var dialEditing by remember { mutableStateOf<SpeedDial?>(null) }
    var dialName by remember { mutableStateOf("") }
    var dialUrl by remember { mutableStateOf("") }
    fun openDialDialog(editing: SpeedDial?) {
        if (editing == null && speedDials.size >= 12) {
            android.widget.Toast.makeText(
                homeContext, "快捷方式最多 12 个，先删掉一个再加",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        dialEditing = editing
        dialName = editing?.title ?: ""
        dialUrl = editing?.url ?: ""
        dialDialogOpen = true
    }
    val target = actionTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { actionTarget = null },
            title = { Text(target.title, maxLines = 2) },
            text = {
                LazyColumn {
                    item {
                        ListItem(
                            headlineContent = {
                                Text(if (target.pinned) "取消置顶" else "置顶到最前面")
                            },
                            leadingContent = {
                                Icon(Icons.Filled.PushPin, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTogglePinBookmark(target)
                                    actionTarget = null
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("删除书签") },
                            leadingContent = {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    actionTarget = null
                                    pendingDelete = target
                                }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { actionTarget = null }) { Text("取消") }
            }
        )
    }
    val toDelete = pendingDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这个书签？") },
            text = { Text("会把「${toDelete.title}」从书签里删掉，首页的快捷入口也会一起消失。") },
            confirmButton = {
                Button(onClick = {
                    onDeleteBookmark(toDelete)
                    pendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            }
        )
    }
    val dialTarget = dialActionTarget
    if (dialTarget != null) {
        AlertDialog(
            onDismissRequest = { dialActionTarget = null },
            title = { Text(dialTarget.title, maxLines = 2) },
            text = {
                LazyColumn {
                    item {
                        ListItem(
                            headlineContent = { Text("编辑") },
                            leadingContent = {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dialActionTarget = null
                                    openDialDialog(dialTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("删除") },
                            leadingContent = {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    dialActionTarget = null
                                    dialPendingDelete = dialTarget
                                }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { dialActionTarget = null }) { Text("取消") }
            }
        )
    }
    val dialToDelete = dialPendingDelete
    if (dialToDelete != null) {
        AlertDialog(
            onDismissRequest = { dialPendingDelete = null },
            title = { Text("删除这个快捷方式？") },
            text = { Text("会把「${dialToDelete.title}」从首页快捷方式里删掉。") },
            confirmButton = {
                Button(onClick = {
                    onDeleteSpeedDial(dialToDelete)
                    dialPendingDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { dialPendingDelete = null }) { Text("取消") }
            }
        )
    }
    if (dialDialogOpen) {
        // Opens focused on the first field with the keyboard up.
        val dialFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { dialFocusRequester.requestFocus() }
        AlertDialog(
            onDismissRequest = { dialDialogOpen = false },
            title = {
                Text(if (dialEditing == null) "添加快捷方式" else "编辑快捷方式")
            },
            text = {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = dialName,
                            onValueChange = { dialName = it },
                            label = { Text("名称") },
                            placeholder = { Text("可以不填，会用网址的域名") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(dialFocusRequester),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = dialUrl,
                            onValueChange = { dialUrl = it },
                            label = { Text("网址") },
                            placeholder = { Text("比如 example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (dialUrl.isBlank()) {
                        android.widget.Toast.makeText(
                            homeContext, "先填网址",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else if (hasBlockedPageScheme(dialUrl)) {
                        // Non-web schemes are never saved as speed
                        // dials (v0.31.0, same rule as openUrl).
                        android.widget.Toast.makeText(
                            homeContext,
                            "这个链接打不开：只支持打开 http 或 https 开头的网址",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val resolved = resolveInput(
                            dialUrl, currentEngine, currentCustomTemplate
                        )
                        val name = dialName.trim()
                            .ifBlank { hostOf(resolved) ?: resolved }
                        val editing = dialEditing
                        when {
                            editing != null -> {
                                onEditSpeedDial(
                                    editing, SpeedDial(name, resolved)
                                )
                                dialDialogOpen = false
                            }
                            speedDials.any { it.url == resolved } ->
                                android.widget.Toast.makeText(
                                    homeContext, "已经有这个快捷方式了",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            else -> {
                                onAddSpeedDial(SpeedDial(name, resolved))
                                dialDialogOpen = false
                            }
                        }
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { dialDialogOpen = false }) { Text("取消") }
            }
        )
    }
    if (showEnginePicker) {
        AlertDialog(
            onDismissRequest = { showEnginePicker = false },
            title = { Text("选择搜索引擎") },
            text = {
                LazyColumn {
                    items(SearchEngine.entries) { engine ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    if (engine == SearchEngine.CUSTOM &&
                                        currentCustomName.isNotBlank()
                                    ) {
                                        currentCustomName
                                    } else {
                                        engine.label
                                    }
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = currentEngine == engine,
                                    onClick = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (engine == SearchEngine.CUSTOM) {
                                        homeCustomName = currentCustomName
                                        homeCustomTemplate = currentCustomTemplate
                                        homeCustomError = null
                                        showEnginePicker = false
                                        showHomeCustomDialog = true
                                    } else {
                                        currentEngine = engine
                                        onSearchEngineSelected(engine)
                                        showEnginePicker = false
                                    }
                                }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEnginePicker = false }) { Text("取消") }
            }
        )
    }
    if (showHomeCustomDialog) {
        // Opens focused on the first field with the keyboard up.
        val homeCustomFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { homeCustomFocusRequester.requestFocus() }
        AlertDialog(
            onDismissRequest = { showHomeCustomDialog = false },
            title = { Text("自定义搜索引擎") },
            text = {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = homeCustomName,
                            onValueChange = { homeCustomName = it },
                            label = { Text("名称") },
                            placeholder = { Text("比如：我的搜索") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(homeCustomFocusRequester),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = homeCustomTemplate,
                            onValueChange = { homeCustomTemplate = it },
                            label = { Text("网址模板") },
                            placeholder = {
                                Text("比如：https://example.com/search?q=%s")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )
                    }
                    item {
                        Text(
                            "模板里必须有 %s，它会被换成你要搜的词；" +
                                "网址要以 http:// 或 https:// 开头。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    val error = homeCustomError
                    if (error != null) {
                        item {
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val template = homeCustomTemplate.trim()
                    homeCustomError = when {
                        !template.contains("%s") ->
                            "模板里没有 %s，搜的时候没地方放要搜的词，请补上。"
                        !template.startsWith("http://") &&
                            !template.startsWith("https://") ->
                            "网址要以 http:// 或 https:// 开头，请改一下。"
                        else -> null
                    }
                    if (homeCustomError == null) {
                        currentCustomName = homeCustomName.trim()
                        currentCustomTemplate = template
                        currentEngine = SearchEngine.CUSTOM
                        onSaveCustomSearch(homeCustomName.trim(), template)
                        showHomeCustomDialog = false
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showHomeCustomDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 16.dp,
            bottom = 24.dp
        )
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            // Brand area: the app icon on a primary-coloured official
            // Card, the name in headline type, and the one-line
            // tagline from the README.
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        // The launcher foreground's strokes are fixed
                        // white in the drawable, which is invisible on
                        // the dark scheme's light primary — render it as
                        // an Icon tinted with onPrimary, the official
                        // content colour paired with primary in both
                        // schemes (white on blue, navy on light blue).
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                com.tuxunbrowse.android.R.drawable
                                    .ic_launcher_foreground
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(56.dp)
                                .padding(6.dp)
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            "TuxunBrowse",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "打开就是网页——没有信息流，没有推送，没有广告。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // The big home search box is the official DockedSearchBar
            // (inactive state: an input field only, no suggestions
            // layer). Submitting goes through the same resolveInput
            // path the old text field used.
            DockedSearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = {
                    if (query.isNotBlank()) {
                        // Same refusal as the address bar (v0.31.0):
                        // a non-web scheme typed here is not a search.
                        if (hasBlockedPageScheme(query)) {
                            android.widget.Toast.makeText(
                                homeContext,
                                "这个链接打不开：只支持打开 http 或 https 开头的网址",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onSearchUrl(
                                resolveInput(query, currentEngine, currentCustomTemplate)
                            )
                        }
                    }
                },
                active = false,
                onActiveChange = {},
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("用${engineShortName()}搜索，或输入网址", maxLines = 1)
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            ) {}
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // Quick engine switch: only the short engine name on the
            // chip (a long label would squeeze the search box, the
            // same lesson as the address bar). The search field above
            // keeps the full width of the row.
            AssistChip(
                onClick = { showEnginePicker = true },
                label = { Text(engineShortName(), maxLines = 1) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            )
        }
        // Speed dials live above the bookmarks grid and are stored
        // separately from bookmarks. With no dials at all, only a
        // small "add" entry remains (no header, no grid), keeping
        // the home page as lean as before.
        if (speedDials.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextButton(onClick = { openDialDialog(null) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("添加快捷方式", modifier = Modifier.padding(start = 4.dp))
                }
            }
        } else {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text("快捷方式", style = MaterialTheme.typography.titleMedium)
            }
            gridItems(speedDials, key = { it.title + "\n" + it.url }) { dial ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onOpenSpeedDial(dial) },
                            onLongClick = { dialActionTarget = dial }
                        )
                ) {
                    ListItem(
                        headlineContent = { Text(dial.title, maxLines = 2) },
                        supportingContent = {
                            Text(hostOf(dial.url) ?: dial.url, maxLines = 1)
                        },
                        leadingContent = {
                            Text(
                                dial.title.firstOrNull()?.toString() ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
            item {
                ElevatedCard(
                    onClick = { openDialDialog(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("添加") },
                        supportingContent = { Text("最多 12 个") },
                        leadingContent = {
                            Icon(Icons.Filled.Add, contentDescription = null)
                        }
                    )
                }
            }
        }
        // The bookmarks block only exists when there really are
        // bookmarks: no header, no grey empty-state card with a long
        // explanation. History is not shown on the home page at all —
        // it lives on the History page.
        if (bookmarks.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("常用书签", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onOpenAllBookmarks) { Text("全部书签") }
                }
            }
            gridItems(
                bookmarks.sortedByDescending { it.pinned }.take(8),
                key = { it.url }
            ) { bookmark ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onOpenBookmark(bookmark) },
                            onLongClick = { actionTarget = bookmark }
                        )
                ) {
                    ListItem(
                        headlineContent = { Text(bookmark.title, maxLines = 2) },
                        supportingContent = {
                            Text(hostOf(bookmark.url) ?: bookmark.url, maxLines = 1)
                        },
                        leadingContent = {
                            Icon(Icons.Filled.Bookmark, contentDescription = null)
                        },
                        trailingContent = if (bookmark.pinned) {
                            { Icon(Icons.Filled.PushPin, contentDescription = "已置顶") }
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnginePickerScreen(
    title: String,
    subtitle: String,
    initial: Engine,
    confirmLabel: String,
    onConfirm: (Engine) -> Unit
) {
    var selected by remember { mutableStateOf(initial) }
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                EngineCard(
                    engine = Engine.GECKO,
                    description = "火狐自己的内核，独立于系统，自带内核级反跟踪，隐私保护更强，新功能跟得快。代价是安装包大很多（光内核就一百多 MB）。",
                    selected = selected == Engine.GECKO,
                    onSelect = { selected = Engine.GECKO }
                )
            }
            item {
                EngineCard(
                    engine = Engine.WEBVIEW,
                    description = "手机系统自带的内核，兼容性好、省电、占空间小，但版本跟着系统走，老系统上可能偏旧。",
                    selected = selected == Engine.WEBVIEW,
                    onSelect = { selected = Engine.WEBVIEW }
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "两个内核的登录状态互不相通：在一个内核里登录过的网站，换到另一个内核打开需要重新登录。某个网站在火狐内核下显示不正常时，可以点地址栏旁边的切换内核按钮，把当前标签切换到系统 WebView（兼容模式），还能记住这个网站以后都用兼容模式打开。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                Button(
                    onClick = { onConfirm(selected) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(confirmLabel)
                }
            }
        }
    }
}

@Composable
private fun EngineCard(
    engine: Engine,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    ElevatedCard(onClick = onSelect, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(engine.label) },
            supportingContent = { Text(description) },
            leadingContent = { Icon(Icons.Filled.Language, contentDescription = null) },
            trailingContent = { RadioButton(selected = selected, onClick = onSelect) }
        )
    }
}

/**
 * The app's own load-failure page, shown when a tab's page could not
 * be loaded at the network level (offline, unknown host, refused
 * connection, ...). Real Material 3 components only; the column
 * scrolls so nothing is ever cut off on a small screen.
 */
@Composable
private fun LoadErrorContent(
    error: TabLoadError,
    onReload: () -> Unit,
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Error,
            contentDescription = "加载失败",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(56.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            "网页打不开",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            error.reason,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            "打不开的网址：${error.failedUrl}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            error.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onReload) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Text("重新加载", modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(
            onClick = onGoHome,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Icon(Icons.Filled.Home, contentDescription = null)
            Text("回首页", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun BrowserScreen(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    isBookmarked: Boolean,
    currentHostIsCompat: Boolean,
    onSelectTab: (BrowserTab) -> Unit,
    onTabsOpened: () -> Unit,
    onCloseTab: (BrowserTab) -> Unit,
    onCloseAllTabs: () -> Unit,
    onCloseOtherTabs: () -> Unit,
    recentClosed: List<ClosedTabInfo>,
    onReopenClosed: (ClosedTabInfo) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: (Engine) -> Unit,
    incognitoEngine: Engine,
    onIncognitoEngineChosen: (Engine) -> Unit,
    onToggleBookmark: () -> Unit,
    onNavigate: (String) -> Unit,
    bookmarks: List<Bookmark>,
    searchEngine: SearchEngine,
    customSearchName: String = "",
    onOpenSuggestion: (String) -> Unit,
    onGoHome: () -> Unit,
    onReloadPage: () -> Unit,
    onSwitchEngine: (Engine, Boolean) -> Unit,
    onToggleSiteCompat: () -> Unit = {},
    onClearSiteData: (String) -> Unit = {},
    activeDesktopMode: Boolean,
    onToggleDesktopMode: (Boolean) -> Unit,
    onFind: (String, Boolean) -> Unit,
    onClearFind: () -> Unit,
    fullscreenView: android.view.View?,
    chromeHidden: Boolean,
    onExitFullscreen: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onPrintPage: () -> Unit,
    onOpenSniffer: () -> Unit,
    onScreenshot: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    adBlockEnabled: Boolean,
    onAddToReadingList: () -> Unit,
    onOpenReadingList: () -> Unit,
    homeContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var showTabs by remember { mutableStateOf(false) }
    var tabQuery by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showIncognitoPicker by remember { mutableStateOf(false) }
    var showEngineSwitch by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    // The address bar edits a TextFieldValue (not a plain String) so
    // focus can select the whole URL: tapping the bar selects
    // everything and typing replaces it, the way other browsers
    // behave. addressText stays as the plain-text view of the value
    // for the suggestion/search logic below.
    var addressValue by remember(activeTab?.id) {
        mutableStateOf(
            TextFieldValue(activeTab?.takeUnless { it.isHome }?.url ?: "")
        )
    }
    val addressText = addressValue.text
    var addressFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(activeTab?.url) {
        if (!addressFocused && activeTab != null) {
            addressValue = TextFieldValue(
                if (activeTab.isHome) "" else activeTab.url
            )
        }
    }
    // Switching tabs hands focus back to the page: the text above is
    // already re-keyed to the new tab, and this drops any leftover
    // editing focus (and the keyboard) from the previous tab.
    LaunchedEffect(activeTab?.id) {
        focusManager.clearFocus()
    }
    // Select-all on focus gain, done in an effect so it lands after
    // the tap's own cursor placement and wins over it.
    LaunchedEffect(addressFocused) {
        if (addressFocused && addressValue.text.isNotEmpty()) {
            addressValue = addressValue.copy(
                selection = TextRange(0, addressValue.text.length)
            )
        }
    }
    // Submitting the address bar navigates and hands focus (and the
    // keyboard) back to the web page.
    fun submitAddress() {
        onNavigate(addressValue.text)
        focusManager.clearFocus()
    }
    // The find bar grabs focus (and the keyboard) as soon as it opens.
    val findFocusRequester = remember { FocusRequester() }
    LaunchedEffect(showFindBar) {
        if (showFindBar) {
            findFocusRequester.requestFocus()
        }
    }
    // Clipboard link hint: read the clipboard once when this screen
    // appears; if it holds a web link, offer (never force) opening it.
    // Incognito rule (v0.27.0): while the active tab is incognito the
    // clipboard is never read and no hint is shown — what the user
    // copied elsewhere is none of a private session's business. The
    // effect is keyed on the flag so switching into an incognito tab
    // also drops a hint that was read earlier, and switching back to
    // a normal tab re-reads as before.
    val clipboardManager = LocalClipboardManager.current
    var clipUrl by remember { mutableStateOf<String?>(null) }
    var clipDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(activeTab?.incognito) {
        if (activeTab?.incognito == true) {
            clipUrl = null
            return@LaunchedEffect
        }
        val text = try {
            clipboardManager.getText()?.text?.trim()
        } catch (t: Throwable) {
            null
        }
        if (text != null &&
            (text.startsWith("http://") || text.startsWith("https://")) &&
            !text.contains(" ") && text.contains(".")
        ) {
            clipUrl = text
        }
    }
    var showRecentClosed by remember { mutableStateOf(false) }
    var showSiteInfo by remember { mutableStateOf(false) }
    var showMoreTools by remember { mutableStateOf(false) }
    // Long-press on back/forward: this tab's real back/forward stack.
    var navHistory by remember {
        mutableStateOf<Pair<List<NavHistoryEntry>, Int>?>(null)
    }
    var showNavHistory by remember { mutableStateOf(false) }
    fun openNavHistory() {
        val tab = activeTab ?: return
        if (tab.isHome) return
        navHistory = tab.backForwardList()
        showNavHistory = true
    }
    var showClearSiteConfirm by remember { mutableStateOf(false) }
    var showCloseOthersConfirm by remember { mutableStateOf(false) }
    if (showCloseOthersConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseOthersConfirm = false },
            title = { Text("关闭其他标签页？") },
            text = {
                Text("会关掉除当前标签外的 ${tabs.size - 1} 个标签页。普通标签会记进「最近关闭」（最多 10 个），之后能在标签列表里一个个恢复；无痕标签不会被记录。无痕标签关掉后，系统 WebView 的登录状态会被清掉。")
            },
            confirmButton = {
                Button(onClick = {
                    showCloseOthersConfirm = false
                    showTabs = false
                    onCloseOtherTabs()
                }) { Text("关闭其他") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseOthersConfirm = false }) { Text("取消") }
            }
        )
    }

    var showCloseAllConfirm by remember { mutableStateOf(false) }
    if (showCloseAllConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseAllConfirm = false },
            title = { Text("关闭全部标签页？") },
            text = {
                Text("现在开着的 ${tabs.size} 个标签页会全部关掉，然后开一个新的首页标签。普通标签会记进「最近关闭」（最多 10 个），之后能在标签列表里一个个恢复；无痕标签不会被记录。无痕标签关掉后，系统 WebView 的登录状态会被清掉。")
            },
            confirmButton = {
                Button(onClick = {
                    showCloseAllConfirm = false
                    showTabs = false
                    onCloseAllTabs()
                }) { Text("全部关闭") }
            },
            dismissButton = {
                TextButton(onClick = { showCloseAllConfirm = false }) { Text("取消") }
            }
        )
    }

    if (fullscreenView != null) {
        // WebView fullscreen (e.g. a video): show only the fullscreen
        // view plus an exit button. Back also exits (handled upstream).
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { fullscreenView },
                modifier = Modifier.fillMaxSize()
            )
            FilledIconButton(
                onClick = onExitFullscreen,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "退出全屏")
            }
        }
        return
    }

    // System back, resolved in layers (v0.37.0 back-key audit).
    // Compose hands a back press to the most recently composed
    // *enabled* BackHandler, so the handlers below are declared from
    // the outermost layer inwards — the one declared last wins when
    // several are enabled at once: web-history back (weakest), then
    // reader mode, then address-bar focus, then the find bar, and
    // Gecko fullscreen last (strongest). Dialogs, the dropdown menu
    // and the bottom sheets are composed after this block (or handle
    // back inside their own window), so they still win over every
    // layer here. WebView fullscreen never reaches this block: the
    // screen returned early above and BrowserApp's own handler exits
    // it. While Gecko fullscreen hides the chrome, only its handler
    // stays enabled — the layers underneath are not even visible.
    BackHandler(
        enabled = !chromeHidden && activeTab != null &&
            !activeTab.isHome && activeTab.canGoBack
    ) {
        activeTab?.goBack()
    }
    BackHandler(enabled = !chromeHidden && activeTab?.readerActive == true) {
        onToggleReaderMode()
    }
    BackHandler(enabled = !chromeHidden && addressFocused) {
        // Dropping focus also collapses the suggestion list, which
        // is derived from the focus state, and hides the keyboard.
        focusManager.clearFocus()
    }
    BackHandler(
        enabled = !chromeHidden && showFindBar &&
            activeTab != null && !activeTab.isHome
    ) {
        // Same steps as the find bar's own close button.
        showFindBar = false
        findQuery = ""
        onClearFind()
        focusManager.clearFocus()
    }
    BackHandler(enabled = chromeHidden) {
        onExitFullscreen()
    }

    Scaffold(
        topBar = {
            if (!chromeHidden) {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            activeTab == null || activeTab.isHome -> "TuxunBrowse"
                            else -> activeTab.title.ifBlank { "TuxunBrowse" }
                        },
                        maxLines = 1
                    )
                },
                actions = {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            if (isBookmarked) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription =
                                if (isBookmarked) "已收藏，点按取消收藏" else "收藏"
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "菜单")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        // Slim main menu (user's own list): high-
                        // frequency items only. The ad-block status
                        // line moved to the settings ad-block page;
                        // low-frequency tools live under "更多工具".
                        DropdownMenuItem(
                            text = { Text("新建标签页") },
                            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                            onClick = { showMenu = false; onNewTab() }
                        )
                        DropdownMenuItem(
                            text = { Text("新建无痕标签页") },
                            leadingIcon = { Icon(Icons.Filled.VisibilityOff, contentDescription = null) },
                            onClick = { showMenu = false; showIncognitoPicker = true }
                        )
                        DropdownMenuItem(
                            text = { Text("回到首页") },
                            leadingIcon = { Icon(Icons.Filled.Home, contentDescription = null) },
                            onClick = { showMenu = false; onGoHome() }
                        )
                        DropdownMenuItem(
                            text = { Text("分享当前页") },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                val tab = activeTab
                                if (tab == null || tab.isHome) {
                                    android.widget.Toast.makeText(
                                        context, "先打开一个网页再分享",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_SEND
                                        )
                                            .setType("text/plain")
                                            .putExtra(
                                                android.content.Intent.EXTRA_SUBJECT,
                                                tab.title.ifBlank { tab.url }
                                            )
                                            .putExtra(
                                                android.content.Intent.EXTRA_TEXT, tab.url
                                            )
                                        context.startActivity(
                                            android.content.Intent.createChooser(
                                                intent, "分享当前页"
                                            )
                                        )
                                    } catch (t: Throwable) {
                                        android.widget.Toast.makeText(
                                            context, "没有能分享的应用",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("复制当前网址") },
                            leadingIcon = {
                                Icon(Icons.Filled.ContentCopy, contentDescription = null)
                            },
                            onClick = {
                                showMenu = false
                                val tab = activeTab
                                if (tab == null || tab.isHome) {
                                    android.widget.Toast.makeText(
                                        context, "现在没有可复制的网址",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    val ok = copyTextToClipboard(
                                        context, "url", tab.url
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        if (ok) "网址已复制" else "复制失败，请重试",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("页面内查找") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                if (activeTab == null || activeTab.isHome) {
                                    android.widget.Toast.makeText(
                                        context, "先打开一个网页再查找",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    showFindBar = true
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (activeTab?.readerActive == true) {
                                        "退出阅读模式"
                                    } else {
                                        "阅读模式"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Filled.MenuBook, contentDescription = null)
                            },
                            onClick = { showMenu = false; onToggleReaderMode() }
                        )
                        DropdownMenuItem(
                            text = { Text("桌面版网站") },
                            leadingIcon = {
                                Icon(Icons.Filled.Language, contentDescription = null)
                            },
                            trailingIcon = {
                                Switch(
                                    checked = activeDesktopMode,
                                    onCheckedChange = { onToggleDesktopMode(it) }
                                )
                            },
                            onClick = { onToggleDesktopMode(!activeDesktopMode) }
                        )
                        DropdownMenuItem(
                            text = { Text("下载") },
                            leadingIcon = {
                                Icon(Icons.Filled.Download, contentDescription = null)
                            },
                            onClick = { showMenu = false; onOpenDownloads() }
                        )
                        DropdownMenuItem(
                            text = { Text("书签") },
                            leadingIcon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                            onClick = { showMenu = false; onOpenBookmarks() }
                        )
                        DropdownMenuItem(
                            text = { Text("历史记录") },
                            leadingIcon = { Icon(Icons.Filled.History, contentDescription = null) },
                            onClick = { showMenu = false; onOpenHistory() }
                        )
                        // Incognito tabs never write anything to disk,
                        // so they do not offer "add to reading list".
                        if (activeTab != null && !activeTab.incognito &&
                            !activeTab.isHome
                        ) {
                            DropdownMenuItem(
                                text = { Text("加入阅读列表") },
                                leadingIcon = {
                                    Icon(Icons.Filled.MenuBook, contentDescription = null)
                                },
                                onClick = { showMenu = false; onAddToReadingList() }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("设置") },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            onClick = { showMenu = false; onOpenSettings() }
                        )
                        DropdownMenuItem(
                            text = { Text("更多工具") },
                            leadingIcon = {
                                Icon(Icons.Filled.MoreHoriz, contentDescription = null)
                            },
                            onClick = { showMenu = false; showMoreTools = true }
                        )
                    }
                }
            )
            }
        },
        bottomBar = {
            if (!chromeHidden) {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { onTabsOpened(); tabQuery = ""; showTabs = true },
                    icon = { Icon(Icons.Filled.Tab, contentDescription = null) },
                    label = { Text("标签页 ${tabs.size}") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNewTab,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    label = { Text("新建") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { showIncognitoPicker = true },
                    icon = { Icon(Icons.Filled.VisibilityOff, contentDescription = null) },
                    label = { Text("无痕") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onOpenSettings,
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text("设置") }
                )
            }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!chromeHidden) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Back/forward: tap steps one page; long-press opens
                // this tab's back/forward history sheet. IconButton
                // has no long-press path, so the buttons are Boxes
                // with combinedClickable (tap + long-press) sized
                // like the neighbouring icon buttons.
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .combinedClickable(
                            enabled = activeTab != null && !activeTab.isHome,
                            onClick = {
                                if (activeTab?.canGoBack == true) {
                                    activeTab?.goBack()
                                }
                            },
                            onLongClick = { openNavHistory() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "后退（长按看历史）",
                        tint = if (activeTab?.canGoBack == true) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .combinedClickable(
                            enabled = activeTab != null && !activeTab.isHome,
                            onClick = {
                                if (activeTab?.canGoForward == true) {
                                    activeTab?.goForward()
                                }
                            },
                            onLongClick = { openNavHistory() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "前进（长按看历史）",
                        tint = if (activeTab?.canGoForward == true) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
                OutlinedTextField(
                    value = addressValue,
                    onValueChange = { addressValue = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { addressFocused = it.isFocused },
                    singleLine = true,
                    placeholder = {
                        // Never wrap: a wrapped placeholder in a
                        // squeezed field renders one character per
                        // line (the vertical-text bug from the user's
                        // screenshot).
                        Text("输入网址或搜索", maxLines = 1, softWrap = false)
                    },
                    leadingIcon = {
                        // Tapping the status icon opens the site-info
                        // sheet (for the home page it shows the
                        // internal-page info, so the icon always does
                        // something when tapped).
                        IconButton(onClick = { showSiteInfo = true }) {
                            when {
                                activeTab == null || activeTab.isHome ->
                                    Icon(Icons.Filled.Search, contentDescription = "页面信息")
                                activeTab.url.startsWith("https://") ->
                                    Icon(Icons.Filled.Lock, contentDescription = "安全连接，点按看网站信息")
                                else ->
                                    Icon(Icons.Filled.Warning, contentDescription = "非安全连接，点按看网站信息")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = { submitAddress() }
                    )
                )
                if (activeTab != null) {
                    // A narrow icon button, not a labelled chip: the
                    // chip's text ate the address field's width on
                    // narrow screens and squeezed its placeholder into
                    // a vertical strip. The switch dialog still shows
                    // which engine is current.
                    IconButton(onClick = { showEngineSwitch = true }) {
                        Icon(
                            Icons.Filled.SwapHoriz,
                            contentDescription = "切换内核（现在是" +
                                if (activeTab.engine == Engine.GECKO) "火狐）" else "系统 WebView）"
                        )
                    }
                }
                IconButton(onClick = { activeTab?.reload() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                }
            }
            // Loading progress: the bar fades/slides in and out and
            // its value animates smoothly instead of jumping in steps.
            val rawProgress = activeTab?.progress ?: 100
            val animatedProgress by animateFloatAsState(
                targetValue = rawProgress / 100f,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                label = "loadProgress"
            )
            AnimatedVisibility(
                visible = activeTab != null && rawProgress in 1..99,
                enter = fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(200, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(200, easing = FastOutSlowInEasing)) +
                    shrinkVertically(tween(200, easing = FastOutSlowInEasing))
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val clip = clipUrl
            AnimatedVisibility(
                visible = clip != null && !clipDismissed && clip != activeTab?.url &&
                    activeTab?.incognito != true,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                    shrinkVertically(tween(180, easing = FastOutSlowInEasing))
            ) {
            if (clip != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {
                            clipDismissed = true
                            onOpenSuggestion(clip)
                        },
                        label = { Text("打开复制的链接：${hostOf(clip) ?: clip}", maxLines = 1) },
                        leadingIcon = {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { clipDismissed = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "不再提示")
                    }
                }
            }
            }
            // Suggestions are bookmarks only (plus the search tail
            // below): history stays on the History page and is no
            // longer mixed into the address bar. matchSuggestions
            // itself is unchanged — it is simply handed no history.
            val suggestions = if (addressFocused && addressText.isNotBlank()) {
                matchSuggestions(addressText, bookmarks, emptyList())
            } else {
                emptyList()
            }
            // Trailing "just search this" row: shown whenever the typed
            // text is not already a URL (a URL would only duplicate the
            // Go action), and shown even when there are no bookmark
            // matches, so the list is never a dead end.
            val trimmedAddress = addressText.trim()
            val showSearchTail = addressFocused && trimmedAddress.isNotEmpty() &&
                !isUrlLikeInput(trimmedAddress)
            // The suggestion card unfolds under the address bar when
            // it gains focus and folds away again afterwards.
            AnimatedVisibility(
                visible = suggestions.isNotEmpty() || showSearchTail,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                    shrinkVertically(tween(180, easing = FastOutSlowInEasing))
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(suggestions, key = { it.url }) { suggestion ->
                            ListItem(
                                headlineContent = {
                                    Text(suggestion.title, maxLines = 1)
                                },
                                supportingContent = {
                                    Text(suggestion.url, maxLines = 1)
                                },
                                leadingContent = {
                                    Icon(
                                        if (suggestion.bookmarked) {
                                            Icons.Filled.Bookmark
                                        } else {
                                            Icons.Filled.History
                                        },
                                        contentDescription = if (suggestion.bookmarked) {
                                            "书签"
                                        } else {
                                            "历史"
                                        }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenSuggestion(suggestion.url)
                                        focusManager.clearFocus()
                                    }
                            )
                        }
                        if (showSearchTail) {
                            item(key = "search-tail") {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "用${
                                                if (searchEngine == SearchEngine.CUSTOM) {
                                                    customSearchName.ifBlank { "自定义" }
                                                } else {
                                                    searchEngine.label
                                                }
                                            }搜索「$trimmedAddress」",
                                            maxLines = 1
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Filled.Search,
                                            contentDescription = "搜索"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { submitAddress() }
                                )
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = showFindBar && activeTab != null && !activeTab.isHome,
                enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                    expandVertically(tween(220, easing = FastOutSlowInEasing)),
                exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                    shrinkVertically(tween(180, easing = FastOutSlowInEasing))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = findQuery,
                        onValueChange = { findQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(findFocusRequester),
                        singleLine = true,
                        placeholder = { Text("在页面里查找") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onFind(findQuery, true) }
                        )
                    )
                    if (activeTab?.engine == Engine.WEBVIEW &&
                        !activeTab?.findInfo.isNullOrEmpty()
                    ) {
                        Text(
                            activeTab?.findInfo.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    IconButton(onClick = { onFind(findQuery, false) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一个")
                    }
                    IconButton(onClick = { onFind(findQuery, true) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "下一个"
                        )
                    }
                    IconButton(onClick = {
                        showFindBar = false
                        findQuery = ""
                        onClearFind()
                        // Closing the find bar hands focus (and the
                        // keyboard) back to the web page.
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭查找")
                    }
                }
            }
            }
            // Tab content crossfades on every switch: changing tabs,
            // creating or closing one, home <-> web page, and the
            // error page appearing or clearing. Fade only (no slide):
            // the content is a native web view whose surface does not
            // travel well. Keyed by tab id, and the content is looked
            // up by that id so the outgoing and incoming sides each
            // show their own tab during the transition.
            val tabContent: @Composable () -> Unit = {
            AnimatedContent(
                targetState = activeTab?.id,
                transitionSpec = {
                    fadeIn(tween(220, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(tween(180, easing = FastOutSlowInEasing))
                },
                label = "tabContentTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetTabId ->
                val tab = tabs.firstOrNull { it.id == targetTabId }
                when {
                    tab == null -> Box(modifier = Modifier.fillMaxSize())
                    tab.isHome -> homeContent()
                    tab.loadError != null ->
                        // The page failed to load at the network level:
                        // show the app's own error page instead of the
                        // engine's blank/default one. It disappears on
                        // its own once a load succeeds (both engines
                        // clear the state when a new load starts).
                        LoadErrorContent(
                            error = tab.loadError!!,
                            onReload = onReloadPage,
                            onGoHome = onGoHome
                        )
                    else -> TabContent(tab = tab, modifier = Modifier.fillMaxSize())
                }
            }
            }
            // Pull-to-refresh (official Material 3 PullToRefreshBox):
            // pulling down on a web page reloads the current tab.
            // The home page is an app page — a pull there means
            // nothing, so it is not wrapped; the error overlay and
            // fullscreen states (chromeHidden / the early fullscreen
            // return above) are not wrapped either. isRefreshing is
            // bound to the tab's own loading progress, so the
            // indicator folds away by itself when the load ends.
            // Both engines go through the same onReloadPage path.
            val refreshTab = activeTab
            if (refreshTab != null && !refreshTab.isHome &&
                refreshTab.loadError == null && !chromeHidden
            ) {
                PullToRefreshBox(
                    isRefreshing = refreshTab.progress < 100,
                    onRefresh = onReloadPage,
                    modifier = Modifier.fillMaxSize()
                ) {
                    tabContent()
                }
            } else {
                tabContent()
            }
        }
    }

    // ---- More-tools sheet: the low-frequency tools that left the
    // main menu (their features and handlers are unchanged), plus
    // the reading-list view entry. ----
    if (showMoreTools) {
        ModalBottomSheet(
            onDismissRequest = { showMoreTools = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text("更多工具", style = MaterialTheme.typography.titleLarge)
                }
                item {
                    ListItem(
                        headlineContent = { Text("网页截图") },
                        leadingContent = {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMoreTools = false; onScreenshot() }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("资源嗅探") },
                        leadingContent = {
                            Icon(Icons.Filled.FindInPage, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMoreTools = false; onOpenSniffer() }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("打印 / 存为 PDF") },
                        leadingContent = {
                            Icon(Icons.Filled.FileUpload, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMoreTools = false; onPrintPage() }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("用其他浏览器打开") },
                        leadingContent = {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showMoreTools = false
                                val tab = activeTab
                                if (tab == null || tab.isHome ||
                                    !(tab.url.startsWith("http://") ||
                                        tab.url.startsWith("https://"))
                                ) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "这是应用内部的页面，没法用其他浏览器打开",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(tab.url)
                                        )
                                        context.startActivity(
                                            android.content.Intent.createChooser(
                                                intent, "用其他浏览器打开"
                                            )
                                        )
                                    } catch (t: Throwable) {
                                        android.widget.Toast.makeText(
                                            context,
                                            "没有找到其他浏览器",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                    )
                }
                item {
                    ListItem(
                        headlineContent = { Text("阅读列表") },
                        leadingContent = {
                            Icon(
                                Icons.Filled.FormatListBulleted,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMoreTools = false; onOpenReadingList() }
                    )
                }
            }
        }
    }

    // ---- Back/forward history sheet (long-press on back/forward) ----
    if (showNavHistory) {
        ModalBottomSheet(
            onDismissRequest = { showNavHistory = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text("本标签的浏览历史", style = MaterialTheme.typography.titleLarge)
                }
                val nav = navHistory
                if (nav == null || nav.first.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.Filled.History,
                            "这个标签还没有可以前进后退的页面。"
                        )
                    }
                } else {
                    // Newest first, so the pages you can go back to
                    // are at the top; the index math uses the real
                    // stack position, not the display position.
                    val entries = nav.first
                    val current = nav.second
                    items(entries.indices.reversed().toList(), key = { it }) { idx ->
                        val entry = entries[idx]
                        val isCurrent = idx == current
                        ListItem(
                            headlineContent = {
                                Text(
                                    entry.title.ifBlank { entry.url },
                                    maxLines = 1,
                                    fontWeight = if (isCurrent) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Normal
                                    }
                                )
                            },
                            supportingContent = { Text(entry.url, maxLines = 1) },
                            trailingContent = if (isCurrent) {
                                { Text("当前页") }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showNavHistory = false
                                    activeTab?.goToHistoryIndex(idx)
                                }
                        )
                    }
                }
            }
        }
    }

    // ---- Site-info sheet (address-bar status icon) ----
    val siteTab = activeTab
    val siteHost = siteTab?.let { hostOf(it.url) }
    val siteSheetState = rememberModalBottomSheetState()
    if (showClearSiteConfirm && siteHost != null) {
        AlertDialog(
            onDismissRequest = { showClearSiteConfirm = false },
            title = { Text("清除这个网站的数据？") },
            text = {
                Text(
                    "会清掉火狐里 $siteHost 的 Cookie 和网站数据，并删掉本机保存的" +
                        "这个网站的历史记录。注意：系统 WebView 的网站数据没法按网站单独清" +
                        "（系统 WebView 不支持按网站清除），这次不会清系统 WebView 的数据；" +
                        "要清的话只能去设置里的「清除浏览数据」整清。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showClearSiteConfirm = false
                    onClearSiteData(siteHost)
                    android.widget.Toast.makeText(
                        context, "已清除 $siteHost 在火狐里的数据和本机历史",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearSiteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
    if (showSiteInfo) {
        ModalBottomSheet(
            onDismissRequest = { showSiteInfo = false },
            sheetState = siteSheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text("网站信息", style = MaterialTheme.typography.titleLarge)
                }
                if (siteTab == null || siteTab.isHome) {
                    item {
                        ListItem(
                            headlineContent = { Text("TuxunBrowse 首页") },
                            supportingContent = {
                                Text("这是应用自己的内部页面，不是一个网站，没有网络连接，也没有网站数据可清。")
                            },
                            leadingContent = {
                                Icon(Icons.Filled.Home, contentDescription = null)
                            }
                        )
                    }
                    if (siteTab != null) {
                        item {
                            ListItem(
                                headlineContent = { Text("当前标签内核") },
                                supportingContent = { Text(siteTab.engine.label) },
                                leadingContent = {
                                    Icon(Icons.Filled.Language, contentDescription = null)
                                }
                            )
                        }
                    }
                } else {
                    item {
                        ListItem(
                            headlineContent = { Text(siteHost ?: siteTab.url) },
                            supportingContent = { Text("域名") },
                            leadingContent = {
                                Icon(Icons.Filled.Language, contentDescription = null)
                            }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("完整网址") },
                            supportingContent = { Text(siteTab.url) },
                            leadingContent = {
                                Icon(Icons.Filled.OpenInNew, contentDescription = null)
                            }
                        )
                    }
                    item {
                        if (siteTab.url.startsWith("https://")) {
                            ListItem(
                                headlineContent = { Text("连接安全") },
                                supportingContent = {
                                    Text("这个网站用了加密连接，内容在路上传输时不容易被别人看到或改掉。")
                                },
                                leadingContent = {
                                    Icon(Icons.Filled.Lock, contentDescription = null)
                                }
                            )
                        } else {
                            ListItem(
                                headlineContent = { Text("连接不安全") },
                                supportingContent = {
                                    Text("这个网站没有加密连接，在这里输入的密码等内容可能被别人看到，重要的事情别在这个网站上做。")
                                },
                                leadingContent = {
                                    Icon(Icons.Filled.Warning, contentDescription = null)
                                }
                            )
                        }
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("当前标签内核") },
                            supportingContent = { Text(siteTab.engine.label) },
                            leadingContent = {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                            }
                        )
                    }
                    item {
                        if (siteTab.engine == Engine.WEBVIEW) {
                            ListItem(
                                headlineContent = { Text("本页已拦截") },
                                supportingContent = {
                                    Text("这个页面已拦下 ${siteTab.blockedCount} 个广告/跟踪请求。")
                                },
                                leadingContent = {
                                    Icon(Icons.Filled.VisibilityOff, contentDescription = null)
                                }
                            )
                        } else {
                            ListItem(
                                headlineContent = { Text("本页已拦截") },
                                supportingContent = {
                                    Text("火狐用内核自带的反跟踪拦截，没有个数统计，所以这里不显示数字。")
                                },
                                leadingContent = {
                                    Icon(Icons.Filled.VisibilityOff, contentDescription = null)
                                }
                            )
                        }
                    }
                    if (siteTab.engine == Engine.WEBVIEW &&
                        (siteTab.certIssuedTo.isNotBlank() || siteTab.certIssuedBy.isNotBlank())
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("证书") },
                                supportingContent = {
                                    Text(
                                        listOfNotNull(
                                            siteTab.certIssuedTo.takeIf { it.isNotBlank() }
                                                ?.let { "颁发给：$it" },
                                            siteTab.certIssuedBy.takeIf { it.isNotBlank() }
                                                ?.let { "颁发者：$it" }
                                        ).joinToString("\n")
                                    )
                                },
                                leadingContent = {
                                    Icon(Icons.Filled.Lock, contentDescription = null)
                                }
                            )
                        }
                    }
                    item {
                        Button(
                            onClick = {
                                showSiteInfo = false
                                onToggleSiteCompat()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                if (currentHostIsCompat) {
                                    "取消本站兼容模式"
                                } else {
                                    "本站用兼容模式打开"
                                }
                            )
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = {
                                showSiteInfo = false
                                showClearSiteConfirm = true
                            },
                            enabled = siteHost != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("清除本站数据")
                        }
                    }
                    item {
                        TextButton(
                            onClick = {
                                val ok = try {
                                    clipboardManager.setText(
                                        AnnotatedString(siteTab.url)
                                    )
                                    true
                                } catch (t: Throwable) {
                                    false
                                }
                                showSiteInfo = false
                                android.widget.Toast.makeText(
                                    context,
                                    if (ok) "网址已复制" else "复制失败，请重试",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("复制网址")
                        }
                    }
                }
            }
        }
    }

    val visibleTabs = if (tabQuery.isBlank()) {
        tabs
    } else {
        tabs.filter {
            it.title.contains(tabQuery, ignoreCase = true) ||
                it.url.contains(tabQuery, ignoreCase = true)
        }
    }
    val sheetState = rememberModalBottomSheetState()
    if (showTabs) {
        ModalBottomSheet(
            onDismissRequest = { showTabs = false },
            sheetState = sheetState
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("标签页", style = MaterialTheme.typography.titleLarge)
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    OutlinedTextField(
                        value = tabQuery,
                        onValueChange = { tabQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("搜索标签页（标题或网址）") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
                if (visibleTabs.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Filled.Search, "没有找到相关的标签页。")
                    }
                }
                gridItems(visibleTabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTab?.id
                    ElevatedCard(
                        onClick = { onSelectTab(tab); showTabs = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isActive) {
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        } else {
                            CardDefaults.elevatedCardColors()
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            (if (tab.incognito) "无痕 · " else "") +
                                                if (tab.engine == Engine.GECKO) {
                                                    "火狐"
                                                } else {
                                                    "系统 WebView"
                                                }
                                        )
                                    }
                                )
                                IconButton(onClick = { onCloseTab(tab) }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "关闭标签"
                                    )
                                }
                            }
                            // Preview area, same height on every card.
                            // WebView tabs show their real captured
                            // thumbnail when one exists; GeckoView has
                            // no screenshot API, so its cards say so in
                            // words instead of faking a preview image.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                val thumb = tab.thumbnail
                                when {
                                    // Incognito tabs never show a
                                    // preview (v0.27.0): they are never
                                    // captured in the first place, and
                                    // the grid says so in words instead.
                                    tab.incognito -> Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme
                                                .onSurfaceVariant,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(bottom = 4.dp)
                                        )
                                        Text(
                                            "无痕标签不显示预览",
                                            style = MaterialTheme.typography
                                                .bodySmall,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )
                                    }
                                    thumb != null -> FoundationImage(
                                        bitmap = thumb.asImageBitmap(),
                                        contentDescription = "页面预览",
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.TopCenter,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    tab.engine == Engine.GECKO -> Column(
                                        horizontalAlignment =
                                            Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.Language,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme
                                                .onSurfaceVariant,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .padding(bottom = 4.dp)
                                        )
                                        Text(
                                            "火狐标签暂无预览",
                                            style = MaterialTheme.typography
                                                .bodySmall,
                                            color = MaterialTheme.colorScheme
                                                .onSurfaceVariant
                                        )
                                    }
                                    else -> Icon(
                                        Icons.Filled.Language,
                                        contentDescription = "暂无预览",
                                        tint = MaterialTheme.colorScheme
                                            .onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                if (tab.isHome) {
                                    "首页"
                                } else {
                                    tab.title.ifBlank { "新标签页" }
                                },
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 2
                            )
                            Text(
                                if (tab.isHome) {
                                    "TuxunBrowse 首页"
                                } else {
                                    hostOf(tab.url) ?: tab.url
                                },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                            if (isActive) {
                                Text(
                                    "当前标签",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    FilledTonalButton(
                        onClick = { showTabs = false; onNewTab() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("新建标签页")
                    }
                }
                if (recentClosed.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showRecentClosed = !showRecentClosed },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (showRecentClosed) {
                                        "最近关闭（${recentClosed.size}）· 收起"
                                    } else {
                                        "最近关闭（${recentClosed.size}）"
                                    }
                                )
                            }
                            AnimatedVisibility(
                                visible = showRecentClosed,
                                enter = fadeIn(
                                    tween(220, easing = FastOutSlowInEasing)
                                ) + expandVertically(
                                    tween(220, easing = FastOutSlowInEasing)
                                ),
                                exit = fadeOut(
                                    tween(180, easing = FastOutSlowInEasing)
                                ) + shrinkVertically(
                                    tween(180, easing = FastOutSlowInEasing)
                                )
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    recentClosed.forEach { info ->
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    if (info.url == HOME_URL) {
                                                        "首页"
                                                    } else {
                                                        info.title
                                                    },
                                                    maxLines = 1
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    (if (info.engine == Engine.GECKO) {
                                                        "火狐 · "
                                                    } else {
                                                        "系统 WebView · "
                                                    }) + (hostOf(info.url) ?: info.url),
                                                    maxLines = 1
                                                )
                                            },
                                            leadingContent = {
                                                Icon(
                                                    Icons.Filled.History,
                                                    contentDescription = null
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    showTabs = false
                                                    onReopenClosed(info)
                                                }
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                }
                if (tabs.size > 1) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutlinedButton(
                            onClick = { showCloseOthersConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("关闭其他标签页")
                        }
                    }
                }
                if (tabs.size > 1) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutlinedButton(
                            onClick = { showCloseAllConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("关闭全部标签页")
                        }
                    }
                }
            }
        }
    }

    if (showEngineSwitch && activeTab != null) {
        var picked by remember { mutableStateOf(activeTab.engine) }
        var rememberSite by remember { mutableStateOf(true) }
        val currentHost = hostOf(activeTab.url)
        AlertDialog(
            onDismissRequest = { showEngineSwitch = false },
            title = { Text("这个标签用哪个内核？") },
            text = {
                LazyColumn {
                    item {
                        Text("切换后这个标签会用新内核重新打开当前页面，登录状态和原来那个内核不互通。")
                    }
                    items(Engine.entries) { engine ->
                        ListItem(
                            headlineContent = { Text(engine.label) },
                            leadingContent = {
                                RadioButton(
                                    selected = picked == engine,
                                    onClick = { picked = engine }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { picked = engine }
                        )
                    }
                    if (currentHost != null && picked == Engine.WEBVIEW &&
                        activeTab.engine == Engine.GECKO
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("这个网站以后都用兼容模式打开") },
                                supportingContent = { Text(currentHost) },
                                trailingContent = {
                                    Switch(
                                        checked = rememberSite,
                                        onCheckedChange = { rememberSite = it }
                                    )
                                }
                            )
                        }
                    }
                    if (currentHost != null && picked == Engine.GECKO && currentHostIsCompat) {
                        item {
                            ListItem(
                                headlineContent = { Text("取消这个网站的自动兼容记忆") },
                                supportingContent = { Text(currentHost) },
                                trailingContent = {
                                    Switch(
                                        checked = rememberSite,
                                        onCheckedChange = { rememberSite = it }
                                    )
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showEngineSwitch = false
                    onSwitchEngine(picked, rememberSite)
                }) {
                    Text("切换")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEngineSwitch = false }) { Text("取消") }
            }
        )
    }

    if (showIncognitoPicker) {
        var picked by remember { mutableStateOf(incognitoEngine) }
        AlertDialog(
            onDismissRequest = { showIncognitoPicker = false },
            title = { Text("无痕浏览用哪个内核？") },
            text = {
                LazyColumn {
                    item {
                        Text("无痕标签不会记录历史。选好的内核会单独记住，下次无痕时默认用它。火狐无痕是真隔离，Cookie 独立存放；系统 WebView 的无痕标签全部关掉后，会清掉系统 WebView 的全部 Cookie，普通标签也会跟着退出登录（系统 WebView 只有一个 Cookie 罐，分不开）。")
                    }
                    items(Engine.entries) { engine ->
                        ListItem(
                            headlineContent = { Text(engine.label) },
                            leadingContent = {
                                RadioButton(
                                    selected = picked == engine,
                                    onClick = { picked = engine }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { picked = engine }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onIncognitoEngineChosen(picked)
                    showIncognitoPicker = false
                    onNewIncognitoTab(picked)
                }) {
                    Text("打开无痕标签")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIncognitoPicker = false }) { Text("取消") }
            }
        )
    }
}

/**
 * Shared empty state: one large muted icon and one short line,
 * centred — never a bare paragraph of text on a plain background.
 */
@Composable
private fun EmptyState(icon: ImageVector, text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(48.dp)
                .padding(bottom = 12.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksScreen(
    bookmarks: List<Bookmark>,
    onBack: () -> Unit,
    onOpen: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit,
    onEdit: (Bookmark, String, String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDeleteMany: (List<Bookmark>) -> Unit = {}
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Bookmark?>(null) }
    val editingBookmark = editing
    if (editingBookmark != null) {
        var editTitle by remember(editingBookmark) { mutableStateOf(editingBookmark.title) }
        var editUrl by remember(editingBookmark) { mutableStateOf(editingBookmark.url) }
        // Opens focused on the first field with the keyboard up.
        val editFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { editFocusRequester.requestFocus() }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("编辑书签") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(editFocusRequester),
                            label = { Text("名称") },
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editUrl,
                            onValueChange = { editUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("网址") },
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hasBlockedPageScheme(editUrl)) {
                            // Non-web schemes are never saved as
                            // bookmarks (v0.31.0, same rule as openUrl).
                            android.widget.Toast.makeText(
                                context,
                                "这个链接打不开：只支持打开 http 或 https 开头的网址",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val resolved = resolveInput(editUrl)
                            onEdit(
                                editingBookmark,
                                editTitle.ifBlank { resolved },
                                resolved
                            )
                            editing = null
                        }
                    },
                    enabled = editUrl.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("取消") }
            }
        )
    }
    var bookmarkQuery by remember { mutableStateOf("") }
    val visibleBookmarks = if (bookmarkQuery.isBlank()) {
        bookmarks
    } else {
        bookmarks.filter {
            it.title.contains(bookmarkQuery, ignoreCase = true) ||
                it.url.contains(bookmarkQuery, ignoreCase = true)
        }
    }
    // Batch-manage mode: checkboxes on every row, a delete-selected
    // button on top, and clear ways out (back key, the close icon
    // and the "完成" button all leave the mode).
    var batchMode by remember { mutableStateOf(false) }
    var selectedUrls by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    fun exitBatchMode() {
        batchMode = false
        selectedUrls = emptySet()
    }
    BackHandler(enabled = batchMode) { exitBatchMode() }
    // Outside batch mode the same back key leaves the page itself
    // (v0.37.0): before this, no handler was enabled here and the
    // system finished the activity instead of returning to the
    // browser. The two handlers are mutually exclusive by enabled.
    BackHandler(enabled = !batchMode) { onBack() }
    if (showBatchDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { Text("删除所选书签？") },
            text = {
                Text("选中的 ${selectedUrls.size} 条书签会被删掉，没法恢复。")
            },
            confirmButton = {
                Button(onClick = {
                    showBatchDeleteConfirm = false
                    onDeleteMany(bookmarks.filter { it.url in selectedUrls })
                    exitBatchMode()
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (batchMode) "已选 ${selectedUrls.size} 条" else "书签")
                },
                navigationIcon = {
                    if (batchMode) {
                        IconButton(onClick = { exitBatchMode() }) {
                            Icon(Icons.Filled.Close, contentDescription = "退出批量管理")
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                actions = {
                    if (batchMode) {
                        TextButton(onClick = { exitBatchMode() }) {
                            Text("完成")
                        }
                    } else {
                        if (bookmarks.isNotEmpty()) {
                            TextButton(onClick = {
                                selectedUrls = emptySet()
                                batchMode = true
                            }) {
                                Text("批量管理")
                            }
                        }
                        IconButton(onClick = onImport) {
                            Icon(Icons.Filled.FileDownload, contentDescription = "导入书签")
                        }
                        IconButton(onClick = onExport) {
                            Icon(Icons.Filled.FileUpload, contentDescription = "导出书签")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 24.dp
            )
        ) {
            if (bookmarks.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Bookmark,
                        "还没有书签，点网页右上角的星星就能存一个。"
                    )
                }
            } else {
                item {
                    OutlinedTextField(
                        value = bookmarkQuery,
                        onValueChange = { bookmarkQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        placeholder = { Text("搜索书签（名称或网址）") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
                if (visibleBookmarks.isEmpty()) {
                    item {
                        EmptyState(Icons.Filled.Search, "没有找到相关的书签。")
                    }
                }
            }
            if (batchMode && visibleBookmarks.isNotEmpty()) {
                item {
                    Button(
                        onClick = { showBatchDeleteConfirm = true },
                        enabled = selectedUrls.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("删除所选（${selectedUrls.size}）")
                    }
                }
            }
            items(visibleBookmarks, key = { it.url }) { bookmark ->
                val selected = bookmark.url in selectedUrls
                ListItem(
                    headlineContent = { Text(bookmark.title) },
                    supportingContent = {
                        Text(
                            if (bookmark.pinned) {
                                "已置顶（在首页最前面） · ${bookmark.url}"
                            } else {
                                bookmark.url
                            }
                        )
                    },
                    leadingContent = {
                        if (batchMode) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { checked ->
                                    selectedUrls = if (checked) {
                                        selectedUrls + bookmark.url
                                    } else {
                                        selectedUrls - bookmark.url
                                    }
                                }
                            )
                        } else {
                            Icon(Icons.Filled.Bookmark, contentDescription = null)
                        }
                    },
                    trailingContent = if (batchMode) {
                        null
                    } else {
                        {
                            Row {
                                IconButton(onClick = { editing = bookmark }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "编辑书签")
                                }
                                IconButton(onClick = { onDelete(bookmark) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "删除书签")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (batchMode) {
                                selectedUrls = if (selected) {
                                    selectedUrls - bookmark.url
                                } else {
                                    selectedUrls + bookmark.url
                                }
                            } else {
                                onOpen(bookmark)
                            }
                        }
                )
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryScreen(
    history: List<HistoryItem>,
    onBack: () -> Unit,
    onOpen: (HistoryItem) -> Unit,
    onDelete: (HistoryItem) -> Unit,
    onClear: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) {
        history
    } else {
        history.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.url.contains(query, ignoreCase = true)
        }
    }
    val zone = java.time.ZoneId.systemDefault()
    val today = java.time.LocalDate.now(zone)
    val yesterday = today.minusDays(1)
    fun bucketOf(item: HistoryItem): String {
        val date = java.time.Instant.ofEpochMilli(item.time)
            .atZone(zone).toLocalDate()
        return when {
            date == today -> "今天"
            date == yesterday -> "昨天"
            else -> "更早"
        }
    }
    // Grouping is precomputed once per (filtered list, day) instead
    // of on every recomposition (v0.23.0): bucketOf converts each
    // item's timestamp to a LocalDate, and the old inline code ran
    // that conversion up to three times per item (once per bucket
    // filter) whenever this screen recomposed. remember keeps the
    // result stable while the user scrolls; it is recomputed only
    // when the filtered list or the calendar day changes.
    val groups = remember(filtered, today) {
        listOf("今天", "昨天", "更早")
            .map { bucket -> bucket to filtered.filter { bucketOf(it) == bucket } }
            .filter { it.second.isNotEmpty() }
    }
    // System back returns to the browser (v0.37.0); before this the
    // key finished the activity from every sub-page.
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 24.dp
            )
        ) {
            if (history.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        placeholder = { Text("搜索历史（标题或网址）") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
                item {
                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("清除全部历史")
                    }
                }
            }
            if (history.isEmpty()) {
                item {
                    EmptyState(Icons.Filled.History, "还没有历史记录。")
                }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyState(Icons.Filled.Search, "没有找到相关的历史记录。")
                }
            }
            groups.forEach { (bucket, bucketItems) ->
                item(key = "bucket-$bucket") {
                    Text(
                        "$bucket · ${bucketItems.size} 条",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(bucketItems, key = { it.time.toString() + it.url }) { item ->
                    ListItem(
                        headlineContent = { Text(item.title) },
                        supportingContent = { Text(item.url) },
                        leadingContent = {
                            Icon(Icons.Filled.History, contentDescription = null)
                        },
                        trailingContent = {
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "删除这条历史"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(item) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Reading list ("read it later"): pages saved from the browser menu,
 * newest first. Unread entries stand out with a bold title and a dot;
 * opening one marks it read. Stored locally like bookmarks — engine
 * agnostic, entries open through the usual openUrl path.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingListScreen(
    items: List<ReadingItem>,
    onBack: () -> Unit,
    onOpen: (ReadingItem) -> Unit,
    onDelete: (ReadingItem) -> Unit,
    onClearRead: () -> Unit
) {
    val timeFormat = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    }
    // System back returns to the browser (v0.37.0).
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("阅读列表") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 24.dp
            )
        ) {
            if (items.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.MenuBook,
                        "阅读列表还是空的。打开网页后，在菜单里点「加入阅读列表」就能存进来稍后看。"
                    )
                }
            } else {
                if (items.any { it.read }) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onClearRead) {
                                Text("清除已读")
                            }
                        }
                    }
                }
                items(items, key = { it.url }) { item ->
                    ListItem(
                        headlineContent = {
                            Text(
                                item.title,
                                fontWeight = if (item.read) {
                                    FontWeight.Normal
                                } else {
                                    FontWeight.Bold
                                }
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(item.url)
                                Text(
                                    "加入于 " +
                                        timeFormat.format(java.util.Date(item.time))
                                )
                            }
                        },
                        leadingContent = {
                            if (item.read) {
                                Icon(
                                    Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Icon(
                                    Icons.Filled.FiberManualRecord,
                                    contentDescription = "未读",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "从阅读列表删除"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(item) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

/**
 * Page resource sniffer (WebView tabs only; Gecko tabs never reach
 * this screen — the menu entry explains why). Runs the collection
 * script once when the screen appears, then lists what was found
 * grouped by kind, each row downloadable through the existing
 * system-download path.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SniffScreen(
    tab: BrowserTab?,
    onBack: () -> Unit,
    onDownload: (SniffedResource) -> Unit,
    onDownloadAll: (List<SniffedResource>) -> Unit
) {
    var loading by remember(tab?.id) { mutableStateOf(true) }
    var resources by remember(tab?.id) {
        mutableStateOf<List<SniffedResource>>(emptyList())
    }
    LaunchedEffect(tab?.id) {
        val target = tab
        if (target == null) {
            loading = false
        } else {
            target.collectPageResources { found ->
                resources = found
                loading = false
            }
        }
    }
    // System back returns to the browser (v0.37.0).
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("资源嗅探") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                "当前页面：" + (tab?.let { it.title.ifBlank { it.url } } ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                resources.isEmpty() -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        EmptyState(
                            Icons.Filled.FindInPage,
                            "这个页面里没找到可下载的资源。"
                        )
                    }
                }
                else -> {
                    Button(
                        onClick = { onDownloadAll(resources) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Text(
                            "全部下载（${resources.size} 个）",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    val groups = listOf(
                        SniffKind.FILE to "文件",
                        SniffKind.IMAGE to "图片",
                        SniffKind.MEDIA to "音视频"
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        groups.forEach { (kind, label) ->
                            val inGroup = resources.filter { it.kind == kind }
                            if (inGroup.isNotEmpty()) {
                                item(key = "sniff-head-$kind") {
                                    Text(
                                        "$label（${inGroup.size}）",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(
                                            horizontal = 16.dp,
                                            vertical = 8.dp
                                        )
                                    )
                                }
                                items(inGroup, key = { it.url }) { res ->
                                    ListItem(
                                        headlineContent = {
                                            Text(res.name, maxLines = 2)
                                        },
                                        supportingContent = {
                                            Text(
                                                res.host.ifBlank { res.url },
                                                maxLines = 1
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                when (kind) {
                                                    SniffKind.FILE ->
                                                        Icons.Filled.InsertDriveFile
                                                    SniffKind.IMAGE ->
                                                        Icons.Filled.Image
                                                    SniffKind.MEDIA ->
                                                        Icons.Filled.Movie
                                                },
                                                contentDescription = label
                                            )
                                        },
                                        trailingContent = {
                                            IconButton(
                                                onClick = { onDownload(res) }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Download,
                                                    contentDescription = "下载"
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class DownloadEntry(
    val id: Long,
    val title: String,
    val status: Int,
    val statusText: String,
    val sizeText: String,
    val mediaType: String?,
    val sourceUrl: String?
)

/** Plain-language text for a pending/paused download's reason code. */
private fun downloadPausedReasonText(reason: Int): String = when (reason) {
    android.app.DownloadManager.PAUSED_WAITING_TO_RETRY -> "等待重试"
    android.app.DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "在等网络"
    android.app.DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "在等 WiFi"
    android.app.DownloadManager.PAUSED_UNKNOWN -> "原因未知"
    else -> "原因码 $reason"
}

/** Plain-language text for a failed download's reason code. */
private fun downloadFailedReasonText(reason: Int): String = when (reason) {
    android.app.DownloadManager.ERROR_UNKNOWN -> "未知错误"
    android.app.DownloadManager.ERROR_FILE_ERROR ->
        "写文件失败（存储空间或文件出问题）"
    android.app.DownloadManager.ERROR_UNHANDLED_HTTP_CODE ->
        "服务器返回了一个没法处理的状态码"
    android.app.DownloadManager.ERROR_HTTP_DATA_ERROR ->
        "传输中断（网络数据出错）"
    android.app.DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "跳转次数太多"
    android.app.DownloadManager.ERROR_INSUFFICIENT_SPACE -> "存储空间不足"
    android.app.DownloadManager.ERROR_DEVICE_NOT_FOUND -> "找不到存储设备"
    android.app.DownloadManager.ERROR_CANNOT_RESUME -> "这个下载没法接着之前继续下"
    android.app.DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "同名文件已经存在"
    in 300..599 -> "服务器返回 HTTP $reason"
    else -> "原因码 $reason"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsScreen(
    onBack: () -> Unit,
    retryReferer: String? = null
) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<DownloadEntry>>(emptyList()) }
    var refreshTick by remember { mutableStateOf(0) }
    LaunchedEffect(refreshTick) {
        val manager = context.getSystemService(
            android.content.Context.DOWNLOAD_SERVICE
        ) as android.app.DownloadManager
        val loaded = mutableListOf<DownloadEntry>()
        try {
            val cursor = manager.query(android.app.DownloadManager.Query())
            cursor?.use { c ->
                val idIdx = c.getColumnIndex(android.app.DownloadManager.COLUMN_ID)
                val titleIdx = c.getColumnIndex(android.app.DownloadManager.COLUMN_TITLE)
                val statusIdx = c.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS)
                val totalIdx =
                    c.getColumnIndex(android.app.DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                val doneIdx = c.getColumnIndex(
                    android.app.DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                )
                val typeIdx =
                    c.getColumnIndex(android.app.DownloadManager.COLUMN_MEDIA_TYPE)
                val uriIdx = c.getColumnIndex(android.app.DownloadManager.COLUMN_URI)
                val reasonIdx =
                    c.getColumnIndex(android.app.DownloadManager.COLUMN_REASON)
                while (c.moveToNext()) {
                    val status = c.getInt(statusIdx)
                    val reason = if (reasonIdx >= 0) c.getInt(reasonIdx) else 0
                    // Never leave the user staring at a bare "排队中"
                    // or "失败": the system's reason code is mapped
                    // to plain language and appended.
                    val statusText = when (status) {
                        android.app.DownloadManager.STATUS_SUCCESSFUL -> "已完成"
                        android.app.DownloadManager.STATUS_FAILED ->
                            "失败 · ${downloadFailedReasonText(reason)}"
                        android.app.DownloadManager.STATUS_RUNNING -> "下载中"
                        android.app.DownloadManager.STATUS_PAUSED ->
                            "已暂停 · ${downloadPausedReasonText(reason)}"
                        else -> "排队中 · ${downloadPausedReasonText(reason)}"
                    }
                    val total = c.getLong(totalIdx)
                    val done = c.getLong(doneIdx)
                    val sizeText = if (total > 0) {
                        "${done / 1024} KB / ${total / 1024} KB"
                    } else {
                        "${done / 1024} KB"
                    }
                    loaded.add(
                        DownloadEntry(
                            id = c.getLong(idIdx),
                            title = c.getString(titleIdx) ?: "未命名文件",
                            status = status,
                            statusText = statusText,
                            sizeText = sizeText,
                            mediaType = c.getString(typeIdx),
                            sourceUrl = c.getString(uriIdx)
                        )
                    )
                }
            }
        } catch (t: Throwable) {
            // Download list is best-effort; an empty list is shown instead.
        }
        entries = loaded.reversed()
    }

    // Status filter (presentation only — the DownloadManager query
    // above is unchanged, this just narrows what the list shows).
    // 0 = all, 1 = in progress (queued / running / paused),
    // 2 = completed, 3 = failed.
    var statusFilter by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()
    LaunchedEffect(statusFilter) {
        listState.scrollToItem(0)
    }
    // Presentation filter, memoised on the inputs (v0.23.0): there
    // is no polling timer on this screen — entries only change when
    // refreshTick bumps (screen entry, delete, retry) — and with
    // remember the filter no longer re-runs on unrelated
    // recompositions while the list is being scrolled.
    val visibleEntries = remember(entries, statusFilter) {
        entries.filter { entry ->
            when (statusFilter) {
                1 -> entry.status == android.app.DownloadManager.STATUS_PENDING ||
                    entry.status == android.app.DownloadManager.STATUS_RUNNING ||
                    entry.status == android.app.DownloadManager.STATUS_PAUSED
                2 -> entry.status == android.app.DownloadManager.STATUS_SUCCESSFUL
                3 -> entry.status == android.app.DownloadManager.STATUS_FAILED
                else -> true
            }
        }
    }

    var pendingDeleteEntry by remember { mutableStateOf<DownloadEntry?>(null) }
    val toDeleteEntry = pendingDeleteEntry
    if (toDeleteEntry != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text("删除这条下载记录？") },
            text = {
                Text("「${toDeleteEntry.title}」的记录会被删掉，已经下载好的文件也会一起从下载文件夹里删掉，没法恢复。")
            },
            confirmButton = {
                Button(onClick = {
                    try {
                        val manager = context.getSystemService(
                            android.content.Context.DOWNLOAD_SERVICE
                        ) as android.app.DownloadManager
                        manager.remove(toDeleteEntry.id)
                    } catch (t: Throwable) {
                        // Best-effort; the list refresh below shows the truth.
                    }
                    pendingDeleteEntry = null
                    refreshTick++
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteEntry = null }) { Text("取消") }
            }
        )
    }

    fun retryDownload(entry: DownloadEntry) {
        val source = entry.sourceUrl
        if (source.isNullOrBlank()) {
            android.widget.Toast.makeText(
                context, "这条记录没有原始地址，没法重试",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        // Retry goes through the exact same path as a fresh download:
        // startSystemDownload fetches the current session's Cookie
        // and a fresh User-Agent at enqueue time and adds the
        // Referer it is given (the page currently open, passed in
        // from the app). The old bare re-enqueue sent no headers at
        // all, which is why retries failed the same way. The old
        // failed record is kept (existing behaviour): the retry is
        // a new record next to it, and the user can delete the old
        // one with the delete button.
        startSystemDownload(
            context, source, null, null, entry.mediaType,
            referer = retryReferer
        )
        refreshTick++
    }
    // System back returns to the browser (v0.37.0).
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("下载") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 24.dp
            )
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("全部", "进行中", "已完成", "失败")
                        .forEachIndexed { index, label ->
                            FilterChip(
                                selected = statusFilter == index,
                                onClick = { statusFilter = index },
                                label = { Text(label) }
                            )
                        }
                }
            }
            if (entries.isEmpty()) {
                item {
                    EmptyState(Icons.Filled.Download, "还没有下载记录。")
                }
            } else if (visibleEntries.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Download,
                        when (statusFilter) {
                            1 -> "没有进行中的下载"
                            2 -> "没有已完成的下载"
                            3 -> "没有失败的下载"
                            else -> "还没有下载记录。"
                        }
                    )
                }
            }
            items(visibleEntries, key = { it.id }) { entry ->
                ListItem(
                    headlineContent = { Text(entry.title, maxLines = 2) },
                    supportingContent = {
                        val parts = mutableListOf(entry.statusText, entry.sizeText)
                        if (!entry.mediaType.isNullOrBlank()) {
                            parts.add(entry.mediaType)
                        }
                        Text(parts.joinToString(" · "))
                    },
                    leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                    trailingContent = {
                        Row {
                            if (entry.status ==
                                android.app.DownloadManager.STATUS_FAILED
                            ) {
                                IconButton(onClick = { retryDownload(entry) }) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = "重新下载"
                                    )
                                }
                            }
                            IconButton(onClick = { pendingDeleteEntry = entry }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "删除这条记录"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (entry.status !=
                                android.app.DownloadManager.STATUS_SUCCESSFUL
                            ) {
                                android.widget.Toast.makeText(
                                    context,
                                    when (entry.status) {
                                        android.app.DownloadManager.STATUS_FAILED ->
                                            "这个文件下载失败了，点右边的按钮可以重新下载"
                                        android.app.DownloadManager.STATUS_RUNNING ->
                                            "还在下载中，好了以后再打开"
                                        android.app.DownloadManager.STATUS_PAUSED ->
                                            "这个下载暂停了，等它继续"
                                        else -> "这个下载还在排队"
                                    },
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@clickable
                            }
                            try {
                                val manager = context.getSystemService(
                                    android.content.Context.DOWNLOAD_SERVICE
                                ) as android.app.DownloadManager
                                val uri = manager.getUriForDownloadedFile(entry.id)
                                if (uri != null) {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW
                                    )
                                        .setDataAndType(
                                            uri, manager.getMimeTypeForDownloadedFile(entry.id)
                                        )
                                        .addFlags(
                                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        )
                                        .addFlags(
                                            android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                        )
                                    context.startActivity(intent)
                                }
                            } catch (t: Throwable) {
                                android.widget.Toast.makeText(
                                    context, "这个文件暂时打不开",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                )
                HorizontalDivider()
            }
            item {
                // Duplicate-name and list-scope notes, so a renamed
                // or Gecko-side download is never mistaken for a
                // lost file: DownloadManager never overwrites an
                // existing file (a repeated download is either
                // saved under an adjusted name or reported as
                // "同名文件已经存在"), and Gecko-tab downloads are
                // saved straight into the system Downloads folder
                // by the app itself, so they do not appear in this
                // DownloadManager-backed list.
                Text(
                    text = "说明：同一个文件重复下载时，原来的文件不会被覆盖——" +
                        "系统可能给新文件自动改名保存，也可能提示「同名文件已经存在」。" +
                        "火狐标签下载的文件由浏览器直接存进系统下载文件夹，不出现在这个列表里。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClearDataScreen(
    geckoStarted: Boolean,
    onBack: () -> Unit,
    onClear: (Boolean, Boolean, Boolean) -> Unit,
    onClearSite: (String) -> Unit
) {
    var clearHistorySel by remember { mutableStateOf(true) }
    var clearWebView by remember { mutableStateOf(true) }
    var clearGecko by remember { mutableStateOf(true) }
    var showConfirm by remember { mutableStateOf(false) }
    var doneText by remember { mutableStateOf<String?>(null) }
    var siteInput by remember { mutableStateOf("") }
    var siteDoneText by remember { mutableStateOf<String?>(null) }
    // Storage usage of the app's own private directories, measured
    // on a background thread (recursive file walk). A null entry
    // means that directory does not exist / cannot be measured, and
    // the card shows "—" for it instead of inventing a number.
    val clearDataContext = LocalContext.current
    var storageSizes by remember {
        mutableStateOf<Triple<Long?, Long?, Long?>?>(null)
    }
    var storageTick by remember { mutableStateOf(0) }
    LaunchedEffect(storageTick) {
        storageSizes = kotlinx.coroutines.withContext(
            kotlinx.coroutines.Dispatchers.IO
        ) {
            val cache = dirSizeBytes(clearDataContext.cacheDir)
            val dataDir = java.io.File(clearDataContext.applicationInfo.dataDir)
            val webviewData = dirSizeBytes(java.io.File(dataDir, "app_webview"))
            val geckoDirs = try {
                clearDataContext.filesDir.listFiles()?.filter {
                    it.isDirectory &&
                        (it.name.contains("gecko", ignoreCase = true) ||
                            it.name.contains("mozilla", ignoreCase = true))
                } ?: emptyList()
            } catch (t: Throwable) {
                emptyList()
            }
            var geckoData: Long? = if (geckoDirs.isEmpty()) null else 0L
            for (dir in geckoDirs) {
                val size = dirSizeBytes(dir)
                if (size != null) geckoData = (geckoData ?: 0L) + size
            }
            Triple(cache, webviewData, geckoData)
        }
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("确认清除？") },
            text = {
                Text("勾选的数据会被直接删掉，没法恢复。网站登录状态清掉后要重新登录。")
            },
            confirmButton = {
                Button(onClick = {
                    showConfirm = false
                    onClear(clearHistorySel, clearWebView, clearGecko)
                    doneText = "已清除勾选的数据。"
                    storageTick++
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("取消") }
            }
        )
    }
    // System back returns to the settings page this screen was
    // opened from (v0.37.0) — onBack already points there.
    BackHandler { onBack() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("清除浏览数据") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            "存储占用",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        val sizes = storageSizes
                        if (sizes == null) {
                            Text(
                                "正在计算…",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        } else {
                            ListItem(
                                headlineContent = { Text("应用缓存") },
                                trailingContent = {
                                    Text(formatStorageSize(sizes.first))
                                }
                            )
                            ListItem(
                                headlineContent = { Text("网页数据（系统 WebView）") },
                                trailingContent = {
                                    Text(formatStorageSize(sizes.second))
                                }
                            )
                            ListItem(
                                headlineContent = { Text("火狐数据") },
                                trailingContent = {
                                    Text(formatStorageSize(sizes.third))
                                }
                            )
                        }
                        Text(
                            "这里数的是应用自己文件夹里能数到的部分；" +
                                "用系统下载下的文件存在公共下载文件夹里，不在这里面。" +
                                "显示「—」的那项是这次没有找到对应的文件夹。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Text(
                            "缓存超过 150 MB 时会在启动时自动清理（不影响登录状态）。" +
                                "火狐的缓存由火狐内核自己管理，有自己的容量上限，这里不会动它。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            item {
                Text(
                    "想清哪样就勾哪样，最后点下面的按钮一次清掉。书签不在这里清，在书签页单独删。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("历史记录") },
                    supportingContent = { Text("应用里记下的浏览历史会全部清掉。") },
                    trailingContent = {
                        Switch(
                            checked = clearHistorySel,
                            onCheckedChange = { clearHistorySel = it }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("系统 WebView 的 Cookie 和网站数据") },
                    supportingContent = { Text("清完后用系统 WebView 打开的网站都要重新登录。") },
                    trailingContent = {
                        Switch(
                            checked = clearWebView,
                            onCheckedChange = { clearWebView = it }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("火狐的 Cookie、网站数据和缓存") },
                    supportingContent = {
                        Text(
                            if (geckoStarted) {
                                "清完后用火狐打开的网站都要重新登录。"
                            } else {
                                "火狐内核这次还没启动过，没有数据需要清。"
                            }
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = clearGecko && geckoStarted,
                            onCheckedChange = { clearGecko = it },
                            enabled = geckoStarted
                        )
                    }
                )
            }
            item {
                Button(
                    onClick = { showConfirm = true },
                    enabled = clearHistorySel || clearWebView || (clearGecko && geckoStarted),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清除所选数据")
                }
            }
            if (doneText != null) {
                item { Text(doneText!!) }
            }
            item {
                HorizontalDivider()
            }
            item {
                Text("按网站清除", style = MaterialTheme.typography.titleMedium)
                Text(
                    "只想清某一个网站时用这里：输入域名，清掉火狐里这个网站的 Cookie 和网站数据，" +
                        "并删掉本机保存的这个网站的历史记录。系统 WebView 的网站数据没法按网站单独清，" +
                        "只能用上面那项整清（系统 WebView 不支持按网站清除）。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                OutlinedTextField(
                    value = siteInput,
                    onValueChange = { siteInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("网站域名") },
                    placeholder = { Text("如 example.com") }
                )
            }
            item {
                OutlinedButton(
                    onClick = {
                        val host = hostOf(siteInput.trim())
                            ?: hostOf("https://" + siteInput.trim())
                        if (host == null) {
                            siteDoneText = "这个域名看不懂，检查一下再试。"
                        } else {
                            onClearSite(host)
                            storageTick++
                            siteDoneText = if (geckoStarted) {
                                "已清除 $host 在火狐里的数据和本机历史。"
                            } else {
                                "已删掉 $host 的本机历史。火狐这次还没启动过，" +
                                    "没有它的数据需要清。"
                            }
                        }
                    },
                    enabled = siteInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清除这个网站的数据")
                }
            }
            if (siteDoneText != null) {
                item { Text(siteDoneText!!) }
            }
            item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    store: Store,
    refreshTick: Int = 0,
    onOpenReleaseUrl: (String) -> Unit = {},
    onBack: () -> Unit,
    onClearHistory: () -> Unit,
    onRemoveCompatSite: (String) -> Unit,
    onOpenClearData: () -> Unit,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onSearchEngineChange: () -> Unit = {},
    onTextZoomChange: (Int) -> Unit,
    onWebDarkChange: (Boolean) -> Unit,
    onThirdPartyCookiesChange: (Boolean) -> Unit = {}
) {
    // Every state below mirrors a stored setting. In-page edits
    // write the local state and the store together, so within this
    // page they cannot drift — except when the store is rewritten
    // wholesale behind this page's back (a backup import is launched
    // from this very page). Those mirrors are therefore keyed on
    // refreshTick: BrowserApp bumps it after such a rewrite and the
    // states re-read the store, instead of showing stale values
    // until the page is re-entered (v0.33.0).
    var mainEngine by remember(refreshTick) { mutableStateOf(store.mainEngine) }
    var incognitoEngine by remember(refreshTick) { mutableStateOf(store.incognitoEngine) }
    var searchEngine by remember(refreshTick) { mutableStateOf(store.searchEngine) }
    var showCustomSearchDialog by remember { mutableStateOf(false) }
    var customSearchName by remember(refreshTick) { mutableStateOf(store.customSearchName) }
    var customSearchTemplate by remember(refreshTick) { mutableStateOf(store.customSearchTemplate) }
    var customSearchError by remember { mutableStateOf<String?>(null) }
    var adBlock by remember(refreshTick) { mutableStateOf(store.adBlockEnabled) }
    var adHideSlots by remember(refreshTick) { mutableStateOf(store.adHideSlots) }
    var totalBlocked by remember(refreshTick) { mutableStateOf(store.totalBlocked) }
    var textZoom by remember(refreshTick) { mutableStateOf(store.textZoomPercent) }
    var webDark by remember(refreshTick) { mutableStateOf(store.webDarkMode) }
    var startupRestore by remember(refreshTick) { mutableStateOf(store.startupRestore) }
    var allowThirdPartyCookies by remember(refreshTick) {
        mutableStateOf(store.allowThirdPartyCookies)
    }
    var downloadMode by remember(refreshTick) { mutableStateOf(store.downloadMode) }
    var historyRetention by remember(refreshTick) { mutableStateOf(store.historyRetention) }
    var themeMode by remember(refreshTick) { mutableStateOf(store.themeMode) }
    // Settings is a two-level menu now: a home list of six
    // categories, each opening its own sub-page. Back from a
    // sub-page returns here; back from home leaves settings.
    var settingsPage by remember { mutableStateOf("home") }
    BackHandler(enabled = settingsPage != "home") { settingsPage = "home" }
    // From the settings home the same back key leaves settings for
    // the browser (v0.37.0): before this, no handler was enabled on
    // the home page and the system finished the activity instead.
    // The two handlers are mutually exclusive by enabled.
    BackHandler(enabled = settingsPage == "home") { onBack() }
    var compatList by remember(refreshTick) { mutableStateOf(store.loadCompatSites().sorted()) }
    var showCleared by remember { mutableStateOf(false) }
    var defaultTick by remember { mutableStateOf(0) }
    val settingsContext = LocalContext.current
    val roleManager = remember {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            settingsContext.getSystemService(android.app.role.RoleManager::class.java)
        } else {
            null
        }
    }
    val isDefaultBrowser = remember(defaultTick) {
        try {
            roleManager?.isRoleHeld(android.app.role.RoleManager.ROLE_BROWSER) ?: false
        } catch (t: Throwable) {
            false
        }
    }
    val versionName = remember {
        try {
            settingsContext.packageManager
                .getPackageInfo(settingsContext.packageName, 0).versionName ?: ""
        } catch (t: Throwable) {
            ""
        }
    }
    // Manual update check: nothing touches the network until the
    // user taps the button — no automatic checks, no polling, and
    // the app never installs anything itself.
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateAvailable by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showUpToDate by remember { mutableStateOf(false) }
    val settingsScope = rememberCoroutineScope()
    fun checkForUpdate() {
        if (checkingUpdate) return
        checkingUpdate = true
        settingsScope.launch {
            val latest = kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.IO
            ) {
                try {
                    val conn = java.net.URL(
                        "https://api.github.com/repos/liyuzhou79-star/TuxunBrowse/releases?per_page=5"
                    ).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 10_000
                    conn.readTimeout = 10_000
                    conn.setRequestProperty(
                        "Accept", "application/vnd.github+json"
                    )
                    conn.setRequestProperty("User-Agent", "TuxunBrowse")
                    if (conn.responseCode !in 200..299) {
                        null
                    } else {
                        val raw = conn.inputStream.bufferedReader().readText()
                        val arr = org.json.JSONArray(raw)
                        val pairs = (0 until arr.length()).map { i ->
                            val o = arr.getJSONObject(i)
                            o.optString("tag_name") to o.optString("html_url")
                        }
                        pickLatestRelease(pairs)
                    }
                } catch (t: Throwable) {
                    null
                }
            }
            checkingUpdate = false
            when {
                latest == null -> android.widget.Toast.makeText(
                    settingsContext, "检查失败，请检查网络",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                compareVersionNames(latest.first, versionName) > 0 ->
                    updateAvailable = latest
                else -> showUpToDate = true
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (settingsPage) {
                            "engine" -> "内核与搜索引擎"
                            "appearance" -> "外观与字体"
                            "privacy" -> "隐私与数据"
                            "download" -> "下载"
                            "adblock" -> "广告拦截"
                            "about" -> "关于"
                            else -> "设置"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (settingsPage != "home") {
                            settingsPage = "home"
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 24.dp
            )
        ) {
            if (settingsPage == "home") {
                item {
                    Text(
                        "点一个分类进去改，改完点左上角返回。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(
                    listOf(
                        Triple("engine", "内核与搜索引擎", Icons.Filled.Language),
                        Triple("appearance", "外观与字体", Icons.Filled.Palette),
                        Triple("privacy", "隐私与数据", Icons.Filled.Lock),
                        Triple("download", "下载", Icons.Filled.Download),
                        Triple("adblock", "广告拦截", Icons.Filled.VisibilityOff),
                        Triple("about", "关于", Icons.Filled.Info)
                    )
                ) { (key, label, icon) ->
                    ElevatedCard(
                        onClick = { settingsPage = key },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = { Text(label) },
                            leadingContent = {
                                Icon(icon, contentDescription = null)
                            },
                            trailingContent = {
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = "进入"
                                )
                            }
                        )
                    }
                }
            }
            if (settingsPage == "engine") {
            item {
                Text("主内核", style = MaterialTheme.typography.titleMedium)
                Text(
                    "新建的普通标签页用这个内核打开。已经打开的标签不会中途换内核，要换就点地址栏旁边的切换内核按钮。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(Engine.entries) { engine ->
                ElevatedCard(
                    onClick = {
                        mainEngine = engine
                        store.mainEngine = engine
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(engine.label) },
                        trailingContent = {
                            RadioButton(
                                selected = mainEngine == engine,
                                onClick = {
                                    mainEngine = engine
                                    store.mainEngine = engine
                                }
                            )
                        }
                    )
                }
            }
            item {
                Text("无痕内核", style = MaterialTheme.typography.titleMedium)
                Text(
                    "无痕标签单独用这个内核，和主内核互不影响。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(Engine.entries) { engine ->
                ElevatedCard(
                    onClick = {
                        incognitoEngine = engine
                        store.incognitoEngine = engine
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(engine.label) },
                        trailingContent = {
                            RadioButton(
                                selected = incognitoEngine == engine,
                                onClick = {
                                    incognitoEngine = engine
                                    store.incognitoEngine = engine
                                }
                            )
                        }
                    )
                }
            }
            item {
                Text("搜索引擎", style = MaterialTheme.typography.titleMedium)
                Text(
                    "在地址栏和首页搜索框里输入的词，用这个搜索引擎去搜。默认百度，国内能直接打开。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(SearchEngine.entries) { engine ->
                ElevatedCard(
                    onClick = {
                        if (engine == SearchEngine.CUSTOM) {
                            // Picking "custom" opens the editor; the
                            // engine only becomes active once a valid
                            // name + template are saved there.
                            customSearchName = store.customSearchName
                            customSearchTemplate = store.customSearchTemplate
                            customSearchError = null
                            showCustomSearchDialog = true
                        } else {
                            searchEngine = engine
                            store.searchEngine = engine
                            onSearchEngineChange()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(engine.label) },
                        supportingContent = if (engine == SearchEngine.CUSTOM &&
                            store.customSearchName.isNotBlank()
                        ) {
                            { Text(store.customSearchName) }
                        } else {
                            null
                        },
                        trailingContent = {
                            RadioButton(
                                selected = searchEngine == engine,
                                onClick = {
                                    if (engine == SearchEngine.CUSTOM) {
                                        customSearchName = store.customSearchName
                                        customSearchTemplate = store.customSearchTemplate
                                        customSearchError = null
                                        showCustomSearchDialog = true
                                    } else {
                                        searchEngine = engine
                                        store.searchEngine = engine
                                        onSearchEngineChange()
                                    }
                                }
                            )
                        }
                    )
                }
            }
            if (searchEngine == SearchEngine.CUSTOM) {
                item {
                    Text(
                        if (isValidCustomSearchTemplate(store.customSearchTemplate)) {
                            "正在用自定义搜索引擎「" +
                                store.customSearchName.ifBlank { "自定义" } +
                                "」。点上面的「自定义」可以修改。"
                        } else {
                            "自定义搜索引擎还没填好：先点上面的「自定义」填写，" +
                                "没填好之前搜索会先用百度。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Text("兼容模式记住的网站", style = MaterialTheme.typography.titleMedium)
                Text(
                    "这些网站在火狐内核下打开时会自动改用兼容模式（系统 WebView）打开。删掉某个网站，它就恢复成用你选的主内核打开。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (compatList.isEmpty()) {
                item {
                    Text(
                        "还没有。在火狐标签里打开某个网站觉得不对劲时，点地址栏旁边的切换内核按钮，把当前标签切换到系统 WebView 并勾选记住，就会记到这里。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(compatList, key = { it }) { site ->
                    ListItem(
                        headlineContent = { Text(site) },
                        trailingContent = {
                            IconButton(onClick = {
                                onRemoveCompatSite(site)
                                compatList = compatList - site
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除兼容记忆")
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
            }
            if (settingsPage == "adblock") {
            item {
                Text("广告拦截", style = MaterialTheme.typography.titleMedium)
            }

                item {
                    Text(
                        if (adBlock) "广告拦截已开" else "广告拦截已关",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            item {
                ListItem(
                    headlineContent = { Text("拦截广告和跟踪请求") },
                    supportingContent = {
                        Text("系统 WebView 按内置名单拦截常见广告域名，立即生效；火狐内核用自带的反跟踪，重启应用后完全生效。关掉后所有网站原样加载。")
                    },
                    trailingContent = {
                        Switch(
                            checked = adBlock,
                            onCheckedChange = {
                                adBlock = it
                                store.adBlockEnabled = it
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    modifier = Modifier.alpha(if (adBlock) 1f else 0.5f),
                    headlineContent = { Text("隐藏页面广告位") },
                    supportingContent = {
                        Text("把网页上拦掉广告后剩下的空广告位也藏起来。只认特征很明确的广告容器，宁可漏掉也不误藏正文。此项仅对系统 WebView 标签生效；火狐标签由内核自带的反跟踪处理。")
                    },
                    trailingContent = {
                        Switch(
                            checked = adHideSlots,
                            enabled = adBlock,
                            onCheckedChange = {
                                adHideSlots = it
                                store.adHideSlots = it
                            }
                        )
                    }
                )
            }
            item {
                Text(
                    "内置拦截名单共 ${AdBlocker.blockedHostCount} 个广告/跟踪域名。" +
                        "系统 WebView 标签按这份名单拦截，单个页面拦下了几个可以在网站信息里看；" +
                        "火狐标签用内核自带的反跟踪，没有个数统计。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "累计已拦截 $totalBlocked 个（系统 WebView 标签统计）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        store.resetTotalBlocked()
                        totalBlocked = 0L
                        android.widget.Toast.makeText(
                            settingsContext, "累计拦截数已清零",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }) {
                        Text("清零")
                    }
                }
            }
            item {
                Text(
                    "这个累计数只统计系统 WebView 标签拦下的请求；火狐是内核级拦截，没法计数，不算在里面。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }
            if (settingsPage == "download") {
            item {
                Text("下载", style = MaterialTheme.typography.titleMedium)
                Text(
                    "在网页里点到下载链接、或者用资源嗅探下载文件时，文件交给谁来下。" +
                        "TuxunDown 是多线程下载器，下大文件更快，需要另外安装这个 App；" +
                        "没装的时候会自动改用系统下载，不会下不了。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(DownloadMode.entries) { mode ->
                ElevatedCard(
                    onClick = {
                        downloadMode = mode
                        store.downloadMode = mode
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(mode.label) },
                        trailingContent = {
                            RadioButton(
                                selected = downloadMode == mode,
                                onClick = {
                                    downloadMode = mode
                                    store.downloadMode = mode
                                }
                            )
                        }
                    )
                }
            }
            item {
                Text(
                    "火狐标签的下载由浏览器直接保存，不走此设置。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            }
            if (settingsPage == "appearance") {
            item {
                Text("外观", style = MaterialTheme.typography.titleMedium)
                Text(
                    "应用界面用浅色还是深色。选完立刻生效，所有页面一起变。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(ThemeMode.entries) { mode ->
                ElevatedCard(
                    onClick = {
                        themeMode = mode
                        store.themeMode = mode
                        onThemeModeChange(mode)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("界面主题 · ${mode.label}") },
                        trailingContent = {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    themeMode = mode
                                    store.themeMode = mode
                                    onThemeModeChange(mode)
                                }
                            )
                        }
                    )
                }
            }
            item {
                Text(
                    "注意：这里只管应用自己的界面（工具栏、设置、首页这些）。" +
                        "网页内容是深色还是浅色由网页自己决定，和下面的「网页深色模式」设置有关，" +
                        "跟这里不是一回事，别弄混。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Text("显示", style = MaterialTheme.typography.titleMedium)
                Text(
                    "网页文字大小对两个内核都生效，改完已经打开的网页立刻跟着变。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(
                listOf(85 to "小", 100 to "标准", 115 to "大", 130 to "特大")
            ) { (percent, label) ->
                ElevatedCard(
                    onClick = {
                        textZoom = percent
                        store.textZoomPercent = percent
                        onTextZoomChange(percent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("文字大小 · $label（$percent%）") },
                        trailingContent = {
                            RadioButton(
                                selected = textZoom == percent,
                                onClick = {
                                    textZoom = percent
                                    store.textZoomPercent = percent
                                    onTextZoomChange(percent)
                                }
                            )
                        }
                    )
                }
            }
            item {
                ListItem(
                    headlineContent = { Text("网页深色模式") },
                    supportingContent = {
                        Text("让网页按深色显示。系统 WebView 标签在 Android 13 以上生效；火狐内核的网页深浅跟随系统深色模式，系统开了深色它就是深色。")
                    },
                    trailingContent = {
                        Switch(
                            checked = webDark,
                            onCheckedChange = {
                                webDark = it
                                store.webDarkMode = it
                                onWebDarkChange(it)
                            }
                        )
                    }
                )
            }
            }
            if (settingsPage == "privacy") {
            item {
                Text("启动", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ListItem(
                    headlineContent = { Text("启动时恢复上次的标签页") },
                    supportingContent = {
                        Text("打开后接着上次没看完的页面继续看。关掉它，每次打开都从首页开始。无痕标签永远不会被恢复。")
                    },
                    trailingContent = {
                        Switch(
                            checked = startupRestore,
                            onCheckedChange = {
                                startupRestore = it
                                store.startupRestore = it
                            }
                        )
                    }
                )
            }
            }
            if (settingsPage == "engine") {
            item {
                Text("默认浏览器", style = MaterialTheme.typography.titleMedium)
                Text(
                    "设成默认浏览器后，在微信、短信等其他应用里点到的网页链接会用 TuxunBrowse 打开。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                ListItem(
                    headlineContent = {
                        Text(
                            if (isDefaultBrowser) {
                                "已经是默认浏览器了"
                            } else {
                                "还不是默认浏览器"
                            }
                        )
                    },
                    supportingContent = {
                        Text(
                            if (isDefaultBrowser) {
                                "别的 App 里的链接会用 TuxunBrowse 打开。"
                            } else {
                                "点右边按钮去系统里选一下；回来后点「重新检测」刷新状态。"
                            }
                        )
                    },
                    trailingContent = if (!isDefaultBrowser) {
                        {
                            Button(onClick = {
                                try {
                                    if (roleManager != null &&
                                        roleManager.isRoleAvailable(
                                            android.app.role.RoleManager.ROLE_BROWSER
                                        )
                                    ) {
                                        settingsContext.startActivity(
                                            roleManager.createRequestRoleIntent(
                                                android.app.role.RoleManager.ROLE_BROWSER
                                            )
                                        )
                                    } else {
                                        settingsContext.startActivity(
                                            android.content.Intent(
                                                android.provider.Settings
                                                    .ACTION_MANAGE_DEFAULT_APPS_SETTINGS
                                            )
                                        )
                                    }
                                } catch (t: Throwable) {
                                    android.widget.Toast.makeText(
                                        settingsContext,
                                        "没能打开系统设置，请到系统设置的应用管理里手动设",
                                        android.widget.Toast.LENGTH_LONG
                                    ).show()
                                }
                            }) {
                                Text("去设置")
                            }
                        }
                    } else {
                        null
                    }
                )
            }
            item {
                TextButton(onClick = { defaultTick++ }) {
                    Text("重新检测默认浏览器状态")
                }
            }
            item {
                Text("两个内核的差别", style = MaterialTheme.typography.titleMedium)
                Text(
                    "有些功能只有一边有，选内核时可以对照着看。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            "只有系统 WebView 有",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(
                                start = 16.dp, top = 16.dp, end = 16.dp
                            )
                        )
                        ListItem(headlineContent = { Text("资源嗅探") })
                        ListItem(headlineContent = { Text("网页截图与长截图") })
                        ListItem(headlineContent = { Text("标签预览图") })
                        ListItem(headlineContent = { Text("阅读模式") })
                        ListItem(headlineContent = { Text("页内查找计数") })
                        ListItem(headlineContent = { Text("本页拦截计数") })
                        ListItem(headlineContent = { Text("广告位隐藏") })
                        ListItem(headlineContent = { Text("打印 / 存为 PDF") })
                        HorizontalDivider()
                        Text(
                            "火狐的特点",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(
                                start = 16.dp, top = 16.dp, end = 16.dp
                            )
                        )
                        ListItem(
                            headlineContent = { Text("内核级反跟踪") },
                            supportingContent = {
                                Text("由内核拦截跟踪，不靠应用的名单")
                            }
                        )
                        ListItem(
                            headlineContent = { Text("独立的 Cookie 罐") },
                            supportingContent = {
                                Text("和系统 WebView 分开存；无痕是真隔离")
                            }
                        )
                        HorizontalDivider()
                        Text(
                            "两个内核都有",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(
                                start = 16.dp, top = 16.dp, end = 16.dp
                            )
                        )
                        ListItem(
                            headlineContent = {
                                Text("文件上传、相机和麦克风、网站登录（基础认证）")
                            }
                        )
                        ListItem(
                            headlineContent = { Text("下载") },
                            supportingContent = {
                                Text(
                                    "通路不同：系统 WebView 的下载按设置里的下载方式来；" +
                                        "火狐标签的下载由浏览器直接保存到系统下载文件夹"
                                )
                            }
                        )
                    }
                }
            }
            }
            if (settingsPage == "privacy") {
            item {
                Text("隐私", style = MaterialTheme.typography.titleMedium)
                Text(
                    "历史记录保留多久。超过期限的记录会在下次打开应用时自动清掉。" +
                        "无痕标签的访问本来就不记历史，不受这个设置影响。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(HistoryRetention.entries) { retention ->
                ElevatedCard(
                    onClick = {
                        historyRetention = retention
                        store.historyRetention = retention
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("历史记录 · ${retention.label}") },
                        trailingContent = {
                            RadioButton(
                                selected = historyRetention == retention,
                                onClick = {
                                    historyRetention = retention
                                    store.historyRetention = retention
                                }
                            )
                        }
                    )
                }
            }
            item {
                Text("网站登录", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ListItem(
                    headlineContent = {
                        Text("允许第三方 Cookie（系统 WebView 标签）")
                    },
                    supportingContent = {
                        Text(
                            "网页里嵌着的其他网站（比如跳转登录、网页版支付）" +
                                "需要用到第三方 Cookie。这个开关默认开着：" +
                                "浏览器里挡掉第三方 Cookie，打断登录的麻烦比好处大，" +
                                "电脑上的浏览器默认也是允许的。关掉它更防跟踪，" +
                                "但有些网站会登录不上。改了之后，已经打开的" +
                                "系统 WebView 标签也会马上按新设置来；新开的标签同样生效。" +
                                "火狐标签不受这个开关影响：火狐的第三方 Cookie " +
                                "由火狐内核自带的反跟踪决定（只拦已知的跟踪网站，" +
                                "不是全拦）。"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = allowThirdPartyCookies,
                            onCheckedChange = {
                                allowThirdPartyCookies = it
                                store.allowThirdPartyCookies = it
                                onThirdPartyCookiesChange(it)
                            }
                        )
                    }
                )
            }
            item {
                Text("数据", style = MaterialTheme.typography.titleMedium)
            }
            item {
                OutlinedButton(
                    onClick = onOpenClearData,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清除浏览数据（可选细项）")
                }
            }
            item {
                OutlinedButton(
                    onClick = { onClearHistory(); showCleared = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清除历史记录")
                }
            }
            if (showCleared) {
                item { Text("历史记录已清除。") }
            }
            item {
                Text("备份与恢复", style = MaterialTheme.typography.titleMedium)
                Text(
                    "把书签、历史、阅读列表、首页快捷方式和主要设置存成一个备份文件，" +
                        "放在下载文件夹里；换手机或重装后，用导入把它们合并回来" +
                        "（重复的会自动跳过，设置会被备份里的覆盖，导入前会先让你确认）。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            item {
                OutlinedButton(
                    onClick = onExportBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导出备份到下载文件夹")
                }
            }
            item {
                OutlinedButton(
                    onClick = onImportBackup,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("从备份文件导入")
                }
            }
            }
            if (settingsPage == "about") {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关于 TuxunBrowse", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "版本 $versionName。免费开源的双内核浏览器：火狐内核（GeckoView）与系统 WebView 由用户自选，" +
                                "首页是应用自己的页面（搜索框、快捷方式和常用书签快捷入口），不拿任何网站的页面冒充首页。" +
                                "两个内核的登录状态互不相通，这是双内核的固有代价。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = { checkForUpdate() },
                            enabled = !checkingUpdate,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Text(if (checkingUpdate) "正在检查…" else "检查更新")
                        }
                        Text(
                            "只有点这个按钮时才会联网检查，平时不会自动检查、也不会自己下载安装。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }            }

        }
    }
    val newRelease = updateAvailable
    if (newRelease != null) {
        AlertDialog(
            onDismissRequest = { updateAvailable = null },
            title = { Text("有新版本") },
            text = {
                Text(
                    "发现新版本 ${newRelease.first.removePrefix("v")}（你现在用的是 $versionName）。" +
                        "点「打开下载页」会打开这个版本的下载页面，下载后需要你手动安装。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    updateAvailable = null
                    if (newRelease.second.isNotBlank()) {
                        onOpenReleaseUrl(newRelease.second)
                    }
                }) { Text("打开下载页") }
            },
            dismissButton = {
                TextButton(onClick = { updateAvailable = null }) {
                    Text("以后再说")
                }
            }
        )
    }
    if (showUpToDate) {
        AlertDialog(
            onDismissRequest = { showUpToDate = false },
            title = { Text("已是最新版本") },
            text = { Text("你现在用的 $versionName 就是最新版，不用更新。") },
            confirmButton = {
                Button(onClick = { showUpToDate = false }) { Text("知道了") }
            }
        )
    }
    // Custom search engine editor: a name plus a URL template. The
    // template must contain %s (where the search words go) and start
    // with http:// or https://; anything else is explained in plain
    // language and not saved.
    if (showCustomSearchDialog) {
        // Opens focused on the first field with the keyboard up.
        val customSearchFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { customSearchFocusRequester.requestFocus() }
        AlertDialog(
            onDismissRequest = { showCustomSearchDialog = false },
            title = { Text("自定义搜索引擎") },
            text = {
                LazyColumn {
                    item {
                        OutlinedTextField(
                            value = customSearchName,
                            onValueChange = { customSearchName = it },
                            label = { Text("名称") },
                            placeholder = { Text("比如：我的搜索") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(customSearchFocusRequester),
                            singleLine = true
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = customSearchTemplate,
                            onValueChange = { customSearchTemplate = it },
                            label = { Text("网址模板") },
                            placeholder = {
                                Text("比如：https://example.com/search?q=%s")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            singleLine = true
                        )
                    }
                    item {
                        Text(
                            "模板里必须有 %s，它会被换成你要搜的词；" +
                                "网址要以 http:// 或 https:// 开头。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    val error = customSearchError
                    if (error != null) {
                        item {
                            Text(
                                error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val template = customSearchTemplate.trim()
                    customSearchError = when {
                        !template.contains("%s") ->
                            "模板里没有 %s，搜的时候没地方放要搜的词，请补上。"
                        !template.startsWith("http://") &&
                            !template.startsWith("https://") ->
                            "网址要以 http:// 或 https:// 开头，请改一下。"
                        else -> null
                    }
                    if (customSearchError == null) {
                        store.customSearchName = customSearchName.trim()
                        store.customSearchTemplate = template
                        searchEngine = SearchEngine.CUSTOM
                        store.searchEngine = SearchEngine.CUSTOM
                        onSearchEngineChange()
                        showCustomSearchDialog = false
                    }
                }) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSearchDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
