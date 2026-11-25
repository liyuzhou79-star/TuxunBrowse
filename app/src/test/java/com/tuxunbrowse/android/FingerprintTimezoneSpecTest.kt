package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.FingerprintProfiles
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * The timezone spec the payload promises (v0.207.0 DST audit).
 *
 * The injected script does NOT hardcode offsets: it asks the
 * host engine's Intl — i.e. the IANA tz database — for the
 * claimed zone's offset at each instant, so DST transition
 * days are right by construction. What a test CAN pin on the
 * JVM is the expectation itself, against java.time's copy of
 * the same IANA database: the standard/daylight offsets of
 * every zone the profiles carry, on both sides of the 2025
 * and 2026 US transition days. If a future change ever swaps
 * in a hardcoded table or a wrong zone id, these are the
 * numbers it has to match. (The local-getter hooks that ride
 * on the same offset math were verified against Intl ground
 * truth in Node during the audit — thousands of instants per
 * zone, including dense samples across the transition
 * windows; see the v0.207.0 checkpoint chapter.)
 */
class FingerprintTimezoneSpecTest {

    /** getTimezoneOffset() minutes for [zone] at [iso]. */
    private fun gtz(zone: String, iso: String): Int {
        val offset = ZoneId.of(zone).rules
            .getOffset(Instant.parse(iso))
        return -offset.totalSeconds / 60
    }

    @Test
    fun everyProfileZoneExistsInTheIanaDatabase() {
        for (profile in FingerprintProfiles.all) {
            for (tz in profile.timezones) {
                assertNotNull(
                    "unknown zone ${tz.id}",
                    ZoneId.of(tz.id)
                )
            }
        }
    }

    @Test
    fun standardAndDaylightOffsetsPerZone() {
        // Winter / summer mid-season values, getTimezoneOffset
        // convention (minutes, positive west of Greenwich).
        val winter = "2025-01-15T12:00:00Z"
        val summer = "2025-07-15T12:00:00Z"
        assertEquals(300, gtz("America/New_York", winter))
        assertEquals(240, gtz("America/New_York", summer))
        assertEquals(360, gtz("America/Chicago", winter))
        assertEquals(300, gtz("America/Chicago", summer))
        assertEquals(420, gtz("America/Denver", winter))
        assertEquals(360, gtz("America/Denver", summer))
        assertEquals(480, gtz("America/Los_Angeles", winter))
        assertEquals(420, gtz("America/Los_Angeles", summer))
        // No-DST zones hold one value all year.
        assertEquals(-540, gtz("Asia/Tokyo", winter))
        assertEquals(-540, gtz("Asia/Tokyo", summer))
        assertEquals(-480, gtz("Asia/Singapore", winter))
        assertEquals(-480, gtz("Asia/Singapore", summer))
        assertEquals(-480, gtz("Asia/Taipei", winter))
        assertEquals(-480, gtz("Asia/Taipei", summer))
    }

    @Test
    fun usTransitionBoundaries2025And2026() {
        // US rule: DST starts the second Sunday of March at
        // 02:00 local standard time, ends the first Sunday of
        // November at 02:00 local daylight time. Transition
        // instants in UTC therefore differ per zone; each row
        // is [zone, startUtc, endUtc, stdGtz, dstGtz] with the
        // same wall-clock rule applied to 2025 (Mar 9 / Nov 2)
        // and 2026 (Mar 8 / Nov 1).
        val rows = listOf(
            Triple("America/New_York", 300, 240),
            Triple("America/Chicago", 360, 300),
            Triple("America/Denver", 420, 360),
            Triple("America/Los_Angeles", 480, 420)
        )
        for ((zone, std, dst) in rows) {
            for (year in listOf(2025, 2026)) {
                val startDay = if (year == 2025) "03-09" else "03-08"
                val endDay = if (year == 2025) "11-02" else "11-01"
                // 02:00 standard local = (2 + std/60) :00 UTC.
                val startUtc = "%d-%sT%02d:00:00Z"
                    .format(year, startDay, 2 + std / 60)
                // 02:00 daylight local = (2 + dst/60) :00 UTC.
                val endUtc = "%d-%sT%02d:00:00Z"
                    .format(year, endDay, 2 + dst / 60)
                val before = Instant.parse(startUtc).minusSeconds(60)
                val after = Instant.parse(startUtc).plusSeconds(60)
                assertEquals(
                    "$zone $year spring, before",
                    std, gtz(zone, before.toString())
                )
                assertEquals(
                    "$zone $year spring, after",
                    dst, gtz(zone, after.toString())
                )
                val endBefore = Instant.parse(endUtc).minusSeconds(60)
                val endAfter = Instant.parse(endUtc).plusSeconds(60)
                assertEquals(
                    "$zone $year fall, before",
                    dst, gtz(zone, endBefore.toString())
                )
                assertEquals(
                    "$zone $year fall, after",
                    std, gtz(zone, endAfter.toString())
                )
            }
        }
    }
}
