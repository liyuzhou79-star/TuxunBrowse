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
 * - Row height 52dp (v0.193.0: tightened from 56 — the
 *   user's "bars feel loose" pass; the pill and the 48dp
 *   targets leave 2dp of air top and bottom), every button
 *   a 48dp target, the pill 48dp tall and fully rounded;
 *   the row is vertically centred
 *   as a whole, so the pill and the buttons share one centre
 *   line by construction (the pre-0.187 row took its height
 *   from the pill's intrinsic measurement and the pill read
 *   as sunk on the real device).
 * - The pill keeps the address text single-line at weight(1f):
 *   it takes whatever width the buttons leave, never less —
 *   the buttons are fixed-size, the pill is the only flexible
 *   element (the v0.13.0 vertical-placeholder lesson).
 * - The bottom bar, where a shell has one, is a 48dp
 *   surfaceContainer body (v0.193.0: tightened from 56, the
 *   48dp targets fill it exactly and the glyphs grow to
 *   28dp so the strip no longer reads as a big grey field
 *   with small buttons in it) with the targets spread
 *   evenly. The navigation-bar inset is NOT part of this
 *   height: it is padded outside the body by the bar's own
 *   windowInsetsPadding, exactly one layer, owned by the
 *   bar surface itself (see the inset-ownership rule in
 *   this file's tail).
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
    const val ROW_HEIGHT_DP: Int = 52
    const val PILL_HEIGHT_DP: Int = 48
    const val BUTTON_DP: Int = 48
    const val BOTTOM_BAR_HEIGHT_DP: Int = 48
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

    /**
     * Bottom-bar glyph size (v0.193.0): the buttons keep
     * their 48dp targets, the glyphs themselves grow from
     * the default 24dp to 28dp so the tightened 48dp bar
     * reads as buttons in a bar, not dots in a field.
     */
    const val BOTTOM_ICON_DP: Int = 28

    /**
     * Inset ownership (v0.193.0, hard rule). Exactly one
     * consumer per system inset, app-wide:
     * - statusBars, top: the normal shell's toolbar Row
     *   carries the one windowInsetsPadding(WindowInsets
     *   .statusBars) — the row is a hand-built Row in the
     *   Scaffold topBar slot, NOT a TopAppBar, so nothing
     *   else pads it (v0.191.0 deleted this layer believing
     *   the Scaffold covered it; the row slid straight
     *   under the status bar, user's screenshot 4121). The
     *   fingerprint shell needs none: it has no topBar, so
     *   its Scaffold content padding owns the inset, and
     *   its identity strip must never add its own (that was
     *   the v0.191.0 double-inset bug).
     * - navigationBars, bottom: each shell's bottom-bar
     *   Surface pads it outside its own 48dp body via
     *   windowInsetsPadding — never by adding the inset
     *   into a hand-computed height with the row centred
     *   across the whole zone (v0.191.0's construction,
     *   which sank the icons half an inset toward the
     *   gesture bar).
     * Future "too much / too little blank space" reports are
     * diagnosed by COUNTING inset consumers, never by
     * editing the height numbers in this file.
     */
    const val STATUS_BAR_INSET_OWNERS: Int = 1
    const val NAV_BAR_INSET_OWNERS_PER_SHELL: Int = 1

    /** The one toolbar order, by slot name, for the record. */
    val SLOT_ORDER: List<String> = listOf(
        "home", "pill", "engineSwitch", "newTab", "tabCount", "menu"
    )
}
