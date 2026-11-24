package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.rfc5987FileName
import com.tuxunbrowse.android.engine.sanitizeDownloadFileName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Download file-name resolution (v0.112.0 download-path audit):
 * the RFC 5987 filename* parser and the name sanitizer are the two
 * pure pieces of resolveDownloadFileName (the resolver itself calls
 * URLUtil, an Android API, so it is covered by code audit instead).
 * These tests pin the edge cases the audit walked through: quoted
 * and UTF-8-encoded filename* values, path separators in suggested
 * names, dot-only names, and the character AND byte length caps.
 */
class DownloadFileNameTest {

    @Test
    fun rfc5987DecodesUtf8PercentEncodedName() {
        val cd = "attachment; filename*=UTF-8''%E5%9B%BE%E5%AF%BB%E5%8D%9A%E5%AE%A2.apk"
        assertEquals("图寻博客.apk", rfc5987FileName(cd))
    }

    @Test
    fun rfc5987HandlesQuotedValueAndTrailingParams() {
        val cd = "attachment; filename*=\"UTF-8''report%202026.pdf\"; filename=\"other.pdf\""
        assertEquals("report 2026.pdf", rfc5987FileName(cd))
    }

    @Test
    fun rfc5987StopsAtSemicolonForUnquotedValue() {
        val cd = "attachment; filename*=UTF-8''a%20b.zip; size=42"
        assertEquals("a b.zip", rfc5987FileName(cd))
    }

    @Test
    fun rfc5987ReturnsNullWithoutStarParam() {
        assertNull(rfc5987FileName("attachment; filename=\"plain.apk\""))
        assertNull(rfc5987FileName("attachment"))
        assertNull(rfc5987FileName(null))
        assertNull(rfc5987FileName(""))
    }

    @Test
    fun rfc5987ReturnsNullForMalformedValue() {
        // No charset'lang' structure at all.
        assertNull(rfc5987FileName("attachment; filename*=noquotes.apk"))
        // Empty encoded part.
        assertNull(rfc5987FileName("attachment; filename*=UTF-8''"))
    }

    @Test
    fun sanitizeStripsDirectoryPartsBothSeparators() {
        assertEquals("file.apk", sanitizeDownloadFileName("/sdcard/dir/file.apk"))
        assertEquals("file.apk", sanitizeDownloadFileName("C:\\dir\\file.apk"))
        assertEquals("file.apk", sanitizeDownloadFileName("../../file.apk"))
    }

    @Test
    fun sanitizeRemovesQuotesAndControlChars() {
        assertEquals("ab.apk", sanitizeDownloadFileName("\"a\"b.apk"))
        assertEquals("ab.apk", sanitizeDownloadFileName("a\u0001b.apk"))
    }

    @Test
    fun sanitizeDotOnlyNameBecomesBlank() {
        assertEquals("", sanitizeDownloadFileName(".."))
        assertEquals("", sanitizeDownloadFileName(". ."))
        assertEquals("", sanitizeDownloadFileName(null))
        assertEquals("", sanitizeDownloadFileName("   "))
    }

    @Test
    fun sanitizeKeepsNormalNamesUntouched() {
        assertEquals("图寻博客 v2.apk", sanitizeDownloadFileName("图寻博客 v2.apk"))
        assertEquals("report.final.pdf", sanitizeDownloadFileName("report.final.pdf"))
    }

    @Test
    fun sanitizeCapsCharacterLengthKeepingExtension() {
        val long = "a".repeat(200) + ".apk"
        val out = sanitizeDownloadFileName(long)
        assertEquals(120, out.length)
        assertTrue(out.endsWith(".apk"))
    }

    @Test
    fun sanitizeCapsUtf8ByteLengthKeepingExtension() {
        // 100 CJK characters = 300 UTF-8 bytes: under the 120-char
        // cap but over the 200-byte cap the audit added.
        val long = "图".repeat(100) + ".apk"
        val out = sanitizeDownloadFileName(long)
        assertTrue(out.toByteArray(Charsets.UTF_8).size <= 200)
        assertTrue(out.endsWith(".apk"))
    }
}
