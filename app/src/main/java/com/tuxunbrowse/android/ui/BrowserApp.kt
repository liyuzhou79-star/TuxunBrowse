package com.tuxunbrowse.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.SavedTab
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.Store
import com.tuxunbrowse.android.data.bookmarksFromJson
import com.tuxunbrowse.android.data.bookmarksToJson
import com.tuxunbrowse.android.data.hostOf
import com.tuxunbrowse.android.data.isCompatHost
import com.tuxunbrowse.android.data.resolveInput
import com.tuxunbrowse.android.engine.BrowserTab
import com.tuxunbrowse.android.engine.GeckoHolder
import com.tuxunbrowse.android.engine.TabContent
import kotlinx.coroutines.flow.collect

private enum class Screen { PICKER, BROWSER, BOOKMARKS, HISTORY, SETTINGS, DOWNLOADS, CLEAR_DATA }

private data class ClosedTabInfo(
    val engine: Engine,
    val incognito: Boolean,
    val url: String,
    val desktopMode: Boolean
)

@Composable
fun BrowserApp() {
    val context = LocalContext.current
    val store = remember { Store(context) }
    var screen by remember {
        mutableStateOf(if (store.mainEngineChosen) Screen.BROWSER else Screen.PICKER)
    }
    val tabs = remember { mutableStateListOf<BrowserTab>() }
    var activeTabId by remember { mutableStateOf(-1) }
    val bookmarks = remember { mutableStateListOf<Bookmark>().apply { addAll(store.loadBookmarks()) } }
    val history = remember { mutableStateListOf<HistoryItem>().apply { addAll(store.loadHistory()) } }
    val compatSites = remember {
        mutableStateListOf<String>().apply { addAll(store.loadCompatSites()) }
    }
    var linkMenuTarget by remember { mutableStateOf<String?>(null) }
    var lastClosedTab by remember { mutableStateOf<ClosedTabInfo?>(null) }
    var startupDone by remember { mutableStateOf(false) }
    var fullscreenView by remember { mutableStateOf<android.view.View?>(null) }
    var geckoFullscreen by remember { mutableStateOf(false) }

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

    // Gecko tabs report remembered compat sites through this handler; it is
    // wired up below once the tab-replacement helper exists.
    var compatRedirectHandler: (String) -> Unit = {}

    fun makeTab(
        engine: Engine,
        incognito: Boolean,
        url: String,
        desktopMode: Boolean = false
    ): BrowserTab =
        BrowserTab(
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
            onLinkContext = { link -> linkMenuTarget = link },
            textZoomProvider = { store.textZoomPercent },
            webDarkProvider = { store.webDarkMode },
            onFullscreenShow = { view -> fullscreenView = view },
            onFullscreenHide = { fullscreenView = null },
            onGeckoFullscreen = { geckoFullscreen = it }
        )

    fun effectiveEngine(engine: Engine, url: String): Engine =
        if (engine == Engine.GECKO && url.startsWith("http") &&
            isCompatHost(hostOf(url), compatSites.toSet())
        ) Engine.WEBVIEW else engine

    fun newTab(
        engine: Engine,
        incognito: Boolean,
        url: String = HOME_URL,
        desktopMode: Boolean = false
    ): BrowserTab {
        val tab = makeTab(effectiveEngine(engine, url), incognito, url, desktopMode)
        tabs.add(tab)
        activeTabId = tab.id
        return tab
    }

    fun recreateTab(tab: BrowserTab, newEngine: Engine, desktopMode: Boolean): BrowserTab {
        val idx = tabs.indexOf(tab)
        if (idx < 0) return tab
        if (tab.engine == newEngine && tab.desktopMode == desktopMode) return tab
        val replacement = makeTab(newEngine, tab.incognito, tab.url, desktopMode)
        tabs[idx] = replacement
        tab.destroy()
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

    fun closeTab(tab: BrowserTab) {
        val idx = tabs.indexOf(tab)
        val wasIncognito = tab.incognito
        lastClosedTab = ClosedTabInfo(tab.engine, tab.incognito, tab.url, tab.desktopMode)
        tab.destroy()
        tabs.remove(tab)
        if (wasIncognito && tabs.none { it.incognito }) {
            // The system WebView keeps one cookie jar for the whole app,
            // so an incognito WebView tab cannot have cookies of its own.
            // When the last incognito tab closes, wipe WebView cookies
            // and web storage so the session leaves nothing behind.
            // (This also signs normal WebView tabs out — Gecko tabs are
            // unaffected, they use real private mode.)
            android.webkit.CookieManager.getInstance().removeAllCookies(null)
            android.webkit.CookieManager.getInstance().flush()
            android.webkit.WebStorage.getInstance().deleteAllData()
        }
        if (tabs.isEmpty()) {
            newTab(store.mainEngine, false)
        } else if (activeTabId == tab.id) {
            activeTabId = tabs[minOf(idx, tabs.size - 1)].id
        }
    }

    fun reopenLastClosed() {
        val info = lastClosedTab ?: return
        lastClosedTab = null
        newTab(info.engine, info.incognito, info.url, info.desktopMode)
        screen = Screen.BROWSER
    }

    // Session restore: reopen the tabs that were open when the app was
    // last closed (normal tabs only — incognito tabs are never saved).
    // Runs once, before the persistence effect below is allowed to save
    // anything, so a fresh start can never wipe the saved session first.
    LaunchedEffect(Unit) {
        if (store.mainEngineChosen && tabs.isEmpty()) {
            val (saved, savedActive) = store.loadTabs()
            val usable = if (store.startupRestore) {
                saved.filter { it.url.startsWith("http") || it.url == HOME_URL }
            } else {
                emptyList()
            }
            if (usable.isNotEmpty()) {
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
    }

    // Keep the saved session in sync with the live tab list.
    LaunchedEffect(startupDone) {
        if (!startupDone) return@LaunchedEffect
        snapshotFlow {
            val normal = tabs.filter { !it.incognito }
            normal.map { Triple(it.engine, it.url, it.desktopMode) } to
                normal.indexOfFirst { it.id == activeTabId }
        }.collect { (list, idx) ->
            store.saveTabs(
                list.map { SavedTab(it.first, it.second, it.third) },
                idx.coerceAtLeast(0)
            )
        }
    }

    // Bookmark import: system file picker, JSON files only, merged in
    // with duplicates (same URL) skipped.
    val importBookmarksLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val raw = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.readText() ?: ""
            val imported = bookmarksFromJson(raw)
            var added = 0
            imported.forEach { bm ->
                if (bookmarks.none { it.url == bm.url }) {
                    bookmarks.add(bm)
                    added++
                }
            }
            if (added > 0) store.saveBookmarks(bookmarks)
            android.widget.Toast.makeText(
                context,
                if (imported.isEmpty()) "这个文件里没有可导入的书签"
                else "已导入 $added 条书签（重复的自动跳过）",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "导入失败，请换一个书签文件重试",
                android.widget.Toast.LENGTH_LONG
            ).show()
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
        var tab = activeTab() ?: newTab(store.mainEngine, false, url)
        if (tab.engine == Engine.GECKO &&
            effectiveEngine(Engine.GECKO, url) == Engine.WEBVIEW
        ) {
            tab = replaceTabEngine(tab, Engine.WEBVIEW)
        }
        if (tab.url != url) tab.load(url)
        screen = Screen.BROWSER
    }

    BackHandler(enabled = fullscreenView != null) {
        activeTab()?.exitFullscreen()
        fullscreenView = null
    }

    when (screen) {
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
                onSelectTab = { activeTabId = it.id },
                onCloseTab = { closeTab(it) },
                canReopenClosed = lastClosedTab != null,
                onReopenClosed = { reopenLastClosed() },
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
                    activeTab()?.load(resolveInput(text, store.searchEngine))
                },
                onGoHome = { activeTab()?.load(HOME_URL) },
                onSwitchEngine = { engine, rememberSite ->
                    val tab = activeTab() ?: return@BrowserScreen
                    switchTabEngine(tab, engine, rememberSite)
                },
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
                        tab.engine == Engine.GECKO -> android.widget.Toast.makeText(
                            context,
                            "阅读模式暂时只支持 WebView 标签，火狐标签可以先切内核再开",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        tab.readerActive -> tab.exitReaderMode()
                        else -> tab.enterReaderMode()
                    }
                },
                onOpenDownloads = { screen = Screen.DOWNLOADS },
                onOpenBookmarks = { screen = Screen.BOOKMARKS },
                onOpenHistory = { screen = Screen.HISTORY },
                onOpenSettings = { screen = Screen.SETTINGS },
                homeContent = {
                    HomeContent(
                        bookmarks = bookmarks,
                        history = history,
                        searchEngine = store.searchEngine,
                        onSearchUrl = { url -> openUrl(url) },
                        onOpenBookmark = { openUrl(it.url) },
                        onDeleteBookmark = {
                            bookmarks.remove(it)
                            store.saveBookmarks(bookmarks)
                        },
                        onOpenHistoryItem = { openUrl(it.url) },
                        onOpenAllBookmarks = { screen = Screen.BOOKMARKS },
                        onOpenAllHistory = { screen = Screen.HISTORY }
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
                importBookmarksLauncher.launch(arrayOf("application/json", "text/plain"))
            }
        )

        Screen.HISTORY -> HistoryScreen(
            history = history,
            onBack = { screen = Screen.BROWSER },
            onOpen = { openUrl(it.url) },
            onClear = {
                history.clear()
                store.clearHistory()
            }
        )

        Screen.SETTINGS -> SettingsScreen(
            store = store,
            onBack = { screen = Screen.BROWSER },
            onClearHistory = {
                history.clear()
                store.clearHistory()
            },
            onRemoveCompatSite = { removeCompatSite(it) },
            onOpenClearData = { screen = Screen.CLEAR_DATA },
            onTextZoomChange = { percent ->
                tabs.forEach { it.applyTextZoom(percent) }
                GeckoHolder.applyFontSizeFactor(percent / 100f)
            },
            onWebDarkChange = { enabled ->
                tabs.forEach { it.applyWebDark(enabled) }
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
            }
        )

        Screen.DOWNLOADS -> DownloadsScreen(
            onBack = { screen = Screen.BROWSER }
        )
    }

    val linkTarget = linkMenuTarget
    if (linkTarget != null) {
        AlertDialog(
            onDismissRequest = { linkMenuTarget = null },
            title = { Text("链接") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            linkTarget,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("在新标签页打开") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    linkMenuTarget = null
                                    newTab(store.mainEngine, false, linkTarget)
                                    screen = Screen.BROWSER
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("复制链接") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val clipboard = context.getSystemService(
                                        android.content.Context.CLIPBOARD_SERVICE
                                    ) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(
                                        android.content.ClipData.newPlainText(
                                            "link", linkTarget
                                        )
                                    )
                                    android.widget.Toast.makeText(
                                        context, "链接已复制",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    linkMenuTarget = null
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("分享链接") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_SEND
                                    )
                                        .setType("text/plain")
                                        .putExtra(
                                            android.content.Intent.EXTRA_TEXT, linkTarget
                                        )
                                    context.startActivity(
                                        android.content.Intent.createChooser(intent, "分享链接")
                                    )
                                    linkMenuTarget = null
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { linkMenuTarget = null }) { Text("取消") }
            }
        )
    }
}

/**
 * The app's own home page: a native Material 3 screen with a big search
 * box, a bookmark shortcut grid and recent history. It never loads an
 * external start page.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeContent(
    bookmarks: List<Bookmark>,
    history: List<HistoryItem>,
    searchEngine: SearchEngine,
    onSearchUrl: (String) -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onOpenHistoryItem: (HistoryItem) -> Unit,
    onOpenAllBookmarks: () -> Unit,
    onOpenAllHistory: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Bookmark?>(null) }
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("TuxunBrowse", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "搜点什么，或者从下面接着上次的看。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("用${searchEngine.label}搜索，或输入网址") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (query.isNotBlank()) onSearchUrl(resolveInput(query, searchEngine))
                    },
                    onGo = {
                        if (query.isNotBlank()) onSearchUrl(resolveInput(query, searchEngine))
                    }
                )
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("常用书签", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onOpenAllBookmarks) { Text("全部书签") }
            }
        }
        if (bookmarks.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "还没有书签。在网页右上角点星星存下来的页面，会变成这里的快捷入口。",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            gridItems(bookmarks.take(8), key = { it.url }) { bookmark ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onOpenBookmark(bookmark) },
                            onLongClick = { pendingDelete = bookmark }
                        )
                ) {
                    ListItem(
                        headlineContent = { Text(bookmark.title, maxLines = 2) },
                        supportingContent = {
                            Text(hostOf(bookmark.url) ?: bookmark.url, maxLines = 1)
                        },
                        leadingContent = {
                            Icon(Icons.Filled.Bookmark, contentDescription = null)
                        }
                    )
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("最近历史", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onOpenAllHistory) { Text("全部历史") }
            }
        }
        if (history.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    "还没有历史记录。浏览过的网页会按时间出现在这里，无痕标签打开的不会出现。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            gridItems(
                history.take(6),
                key = { it.time.toString() + it.url },
                span = { GridItemSpan(maxLineSpan) }
            ) { item ->
                Column {
                    ListItem(
                        headlineContent = { Text(item.title, maxLines = 1) },
                        supportingContent = { Text(item.url, maxLines = 1) },
                        leadingContent = {
                            Icon(Icons.Filled.History, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenHistoryItem(item) }
                    )
                    HorizontalDivider()
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
                    description = "火狐的内核，独立于系统，隐私保护更强，新功能跟得快。代价是安装包大很多（光内核就一百多 MB）。",
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
                        text = "两个内核的登录状态互不相通：在一个内核里登录过的网站，换到另一个内核打开需要重新登录。某个网站在火狐内核下显示不正常时，可以点地址栏旁边的内核按钮把当前标签切到 WebView，还能记住这个网站以后都用 WebView 打开。",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowserScreen(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    isBookmarked: Boolean,
    currentHostIsCompat: Boolean,
    onSelectTab: (BrowserTab) -> Unit,
    onCloseTab: (BrowserTab) -> Unit,
    canReopenClosed: Boolean,
    onReopenClosed: () -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: (Engine) -> Unit,
    incognitoEngine: Engine,
    onIncognitoEngineChosen: (Engine) -> Unit,
    onToggleBookmark: () -> Unit,
    onNavigate: (String) -> Unit,
    onGoHome: () -> Unit,
    onSwitchEngine: (Engine, Boolean) -> Unit,
    activeDesktopMode: Boolean,
    onToggleDesktopMode: (Boolean) -> Unit,
    onFind: (String, Boolean) -> Unit,
    onClearFind: () -> Unit,
    fullscreenView: android.view.View?,
    chromeHidden: Boolean,
    onExitFullscreen: () -> Unit,
    onToggleReaderMode: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    homeContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var showTabs by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showIncognitoPicker by remember { mutableStateOf(false) }
    var showEngineSwitch by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var addressText by remember(activeTab?.id) {
        mutableStateOf(activeTab?.takeUnless { it.isHome }?.url ?: "")
    }
    var addressFocused by remember { mutableStateOf(false) }
    LaunchedEffect(activeTab?.url) {
        if (!addressFocused && activeTab != null) {
            addressText = if (activeTab.isHome) "" else activeTab.url
        }
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
                            contentDescription = "收藏"
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "菜单")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
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
                                    val clipboard = context.getSystemService(
                                        android.content.Context.CLIPBOARD_SERVICE
                                    ) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(
                                        android.content.ClipData.newPlainText(
                                            "url", tab.url
                                        )
                                    )
                                    android.widget.Toast.makeText(
                                        context, "网址已复制",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("页面内查找") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            onClick = { showMenu = false; showFindBar = true }
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
                        DropdownMenuItem(
                            text = { Text("设置") },
                            leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                            onClick = { showMenu = false; onOpenSettings() }
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
                    onClick = { showTabs = true },
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
                IconButton(
                    onClick = { activeTab?.goBack() },
                    enabled = activeTab?.canGoBack == true
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "后退")
                }
                IconButton(
                    onClick = { activeTab?.goForward() },
                    enabled = activeTab?.canGoForward == true
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "前进")
                }
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { addressFocused = it.isFocused },
                    singleLine = true,
                    placeholder = { Text("输入网址或搜索") },
                    leadingIcon = {
                        when {
                            activeTab == null || activeTab.isHome ->
                                Icon(Icons.Filled.Search, contentDescription = null)
                            activeTab.url.startsWith("https://") ->
                                Icon(Icons.Filled.Lock, contentDescription = "安全连接")
                            else ->
                                Icon(Icons.Filled.Warning, contentDescription = "非安全连接")
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = { onNavigate(addressText) }
                    )
                )
                if (activeTab != null) {
                    AssistChip(
                        onClick = { showEngineSwitch = true },
                        label = {
                            Text(if (activeTab.engine == Engine.GECKO) "火狐" else "WebView")
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.SwapHoriz, contentDescription = "切换内核")
                        }
                    )
                }
                IconButton(onClick = { activeTab?.reload() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "刷新")
                }
            }
            if (activeTab != null && activeTab.progress in 1..99) {
                LinearProgressIndicator(
                    progress = { activeTab.progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (showFindBar && activeTab != null && !activeTab.isHome) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = findQuery,
                        onValueChange = { findQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("在页面里查找") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { onFind(findQuery, true) }
                        )
                    )
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
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "关闭查找")
                    }
                }
            }
            }
            if (activeTab != null) {
                if (activeTab.isHome) {
                    homeContent()
                } else {
                    TabContent(tab = activeTab, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState()
    if (showTabs) {
        ModalBottomSheet(
            onDismissRequest = { showTabs = false },
            sheetState = sheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("标签页", style = MaterialTheme.typography.titleLarge)
                }
                items(tabs, key = { it.id }) { tab ->
                    ElevatedCard(
                        onClick = { onSelectTab(tab); showTabs = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    if (tab.isHome) "首页" else tab.title.ifBlank { "新标签页" },
                                    maxLines = 1
                                )
                            },
                            supportingContent = {
                                Text(if (tab.isHome) "TuxunBrowse 首页" else tab.url, maxLines = 2)
                            },
                            trailingContent = {
                                Row {
                                    AssistChip(
                                        onClick = { },
                                        label = {
                                            Text(
                                                (if (tab.incognito) "无痕 · " else "") +
                                                    if (tab.engine == Engine.GECKO) "火狐" else "WebView"
                                            )
                                        }
                                    )
                                    IconButton(onClick = { onCloseTab(tab) }) {
                                        Icon(Icons.Filled.Close, contentDescription = "关闭标签")
                                    }
                                }
                            }
                        )
                    }
                }
                item {
                    FilledTonalButton(
                        onClick = { showTabs = false; onNewTab() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("新建标签页")
                    }
                }
                if (canReopenClosed) {
                    item {
                        OutlinedButton(
                            onClick = { showTabs = false; onReopenClosed() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("恢复刚关闭的标签页")
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (currentHost != null && picked == Engine.WEBVIEW &&
                        activeTab.engine == Engine.GECKO
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("这个网站以后都用 WebView 打开") },
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
                        Text("无痕标签不会记录历史。选好的内核会单独记住，下次无痕时默认用它。火狐无痕是真隔离；WebView 无痕在最后一个无痕标签关掉时会清掉 WebView 的全部 Cookie，普通 WebView 标签也会跟着退出登录（系统 WebView 只有一个 Cookie 罐，分不开）。")
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
                            modifier = Modifier.fillMaxWidth()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarksScreen(
    bookmarks: List<Bookmark>,
    onBack: () -> Unit,
    onOpen: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit,
    onEdit: (Bookmark, String, String) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var editing by remember { mutableStateOf<Bookmark?>(null) }
    val editingBookmark = editing
    if (editingBookmark != null) {
        var editTitle by remember(editingBookmark) { mutableStateOf(editingBookmark.title) }
        var editUrl by remember(editingBookmark) { mutableStateOf(editingBookmark.url) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("编辑书签") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            modifier = Modifier.fillMaxWidth(),
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
                        val resolved = resolveInput(editUrl)
                        onEdit(
                            editingBookmark,
                            editTitle.ifBlank { resolved },
                            resolved
                        )
                        editing = null
                    },
                    enabled = editUrl.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("取消") }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书签") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onImport) {
                        Icon(Icons.Filled.FileDownload, contentDescription = "导入书签")
                    }
                    IconButton(onClick = onExport) {
                        Icon(Icons.Filled.FileUpload, contentDescription = "导出书签")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (bookmarks.isEmpty()) {
                item {
                    Text(
                        "还没有书签。在网页右上角点星星就能把当前页存到这里，首页上也会出现它的快捷入口。",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(bookmarks, key = { it.url }) { bookmark ->
                ListItem(
                    headlineContent = { Text(bookmark.title) },
                    supportingContent = { Text(bookmark.url) },
                    leadingContent = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { editing = bookmark }) {
                                Icon(Icons.Filled.Edit, contentDescription = "编辑书签")
                            }
                            IconButton(onClick = { onDelete(bookmark) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除书签")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(bookmark) }
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
    val groups = listOf("今天", "昨天", "更早")
        .map { bucket -> bucket to filtered.filter { bucketOf(it) == bucket } }
        .filter { it.second.isNotEmpty() }
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
                .padding(padding)
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
                    Text(
                        "还没有历史记录。浏览过的网页会按时间列在这里，无痕标签打开的网页不会出现。",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else if (filtered.isEmpty()) {
                item {
                    Text(
                        "没有找到和「$query」相关的历史记录。",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
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

private data class DownloadEntry(
    val id: Long,
    val title: String,
    val statusText: String,
    val sizeText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<DownloadEntry>>(emptyList()) }
    LaunchedEffect(Unit) {
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
                while (c.moveToNext()) {
                    val status = c.getInt(statusIdx)
                    val statusText = when (status) {
                        android.app.DownloadManager.STATUS_SUCCESSFUL -> "已完成"
                        android.app.DownloadManager.STATUS_FAILED -> "失败"
                        android.app.DownloadManager.STATUS_RUNNING -> "下载中"
                        android.app.DownloadManager.STATUS_PAUSED -> "已暂停"
                        else -> "排队中"
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
                            statusText = statusText,
                            sizeText = sizeText
                        )
                    )
                }
            }
        } catch (t: Throwable) {
            // Download list is best-effort; an empty list is shown instead.
        }
        entries = loaded.reversed()
    }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (entries.isEmpty()) {
                item {
                    Text(
                        "还没有下载记录。在网页里点到下载链接时，文件会交给系统下载，进度和结果会列在这里。",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(entries, key = { it.id }) { entry ->
                ListItem(
                    headlineContent = { Text(entry.title, maxLines = 2) },
                    supportingContent = { Text("${entry.statusText} · ${entry.sizeText}") },
                    leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClearDataScreen(
    geckoStarted: Boolean,
    onBack: () -> Unit,
    onClear: (Boolean, Boolean, Boolean) -> Unit
) {
    var clearHistorySel by remember { mutableStateOf(true) }
    var clearWebView by remember { mutableStateOf(true) }
    var clearGecko by remember { mutableStateOf(true) }
    var showConfirm by remember { mutableStateOf(false) }
    var doneText by remember { mutableStateOf<String?>(null) }
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
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("取消") }
            }
        )
    }
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
                Text(
                    "想清哪样就勾哪样，最后点下面的按钮一次清掉。书签不在这里清，在书签页单独删。",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("历史记录") },
                    supportingContent = { Text("应用里记下的浏览历史，首页的最近历史也会一起没。") },
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
                    headlineContent = { Text("WebView 的 Cookie 和网站数据") },
                    supportingContent = { Text("清完后用 WebView 打开的网站都要重新登录。") },
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
            item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    store: Store,
    onBack: () -> Unit,
    onClearHistory: () -> Unit,
    onRemoveCompatSite: (String) -> Unit,
    onOpenClearData: () -> Unit,
    onTextZoomChange: (Int) -> Unit,
    onWebDarkChange: (Boolean) -> Unit
) {
    var mainEngine by remember { mutableStateOf(store.mainEngine) }
    var incognitoEngine by remember { mutableStateOf(store.incognitoEngine) }
    var searchEngine by remember { mutableStateOf(store.searchEngine) }
    var adBlock by remember { mutableStateOf(store.adBlockEnabled) }
    var textZoom by remember { mutableStateOf(store.textZoomPercent) }
    var webDark by remember { mutableStateOf(store.webDarkMode) }
    var startupRestore by remember { mutableStateOf(store.startupRestore) }
    var compatList by remember { mutableStateOf(store.loadCompatSites().sorted()) }
    var showCleared by remember { mutableStateOf(false) }
    val settingsContext = LocalContext.current
    val versionName = remember {
        try {
            settingsContext.packageManager
                .getPackageInfo(settingsContext.packageName, 0).versionName ?: ""
        } catch (t: Throwable) {
            ""
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                Text("主内核", style = MaterialTheme.typography.titleMedium)
                Text(
                    "新建的普通标签页用这个内核打开。已经打开的标签不会中途换内核，要换就点地址栏旁边的内核按钮。",
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
                        searchEngine = engine
                        store.searchEngine = engine
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(engine.label) },
                        trailingContent = {
                            RadioButton(
                                selected = searchEngine == engine,
                                onClick = {
                                    searchEngine = engine
                                    store.searchEngine = engine
                                }
                            )
                        }
                    )
                }
            }
            item {
                Text("兼容模式记住的网站", style = MaterialTheme.typography.titleMedium)
                Text(
                    "这些网站在火狐内核下打开时会自动改用 WebView。删掉某个网站，它就恢复成用你选的主内核打开。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (compatList.isEmpty()) {
                item {
                    Text(
                        "还没有。在火狐标签里打开某个网站觉得不对劲时，点地址栏旁边的「火狐」按钮切到 WebView 并勾选记住，就会记到这里。",
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
            item {
                Text("广告拦截", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ListItem(
                    headlineContent = { Text("拦截广告和跟踪请求") },
                    supportingContent = {
                        Text("WebView 按内置名单拦截常见广告域名，立即生效；火狐用它自带的跟踪保护，重启应用后完全生效。关掉后所有网站原样加载。")
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
                        Text("让网页按深色显示。WebView 标签在 Android 13 以上生效；火狐内核的网页深浅跟随系统深色模式，系统开了深色它就是深色。")
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关于 TuxunBrowse", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "版本 $versionName。免费开源的双内核浏览器：火狐 GeckoView 与系统 WebView 由用户自选，" +
                                "首页是应用自己的页面（搜索框、常用书签、最近历史），不拿任何网站的页面冒充首页。" +
                                "两个内核的登录状态互不相通，这是双内核的固有代价。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }
        }
    }
}
