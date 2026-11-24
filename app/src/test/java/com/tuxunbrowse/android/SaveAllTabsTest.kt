package com.tuxunbrowse.android

import com.tuxunbrowse.android.data.Bookmark
import com.tuxunbrowse.android.data.HOME_URL
import com.tuxunbrowse.android.data.TabBookmarkCandidate
import com.tuxunbrowse.android.data.defaultSaveAllTabsFolderName
import com.tuxunbrowse.android.data.planSaveAllTabsAsBookmarks
import com.tuxunbrowse.android.data.saveAllTabsResultText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Save all tabs as bookmarks" planning (v0.87.0). The settled
 * rules: incognito tabs are skipped as incognito (even on the
 * home page), non-http(s) normal tabs are skipped as internal,
 * a URL already bookmarked — or already planned earlier in the
 * same run — is skipped as already-bookmarked, and everything
 * else is planned with the (trimmed) target folder. The result
 * text must say what happened even when nothing was saved.
 */
class SaveAllTabsTest {

    private fun tab(
        url: String,
        title: String = "标题",
        incognito: Boolean = false
    ) = TabBookmarkCandidate(title, url, incognito)

    @Test
    fun defaultFolderNameCarriesIsoDate() {
        assertEquals(
            "标签页 2026-11-24",
            defaultSaveAllTabsFolderName(java.time.LocalDate.of(2026, 11, 24))
        )
    }

    @Test
    fun plainWebTabsAreAllPlannedIntoTheFolder() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(
                tab("https://a.example/1", "甲"),
                tab("http://b.example/2", "乙")
            ),
            emptyList(),
            "我的文件夹"
        )
        assertEquals(2, plan.toAdd.size)
        assertEquals(0, plan.skippedTotal)
        assertEquals("甲", plan.toAdd[0].title)
        assertEquals("我的文件夹", plan.toAdd[0].folder)
        assertEquals("我的文件夹", plan.toAdd[1].folder)
    }

    @Test
    fun folderNameIsTrimmedBeforeStoring() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(tab("https://a.example/")),
            emptyList(),
            "  文件夹  "
        )
        assertEquals("文件夹", plan.toAdd.single().folder)
    }

    @Test
    fun incognitoCountsAsIncognitoEvenOnHomePage() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(
                tab(HOME_URL, "", incognito = true),
                tab("https://a.example/", incognito = true)
            ),
            emptyList(),
            "夹"
        )
        assertEquals(0, plan.toAdd.size)
        assertEquals(2, plan.skippedIncognito)
        assertEquals(0, plan.skippedInternal)
    }

    @Test
    fun internalPagesAreSkippedAsInternal() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(
                tab(HOME_URL, ""),
                tab("tuxun://source", "")
            ),
            emptyList(),
            "夹"
        )
        assertEquals(0, plan.toAdd.size)
        assertEquals(2, plan.skippedInternal)
    }

    @Test
    fun alreadyBookmarkedUrlsAreSkipped() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(
                tab("https://a.example/"),
                tab("https://b.example/")
            ),
            listOf(Bookmark("旧", "https://a.example/")),
            "夹"
        )
        assertEquals(1, plan.toAdd.size)
        assertEquals("https://b.example/", plan.toAdd.single().url)
        assertEquals(1, plan.skippedBookmarked)
    }

    @Test
    fun duplicateTabsInOneRunSaveOnlyOneBookmark() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(
                tab("https://a.example/", "一"),
                tab("https://a.example/", "二")
            ),
            emptyList(),
            "夹"
        )
        assertEquals(1, plan.toAdd.size)
        assertEquals(1, plan.skippedBookmarked)
    }

    @Test
    fun blankTitleFallsBackToUrl() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(tab("https://a.example/", "")),
            emptyList(),
            "夹"
        )
        assertEquals("https://a.example/", plan.toAdd.single().title)
    }

    @Test
    fun emptyTabListPlansNothing() {
        val plan = planSaveAllTabsAsBookmarks(emptyList(), emptyList(), "夹")
        assertEquals(0, plan.toAdd.size)
        assertEquals(0, plan.skippedTotal)
        assertEquals("没有可存的标签页", saveAllTabsResultText(plan, "夹"))
    }

    @Test
    fun resultTextReportsSavedAndSkippedByCategory() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(
                tab("https://a.example/"),
                tab("https://b.example/"),
                tab(HOME_URL, ""),
                tab("https://c.example/", incognito = true)
            ),
            listOf(Bookmark("旧", "https://b.example/")),
            "标签页 2026-11-24"
        )
        assertEquals(
            "已存 1 个书签到「标签页 2026-11-24」，" +
                "跳过 3 个（已加入书签 1 个、无痕 1 个、内部页 1 个）",
            saveAllTabsResultText(plan, "标签页 2026-11-24")
        )
    }

    @Test
    fun resultTextWithoutSkipsOmitsBreakdown() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(tab("https://a.example/")),
            emptyList(),
            "夹"
        )
        assertEquals(
            "已存 1 个书签到「夹」",
            saveAllTabsResultText(plan, "夹")
        )
    }

    @Test
    fun resultTextWhenNothingSavedStillExplainsWhy() {
        val plan = planSaveAllTabsAsBookmarks(
            listOf(tab(HOME_URL, "")),
            emptyList(),
            "夹"
        )
        val text = saveAllTabsResultText(plan, "夹")
        assertTrue(text.startsWith("没有新书签存入"))
        assertTrue(text.contains("内部页 1 个"))
    }
}
