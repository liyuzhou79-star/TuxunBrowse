package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.SniffKind
import com.tuxunbrowse.android.engine.parseSniffResult
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sniffer result parsing (v0.93.0 audit of the v0.80.0 multi-select
 * batch download). The sniff screen keys its lazy list and its
 * selection set by URL, so the parser's contract matters beyond
 * display: no duplicate URLs, no blank URLs, kinds mapped, and any
 * malformed answer parses to an empty list instead of throwing.
 * The raw input mimics what evaluateJavascript hands back: the
 * script's JSON string, itself JSON-encoded (a quoted string).
 */
class SniffParseTest {

    private fun rawOf(jsonArrayText: String): String =
        JSONObject.quote(jsonArrayText)

    @Test
    fun parsesKindsAndNames() {
        val raw = rawOf(
            "[" +
                "{\"url\":\"https://a.com/f.zip\",\"kind\":\"file\",\"name\":\"f.zip\"}," +
                "{\"url\":\"https://a.com/p.png\",\"kind\":\"image\",\"name\":\"\"}," +
                "{\"url\":\"https://a.com/v.mp4\",\"kind\":\"media\",\"name\":\"v.mp4\"}" +
                "]"
        )
        val out = parseSniffResult(raw)
        assertEquals(3, out.size)
        assertEquals(SniffKind.FILE, out[0].kind)
        assertEquals(SniffKind.IMAGE, out[1].kind)
        assertEquals("p.png", out[1].name)
        assertEquals(SniffKind.MEDIA, out[2].kind)
        assertEquals("a.com", out[0].host)
    }

    @Test
    fun duplicateUrlsCollapseToFirst() {
        val raw = rawOf(
            "[" +
                "{\"url\":\"https://a.com/f.zip\",\"kind\":\"file\",\"name\":\"f.zip\"}," +
                "{\"url\":\"https://a.com/f.zip\",\"kind\":\"image\",\"name\":\"dup.png\"}" +
                "]"
        )
        val out = parseSniffResult(raw)
        assertEquals(1, out.size)
        assertEquals(SniffKind.FILE, out[0].kind)
    }

    @Test
    fun blankUrlsSkippedAndUnknownKindIsFile() {
        val raw = rawOf(
            "[" +
                "{\"url\":\"\",\"kind\":\"file\",\"name\":\"x\"}," +
                "{\"url\":\"https://a.com/a.bin\",\"kind\":\"other\",\"name\":\"a.bin\"}" +
                "]"
        )
        val out = parseSniffResult(raw)
        assertEquals(1, out.size)
        assertEquals(SniffKind.FILE, out[0].kind)
    }

    @Test
    fun malformedAnswersAreEmpty() {
        assertTrue(parseSniffResult(null).isEmpty())
        assertTrue(parseSniffResult("").isEmpty())
        assertTrue(parseSniffResult("null").isEmpty())
        assertTrue(parseSniffResult(rawOf("not json")).isEmpty())
        assertTrue(parseSniffResult(rawOf("{}")).isEmpty())
    }
}
