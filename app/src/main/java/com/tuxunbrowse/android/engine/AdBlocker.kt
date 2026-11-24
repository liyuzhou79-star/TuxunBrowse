package com.tuxunbrowse.android.engine

import com.tuxunbrowse.android.data.hostOf

/**
 * Built-in ad/tracker host blocklist. A request is blocked when its host
 * is one of these domains or a subdomain of one. Host-only matching keeps
 * false positives low: normal pages on the same registrable domain as an
 * ad service (for example Baidu search) are not affected, only the ad
 * subdomains are listed.
 *
 * List discipline (v0.77.0 expansion): only domains dedicated to ad
 * delivery, ad mediation, analytics/tracking collection, push/pop
 * monetisation or mobile ad SDK traffic belong here. A domain that
 * also serves normal content (search, social, video, shopping, a
 * company/product site, a general CDN) never belongs, however many
 * ads it carries — when only some subdomains are ad-dedicated, the
 * subdomain is listed instead of the registrable domain.
 */
object AdBlocker {
    /**
     * The raw list, kept as a list (not a set) so the unit tests can
     * prove it carries no duplicates; [blockedHosts] is the lookup
     * set built from it.
     */
    val blockedHostList: List<String> = listOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "google-analytics.com", "googletagmanager.com", "googletagservices.com",
        "adservice.google.com", "ads.yahoo.com", "amazon-adsystem.com",
        "scorecardresearch.com", "taboola.com", "outbrain.com",
        "criteo.com", "rubiconproject.com", "pubmatic.com",
        "openx.net", "adnxs.com", "adsrvr.org",
        "moatads.com", "zedo.com", "adform.net",
        "smartadserver.com", "lijit.com", "casalemedia.com",
        "contextweb.com", "advertising.com", "quantserve.com",
        "mathtag.com", "bluekai.com", "demdex.net",
        "krxd.net", "exelator.com", "eyeota.net",
        "rfihub.com", "adroll.com", "bidswitch.net",
        "sharethrough.com", "gumgum.com", "triplelift.com",
        "indexww.com", "sovrn.com", "media.net",
        "bidvertiser.com", "infolinks.com", "viglink.com",
        "skimlinks.com", "33across.com", "nativo.com",
        "teads.tv", "spotxchange.com", "rhythmone.com",
        "tremorhub.com", "undertone.com", "kargo.com",
        "conversantmedia.com", "valueclickmedia.com", "fastclick.net",
        "burstmedia.com", "specificmedia.com", "collective-media.net",
        "adtech.com", "adtechus.com", "sascdn.com",
        "criteo.net", "yieldmanager.com", "rightmedia.com",
        "rmxads.com", "adsonar.com", "quigo.com",
        "pulse360.com", "adblade.com", "adbrite.com",
        "adpepper.com", "buysellads.com", "carbonads.com",
        "advertserve.com", "adbutler.com", "ox-d.com",
        "adswizz.com", "vidoomy.com", "seedtag.com",
        "primis.tech", "brid.tv", "jwpltx.com",
        "connatix.com", "districtm.io", "peer39.com",
        "doubleverify.com", "iasds01.com", "integralads.com",
        "moatpixel.com", "adsafeprotected.com", "flashtalking.com",
        "innovid.com", "sizmek.com", "adition.com",
        "adform.com", "underdogmedia.com", "lkqd.net",
        "tremorvideo.com", "brightroll.com", "adap.tv",
        "amobee.com", "dataxu.com", "xaxis.com",
        "appnexus.com", "thetradedesk.com", "springserve.com",
        "beachfront.com", "adcolony.com", "vungle.com",
        "liftoff.io", "moloco.com", "applovin.com",
        "applvn.com", "chartboost.com", "unityads.unity3d.com",
        "ironsrc.com", "supersonicads.com", "tapjoy.com",
        "fyber.com", "inner-active.mobi", "smaato.net",
        "pubnative.net", "mintegral.com", "mtgglobals.com",
        "pangle.io", "pangleglobal.com", "startapp.com",
        "ogury.com", "adikteev.com", "mopub.com",
        "inmobi.com", "vserv.com", "airpush.com",
        "leadbolt.com", "millennialmedia.com", "mobfox.com",
        "madvertise.com", "admarvel.com", "greystripe.com",
        "medialets.com", "nexage.com", "admeld.com",
        "flurry.com", "appsflyer.com", "adjust.com",
        "adjust.io", "kochava.com", "tenjin.io",
        "singular.net", "segment.io", "heapanalytics.com",
        "fullstory.com", "mouseflow.com", "crazyegg.com",
        "inspectlet.com", "luckyorange.com", "hs-analytics.net",
        "hs-scripts.com", "hsadspixel.net", "hsleadflows.net",
        "mktoresp.com", "pi.pardot.com", "en25.com",
        "addthis.com", "sharethis.com", "popads.net",
        "popcash.net", "propellerads.com", "adcash.com",
        "mgid.com", "revcontent.com", "adsterra.com",
        "hilltopads.net", "exoclick.com", "juicyads.com",
        "trafficjunky.com", "popmyads.com", "poptm.com",
        "clickadu.com", "hilltopads.com", "trafficfactory.com",
        "trafficstars.com", "exosrv.com", "exdynsrv.com",
        "plugrush.com", "adskeeper.com", "adspyglass.com",
        "ad-maven.com", "pushengage.com", "pushwoosh.com",
        "pushcrew.com", "subscribers.com", "aimtell.com",
        "notix.co", "evadav.com", "galaksion.com",
        "mondiad.com", "mybid.io", "richads.com",
        "push.house", "pushground.com", "daopush.com",
        "onclickmax.com", "onclickads.net", "popunder.net",
        "terraclicks.com", "clickpapa.com", "adxpansion.com",
        "bat.bing.com", "clarity.ms", "assoc-amazon.com",
        "rcm.amazon.com", "omtrdc.net", "2o7.net",
        "adobedtm.com", "quantcast.com", "imrworldwide.com",
        "bam.nr-data.net", "pos.baidu.com", "cpro.baidu.com",
        "union.baidu.com", "tanx.com", "mmstat.com",
        "cnzz.com", "gdt.qq.com", "eiv.baidu.com",
        "hm.baidu.com", "tongji.baidu.com", "cpro2.baidu.com",
        "mobads.baidu.com", "mobads-logs.baidu.com", "afd.baidu.com",
        "rigel.baidustatic.com", "eclick.baidu.com", "adx.baidu.com",
        "atanx.alimama.com", "afpeng.alimama.com", "51.la",
        "umeng.com", "alog.umeng.co", "mob.com",
        "growingio.com", "mtj.baidu.com", "pangolin-sdk-toutiao.com",
        "pangle.cn", "ad.toutiao.com", "adimg.uve.weibo.com",
        "alitui.weibo.com", "sax.sina.com.cn", "ad.sina.com.cn",
        "e.qq.com", "pgdt.gtimg.cn", "tajs.qq.com",
        "adsmind.gdtimg.com", "sigmob.cn", "ad.kuaishou.com",
        "ads.kuaishou.com", "adimg.kuaishou.com", "miads.com",
        "ad.mi.com", "ads.mi.com", "tracking.miui.com",
        "ad.xiaomi.com", "adapi.xiaomi.com", "ad.huawei.com",
        "ads.huawei.com", "metrics.data.huawei.com", "ad.oppo.com",
        "ads.oppo.com", "adx.oppo.com", "ad.vivo.com.cn",
        "ad.meizu.com", "tongji.meizu.com", "ad.iqiyi.com",
        "ads.iqiyi.com", "cm.iqiyi.com", "ad.video.qq.com",
        "ads.video.qq.com", "ad.youku.com", "ads.youku.com",
        "cm.youku.com", "ad.mgtv.com", "cm.bilibili.com",
        "data.bilibili.com", "ad.sohu.com", "ad.163.com",
        "ads.163.com", "g.163.com", "analytics.163.com",
        "ad.360.cn", "ads.360.cn", "lianmeng.360.cn",
        "ad.sogou.com", "ads.sogou.com", "fair.sogou.com",
        "ad.ifeng.com", "stad.ifeng.com", "mediav.com",
        "adsame.com", "madhouse.cn", "madhouse.com",
        "domob.cn", "adwo.com", "youmi.net",
        "youmimobile.com", "adsmogo.com", "adchina.com",
        "adview.cn", "adview.com", "adsview.com",
        "admaster.com.cn", "admaster.com", "miaozhen.com",
        "ipinyou.com", "adups.com", "adways.com"
    )

    /**
     * Extra hosts blocked only at the STRICT tracking-protection
     * level (v0.92.0): browser-fingerprinting services,
     * cryptomining pools/scripts and a second tier of analytics /
     * session-replay collectors. Same list discipline as the base
     * list — dedicated-purpose domains only, never a domain that
     * also serves normal content. Kept separate so STANDARD stays
     * exactly the pre-v0.92.0 behaviour and the level choice has a
     * real, countable difference on the WebView side (on the Gecko
     * side the difference is the kernel's own STRICT category set).
     *
     * v0.94.0 false-positive review: matching is by host suffix,
     * so listing a parent domain also blocks every subdomain —
     * including the vendor's own customer app where that lives on
     * a subdomain (app.*, dash.*) or on the apex itself. Parent
     * domains of that shape were withdrawn even though their
     * collection endpoints are real trackers (when in doubt, do
     * not block): hotjar.com, logrocket.com, smartlook.com,
     * mixpanel.com, amplitude.com, segment.com, pendo.io,
     * woopra.com, chartbeat.com, parsely.com, statcounter.com,
     * histats.com, clicky.com, heap.io, uxcam.com,
     * quantummetric.com, gosquared.com. Where a dedicated
     * collection subdomain was already listed it stays and is now
     * the operative entry (static.hotjar.com, cdn.logrocket.io,
     * rec.smartlook.com, getclicky.com). One pure duplicate was
     * also dropped: api.segment.io was already covered by the
     * base list's segment.io suffix, at every level.
     */
    val strictHostList: List<String> = listOf(
        // Fingerprinting services (identification is their product;
        // fraud-scoring and captcha domains are deliberately NOT
        // here — blocking those breaks logins and checkouts).
        "fingerprintjs.com", "fingerprint.com", "fpjs.io",
        "deviceandbrowserinfo.com",
        // In-browser cryptominers (script hosts from public miner
        // blocklists; mining-pool user sites are not listed).
        "coinhive.com", "authedmine.com", "coinimp.com",
        "cryptoloot.pro", "crypto-loot.com", "minero.cc",
        "coinnebula.com", "webmine.pro", "jsecoin.com",
        "nerohut.com", "hashunited.com", "minemytraffic.com",
        // Second-tier analytics / session replay / heatmaps:
        // dedicated collection hosts only (see the review note
        // above for the parent domains withdrawn in v0.94.0).
        "static.hotjar.com", "cdn.logrocket.io",
        "rec.smartlook.com", "sessioncam.com", "clicktale.net",
        "contentsquare.net", "glassboxdigital.io", "decibel.com",
        "getclicky.com"
    )

    private val blockedHosts: Set<String> = blockedHostList.toSet()
    private val strictHosts: Set<String> = strictHostList.toSet()

    /** How many hosts the built-in list carries (read by the settings page). */
    val blockedHostCount: Int
        get() = blockedHosts.size

    /** How many extra hosts the STRICT level adds (v0.92.0). */
    val strictHostCount: Int
        get() = strictHosts.size

    /**
     * Blocked when [url]'s host is a listed domain or a subdomain of
     * one. Lookup walks the host's own suffixes (at most a handful of
     * labels) against the set, so it stays O(1)-ish per request no
     * matter how large the list grows — it never scans the list.
     * With [strict] the strict list is consulted as well.
     */
    fun isAdRequest(url: String, strict: Boolean = false): Boolean {
        val host = hostOf(url) ?: return false
        var candidate = host
        while (true) {
            if (candidate in blockedHosts) return true
            if (strict && candidate in strictHosts) return true
            val dot = candidate.indexOf('.')
            if (dot < 0) return false
            candidate = candidate.substring(dot + 1)
        }
    }
}
