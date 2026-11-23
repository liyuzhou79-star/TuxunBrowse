package com.tuxunbrowse.android.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.Store
import com.tuxunbrowse.android.data.resolveInput
import com.tuxunbrowse.android.engine.BrowserTab
import com.tuxunbrowse.android.engine.TabContent

private enum class Screen { PICKER, BROWSER, BOOKMARKS, HISTORY, SETTINGS }
private const val HOME_URL = "https://duckduckgo.com/"

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

    fun newTab(engine: Engine, incognito: Boolean, url: String = HOME_URL): BrowserTab {
        val tab = BrowserTab(engine, incognito, url) { t, u -> recordVisit(t, u) }
        tabs.add(tab)
        activeTabId = tab.id
        return tab
    }

    fun activeTab(): BrowserTab? = tabs.firstOrNull { it.id == activeTabId }

    fun closeTab(tab: BrowserTab) {
        val idx = tabs.indexOf(tab)
        tab.destroy()
        tabs.remove(tab)
        if (tabs.isEmpty()) {
            newTab(store.mainEngine, false)
        } else if (activeTabId == tab.id) {
            activeTabId = tabs[minOf(idx, tabs.size - 1)].id
        }
    }

    fun openUrl(url: String) {
        val tab = activeTab() ?: newTab(store.mainEngine, false, url)
        if (tab.url != url) tab.load(url)
        screen = Screen.BROWSER
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

        Screen.BROWSER -> BrowserScreen(
            tabs = tabs,
            activeTab = activeTab(),
            isBookmarked = activeTab()?.let { t -> bookmarks.any { it.url == t.url } } ?: false,
            onSelectTab = { activeTabId = it.id },
            onCloseTab = { closeTab(it) },
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
            onOpenBookmarks = { screen = Screen.BOOKMARKS },
            onOpenHistory = { screen = Screen.HISTORY },
            onOpenSettings = { screen = Screen.SETTINGS }
        )

        Screen.BOOKMARKS -> BookmarksScreen(
            bookmarks = bookmarks,
            onBack = { screen = Screen.BROWSER },
            onOpen = { openUrl(it.url) },
            onDelete = {
                bookmarks.remove(it)
                store.saveBookmarks(bookmarks)
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
            }
        )
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
                    description = "火狐的内核，独立于系统，隐私保护更强，新功能跟得快。安装包会大一些（约多 55 MB）。",
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
                        text = "两个内核的登录状态互不相通：在一个内核里登录过的网站，换到另一个内核打开需要重新登录。某个网站在火狐内核下显示不正常时，可以新建一个 WebView 标签打开它。",
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
    onSelectTab: (BrowserTab) -> Unit,
    onCloseTab: (BrowserTab) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: (Engine) -> Unit,
    incognitoEngine: Engine,
    onIncognitoEngineChosen: (Engine) -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showTabs by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showIncognitoPicker by remember { mutableStateOf(false) }
    var addressText by remember(activeTab?.id) { mutableStateOf(activeTab?.url ?: "") }
    var addressFocused by remember { mutableStateOf(false) }
    LaunchedEffect(activeTab?.url) {
        if (!addressFocused && activeTab != null) addressText = activeTab.url
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeTab?.title?.ifBlank { "TuxunBrowse" } ?: "TuxunBrowse",
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
        },
        bottomBar = {
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = { activeTab?.load(resolveInput(addressText)) }
                    )
                )
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
            if (activeTab != null) {
                TabContent(tab = activeTab, modifier = Modifier.fillMaxSize())
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
                                Text(tab.title.ifBlank { "新标签页" }, maxLines = 1)
                            },
                            supportingContent = { Text(tab.url, maxLines = 2) },
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
            }
        }
    }

    if (showIncognitoPicker) {
        var picked by remember { mutableStateOf(incognitoEngine) }
        AlertDialog(
            onDismissRequest = { showIncognitoPicker = false },
            title = { Text("无痕浏览用哪个内核？") },
            text = {
                LazyColumn {
                    item {
                        Text("无痕标签不会记录历史。选好的内核会单独记住，下次无痕时默认用它。")
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
    onDelete: (Bookmark) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("书签") },
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
            if (bookmarks.isEmpty()) {
                item {
                    Text(
                        "还没有书签。在网页右上角点星星就能把当前页存到这里。",
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
                        IconButton(onClick = { onDelete(bookmark) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除书签")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(bookmark) }
                )
                androidx.compose.material3.HorizontalDivider()
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
                    OutlinedButton(
                        onClick = onClear,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
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
            }
            items(history, key = { it.time.toString() + it.url }) { item ->
                ListItem(
                    headlineContent = { Text(item.title) },
                    supportingContent = { Text(item.url) },
                    leadingContent = { Icon(Icons.Filled.History, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(item) }
                )
                androidx.compose.material3.HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    store: Store,
    onBack: () -> Unit,
    onClearHistory: () -> Unit
) {
    var mainEngine by remember { mutableStateOf(store.mainEngine) }
    var incognitoEngine by remember { mutableStateOf(store.incognitoEngine) }
    var showCleared by remember { mutableStateOf(false) }
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
                    "新建的普通标签页用这个内核打开。已经打开的标签不会中途换内核。",
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
                Text("数据", style = MaterialTheme.typography.titleMedium)
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
                            "版本 0.1.0。免费开源的双内核浏览器：火狐 GeckoView 与系统 WebView 由用户自选，" +
                                "没有信息流，没有推送，没有广告。两个内核的登录状态互不相通，这是双内核的固有代价。",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
