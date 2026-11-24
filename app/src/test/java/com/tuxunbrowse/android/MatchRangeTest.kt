package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.matchRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the pure suggestion-highlight matcher
 * ([matchRange], v0.68.0): case-insensitive, first occurrence only,
 * blank inputs and misses return null so rows render unchanged.
 */
class MatchRangeTest {

    @Test
    fun exactMatchCoversWholeText() {
        assertEquals(0..3, matchRange("news", "news"))
    }

    @Test
    fun matchAtStart() {
        assertEquals(0..2, matchRange("github.com", "git"))
    }

    @Test
    fun matchInMiddle() {
        // "www.github.com": the dot is index 3, "git" starts at 4.
        // (The original expectation of 3..5 was written in v0.68.0
        // but never executed — the workflow did not run the tests —
        // and was wrong; first run in v0.69.0 caught it.)
        assertEquals(4..6, matchRange("www.github.com", "git"))
    }

    @Test
    fun matchAtEnd() {
        assertEquals(4..7, matchRange("opennews", "news"))
    }

    @Test
    fun caseInsensitiveBothWays() {
        assertEquals(0..2, matchRange("GitHub", "git"))
        assertEquals(0..2, matchRange("github", "GIT"))
    }

    @Test
    fun firstOccurrenceWins() {
        assertEquals(1..2, matchRange("banana", "an"))
    }

    @Test
    fun queryIsTrimmedBeforeMatching() {
        assertEquals(4..6, matchRange("www.github.com", "  git  "))
    }

    @Test
    fun noMatchReturnsNull() {
        assertNull(matchRange("github.com", "news"))
    }

    @Test
    fun blankQueryReturnsNull() {
        assertNull(matchRange("github.com", ""))
        assertNull(matchRange("github.com", "   "))
    }

    @Test
    fun blankTextReturnsNull() {
        assertNull(matchRange("", "git"))
    }

    @Test
    fun queryLongerThanTextReturnsNull() {
        assertNull(matchRange("git", "github"))
    }

    @Test
    fun cjkMatch() {
        assertEquals(2..3, matchRange("搜索新闻页面", "新闻"))
    }
}
