package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.bookmarksFromHtml
import com.tuxunbrowse.android.data.bookmarksToHtml
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Netscape HTML bookmark export (v0.90.0): the mirror of the HTML
 * import — folders become H3/DL sections, and the output must
 * round-trip through the importer with titles, URLs and folders
 * intact (pinned has no HTML representation and stays behind).
 */
class BookmarksToHtmlTest {

    private val sample = listOf(
        Bookmark("Example", "https://example.com/"),
        Bookmark("Doc A", "https://a.example.com/", folder = "工作"),
        Bookmark("Pinned One", "https://pin.example.com/", pinned = true),
        Bookmark("Doc B", "https://b.example.com/", folder = "工作"),
        Bookmark("Fun", "https://fun.example.com/", folder = "娱乐")
    )

    @Test
    fun writesNetscapeHeaderAndFolderSections() {
        val html = bookmarksToHtml(sample)
        assertTrue(html.startsWith("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        assertTrue(html.contains("<DT><H3>工作</H3>"))
        assertTrue(html.contains("<DT><H3>娱乐</H3>"))
        // Folder sections come after the root entries.
        val rootIdx = html.indexOf("https://example.com/")
        val folderIdx = html.indexOf("<DT><H3>工作</H3>")
        assertTrue(rootIdx in 0 until folderIdx)
    }

    @Test
    fun roundTripsThroughTheImporter() {
        val (parsed, skipped) = bookmarksFromHtml(bookmarksToHtml(sample))
        assertEquals(0, skipped)
        assertEquals(sample.size, parsed.size)
        val byUrl = parsed.associateBy { it.url }
        for (b in sample) {
            val back = byUrl.getValue(b.url)
            assertEquals(b.title, back.title)
            assertEquals(b.folder, back.folder)
            assertFalse(back.pinned)
        }
    }

    @Test
    fun escapesTitlesUrlsAndFolderNames() {
        val tricky = listOf(
            Bookmark(
                "Tom & \"Jerry\" <3",
                "https://example.com/?a=1&b=\"2\"",
                folder = "A & B"
            )
        )
        val html = bookmarksToHtml(tricky)
        assertTrue(html.contains("Tom &amp; &quot;Jerry&quot; &lt;3"))
        assertTrue(html.contains("a=1&amp;b=&quot;2&quot;"))
        assertTrue(html.contains("<DT><H3>A &amp; B</H3>"))
        val (parsed, _) = bookmarksFromHtml(html)
        assertEquals(1, parsed.size)
        assertEquals("Tom & \"Jerry\" <3", parsed[0].title)
        assertEquals("https://example.com/?a=1&b=\"2\"", parsed[0].url)
        assertEquals("A & B", parsed[0].folder)
    }

    @Test
    fun folderOrderIsFirstAppearanceAndEntriesKeepOrder() {
        val html = bookmarksToHtml(sample)
        val workIdx = html.indexOf("<DT><H3>工作</H3>")
        val funIdx = html.indexOf("<DT><H3>娱乐</H3>")
        assertTrue(workIdx in 0 until funIdx)
        val aIdx = html.indexOf("https://a.example.com/")
        val bIdx = html.indexOf("https://b.example.com/")
        assertTrue(aIdx in workIdx until bIdx)
    }

    @Test
    fun emptyListExportsAnEmptyParseableFile() {
        val html = bookmarksToHtml(emptyList())
        assertTrue(html.startsWith("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        val (parsed, skipped) = bookmarksFromHtml(html)
        assertTrue(parsed.isEmpty())
        assertEquals(0, skipped)
    }
}
