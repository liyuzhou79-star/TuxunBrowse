package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.shouldReleaseTransientUiForTab
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Transient-UI release gate (v0.168.0): when a tab is removed, the
 * app-global pending answers (JS / auth / SSL dialogs, file
 * picker, system permissions) are released only when the removed
 * tab is the one the user is looking at — the active tab of
 * EITHER tab group — or when the whole group goes away at once
 * (force). Before this version the gate compared only against
 * the normal group's active id, so closing the active
 * fingerprint tab never answered its pendings. Tab ids come from
 * one process-wide counter, so an id can match at most one
 * group's active tab; -1 is the "no active tab in this group"
 * sentinel and never matches a real (positive) tab id.
 */
class TransientUiReleaseTest {

    @Test
    fun activeNormalTabReleases() {
        assertTrue(
            shouldReleaseTransientUiForTab(
                removedTabId = 7, force = false,
                activeTabId = 7, activeFpTabId = 12
            )
        )
    }

    @Test
    fun activeFingerprintTabReleases() {
        // The v0.168.0 fix: the fingerprint group's active tab is
        // a first-class "active" for the release gate.
        assertTrue(
            shouldReleaseTransientUiForTab(
                removedTabId = 12, force = false,
                activeTabId = 7, activeFpTabId = 12
            )
        )
    }

    @Test
    fun backgroundNormalTabDoesNotRelease() {
        assertFalse(
            shouldReleaseTransientUiForTab(
                removedTabId = 3, force = false,
                activeTabId = 7, activeFpTabId = 12
            )
        )
    }

    @Test
    fun backgroundFingerprintTabDoesNotRelease() {
        // Closing a background fingerprint tab must not answer a
        // pending dialog that belongs to the active normal tab
        // (or to the active fingerprint tab).
        assertFalse(
            shouldReleaseTransientUiForTab(
                removedTabId = 9, force = false,
                activeTabId = 7, activeFpTabId = 12
            )
        )
    }

    @Test
    fun forceAlwaysReleases() {
        assertTrue(
            shouldReleaseTransientUiForTab(
                removedTabId = 3, force = true,
                activeTabId = 7, activeFpTabId = 12
            )
        )
        assertTrue(
            shouldReleaseTransientUiForTab(
                removedTabId = 12, force = true,
                activeTabId = 7, activeFpTabId = 12
            )
        )
    }

    @Test
    fun missingFingerprintGroupNeverMatchesRealTab() {
        // No fingerprint tab was ever opened: the sentinel must
        // not turn a background normal tab into a release.
        assertFalse(
            shouldReleaseTransientUiForTab(
                removedTabId = 3, force = false,
                activeTabId = 7, activeFpTabId = -1
            )
        )
        assertTrue(
            shouldReleaseTransientUiForTab(
                removedTabId = 7, force = false,
                activeTabId = 7, activeFpTabId = -1
            )
        )
    }
}
