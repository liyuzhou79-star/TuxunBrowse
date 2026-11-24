package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.SOURCE_DISPLAY_LIMIT
import com.tuxunbrowse.android.engine.isSourceTruncated
import com.tuxunbrowse.android.engine.truncateSourceForDisplay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Page-source display truncation (v0.84.0). The settled rule:
 * only the DISPLAY is cut to the leading [SOURCE_DISPLAY_LIMIT]
 * characters; the full text is what "复制全文" copies. These
 * tests pin the boundary behaviour of the two pure helpers the
 * source page renders from.
 */
class PageSourceTest {

    @Test
    fun shortSourceIsShownWhole() {
        val source = "<html><body>hi</body></html>"
        assertEquals(source, truncateSourceForDisplay(source))
        assertFalse(isSourceTruncated(source))
    }

    @Test
    fun emptySourceIsNotTruncated() {
        assertEquals("", truncateSourceForDisplay(""))
        assertFalse(isSourceTruncated(""))
    }

    @Test
    fun sourceExactlyAtLimitIsShownWhole() {
        val source = "a".repeat(100)
        assertEquals(source, truncateSourceForDisplay(source, limit = 100))
        assertFalse(isSourceTruncated(source, limit = 100))
    }

    @Test
    fun sourceOneOverLimitIsCutToLimit() {
        val source = "a".repeat(101)
        val shown = truncateSourceForDisplay(source, limit = 100)
        assertEquals(100, shown.length)
        assertEquals("a".repeat(100), shown)
        assertTrue(isSourceTruncated(source, limit = 100))
    }

    @Test
    fun truncationKeepsTheLeadingPart() {
        val source = "<html>" + "x".repeat(500) + "</html>"
        val shown = truncateSourceForDisplay(source, limit = 50)
        assertEquals(source.substring(0, 50), shown)
        assertTrue(shown.startsWith("<html>"))
    }

    @Test
    fun defaultLimitIsTheSettledDisplayLimit() {
        val source = "b".repeat(SOURCE_DISPLAY_LIMIT + 1)
        assertTrue(isSourceTruncated(source))
        assertEquals(
            SOURCE_DISPLAY_LIMIT,
            truncateSourceForDisplay(source).length
        )
        assertFalse(
            isSourceTruncated("b".repeat(SOURCE_DISPLAY_LIMIT))
        )
    }
}
