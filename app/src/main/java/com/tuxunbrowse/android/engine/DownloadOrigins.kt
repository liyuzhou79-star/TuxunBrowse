package com.tuxunbrowse.android.engine

import org.json.JSONObject

/**
 * Where a system download was started from (v0.205.0): the
 * downloads screen's records belong to the system
 * DownloadManager, which knows nothing about personas — a
 * failed download retried from that screen used to re-read
 * the DEFAULT cookie jar even when the original download was
 * started by a fingerprint tab carrying its combination's
 * jar (v0.203.0 closed that leak for first-run downloads;
 * the retry path kept it open, because by retry time nothing
 * remembered which persona the record belonged to).
 *
 * This is the side table that remembers: DownloadManager id
 * → fingerprint origin (the combination's storage jar name,
 * plus a human label for the downloads row). Absence of an
 * entry IS the normal origin — normal downloads behave
 * exactly as before and never touch this table's read path
 * beyond the lookup. The table is device-local bookkeeping
 * (system download ids mean nothing on another device), so
 * it stays out of backups like the other fp_* state.
 *
 * Pure functions over strings/values, in the FpProxyBook
 * shape, so the whole contract is unit-testable: broken
 * input degrades to an empty table, never a half-read one.
 */
data class DownloadOrigin(
    /** The combination's storage profile (cookie jar) name. */
    val storageName: String,
    /** Human label of the combination, for the downloads row. */
    val comboLabel: String
)

object DownloadOrigins {

    /** Hard cap on remembered origins; the oldest ids go first. */
    const val MAX_ENTRIES = 300

    /**
     * The retry's cookie decision, as a pure answer: a record
     * with a fingerprint origin retries through that origin's
     * jar (FpStorage.downloadCookie's three-way contract);
     * anything else takes the plain default-jar path, exactly
     * as retries always did.
     */
    fun usesFingerprintJar(origin: DownloadOrigin?): Boolean =
        origin != null && origin.storageName.isNotBlank()

    /** Serialise the table; entries with a blank jar are dropped. */
    fun encode(table: Map<Long, DownloadOrigin>): String {
        val obj = JSONObject()
        for ((id, origin) in table) {
            if (origin.storageName.isBlank()) continue
            obj.put(
                id.toString(),
                JSONObject()
                    .put("jar", origin.storageName)
                    .put("label", origin.comboLabel)
            )
        }
        return obj.toString()
    }

    /** Parse a stored table; anything broken reads as empty. */
    fun decode(raw: String?): Map<Long, DownloadOrigin> {
        if (raw.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val out = LinkedHashMap<Long, DownloadOrigin>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val id = key.toLongOrNull() ?: continue
                val entry = obj.optJSONObject(key) ?: continue
                val jar = entry.optString("jar", "")
                if (jar.isBlank()) continue
                out[id] = DownloadOrigin(
                    storageName = jar,
                    comboLabel = entry.optString("label", "")
                )
            }
            out
        } catch (t: Throwable) {
            emptyMap()
        }
    }

    /** Enforce the cap by dropping the smallest — oldest — ids. */
    fun cap(table: Map<Long, DownloadOrigin>): Map<Long, DownloadOrigin> {
        if (table.size <= MAX_ENTRIES) return table
        val oldest = table.keys.sorted()
            .take(table.size - MAX_ENTRIES).toSet()
        return table.filterKeys { it !in oldest }
    }

    /**
     * Drop origins the DownloadManager no longer lists (the
     * record was deleted here or on the system side), then
     * enforce the cap. Called with the downloads screen's
     * full, unfiltered id set, so a live record's origin is
     * never pruned.
     */
    fun prune(
        table: Map<Long, DownloadOrigin>,
        liveIds: Set<Long>
    ): Map<Long, DownloadOrigin> =
        cap(table.filterKeys { it in liveIds })
}
