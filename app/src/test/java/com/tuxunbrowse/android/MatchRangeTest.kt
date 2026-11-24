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
        assertEquals(3..5, matchRange("www.github.com", "git"))
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
        assertEquals(3..5, matchRange("www.github.com", "  git  "))
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
