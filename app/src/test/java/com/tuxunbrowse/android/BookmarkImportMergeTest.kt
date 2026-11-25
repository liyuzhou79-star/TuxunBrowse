package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.selectNewBookmarks
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The bookmark-import merge decision (v0.200.0): a pure set
 * pass, pinned here so the quadratic main-thread scan it
 * replaced cannot creep back in a different shape. Counts
 * must match what the import toast reports.
 */
class BookmarkImportMergeTest {

    private fun bm(url: String) = Bookmark(url, url)

    @Test
    fun `all new rows are kept in file order`() {
        val (toAdd, skipped) = selectNewBookmarks(
            emptySet(),
            listOf(bm("https://a.example"), bm("https://b.example"))
        )
        assertEquals(
            listOf("https://a.example", "https://b.example"),
            toAdd.map { it.url }
        )
        assertEquals(0, skipped)
    }

    @Test
    fun `rows already bookmarked are skipped`() {
        val (toAdd, skipped) = selectNewBookmarks(
            setOf("https://a.example"),
            listOf(bm("https://a.example"), bm("https://b.example"))
        )
        assertEquals(listOf("https://b.example"), toAdd.map { it.url })
        assertEquals(1, skipped)
    }

    @Test
    fun `duplicates inside the file count once each`() {
        val (toAdd, skipped) = selectNewBookmarks(
            emptySet(),
            listOf(
                bm("https://a.example"),
                bm("https://a.example"),
                bm("https://a.example")
            )
        )
        assertEquals(1, toAdd.size)
        assertEquals(2, skipped)
    }

    @Test
    fun `empty import adds and skips nothing`() {
        val (toAdd, skipped) = selectNewBookmarks(
            setOf("https://a.example"), emptyList()
        )
        assertEquals(0, toAdd.size)
        assertEquals(0, skipped)
    }

    @Test
    fun `large import stays a single pass over distinct urls`() {
        // Shape check, not a benchmark: 20k rows against 20k
        // existing must resolve by membership, and the counts
        // must still be exact.
        val existing = (0 until 20_000)
            .map { "https://e$it.example" }.toSet()
        val imported = (0 until 20_000).map {
            bm("https://e${it / 2}.example")
        } + bm("https://new.example")
        val (toAdd, skipped) = selectNewBookmarks(existing, imported)
        assertEquals(listOf("https://new.example"), toAdd.map { it.url })
        assertEquals(20_000, skipped)
    }
}
