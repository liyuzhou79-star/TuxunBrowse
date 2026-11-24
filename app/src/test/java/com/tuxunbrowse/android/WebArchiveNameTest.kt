package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.ARCHIVE_TITLE_LIMIT
import com.tuxunbrowse.android.engine.archiveFileBaseName
import com.tuxunbrowse.android.engine.archiveFileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Web-archive file naming (v0.85.0). The settled rule: the file is
 * named after the page title with filesystem-hostile characters
 * cleaned out, plus a date stamp and the .mhtml extension; a title
 * that cleans down to nothing falls back to a plain placeholder
 * instead of producing an empty or dot-only name.
 */
class WebArchiveNameTest {

    @Test
    fun plainTitlePassesThrough() {
        assertEquals("Example Page", archiveFileBaseName("Example Page"))
    }

    @Test
    fun forbiddenCharactersBecomeUnderscores() {
        val base = archiveFileBaseName("a/b\\c:d*e?f\"g<h>i|j")
        assertEquals("a_b_c_d_e_f_g_h_i_j", base)
        assertFalse(base.contains('/'))
        assertFalse(base.contains(':'))
    }

    @Test
    fun whitespaceRunsCollapseAndTrim() {
        assertEquals(
            "Hello World",
            archiveFileBaseName("  Hello \t\n  World  ")
        )
    }

    @Test
    fun blankTitleFallsBack() {
        assertEquals("网页存档", archiveFileBaseName(""))
        assertEquals("网页存档", archiveFileBaseName("   "))
    }

    @Test
    fun punctuationOnlyTitleFallsBack() {
        assertEquals("网页存档", archiveFileBaseName("..."))
        assertEquals("网页存档", archiveFileBaseName(" . . . "))
    }

    @Test
    fun leadingAndTrailingDotsAreTrimmed() {
        assertEquals("Page", archiveFileBaseName("..Page.."))
    }

    @Test
    fun longTitleIsCappedAtTheLimit() {
        val base = archiveFileBaseName("x".repeat(200))
        assertEquals(ARCHIVE_TITLE_LIMIT, base.length)
        assertTrue(base.all { it == 'x' })
    }

    @Test
    fun fullNameAppendsDateAndExtension() {
        assertEquals(
            "Example Page-20261124.mhtml",
            archiveFileName("Example Page", "20261124")
        )
        assertEquals(
            "网页存档-20261124.mhtml",
            archiveFileName("", "20261124")
        )
    }
}
