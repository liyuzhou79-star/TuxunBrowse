package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.ReaderCandidateFeatures
import com.tuxunbrowse.android.engine.readerCandidateScore
import com.tuxunbrowse.android.engine.readerTagWeight
import com.tuxunbrowse.android.engine.selectReaderCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Offline samples for the reader-mode candidate scoring
 * (v0.116.0). Each sample feeds the FEATURES a typical page shape
 * would produce in the injected script (tag / total text length /
 * link text length per candidate block, in document order) and
 * pins which block the pure logic in ReaderScoring.kt selects.
 * The script in TabController.kt mirrors the same formulas.
 */
class ReaderScoringTest {

    private fun cand(tag: String, text: Int, link: Int) =
        ReaderCandidateFeatures(tag, text, link)

    @Test
    fun scoreFormulaIsNonLinkTimesDensityFactorTimesWeight() {
        // 750 non-link chars, density 0.25, plain div weight 1.0.
        assertEquals(562.5, readerCandidateScore(cand("div", 1000, 250)), 0.001)
        assertEquals(0.0, readerCandidateScore(cand("div", 0, 0)), 0.001)
        assertEquals(0.0, readerCandidateScore(cand("div", 500, 500)), 0.001)
    }

    @Test
    fun tagWeightsPreferSemanticContainers() {
        assertEquals(1.5, readerTagWeight("article"), 0.001)
        assertEquals(1.4, readerTagWeight("MAIN"), 0.001)
        assertEquals(1.1, readerTagWeight("section"), 0.001)
        assertEquals(1.0, readerTagWeight("div"), 0.001)
        assertEquals(1.0, readerTagWeight("td"), 0.001)
    }

    @Test
    fun newsPagePicksArticleOverWrapperNavAndComments() {
        val candidates = listOf(
            cand("div", 4200, 1400),     // 0 page wrapper (everything)
            cand("article", 2600, 150),  // 1 the story
            cand("div", 600, 570),       // 2 nav
            cand("div", 800, 100),       // 3 comments
            cand("div", 500, 350)        // 4 sidebar
        )
        assertEquals(1, selectReaderCandidate(candidates))
    }

    @Test
    fun blogWithoutArticleTagPicksPostDivNotWrapper() {
        val candidates = listOf(
            cand("div", 3000, 900),  // 0 wrapper
            cand("div", 2100, 120),  // 1 post body
            cand("div", 700, 630)    // 2 link-heavy sidebar
        )
        assertEquals(1, selectReaderCandidate(candidates))
    }

    @Test
    fun forumThreadPicksWholeThreadNotSinglePost() {
        val candidates = listOf(
            cand("div", 6000, 1500),  // 0 wrapper
            cand("div", 5200, 900),   // 1 thread container (all posts)
            cand("div", 700, 60)      // 2 one post only — under the 60% bar
        )
        assertEquals(1, selectReaderCandidate(candidates))
    }

    @Test
    fun docsPagePicksMainOverLinkDenseNav() {
        val candidates = listOf(
            cand("div", 2800, 1200),  // 0 wrapper
            cand("main", 1900, 300),  // 1 documentation body
            cand("div", 900, 850)     // 2 sidebar nav, almost all links
        )
        assertEquals(1, selectReaderCandidate(candidates))
    }

    @Test
    fun tightestQualifyingBlockUnwrapsNesting() {
        // Inner div holds most of the section's readable text; both
        // qualify, so the smaller (tighter) block must win.
        val candidates = listOf(
            cand("section", 2000, 200),  // 0 outer section
            cand("div", 1500, 150)       // 1 inner body
        )
        assertEquals(1, selectReaderCandidate(candidates))
    }

    @Test
    fun equalLengthLinkHeavyBlockLosesToTextBlock() {
        val candidates = listOf(
            cand("div", 1000, 950),  // 0 nav block
            cand("div", 1000, 50)    // 1 text block
        )
        assertEquals(1, selectReaderCandidate(candidates))
    }

    @Test
    fun linkFarmHasNoQualifyingCandidate() {
        val candidates = listOf(
            cand("div", 1500, 1400),
            cand("div", 900, 820),
            cand("section", 600, 540)
        )
        assertNull(selectReaderCandidate(candidates))
    }

    @Test
    fun shortPageHasNoQualifyingCandidate() {
        val candidates = listOf(
            cand("article", 150, 10),
            cand("div", 180, 30)
        )
        assertNull(selectReaderCandidate(candidates))
    }

    @Test
    fun emptyAndTextlessInputsFail() {
        assertNull(selectReaderCandidate(emptyList()))
        assertNull(selectReaderCandidate(listOf(cand("div", 0, 0))))
    }
}
