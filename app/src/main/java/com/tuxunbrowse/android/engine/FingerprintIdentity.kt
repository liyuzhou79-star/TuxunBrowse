package com.tuxunbrowse.android.engine

import java.security.MessageDigest

/**
 * The device-fingerprint identity shown on the fingerprint
 * home (v0.224.0): a short stable hash OF the combination,
 * derived deterministically from the resolved profile — the
 * effective noise seed plus the parameters a site could
 * cross-check. Same combination ⇒ same identity, always;
 * different seed, device, zone or language ⇒ different
 * identity. The Chrome version is deliberately NOT an input:
 * the claim follows the host WebView, and a host update must
 * not rename the persona. Pure functions only — the UI masks
 * and expands [masked]/full forms of the same value, and the
 * derivation contract is unit-pinned.
 */
object FingerprintIdentity {

    /** Hex chars of the identity (128 bits of the digest). */
    const val ID_LENGTH = 32

    /** Visible prefix in the masked form. */
    const val PREFIX_LENGTH = 6

    /** The canonical string the digest runs over. */
    fun canonicalOf(resolved: ResolvedFingerprint): String =
        listOf(
            resolved.seed.toString(),
            resolved.profile.regionId,
            resolved.device.id,
            resolved.device.uaModel,
            resolved.timezone.id,
            resolved.language,
            resolved.device.screenWidth.toString(),
            resolved.device.screenHeight.toString(),
            resolved.device.webGlRenderer,
            resolved.device.hardwareConcurrency.toString(),
            resolved.device.deviceMemory.toString()
        ).joinToString("|")

    /** The full identity of one resolved combination. */
    fun idOf(resolved: ResolvedFingerprint): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonicalOf(resolved).toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02x".format(it) }
        return hex.take(ID_LENGTH)
    }

    /** The first [PREFIX_LENGTH] chars — candidate-card preview. */
    fun prefixOf(id: String): String = id.take(PREFIX_LENGTH)

    /** Masked display form: prefix + bullets, rest hidden. */
    fun masked(id: String): String = prefixOf(id) + "••••••"

    /** Display label for a random-device entry. */
    fun candidateLabel(device: FpDevice, id: String): String =
        "随机设备 · " + device.deviceModel + " · " + prefixOf(id)

    /**
     * Roll [count] random-device candidates inside [profile]:
     * a library device plus a fresh seed each, as saveable
     * custom entries. Candidates must be mutually distinct AND
     * distinct from [current] BY IDENTITY (a different device
     * model on the table seed could still be the current
     * persona if the user already runs it; a same-model entry
     * with a fresh seed is a genuinely new device). Randomness
     * comes from [nextRandom] (the UI passes a SecureRandom
     * source; tests pass a scripted one). Bounded retries per
     * candidate, then a deterministic seed nudge — the function
     * always terminates with exactly [count] entries or fewer
     * only when the region cannot supply them (a region always
     * can: seeds are a 31-bit space).
     */
    fun randomCandidates(
        profile: FingerprintProfile,
        timezoneId: String?,
        language: String?,
        current: ResolvedFingerprint,
        nextRandom: () -> Int,
        count: Int = 3
    ): List<CustomFingerprint> {
        val zone = profile.timezone(timezoneId)
        val lang = profile.languageOrDefault(language)
        val seen = mutableSetOf(idOf(current))
        val out = mutableListOf<CustomFingerprint>()
        var guard = 0
        while (out.size < count && guard < 64) {
            guard++
            val device = profile.devices[
                Math.floorMod(nextRandom(), profile.devices.size)
            ]
            var seed = Math.floorMod(nextRandom(), Int.MAX_VALUE - 1) + 1
            var resolved = ResolvedFingerprint(
                profile, zone, device, lang, seedOverride = seed
            )
            var nudges = 0
            while (idOf(resolved) in seen && nudges < 32) {
                nudges++
                seed = if (seed >= Int.MAX_VALUE - 1) 1 else seed + 1
                resolved = ResolvedFingerprint(
                    profile, zone, device, lang, seedOverride = seed
                )
            }
            val id = idOf(resolved)
            if (id in seen) continue
            seen += id
            out += CustomFingerprint(
                regionId = profile.regionId,
                deviceId = device.id,
                timezoneId = zone.id,
                language = lang,
                label = candidateLabel(device, id),
                seed = seed
            )
        }
        return out
    }
}
