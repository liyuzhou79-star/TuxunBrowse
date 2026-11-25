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

    /** The one toolbar order, by slot name, for the record. */
    val SLOT_ORDER: List<String> = listOf(
        "home", "pill", "engineSwitch", "newTab", "tabCount", "menu"
    )
}
