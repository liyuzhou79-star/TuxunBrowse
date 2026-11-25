package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.bookmarksFromHtml
import com.tuxunbrowse.android.data.bookmarksFromJson
import com.tuxunbrowse.android.data.jsonNestingTooDeep
import com.tuxunbrowse.android.data.parseBackup
import com.tuxunbrowse.android.engine.FingerprintProfiles
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Malformed-input sweep over every file entrance (v0.207.0).
 * The contract being pinned, per entrance: a pathological file
 * either parses or fails cleanly — never crashes the parse
 * thread, never hangs it, and never touches data that was
 * already there (the parsers are pure, so "untouched" is
 * structural; what needs pinning is the fail-cleanly half).
 */
class MalformedImportTest {

    // ---- JSON nesting depth guard --------------------------
    // The platform org.json parser is recursive with no depth
    // limit; the guard is what stands between a deeply nested
    // file and a StackOverflowError the parsers' catch
    // (Exception) cannot see (it is an Error).

    @Test
    fun nestingGuardCountsOnlyStructuralBrackets() {
        assertFalse(jsonNestingTooDeep(""))
        assertFalse(jsonNestingTooDeep("""{"a":[1,2,{"b":"x"}]}"""))
        // Brackets inside string literals are not structure.
        assertFalse(jsonNestingTooDeep("""{"a":"[[[[[[[["}"""))
        // An escaped quote does not end the string.
        assertFalse(jsonNestingTooDeep("""{"a":"\"]]]]]]"}"""))
        // Unbalanced closers neither crash nor count as depth.
        assertFalse(jsonNestingTooDeep("]]]]]]"))
        assertTrue(jsonNestingTooDeep("[".repeat(101)))
        assertTrue(jsonNestingTooDeep("{\"a\":".repeat(101)))
        assertFalse(jsonNestingTooDeep("[".repeat(100)))
    }

    @Test
    fun deeplyNestedBookmarkFileFailsCleanly() {
        val deep = "[".repeat(500) + "]".repeat(500)
        assertTrue(bookmarksFromJson(deep).isEmpty())
        // A sane file still parses afterwards (guard is pure).
        val ok = bookmarksFromJson(
            """[{"title":"a","url":"https://a.example/"}]"""
        )
        assertEquals(1, ok.size)
    }

    @Test
    fun deeplyNestedBackupFailsCleanly() {
        val deep = "{\"a\":".repeat(500) + "1" + "}".repeat(500)
        assertNull(parseBackup(deep))
        // A minimal real backup header still parses.
        assertTrue(parseBackup("""{"format":"tuxunbrowse-backup"}""") != null)
    }

    // ---- Fingerprint-combination import --------------------
    // Was: unbounded entry count with a linear duplicate scan
    // per entry — O(n²) over a file-controlled n.

    @Test
    fun customProfileImportCollapsesMassDuplicates() {
        val entry = """{"r":"US","d":"SM-S921U",""" +
            """"t":"America/New_York","l":"en-US","label":"x"}"""
        val raw = (1..5000).joinToString(",", "[", "]") { entry }
        val parsed = FingerprintProfiles.parseCustomProfiles(raw)
        assertEquals(1, parsed.size)
    }

    @Test
    fun customProfileImportCapsAtTheLimit() {
        val raw = (1..300).joinToString(",", "[", "]") { i ->
            """{"r":"US","d":"D$i","t":"America/New_York",""" +
                """"l":"en-US","label":"p$i"}"""
        }
        val parsed = FingerprintProfiles.parseCustomProfiles(raw)
        assertEquals(
            FingerprintProfiles.MAX_CUSTOM_PROFILE_IMPORT,
            parsed.size
        )
    }

    // ---- Netscape HTML import ------------------------------
    // Was: unbounded regex quantifiers — a file dense in "<A "
    // with no ">" anywhere made the backtracking HREF hunt
    // re-walk the tail at every tag start (~67 s for 750 KB in
    // a desktop regex engine). The quantifiers are bounded
    // now; this sample is the shape that hung, at a size that
    // keeps the test itself quick either way.

    @Test
    fun denseAnchorStartsWithoutTagEndsParseToNothing() {
        val raw = "<A ".repeat(30_000)
        val (found, skipped) = bookmarksFromHtml(raw)
        assertTrue(found.isEmpty())
        assertEquals(0, skipped)
    }

    @Test
    fun longTitledEntryStillImportsAfterTheRegexBounds() {
        val title = "长".repeat(2000)
        val raw = "<DL><p><DT><A HREF=\"https://a.example/\">" +
            title + "</A></DL>"
        val (found, _) = bookmarksFromHtml(raw)
        assertEquals(1, found.size)
        assertEquals(title, found[0].title)
    }

    @Test
    fun deeplyNestedFoldersDoNotOverflow() {
        // The folder walk is an explicit stack, not recursion;
        // 20k nesting levels just make a tall stack of names.
        val raw = buildString {
            repeat(20_000) { append("<DT><H3>F</H3><DL><p>") }
            append("<DT><A HREF=\"https://deep.example/\">D</A>")
        }
        val (found, _) = bookmarksFromHtml(raw)
        assertEquals(1, found.size)
        assertEquals("F", found[0].folder)
    }
}
