package com.tuxunbrowse.android.engine

import com.tuxunbrowse.android.data.hostOf

/**
 * Built-in ad/tracker host blocklist. A request is blocked when its host
 * is one of these domains or a subdomain of one. Host-only matching keeps
 * false positives low: normal pages on the same registrable domain as an
 * ad service (for example Baidu search) are not affected, only the ad
 * subdomains are listed.
 */
object AdBlocker {
    private val blockedHosts = setOf(
        // Global ad networks and trackers
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "googletagmanager.com", "googletagservices.com",
        "adservice.google.com", "ads.yahoo.com", "amazon-adsystem.com",
        "scorecardresearch.com", "taboola.com", "outbrain.com", "criteo.com",
        "rubiconproject.com", "pubmatic.com", "openx.net", "adnxs.com",
        "adsrvr.org", "moatads.com", "zedo.com", "adform.net",
        "smartadserver.com", "lijit.com", "casalemedia.com", "contextweb.com",
        "advertising.com", "quantserve.com", "mathtag.com", "bluekai.com",
        "demdex.net", "krxd.net", "exelator.com", "eyeota.net", "rfihub.com",
        "adroll.com", "bidswitch.net", "sharethrough.com", "gumgum.com",
        "triplelift.com", "indexww.com", "sovrn.com",
        // Pop-under / aggressive networks
        "popads.net", "popcash.net", "propellerads.com", "adcash.com",
        "mgid.com", "revcontent.com", "adsterra.com", "hilltopads.net",
        "exoclick.com", "juicyads.com", "trafficjunky.com",
        // China ad platforms (ad subdomains only, portals stay reachable)
        "pos.baidu.com", "cpro.baidu.com", "union.baidu.com",
        "tanx.com", "mmstat.com", "cnzz.com", "gdt.qq.com", "mi.gdt.qq.com"
    )

    /** How many hosts the built-in list carries (read by the settings page). */
    val blockedHostCount: Int
        get() = blockedHosts.size

    fun isAdRequest(url: String): Boolean {
        val host = hostOf(url) ?: return false
        return blockedHosts.any { blocked -> host == blocked || host.endsWith(".$blocked") }
    }
}
