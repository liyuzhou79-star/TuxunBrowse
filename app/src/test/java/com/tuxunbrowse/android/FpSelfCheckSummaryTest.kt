package com.tuxunbrowse.android

import com.tuxunbrowse.android.ui.selfCheckSummaryOf
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * v0.225.0: the self-check summary is a pure tally of one
 * finished run's rows. The counts and the first-bad index
 * are the whole contract — the card's sentence and its jump
 * action both read them, so a miscount would misstate the
 * run or land the jump on the wrong row.
 */
class FpSelfCheckSummaryTest {

    private fun row(ok: String) = Triple("探针", ok, "")

    @Test
    fun countsEveryStateAndFindsTheFirstBadRow() {
        val rows = listOf(
            row("ok"), row("na"), row("bad"),
            row("off"), row("ok"), row("bad")
        )
        val summary = selfCheckSummaryOf(rows)
        assertEquals(2, summary.ok)
        assertEquals(2, summary.bad)
        assertEquals(1, summary.na)
        assertEquals(1, summary.off)
        assertEquals(2, summary.firstBadIndex)
    }

    @Test
    fun cleanRunHasNoBadRowToJumpTo() {
        val summary = selfCheckSummaryOf(
            listOf(row("ok"), row("na"), row("off"))
        )
        assertEquals(0, summary.bad)
        assertEquals(-1, summary.firstBadIndex)
    }

    @Test
    fun emptyRunTalliesZero() {
        val summary = selfCheckSummaryOf(emptyList())
        assertEquals(0, summary.ok)
        assertEquals(0, summary.bad)
        assertEquals(0, summary.na)
        assertEquals(0, summary.off)
        assertEquals(-1, summary.firstBadIndex)
    }

    @Test
    fun unknownStatesAreNotCountedAsAnything() {
        // A state string the tally does not know must not be
        // silently promoted into a pass or a failure.
        val summary = selfCheckSummaryOf(
            listOf(row("weird"), row("ok"))
        )
        assertEquals(1, summary.ok)
        assertEquals(0, summary.bad)
        assertEquals(0, summary.na)
        assertEquals(0, summary.off)
    }
}
