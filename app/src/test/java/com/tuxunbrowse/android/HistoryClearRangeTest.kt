package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.HistoryClearRange
import com.tuxunbrowse.android.data.HistoryItem
import com.tuxunbrowse.android.data.countHistoryInClearRange
import com.tuxunbrowse.android.data.historyClearCutoff
import com.tuxunbrowse.android.data.isInClearRange
import com.tuxunbrowse.android.data.keptHistoryAfterClear
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Ranged history clear (v0.79.0): the boundary rule is pinned —
 * an entry timestamped exactly AT the cutoff is inside the
 * window and is removed; one millisecond older survives.
 */
class HistoryClearRangeTest {

    private val now = 1_700_000_000_000L

    private fun item(time: Long) = HistoryItem("t$time", "https://example.com/$time", time)

    @Test
    fun cutoffMath() {
        assertEquals(now - 3_600_000L, historyClearCutoff(HistoryClearRange.LAST_HOUR, now))
        assertEquals(now - 86_400_000L, historyClearCutoff(HistoryClearRange.LAST_24_HOURS, now))
        assertEquals(Long.MIN_VALUE, historyClearCutoff(HistoryClearRange.ALL, now))
    }

    @Test
    fun boundaryEntryIsRemoved() {
        val cutoff = historyClearCutoff(HistoryClearRange.LAST_HOUR, now)
        assertTrue(isInClearRange(cutoff, cutoff))
        assertTrue(isInClearRange(cutoff + 1, cutoff))
        assertFalse(isInClearRange(cutoff - 1, cutoff))
    }

    @Test
    fun keptListDropsOnlyWindow() {
        val cutoff = historyClearCutoff(HistoryClearRange.LAST_24_HOURS, now)
        val items = listOf(
            item(now),
            item(cutoff),
            item(cutoff - 1),
            item(now - 90_000_000L)
        )
        val kept = keptHistoryAfterClear(items, cutoff)
        assertEquals(listOf(cutoff - 1, now - 90_000_000L), kept.map { it.time })
        assertEquals(2, countHistoryInClearRange(items, cutoff))
    }

    @Test
    fun allRemovesEverything() {
        val cutoff = historyClearCutoff(HistoryClearRange.ALL, now)
        val items = listOf(item(0L), item(1L), item(now))
        assertTrue(keptHistoryAfterClear(items, cutoff).isEmpty())
        assertEquals(3, countHistoryInClearRange(items, cutoff))
    }

    @Test
    fun futureTimestampIsInsideWindow() {
        val cutoff = historyClearCutoff(HistoryClearRange.LAST_HOUR, now)
        assertTrue(isInClearRange(now + 60_000L, cutoff))
    }

    @Test
    fun emptyListStaysEmpty() {
        val cutoff = historyClearCutoff(HistoryClearRange.LAST_HOUR, now)
        assertTrue(keptHistoryAfterClear(emptyList(), cutoff).isEmpty())
        assertEquals(0, countHistoryInClearRange(emptyList(), cutoff))
    }
}
