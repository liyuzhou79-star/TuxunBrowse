package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.SPEECH_CHUNK_MAX_CHARS
import com.tuxunbrowse.android.engine.splitSpeechChunks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reader speech chunking (v0.82.0): the splitter decides what one
 * TTS utterance gets, so its contract is pinned here — chunks are
 * never empty, never over the limit, keep the text in order, break
 * at sentence ends where possible, and never merge paragraphs.
 */
class ReaderSpeechTest {

    @Test
    fun blankTextYieldsNoChunks() {
        assertEquals(emptyList<String>(), splitSpeechChunks(""))
        assertEquals(emptyList<String>(), splitSpeechChunks("  \n \n  "))
    }

    @Test
    fun shortTextIsOneChunk() {
        assertEquals(
            listOf("这是一段短文。"),
            splitSpeechChunks("这是一段短文。")
        )
    }

    @Test
    fun sentencesPackUntilTheLimit() {
        val text = "第一句。第二句。第三句。"
        val chunks = splitSpeechChunks(text, maxChars = 10)
        // Each chunk holds whole sentences only, packed greedily:
        // two 4-char sentences plus the joining space fit in 10,
        // the third sentence starts a new chunk.
        assertEquals(listOf("第一句。 第二句。", "第三句。"), chunks)
        val tight = splitSpeechChunks(text, maxChars = 8)
        assertEquals(listOf("第一句。", "第二句。", "第三句。"), tight)
    }

    @Test
    fun paragraphsNeverMerge() {
        val chunks = splitSpeechChunks("第一段。\n第二段。", maxChars = 100)
        assertEquals(listOf("第一段。", "第二段。"), chunks)
    }

    @Test
    fun longSentenceSplitsAtSoftBreak() {
        val sentence = "甲乙丙丁，戊己庚辛，壬癸子丑，寅卯辰巳。"
        val chunks = splitSpeechChunks(sentence, maxChars = 10)
        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= 10 })
        // No characters are lost or reordered by the split.
        assertEquals(
            sentence,
            chunks.joinToString("").replace(" ", "")
        )
    }

    @Test
    fun sentenceWithoutAnyBreakIsHardCut() {
        val sentence = "abcdefghijklmnopqrst"
        val chunks = splitSpeechChunks(sentence, maxChars = 6)
        assertEquals(listOf("abcdef", "ghijkl", "mnopqr", "st"), chunks)
    }

    @Test
    fun englishSentenceEndersAlsoSplit() {
        val chunks = splitSpeechChunks(
            "Hello world! How are you? Fine.",
            maxChars = 14
        )
        assertTrue(chunks.all { it.length <= 14 })
        assertEquals("Hello world!", chunks.first())
    }

    @Test
    fun defaultLimitIsTheSpeechConstant() {
        val long = "句。".repeat(600)
        val chunks = splitSpeechChunks(long)
        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= SPEECH_CHUNK_MAX_CHARS })
    }
}
