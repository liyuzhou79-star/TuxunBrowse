package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.bookmarksFromHtml
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Folder-structure parsing of Netscape bookmark HTML imports
 * (v0.89.0): a heading names the folder whose entries live in the
 * list that follows it; nested lists take the innermost folder.
 */
class HtmlImportFoldersTest {

    @Test
    fun entriesInAFolderTakeItsName() {
        val html = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <DL><p>
                <DT><H3>工作</H3>
                <DL><p>
                    <DT><A HREF="https://a.example.com/">A</A>
                    <DT><A HREF="https://b.example.com/">B</A>
                </DL><p>
                <DT><A HREF="https://root.example.com/">Root</A>
            </DL><p>
        """.trimIndent()
        val (list, skipped) = bookmarksFromHtml(html)
        assertEquals(0, skipped)
        assertEquals(3, list.size)
        assertEquals("工作", list[0].folder)
        assertEquals("工作", list[1].folder)
        assertEquals("", list[2].folder)
    }

    @Test
    fun nestedFoldersTakeTheInnermostName() {
        val html = """
            <DL><p>
                <DT><H3>外层</H3>
                <DL><p>
                    <DT><A HREF="https://outer.example.com/">Outer</A>
                    <DT><H3>内层</H3>
                    <DL><p>
                        <DT><A HREF="https://inner.example.com/">Inner</A>
                    </DL><p>
                    <DT><A HREF="https://outer2.example.com/">Outer2</A>
                </DL><p>
            </DL><p>
        """.trimIndent()
        val (list, _) = bookmarksFromHtml(html)
        assertEquals(3, list.size)
        assertEquals("外层", list[0].folder)
        assertEquals("内层", list[1].folder)
        assertEquals("外层", list[2].folder)
    }

    @Test
    fun siblingFoldersDoNotBleedIntoEachOther() {
        val html = """
            <DL><p>
                <DT><H3>甲</H3>
                <DL><p>
                    <DT><A HREF="https://a.example.com/">A</A>
                </DL><p>
                <DT><H3>乙</H3>
                <DL><p>
                    <DT><A HREF="https://b.example.com/">B</A>
                </DL><p>
            </DL><p>
        """.trimIndent()
        val (list, _) = bookmarksFromHtml(html)
        assertEquals("甲", list[0].folder)
        assertEquals("乙", list[1].folder)
    }

    @Test
    fun folderNamesAreDecodedAndTrimmed() {
        val html = """
            <DL><p>
                <DT><H3 ADD_DATE="1700000000">  工作 &amp; 学习  </H3>
                <DL><p>
                    <DT><A HREF="https://a.example.com/">A</A>
                </DL><p>
            </DL><p>
        """.trimIndent()
        val (list, _) = bookmarksFromHtml(html)
        assertEquals("工作 & 学习", list[0].folder)
    }

    @Test
    fun skippedEntriesInsideAFolderStillCount() {
        val html = """
            <DL><p>
                <DT><H3>夹</H3>
                <DL><p>
                    <DT><A HREF="file:///etc/passwd">Local</A>
                    <DT><A HREF="https://ok.example.com/">OK</A>
                </DL><p>
            </DL><p>
        """.trimIndent()
        val (list, skipped) = bookmarksFromHtml(html)
        assertEquals(1, skipped)
        assertEquals(1, list.size)
        assertEquals("夹", list[0].folder)
    }

    @Test
    fun unclosedFolderListStillAssignsItsEntries() {
        val html = """
            <DL><p>
                <DT><H3>未闭合</H3>
                <DL><p>
                    <DT><A HREF="https://a.example.com/">A</A>
        """.trimIndent()
        val (list, _) = bookmarksFromHtml(html)
        assertEquals(1, list.size)
        assertEquals("未闭合", list[0].folder)
    }

    @Test
    fun headingWithoutAListNamesNothing() {
        val html = """
            <DL><p>
                <DT><H3>空标题</H3>
                <DT><A HREF="https://a.example.com/">A</A>
            </DL><p>
        """.trimIndent()
        val (list, _) = bookmarksFromHtml(html)
        assertEquals("", list[0].folder)
    }

    @Test
    fun entriesBeforeAnyListStayUncategorized() {
        val html = """<DT><A HREF="https://a.example.com/">A</A>"""
        val (list, _) = bookmarksFromHtml(html)
        assertEquals("", list[0].folder)
    }
}
