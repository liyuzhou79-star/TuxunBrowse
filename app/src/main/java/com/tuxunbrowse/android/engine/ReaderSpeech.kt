package com.tuxunbrowse.android.engine

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Reader-mode speech (v0.82.0): read the extracted reader text
 * aloud with the system text-to-speech engine. No permission is
 * involved — TTS is a system service the app talks to, nothing is
 * recorded and nothing leaves the device through this code.
 *
 * The text is fed to the engine in chunks (see [splitSpeechChunks]):
 * one giant speak() call is unreliable on many engines, so the
 * controller speaks one chunk at a time and advances on each
 * utterance-done callback.
 *
 * PAUSE IS NOT A REAL ENGINE PAUSE. Android TTS has no pause that
 * keeps an utterance mid-sentence, so "pause" here stops the
 * engine and remembers the CURRENT chunk index; "continue" starts
 * speaking again FROM THE START OF THAT CHUNK. The UI says 暂停 /
 * 继续 and the position kept is chunk-granular — never pretend
 * otherwise.
 */

/** Default chunk ceiling in characters for one utterance. */
const val SPEECH_CHUNK_MAX_CHARS = 480

private val SENTENCE_ENDERS = setOf('。', '！', '？', '!', '?', '；', ';', '…')
private val SOFT_BREAKS = setOf('，', ',', '、', '：', ':')

/**
 * Split [text] into speakable chunks of at most [maxChars]
 * characters. Paragraphs (newline-separated) are split into
 * sentences at sentence-ending punctuation, sentences are packed
 * greedily into chunks, and a single over-long sentence is cut at
 * the last soft break (comma-like) before the limit, or hard-cut
 * at the limit when there is none. Blank input yields no chunks.
 * Pure and deterministic — unit-tested.
 */
fun splitSpeechChunks(
    text: String,
    maxChars: Int = SPEECH_CHUNK_MAX_CHARS
): List<String> {
    require(maxChars > 0) { "maxChars must be positive" }
    val chunks = mutableListOf<String>()
    val current = StringBuilder()
    fun flush() {
        val s = current.toString().trim()
        if (s.isNotEmpty()) chunks.add(s)
        current.clear()
    }
    // Split one over-long sentence into pieces and feed each piece
    // through the same packing as a normal sentence.
    fun addSentence(sentence: String) {
        var rest = sentence.trim()
        if (rest.isEmpty()) return
        while (rest.length > maxChars) {
            val window = rest.substring(0, maxChars)
            val soft = window.lastIndexOfAny(SOFT_BREAKS.toList())
            val cut = if (soft >= maxChars / 3) soft + 1 else maxChars
            val piece = rest.substring(0, cut).trim()
            if (current.isNotEmpty()) flush()
            if (piece.isNotEmpty()) chunks.add(piece)
            rest = rest.substring(cut).trim()
        }
        if (rest.isEmpty()) return
        // The +1 is the joining space appended below: a chunk must
        // never exceed maxChars including that separator.
        if (current.length > 0 &&
            current.length + 1 + rest.length > maxChars
        ) {
            flush()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(rest)
    }
    for (paragraph in text.split('\n')) {
        val p = paragraph.trim()
        if (p.isEmpty()) continue
        var start = 0
        for (i in p.indices) {
            if (p[i] in SENTENCE_ENDERS) {
                addSentence(p.substring(start, i + 1))
                start = i + 1
            }
        }
        if (start < p.length) addSentence(p.substring(start))
        // A paragraph boundary is a natural pause: never pack two
        // paragraphs into one utterance.
        flush()
    }
    flush()
    return chunks
}

enum class SpeechState { IDLE, SPEAKING, PAUSED }

/**
 * Owns one lazily-created [TextToSpeech] and the chunk queue for
 * the tab it was created for. The engine is only created when the
 * user first presses play — a user who never uses speech never
 * pays for it. [release] stops and shuts the engine down for good;
 * callers tie it to composition/tab lifecycle so speech can never
 * outlive the reader page it belongs to.
 */
class ReaderSpeechController(context: Context) {

    private val appContext = context.applicationContext

    var state by mutableStateOf(SpeechState.IDLE)
        private set

    /** Index of the chunk currently (or next) being spoken. */
    var chunkIndex by mutableStateOf(0)
        private set

    var chunkCount by mutableStateOf(0)
        private set

    /** Set when the device has no working TTS engine. */
    var unavailable by mutableStateOf(false)
        private set

    /** Invoked when speech cannot start because TTS is unusable. */
    var onUnavailable: (() -> Unit)? = null

    private var tts: TextToSpeech? = null
    private var engineReady = false
    private var engineFailed = false
    private var pendingStart = false
    private var chunks: List<String> = emptyList()
    private var sourceText: String = ""

    /**
     * Start speaking [text] from the beginning — or, when paused
     * on the SAME text, continue from the remembered chunk.
     */
    fun playOrResume(text: String) {
        if (state == SpeechState.SPEAKING) return
        if (state == SpeechState.PAUSED && text == sourceText && chunks.isNotEmpty()) {
            state = SpeechState.SPEAKING
            speakCurrent()
            return
        }
        val split = splitSpeechChunks(text)
        if (split.isEmpty()) {
            onUnavailable?.invoke()
            return
        }
        chunks = split
        sourceText = text
        chunkCount = split.size
        chunkIndex = 0
        unavailable = false
        state = SpeechState.SPEAKING
        speakCurrent()
    }

    /**
     * "Pause": stop the engine mid-chunk and keep the chunk index,
     * so [playOrResume] restarts the CURRENT chunk (see the file
     * header — TTS has no true pause).
     */
    fun pause() {
        if (state != SpeechState.SPEAKING) return
        try {
            tts?.stop()
        } catch (t: Throwable) {
            // Engine already gone; the remembered index still holds.
        }
        state = SpeechState.PAUSED
    }

    /** Stop speaking and forget the position entirely. */
    fun stopAndReset() {
        try {
            tts?.stop()
        } catch (t: Throwable) {
            // Nothing to stop.
        }
        state = SpeechState.IDLE
        chunkIndex = 0
        chunkCount = 0
        chunks = emptyList()
        sourceText = ""
        pendingStart = false
    }

    /** Stop, shut the engine down and never speak again. */
    fun release() {
        stopAndReset()
        try {
            tts?.shutdown()
        } catch (t: Throwable) {
            // Shutdown is best-effort.
        }
        tts = null
        engineReady = false
    }

    private fun speakCurrent() {
        if (engineFailed) {
            markUnavailable()
            return
        }
        val engine = tts
        if (engine == null || !engineReady) {
            // First use (or engine still starting): create it and
            // let the init callback fire the pending utterance.
            pendingStart = true
            if (engine == null) createEngine()
            return
        }
        val chunk = chunks.getOrNull(chunkIndex) ?: run {
            stopAndReset()
            return
        }
        val result = try {
            engine.speak(
                chunk, TextToSpeech.QUEUE_FLUSH, null,
                "tuxun-reader-$chunkIndex"
            )
        } catch (t: Throwable) {
            TextToSpeech.ERROR
        }
        if (result == TextToSpeech.ERROR) {
            // A single rejected utterance must not strand the
            // queue: advance as if it had finished.
            advance()
        }
    }

    private fun createEngine() {
        val created = try {
            TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    engineReady = true
                    applyLanguage()
                    attachListener()
                    if (pendingStart && state == SpeechState.SPEAKING) {
                        pendingStart = false
                        speakCurrent()
                    }
                } else {
                    engineFailed = true
                    markUnavailable()
                }
            }
        } catch (t: Throwable) {
            engineFailed = true
            markUnavailable()
            return
        }
        tts = created
    }

    /**
     * Language follows the system locale. When the engine reports
     * the locale's voice data missing or unsupported, nothing is
     * forced: the engine keeps its own default voice (the fallback
     * is "system default", recorded here so nobody has to guess).
     */
    private fun applyLanguage() {
        val engine = tts ?: return
        try {
            val result = engine.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                // Deliberate fallback: leave the engine default.
            }
        } catch (t: Throwable) {
            // Language selection is best-effort.
        }
    }

    private fun attachListener() {
        try {
            tts?.setOnUtteranceProgressListener(object :
                UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    advance()
                }

                @Deprecated("Deprecated in the platform")
                override fun onError(utteranceId: String?) {
                    advance()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    advance()
                }
            })
        } catch (t: Throwable) {
            // Without a listener speech would stall after chunk 1;
            // that surfaces on a real device, not silently here.
        }
    }

    private fun advance() {
        if (state != SpeechState.SPEAKING) return
        if (chunkIndex + 1 < chunks.size) {
            chunkIndex += 1
            speakCurrent()
        } else {
            stopAndReset()
        }
    }

    private fun markUnavailable() {
        unavailable = true
        pendingStart = false
        state = SpeechState.IDLE
        chunkIndex = 0
        chunkCount = 0
        chunks = emptyList()
        sourceText = ""
        onUnavailable?.invoke()
    }
}
