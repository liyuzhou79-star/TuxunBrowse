package com.tuxunbrowse.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars

/**
 * The floating menu panel (v0.188.0): one menu form for every
 * shell, modelled on the panel shape the user pointed at —
 * a rounded card floating over the page at the top end, with
 * air on every side (never full-width, never touching the
 * screen edges, never a square corner). Official Material 3
 * pieces only: a Popup hosts an official Card (extraLarge
 * 28dp shape, surfaceContainerHigh, 6dp elevation) whose
 * content is a circle-button row plus grouped ListItem rows.
 *
 * [topOffsetDp] is how far below the status bar the panel's
 * top edge sits — each shell passes its own chrome height
 * (toolbar row, plus the fingerprint shell's identity and
 * profile strips) plus an 8dp gap, so the panel always lands
 * just under the shell's own bars with visible air around it.
 */
@Composable
internal fun FloatingMenuPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    topOffsetDp: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visible) return
    val density = LocalDensity.current
    val statusTopPx = WindowInsets.statusBars.getTop(density)
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val panelWidth = minOf(320, screenWidthDp - 32).dp
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(
            x = with(density) { (-16).dp.roundToPx() },
            y = statusTopPx +
                with(density) { topOffsetDp.dp.roundToPx() }
        ),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier.width(panelWidth)
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                content = content
            )
        }
    }
}

/** The panel's top circle-button row (back / forward / star / …). */
@Composable
internal fun MenuPanelCircleRow(
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Hairline between the panel's groups, inset like the rows. */
@Composable
internal fun MenuPanelDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(
            horizontal = 16.dp,
            vertical = 4.dp
        )
    )
}

/**
 * One grouped row of the panel. A [conflict] row is the
 * fingerprint mode's rule (v0.188.0): the item is NOT hidden
 * and NOT disabled — it renders greyed (38% content colour,
 * the app's established unavailable tint) but stays tappable,
 * and the tap explains the conflict instead of running the
 * action. A merely [enabled]-false row is a normal disabled
 * row (greyed, not tappable), for state like "no forward
 * page".
 */
@Composable
internal fun MenuPanelItem(
    icon: ImageVector,
    label: String,
    supporting: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    conflict: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val dimmed = conflict || !enabled
    val headlineColor = if (dimmed) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val supportingColor = if (dimmed) {
        MaterialTheme.colorScheme.onSurfaceVariant
            .copy(alpha = 0.38f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    ListItem(
        headlineContent = { Text(label, color = headlineColor) },
        supportingContent = supporting?.let {
            { Text(it, color = supportingColor) }
        },
        leadingContent = {
            Icon(
                icon,
                contentDescription = null,
                tint = headlineColor
            )
        },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = conflict || enabled) { onClick() }
    )
}
