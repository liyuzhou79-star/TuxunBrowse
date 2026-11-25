package com.tuxunbrowse.android

import com.tuxunbrowse.android.ui.ToolbarSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The unified toolbar numbers (v0.191.0): these constants
 * are the contract every shell's chrome is built from, and
 * the v0.191.0 distortion fixes hang off them — the pill's
 * centred text line needs the pill taller than the text,
 * the width budget needs the compact button size, the tab
 * count box is one shared shape, and the menu panel's
 * narrower/longer geometry is pinned here so a future edit
 * cannot quietly widen it back.
 */
class ToolbarSpecTest {

    @Test
    fun slotOrderIsTheUnifiedSix() {
        assertEquals(
            listOf(
                "home", "pill", "engineSwitch",
                "newTab", "tabCount", "menu"
            ),
            ToolbarSpec.SLOT_ORDER
        )
    }

    @Test
    fun pillLineFitsWithAirAboveAndBelow() {
        // The pill's text is the theme's bodyLarge: a 24dp
        // line. The pill must be strictly taller, with at
        // least 8dp of air on each side, or the line reads
        // as clipped again (the v0.191.0 P0).
        assertEquals(48, ToolbarSpec.PILL_HEIGHT_DP)
        assertTrue(
            ToolbarSpec.PILL_HEIGHT_DP - 24 >= 16
        )
        assertTrue(
            ToolbarSpec.PILL_HEIGHT_DP <
                ToolbarSpec.ROW_HEIGHT_DP
        )
    }

    @Test
    fun compactButtonsKeepFullTouchTargetBudget() {
        // Layout shrinks to 40dp; the hit area stays 48dp
        // via expandedTouchTarget, so the compact size must
        // stay below the row's button size it replaces.
        assertEquals(40, ToolbarSpec.BUTTON_COMPACT_DP)
        assertTrue(
            ToolbarSpec.BUTTON_COMPACT_DP <
                ToolbarSpec.BUTTON_DP
        )
        assertEquals(48, ToolbarSpec.BUTTON_DP)
    }

    @Test
    fun tabCountBoxIsOneSharedShape() {
        assertEquals(24, ToolbarSpec.TAB_COUNT_BOX_DP)
        assertEquals(6, ToolbarSpec.TAB_COUNT_RADIUS_DP)
    }

    @Test
    fun menuPanelIsNarrowerAndLongerThanTheOldSheet() {
        assertEquals(300, ToolbarSpec.MENU_PANEL_MAX_WIDTH_DP)
        assertTrue(
            ToolbarSpec.MENU_PANEL_WIDTH_FRACTION in 0.70f..0.85f
        )
        assertEquals(640, ToolbarSpec.MENU_PANEL_MAX_HEIGHT_DP)
        assertTrue(
            ToolbarSpec.MENU_PANEL_MAX_HEIGHT_DP >
                ToolbarSpec.BOTTOM_BAR_HEIGHT_DP * 10
        )
    }

    @Test
    fun barBodiesAreTheTightened193Numbers() {
        // v0.193.0 tightening (user: the grey bottom strip
        // felt huge with small buttons in it, and the top
        // row's air should narrow too): the bottom body is
        // exactly one 48dp target tall — the buttons fill
        // it, no vertical air left to read as emptiness —
        // and the top row keeps just 2dp of air above and
        // below its 48dp children. The system insets are
        // NOT in these numbers: they are padded outside the
        // bodies by the single owning layer per shell (see
        // InsetsOwnershipTest).
        assertEquals(48, ToolbarSpec.BOTTOM_BAR_HEIGHT_DP)
        assertEquals(52, ToolbarSpec.ROW_HEIGHT_DP)
        assertEquals(
            ToolbarSpec.BUTTON_DP,
            ToolbarSpec.BOTTOM_BAR_HEIGHT_DP
        )
        assertEquals(
            4,
            ToolbarSpec.ROW_HEIGHT_DP - ToolbarSpec.PILL_HEIGHT_DP
        )
    }

    @Test
    fun bottomGlyphsAreBiggerThanTheDefaultIcon() {
        // 28dp glyphs in 48dp targets: visibly buttons, not
        // dots, while the hit area is unchanged.
        assertEquals(28, ToolbarSpec.BOTTOM_ICON_DP)
        assertTrue(ToolbarSpec.BOTTOM_ICON_DP > 24)
        assertTrue(
            ToolbarSpec.BOTTOM_ICON_DP <
                ToolbarSpec.BOTTOM_BAR_HEIGHT_DP
        )
    }
}
