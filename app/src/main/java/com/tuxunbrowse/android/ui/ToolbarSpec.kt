package com.tuxunbrowse.android.ui

/**
 * One toolbar language for every shell (v0.187.0).
 *
 * The normal shell (system WebView tabs and Gecko tabs share
 * it) and the fingerprint shell lay their top toolbar row out
 * with the SAME order and the SAME numbers, so the app never
 * wears three different skins:
 *
 *   home → address pill → engine switch → new tab →
 *   tab count → overflow menu
 *
 * - Row height 56dp, every button a 48dp target, the pill
 *   48dp tall and fully rounded; the row is vertically centred
 *   as a whole, so the pill and the buttons share one centre
 *   line by construction (the pre-0.187 row took its height
 *   from the pill's intrinsic measurement and the pill read
 *   as sunk on the real device).
 * - The pill keeps the address text single-line at weight(1f):
 *   it takes whatever width the buttons leave, never less —
 *   the buttons are fixed-size, the pill is the only flexible
 *   element (the v0.13.0 vertical-placeholder lesson).
 * - The bottom bar, where a shell has one, is the same 56dp
 *   surfaceContainer surface with 48dp targets spread evenly.
 *
 * Allowed fingerprint-shell differences (and only these):
 * the mode identity tint on its bars, the always-on profile
 * strip under the toolbar, the exit button, and conflict
 * items shown greyed-but-tappable in its menu. In the
 * fingerprint shell the engine-switch seat is kept but
 * greyed: the mode is WebView-only, and tapping it explains
 * the conflict instead of switching.
 */
internal object ToolbarSpec {
    const val ROW_HEIGHT_DP: Int = 56
    const val PILL_HEIGHT_DP: Int = 48
    const val BUTTON_DP: Int = 48
    const val BOTTOM_BAR_HEIGHT_DP: Int = 56
    const val TAB_COUNT_BOX_DP: Int = 24
    const val TAB_COUNT_RADIUS_DP: Int = 6

    /**
     * Toolbar buttons' layout size (v0.191.0 width budget):
     * the glyphs lay out at 40dp while expandedTouchTarget
     * keeps the full 48dp hit area, so the six-slot row
     * leaves the pill a usable width at 360dp. The tab-count
     * box above is drawn inside one of these buttons and is
     * the same shared TabCountButton composable in every
     * shell and sheet — there are no per-shell copies.
     */
    const val BUTTON_COMPACT_DP: Int = 40

    /**
     * Menu panel geometry (v0.191.0): the panel used to be
     * min(320, screen − 32)dp wide and at most 560dp tall,
     * which on the real device read as a wide, squat sheet
     * with its last row shaved in half at the bottom edge.
     * It is now at most 300dp and at most 78% of the screen
     * wide, and up to 640dp tall — narrower and longer, the
     * shape the user asked for.
     */
    const val MENU_PANEL_MAX_WIDTH_DP: Int = 300
    const val MENU_PANEL_WIDTH_FRACTION: Float = 0.78f
    const val MENU_PANEL_MAX_HEIGHT_DP: Int = 640

    /** The one toolbar order, by slot name, for the record. */
    val SLOT_ORDER: List<String> = listOf(
        "home", "pill", "engineSwitch", "newTab", "tabCount", "menu"
    )
}
