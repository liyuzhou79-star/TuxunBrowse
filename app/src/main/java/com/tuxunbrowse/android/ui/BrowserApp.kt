package com.tuxunbrowse.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.DownloadMode
import com.tuxunbrowse.android.data.GeckoDownloadRecord
import com.tuxunbrowse.android.data.Engine
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.HistoryRetention
import com.tuxunbrowse.android.data.TUXUNDOWN_PACKAGE
import com.tuxunbrowse.android.data.ReaderBackground
import com.tuxunbrowse.android.data.ReaderFontSize
import com.tuxunbrowse.android.data.compareVersionNames
import com.tuxunbrowse.android.data.restoredActiveIndex
import com.tuxunbrowse.android.data.pickLatestRelease
import com.tuxunbrowse.android.data.UpdateFetchResult
import com.tuxunbrowse.android.data.updateFetchResult
import com.tuxunbrowse.android.data.SavedTab
import com.tuxunbrowse.android.data.SearchEngine
import com.tuxunbrowse.android.data.Store
import com.tuxunbrowse.android.data.ThemeMode
import com.tuxunbrowse.android.data.TrackingProtectionLevel
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
import com.tuxunbrowse.android.data.selectNewBookmarks
import com.tuxunbrowse.android.data.bookmarksToHtml
import com.tuxunbrowse.android.data.bookmarksToJson
import com.tuxunbrowse.android.data.MAX_IMPORT_FILE_CHARS
import com.tuxunbrowse.android.MainActivity
import com.tuxunbrowse.android.data.hostOf
import com.tuxunbrowse.android.data.pinnedShortcutId
import com.tuxunbrowse.android.data.pinnedShortcutInitial
import com.tuxunbrowse.android.data.pinnedShortcutLabel
import com.tuxunbrowse.android.data.isAppDarkTheme
import com.tuxunbrowse.android.data.hostMatchesSite
import com.tuxunbrowse.android.data.isCompatHost
import com.tuxunbrowse.android.data.isDesktopHost
import com.tuxunbrowse.android.data.extractFirstWebUrl
import com.tuxunbrowse.android.data.DiagnosticInfo
import com.tuxunbrowse.android.data.GECKOVIEW_VERSION
import com.tuxunbrowse.android.data.diagnosticText
import com.tuxunbrowse.android.data.displayHostFor
import com.tuxunbrowse.android.data.hasBlockedPageScheme
import com.tuxunbrowse.android.data.isAllowedPageUrl
import com.tuxunbrowse.android.data.isUrlLikeInput
import com.tuxunbrowse.android.data.historyRestoreIndex
import com.tuxunbrowse.android.data.shouldPreheatGecko
import com.tuxunbrowse.android.data.TabBookmarkCandidate
import com.tuxunbrowse.android.data.allBookmarkFolders
import com.tuxunbrowse.android.data.bookmarksInFolder
import com.tuxunbrowse.android.data.clearBookmarksFolder
import com.tuxunbrowse.android.data.normalizeBookmarkFolderName
import com.tuxunbrowse.android.data.renameBookmarksFolder
import com.tuxunbrowse.android.data.matchRange
import com.tuxunbrowse.android.data.matchSuggestions
import com.tuxunbrowse.android.data.resolveInput
import com.tuxunbrowse.android.data.upgradeToHttps
import com.tuxunbrowse.android.data.downgradeToHttp
import com.tuxunbrowse.android.data.HistoryClearRange
import com.tuxunbrowse.android.data.countHistoryInClearRange
import com.tuxunbrowse.android.data.historyClearCutoff
import com.tuxunbrowse.android.data.keptHistoryAfterClear
import com.tuxunbrowse.android.engine.ALIVE_ENGINE_LIMIT
import com.tuxunbrowse.android.engine.AdBlocker
import com.tuxunbrowse.android.engine.BrowserTab
import com.tuxunbrowse.android.engine.GeckoDownloadTracker
import com.tuxunbrowse.android.engine.FingerprintProfiles
import com.tuxunbrowse.android.engine.FingerprintProtection
import com.tuxunbrowse.android.engine.FingerprintScript
import com.tuxunbrowse.android.engine.ResolvedFingerprint
import com.tuxunbrowse.android.engine.GeckoHolder
import com.tuxunbrowse.android.engine.GuardScript
import com.tuxunbrowse.android.engine.SniffKind
import com.tuxunbrowse.android.engine.SniffedResource
import com.tuxunbrowse.android.engine.TabLoadError
import com.tuxunbrowse.android.engine.ThumbnailCache
import com.tuxunbrowse.android.engine.normalizePickerMime
import com.tuxunbrowse.android.engine.EngineFallbackAction
import com.tuxunbrowse.android.engine.engineFallbackActions
import com.tuxunbrowse.android.engine.engineFallbackReason
import com.tuxunbrowse.android.engine.otherEngineOpenLabel
import com.tuxunbrowse.android.engine.GeckoJsPromptKind
import com.tuxunbrowse.android.engine.GeckoSelectionMenu
import com.tuxunbrowse.android.engine.HttpAuthRequestInfo
import com.tuxunbrowse.android.engine.PrivacySignals
import com.tuxunbrowse.android.engine.EngineSleepCandidate
import com.tuxunbrowse.android.engine.TabContent
import com.tuxunbrowse.android.engine.selectSleepVictims
import com.tuxunbrowse.android.engine.NewTabGroup
import com.tuxunbrowse.android.engine.newTabGroupForSource
import com.tuxunbrowse.android.engine.shouldReleaseTransientUiForTab
import com.tuxunbrowse.android.engine.trimAliveLimit
import com.tuxunbrowse.android.engine.savePageScreenshot
import com.tuxunbrowse.android.engine.sharePageScreenshot
import com.tuxunbrowse.android.engine.resolveDownloadFileName
import com.tuxunbrowse.android.engine.SOURCE_DISPLAY_LIMIT
import com.tuxunbrowse.android.engine.isSourceTruncated
import com.tuxunbrowse.android.engine.startSystemDownload
import com.tuxunbrowse.android.engine.truncateSourceForDisplay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mozilla.geckoview.GeckoSession

private enum class Screen { PICKER, BROWSER, BOOKMARKS, HISTORY, SETTINGS, DOWNLOADS, CLEAR_DATA, SNIFF }

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
 * Accessibility touch-target expansion (v0.99.0 audit): measure
 * the child at [touchSize] but report only [visualSize] to the
 * parent layout, centring the larger child so its interactive
 * area overflows the reported box evenly on every side. The
 * visual result is pixel-identical to a [visualSize] box (the
 * child's own content stays centred), the layout footprint keeps
 * the small size, and the clickable inside the child keeps the
 * full [touchSize] hit area — the standard way to give a compact
 * 40dp icon button the 48dp minimum touch target without handing
 * the layout space back.
 */
private fun Modifier.expandedTouchTarget(
    visualSize: Dp,
    touchSize: Dp
): Modifier = this.layout { measurable, _ ->
    val touchPx = touchSize.roundToPx()
    val visualPx = visualSize.roundToPx()
    val placeable = measurable.measure(Constraints.fixed(touchPx, touchPx))
    layout(visualPx, visualPx) {
        placeable.place(-(touchPx - visualPx) / 2, -(touchPx - visualPx) / 2)
    }
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
 * The generated icon for a pinned home-screen shortcut (v0.86.0):
 * a filled circle in the theme's primary colour with the page
 * label's first letter on it in the matching on-primary colour —
 * the same "theme colour + first letter" device the home page's
 * speed-dial tiles use. Deliberately NOT the site's favicon: the
 * app never downloaded it, and a generated icon cannot pretend to
 * be artwork it is not. 192×192 is comfortably above what
 * launchers downscale from.
 */
private fun createPinnedShortcutIcon(
    bgColor: Int,
    fgColor: Int,
    letter: String
): android.graphics.Bitmap {
    val size = 192
    val bitmap = android.graphics.Bitmap.createBitmap(
        size, size, android.graphics.Bitmap.Config.ARGB_8888
    )
    val canvas = android.graphics.Canvas(bitmap)
    val circlePaint = android.graphics.Paint(
        android.graphics.Paint.ANTI_ALIAS_FLAG
    ).apply { color = bgColor }
    canvas.drawCircle(
        size / 2f, size / 2f, size / 2f, circlePaint
    )
    if (letter.isNotEmpty()) {
        val textPaint = android.graphics.Paint(
            android.graphics.Paint.ANTI_ALIAS_FLAG
        ).apply {
            color = fgColor
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = size * 0.5f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        val metrics = textPaint.fontMetrics
        val baseline = size / 2f - (metrics.ascent + metrics.descent) / 2f
        canvas.drawText(letter, size / 2f, baseline, textPaint)
    }
    return bitmap
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
    val incognito: Boolean,
    // The SOURCE tab's id (v0.172.0): "open in new tab" decides
    // its landing group from this id (see newTabGroupForSource) —
    // a long-press inside a fingerprint tab must open its new
    // tab inside the fingerprint group, not the normal one.
    // Until this version the state carried no tab identity at
    // all, so the group decision could not even be made.
    val tabId: Int = -1
)

/**
 * A Gecko text-selection menu waiting to be shown (v0.43.0). Carries
 * the SOURCE tab's id and incognito flag, captured when the
 * selection was made: "search the web for this" must load in that
 * same tab (an incognito selection must stay in the incognito tab),
 * never in a fresh normal tab.
 */
private data class GeckoSelectionMenuState(
    val tabId: Int,
    val incognito: Boolean,
    val menu: GeckoSelectionMenu
)

/**
 * A Gecko page's clipboard-read permission request waiting for the
 * user's decision (v0.45.0). Carries the SOURCE tab's id and the
 * requesting page's URI (falling back to the tab's current URL when
 * Gecko's permission object carries none), plus the one-shot answer
 * callback into the tab layer — true only for an explicit "allow".
 * Nothing about the clipboard's contents is ever carried here: the
 * permission object has no content field, and the app never reads
 * the clipboard for this flow.
 */
private data class GeckoClipboardPermissionState(
    val tabId: Int,
    val uri: String,
    val respond: (Boolean) -> Unit
)

/**
 * Suggestion-row text with the part matching what the user typed
 * picked out in the theme colour at semi-bold weight (v0.68.0).
 * The match itself is decided by the pure [matchRange] function
 * (case-insensitive, first occurrence); with no match the text is
 * returned unchanged. Used by every suggestion row on both
 * surfaces — address bar and home search box: bookmark rows (title
 * and URL), online suggestion rows and the "search this" tail row.
 */
private fun highlightMatchedText(
    text: String,
    query: String,
    highlightColor: androidx.compose.ui.graphics.Color
): AnnotatedString {
    val range = matchRange(text, query) ?: return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, range.first))
        withStyle(
            SpanStyle(
                color = highlightColor,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(text.substring(range.first, range.last + 1))
        }
        append(text.substring(range.last + 1))
    }
}

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
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {},
    onThemeModeChanged: (ThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val store = remember { Store(context) }
    // Scope for the file work below (v0.167.0 stability audit):
    // imports and exports used to read, parse, serialize and write
    // whole files synchronously inside activity-result callbacks
    // and click handlers — all main-thread work, so a slow content
    // provider or a large import file froze the UI for the duration.
    // Those paths now launch here, do the heavy part off the main
    // thread, and come back for state changes and toasts.
    val appScope = rememberCoroutineScope()
    var screen by remember {
        mutableStateOf(if (store.mainEngineChosen) Screen.BROWSER else Screen.PICKER)
    }
    val tabs = remember { mutableStateListOf<BrowserTab>() }
    var activeTabId by remember { mutableStateOf(-1) }
    // Fingerprint mode (v0.144.0): its own tab group, apart from
    // `tabs` — the normal shell, tab persistence and the engine
    // sleep pass never see these tabs. Fingerprint tabs are always
    // WebView tabs (GeckoView 132 has no per-session fingerprint
    // surface; see the checkpoint) and never record history.
    val fpTabs = remember { mutableStateListOf<BrowserTab>() }
    var activeFpTabId by remember { mutableStateOf(-1) }
    var fingerprintMode by remember { mutableStateOf(false) }
    var fingerprintProtection by remember {
        mutableStateOf(store.fingerprintProtection)
    }
    // Startup loading split (v0.28.0 audit): the big JSON lists
    // (and, since v0.79.0, the bookmark folder names) start out
    // EMPTY here and are filled in by the startup effect
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
    // Compat sites also stay synchronous on purpose: the very first
    // restored tab must already know them (effectiveEngine / the
    // shouldUseWebView check run when that tab is created and first
    // loads), and the set is a handful of domain strings.
    val compatSites = remember {
        mutableStateListOf<String>().apply { addAll(store.loadCompatSites()) }
    }
    // Desktop-mode sites (v0.56.0) stay synchronous for the same
    // reason: a tab created in the very first moments must already
    // know whether its start host starts in desktop mode (newTab
    // consults this list), and the set is a handful of host strings.
    val desktopSites = remember {
        mutableStateListOf<String>().apply { addAll(store.loadDesktopSites()) }
    }
    // Text another app shared to us, waiting for the user's choice
    // in the share dialog (v0.73.0). Nothing is opened or saved
    // until the user picks — and the clipboard is never involved.
    var pendingShareText by remember { mutableStateOf<String?>(null) }
    // Stored bookmark folder names (v0.72.0): this list exists so
    // EMPTY folders survive; the folder list the UI offers is
    // always allBookmarkFolders(bookmarks, bookmarkFolders), the
    // union of this stored list with the names derived from the
    // bookmarks themselves. Startup audit (v0.79.0): unlike the
    // three site lists above, nothing in the first restored tab
    // consults folder names — every folder mutation happens from
    // the bookmarks UI, which needs an active tab, and tabs are
    // created only after the startup fill below. The names
    // therefore start EMPTY here and are filled in by that same
    // background fill, off the main thread.
    val bookmarkFolders = remember { mutableStateListOf<String>() }
    // One-time compat bypasses (v0.52.0): tab id -> the exact URL whose
    // first Gecko load must NOT be bounced back to WebView by the
    // per-site compat memory. Set only by the load-error page's
    // "open in the other engine" action: the user asked to try the
    // other engine for this one load, so the compat list itself is
    // left untouched (nothing added, nothing removed) and the bypass
    // is consumed by the first matching navigation check.
    val compatBypassLoads = remember { mutableMapOf<Int, String>() }
    var pageContextMenu by remember {
        mutableStateOf<PageContextMenuState?>(null)
    }
    var geckoSelectionMenu by remember {
        mutableStateOf<GeckoSelectionMenuState?>(null)
    }
    // Clipboard-read permission prompt (v0.45.0): at most one request
    // is shown; further requests from the same active tab queue up
    // behind it. Requests from a background tab are denied on the
    // spot — a prompt naming a site the user is not looking at would
    // be a trap, not a question. Nothing is remembered per site:
    // every request asks again.
    var geckoClipboardPermission by remember {
        mutableStateOf<GeckoClipboardPermissionState?>(null)
    }
    val geckoClipboardQueue = remember {
        mutableStateListOf<GeckoClipboardPermissionState>()
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
    // Gecko warm-up (v0.71.0): the runtime used to be created only
    // when the first Firefox tab was opened, and that tab sat
    // waiting for it. When either engine choice is Firefox, kick
    // the creation off right after the first frame, on a background
    // dispatcher — the first screen never waits for it, and when
    // both engines are the system WebView nothing is created at
    // all. A failed warm-up is silent by design: ensure() leaves
    // the runtime null and the first real Firefox tab retries
    // through the existing path (plain-language fallback page
    // included), exactly as before.
    // First-launch gate (v0.121.0): while the engine picker is up
    // no engine has been CHOSEN yet — store.mainEngine only reads
    // as Gecko because that is the fromId fallback for an absent
    // key. Warming the runtime up here would create the Firefox
    // runtime before the user picks anything (and waste it
    // entirely when they pick WebView for both slots). Keying on
    // the picker state re-runs this once the choice is confirmed,
    // and ensure() is idempotent for every later re-run.
    LaunchedEffect(screen == Screen.PICKER) {
        if (screen != Screen.PICKER &&
            shouldPreheatGecko(store.mainEngine, store.incognitoEngine)
        ) {
            kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.Default
            ) {
                GeckoHolder.ensure(context.applicationContext)
            }
        }
    }
    // Page-guard payload warm-up (v0.95.0 startup audit): the
    // payload is a ~150 KB asset read plus assembly, cached per
    // process by GuardScript — but the first WebView tab used to
    // pay that cost on the main thread inside obtainWebView,
    // because registration happens there before the first load.
    // Build the cache here instead, on a background dispatcher
    // right after the first frame, so tab creation later only
    // reads the finished string. Only when the switch is on: with
    // it off nothing ever asks for the payload. The build is
    // idempotent, so a tab created before this finishes simply
    // builds (or finds) the same bytes itself, exactly as before.
    LaunchedEffect(Unit) {
        if (store.guardScriptEnabled) {
            kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.Default
            ) {
                GuardScript.payload(context.applicationContext)
            }
        }
    }
    // Fingerprint payload warm-up (v0.165.0 startup audit): the
    // fingerprint payload is the largest of the three script
    // payloads and, unlike the guard payload, had no warm-up at
    // all — the first fingerprint tab paid the full assembly on
    // the main thread at creation, and every page start paid it
    // again until FingerprintScript grew its own cache (this
    // version). Entering the mode is a deliberate tap with the
    // shell already on screen, so build the (now cached) payload
    // here on a background dispatcher; tab creation then only
    // reads the finished string. Normal mode never runs this:
    // the effect's key stays false and its body stays out.
    LaunchedEffect(fingerprintMode) {
        if (fingerprintMode) {
            kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.Default
            ) {
                FingerprintScript.payload(
                    store.resolvedFingerprint(),
                    store.fingerprintProtection
                )
            }
        }
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
    // Keep-screen-on while fullscreen (v0.66.0): the ONLY place the
    // window flag is touched. Both fullscreen states converge here —
    // WebView's fullscreenView and Gecko's geckoFullscreen — and the
    // flag is a pure derivation of "either one is active": every exit
    // path that clears those states (onHideCustomView, the overlay
    // exit button, either back handler, Gecko's onFullScreen(false))
    // clears the flag on the next recomposition, and no path can set
    // the flag without a fullscreen state behind it. Ordinary
    // browsing, reader mode and windowed video never reach it. No
    // setting: fullscreen keep-on is the industry default. No
    // permission is needed for this window flag. Best-effort only:
    // no activity window, no flag, no crash.
    val anyFullscreen = fullscreenView != null || geckoFullscreen
    LaunchedEffect(anyFullscreen) {
        try {
            val window = (context as? android.app.Activity)?.window
            if (anyFullscreen) {
                window?.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
            } else {
                window?.clearFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
            }
        } catch (t: Throwable) {
            // Keep-screen-on is a comfort feature only; never crash.
        }
        // Immersive system bars (v0.123.0 fullscreen audit): until
        // this version nothing ever hid the system bars, so the
        // status-bar icons and the navigation bar stayed drawn over
        // fullscreen video (the activity is edge-to-edge). The bars
        // now follow the same single derivation as keep-screen-on:
        // hidden while either fullscreen state is live — a swipe
        // from the edge brings them back transiently — and shown
        // again by the same effect the moment fullscreen ends, so
        // every exit path restores them without knowing about them.
        try {
            val activity = context as? android.app.Activity
            val window = activity?.window
            if (window != null) {
                val controller =
                    WindowCompat.getInsetsController(window, window.decorView)
                if (anyFullscreen) {
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat
                            .BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        } catch (t: Throwable) {
            // Immersion is a comfort feature only; never crash.
        }
    }
    // If the whole UI goes away mid-fullscreen (activity destroyed),
    // do not leave the flag set on a dying window.
    DisposableEffect(Unit) {
        onDispose {
            try {
                val window = (context as? android.app.Activity)?.window
                window?.clearFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                )
                // Same for the system bars: never leave them hidden
                // on a window whose UI is gone (v0.123.0).
                if (window != null) {
                    WindowCompat.getInsetsController(window, window.decorView)
                        .show(WindowInsetsCompat.Type.systemBars())
                }
            } catch (t: Throwable) {
                // Window already gone; nothing left to clear.
            }
        }
    }
    var jsPromptRequest by remember { mutableStateOf<JsPromptState?>(null) }
    var authPromptRequest by remember { mutableStateOf<AuthPromptState?>(null) }
    var sslPromptRequest by remember { mutableStateOf<SslPromptState?>(null) }
    // The tab whose page resources the sniffer screen is showing.
    var sniffTabId by remember { mutableStateOf(-1) }
    // The tab waiting on the screenshot save/share dialog.
    var screenshotTab by remember { mutableStateOf<BrowserTab?>(null) }

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
     * The User-Agent a tab's page is actually browsing with, for
     * download call sites that never see the DownloadListener's
     * reported UA (direct-link intercept, sniffer, image save,
     * retry). A WebView tab exposes its live settings UA — which
     * already includes the desktop-mode UA when that mode is on —
     * so those downloads go out with the same UA as the page
     * (v0.112.0 download-path audit). A Gecko tab has no string UA
     * to hand over (its UA is an engine-side mode), so this returns
     * null there and startSystemDownload's default-UA fallback
     * applies, exactly as before.
     */
    fun pageUserAgent(tab: BrowserTab?): String? =
        tab?.webView?.settings?.userAgentString

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
        // Download links pass the same gate as every other inbound
        // URL (v0.87.0 inbound audit, second round): the WebView
        // download listener can hand over blob:/data: URLs, which
        // are not downloadable files at all — before this check
        // they were shared to TuxunDown as dead text or reached
        // DownloadManager, which refuses non-http(s) URIs with an
        // exception and a misleading "failed, retry" toast. Refuse
        // here, once, with the gate's plain-language message. The
        // other callers (direct-download intercept, sniffer) only
        // ever produce http(s), so nothing legitimate changes.
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            android.widget.Toast.makeText(
                context,
                "这个下载链接打不开：只支持 http/https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
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
                    "无痕提醒：下载记录会留在系统下载管理里，关掉无痕标签也不删",
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
        }
    }

    /**
     * Batch version of [downloadWithMode] (sniffer "download all"):
     * never shares per item — the whole batch is one share with one
     * link per line.
     */
    fun downloadBatchWithMode(
        urls: List<String>,
        fromIncognito: Boolean = false,
        systemFallback: (List<String>) -> Unit
    ) {
        // Same inbound gate as downloadWithMode (v0.87.0): only
        // http(s) entries take part. The sniffer already filters
        // at its script, so this is defence in depth — a filtered
        // entry is dropped from the batch, never shared onward.
        // The fallback receives the FILTERED list (v0.112.0
        // download-path audit): before this, the share text was
        // built from the filtered list but the system fallback
        // still iterated the caller's original list, so a dropped
        // entry would have reached DownloadManager anyway and
        // failed with the misleading "try another link" toast.
        val clean = urls.filter {
            it.startsWith("http://") || it.startsWith("https://")
        }
        if (clean.isEmpty()) return
        val joined = clean.joinToString("\n")
        // Same incognito honesty as downloadWithMode: one reminder
        // for the whole batch, only if a system download happens.
        val effectiveFallback: () -> Unit = if (fromIncognito) {
            {
                android.widget.Toast.makeText(
                    context,
                    "无痕提醒：下载记录会留在系统下载管理里，关掉无痕标签也不删",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                systemFallback(clean)
            }
        } else {
            { systemFallback(clean) }
        }
        when (store.downloadMode) {
            DownloadMode.SYSTEM -> effectiveFallback()
            DownloadMode.TUXUNDOWN -> tuxunDownOrFallback(joined, effectiveFallback)
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

    // ---- Auto-reload once when the network comes back (v0.70.0) ----
    // A system network callback watches for the offline -> online
    // transition. When it fires, if the ACTIVE tab is sitting on the
    // app's load-error page, that tab reloads itself once — the user
    // no longer has to notice the network is back and tap retry.
    // The same error state (tab id + failed URL) never auto-retries
    // twice: [autoReloadUsedKey] remembers the one shot, and it is
    // reset when the user reloads manually (see the onReloadPage
    // wiring at the BrowserScreen call site), so a fresh failure
    // after a manual retry can earn one more automatic shot. The
    // callback is registered and unregistered with this composition
    // (DisposableEffect), so nothing leaks or keeps watching in the
    // background. Incognito tabs are covered exactly the same way —
    // the reload lives only in memory, nothing is persisted.
    var networkRestoredTick by remember { mutableStateOf(0) }
    var autoReloadUsedKey by remember { mutableStateOf<String?>(null) }
    DisposableEffect(context) {
        val connectivity = context.getSystemService(
            android.content.Context.CONNECTIVITY_SERVICE
        ) as? android.net.ConnectivityManager
        if (connectivity == null) {
            onDispose { }
        } else {
            var wasOffline = connectivity.activeNetwork == null
            val callback = object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    if (wasOffline) {
                        wasOffline = false
                        networkRestoredTick++
                    }
                }

                override fun onLost(network: android.net.Network) {
                    // Only a full loss counts: losing one of two
                    // networks (Wi-Fi + mobile) is not going offline.
                    if (connectivity.activeNetwork == null) wasOffline = true
                }

                override fun onUnavailable() {
                    wasOffline = true
                }
            }
            try {
                connectivity.registerDefaultNetworkCallback(callback)
            } catch (t: Throwable) {
                // No callback, no auto-reload; manual retry remains.
            }
            onDispose {
                try {
                    connectivity.unregisterNetworkCallback(callback)
                } catch (t: Throwable) {
                    // Already gone; nothing to clean up.
                }
            }
        }
    }
    LaunchedEffect(networkRestoredTick) {
        if (networkRestoredTick == 0) return@LaunchedEffect
        val tab = activeTab() ?: return@LaunchedEffect
        val error = tab.loadError ?: return@LaunchedEffect
        val key = "${tab.id}|${error.failedUrl}"
        if (autoReloadUsedKey == key) return@LaunchedEffect
        autoReloadUsedKey = key
        tab.reload()
    }

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
    /**
     * One sleep pass over one tab group (see the two wrappers
     * below it). Declared FIRST: Kotlin local functions cannot be
     * forward-referenced by sibling local functions.
     */
    fun enforceSleepLimitFor(
        group: List<BrowserTab>,
        groupActiveId: Int,
        limit: Int
    ) {
        val alive = group.filter { it.engineCreated }
        if (alive.size <= limit) return
        // Victim choice is the shared pure function (v0.78.0): the
        // ordinary cap pass and the memory-pressure pass below pick
        // exactly the same way, only the limit differs.
        val victimIds = selectSleepVictims(
            alive.map {
                EngineSleepCandidate(it.id, it.lastUsedAt, it.geckoDownloadActive)
            },
            groupActiveId,
            limit
        )
        if (victimIds.isEmpty()) return
        val victims = alive.filter { it.id in victimIds }
        for (victim in victims) {
            victim.sleepEngine()
        }
    }

    /** The normal group's sleep pass (v0.41.0). */
    fun enforceEngineSleepLimit(limit: Int = ALIVE_ENGINE_LIMIT) {
        enforceSleepLimitFor(tabs, activeTabId, limit)
    }

    /**
     * The fingerprint group's own sleep pass (v0.170.0): until this
     * version the cap above covered only the normal group, so
     * fingerprint engines accumulated without any bound — every
     * fingerprint tab ever shown kept its WebView alive for the
     * whole session (the shell's new-tab button can be tapped
     * without limit, and a restored archive can bring many tabs
     * back). The fingerprint group now gets the same cap, the same
     * victim rule and the same sleepEngine() teardown; a slept
     * fingerprint tab re-creates its engine through the very same
     * TabContent obtain path as a normal tab (its fingerprint
     * script handler is dropped at sleep and re-registered at
     * creation, exactly like the guard/privacy handlers). The two
     * groups are capped separately on purpose: fingerprint tabs
     * must never push normal tabs out of memory or the other way
     * round — each group keeps its own working set.
     */
    fun enforceFpEngineSleepLimit(limit: Int = ALIVE_ENGINE_LIMIT) {
        enforceSleepLimitFor(fpTabs, activeFpTabId, limit)
    }

    // Memory-pressure sleep (v0.78.0): when the system reports a
    // critical trim-memory level, squeeze the alive engines down to
    // the active tab alone through the very same sleep pass as the
    // ordinary cap — same victim rule (active tab and
    // download-busy tabs are never slept), same sleepEngine()
    // teardown, same lazy re-creation when the user switches back
    // (the slept tab reloads its page through the existing obtain
    // path). Lower levels do nothing at all: trimAliveLimit is the
    // pure, unit-tested gate. Incognito tabs are treated exactly
    // like normal ones — sleeping writes nothing anywhere, and this
    // pass persists nothing either.
    fun enforceTrimMemoryPressure(level: Int) {
        val limit = trimAliveLimit(level) ?: return
        enforceEngineSleepLimit(limit)
        // The fingerprint group squeezes under the same pressure
        // (v0.170.0): its engines are the same WebViews, and a
        // memory crunch is exactly when an uncapped second group
        // would hurt most.
        enforceFpEngineSleepLimit(limit)
        // Same pressure pass, second squeeze (v0.127.0): the tab
        // thumbnails are up to ~11 MB of pure decoration and were
        // previously left alone by this pass. They are dropped
        // unconditionally at these levels — even when few engines
        // are alive — and recapture themselves on the next page
        // finish or switch-away.
        ThumbnailCache.clearAll()
    }

    // MainActivity forwards onTrimMemory to this bridge. The
    // handler is re-read through rememberUpdatedState so the
    // registered lambda always sees the current tab list, and the
    // registration is cleared when this composition leaves, so
    // nothing keeps a dead UI alive.
    val latestTrimHandler = rememberUpdatedState<(Int) -> Unit> {
        level -> enforceTrimMemoryPressure(level)
    }
    DisposableEffect(Unit) {
        com.tuxunbrowse.android.MainActivity.trimMemoryListener = { level ->
            latestTrimHandler.value(level)
        }
        onDispose {
            com.tuxunbrowse.android.MainActivity.trimMemoryListener = null
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

    // Open a page-requested new window inside the FINGERPRINT
    // group (v0.172.0): wired up right after newFpTab is declared
    // below (local functions cannot be forward-referenced, so the
    // new-window handlers above reach the fingerprint group
    // through this holder, exactly like
    // pendingEngineAnswersCleanup below).
    var newWindowInFpGroupHandler: (String) -> Unit = {}

    // Clipboard-read permission plumbing (v0.45.0). Every path that
    // is not an explicit user "allow" answers deny — dismiss,
    // tab switch, tab close/recreate/sleep, engine dismiss — so the
    // GeckoResult in the tab layer is never left hanging (a hanging
    // result freezes the requesting page's script). The tab layer
    // additionally drains pending answers itself on destroy/sleep
    // and reports through the "gone" callback, which lands here.
    fun promoteNextClipboardPermission() {
        if (geckoClipboardPermission != null) return
        // One page dialog at a time across ALL kinds (v0.102.0): the
        // JS / auth / SSL dialogs already refuse a newcomer while one
        // of them is up; the clipboard prompt now waits in its queue
        // instead of stacking a second dialog underneath (or over)
        // one of them. The LaunchedEffect next to the clipboard
        // lifetime wiring re-runs this once those dialogs clear.
        if (jsPromptRequest != null || authPromptRequest != null ||
            sslPromptRequest != null
        ) return
        // The two page menus hold the stage too (v0.122.0): a queued
        // clipboard prompt must not pop up over a long-press or
        // selection menu the user is still deciding on.
        if (pageContextMenu != null || geckoSelectionMenu != null) return
        while (geckoClipboardQueue.isNotEmpty()) {
            val next = geckoClipboardQueue.removeAt(0)
            if (tabs.any { it.id == next.tabId } &&
                next.tabId == activeTabId
            ) {
                geckoClipboardPermission = next
                return
            }
            next.respond(false)
        }
    }

    fun answerClipboardPermission(allow: Boolean) {
        val current = geckoClipboardPermission ?: return
        geckoClipboardPermission = null
        current.respond(allow)
        promoteNextClipboardPermission()
    }

    fun denyClipboardPermissionsForTab(tabId: Int) {
        val queued = geckoClipboardQueue.filter { it.tabId == tabId }
        if (queued.isNotEmpty()) {
            geckoClipboardQueue.removeAll(queued)
            queued.forEach { it.respond(false) }
        }
        if (geckoClipboardPermission?.tabId == tabId) {
            answerClipboardPermission(false)
        }
    }

    fun handleClipboardPermissionRequest(
        tabId: Int,
        uri: String,
        respond: (Boolean) -> Unit
    ) {
        val tab = tabs.firstOrNull { it.id == tabId }
        if (tab == null || tabId != activeTabId) {
            // Background (or already gone) tab: no prompt, clean deny.
            respond(false)
            return
        }
        val state = GeckoClipboardPermissionState(
            tabId = tabId,
            uri = uri.ifBlank { tab.url },
            respond = respond
        )
        if (geckoClipboardPermission == null && jsPromptRequest == null &&
            authPromptRequest == null && sslPromptRequest == null &&
            pageContextMenu == null && geckoSelectionMenu == null
        ) {
            geckoClipboardPermission = state
        } else {
            // Either another clipboard prompt is already showing, or
            // a JS / auth / SSL dialog owns the stage (v0.102.0), or
            // a long-press / selection menu does (v0.122.0):
            // queue — promotion re-checks the stage is free, the tab
            // is still there and still active before showing.
            geckoClipboardQueue.add(state)
        }
    }

    fun makeTab(
        engine: Engine,
        incognito: Boolean,
        url: String,
        desktopMode: Boolean = false,
        fingerprint: Boolean = false
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
            onVisit = { t, u -> if (!fingerprint) recordVisit(t, u) },
            shouldUseWebView = { target ->
                // A pending one-time bypass for exactly this URL (the
                // load-error page's other-engine action, v0.52.0) wins
                // over the compat memory and is consumed on use, so
                // later navigations follow the compat list again.
                if (created?.id?.let { compatBypassLoads[it] } == target) {
                    created?.id?.let { compatBypassLoads.remove(it) }
                    false
                } else {
                    engine == Engine.GECKO &&
                        isCompatHost(hostOf(target), compatSites.toSet())
                }
            },
            onCompatRedirect = { target -> compatRedirectHandler(target) },
            isAdBlockEnabled = { store.adBlockEnabled },
            onPageContextMenu = { link, image ->
                val src = created
                pageContextMenu = PageContextMenuState(
                    linkUrl = link,
                    imageUrl = image,
                    engine = src?.engine ?: engine,
                    incognito = src?.incognito ?: incognito,
                    tabId = src?.id ?: -1
                )
            },
            onGeckoSelectionShow = { menu ->
                val src = created
                if (src != null) {
                    geckoSelectionMenu = GeckoSelectionMenuState(
                        tabId = src.id,
                        incognito = src.incognito,
                        menu = menu
                    )
                }
            },
            onGeckoSelectionHide = {
                val src = created
                if (src != null && geckoSelectionMenu?.tabId == src.id) {
                    geckoSelectionMenu = null
                }
            },
            onGeckoClipboardPermissionRequest = { uri, respond ->
                val src = created
                if (src != null) {
                    handleClipboardPermissionRequest(src.id, uri, respond)
                } else {
                    respond(false)
                }
            },
            onGeckoClipboardPermissionDismiss = {
                val src = created
                if (src != null) {
                    denyClipboardPermissionsForTab(src.id)
                }
            },
            onGeckoClipboardPermissionGone = {
                val src = created
                if (src != null) {
                    denyClipboardPermissionsForTab(src.id)
                }
            },
            textZoomProvider = { store.textZoomPercent },
            webDarkProvider = { store.webDarkMode },
            readerFontSizeProvider = { store.readerFontSize },
            readerBackgroundProvider = { store.readerBackground },
            appDarkProvider = { isAppDarkTheme(context, store.themeMode) },
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
                        referer = created?.url,
                        cookieHeader = if (fingerprint) {
                            com.tuxunbrowse.android.engine
                                .FpStorage.cookieFor(
                                    dlUrl,
                                    store.fingerprintStorageName()
                                )
                        } else {
                            null
                        }
                    )
                }
            },
            onDirectDownload = { target ->
                // A tapped direct file link, intercepted by either
                // engine before navigating. URL-type dispatch, same
                // as the download listener: startSystemDownload
                // fills in the current session's Cookie itself; the
                // User-Agent is the tab's live page UA when it has
                // one (pageUserAgent), and the Referer is the
                // page the tap happened on (the tab has not moved).
                val fileName = resolveDownloadFileName(target, null, null)
                downloadWithMode(
                    target, fileName,
                    fromIncognito = created?.incognito == true
                ) {
                    startSystemDownload(
                        context, target, pageUserAgent(created), null, null,
                        referer = created?.url,
                        cookieHeader = if (fingerprint) {
                            com.tuxunbrowse.android.engine
                                .FpStorage.cookieFor(
                                    target,
                                    store.fingerprintStorageName()
                                )
                        } else {
                            null
                        }
                    )
                }
                android.widget.Toast.makeText(
                    context, "已开始下载 $fileName",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            isAdHideSlotsEnabled = { store.adBlockEnabled },
            onBlockedRequest = { store.noteBlocked() },
            guardScriptProvider = {
                !fingerprint && store.guardScriptEnabled
            },
            privacySignalsProvider = {
                PrivacySignals(
                    doNotTrack = store.doNotTrackEnabled,
                    globalPrivacyControl =
                        store.globalPrivacyControlEnabled,
                    webRtcDisabled = store.webRtcDisabled
                )
            },
            fingerprintProfileProvider = {
                if (fingerprint) store.resolvedFingerprint() else null
            },
            fpStorageNameProvider = {
                if (fingerprint) store.fingerprintStorageName()
                else null
            },
            fingerprintProtectionProvider = {
                store.fingerprintProtection
            },
            trackingStrictProvider = {
                store.trackingProtectionLevel ==
                    TrackingProtectionLevel.STRICT
            }
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
            // The wipe has run, so the process-death marker the
            // startup pass checks (v0.128.0) is settled too.
            store.webViewIncognitoDirty = false
        }
    }

    fun newTab(
        engine: Engine,
        incognito: Boolean,
        url: String = HOME_URL,
        desktopMode: Boolean = false,
        activate: Boolean = true,
        applyDesktopMemory: Boolean = true
    ): BrowserTab {
        // Desktop-mode memory (v0.56.0): desktop mode is a property
        // of the tab, and the per-site list only decides how a NEW
        // tab starts. A new tab opened on a remembered host starts
        // in desktop mode; navigation inside an existing tab never
        // changes that tab's mode, and incognito tabs neither read
        // nor write the list. Callers carrying an explicit live
        // value (a reopened closed tab's captured mode, a new
        // window inheriting its opener's mode) pass
        // applyDesktopMemory = false so the live value wins.
        val initialDesktopMode = desktopMode ||
            (applyDesktopMemory && !incognito &&
                isDesktopHost(hostOf(url), desktopSites.toSet()))
        // The tab being switched away from gets its one switch-away
        // thumbnail capture (the other capture point is page finish).
        // Deferred: drawing the bitmap in the same frame the switch
        // crossfade starts could stall the transition's first frame.
        // A background tab (activate = false, v0.48.0) switches away
        // from nothing, so there is no capture and no active change:
        // the new tab stays uncreated-engine (TabContent composes
        // only the active tab) until the user selects it, exactly
        // like a restored tab under the v0.23.0 lazy contract, and
        // the activeTabId-keyed sleep pass (v0.41.0) does not run.
        if (activate) activeTab()?.captureThumbnailDeferred()
        val tab = makeTab(
            effectiveEngine(engine, url), incognito, url, initialDesktopMode
        )
        tabs.add(tab)
        if (activate) activeTabId = tab.id
        return tab
    }

    // Filled in further down (next to the file-picker and
    // system-permission launchers it cleans up): answers a pending
    // file-picker request with "cancelled" and a pending
    // system-permission request with "nothing granted". Declared
    // here because the tab-removal paths below call it, and local
    // functions must be declared before their callers.
    var pendingEngineAnswersCleanup: () -> Unit = {}

    /**
     * Release the transient app-level UI state that belongs to a
     * tab which is about to go away (v0.78.0 release audit). Until
     * this version only the clipboard-read requests were answered
     * per tab on close; the JS / auth / SSL dialogs, the shared
     * file-picker answer and the system-permission answer are
     * app-global singletons with no tab id, and nothing answered
     * them when their tab closed — the dialog stayed on screen over
     * the next tab and its answer went into a destroyed engine
     * (harmless only thanks to the engine-side try/catch wrappers),
     * and a Gecko fullscreen tab closed mid-stream could leave
     * [geckoFullscreen] set, hiding the chrome and holding the
     * screen on for a tab that no longer existed. These prompts can
     * only ever belong to the ACTIVE tab (it is the only composed,
     * page-interacting one), so the non-force path answers them
     * only when the tab going away is the active one — closing a
     * background tab never touches the active tab's pending dialog.
     * Every answer uses the same "cancel" value as the v0.34.0
     * composition-disposal safety net. [force] is for close-all,
     * where every tab goes away at once.
     *
     * "Active" spans both tab groups (v0.168.0): the fingerprint
     * group has its own active tab ([activeFpTabId]), and its pages
     * raise the very same singleton pendings (SSL / auth dialogs,
     * file picker, system permissions — fingerprint tabs are
     * WebView tabs). Until this version the check compared only
     * against the normal group's [activeTabId], so closing the
     * active fingerprint tab answered nothing: its dialog stayed
     * on screen over the next tab and its answer went into a
     * destroyed WebView. The gate itself is the pure
     * [shouldReleaseTransientUiForTab]; tab ids are unique across
     * both groups, so a background tab of either group still never
     * matches.
     */
    fun releaseTransientUiForRemovedTab(tabId: Int, force: Boolean = false) {
        if (!shouldReleaseTransientUiForTab(
                tabId, force, activeTabId, activeFpTabId
            )
        ) return
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
        pendingEngineAnswersCleanup()
        if (force || geckoSelectionMenu?.tabId == tabId) {
            geckoSelectionMenu = null
        }
        pageContextMenu = null
        fullscreenView = null
        geckoFullscreen = false
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
        // The old tab's pending clipboard-read request is answered
        // deny before its engine goes away (v0.45.0).
        denyClipboardPermissionsForTab(tab.id)
        // Same for its pending dialogs / picker / permission
        // answers and fullscreen state (v0.78.0): recreation is a
        // removal of the old tab object, so it goes through the
        // same transient-UI release as closing.
        releaseTransientUiForRemovedTab(tab.id)
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
        // The landing group follows the SOURCE tab (v0.172.0
        // new-tab routing audit): a new window asked for by a
        // fingerprint tab opens inside the fingerprint group,
        // wearing the profile — until this version every new
        // window landed in the normal group whatever tab asked.
        // (The WebView side of this path is dormant today —
        // supportMultipleWindows stays off per the v0.113.0
        // record — so this is the routing the handler will use
        // if that gate is ever opened, and the rule is pinned
        // now rather than rediscovered then.)
        if (newTabGroupForSource(
                source.id, fpTabs.map { it.id }.toSet()
            ) == NewTabGroup.FINGERPRINT
        ) {
            newWindowInFpGroupHandler(target)
        } else {
            // The new window inherits the opener tab's desktop mode
            // verbatim (the pre-v0.56.0 behaviour); the per-site list
            // does not override a live value.
            newTab(
                source.engine, source.incognito, target, source.desktopMode,
                applyDesktopMemory = false
            )
        }
        screen = Screen.BROWSER
    }

    // Same for Gecko tabs: the source tab's onNewSession already
    // created and opened the new session; build the new tab around it.
    // attachAdoptedGeckoSession wires the new tab's delegates into the
    // session synchronously, before Gecko starts loading the page.
    geckoNewSessionHandler = { source, session, uri ->
        if (newTabGroupForSource(
                source.id, fpTabs.map { it.id }.toSet()
            ) == NewTabGroup.FINGERPRINT
        ) {
            // Unreachable today — fingerprint tabs are WebView-only,
            // so no Gecko session can ever belong to one — but the
            // group rule is total: never adopt a session into the
            // normal group on a fingerprint tab's behalf. Close the
            // handed-over session and open the address as a regular
            // fingerprint tab instead.
            try {
                session.close()
            } catch (t: Throwable) {
                // Already gone; nothing to close.
            }
            newWindowInFpGroupHandler(uri)
            screen = Screen.BROWSER
        } else {
            val tab = makeTab(source.engine, source.incognito, uri, source.desktopMode)
            tab.attachAdoptedGeckoSession(session, context)
            tabs.add(tab)
            activeTabId = tab.id
            screen = Screen.BROWSER
        }
    }

    // A WebView tab's rendering process was killed by the system: rebuild
    // the tab in place (same slot, same engine, same incognito flag) via
    // the usual tab-recreation path and reload the page it was on. Works
    // for incognito tabs too — only the URL is carried over, nothing is
    // written to disk that was not already there.
    renderProcessGoneHandler = { tab, deadUrl ->
        if (tab.engine == Engine.WEBVIEW && fpTabs.contains(tab)) {
            // Fingerprint tabs live in their own list (v0.148.0):
            // before this branch the handler only knew the normal
            // list, so a killed renderer left a fingerprint tab on
            // a permanent white screen with nothing recovering it.
            // Same posted-rebuild shape as the normal path below,
            // swapped inside fpTabs and rebuilt WITH the profile.
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                if (!fpTabs.contains(tab)) return@post
                // Same pre-replacement cleanup as recreateTab on
                // the normal path (v0.168.0): the dead tab's pending
                // clipboard answers are denied and — when it was
                // the active fingerprint tab — its pending dialogs /
                // picker / permission answers are released with
                // their cancel values instead of being left waiting
                // on a WebView whose renderer is gone.
                denyClipboardPermissionsForTab(tab.id)
                releaseTransientUiForRemovedTab(tab.id)
                val target = deadUrl.ifBlank { tab.url }
                val idx = fpTabs.indexOf(tab)
                val replacement = makeTab(
                    engine = Engine.WEBVIEW,
                    // Fingerprint tabs are NOT incognito (v0.166.0
                    // incognito audit): every other fingerprint
                    // creation site (newFpTab, session restore,
                    // profile switch) builds them with incognito =
                    // false — the mode's disclosed model is a shared
                    // cookie jar and its own tab archive, with only
                    // history suppressed (via the fingerprint flag).
                    // This branch used to pass true, so a tab whose
                    // renderer died silently changed identity:
                    // thumbnails stopped, its downloads suddenly
                    // took the incognito reminder path, and the flag
                    // flipped back on the next cold restore.
                    incognito = false,
                    url = tab.url,
                    desktopMode = tab.desktopMode,
                    fingerprint = true
                )
                fpTabs[idx] = replacement
                if (activeFpTabId == tab.id) {
                    activeFpTabId = replacement.id
                }
                tab.destroy()
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
        } else if (tab.engine == Engine.WEBVIEW && tabs.contains(tab)) {
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

    fun addDesktopSite(domain: String) {
        store.addDesktopSite(domain)
        if (domain !in desktopSites) desktopSites.add(domain)
    }

    fun removeDesktopSite(domain: String) {
        store.removeDesktopSite(domain)
        desktopSites.remove(domain)
    }

    
    /**
     * Forget the desktop-mode memory covering [host]: every stored
     * entry this host belongs to (the host itself may have been
     * remembered through a parent domain) is removed.
     */
    fun removeDesktopMemoryForHost(host: String) {
        desktopSites.filter { hostMatchesSite(host, it) }
            .forEach { removeDesktopSite(it) }
    }

    fun clearDesktopSites() {
        desktopSites.toList().forEach { removeDesktopSite(it) }
    }

    /**
     * Forget the compat memory covering [host]: every stored entry
     * this host belongs to (the host itself may be remembered
     * through a parent domain, which only a backup import can
     * create) is removed. Twin of [removeDesktopMemoryForHost];
     * exact-host removal alone silently left parent-domain entries
     * in place while the UI claimed the memory was gone
     * (v0.61.0 regression fix).
     */
    fun removeCompatMemoryForHost(host: String) {
        compatSites.filter { hostMatchesSite(host, it) }
            .forEach { removeCompatSite(it) }
    }

    fun switchTabEngine(tab: BrowserTab, newEngine: Engine, rememberSite: Boolean) {
        if (tab.engine == newEngine) {
            // Already on the picked engine: the one promise the
            // switch dialog can still make in this state is
            // cancelling the site's compat memory (it offers the
            // switch whenever a Gecko tab sits on a compat-listed
            // site — reachable after an error-page one-time bypass
            // or a backup import). Honour it instead of silently
            // doing nothing (v0.61.0 regression fix).
            if (newEngine == Engine.GECKO && rememberSite) {
                val host = hostOf(tab.url)
                if (host != null &&
                    compatSites.any { hostMatchesSite(host, it) }
                ) {
                    removeCompatMemoryForHost(host)
                    android.widget.Toast.makeText(
                        context, "已取消这个网站的自动兼容记忆",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            return
        }
        val host = hostOf(tab.url)
        if (rememberSite && host != null) {
            when (newEngine) {
                Engine.WEBVIEW -> addCompatSite(host)
                Engine.GECKO -> removeCompatMemoryForHost(host)
            }
        }
        replaceTabEngine(tab, newEngine)
    }

    /**
     * The load-error page's recovery action (v0.52.0): reopen the
     * failed URL in the OTHER engine, this tab only, this once.
     * Deliberately NOT switchTabEngine: that path can write the
     * per-site compat list (rememberSite), and the user has not said
     * "always use this engine for this site" — they only want this
     * failed page tried in the other engine. So this goes through
     * replaceTabEngine directly (same slot, same desktop mode, same
     * incognito flag via recreateTab) and never touches the compat
     * list. One wrinkle: if the failed host is IN the compat list,
     * a fresh Gecko tab would be bounced straight back to WebView by
     * its own compat check before loading anything — so the Gecko
     * direction registers a one-time bypass for exactly the failed
     * URL (see compatBypassLoads), consumed by that first load.
     * If the other engine fails too, the new tab's own loadError
     * brings the error page back with the button now pointing back
     * the other way, because the label follows the tab's engine.
     */
    fun openLoadErrorInOtherEngine(tab: BrowserTab, failedUrl: String) {
        // Fingerprint tabs have no other engine (v0.172.0): the
        // mode is WebView-only, and recreateTab below only knows
        // the normal list, so without this guard the call would
        // silently reload the same tab in the same engine and
        // look like a switch that never happened. The fingerprint
        // shell's error page hides the switch button and says why
        // (see FingerprintShell); this guard is the second net
        // for any other caller.
        if (fpTabs.contains(tab)) {
            android.widget.Toast.makeText(
                context, "指纹模式只用系统 WebView，不能换内核打开",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        if (!failedUrl.startsWith("http://") &&
            !failedUrl.startsWith("https://")
        ) return
        val other = when (tab.engine) {
            Engine.GECKO -> Engine.WEBVIEW
            Engine.WEBVIEW -> Engine.GECKO
        }
        val replacement = replaceTabEngine(tab, other)
        if (other == Engine.GECKO) {
            compatBypassLoads[replacement.id] = failedUrl
        }
        if (replacement.url != failedUrl) replacement.load(failedUrl)
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
        // A clipboard-read request from this tab dies with it:
        // answer deny first so the page never hangs (v0.45.0).
        denyClipboardPermissionsForTab(tab.id)
        // Its pending dialogs / picker / permission answers and its
        // fullscreen state die with it too (v0.78.0, see helper).
        releaseTransientUiForRemovedTab(tab.id)
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

    /** The fingerprint tab currently shown in fingerprint mode. */
    fun activeFpTab(): BrowserTab? =
        fpTabs.firstOrNull { it.id == activeFpTabId }

    /**
     * Open a fingerprint tab (always WebView, never history).
     * [activate] = false is the background-open form (v0.172.0,
     * the long-press menu's rule since v0.48.0): the tab joins
     * the group without stealing the screen from the page the
     * user is reading.
     */
    fun newFpTab(url: String = HOME_URL, activate: Boolean = true): BrowserTab {
        val tab = makeTab(Engine.WEBVIEW, false, url, fingerprint = true)
        fpTabs.add(tab)
        if (activate) activeFpTabId = tab.id
        return tab
    }

    // The new-window handlers (declared above, before this
    // function existed in scope) reach the fingerprint group
    // through this holder (v0.172.0).
    newWindowInFpGroupHandler = { target -> newFpTab(target) }

    /** Close a fingerprint tab; the group never sits empty. */
    fun closeFpTab(tab: BrowserTab) {
        val idx = fpTabs.indexOf(tab)
        denyClipboardPermissionsForTab(tab.id)
        // Same transient-UI release as closeTab (v0.168.0): the
        // helper's active check spans both groups' active tabs, so
        // closing the ACTIVE fingerprint tab now answers its
        // pending dialogs / picker / permission requests with
        // their cancel values, exactly like the normal path.
        releaseTransientUiForRemovedTab(tab.id)
        tab.destroy()
        fpTabs.remove(tab)
        if (fpTabs.isEmpty()) {
            newFpTab()
        } else if (activeFpTabId == tab.id) {
            activeFpTabId = fpTabs[minOf(idx, fpTabs.size - 1)].id
        }
    }

    /** Enter fingerprint mode (from 设置 → 隐私与数据 → 指纹模式). */
    fun enterFingerprintMode() {
        if (fpTabs.isEmpty()) newFpTab()
        fingerprintMode = true
        screen = Screen.BROWSER
        // The mode's proxy window opens with the mode (v0.197.0):
        // the override is app-wide, so it is applied exactly
        // here and cleared in exitFingerprintMode, nowhere else.
        com.tuxunbrowse.android.engine.FpProxyControl
            .apply(store.fpProxy)
    }

    /**
     * Enter fingerprint mode with a brand-new fingerprint tab
     * (v0.188.0): the incognito picker's fingerprint option —
     * unlike plain entry it always opens a fresh tab on the
     * current profile instead of landing on whatever tab the
     * mode last showed.
     */
    fun enterFingerprintModeWithNewTab() {
        newFpTab()
        fingerprintMode = true
        screen = Screen.BROWSER
        com.tuxunbrowse.android.engine.FpProxyControl
            .apply(store.fpProxy)
    }

    /** Leave fingerprint mode; its tabs stay as they are. */
    fun exitFingerprintMode() {
        fingerprintMode = false
        // The proxy window closes with the mode (v0.197.0).
        com.tuxunbrowse.android.engine.FpProxyControl.clear()
    }

    /**
     * Switch the active fingerprint profile (v0.144.0): store the
     * choice and rebuild every fingerprint tab on it, keeping
     * URLs. A live tab's native UA and document-start handler are
     * creation-time state, so a rebuild is the only way to swap
     * profiles without a half-applied tab; per-tab history stacks
     * reset, which the mode never promised to keep.
     */
    fun applyFingerprintProfile(
        regionId: String,
        timezoneId: String,
        deviceId: String,
        language: String
    ) {
        store.fingerprintRegionId = regionId
        store.fingerprintTimezoneId = timezoneId
        store.fingerprintDeviceId = deviceId
        store.fingerprintLanguage = language
        // The proxy is saved per combination (v0.198.0): with
        // the selection now pointing at the new combination,
        // store.fpProxy reads THAT combination's config, and
        // while the mode is open the routing follows the
        // persona — a usable config applies, anything else
        // clears, so the previous combination's proxy never
        // leaks into the new one's window.
        if (fingerprintMode) {
            val proxy = store.fpProxy
            if (proxy.isUsable) {
                com.tuxunbrowse.android.engine.FpProxyControl
                    .apply(proxy)
            } else {
                com.tuxunbrowse.android.engine.FpProxyControl
                    .clear()
            }
        }
        if (fpTabs.isEmpty()) return
        val urls = fpTabs.map { it.url }
        val activeIdx = fpTabs.indexOfFirst { it.id == activeFpTabId }
        // Every fingerprint tab goes away at once, so — exactly
        // like closeAllTabs on the normal group — the transient-UI
        // release runs unconditionally first (v0.168.0): the active
        // fingerprint tab's pending dialogs / picker / permission
        // answers get their cancel values instead of being left
        // waiting on WebViews this rebuild is about to destroy.
        // (The per-tab clipboard deny below stays as the second
        // net, same as the normal group's close paths.)
        releaseTransientUiForRemovedTab(activeFpTabId, force = true)
        fpTabs.toList().forEach { old ->
            denyClipboardPermissionsForTab(old.id)
            old.destroy()
        }
        fpTabs.clear()
        urls.forEach { newFpTab(it) }
        if (activeIdx in fpTabs.indices) {
            activeFpTabId = fpTabs[activeIdx].id
        }
    }

    /** Reopen one entry of the recently-closed stack and drop it. */
    /**
     * A protection-options toggle (v0.190.0): persist the new
     * switch set, then rebuild the fingerprint tabs through
     * the profile path so every live tab re-reads its payload
     * (with the new switches) at creation. Outside the mode
     * there are no tabs to rebuild and the next entry picks
     * the switches up from the store.
     */
    fun applyFingerprintProtection(next: FingerprintProtection) {
        fingerprintProtection = next
        store.fingerprintProtection = next
        if (fpTabs.isEmpty()) return
        applyFingerprintProfile(
            store.fingerprintRegionId,
            store.fingerprintTimezoneId,
            store.fingerprintDeviceId,
            store.fingerprintLanguage
        )
    }

    fun reopenClosedTab(info: ClosedTabInfo) {
        recentClosedTabs.remove(info)
        // The captured mode is a live value, like a saved session:
        // it wins over the per-site list (v0.56.0).
        newTab(
            info.engine, false, info.url, info.desktopMode,
            applyDesktopMemory = false
        )
        screen = Screen.BROWSER
    }

    fun closeAllTabs() {
        // Pending clipboard-read requests are answered deny before
        // every engine goes away (v0.45.0).
        tabs.toList().forEach { denyClipboardPermissionsForTab(it.id) }
        // Every tab goes away, so the transient-UI release runs
        // unconditionally (v0.78.0) — same answers as closeTab.
        releaseTransientUiForRemovedTab(activeTabId, force = true)
        val hadIncognito = tabs.any { it.incognito }
        // Every normal tab goes onto the recently-closed stack
        // (incognito tabs are skipped inside pushClosedTab).
        // Pushed in reverse tab order (v0.131.0 tab-sheet audit):
        // pushClosedTab puts each entry on TOP of the stack, so
        // walking the tab list backwards leaves the FIRST tab on
        // top — reopening the batch one by one then rebuilds the
        // tabs in their original left-to-right order instead of
        // reversed.
        tabs.toList().asReversed().forEach { pushClosedTab(it) }
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
    /**
     * Close every tab except [keep] (v0.72.0: generalized from
     * closeOtherTabs so the tab card's long-press menu can keep an
     * arbitrary tab, not just the active one). If the active tab is
     * among the closed, [keep] becomes the active tab.
     */
    fun closeOtherTabsKeeping(keep: BrowserTab) {
        val others = tabs.filter { it.id != keep.id }
        if (others.isEmpty()) return
        // If the active tab is among the ones going away, its
        // transient UI state goes with it (v0.78.0); when the
        // active tab is the one kept, nothing is touched. (The
        // clipboard answers of the closed tabs are drained by
        // BrowserTab.destroy itself, as before.)
        if (others.any { it.id == activeTabId }) {
            releaseTransientUiForRemovedTab(activeTabId, force = true)
        }
        val closedIncognito = others.any { it.incognito }
        // Reverse order, same reason as closeAllTabs (v0.131.0):
        // the stack top ends up being the earliest of the batch,
        // so reopening restores the original relative order.
        others.asReversed().forEach { pushClosedTab(it) }
        others.forEach { it.destroy() }
        tabs.removeAll(others)
        if (activeTab() == null) activeTabId = keep.id
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

    /** Close every tab except the active one (see above). */
    fun closeOtherTabs() {
        val keep = activeTab() ?: return
        closeOtherTabsKeeping(keep)
    }

    /**
     * Toggle one tab's bookmark (v0.72.0: extracted from the star
     * button's lambda so the tab card's long-press menu toggles the
     * bookmark of an arbitrary tab through the exact same code —
     * same home-page refusal, same URL rule, same storage).
     */
    fun toggleBookmarkForTab(tab: BrowserTab) {
        if (tab.isHome) {
            // The star sits in the home tab's bar too; without this
            // branch the tap silently did nothing (v0.61.0
            // regression fix — every other home-tab action answers
            // with the same kind of toast).
            android.widget.Toast.makeText(
                context, "先打开一个网页再加入书签",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val existing = bookmarks.firstOrNull { it.url == tab.url }
        if (existing != null) {
            bookmarks.remove(existing)
        } else if (tab.url.startsWith("http")) {
            bookmarks.add(0, Bookmark(tab.title.ifBlank { tab.url }, tab.url))
        }
        store.saveBookmarks(bookmarks)
    }

    /**
     * Batch-bookmark a set of tabs (v0.76.0, the tab sheet's
     * multi-select "加入书签"). Only normal tabs take part: an
     * incognito tab is skipped and COUNTED (the caller reports the
     * skip honestly in its toast — incognito never writes a
     * bookmark, the same rule as the single-tab paths), the home
     * page and non-web addresses are not bookmarkable, and a tab
     * whose URL is already bookmarked is not added twice. Returns
     * (addedCount, skippedIncognitoCount).
     */
    fun bookmarkTabs(selected: List<BrowserTab>): Pair<Int, Int> {
        var added = 0
        var skippedIncognito = 0
        for (tab in selected) {
            if (tab.incognito) {
                skippedIncognito++
                continue
            }
            if (tab.isHome || !tab.url.startsWith("http")) continue
            if (bookmarks.any { it.url == tab.url }) continue
            bookmarks.add(0, Bookmark(tab.title.ifBlank { tab.url }, tab.url))
            added++
        }
        if (added > 0) store.saveBookmarks(bookmarks)
        return added to skippedIncognito
    }

    
    // Session restore: reopen the tabs that were open when the app was
    // last closed (normal tabs only — incognito tabs are never saved).
    // Runs once, before the persistence effect below is allowed to save
    // anything, so a fresh start can never wipe the saved session first.
    LaunchedEffect(Unit) {
        // Incognito WebView leftovers from a dead process
        // (v0.128.0 process-death audit). The shared WebView
        // cookie jar is wiped when the last incognito WebView tab
        // closes — but if the process was killed first, that wipe
        // never ran, while the pause-time cookie flush had already
        // persisted the jar, incognito cookies included. The dirty
        // marker says an incognito WebView tab existed when the
        // process died: finish the wipe now, before any page can
        // read the jar. Same documented side effect as the
        // close-time wipe (normal WebView tabs are signed out —
        // the jar is shared and there is no per-tab alternative).
        if (store.webViewIncognitoDirty) {
            try {
                android.webkit.CookieManager.getInstance()
                    .removeAllCookies(null)
                android.webkit.CookieManager.getInstance().flush()
                android.webkit.WebStorage.getInstance().deleteAllData()
            } catch (t: Throwable) {
                // Best-effort: a failed wipe must not break startup.
            }
            store.webViewIncognitoDirty = false
        }
        // Background fill of the three big lists (v0.28.0): parse the
        // stored JSON off the main thread, then publish the results
        // into the state lists above. This must stay the FIRST step of
        // this effect — the retention cleanup below reads history, and
        // tab restore further down is what makes user actions on these
        // lists possible at all, so the fill always lands first.
        val loadedLists = kotlinx.coroutines.withContext(
            kotlinx.coroutines.Dispatchers.Default
        ) {
            store.loadStartupLists()
        }
        bookmarks.addAll(loadedLists.bookmarks)
        history.addAll(loadedLists.history)
        bookmarkFolders.addAll(loadedLists.bookmarkFolders)
        // One-time migration (v0.91.0): the removed reading list
        // and speed dials fold into the bookmarks (see Store).
        // Runs after the fill above so the de-dup sees the real
        // bookmark list; the migrated entries are then published
        // into the state list from the store's own reload. Startup
        // audit (v0.95.0): the reload now happens only when the
        // migration actually merged something — once it has run,
        // every later cold start skips the second full bookmark
        // parse entirely (the fill above already loaded the list).
        val migratedBookmarks = kotlinx.coroutines.withContext(
            kotlinx.coroutines.Dispatchers.Default
        ) {
            if (store.migrateRemovedListsToBookmarks()) {
                store.loadBookmarks()
            } else {
                null
            }
        }
        if (migratedBookmarks != null &&
            migratedBookmarks.size != bookmarks.size
        ) {
            bookmarks.clear()
            bookmarks.addAll(migratedBookmarks)
        }
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
                // The re-save serializes the whole kept list (up to
                // the 500-entry cap) back to JSON — that is done on
                // the background dispatcher too (v0.79.0 audit), and
                // only then is the trimmed list published, so the
                // in-memory list and the stored one never disagree.
                kotlinx.coroutines.withContext(
                    kotlinx.coroutines.Dispatchers.Default
                ) {
                    store.saveHistory(kept)
                }
                history.clear()
                history.addAll(kept)
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
            // The restore filter and the active-index mapping must
            // agree (v0.109.0 field audit): the stored index counts
            // positions in the SAVED list, so it is mapped through
            // this same filter by restoredActiveIndex instead of
            // being applied raw to the filtered list.
            val usableSavedTab: (SavedTab) -> Boolean =
                { it.url.startsWith("http") || it.url == HOME_URL }
            val usable = if (store.startupRestore) {
                saved.filter(usableSavedTab)
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
                activeTabId = tabs[
                    restoredActiveIndex(saved, savedActive, usableSavedTab)
                        .coerceIn(0, tabs.lastIndex)
                ].id
            } else {
                newTab(store.mainEngine, false)
            }
        }
        // Fingerprint session restore (v0.161.0): the fp tab
        // group used to live only in memory, so a process death
        // (the system reclaiming the app while it sits in the
        // background) silently evaporated every fingerprint tab
        // while the normal session came back — the mode whose
        // whole point is a stable identity lost its session to
        // exactly the event the normal mode survives. Rebuild
        // the group from its own archive, lazily like the normal
        // restore (data only; the engine builds on first
        // display). The mode itself is NOT re-entered: entering
        // is a deliberate tap, and exitFingerprintMode already
        // promises the group survives leaving — a cold start
        // now keeps the same promise, no further.
        // Startup audit (v0.165.0): the restore is skipped
        // entirely when no fingerprint archive exists — one
        // string read on the store answers that. A user who
        // never entered fingerprint mode used to pay a full
        // background round-trip here on every cold start (parse
        // of an absent key, empty result) and, because this sits
        // before startupDone, that round-trip also delayed the
        // external-link / share consumers gated on startupDone.
        // With an archive present nothing changes.
        if (fpTabs.isEmpty() && store.hasFpTabsArchive) {
            val (savedFp, savedFpActive) =
                kotlinx.coroutines.withContext(
                    kotlinx.coroutines.Dispatchers.Default
                ) {
                    store.loadFpTabs()
                }
            // The active index is mapped through the SAME filter
            // that builds the restored list, via the shared
            // restoredActiveIndex helper — the discipline the
            // normal restore got in v0.109.0. The fp restore used
            // to coerce the raw stored index onto the FILTERED
            // list, so an unusable entry ahead of the active one
            // (a non-http URL left by an odd navigation) shifted
            // which tab came back active.
            val usableFpTab: (SavedTab) -> Boolean =
                { it.url.startsWith("http") || it.url == HOME_URL }
            val usableFp = savedFp.filter(usableFpTab)
            usableFp.forEach { st ->
                fpTabs.add(
                    makeTab(
                        Engine.WEBVIEW, false, st.url,
                        st.desktopMode, fingerprint = true
                    )
                )
            }
            if (fpTabs.isNotEmpty()) {
                activeFpTabId = fpTabs[
                    restoredActiveIndex(savedFp, savedFpActive, usableFpTab)
                        .coerceIn(0, fpTabs.lastIndex)
                ].id
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

    // Incognito WebView marker (v0.128.0): while any incognito
    // WebView tab exists, the store flag the startup pass checks
    // must be set, so a process death from here leaves the next
    // start able to finish the cookie-jar wipe. Setting it is
    // idempotent and cheap; clearing is the wipe's own job (the
    // close-time wipe above and the startup wipe both clear it).
    val hasIncognitoWebViewTab =
        tabs.any { it.incognito && it.engine == Engine.WEBVIEW }
    LaunchedEffect(hasIncognitoWebViewTab) {
        if (hasIncognitoWebViewTab) store.webViewIncognitoDirty = true
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

    // The fingerprint group's archive follows the exact same
    // discipline as the normal session's (v0.161.0): debounced,
    // gated on startupDone so the writer can never beat the
    // restore reader, same SavedTab mapping.
    LaunchedEffect(startupDone) {
        if (!startupDone) return@LaunchedEffect
        snapshotFlow {
            fpTabs.map { Triple(it.engine, it.url, it.desktopMode) } to
                fpTabs.indexOfFirst { it.id == activeFpTabId }
        }.debounce(400L).collect { (list, idx) ->
            // Never create an archive for a group that has never
            // held a tab (v0.173.0 regression fix): this writer and
            // the ON_PAUSE one used to save an EMPTY archive on
            // every install, so the v0.165.0 cold-start short
            // circuit (hasFpTabsArchive) could only ever fire on
            // the very first install — from the second cold start
            // on, every user paid the restore round-trip the fix
            // removed. An empty group has nothing to restore, so
            // skipping the write changes no restore outcome; once
            // an archive exists, writes proceed exactly as before.
            if (list.isNotEmpty() || store.hasFpTabsArchive) {
                store.saveFpTabs(
                    list.map { SavedTab(it.first, it.second, it.third) },
                    idx.coerceAtLeast(0)
                )
            }
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
        // Threading (v0.167.0): the read (up to the 8M-char cap,
        // through the content provider) and the parse run on a
        // background dispatcher; only the merge into the live
        // bookmark list and the result toast stay on the main
        // thread, exactly as before.
        // Merge shape (v0.200.0): WHICH rows to add is also
        // decided on the background dispatcher now, against a
        // URL set snapshotted here — the old main-thread merge
        // scanned the whole live list once per imported row
        // (quadratic; a large desktop export froze the UI).
        // The main thread only appends the decided rows.
        appScope.launch {
            try {
                val existingUrls =
                    bookmarks.map { it.url }.toHashSet()
                val (toAdd, skipped) =
                    withContext(Dispatchers.Default) {
                        val raw = readImportFileText(context, uri)
                        val head = raw.trimStart()
                        val isHtml = head.startsWith(
                            "<!DOCTYPE NETSCAPE", ignoreCase = true
                        ) || head.startsWith("<DL", ignoreCase = true)
                        var bad = 0
                        val parsed = if (isHtml) {
                            val (list, badCount) =
                                bookmarksFromHtml(raw)
                            bad = badCount
                            list
                        } else {
                            bookmarksFromJson(raw)
                        }
                        // De-duplicates within the file and
                        // against the existing list in one
                        // set pass (see selectNewBookmarks).
                        val (newOnes, dupes) = selectNewBookmarks(
                            existingUrls, parsed
                        )
                        newOnes to (bad + dupes)
                    }
                val imported = toAdd
                val added = toAdd.size
                bookmarks.addAll(toAdd)
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
    }

    // ---- Full backup / restore (settings page "备份与恢复") ----
    // A parsed backup waiting for the user's overwrite confirmation.
    var pendingBackup by remember { mutableStateOf<BackupData?>(null) }

    fun exportBackup() {
        // Threading (v0.167.0): the serialization and the Downloads
        // write used to run synchronously in the settings click
        // handler, on the main thread. The lists are snapshotted
        // here; everything else runs on IO and only the result
        // toast comes back. Same file, same name, same failure
        // cleanup (v0.107.0) as before.
        val bookmarksSnapshot = bookmarks.toList()
        val historySnapshot = history.toList()
        val foldersSnapshot = bookmarkFolders.toList()
        appScope.launch {
        val exportOk = withContext(Dispatchers.IO) {
        val json = backupToJson(
            bookmarksSnapshot, historySnapshot,
            BackupSettings(
                mainEngine = store.mainEngine,
                incognitoEngine = store.incognitoEngine,
                searchEngine = store.searchEngine,
                adBlockEnabled = store.adBlockEnabled,
                downloadMode = store.downloadMode,
                historyRetention = store.historyRetention,
                textZoomPercent = store.textZoomPercent,
                startupRestore = store.startupRestore,
                themeMode = store.themeMode,
                webDarkMode = store.webDarkMode,
                readerFontSize = store.readerFontSize,
                readerBackground = store.readerBackground,
                httpsFirstEnabled = store.httpsFirstEnabled,
                guardScriptEnabled = store.guardScriptEnabled,
                trackingProtectionLevel = store.trackingProtectionLevel,
                doNotTrackEnabled = store.doNotTrackEnabled,
                globalPrivacyControlEnabled =
                    store.globalPrivacyControlEnabled,
                webRtcDisabled = store.webRtcDisabled
            ),
            compatSites = store.loadCompatSites().sorted(),
            desktopSites = store.loadDesktopSites().sorted(),
            bookmarkFolders = allBookmarkFolders(
                bookmarksSnapshot, foldersSnapshot
            ),
            totalBlocked = store.totalBlocked
        )
        val fileName = "tuxunbrowse-backup.json"
        // MediaStore cleanup (v0.107.0 release audit): the insert
        // creates the Downloads entry BEFORE any byte is written, so
        // a failed or unwritable write must delete the entry again —
        // before this pass a failed export left an empty file behind
        // in Downloads, and a null output stream was silently
        // reported as success.
        var backupDestUri: android.net.Uri? = null
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
                backupDestUri = dest
                val out = resolver.openOutputStream(dest)
                    ?: throw IllegalStateException("open output failed")
                out.use {
                    it.write(json.toByteArray(Charsets.UTF_8))
                }
            } else {
                val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                java.io.File(dir, fileName).writeText(json, Charsets.UTF_8)
            }
            true
        } catch (t: Throwable) {
            try {
                backupDestUri?.let {
                    context.contentResolver.delete(it, null, null)
                }
            } catch (t2: Throwable) {
                // Best-effort cleanup of the partial entry.
            }
            false
        }
        }
        android.widget.Toast.makeText(
            context,
            if (exportOk) {
                "备份已导出到下载文件夹：tuxunbrowse-backup.json"
            } else {
                "导出失败，请重试"
            },
            android.widget.Toast.LENGTH_LONG
        ).show()
        }
    }

    /**
     * Merge a confirmed backup into the live data: lists merge with
     * de-duplication (bookmarks by URL, history by URL + time),
     * settings overwrite. Returns the per-list added counts for the
     * result toast.
     */
    fun applyBackup(data: BackupData): String {
        var addedBookmarks = 0
        // Set membership, the same shape the history merge below
        // already uses (v0.200.0): the per-row none{} scan this
        // replaces was quadratic in the size of a large backup.
        val bookmarkUrls = bookmarks.map { it.url }.toHashSet()
        data.bookmarks.distinctBy { it.url }.forEach { bm ->
            if (bookmarkUrls.add(bm.url)) {
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
        val s = data.settings
        s.mainEngine?.let { store.mainEngine = it }
        s.incognitoEngine?.let { store.incognitoEngine = it }
        s.searchEngine?.let { store.searchEngine = it }
        s.adBlockEnabled?.let { store.adBlockEnabled = it }
        s.downloadMode?.let { store.downloadMode = it }
        s.historyRetention?.let { store.historyRetention = it }
        s.startupRestore?.let { store.startupRestore = it }
        s.textZoomPercent?.let { percent ->
            store.textZoomPercent = percent
            tabs.forEach { it.applyTextZoom(percent) }
            GeckoHolder.applyFontSizeFactor(percent / 100f)
        }
        // Settings added to the backup slice in v0.51.0. Each one
        // takes effect through the same path its settings-page
        // switch uses, so an import behaves exactly like flipping
        // the switches by hand.
        s.themeMode?.let { mode ->
            store.themeMode = mode
            // The theme's single state source lives in MainActivity
            // (v0.20.0); this callback is how the settings page
            // reaches it, and the import goes through it too.
            onThemeModeChanged(mode)
        }
        s.webDarkMode?.let { enabled ->
            store.webDarkMode = enabled
            tabs.forEach { it.applyWebDark(enabled) }
        }
        // Reader typesetting (v0.58.0): plain Store writes, plus a
        // style re-apply so a tab currently in reader mode picks the
        // restored values up at once (same live path as the settings
        // page).
        s.readerFontSize?.let { store.readerFontSize = it }
        s.readerBackground?.let { store.readerBackground = it }
        // HTTPS first (v0.77.0): plain Store write — the four
        // inbound paths read it at open time, exactly like the
        // settings-page switch writes it.
        s.httpsFirstEnabled?.let { store.httpsFirstEnabled = it }
        s.guardScriptEnabled?.let { store.guardScriptEnabled = it }
        // Privacy section (v0.92.0): plain Store writes — WebView
        // tabs read the signals live at page start / load, and the
        // Gecko runtime is re-applied from prefs right away.
        s.trackingProtectionLevel?.let { store.trackingProtectionLevel = it }
        s.doNotTrackEnabled?.let { store.doNotTrackEnabled = it }
        s.globalPrivacyControlEnabled?.let {
            store.globalPrivacyControlEnabled = it
        }
        s.webRtcDisabled?.let { store.webRtcDisabled = it }
        if (s.trackingProtectionLevel != null ||
            s.globalPrivacyControlEnabled != null
        ) {
            GeckoHolder.applyPrivacySettings(context)
        }
        if (s.readerFontSize != null || s.readerBackground != null) {
            tabs.forEach { it.applyReaderStyle() }
        }
        // Compat sites merge like the lists above (union): restoring
        // a backup must never delete a site the user marked here.
        var addedCompat = 0
        data.compatSites.forEach { domain ->
            if (domain !in compatSites) {
                addCompatSite(domain)
                addedCompat++
            }
        }
        // Desktop-mode sites merge the same way (union, v0.56.0).
        var addedDesktop = 0
        data.desktopSites.forEach { domain ->
            if (domain !in desktopSites) {
                addDesktopSite(domain)
                addedDesktop++
            }
        }
        // Bookmark folder names merge the same way (union,
        // v0.72.0): a restore must never delete a folder the user
        // made on this device. (Folders of the imported bookmarks
        // themselves need no handling here — they derive from the
        // bookmark list.)
        var addedFolders = 0
        data.bookmarkFolders.forEach { name ->
            if (name !in bookmarkFolders) {
                bookmarkFolders.add(name)
                addedFolders++
            }
        }
        if (addedFolders > 0) store.saveBookmarkFolders(bookmarkFolders)
        // Blocked statistics (v0.74.0): a backup that carries them
        // restores them by overwrite (see BackupData.totalBlocked);
        // a backup without them leaves the current counters alone.
        var restoredBlocked = false
        data.totalBlocked?.let { total ->
            store.restoreBlockedStats(total)
            restoredBlocked = true
        }
        if (data.settings.overwriteSummary().isNotEmpty() ||
            addedCompat > 0 || addedDesktop > 0 ||
            restoredBlocked
        ) {
            // The store was just rewritten behind the settings
            // page's back: sync this level's search-engine mirror at
            // the source (same two-way sync the settings/home
            // pickers use) and bump the tick the settings page keys
            // its own mirrors on, so every page shows the imported
            // values at once instead of after a re-entry.
            searchEngineState = store.searchEngine
            settingsRefreshTick++
        }
        return "书签 +$addedBookmarks 条、历史 +$addedHistory 条" +
            (if (addedCompat > 0) "、兼容名单 +$addedCompat 个" else "") +
            (if (addedDesktop > 0) "、桌面版站点 +$addedDesktop 个" else "") +
            (if (addedFolders > 0) "、书签文件夹 +$addedFolders 个" else "") +
            (if (restoredBlocked) "、拦截统计已按备份恢复" else "")
    }

    // Backup import: system file picker. The file is validated by
    // its format marker first — anything else is refused in plain
    // language and no data is touched. A valid file still changes
    // nothing until the user confirms the overwrite dialog.
    val importBackupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        // Threading (v0.167.0): same move as the bookmark import —
        // the file read and the backup parse run off the main
        // thread; the confirmation dialog state is set back here.
        appScope.launch {
            try {
                val parsed = withContext(Dispatchers.Default) {
                    parseBackup(readImportFileText(context, uri))
                }
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
                            "历史 ${backupToConfirm.history.size} 条会合并进来，" +
                            "和现有的重复的会跳过，不会删掉现有的任何东西。" +
                            if (backupToConfirm.compatSites.isNotEmpty()) {
                                "\n\n备份里的兼容名单 ${backupToConfirm.compatSites.size} 个网站" +
                                    "也会合并进来，现有的兼容名单不会被删掉。"
                            } else {
                                ""
                            } +
                            if (backupToConfirm.desktopSites.isNotEmpty()) {
                                "\n\n备份里的桌面版站点 ${backupToConfirm.desktopSites.size} 个网站" +
                                    "也会合并进来，现有的桌面版站点不会被删掉。"
                            } else {
                                ""
                            } +
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
     *
     * Returns whether the Gecko-side clear was actually dispatched
     * (v0.125.0 site-info audit): GeckoHolder.clearDataFromHost
     * returns false when the Gecko runtime has not started this
     * session (both engine choices set to the system WebView, or a
     * failed start) — Gecko data saved by earlier sessions is still
     * on disk then, so claiming "cleared" would be false. The
     * history half always runs. Callers must word their result
     * message from this return value, never assume success.
     */
    fun clearSiteData(host: String): Boolean {
        val geckoCleared = GeckoHolder.clearDataFromHost(host)
        val before = history.size
        history.removeAll { hostMatchesSite(hostOf(it.url), host) }
        if (history.size != before) store.saveHistory(history)
        return geckoCleared
    }

    /**
     * Delete every history entry belonging to one site (the site
     * itself plus its subdomains — the shared hostMatchesSite rule,
     * the same one clearSiteData uses). History records only: this
     * never touches the site's cookies or stored web data. Returns
     * how many entries were removed.
     */
    fun deleteHistoryForSite(host: String): Int {
        val before = history.size
        history.removeAll { hostMatchesSite(hostOf(it.url), host) }
        val removed = before - history.size
        if (removed > 0) store.saveHistory(history)
        return removed
    }

    /**
     * Ranged history clear (v0.79.0): the ONE implementation both
     * the history page and the settings page route through, so the
     * range rule can never differ between entry points. ALL keeps
     * the old full-wipe path (key removed outright); a windowed
     * range keeps the survivors and re-saves them. Returns how
     * many entries were removed.
     */
    fun clearHistoryRange(range: HistoryClearRange): Int {
        if (range == HistoryClearRange.ALL) {
            val removed = history.size
            history.clear()
            store.clearHistory()
            return removed
        }
        val cutoff = historyClearCutoff(range, System.currentTimeMillis())
        val kept = keptHistoryAfterClear(history, cutoff)
        val removed = history.size - kept.size
        if (removed > 0) {
            history.clear()
            history.addAll(kept)
            store.saveHistory(history)
        }
        return removed
    }

    /** Per-range removal counts for the clear dialog (v0.79.0). */
    fun historyClearCounts(): Map<HistoryClearRange, Int> {
        val now = System.currentTimeMillis()
        return HistoryClearRange.entries.associateWith { range ->
            countHistoryInClearRange(history, historyClearCutoff(range, now))
        }
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
            removeCompatMemoryForHost(host)
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
    // Single-choice variant (v0.133.0): a page whose file input does
    // not allow multiple files must not be handed several — the two
    // engines say so in their request (WebView FileChooserParams.mode,
    // Gecko FilePrompt.type), and the picker now honours it instead of
    // always opening the multiple-documents contract.
    val fileChooserSingleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        pendingFileAnswer?.invoke(listOfNotNull(uri))
        pendingFileAnswer = null
    }
    fun launchFilePicker(
        mime: String,
        multiple: Boolean,
        answer: (List<android.net.Uri>) -> Unit
    ) {
        pendingFileAnswer?.invoke(emptyList())
        pendingFileAnswer = answer
        // The page's accept hint is not necessarily a launchable MIME
        // type (it can be a bare extension like ".pdf"); normalize it
        // or the system picker cannot open at all and the request
        // would end as a silent cancel (normalizePickerMime).
        val launchMime = normalizePickerMime(mime)
        try {
            if (multiple) {
                fileChooserLauncher.launch(arrayOf(launchMime))
            } else {
                fileChooserSingleLauncher.launch(arrayOf(launchMime))
            }
        } catch (t: Throwable) {
            pendingFileAnswer = null
            answer(emptyList())
        }
    }
    fileChooserHandler = { params, callback ->
        val mime = params.acceptTypes?.firstOrNull { it.isNotBlank() } ?: "*/*"
        val multiple = params.mode ==
            android.webkit.WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
        launchFilePicker(mime, multiple) { uris ->
            callback.onReceiveValue(
                if (uris.isEmpty()) null else uris.toTypedArray()
            )
        }
    }
    geckoFileChooserHandler = { prompt, respond ->
        val mime = prompt.mimeTypes?.firstOrNull { it.isNotBlank() } ?: "*/*"
        val multiple = prompt.type ==
            GeckoSession.PromptDelegate.FilePrompt.Type.MULTIPLE
        launchFilePicker(mime, multiple, respond)
    }

    // Gecko JavaScript dialogs: one at a time. A second prompt arriving
    // while a dialog is up is cancelled at once rather than queued, so
    // no page waits forever and no answer callback leaks.
    geckoJsPromptHandler = { kind, message, defaultValue, respond ->
        if (jsPromptRequest != null || authPromptRequest != null ||
            sslPromptRequest != null || geckoClipboardPermission != null ||
            pageContextMenu != null || geckoSelectionMenu != null
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
            sslPromptRequest != null || geckoClipboardPermission != null ||
            pageContextMenu != null || geckoSelectionMenu != null
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
            jsPromptRequest != null || geckoClipboardPermission != null ||
            pageContextMenu != null || geckoSelectionMenu != null
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

    // Tab-removal cleanup for the two pending answers that live in
    // this section (v0.78.0): the shared file-picker answer and the
    // system-permission answer. releaseTransientUiForRemovedTab
    // (declared with the tab-removal paths above) calls this; each
    // answer uses its "cancel" value, exactly like the disposal
    // safety net below, and the engine-side wrappers guard against
    // a double answer.
    pendingEngineAnswersCleanup = {
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
            // The fingerprint group is destroyed on the same pass
            // (v0.170.0): until this version the loop covered only
            // the normal group, so on a recreation the process
            // survives, every fingerprint WebView was abandoned
            // with its script handlers and callbacks still wired
            // to a dead activity — the exact leak this teardown
            // exists to prevent for normal tabs.
            (tabs + fpTabs).forEach { tab ->
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
                    // Process-death hardening (v0.128.0): going to
                    // the background is where a process death
                    // normally follows, so settle the two lagging
                    // writers right here instead of trusting their
                    // timers — the tab archive's 400 ms debounce may
                    // not have fired for the very last navigation,
                    // and the blocked counter may hold unflushed
                    // hits. The tab save mirrors the debounced
                    // writer's mapping exactly (normal tabs only,
                    // same active-index rule) and is skipped until
                    // startup restore has run, so it can never save
                    // over the archive restore is about to read.
                    if (startupDone) {
                        val normalTabs = tabs.filter { !it.incognito }
                        store.saveTabs(
                            normalTabs.map {
                                SavedTab(it.engine, it.url, it.desktopMode)
                            },
                            normalTabs.indexOfFirst {
                                it.id == activeTabId
                            }.coerceAtLeast(0)
                        )
                        // The fingerprint archive settles on the
                        // same pause (v0.161.0), same mapping — with
                        // the same never-create-an-empty-archive
                        // guard as the debounced writer (v0.173.0).
                        if (fpTabs.isNotEmpty() ||
                            store.hasFpTabsArchive
                        ) {
                            store.saveFpTabs(
                                fpTabs.map {
                                    SavedTab(it.engine, it.url, it.desktopMode)
                                },
                                fpTabs.indexOfFirst {
                                    it.id == activeFpTabId
                                }.coerceAtLeast(0)
                            )
                        }
                    }
                    store.flushBlockedCounter()
                    tabs.forEach { it.pauseEngine() }
                    // Fingerprint tabs pause on the same pass
                    // (v0.170.0): they are live WebViews like any
                    // other, and until this version they kept
                    // playing media and running JS timers in the
                    // background while every normal tab idled.
                    // Pausing touches behaviour only — timers and
                    // media — never any fingerprint parameter.
                    fpTabs.forEach { it.pauseEngine() }
                }
                Lifecycle.Event.ON_RESUME -> {
                    tabs.forEach { it.resumeEngine(it.id == activeTabId) }
                    fpTabs.forEach {
                        it.resumeEngine(it.id == activeFpTabId)
                    }
                }
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

    // The fingerprint group's twin of the pass above (v0.170.0):
    // stamp the newly active fingerprint tab as most recently used,
    // then hold the group to the same alive-engine cap. Keyed on
    // activeFpTabId, so it runs after every fingerprint tab
    // creation, switch, profile rebuild and startup restore — and,
    // exactly like the normal pass, after composition, when the
    // shell's TabContent has created the newly shown tab's engine.
    // Fingerprint tabs are WebView-only, so there is no Gecko
    // active-state to apply here.
    LaunchedEffect(activeFpTabId) {
        activeFpTab()?.noteUsed()
        enforceFpEngineSleepLimit()
    }

    // Clipboard-read prompt lifetime (v0.45.0): switching away from
    // the requesting tab answers its pending request with deny — the
    // prompt must never outlive the page context it belongs to, and
    // the page must never be left hanging either.
    LaunchedEffect(activeTabId) {
        val current = geckoClipboardPermission
        if (current != null && current.tabId != activeTabId) {
            answerClipboardPermission(false)
        }
    }

    // Clipboard queue promotion (v0.102.0): a clipboard request that
    // arrived while a JS / auth / SSL dialog owned the stage waits in
    // the queue; when the last of those dialogs clears, promote it
    // (promotion itself re-checks the stage is free and the tab is
    // still the active one, denying cleanly otherwise).
    LaunchedEffect(
        jsPromptRequest, authPromptRequest, sslPromptRequest,
        pageContextMenu, geckoSelectionMenu
    ) {
        if (jsPromptRequest == null && authPromptRequest == null &&
            sslPromptRequest == null && pageContextMenu == null &&
            geckoSelectionMenu == null
        ) {
            promoteNextClipboardPermission()
        }
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

    // Bookmark export (v0.90.0): tapping export now asks for the
    // format first — this app's JSON (lossless: pinned + folders) or
    // the standard HTML every other browser can import (folders
    // carried, pinned not representable). The write itself is the
    // same MediaStore-to-Downloads path for both.
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var exportAsHtml by remember { mutableStateOf(false) }

    fun exportBookmarks() {
        if (bookmarks.isEmpty()) {
            android.widget.Toast.makeText(
                context, "还没有书签可以导出", android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        showExportFormatDialog = true
    }

    fun doExportBookmarks(html: Boolean) {
        val fileName = if (html) {
            "tuxunbrowse-bookmarks.html"
        } else {
            "tuxunbrowse-bookmarks.json"
        }
        val mimeType = if (html) "text/html" else "application/json"
        // Threading (v0.167.0): same move as the backup export —
        // serialize and write off the main thread, toast back here.
        val bookmarksSnapshot = bookmarks.toList()
        appScope.launch {
        val exportOk = withContext(Dispatchers.IO) {
        val content = if (html) {
            bookmarksToHtml(bookmarksSnapshot)
        } else {
            bookmarksToJson(bookmarksSnapshot)
        }
        // Same MediaStore cleanup as the backup export above
        // (v0.107.0): a failed write deletes the just-inserted
        // entry instead of leaving an empty file in Downloads, and
        // a null output stream counts as failure, not success.
        var exportDestUri: android.net.Uri? = null
        try {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val resolver = context.contentResolver
                val values = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(android.provider.MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(
                        android.provider.MediaStore.Downloads.RELATIVE_PATH,
                        android.os.Environment.DIRECTORY_DOWNLOADS
                    )
                }
                val dest = resolver.insert(
                    android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
                ) ?: throw IllegalStateException("insert failed")
                exportDestUri = dest
                val out = resolver.openOutputStream(dest)
                    ?: throw IllegalStateException("open output failed")
                out.use {
                    it.write(content.toByteArray(Charsets.UTF_8))
                }
            } else {
                val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                    ?: context.filesDir
                java.io.File(dir, fileName)
                    .writeText(content, Charsets.UTF_8)
            }
            true
        } catch (t: Throwable) {
            try {
                exportDestUri?.let {
                    context.contentResolver.delete(it, null, null)
                }
            } catch (t2: Throwable) {
                // Best-effort cleanup of the partial entry.
            }
            false
        }
        }
        android.widget.Toast.makeText(
            context,
            if (exportOk) {
                "书签已导出到下载文件夹：$fileName"
            } else {
                "导出失败，请重试"
            },
            android.widget.Toast.LENGTH_LONG
        ).show()
        }
    }

    if (showExportFormatDialog) {
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = { Text("导出书签") },
            text = {
                // Scrollable body (v0.115.0 extreme-viewport pass):
                // the two radio rows carry long supporting lines;
                // at font scale 1.3 in a 360dp-tall landscape window
                // the unscrollable Column overflowed and clipped
                // the second row's tail. verticalScroll changes
                // nothing when the content fits (see the history
                // clear dialog for the full accounting).
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("导出成哪种文件？都会存到下载文件夹。")
                    ListItem(
                        headlineContent = { Text("本应用 JSON 格式") },
                        supportingContent = {
                            Text("信息最全：置顶和文件夹都在。适合导回本应用或留作备份。")
                        },
                        leadingContent = {
                            RadioButton(
                                selected = !exportAsHtml,
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        },
                        modifier = Modifier.clickable { exportAsHtml = false }
                    )
                    ListItem(
                        headlineContent = { Text("通用 HTML 格式") },
                        supportingContent = {
                            Text("Chrome、火狐等其他浏览器也能导入的通用书签文件；文件夹会带上，置顶不会带。")
                        },
                        leadingContent = {
                            RadioButton(
                                selected = exportAsHtml,
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        },
                        modifier = Modifier.clickable { exportAsHtml = true }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showExportFormatDialog = false
                    doExportBookmarks(exportAsHtml)
                }) { Text("导出") }
            },
            dismissButton = {
                TextButton(onClick = { showExportFormatDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    fun openUrl(url: String, httpsUpgrade: Boolean = false) {
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
                "这个链接打不开：只支持 http/https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        // HTTPS first (v0.77.0): only the callers on the four
        // inbound paths pass httpsUpgrade = true (external VIEW
        // links, home search opens, the share sheet's "open"); the
        // other openUrl callers (bookmarks, history, reading list,
        // suggestions…) deliberately keep the address exactly as
        // stored. The tab is marked when WE upgraded its address,
        // so a failure can offer the one-off http fallback.
        val target =
            if (httpsUpgrade) upgradeToHttps(url, store.httpsFirstEnabled) else url
        if (fingerprintMode) {
            // Same routing as openUrlInNewTab (v0.153.0): while
            // the mode is on, the only callers that can reach this
            // gate are the outside ones (VIEW intents and kin) —
            // the mode's screens offer no bookmark/history path —
            // and their links belong in the mode's own tab group.
            val active = activeFpTab()
            if (active != null && active.isHome) {
                active.load(target)
            } else {
                newFpTab(target)
            }
            screen = Screen.BROWSER
            return
        }
        val existing = activeTab()
        var tab = existing ?: newTab(store.mainEngine, false, target)
        // A tab created for this address loads target on its own;
        // only the upgrade marker is ours to set.
        if (existing == null && target != url) tab.httpsUpgraded = true
        if (tab.engine == Engine.GECKO &&
            effectiveEngine(Engine.GECKO, target) == Engine.WEBVIEW
        ) {
            tab = replaceTabEngine(tab, Engine.WEBVIEW)
            // The replacement loads target afresh, so an upgraded
            // open stays marked across the engine switch.
            if (target != url) tab.httpsUpgraded = true
        }
        if (tab.url != target) {
            tab.load(target)
            if (target != url) tab.httpsUpgraded = true
        }
        screen = Screen.BROWSER
    }

    // A link opened from another app: show it once the session is ready.
    // First-launch gate (v0.121.0): on a fresh install this must
    // wait for the engine picker — opening the link straight away
    // used to jump past the picker on the fallback Gecko default,
    // create the first tab in an engine the user never chose, and
    // leave mainEngineChosen unset so the NEXT cold start showed
    // the picker again over the saved session. The link is not
    // consumed while gated, and keying on the picker state re-runs
    // this right after the user confirms a choice.
    LaunchedEffect(initialUrl, startupDone, screen == Screen.PICKER) {
        if (startupDone && screen != Screen.PICKER && initialUrl != null) {
            openUrl(initialUrl, httpsUpgrade = true)
            onInitialUrlConsumed()
        }
    }

    /**
     * Open [url] in a brand-new normal tab (v0.73.0, for the share
     * dialog's "open" choice). Goes through the same v0.31.0
     * inbound gate as openUrl — the refusal is identical — but a
     * share must never hijack the page the user is currently
     * reading, so it gets its own tab instead of reusing the
     * active one. Engine choice still flows through newTab, so a
     * compat-listed host lands on WebView exactly as with openUrl.
     */
    fun openUrlInNewTab(url: String, httpsUpgrade: Boolean = false) {
        if (!isAllowedPageUrl(url)) {
            android.widget.Toast.makeText(
                context,
                "这个链接打不开：只支持 http/https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        // HTTPS first (v0.77.0): only the share sheet's "open" path
        // passes httpsUpgrade = true; see openUrl for the rule.
        val target =
            if (httpsUpgrade) upgradeToHttps(url, store.httpsFirstEnabled) else url
        if (fingerprintMode) {
            // An outside link arriving while fingerprint mode is
            // on (v0.153.0) lands where the user actually is: the
            // mode's own tab group, wearing the profile — into the
            // home tab if that is what is showing, else a new
            // fingerprint tab. Before this it opened a NORMAL tab
            // behind the shell and the tap looked like a no-op.
            val active = activeFpTab()
            if (active != null && active.isHome) {
                active.load(target)
            } else {
                newFpTab(target)
            }
            screen = Screen.BROWSER
            return
        }
        val tab = newTab(store.mainEngine, false, target)
        if (target != url) tab.httpsUpgraded = true
        screen = Screen.BROWSER
    }

    /** The share dialog's "open" choice (v0.73.0). */
    fun openSharedText(text: String) {
        val url = extractFirstWebUrl(text)
        when {
            url != null -> openUrlInNewTab(url, httpsUpgrade = true)
            // No URL inside: the text itself is the input. A blocked
            // scheme is refused with the same message as everywhere
            // else; anything else goes through resolveInput, so
            // words become a search and a bare host becomes a site.
            hasBlockedPageScheme(text) -> android.widget.Toast.makeText(
                context,
                "这个链接打不开：只支持 http/https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            else -> openUrlInNewTab(
                resolveInput(text, store.searchEngine),
                httpsUpgrade = true
            )
        }
    }

    
    /** The share dialog's "save as bookmark" choice. */
    fun saveSharedUrlAsBookmark(url: String) {
        if (bookmarks.any { it.url == url }) {
            android.widget.Toast.makeText(
                context, "这个网址已经在书签里了",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        bookmarks.add(0, Bookmark(url, url))
        store.saveBookmarks(bookmarks)
        android.widget.Toast.makeText(
            context, "已存为书签",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    // Shared text waits for the session to be ready, then asks the
    // user what to do with it — it is never opened on its own.
    // Same first-launch gate as the external-link effect above
    // (v0.121.0): the share dialog must not appear over the engine
    // picker, and its "open" choice must not create the first tab
    // before an engine has been chosen.
    LaunchedEffect(sharedText, startupDone, screen == Screen.PICKER) {
        if (startupDone && screen != Screen.PICKER && sharedText != null) {
            pendingShareText = sharedText
            onSharedTextConsumed()
        }
    }

    // Incoming share (v0.73.0): another app handed us text and the
    // user decides what happens — open it, save its URL as a
    // bookmark, or drop it. Nothing runs before this choice,
    // and dismissing the dialog is the same as cancelling.
    val shareText = pendingShareText
    if (shareText != null) {
        val sharedUrl = extractFirstWebUrl(shareText)
        AlertDialog(
            onDismissRequest = { pendingShareText = null },
            title = { Text("收到一段分享内容") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(shareText)
                    if (sharedUrl != null) {
                        Text(
                            "\n里面找到的网址：$sharedUrl",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "\n里面没有现成的网址，点「打开」会按输入内容打开或搜索。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    pendingShareText = null
                    openSharedText(shareText)
                }) {
                    Text("打开")
                }
            },
            dismissButton = {
                Row {
                    if (sharedUrl != null) {
                        TextButton(onClick = {
                            pendingShareText = null
                            saveSharedUrlAsBookmark(sharedUrl)
                        }) {
                            Text("存为书签")
                        }
                    }
                    TextButton(onClick = { pendingShareText = null }) {
                        Text("取消")
                    }
                }
            }
        )
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
            // Fingerprint mode gets its own enter/exit motion
            // (v0.147.0): the mode is a different place, and the
            // switch should feel like travelling, not like a
            // settings toggle. Fade-only, per the standing rule
            // for transitions that carry a native web view (see
            // the screenTransition comment above) — 220/180 ms,
            // standard easing.
            AnimatedContent(
                targetState = fingerprintMode,
                transitionSpec = {
                    fadeIn(tween(220, easing = FastOutSlowInEasing))
                        .togetherWith(
                            fadeOut(
                                tween(180, easing = FastOutSlowInEasing)
                            )
                        )
                },
                label = "fingerprintModeSwitch"
            ) { inFingerprintMode ->
            if (inFingerprintMode) {
                FingerprintShell(
                    tabs = fpTabs,
                    activeTab = activeFpTab(),
                    resolved = store.resolvedFingerprint(),
                    searchEngine = searchEngineState,
                    onSelectTab = { activeFpTabId = it.id },
                    onCloseTab = { closeFpTab(it) },
                    onNewTab = { newFpTab() },
                    onExit = { exitFingerprintMode() },
                    onProfileChange = { regionId, tzId, deviceId, language ->
                        applyFingerprintProfile(
                            regionId, tzId, deviceId, language
                        )
                    },
                    customProfiles = store.customFingerprints(),
                    onSaveCustomProfile = { store.addCustomFingerprint(it) },
                    onDeleteCustomProfile = {
                        store.removeCustomFingerprint(it)
                    },
                    isBookmarked = activeFpTab()?.let { t ->
                        bookmarks.any { it.url == t.url }
                    } ?: false,
                    onToggleBookmark = {
                        activeFpTab()?.let {
                            toggleBookmarkForTab(it)
                        }
                    },
                    onOpenDownloads = { screen = Screen.DOWNLOADS },
                    onOpenBookmarks = { screen = Screen.BOOKMARKS },
                    onOpenHistory = { screen = Screen.HISTORY },
                    onOpenSettings = { screen = Screen.SETTINGS },
                    onCloseAllTabs = {
                        fpTabs.toList().forEach { closeFpTab(it) }
                    },
                    protection = fingerprintProtection,
                    onProtectionChange = {
                        applyFingerprintProtection(it)
                    },
                    proxyConfig = store.fpProxy,
                    onProxyChange = { config ->
                        store.fpProxy = config
                        // The mode is open when this fires, so the
                        // new routing takes effect at once: a
                        // usable enabled config applies, anything
                        // else clears (v0.197.0).
                        if (config.isUsable) {
                            com.tuxunbrowse.android.engine
                                .FpProxyControl.apply(config)
                        } else {
                            com.tuxunbrowse.android.engine
                                .FpProxyControl.clear()
                        }
                    }
                )
            } else {
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
                onCloseOtherTabsFor = { closeOtherTabsKeeping(it) },
                onToggleBookmarkFor = { toggleBookmarkForTab(it) },
                onBookmarkTabs = { bookmarkTabs(it) },
                bookmarkFolderNames = allBookmarkFolders(bookmarks, bookmarkFolders),
                recentClosed = recentClosedTabs,
                onReopenClosed = { reopenClosedTab(it) },
                onNewTab = { newTab(store.mainEngine, false) },
                onNewTabWithEngine = { engine -> newTab(engine, false) },
                mainEngine = store.mainEngine,
                onNewIncognitoTab = { engine -> newTab(engine, true) },
                incognitoEngine = store.incognitoEngine,
                onIncognitoEngineChosen = { store.incognitoEngine = it },
                onToggleBookmark = {
                    val tab = activeTab() ?: return@BrowserScreen
                    toggleBookmarkForTab(tab)
                },
                onNavigate = { text ->
                    // Typed input with a non-web scheme (file:,
                    // javascript:, data:, ...) is refused outright
                    // (v0.31.0) instead of being reinterpreted as a
                    // search or a host name.
                    if (hasBlockedPageScheme(text)) {
                        android.widget.Toast.makeText(
                            context,
                            "这个链接打不开：只支持 http/https 开头的网址",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // HTTPS first (v0.77.0): typed address-bar
                        // input is one of the four inbound paths —
                        // an http:// address is upgraded before the
                        // load, and the tab is marked so a failure
                        // can offer the one-off http fallback.
                        val resolved = resolveInput(
                            text, store.searchEngine
                        )
                        val target = upgradeToHttps(resolved, store.httpsFirstEnabled)
                        val tab = activeTab()
                        tab?.load(target)
                        if (tab != null && target != resolved) {
                            tab.httpsUpgraded = true
                        }
                    }
                },
                bookmarks = bookmarks,
                searchEngine = searchEngineState,
                onOpenSuggestion = { url -> openUrl(url) },
                onGoHome = { activeTab()?.load(HOME_URL) },
                onReloadPage = {
                    // A manual reload resets the network-restore
                    // auto-reload shot (v0.70.0): if this load fails
                    // into a fresh error state, it may auto-retry
                    // once more when the network comes back.
                    autoReloadUsedKey = null
                    activeTab()?.reload()
                },
                onOpenInOtherEngine = {
                    val tab = activeTab()
                    val error = tab?.loadError
                    if (tab != null && error != null) {
                        openLoadErrorInOtherEngine(tab, error.failedUrl)
                    }
                },
                onOpenHttpFallback = {
                    // HTTPS-first escape hatch (v0.77.0): the page
                    // WE upgraded failed to load — reopen the failed
                    // address over plain http, this once only. The
                    // switch stays on, nothing is remembered, and
                    // load() clears the upgrade marker, so if the
                    // http load fails too there is no loop.
                    val tab = activeTab()
                    val error = tab?.loadError
                    if (tab != null && error != null && tab.httpsUpgraded) {
                        tab.load(downgradeToHttp(error.failedUrl))
                    }
                },
                onEngineFallbackSwitch = {
                    // Engine-unavailable fallback (v0.81.0): the
                    // tab's engine could not be created at all —
                    // reopen its current address in the OTHER
                    // engine through the very same one-time switch
                    // the load-error page uses (compat bypass and
                    // all), never touching the per-site compat list.
                    val tab = activeTab()
                    if (tab != null && tab.engineUnavailable) {
                        openLoadErrorInOtherEngine(tab, tab.url)
                    }
                },
                onEngineFallbackRetry = {
                    // Retry = rebuild the tab in place with the
                    // same engine (v0.81.0): a fresh tab object
                    // retries creation from scratch. If the engine
                    // is still unavailable, the new tab lands on
                    // this same fallback page — no crash, no loop
                    // (each attempt needs a fresh tap).
                    val tab = activeTab()
                    if (tab != null && tab.engineUnavailable) {
                        recreateTab(tab, tab.engine, tab.desktopMode, force = true)
                    }
                },
                onSwitchEngine = { engine, rememberSite ->
                    val tab = activeTab() ?: return@BrowserScreen
                    switchTabEngine(tab, engine, rememberSite)
                },
                onEnterFingerprintMode = { enterFingerprintMode() },
                onEnterFingerprintNewTab = {
                    enterFingerprintModeWithNewTab()
                },
                onOpenClearData = { screen = Screen.CLEAR_DATA },
                onToggleSiteCompat = { toggleSiteCompat() },
                onClearSiteData = { host -> clearSiteData(host) },
                activeDesktopMode = current?.desktopMode ?: false,
                onToggleDesktopMode = { enabled ->
                    val tab = activeTab() ?: return@BrowserScreen
                    recreateTab(tab, tab.engine, enabled)
                    // Toggle-is-memory (v0.56.0, normal tabs only):
                    // switching desktop mode on for a site remembers
                    // the site, switching it off forgets it, so the
                    // next new tab on this site starts the same way.
                    // Incognito toggles affect only their own tab and
                    // are never written down.
                    if (!tab.incognito) {
                        val host = hostOf(tab.url)
                        if (host != null) {
                            if (enabled) {
                                addDesktopSite(host)
                            } else {
                                removeDesktopMemoryForHost(host)
                            }
                        }
                    }
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
                            "阅读模式只支持系统 WebView，火狐标签请先切换内核再开",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        tab.readerActive -> tab.exitReaderMode()
                        else -> tab.enterReaderMode {
                            android.widget.Toast.makeText(
                                context, "这个页面没有找到适合阅读的正文",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                onPrintPage = {
                    val tab = activeTab() ?: return@BrowserScreen
                    when {
                        // Home first (v0.126.0): a Gecko home tab used
                        // to be told to switch engines and print, but
                        // the home page cannot print in either engine —
                        // the reader/share handlers check isHome first
                        // for the same reason.
                        tab.isHome -> android.widget.Toast.makeText(
                            context, "先打开一个网页再打印",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        tab.engine == Engine.GECKO -> android.widget.Toast.makeText(
                            context,
                            "火狐标签不能打印，请切换到系统 WebView 再试",
                            android.widget.Toast.LENGTH_LONG
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
                            "火狐标签不支持嗅探，请切换到系统 WebView 再试",
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
                            "火狐标签不支持网页截图，请切换到系统 WebView 再试",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        else -> screenshotTab = tab
                    }
                },
                onOpenDownloads = { screen = Screen.DOWNLOADS },
                onOpenBookmarks = { screen = Screen.BOOKMARKS },
                onOpenHistory = { screen = Screen.HISTORY },
                onOpenSettings = { screen = Screen.SETTINGS },
                homeContent = {
                    HomeContent(
                        bookmarks = bookmarks,
                        searchEngine = searchEngineState,
                        activeIncognito = current?.incognito == true,
                        onSearchEngineSelected = { engine ->
                            store.searchEngine = engine
                            searchEngineState = engine
                        },
                        onSearchUrl = { url -> openUrl(url, httpsUpgrade = true) },
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
                        onOpenAllBookmarks = { screen = Screen.BOOKMARKS },
                        onEnterFingerprint = { enterFingerprintMode() }
                    )
                }
            )
            }
            }
        }

        Screen.BOOKMARKS -> BookmarksScreen(
            bookmarks = bookmarks,
            folders = allBookmarkFolders(bookmarks, bookmarkFolders),
            onBack = { screen = Screen.BROWSER },
            onOpen = { openUrl(it.url) },
            onDelete = {
                bookmarks.remove(it)
                store.saveBookmarks(bookmarks)
            },
            onEdit = { old, newTitle, newUrl, newFolder ->
                val idx = bookmarks.indexOf(old)
                if (idx >= 0) {
                    // Field-level update via copy() (v0.55.0 audit):
                    // rebuilding Bookmark(newTitle, newUrl) here used
                    // to reset every non-edited field to its default —
                    // a pinned bookmark silently lost its pin when the
                    // user only renamed it. Title, URL and (v0.72.0)
                    // folder are the edited fields; everything else
                    // (pinned) carries over from the old entry.
                    bookmarks[idx] = old.copy(
                        title = newTitle, url = newUrl,
                        folder = normalizeBookmarkFolderName(newFolder)
                    )
                    store.saveBookmarks(bookmarks)
                    // A folder name first typed in the edit dialog
                    // joins the stored list too, so the union's
                    // order stays stable.
                    val folderName = normalizeBookmarkFolderName(newFolder)
                    if (folderName.isNotEmpty() &&
                        folderName !in bookmarkFolders
                    ) {
                        bookmarkFolders.add(folderName)
                        store.saveBookmarkFolders(bookmarkFolders)
                    }
                }
            },
            onCreateFolder = { name ->
                val folderName = normalizeBookmarkFolderName(name)
                if (folderName.isNotEmpty() &&
                    folderName !in bookmarkFolders
                ) {
                    bookmarkFolders.add(folderName)
                    store.saveBookmarkFolders(bookmarkFolders)
                }
            },
            onRenameFolder = { oldName, newName ->
                val target = normalizeBookmarkFolderName(newName)
                if (target.isNotEmpty() && target != oldName) {
                    val renamed = renameBookmarksFolder(
                        bookmarks, oldName, target
                    )
                    bookmarks.clear()
                    bookmarks.addAll(renamed)
                    store.saveBookmarks(bookmarks)
                    val storedIdx = bookmarkFolders.indexOf(oldName)
                    if (storedIdx >= 0) {
                        bookmarkFolders[storedIdx] = target
                    } else if (target !in bookmarkFolders) {
                        bookmarkFolders.add(target)
                    }
                    store.saveBookmarkFolders(bookmarkFolders)
                }
            },
            onDeleteFolder = { name ->
                // Only the folder goes away: its bookmarks fall
                // back to uncategorized (clearBookmarksFolder),
                // never deleted.
                val cleared = clearBookmarksFolder(bookmarks, name)
                bookmarks.clear()
                bookmarks.addAll(cleared)
                store.saveBookmarks(bookmarks)
                if (bookmarkFolders.remove(name)) {
                    store.saveBookmarkFolders(bookmarkFolders)
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
            },
            onOpenInNewTab = {
                // Background open (v0.48.0 rule, same as the history
                // page): the user stays on the bookmarks page.
                newTab(store.mainEngine, false, it.url, activate = false)
                android.widget.Toast.makeText(
                    context, "已在后台打开新标签",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onCopyUrl = {
                val ok = copyTextToClipboard(context, "url", it.url)
                android.widget.Toast.makeText(
                    context, if (ok) "网址已复制" else "复制失败，请重试",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onTogglePin = { bm ->
                // Same pinned-field write as the home grid's toggle.
                val idx = bookmarks.indexOf(bm)
                if (idx >= 0) {
                    bookmarks[idx] = bm.copy(pinned = !bm.pinned)
                    store.saveBookmarks(bookmarks)
                }
            }
        )

        Screen.HISTORY -> HistoryScreen(
            history = history,
            onBack = { screen = Screen.BROWSER },
            onOpen = { openUrl(it.url) },
            onOpenInNewTab = {
                // Background open (v0.48.0 rule): a long-press "open
                // in new tab" keeps the user where they are — here,
                // on the history page — and confirms with a toast.
                newTab(store.mainEngine, false, it.url, activate = false)
                android.widget.Toast.makeText(
                    context, "已在后台打开新标签",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onCopyUrl = {
                val ok = copyTextToClipboard(context, "url", it.url)
                android.widget.Toast.makeText(
                    context, if (ok) "网址已复制" else "复制失败，请重试",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onDelete = {
                history.remove(it)
                store.saveHistory(history)
            },
            onSwipeRemove = {
                // Memory only (v0.71.0): the removal reaches disk
                // when the undo window closes (onSwipeCommit) —
                // or never, when undo restores the entry instead.
                history.remove(it)
            },
            onSwipeUndo = {
                // Back in by its own timestamp, then saved, so the
                // restored order is the order it was deleted from.
                history.add(historyRestoreIndex(history, it), it)
                store.saveHistory(history)
            },
            onSwipeCommit = {
                store.saveHistory(history)
            },
            onDeleteSite = { host ->
                val removed = deleteHistoryForSite(host)
                android.widget.Toast.makeText(
                    context, "已删除 $host 的 $removed 条历史记录",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onClearRange = { range -> clearHistoryRange(range) },
            clearCounts = historyClearCounts()
        )

        Screen.SETTINGS -> SettingsScreen(
            store = store,
            refreshTick = settingsRefreshTick,
            bookmarkCount = bookmarks.size,
            historyCount = history.size,
            onOpenReleaseUrl = { url -> openUrl(url) },
            onEnterFingerprintMode = { enterFingerprintMode() },
            onBack = { screen = Screen.BROWSER },
            onClearHistoryRange = { range -> clearHistoryRange(range) },
            historyClearCounts = historyClearCounts(),
            onRemoveCompatSite = { removeCompatSite(it) },
            onRemoveDesktopSite = { removeDesktopSite(it) },
            onClearDesktopSites = { clearDesktopSites() },
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
            onReaderStyleChange = {
                tabs.forEach { it.applyReaderStyle() }
            }
        )

        Screen.CLEAR_DATA -> ClearDataScreen(
            geckoStarted = GeckoHolder.hasRuntime,
            onBack = { screen = Screen.SETTINGS },
            onClear = { clearHistorySel, clearWebView, clearGecko, onDone ->
                if (clearHistorySel) {
                    history.clear()
                    store.clearHistory()
                }
                if (clearGecko) {
                    GeckoHolder.clearSiteData()
                }
                if (clearWebView) {
                    // Full WebView spectrum (v0.129.0 site-data
                    // audit): cookies + web storage, PLUS the HTTP
                    // cache (via a throwaway WebView, the same trick
                    // as the startup auto-clear — it touches cache
                    // only and needs no live tab), saved form data
                    // and saved HTTP-auth credentials. SSL error
                    // decisions are NOT covered: clearing them needs
                    // a live WebView instance API whose profile-wide
                    // effect from a throwaway view is unverified, so
                    // nothing claims them.
                    try {
                        val tempWebView = android.webkit.WebView(context)
                        try {
                            tempWebView.clearCache(true)
                        } finally {
                            tempWebView.destroy()
                        }
                        val webDb = android.webkit.WebViewDatabase.getInstance(context)
                        webDb.clearFormData()
                        webDb.clearHttpAuthUsernamePassword()
                    } catch (t: Throwable) {
                        // Best-effort: the cookie/storage clear below
                        // is the part the page promises first.
                    }
                    android.webkit.WebStorage.getInstance().deleteAllData()
                    // Fingerprint mode's own storage jars go
                    // too (v0.196.0; one per combination since
                    // v0.198.0, and clearData wipes them all):
                    // this flow promises "clear everything", and
                    // since the mode stopped sharing the default
                    // jar its data would otherwise survive a
                    // clear that claims to have removed it.
                    com.tuxunbrowse.android.engine.FpStorage.clearData()
                    // removeAllCookies is asynchronous: the page's
                    // "cleared" message must wait for the completion
                    // callback instead of reporting success the
                    // moment the call returns (v0.129.0 — the same
                    // false-completion class the v0.125.0 per-site
                    // fix removed). flush() runs inside the callback
                    // so the empty jar is what gets persisted.
                    android.webkit.CookieManager.getInstance()
                        .removeAllCookies {
                            try {
                                android.webkit.CookieManager.getInstance().flush()
                            } catch (t: Throwable) {
                                // Persistence is best-effort only.
                            }
                            onDone()
                        }
                } else {
                    onDone()
                }
            },
        )

        Screen.DOWNLOADS -> DownloadsScreen(
            onBack = { screen = Screen.BROWSER },
            retryReferer = activeTab()?.url,
            retryUserAgent = pageUserAgent(activeTab())
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
                        context, res.url,
                        pageUserAgent(
                            tabs.firstOrNull { t -> t.id == sniffTabId }
                        ),
                        null, null,
                        referer = tabs.firstOrNull { t -> t.id == sniffTabId }?.url
                    )
                }
            },
            onDownloadAll = { list ->
                downloadBatchWithMode(
                    list.map { it.url },
                    fromIncognito =
                        tabs.firstOrNull { t -> t.id == sniffTabId }?.incognito == true
                ) { cleanUrls ->
                    // The fallback iterates the filtered URL list
                    // handed back by downloadBatchWithMode, not the
                    // original selection (v0.112.0).
                    cleanUrls.forEach { cleanUrl ->
                        startSystemDownload(
                            context, cleanUrl,
                            pageUserAgent(
                                tabs.firstOrNull { t -> t.id == sniffTabId }
                            ),
                            null, null,
                            referer = tabs.firstOrNull { t -> t.id == sniffTabId }?.url
                        )
                    }
                }
            },
            onDownloadSelected = { list ->
                // Same dispatch as "download all" above, word for
                // word (v0.80.0): the batch entry point is the one
                // existing multi-item path — its system fallback
                // enqueues every item through the exact single-item
                // startSystemDownload call, ask mode asks once for
                // the whole selection and TuxunDown gets one share,
                // where per-item downloadWithMode calls would stack
                // N ask dialogs / N shares. No new download path.
                downloadBatchWithMode(
                    list.map { it.url },
                    fromIncognito =
                        tabs.firstOrNull { t -> t.id == sniffTabId }?.incognito == true
                ) { cleanUrls ->
                    cleanUrls.forEach { cleanUrl ->
                        startSystemDownload(
                            context, cleanUrl,
                            pageUserAgent(
                                tabs.firstOrNull { t -> t.id == sniffTabId }
                            ),
                            null, null,
                            referer = tabs.firstOrNull { t -> t.id == sniffTabId }?.url
                        )
                    }
                }
            }
        )
        }
    }

    // Long-press context menu (v0.42.0): ONE combined dialog for
    // link and/or image, fed by both engines. The address is shown
    // (capped at 3 lines with an ellipsis, so a huge URL cannot blow
    // up the layout) so the user sees exactly what would be opened.
    // "Open in new tab" reuses the source tab's engine + incognito
    // flag through the existing newTab path, in the BACKGROUND
    // (v0.48.0): the user stays on the current page. Non-http(s)
    // targets are
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
                                            // Background open (v0.48.0):
                                            // a long-press "open in new
                                            // tab" means "keep this for
                                            // later" — the user stays on
                                            // the current page and gets a
                                            // toast instead of being
                                            // switched away.
                                            pageContextMenu = null
                                            // Landing group follows
                                            // the SOURCE tab (v0.172.0):
                                            // a link long-pressed in a
                                            // fingerprint tab opens in
                                            // the fingerprint group,
                                            // wearing the profile and
                                            // staying out of normal
                                            // history — before this it
                                            // always landed in the
                                            // normal group behind the
                                            // fingerprint shell.
                                            if (newTabGroupForSource(
                                                    ctxMenu.tabId,
                                                    fpTabs.map { it.id }
                                                        .toSet()
                                                ) == NewTabGroup.FINGERPRINT
                                            ) {
                                                newFpTab(
                                                    link, activate = false
                                                )
                                            } else {
                                                newTab(
                                                    ctxMenu.engine,
                                                    ctxMenu.incognito,
                                                    link,
                                                    activate = false
                                                )
                                            }
                                            android.widget.Toast.makeText(
                                                context,
                                                "已在后台打开新标签",
                                                android.widget.Toast
                                                    .LENGTH_SHORT
                                            ).show()
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
                                            // Background open, same
                                            // rule as the link item
                                            // above (v0.48.0).
                                            pageContextMenu = null
                                            // Same group rule as the
                                            // link item above (v0.172.0).
                                            if (newTabGroupForSource(
                                                    ctxMenu.tabId,
                                                    fpTabs.map { it.id }
                                                        .toSet()
                                                ) == NewTabGroup.FINGERPRINT
                                            ) {
                                                newFpTab(
                                                    image, activate = false
                                                )
                                            } else {
                                                newTab(
                                                    ctxMenu.engine,
                                                    ctxMenu.incognito,
                                                    image,
                                                    activate = false
                                                )
                                            }
                                            android.widget.Toast.makeText(
                                                context,
                                                "已在后台打开新标签",
                                                android.widget.Toast
                                                    .LENGTH_SHORT
                                            ).show()
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
                                                    "无痕提醒：下载记录会留在系统下载管理里，关掉无痕标签也不删",
                                                    android.widget.Toast
                                                        .LENGTH_LONG
                                                ).show()
                                            }
                                            startSystemDownload(
                                                context, image,
                                                pageUserAgent(activeTab()),
                                                null, null,
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

    // Text-selection menu for Gecko tabs (v0.43.0). Before this
    // version Gecko tabs had no SelectionActionDelegate at all, so
    // long-pressing to select text produced no menu and copy was
    // unreachable. The delegate now forwards Gecko's Selection
    // (selected text + the actions Gecko says are available) here;
    // the menu is this AlertDialog, same shape as the long-press
    // context menu above. Every exit path finishes the selection on
    // the Gecko side (hide) except "select all", which Gecko answers
    // with a fresh show request carrying the expanded selection.
    // Copy goes through the shared copyTextToClipboard helper with
    // the Selection's own text; the incognito clipboard-hint
    // suppression (v0.27.0) is about READING the clipboard for the
    // address-bar hint and is untouched. A password field's text is
    // never previewed, shared or searched.
    val selMenu = geckoSelectionMenu
    if (selMenu != null) {
        val menu = selMenu.menu
        val selText = menu.text
        fun closeSelectionMenu() {
            geckoSelectionMenu = null
            menu.hide()
        }
        AlertDialog(
            onDismissRequest = { closeSelectionMenu() },
            title = { Text("已选文字") },
            text = {
                LazyColumn {
                    if (menu.isPassword) {
                        item {
                            Text(
                                "这是密码框里的内容，不显示原文，也不能分享或搜索。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme
                                    .onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    } else if (selText.isNotBlank()) {
                        item {
                            Text(
                                selText,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    if (menu.isAvailable(
                            GeckoSession.SelectionActionDelegate.ACTION_COPY
                        )
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("复制") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val ok = copyTextToClipboard(
                                            context, "selection", selText
                                        )
                                        android.widget.Toast.makeText(
                                            context,
                                            if (ok) "已复制"
                                            else "复制失败，请重试",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                        closeSelectionMenu()
                                    }
                            )
                        }
                    }
                    if (menu.isAvailable(
                            GeckoSession.SelectionActionDelegate.ACTION_CUT
                        )
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("剪切") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        menu.execute(
                                            GeckoSession.SelectionActionDelegate
                                                .ACTION_CUT
                                        )
                                        closeSelectionMenu()
                                    }
                            )
                        }
                    }
                    if (menu.isAvailable(
                            GeckoSession.SelectionActionDelegate.ACTION_PASTE
                        )
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("粘贴") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Paste (v0.45.0): Gecko only
                                        // lists ACTION_PASTE when the
                                        // selection is editable AND the
                                        // clipboard holds matching data
                                        // (SelectionActionDelegateChild
                                        // predicate, verified in the
                                        // AAR's shipped JS). Executing
                                        // it runs the engine's own
                                        // cmd_paste; if that paste
                                        // itself asks for clipboard-read
                                        // confirmation, the request comes
                                        // back through the clipboard
                                        // permission prompt below.
                                        menu.execute(
                                            GeckoSession.SelectionActionDelegate
                                                .ACTION_PASTE
                                        )
                                        closeSelectionMenu()
                                    }
                            )
                        }
                    }
                    if (menu.isAvailable(
                            GeckoSession.SelectionActionDelegate
                                .ACTION_PASTE_AS_PLAIN_TEXT
                        )
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("粘贴为纯文本") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Paste as plain text (v0.48.0):
                                        // Gecko only lists
                                        // ACTION_PASTE_AS_PLAIN_TEXT when
                                        // the selection is in an HTML-
                                        // editable area AND the clipboard
                                        // holds text/html (predicate read
                                        // from the AAR's shipped
                                        // SelectionActionDelegateChild JS;
                                        // the id's constant value
                                        // "org.mozilla.geckoview
                                        // .PASTE_AS_PLAIN_TEXT" was
                                        // re-verified in the 132 AAR
                                        // class constant pool). Executing
                                        // it runs the engine's own
                                        // _performPasteAsPlainText, so
                                        // rich formatting is stripped by
                                        // the engine, not by the app.
                                        menu.execute(
                                            GeckoSession.SelectionActionDelegate
                                                .ACTION_PASTE_AS_PLAIN_TEXT
                                        )
                                        closeSelectionMenu()
                                    }
                            )
                        }
                    }
                    if (menu.isAvailable(
                            GeckoSession.SelectionActionDelegate.ACTION_DELETE
                        )
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("删除") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        menu.execute(
                                            GeckoSession.SelectionActionDelegate
                                                .ACTION_DELETE
                                        )
                                        closeSelectionMenu()
                                    }
                            )
                        }
                    }
                    if (menu.isAvailable(
                            GeckoSession.SelectionActionDelegate
                                .ACTION_SELECT_ALL
                        )
                    ) {
                        item {
                            ListItem(
                                headlineContent = { Text("全选") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // No close: Gecko answers with a
                                        // fresh show request for the
                                        // expanded selection, which
                                        // replaces this menu's state.
                                        menu.execute(
                                            GeckoSession.SelectionActionDelegate
                                                .ACTION_SELECT_ALL
                                        )
                                    }
                            )
                        }
                    }
                    if (!menu.isPassword && selText.isNotBlank()) {
                        item {
                            ListItem(
                                headlineContent = { Text("分享选中文字") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        closeSelectionMenu()
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
                                                        selText
                                                    )
                                            context.startActivity(
                                                android.content.Intent
                                                    .createChooser(
                                                        intent, "分享选中文字"
                                                    )
                                            )
                                        } catch (t: Throwable) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "没有能分享文字的应用",
                                                android.widget.Toast
                                                    .LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            )
                        }
                        item {
                            ListItem(
                                headlineContent = { Text("网页搜索选中文字") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Same tab, always: an incognito
                                        // selection is searched inside
                                        // its incognito tab, never in a
                                        // new normal tab.
                                        val target = tabs.firstOrNull {
                                            it.id == selMenu.tabId
                                        }
                                        closeSelectionMenu()
                                        if (target != null) {
                                            target.load(
                                                resolveInput(
                                                    selText,
                                                    store.searchEngine
                                                )
                                            )
                                            screen = Screen.BROWSER
                                        }
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { closeSelectionMenu() }) {
                    Text("取消")
                }
            }
        )
    }

    // Clipboard-read permission prompt for Gecko pages (v0.45.0).
    // Before this version the engine's default answer was a silent
    // deny: a site calling navigator.clipboard.readText() simply
    // failed and the user never knew it had asked. Now the user is
    // asked, in plain words, with the site's name up front. Only an
    // explicit tap on "允许" answers allow; every other exit —
    // "拒绝", tapping outside, Back, tab switch/close/sleep, the
    // engine's own dismiss — answers deny (see the plumbing above),
    // so the page is never left hanging. The decision is NOT
    // remembered: the next request asks again. Incognito tabs are
    // asked exactly the same way — going incognito is not a reason
    // to hand a site the clipboard silently, nor to refuse it
    // silently either. The clipboard's contents are never read by
    // the app for this flow, never logged and never stored.
    val clipPerm = geckoClipboardPermission
    if (clipPerm != null) {
        val clipHost = hostOf(clipPerm.uri)?.takeIf { it.isNotBlank() }
            ?: clipPerm.uri
        AlertDialog(
            onDismissRequest = { answerClipboardPermission(false) },
            title = { Text("网站想读取剪贴板") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "「$clipHost」想读取你的剪贴板内容。" +
                                "允许后，这个网站就能看到你最近复制的文字，" +
                                "比如你刚复制的链接、验证码或者其他内容。"
                        )
                    }
                    item {
                        Text(
                            "只在这个网站确实需要你粘贴东西、而且你信任它时，才点「允许」。" +
                                "这次允许只对这一次有效，下次它想读还会再问你。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { answerClipboardPermission(true) }) {
                    Text("允许")
                }
            },
            dismissButton = {
                TextButton(onClick = { answerClipboardPermission(false) }) {
                    Text("拒绝")
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
                                singleLine = true,
                                // Enter confirms, like the auth
                                // dialog's last field since the
                                // v0.83.0 audit — this older dialog
                                // (v0.12.0) never got that pass, so
                                // its Enter key used to do nothing
                                // (v0.104.0 input audit, third round).
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        answerJsPrompt(true, textInput)
                                    }
                                )
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
        val authPassFocusRequester = remember { FocusRequester() }
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
                                singleLine = true,
                                // Enter moves on to the password
                                // field instead of doing nothing
                                // (v0.83.0 input audit).
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = {
                                        authPassFocusRequester.requestFocus()
                                    }
                                )
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
                                    .focusRequester(authPassFocusRequester)
                            },
                            singleLine = true,
                            visualTransformation = if (authPassVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                // Enter on the last field submits
                                // the login (v0.83.0 input audit).
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    answerAuth(
                                        if (info.onlyPassword) {
                                            info.prefillUsername
                                        } else {
                                            authUser
                                        },
                                        authPass
                                    )
                                }
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
    // honestly at the menu entry that it is not supported there):
    // captures the visible screen, then saves or shares it.
    val shotTab = screenshotTab
    if (shotTab != null) {
        fun shotToast(message: String) {
            android.widget.Toast.makeText(
                context, message, android.widget.Toast.LENGTH_LONG
            ).show()
        }
        fun runShot(share: Boolean) {
            screenshotTab = null
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
        }
        AlertDialog(
            onDismissRequest = { screenshotTab = null },
            title = { Text("网页截图") },
            text = {
                Text("截取现在屏幕里能看到的这一屏，截好后可以存到相册，也可以直接分享。")
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
 * The app's own home page: a native Material 3 screen with a big
 * search box and, when bookmarks exist, a pinned-bookmark shortcut
 * grid. It never loads an external start page, and history is not
 * shown here — it lives on the History page.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    bookmarks: List<Bookmark>,
    searchEngine: SearchEngine,
    activeIncognito: Boolean = false,
    onSearchEngineSelected: (SearchEngine) -> Unit = {},
    onSearchUrl: (String) -> Unit,
    onOpenBookmark: (Bookmark) -> Unit,
    onDeleteBookmark: (Bookmark) -> Unit,
    onTogglePinBookmark: (Bookmark) -> Unit,
    onOpenAllBookmarks: () -> Unit,
    onEnterFingerprint: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<Bookmark?>(null) }
    var actionTarget by remember { mutableStateOf<Bookmark?>(null) }
    val homeContext = LocalContext.current
    // Home-page search-engine quick switch: same data source as the
    // settings page (the store, via the callbacks above); this local
    // mirror is what the home search box reads, so a pick here takes
    // effect on the home page at once. The mirror is keyed on the
    // incoming value (v0.132.0 settings audit): unkeyed, it would
    // keep the engine from first composition if the shared source
    // ever changed while this content stayed composed — the stale-
    // snapshot class the v0.108.0 audit fixed on the settings page.
    // A pick here writes the same value back through the callback,
    // so re-keying on it re-reads the identical value.
    var currentEngine by remember(searchEngine) {
        mutableStateOf(searchEngine)
    }
    var showEnginePicker by remember { mutableStateOf(false) }
    var homeSearchFocused by remember { mutableStateOf(false) }
    val homeFocusManager = LocalFocusManager.current
    // System back while the home search box is focused first
    // drops the focus (v0.105.0 back audit): the suggestion list
    // is derived from the focus state, so this collapses it and
    // hides the keyboard — the same innermost-layer rule the
    // address bar has had since v0.37.0. Before this, no handler
    // was enabled on the home page and the key fell through and
    // finished the activity mid-typing.
    BackHandler(enabled = homeSearchFocused) {
        homeFocusManager.clearFocus()
    }
    val homeSuggestionQuery = query.trim()
    // Suggestions are bookmarks only (plus the search tail below):
    // history stays on the History page, exactly like the address bar.
    // Same derived-state treatment as the address bar (v0.95.0): the
    // bookmark scan re-runs only when the query, the focus or the
    // bookmark list actually changes, not on every recomposition.
    val homeBookmarkSuggestions by remember {
        derivedStateOf {
            if (homeSearchFocused && query.isNotBlank()) {
                matchSuggestions(query, bookmarks, emptyList())
            } else {
                emptyList()
            }
        }
    }
    val homeShowSearchTail = homeSearchFocused &&
        homeSuggestionQuery.isNotEmpty() &&
        !isUrlLikeInput(homeSuggestionQuery)
    // "Paste and go" row (v0.69.0), the home twin of the address
    // bar's row: when the home search box gains focus while empty,
    // read the clipboard ONCE at that focus moment (never in the
    // background, never at startup) and offer its text as the top
    // suggestion row. Incognito home tabs never read the clipboard,
    // the same gate the online suggestions above already apply.
    val homeClipboardManager = LocalClipboardManager.current
    var homePasteCandidate by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(homeSearchFocused, activeIncognito) {
        if (!homeSearchFocused || activeIncognito) {
            homePasteCandidate = null
            return@LaunchedEffect
        }
        val text = try {
            homeClipboardManager.getText()?.text?.trim()
        } catch (t: Throwable) {
            null
        }
        homePasteCandidate = text?.takeIf { it.isNotEmpty() }
    }
    val homeShowPasteRow = homeSearchFocused &&
        homePasteCandidate != null && query.isBlank()
    // The home submit path, shared by the search box's own submit
    // and by tapping an online suggestion or the search tail: the
    // same blocked-scheme refusal and the same resolveInput call
    // with the home box's current engine. Returns true when a page
    // was actually opened.
    fun submitHomeSearch(text: String): Boolean {
        if (text.isBlank()) return false
        if (hasBlockedPageScheme(text)) {
            android.widget.Toast.makeText(
                homeContext,
                "这个链接打不开：只支持 http/https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return false
        }
        onSearchUrl(resolveInput(text, currentEngine))
        return true
    }
    // Tapping any suggestion row opens it, then the typed text and
    // the list go away (the tab navigates to the page).
    fun finishHomeSuggestionPick() {
        query = ""
        homeFocusManager.clearFocus()
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
            text = {
                // Scrollable body (v0.169.0 alignment pass, same
                // v0.115.0/v0.137.0 treatment): the body embeds the
                // bookmark's own title, an unbounded web-page value —
                // a very long title at the largest font scale in a
                // short landscape window could push the buttons off
                // the bottom. verticalScroll changes nothing when
                // the content fits.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("会把「${toDelete.title}」从书签里删掉，首页的快捷入口也会一起消失。")
                }
            },
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
    if (showEnginePicker) {
        AlertDialog(
            onDismissRequest = { showEnginePicker = false },
            title = { Text("选择搜索引擎") },
            text = {
                LazyColumn {
                    items(SearchEngine.entries) { engine ->
                        ListItem(
                            headlineContent = { Text(engine.label) },
                            trailingContent = {
                                RadioButton(
                                    selected = currentEngine == engine,
                                    onClick = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentEngine = engine
                                    onSearchEngineSelected(engine)
                                    showEnginePicker = false
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
    // Home quick-access list: pinned first, at most eight. The
    // v0.96.0 fluency pass memoised this with remember(bookmarks),
    // but that key is the ONE snapshot-state list instance the app
    // keeps for its whole lifetime — it never changes identity, so
    // the cached sort never re-ran while this page stayed composed:
    // deleting or pinning a bookmark from THIS page's own long-press
    // menu left the grid showing the old card / old order until the
    // page was left and re-entered (v0.117.0 home audit). A derived
    // state keeps the fluency property (the sort re-runs only when
    // the bookmark list actually changes, not on every
    // recomposition) and stays live, exactly like the suggestion
    // list above.
    val homeBookmarks by remember {
        derivedStateOf {
            bookmarks.sortedByDescending { it.pinned }.take(8)
        }
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
                        // The positioning line (v0.187.0): the
                        // home page says what this browser IS —
                        // a purpose-built open-source Android
                        // fingerprint browser — not just what it
                        // does not have.
                        Text(
                            "专门的开源 Android 指纹浏览器",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "双内核 · GPLv3 开源 · 没有信息流，没有推送，没有广告。",
                            style = MaterialTheme.typography.bodySmall,
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
                    // Same refusal as the address bar (v0.31.0):
                    // a non-web scheme typed here is not a search.
                    // submitHomeSearch carries that exact logic; a
                    // successful submit clears the box so the
                    // suggestion list goes away with it (v0.59.0).
                    if (submitHomeSearch(query)) {
                        query = ""
                    }
                },
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { homeSearchFocused = it.isFocused },
                placeholder = {
                    Text("用${currentEngine.label}搜索，或输入网址", maxLines = 1)
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
            ) {}
        }
        // Home search suggestions (v0.59.0): while the search box
        // is focused AND holds text this item lists matches; with no
        // input it exists only for the paste-and-go row (v0.69.0),
        // and only when the clipboard actually holds text — so the
        // default home page otherwise stays exactly brand + search
        // box + quick grid. Rows mirror the
        // address bar's: bookmark matches first, then online words,
        // then the "search this" tail, in an official Card of
        // ListItems inside the home page's own scrolling grid.
        if (homeBookmarkSuggestions.isNotEmpty() ||
            homeShowSearchTail || homeShowPasteRow
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (homeShowPasteRow) {
                            // Top row (v0.69.0): paste-and-go, via
                            // the home box's own submit path (same
                            // blocked-scheme refusal and resolveInput
                            // call as typing it in by hand).
                            val pasteText = homePasteCandidate.orEmpty()
                            val pasteSummary = pasteText
                                .replace(Regex("\\s+"), " ")
                                .take(48)
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "粘贴并打开：$pasteSummary",
                                        maxLines = 1
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Filled.ContentPaste,
                                        contentDescription = "粘贴并打开"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (submitHomeSearch(pasteText)) {
                                            finishHomeSuggestionPick()
                                        }
                                    }
                            )
                        }
                        homeBookmarkSuggestions.forEach { suggestion ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        highlightMatchedText(
                                            suggestion.title, query,
                                            MaterialTheme.colorScheme.primary
                                        ),
                                        maxLines = 1
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        highlightMatchedText(
                                            suggestion.url, query,
                                            MaterialTheme.colorScheme.primary
                                        ),
                                        maxLines = 1
                                    )
                                },
                                leadingContent = {
                                    Icon(
                                        Icons.Filled.Bookmark,
                                        contentDescription = "书签"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val bm = bookmarks.firstOrNull {
                                            it.url == suggestion.url
                                        }
                                        finishHomeSuggestionPick()
                                        if (bm != null) {
                                            onOpenBookmark(bm)
                                        } else {
                                            onSearchUrl(suggestion.url)
                                        }
                                    }
                            )
                        }
                        if (homeShowSearchTail) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        highlightMatchedText(
                                            "用${currentEngine.label}搜索「$homeSuggestionQuery」",
                                            homeSuggestionQuery,
                                            MaterialTheme.colorScheme.primary
                                        ),
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
                                    .clickable {
                                        if (submitHomeSearch(
                                                homeSuggestionQuery
                                            )
                                        ) {
                                            finishHomeSuggestionPick()
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // Quick engine switch: only the short engine name on the
            // chip (a long label would squeeze the search box, the
            // same lesson as the address bar). The search field above
            // keeps the full width of the row.
            AssistChip(
                onClick = { showEnginePicker = true },
                label = { Text(currentEngine.label, maxLines = 1) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null)
                },
                trailingIcon = {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            )
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // Fingerprint mode entry card (v0.187.0): the
            // product's signature feature gets a first-screen
            // seat on its own home page — one tap enters the
            // mode through the same path as the engine-switch
            // dialog's fingerprint option.
            ElevatedCard(
                onClick = onEnterFingerprint,
                modifier = Modifier.fillMaxWidth()
            ) {
                ListItem(
                    headlineContent = { Text("指纹模式") },
                    supportingContent = {
                        Text(
                            "四地区档案，参数互相自洽；带 IP 自查，" +
                                "看看出口对不对得上。"
                        )
                    },
                    leadingContent = {
                        Icon(
                            Icons.Filled.Fingerprint,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "进入指纹模式"
                        )
                    }
                )
            }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            // Open-source entry (v0.187.0): the source lives on
            // GitHub; the row opens the repository in this tab
            // through the home page's own open path.
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text("GitHub 开源仓库") },
                    supportingContent = {
                        Text("github.com/liyuzhou79-star/TuxunBrowse")
                    },
                    leadingContent = {
                        Icon(Icons.Filled.Code, contentDescription = null)
                    },
                    trailingContent = {
                        Icon(
                            Icons.Filled.OpenInNew,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSearchUrl(
                                "https://github.com/liyuzhou79-star/" +
                                    "TuxunBrowse"
                            )
                        }
                )
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
                homeBookmarks,
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
                        headlineContent = {
                            Text(
                                bookmark.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            Text(
                                hostOf(bookmark.url) ?: bookmark.url,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
                        text = "两个内核的登录状态互不相通：在一个内核里登录过的网站，换到另一个内核打开需要重新登录。某个网站在火狐内核下显示不正常时，可以点地址栏里的切换内核按钮，把当前标签切换到系统 WebView（兼容模式），还能记住这个网站以后都用兼容模式打开。",
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
            // Passive indicator (v0.136.0 accessibility pass):
            // the card itself is the one click stop; a live
            // onClick on the radio made TalkBack stop twice on
            // the same choice (same fix as v0.99.0).
            trailingContent = { RadioButton(selected = selected, onClick = null) }
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
    onGoHome: () -> Unit,
    // "Open in the other engine" recovery action (v0.52.0): null
    // hides the button (the caller passes null for anything that is
    // not a real http(s) page failure — internal/blank pages have no
    // engine failure to recover from). The label names the OTHER
    // engine, following the failing tab's current engine, so after a
    // failed switch it points back the way it came.
    switchEngineLabel: String? = null,
    onSwitchEngine: () -> Unit = {},
    // Why there is no switch button (v0.172.0): passed by the
    // fingerprint shell, whose tabs are WebView-only — the mode
    // has no other engine to switch to, so instead of a button
    // that would have to land in the normal group (or silently
    // do nothing), the page says so in plain words.
    switchUnavailableNote: String? = null,
    // "Open over http instead" escape hatch (v0.77.0): only passed
    // when this tab's failed page is one the app itself upgraded
    // from http to https — a site that cannot do https must never
    // leave the user stuck on this error page.
    showHttpFallback: Boolean = false,
    onOpenHttpFallback: () -> Unit = {}
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
        if (showHttpFallback) {
            OutlinedButton(
                onClick = onOpenHttpFallback,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.LockOpen, contentDescription = null)
                Text(
                    "改用 http 打开这个网站",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                "这个网站是按你的设置先换成 https 打开的，可能它还不支持 https。" +
                    "改用 http 只对这一次有效，不会关掉「优先用 HTTPS」设置。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (switchEngineLabel != null) {
            OutlinedButton(
                onClick = onSwitchEngine,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                Text(
                    switchEngineLabel,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                "只换这一次，不会改这个网站以后用哪个内核打开",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (switchEngineLabel == null && switchUnavailableNote != null) {
            Text(
                switchUnavailableNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
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

/**
 * Engine-unavailable fallback page (v0.81.0): shown when a tab's
 * engine view could not be created at all — the system WebView is
 * disabled / missing / broken, or the Gecko runtime failed to
 * start. Same shape as the load-error page above (icon, plain-
 * language reason, the address it was opening, official buttons,
 * a column that scrolls), with the actions decided by the pure
 * engineFallbackActions: open this site in the other engine
 * (http(s) only), retry this engine, go home. Before this
 * version only the Gecko side had any fallback (a bare line of
 * text with no buttons), and a broken system WebView crashed
 * the whole app.
 */
@Composable
private fun EngineUnavailableContent(
    engine: Engine,
    url: String,
    onSwitchEngine: () -> Unit,
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    // Fingerprint shell form (v0.172.0): non-null hides the
    // switch-engine button and shows this note instead — the
    // mode is WebView-only, there is no other engine to switch
    // to, and the tab must not be moved into the normal group.
    switchUnavailableNote: String? = null
) {
    val actions = engineFallbackActions(url)
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
            contentDescription = "内核不可用",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .size(56.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            "内核打不开",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            engineFallbackReason(engine),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (url.isNotBlank() && url != HOME_URL) {
            Text(
                "要打开的网址：$url",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        if (EngineFallbackAction.SWITCH_ENGINE in actions &&
            switchUnavailableNote == null
        ) {
            Button(onClick = onSwitchEngine) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                Text(
                    otherEngineOpenLabel(engine),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Text(
                "只换这一次，不会改这个网站以后用哪个内核打开",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (switchUnavailableNote != null) {
            Text(
                switchUnavailableNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (EngineFallbackAction.RETRY in actions) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("重试", modifier = Modifier.padding(start = 8.dp))
            }
        }
        if (EngineFallbackAction.GO_HOME in actions) {
            OutlinedButton(
                onClick = onGoHome,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Icon(Icons.Filled.Home, contentDescription = null)
                Text("回首页", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

/**
 * The shell's page-load progress bar (extracted in v0.171.0,
 * load-speed audit). The engines report progress per percent
 * during a load; while this value was read inline in the shell's
 * content scope, every callback recomposed that whole scope —
 * address bar, suggestion card and the tab-content wiring
 * included. Read inside this leaf composable instead, a progress
 * tick recomposes only the bar itself. Presentation is unchanged:
 * the bar fades/slides in and out and its value animates smoothly
 * instead of jumping in steps.
 */
@Composable
private fun TabLoadProgressBar(tab: BrowserTab?) {
    val rawProgress = tab?.progress ?: 100
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress / 100f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label = "loadProgress"
    )
    AnimatedVisibility(
        visible = tab != null && rawProgress in 1..99,
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
    onCloseOtherTabsFor: (BrowserTab) -> Unit = {},
    onToggleBookmarkFor: (BrowserTab) -> Unit = {},
    onBookmarkTabs: (List<BrowserTab>) -> Pair<Int, Int> = { 0 to 0 },
    bookmarkFolderNames: List<String> = emptyList(),
    recentClosed: List<ClosedTabInfo>,
    onReopenClosed: (ClosedTabInfo) -> Unit,
    onNewTab: () -> Unit,
    onNewTabWithEngine: (Engine) -> Unit = {},
    mainEngine: Engine = Engine.GECKO,
    onNewIncognitoTab: (Engine) -> Unit,
    incognitoEngine: Engine,
    onIncognitoEngineChosen: (Engine) -> Unit,
    onToggleBookmark: () -> Unit,
    onNavigate: (String) -> Unit,
    bookmarks: List<Bookmark>,
    searchEngine: SearchEngine,
    onOpenSuggestion: (String) -> Unit,
    onGoHome: () -> Unit,
    onReloadPage: () -> Unit,
    onOpenInOtherEngine: () -> Unit = {},
    onOpenHttpFallback: () -> Unit = {},
    onEngineFallbackSwitch: () -> Unit = {},
    onEngineFallbackRetry: () -> Unit = {},
    onSwitchEngine: (Engine, Boolean) -> Unit,
    onEnterFingerprintMode: () -> Unit = {},
    onEnterFingerprintNewTab: () -> Unit = {},
    onOpenClearData: () -> Unit = {},
    onToggleSiteCompat: () -> Unit = {},
    onClearSiteData: (String) -> Boolean = { true },
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
    homeContent: @Composable () -> Unit
) {
    val context = LocalContext.current
    var showTabs by remember { mutableStateOf(false) }
    var tabQuery by remember { mutableStateOf("") }
    var tabSheetMenu by remember { mutableStateOf(false) }
    // Tab sheet multi-select (v0.76.0): while on, card taps toggle
    // a check instead of switching tabs. Selection is a set of tab
    // ids, so it survives the sheet's search filtering untouched.
    // Tab multi-select (v0.76.0) is saveable since v0.85.0 (the
    // second configuration-change audit): rotation itself never
    // recreates the activity (the v0.34.0 configChanges list), but
    // an uncovered configuration change or a process death with
    // state restore used to wipe a half-finished selection. The
    // selected ids are saved as a plain list; tab ids are handed
    // out in creation order, so a restored set still names the
    // tabs the startup restore rebuilt from the archive.
    var tabMultiSelect by rememberSaveable { mutableStateOf(false) }
    var selectedTabIds by rememberSaveable(
        saver = listSaver<MutableState<Set<Int>>, Int>(
            save = { it.value.toList() },
            restore = { mutableStateOf(it.toSet()) }
        )
    ) { mutableStateOf<Set<Int>>(emptySet()) }
    // Selection fingerprints (v0.93.0 audit): the URL each selected
    // tab had when it was picked, saved alongside the ids. Tab ids
    // are handed out by a process-wide counter and the saved session
    // stores no ids, so after a process death the rebuilt tabs get a
    // fresh 1..N id sequence in saved order — a restored id can then
    // name a DIFFERENT tab than the one the user picked (any tab
    // closed earlier, or an incognito tab, which consumes an id but
    // is never saved, shifts the mapping). The validation effect
    // below drops every restored entry whose live tab does not carry
    // the fingerprinted URL, so a stale selection can never act on
    // the wrong tab; entries restored from a build that saved no
    // fingerprints (no map entry) keep the old id-only behaviour for
    // that one restore. Saved as a flat id/url pair list.
    var selectedTabUrls by rememberSaveable(
        saver = listSaver<MutableState<Map<Int, String>>, String>(
            save = { state ->
                state.value.flatMap { (id, url) ->
                    listOf(id.toString(), url)
                }
            },
            restore = { flat ->
                mutableStateOf(
                    flat.chunked(2).mapNotNull { pair ->
                        pair.getOrNull(0)?.toIntOrNull()?.let { id ->
                            id to (pair.getOrNull(1) ?: "")
                        }
                    }.toMap()
                )
            }
        )
    ) { mutableStateOf<Map<Int, String>>(emptyMap()) }
    fun exitTabMultiSelect() {
        tabMultiSelect = false
        selectedTabIds = emptySet()
        selectedTabUrls = emptyMap()
    }
    fun toggleTabSelected(tab: BrowserTab) {
        if (tab.id in selectedTabIds) {
            selectedTabIds = selectedTabIds - tab.id
            selectedTabUrls = selectedTabUrls - tab.id
        } else {
            selectedTabIds = selectedTabIds + tab.id
            selectedTabUrls = selectedTabUrls + (tab.id to tab.url)
        }
    }
    // Selection hygiene (v0.93.0 audit): whenever the live tab set
    // changes, drop selected ids that no longer name a live tab
    // (closed through any other path, replaced by an engine switch)
    // and entries whose fingerprint no longer matches the live tab's
    // URL (a selection restored after process death pointing at a
    // rebuilt tab, or a selected tab that navigated itself). Runs
    // only once tabs exist, so the startup restore — which fills the
    // list after the first composition — is validated, not wiped.
    val tabSelectionKey = tabs.joinToString("|") { "${it.id}:${it.url}" }
    LaunchedEffect(tabSelectionKey) {
        if (tabs.isEmpty() || selectedTabIds.isEmpty()) {
            return@LaunchedEffect
        }
        val liveById = tabs.associateBy { it.id }
        val kept = selectedTabIds.filter { id ->
            val live = liveById[id] ?: return@filter false
            val fingerprinted = selectedTabUrls[id]
            fingerprinted == null || fingerprinted == live.url
        }.toSet()
        if (kept != selectedTabIds) {
            selectedTabIds = kept
            selectedTabUrls = selectedTabUrls.filterKeys { it in kept }
        }
    }
    var showMenu by remember { mutableStateOf(false) }
    var showNewTabMenu by remember { mutableStateOf(false) }
    var showIncognitoPicker by remember { mutableStateOf(false) }
    var showEngineSwitch by remember { mutableStateOf(false) }
    var showFindBar by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    // What the address bar shows for a tab while it is NOT being
    // edited (v0.76.0): only the host for a web page
    // (displayHostFor). Two surfaces are NOT dressed up as a bare
    // domain: the home page (its bar stays empty with the
    // placeholder showing, as before) and a page sitting on the
    // load-error overlay (its full failed URL stays visible, as
    // before). Declared before every use below (local-fun order).
    fun addressDisplayFor(tab: BrowserTab?): String = when {
        tab == null || tab.isHome -> ""
        tab.loadError != null -> tab.url
        else -> displayHostFor(tab.url)
    }
    // The address bar edits a TextFieldValue (not a plain String) so
    // focus can select the whole URL: tapping the bar selects
    // everything and typing replaces it, the way other browsers
    // behave. addressText stays as the plain-text view of the value
    // for the suggestion/search logic below. While unfocused the
    // value holds the friendly display text above; gaining focus
    // swaps it back to the full URL (see the focus effect below).
    var addressValue by remember(activeTab?.id) {
        mutableStateOf(TextFieldValue(addressDisplayFor(activeTab)))
    }
    val addressText = addressValue.text
    var addressFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(activeTab?.url, activeTab?.loadError) {
        if (!addressFocused && activeTab != null) {
            addressValue = TextFieldValue(addressDisplayFor(activeTab))
        }
    }
    // Switching tabs hands focus back to the page: the text above is
    // already re-keyed to the new tab, and this drops any leftover
    // editing focus (and the keyboard) from the previous tab.
    LaunchedEffect(activeTab?.id) {
        focusManager.clearFocus()
        // Find state does not travel across tabs (v0.124.0 find
        // audit): the query text and the bar are screen-level state,
        // while the counter and the highlights live on each tab —
        // left as they were, switching tabs with the bar open would
        // show the incoming tab's stale counter (counted for some
        // older query) under the current query text. Same rule as
        // other browsers: a tab switch closes the find bar, clears
        // the query and clears the incoming tab's find state, so
        // whatever a tab shows was searched on this visit to it.
        if (showFindBar) {
            showFindBar = false
            findQuery = ""
        }
        activeTab?.clearFind()
    }
    // Focus gain swaps the friendly host-only display back to the
    // full URL and selects it whole; focus loss restores the
    // display text. Done in an effect so it lands after the tap's
    // own cursor placement and wins over it (v0.76.0; the select-all
    // behaviour itself is the pre-existing one).
    LaunchedEffect(addressFocused) {
        if (addressFocused) {
            val tab = activeTab
            if (tab != null && !tab.isHome) {
                addressValue = TextFieldValue(
                    tab.url, TextRange(0, tab.url.length)
                )
            } else if (addressValue.text.isNotEmpty()) {
                addressValue = addressValue.copy(
                    selection = TextRange(0, addressValue.text.length)
                )
            }
        } else if (activeTab != null) {
            addressValue = TextFieldValue(addressDisplayFor(activeTab))
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
    // "Paste and go" row (v0.69.0): when the address bar gains focus
    // while empty (or with its whole text selected, which is how the
    // bar presents an existing URL on tap), read the clipboard ONCE
    // — at that focus moment only, never in the background and never
    // at startup — and offer whatever text it holds as the top
    // suggestion row. Android 12+ shows a system notice when an app
    // reads the clipboard; tying the read to this deliberate focus
    // tap keeps the notice attached to an action the user just took.
    // Incognito tabs never read the clipboard here either — the same
    // rule as the link hint above (v0.27.0). An empty or text-less
    // clipboard simply produces no row.
    var pasteCandidate by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(addressFocused, activeTab?.incognito) {
        if (!addressFocused || activeTab?.incognito == true) {
            pasteCandidate = null
            return@LaunchedEffect
        }
        val text = try {
            clipboardManager.getText()?.text?.trim()
        } catch (t: Throwable) {
            null
        }
        pasteCandidate = text?.takeIf { it.isNotEmpty() }
    }
    val addressAllSelected = addressValue.text.isNotEmpty() &&
        addressValue.selection.start == 0 &&
        addressValue.selection.end == addressValue.text.length
    val showPasteRow = addressFocused && pasteCandidate != null &&
        (addressText.isBlank() || addressAllSelected)
    // (The friendly host-only display text itself is computed by
    // addressDisplayFor above, next to the address value's state.)
    var showRecentClosed by remember { mutableStateOf(false) }
    var showSiteInfo by remember { mutableStateOf(false) }
    var showMoreTools by remember { mutableStateOf(false) }
    // Pin the current page to the home screen (v0.86.0): the
    // shortcut's intent is a plain ACTION_VIEW on the page URL,
    // addressed to this app's own activity, so a tap on the
    // shortcut comes back through the exact inbound gate every
    // external link uses (MainActivity takes only http/https,
    // BrowserApp.openUrl re-checks) — no new door is opened. The
    // icon colours are captured here, in composition, because the
    // handler below is a plain local function.
    val shortcutIconBg = MaterialTheme.colorScheme.primary.toArgb()
    val shortcutIconFg = MaterialTheme.colorScheme.onPrimary.toArgb()
    fun handlePinToHomeScreen() {
        val tab = activeTab
        when {
            tab == null || tab.isHome ||
                !(tab.url.startsWith("http://") ||
                    tab.url.startsWith("https://")) ->
                android.widget.Toast.makeText(
                    context, "先打开一个网页再添加到主屏幕",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            // Incognito (v0.72.0 rule, same as the tab sheet's
            // bookmark item): the entry stays visible, the tap is
            // intercepted — a home-screen shortcut IS a trace of
            // the visit, so an incognito page never gets one.
            tab.incognito ->
                android.widget.Toast.makeText(
                    context,
                    "无痕标签不会在主屏幕留下快捷方式，请在普通标签里添加",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            else -> {
                if (!ShortcutManagerCompat
                        .isRequestPinShortcutSupported(context)
                ) {
                    android.widget.Toast.makeText(
                        context,
                        "你现在的桌面不支持把网页放到主屏幕",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return
                }
                try {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(tab.url)
                    ).setClass(context, MainActivity::class.java)
                        .addFlags(
                            android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    val shortcut = ShortcutInfoCompat.Builder(
                        context, pinnedShortcutId(tab.url)
                    )
                        .setShortLabel(
                            pinnedShortcutLabel(tab.title, tab.url)
                        )
                        .setIcon(
                            IconCompat.createWithBitmap(
                                createPinnedShortcutIcon(
                                    shortcutIconBg, shortcutIconFg,
                                    pinnedShortcutInitial(
                                        tab.title, tab.url
                                    )
                                )
                            )
                        )
                        .setIntent(intent)
                        .build()
                    val requested = ShortcutManagerCompat
                        .requestPinShortcut(context, shortcut, null)
                    android.widget.Toast.makeText(
                        context,
                        if (requested) {
                            "已请求添加到主屏幕，按桌面提示确认就会出现"
                        } else {
                            "你现在的桌面不支持把网页放到主屏幕"
                        },
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } catch (t: Throwable) {
                    android.widget.Toast.makeText(
                        context, "添加失败，请重试",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    var showClearSiteConfirm by remember { mutableStateOf(false) }
    var showCloseOthersConfirm by remember { mutableStateOf(false) }
    if (showCloseOthersConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseOthersConfirm = false },
            title = { Text("关闭其他标签页？") },
            text = {
                // Scrollable body (v0.137.0, same v0.115.0
                // treatment): a long body at the largest font
                // scale in a short landscape window must scroll
                // instead of pushing the buttons off-screen.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("会关掉除当前标签外的 ${tabs.size - 1} 个标签页。普通标签会记进「最近关闭」（最多 10 个），之后能在标签列表里一个个恢复；无痕标签不会被记录。无痕标签关掉后，系统 WebView 的登录状态会被清掉。")
                }
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
                // Scrollable body (v0.137.0, same v0.115.0
                // treatment as the close-others dialog above).
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("现在开着的 ${tabs.size} 个标签页会全部关掉，然后开一个新的首页标签。普通标签会记进「最近关闭」（最多 10 个），之后能在标签列表里一个个恢复；无痕标签不会被记录。无痕标签关掉后，系统 WebView 的登录状态会被清掉。")
                }
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
    // Engine-unavailable fallback page (v0.84.0 back audit): the
    // engine never loaded anything, so web-history back is
    // meaningless there and the key used to fall through and exit
    // the app. The fallback page is an app page — back does what
    // its own "回首页" button does. Declared after web back (it
    // wins when both could apply) and before the stronger layers
    // below, so a focused address bar or an open find bar on the
    // fallback page still gets the key first.
    BackHandler(
        enabled = !chromeHidden && activeTab?.engineUnavailable == true
    ) {
        onGoHome()
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
            // Merged toolbar (v0.92.0): the browser chrome used
            // to be two layers — a 64dp TopAppBar (page title,
            // star, menu) over a 56dp address row (back, forward,
            // field, engine switch, reload) — 120dp in total.
            // It is ONE 56dp row now: back, the address field
            // (site icon at its head, the bookmark star inside
            // its tail), the engine switch and the menu. Forward
            // and reload moved into the menu (pull-to-refresh
            // also reloads); the page title still shows in the
            // tab switcher. Every remaining button keeps its
            // 48dp touch target.
            // Narrow-screen width pass (v0.94.0 audit): the row's
            // own padding is 4dp and the children sit gapless —
            // every button already carries its icon padding, so
            // the old 8dp + 4dp gaps only ate address-field width.
            // The two icon buttons INSIDE the field occupy 40dp
            // of layout (their glyphs stay 24dp): at 360dp wide
            // the field's text area grows from ~84dp to ~120dp,
            // enough for the full placeholder; at 320dp it grows
            // from ~44dp to ~80dp. The outside buttons stay 48dp.
            // Touch targets (v0.99.0 accessibility audit): a plain
            // Modifier.size(40.dp) also pinned the buttons' hit
            // area to 40dp — below the 48dp minimum. They now go
            // through expandedTouchTarget: 40dp of layout, a full
            // 48dp hit area overflowing evenly into the field's
            // own padding, visuals and widths unchanged.
            // Chrome-layout pass (v0.180.0): the field becomes a
            // filled pill (site icon / lock inside its head, the
            // star inside its tail), a tab-count button joins
            // the row at its right next to the overflow menu,
            // and the back button leaves the top row — back /
            // forward now lead the bottom toolbar (and the edge
            // gestures from v0.179.0 cover them too). The engine
            // switch keeps its one-tap seat between pill and
            // count: it is this browser's own signature control
            // and the checklist leans on it.
            // Inset ownership (v0.193.0): this Row is the ONE
            // consumer of the status-bar inset in this shell.
            // It is a hand-built Row in the Scaffold's topBar
            // slot, not a TopAppBar, so the Scaffold does not
            // pad it — v0.191.0 deleted this windowInsetsPadding
            // believing the Scaffold had it covered, and the
            // row slid under the status bar, fused with the
            // system icons (user's screenshot, 2026-11-25
            // 21:25). The padding sits outside the pinned row
            // height: total = inset + ToolbarSpec row body,
            // exactly one layer. Never delete it again; blank-
            // space bugs are diagnosed by counting inset
            // consumers (see ToolbarSpec), not by removing
            // layers or editing heights.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .height(ToolbarSpec.ROW_HEIGHT_DP.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unified toolbar row (v0.187.0): home, pill,
                // engine switch, new tab, tab count, menu — the
                // same order and the same numbers in every shell
                // (see ToolbarSpec). The row's height is pinned
                // by ToolbarSpec and every child is a 48dp target, so
                // the pill and the buttons share one centre line
                // by construction: before this version the row
                // took its height from the pill's own intrinsic
                // measurement while the buttons centred on
                // themselves, and on the real device the pill
                // read as sunk below the button line (user's
                // screenshot, 2026-11-25 19:50).
                IconButton(
                    onClick = onGoHome,
                    modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "主页"
                    )
                }
                // The pill is the shared PillTextField
                // (v0.191.0): the stock filled TextField
                // measured against its own 56dp minimum height
                // inside this pinned 48dp box and let the text
                // line sink until the glyphs clipped on the
                // real device (user's screenshots). The shared
                // pill centres its line by construction; see
                // PillTextField.kt. The status icon at its head
                // and the bookmark star at its tail ride along
                // unchanged.
                PillTextField(
                    value = addressValue,
                    onValueChange = { addressValue = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { addressFocused = it.isFocused },
                    placeholder = "输入网址或搜索",
                    leadingIcon = {
                        // Tapping the status icon opens the
                        // site-info sheet (for the home page it
                        // shows the internal-page info, so the
                        // icon always does something).
                        IconButton(
                            onClick = { showSiteInfo = true },
                            modifier = Modifier.expandedTouchTarget(32.dp, 48.dp)
                        ) {
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
                    trailingIcon = {
                        // The bookmark star inside the pill's
                        // tail (v0.92.0): still one tap, still
                        // always visible. The filled/border
                        // swap crossfades (v0.111.0 motion
                        // audit, 200ms on the shared easing).
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.expandedTouchTarget(32.dp, 48.dp)
                        ) {
                            Crossfade(
                                targetState = isBookmarked,
                                animationSpec = tween(
                                    200, easing = FastOutSlowInEasing
                                ),
                                label = "bookmarkStar"
                            ) { bookmarked ->
                                Icon(
                                    if (bookmarked) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription =
                                        if (bookmarked) "已加入书签，点按取消书签" else "加入书签"
                                )
                            }
                        }
                    },
                    // Keyboard fit (v0.138.0 input audit): a
                    // URL keyboard with autocorrect off, so the
                    // IME cannot "fix" a typed host name into a
                    // dictionary word.
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Go
                    ),
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
                    IconButton(
                        onClick = { showEngineSwitch = true },
                        modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                    ) {
                        Icon(
                            Icons.Filled.SwapHoriz,
                            contentDescription = "切换内核（现在是" +
                                if (activeTab.engine == Engine.GECKO) "火狐）" else "系统 WebView）"
                        )
                    }
                }
                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .combinedClickable(
                                onClick = onNewTab,
                                onLongClick = {
                                    showNewTabMenu = true
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "新建标签页"
                        )
                    }
                    DropdownMenu(
                        expanded = showNewTabMenu,
                        onDismissRequest = { showNewTabMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("新建标签页") },
                            leadingIcon = {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            },
                            onClick = { showNewTabMenu = false; onNewTab() }
                        )
                        DropdownMenuItem(
                            text = { Text("用火狐新建") },
                            leadingIcon = {
                                Icon(Icons.Filled.Language, contentDescription = null)
                            },
                            trailingIcon = if (mainEngine == Engine.GECKO) {
                                {
                                    Text(
                                        "默认",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                null
                            },
                            onClick = {
                                showNewTabMenu = false
                                onNewTabWithEngine(Engine.GECKO)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("用系统 WebView 新建") },
                            leadingIcon = {
                                Icon(Icons.Filled.Language, contentDescription = null)
                            },
                            trailingIcon = if (mainEngine == Engine.WEBVIEW) {
                                {
                                    Text(
                                        "默认",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                null
                            },
                            onClick = {
                                showNewTabMenu = false
                                onNewTabWithEngine(Engine.WEBVIEW)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("新建无痕标签页") },
                            leadingIcon = {
                                Icon(Icons.Filled.VisibilityOff, contentDescription = null)
                            },
                            onClick = {
                                showNewTabMenu = false
                                // NOT a shortcut around the
                                // picker: the incognito
                                // engine is still chosen in
                                // the usual dialog (v0.2.0).
                                showIncognitoPicker = true
                            }
                        )
                    }
                }
                // Tab-count button (v0.180.0): the switcher's
                // entry moves up here, Chrome-style — a rounded
                // square outline carrying the open-tab count.
                // The semantics wrapper names it for TalkBack;
                // the tap does exactly what the old bottom-bar
                // tabs item did, query reset included.
                TabCountButton(
                    count = tabs.size,
                    onClick = {
                        onTabsOpened(); tabQuery = ""; showTabs = true
                    }
                )
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "菜单")
                }
            }
            }
        },
        bottomBar = {
            if (!chromeHidden) {
            // Bottom toolbar (v0.180.0 shape, v0.187.0
            // membership): icon-only buttons spread evenly on
            // the same 56dp surface in every shell. Back /
            // forward lead it (both keep the canGo tint rule
            // and 48dp targets), then incognito and settings.
            // New tab moved UP into the unified top row in
            // v0.187.0 (its long-press menu rode along), and
            // the tab switcher sits in the top row's count
            // button — nothing is duplicated between the bars.
            // Container colour/elevation match the old
            // NavigationBar's (surfaceContainer, 3dp), and the
            // navigation-bar inset is padded outside the bar's
            // own height exactly as the v0.92.0 bar did, so
            // nothing slides under the system buttons.
            Surface(
                // Bar zone (v0.193.0): the navigation-bar
                // inset is padded OUTSIDE the bar's own height
                // (the v0.92.0/v0.190.0 construction): the row
                // fills only the 48dp body, centred in it, and
                // the inset band below is plain surface colour
                // keeping the bar clear of the gesture bar.
                // v0.191.0 instead folded the inset into a
                // hand-computed height and centred the row
                // across body+inset, which sank the icons half
                // an inset toward the gesture bar and made the
                // strip read as one big grey field (user's
                // reports, 2026-11-25 21:25). This surface is
                // the shell's ONE navigation-inset consumer.
                // The spread is SpaceEvenly: equal gaps
                // everywhere, no half-size end gaps.
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.only(
                            WindowInsetsSides.Bottom
                        )
                    )
                    .height(ToolbarSpec.BOTTOM_BAR_HEIGHT_DP.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .combinedClickable(
                                enabled =
                                    activeTab?.canGoBack == true,
                                onClick = {
                                    if (activeTab?.canGoBack ==
                                        true
                                    ) {
                                        activeTab?.goBack()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "后退",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            ),
                            tint = if (
                                activeTab?.canGoBack == true
                            ) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.38f)
                            }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .combinedClickable(
                                enabled =
                                    activeTab?.canGoForward == true,
                                onClick = {
                                    if (activeTab?.canGoForward ==
                                        true
                                    ) {
                                        activeTab?.goForward()
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "前进",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            ),
                            tint = if (
                                activeTab?.canGoForward == true
                            ) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface
                                    .copy(alpha = 0.38f)
                            }
                        )
                    }
                    IconButton(
                        onClick = { showIncognitoPicker = true }
                    ) {
                        Icon(
                            Icons.Filled.VisibilityOff,
                            contentDescription = "新建无痕标签页",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            )
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "设置",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            )
                        )
                    }
                }
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
            // Loading progress bar (v0.171.0): the read of the tab's
            // progress state lives inside TabLoadProgressBar's own
            // composition scope, so the per-percent progress
            // callbacks during a load recompose only that leaf —
            // not this whole shell content scope (address bar,
            // suggestion card, tab content) as they used to when
            // the value was read inline here.
            TabLoadProgressBar(tab = activeTab)
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
            // below): history stays on the History page and is not
            // mixed into the address bar. Startup/open-speed audit
            // (v0.95.0): the match runs in a derived state, so it
            // re-runs only when the text, the focus or the bookmark
            // list actually changes — as plain composition code it
            // re-scanned the whole list (imports can hold thousands
            // of entries) on every recomposition, including the ones
            // page-load progress drives while the user is typing.
            // Keyed on the tab for the same reason addressValue is:
            // the text state is re-created per tab.
            val suggestions by remember(activeTab?.id) {
                derivedStateOf {
                    val text = addressValue.text
                    if (addressFocused && text.isNotBlank()) {
                        matchSuggestions(text, bookmarks, emptyList())
                    } else {
                        emptyList()
                    }
                }
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
                visible = suggestions.isNotEmpty() ||
                    showSearchTail || showPasteRow,
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
                        if (showPasteRow) {
                            item(key = "paste-and-go") {
                                // Top row (v0.69.0): paste-and-go.
                                // Tapping opens the clipboard text
                                // through the exact same path as
                                // submitting the bar by hand, so a URL
                                // goes straight there and plain words
                                // become a search (resolveInput inside
                                // onNavigate; blocked schemes are
                                // refused there too).
                                val pasteText = pasteCandidate.orEmpty()
                                val pasteSummary = pasteText
                                    .replace(Regex("\\s+"), " ")
                                    .take(48)
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "粘贴并打开：$pasteSummary",
                                            maxLines = 1
                                        )
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Filled.ContentPaste,
                                            contentDescription = "粘贴并打开"
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onNavigate(pasteText)
                                            focusManager.clearFocus()
                                        }
                                )
                            }
                        }
                        items(suggestions, key = { it.url }) { suggestion ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        highlightMatchedText(
                                            suggestion.title, addressText,
                                            MaterialTheme.colorScheme.primary
                                        ),
                                        maxLines = 1
                                    )
                                },
                                supportingContent = {
                                    Text(
                                        highlightMatchedText(
                                            suggestion.url, addressText,
                                            MaterialTheme.colorScheme.primary
                                        ),
                                        maxLines = 1
                                    )
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
                                            highlightMatchedText(
                                                "用${searchEngine.label}搜索「$trimmedAddress」",
                                                trimmedAddress,
                                                MaterialTheme.colorScheme.primary
                                            ),
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
                        onValueChange = { newQuery ->
                            findQuery = newQuery
                            // Find as you type (v0.124.0): before
                            // this, editing the text left the old
                            // query's counter and highlights on
                            // screen until a button was pressed, so
                            // the bar could show "3 / 10" for words
                            // no longer in the box. Every edit now
                            // re-runs the find (a changed query starts
                            // a fresh find inside the tab), and
                            // emptying the box clears the find
                            // altogether.
                            if (newQuery.isBlank()) {
                                onClearFind()
                            } else {
                                onFind(newQuery, true)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(findFocusRequester),
                        singleLine = true,
                        placeholder = { Text("在页面里查找") },
                        // No auto-correct (v0.138.0): the words
                        // being hunted are page text, often names
                        // or code — an IME rewriting them would
                        // search for something never typed.
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search
                        ),
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
                    if (activeTab?.engine == Engine.GECKO) {
                        // GeckoView offers no match-count API
                        // (v0.9.0), and an unlabelled empty space
                        // where WebView tabs show a counter reads
                        // as "nothing found" (v0.124.0). Say so
                        // plainly instead of faking a number:
                        // matches still highlight and scroll into
                        // view exactly as on the WebView side.
                        Text(
                            "火狐内核不显示计数",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
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
                    tab.engineUnavailable ->
                        // The engine itself could not be created
                        // (v0.81.0): the app's own fallback page,
                        // the same shape for both engines.
                        EngineUnavailableContent(
                            engine = tab.engine,
                            url = tab.url,
                            onSwitchEngine = onEngineFallbackSwitch,
                            onRetry = onEngineFallbackRetry,
                            onGoHome = onGoHome
                        )
                    tab.loadError != null ->
                        // The page failed to load at the network level:
                        // show the app's own error page instead of the
                        // engine's blank/default one. It disappears on
                        // its own once a load succeeds (both engines
                        // clear the state when a new load starts).
                        LoadErrorContent(
                            error = tab.loadError!!,
                            onReload = onReloadPage,
                            onGoHome = onGoHome,
                            switchEngineLabel =
                                if (tab.loadError!!.failedUrl.startsWith("http://") ||
                                    tab.loadError!!.failedUrl.startsWith("https://")
                                ) {
                                    when (tab.engine) {
                                        Engine.GECKO -> "用系统 WebView 打开"
                                        Engine.WEBVIEW -> "用火狐内核打开"
                                    }
                                } else null,
                            onSwitchEngine = onOpenInOtherEngine,
                            showHttpFallback = tab.httpsUpgraded &&
                                tab.loadError!!.failedUrl.startsWith("https://"),
                            onOpenHttpFallback = onOpenHttpFallback
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
            // The refreshing flag as a derived state (v0.171.0):
            // reading progress directly here would subscribe this
            // whole shell scope to every per-percent progress
            // callback; the derived boolean only changes when the
            // load actually starts or ends, which is all the box
            // ever acts on.
            val refreshActive by remember(refreshTab?.id) {
                derivedStateOf {
                    refreshTab?.let { it.progress < 100 } ?: false
                }
            }
            if (refreshTab != null && !refreshTab.isHome &&
                refreshTab.loadError == null &&
                !refreshTab.engineUnavailable && !chromeHidden
            ) {
                PullToRefreshBox(
                    isRefreshing = refreshActive,
                    onRefresh = onReloadPage,
                    modifier = Modifier.fillMaxSize()
                ) {
                    tabContent()
                }
            } else {
                tabContent()
            }
            // Edge-swipe navigation (v0.179.0): over the web
            // content only (fullscreen chrome-hidden keeps it
            // too — the early return above hosts its own view —
            // this overlay sits in the normal content box).
            if (activeTab != null && !chromeHidden) {
                EdgeSwipeNavigation(
                    onBack = { activeTab?.goBack() },
                    onForward = { activeTab?.goForward() }
                )
            }
        }
    }

    // ---- Main menu: the floating panel (v0.188.0) ----
    // The DropdownMenu is gone: the menu is now the shared
    // FloatingMenuPanel — a rounded card floating at the top
    // end with air on every side, a circle-button row (back /
    // forward / star / downloads / reload) and grouped rows,
    // the same form the fingerprint shell's menu uses. Only
    // real pathways are listed: there is no page-translation
    // feature in this browser, so no translation row is shown
    // (the sample browser has one; a fake entry is worse than
    // an absent one).
    FloatingMenuPanel(
        visible = showMenu,
        onDismiss = { showMenu = false },
        topOffsetDp = 64
    ) {
        MenuPanelCircleRow {
            FilledTonalIconButton(
                onClick = { showMenu = false; activeTab?.goBack() },
                enabled = activeTab?.canGoBack == true
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "后退"
                )
            }
            FilledTonalIconButton(
                onClick = {
                    showMenu = false; activeTab?.goForward()
                },
                enabled = activeTab?.canGoForward == true
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "前进"
                )
            }
            FilledTonalIconButton(
                onClick = { showMenu = false; onToggleBookmark() }
            ) {
                Icon(
                    if (isBookmarked) {
                        Icons.Filled.Star
                    } else {
                        Icons.Filled.StarBorder
                    },
                    contentDescription =
                        if (isBookmarked) {
                            "已加入书签，点按取消书签"
                        } else {
                            "加入书签"
                        }
                )
            }
            FilledTonalIconButton(
                onClick = { showMenu = false; onOpenDownloads() }
            ) {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = "下载"
                )
            }
            if (activeTab?.loading == true) {
                FilledTonalIconButton(
                    onClick = {
                        showMenu = false; activeTab?.stopLoading()
                    }
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "停止加载"
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = { showMenu = false; activeTab?.reload() }
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "重新加载"
                    )
                }
            }
        }
        MenuPanelDivider()
        MenuPanelItem(
            icon = Icons.Filled.Add,
            label = "新建标签页",
            onClick = { showMenu = false; onNewTab() }
        )
        MenuPanelItem(
            icon = Icons.Filled.VisibilityOff,
            label = "新建无痕标签页",
            onClick = { showMenu = false; showIncognitoPicker = true }
        )
        MenuPanelItem(
            icon = Icons.Filled.History,
            label = "历史记录",
            onClick = { showMenu = false; onOpenHistory() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Restore,
            label = "最近关闭的标签页",
            onClick = {
                showMenu = false
                onTabsOpened(); tabQuery = ""; showTabs = true
            }
        )
        MenuPanelItem(
            icon = Icons.Filled.Delete,
            label = "删除浏览资料",
            onClick = { showMenu = false; onOpenClearData() }
        )
        MenuPanelDivider()
        MenuPanelItem(
            icon = Icons.Filled.Tune,
            label = "网站控制选项",
            onClick = { showMenu = false; showSiteInfo = true }
        )
        MenuPanelItem(
            icon = Icons.Filled.Bookmark,
            label = "书签",
            onClick = { showMenu = false; onOpenBookmarks() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Share,
            label = "分享当前页",
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
                                android.content.Intent.EXTRA_TEXT,
                                tab.url
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
        MenuPanelItem(
            icon = Icons.Filled.ContentCopy,
            label = "复制当前网址",
            onClick = {
                showMenu = false
                val tab = activeTab
                if (tab == null || tab.isHome) {
                    android.widget.Toast.makeText(
                        context, "现在没有可复制的网址",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val ok = copyTextToClipboard(context, "url", tab.url)
                    android.widget.Toast.makeText(
                        context,
                        if (ok) "网址已复制" else "复制失败，请重试",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        MenuPanelItem(
            icon = Icons.Filled.Search,
            label = "在网页中寻找",
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
        MenuPanelItem(
            icon = Icons.Filled.MenuBook,
            label = if (activeTab?.readerActive == true) {
                "退出阅读模式"
            } else {
                "显示阅读模式"
            },
            onClick = { showMenu = false; onToggleReaderMode() }
        )
        MenuPanelItem(
            icon = Icons.Filled.AddToHomeScreen,
            label = "安装并建立捷径",
            supporting = "把当前页加到主屏幕",
            onClick = { showMenu = false; handlePinToHomeScreen() }
        )
        if (activeTab?.isHome != true) {
            MenuPanelItem(
                icon = Icons.Filled.Language,
                label = "桌面版网站",
                supporting = if (activeTab?.incognito == true) {
                    "无痕标签里只对这个标签生效，不会记住这个网站"
                } else {
                    "打开后会记住这个网站，下次打开自动用桌面版"
                },
                trailing = {
                    Checkbox(
                        checked = activeDesktopMode,
                        onCheckedChange = { onToggleDesktopMode(it) },
                        modifier = Modifier.semantics {
                            contentDescription = "桌面版网站"
                        }
                    )
                },
                onClick = { onToggleDesktopMode(!activeDesktopMode) }
            )
        }
        MenuPanelDivider()
        MenuPanelItem(
            icon = Icons.Filled.SwapHoriz,
            label = "切换内核",
            onClick = { showMenu = false; showEngineSwitch = true }
        )
        MenuPanelItem(
            icon = Icons.Filled.Fingerprint,
            label = "指纹模式",
            onClick = { showMenu = false; onEnterFingerprintMode() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Settings,
            label = "设置",
            onClick = { showMenu = false; onOpenSettings() }
        )
        MenuPanelItem(
            icon = Icons.Filled.MoreHoriz,
            label = "更多工具",
            onClick = { showMenu = false; showMoreTools = true }
        )
    }

    // ---- More-tools sheet: the low-frequency tools that left the
    // main menu. ----
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
                // Scrollable body (v0.137.0 visual regression pass,
                // same treatment as the v0.115.0 dialogs): this is
                // the longest dialog body in the app and it carries
                // an unbounded external value (the site's host); at
                // the largest font scale in a short landscape window
                // an unscrollable Text could push the buttons off
                // the bottom. verticalScroll changes nothing when
                // the content fits.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "会清掉火狐里 $siteHost 的 Cookie 和网站数据，并删掉本机保存的" +
                            "这个网站的历史记录。注意：系统 WebView 的网站数据没法按网站单独清" +
                            "（系统 WebView 不支持按网站清除），这次不会清系统 WebView 的数据；" +
                            "要清的话只能去设置里的「清除浏览数据」整清。"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showClearSiteConfirm = false
                    val geckoCleared = onClearSiteData(siteHost)
                    android.widget.Toast.makeText(
                        context,
                        if (geckoCleared) {
                            "已清除 $siteHost 在火狐里的数据和本机历史"
                        } else {
                            // The Gecko runtime never started this
                            // session, so its saved site data could
                            // not be touched (v0.125.0): say so
                            // instead of claiming a clear that did
                            // not happen. The history half did run.
                            "本机历史已删；火狐里的数据这次没清成" +
                                "（火狐内核还没启动），打开一个火狐标签后再清一次"
                        },
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
                            // Same guard as the clear-data button
                            // below (v0.125.0): with no parseable
                            // host the toggle would close the sheet
                            // and silently do nothing.
                            enabled = siteHost != null,
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
    // Open the sheet already scrolled to the active tab
    // (v0.131.0 tab-sheet audit): the grid used to always start
    // at the very first tab, so with many tabs open the user had
    // to hunt for the one they were on — the sheet's whole point
    // is the current tab's neighbourhood. The scroll runs once
    // per opening (keyed on showTabs only, so typing in the
    // search field never yanks the list around); the item index
    // counts the full-span rows ahead of the cards — the
    // header row (v0.190.0: it carries the search pill now),
    // plus the multi-select action row while multi-select
    // is on.
    val tabGridState = rememberLazyGridState()
    LaunchedEffect(showTabs) {
        if (showTabs) {
            val activeIndex = visibleTabs.indexOfFirst {
                it.id == activeTab?.id
            }
            if (activeIndex >= 0) {
                val headerItems = if (tabMultiSelect) 2 else 1
                tabGridState.scrollToItem(headerItems + activeIndex)
            }
        }
    }
    if (showTabs) {
        ModalBottomSheet(
            onDismissRequest = { showTabs = false; exitTabMultiSelect() },
            sheetState = sheetState
        ) {
            // Back first leaves multi-select, only then closes the
            // sheet (v0.76.0; same layering as the bookmarks batch
            // mode's BackHandler, v0.19.0).
            BackHandler(enabled = tabMultiSelect) { exitTabMultiSelect() }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = tabGridState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (tabMultiSelect) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Text(
                                "已选 ${tabs.count { it.id in selectedTabIds }} 个",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        liveRegion =
                                            LiveRegionMode.Polite
                                    }
                            )
                            TextButton(
                                onClick = { exitTabMultiSelect() }
                            ) {
                                Text("退出多选")
                            }
                        }
                    } else {
                        // Chrome-style header row (v0.190.0): a
                        // filled + button, the pill search field,
                        // the tab-count box and a floating menu —
                        // the same component family and numbers as
                        // the shells' toolbar (ToolbarSpec). The
                        // search field lives in this row now; the
                        // old standalone search row is gone.
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = {
                                    showTabs = false; onNewTab()
                                }
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "新增分页"
                                )
                            }
                            PillTextField(
                                value = tabQuery,
                                onValueChange = { tabQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = "搜寻分页",
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = null
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    autoCorrectEnabled = false,
                                    imeAction = ImeAction.Search
                                )
                            )

                            TabCountButton(
                                count = tabs.size,
                                onClick = {}
                            )
                            Box {
                                IconButton(
                                    onClick = { tabSheetMenu = true }
                                ) {
                                    Icon(
                                        Icons.Filled.MoreVert,
                                        contentDescription = "更多选项"
                                    )
                                }
                                MaterialTheme(
                                    shapes = MaterialTheme.shapes.copy(
                                        extraSmall =
                                            RoundedCornerShape(16.dp)
                                    )
                                ) {
                                    DropdownMenu(
                                        expanded = tabSheetMenu,
                                        onDismissRequest = {
                                            tabSheetMenu = false
                                        },
                                        offset = DpOffset(0.dp, 8.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("新增分页") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                tabSheetMenu = false
                                                showTabs = false
                                                onNewTab()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text("新增无痕分页")
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled
                                                        .VisibilityOff,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                tabSheetMenu = false
                                                showTabs = false
                                                showIncognitoPicker = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text("关闭所有分页")
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                tabSheetMenu = false
                                                showTabs = false
                                                onCloseAllTabs()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("选取分页") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                tabSheetMenu = false
                                                tabMultiSelect = true
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Text("删除浏览资料")
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Delete,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                tabSheetMenu = false
                                                showTabs = false
                                                onOpenClearData()
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("设定") },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Filled.Settings,
                                                    null
                                                )
                                            },
                                            onClick = {
                                                tabSheetMenu = false
                                                showTabs = false
                                                onOpenSettings()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if (tabMultiSelect) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        val selectedTabs = tabs.filter {
                            it.id in selectedTabIds
                        }
                        Row(
                            // Multi-select action row animation
                            // (v0.82.0 visual pass): the lazy-grid
                            // item fade in/out on the shared
                            // 220/180ms spec instead of popping in.
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(
                                    fadeInSpec = tween(
                                        220, easing = FastOutSlowInEasing
                                    ),
                                    fadeOutSpec = tween(
                                        180, easing = FastOutSlowInEasing
                                    )
                                ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                enabled = selectedTabs.isNotEmpty(),
                                onClick = {
                                    // The regular close path, one
                                    // tab at a time: recently-closed
                                    // stack, incognito WebView wipe
                                    // and the active-tab fallback all
                                    // behave exactly as closing by
                                    // hand (v0.76.0). Reverse tab
                                    // order (v0.131.0): each close
                                    // pushes onto the TOP of the
                                    // recently-closed stack, so the
                                    // batch lands with its earliest
                                    // tab on top and reopening
                                    // restores the original order —
                                    // same rule as close-all and
                                    // close-others.
                                    selectedTabs.asReversed().forEach {
                                        onCloseTab(it)
                                    }
                                    exitTabMultiSelect()
                                    showTabs = false
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("关闭所选") }
                            FilledTonalButton(
                                enabled = selectedTabs.isNotEmpty(),
                                onClick = {
                                    val (added, skipped) =
                                        onBookmarkTabs(selectedTabs)
                                    android.widget.Toast.makeText(
                                        context,
                                        if (skipped > 0) {
                                            "已加 $added 个书签，" +
                                                "无痕标签跳过 $skipped 个"
                                        } else {
                                            "已加 $added 个书签"
                                        },
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    exitTabMultiSelect()
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("加入书签") }
                        }
                    }
                }
                if (visibleTabs.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(Icons.Filled.Search, "没有找到相关的标签页。")
                    }
                }
                gridItems(visibleTabs, key = { it.id }) { tab ->
                    val isActive = tab.id == activeTab?.id
                    // Engine / incognito badge text, shared by the
                    // badge chip (visual) and the card's own
                    // semantics (what TalkBack reads), v0.136.0.
                    val engineBadge =
                        (if (tab.incognito) "无痕 · " else "") +
                            if (tab.engine == Engine.GECKO) {
                                "火狐"
                            } else {
                                "系统 WebView"
                            }
                    // Long-press menu (v0.72.0): the card itself
                    // carries combinedClickable (tap still selects
                    // the tab, the close button keeps its own tap),
                    // and the official DropdownMenu anchors to the
                    // card's Box.
                    var tabMenuOpen by remember(tab.id) {
                        mutableStateOf(false)
                    }
                    Box(
                        // Card enter / exit / placement animation
                        // (v0.131.0 tab-sheet audit): every other
                        // long list got the shared 220/180ms
                        // animateItem treatment in the v0.82.0 /
                        // v0.111.0 motion passes, but the tab cards
                        // themselves still popped in and out
                        // instantly when a tab was closed from the
                        // sheet — the remaining cards jumped to
                        // their new spots with no motion at all.
                        // Same spec as the rest of the app.
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(
                                220, easing = FastOutSlowInEasing
                            ),
                            fadeOutSpec = tween(
                                180, easing = FastOutSlowInEasing
                            )
                        )
                    ) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isActive) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme
                                            .primary,
                                        RoundedCornerShape(24.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            // Selection state on the card itself
                            // (v0.99.0 accessibility audit): in
                            // multi-select a tap on the card toggles
                            // the tab, but the card's semantics
                            // carried no state — TalkBack users
                            // heard the state only on the Checkbox.
                            // stateDescription puts "已选 / 未选" on
                            // the card stop as well; visuals and
                            // behaviour are unchanged.
                            // The card stop also carries the engine
                            // badge as its content description
                            // (v0.136.0 accessibility pass): the
                            // badge chip itself is a label with an
                            // empty onClick, which left a dead
                            // button stop on every card — the chip
                            // is now cleared from the semantics
                            // tree and its text lives here instead.
                            .semantics {
                                contentDescription = engineBadge
                                if (tabMultiSelect) {
                                    stateDescription =
                                        if (tab.id in selectedTabIds) {
                                            "已选"
                                        } else {
                                            "未选"
                                        }
                                }
                            }
                            .combinedClickable(
                                onClick = {
                                    // Multi-select (v0.76.0): a tap
                                    // toggles the check instead of
                                    // switching to the tab.
                                    if (tabMultiSelect) {
                                        toggleTabSelected(tab)
                                    } else {
                                        onSelectTab(tab); showTabs = false
                                    }
                                },
                                onLongClick = {
                                    if (!tabMultiSelect) {
                                        tabMenuOpen = true
                                    }
                                }
                            ),
                        colors = if (
                            tabMultiSelect && tab.id in selectedTabIds
                        ) {
                            CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        } else {
                            CardDefaults.elevatedCardColors()
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            MaterialTheme.colorScheme
                                                .surfaceContainerHighest,
                                            CircleShape
                                        ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {
                                    Icon(
                                        if (tab.isHome) {
                                            Icons.Filled.Home
                                        } else {
                                            Icons.Filled.Language
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                    )
                                }
                                Text(
                                    if (tab.isHome) {
                                        "首页"
                                    } else {
                                        tab.title.ifBlank {
                                            "新标签页"
                                        }
                                    },
                                    style = MaterialTheme.typography
                                        .titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )
                                if (tabMultiSelect) {
                                    // Multi-select (v0.76.0): the
                                    // official Checkbox carries the
                                    // selection state in place of
                                    // the close button.
                                    Checkbox(
                                        checked = tab.id in selectedTabIds,
                                        onCheckedChange = {
                                            toggleTabSelected(tab)
                                        },
                                        // Name the toggle for TalkBack
                                        // (v0.78.0 accessibility pass):
                                        // a bare Checkbox announces
                                        // only checked / not checked,
                                        // never WHICH tab it selects.
                                        modifier = Modifier.semantics {
                                            contentDescription =
                                                "选择标签：" +
                                                if (tab.isHome) {
                                                    "首页"
                                                } else {
                                                    tab.title.ifBlank {
                                                        tab.url
                                                    }
                                                }
                                        }
                                    )
                                } else {
                                    IconButton(
                                        onClick = { onCloseTab(tab) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "关闭标签"
                                        )
                                    }
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
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        MaterialTheme.colorScheme
                                            .surfaceContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val thumb = tab.thumbnail
                                // Convert once per thumbnail (v0.96.0
                                // fluency pass): asImageBitmap() wraps
                                // the Bitmap in a fresh ImageBitmap on
                                // every call, and this card recomposes
                                // on every selection toggle in the
                                // sheet — without the remember, every
                                // visible card re-wrapped its bitmap
                                // each time.
                                val thumbBitmap = remember(thumb) {
                                    thumb?.asImageBitmap()
                                }
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
                                    thumbBitmap != null -> FoundationImage(
                                        bitmap = thumbBitmap,
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
                                    "TuxunBrowse 首页"
                                } else {
                                    hostOf(tab.url) ?: tab.url
                                },
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                    DropdownMenu(
                        expanded = tabMenuOpen,
                        onDismissRequest = { tabMenuOpen = false }
                    ) {
                        if (tabs.size > 1) {
                            DropdownMenuItem(
                                text = { Text("关闭其他标签") },
                                // Leading icon (v0.169.0 alignment
                                // pass): every other item in this
                                // menu — and every item in the main
                                // and new-tab menus — carries one;
                                // this item was the lone exception.
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    tabMenuOpen = false
                                    showTabs = false
                                    onCloseOtherTabsFor(tab)
                                }
                            )
                        }
                        if (!tab.isHome) {
                            DropdownMenuItem(
                                text = { Text("复制网址") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    tabMenuOpen = false
                                    val ok = copyTextToClipboard(
                                        context, "url", tab.url
                                    )
                                    android.widget.Toast.makeText(
                                        context,
                                        if (ok) {
                                            "网址已复制"
                                        } else {
                                            "复制失败，请重试"
                                        },
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                        // Bookmark toggle: an incognito tab never
                        // writes one (v0.72.0, the app-wide
                        // incognito rule — nothing from an
                        // incognito tab is persisted). The item
                        // stays visible but is intercepted with a
                        // plain-language toast instead of saving.
                        val tabBookmarked = !tab.incognito &&
                            bookmarks.any { it.url == tab.url }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (tabBookmarked) {
                                        "取消书签"
                                    } else {
                                        "加入书签"
                                    }
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (tabBookmarked) {
                                        Icons.Filled.Star
                                    } else {
                                        Icons.Filled.StarBorder
                                    },
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                tabMenuOpen = false
                                if (tab.incognito) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "无痕标签不会保存书签，" +
                                            "请在普通标签里加入书签",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    onToggleBookmarkFor(tab)
                                }
                            }
                        )
                    }
                    }
                }
                if (!tabMultiSelect && recentClosed.isNotEmpty()) {
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
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    (if (info.engine == Engine.GECKO) {
                                                        "火狐 · "
                                                    } else {
                                                        "系统 WebView · "
                                                    }) + (hostOf(info.url) ?: info.url),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
                if (!tabMultiSelect && tabs.size > 1) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        OutlinedButton(
                            onClick = { showCloseOthersConfirm = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("关闭其他标签页")
                        }
                    }
                }
                if (!tabMultiSelect && tabs.size > 1) {
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
                                    // Passive indicator (v0.136.0 accessibility
                                    // pass): the enclosing row/card is the one
                                    // click stop; a live onClick here made
                                    // TalkBack stop twice on the same choice
                                    // (same fix as v0.99.0).
                                    onClick = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { picked = engine }
                        )
                    }
                    item {
                        // Fingerprint mode rides along in this
                        // picker (v0.145.0, user-directed): not a
                        // third engine — a separate mode with its
                        // own shell and tab group, entered from
                        // here as well as from 设置 → 隐私与数据.
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("指纹模式") },
                            supportingContent = {
                                Text("用预制档案浏览，网站看到的是档案设备的信息")
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Filled.Fingerprint,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showEngineSwitch = false
                                    onEnterFingerprintMode()
                                }
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
                                    // Named for TalkBack (v0.136.0, same fix as
                                    // the v0.99.0 privacy switches): a bare Switch
                                    // announces only its on/off state.
                                    Switch(
                                        checked = rememberSite,
                                        onCheckedChange = { rememberSite = it },
                                        modifier = Modifier.semantics {
                                            contentDescription = "这个网站以后都用兼容模式打开"
                                        }
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
                                    // Named for TalkBack (v0.136.0, same fix as
                                    // the v0.99.0 privacy switches): a bare Switch
                                    // announces only its on/off state.
                                    Switch(
                                        checked = rememberSite,
                                        onCheckedChange = { rememberSite = it },
                                        modifier = Modifier.semantics {
                                            contentDescription = "取消这个网站的自动兼容记忆"
                                        }
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
        var pickedFingerprint by remember { mutableStateOf(false) }
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
                                    selected = picked == engine &&
                                        !pickedFingerprint,
                                    // Passive indicator (v0.136.0 accessibility
                                    // pass): the enclosing row/card is the one
                                    // click stop; a live onClick here made
                                    // TalkBack stop twice on the same choice
                                    // (same fix as v0.99.0).
                                    onClick = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                                                .clickable {
                                    picked = engine
                                    pickedFingerprint = false
                                }
                        )
                    }
                    item {
                        // Fingerprint option (v0.188.0): not an
                        // engine, and NOT remembered as the
                        // incognito engine — picking it enters
                        // fingerprint mode with a fresh
                        // fingerprint tab instead of opening an
                        // incognito tab here.
                        ListItem(
                            headlineContent = { Text("指纹模式") },
                            supportingContent = {
                                Text("不记历史，按当前档案伪装参数")
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = pickedFingerprint,
                                    onClick = null
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pickedFingerprint = true }
                        )
                    }
                    item {
                        Text(
                            "选指纹模式不会改上面记住的无痕内核。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showIncognitoPicker = false
                    if (pickedFingerprint) {
                        // Deliberately NOT routed through
                        // onIncognitoEngineChosen: the remembered
                        // incognito engine stays whatever it was.
                        onEnterFingerprintNewTab()
                    } else {
                        onIncognitoEngineChosen(picked)
                        onNewIncognitoTab(picked)
                    }
                }) {
                    Text(
                        if (pickedFingerprint) {
                            "打开指纹标签"
                        } else {
                            "打开无痕标签"
                        }
                    )
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun BookmarksScreen(
    bookmarks: List<Bookmark>,
    folders: List<String> = emptyList(),
    onBack: () -> Unit,
    onOpen: (Bookmark) -> Unit,
    onDelete: (Bookmark) -> Unit,
    onEdit: (Bookmark, String, String, String) -> Unit,
    onCreateFolder: (String) -> Unit = {},
    onRenameFolder: (String, String) -> Unit = { _, _ -> },
    onDeleteFolder: (String) -> Unit = {},
    onExport: () -> Unit,
    onImport: () -> Unit,
    onDeleteMany: (List<Bookmark>) -> Unit = {},
    onOpenInNewTab: (Bookmark) -> Unit = {},
    onCopyUrl: (Bookmark) -> Unit = {},
    onTogglePin: (Bookmark) -> Unit = {}
) {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Bookmark?>(null) }
    val editingBookmark = editing
    if (editingBookmark != null) {
        var editTitle by remember(editingBookmark) { mutableStateOf(editingBookmark.title) }
        var editUrl by remember(editingBookmark) { mutableStateOf(editingBookmark.url) }
        // Folder picker (v0.72.0): pick an existing folder (or
        // uncategorized), or switch to typing a brand-new name.
        var editFolder by remember(editingBookmark) {
            mutableStateOf(editingBookmark.folder)
        }
        var editFolderIsNew by remember(editingBookmark) {
            mutableStateOf(false)
        }
        var editNewFolder by remember(editingBookmark) { mutableStateOf("") }
        var editFolderMenuOpen by remember(editingBookmark) {
            mutableStateOf(false)
        }
        // Opens focused on the first field with the keyboard up.
        val editFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { editFocusRequester.requestFocus() }
        // The inline new-folder field only appears after a menu
        // tap; without this it showed up unfocused and needed a
        // second tap before typing (v0.83.0 input audit).
        val editNewFolderFocusRequester = remember { FocusRequester() }
        LaunchedEffect(editFolderIsNew) {
            if (editFolderIsNew) editNewFolderFocusRequester.requestFocus()
        }
        // Enter chain (v0.104.0 input audit, third round): this
        // dialog's three fields carried no IME actions at all, so
        // Enter did nothing in any of them. Now it walks the same
        // Next/Done chain the auth dialog got in v0.83.0 — title
        // moves to the URL, the URL moves to the new-folder field
        // when that is showing, and the last field's Done saves
        // through this one shared confirm path (button and Enter
        // behave identically, including the blocked-scheme refusal).
        val editUrlFocusRequester = remember { FocusRequester() }
        fun confirmEditBookmark() {
            if (editUrl.isBlank()) return
            if (hasBlockedPageScheme(editUrl)) {
                // Non-web schemes are never saved as
                // bookmarks (v0.31.0, same rule as openUrl).
                android.widget.Toast.makeText(
                    context,
                    "这个链接打不开：只支持 http/https 开头的网址",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            } else {
                val resolved = resolveInput(editUrl)
                onEdit(
                    editingBookmark,
                    editTitle.ifBlank { resolved },
                    resolved,
                    if (editFolderIsNew) {
                        editNewFolder.trim()
                    } else {
                        editFolder
                    }
                )
                editing = null
            }
        }
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
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    editUrlFocusRequester.requestFocus()
                                }
                            )
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = editUrl,
                            onValueChange = { editUrl = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(editUrlFocusRequester),
                            label = { Text("网址") },
                            singleLine = true,
                            // URL keyboard, no auto-correct —
                            // same fit as the address bar
                            // (v0.138.0 input audit).
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                autoCorrectEnabled = false,
                                imeAction = if (editFolderIsNew) {
                                    ImeAction.Next
                                } else {
                                    ImeAction.Done
                                }
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    editNewFolderFocusRequester
                                        .requestFocus()
                                },
                                onDone = { confirmEditBookmark() }
                            )
                        )
                    }
                    item {
                        if (editFolderIsNew) {
                            OutlinedTextField(
                                value = editNewFolder,
                                onValueChange = { editNewFolder = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(editNewFolderFocusRequester),
                                label = { Text("新文件夹名称") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = { confirmEditBookmark() }
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        editFolderIsNew = false
                                    }) {
                                        Icon(
                                            Icons.Filled.Close,
                                            contentDescription = "改回选择已有文件夹"
                                        )
                                    }
                                }
                            )
                        } else {
                            Box {
                                OutlinedButton(
                                    onClick = { editFolderMenuOpen = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        if (editFolder.isBlank()) {
                                            "文件夹：未分类"
                                        } else {
                                            "文件夹：$editFolder"
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        Icons.Filled.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                                DropdownMenu(
                                    expanded = editFolderMenuOpen,
                                    onDismissRequest = {
                                        editFolderMenuOpen = false
                                    }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("未分类") },
                                        onClick = {
                                            editFolderMenuOpen = false
                                            editFolder = ""
                                        },
                                        leadingIcon = {
                                            if (editFolder.isBlank()) {
                                                Icon(
                                                    Icons.Filled.Check,
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                    )
                                    folders.forEach { name ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                editFolderMenuOpen = false
                                                editFolder = name
                                            },
                                            leadingIcon = {
                                                if (editFolder == name) {
                                                    Icon(
                                                        Icons.Filled.Check,
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("新建文件夹…") },
                                        onClick = {
                                            editFolderMenuOpen = false
                                            editFolderIsNew = true
                                            editNewFolder = ""
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { confirmEditBookmark() },
                    enabled = editUrl.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { editing = null }) { Text("取消") }
            }
        )
    }
    // Search word, folder filter and the batch selection are
    // saveable since v0.85.0 (second configuration-change audit):
    // they are work in progress, and an activity recreation
    // (uncovered configuration change / process death with state
    // restore) must not silently drop a half-finished selection.
    var bookmarkQuery by rememberSaveable { mutableStateOf("") }
    // Folder filter (v0.72.0): page-local state, never persisted —
    // same precedent as the downloads-page search (v0.64.0). null =
    // 全部, "" = 未分类, anything else = that folder. The pipeline
    // is settled in ONE place, in this order: folder filter first,
    // then the search filter, then the display sort below reads
    // only the final list. Every action (open/edit/pin/delete,
    // batch selection) is keyed by the entry itself, so filtering
    // can never point an action at an entry the user is not
    // looking at.
    var folderFilter by rememberSaveable { mutableStateOf<String?>(null) }
    // Folder filter then search filter, the settled pipeline
    // above, as a derived state (v0.118.0): the v0.96.0 fluency
    // pass memoised this with remember(bookmarks, folderFilter,
    // bookmarkQuery), but bookmarks is the ONE snapshot-state
    // list instance the app keeps for its whole lifetime — its
    // identity never changes, so whenever a folder filter or a
    // search word was active the cached copy never re-ran while
    // this screen stayed composed: deleting, editing, pinning or
    // batch-deleting a bookmark from THIS page left the old row
    // on screen until the page was left and re-entered (the same
    // stale-cache class the v0.117.0 home audit fixed for the
    // home grid). A derived state keeps the fluency property
    // (the filter re-runs only when the bookmark list, the folder
    // filter or the search word actually changes) and stays
    // live. Same pipeline, same result.
    val visibleBookmarks by remember {
        derivedStateOf {
            val folderFiltered = bookmarksInFolder(bookmarks, folderFilter)
            if (bookmarkQuery.isBlank()) {
                folderFiltered
            } else {
                folderFiltered.filter {
                    it.title.contains(bookmarkQuery, ignoreCase = true) ||
                        it.url.contains(bookmarkQuery, ignoreCase = true)
                }
            }
        }
    }
    // A filter naming a folder that no longer exists (renamed or
    // deleted elsewhere) falls back to 全部 instead of stranding
    // the user on an empty page. The rename flow below re-points
    // the filter itself, so this is only a safety net.
    LaunchedEffect(folderFilter, folders) {
        val current = folderFilter
        if (current != null && current.isNotEmpty() &&
            current !in folders
        ) {
            folderFilter = null
        }
    }
    // Folder management dialogs (v0.72.0): create / rename / delete.
    var showCreateFolder by remember { mutableStateOf(false) }
    var createFolderName by remember { mutableStateOf("") }
    var renameFolderTarget by remember { mutableStateOf<String?>(null) }
    var renameFolderText by remember { mutableStateOf("") }
    var deleteFolderTarget by remember { mutableStateOf<String?>(null) }
    if (showCreateFolder) {
        // Opens focused with the keyboard up, like every other
        // input dialog since the v0.36.0 audit — this dialog was
        // added later (v0.72.0) and missed that pass (v0.83.0).
        val createFolderFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { createFolderFocusRequester.requestFocus() }
        fun confirmCreateFolder() {
            val name = createFolderName.trim()
            if (name.isBlank()) return
            if (name in folders) {
                android.widget.Toast.makeText(
                    context, "已经有这个文件夹了",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                showCreateFolder = false
                onCreateFolder(name)
                // Land on the new folder so the user
                // sees where their bookmarks will go.
                folderFilter = name
            }
        }
        AlertDialog(
            onDismissRequest = { showCreateFolder = false },
            title = { Text("新建文件夹") },
            text = {
                OutlinedTextField(
                    value = createFolderName,
                    onValueChange = { createFolderName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(createFolderFocusRequester),
                    label = { Text("文件夹名称") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { confirmCreateFolder() }
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { confirmCreateFolder() },
                    enabled = createFolderName.isNotBlank()
                ) { Text("新建") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFolder = false }) {
                    Text("取消")
                }
            }
        )
    }
    val renameTarget = renameFolderTarget
    if (renameTarget != null) {
        // Same v0.83.0 fix as the create dialog above: autofocus
        // on open, Enter confirms.
        val renameFolderFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { renameFolderFocusRequester.requestFocus() }
        fun confirmRenameFolder() {
            val name = renameFolderText.trim()
            if (name.isBlank()) return
            if (name != renameTarget && name in folders) {
                android.widget.Toast.makeText(
                    context, "已经有这个文件夹了",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                renameFolderTarget = null
                if (name != renameTarget) {
                    onRenameFolder(renameTarget, name)
                    folderFilter = name
                }
            }
        }
        AlertDialog(
            onDismissRequest = { renameFolderTarget = null },
            title = { Text("重命名文件夹") },
            text = {
                OutlinedTextField(
                    value = renameFolderText,
                    onValueChange = { renameFolderText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(renameFolderFocusRequester),
                    label = { Text("文件夹名称") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { confirmRenameFolder() }
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = { confirmRenameFolder() },
                    enabled = renameFolderText.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { renameFolderTarget = null }) {
                    Text("取消")
                }
            }
        )
    }
    val deleteTarget = deleteFolderTarget
    if (deleteTarget != null) {
        val inFolder = bookmarks.count { it.folder == deleteTarget }
        AlertDialog(
            onDismissRequest = { deleteFolderTarget = null },
            title = { Text("删除文件夹「$deleteTarget」？") },
            text = {
                Text(
                    if (inFolder > 0) {
                        "只会删掉这个文件夹，里面的 $inFolder 条书签" +
                            "会回到「未分类」，不会被删掉。"
                    } else {
                        "这个文件夹是空的，删掉它不会影响任何书签。"
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    deleteFolderTarget = null
                    onDeleteFolder(deleteTarget)
                    folderFilter = null
                }) { Text("删除文件夹") }
            },
            dismissButton = {
                TextButton(onClick = { deleteFolderTarget = null }) {
                    Text("取消")
                }
            }
        )
    }
    val displayBookmarks = visibleBookmarks
    // Batch-manage mode: checkboxes on every row, a delete-selected
    // button on top, and clear ways out (back key, the close icon
    // and the "完成" button all leave the mode).
    var batchMode by rememberSaveable { mutableStateOf(false) }
    var selectedUrls by rememberSaveable(
        saver = listSaver<MutableState<Set<String>>, String>(
            save = { it.value.toList() },
            restore = { mutableStateOf(it.toSet()) }
        )
    ) { mutableStateOf<Set<String>>(emptySet()) }
    // Switching the folder filter clears the batch selection
    // (v0.72.0): batch delete must only ever act on entries the
    // user can currently see selected.
    // Changing the folder filter clears the batch selection (the
    // selected rows may not even be visible under the new filter).
    // The comparison against the last seen value matters since
    // v0.85.0: filter and selection are both saveable now, and the
    // effect's first run after a state restore must NOT wipe the
    // just-restored selection — the remembered "last seen" starts
    // at the restored filter, so only a genuine later change
    // clears.
    var lastFolderFilter by remember { mutableStateOf(folderFilter) }
    LaunchedEffect(folderFilter) {
        if (folderFilter != lastFolderFilter) {
            lastFolderFilter = folderFilter
            selectedUrls = emptySet()
        }
    }
    // Selection hygiene under SEARCH (v0.143.0 audit): the folder
    // filter clears the selection outright (above), but the search
    // word did not touch it — select rows, then type a word that
    // hides some of them, and the batch bar still counted the
    // hidden rows and 删除所选 deleted them from the full list,
    // breaking the v0.72.0 rule that batch delete only ever acts
    // on entries the user can see selected. Same prune the sniff
    // screen got in v0.93.0: while in batch mode, the selection
    // is intersected with the visible list whenever that list
    // changes. (Outside batch mode the selection is inert —
    // entering batch mode resets it anyway.)
    LaunchedEffect(visibleBookmarks, batchMode) {
        if (batchMode && selectedUrls.isNotEmpty()) {
            val live = visibleBookmarks.map { it.url }.toSet()
            val pruned = selectedUrls intersect live
            if (pruned != selectedUrls) {
                selectedUrls = pruned
            }
        }
    }
    var showBatchDeleteConfirm by remember { mutableStateOf(false) }
    // Long-press menu target (v0.54.0, same shape as the history
    // page's v0.53.0 menu) and the pending single-delete
    // confirmation. The menu only exists outside batch mode: in
    // batch mode a press — short or long — just toggles selection,
    // so the two gestures never fight.
    var menuBookmark by remember { mutableStateOf<Bookmark?>(null) }
    var pendingDeleteBookmark by remember {
        mutableStateOf<Bookmark?>(null)
    }
    fun exitBatchMode() {
        batchMode = false
        selectedUrls = emptySet()
    }
    BackHandler(enabled = batchMode) { exitBatchMode() }
    // Outside batch mode the key follows the settled page-level
    // rule (v0.84.0, completed for this page in v0.105.0): first
    // clear the search word, then the folder filter, and only
    // then leave the page — the same layering the history and
    // downloads pages already had; this page used to leave
    // outright even with a search word or folder filter active.
    // Leaving the page itself is the v0.37.0 handler: before it,
    // no handler was enabled here and the system finished the
    // activity instead of returning to the browser. All four
    // handlers are mutually exclusive by enabled.
    BackHandler(enabled = !batchMode && bookmarkQuery.isNotBlank()) {
        bookmarkQuery = ""
    }
    BackHandler(
        enabled = !batchMode && bookmarkQuery.isBlank() &&
            folderFilter != null
    ) {
        folderFilter = null
    }
    BackHandler(
        enabled = !batchMode && bookmarkQuery.isBlank() &&
            folderFilter == null
    ) {
        onBack()
    }
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
    // Single-delete confirmation for the long-press menu (v0.54.0):
    // same wording and shape as the home grid's delete confirmation
    // — the bookmarks page's trailing delete button keeps its
    // existing one-tap behaviour, the menu path asks first.
    val pendingBmDelete = pendingDeleteBookmark
    if (pendingBmDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteBookmark = null },
            title = { Text("删除这个书签？") },
            text = {
                // Scrollable body (v0.169.0, same treatment as the
                // home-page twin of this dialog): the embedded
                // bookmark title is unbounded.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "会把「${pendingBmDelete.title}」从书签里删掉，" +
                            "首页的快捷入口也会一起消失。"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    pendingDeleteBookmark = null
                    onDelete(pendingBmDelete)
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteBookmark = null }) {
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
                        // Live-filter field (v0.138.0): Search
                        // action face, no auto-correct — the text
                        // is matched against names and URLs.
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search
                        ),
                        placeholder = { Text("搜索书签（名称或网址）") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
                item {
                    // Folder filter chips (v0.72.0): official
                    // FilterChip in a horizontally scrollable row —
                    // 全部 / 未分类 / one chip per folder, plus the
                    // create-folder entry at the end.
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = folderFilter == null,
                                onClick = { folderFilter = null },
                                label = { Text("全部") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = folderFilter == "",
                                onClick = { folderFilter = "" },
                                label = { Text("未分类") }
                            )
                        }
                        items(folders, key = { it }) { name ->
                            FilterChip(
                                selected = folderFilter == name,
                                onClick = { folderFilter = name },
                                label = { Text(name) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Folder,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                        item {
                            AssistChip(
                                onClick = {
                                    createFolderName = ""
                                    showCreateFolder = true
                                },
                                label = { Text("新建文件夹") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
                val selectedFolder = folderFilter
                if (selectedFolder != null && selectedFolder.isNotEmpty()) {
                    item {
                        // Folder management (v0.72.0): rename and
                        // delete live next to the selected folder's
                        // chip. Delete only removes the folder —
                        // its bookmarks fall back to 未分类 (the
                        // confirmation dialog says so in words).
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            TextButton(onClick = {
                                renameFolderText = selectedFolder
                                renameFolderTarget = selectedFolder
                            }) {
                                Text("重命名文件夹")
                            }
                            TextButton(onClick = {
                                deleteFolderTarget = selectedFolder
                            }) {
                                Text("删除文件夹")
                            }
                        }
                    }
                }
                if (visibleBookmarks.isEmpty()) {
                    item {
                        if (bookmarkQuery.isNotBlank()) {
                            EmptyState(
                                Icons.Filled.Search,
                                "没有找到相关的书签。"
                            )
                        } else {
                            // Folder filter with no hits / an empty
                            // folder (v0.72.0): the shared empty
                            // state, never a blank board.
                            EmptyState(
                                Icons.Filled.Folder,
                                if (folderFilter == "") {
                                    "还没有未分类的书签。"
                                } else {
                                    "这个文件夹里还没有书签，" +
                                        "编辑书签时可以把它放进来。"
                                }
                            )
                        }
                    }
                }
            }
            if (batchMode && displayBookmarks.isNotEmpty()) {
                item {
                    Button(
                        onClick = { showBatchDeleteConfirm = true },
                        enabled = selectedUrls.isNotEmpty(),
                        // Batch bar animation (v0.111.0 motion
                        // audit): v0.82.0 aligned the sniff and
                        // tab multi-select bars to the shared spec
                        // but this third multi-select bar still
                        // popped in instantly. Same LazyItem
                        // animateItem treatment as the tab sheet's
                        // action row: fadeIn 220ms / fadeOut 180ms,
                        // FastOutSlowIn.
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .animateItem(
                                fadeInSpec = tween(
                                    220, easing = FastOutSlowInEasing
                                ),
                                fadeOutSpec = tween(
                                    180, easing = FastOutSlowInEasing
                                )
                            )
                    ) {
                        Text("删除所选（${selectedUrls.size}）")
                    }
                }
            }
            items(displayBookmarks, key = { it.url }) { bookmark ->
                val selected = bookmark.url in selectedUrls
                ListItem(
                    headlineContent = {
                        // Capped like every other list row
                        // (v0.143.0 audit): an uncapped title or
                        // detail line used to wrap without limit
                        // and stretch the row.
                        Text(
                            bookmark.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        Text(
                            listOfNotNull(
                                if (bookmark.pinned) {
                                    "已置顶（在首页最前面）"
                                } else {
                                    null
                                },
                                bookmark.folder.takeIf { it.isNotBlank() }
                                    ?.let { "文件夹：$it" },
                                bookmark.url
                            ).joinToString(" · "),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                },
                                // Name the toggle for TalkBack
                                // (v0.136.0 accessibility pass, same
                                // fix as the tab-card and sniff-row
                                // checkboxes): a bare Checkbox
                                // announces only checked / not
                                // checked, never WHICH bookmark it
                                // selects.
                                modifier = Modifier.semantics {
                                    contentDescription =
                                        "选择 " + bookmark.title.ifBlank {
                                            bookmark.url
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
                        // Selection state on the row itself in
                        // batch mode (v0.136.0 accessibility pass,
                        // same fix as the tab cards in v0.99.0 and
                        // the sniff rows): the row tap toggles the
                        // selection, so the row stop says 已选 /
                        // 未选 instead of leaving the state to the
                        // Checkbox stop alone.
                        .then(
                            if (batchMode) {
                                Modifier.semantics {
                                    stateDescription =
                                        if (selected) "已选" else "未选"
                                }
                            } else {
                                Modifier
                            }
                        )
                        .combinedClickable(
                            onClick = {
                                if (batchMode) {
                                    selectedUrls = if (selected) {
                                        selectedUrls - bookmark.url
                                    } else {
                                        selectedUrls + bookmark.url
                                    }
                                } else {
                                    onOpen(bookmark)
                                }
                            },
                            onLongClick = {
                                // No menu in batch mode: a long
                                // press there means the same toggle
                                // as a tap, nothing more.
                                if (!batchMode) {
                                    menuBookmark = bookmark
                                }
                            }
                        )
                )
                HorizontalDivider()
            }
        }
    }
    // Long-press menu (v0.54.0): same AlertDialog + LazyColumn +
    // ListItem shape as the history page's menu (v0.53.0). Edit
    // goes through the very same editor dialog as the trailing
    // edit button; pin toggling and delete reuse the existing
    // callbacks and storage.
    val bmMenuTarget = menuBookmark
    if (bmMenuTarget != null) {
        AlertDialog(
            onDismissRequest = { menuBookmark = null },
            title = {
                Text(
                    bmMenuTarget.title.ifBlank { bmMenuTarget.url },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                LazyColumn {
                    item {
                        Text(
                            bmMenuTarget.url,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("打开") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuBookmark = null
                                    onOpen(bmMenuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("在新标签打开") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuBookmark = null
                                    onOpenInNewTab(bmMenuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("复制网址") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuBookmark = null
                                    onCopyUrl(bmMenuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("编辑") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuBookmark = null
                                    editing = bmMenuTarget
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = {
                                Text(
                                    if (bmMenuTarget.pinned) {
                                        "取消置顶"
                                    } else {
                                        "置顶到最前面"
                                    }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuBookmark = null
                                    onTogglePin(bmMenuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("删除") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuBookmark = null
                                    pendingDeleteBookmark = bmMenuTarget
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { menuBookmark = null }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * Range picker + confirmation for clearing history (v0.79.0):
 * one official AlertDialog shared by the history page and the
 * settings page, so both entry points offer exactly the same
 * three ranges with the same per-range counts. Picking a range
 * and pressing 清除 is the whole flow — the range wording and
 * the count ARE the confirmation.
 */
@Composable
private fun HistoryRangeClearDialog(
    counts: Map<HistoryClearRange, Int>,
    onDismiss: () -> Unit,
    onConfirm: (HistoryClearRange) -> Unit
) {
    // The preselected range is the LEAST destructive one
    // (v0.142.0 audit): the dialog used to open with 全部时间
    // selected, so opening it and tapping 清除 out of habit wiped
    // the whole history. Widening the range is one tap; the counts
    // for all three ranges are printed on their rows either way.
    var selected by remember { mutableStateOf(HistoryClearRange.LAST_HOUR) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("清除历史记录") },
        text = {
            // Scrollable body (v0.115.0 extreme-viewport pass):
            // three two-line radio rows plus the intro line total
            // roughly 270dp at font scale 1.3, which with the title
            // and button row overflows a 360dp-tall landscape
            // window — the stock AlertDialog does not scroll its
            // text, so the last range was clipped. A plain
            // verticalScroll is a no-op whenever the content fits,
            // so normal viewports render exactly as before.
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("选择要清除的时间范围，范围之外的记录会保留。")
                HistoryClearRange.entries.forEach { range ->
                    ListItem(
                        headlineContent = { Text(range.label) },
                        supportingContent = {
                            Text("将清除 ${counts[range] ?: 0} 条")
                        },
                        leadingContent = {
                            RadioButton(
                                selected = selected == range,
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selected = range }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) { Text("清除") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun HistoryScreen(
    history: List<HistoryItem>,
    onBack: () -> Unit,
    onOpen: (HistoryItem) -> Unit,
    onOpenInNewTab: (HistoryItem) -> Unit,
    onCopyUrl: (HistoryItem) -> Unit,
    onDelete: (HistoryItem) -> Unit,
    onSwipeRemove: (HistoryItem) -> Unit,
    onSwipeUndo: (HistoryItem) -> Unit,
    onSwipeCommit: () -> Unit,
    onDeleteSite: (String) -> Unit,
    onClearRange: (HistoryClearRange) -> Unit,
    clearCounts: Map<HistoryClearRange, Int>
) {
    var showClearDialog by remember { mutableStateOf(false) }
    // Search word and site filter are saveable since v0.85.0
    // (second configuration-change audit), same rule as the
    // downloads page: recreation must not silently reset them.
    var query by rememberSaveable { mutableStateOf("") }
    // Long-press menu target (v0.53.0) and the pending "delete this
    // site's history" confirmation (host + live count). Both live
    // here in the screen: the list, the search query and the grouping
    // all read the shared history list, so deleting through the
    // callbacks refreshes in place without touching the query.
    var menuItem by remember { mutableStateOf<HistoryItem?>(null) }
    var siteDeleteTarget by remember {
        mutableStateOf<Pair<String, Int>?>(null)
    }
    // Search filter as a derived state (v0.118.0): the v0.96.0
    // fluency pass memoised this with remember(history, query),
    // but history is the ONE snapshot-state list instance the app
    // keeps for its whole lifetime — its identity never changes,
    // so with a search word typed the cached copy never re-ran
    // while this screen stayed composed, and a deletion made
    // from THIS page (row, menu, per-site, swipe commit) left
    // the old row on screen until the page was left and
    // re-entered (the same stale-cache class the v0.117.0 home
    // audit fixed for the home grid). The blank-query branch
    // returns a copy rather than the live list itself, so the
    // derived value carries content semantics: the grouping
    // below keys its remember on this list, and a same-identity
    // live list would freeze that cache the same way. A derived
    // state keeps the fluency property (the filter re-runs only
    // when the history list or the search word actually
    // changes) and stays live. Same pipeline, same result.
    val filtered by remember {
        derivedStateOf {
            if (query.isBlank()) {
                history.toList()
            } else {
                history.filter {
                    it.title.contains(query, ignoreCase = true) ||
                        it.url.contains(query, ignoreCase = true)
                }
            }
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
    // when the filtered list or the calendar day changes. Since
    // v0.118.0 the filtered list is a derived-state value with
    // content semantics (a fresh copy whenever its content
    // changes), so this key comparison sees real content changes
    // instead of one unchanging list identity.
    val groups = remember(filtered, today) {
        listOf("今天", "昨天", "更早")
            .map { bucket -> bucket to filtered.filter { bucketOf(it) == bucket } }
            .filter { it.second.isNotEmpty() }
    }
    // Swipe-to-delete with undo (v0.71.0): swiping a row from the
    // end towards the start removes the entry from the in-memory
    // list at once but does NOT save yet — the snackbar's undo
    // window is the grace period. Undo puts the entry back by its
    // own timestamp (historyRestoreIndex) and saves; the snackbar
    // timing out or being dismissed commits the removal to disk.
    // A second swipe while one deletion is still pending commits
    // the first one first, so at most one entry is ever in limbo;
    // the other history actions on this page (row delete button,
    // long-press delete, per-site delete, clear-all) commit a
    // pending swipe deletion before they save, for the same
    // reason. Leaving the page commits too (DisposableEffect).
    val snackbarHostState = remember { SnackbarHostState() }
    val historyScope = rememberCoroutineScope()
    var pendingSwipeDelete by remember {
        mutableStateOf<HistoryItem?>(null)
    }
    val pendingSwipeDeleteState = rememberUpdatedState(pendingSwipeDelete)
    fun commitPendingSwipeDelete() {
        if (pendingSwipeDelete != null) {
            pendingSwipeDelete = null
            onSwipeCommit()
        }
    }
    fun startSwipeDelete(item: HistoryItem) {
        commitPendingSwipeDelete()
        pendingSwipeDelete = item
        onSwipeRemove(item)
        historyScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "已删除一条历史记录",
                actionLabel = "撤销",
                duration = SnackbarDuration.Short
            )
            // A newer swipe may have taken over the pending slot
            // (and already committed this one) while the snackbar
            // was up; only the swipe that still owns the slot
            // settles itself here.
            if (pendingSwipeDelete == item) {
                pendingSwipeDelete = null
                if (result == SnackbarResult.ActionPerformed) {
                    onSwipeUndo(item)
                } else {
                    onSwipeCommit()
                }
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (pendingSwipeDeleteState.value != null) {
                onSwipeCommit()
            }
        }
    }
    // System back (v0.37.0): with a search word active the key
    // clears the search first; only then does it leave the page.
    BackHandler(enabled = query.isBlank()) { onBack() }
    BackHandler(enabled = query.isNotBlank()) { query = "" }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        // Live-filter field (v0.138.0): Search
                        // action face, no auto-correct — the text
                        // is matched against titles and URLs.
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search
                        ),
                        placeholder = { Text("搜索历史（标题或网址）") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
                item {
                    OutlinedButton(
                        onClick = {
                            // A pending swipe deletion is
                            // committed first (v0.71.0) so its undo
                            // cannot resurrect an entry into the
                            // just-cleared list. The range dialog
                            // (v0.79.0) then decides how much goes.
                            commitPendingSwipeDelete()
                            showClearDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("清除历史记录")
                    }
                }
            }
            if (history.isEmpty()) {
                item {
                    EmptyState(Icons.Filled.History, "还没有历史记录。")
                }
            } else if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Filled.Search,
                        "没有找到相关的历史记录。"
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
                    // Swipe-to-delete (v0.71.0): official M3
                    // SwipeToDismissBox, end-to-start only — the
                    // other direction is disabled, so a row can
                    // only ever be swiped away, never swiped into
                    // some other action. The item handed over is
                    // the very entry this row renders (identity by
                    // url + time, the same key the list uses), so
                    // search and site filters cannot shift the
                    // deletion onto a different entry.
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                startSwipeDelete(item)
                                true
                            } else {
                                false
                            }
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme
                                        .onErrorContainer
                                )
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = {
                                // Capped like every other list
                                // row in the app (v0.142.0 audit):
                                // an uncapped title used to wrap
                                // without limit and stretch the row.
                                Text(
                                    item.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text(
                                    item.url,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingContent = {
                                Icon(Icons.Filled.History, contentDescription = null)
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    commitPendingSwipeDelete()
                                    onDelete(item)
                                }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "删除这条历史"
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor =
                                    MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { onOpen(item) },
                                    onLongClick = { menuItem = item }
                                )
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
    // Long-press menu (v0.53.0): same AlertDialog + ListItem shape
    // as the web-page context menus (v0.42.0/v0.43.0).
    val menuTarget = menuItem
    if (menuTarget != null) {
        val siteHost = hostOf(menuTarget.url)
        AlertDialog(
            onDismissRequest = { menuItem = null },
            title = {
                Text(
                    menuTarget.title.ifBlank { menuTarget.url },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                LazyColumn {
                    item {
                        Text(
                            menuTarget.url,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("打开") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuItem = null
                                    onOpen(menuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("在新标签打开") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuItem = null
                                    onOpenInNewTab(menuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("复制网址") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuItem = null
                                    onCopyUrl(menuTarget)
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("删除这条") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    menuItem = null
                                    commitPendingSwipeDelete()
                                    onDelete(menuTarget)
                                }
                        )
                    }
                    if (siteHost != null) {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text("删除本站全部记录")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        // Count against the full
                                        // history list right now, so
                                        // the confirmation states the
                                        // real number even while a
                                        // search filter is active.
                                        val count = history.count {
                                            hostMatchesSite(
                                                hostOf(it.url), siteHost
                                            )
                                        }
                                        menuItem = null
                                        siteDeleteTarget =
                                            siteHost to count
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { menuItem = null }) {
                    Text("取消")
                }
            }
        )
    }
    // "Delete this site's history" confirmation (v0.53.0): states
    // the domain and the real entry count, and says plainly that
    // only history records go — cookies and site data are the
    // clear-data page's job and are not touched here.
    val siteTarget = siteDeleteTarget
    if (siteTarget != null) {
        val (host, count) = siteTarget
        AlertDialog(
            onDismissRequest = { siteDeleteTarget = null },
            title = { Text("删除本站全部记录？") },
            text = {
                Text(
                    "将删除 $host 的全部 $count 条历史记录" +
                        "（含它的子域名）。" +
                        "只删历史记录，不会清除这个网站的 Cookie 和网站数据。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    siteDeleteTarget = null
                    commitPendingSwipeDelete()
                    onDeleteSite(host)
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { siteDeleteTarget = null }) {
                    Text("取消")
                }
            }
        )
    }
    if (showClearDialog) {
        HistoryRangeClearDialog(
            counts = clearCounts,
            onDismiss = { showClearDialog = false },
            onConfirm = { range ->
                showClearDialog = false
                onClearRange(range)
            }
        )
    }
}

/**
 * Page resource sniffer (WebView tabs only; Gecko tabs never reach
 * this screen — the menu entry explains why). Runs the collection
 * script once when the screen appears and again from the top-bar
 * refresh action (v0.140.0), then lists what was found
 * grouped by kind, each row downloadable through the existing
 * system-download path. Multi-select (v0.80.0) lets the user pick
 * a subset and start them all through the same batch dispatch the
 * "download all" button uses; the entry only exists when the
 * result list is non-empty (it lives in the non-empty branch).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SniffScreen(
    tab: BrowserTab?,
    onBack: () -> Unit,
    onDownload: (SniffedResource) -> Unit,
    onDownloadAll: (List<SniffedResource>) -> Unit,
    onDownloadSelected: (List<SniffedResource>) -> Unit = {}
) {
    val context = LocalContext.current
    var loading by remember(tab?.id) { mutableStateOf(true) }
    var resources by remember(tab?.id) {
        mutableStateOf<List<SniffedResource>>(emptyList())
    }
    // Multi-select mode (v0.80.0): same shape as the bookmarks
    // batch mode and the tab multi-select — an explicit mode, an
    // official Checkbox per row, a live "已选 N 个" count. Every
    // sniffed kind (file / image / media) is selectable: all of
    // them are downloadable through the single-item path already.
    // Saveable since v0.85.0 (second configuration-change audit),
    // still keyed by tab: a half-finished sniff selection survives
    // an activity recreation, and the resources are re-collected
    // for the same tab on the way back in, so the restored URLs
    // still name rows of the same list.
    var selectMode by rememberSaveable(tab?.id) { mutableStateOf(false) }
    var selectedUrls by rememberSaveable(
        tab?.id,
        saver = listSaver<MutableState<Set<String>>, String>(
            save = { it.value.toList() },
            restore = { mutableStateOf(it.toSet()) }
        )
    ) { mutableStateOf<Set<String>>(emptySet()) }
    fun toggleSniffSelected(url: String) {
        selectedUrls = if (url in selectedUrls) {
            selectedUrls - url
        } else {
            selectedUrls + url
        }
    }
    fun exitSniffSelectMode() {
        selectMode = false
        selectedUrls = emptySet()
    }
    // Collection runs (v0.140.0): the screen collects once when it
    // appears and again whenever the user taps the refresh action —
    // a page that was still loading (or lazy-loads its resources)
    // otherwise leaves the user with a partial list and no way to
    // ask again short of leaving and re-entering. Each run takes a
    // generation number and only the newest run may land its
    // result, so a slow answer from an earlier run can never
    // overwrite a fresher one.
    var collectSeq by remember(tab?.id) { mutableStateOf(0) }
    fun runCollect() {
        val target = tab
        if (target == null) {
            loading = false
            return
        }
        val seq = ++collectSeq
        loading = true
        target.collectPageResources { found ->
            if (seq == collectSeq) {
                resources = found
                loading = false
            }
        }
    }
    LaunchedEffect(tab?.id) {
        if (tab == null) {
            loading = false
        } else {
            runCollect()
        }
    }
    // Selection hygiene (v0.93.0 audit): a selection restored from
    // saved state (or left over from a previous visit to this tab)
    // can name URLs the freshly collected list no longer contains —
    // the page navigated, or its dynamic content changed. Prune to
    // the live list as soon as a collection lands, so the header
    // count, the select-all toggle and the download button all count
    // exactly the rows the batch download would take. Gated on the
    // load having finished: pruning against the initial empty list
    // would wipe a legitimately restored selection before the
    // collection for the same page comes back.
    LaunchedEffect(loading, resources) {
        if (!loading && selectedUrls.isNotEmpty()) {
            val live = resources.map { it.url }.toSet()
            val pruned = selectedUrls intersect live
            if (pruned != selectedUrls) {
                selectedUrls = pruned
            }
        }
    }
    // System back (v0.37.0, layered in v0.84.0): in multi-select
    // mode the key first leaves the mode — the same layering as
    // the bookmarks batch mode and the tab multi-select, which
    // this screen was missing: its single unconditional handler
    // used to leave the whole page mid-selection. Outside the
    // mode the key returns to the browser. The two handlers are
    // mutually exclusive by enabled.
    BackHandler(enabled = selectMode) { exitSniffSelectMode() }
    BackHandler(enabled = !selectMode) { onBack() }
    // Kind groups, memoised on the collected list (v0.96.0 fluency
    // pass): the three-way filter used to re-run inside the list's
    // composition on every recomposition of this screen (selection
    // toggles included). Same groups, same order.
    val sniffGroups = remember(resources) {
        listOf(
            SniffKind.FILE to "文件",
            SniffKind.IMAGE to "图片",
            SniffKind.MEDIA to "音视频"
        ).map { (kind, label) ->
            Triple(kind, label, resources.filter { it.kind == kind })
        }
    }
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
                },
                actions = {
                    // Re-collect (v0.140.0): the page may have
                    // loaded more resources since the screen opened
                    // (or was still loading when it did). Disabled
                    // while a collection is in flight.
                    IconButton(
                        onClick = { runCollect() },
                        enabled = !loading && tab != null
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "重新收集"
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
                    // Multi-select bar animation (v0.82.0 visual
                    // pass): both header bars now arrive and leave
                    // with the shared 220/180ms fade + expand/shrink
                    // instead of swapping instantly.
                    AnimatedVisibility(
                        visible = selectMode,
                        enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                            expandVertically(tween(220, easing = FastOutSlowInEasing)),
                        exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                            shrinkVertically(tween(180, easing = FastOutSlowInEasing))
                    ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "已选 ${selectedUrls.size} 个",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        liveRegion = LiveRegionMode.Polite
                                    }
                            )
                            TextButton(
                                onClick = {
                                    selectedUrls = if (
                                        selectedUrls.size == resources.size
                                    ) {
                                        emptySet()
                                    } else {
                                        resources.map { it.url }.toSet()
                                    }
                                }
                            ) {
                                Text(
                                    if (selectedUrls.size == resources.size) {
                                        "取消全选"
                                    } else {
                                        "全选"
                                    }
                                )
                            }
                            TextButton(onClick = { exitSniffSelectMode() }) {
                                Text("退出多选")
                            }
                        }
                        Button(
                            enabled = selectedUrls.isNotEmpty(),
                            onClick = {
                                val chosen = resources.filter {
                                    it.url in selectedUrls
                                }
                                if (chosen.isNotEmpty()) {
                                    onDownloadSelected(chosen)
                                    android.widget.Toast.makeText(
                                        context,
                                        "已开始下载 ${chosen.size} 个",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    exitSniffSelectMode()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null)
                            Text(
                                "下载所选（${selectedUrls.size} 个）",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    }
                    AnimatedVisibility(
                        visible = !selectMode,
                        enter = fadeIn(tween(220, easing = FastOutSlowInEasing)) +
                            expandVertically(tween(220, easing = FastOutSlowInEasing)),
                        exit = fadeOut(tween(180, easing = FastOutSlowInEasing)) +
                            shrinkVertically(tween(180, easing = FastOutSlowInEasing))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onDownloadAll(resources) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.Download,
                                    contentDescription = null
                                )
                                Text(
                                    "全部下载（${resources.size} 个）",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = { selectMode = true }
                            ) {
                                Text("多选下载")
                            }
                        }
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        sniffGroups.forEach { (kind, label, inGroup) ->
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
                                            Text(
                                                res.name,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                res.host.ifBlank { res.url },
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingContent = {
                                            if (selectMode) {
                                                Checkbox(
                                                    checked = res.url in selectedUrls,
                                                    onCheckedChange = {
                                                        toggleSniffSelected(res.url)
                                                    },
                                                    modifier = Modifier.semantics {
                                                        contentDescription =
                                                            "选择 ${res.name}"
                                                    }
                                                )
                                            } else {
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
                                            }
                                        },
                                        trailingContent = {
                                            if (!selectMode) {
                                                IconButton(
                                                    onClick = { onDownload(res) }
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Download,
                                                        contentDescription = "下载"
                                                    )
                                                }
                                            }
                                        },
                                        modifier = if (selectMode) {
                                            // Row stop carries the
                                            // selection state too
                                            // (v0.99.0 audit, same fix
                                            // as the tab cards): the
                                            // row tap toggles, so its
                                            // semantics say 已选 / 未选
                                            // instead of leaving the
                                            // state to the Checkbox
                                            // stop alone.
                                            Modifier
                                                .semantics {
                                                    stateDescription =
                                                        if (res.url in
                                                            selectedUrls
                                                        ) {
                                                            "已选"
                                                        } else {
                                                            "未选"
                                                        }
                                                }
                                                .clickable {
                                                    toggleSniffSelected(
                                                        res.url
                                                    )
                                                }
                                        } else {
                                            Modifier
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

/**
 * Run one DownloadManager [query] and map its rows to
 * [DownloadEntry]s, newest first. Shared by the downloads screen's
 * full load (unfiltered query) and its once-a-second in-progress
 * poll (query filtered by the active rows' ids) so both paths map
 * rows exactly the same way (v0.70.0).
 */
private fun readDownloadEntries(
    context: android.content.Context,
    query: android.app.DownloadManager.Query
): List<DownloadEntry> {
    val manager = context.getSystemService(
        android.content.Context.DOWNLOAD_SERVICE
    ) as android.app.DownloadManager
    val loaded = mutableListOf<DownloadEntry>()
    try {
        val cursor = manager.query(query)
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
    return loaded.reversed()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DownloadsScreen(
    onBack: () -> Unit,
    retryReferer: String? = null,
    retryUserAgent: String? = null
) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<DownloadEntry>>(emptyList()) }
    var refreshTick by remember { mutableStateOf(0) }
    // The DownloadManager query is a provider call: it runs on the
    // IO dispatcher, never on the composition thread (v0.141.0
    // audit — the same applies to the once-a-second poll below; a
    // slow provider answer used to stall the list's scrolling).
    LaunchedEffect(refreshTick) {
        entries = kotlinx.coroutines.withContext(
            kotlinx.coroutines.Dispatchers.IO
        ) {
            readDownloadEntries(
                context, android.app.DownloadManager.Query()
            )
        }
    }

    // Live refresh (v0.70.0): while this screen is visible, once a
    // second re-query ONLY the rows that are still in progress,
    // filtered by their ids — finished and failed history rows are
    // never re-read, so the polling cost stays proportional to the
    // number of active downloads, not the size of the history. The
    // id filter ignores status, so a row that just finished still
    // comes back carrying its final state. When nothing is in
    // progress no query runs at all. Leaving the screen cancels
    // this effect (it is composition-scoped), so there is no
    // background polling. Gecko in-progress rows need no polling:
    // GeckoDownloadTracker.active is a snapshot state list the copy
    // loop publishes progress into (v0.69.0) — already reactive.
    val entriesSnapshot = rememberUpdatedState(entries)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            val current = entriesSnapshot.value
            val activeIds = current.filter {
                it.status == android.app.DownloadManager.STATUS_PENDING ||
                    it.status == android.app.DownloadManager.STATUS_RUNNING ||
                    it.status == android.app.DownloadManager.STATUS_PAUSED
            }.map { it.id }
            if (activeIds.isEmpty()) continue
            val fresh = kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.IO
            ) {
                readDownloadEntries(
                    context,
                    android.app.DownloadManager.Query()
                        .setFilterById(*activeIds.toLongArray())
                )
            }
            if (fresh.isEmpty()) continue
            val freshById = fresh.associateBy { it.id }
            val merged = current.map { freshById[it.id] ?: it }
            if (merged != current) entries = merged
        }
    }

    // Status filter (presentation only — the DownloadManager query
    // above is unchanged, this just narrows what the list shows).
    // 0 = all, 1 = in progress (queued / running / paused),
    // 2 = completed, 3 = failed.
    // Saveable since v0.85.0 (second configuration-change audit):
    // a typed search word and a chosen filter are work in progress,
    // not session confetti — an activity recreation (uncovered
    // configuration change, process death with state restore) must
    // not silently reset them. Rotation never recreated anyway
    // (v0.34.0 configChanges); this covers the remaining paths.
    var statusFilter by rememberSaveable { mutableStateOf(0) }
    // Page-local filename search (v0.64.0), same shape as the
    // bookmark/history page searches: memory only, never persisted,
    // and it survives filter changes and list refreshes because
    // nothing below ever resets it.
    var downloadQuery by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(statusFilter, downloadQuery) {
        listState.scrollToItem(0)
    }
    // Presentation filter, memoised on the inputs (v0.23.0): with
    // remember the filter no longer re-runs on unrelated
    // recompositions while the list is being scrolled. Entries
    // change on refreshTick bumps (screen entry, delete, retry) and,
    // since v0.70.0, on the once-a-second in-progress poll above.
    // Single pipeline for what the DownloadManager list shows
    // (v0.64.0): status filter first, then the filename search on
    // top of it (case-insensitive substring on the file name).
    // Everything display-side reads this one list; everything
    // destructive (bulk clear counts and target ids below) keeps
    // reading the full [entries] list, so a search can never
    // shrink what a bulk clear actually removes.
    val visibleEntries = remember(entries, statusFilter, downloadQuery) {
        val query = downloadQuery.trim()
        entries.filter { entry ->
            when (statusFilter) {
                1 -> entry.status == android.app.DownloadManager.STATUS_PENDING ||
                    entry.status == android.app.DownloadManager.STATUS_RUNNING ||
                    entry.status == android.app.DownloadManager.STATUS_PAUSED
                2 -> entry.status == android.app.DownloadManager.STATUS_SUCCESSFUL
                3 -> entry.status == android.app.DownloadManager.STATUS_FAILED
                else -> true
            }
        }.filter { entry ->
            query.isEmpty() || entry.title.contains(query, ignoreCase = true)
        }
    }

    // In-app records of Gecko (Firefox) tab downloads (v0.57.0).
    // Those downloads are saved by the app itself and never pass
    // through DownloadManager, so they live in their own section
    // below the DownloadManager list — the two state models are
    // different (these rows are saved files, with no DownloadManager
    // status or retry), and mixing the rows would mislead. Downloads
    // still in progress are a third, live group rendered above the
    // records (v0.69.0). The status filter chips above apply to the
    // DownloadManager list only.
    val geckoStore = remember { Store(context) }
    // Threading (v0.167.0): the records list used to be parsed
    // synchronously inside remember{} during composition (up to
    // the 200-record cap, on the main thread), and the reload
    // effect below re-parsed it on the main thread too. It now
    // starts empty and every load runs on IO — the effect keyed
    // on the live-download ids below performs the first load as
    // well, since it fires on entry.
    var geckoRecords by remember {
        mutableStateOf<List<GeckoDownloadRecord>>(emptyList())
    }
    val downloadsScope = rememberCoroutineScope()
    val geckoTimeFormat = remember {
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    }
    // The Gecko section follows the same filename search
    // (v0.64.0): same case-insensitive substring rule on the file
    // name. Display reads this filtered list; single delete, open
    // and the clear-this-section action keep working on the full
    // [geckoRecords] list, exactly like the bulk clear above.
    val visibleGeckoRecords = remember(geckoRecords, downloadQuery) {
        val query = downloadQuery.trim()
        if (query.isEmpty()) {
            geckoRecords
        } else {
            geckoRecords.filter {
                it.fileName.contains(query, ignoreCase = true)
            }
        }
    }
    // In-progress Gecko downloads (v0.69.0): live entries from the
    // engine-side tracker, rendered above the completed records in
    // their own "downloading" block with a progress bar and a cancel
    // button. Reading the snapshot list here re-renders this screen
    // whenever the copy loop publishes a progress update. The same
    // filename search filters these rows too.
    val activeGeckoDownloads = GeckoDownloadTracker.active
    val visibleActiveGeckoDownloads = remember(
        activeGeckoDownloads.toList(), downloadQuery
    ) {
        val query = downloadQuery.trim()
        if (query.isEmpty()) {
            activeGeckoDownloads.toList()
        } else {
            activeGeckoDownloads.filter {
                it.fileName.contains(query, ignoreCase = true)
            }
        }
    }
    // Mirror fix (v0.86.0 audit): the records list above is loaded
    // once when this screen opens, but a Gecko download that
    // finishes WHILE the user is looking at this screen writes its
    // record from the engine side — the row used to vanish from
    // the live section and never appear in the records section
    // until the screen was reopened. The tracker retires a live
    // entry only after its record is written (saveGeckoDownload),
    // so any change in the live id set is a safe moment to reload
    // the records from the same store they were written to.
    val activeGeckoIds = activeGeckoDownloads.map { it.id }
    LaunchedEffect(activeGeckoIds) {
        geckoRecords = kotlinx.coroutines.withContext(
            kotlinx.coroutines.Dispatchers.IO
        ) {
            geckoStore.loadGeckoDownloads()
        }
    }
    var pendingGeckoDelete by remember {
        mutableStateOf<GeckoDownloadRecord?>(null)
    }
    var showClearGeckoConfirm by remember { mutableStateOf(false) }

    var pendingDeleteEntry by remember { mutableStateOf<DownloadEntry?>(null) }
    val toDeleteEntry = pendingDeleteEntry
    if (toDeleteEntry != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteEntry = null },
            title = { Text("删除这条下载记录？") },
            text = {
                // Scrollable body (v0.169.0, same v0.115.0/v0.137.0
                // treatment): the embedded entry title is an
                // unbounded file name from the network.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("「${toDeleteEntry.title}」的记录会被删掉，已经下载好的文件也会一起从下载文件夹里删掉，没法恢复。")
                }
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

    // Bulk clear of finished records (v0.49.0). Records that are
    // still going (pending / running / paused) are never in scope:
    // the target filter below only ever matches STATUS_SUCCESSFUL
    // and STATUS_FAILED, so a half-finished download cannot be
    // wiped by accident. The wording follows the single-delete
    // dialog above: DownloadManager.remove deletes the downloaded
    // file together with the record, and the counts shown are the
    // real counts from the loaded list, never hard-coded.
    val completedCount = entries.count {
        it.status == android.app.DownloadManager.STATUS_SUCCESSFUL
    }
    val failedCount = entries.count {
        it.status == android.app.DownloadManager.STATUS_FAILED
    }
    val finishedCount = completedCount + failedCount
    var showBulkClearPicker by remember { mutableStateOf(false) }
    var bulkPick by remember { mutableStateOf(0) }
    var bulkConfirmScope by remember { mutableStateOf<Int?>(null) }
    val bulkScope = rememberCoroutineScope()

    // Scope encoding: 0 = completed only, 1 = failed only,
    // 2 = all finished (completed + failed).
    fun bulkTargetIds(scope: Int): List<Long> = entries.filter { entry ->
        when (scope) {
            0 -> entry.status == android.app.DownloadManager.STATUS_SUCCESSFUL
            1 -> entry.status == android.app.DownloadManager.STATUS_FAILED
            else -> entry.status ==
                android.app.DownloadManager.STATUS_SUCCESSFUL ||
                entry.status == android.app.DownloadManager.STATUS_FAILED
        }
    }.map { it.id }

    if (showBulkClearPicker) {
        val pickCount = when (bulkPick) {
            0 -> completedCount
            1 -> failedCount
            else -> finishedCount
        }
        AlertDialog(
            onDismissRequest = { showBulkClearPicker = false },
            title = { Text("清除下载记录") },
            text = {
                LazyColumn {
                    item {
                        Text(
                            "选择要清除的范围。下载中、排队中和已暂停的" +
                                "记录不在范围内，不会被清掉。"
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("已完成的记录") },
                            supportingContent = {
                                Text(
                                    "共 $completedCount 条；记录删掉后，" +
                                        "已经下载好的文件也会一起删掉"
                                )
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = bulkPick == 0,
                                    // Passive indicator (v0.136.0 accessibility
                                    // pass): the enclosing row/card is the one
                                    // click stop; a live onClick here made
                                    // TalkBack stop twice on the same choice
                                    // (same fix as v0.99.0).
                                    onClick = null,
                                    enabled = completedCount > 0
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = completedCount > 0) {
                                    bulkPick = 0
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("失败的记录") },
                            supportingContent = {
                                Text("共 $failedCount 条")
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = bulkPick == 1,
                                    // Passive indicator (v0.136.0 accessibility
                                    // pass): the enclosing row/card is the one
                                    // click stop; a live onClick here made
                                    // TalkBack stop twice on the same choice
                                    // (same fix as v0.99.0).
                                    onClick = null,
                                    enabled = failedCount > 0
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = failedCount > 0) {
                                    bulkPick = 1
                                }
                        )
                    }
                    item {
                        ListItem(
                            headlineContent = { Text("全部已结束的记录") },
                            supportingContent = {
                                Text(
                                    "已完成加失败，共 $finishedCount 条；" +
                                        "已完成记录对应的文件也会一起删掉"
                                )
                            },
                            leadingContent = {
                                RadioButton(
                                    selected = bulkPick == 2,
                                    // Passive indicator (v0.136.0 accessibility
                                    // pass): the enclosing row/card is the one
                                    // click stop; a live onClick here made
                                    // TalkBack stop twice on the same choice
                                    // (same fix as v0.99.0).
                                    onClick = null,
                                    enabled = finishedCount > 0
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = finishedCount > 0) {
                                    bulkPick = 2
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showBulkClearPicker = false
                    if (pickCount == 0) {
                        android.widget.Toast.makeText(
                            context, "没有可清除的记录",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        bulkConfirmScope = bulkPick
                    }
                }) { Text("下一步") }
            },
            dismissButton = {
                TextButton(onClick = { showBulkClearPicker = false }) {
                    Text("取消")
                }
            }
        )
    }

    val confirmScope = bulkConfirmScope
    if (confirmScope != null) {
        val targetIds = bulkTargetIds(confirmScope)
        val confirmText = when (confirmScope) {
            0 -> "将清除 ${targetIds.size} 条已完成的下载记录。记录删掉后，" +
                "已经下载好的文件也会一起从下载文件夹里删掉，没法恢复。" +
                "下载中、排队中和已暂停的记录不会被清掉。"
            1 -> "将清除 ${targetIds.size} 条失败的下载记录，没法恢复；" +
                "如果留有没下完的文件，也会一起删掉。" +
                "下载中、排队中和已暂停的记录不会被清掉。"
            else -> "将清除 ${targetIds.size} 条已结束的下载记录" +
                "（已完成 $completedCount 条、失败 $failedCount 条）。" +
                "已完成记录对应的文件也会一起从下载文件夹里删掉，没法恢复。" +
                "下载中、排队中和已暂停的记录不会被清掉。"
        }
        AlertDialog(
            onDismissRequest = { bulkConfirmScope = null },
            title = { Text("确认清除？") },
            text = { Text(confirmText) },
            confirmButton = {
                Button(onClick = {
                    bulkConfirmScope = null
                    if (targetIds.isEmpty()) {
                        android.widget.Toast.makeText(
                            context, "没有可清除的记录",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // The remove calls run off the main thread:
                        // with many records they must not block the UI.
                        bulkScope.launch {
                            val removed = kotlinx.coroutines.withContext(
                                kotlinx.coroutines.Dispatchers.IO
                            ) {
                                try {
                                    val manager = context.getSystemService(
                                        android.content.Context.DOWNLOAD_SERVICE
                                    ) as android.app.DownloadManager
                                    manager.remove(*targetIds.toLongArray())
                                } catch (t: Throwable) {
                                    -1
                                }
                            }
                            if (removed >= 0) {
                                android.widget.Toast.makeText(
                                    context, "已清除 $removed 条记录",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                android.widget.Toast.makeText(
                                    context, "清除失败，请重试",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                            refreshTick++
                        }
                    }
                }) { Text("清除") }
            },
            dismissButton = {
                TextButton(onClick = { bulkConfirmScope = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Whether the file a Gecko record points at is still there.
    fun geckoRecordFileExists(record: GeckoDownloadRecord): Boolean {
        return try {
            val uriText = record.contentUri
            if (uriText != null) {
                val uri = android.net.Uri.parse(uriText)
                context.contentResolver.query(
                    uri,
                    arrayOf(android.provider.MediaStore.Downloads._ID),
                    null, null, null
                )?.use { it.moveToFirst() } ?: false
            } else {
                val path = record.filePath
                path != null && java.io.File(path).exists()
            }
        } catch (t: Throwable) {
            false
        }
    }

    // Delete the file behind a Gecko record. Returns true when the
    // file is gone afterwards (deleted here, or already missing).
    // MediaStore deletes are allowed for entries this app inserted
    // itself, which is exactly what these URIs are.
    fun deleteGeckoRecordFile(record: GeckoDownloadRecord): Boolean {
        if (!geckoRecordFileExists(record)) return true
        return try {
            val uriText = record.contentUri
            if (uriText != null) {
                context.contentResolver.delete(
                    android.net.Uri.parse(uriText), null, null
                ) > 0 && !geckoRecordFileExists(record)
            } else {
                val path = record.filePath
                path != null && java.io.File(path).delete() &&
                    !geckoRecordFileExists(record)
            }
        } catch (t: Throwable) {
            false
        }
    }

    // Delete semantics for this section (v0.57.0, fixed): a delete
    // removes the record AND its file together. The file goes
    // first; if the file cannot be deleted the record is KEPT and
    // the failure is reported in plain language, so a record never
    // silently outlives a file the user believes is gone, and a
    // file is never deleted while its record claims otherwise.
    // Returns true when record + file are both gone.
    fun deleteGeckoRecord(record: GeckoDownloadRecord): Boolean {
        val fileGone = deleteGeckoRecordFile(record)
        if (!fileGone) return false
        geckoStore.removeGeckoDownload(record)
        geckoRecords = geckoStore.loadGeckoDownloads()
        return true
    }

    // Open a Gecko-downloaded file through the MediaStore URI the
    // record carries (v0.57.0), with the same plain-language
    // failure wording as the DownloadManager side (v0.44.0).
    fun openGeckoDownload(record: GeckoDownloadRecord) {
        try {
            val uriText = record.contentUri
            if (uriText == null) {
                // Pre-Android-10 records carry a plain file path in
                // the app download folder instead of a MediaStore
                // URI. A file:// URI cannot leave the app on modern
                // Android (FileUriExposedException), so the path is
                // served through the app's FileProvider instead
                // (v0.195.0) — before this, such a record could
                // never be opened from this page on a current
                // device and only ever met the failure toast.
                val path = record.filePath
                if (path == null || !java.io.File(path).exists()) {
                    android.widget.Toast.makeText(
                        context, "文件已不存在，可能已被删除或移动",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    return
                }
                val servedUri =
                    androidx.core.content.FileProvider.getUriForFile(
                        context,
                        context.packageName + ".fileprovider",
                        java.io.File(path)
                    )
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW
                )
                    .setDataAndType(
                        servedUri,
                        record.mimeType ?: "*/*"
                    )
                    .addFlags(
                        android.content.Intent
                            .FLAG_GRANT_READ_URI_PERMISSION
                    )
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
            val uri = android.net.Uri.parse(uriText)
            if (!geckoRecordFileExists(record)) {
                android.widget.Toast.makeText(
                    context, "文件已不存在，可能已被删除或移动",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }
            val mime = record.mimeType?.takeIf { it.isNotBlank() }
                ?: try {
                    context.contentResolver.getType(uri)
                } catch (t: Throwable) {
                    null
                } ?: "*/*"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW
            )
                .setDataAndType(uri, mime)
                .addFlags(
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                .addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                )
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(
                context, "没有能打开这种文件的应用",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "打开失败，文件可能已被删除或移动",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    val toDeleteGecko = pendingGeckoDelete
    if (toDeleteGecko != null) {
        AlertDialog(
            onDismissRequest = { pendingGeckoDelete = null },
            title = { Text("删除这条记录？") },
            text = {
                // Scrollable body (v0.169.0, same treatment as the
                // system-download twin above): the embedded file
                // name is unbounded.
                Column(
                    modifier = Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "「${toDeleteGecko.fileName}」的记录会被删掉，" +
                            "下载文件夹里的文件也会一起删掉，没法恢复。" +
                            "如果文件删不掉，记录会保留下来并告诉你。"
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    pendingGeckoDelete = null
                    // Threading (v0.167.0): deleting the record's
                    // file is provider/file I/O (existence query,
                    // delete, store rewrite) — it runs on IO, the
                    // result toast comes back to the main thread.
                    downloadsScope.launch {
                        val deleted = kotlinx.coroutines.withContext(
                            kotlinx.coroutines.Dispatchers.IO
                        ) {
                            deleteGeckoRecord(toDeleteGecko)
                        }
                        android.widget.Toast.makeText(
                            context,
                            if (deleted) {
                                "已删除记录和文件"
                            } else {
                                "文件没删掉，记录先保留着"
                            },
                            if (deleted) {
                                android.widget.Toast.LENGTH_SHORT
                            } else {
                                android.widget.Toast.LENGTH_LONG
                            }
                        ).show()
                    }
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingGeckoDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showClearGeckoConfirm) {
        AlertDialog(
            onDismissRequest = { showClearGeckoConfirm = false },
            title = { Text("清空本节记录？") },
            text = {
                Text(
                    "将清除本节全部 ${geckoRecords.size} 条记录，" +
                        "对应的文件也会一起从下载文件夹里删掉，没法恢复。" +
                        "哪个文件删不掉，哪条记录就会保留下来。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showClearGeckoConfirm = false
                    // Threading (v0.167.0): this loop used to run
                    // entirely on the main thread — per record a
                    // provider existence query, a file delete and
                    // a full store load+save, so clearing the whole
                    // section blocked the UI for the whole list.
                    // Same semantics as a single delete, per record
                    // (file first, record only once the file is
                    // gone), now on IO; the counts toast returns
                    // to the main thread.
                    val recordsSnapshot = geckoRecords.toList()
                    downloadsScope.launch {
                        val (removedCount, keptCount) =
                            kotlinx.coroutines.withContext(
                                kotlinx.coroutines.Dispatchers.IO
                            ) {
                                var removed = 0
                                var kept = 0
                                recordsSnapshot.forEach { record ->
                                    if (deleteGeckoRecord(record)) {
                                        removed++
                                    } else {
                                        kept++
                                    }
                                }
                                removed to kept
                            }
                        android.widget.Toast.makeText(
                            context,
                            if (keptCount == 0) {
                                "已清除 $removedCount 条记录"
                            } else {
                                "已清除 $removedCount 条；" +
                                    "$keptCount 条的文件没删掉，记录保留着"
                            },
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearGeckoConfirm = false }) {
                    Text("取消")
                }
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
            context, source, retryUserAgent, null, entry.mediaType,
            referer = retryReferer
        )
        refreshTick++
    }

    // Open a completed download through the official DownloadManager
    // channel (v0.44.0): getUriForDownloadedFile hands out a content
    // URI for the file, the type comes from the entry's
    // COLUMN_MEDIA_TYPE value (falling back to the manager's own
    // lookup), and ACTION_VIEW with a read grant lets the system
    // pick the handler — an APK therefore lands in the system
    // installer, other files in whatever app claims their type.
    // Every failure says so in plain language instead of doing
    // nothing: a null URI means the record outlived the file, and
    // ActivityNotFoundException means no installed app can open
    // this type.
    fun openDownloadedFile(entry: DownloadEntry) {
        try {
            val manager = context.getSystemService(
                android.content.Context.DOWNLOAD_SERVICE
            ) as android.app.DownloadManager
            val uri = manager.getUriForDownloadedFile(entry.id)
            if (uri == null) {
                android.widget.Toast.makeText(
                    context, "文件已不存在，可能已被删除或移动",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                return
            }
            val mime = entry.mediaType?.takeIf { it.isNotBlank() }
                ?: manager.getMimeTypeForDownloadedFile(entry.id)
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW
            )
                .setDataAndType(uri, mime)
                .addFlags(
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                .addFlags(
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                )
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            android.widget.Toast.makeText(
                context, "没有能打开这种文件的应用",
                android.widget.Toast.LENGTH_LONG
            ).show()
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "打开失败，文件可能已被删除或移动",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    // System back (v0.37.0, layered in v0.84.0 — same settled
    // rule as the history page: search text clears first, then
    // the status filter, and only then does the key leave the
    // page). Before this the key left the page outright even
    // with a search word or a non-default filter active. The
    // three handlers are mutually exclusive by enabled, declared
    // weakest first.
    BackHandler(enabled = downloadQuery.isBlank() && statusFilter == 0) {
        onBack()
    }
    BackHandler(enabled = downloadQuery.isBlank() && statusFilter != 0) {
        statusFilter = 0
    }
    BackHandler(enabled = downloadQuery.isNotBlank()) { downloadQuery = "" }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("下载") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // Bulk-clear entry (v0.49.0): a plain text
                    // button in the top bar, matching the page's
                    // existing controls. With nothing finished it
                    // still answers honestly instead of opening an
                    // empty picker.
                    TextButton(onClick = {
                        if (finishedCount == 0) {
                            android.widget.Toast.makeText(
                                context, "没有可清除的记录",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            bulkPick = when {
                                completedCount > 0 -> 0
                                failedCount > 0 -> 1
                                else -> 2
                            }
                            showBulkClearPicker = true
                        }
                    }) { Text("清除记录") }
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
            if (entries.isNotEmpty() || geckoRecords.isNotEmpty() ||
                activeGeckoDownloads.isNotEmpty()
            ) {
                item {
                    OutlinedTextField(
                        value = downloadQuery,
                        onValueChange = { downloadQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        singleLine = true,
                        // Live-filter field (v0.138.0): Search
                        // action face, no auto-correct — the text
                        // is matched against file names.
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search
                        ),
                        placeholder = { Text("搜索下载（文件名）") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                }
            }
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
            if (entries.isEmpty() && geckoRecords.isEmpty() &&
                activeGeckoDownloads.isEmpty()
            ) {
                item {
                    EmptyState(Icons.Filled.Download, "还没有下载记录。")
                }
            } else if (downloadQuery.isNotBlank() &&
                visibleEntries.isEmpty() && visibleGeckoRecords.isEmpty() &&
                visibleActiveGeckoDownloads.isEmpty()
            ) {
                // Search found nothing in either list (v0.64.0):
                // its own wording, distinct from both the
                // no-records-at-all state above and the
                // per-status empty states below.
                item {
                    EmptyState(
                        Icons.Filled.Search,
                        "没有找到包含「${downloadQuery.trim()}」的下载"
                    )
                }
            } else if (entries.isNotEmpty() && visibleEntries.isEmpty() &&
                downloadQuery.isBlank()
            ) {
                // Per-status empty note (v0.141.0: only with no
                // search word — with a search word the all-sections
                // empty state above owns the no-match case, and when
                // the word matched another section this note would
                // wrongly claim there are no records at all).
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
                    headlineContent = {
                        Text(
                            entry.title,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    supportingContent = {
                        val parts = mutableListOf(entry.statusText, entry.sizeText)
                        if (!entry.mediaType.isNullOrBlank()) {
                            parts.add(entry.mediaType)
                        }
                        if (entry.status ==
                            android.app.DownloadManager.STATUS_SUCCESSFUL
                        ) {
                            // Discoverability (v0.44.0): a completed
                            // entry can be tapped to open the file,
                            // and the row says so.
                            parts.add("点按打开")
                        }
                        Text(parts.joinToString(" · "))
                    },
                    leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                    trailingContent = {
                        Row {
                            if (entry.status ==
                                android.app.DownloadManager.STATUS_SUCCESSFUL
                            ) {
                                IconButton(onClick = { openDownloadedFile(entry) }) {
                                    Icon(
                                        Icons.Filled.OpenInNew,
                                        contentDescription = "打开文件"
                                    )
                                }
                            }
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
                            openDownloadedFile(entry)
                        }
                )
                HorizontalDivider()
            }
            if (visibleActiveGeckoDownloads.isNotEmpty()) {
                item {
                    // In-progress Gecko downloads header (v0.69.0):
                    // these copies are still running, so they sit
                    // above the completed-records section below and
                    // disappear the moment each copy ends (the row is
                    // replaced by the persisted record on success).
                    Column(
                        // Live section animation (v0.111.0 motion
                        // audit): this whole in-progress section
                        // used to pop in and out of the list the
                        // moment a Gecko download started or ended.
                        // It now fades on the shared spec, like
                        // the multi-select bars (v0.82.0) and the
                        // bookmarks batch bar (this version).
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 16.dp)
                            .animateItem(
                                fadeInSpec = tween(
                                    220, easing = FastOutSlowInEasing
                                ),
                                fadeOutSpec = tween(
                                    180, easing = FastOutSlowInEasing
                                )
                            )
                    ) {
                        Text(
                            "正在下载（火狐）",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "火狐标签正在下载的文件，进度实时更新；" +
                                "点右侧按钮可以取消，取消后未完成的文件会被删掉。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(
                    visibleActiveGeckoDownloads,
                    key = { "gecko-active-${it.id}" }
                ) { progress ->
                    ListItem(
                        headlineContent = {
                            Text(
                                progress.fileName,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            Column {
                                if (progress.totalBytes > 0) {
                                    val percent = (
                                        progress.downloadedBytes * 100 /
                                            progress.totalBytes
                                        ).coerceIn(0, 100)
                                    Text(
                                        "已下载 ${formatStorageSize(progress.downloadedBytes)}" +
                                            " / ${formatStorageSize(progress.totalBytes)}" +
                                            " · $percent%"
                                    )
                                    LinearProgressIndicator(
                                        progress = {
                                            (
                                                progress.downloadedBytes.toFloat() /
                                                    progress.totalBytes.toFloat()
                                                ).coerceIn(0f, 1f)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    )
                                } else {
                                    // No Content-Length: no honest
                                    // percentage exists, so the bar
                                    // stays indeterminate and the
                                    // text reports bytes so far.
                                    Text(
                                        "已下载 ${formatStorageSize(progress.downloadedBytes)}" +
                                            "（总大小未知）"
                                    )
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Filled.Download, contentDescription = null)
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                GeckoDownloadTracker.requestCancel(progress.id)
                            }) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "取消下载"
                                )
                            }
                        },
                        // Same live-section fade as the header
                        // above (v0.111.0): a row fades in when
                        // its download starts and out when it
                        // ends; progress updates in between do
                        // not re-trigger it (same key, same slot).
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(
                                fadeInSpec = tween(
                                    220, easing = FastOutSlowInEasing
                                ),
                                fadeOutSpec = tween(
                                    180, easing = FastOutSlowInEasing
                                )
                            )
                    )
                    HorizontalDivider()
                }
            }
            if (visibleGeckoRecords.isNotEmpty()) {
                item {
                    // Section header for the Gecko records
                    // (v0.57.0). Kept apart from the DownloadManager
                    // list above on purpose: these rows have no
                    // download status, progress or retry — the file
                    // is simply saved already.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "应用内保存",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "火狐标签下载的文件由浏览器直接保存，" +
                                    "不走系统下载管理，所以单独列在这里。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { showClearGeckoConfirm = true }) {
                            Text("清空本节记录")
                        }
                    }
                }
                items(
                    visibleGeckoRecords,
                    key = { "gecko-${it.time}-${it.contentUri ?: it.filePath ?: it.fileName}" }
                ) { record ->
                    ListItem(
                        headlineContent = {
                            Text(
                                record.fileName,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            val parts = mutableListOf(
                                formatStorageSize(record.sizeBytes),
                                geckoTimeFormat.format(java.util.Date(record.time))
                            )
                            record.mimeType?.takeIf { it.isNotBlank() }
                                ?.let { parts.add(it) }
                            hostOf(record.sourceUrl)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { parts.add(it) }
                            parts.add("点按打开")
                            Text(parts.joinToString(" · "))
                        },
                        leadingContent = {
                            Icon(Icons.Filled.Download, contentDescription = null)
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { openGeckoDownload(record) }) {
                                    Icon(
                                        Icons.Filled.OpenInNew,
                                        contentDescription = "打开文件"
                                    )
                                }
                                IconButton(onClick = { pendingGeckoDelete = record }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "删除这条记录"
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openGeckoDownload(record) }
                    )
                    HorizontalDivider()
                }
            }
            item {
                // Duplicate-name and list-scope notes, so a renamed
                // download is never mistaken for a lost file:
                // DownloadManager never overwrites an existing file
                // (a repeated download is either saved under an
                // adjusted name or reported as "同名文件已经存在").
                // Gecko-tab downloads are recorded in the section
                // above (v0.57.0); incognito Gecko downloads are
                // saved as usual but deliberately leave no record.
                Text(
                    text = "说明：同一个文件重复下载时，原来的文件不会被覆盖——" +
                        "系统可能给新文件自动改名保存，也可能提示「同名文件已经存在」。" +
                        "火狐标签下载的文件会列在上面的「应用内保存」一节里；" +
                        "火狐无痕标签的下载只保存文件、不留记录，不会出现在这里；" +
                        "系统 WebView 无痕标签的下载会留在上面的系统下载记录里，关掉无痕标签也不会自动删掉。",
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
    onClear: (Boolean, Boolean, Boolean, () -> Unit) -> Unit
) {
    var clearHistorySel by remember { mutableStateOf(true) }
    var clearWebView by remember { mutableStateOf(true) }
    var clearGecko by remember { mutableStateOf(true) }
    var showConfirm by remember { mutableStateOf(false) }
    var doneText by remember { mutableStateOf<String?>(null) }
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
                    // The done message waits for the clear's real
                    // completion callback (v0.129.0): the WebView
                    // cookie removal is asynchronous, so reporting
                    // success here, before it lands, would be a
                    // false completion.
                    onClear(clearHistorySel, clearWebView, clearGecko) {
                        doneText = "已清除勾选的数据。"
                        storageTick++
                    }
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
                Text(
                    "清除不会删掉你导出到下载文件夹的备份文件和书签文件。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("历史记录") },
                    supportingContent = { Text("应用里记下的浏览历史会全部清掉。") },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = clearHistorySel,
                            onCheckedChange = { clearHistorySel = it },
                            modifier = Modifier.semantics {
                                contentDescription = "历史记录"
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("系统 WebView 的 Cookie、网站数据和缓存") },
                    supportingContent = { Text("清完后用系统 WebView 打开的网站都要重新登录。") },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = clearWebView,
                            onCheckedChange = { clearWebView = it },
                            modifier = Modifier.semantics {
                                contentDescription = "系统 WebView 的 Cookie、网站数据和缓存"
                            }
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
                                "火狐内核还没启动，这次清不了火狐数据；" +
                                    "先打开一个火狐标签，再回来清。"
                            }
                        )
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = clearGecko && geckoStarted,
                            onCheckedChange = { clearGecko = it },
                            enabled = geckoStarted,
                            modifier = Modifier.semantics {
                                contentDescription = "火狐的 Cookie、网站数据和缓存"
                            }
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
                item {
                    // Polite live region (v0.136.0 accessibility
                    // pass): the text only appears once the clear's
                    // real completion callback lands (v0.129.0), and
                    // until now it appeared silently — a TalkBack
                    // user who had just confirmed the clear heard
                    // nothing. Same pattern as the tab-sheet and
                    // sniff selection counts (v0.78.0).
                    Text(
                        doneText!!,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                }
            }
            item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }
        }
    }
}

/**
 * Fingerprint mode's own shell (v0.144.0): a deliberately smaller
 * browser chrome than the normal one — a top bar that says
 * 指纹模式 out loud, one address row with back / forward /
 * reload / tabs / new-tab, a profile strip that opens the
 * region & time-zone picker from inside the mode, and nothing
 * else. Fingerprint tabs are a separate group (see fpTabs in
 * BrowserApp): they never record history, always run on the
 * system WebView, and present the picked profile. Exiting returns
 * to the normal shell; the fingerprint tabs stay as they are.
 */
/**
 * Edge-swipe web navigation (v0.179.0, both shells;
 * restructured v0.186.0): a drag that STARTS within 24dp of
 * the left edge and travels 64dp inward goes back in the web
 * history; the right edge mirrors it forward. Vertical drift
 * past 48dp bails out untouched, so page scrolling and
 * pull-to-refresh never see a consumed event from us; once
 * fired, the rest of that gesture is consumed so the page
 * does not also react. Nothing is consumed before the
 * threshold, so ordinary page touches — including horizontal
 * carousels NOT starting at the very edge — are unaffected;
 * an edge-born horizontal swipe past 64dp is claimed by
 * navigation (the documented trade-off of having this
 * gesture at all).
 *
 * Structure (v0.186.0): the detector lives ONLY on the two
 * 24dp edge strips below. There is deliberately NO full-size
 * pointer-input layer over the page any more: the pre-0.186
 * shape put one full-screen overlay box on top of the
 * WebView (inside the fingerprint shell's pull-to-refresh
 * box, as its sibling after the page content), and every
 * touch on the page had to pass that layer's gesture loop
 * first — on the real device, fingerprint-mode pages stopped
 * scrolling altogether. The geometry and the verdict logic
 * live in [EdgeSwipeSpec] so unit tests pin them; the strips
 * here only feed it pointer positions. A pointer that starts
 * on a strip keeps reporting to that strip even after it
 * travels past the strip's inner edge, so the 64dp trigger
 * distance is unaffected by the narrower hit area.
 *
 * System-gesture avoidance: the OS back gesture owns the
 * edges by default. Two invisible 24dp x 200dp strips at the
 * vertical centre of each edge carry systemGestureExclusion,
 * inside which the system is asked to let the app's gesture
 * win; above and below those strips the system back gesture
 * keeps its priority, and on three-button navigation there
 * is no conflict anywhere. The exclusion is an OS-honoured
 * request (capped per edge by the platform), not a guarantee
 * on every OEM build — the real-device checklist carries it.
 */
@Composable
private fun EdgeSwipeNavigation(
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        EdgeSwipeStrip(
            fromLeft = true,
            onTrigger = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        EdgeSwipeStrip(
            fromLeft = false,
            onTrigger = onForward,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun EdgeSwipeStrip(
    fromLeft: Boolean,
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val triggerPx = with(density) { EdgeSwipeSpec.TRIGGER_DP.dp.toPx() }
    val driftPx = with(density) { EdgeSwipeSpec.DRIFT_DP.dp.toPx() }
    Box(
        modifier = modifier
            .width(EdgeSwipeSpec.EDGE_WIDTH_DP.dp)
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .align(
                    if (fromLeft) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
                )
                .width(EdgeSwipeSpec.EDGE_WIDTH_DP.dp)
                .height(EdgeSwipeSpec.EXCLUSION_HEIGHT_DP.dp)
                .systemGestureExclusion()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(onTrigger) {
                    awaitEachGesture {
                        val down = awaitFirstDown(
                            requireUnconsumed = false
                        )
                        var fired = false
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes
                                .firstOrNull { it.id == down.id }
                                ?: break
                            if (!change.pressed) break
                            if (fired) {
                                change.consume()
                                continue
                            }
                            val verdict = EdgeSwipeSpec.classify(
                                fromLeft = fromLeft,
                                dxPx = change.position.x -
                                    down.position.x,
                                dyPx = change.position.y -
                                    down.position.y,
                                triggerPx = triggerPx,
                                driftPx = driftPx
                            )
                            when (verdict) {
                                EdgeSwipeSpec.Verdict.ABANDON ->
                                    break
                                EdgeSwipeSpec.Verdict.FIRE -> {
                                    fired = true
                                    change.consume()
                                    onTrigger()
                                }
                                EdgeSwipeSpec.Verdict.CONTINUE ->
                                    Unit
                            }
                        }
                    }
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FingerprintShell(
    tabs: List<BrowserTab>,
    activeTab: BrowserTab?,
    resolved: ResolvedFingerprint,
    searchEngine: SearchEngine,
    onSelectTab: (BrowserTab) -> Unit,
    onCloseTab: (BrowserTab) -> Unit,
    onNewTab: () -> Unit,
    onExit: () -> Unit,
    onProfileChange: (String, String, String, String) -> Unit,
    customProfiles: List<com.tuxunbrowse.android.engine.CustomFingerprint> =
        emptyList(),
    onSaveCustomProfile: (com.tuxunbrowse.android.engine.CustomFingerprint) -> Unit =
        {},
    onDeleteCustomProfile: (com.tuxunbrowse.android.engine.CustomFingerprint) -> Unit =
        {},
    isBookmarked: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onOpenDownloads: () -> Unit = {},
    onOpenBookmarks: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onCloseAllTabs: () -> Unit = {},
    protection: FingerprintProtection = FingerprintProtection(),
    onProtectionChange: (FingerprintProtection) -> Unit = {},
    proxyConfig: com.tuxunbrowse.android.engine.FpProxyConfig =
        com.tuxunbrowse.android.engine.FpProxyConfig(),
    onProxyChange: (com.tuxunbrowse.android.engine.FpProxyConfig) -> Unit =
        {}
) {
    val context = LocalContext.current
    val shellScope = rememberCoroutineScope()
    val profile = resolved.profile
    val zone = resolved.timezone
    val device = resolved.device
    // Saved combinations (v0.146.0): seeded from the store-backed
    // prop, then held locally so a save/delete shows at once; the
    // callbacks persist every change alongside.
    var customs by remember {
        mutableStateOf(customProfiles)
    }
    // Export / import for the saved combinations (v0.197.0):
    // the file is the wrapped JSON FingerprintProfiles builds;
    // importing merges through the same dedupe the save button
    // uses and reports the count honestly, including zero.
    val exportCustomsLauncher =
        rememberLauncherForActivityResult(
            androidx.activity.result.contract
                .ActivityResultContracts.CreateDocument(
                    "application/json"
                )
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            try {
                context.contentResolver.openOutputStream(uri)
                    ?.use { out ->
                        out.write(
                            com.tuxunbrowse.android.engine
                                .FingerprintProfiles
                                .customProfilesToJson(customs)
                                .toByteArray(Charsets.UTF_8)
                        )
                    }
                android.widget.Toast.makeText(
                    context, "已导出 ${customs.size} 条档案",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } catch (t: Throwable) {
                android.widget.Toast.makeText(
                    context, "导出失败，文件没有写成",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    val importCustomsLauncher =
        rememberLauncherForActivityResult(
            androidx.activity.result.contract
                .ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            // Bounded read + background parse (v0.200.0): this
            // was the one import path still reading its file
            // with an unbounded readText() on the main thread —
            // the bookmark and backup imports both moved to
            // readImportFileText (8M-char cap) off the main
            // thread in v0.167.0. An oversized or pathological
            // file could exhaust memory before the parser ever
            // rejected it; now it fails the same plain way the
            // other imports do, and existing combinations are
            // untouched on failure.
            shellScope.launch {
                try {
                    val parsed = withContext(Dispatchers.Default) {
                        com.tuxunbrowse.android.engine
                            .FingerprintProfiles
                            .parseCustomProfiles(
                                readImportFileText(context, uri)
                            )
                    }
                    var added = 0
                    for (entry in parsed) {
                        if (customs.none {
                                it.regionId == entry.regionId &&
                                    it.deviceId == entry.deviceId &&
                                    it.timezoneId == entry.timezoneId &&
                                    it.language == entry.language
                            }
                        ) {
                            customs = customs + entry
                            onSaveCustomProfile(entry)
                            added++
                        }
                    }
                    android.widget.Toast.makeText(
                        context,
                        if (added > 0) {
                            "已导入 $added 条档案"
                        } else {
                            "没有导入新的档案（文件里没有新组合）"
                        },
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                } catch (t: Throwable) {
                    android.widget.Toast.makeText(
                        context, "导入失败，这个文件读不了",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    var showTabs by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    var showFpMenu by remember { mutableStateOf(false) }
    var tzExpanded by remember { mutableStateOf(false) }
    var addressText by remember { mutableStateOf("") }
    var addressFocused by remember { mutableStateOf(false) }
    LaunchedEffect(activeTab?.id, activeTab?.url, addressFocused) {
        if (!addressFocused) {
            addressText = if (activeTab == null || activeTab.isHome) {
                ""
            } else {
                // Host-only display while unfocused, the
                // normal shell's rule (v0.191.0): the full
                // URL in this narrow pill used to scroll to
                // its own tail and show a path fragment
                // ("/inni…") instead of the site's name.
                displayHostFor(activeTab.url)
            }
        }
    }
    // Focus gain swaps the host-only display back to the
    // full URL for editing, the normal shell's behaviour
    // (v0.191.0); focus loss restores it via the effect above.
    LaunchedEffect(addressFocused) {
        if (addressFocused) {
            val tab = activeTab
            if (tab != null && !tab.isHome) {
                addressText = tab.url
            }
        }
    }
    // The fingerprint-conflict rule (v0.188.0): an item that
    // would change a parameter the profile is disguising (or
    // reach outside the mode's own bookkeeping) is shown
    // greyed but stays tappable; the tap explains, in plain
    // words, that running it could leak the fingerprint. The
    // action itself never runs in this mode.
    fun fpConflictToast(reason: String) {
        android.widget.Toast.makeText(
            context,
            "与指纹模式冲突：$reason，可能导致指纹泄漏",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }

    fun shareFpPage() {
        val tab = activeTab
        if (tab == null || tab.isHome) {
            android.widget.Toast.makeText(
                context, "先打开一个网页再分享",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        try {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_SEND
            )
                .setType("text/plain")
                .putExtra(
                    android.content.Intent.EXTRA_SUBJECT,
                    tab.title.ifBlank { tab.url }
                )
                .putExtra(android.content.Intent.EXTRA_TEXT, tab.url)
            context.startActivity(
                android.content.Intent.createChooser(intent, "分享当前页")
            )
        } catch (t: Throwable) {
            android.widget.Toast.makeText(
                context, "没有能分享的应用",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun submitAddress() {
        val tab = activeTab ?: return
        val text = addressText.trim()
        if (text.isEmpty()) return
        if (hasBlockedPageScheme(text)) {
            android.widget.Toast.makeText(
                context, "这个链接打不开：只支持 http/https 开头的网址",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        tab.load(resolveInput(text, searchEngine))
    }
    Scaffold(
        bottomBar = {
            // Bottom bar (v0.187.0, three-shell unification):
            // the same surfaceContainer bar body (ToolbarSpec)
            // with evenly spread 48dp targets as the normal
            // shell; the
            // fingerprint set is the mode's own high-frequency
            // actions — back / forward / reload / profile.
            Surface(
                // Bar zone (v0.193.0): same construction
                // as the normal shell — the navigation-bar
                // inset is padded OUTSIDE the 48dp body, the
                // row fills the body only, and this surface
                // is the shell's ONE navigation-inset
                // consumer. (v0.191.0's fold-the-inset-in
                // height sank the icons toward the gesture
                // bar; see the normal shell's note.)
                modifier = Modifier
                    .windowInsetsPadding(
                        WindowInsets.navigationBars.only(
                            WindowInsetsSides.Bottom
                        )
                    )
                    .height(ToolbarSpec.BOTTOM_BAR_HEIGHT_DP.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activeTab?.goBack() },
                        enabled = activeTab?.canGoBack == true
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "后退",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            )
                        )
                    }
                    IconButton(
                        onClick = { activeTab?.goForward() },
                        enabled = activeTab?.canGoForward == true
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "前进",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            )
                        )
                    }
                    IconButton(onClick = {
                        if (activeTab?.loading == true) {
                            activeTab.stopLoading()
                        } else {
                            activeTab?.reload()
                        }
                    }) {
                        Icon(
                            if (activeTab?.loading == true) {
                                Icons.Filled.Close
                            } else {
                                Icons.Filled.Refresh
                            },
                            contentDescription =
                                if (activeTab?.loading == true) {
                                    "停止加载"
                                } else {
                                    "刷新"
                                },
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            )
                        )
                    }
                    IconButton(onClick = { showPicker = true }) {
                        Icon(
                            Icons.Filled.Fingerprint,
                            contentDescription = "切换档案",
                            modifier = Modifier.size(
                                ToolbarSpec.BOTTOM_ICON_DP.dp
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Identity strip (v0.147.0's mode tint, kept as
            // the mode's allowed difference in the unified
            // layout): the tertiary colour lives on this slim
            // strip now, so the toolbar row below can share
            // the normal shell's exact geometry and surface.
            // Height is exactly 48dp (v0.191.0): the strip
            // used to add its own status-bar inset padding on
            // top of the 48dp, but the Scaffold's content
            // padding already carries that inset, so the
            // strip rendered at 48 + inset with the title
            // sunk to its bottom edge and a blank band above
            // it (user's screenshot 4118). One inset, one
            // owner: the Scaffold's.
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onTertiaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "指纹模式",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onExit) { Text("退出") }
                }
            }
            // Unified toolbar row (v0.187.0): the same
            // order and numbers as the normal shell — home,
            // pill, engine switch, new tab, tab count, menu —
            // on a 56dp row with 48dp targets and the pill at
            // weight(1f). The mode's allowed differences: the
            // engine-switch seat is present but greyed (the
            // mode is WebView-only; tapping it explains the
            // conflict instead of switching), and the pill's
            // leading icon is display-only (the site-info
            // sheet is a normal-shell surface). Back / forward
            // / reload live on the bottom bar below and in the
            // menu, the same seats as the normal shell.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ToolbarSpec.ROW_HEIGHT_DP.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { activeTab?.load(HOME_URL) },
                    modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                ) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "指纹模式主页"
                    )
                }
                // The shared pill (v0.191.0), same object as
                // the normal shell's — see PillTextField.kt
                // for why the stock filled TextField had to
                // go (its text line sank and clipped inside
                // the pinned 48dp height).
                PillTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            addressFocused = it.isFocused
                        },
                    placeholder = "搜索或输入网址",
                    leadingIcon = {
                        Box(
                            modifier = Modifier.size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                activeTab == null ||
                                    activeTab.isHome ->
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = null
                                    )
                                activeTab.url.startsWith(
                                    "https://"
                                ) ->
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null
                                    )
                                else ->
                                    Icon(
                                        Icons.Filled.Warning,
                                        contentDescription = null
                                    )
                            }
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .expandedTouchTarget(32.dp, 48.dp)
                        ) {
                            Icon(
                                if (isBookmarked) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Filled.StarBorder
                                },
                                contentDescription =
                                    if (isBookmarked) {
                                        "已加入书签，点按取消书签"
                                    } else {
                                        "加入书签"
                                    }
                            )
                        }
                    },
                    // Same input spec as the normal shell's
                    // address bar (v0.138.0): a URL keyboard
                    // with autocorrect off.
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Go
                    ),
                    keyboardActions = KeyboardActions(
                        onGo = { submitAddress() }
                    )
                )

                // Engine switch, greyed but tappable
                // (v0.187.0, the fingerprint-conflict rule):
                // the seat stays in the unified row, the action
                // does not run — tapping explains that
                // switching engines would break the profile's
                // disguise and could leak the fingerprint.
                IconButton(
                    onClick = {
                        fpConflictToast(
                            "指纹模式只用系统 WebView，切换内核会让伪装失效"
                        )
                    },
                    modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                ) {
                    Icon(
                        Icons.Filled.SwapHoriz,
                        contentDescription =
                            "切换内核（指纹模式下不可用）",
                        tint = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.38f)
                    )
                }
                IconButton(
                    onClick = onNewTab,
                    modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "新建标签页"
                    )
                }
                TabCountButton(
                    count = tabs.size,
                    onClick = { showTabs = true }
                )

                IconButton(
                    onClick = { showFpMenu = true },
                    modifier = Modifier.expandedTouchTarget(40.dp, 48.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "菜单"
                    )
                }
            }
            // The always-on profile strip (v0.147.0): region,
            // city, device and language stay visible the whole
            // time — in fingerprint work, forgetting which profile
            // you are wearing is the expensive mistake. A plain
            // secondary-container bar, not the normal shell's
            // chip-on-background look.
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "${profile.regionLabel} · ${zone.city} · " +
                            "${device.deviceModel} · ${resolved.language}",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    TextButton(onClick = { showPicker = true }) {
                        Text("切换档案")
                    }
                }
            }
            if (activeTab?.loading == true) {
                LinearProgressIndicator(
                    progress = {
                        (activeTab.progress / 100f)
                            .coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            val tab = activeTab
            when {
                tab == null || tab.isHome -> {
                    FingerprintHome(
                        resolved = resolved,
                        searchEngine = searchEngine,
                        onOpenPicker = { showPicker = true },
                        onSearch = { text ->
                            activeTab?.load(resolveInput(text, searchEngine))
                        },
                        onQuickRegion = { candidate ->
                            onProfileChange(
                                candidate.regionId,
                                candidate.timezones.first().id,
                                candidate.devices.first().id,
                                candidate.language
                            )
                        },
                        protection = protection,
                        onProtectionChange = onProtectionChange,
                        proxyConfig = proxyConfig,
                        onProxyChange = onProxyChange,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                // Engine could not be created at all (v0.172.0):
                // the shell used to render the blank placeholder
                // TabContent returns in this state, forever. The
                // fallback page now shows here too — without the
                // switch-engine button (the mode is WebView-only)
                // and with a note saying so; retry reloads the
                // address, which clears the flag and retries
                // creation through the usual obtain path.
                tab.engineUnavailable -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        EngineUnavailableContent(
                            engine = tab.engine,
                            url = tab.url,
                            onSwitchEngine = {},
                            onRetry = { tab.load(tab.url) },
                            onGoHome = { tab.load(HOME_URL) },
                            switchUnavailableNote =
                                "指纹模式只用系统 WebView，没有另一个内核可换。"
                        )
                    }
                }
                // Load failure (v0.172.0): until this version the
                // shell ignored loadError entirely — a failed
                // fingerprint page sat on a blank WebView with no
                // reload, no home and no explanation. The error
                // page shows here in its no-switch form: the mode
                // has no other engine, the button is hidden and
                // the note says where a switch IS possible
                // instead of silently opening a normal tab.
                tab.loadError != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LoadErrorContent(
                            error = tab.loadError!!,
                            onReload = { tab.reload() },
                            onGoHome = { tab.load(HOME_URL) },
                            switchUnavailableNote =
                                "指纹模式只用系统 WebView，不能换火狐内核打开；" +
                                    "退出指纹模式后，可以在普通标签里换内核打开这个网址。",
                            showHttpFallback = tab.httpsUpgraded &&
                                tab.loadError!!.failedUrl
                                    .startsWith("https://"),
                            onOpenHttpFallback = {
                                tab.load(
                                    downgradeToHttp(
                                        tab.loadError!!.failedUrl
                                    )
                                )
                            }
                        )
                    }
                }
                else -> {
                    // Pull-to-refresh (v0.178.0): the fingerprint
                    // shell gains the normal shell's gesture — the
                    // official PullToRefreshBox with its official
                    // indicator, threshold and rebound; refreshing
                    // bound to the tab's own loading progress via
                    // a derived boolean (the v0.171.0 discipline),
                    // so the indicator folds away by itself when
                    // the load ends. The box only engages after
                    // the page itself declines to scroll up (the
                    // nested-scroll contract), i.e. at the top.
                    // Home / error / engine-unavailable above are
                    // app pages and stay unwrapped — the same
                    // gating as the normal shell.
                    val fpRefreshActive by remember(tab.id) {
                        derivedStateOf { tab.progress < 100 }
                    }
                    // Layout mirrors the normal shell exactly
                    // (v0.186.0): the pull-to-refresh box wraps
                    // ONLY the page content, and the edge-swipe
                    // strips are a sibling overlay in the same
                    // outer box — never children of the refresh
                    // box. The pre-0.186 fingerprint arrangement
                    // nested the (then full-screen) gesture layer
                    // inside the refresh box on top of the
                    // WebView, and fingerprint pages would not
                    // scroll on the real device.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        PullToRefreshBox(
                            isRefreshing = fpRefreshActive,
                            onRefresh = { tab.reload() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            key(tab.id) {
                                TabContent(
                                    tab = tab,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        // Edge-swipe navigation: same strips,
                        // same geometry as the normal shell.
                        EdgeSwipeNavigation(
                            onBack = { tab.goBack() },
                            onForward = { tab.goForward() }
                        )
                    }
                }
            }
        }
    }
    // ---- Fingerprint menu: the same floating panel
    // (v0.188.0) ---- Items that would fight the profile are
    // present but greyed and tappable (the conflict rule):
    // tapping one explains the leak risk and nothing runs.
    // Items the mode genuinely has work as usual; items the
    // mode does not have at all (site-info sheet, find bar,
    // reader mode) are not listed — no fake entries.
    FloatingMenuPanel(
        visible = showFpMenu,
        onDismiss = { showFpMenu = false },
        topOffsetDp = 156
    ) {
        MenuPanelCircleRow {
            FilledTonalIconButton(
                onClick = { showFpMenu = false; activeTab?.goBack() },
                enabled = activeTab?.canGoBack == true
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "后退"
                )
            }
            FilledTonalIconButton(
                onClick = {
                    showFpMenu = false; activeTab?.goForward()
                },
                enabled = activeTab?.canGoForward == true
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "前进"
                )
            }
            FilledTonalIconButton(
                onClick = { showFpMenu = false; onToggleBookmark() }
            ) {
                Icon(
                    if (isBookmarked) {
                        Icons.Filled.Star
                    } else {
                        Icons.Filled.StarBorder
                    },
                    contentDescription =
                        if (isBookmarked) {
                            "已加入书签，点按取消书签"
                        } else {
                            "加入书签"
                        }
                )
            }
            FilledTonalIconButton(
                onClick = { showFpMenu = false; onOpenDownloads() }
            ) {
                Icon(
                    Icons.Filled.Download,
                    contentDescription = "下载"
                )
            }
            if (activeTab?.loading == true) {
                FilledTonalIconButton(
                    onClick = {
                        showFpMenu = false; activeTab?.stopLoading()
                    }
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "停止加载"
                    )
                }
            } else {
                FilledTonalIconButton(
                    onClick = { showFpMenu = false; activeTab?.reload() }
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "重新加载"
                    )
                }
            }
        }
        MenuPanelDivider()
        MenuPanelItem(
            icon = Icons.Filled.Add,
            label = "新建指纹标签页",
            onClick = { showFpMenu = false; onNewTab() }
        )
        MenuPanelItem(
            icon = Icons.Filled.VisibilityOff,
            label = "新建无痕标签页",
            conflict = true,
            onClick = {
                showFpMenu = false
                fpConflictToast(
                    "无痕标签走普通模式的口径，会和指纹档案混杂"
                )
            }
        )
        MenuPanelItem(
            icon = Icons.Filled.History,
            label = "历史记录",
            onClick = { showFpMenu = false; onOpenHistory() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Delete,
            label = "删除浏览资料",
            conflict = true,
            onClick = {
                showFpMenu = false
                fpConflictToast(
                    "这项会连普通模式的浏览资料一起清，模式里不能单独清"
                )
            }
        )
        MenuPanelDivider()
        MenuPanelItem(
            icon = Icons.Filled.Bookmark,
            label = "书签",
            onClick = { showFpMenu = false; onOpenBookmarks() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Share,
            label = "分享当前页",
            onClick = { showFpMenu = false; shareFpPage() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Language,
            label = "桌面版网站",
            supporting = "会改掉档案的手机参数",
            conflict = true,
            trailing = {
                Checkbox(
                    checked = false,
                    onCheckedChange = {
                        showFpMenu = false
                        fpConflictToast(
                            "桌面版网站会改用户代理和页面宽度，和档案的机型对不上"
                        )
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "桌面版网站（指纹模式下不可用）"
                    }
                )
            },
            onClick = {
                showFpMenu = false
                fpConflictToast(
                    "桌面版网站会改用户代理和页面宽度，和档案的机型对不上"
                )
            }
        )
        MenuPanelDivider()
        MenuPanelItem(
            icon = Icons.Filled.SwapHoriz,
            label = "切换内核",
            conflict = true,
            onClick = {
                showFpMenu = false
                fpConflictToast(
                    "指纹模式只用系统 WebView，切换内核会让伪装失效"
                )
            }
        )
        MenuPanelItem(
            icon = Icons.Filled.Fingerprint,
            label = "切换档案",
            onClick = { showFpMenu = false; showPicker = true }
        )
        MenuPanelItem(
            icon = Icons.Filled.Settings,
            label = "设置",
            onClick = { showFpMenu = false; onOpenSettings() }
        )
        MenuPanelItem(
            icon = Icons.Filled.Close,
            label = "退出指纹模式",
            onClick = { showFpMenu = false; onExit() }
        )
    }
    if (showTabs) {
        ModalBottomSheet(onDismissRequest = { showTabs = false }) {
            var fpTabQuery by remember { mutableStateOf("") }
            var fpSheetMenu by remember { mutableStateOf(false) }
            val visibleFpTabs = if (fpTabQuery.isBlank()) {
                tabs
            } else {
                tabs.filter {
                    it.title.contains(fpTabQuery, ignoreCase = true) ||
                        it.url.contains(fpTabQuery, ignoreCase = true)
                }
            }
            // Same grid language as the normal tab sheet
            // (v0.190.0 three-shell unification): 24dp cards,
            // glyph head, pill search header. Fingerprint tabs
            // never show thumbnails — the body is a labelled
            // placeholder, per the mode's privacy rule.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            onClick = {
                                showTabs = false; onNewTab()
                            },
                            colors = IconButtonDefaults
                                .filledIconButtonColors(
                                    containerColor =
                                        MaterialTheme.colorScheme
                                            .tertiaryContainer,
                                    contentColor =
                                        MaterialTheme.colorScheme
                                            .onTertiaryContainer
                                )
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "新增指纹分页"
                            )
                        }
                        PillTextField(
                            value = fpTabQuery,
                            onValueChange = { fpTabQuery = it },
                            modifier = Modifier.weight(1f),
                            placeholder = "搜寻分页",
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                imeAction = ImeAction.Search
                            )
                        )

                        TabCountButton(
                            count = tabs.size,
                            onClick = {}
                        )
                        Box {
                            IconButton(
                                onClick = { fpSheetMenu = true }
                            ) {
                                Icon(
                                    Icons.Filled.MoreVert,
                                    contentDescription = "更多选项"
                                )
                            }
                            MaterialTheme(
                                shapes = MaterialTheme.shapes.copy(
                                    extraSmall =
                                        RoundedCornerShape(16.dp)
                                )
                            ) {
                                DropdownMenu(
                                    expanded = fpSheetMenu,
                                    onDismissRequest = {
                                        fpSheetMenu = false
                                    },
                                    offset = DpOffset(0.dp, 8.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("新增指纹分页") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Add, null)
                                        },
                                        onClick = {
                                            fpSheetMenu = false
                                            showTabs = false
                                            onNewTab()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("新增无痕分页") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled
                                                    .VisibilityOff,
                                                null
                                            )
                                        },
                                        onClick = {
                                            fpSheetMenu = false
                                            fpConflictToast(
                                                "无痕标签走普通模式口径，" +
                                                    "会和指纹档案混杂"
                                            )
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor =
                                                MaterialTheme.colorScheme
                                                    .onSurface.copy(
                                                        alpha = 0.38f
                                                    ),
                                            leadingIconColor =
                                                MaterialTheme.colorScheme
                                                    .onSurface.copy(
                                                        alpha = 0.38f
                                                    )
                                        )
                                    )
                                    DropdownMenuItem(
                                        text = { Text("关闭所有分页") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Close,
                                                null
                                            )
                                        },
                                        onClick = {
                                            fpSheetMenu = false
                                            showTabs = false
                                            onCloseAllTabs()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("删除浏览资料") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Delete,
                                                null
                                            )
                                        },
                                        onClick = {
                                            fpSheetMenu = false
                                            fpConflictToast(
                                                "这项会连普通模式的" +
                                                    "浏览资料一起清，" +
                                                    "模式里不能单独清"
                                            )
                                        },
                                        colors = MenuDefaults.itemColors(
                                            textColor =
                                                MaterialTheme.colorScheme
                                                    .onSurface.copy(
                                                        alpha = 0.38f
                                                    ),
                                            leadingIconColor =
                                                MaterialTheme.colorScheme
                                                    .onSurface.copy(
                                                        alpha = 0.38f
                                                    )
                                        )
                                    )
                                    DropdownMenuItem(
                                        text = { Text("设定") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Filled.Settings,
                                                null
                                            )
                                        },
                                        onClick = {
                                            fpSheetMenu = false
                                            showTabs = false
                                            onOpenSettings()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                if (visibleFpTabs.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        EmptyState(
                            Icons.Filled.Search,
                            "没有找到相关的标签页。"
                        )
                    }
                }
                gridItems(visibleFpTabs, key = { it.id }) { tab ->
                    val isActiveFp = tab.id == activeTab?.id
                    ElevatedCard(
                        onClick = {
                            showTabs = false
                            onSelectTab(tab)
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isActiveFp) {
                                    Modifier.border(
                                        2.dp,
                                        MaterialTheme.colorScheme
                                            .tertiary,
                                        RoundedCornerShape(24.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment =
                                    Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            MaterialTheme.colorScheme
                                                .tertiaryContainer,
                                            CircleShape
                                        ),
                                    contentAlignment =
                                        Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme
                                            .onTertiaryContainer
                                    )
                                }
                                Text(
                                    if (tab.isHome) {
                                        "指纹模式首页"
                                    } else {
                                        tab.title.ifBlank {
                                            "新标签页"
                                        }
                                    },
                                    style = MaterialTheme.typography
                                        .titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { onCloseTab(tab) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "关闭标签页"
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                                    .padding(vertical = 8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        MaterialTheme.colorScheme
                                            .surfaceContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.VisibilityOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme
                                            .onSurfaceVariant,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .padding(bottom = 4.dp)
                                    )
                                    Text(
                                        "指纹标签不显示预览",
                                        style = MaterialTheme.typography
                                            .bodySmall,
                                        color = MaterialTheme.colorScheme
                                            .onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                if (tab.isHome) {
                                    "指纹模式首页"
                                } else {
                                    hostOf(tab.url) ?: tab.url
                                },
                                style = MaterialTheme.typography
                                    .bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isActiveFp) {
                                Text(
                                    "当前标签",
                                    style = MaterialTheme.typography
                                        .labelMedium,
                                    color = MaterialTheme.colorScheme
                                        .tertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        ModalBottomSheet(onDismissRequest = { showPicker = false }) {
            LazyColumn(
                // Height cap (v0.169.0, same alignment as the
                // fingerprint tab sheet above): the picker is the
                // longest sheet in the app (region/device/timezone/
                // language sections) and was uncapped.
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 640.dp),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "选择地区",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(FingerprintProfiles.all) { candidate ->
                    ElevatedCard(
                        onClick = {
                            onProfileChange(
                                candidate.regionId,
                                candidate.timezones.first().id,
                                candidate.devices.first().id,
                                candidate.language
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(candidate.regionLabel)
                            },
                            supportingContent = {
                                Text(
                                    "${candidate.devices.first().deviceModel} · " +
                                        candidate.language
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = candidate.regionId ==
                                        profile.regionId,
                                    onClick = null
                                )
                            }
                        )
                    }
                }
                item {
                    Text(
                        "机型",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(profile.devices) { candidate ->
                    ElevatedCard(
                        onClick = {
                            onProfileChange(
                                profile.regionId, zone.id,
                                candidate.id, resolved.language
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(candidate.deviceModel)
                            },
                            supportingContent = {
                                Text(
                                    "${candidate.webGlRenderer} · " +
                                        "${candidate.screenWidth}×" +
                                        candidate.screenHeight.toString(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            trailingContent = {
                                RadioButton(
                                    selected = candidate.id == device.id,
                                    onClick = null
                                )
                            }
                        )
                    }
                }
                item {
                    Text(
                        "时区",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    ListItem(
                        headlineContent = {
                            Text("${zone.label} · ${zone.city}")
                        },
                        supportingContent = { Text(zone.id) },
                        trailingContent = {
                            IconButton(onClick = {
                                tzExpanded = !tzExpanded
                            }) {
                                Icon(
                                    if (tzExpanded) {
                                        Icons.Filled.ExpandLess
                                    } else {
                                        Icons.Filled.ExpandMore
                                    },
                                    contentDescription =
                                        if (tzExpanded) {
                                            "收起时区列表"
                                        } else {
                                            "展开时区列表"
                                        }
                                )
                            }
                        }
                    )
                }
                if (tzExpanded) {
                    item {
                        // Expanded-area divider (v0.191.0):
                        // the deeper group line marks where
                        // the expanded list begins.
                        HorizontalDivider(
                            thickness = 1.dp,
                            color =
                                MaterialTheme.colorScheme.outline
                        )
                    }
                    items(profile.timezones) { candidate ->
                        ElevatedCard(
                            onClick = {
                                onProfileChange(
                                    profile.regionId, candidate.id,
                                    device.id, resolved.language
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = {
                                    Text(
                                        "${candidate.label} · " +
                                            candidate.city
                                    )
                                },
                                supportingContent = {
                                    Text(candidate.id)
                                },
                                trailingContent = {
                                    RadioButton(
                                        selected = candidate.id == zone.id,
                                        onClick = null
                                    )
                                }
                            )
                        }
                    }
                }
                if (profile.languageOptions.size > 1) {
                    item {
                        Text(
                            "语言",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    items(profile.languageOptions) { candidate ->
                        ElevatedCard(
                            onClick = {
                                onProfileChange(
                                    profile.regionId, zone.id,
                                    device.id, candidate
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineContent = { Text(candidate) },
                                trailingContent = {
                                    RadioButton(
                                        selected = candidate ==
                                            resolved.language,
                                        onClick = null
                                    )
                                }
                            )
                        }
                    }
                }
                item {
                    Text(
                        "跟时区联动",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp)
                        ) {
                            Text("定位：${zone.city}（${zone.latitude}，${zone.longitude}）")
                            Text("语言：${resolved.language}")
                            Text("日期格式：${profile.dateFormatSample}")
                            Text("设备：${device.deviceModel}")
                        }
                    }
                }
                item {
                    Text(
                        "切换地区、机型、时区或语言后，已打开的指纹标签会按新档案重开当前网页。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                item {
                    Text(
                        "我的档案",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                item {
                    OutlinedButton(
                        onClick = {
                            val entry =
                                com.tuxunbrowse.android.engine
                                    .CustomFingerprint(
                                        regionId = profile.regionId,
                                        deviceId = device.id,
                                        timezoneId = zone.id,
                                        language = resolved.language,
                                        label = "${profile.regionLabel} · " +
                                            "${device.deviceModel} · " +
                                            zone.city
                                    )
                            if (customs.none {
                                    it.regionId == entry.regionId &&
                                        it.deviceId == entry.deviceId &&
                                        it.timezoneId == entry.timezoneId &&
                                        it.language == entry.language
                                }
                            ) {
                                customs = customs + entry
                                onSaveCustomProfile(entry)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("保存当前组合")
                    }
                }
                item {
                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportCustomsLauncher.launch(
                                    "tuxunbrowse-fingerprint-profiles.json"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("导出我的档案")
                        }
                        OutlinedButton(
                            onClick = {
                                importCustomsLauncher.launch(
                                    arrayOf("application/json")
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("导入档案")
                        }
                    }
                }
                items(customs) { entry ->
                    ElevatedCard(
                        onClick = {
                            onProfileChange(
                                entry.regionId, entry.timezoneId,
                                entry.deviceId, entry.language
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    entry.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            supportingContent = {
                                Text("点一下切换到这个组合")
                            },
                            trailingContent = {
                                IconButton(onClick = {
                                    customs = customs - entry
                                    onDeleteCustomProfile(entry)
                                }) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "删除这条档案"
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

/**
 * One exit-IP lookup answer (v0.147.0): what the network side of
 * the story currently looks like, to set against the profile.
 */
private data class IpCheckResult(
    val ip: String,
    val countryCode: String,
    val city: String,
    val timezone: String,
    /** Which geo service answered (v0.155.0) — shown on the
     * card so a dead source is diagnosable at a glance. */
    val source: String
)

/**
 * Ask a geo-IP service where this device's traffic currently
 * exits (v0.147.0; fallback source v0.154.0). One plain HTTPS
 * GET, no account, no key; null only when every source fails or
 * answers without a country. This is the ONE network call
 * fingerprint mode makes about itself, and it only runs when
 * the user taps the self-check button — the home says so,
 * including which services may be asked.
 */
private suspend fun fetchExitIp(): IpCheckResult? {
    fetchExitIpFrom("https://ipapi.co/json/", "ipapi.co")
        ?.let { return it }
    // ipapi.co is not reachable from every network the app runs
    // on; ip.sb's free geo endpoint answers the same four fields
    // under the same names. A source that answers without a
    // country counts as failed — no verdict is ever guessed.
    return fetchExitIpFrom("https://api.ip.sb/geoip", "api.ip.sb")
}

private suspend fun fetchExitIpFrom(
    url: String,
    source: String
): IpCheckResult? =
    withContext(Dispatchers.IO) {
        var conn: java.net.HttpURLConnection? = null
        try {
            conn = java.net.URL(url)
                .openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("User-Agent", "TuxunBrowse")
            if (conn.responseCode != 200) return@withContext null
            val body = conn.inputStream.bufferedReader().readText()
            val obj = org.json.JSONObject(body)
            val country = obj.optString("country_code")
            if (country.isBlank()) return@withContext null
            IpCheckResult(
                ip = obj.optString("ip"),
                countryCode = country,
                city = obj.optString("city"),
                timezone = obj.optString("timezone"),
                source = source
            )
        } catch (t: Throwable) {
            null
        } finally {
            conn?.disconnect()
        }
    }

/**
 * Fingerprint mode's home (v0.144.0, identity pass v0.147.0):
 * deliberately NOT the normal home's layout — an identity header
 * in the mode's own tint carrying the active profile, then the
 * search box, then the exit-IP self-check, then one-tap region
 * switching. No bookmarks, no history — the mode keeps nothing
 * of either.
 */
/**
 * The self-check probe (v0.184.0): runs INSIDE the throwaway
 * WebView after the current profile's payload has been
 * evaluated into it, compares what a page would read with
 * what the profile claims ([expJson]), and reports the rows
 * through the FpSelfCheck bridge. ok is "ok" / "bad" / "na"
 * (the surface does not exist in this environment at all —
 * shown as 无法检测, never dressed up as a pass).
 */
private fun fpSelfCheckProbe(expJson: String): String = """
(function () {
  var EXP = $expJson;
  var rows = [];
  function row(name, ok, detail) {
    rows.push({ name: name, ok: ok, detail: detail || '' });
  }
  function hashOf(arr) {
    var h = 0;
    for (var i = 0; i < arr.length; i++) {
      h = (h * 31 + arr[i]) >>> 0;
    }
    return h;
  }
  try {
    row('语言', navigator.language === EXP.language
      ? 'ok' : 'bad', String(navigator.language));
  } catch (e) { row('语言', 'bad', '读取失败'); }
  try {
    // The full fallback chain (v0.194.0): a real-device report
    // once showed a lone 'ja-JP' here — the chain behind the
    // primary language is part of the claim, so the probe
    // compares the whole list, not just its head.
    var langs = (navigator.languages || []).join(',');
    row('语言列表', langs === EXP.languages ? 'ok' : 'bad',
      langs);
  } catch (e) { row('语言列表', 'bad', '读取失败'); }
  // The request-header language (v0.201.0): Accept-Language is
  // the one language surface a local probe CANNOT read — a page
  // only ever sees navigator.*, never its own request headers.
  // The row exists so the gap is stated where a user checks,
  // not hidden: it is permanently 'na' (无法检测), never a
  // dressed-up pass, and the detail says what a site actually
  // receives. This is the known structural item the mode's
  // home page also discloses.
  row('请求头语言', 'na',
    '本地测不了请求头：网站收到的 Accept-Language 仍是' +
    '系统语言，可能与上面的页面语言不一致（已知结构性项）');
  try {
    // Network story (v0.201.0): the payload pins a healthy 4g
    // link on the real NetworkInformation prototype; compare
    // all four fields the profile claims.
    var conn = navigator.connection;
    if (conn) {
      var connOk = conn.effectiveType === '4g' &&
        conn.rtt === EXP.connRtt &&
        conn.downlink === EXP.connDownlink &&
        conn.saveData === false;
      row('网络信息', connOk ? 'ok' : 'bad',
        'effectiveType=' + conn.effectiveType +
        '，rtt=' + conn.rtt + '，downlink=' + conn.downlink +
        '，saveData=' + conn.saveData);
    } else {
      row('网络信息', 'na', '无 navigator.connection');
    }
  } catch (e) { row('网络信息', 'bad', '读取失败'); }
  try {
    var tz = Intl.DateTimeFormat().resolvedOptions().timeZone;
    row('时区', tz === EXP.tz ? 'ok' : 'bad', String(tz));
  } catch (e) { row('时区', 'bad', '读取失败'); }
  try {
    row('机型（UA）',
      navigator.userAgent.indexOf(EXP.uaModel) >= 0
        ? 'ok' : 'bad', EXP.uaModel);
  } catch (e) { row('机型（UA）', 'bad', '读取失败'); }
  try {
    row('平台标识', navigator.platform === EXP.platform
      ? 'ok' : 'bad', String(navigator.platform));
  } catch (e) { row('平台标识', 'bad', '读取失败'); }
  try {
    var land = !!(screen.orientation &&
      String(screen.orientation.type)
        .indexOf('landscape') === 0);
    var ew = land ? EXP.scrH : EXP.scrW;
    var eh = land ? EXP.scrW : EXP.scrH;
    var scrOk = screen.width === ew &&
      screen.height === eh &&
      screen.availWidth <= screen.width &&
      screen.availHeight <= screen.height;
    row('屏幕尺寸', scrOk ? 'ok' : 'bad',
      screen.width + '×' + screen.height +
      (land ? '（横屏）' : '（竖屏）'));
  } catch (e) { row('屏幕尺寸', 'bad', '读取失败'); }
  try {
    var c = document.createElement('canvas');
    var gl = c.getContext('webgl') ||
      c.getContext('experimental-webgl');
    if (gl) {
      var ext = gl.getExtension('WEBGL_debug_renderer_info');
      var v = ext
        ? gl.getParameter(ext.UNMASKED_VENDOR_WEBGL)
        : gl.getParameter(gl.VENDOR);
      var r = ext
        ? gl.getParameter(ext.UNMASKED_RENDERER_WEBGL)
        : gl.getParameter(gl.RENDERER);
      row('WebGL 厂商', EXP.webglOff ? 'off'
          : (v === EXP.glVendor ? 'ok' : 'bad'),
        EXP.webglOff ? '已在保护选项里关闭' : String(v));
      row('WebGL 型号', EXP.webglOff ? 'off'
          : (r === EXP.glRenderer ? 'ok' : 'bad'),
        EXP.webglOff ? '已在保护选项里关闭' : String(r));
    } else {
      row('WebGL 厂商', 'na', 'WebGL 不可用');
      row('WebGL 型号', 'na', 'WebGL 不可用');
    }
  } catch (e) { row('WebGL 厂商', 'bad', '读取失败'); }
  try {
    var fIn = document.fonts.check(
      '16px "' + EXP.fontIn + '"');
    var fOut = document.fonts.check(
      '16px "' + EXP.fontOut + '"');
    row('字体清单', EXP.fontOff ? 'off'
        : (fIn === true && fOut === false ? 'ok' : 'bad'),
      EXP.fontOff ? '已在保护选项里关闭'
        : (EXP.fontIn + ' 应在架、' + EXP.fontOut +
          ' 应不在架'));
  } catch (e) { row('字体清单', 'bad', '读取失败'); }
  try {
    var cv = document.createElement('canvas');
    cv.width = 64; cv.height = 16;
    var cx = cv.getContext('2d');
    cx.fillStyle = '#f60'; cx.fillRect(0, 0, 64, 16);
    cx.font = '14px sans-serif'; cx.fillStyle = '#069';
    cx.fillText('fp-check', 2, 12);
    var h1 = hashOf(cx.getImageData(0, 0, 64, 16).data);
    var h2 = hashOf(cx.getImageData(0, 0, 64, 16).data);
    row('画布稳定性', h1 === h2 ? 'ok' : 'bad',
      h1 === h2 ? '两次读取一致' : '两次读取不一致');
  } catch (e) { row('画布稳定性', 'bad', '读取失败'); }
  try {
    // The pair must agree with each other AND with the value
    // the persona claims (v0.194.0): every profile here is an
    // Android 14 device, where Chrome's inline PDF rollout
    // never landed as a default — false with an empty plugin
    // list is the truthful pair for the claimed device, and a
    // build claiming true/5 here would contradict its own
    // Android version.
    var pair =
      (navigator.pdfViewerEnabled === false &&
        navigator.plugins.length === 0) ||
      (navigator.pdfViewerEnabled === true &&
        navigator.plugins.length === 5);
    var claimed =
      navigator.pdfViewerEnabled === EXP.pdfViewer;
    row('PDF 查看器一致性', pair && claimed ? 'ok' : 'bad',
      'pdfViewerEnabled=' + navigator.pdfViewerEnabled +
      '，plugins ' + navigator.plugins.length +
      ' 项（安卓 14 档案应为 false／0 项）');
  } catch (e) { row('PDF 查看器一致性', 'bad', '读取失败'); }
  try {
    row('WebRTC 在场',
      typeof RTCPeerConnection === 'function' ? 'ok' : 'bad',
      typeof RTCPeerConnection);
  } catch (e) { row('WebRTC 在场', 'bad', '读取失败'); }
  var gpuSettled = false;
  var uaSettled = false;
  var mediaSettled = false;
  var voicesSettled = false;
  function maybeReport() {
    if (gpuSettled && uaSettled && mediaSettled &&
        voicesSettled) {
      FpSelfCheck.report(JSON.stringify(rows));
    }
  }
  function finish() { gpuSettled = true; maybeReport(); }
  try {
    // Media devices (v0.201.0): the payload answers
    // enumerateDevices with one mic, one camera, one speaker
    // and empty labels — the shape real Chrome shows without a
    // permission grant, with no host device name inside. Any
    // label text here means the host list leaked through.
    if (navigator.mediaDevices &&
        navigator.mediaDevices.enumerateDevices) {
      var mediaDone = false;
      setTimeout(function () {
        if (mediaDone) return;
        mediaDone = true;
        row('设备列表', 'na', '查询超时');
        mediaSettled = true; maybeReport();
      }, 3000);
      navigator.mediaDevices.enumerateDevices()
        .then(function (devs) {
          if (mediaDone) return;
          mediaDone = true;
          try {
            var kinds = devs.map(function (d) {
              return d.kind;
            }).sort().join(',');
            var labelsClean = devs.every(function (d) {
              return !d.label;
            });
            var mediaOk = devs.length === 3 && labelsClean &&
              kinds === 'audioinput,audiooutput,videoinput';
            row('设备列表', mediaOk ? 'ok' : 'bad',
              devs.length + ' 个设备，标签' +
              (labelsClean ? '全空' : '有文字（疑似真机泄漏）') +
              '：' + kinds);
          } catch (e) { row('设备列表', 'bad', '读取失败'); }
          mediaSettled = true; maybeReport();
        }).catch(function () {
          if (mediaDone) return;
          mediaDone = true;
          row('设备列表', 'na', '查询被拒绝');
          mediaSettled = true; maybeReport();
        });
    } else {
      row('设备列表', 'na', '无 mediaDevices');
      mediaSettled = true;
    }
  } catch (e) {
    row('设备列表', 'bad', '读取失败');
    mediaSettled = true;
  }
  try {
    // Speech voices (v0.201.0): the list must be the PROFILE
    // region's TTS set, in count and in languages — a host
    // engine's voices (or an empty list where the payload
    // claims several) both read as 'bad'. getVoices can lag
    // behind voiceschanged, so an empty first read waits for
    // the event before judging.
    if (window.speechSynthesis) {
      var voicesDone = false;
      function judgeVoices() {
        if (voicesDone) return;
        var vs = [];
        try { vs = speechSynthesis.getVoices() || []; }
        catch (e) { vs = []; }
        if (vs.length === 0) return false;
        voicesDone = true;
        try {
          var vlangs = vs.map(function (v) {
            return v.lang;
          }).sort().join(',');
          var vOk = vs.length === EXP.voiceCount &&
            vlangs === EXP.voiceLangs;
          row('语音列表', vOk ? 'ok' : 'bad',
            vs.length + ' 个语音：' + vlangs);
        } catch (e) { row('语音列表', 'bad', '读取失败'); }
        voicesSettled = true; maybeReport();
        return true;
      }
      if (!judgeVoices()) {
        try {
          speechSynthesis.addEventListener(
            'voiceschanged', judgeVoices);
        } catch (e) {}
        setTimeout(function () {
          if (voicesDone) return;
          if (!judgeVoices()) {
            voicesDone = true;
            row('语音列表', 'bad',
              '语音列表为空，与档案声称的 ' + EXP.voiceCount +
              ' 个对不上');
            voicesSettled = true; maybeReport();
          }
        }, 3000);
      }
    } else {
      row('语音列表', 'na', '无 speechSynthesis');
      voicesSettled = true;
    }
  } catch (e) {
    row('语音列表', 'bad', '读取失败');
    voicesSettled = true;
  }
  try {
    if (navigator.userAgentData &&
        navigator.userAgentData
          .getHighEntropyValues) {
      navigator.userAgentData.getHighEntropyValues(
        ['architecture', 'bitness', 'platform']
      ).then(function (v) {
        if (uaSettled) return;
        uaSettled = true;
        row('UA 架构位数',
          (v.architecture === EXP.uaArch &&
            v.bitness === EXP.uaBitness &&
            v.platform === EXP.uaPlatform)
            ? 'ok' : 'bad',
          'architecture=' + v.architecture +
          '，bitness=' + v.bitness +
          '，platform=' + v.platform);
        maybeReport();
      }).catch(function () {
        if (uaSettled) return;
        uaSettled = true;
        row('UA 架构位数', 'na', '查询失败');
        maybeReport();
      });
      setTimeout(function () {
        if (uaSettled) return;
        uaSettled = true;
        row('UA 架构位数', 'na', '查询超时');
        maybeReport();
      }, 3000);
    } else {
      row('UA 架构位数', 'na', '无 userAgentData');
      uaSettled = true;
    }
  } catch (e) {
    row('UA 架构位数', 'bad', '读取失败');
    uaSettled = true;
  }
  if (navigator.gpu && navigator.gpu.requestAdapter) {
    var done = false;
    setTimeout(function () {
      if (!done) {
        done = true;
        row('WebGPU 厂商', 'na', '查询超时');
        finish();
      }
    }, 3000);
    navigator.gpu.requestAdapter().then(function (a) {
      if (done) return;
      done = true;
      try {
        var info = a && a.info;
        row('WebGPU 厂商', EXP.webglOff ? 'off'
            : (info && info.vendor === EXP.gpuVendor
              ? 'ok' : 'bad'),
          EXP.webglOff ? '已在保护选项里关闭'
            : (info ? String(info.vendor) : '无适配器信息'));
      } catch (e) { row('WebGPU 厂商', 'bad', '读取失败'); }
      finish();
    }).catch(function () {
      if (done) return;
      done = true;
      row('WebGPU 厂商', 'na', '没有可用适配器');
      finish();
    });
  } else {
    row('WebGPU 厂商', 'na', '此环境未提供 WebGPU');
    finish();
  }
})();
"""

/**
 * The tab-count box, shared by the tab sheets (v0.190.0):
 * a 24dp rounded-square outline carrying the open-tab count
 * — ToolbarSpec numbers, the same box the shells' toolbars
 * draw inline.
 */
@Composable
private fun TabCountButton(count: Int, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .semantics {
                contentDescription = "标签页，共 $count 个"
            }
            .expandedTouchTarget(40.dp, 48.dp)
    ) {
        Box(
            modifier = Modifier
                .size(ToolbarSpec.TAB_COUNT_BOX_DP.dp)
                .border(
                    2.dp,
                    MaterialTheme.colorScheme.onSurface,
                    RoundedCornerShape(
                        ToolbarSpec.TAB_COUNT_RADIUS_DP.dp
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        }
    }
}

/**
 * The settings group divider (v0.191.0): a thicker,
 * primary-tinted style line between settings groups. The
 * plain hairline was too faint to see where one group ended
 * and the next began (user's report); item-level dividers
 * stay the faint official hairline, and the two weights are
 * the hierarchy — group lines deeper and tinted, item lines
 * light. Visible in dark mode too (it is the primary colour,
 * not a grey).
 */
@Composable
internal fun GroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FingerprintHome(
    resolved: ResolvedFingerprint,
    searchEngine: SearchEngine,
    onOpenPicker: () -> Unit,
    onSearch: (String) -> Unit,
    onQuickRegion: (com.tuxunbrowse.android.engine.FingerprintProfile) -> Unit =
        {},
    protection: FingerprintProtection = FingerprintProtection(),
    onProtectionChange: (FingerprintProtection) -> Unit = {},
    proxyConfig: com.tuxunbrowse.android.engine.FpProxyConfig =
        com.tuxunbrowse.android.engine.FpProxyConfig(),
    onProxyChange: (com.tuxunbrowse.android.engine.FpProxyConfig) -> Unit =
        {},
    modifier: Modifier = Modifier
) {
    val profile = resolved.profile
    val zone = resolved.timezone
    val fpHomeContext = LocalContext.current
    var query by remember { mutableStateOf("") }
    var showProxyDialog by remember { mutableStateOf(false) }
    // Built-in fingerprint self-check (v0.184.0): a local-only
    // probe run. A throwaway WebView loads about:blank, the
    // CURRENT profile's payload is evaluated into it, and a
    // probe script compares what a page would read against
    // what the profile claims — language, time zone, UA model,
    // WebGL vendor/renderer, WebGPU vendor, the font shelf,
    // canvas read stability, the plugins/pdfViewerEnabled
    // agreement and RTCPeerConnection's presence. v0.201.0
    // added the network story, the media-device list, the
    // speech-voice list, and an honestly-unmeasurable row for
    // the Accept-Language request header. Nothing leaves the
    // device: the page is blank, the results land in this
    // state and nowhere else.
    var selfCheckRows by remember {
        mutableStateOf<List<Triple<String, String, String>>?>(null)
    }
    var selfCheckRunning by remember { mutableStateOf(false) }
    var checkWebView by remember { mutableStateOf<WebView?>(null) }
    var checkStarted by remember { mutableStateOf(false) }
    LaunchedEffect(
        profile.regionId, zone.id, resolved.device.id,
        resolved.language, protection
    ) {
        selfCheckRows = null
    }
    DisposableEffect(Unit) {
        onDispose {
            checkWebView?.destroy()
            checkWebView = null
        }
    }
    fun runSelfCheck() {
        if (selfCheckRunning) return
        selfCheckRunning = true
        selfCheckRows = null
        val device = resolved.device
        val fontIn = when {
            "SamsungOne" in device.fonts -> "SamsungOne"
            "Google Sans" in device.fonts -> "Google Sans"
            else -> device.fonts.firstOrNull() ?: ""
        }
        val fontOut = if (fontIn == "SamsungOne") {
            "Google Sans"
        } else {
            "SamsungOne"
        }
        val exp = org.json.JSONObject()
            .put("language", resolved.language)
            .put("languages", resolved.languages.joinToString(","))
            .put("pdfViewer", false)
            .put("tz", zone.id)
            .put("uaModel", device.uaModel)
            .put("glVendor", device.webGlVendor)
            .put("glRenderer", device.webGlRenderer)
            .put("gpuVendor", device.webGpuVendor)
            .put("fontIn", fontIn)
            .put("fontOut", fontOut)
            .put("platform",
                com.tuxunbrowse.android.engine
                    .FingerprintProfiles.PLATFORM)
            .put("uaArch",
                com.tuxunbrowse.android.engine
                    .FingerprintProfiles.UA_ARCHITECTURE)
            .put("uaBitness",
                com.tuxunbrowse.android.engine
                    .FingerprintProfiles.UA_BITNESS)
            .put("uaPlatform",
                com.tuxunbrowse.android.engine
                    .FingerprintProfiles.UA_PLATFORM)
            .put("scrW", device.screenWidth)
            .put("scrH", device.screenHeight)
            .put("webglOff", !protection.webglDisguise)
            .put("fontOff", !protection.fontSet)
            .put("connRtt", profile.connectionRtt)
            .put("connDownlink", profile.connectionDownlink)
            .put("voiceCount", profile.voices.size)
            .put("voiceLangs",
                profile.voices.map { it.lang }.sorted()
                    .joinToString(","))
            .toString()
        val probe = fpSelfCheckProbe(exp)
        val wv = WebView(fpHomeContext)
        wv.settings.javaScriptEnabled = true
        wv.addJavascriptInterface(object {
            @JavascriptInterface
            fun report(json: String) {
                android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post {
                    try {
                        val arr = org.json.JSONArray(json)
                        val rows =
                            mutableListOf<Triple<String, String, String>>()
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            rows += Triple(
                                o.getString("name"),
                                o.getString("ok"),
                                o.optString("detail")
                            )
                        }
                        selfCheckRows = rows
                    } catch (e: Exception) {
                        selfCheckRows = listOf(
                            Triple("自查", "bad", "结果解析失败")
                        )
                    }
                    selfCheckRunning = false
                    checkWebView?.destroy()
                    checkWebView = null
                    checkStarted = false
                }
            }
        }, "FpSelfCheck")
        wv.webViewClient = object : WebViewClient() {
            override fun onPageFinished(
                view: WebView, url: String
            ) {
                view.evaluateJavascript(
                    FingerprintScript.payload(resolved, protection)
                ) {
                    view.evaluateJavascript(probe, null)
                }
            }
        }
        checkStarted = false
        checkWebView = wv
    }
    var ipResult by remember { mutableStateOf<IpCheckResult?>(null) }
    var ipFailed by remember { mutableStateOf(false) }
    var ipLoading by remember { mutableStateOf(false) }
    val ipScope = rememberCoroutineScope()
    // A check belongs to the combination it was run under; a
    // profile or zone change retires the old answer.
    LaunchedEffect(profile.regionId, zone.id) {
        ipResult = null
        ipFailed = false
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.tertiaryContainer,
                contentColor =
                    MaterialTheme.colorScheme.onTertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        contentDescription = null
                    )
                    Text(
                        "当前档案",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text("地区：${profile.regionLabel}")
                Text("设备：${resolved.device.deviceModel}")
                Text("语言：${resolved.language}")
                Text("时区：${zone.label} · ${zone.city}")
                Text("定位：${zone.latitude}，${zone.longitude}")
                Text("日期格式：${profile.dateFormatSample}")
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("搜索或输入网址") },
            // URL keyboard, no autocorrect — same spec as the
            // shell's address field above (v0.164.0).
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go
            ),
            keyboardActions = KeyboardActions(onGo = {
                if (query.isNotBlank()) onSearch(query)
            })
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "保护选项",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "关掉哪一项，哪一项就用回这台手机的真实样子；" +
                        "其余照常按档案伪装。"
                )
                val protectionRows = listOf(
                    Triple(
                        "画布噪声",
                        "给画布读数掺入按档案定的细微噪声",
                        protection.canvasNoise
                    ),
                    Triple(
                        "显卡伪装",
                        "显卡型号、参数和 WebGPU 按档案机型回答",
                        protection.webglDisguise
                    ),
                    Triple(
                        "音频噪声",
                        "给音频读数掺入按档案定的细微噪声",
                        protection.audioNoise
                    ),
                    Triple(
                        "字体清单",
                        "字体查询只答档案机型自带的字体",
                        protection.fontSet
                    ),
                    Triple(
                        "WebRTC 中转形态",
                        "语音视频通话只走中转，不暴露真实地址",
                        protection.webrtcRelay
                    ),
                    Triple(
                        "传感器",
                        "方向和运动传感器按档案生成静止手持的样子",
                        protection.sensors
                    ),
                    Triple(
                        "电池",
                        "电量和充电状态按档案讲一个固定故事",
                        protection.battery
                    ),
                    Triple(
                        "Chrome 对象",
                        "补上真 Chrome 带的 window.chrome 对象",
                        protection.chromeObject
                    )
                )
                protectionRows.forEachIndexed { index, row ->
                    ListItem(
                        headlineContent = { Text(row.first) },
                        supportingContent = { Text(row.second) },
                        trailingContent = {
                            Switch(
                                checked = row.third,
                                onCheckedChange = { value ->
                                    onProtectionChange(
                                        when (index) {
                                            0 -> protection.copy(
                                                canvasNoise = value
                                            )
                                            1 -> protection.copy(
                                                webglDisguise = value
                                            )
                                            2 -> protection.copy(
                                                audioNoise = value
                                            )
                                            3 -> protection.copy(
                                                fontSet = value
                                            )
                                            4 -> protection.copy(
                                                webrtcRelay = value
                                            )
                                            5 -> protection.copy(
                                                sensors = value
                                            )
                                            6 -> protection.copy(
                                                battery = value
                                            )
                                            else -> protection.copy(
                                                chromeObject = value
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "出口 IP 自查",
                    style = MaterialTheme.typography.titleMedium
                )
                val result = ipResult
                if (ipLoading) {
                    Text("正在查当前出口 IP 的归属……")
                } else if (result != null) {
                    Text("出口 IP：${result.ip}")
                    Text(
                        "归属：${result.countryCode}" +
                            if (result.city.isNotBlank()) {
                                " · ${result.city}"
                            } else {
                                ""
                            }
                    )
                    Text("IP 时区：${result.timezone}")
                    Text("数据来源：${result.source}")
                    val countryOk =
                        result.countryCode == profile.regionId
                    val zoneOk = result.timezone == zone.id
                    Text(
                        when {
                            countryOk && zoneOk ->
                                "和当前档案对得上：国家、时区都一致。"
                            countryOk ->
                                "国家对得上，但 IP 时区和档案时区不一致" +
                                    "——网站交叉对账时会露，换个时区或换个节点。"
                            else ->
                                "归属和当前档案对不上：档案是" +
                                    "${profile.regionLabel}，IP 在" +
                                    "${result.countryCode}——先把网络切到档案地区再上网。"
                        }
                    )
                } else if (ipFailed) {
                    Text("没查到，检查网络后再试。")
                } else {
                    Text(
                        "查一下当前网络的出口 IP 在哪，和档案对不对得上。" +
                            "点按钮会向 ipapi.co 查询，查不到时改问 api.ip.sb。"
                    )
                }
                OutlinedButton(
                    onClick = {
                        ipLoading = true
                        ipFailed = false
                        ipScope.launch {
                            val fetched = fetchExitIp()
                            ipResult = fetched
                            ipFailed = fetched == null
                            ipLoading = false
                        }
                    },
                    enabled = !ipLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (ipResult != null) "重新自查" else "开始自查")
                }
            }
        }
        // Proxy card (v0.197.0): the mode's one proxy, applied
        // for exactly the window the mode is open. The wording
        // carries the platform truth — the WebView override is
        // app-wide and Gecko never reads it — because a user
        // pairing an IP with a fingerprint deserves to know
        // precisely which tabs the pairing covers.
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "代理",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("当前：${proxyConfig.summary}")
                Text(
                    "代理按当前的「地区＋机型」组合保存，切换组合会换用" +
                        "那个组合保存的代理。打开指纹模式期间生效，退出模式自动断开。" +
                        "这期间普通模式的系统 WebView 标签也会走这个代理，" +
                        "火狐标签不受影响。系统接口不支持带账号密码的代理。" +
                        "设好后用上面的出口 IP 自查确认出口对不对。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showProxyDialog = true }
                    ) {
                        Text("设置代理")
                    }
                    if (proxyConfig.host.isNotBlank()) {
                        OutlinedButton(
                            onClick = {
                                onProxyChange(
                                    com.tuxunbrowse.android.engine
                                        .FpProxyConfig()
                                )
                            }
                        ) {
                            Text("清除")
                        }
                    }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "指纹自查",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "在本机用当前档案跑一遍主要探针，逐项和档案对账。" +
                        "全程不出这台手机，结果不外发。"
                )
                if (selfCheckRunning) {
                    Text("正在自查……")
                }
                val scRows = selfCheckRows
                if (!selfCheckRunning && scRows != null) {
                    for ((name, ok, detail) in scRows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name)
                                if (detail.isNotBlank()) {
                                    Text(
                                        detail,
                                        style = MaterialTheme
                                            .typography.bodySmall,
                                        color = MaterialTheme
                                            .colorScheme
                                            .onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                when (ok) {
                                    "ok" -> "通过"
                                    "na" -> "无法检测"
                                    "off" -> "已关闭"
                                    else -> "异常"
                                },
                                color = when (ok) {
                                    "ok" -> MaterialTheme
                                        .colorScheme.primary
                                    "na", "off" -> MaterialTheme
                                        .colorScheme
                                        .onSurfaceVariant
                                    else -> MaterialTheme
                                        .colorScheme.error
                                },
                                style = MaterialTheme
                                    .typography.labelLarge
                            )
                        }
                    }
                }
                OutlinedButton(
                    onClick = { runSelfCheck() },
                    enabled = !selfCheckRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (selfCheckRows != null) {
                            "重新自查"
                        } else {
                            "开始自查"
                        }
                    )
                }
            }
        }
        // The probe WebView's host (v0.184.0): attached only
        // while a run is in flight — a detached WebView may
        // pause its JS — and destroyed by the report callback
        // or when this page leaves composition.
        if (checkWebView != null) {
            AndroidView(
                factory = { checkWebView!! },
                update = { wv ->
                    if (!checkStarted) {
                        checkStarted = true
                        wv.loadUrl("about:blank")
                    }
                },
                modifier = Modifier.size(1.dp)
            )
        }
        Text(
            "快捷切换地区",
            style = MaterialTheme.typography.titleMedium
        )
        // FlowRow, not an equal-weight Row (v0.164.0): four
        // weighted chips squeezed 「台湾地区」 into a quarter
        // of the width, and at large system font sizes the
        // label clipped instead of wrapping. Chips now take
        // their natural width and wrap to a second line when
        // they no longer fit — same single row at normal sizes.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (candidate in FingerprintProfiles.all) {
                FilterChip(
                    selected = candidate.regionId == profile.regionId,
                    onClick = { onQuickRegion(candidate) },
                    label = { Text(candidate.regionLabel) }
                )
            }
        }
        Button(
            onClick = onOpenPicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("选择地区、机型与时区")
        }
        // The storage sentence follows the live capability
        // (v0.196.0; per combination since v0.198.0): isolated
        // where the host WebView offers the multi-profile API,
        // shared where it does not — the copy never claims the
        // better world on a device that is not in it.
        val storageSentence = if (com.tuxunbrowse.android.engine
                .FpStorage.isSupported()
        ) {
            "Cookie 和网站数据按「地区＋机型」分开存：" +
                "每个组合一份，与普通模式、与其他组合都不相通；"
        } else {
            "这台设备的系统 WebView 不支持分开存储，" +
                "Cookie 和网站数据与普通模式共用一份；"
        }
        Text(
            "指纹模式只用系统 WebView，标签页不记历史；" +
                storageSentence +
                "网站能从请求里看出你在用本浏览器（应用标识头关不掉）。",
            style = MaterialTheme.typography.bodyMedium
        )
    }
    if (showProxyDialog) {
        var scheme by remember(showProxyDialog) {
            mutableStateOf(proxyConfig.scheme)
        }
        var host by remember(showProxyDialog) {
            mutableStateOf(proxyConfig.host)
        }
        var portText by remember(showProxyDialog) {
            mutableStateOf(
                if (proxyConfig.port in 1..65535) {
                    proxyConfig.port.toString()
                } else {
                    ""
                }
            )
        }
        var enabled by remember(showProxyDialog) {
            mutableStateOf(proxyConfig.enabled)
        }
        val port = portText.toIntOrNull()
        val valid = host.isNotBlank() && port != null &&
            port in 1..65535
        AlertDialog(
            onDismissRequest = { showProxyDialog = false },
            title = { Text("设置代理") },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        for (candidate in com.tuxunbrowse
                            .android.engine.FpProxyConfig.SCHEMES
                        ) {
                            FilterChip(
                                selected = scheme == candidate,
                                onClick = { scheme = candidate },
                                label = {
                                    Text(
                                        when (candidate) {
                                            "http" -> "HTTP"
                                            "https" -> "HTTPS"
                                            else -> "SOCKS5"
                                        }
                                    )
                                }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it.trim() },
                        label = { Text("主机地址") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = portText,
                        onValueChange = { input ->
                            portText =
                                input.filter { it.isDigit() }
                                    .take(5)
                        },
                        label = { Text("端口") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "启用",
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = enabled,
                            onCheckedChange = { enabled = it }
                        )
                    }
                    Text(
                        "这份代理只属于当前的「地区＋机型」组合；" +
                            "切换组合后请在那个组合下另行设置。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                    Text(
                        "系统接口不支持带账号密码的代理；" +
                            "需要鉴权的代理请用无鉴权的入口或系统 VPN。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme
                            .onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = valid,
                    onClick = {
                        showProxyDialog = false
                        onProxyChange(
                            com.tuxunbrowse.android.engine
                                .FpProxyConfig(
                                    enabled = enabled,
                                    scheme = scheme,
                                    host = host,
                                    port = port ?: 0
                                )
                        )
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showProxyDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun SettingsScreen(
    store: Store,
    refreshTick: Int = 0,
    bookmarkCount: Int = 0,
    historyCount: Int = 0,
    onOpenReleaseUrl: (String) -> Unit = {},
    onEnterFingerprintMode: () -> Unit = {},
    onBack: () -> Unit,
    onClearHistoryRange: (HistoryClearRange) -> Unit = {},
    historyClearCounts: Map<HistoryClearRange, Int> = emptyMap(),
    onRemoveCompatSite: (String) -> Unit,
    onRemoveDesktopSite: (String) -> Unit = {},
    onClearDesktopSites: () -> Unit = {},
    onOpenClearData: () -> Unit,
    onExportBackup: () -> Unit = {},
    onImportBackup: () -> Unit = {},
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onSearchEngineChange: () -> Unit = {},
    onTextZoomChange: (Int) -> Unit,
    onWebDarkChange: (Boolean) -> Unit,
    onReaderStyleChange: () -> Unit = {}
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
    var adBlock by remember(refreshTick) { mutableStateOf(store.adBlockEnabled) }
    var guardScript by remember(refreshTick) {
        mutableStateOf(store.guardScriptEnabled)
    }
    // The blocked total is NOT a remember snapshot like the states
    // above (v0.108.0 mirror audit): it is the one value on this
    // page that changes on its own — a WebView page still loading
    // when the user opens settings keeps counting blocked requests
    // through noteBlocked — so a snapshot frozen at composition
    // (or at the last refreshTick bump) could show a stale number
    // in both the ad-block row and the diagnostics card while the
    // neighbouring bookmark/history counts, which arrive as live
    // parameters, moved on. Reading the store straight in the
    // composition body costs one cached-memory read and picks the
    // current value up at every recomposition (returning to the
    // ad-block sub-page, closing the reset dialog below, ...).
    // The reset action needs no local assignment any more:
    // resetTotalBlocked updates the same memory cache this reads,
    // and closing its dialog recomposes this body.
    val totalBlocked = store.totalBlocked
    var showResetBlockedConfirm by remember { mutableStateOf(false) }
    // Diagnostics stay hidden until the version row on the About
    // page is long-pressed (v0.91.0): they are a troubleshooting
    // aid, not a setting, so they no longer take up About space.
    var showDiagnostics by remember { mutableStateOf(false) }
    var textZoom by remember(refreshTick) { mutableStateOf(store.textZoomPercent) }
    var webDark by remember(refreshTick) { mutableStateOf(store.webDarkMode) }
    var startupRestore by remember(refreshTick) { mutableStateOf(store.startupRestore) }
    var httpsFirst by remember(refreshTick) {
        mutableStateOf(store.httpsFirstEnabled)
    }
    // Privacy section (v0.92.0): tracking-protection level plus
    // the three signal switches. All read the Store live, exactly
    // like the states above.
    var tpLevel by remember(refreshTick) {
        mutableStateOf(store.trackingProtectionLevel)
    }
    var doNotTrack by remember(refreshTick) {
        mutableStateOf(store.doNotTrackEnabled)
    }
    var gpcSignal by remember(refreshTick) {
        mutableStateOf(store.globalPrivacyControlEnabled)
    }
    var webRtcOff by remember(refreshTick) {
        mutableStateOf(store.webRtcDisabled)
    }
    var downloadMode by remember(refreshTick) { mutableStateOf(store.downloadMode) }
    var historyRetention by remember(refreshTick) { mutableStateOf(store.historyRetention) }
    var themeMode by remember(refreshTick) { mutableStateOf(store.themeMode) }
    var readerFont by remember(refreshTick) { mutableStateOf(store.readerFontSize) }
    var readerBg by remember(refreshTick) { mutableStateOf(store.readerBackground) }
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
    var desktopList by remember(refreshTick) { mutableStateOf(store.loadDesktopSites().sorted()) }
    var showClearDesktopConfirm by remember { mutableStateOf(false) }
    var showCleared by remember { mutableStateOf(false) }
    var showHistoryClearDialog by remember { mutableStateOf(false) }
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
    // Diagnostics card data (v0.74.0): the app version code and the
    // system WebView's own package info, read once. The WebView
    // package can genuinely be absent (no WebView provider on the
    // device) — that case is shown as "unknown", never invented.
    val versionCode = remember {
        try {
            val info = settingsContext.packageManager
                .getPackageInfo(settingsContext.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= 28) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION") info.versionCode.toLong()
            }
        } catch (t: Throwable) {
            0L
        }
    }
    val webViewPackageInfo = remember {
        try {
            android.webkit.WebView.getCurrentWebViewPackage()
        } catch (t: Throwable) {
            null
        }
    }
    // App storage for the diagnostics card: the same three-part
    // measure the clear-data page has used since v0.20.0 (app cache
    // + WebView data + Firefox data), summed. Measured off the main
    // thread the first time the About page is shown.
    var diagnosticsStorageText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(settingsPage) {
        if (settingsPage == "about" && diagnosticsStorageText == null) {
            diagnosticsStorageText =
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val cache = dirSizeBytes(settingsContext.cacheDir) ?: 0L
                    val dataDir =
                        java.io.File(settingsContext.applicationInfo.dataDir)
                    val webviewData =
                        dirSizeBytes(java.io.File(dataDir, "app_webview")) ?: 0L
                    val geckoDirs = try {
                        settingsContext.filesDir.listFiles()?.filter {
                            it.isDirectory &&
                                (it.name.contains("gecko", ignoreCase = true) ||
                                    it.name.contains("mozilla", ignoreCase = true))
                        } ?: emptyList()
                    } catch (t: Throwable) {
                        emptyList()
                    }
                    var geckoData = 0L
                    for (dir in geckoDirs) {
                        geckoData += dirSizeBytes(dir) ?: 0L
                    }
                    formatStorageSize(cache + webviewData + geckoData)
                }
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
            val result = kotlinx.coroutines.withContext(
                kotlinx.coroutines.Dispatchers.IO
            ) {
                // The connection and its response stream are closed
                // on every path (v0.107.0 release audit): before this
                // pass the reader was never closed and disconnect()
                // was never called, so every manual update check —
                // success, HTTP error or exception alike — left the
                // connection hanging until the system reaped it.
                //
                // The source is the releases LIST, never the
                // /releases/latest endpoint (v0.119.0 audit): every
                // release in this repo is a prerelease, and GitHub's
                // "latest" only ever names a non-prerelease release —
                // against this repo it answers 404 (verified against
                // the live API), so a check built on it could never
                // find anything. The list includes prereleases; the
                // highest version in it wins via pickLatestRelease.
                // The version compared is tag_name, never the
                // release title (titles carry extra words, e.g.
                // "v0.118.0 测试版").
                //
                // The outcome is classified by updateFetchResult
                // (v0.119.0): rate limiting and an empty list no
                // longer masquerade as a network failure.
                var conn: java.net.HttpURLConnection? = null
                try {
                    conn = java.net.URL(
                        "https://api.github.com/repos/liyuzhou79-star/TuxunBrowse/releases?per_page=5"
                    ).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 10_000
                    conn.readTimeout = 10_000
                    conn.setRequestProperty(
                        "Accept", "application/vnd.github+json"
                    )
                    conn.setRequestProperty("User-Agent", "TuxunBrowse")
                    val code = conn.responseCode
                    if (code !in 200..299) {
                        updateFetchResult(code, null)
                    } else {
                        val raw = conn.inputStream.bufferedReader()
                            .use { it.readText() }
                        val arr = org.json.JSONArray(raw)
                        val pairs = (0 until arr.length()).map { i ->
                            val o = arr.getJSONObject(i)
                            o.optString("tag_name") to o.optString("html_url")
                        }
                        updateFetchResult(code, pickLatestRelease(pairs))
                    }
                } catch (t: Throwable) {
                    updateFetchResult(null, null)
                } finally {
                    try {
                        conn?.disconnect()
                    } catch (t: Throwable) {
                        // Already gone; nothing to release.
                    }
                }
            }
            checkingUpdate = false
            when (result) {
                is UpdateFetchResult.Found ->
                    if (compareVersionNames(result.tag, versionName) > 0) {
                        updateAvailable = result.tag to result.pageUrl
                    } else {
                        showUpToDate = true
                    }
                UpdateFetchResult.RateLimited ->
                    android.widget.Toast.makeText(
                        settingsContext, "检查太频繁了，请过一会儿再试",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                UpdateFetchResult.NoReleases ->
                    android.widget.Toast.makeText(
                        settingsContext, "暂时没查到版本信息，请稍后再试",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                UpdateFetchResult.Failed ->
                    android.widget.Toast.makeText(
                        settingsContext, "检查失败，请检查网络后重试",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
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
        // Sub-page transitions (v0.111.0 motion audit): the six
        // sub-pages (and the home list, and the v0.92.0 privacy
        // page) used to swap instantly inside this one LazyColumn
        // while every other page change in the app goes through
        // the top-level AnimatedContent. They now ride the same
        // sub-page spec as that transition's non-browser branch:
        // fade in 250ms plus a slight horizontal slide, fade out
        // 200ms, FastOutSlowIn throughout. Each side renders the
        // page it was keyed with (targetPage), so the outgoing
        // list keeps showing the old sub-page while it fades.
        // Side benefit: each sub-page gets its own list, so a
        // sub-page now starts at its top instead of inheriting
        // the home list's scroll offset.
        AnimatedContent(
            targetState = settingsPage,
            transitionSpec = {
                (fadeIn(tween(250, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        tween(250, easing = FastOutSlowInEasing)
                    ) { it / 10 }) togetherWith
                    fadeOut(tween(200, easing = FastOutSlowInEasing))
            },
            label = "settingsPageTransition"
        ) { targetPage ->
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
            if (targetPage == "home") {
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
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
            if (targetPage == "engine") {
            item { GroupDivider() }
            item {
                Text("主内核", style = MaterialTheme.typography.titleMedium)
                Text(
                    "新建的普通标签页用此内核打开；已打开的标签不变。",
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
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            item { GroupDivider() }
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
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            item { GroupDivider() }
            item {
                Text("搜索引擎", style = MaterialTheme.typography.titleMedium)
                Text(
                    "地址栏和首页搜索框使用的搜索引擎。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(SearchEngine.entries) { engine ->
                ElevatedCard(
                    onClick = {
                        searchEngine = engine
                        store.searchEngine = engine
                        onSearchEngineChange()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text(engine.label) },
                        trailingContent = {
                            RadioButton(
                                selected = searchEngine == engine,
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            item { GroupDivider() }
            item {
                Text("兼容模式记住的网站", style = MaterialTheme.typography.titleMedium)
                Text(
                    "这些网站自动用兼容模式（系统 WebView）打开；删除后恢复主内核。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (compatList.isEmpty()) {
                item {
                    Text(
                        "还没有。在火狐标签里点切换内核按钮、切到系统 WebView 并勾选记住，即可添加。",
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
            item { GroupDivider() }
            item {
                Text("桌面版站点", style = MaterialTheme.typography.titleMedium)
                Text(
                    "打开过「桌面版网站」的网站记在这里，下次打开自动用桌面版；已打开的标签不受名单变化影响。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (desktopList.isEmpty()) {
                item {
                    Text(
                        "还没有。在任意网站点右上角菜单打开「桌面版网站」，那个网站就会记到这里。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(desktopList, key = { it }) { site ->
                    ListItem(
                        headlineContent = { Text(site) },
                        trailingContent = {
                            IconButton(onClick = {
                                onRemoveDesktopSite(site)
                                desktopList = desktopList - site
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除桌面版记忆")
                            }
                        }
                    )
                    HorizontalDivider()
                }
                item {
                    TextButton(onClick = { showClearDesktopConfirm = true }) {
                        Text("清空全部桌面版站点")
                    }
                }
            }
            }
            if (targetPage == "adblock") {
            item { GroupDivider() }
            item {
                Text("广告拦截", style = MaterialTheme.typography.titleMedium)
            }

            item {
                ListItem(
                    headlineContent = { Text("拦截广告和跟踪请求") },
                    supportingContent = {
                        Text("系统 WebView 按内置名单拦截；火狐用内核自带的反跟踪，名单里的域名在打开时也会拦下。")
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = adBlock,
                            onCheckedChange = {
                                adBlock = it
                                store.adBlockEnabled = it
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "拦截广告和跟踪请求"
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("内置净化脚本") },
                    supportingContent = {
                        Text("在系统 WebView 网页打开前运行，清理广告、拦截恶意跳转和跟踪指纹；火狐标签不支持。")
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = guardScript,
                            onCheckedChange = {
                                guardScript = it
                                store.guardScriptEnabled = it
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "内置净化脚本"
                            }
                        )
                    }
                )
            }
            item {
                Text(
                    "内置名单共 ${AdBlocker.blockedHostCount} 个域名；" +
                        "单页拦截数可在网站信息里看，火狐标签不统计。",
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
                    TextButton(onClick = { showResetBlockedConfirm = true }) {
                        Text("清零")
                    }
                }
            }
            }
            if (targetPage == "download") {
            item { GroupDivider() }
            item {
                Text("下载", style = MaterialTheme.typography.titleMedium)
                Text(
                    "网页和资源嗅探的下载由谁处理。TuxunDown 需另行安装，未安装时自动改用系统下载。",
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
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
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
            if (targetPage == "appearance") {
            item { GroupDivider() }
            item {
                Text("外观", style = MaterialTheme.typography.titleMedium)
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
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            item {
                Text(
                    "只影响应用界面；网页深浅见下方「网页深色模式」。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { GroupDivider() }
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
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            item {
                ListItem(
                    headlineContent = { Text("网页深色模式") },
                    supportingContent = {
                        Text("系统 WebView 需 Android 13 以上；火狐跟随系统深色模式。")
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = webDark,
                            onCheckedChange = {
                                webDark = it
                                store.webDarkMode = it
                                onWebDarkChange(it)
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "网页深色模式"
                            }
                        )
                    }
                )
            }
            item { GroupDivider() }
            item {
                Text("阅读模式", style = MaterialTheme.typography.titleMedium)
                Text(
                    "阅读模式的字号和背景；仅系统 WebView 标签可用。",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(ReaderFontSize.entries) { font ->
                ElevatedCard(
                    onClick = {
                        readerFont = font
                        store.readerFontSize = font
                        onReaderStyleChange()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text("阅读字号 · ${font.label}（${font.bodyPx}px）")
                        },
                        trailingContent = {
                            RadioButton(
                                selected = readerFont == font,
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            items(ReaderBackground.entries) { bg ->
                ElevatedCard(
                    onClick = {
                        readerBg = bg
                        store.readerBackground = bg
                        onReaderStyleChange()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("阅读背景 · ${bg.label}") },
                        supportingContent = if (bg == ReaderBackground.FOLLOW) {
                            { Text("跟随进入阅读模式时应用界面的深浅。") }
                        } else {
                            null
                        },
                        trailingContent = {
                            RadioButton(
                                selected = readerBg == bg,
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            }
            if (targetPage == "privacy") {
            item { GroupDivider() }
            item {
                Text("启动", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ListItem(
                    headlineContent = { Text("启动时恢复上次的标签页") },
                    supportingContent = {
                        Text("无痕标签不会被恢复。")
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = startupRestore,
                            onCheckedChange = {
                                startupRestore = it
                                store.startupRestore = it
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "启动时恢复上次的标签页"
                            }
                        )
                    }
                )
            }
            }
            if (targetPage == "engine") {
            item { GroupDivider() }
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
            item { GroupDivider() }
            item {
                Text("两个内核的差别", style = MaterialTheme.typography.titleMedium)
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
                        ListItem(headlineContent = { Text("网页截图") })
                        ListItem(headlineContent = { Text("标签预览图") })
                        ListItem(headlineContent = { Text("阅读模式与排版设置") })
                        ListItem(headlineContent = { Text("页内查找计数") })
                        ListItem(headlineContent = { Text("本页拦截计数") })
                        ListItem(headlineContent = { Text("广告位隐藏") })
                        ListItem(headlineContent = { Text("打印 / 存为 PDF") })
                        ListItem(headlineContent = { Text("内置净化脚本") })
                        ListItem(headlineContent = { Text("不要追踪") })
                        ListItem(headlineContent = { Text("停用 WebRTC") })
                        ListItem(headlineContent = { Text("指纹模式") })
                        ListItem(headlineContent = { Text("网页深色模式") })
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
                        ListItem(
                            headlineContent = { Text("选字菜单") },
                            supportingContent = {
                                Text("长按选中文字可复制、剪切、粘贴、分享或搜索。")
                            }
                        )
                        ListItem(
                            headlineContent = { Text("剪贴板读权限询问") },
                            supportingContent = {
                                Text("网站想读取剪贴板时先弹窗问你，允许只对这一次有效")
                            }
                        )
                        ListItem(
                            headlineContent = { Text("下载有应用内记录") },
                            supportingContent = {
                                Text("可在下载页查看进度和取消；完成的文件在「应用内保存」里打开。")
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
                            headlineContent = { Text("网页提示框") },
                            supportingContent = {
                                Text(
                                    "网页的提醒、确认和输入框：火狐用应用内弹窗显示，" +
                                        "系统 WebView 用系统对话框显示"
                                )
                            }
                        )
                        ListItem(headlineContent = { Text("不出售或分享资料信号") })
                        ListItem(headlineContent = { Text("长按链接 / 图片菜单") })
                        ListItem(
                            headlineContent = { Text("下载") },
                            supportingContent = {
                                Text(
                                    "通路不同：系统 WebView 的下载按设置里的下载方式来；" +
                                        "火狐标签的下载由浏览器直接保存到下载文件夹"
                                )
                            }
                        )
                    }
                }
            }
            }
            if (targetPage == "privacy") {
            item { GroupDivider() }
            item {
                Text("跟踪保护", style = MaterialTheme.typography.titleMedium)
            }
            items(TrackingProtectionLevel.entries) { level ->
                ElevatedCard(
                    onClick = {
                        tpLevel = level
                        store.trackingProtectionLevel = level
                        GeckoHolder.applyPrivacySettings(settingsContext)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text("增强跟踪保护 · ${level.label}")
                        },
                        supportingContent = {
                            Text(
                                if (level ==
                                    TrackingProtectionLevel.STRICT
                                ) {
                                    "多拦指纹追踪、挖矿脚本和更多跟踪；" +
                                        "少数网站可能显示不正常。"
                                } else {
                                    "拦截常见广告与跟踪，网站一般都能正常用。"
                                }
                            )
                        },
                        trailingContent = {
                            // Passive indicator (v0.99.0
                            // accessibility audit): the whole card
                            // above is the one clickable, and a
                            // live onClick here made TalkBack stop
                            // twice on the same choice and read the
                            // selection state twice. With onClick =
                            // null the radio merges into the card's
                            // semantics — one stop, the card reads
                            // its label, description and selected
                            // state once (same pattern as the
                            // search-engine picker).
                            RadioButton(
                                selected = tpLevel == level,
                                onClick = null
                            )
                        }
                    )
                }
            }
            item {
                ListItem(
                    headlineContent = { Text("不要追踪") },
                    supportingContent = {
                        Text("告诉网站不要追踪你；只对系统 WebView 有效。")
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.99.0 audit): a
                        // bare Switch announces only its on/off
                        // state, never which setting it belongs to
                        // — the row's headline is a separate node.
                        Switch(
                            checked = doNotTrack,
                            onCheckedChange = {
                                doNotTrack = it
                                store.doNotTrackEnabled = it
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "不要追踪"
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("不出售或分享资料") },
                    supportingContent = {
                        Text("告诉网站不要出售或分享你的个人资料。")
                    },
                    trailingContent = {
                        Switch(
                            checked = gpcSignal,
                            onCheckedChange = {
                                gpcSignal = it
                                store.globalPrivacyControlEnabled = it
                                GeckoHolder.applyPrivacySettings(
                                    settingsContext
                                )
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "不出售或分享资料"
                            }
                        )
                    }
                )
            }
            item {
                ListItem(
                    headlineContent = { Text("停用 WebRTC") },
                    supportingContent = {
                        Text(
                            "防止网站查到你的真实 IP 地址；" +
                                "只对系统 WebView 有效。"
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = webRtcOff,
                            onCheckedChange = {
                                webRtcOff = it
                                store.webRtcDisabled = it
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "停用 WebRTC"
                            }
                        )
                    }
                )
            }
            item { GroupDivider() }
            item {
                Text("指纹模式", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ElevatedCard(
                    onClick = onEnterFingerprintMode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("指纹模式") },
                        supportingContent = {
                            Text(
                                "按所选地区的设备、语言、时区和定位浏览网页；" +
                                    "只对系统 WebView 有效，IP 归属地由你的网络决定。"
                            )
                        },
                        leadingContent = {
                            Icon(
                                Icons.Filled.Fingerprint,
                                contentDescription = null
                            )
                        },
                        trailingContent = {
                            Icon(
                                Icons.Filled.ChevronRight,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
            item { GroupDivider() }
            item {
                Text("隐私", style = MaterialTheme.typography.titleMedium)
                Text(
                    "过期记录在下次打开应用时自动清除；无痕标签不记历史。",
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
                                // Passive indicator (v0.136.0 accessibility
                                // pass): the enclosing row/card is the one
                                // click stop; a live onClick here made
                                // TalkBack stop twice on the same choice
                                // (same fix as v0.99.0).
                                onClick = null
                            )
                        }
                    )
                }
            }
            item { GroupDivider() }
            item {
                Text("打开网站", style = MaterialTheme.typography.titleMedium)
            }
            item {
                ListItem(
                    headlineContent = {
                        Text("优先用 HTTPS 打开网站")
                    },
                    supportingContent = {
                        Text(
                            "把输入或点开的 http:// 网址先换成 https:// 打开；" +
                                "IP 和局域网地址不换，网页内链接不受影响。"
                        )
                    },
                    trailingContent = {
                        // Named for TalkBack (v0.136.0, same fix as
                        // the v0.99.0 privacy switches): a bare Switch
                        // announces only its on/off state.
                        Switch(
                            checked = httpsFirst,
                            onCheckedChange = {
                                httpsFirst = it
                                store.httpsFirstEnabled = it
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "优先用 HTTPS 打开网站"
                            }
                        )
                    }
                )
            }
            item { GroupDivider() }
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
                    onClick = { showHistoryClearDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("清除历史记录")
                }
            }
            if (showCleared) {
                item { Text("历史记录已清除。") }
            }
            item { GroupDivider() }
            item {
                Text("备份与恢复", style = MaterialTheme.typography.titleMedium)
                Text(
                    "把书签、历史和设置存成备份文件，可导入合并恢复。",
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
            if (targetPage == "about") {
            item {
                // Brand header (v0.92.0), laid out after the
                // TuxunGram About page the user supplied: brand
                // name, the WeChat Pay support card (the very same
                // image asset as TuxunGram's — one person, one
                // code), the "-支持我们-" caption, then the plain
                // info lines below. The diagnostics long-press
                // target moved here from the old About card: the
                // header is still the page's top block, and the
                // hidden-until-long-press rule is unchanged.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showDiagnostics = true }
                        )
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tuxun",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    FoundationImage(
                        painter = androidx.compose.ui.res.painterResource(
                            com.tuxunbrowse.android.R.drawable
                                .about_support_qr
                        ),
                        contentDescription = "微信支付收款码",
                        modifier = Modifier
                            .width(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "-支持我们-",
                        style = MaterialTheme.typography.bodyMedium,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                // Info lines: label (secondary) + value (primary),
                // the same hierarchy as the reference page. The
                // version is read live from the package, so it can
                // never drift from the shipped build.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Each info row merges into one semantics
                    // node (v0.99.0 audit): TalkBack reads
                    // "版本号：0.99.0" as one phrase instead of
                    // stopping on the label and the value as two
                    // unrelated stops. The GitHub row below keeps
                    // its own clickable node (a text link).
                    Row(
                        modifier = Modifier.semantics(
                            mergeDescendants = true
                        ) {}
                    ) {
                        Text(
                            "版本号：",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            versionName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(
                        modifier = Modifier.semantics(
                            mergeDescendants = true
                        ) {}
                    ) {
                        Text(
                            "我们的作者：",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "LZQ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row {
                        Text(
                            "GitHub：",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "https://github.com/liyuzhou79-star/TuxunBrowse",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                try {
                                    settingsContext.startActivity(
                                        android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse(
                                                "https://github.com/liyuzhou79-star/TuxunBrowse"
                                            )
                                        )
                                    )
                                } catch (t: Throwable) {
                                    android.widget.Toast.makeText(
                                        settingsContext,
                                        "没有能打开链接的应用",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedButton(
                            onClick = { checkForUpdate() },
                            enabled = !checkingUpdate,
                            modifier = Modifier.fillMaxWidth()
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
            if (showDiagnostics) {
            item {
                // Diagnostics card (v0.74.0): versions, counts and
                // the blocked total only — no URLs, titles or any
                // browsing content (DiagnosticInfo has no field that
                // could carry any). The counts come from the very
                // lists BrowserApp holds and applyBackup writes, so
                // what is shown here always matches the live data.
                val diagnosticInfo = DiagnosticInfo(
                    appVersionName = versionName,
                    appVersionCode = versionCode,
                    geckoVersion = GECKOVIEW_VERSION,
                    webViewPackageName = webViewPackageInfo?.packageName,
                    webViewVersionName = webViewPackageInfo?.versionName,
                    androidRelease = android.os.Build.VERSION.RELEASE ?: "未知",
                    androidSdk = android.os.Build.VERSION.SDK_INT,
                    storageText = diagnosticsStorageText ?: "正在计算…",
                    bookmarkCount = bookmarkCount,
                    historyCount = historyCount,
                    totalBlocked = totalBlocked
                )
                val diagnosticRows = listOf(
                    "应用版本" to
                        "${diagnosticInfo.appVersionName}（版本号 ${diagnosticInfo.appVersionCode}）",
                    "火狐内核（GeckoView）版本" to diagnosticInfo.geckoVersion,
                    "系统 WebView" to (
                        if (diagnosticInfo.webViewPackageName.isNullOrBlank()) {
                            "未知"
                        } else {
                            "${diagnosticInfo.webViewPackageName} " +
                                (diagnosticInfo.webViewVersionName
                                    ?.takeIf { it.isNotBlank() } ?: "未知")
                        }
                        ),
                    "Android 版本" to
                        "${diagnosticInfo.androidRelease}（API ${diagnosticInfo.androidSdk}）",
                    "应用存储占用" to diagnosticInfo.storageText,
                    "书签" to "${diagnosticInfo.bookmarkCount} 条",
                    "历史记录" to "${diagnosticInfo.historyCount} 条",
                    "广告拦截累计" to "${diagnosticInfo.totalBlocked} 个"
                )
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            "诊断信息",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 4.dp
                            )
                        )
                        diagnosticRows.forEach { (label, value) ->
                            // Values sit UNDER the label, not in the
                            // trailing slot (v0.81.0 scroll/overflow
                            // audit): the system WebView row's value
                            // is a package name plus a long version
                            // string, and a ListItem's trailing slot
                            // is a small side area — on a narrow
                            // screen it squeezed the label and
                            // clipped the value. Supporting content
                            // takes the full row width and wraps.
                            ListItem(
                                headlineContent = { Text(label) },
                                supportingContent = { Text(value) }
                            )
                        }
                        Text(
                            "这里只有版本号、条数和统计数字，没有任何网址、标题或浏览内容。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = 16.dp, vertical = 4.dp
                            )
                        )
                        Button(
                            onClick = {
                                val copied = copyTextToClipboard(
                                    settingsContext, "诊断信息",
                                    diagnosticText(diagnosticInfo)
                                )
                                android.widget.Toast.makeText(
                                    settingsContext,
                                    if (copied) {
                                        "诊断信息已复制"
                                    } else {
                                        "复制失败，请重试"
                                    },
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("复制诊断信息")
                        }
                    }
                }
            }
            }
            item { Spacer(modifier = Modifier.padding(bottom = 24.dp)) }            }

        }
        }
    }
    if (showHistoryClearDialog) {
        HistoryRangeClearDialog(
            counts = historyClearCounts,
            onDismiss = { showHistoryClearDialog = false },
            onConfirm = { range ->
                showHistoryClearDialog = false
                onClearHistoryRange(range)
                showCleared = true
            }
        )
    }
    val newRelease = updateAvailable
    if (newRelease != null) {
        AlertDialog(
            onDismissRequest = { updateAvailable = null },
            title = { Text("有新版本") },
            text = {
                Text(
                    "发现新版本 ${newRelease.first.removePrefix("v").removePrefix("V")}（你现在用的是 $versionName）。" +
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
    if (showResetBlockedConfirm) {
        AlertDialog(
            onDismissRequest = { showResetBlockedConfirm = false },
            title = { Text("清零拦截统计？") },
            text = {
                Text(
                    "累计拦截数会清零，清完从 0 重新数，没法恢复。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showResetBlockedConfirm = false
                    store.resetTotalBlocked()
                    android.widget.Toast.makeText(
                        settingsContext, "拦截统计已清零",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }) { Text("清零") }
            },
            dismissButton = {
                TextButton(onClick = { showResetBlockedConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
    if (showClearDesktopConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDesktopConfirm = false },
            title = { Text("清空桌面版站点？") },
            text = {
                Text(
                    "会把记住的 ${desktopList.size} 个网站全部去掉，" +
                        "它们下次打开时恢复用手机版显示。已经打开的标签页不受影响。"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showClearDesktopConfirm = false
                    onClearDesktopSites()
                    desktopList = emptyList()
                }) { Text("清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDesktopConfirm = false }) {
                    Text("取消")
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
}
