package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.PIN_SHORTCUT_LABEL_LIMIT
import com.tuxunbrowse.android.data.pinnedShortcutId
import com.tuxunbrowse.android.data.pinnedShortcutInitial
import com.tuxunbrowse.android.data.pinnedShortcutLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pinned home-screen shortcut assembly (v0.86.0). The settled
 * rules: the id is derived from the URL alone, so the same page
 * always maps to the same shortcut; the label is the page title
 * (whitespace collapsed, capped at the app limit) with the host
 * as the fallback for a title-less page; the icon letter is the
 * label's first character.
 */
class PinnedShortcutTest {

    @Test
    fun sameUrlAlwaysYieldsSameId() {
        val url = "https://example.com/some/page?x=1"
        assertEquals(pinnedShortcutId(url), pinnedShortcutId(url))
    }

    @Test
    fun differentUrlsYieldDifferentIds() {
        assertNotEquals(
            pinnedShortcutId("https://example.com/a"),
            pinnedShortcutId("https://example.com/b")
        )
        assertNotEquals(
            pinnedShortcutId("https://example.com/a"),
            pinnedShortcutId("https://other.example/a")
        )
    }

    @Test
    fun idHasStableShape() {
        val id = pinnedShortcutId("https://example.com/")
        assertTrue(id.startsWith("web-"))
        assertEquals("web-".length + 16, id.length)
    }

    @Test
    fun plainTitlePassesThrough() {
        assertEquals(
            "Example Page",
            pinnedShortcutLabel("Example Page", "https://example.com/")
        )
    }

    @Test
    fun titleWhitespaceIsCollapsed() {
        assertEquals(
            "A B C",
            pinnedShortcutLabel("  A\n  B\t C ", "https://example.com/")
        )
    }

    @Test
    fun longTitleIsCappedAtLimit() {
        val label = pinnedShortcutLabel(
            "x".repeat(PIN_SHORTCUT_LABEL_LIMIT + 20),
            "https://example.com/"
        )
        assertEquals(PIN_SHORTCUT_LABEL_LIMIT, label.length)
    }

    @Test
    fun blankTitleFallsBackToHost() {
        assertEquals(
            "example.com",
            pinnedShortcutLabel("", "https://example.com/some/page")
        )
        assertEquals(
            "example.com",
            pinnedShortcutLabel("   ", "https://example.com/some/page")
        )
    }

    @Test
    fun initialIsFirstLabelCharacter() {
        assertEquals(
            "E",
            pinnedShortcutInitial("Example Page", "https://example.com/")
        )
        assertEquals(
            "e",
            pinnedShortcutInitial("", "https://example.com/")
        )
    }
}
