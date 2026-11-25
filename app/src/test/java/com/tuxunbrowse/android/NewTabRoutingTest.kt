package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.NewTabGroup
import com.tuxunbrowse.android.engine.newTabGroupForSource
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * New-tab landing-group decision (v0.172.0): a tab created from
 * another tab lands in the SOURCE tab's group. Fingerprint tabs
 * are identified by membership in the fingerprint group's id
 * set; tab ids come from one process-wide counter, so an id
 * belongs to at most one group. Before this version the
 * long-press context menu carried no group identity and its
 * "open in new tab" always landed in the normal group — a link
 * long-pressed inside fingerprint mode lost the profile and
 * entered normal history.
 */
class NewTabRoutingTest {

    @Test
    fun fingerprintSourceLandsInFingerprintGroup() {
        assertEquals(
            NewTabGroup.FINGERPRINT,
            newTabGroupForSource(12, setOf(12, 15))
        )
    }

    @Test
    fun normalSourceLandsInNormalGroup() {
        assertEquals(
            NewTabGroup.NORMAL,
            newTabGroupForSource(7, setOf(12, 15))
        )
    }

    @Test
    fun normalSourceWithNoFingerprintTabsStaysNormal() {
        assertEquals(
            NewTabGroup.NORMAL,
            newTabGroupForSource(7, emptySet())
        )
    }

    @Test
    fun unknownSourceIdLandsInNormalGroup() {
        // A source id in neither group (already closed, or the
        // -1 "no tab" sentinel) can never smuggle a new tab
        // into the fingerprint group.
        assertEquals(
            NewTabGroup.NORMAL,
            newTabGroupForSource(99, setOf(12, 15))
        )
        assertEquals(
            NewTabGroup.NORMAL,
            newTabGroupForSource(-1, setOf(12, 15))
        )
    }

    @Test
    fun everyFingerprintIdRoutesToFingerprintGroup() {
        val fpIds = setOf(3, 8, 21)
        for (id in fpIds) {
            assertEquals(
                NewTabGroup.FINGERPRINT,
                newTabGroupForSource(id, fpIds)
            )
        }
        for (id in listOf(1, 2, 4, 22)) {
            assertEquals(
                NewTabGroup.NORMAL,
                newTabGroupForSource(id, fpIds)
            )
        }
    }
}
