package com.tuxunbrowse.android.engine

/**
 * Reader-mode candidate scoring (v0.116.0), pure logic.
 *
 * Up to v0.115.0 the reader script simply picked the candidate
 * element with the longest innerText. On real pages that is almost
 * always the outermost page wrapper, so the "reader page" kept the
 * sidebar, the comment block and everything else the wrapper
 * contained, and any page — even a link farm or a web app with no
 * article at all — "succeeded" and switched into reader mode.
 *
 * The scoring here is the single source of truth for the algorithm;
 * the injected script in TabController.kt (buildReaderJs) mirrors
 * these exact constants and formulas in JavaScript, because the
 * script is what actually runs on the page. Keep the two in sync:
 * any change here must be mirrored there, and the unit tests in
 * ReaderScoringTest pin the behaviour of this copy.
 *
 * Score of one candidate =
 *   non-link text length × (1 − link density) × tag weight
 * so link-heavy blocks (navigation, sidebars, link lists) are
 * discounted twice: their link text does not count, and the density
 * factor shrinks what is left.
 *
 * Selection: the best-scoring candidate sets the bar. Among the
 * candidates reaching [READER_TIGHTEST_FRACTION] of the best score,
 * the one with the SMALLEST total text wins — that is the tightest
 * block that still holds (nearly) all the readable text, which is
 * what unwraps an article from its page wrapper. The winner must
 * also hold at least [READER_MIN_NON_LINK_TEXT] characters of
 * non-link text, otherwise there is no article to read and the
 * caller must stay on the original page.
 */

/** Observable features of one candidate element, as the script measures them. */
data class ReaderCandidateFeatures(
    val tag: String,
    val textLength: Int,
    val linkTextLength: Int
) {
    val nonLinkTextLength: Int get() = (textLength - linkTextLength).coerceAtLeast(0)
    val linkDensity: Double
        get() = if (textLength <= 0) 1.0
        else (linkTextLength.toDouble() / textLength.toDouble()).coerceIn(0.0, 1.0)
}

/** Minimum non-link characters a winner must hold for reader mode to engage. */
const val READER_MIN_NON_LINK_TEXT = 200

/** A candidate qualifies for the "tightest block" pick at this fraction of the best score. */
const val READER_TIGHTEST_FRACTION = 0.6

/** Semantic containers are more likely to be the article than a plain div. */
fun readerTagWeight(tag: String): Double = when (tag.lowercase()) {
    "article" -> 1.5
    "main" -> 1.4
    "section" -> 1.1
    else -> 1.0
}

fun readerCandidateScore(candidate: ReaderCandidateFeatures): Double {
    if (candidate.textLength <= 0) return 0.0
    return candidate.nonLinkTextLength *
        (1.0 - candidate.linkDensity) *
        readerTagWeight(candidate.tag)
}

/**
 * Index of the candidate to extract, or null when no candidate
 * qualifies (extraction failure: the caller stays on the original
 * page instead of showing an empty reader page).
 */
fun selectReaderCandidate(candidates: List<ReaderCandidateFeatures>): Int? {
    if (candidates.isEmpty()) return null
    val scores = candidates.map { readerCandidateScore(it) }
    val best = scores.indices.maxByOrNull { scores[it] } ?: return null
    if (candidates[best].nonLinkTextLength < READER_MIN_NON_LINK_TEXT) return null
    val bar = scores[best] * READER_TIGHTEST_FRACTION
    val qualifying = scores.indices.filter { scores[it] >= bar }
    // Tightest qualifying block: smallest total text; ties go to the
    // higher score so the pick stays deterministic.
    return qualifying.minWithOrNull(
        compareBy<Int> { candidates[it].textLength }.thenByDescending { scores[it] }
    )
}
