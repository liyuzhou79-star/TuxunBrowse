package com.tuxunbrowse.android

import com.tuxunbrowse.android.ui.EdgeSwipeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Edge-swipe navigation contract (v0.186.0): the gesture
 * layer exists only on two 24dp edge strips — the middle of
 * the screen has no detector at all, which is the structural
 * fix for fingerprint-mode pages not scrolling (the pre-0.186
 * full-screen overlay sat on top of the WebView inside the
 * pull-to-refresh box). These tests pin the geometry and the
 * verdict logic the strips run:
 * - a vertical-dominant drag is always abandoned, so page
 *   scrolling and pull-to-refresh win;
 * - an inward drag fires exactly at the trigger distance;
 * - an outward or short drag never fires.
 */
class EdgeSwipeSpecTest {

    private val trigger = 64f
    private val drift = 48f

    @Test
    fun stripsStayNarrow() {
        // Two strips together must leave the overwhelming
        // majority of any real screen width detector-free:
        // even on a narrow 320dp phone they cover 48dp of
        // 320dp, i.e. the middle 85% has no gesture layer.
        assertEquals(24, EdgeSwipeSpec.EDGE_WIDTH_DP)
        assertEquals(64, EdgeSwipeSpec.TRIGGER_DP)
        assertEquals(48, EdgeSwipeSpec.DRIFT_DP)
        val covered = 2 * EdgeSwipeSpec.EDGE_WIDTH_DP
        assertTrue(covered * 100 / 320 <= 15)
    }

    @Test
    fun verticalScrollIsAlwaysAbandoned() {
        // Straight down / up, and mostly-vertical diagonals:
        // the page keeps every one of these.
        assertEquals(
            EdgeSwipeSpec.Verdict.ABANDON,
            EdgeSwipeSpec.classify(true, 4f, 120f, trigger, drift)
        )
        assertEquals(
            EdgeSwipeSpec.Verdict.ABANDON,
            EdgeSwipeSpec.classify(true, 30f, -90f, trigger, drift)
        )
        assertEquals(
            EdgeSwipeSpec.Verdict.ABANDON,
            EdgeSwipeSpec.classify(false, -20f, 60f, trigger, drift)
        )
    }

    @Test
    fun inwardDragFiresAtTrigger() {
        assertEquals(
            EdgeSwipeSpec.Verdict.FIRE,
            EdgeSwipeSpec.classify(true, 64f, 0f, trigger, drift)
        )
        assertEquals(
            EdgeSwipeSpec.Verdict.FIRE,
            EdgeSwipeSpec.classify(false, -80f, 10f, trigger, drift)
        )
        // Just short of the trigger: nothing fires, nothing is
        // consumed — the gesture is still the page's.
        assertEquals(
            EdgeSwipeSpec.Verdict.CONTINUE,
            EdgeSwipeSpec.classify(true, 63f, 0f, trigger, drift)
        )
    }

    @Test
    fun outwardDragNeverFires() {
        assertEquals(
            EdgeSwipeSpec.Verdict.CONTINUE,
            EdgeSwipeSpec.classify(true, -100f, 0f, trigger, drift)
        )
        assertEquals(
            EdgeSwipeSpec.Verdict.CONTINUE,
            EdgeSwipeSpec.classify(false, 100f, 0f, trigger, drift)
        )
    }
}
