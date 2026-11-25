package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.DOWNLOAD_EXTENSIONS
import com.tuxunbrowse.android.engine.SNIFF_FILE_EXTENSIONS
import com.tuxunbrowse.android.engine.SNIFF_MEDIA_EXTENSIONS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The sniffer's extension sets (v0.140.0 audit). The sniffer script
 * is generated from [SNIFF_FILE_EXTENSIONS] and
 * [SNIFF_MEDIA_EXTENSIONS], and its anchor pass accepts the union
 * of the two — so every extension that turns a tap into a direct
 * download ([DOWNLOAD_EXTENSIONS], the same union by construction)
 * must also be collectable by the sniffer. Before v0.140.0 the
 * anchor pass gated on the file set alone and silently skipped
 * direct links to media-only extensions (.webm/.m4a/.wav/.ogg/
 * .flac); these tests pin the set relationships that fix relies on
 * so a future edit to any of the three sets cannot reopen the gap
 * unnoticed.
 */
class SniffExtensionSetsTest {

    @Test
    fun downloadSetIsExactlyTheSniffUnion() {
        assertEquals(
            SNIFF_FILE_EXTENSIONS + SNIFF_MEDIA_EXTENSIONS,
            DOWNLOAD_EXTENSIONS
        )
    }

    @Test
    fun mediaOnlyExtensionsAreCollectable() {
        val mediaOnly = listOf("webm", "m4a", "wav", "ogg", "flac")
        for (ext in mediaOnly) {
            assertTrue("$ext must be in the media set", ext in SNIFF_MEDIA_EXTENSIONS)
            assertFalse("$ext must stay media-only", ext in SNIFF_FILE_EXTENSIONS)
            assertTrue("$ext must be downloadable", ext in DOWNLOAD_EXTENSIONS)
        }
    }

    @Test
    fun sharedExtensionsClassifyAsMedia() {
        // Extensions present in both sets (mp4/mp3/mov/mkv/avi) are
        // reported as media by both sniffer passes; the overlap is
        // deliberate and must not be "tidied" away.
        val shared = listOf("mp4", "mp3", "mov", "mkv", "avi")
        for (ext in shared) {
            assertTrue(ext in SNIFF_FILE_EXTENSIONS)
            assertTrue(ext in SNIFF_MEDIA_EXTENSIONS)
        }
    }

    @Test
    fun everyDownloadExtensionIsInTheSniffUnion() {
        val union = SNIFF_FILE_EXTENSIONS + SNIFF_MEDIA_EXTENSIONS
        for (ext in DOWNLOAD_EXTENSIONS) {
            assertTrue("$ext must be collectable by the sniffer", ext in union)
        }
    }
}
