package com.tuxunbrowse.android.ui

/**
 * Edge-swipe navigation geometry and gesture classification
 * (v0.179.0, restructured v0.186.0).
 *
 * The detector lives ONLY on two 24dp-wide strips at the left
 * and right edges of the content area. The middle of the screen
 * carries no gesture layer at all — that is a structural
 * guarantee, not a runtime promise: a vertical page scroll that
 * starts anywhere except the outermost 24dp never meets this
 * code. (Before v0.186.0 the detector sat on one full-size
 * overlay box above the WebView; see the v0.186.0 checkpoint
 * chapter for what that broke in the fingerprint shell.)
 *
 * The classification itself is pure so the unit tests can pin
 * the contract: a vertical-dominant drag is always abandoned
 * (page scroll wins), an inward drag fires exactly at the
 * trigger distance, an outward or short drag never fires.
 */
internal object EdgeSwipeSpec {

    /** Width of each edge detector strip. */
    const val EDGE_WIDTH_DP: Int = 24

    /** Inward travel that triggers back / forward. */
    const val TRIGGER_DP: Int = 64

    /**
     * Vertical drift beyond which (when it also dominates the
     * horizontal travel) the gesture is abandoned to the page.
     */
    const val DRIFT_DP: Int = 48

    /** Height of the centred system-gesture exclusion strip. */
    const val EXCLUSION_HEIGHT_DP: Int = 200

    enum class Verdict { CONTINUE, FIRE, ABANDON }

    /**
     * Classify one move of an edge-born gesture. [fromLeft] is
     * the strip the gesture started on; [dxPx]/[dyPx] are the
     * travel from the down point in pixels; [triggerPx] and
     * [driftPx] are the density-converted thresholds.
     */
    fun classify(
        fromLeft: Boolean,
        dxPx: Float,
        dyPx: Float,
        triggerPx: Float,
        driftPx: Float
    ): Verdict {
        val absDx = kotlin.math.abs(dxPx)
        val absDy = kotlin.math.abs(dyPx)
        if (absDy > driftPx && absDy > absDx) return Verdict.ABANDON
        val inward = if (fromLeft) dxPx else -dxPx
        return if (inward >= triggerPx) Verdict.FIRE else Verdict.CONTINUE
    }
}
