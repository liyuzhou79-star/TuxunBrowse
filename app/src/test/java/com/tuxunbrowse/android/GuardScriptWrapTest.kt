package com.tuxunbrowse.android

import com.tuxunbrowse.android.engine.GuardScript
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Page-guard payload assembly (v0.91.0): the verbatim script body
 * must sit BETWEEN the config-seeding prelude and the UI-hiding
 * epilogue — prelude first (the script reads its config at run
 * time), epilogue last (the script mounts its floating UI while
 * it runs, so the hiding style can only land afterwards).
 */
class GuardScriptWrapTest {

    @Test
    fun wrapOrdersPreludeBodyEpilogue() {
        val payload = GuardScript.wrap("/* BODY */")
        val preludeAt = payload.indexOf("__via_lg_v65_global_cfg__")
        val bodyAt = payload.indexOf("/* BODY */")
        val epilogueAt = payload.indexOf("__tuxun_hide__")
        assertTrue(preludeAt >= 0)
        assertTrue(bodyAt > preludeAt)
        assertTrue(epilogueAt > bodyAt)
    }

    @Test
    fun preludePinsEnginesOnAndScriptNightModeOff() {
        val payload = GuardScript.wrap("")
        assertTrue(payload.contains("masterAdPurge: true"))
        assertTrue(payload.contains("masterRedirectGuard: true"))
        assertTrue(payload.contains("masterPrivacyShield: true"))
        assertTrue(payload.contains("masterSpeedBoost: true"))
        assertTrue(payload.contains("masterReadUnlock: true"))
        assertTrue(payload.contains("darkMode: false"))
        // A stale per-site record must not opt a site out or
        // darken it behind the user's back.
        assertTrue(payload.contains("__via_lg_v65_site_cfg__"))
    }

    @Test
    fun epilogueHidesFloatingUiInsideShadowRoot() {
        val payload = GuardScript.wrap("")
        assertTrue(payload.contains("__via_lg_v65_host__"))
        assertTrue(payload.contains("shadowRoot"))
        assertTrue(payload.contains("display:none !important"))
    }
}
