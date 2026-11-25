package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.normalizePickerMime
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The page-supplied file-picker accept hint is not always a MIME type
 * the system document picker can launch with (v0.133.0): extensions
 * and comma lists must fall back to the pick-anything type instead of
 * making the picker fail to open and the upload look cancelled.
 */
class FilePickerMimeTest {

    @Test
    fun validMimeTypesPassThrough() {
        assertEquals("image/png", normalizePickerMime("image/png"))
        assertEquals("image/*", normalizePickerMime("image/*"))
        assertEquals("application/pdf", normalizePickerMime("application/pdf"))
        assertEquals("*/*", normalizePickerMime("*/*"))
    }

    @Test
    fun surroundingWhitespaceIsTrimmed() {
        assertEquals("image/png", normalizePickerMime("  image/png  "))
    }

    @Test
    fun extensionsAndListsFallBackToAnyType() {
        assertEquals("*/*", normalizePickerMime(".pdf"))
        assertEquals("*/*", normalizePickerMime(".jpg,.png"))
        assertEquals("*/*", normalizePickerMime("image/*,video/*"))
    }

    @Test
    fun blankAndMalformedHintsFallBackToAnyType() {
        assertEquals("*/*", normalizePickerMime(null))
        assertEquals("*/*", normalizePickerMime(""))
        assertEquals("*/*", normalizePickerMime("   "))
        assertEquals("*/*", normalizePickerMime("image"))
        assertEquals("*/*", normalizePickerMime("/png"))
        assertEquals("*/*", normalizePickerMime("image/"))
        assertEquals("*/*", normalizePickerMime("image / png"))
    }
}
