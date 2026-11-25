package com.tuxunbrowse.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * The one pill-shaped single-line input for every shell
 * (v0.191.0).
 *
 * Why this exists: the pills used to be the stock Material 3
 * filled TextField squeezed into a 48dp box. That component
 * measures itself against a 56dp minimum height with its own
 * internal top/bottom padding for a floating label slot; given
 * exactly 48dp it keeps the padding and lets the text line
 * sink, so on the real device the glyphs read as vertically
 * clipped — the bottoms of the characters shaved off, the
 * cursor riding the top edge (user's screenshots 4116/4118,
 * 2026-11-25). The same construction was copy-pasted into
 * four places (both shells' address pills and both tab-sheet
 * search pills), so all four wore the same distortion.
 *
 * This pill is built on BasicTextField instead: the 48dp
 * height is pinned by ToolbarSpec, the decoration is a plain
 * Row centred on the cross axis, and the text line (the
 * theme's bodyLarge, 24sp line height) sits in a weight(1f)
 * box with the placeholder overlaid in the same box. There is
 * no label slot and no hidden padding, so the text cannot
 * sink: 48dp of height against a 24dp line leaves 12dp of
 * air above and below by construction.
 *
 * The container is drawn here (surfaceContainerHigh, fully
 * rounded) so every pill in the app is the same object, not
 * four lookalikes. Leading/trailing slots take the caller's
 * own icon buttons, which keep their own touch targets.
 */
@Composable
internal fun PillTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val shape = RoundedCornerShape(percent = 50)
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(ToolbarSpec.PILL_HEIGHT_DP.dp),
        singleLine = true,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape
                    )
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.invoke()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.text.isEmpty()) {
                        // Never wrap: a wrapped placeholder in a
                        // squeezed field renders one character
                        // per line (the v0.13.0 lesson).
                        Text(
                            placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme
                                .onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}

/** String-valued overload for the simpler pills (tab search). */
@Composable
internal fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    // The TextFieldValue is held locally so the cursor/selection
    // survives recomposition (a fresh TextFieldValue(text) built
    // from the plain string on every frame would slam the cursor
    // back to the start after each keystroke). External writes
    // (the tab sheet resetting its query when it opens) are
    // picked up by the sync below, with the cursor parked at
    // the end of the new text.
    var held by remember {
        mutableStateOf(
            TextFieldValue(
                value,
                androidx.compose.ui.text.TextRange(value.length)
            )
        )
    }
    if (held.text != value) {
        held = TextFieldValue(
            value,
            androidx.compose.ui.text.TextRange(value.length)
        )
    }
    PillTextField(
        value = held,
        onValueChange = {
            held = it
            onValueChange(it.text)
        },
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}
