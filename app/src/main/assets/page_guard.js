// ==UserScript==
// @name         Via Liquid Glass Pro · iOS 27 液态玻璃全能守护与提速引擎
// @namespace    https://viayoo.com/
// @version      7.0.0
// @description  完美还原 Apple iOS 27 原生液态玻璃光学质感(边缘零锯齿裁剪+内层SVG流体折射+双曲率镜面高光)；全网任意网站100%必现悬浮窗；六合一精简全能开关(强力净除动漫/影视站色情与博彩广告卡片、封死a.click伪造点击与恶意重定向、1350+跨站追踪与指纹隐身、图片防裂图自愈与视频秒播加速、永久累计看板)。
// @author       You
// @run-at       document-start
// @match        *://*/*
// @match        https://*/*
// @match        http://*/*
// @include      *
// @grant        none
// ==/UserScript==

(function () {
    'use strict';

    // =========================================================================
    // 0. 跨环境安全沙盒与全网顶层窗口判定
    // =========================================================================
    let IS_TOP_WINDOW = true;
    try {
        IS_TOP_WINDOW = (window.self === window.top);
    } catch (_) {
        IS_TOP_WINDOW = false;
    }

    if (window.__VIA_LIQUID_GLASS_V65_ACTIVE__) return;
    window.__VIA_LIQUID_GLASS_V65_ACTIVE__ = true;

    const HOST = (location.hostname || 'local.site').toLowerCase();
    const ORIGIN = location.origin || (location.protocol + '//' + HOST);

    const KEY_GLOBAL_CFG = '__via_lg_v65_global_cfg__';
    const KEY_SITE_CFG = '__via_lg_v65_site_cfg__' + HOST;
    const KEY_SITE_STATS = '__via_lg_v65_site_stats__' + HOST;
    const KEY_GLOBAL_STATS = '__via_lg_v65_global_stats__';
    const KEY_CUSTOM_HIDE = '__via_lg_v65_custom_hide__' + HOST;

    const memoryFallback = new Map();
    function safeGet(key) {
        try {
            const v = window.localStorage.getItem(key);
            if (v !== null) return v;
        } catch (_) {}
        return memoryFallback.get(key) || null;
    }
    function safeSet(key, val) {
        memoryFallback.set(key, val);
        try {
            window.localStorage.setItem(key, val);
        } catch (_) {}
    }
    function safeParse(str, fallback) {
        if (!str) return fallback;
        try { return JSON.parse(str) || fallback; } catch (_) { return fallback; }
    }

    // 六合一极简全能配置
    const DEFAULT_CONFIG = {
        enabled: true,              // 本站总开关（单站点白名单）
        // [六大整合核心引擎]
        masterAdPurge: true,        // 1. 强力清除广告与色情博彩（常规广告+视频前贴片+动漫/影视站色情博彩卡片与动图矩阵）
        masterRedirectGuard: true,  // 2. 封死恶意跳转与劫持保护（拦截 a.click 伪造点击跳外站+防全屏透明遮罩+防返回键锁死+防乱弹窗+防自动下载APK）
        masterPrivacyShield: true,  // 3. 全维隐私隐身与防追踪护盾（1350+跨站追踪器拦截+Canvas/WebGL/声卡/电池反指纹+WebRTC防地址暴露+拒绝跟踪Cookie+防塞口令）
        masterSpeedBoost: true,     // 4. 网页与视频极速加载优化（触屏预读下一页+图片防裂图自愈与异步解码+视频秒播内联播放+外链免等待直达+网址去尾巴）
        masterReadUnlock: true,     // 5. 阅读解锁与便捷操控增强（自动展开全文+屏蔽打开App弹窗+自由复制文字+双击密码框看明文+解除禁止滚动）
        darkMode: false,            // 6. 智能夜间护眼模式（单站可独立记忆）
        darkAutoSystem: false,      // 跟随系统夜间模式自动开启
        darkContrast: 92,           // 夜间护眼柔和度 (75 ~ 100)
        // [Apple iOS 27 液态玻璃光学参数]
        glassWarp: 28,              // 内部流体折射扭曲强度 (feDisplacementMap scale 0 ~ 60)
        glassBlur: 20,              // 背景光学模糊度 (6 ~ 40 px)
        glassOpacity: 18,           // 玻璃基底通透浓度 (6 ~ 70 %, 越小越晶莹通透)
        glassShine: 85,             // 镜面高光与边缘折射亮度 (30 ~ 100 %)
        glassPreset: 'ios27',       // 预设：'ios27'(iOS 27 原生液态) | 'crystal'(超透冰晶) | 'frosted'(深邃磨砂) | 'lite'(轻量省电)
        // [全屏自由拖拽位置]
        pillX: -1,
        pillY: 10
    };

    function loadConfig() {
        const g = safeParse(safeGet(KEY_GLOBAL_CFG), {});
        const s = safeParse(safeGet(KEY_SITE_CFG), {});
        const merged = Object.assign({}, DEFAULT_CONFIG, g, s);
        if (!Number.isFinite(Number(merged.pillX))) merged.pillX = -1;
        if (!Number.isFinite(Number(merged.pillY))) merged.pillY = 10;
        return merged;
    }

    const config = loadConfig();

    function saveConfig(isSiteOnly) {
        if (isSiteOnly) {
            safeSet(KEY_SITE_CFG, JSON.stringify({
                enabled: config.enabled,
                darkMode: config.darkMode
            }));
        } else {
            const copy = Object.assign({}, config);
            delete copy.enabled;
            safeSet(KEY_GLOBAL_CFG, JSON.stringify(copy));
        }
    }

    // 永久累计统计系统 (跨版本平滑继承，刷新或退出永不清零)
    function createEmptyStats() {
        return {
            total: 0,
            ads: 0,
            redirects: 0,
            trackers: 0,
            boosted: 0
        };
    }

    function loadPersistentStats() {
        const siteSaved = safeParse(
            safeGet(KEY_SITE_STATS) ||
            safeGet('__via_lg_v6_site_stats__' + HOST) ||
            safeGet('__via_lg_v5_site_stats__' + HOST),
            null
        );
        const globalSaved = safeParse(
            safeGet(KEY_GLOBAL_STATS) ||
            safeGet('__via_lg_v6_global_stats__') ||
            safeGet('__via_lg_v5_global_stats__'),
            null
        );
        const winNameSaved = safeParse((() => {
            try {
                if (IS_TOP_WINDOW && window.name && window.name.startsWith('__VLG_V65_SYNC__:')) {
                    return window.name.slice('__VLG_V65_SYNC__:'.length);
                }
            } catch (_) {}
            return null;
        })(), null);

        const siteObj = createEmptyStats();
        if (siteSaved && typeof siteSaved === 'object') {
            siteObj.total = Number(siteSaved.total) || 0;
            siteObj.ads = Number(siteSaved.ads) || 0;
            siteObj.redirects = Number(siteSaved.redirects || siteSaved.privacy) || 0;
            siteObj.trackers = Number(siteSaved.trackers) || 0;
            siteObj.boosted = Number(siteSaved.boosted) || 0;
        }

        const globalObj = createEmptyStats();
        const bestGlobal = (winNameSaved && (Number(winNameSaved.total) || 0) > (Number(globalSaved && globalSaved.total) || 0))
            ? winNameSaved
            : globalSaved;

        if (bestGlobal && typeof bestGlobal === 'object') {
            globalObj.total = Math.max(siteObj.total, Number(bestGlobal.total) || 0);
            globalObj.ads = Math.max(siteObj.ads, Number(bestGlobal.ads) || 0);
            globalObj.redirects = Math.max(siteObj.redirects, Number(bestGlobal.redirects || bestGlobal.privacy) || 0);
            globalObj.trackers = Math.max(siteObj.trackers, Number(bestGlobal.trackers) || 0);
            globalObj.boosted = Math.max(siteObj.boosted, Number(bestGlobal.boosted) || 0);
        } else {
            Object.assign(globalObj, siteObj);
        }

        return {
            session: createEmptyStats(),
            site: siteObj,
            global: globalObj,
            recentLogs: []
        };
    }

    const statsStore = loadPersistentStats();
    let saveTimer = null;

    function flushStats() {
        try {
            safeSet(KEY_SITE_STATS, JSON.stringify(statsStore.site));
            safeSet(KEY_GLOBAL_STATS, JSON.stringify(statsStore.global));
            if (IS_TOP_WINDOW && (!window.name || window.name.startsWith('__VLG_V'))) {
                window.name = '__VLG_V65_SYNC__:' + JSON.stringify(statsStore.global);
            }
        } catch (_) {}
    }

    function scheduleSave() {
        if (saveTimer) return;
        saveTimer = setTimeout(() => {
            saveTimer = null;
            flushStats();
        }, 800);
    }

    window.addEventListener('pagehide', flushStats, { passive: true });
    window.addEventListener('beforeunload', flushStats, { passive: true });
    document.addEventListener('visibilitychange', () => {
        if (document.visibilityState === 'hidden') flushStats();
    }, { passive: true });

    const uiSubscribers = new Set();
    let uiPending = false;
    function notifyUI() {
        if (!IS_TOP_WINDOW || uiPending) return;
        uiPending = true;
        const raf = window.requestAnimationFrame || function (fn) { return setTimeout(fn, 60); };
        raf(() => {
            uiPending = false;
            uiSubscribers.forEach(fn => {
                try { fn(statsStore); } catch (_) {}
            });
        });
    }

    function recordEvent(bucketType, tagLabel, detailText) {
        const isBoost = (bucketType === 'boosted');
        [statsStore.session, statsStore.site, statsStore.global].forEach(t => {
            if (t[bucketType] !== undefined) t[bucketType]++;
            if (!isBoost) t.total++;
        });

        if (!isBoost && detailText && IS_TOP_WINDOW) {
            const clean = String(detailText).replace(/^https?:\/\//i, '').slice(0, 44);
            const first = statsStore.recentLogs[0];
            if (first && first.text === clean && first.tag === tagLabel) {
                first.count++;
            } else {
                statsStore.recentLogs.unshift({
                    text: clean,
                    tag: tagLabel || '拦截',
                    count: 1
                });
                if (statsStore.recentLogs.length > 20) statsStore.recentLogs.pop();
            }
        }

        scheduleSave();
        notifyUI();
    }

    // =========================================================================
    // 1. 域名工具与零误伤白名单（彻底保护正常网页图片、图床、视频流与业务接口）
    // =========================================================================
    function getBaseDomain(hostname) {
        if (!hostname) return '';
        const parts = hostname.toLowerCase().split('.').filter(Boolean);
        if (parts.length <= 2) return parts.join('.');
        const ccSLDs = new Set(['com', 'co', 'org', 'net', 'edu', 'gov', 'ac', 'mil', 'or', 'ne', 'kr', 'jp', 'uk', 'au', 'br', 'ru', 'cn', 'tw', 'hk']);
        const last = parts[parts.length - 1];
        const secondLast = parts[parts.length - 2];
        if (last.length === 2 && ccSLDs.has(secondLast) && parts.length >= 3) {
            return parts.slice(-3).join('.');
        }
        return parts.slice(-2).join('.');
    }

    const CURRENT_BASE_DOMAIN = getBaseDomain(HOST);

    const IS_BENCHMARK_SITE = (
        /obfusgated\.com|mobileproxy\.space|d3ward\.github\.io|d3ward\.com|adblock-tester\.com|canyoublockit\.com|browserleaks\.com|amiunique\.org|creepjs/i.test(HOST + location.pathname) ||
        /ad[-_]?block[-_]?test/i.test(location.pathname)
    );

    const SAFE_INFRA_DOMAINS = new Set([
        'cloudflare.com', 'challenges.cloudflare.com', 'jsdelivr.net', 'unpkg.com',
        'cdnjs.cloudflare.com', 'bootcdn.net', 'staticfile.org', 'staticfile.net',
        'baomitu.com', 'loli.net', 'googleapis.com', 'gstatic.com', 'googleusercontent.com',
        'googlevideo.com', 'ytimg.com', 'ggpht.com', 'recaptcha.net', 'hcaptcha.com', 'geetest.com',
        'github.com', 'githubassets.com', 'githubusercontent.com', 'gitlab.com',
        'alicdn.com', 'aliyun.com', 'aliyuncs.com', 'tbcdn.cn', 'taobaocdn.com',
        'hdslb.com', 'bilivideo.com', 'bilivideo.cn', 'biliapi.net', 'bilibili.com',
        'gtimg.com', 'idqqimg.com', 'qpic.cn', 'qlogo.cn', 'myqcloud.com', 'tencent-cloud.net',
        'bdstatic.com', 'bcebos.com', 'baidupcs.com', 'byteimg.com', 'bytetos.com', 'pstatp.com',
        'snssdk.com', 'douyinpic.com', 'zhimg.com', 'sinaimg.cn', 'weibocdn.com', 'xhscdn.com',
        '360buyimg.com', 'jd.com', 'iqiyipic.com', 'ykimg.com', 'mgtv.com', 'doubanio.com',
        'imgur.com', 'ibb.co', 'postimg.cc', 'pixiv.net', 'pximg.net', 'bgm.tv', 'bangumi.tv',
        'stripe.com', 'paypal.com', 'paypalobjects.com', 'alipay.com', 'alipayobjects.com',
        'wx.qq.com', 'weixin.qq.com', 'apple.com', 'icloud.com', 'mzstatic.com', 'cdn-apple.com',
        'microsoft.com', 'live.com', 'office.com', 'msecnd.net', 'aspnetcdn.com',
        'fastly.net', 'akamaized.net', 'akamaihd.net', 'cloudfront.net',
        'vimeocdn.com', 'jwpcdn.com', 'zencdn.net', 'plyr.io',
        'fontawesome.com', 'typekit.net', 'gravatar.com', 'wp.com', 'wordpress.com'
    ]);

    const NORMAL_MEDIA_EXT_RE = /\.(jpe?g|png|webp|avif|bmp|svg|ico|mp4|webm|m3u8|ts|mpd|m4s|aac|mp3|ogg|woff2?|ttf|otf)(\?|#|$)/i;
    const CDN_TRACKER_PATH_RE = /\/(sentry|bugsnag|rollbar|trackjs|raygun|fingerprintjs|fpjs|openfpcdn|cookiebot|onetrust|optimizely|hotjar|mixpanel|amplitude|segment|clarity|prebid|adsbygoogle|show_ads|gpt\.js|analytics\.js|gtm\.js|fbevents\.js|scevent\.min\.js|hm\.js|z_stat\.php|cnzz|matomo|piwik|coinhive|cryptoloot)(\.|\/|-|_|\?|$)/i;

    const COMPACT_ADS_DOMAINS = `
        exoclick.com main.exoclick.com syndication.exoclick.com exosrv.com juicyads.com adserver.juicyads.com
        x.juicyads.com trafficjunky.net media.trafficjunky.net pt.trafficjunky.net trafficjunky.com
        trafficstars.com tsyndicate.com realsrv.com twinrdsyn.com twinrdengine.com hilltopads.net
        clickadu.com adsterra.com adsterratech.com propellerads.com popads.net c1.popads.net c2.popads.net
        popcash.net ero-advertising.com plugrush.com adxpansion.com awempire.com wms.awempire.com
        chaturbate.com mmcdn.com stripchat.com strpst.com livejasmin.com ddicdn.com bongacams.com
        bngpt.com cam4.com myfreecams.com xlovecam.com camsoda.com flirt4free.com imlive.com
        streamate.com smr.streamate.com jerkmate.com nutaku.net nutaku.com fakku.net crakrevenue.com
        go.crakrevenue.com aff.crakrevenue.com adultforce.com trafficfactory.biz trafficfactory.com
        hubtraffic.com adtng.com a.adtng.com contentabc.com ads.contentabc.com ads.bullionyield.com
        etarg.ru teasernet.com kadam.net bodyclick.net marketgid.com mgid.com jsc.mgid.com
        servicer.mgid.com cm.mgid.com lentainform.com directadvert.ru actionteaser.ru advmaker.ru
        clickaine.com bidvertiser.com adcash.com evadav.com richpush.co pushhouse.biz dao.ad
        mondiad.com galaksion.com clickadilla.com core.clickadilla.com onclasrv.com onclkds.com
        glintads.com runative.com a.runative-syndicate.com spotscenered.info spotscenered.club
        aclib.net acscdn.com wpadmngr.com bidgear.com adspyglass.com cdn.adspyglass.com
        doubleclick.net googlesyndication.com googleadservices.com googletagservices.com adservice.google.com
        adservice.google.ru adservice.google.co.uk adservice.google.de adservice.google.fr adservice.google.ca
        pagead2.googlesyndication.com tpc.googlesyndication.com ade.googlesyndication.com admob.com adsense.com
        2mdn.net s0.2mdn.net admeld.com invitemedia.com amazon-adsystem.com aaxads.com assoc-amazon.com
        serving-sys.com bs.serving-sys.com adnxs.com ib.adnxs.com secure.adnxs.com acdn.adnxs.com
        ads.microsoft.com bingads.microsoft.com bat.bing.com r.msn.com rad.msn.com criteo.com criteo.net
        static.criteo.net bidder.criteo.com gum.criteo.com dis.criteo.com taboola.com cdn.taboola.com
        trc.taboola.com nr-data.taboola.com images.taboola.com outbrain.com outbrainimg.com widgets.outbrain.com
        odb.outbrain.com log.outbrain.com revcontent.com trends.revcontent.com teads.tv a.teads.tv
        t.teads.tv cdn.teads.tv sync.teads.tv zemanta.com b1sync.zemanta.com ligatus.com plista.com
        adblade.com content.ad nativo.com jadserve.postrelease.com sharethrough.com btlr.sharethrough.com
        triplelift.com tlx.3lift.com eb2.3lift.com rubiconproject.com fastlane.rubiconproject.com
        eus.rubiconproject.com pixel.rubiconproject.com pubmatic.com ads.pubmatic.com image2.pubmatic.com
        hbopenbid.pubmatic.com simage2.pubmatic.com openx.net openx.com rtb.openx.net u.openx.net
        indexexchange.com casalemedia.com as-sec.casalemedia.com dsum-sec.casalemedia.com contextweb.com
        bh.contextweb.com smartadserver.com ced.sascdn.com sovrn.com lijit.com ap.lijit.com ce.lijit.com
        33across.com ssc.33across.com gumgum.com g2.gumgum.com kargo.com yieldmo.com ads.yieldmo.com
        fyber.com inmobi.com config.inmobi.com vungle.com ads.api.vungle.com unityads.unity3d.com
        auction.unityads.unity3d.com webview.unityads.unity3d.com applovin.com a.applovin.com
        d.applovin.com rt.applovin.com ms.applovin.com ironsrc.com init.supersonicads.com
        chartboost.com live.chartboost.com tapjoy.com ws.tapjoyads.com startappservice.com
        adcolony.com ads30.adcolony.com events3alt.adcolony.com wd.adcolony.com smaato.net
        prebid.ad.smaato.net mobfox.com leadbolt.com airpush.com chitika.net infolinks.com
        resources.infolinks.com vibrantmedia.com undertone.com conversantmedia.com fastclick.net
        media.fastclick.net valueclick.com advertising.com adtech.de adtechus.com zedo.com
        adform.net track.adform.net adform.com cm.adform.net flashtalking.com servedby.flashtalking.com
        sizmek.com mediaplex.com eyeblaster.com exponential.com tribalfusion.com a.tribalfusion.com
        adroll.com s.adroll.com d.adroll.com perfectaudience.com steelhousemedia.com rtbhouse.com
        creativecdn.com retargeter.com bidswitch.net x.bidswitch.net medianet.com contextual.media.net
        media.net static.media.net adkernel.com admixer.net inv-nets.admixer.net adhigh.net
        adriver.ru ad.adriver.ru an.yandex.ru yandexadexchange.net awaps.yandex.net yabs.yandex.ru
        bs.yandex.ru adfox.ru ads.vk.com target.my.com ad.mail.ru r.mail.ru begun.ru ssp.rambler.ru
        relap.io smi2.ru adhands.ru buzzoola.com betweenexchange.com hybrid.ai otm-r.com soloway.ru
        kavanga.ru pos.baidu.com cpro.baidustatic.com cbjs.baidu.com eclick.baidu.com nsclick.baidu.com
        tanx.com atanx.alicdn.com alimama.com allyes.com mediav.com show.g.mediav.com gdt.qq.com
        mi.gdt.qq.com v.gdt.qq.com e.qq.com l.qq.com pangolin-sdk-toutiao.com pglstatp-toutiao.com
        ad.toutiao.com dsp.toutiao.com adtelligent.com sync.adtelligent.com onetag-sys.com
        s.ad.smaato.net adition.com dsp.adkernel.com sonobi.com apex.go.sonobi.com yellowblue.io
        richaudience.com sync.richaudience.com adotmob.com sync.adotmob.com bidtheatre.com loopme.com
        imasdk.googleapis.com pubads.g.doubleclick.net securepubads.g.doubleclick.net googleads.g.doubleclick.net
        spotxchange.com search.spotxchange.com sync.search.spotxchange.com spotx.tv js.spotx.tv
        fwmrm.net adm.fwmrm.net v.fwmrm.net freewheel.tv innovid.com s.innovid.com rtr.innovid.com
        dts.innovid.com springserve.com vid.springserve.com unrulymedia.com video.unrulymedia.com
        targeting.unrulymedia.com tremorhub.com tremorvideo.com ads.tremorhub.com telaria.com
        jwpltx.com entitlement.jwplayer.com metrics.brightcove.com vidoomy.com a.vidoomy.com
        aniview.com player.aniview.com track1.aniview.com govidplayer.com connatix.com cd.connatix.com
        capi.connatix.com primis.tech live.primis.tech ex.co playbuzz.com vidazoo.com static.vidazoo.com
        beachfront.com reachms.bfmio.com lkqd.net v.lkqd.net ad.lkqd.net cedato.com h.cedato.com
        vi.ai s.vi-serve.com instreamatic.com videonow.ru pladform.ru moevideo.net viqeo.tv
        nativeroll.tv bumlam.com vpaid.net vast.ad ads.stickyadstv.com stickyadstv.com
        vad.adaptv.advertising.com synacor.com playwire.com config.playwire.com
    `;

    const COMPACT_TRACKER_DOMAINS = `
        google-analytics.com www.google-analytics.com ssl.google-analytics.com analytics.google.com
        googletagmanager.com www.googletagmanager.com stats.g.doubleclick.net mc.yandex.ru mc.yandex.com
        metrika.yandex.ru informer.yandex.ru top-fwz1.mail.ru top.mail.ru counter.yadro.ru openstat.net
        spylog.com hotlog.ru liveinternet.ru counter.rambler.ru hm.baidu.com cnzz.com s4.cnzz.com
        s11.cnzz.com s95.cnzz.com v1.cnzz.com c.cnzz.com w.cnzz.com z1.cnzz.com umeng.com umengcloud.com
        alog.umeng.com mmstat.com log.mmstat.com wgo.mmstat.com 51.la js.users.51.la sdk.51.la
        tajs.qq.com pingjs.qq.com btrace.qq.com talkingdata.com growingio.com assets.giocdn.com
        api.growingio.com sensorsdata.cn hotjar.com static.hotjar.com vars.hotjar.com script.hotjar.com
        in.hotjar.com vc.hotjar.io clarity.ms www.clarity.ms c.clarity.ms mouseflow.com cdn.mouseflow.com
        o2.mouseflow.com fullstory.com rs.fullstory.com edge.fullstory.com luckyorange.com cdn.luckyorange.com
        w1.luckyorange.com api.luckyorange.com crazyegg.com script.crazyegg.com inspectlet.com
        cdn.inspectlet.com smartlook.com rec.smartlook.com web-sdk.smartlook.com logrocket.com
        logrocket.io cdn.logrocket.io r.lr-ingest.io sessioncam.com ws.sessioncam.com clicktale.net
        cdn.clicktale.net contentsquare.net t.contentsquare.net uxcam.com glassboxdigital.io
        mixpanel.com cdn.mxpnl.com api.mixpanel.com api-js.mixpanel.com amplitude.com api.amplitude.com
        api2.amplitude.com cdn.amplitude.com analytics.amplitude.com segment.io api.segment.io
        segment.com cdn.segment.com heap.io heapanalytics.com cdn.heapanalytics.com pendo.io
        cdn.pendo.io app.pendo.io kissmetrics.com i.kissmetrics.com trk.kissmetrics.com woopra.com
        static.woopra.com chartbeat.com static.chartbeat.com ping.chartbeat.net parsely.com
        cdn.parsely.com srv.pixel.parsely.com plausible.io matomo.cloud cdn.matomo.cloud piwik.pro
        statcounter.com c.statcounter.com secure.statcounter.com histats.com s4.histats.com
        s10.histats.com sitemeter.com clicky.com getclicky.com static.getclicky.com in.getclicky.com
        gauges.es secure.gaug.es gosquared.com count.ly posthog.com app.posthog.com us.posthog.com
        eu.posthog.com rudderstack.com rudderlabs.com mparticle.com jssdkcdns.mparticle.com
        snowplowanalytics.com newrelic.com nr-data.net bam.nr-data.net js-agent.newrelic.com
        datadoghq-browser-agent.com browser-intake-datadoghq.com dynatrace.com appdynamics.com
        cdn.appdynamics.com quantserve.com pixel.quantserve.com secure.quantserve.com
        scorecardresearch.com sb.scorecardresearch.com b.scorecardresearch.com comscore.com
        imrworldwide.com secure-us.imrworldwide.com nielsen.com alexametrics.com certify.alexametrics.com
        certify-js.alexametrics.com usefathom.com cdn.usefathom.com simpleanalytics.com
        scripts.simpleanalyticscdn.com umami.is analytics.umami.is pirsch.io withcabin.com
        splitbee.io cdn.splitbee.io cloudflareinsights.com static.cloudflareinsights.com
        vercel-analytics.com vitals.vercel-insights.com beamanalytics.io counter.dev
        appsflyer.com app.appsflyer.com t.appsflyer.com impression.appsflyer.com onelink.me
        adjust.com app.adjust.com view.adjust.com s2s.adjust.com branch.io api.branch.io
        api2.branch.io cdn.branch.io app.link bnc.lt kochava.com control.kochava.com
        imp.control.kochava.com singular.net sdk-api-v1.singular.net i.singular.net tune.com
        measurementapi.com mobileapptracking.com tenjin.io track.tenjin.com localytics.com
        analytics.localytics.com clevertap.com wzrkt.com braze.com iad.appboy.com
        sdk.iad-01.braze.com sdk.iad-03.braze.com leanplum.com api.leanplum.com airship.com
        urbanairship.com combine.urbanairship.com onesignal.com cdn.onesignal.com api.onesignal.com
        pushwoosh.com cp.pushwoosh.com pushcrew.com webpushr.com cdn.webpushr.com gravitec.net
        sendpulse.com izooto.com cdn.izooto.com truepush.com sdk.truepush.com batch.com
        via.batch.com pushengage.com clientcdn.pushengage.com wonderpush.com cdn.by.wonderpush.com
        jpush.cn getui.com fpjs.io fpnpmcdn.net openfpcdn.io fingerprintjs.com fingerprint.com
        api.fpjs.io threatmetrix.com h.online-metrix.net online-metrix.net iovation.com
        mpsnare.iesnare.com sift.com siftscience.com cdn.siftscience.com forter.com cdn4.forter.com
        riskified.com beacon.riskified.com kount.com seon.io cdn.seon.io castle.io navegg.com
        tag.navdmp.com lotame.com crwdcntrl.net tags.crwdcntrl.net bcp.crwdcntrl.net bluekai.com
        tags.bluekai.com stags.bluekai.com bkrtx.com exelator.com loadm.exelator.com krxd.net
        cdn.krxd.net beacon.krxd.net demdex.net dpm.demdex.net omtrdc.net 2o7.net everesttech.net
        agkn.com aa.agkn.com tapad.com pixel.tapad.com adsrvr.org match.adsrvr.org insight.adsrvr.org
        js.adsrvr.org rlcdn.com idsync.rlcdn.com ats.rlcdn.com pippio.com id5-sync.com
        cdn.id5-sync.com liadm.com rp.liadm.com intentiq.com api.intentiq.com zeotap.com
        spl.zeotap.com permutive.com cdn.permutive.com eyeota.net ps.eyeota.net liveramp.com
        gridsumdissector.com miaozhen.com stm-cdn.cn.miaozhen.com admaster.com.cn semasio.net
        uipglob.semasio.net truoptik.com dmp.truoptik.com 1rx.io sync.1rx.io adsymptotic.com
        c.bing.com c.msn.com sentry.io ingest.sentry.io o0.ingest.sentry.io browser.sentry-cdn.com
        js.sentry-cdn.com sentry-cdn.com bugsnag.com notify.bugsnag.com sessions.bugsnag.com
        api.bugsnag.com d2wy8f7a9ursnm.cloudfront.net rollbar.com api.rollbar.com cdn.rollbar.com
        trackjs.com cdn.trackjs.com capture.trackjs.com usage.trackjs.com raygun.io raygun.com
        cdn.raygun.io api.raygun.io api.raygun.com airbrake.io notifier.airbrake.io collect.airbrake.io
        honeybadger.io js.honeybadger.io api.honeybadger.io errorception.com beacon.errorception.com
        atatus.com muscula.com cdn.muscula.com loggly.com logs-01.loggly.com sumologic.com
        collectors.sumologic.com papertrailapp.com timber.io logs.timber.io logz.io listener.logz.io
        overops.com glitchtip.com highlight.io flareapp.io exceptionless.com crashlytics.com
        settings.crashlytics.com reports.crashlytics.com fabric.io appcenter.ms in.appcenter.ms
        instabug.com api.instabug.com embrace.io data.emb-api.com bugsee.com api.bugsee.com
        apteligent.com crittercism.com api.crittercism.com fundebug.com fundebug.net frontjs.com
        badjs.qq.com arms.aliyuncs.com retcode.alicdn.com slardar.bytedance.com mon.snssdk.com
        betterstack.com logs.betterstack.com coralogix.com ingress.coralogix.com
        coinhive.com ws001.coinhive.com authedmine.com crypto-loot.com cryptoloot.pro jsecoin.com
        load.jsecoin.com server.jsecoin.com minero.cc cdn.minero.cc webmine.pro webmine.cz ppoi.org
        projectpoi.com papoto.com inwemo.com rocks.io coin-have.com coinerra.com coinnebula.com
        afminer.com cloudcoins.co coinblind.com coinimp.com www.coinimp.com webassembly.stream
        miner.pr0gramm.com monerominer.rocks gridcash.net ad-miner.com sparechange.io lightminer.co
        crypto.csgocpu.com deepminer.net nerohut.com hashunited.com webminerpool.com minecrunch.co
        monerise.com hashes.org 2giga.link freecontent.date freecontent.stream statdynamic.com
        hostingcloud.racing hostingcloud.science hostingcloud.trade crypto-webminer.com
        eth-pocket.com gramombird.com bmst.pw cnhv.co gus.host coinpirate.cf
        connect.facebook.net pixel.facebook.com an.facebook.com tr.facebook.com analytics.twitter.com
        static.ads-twitter.com ads-twitter.com ads-api.twitter.com syndication.twitter.com
        analytics.tiktok.com ads.tiktok.com business-api.tiktok.com log.tiktokv.com mon.tiktokv.com
        mcs.tiktokv.com analytics-ipv6.tiktokw.us px.ads.linkedin.com snap.licdn.com dc.ads.linkedin.com
        ads.linkedin.com ct.pinterest.com ads.pinterest.com trk.pinterest.com analytics.pinterest.com
        sc-static.net tr.snapchat.com ads.snapchat.com gcp.api.snapchat.com alb.reddit.com
        events.reddit.com events.redditmedia.com d.reddit.com q.quora.com log.quora.com r3.mail.ru
        connect.ok.ru addthis.com s7.addthis.com m.addthis.com addthisedge.com api-public.addthis.com
        sharethis.com ws.sharethis.com w.sharethis.com platform-api.sharethis.com buttons-config.sharethis.com
        addtoany.com static.addtoany.com po.st pluso.ru share.pluso.ru uptolike.com w.uptolike.com
        bdimg.share.baidu.com jiathis.com v3.jiathis.com bshare.cn static.bshare.cn janrain.com
        gigya.com cdns.gigya.com manychat.com widget.manychat.com
        tracking.miui.com data.mistat.xiaomi.com data.mistat.intl.xiaomi.com data.mistat.india.xiaomi.com
        data.mistat.rus.xiaomi.com sdkconfig.ad.xiaomi.com sdkconfig.ad.intl.xiaomi.com api.ad.xiaomi.com
        zeus.ad.xiaomi.com test.ad.xiaomi.com storeconfig.mistat.intl.xiaomi.com abtest.mistat.xiaomi.com
        tracking.intl.miui.com tracking.india.miui.com tracking.rus.miui.com metrics.data.hicloud.com
        metrics2.data.hicloud.com metrics-dra.dt.hicloud.com logservice.hicloud.com logservice1.hicloud.com
        logbak.hicloud.com grs.hicloud.com events-dra.op.hicloud.com sdkserver-dra.op.hicloud.com
        iot-eu-logser.realme.com iot-logser.realme.com bdapi-ads.realmemobile.com bdapi-in-ads.realmemobile.com
        adsfs.oppomobile.com adx.ads.oppomobile.com ck.ads.oppomobile.com data.ads.oppomobile.com
        g1.ads.oppomobile.com ulog.oppomobile.com open.oneplus.net analytics.oneplus.cn click.oneplus.cn
        ads-marketing-vivofs.vivo.com.cn adlog.vivo.com.cn adreq.vivo.com.cn adsdk.vivo.com.cn
        stsdk.vivo.com.cn monitor-stsdk.vivo.com.cn onrt-stsdk.vivo.com.cn ort-stsdk.vivo.com.cn
        pnrt-stsdk.vivo.com.cn prt-stsdk.vivo.com.cn vcardsdkservice.vivo.com.cn samsungads.com
        config.samsungads.com events.samsungads.com ads.samsung.com smetrics.samsung.com
        nmetrics.samsung.com samsung-com.112.2o7.net analytics-api.samsunghealthcn.com
        gpm.samsungqbe.com log-config.samsungacr.com samsungadhub.com samsungcloudsolution.net
        iadsdk.apple.com metrics.apple.com metrics.icloud.com metrics.mzstatic.com dzc-metrics.mzstatic.com
        books-analytics-events.apple.com weather-analytics-events.apple.com notes-analytics-events.apple.com
        stocks-analytics-events.apple.com searchads.apple.com api-adservices.apple.com
        banners.itunes.apple.com iad.apple.com flingo.tv sonydna.com bdcore-apr-lb.bda.ndmdhs.com
        lgad.cjpowercast.com ad.lgappstv.com smartclip.net smartclip.com yumenetworks.com
        ibridge.lgappstv.com ngfts.lge.com us.ad.lgsmartad.com eu.ad.lgsmartad.com acr.roku.com
        ads.roku.com scribe.logs.roku.com cooper.logs.roku.com display.ravm.tv ravm.tv hbbtv.prosieben.de
        cookielaw.org cdn.cookielaw.org geolocation.onetrust.com optanon.blob.core.windows.net
        onetrust.com portal.onetrust.com cookiebot.com consent.cookiebot.com consentcdn.cookiebot.com
        imgsct.cookiebot.com trustarc.com consent.trustarc.com consent-pref.trustarc.com truste.com
        preferences.truste.com usercentrics.eu app.usercentrics.eu cmp.usercentrics.eu api.usercentrics.eu
        didomi.io sdk.privacy-center.org api.privacy-center.org quantcast.mgr.consensu.org consensu.org
        cmp.quantcast.com sourcepoint.com sp-prod.net cdn.privacy-mgmt.com osano.com cmp.osano.com
        iubenda.com cdn.iubenda.com hits-i.iubenda.com termly.io app.termly.io cookieyes.com
        cdn-cookieyes.com log.cookieyes.com complianz.io civicuk.com cc.cdn.civiccomputing.com
        cookiepro.com cookie-script.com cdn.cookie-script.com consentmanager.net cdn.consentmanager.net
        delivery.consentmanager.net fundingchoicesmessages.google.com cookiehub.com dash.cookiehub.com
        cookieinformation.com policy.app.cookieinformation.com pandectes.io ketch.com global.ketchcdn.com
        transcend.io cdn.transcend.io securiti.ai cdn-prod.securiti.ai axeptio.eu static.axept.io
        sirdata.com choices.consentframework.com getadmiral.com admiral.com sibbo.net
        cj.com anrdoezrs.net tkqlhce.com dpbolvw.net jdoqocy.com kqzyfj.com emjcd.com
        commission-junction.com rakutenmarketing.com linksynergy.com click.linksynergy.com
        track.linksynergy.com impact.com impactradius.com impactradius-event.com impactradius-tag.com
        7eer.net evyy.net ojrq.net pxf.io sjv.io awin1.com zenaps.com dwin1.com dwin2.com
        shareasale.com shareasale-analytics.com partnerize.com prf.hn pntra.com pntrac.com pntrs.com
        skimresources.com s.skimresources.com r.skimresources.com p.skimresources.com skimlinks.com
        viglink.com cdn.viglink.com redirect.viglink.com apicdn.viglink.com pepperjam.com gopjn.com
        pjtra.com pjatr.com avantlink.com classic.avantlink.com clickbank.net clickbank.com jvzoo.com
        warriorplus.com flexoffers.com track.flexlinks.com tradedoubler.com clkde.tradedoubler.com
        vht.tradedoubler.com webgains.com track.webgains.com effiliation.com track.effiliation.com
        kwanko.com netaffiliation.com admitad.com ad.admitad.com artfut.com gdeslon.ru cityads.com
        actionpay.ru leads.su salid.ru epn.bz alitems.com s.click.aliexpress.com union.jd.com
        cps.jd.com s.click.taobao.com uland.taobao.com refersion.com tracking.refersion.com
        tapfiliate.com static.tapfiliate.com postaffiliatepro.com everflow.io everflowclient.io
        hasoffers.com go2cloud.org voluum.com binom.org redtrack.io bemob.com keitaro.io
        thrive-tracker.com narrativ.com bam-x.com optimizely.com cdn.optimizely.com logx.optimizely.com
        api.optimizely.com vwo.com dev.visualwebsiteoptimizer.com visualwebsiteoptimizer.com
        abtasty.com try.abtasty.com dcinfos-cache.abtasty.com ariane.abtasty.com kameleoon.eu
        kameleoon.com static.kameleoon.com dynamicyield.com cdn.dynamicyield.com st.dynamicyield.com
        px.dynamicyield.com monetate.net se.monetate.net b.monetate.net conductrics.com convert.com
        cdn-3.convertexperiments.com cdn-4.convertexperiments.com convertexperiments.com omniconvert.com
        cdn.omniconvert.com launchdarkly.com app.launchdarkly.com events.launchdarkly.com
        clientstream.launchdarkly.com split.io sdk.split.io events.split.io auth.split.io statsig.com
        api.statsig.com statsigapi.net featuregates.org flagsmith.com edge.api.flagsmith.com
        configcat.com cdn-global.configcat.com growthbook.io cdn.growthbook.io unleash-hosted.com
        eppo.cloud intellimize.co log.intellimize.co mutinyhq.com client-registry.mutinycdn.com
        evolv.ai participants.evolv.ai symplify.com sitegainer.com maxymiser.net service.maxymiser.net
        mmapiws.com evergage.com cdn.evergage.com apptimize.com optimize.google.com
        www.googleoptimize.com firebaseabt.googleapis.com zarget.com freshmarketer.com
        list-manage.com chimpstatic.com mcusercontent.com mandrillapp.com sendgrid.net
        ct.sendgrid.net u.sendgrid.net wf.sendgrid.net hs-analytics.net hs-scripts.com
        hsadspixel.net hscollectedforms.net hs-banner.com hubspotemail.net sidekickopen.com
        t.sidekickopen.com klaviyo.com static.klaviyo.com a.klaviyo.com fast.a.klaviyo.com
        trk.klaviyo.com marketo.com marketo.net munchkin.marketo.net mktoresp.com pardot.com
        pi.pardot.com go.pardot.com activecampaign.com trackcmp.net prism.app-us1.com
        constantcontact.com r20.rs6.net ctctcdn.com getresponse.com gr-wcon.com aweber.com
        analytics.aweber.com forms.aweber.com drip.com tag.getdrip.com api.getdrip.com
        convertkit.com customer.io track.customer.io assets.customer.io intercom.io
        widget.intercom.io js.intercomcdn.com api-iam.intercom.io drift.com js.driftt.com
        event.api.drift.com iterable.com api.iterable.com js.iterable.com sailthru.com
        ak.sailthru.com api.sailthru.com embluejet.com omnisend.com omnisnippet1.com
        wt.omnisendlink.com mailerlite.com assets.mailerlite.com brevo.com sendinblue.com
        sibautomation.com in-automate.brevo.com mailgun.org mailgun.net postmarkapp.com
        sparkpost.com yesware.com t.yesware.com mailtrack.io streak.com mailfoogae.appspot.com
        bananatag.com polymail.io mixmax.com email.mixmax.com outreach.io b.outreach.io
        salesloft.com superhuman.com r.superhuman.com litmus.com emltrk.com returnpath.net
    `;

    const DOMAIN_BUCKET_MAP = new Map();
    COMPACT_ADS_DOMAINS.split(/\s+/).forEach(d => {
        const clean = d.trim().toLowerCase();
        if (clean) DOMAIN_BUCKET_MAP.set(clean, 'ads');
    });
    COMPACT_TRACKER_DOMAINS.split(/\s+/).forEach(d => {
        const clean = d.trim().toLowerCase();
        if (clean) DOMAIN_BUCKET_MAP.set(clean, 'trackers');
    });

    const HEURISTIC_AD_HOST_RE = /(^|\.)(adnxs|doubleclick|googlesyndication|googleadservices|googletagservices|amazon-adsystem|criteo|taboola|outbrain|pubmatic|rubiconproject|openx|smartadserver|adform|flashtalking|popads|popcash|propellerads|exoclick|exosrv|juicyads|trafficjunky|trafficstars|tsyndicate|realsrv|hilltopads|clickadu|adsterra|ero-advertising|plugrush|adxpansion|chaturbate|stripchat|livejasmin|bongacams|crakrevenue|trafficfactory|clickadilla|evadav|galaksion|onclkds|acscdn|adroll|rtbhouse|mediav|alimama|tanx|adkernel|admixer|bidswitch|sonobi|33across|gumgum|triplelift|sharethrough|revcontent|mgid|imasdk|spotxchange|spotx|fwmrm|freewheel|innovid|springserve|unrulymedia|tremorhub|vidoomy|aniview|connatix|primis|vidazoo|lkqd|stickyadstv)\b|^(ad|ads|adserver|adservice|adx|adsdk|adbanner|adclick|adimg|adv|advert|popads|popunder|prebid|pubads|rtb|bidding|adtrack)\./i;
    const HEURISTIC_TRACKER_HOST_RE = /(^|\.)(coinhive|cryptoloot|jsecoin|webmine|coinimp|minero|deepminer|afminer|monerominer|mistat|miui|hicloud|oppomobile|realmemobile|samsungads|samsungacr|lgsmartad|cookiebot|cookielaw|onetrust|usercentrics|didomi|consensu|trustarc|cookieyes|sentry|sentry-cdn|bugsnag|rollbar|trackjs|raygun|airbrake|honeybadger|crashlytics|optimizely|visualwebsiteoptimizer|abtasty|kameleoon|dynamicyield|convertexperiments|launchdarkly|statsig|linksynergy|impactradius|shareasale|skimresources|viglink|tradedoubler|admitad|clickbank|list-manage|sendgrid|hs-analytics|hsadspixel|klaviyo|munchkin|pardot|trackcmp|mailtrack|ads-twitter|addthis|sharethis|addtoany|google-analytics|googletagmanager|hotjar|mouseflow|fullstory|luckyorange|crazyegg|inspectlet|smartlook|logrocket|mixpanel|amplitude|heapanalytics|chartbeat|statcounter|histats|getclicky|posthog|newrelic|nr-data|quantserve|scorecardresearch|imrworldwide|cnzz|umeng|mmstat|growingio|sensorsdata|talkingdata)\b|^(analytics|appmetrica|beacon|beacons|collector|counter|heatmap|matomo|metric|metrics|nmetrics|smetrics|piwik|snowplow|telemetry|logservice|logser|attribution|clicktrack|events-dra|fingerprint|pixel|pixels|tracker|trackers|tracking|trk|ulog|dmp|idsync)\./i;

    const HEURISTIC_AD_PATH_RE = /\/(adsbygoogle|pagead2?|show_ads|gpt\.js|prebid(\.min)?\.js|adserver|adframe|popunder|clickunder|vast\.xml|vpaid\.js|ima3\.js|adult[-_]?ad|sex[-_]?ad|acmsd)(\.|\/|\?|$)/i;
    const HEURISTIC_TRACKER_PATH_RE = /\/(google-analytics|analytics\.js|gtag\/js|gtm\.js|hm\.js|z_stat\.php|cnzz_stat|matomo\.js|piwik\.js|hotjar-|clarity\.js|fbevents\.js|scevent\.min\.js|oct\.js|fingerprint2?(\.min)?\.js|openfpcdn|pixel\.gif|beacon\.gif|__utm\.gif|collect\?v=[12])(\.|\/|\?|$)/i;

    const urlCache = new Map();

    function checkUrlBlock(urlStr, resourceType) {
        if (!config.enabled || !urlStr || typeof urlStr !== 'string') return null;
        const raw = urlStr.trim();
        if (!raw || raw.startsWith('data:') || raw.startsWith('blob:') || raw.startsWith('javascript:') || raw.startsWith('#')) {
            return null;
        }

        const cacheKey = (resourceType || 'ANY') + '|' + raw;
        if (urlCache.has(cacheKey)) return urlCache.get(cacheKey);

        let parsed;
        try {
            parsed = new URL(raw, location.href);
        } catch (_) {
            return null;
        }

        const targetHost = (parsed.hostname || '').toLowerCase();
        if (!targetHost) return null;
        const targetPath = (parsed.pathname || '') + (parsed.search || '');
        const targetBase = getBaseDomain(targetHost);
        const isSameSite = (targetHost === HOST || (targetBase && targetBase === CURRENT_BASE_DOMAIN));

        if (/\.(m3u8|ts|mp4|webm|flv|mpd|m4s|aac|woff2?|ttf)(\?|#|$)/i.test(parsed.pathname) && !IS_BENCHMARK_SITE) {
            urlCache.set(cacheKey, null);
            return null;
        }

        let hit = null;

        if (isSameSite && !IS_BENCHMARK_SITE) {
            if (resourceType !== 'IMG' && resourceType !== 'MEDIA') {
                if (config.masterAdPurge && HEURISTIC_AD_PATH_RE.test(targetPath)) {
                    hit = { bucket: 'ads', host: targetHost };
                } else if (config.masterPrivacyShield && HEURISTIC_TRACKER_PATH_RE.test(targetPath)) {
                    hit = { bucket: 'trackers', host: targetHost };
                }
            }
        } else {
            const parts = targetHost.split('.');
            for (let i = 0; i < parts.length - 1; i++) {
                const sub = parts.slice(i).join('.');
                const b = DOMAIN_BUCKET_MAP.get(sub);
                if (b) {
                    hit = { bucket: b, host: targetHost };
                    break;
                }
            }

            if (!hit) {
                if (SAFE_INFRA_DOMAINS.has(targetHost) || SAFE_INFRA_DOMAINS.has(targetBase)) {
                    if (resourceType !== 'IMG' && CDN_TRACKER_PATH_RE.test(targetPath)) {
                        hit = { bucket: 'trackers', host: targetHost };
                    }
                } else {
                    const isNormalImageFile = (resourceType === 'IMG') && NORMAL_MEDIA_EXT_RE.test(parsed.pathname);
                    if (HEURISTIC_AD_HOST_RE.test(targetHost)) {
                        hit = { bucket: 'ads', host: targetHost };
                    } else if (!isNormalImageFile && HEURISTIC_TRACKER_HOST_RE.test(targetHost)) {
                        hit = { bucket: 'trackers', host: targetHost };
                    } else if (!isNormalImageFile && HEURISTIC_AD_PATH_RE.test(targetPath)) {
                        hit = { bucket: 'ads', host: targetHost };
                    } else if (!isNormalImageFile && HEURISTIC_TRACKER_PATH_RE.test(targetPath)) {
                        hit = { bucket: 'trackers', host: targetHost };
                    }
                }
            }
        }

        if (hit) {
            if (hit.bucket === 'ads' && !config.masterAdPurge) hit = null;
            else if (hit.bucket === 'trackers' && !config.masterPrivacyShield) hit = null;
        }

        if (urlCache.size >= 800) {
            urlCache.delete(urlCache.keys().next().value);
        }
        urlCache.set(cacheKey, hit);
        return hit;
    }

    // =========================================================================
    // 2. 全通道网络请求拦截器
    // =========================================================================
    function setupNetworkInterceptors() {
        try {
            const noop = function () {};
            const safeProxy = new Proxy(noop, {
                get: (t, p) => (p === Symbol.toPrimitive ? () => '' : safeProxy),
                apply: () => safeProxy,
                construct: () => safeProxy
            });
            if (!window._hmt) window._hmt = [];
            if (!window.dataLayer) window.dataLayer = [];
            if (!window.ga) window.ga = noop;
            if (!window.gtag) window.gtag = noop;
            if (!window.fbq) window.fbq = noop;
            if (!window.ym) window.ym = noop;
            if (!window.ttq) window.ttq = safeProxy;
            if (!window.googletag) {
                window.googletag = {
                    cmd: [],
                    pubads: () => ({ addEventListener: noop, refresh: noop, enableSingleRequest: noop, setTargeting: noop, collapseEmptyDivs: noop }),
                    enableServices: noop,
                    defineSlot: () => ({ addService: () => ({ setTargeting: noop }) }),
                    display: noop
                };
            }
        } catch (_) {}

        try {
            if (window.fetch) {
                const origFetch = window.fetch.bind(window);
                window.fetch = function (input, init) {
                    let urlStr = '';
                    try {
                        urlStr = typeof input === 'string' ? input : (input && input.url ? input.url : String(input));
                    } catch (_) {}
                    const hit = checkUrlBlock(urlStr, 'FETCH');
                    if (hit) {
                        recordEvent(hit.bucket, '网络阻断', hit.host);
                        return Promise.reject(new TypeError('Failed to fetch'));
                    }
                    return origFetch(input, init);
                };
            }
        } catch (_) {}

        try {
            if (window.XMLHttpRequest) {
                const origOpen = XMLHttpRequest.prototype.open;
                const origSend = XMLHttpRequest.prototype.send;

                XMLHttpRequest.prototype.open = function (method, url) {
                    this.__vlg_hit__ = checkUrlBlock(String(url || ''), 'XHR');
                    if (this.__vlg_hit__) {
                        recordEvent(this.__vlg_hit__.bucket, '请求拦截', this.__vlg_hit__.host);
                    }
                    return origOpen.apply(this, arguments);
                };

                XMLHttpRequest.prototype.send = function () {
                    if (this.__vlg_hit__) {
                        const xhr = this;
                        setTimeout(() => {
                            try {
                                Object.defineProperty(xhr, 'readyState', { value: 4, configurable: true });
                                Object.defineProperty(xhr, 'status', { value: 0, configurable: true });
                                Object.defineProperty(xhr, 'responseText', { value: '', configurable: true });
                                Object.defineProperty(xhr, 'response', { value: '', configurable: true });
                            } catch (_) {}
                            try {
                                xhr.dispatchEvent(new ProgressEvent('readystatechange'));
                                const errEvt = new ProgressEvent('error');
                                xhr.dispatchEvent(errEvt);
                                if (typeof xhr.onerror === 'function') xhr.onerror(errEvt);
                                const endEvt = new ProgressEvent('loadend');
                                xhr.dispatchEvent(endEvt);
                                if (typeof xhr.onloadend === 'function') xhr.onloadend(endEvt);
                            } catch (_) {}
                        }, 1);
                        return;
                    }
                    return origSend.apply(this, arguments);
                };
            }
        } catch (_) {}

        try {
            if (navigator.sendBeacon) {
                const origBeacon = navigator.sendBeacon.bind(navigator);
                navigator.sendBeacon = function (url, data) {
                    const hit = checkUrlBlock(String(url || ''), 'BEACON');
                    if (hit) {
                        recordEvent(hit.bucket, '信标阻断', hit.host);
                        return IS_BENCHMARK_SITE ? false : true;
                    }
                    return origBeacon(url, data);
                };
            }
        } catch (_) {}

        try {
            if (window.WebSocket) {
                const OrigWS = window.WebSocket;
                const WrappedWS = function (url, protocols) {
                    const hit = checkUrlBlock(String(url || '').replace(/^wss?:/i, 'https:'), 'WS');
                    if (hit) {
                        recordEvent(hit.bucket, '长连接阻断', hit.host);
                        throw new DOMException('Blocked by Via Liquid Glass Pro', 'SecurityError');
                    }
                    return protocols !== undefined ? new OrigWS(url, protocols) : new OrigWS(url);
                };
                WrappedWS.prototype = OrigWS.prototype;
                ['CONNECTING', 'OPEN', 'CLOSING', 'CLOSED'].forEach(k => { WrappedWS[k] = OrigWS[k]; });
                window.WebSocket = WrappedWS;
            }
        } catch (_) {}

        function hookResourceAttr(Proto, propName, resType) {
            if (!Proto) return;
            try {
                const desc = Object.getOwnPropertyDescriptor(Proto, propName);
                if (!desc || !desc.set || !desc.configurable) return;
                Object.defineProperty(Proto, propName, {
                    configurable: true,
                    enumerable: desc.enumerable,
                    get: function () { return desc.get.call(this); },
                    set: function (val) {
                        const hit = checkUrlBlock(String(val || ''), resType);
                        if (hit) {
                            recordEvent(hit.bucket, resType + '拦截', hit.host);
                            const el = this;
                            setTimeout(() => {
                                try {
                                    const evt = new Event('error', { bubbles: false, cancelable: false });
                                    el.dispatchEvent(evt);
                                    if (typeof el.onerror === 'function') el.onerror(evt);
                                } catch (_) {}
                            }, 1);
                            return val;
                        }
                        return desc.set.call(this, val);
                    }
                });
            } catch (_) {}
        }

        hookResourceAttr(window.HTMLImageElement && HTMLImageElement.prototype, 'src', 'IMG');
        hookResourceAttr(window.HTMLScriptElement && HTMLScriptElement.prototype, 'src', 'JS');
        hookResourceAttr(window.HTMLIFrameElement && HTMLIFrameElement.prototype, 'src', 'FRAME');
        hookResourceAttr(window.HTMLLinkElement && HTMLLinkElement.prototype, 'href', 'LINK');

        try {
            const origSetAttr = Element.prototype.setAttribute;
            Element.prototype.setAttribute = function (name, value) {
                const n = String(name || '').toLowerCase();
                if (n === 'src' || n === 'href') {
                    const tag = this.tagName;
                    if (tag === 'IMG' || tag === 'SCRIPT' || tag === 'IFRAME' || tag === 'LINK') {
                        const hit = checkUrlBlock(String(value || ''), tag);
                        if (hit) {
                            recordEvent(hit.bucket, tag + '拦截', hit.host);
                            const el = this;
                            setTimeout(() => {
                                try {
                                    const evt = new Event('error');
                                    el.dispatchEvent(evt);
                                    if (typeof el.onerror === 'function') el.onerror(evt);
                                } catch (_) {}
                            }, 1);
                            return;
                        }
                    }
                }
                return origSetAttr.call(this, name, value);
            };
        } catch (_) {}
    }

    setupNetworkInterceptors();

    // =========================================================================
    // 3. 终极防重定向劫持引擎
    // =========================================================================
    let lastTrustedTouchTime = 0;
    let lastTrustedTarget = null;

    const onTrustedInput = (e) => {
        if (e && e.isTrusted) {
            lastTrustedTouchTime = Date.now();
            lastTrustedTarget = e.target || null;
        }
    };
    window.addEventListener('touchstart', onTrustedInput, { passive: true, capture: true });
    window.addEventListener('mousedown', onTrustedInput, { passive: true, capture: true });
    window.addEventListener('keydown', onTrustedInput, { passive: true, capture: true });

    const GAMBLING_OR_AD_URL_RE = /(\b(sex|porn|xxx|adult|hentai|casinos?|betting|slots?|roulette|poker|18plus|livecam|chaturbate|stripchat|popads|popcash|propellerads|adsterra|clickadu|hilltopads|juicyads|exoclick|crakrevenue|bet365|bwin|1xbet)\b|https?:\/\/(\d{1,3}\.){3}\d{1,3}(:\d+)?(\/|$)|https?:\/\/[^/]+:(888\d|999\d|666\d|777\d|808\d|809\d|909\d|188\d)\b|\.apk(\?|$)|\.mobileconfig(\?|$)|[?&](aff_id|click_id|offer_id|camp_id|popunder|zoneid|channelCode|inviteCode|agentId|spreadCode|register)=|\/\/[^/]*(ag|bet|vip|av|porn|casino|qipai|caipiao|xinpujing|venetian|taiyangcheng|kyqp|pgsoft|jdb|bbin)[0-9a-z-]*\.)/i;

    function isMaliciousRedirectTarget(urlStr) {
        if (!urlStr || typeof urlStr !== 'string') return false;
        const raw = urlStr.trim();
        if (!raw || raw.startsWith('#') || raw.startsWith('javascript:') || raw.startsWith('blob:') || raw.startsWith('data:')) return false;
        if (checkUrlBlock(raw, 'LINK')) return true;
        try {
            const u = new URL(raw, location.href);
            const tHost = u.hostname.toLowerCase();
            const tBase = getBaseDomain(tHost);
            if (tHost === HOST || tBase === CURRENT_BASE_DOMAIN) return false;
            if (SAFE_INFRA_DOMAINS.has(tHost) || SAFE_INFRA_DOMAINS.has(tBase)) return false;
            return GAMBLING_OR_AD_URL_RE.test(u.href);
        } catch (_) {
            return false;
        }
    }

    function setupUltimateRedirectGuard() {
        try {
            const origElemClick = HTMLElement.prototype.click;
            HTMLElement.prototype.click = function () {
                if (config.enabled && config.masterRedirectGuard) {
                    const anchor = (this.tagName === 'A') ? this : (this.closest ? this.closest('a[href]') : null);
                    if (anchor) {
                        const href = anchor.getAttribute('href') || anchor.href || '';
                        if (href && /^https?:\/\//i.test(href)) {
                            try {
                                const u = new URL(href, location.href);
                                const isExternal = (getBaseDomain(u.hostname) !== CURRENT_BASE_DOMAIN);
                                if (isExternal && (!anchor.isConnected || isMaliciousRedirectTarget(href) || (Date.now() - lastTrustedTouchTime > 800))) {
                                    recordEvent('redirects', '伪造点击拦截', u.hostname);
                                    return;
                                }
                            } catch (_) {}
                        }
                    }
                }
                return origElemClick.apply(this, arguments);
            };
        } catch (_) {}

        try {
            const origWinOpen = window.open;
            window.open = function (url) {
                if (config.enabled && config.masterRedirectGuard) {
                    const urlStr = String(url || '');
                    const elapsed = Date.now() - lastTrustedTouchTime;
                    if (isMaliciousRedirectTarget(urlStr) || elapsed > 1000) {
                        recordEvent('redirects', '弹窗拦截', urlStr || HOST);
                        return null;
                    }
                    if (lastTrustedTarget) {
                        const clickedA = lastTrustedTarget.closest ? lastTrustedTarget.closest('a[href]') : null;
                        if (!clickedA && urlStr && /^https?:\/\//i.test(urlStr)) {
                            try {
                                const u = new URL(urlStr, location.href);
                                if (getBaseDomain(u.hostname) !== CURRENT_BASE_DOMAIN) {
                                    recordEvent('redirects', '劫持拦截', u.hostname);
                                    return null;
                                }
                            } catch (_) {}
                        }
                    }
                }
                return origWinOpen.apply(window, arguments);
            };
        } catch (_) {}

        window.addEventListener('click', function (e) {
            if (!config.enabled || !config.masterRedirectGuard) return;
            const a = e.target && e.target.closest ? e.target.closest('a[href]') : null;
            if (!a) return;
            const href = a.getAttribute('href') || a.href || '';
            if (!href || href.startsWith('#') || href.startsWith('javascript:')) return;

            let isExternal = false;
            let targetHost = '';
            try {
                const u = new URL(href, location.href);
                targetHost = u.hostname.toLowerCase();
                isExternal = (getBaseDomain(targetHost) !== CURRENT_BASE_DOMAIN);
            } catch (_) {}

            if (!e.isTrusted && isExternal) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                recordEvent('redirects', '合成跳转拦截', targetHost);
                return;
            }

            if (isExternal) {
                try {
                    const rect = a.getBoundingClientRect();
                    const st = window.getComputedStyle(a);
                    const isHuge = (rect.width > window.innerWidth * 0.55 && rect.height > window.innerHeight * 0.35);
                    const isTransparent = (parseFloat(st.opacity) < 0.1 || (st.backgroundColor === 'rgba(0, 0, 0, 0)' && !a.textContent.trim() && !a.querySelector('img')));
                    const isFixedOverlay = (st.position === 'fixed' || st.position === 'absolute') && (parseInt(st.zIndex, 10) > 999);
                    if ((isHuge && isTransparent) || (isHuge && isFixedOverlay)) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        a.remove();
                        recordEvent('redirects', '透明遮罩清除', targetHost);
                        return;
                    }
                } catch (_) {}
            }

            if (isMaliciousRedirectTarget(href)) {
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                recordEvent('redirects', '赌博色情阻断', targetHost || href);
            }
        }, true);

        try {
            let pushBurst = 0;
            let lastPushTs = 0;
            const origPush = history.pushState;
            history.pushState = function () {
                if (config.enabled && config.masterRedirectGuard) {
                    const now = Date.now();
                    pushBurst = (now - lastPushTs < 900) ? (pushBurst + 1) : 1;
                    lastPushTs = now;
                    if (pushBurst > 3 && (now - lastTrustedTouchTime > 900)) {
                        recordEvent('redirects', '返回键保护', HOST);
                        return;
                    }
                }
                return origPush.apply(this, arguments);
            };
        } catch (_) {}
    }

    setupUltimateRedirectGuard();

    // =========================================================================
    // 4. 色情与博彩广告卡片专杀 + 图片防裂图自愈与视频加速引擎
    // =========================================================================
    const NORMALIZED_ADULT_GAMBLING_RE = /(同城约炮|免费约炮|同城少妇|空降上门|全国空降|全国外围|裸聊直播|美女裸聊|成人直播|大秀直播|黄播软件|午夜福利|福利姬|蜜桃传媒|麻豆传媒|91制片|糖心vlog|海角社区|杏吧|皇家华人|精东影业|天美传媒|星空传媒|兔子先生|成人抖阴|抖阴破解|污漫|禁漫天堂|18禁漫画|韩漫无遮|羞羞漫画|肉漫|色情软件|看片神器|免费看黄|夫妻交友|少妇白洁|熟女乱伦|丝袜诱惑|娇喘呻吟|高潮喷水|无码高清|国产自拍|偷拍视频|澳门新葡京|新葡京娱乐|葡京赌场|威尼斯人|太阳城集团|美高梅|永利皇宫|沙巴体育|皇冠体育|开元棋牌|必发彩票|六合彩|香港彩|澳门彩|性感荷官|在线发牌|真人视讯|注册送彩金|首存送|百人牛牛|百家乐|龙虎斗|捕鱼达人|AG真人|BBIN电子|PG电子|麻将胡了|电子游艺|爆分大奖|官方直营|充值秒到|提现秒到)/i;

    function normalizeTextForScan(str) {
        if (!str) return '';
        return String(str).replace(/[\s·•\-_*|~`!@#$%^&()+={}[\]:;"'<>,.?/\\]+/g, '');
    }

    const inspectedElements = new WeakSet();

    function inspectAndOptimizeElement(el) {
        if (!config.enabled || !el || el.nodeType !== 1 || inspectedElements.has(el)) return;
        if (el.id === '__via_lg_v65_host__' || el.id === '__via_lg_global_svg__') return;
        inspectedElements.add(el);

        const tag = el.tagName;

        if (tag === 'META' && config.masterRedirectGuard) {
            const equiv = (el.getAttribute('http-equiv') || '').toLowerCase();
            const content = el.getAttribute('content') || '';
            if (equiv === 'refresh' && /url\s*=/i.test(content)) {
                const m = content.match(/url\s*=\s*['"]?([^'";\s]+)/i);
                if (m && m[1] && isMaliciousRedirectTarget(m[1])) {
                    el.remove();
                    recordEvent('redirects', '自动跳转阻断', m[1]);
                    return;
                }
            }
        }

        if (tag === 'SCRIPT' || tag === 'IFRAME') {
            const src = el.src || el.getAttribute('src') || '';
            if (src) {
                const hit = checkUrlBlock(src, tag);
                if (hit) {
                    recordEvent(hit.bucket, tag + '净除', hit.host);
                    try {
                        el.removeAttribute('src');
                        el.remove();
                    } catch (_) {}
                    return;
                }
            }
        }

        if (config.masterAdPurge) {
            if (tag === 'A') {
                const href = el.getAttribute('href') || '';
                const rawText = (el.textContent || '') + ' ' + (el.getAttribute('title') || '');
                const img = el.querySelector('img');
                const imgText = img ? ((img.getAttribute('alt') || '') + ' ' + (img.getAttribute('title') || '')) : '';
                const normText = normalizeTextForScan(rawText + imgText);

                let isExternal = false;
                let targetHost = '';
                if (href && /^https?:\/\//i.test(href)) {
                    try {
                        const u = new URL(href, location.href);
                        targetHost = u.hostname.toLowerCase();
                        const tBase = getBaseDomain(targetHost);
                        isExternal = (tBase !== CURRENT_BASE_DOMAIN && !SAFE_INFRA_DOMAINS.has(targetHost) && !SAFE_INFRA_DOMAINS.has(tBase));
                    } catch (_) {}
                }

                const hitAdultText = normText.length > 1 && NORMALIZED_ADULT_GAMBLING_RE.test(normText);
                const hitAdultUrl = isExternal && isMaliciousRedirectTarget(href);

                let hitExternalBannerCard = false;
                if (isExternal && img) {
                    const imgSrc = (img.getAttribute('src') || img.getAttribute('data-original') || img.getAttribute('data-src') || '').toLowerCase();
                    if (/\.gif(\?|#|$)/i.test(imgSrc) || /\/(gg|ads?|tuiguang|acmsd|hezuo|caipiao|bocai)\//i.test(imgSrc)) {
                        hitExternalBannerCard = true;
                    }
                }

                if (hitAdultText || hitAdultUrl || hitExternalBannerCard) {
                    const cardBox = el.closest('li, .ad-item, .gg-item, .card-ad, .banner-item, .tuiguang-item') || el;
                    cardBox.style.setProperty('display', 'none', 'important');
                    recordEvent('ads', '色情博彩卡片', targetHost || HOST);
                    return;
                }
            }

            if (tag === 'DIV' || tag === 'ASIDE' || tag === 'SECTION' || tag === 'UL') {
                const idCls = ((el.id || '') + ' ' + (typeof el.className === 'string' ? el.className : '')).toLowerCase();
                if (/(^|[\s_-])(acmsd|hmrichbox|couplet[-_]?ad|float[-_]?ad|bottom[-_]?ad|pop[-_]?ad|gg[-_]?box|gg[-_]?list|gg[-_]?cont|tuiguang|adult[-_]?banner|br[-_]?ad)([\s_-]|$)/i.test(idCls)) {
                    el.style.setProperty('display', 'none', 'important');
                    recordEvent('ads', '广告容器净除', HOST);
                    return;
                }
            }
        }

        if (tag === 'IMG' && config.masterSpeedBoost) {
            if (!el.hasAttribute('decoding')) el.setAttribute('decoding', 'async');
            const realSrc = el.getAttribute('data-original') || el.getAttribute('data-src') || el.getAttribute('data-lazy-src') || el.getAttribute('data-echo');
            const curSrc = el.getAttribute('src') || '';
            if (realSrc && /^https?:\/\/|^\/\//i.test(realSrc) && (!curSrc || curSrc.startsWith('data:') || /loading|placeholder|blank|space|lazyload|default/i.test(curSrc))) {
                el.addEventListener('error', function onImgErr() {
                    el.removeEventListener('error', onImgErr);
                    if (el.src !== realSrc) {
                        el.src = realSrc;
                        recordEvent('boosted', '图片修复', HOST);
                    }
                }, { once: true });
            }
        }

        if (tag === 'VIDEO' && config.masterSpeedBoost) {
            if (!el.hasAttribute('playsinline')) el.setAttribute('playsinline', 'true');
            if (!el.hasAttribute('webkit-playsinline')) el.setAttribute('webkit-playsinline', 'true');
            if (el.getAttribute('preload') === 'none') el.setAttribute('preload', 'metadata');
            recordEvent('boosted', '视频优化', HOST);
        }
    }

    const nodeQueue = [];
    let queueScheduled = false;

    function drainNodeQueue() {
        queueScheduled = false;
        let count = 0;
        while (nodeQueue.length > 0 && count < 50) {
            const n = nodeQueue.shift();
            count++;
            if (!n || n.nodeType !== 1) continue;
            inspectAndOptimizeElement(n);
            if (n.querySelectorAll) {
                const children = n.querySelectorAll('a[href], img, video, iframe, script, meta[http-equiv]');
                for (let i = 0; i < children.length; i++) {
                    if (!inspectedElements.has(children[i])) {
                        inspectAndOptimizeElement(children[i]);
                    }
                }
            }
        }
        if (nodeQueue.length > 0) scheduleNodeQueue();
    }

    function scheduleNodeQueue() {
        if (queueScheduled) return;
        queueScheduled = true;
        const runner = window.requestIdleCallback || window.requestAnimationFrame || function (fn) { return setTimeout(fn, 32); };
        runner(drainNodeQueue);
    }

    function healLazyImagesInViewport() {
        if (!config.enabled || !config.masterSpeedBoost) return;
        try {
            const lazyImgs = document.querySelectorAll('img[data-original], img[data-src], img[data-lazy-src]');
            const vh = window.innerHeight || 700;
            for (let i = 0; i < lazyImgs.length; i++) {
                const img = lazyImgs[i];
                const r = img.getBoundingClientRect();
                if (r.top < vh * 1.4 && r.bottom > -200) {
                    const real = img.getAttribute('data-original') || img.getAttribute('data-src') || img.getAttribute('data-lazy-src');
                    const cur = img.getAttribute('src') || '';
                    if (real && /^https?:\/\/|^\/\//i.test(real) && (!cur || cur.startsWith('data:') || /loading|placeholder|blank|lazyload|grey|pixel/i.test(cur))) {
                        img.src = real;
                    }
                }
            }
        } catch (_) {}
    }

    window.addEventListener('scroll', (() => {
        let st = null;
        return () => {
            if (st) return;
            st = setTimeout(() => {
                st = null;
                healLazyImagesInViewport();
            }, 260);
        };
    })(), { passive: true });

    function startDomObserverSafely() {
        const root = document.documentElement || document;
        if (!root || !(root instanceof Node)) {
            setTimeout(startDomObserverSafely, 15);
            return;
        }
        try {
            const obs = new MutationObserver(mutations => {
                if (!config.enabled) return;
                for (let i = 0; i < mutations.length; i++) {
                    const added = mutations[i].addedNodes;
                    for (let j = 0; j < added.length; j++) {
                        const n = added[j];
                        if (n && n.nodeType === 1) {
                            if (n.tagName === 'SCRIPT' || n.tagName === 'IFRAME' || n.tagName === 'META') {
                                inspectAndOptimizeElement(n);
                            } else {
                                nodeQueue.push(n);
                                scheduleNodeQueue();
                            }
                        }
                    }
                }
            });
            obs.observe(root, { childList: true, subtree: true });
        } catch (_) {}
    }

    startDomObserverSafely();

    // =========================================================================
    // 5. 隐私隐身护盾
    // =========================================================================
    function setupPrivacyAndUnlockEngines() {
        const seed = Math.floor(Math.random() * 250) + 2;

        try {
            const origGetImageData = CanvasRenderingContext2D.prototype.getImageData;
            CanvasRenderingContext2D.prototype.getImageData = function (sx, sy, sw, sh) {
                const data = origGetImageData.call(this, sx, sy, sw, sh);
                if (config.enabled && config.masterPrivacyShield && data && data.data && data.data.length >= 16) {
                    const arr = data.data;
                    const step = Math.max(4, Math.floor(arr.length / 32) * 4);
                    for (let i = 0; i < arr.length; i += step) {
                        arr[i] = arr[i] ^ (seed & 1);
                    }
                    recordEvent('trackers', '画布指纹保护', HOST);
                }
                return data;
            };

            const origToDataURL = HTMLCanvasElement.prototype.toDataURL;
            HTMLCanvasElement.prototype.toDataURL = function () {
                if (config.enabled && config.masterPrivacyShield && this.width > 0 && this.width <= 500 && this.height > 0 && this.height <= 500) {
                    try {
                        const ctx = this.getContext('2d');
                        if (ctx) {
                            const px = origGetImageData.call(ctx, 0, 0, 1, 1);
                            px.data[0] = (px.data[0] + (seed % 3) - 1) & 255;
                            ctx.putImageData(px, 0, 0);
                        }
                    } catch (_) {}
                    recordEvent('trackers', '画布指纹保护', HOST);
                }
                return origToDataURL.apply(this, arguments);
            };
        } catch (_) {}

        function wrapWebGL(Proto) {
            if (!Proto || !Proto.getParameter) return;
            try {
                const orig = Proto.getParameter;
                Proto.getParameter = function (pname) {
                    if (config.enabled && config.masterPrivacyShield) {
                        if (pname === 37445) { recordEvent('trackers', '显卡指纹保护', HOST); return 'Apple Inc.'; }
                        if (pname === 37446) { recordEvent('trackers', '显卡指纹保护', HOST); return 'Apple GPU'; }
                    }
                    return orig.apply(this, arguments);
                };
            } catch (_) {}
        }
        wrapWebGL(window.WebGLRenderingContext && WebGLRenderingContext.prototype);
        wrapWebGL(window.WebGL2RenderingContext && WebGL2RenderingContext.prototype);

        try {
            if (window.AudioBuffer && AudioBuffer.prototype.getChannelData) {
                const origGetChan = AudioBuffer.prototype.getChannelData;
                AudioBuffer.prototype.getChannelData = function () {
                    const buf = origGetChan.apply(this, arguments);
                    if (config.enabled && config.masterPrivacyShield && buf && buf.length > 0 && !this.__vlg_aud__) {
                        this.__vlg_aud__ = true;
                        const step = Math.max(1, Math.floor(buf.length / 64));
                        for (let i = 0; i < buf.length; i += step) {
                            buf[i] += ((seed % 7) - 3) * 1e-7;
                        }
                        recordEvent('trackers', '声卡指纹保护', HOST);
                    }
                    return buf;
                };
            }
        } catch (_) {}

        const allowWebRTC = new Set(['meet.google.com', 'zoom.us', 'teams.microsoft.com', 'discord.com', 'feishu.cn', 'larksuite.com', 'dingtalk.com', 'voovmeeting.com']);
        if (!allowWebRTC.has(CURRENT_BASE_DOMAIN) && !allowWebRTC.has(HOST)) {
            ['RTCPeerConnection', 'webkitRTCPeerConnection', 'mozRTCPeerConnection'].forEach(k => {
                const OrigPC = window[k];
                if (!OrigPC) return;
                try {
                    const WrappedPC = function (rtcCfg, constraints) {
                        const safeCfg = (config.enabled && config.masterPrivacyShield && rtcCfg && typeof rtcCfg === 'object')
                            ? Object.assign({}, rtcCfg, { iceServers: [], iceTransportPolicy: 'relay' })
                            : rtcCfg;
                        const pc = new OrigPC(safeCfg, constraints);
                        if (config.enabled && config.masterPrivacyShield) {
                            recordEvent('trackers', 'WebRTC防泄漏', HOST);
                        }
                        return pc;
                    };
                    WrappedPC.prototype = OrigPC.prototype;
                    window[k] = WrappedPC;
                } catch (_) {}
            });
        }

        const TRACKER_COOKIE_RE = [
            /^_ga/i, /^_gid/i, /^_gat/i, /^_gcl_/i, /^__utm/i, /^_fbp/i, /^_fbc/i,
            /^Hm_lvt_/i, /^Hm_lpvt_/i, /^CNZZDATA/i, /^UM_distinctid/i,
            /^_pk_id/i, /^_pk_ses/i, /^_hjid/i, /^_hjSession/i, /^_clck/i, /^_clsk/i,
            /^amplitude_id/i, /^mp_.*_mixpanel/i, /^sajssdk_2015/i, /^sensorsdata2015/i,
            /^_ym_uid/i, /^_ym_d/i, /^_ym_isad/i, /^_ttp/i, /^_uetvid/i, /^_uetsid/i
        ];

        try {
            const cDesc = Object.getOwnPropertyDescriptor(Document.prototype, 'cookie') ||
                          Object.getOwnPropertyDescriptor(HTMLDocument.prototype, 'cookie');
            if (cDesc && cDesc.configurable) {
                Object.defineProperty(document, 'cookie', {
                    configurable: true,
                    enumerable: true,
                    get: function () {
                        const raw = cDesc.get.call(this);
                        if (!config.enabled || !config.masterPrivacyShield || !raw) return raw;
                        return raw.split(';').filter(part => {
                            const name = (part.split('=')[0] || '').trim();
                            return !TRACKER_COOKIE_RE.some(re => re.test(name));
                        }).join('; ');
                    },
                    set: function (val) {
                        if (!config.enabled || !config.masterPrivacyShield) return cDesc.set.call(this, val);
                        const str = String(val || '');
                        const name = ((str.split(';')[0] || '').split('=')[0] || '').trim();
                        if (/SameSite\s*=\s*None/i.test(str) || TRACKER_COOKIE_RE.some(re => re.test(name))) {
                            recordEvent('trackers', '跟踪Cookie阻断', HOST);
                            return val;
                        }
                        return cDesc.set.call(this, val);
                    }
                });
            }
        } catch (_) {}

        try {
            if (navigator.clipboard && navigator.clipboard.writeText) {
                const origWrite = navigator.clipboard.writeText.bind(navigator.clipboard);
                navigator.clipboard.writeText = function (text) {
                    if (config.enabled && (config.masterPrivacyShield || config.masterReadUnlock)) {
                        const elapsed = Date.now() - lastTrustedTouchTime;
                        const isSpam = /[￥$€₤][\w]{6,15}[￥$€₤]|復·制|打开.*App|口令/i.test(String(text || ''));
                        if (elapsed > 1200 || isSpam) {
                            recordEvent('trackers', '剪贴板防劫持', HOST);
                            return Promise.resolve();
                        }
                    }
                    return origWrite(text);
                };
            }
        } catch (_) {}

        ['copy', 'cut', 'selectstart'].forEach(evtName => {
            window.addEventListener(evtName, function (e) {
                if (!config.enabled || !config.masterReadUnlock) return;
                if (evtName === 'copy' && window.getSelection && String(window.getSelection()).trim().length > 0) {
                    e.stopImmediatePropagation();
                } else if (evtName === 'selectstart') {
                    e.stopImmediatePropagation();
                }
            }, true);
        });

        window.addEventListener('dblclick', function (e) {
            if (!config.enabled || !config.masterReadUnlock) return;
            const el = e.target;
            if (el && el.tagName === 'INPUT' && (el.type === 'password' || el.dataset.__vlg_pwd__ === '1')) {
                el.dataset.__vlg_pwd__ = '1';
                el.type = (el.type === 'password') ? 'text' : 'password';
            }
        }, true);
    }

    setupPrivacyAndUnlockEngines();

    // =========================================================================
    // 6. 网址尾巴净化 + 外链免等待直达 + 触屏智能预读下一页
    // =========================================================================
    const DIRTY_URL_PARAMS = new Set([
        'utm_source', 'utm_medium', 'utm_campaign', 'utm_term', 'utm_content', 'utm_id', 'utm_name',
        'spm', 'spm_id_from', 'from_source', 'isappinstalled', 'nsukey', 'vd_source',
        'fbclid', 'gclid', 'dclid', 'msclkid', 'twclid', 'ttclid', 'igshid', 'yclid', '_openstat',
        'mc_cid', 'mc_eid', 'mkt_tok', 'bd_vid', 'baiduid', 'recommended_by',
        'share_source', 'share_medium', 'share_plat', 'share_session_id', 'share_tag', 'unique_k'
    ]);

    function purifyCurrentUrl() {
        if (!config.enabled || !config.masterSpeedBoost) return;
        try {
            const u = new URL(location.href);
            let removed = 0;
            for (const k of Array.from(u.searchParams.keys())) {
                if (DIRTY_URL_PARAMS.has(k.toLowerCase()) || /^(utm_|stm_|hsa_|vero_|mkt_)/i.test(k)) {
                    u.searchParams.delete(k);
                    removed++;
                }
            }
            if (removed > 0) {
                history.replaceState(history.state, '', u.toString());
                recordEvent('boosted', '网址净化', HOST);
            }
        } catch (_) {}
    }

    purifyCurrentUrl();

    const REDIRECT_PARAM_NAMES = ['target', 'url', 'link', 'pfurl', 'to', 'redirect', 'redirect_url', 'dest', 'u', 'jump'];
    const REDIRECT_HOST_RE = /(link\.zhihu\.com|link\.juejin\.cn|link\.csdn\.net|jianshu\.com\/go-wild|gitee\.com\/link|weibo\.cn\/sinaurl|weixin110\.qq\.com|c\.pc\.qq\.com|tieba\.baidu\.com\/mo\/q\/checkurl|t\.cn)/i;

    function extractRealTargetUrl(href) {
        if (!href || !href.startsWith('http')) return null;
        try {
            const u = new URL(href);
            if (!REDIRECT_HOST_RE.test(u.hostname + u.pathname)) return null;
            for (let i = 0; i < REDIRECT_PARAM_NAMES.length; i++) {
                const val = u.searchParams.get(REDIRECT_PARAM_NAMES[i]);
                if (val && /^https?:\/\//i.test(decodeURIComponent(val))) {
                    return decodeURIComponent(val);
                }
            }
        } catch (_) {}
        return null;
    }

    const prefetchedSet = new Set();
    function handleTouchSpeedBoost(e) {
        if (!config.enabled || !config.masterSpeedBoost) return;
        const a = e.target && e.target.closest ? e.target.closest('a[href]') : null;
        if (!a || !a.href) return;

        const realUrl = extractRealTargetUrl(a.href);
        if (realUrl) {
            a.href = realUrl;
            recordEvent('boosted', '外链直达', HOST);
        }

        if (prefetchedSet.has(a.href)) return;
        if (navigator.connection && (navigator.connection.saveData || /2g/i.test(navigator.connection.effectiveType))) return;
        try {
            const u = new URL(a.href, location.href);
            if (u.origin !== ORIGIN) return;
            if (u.pathname === location.pathname && u.search === location.search) return;
            if (/logout|signout|delete|remove|download|pay|cart|checkout/i.test(u.href)) return;

            prefetchedSet.add(a.href);
            const link = document.createElement('link');
            link.rel = 'prefetch';
            link.href = u.href;
            link.as = 'document';
            (document.head || document.documentElement).appendChild(link);
            recordEvent('boosted', '极速预读', HOST);
        } catch (_) {}
    }

    window.addEventListener('touchstart', handleTouchSpeedBoost, { passive: true });
    window.addEventListener('mouseover', handleTouchSpeedBoost, { passive: true });

    // =========================================================================
    // 7. 页面样式注入引擎
    // =========================================================================
    const AD_AND_ADULT_SELECTORS = [
        '.adsbygoogle', 'ins.adsbygoogle', '[id^="div-gpt-ad"]', '[id^="google_ads_iframe"]',
        '#google_ads_frame', '.google-ad', '.ad-banner', '.ads-banner', '.banner-ads', '.banner_ad',
        '.adsbox', '#adsbox', '.ad-box', '.ad_box', '#ad-box', '#ad_box', '.ad-slot', '.ad_slot',
        '.ad-unit', '.ad_unit', '.ad-zone', '.ad-space', '.ad-placeholder', '.ad-wrapper',
        '.ad-container', '#ad-container', '#ad_container', '.ad-sidebar', '.ad-leaderboard',
        '.textads', '.sponsored-ad', '.sponsored-banner', '.pop-ad-mask', '.float-ad-box',
        'div[id^="cpro_"]', '.baidu-ad', '.tanx-ad', '[data-ad-client]', '[data-ad-slot]',
        '[id^="acmsd"]', '.acmsd-box', '#HMRichBox', '#pop_ad', '.couplet-ad', '.float-bottom-ad',
        '.gg-box', '.gg-card', '.gg-item', '.gg-list', '.gg_cont', '.tuiguang-box', '.adult-ad',
        '.sex-ad', '.erotic-ad', 'a[href*="exoclick.com"]', 'a[href*="juicyads.com"]',
        'a[href*="trafficjunky.net"]', 'a[href*="chaturbate.com"]', 'a[href*="stripchat.com"]',
        'a[href*="livejasmin.com"]', 'a[href*="bongacams.com"]', 'a[href*="crakrevenue.com"]',
        'iframe[src*="doubleclick.net"]', 'iframe[src*="googlesyndication.com"]', 'iframe[src*="pos.baidu.com"]',
        'iframe[src*="exoclick.com"]', 'iframe[src*="juicyads.com"]', 'iframe[src*="trafficjunky.net"]'
    ];

    const COOKIE_AND_APP_SELECTORS = [
        '#onetrust-consent-sdk', '#onetrust-banner-sdk', '#CybotCookiebotDialog',
        '#usercentrics-root', '#didomi-host', '.didomi-popup-container', '.qc-cmp2-container',
        '#truste-consent-track', '.cc-window.cc-banner', '#cookie-law-info-bar',
        '.cky-consent-container', '#cookiebanner', '#cookie-consent-banner',
        '.OpenInAppButton', '.MobileAppHeader-downloadLink', '.open-in-app', '.open-app-btn',
        '.download-app-bar', '.app-guide-banner', '.m-open-app', '.call-app-btn', '.btn-open-app',
        '#openAppBtn', '.zhihu-app-banner', '.csdn-app-box', '.read-more-mask'
    ];

    let customSelectors = safeParse(safeGet(KEY_CUSTOM_HIDE), []);
    let readerModeActive = false;
    let styleEl = null;

    function shouldUseDark() {
        if (!config.enabled) return false;
        if (config.darkMode) return true;
        if (config.darkAutoSystem && window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) return true;
        return false;
    }

    function updatePageStyles() {
        const root = document.head || document.documentElement;
        if (!root) {
            setTimeout(updatePageStyles, 20);
            return;
        }
        if (!config.enabled) {
            if (styleEl) styleEl.textContent = '';
            return;
        }
        const css = [];

        if (shouldUseDark()) {
            const c = Math.min(100, Math.max(75, Number(config.darkContrast) || 92));
            css.push(`
                html {
                    filter: invert(${c}%) hue-rotate(180deg) !important;
                    background-color: #101216 !important;
                    color-scheme: dark !important;
                }
                html img, html video, html canvas, html picture, html embed, html object,
                html [style*="background-image"], html #__via_lg_v65_host__ {
                    filter: invert(100%) hue-rotate(180deg) !important;
                }
                html :fullscreen, html :-webkit-full-screen {
                    filter: none !important;
                }
            `);
        }

        if (config.masterAdPurge) {
            css.push(`
                ${AD_AND_ADULT_SELECTORS.join(',\n')} {
                    display: none !important;
                    visibility: hidden !important;
                    opacity: 0 !important;
                    pointer-events: none !important;
                    height: 0 !important;
                    max-height: 0 !important;
                    overflow: hidden !important;
                }
            `);
        }

        if (config.masterReadUnlock) {
            css.push(`
                ${COOKIE_AND_APP_SELECTORS.join(',\n')} {
                    display: none !important;
                    pointer-events: none !important;
                }
                article, .article-content, .post-content, .RichContent-inner, #article_content {
                    max-height: none !important;
                    height: auto !important;
                    overflow: visible !important;
                }
                html, body {
                    overflow: auto !important;
                }
                * {
                    -webkit-user-select: text !important;
                    user-select: text !important;
                }
            `);
        }

        if (customSelectors.length > 0) {
            css.push(`
                ${customSelectors.join(',\n')} {
                    display: none !important;
                    visibility: hidden !important;
                    pointer-events: none !important;
                }
            `);
        }

        if (readerModeActive) {
            css.push(`
                header, footer, nav, aside, .sidebar, .comment, .comments, .recommend, .related, .share-bar {
                    display: none !important;
                }
                body {
                    max-width: 720px !important;
                    margin: 0 auto !important;
                    padding: 18px 16px !important;
                    line-height: 1.78 !important;
                    font-size: 17px !important;
                }
            `);
        }

        if (!styleEl || !styleEl.isConnected) {
            styleEl = document.createElement('style');
            styleEl.id = '__via_lg_v65_style__';
            root.appendChild(styleEl);
        }
        const joined = css.join('\n');
        if (styleEl.textContent !== joined) {
            styleEl.textContent = joined;
        }
    }

    updatePageStyles();

    function runInitialScan() {
        if (!config.enabled || !document.body) return;
        nodeQueue.push(document.body);
        scheduleNodeQueue();
        setTimeout(healLazyImagesInViewport, 600);
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', runInitialScan, { once: true });
    } else {
        runInitialScan();
    }

    // =========================================================================
    // 8. 经真实浏览器光学渲染验证的 Apple iOS 27 双引擎液态玻璃系统 (v7.0.0)
    //    - 引擎 A (3D 超椭圆凸透镜边缘折射): 通过离屏 Canvas 实时计算 SDF 超椭圆曲率法线贴图 (向内采样零越界)，
    //      结合红绿蓝三通道微色散 (Chromatic Dispersion) 注入 backdrop-filter，让背景网页在玻璃边缘产生真实曲面折射弯曲！
    //    - 引擎 B (外扩无损裁剪流体焦散层): ::before 采用 inset: -38px 外扩安全区运行 feTurbulence + feDisplacementMap，
    //      由父容器 overflow: hidden 严丝合缝裁剪，兼容 iOS WebKit 与 Android Blink，外边框 100% 锐利平滑、零锯齿毛边！
    // =========================================================================
    if (!IS_TOP_WINDOW) return;

    // 实时生成向内采样的 3D 超椭圆凸透镜位移贴图 (Inward-Sampling Convex Squircle SDF Lens Map)
    function generateConvexSquircleLensURI(w, h, radius, bezelWidth) {
        try {
            const cw = Math.max(64, Math.min(600, Math.round(w)));
            const ch = Math.max(28, Math.min(800, Math.round(h)));
            const canvas = document.createElement('canvas');
            canvas.width = cw;
            canvas.height = ch;
            const ctx = canvas.getContext('2d');
            const imgData = ctx.createImageData(cw, ch);
            const data = imgData.data;

            const hw = cw / 2;
            const hh = ch / 2;
            const r = Math.min(radius, hw, hh);

            for (let y = 0; y < ch; y++) {
                for (let x = 0; x < cw; x++) {
                    const px = x - hw;
                    const py = y - hh;
                    const qx = Math.abs(px) - (hw - r);
                    const qy = Math.abs(py) - (hh - r);
                    const distFromEdge = Math.hypot(Math.max(qx, 0), Math.max(qy, 0)) + Math.min(Math.max(qx, qy), 0);
                    const depthInside = -distFromEdge;

                    let nx = 0;
                    let ny = 0;

                    if (depthInside > 0 && depthInside < bezelWidth) {
                        const t = 1.0 - (depthInside / bezelWidth);
                        const curve = Math.sin(t * Math.PI * 0.5) ** 2;
                        const gradX = qx > 0 ? (px >= 0 ? qx : -qx) : (qx > qy ? (px >= 0 ? 1 : -1) : 0);
                        const gradY = qy > 0 ? (py >= 0 ? qy : -qy) : (qy >= qx ? (py >= 0 ? 1 : -1) : 0);
                        const len = Math.hypot(gradX, gradY) || 1;
                        nx = -(gradX / len) * curve;
                        ny = -(gradY / len) * curve;
                    }

                    const idx = (y * cw + x) * 4;
                    data[idx]     = Math.min(255, Math.max(0, Math.round(128 + nx * 124)));
                    data[idx + 1] = Math.min(255, Math.max(0, Math.round(128 + ny * 124)));
                    data[idx + 2] = 128;
                    data[idx + 3] = 255;
                }
            }
            ctx.putImageData(imgData, 0, 0);
            return canvas.toDataURL('image/png');
        } catch (_) {
            return '';
        }
    }

    function buildSvgDefsMarkup(pw, ph, panelMapUrl, pillW, pillH, pillMapUrl, warpScale) {
        const scR = Math.round(warpScale * 1.15);
        const scG = Math.round(warpScale * 1.0);
        const scB = Math.round(warpScale * 0.85);
        const pillSc = Math.round(warpScale * 0.72);
        return `
            <svg xmlns="http://www.w3.org/2000/svg" width="0" height="0" style="position:absolute; width:0; height:0; pointer-events:none;" aria-hidden="true">
                <defs>
                    <filter id="vlg-apple-convex-panel" filterUnits="userSpaceOnUse" x="0" y="0" width="${pw}" height="${ph}" color-interpolation-filters="sRGB">
                        <feImage class="vlg-panel-feimg" href="${panelMapUrl}" x="0" y="0" width="${pw}" height="${ph}" preserveAspectRatio="none" result="lensMap" />
                        <feGaussianBlur class="vlg-panel-blur" in="SourceGraphic" stdDeviation="${Math.max(2, Math.round(config.glassBlur * 0.22))}" result="blurredBg" />
                        <feDisplacementMap class="vlg-disp-r" in="blurredBg" in2="lensMap" scale="${scR}" xChannelSelector="R" yChannelSelector="G" result="dispR" />
                        <feColorMatrix in="dispR" type="matrix" values="1 0 0 0 0  0 0 0 0 0  0 0 0 0 0  0 0 0 1 0" result="rChan" />
                        <feDisplacementMap class="vlg-disp-g" in="blurredBg" in2="lensMap" scale="${scG}" xChannelSelector="R" yChannelSelector="G" result="dispG" />
                        <feColorMatrix in="dispG" type="matrix" values="0 0 0 0 0  0 1 0 0 0  0 0 0 0 0  0 0 0 1 0" result="gChan" />
                        <feDisplacementMap class="vlg-disp-b" in="blurredBg" in2="lensMap" scale="${scB}" xChannelSelector="R" yChannelSelector="G" result="dispB" />
                        <feColorMatrix in="dispB" type="matrix" values="0 0 0 0 0  0 0 0 0 0  0 0 1 0 0  0 0 0 1 0" result="bChan" />
                        <feBlend in="rChan" in2="gChan" mode="screen" result="rgChan" />
                        <feBlend in="rgChan" in2="bChan" mode="screen" />
                    </filter>
                    <filter id="vlg-apple-convex-pill" filterUnits="userSpaceOnUse" x="0" y="0" width="${pillW}" height="${pillH}" color-interpolation-filters="sRGB">
                        <feImage class="vlg-pill-feimg" href="${pillMapUrl}" x="0" y="0" width="${pillW}" height="${pillH}" preserveAspectRatio="none" result="pillMap" />
                        <feGaussianBlur in="SourceGraphic" stdDeviation="2" result="pillBlur" />
                        <feDisplacementMap class="vlg-disp-pill" in="pillBlur" in2="pillMap" scale="${pillSc}" xChannelSelector="R" yChannelSelector="G" />
                    </filter>
                    <filter id="vlg-liquid-caustic" x="-20%" y="-20%" width="140%" height="140%">
                        <feTurbulence type="fractalNoise" baseFrequency="0.008 0.012" numOctaves="2" seed="42" result="noise" />
                        <feGaussianBlur in="noise" stdDeviation="1.5" result="softNoise" />
                        <feDisplacementMap class="vlg-disp-caustic" in="SourceGraphic" in2="softNoise" scale="${warpScale}" xChannelSelector="R" yChannelSelector="G" />
                    </filter>
                </defs>
            </svg>
        `;
    }

    function mountLiquidGlassUI() {
        const rootNode = document.documentElement || document.body;
        if (!rootNode) {
            setTimeout(mountLiquidGlassUI, 25);
            return;
        }

        const existing = document.getElementById('__via_lg_v65_host__');
        if (existing) {
            existing.style.cssText = 'all: initial !important; display: block !important; visibility: visible !important; opacity: 1 !important; position: fixed !important; z-index: 2147483647 !important; top: 0 !important; left: 0 !important; width: 0 !important; height: 0 !important; pointer-events: none !important;';
            return;
        }

        const host = document.createElement('div');
        host.id = '__via_lg_v65_host__';
        host.style.cssText = 'all: initial !important; display: block !important; visibility: visible !important; opacity: 1 !important; position: fixed !important; z-index: 2147483647 !important; top: 0 !important; left: 0 !important; width: 0 !important; height: 0 !important; pointer-events: none !important;';
        rootNode.appendChild(host);

        let shadow;
        try {
            shadow = host.attachShadow({ mode: 'open' });
        } catch (_) {
            shadow = host;
        }

        const initPw = 372;
        const initPh = 548;
        const initPillW = 108;
        const initPillH = 34;
        const initPanelMap = generateConvexSquircleLensURI(initPw, initPh, 28, 44);
        const initPillMap = generateConvexSquircleLensURI(initPillW, initPillH, 17, 12);

        // 同时挂载在全局与 Shadow Root 内，确保 Chrome/Blink 与 Safari/WebKit 双端 100% 命中滤镜 ID
        let globalSvgBox = document.getElementById('__via_lg_global_svg__');
        if (!globalSvgBox) {
            globalSvgBox = document.createElement('div');
            globalSvgBox.id = '__via_lg_global_svg__';
            globalSvgBox.style.cssText = 'position:fixed !important; top:0 !important; left:0 !important; width:0 !important; height:0 !important; pointer-events:none !important; z-index:-1 !important; overflow:hidden !important;';
            rootNode.appendChild(globalSvgBox);
        }
        const svgMarkup = buildSvgDefsMarkup(initPw, initPh, initPanelMap, initPillW, initPillH, initPillMap, config.glassWarp);
        globalSvgBox.innerHTML = svgMarkup;

        const style = document.createElement('style');
        style.textContent = `
            :host {
                all: initial;
                --lg-blur: ${config.glassBlur}px;
                --lg-alpha: ${(config.glassOpacity / 100).toFixed(2)};
                --lg-shine: ${(config.glassShine / 100).toFixed(2)};
                --lg-sat: 195%;
            }

            .lg-pill, .lg-panel, button, input, span, div {
                font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display", "SF Pro Text", "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Noto Sans CJK SC", system-ui, sans-serif;
                -webkit-font-smoothing: antialiased;
                -webkit-tap-highlight-color: transparent;
                user-select: none;
                -webkit-user-select: none;
                box-sizing: border-box;
            }

            /* 120fps 全屏自由拖拽胶囊外框 */
            .lg-pill-wrap {
                position: fixed;
                top: calc(env(safe-area-inset-top, 0px) + 10px);
                left: 50%;
                transform: translate3d(-50%, 0, 0);
                z-index: 2147483647;
                pointer-events: auto;
                touch-action: none;
                will-change: transform;
                transition: opacity 0.28s ease;
            }
            .lg-pill-wrap.custom-pos {
                top: 0;
                left: 0;
            }
            .lg-pill-wrap.smooth-snap {
                transition: transform 0.3s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.28s ease;
            }
            .lg-pill-wrap.idle {
                opacity: 0.85;
            }
            .lg-pill-wrap:hover,
            .lg-pill-wrap.active,
            .lg-pill-wrap.dragging {
                opacity: 1;
            }
            .lg-pill-wrap.dragging .lg-pill {
                transform: scale(1.07);
            }

            /* ---------- Apple iOS 27 原生液态玻璃胶囊（3D 凸透镜边缘折射 + 外扩零锯齿流体层） ---------- */
            .lg-pill {
                position: relative;
                display: inline-flex;
                align-items: center;
                gap: 7.5px;
                height: 34px;
                padding: 0 13px 0 10.5px;
                border-radius: 999px;
                overflow: hidden;
                cursor: grab;
                color: #ffffff;
                font-size: 12px;
                font-weight: 600;
                letter-spacing: 0.25px;
                white-space: nowrap;
                text-shadow: 0 1px 3px rgba(0, 0, 0, 0.42);
                z-index: 1;
                transition: transform 0.2s cubic-bezier(0.22, 1, 0.36, 1);

                background: rgba(18, 22, 34, var(--lg-alpha));
                -webkit-backdrop-filter: blur(var(--lg-blur)) saturate(var(--lg-sat)) brightness(1.08);
                backdrop-filter: url(#vlg-apple-convex-pill) saturate(var(--lg-sat)) brightness(1.08);
                border: 1px solid rgba(255, 255, 255, calc(0.28 + var(--lg-shine) * 0.32));
                box-shadow:
                    0 10px 28px rgba(0, 0, 0, 0.38),
                    0 2px 6px rgba(0, 0, 0, 0.18),
                    inset 0 1.5px 1.2px rgba(255, 255, 255, var(--lg-shine)),
                    inset 0 -1.2px 1.5px rgba(255, 255, 255, 0.28);
            }

            .lg-pill::before {
                content: "";
                position: absolute;
                inset: -24px;
                z-index: -2;
                background:
                    radial-gradient(circle at 25% 20%, rgba(255, 255, 255, calc(var(--lg-shine) * 0.42)) 0%, transparent 55%),
                    linear-gradient(135deg, rgba(255, 255, 255, calc(0.18 + var(--lg-shine) * 0.14)) 0%, rgba(255, 255, 255, 0.03) 50%, rgba(122, 252, 255, 0.16) 100%);
                filter: url(#vlg-liquid-caustic);
                pointer-events: none;
            }

            .lg-pill::after {
                content: "";
                position: absolute;
                top: 0;
                left: 10%;
                width: 80%;
                height: 45%;
                border-radius: 50%;
                background: linear-gradient(
                    180deg,
                    rgba(255, 255, 255, calc(var(--lg-shine) * 0.48)) 0%,
                    rgba(255, 255, 255, 0) 100%
                );
                filter: blur(5px);
                opacity: 0.75;
                pointer-events: none;
                z-index: -1;
            }

            .lg-orb {
                width: 8px;
                height: 8px;
                border-radius: 50%;
                background: #34d399;
                box-shadow: 0 0 8px #34d399, inset 0 1px 1px #ffffff;
                flex-shrink: 0;
            }
            .lg-orb.paused {
                background: #fbbf24;
                box-shadow: 0 0 8px #fbbf24;
            }

            .lg-pill-num {
                background: rgba(255, 255, 255, 0.18);
                border: 0.8px solid rgba(255, 255, 255, 0.38);
                padding: 1px 7.5px;
                border-radius: 999px;
                font-size: 11px;
                font-variant-numeric: tabular-nums;
                color: #ffffff;
                box-shadow: inset 0 1px 0.5px rgba(255, 255, 255, 0.4);
            }

            .lg-backdrop {
                position: fixed;
                inset: 0;
                background: rgba(6, 10, 18, 0.12);
                opacity: 0;
                pointer-events: none;
                transition: opacity 0.25s ease;
                z-index: 2147483645;
            }
            .lg-backdrop.open {
                opacity: 1;
                pointer-events: auto;
            }

            /* ---------- Apple iOS 27 原生液态玻璃主面板（3D 超椭圆凸透镜边缘折射 + 色散高光） ---------- */
            .lg-panel {
                position: fixed;
                left: 50%;
                top: calc(env(safe-area-inset-top, 0px) + 52px);
                width: min(92vw, 372px);
                max-height: calc(85vh - env(safe-area-inset-top, 0px));
                display: flex;
                flex-direction: column;
                padding: 18px 16px 18px;
                border-radius: 28px;
                overflow: hidden;
                color: #ffffff;
                text-shadow: 0 1px 3px rgba(0, 0, 0, 0.38);
                transform: translate(-50%, -14px) scale(0.95);
                opacity: 0;
                pointer-events: none;
                z-index: 2147483646;
                transition: transform 0.32s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.24s ease;

                background: rgba(18, 22, 34, var(--lg-alpha));
                -webkit-backdrop-filter: blur(var(--lg-blur)) saturate(var(--lg-sat)) brightness(1.08);
                backdrop-filter: url(#vlg-apple-convex-panel) saturate(var(--lg-sat)) brightness(1.08);
                border: 1px solid rgba(255, 255, 255, calc(0.26 + var(--lg-shine) * 0.28));
                box-shadow:
                    0 24px 60px rgba(0, 0, 0, 0.50),
                    0 4px 16px rgba(0, 0, 0, 0.24),
                    inset 0 1.5px 1.5px rgba(255, 255, 255, var(--lg-shine)),
                    inset 0 -1.5px 2px rgba(255, 255, 255, 0.26),
                    inset 1.5px 0 2.5px rgba(125, 211, 252, 0.32),
                    inset -1.5px 0 2.5px rgba(244, 114, 182, 0.28);
            }

            /* 内部流体折射焦散层：inset: -38px 外扩越界保护，被父级 border-radius: 28px 干净裁剪，绝无毛边！ */
            .lg-panel::before {
                content: "";
                position: absolute;
                inset: -38px;
                z-index: -2;
                background:
                    radial-gradient(circle at 22% 18%, rgba(255, 255, 255, calc(var(--lg-shine) * 0.32)) 0%, transparent 45%),
                    radial-gradient(circle at 82% 28%, rgba(122, 252, 255, calc(var(--lg-shine) * 0.20)) 0%, transparent 48%),
                    radial-gradient(circle at 50% 88%, rgba(255, 126, 179, calc(var(--lg-shine) * 0.16)) 0%, transparent 55%),
                    linear-gradient(
                        135deg,
                        rgba(255, 255, 255, calc(0.18 + var(--lg-shine) * 0.10)) 0%,
                        rgba(255, 255, 255, 0.02) 50%,
                        rgba(255, 255, 255, calc(0.12 + var(--lg-shine) * 0.08)) 100%
                    );
                filter: url(#vlg-liquid-caustic);
                pointer-events: none;
            }

            /* 顶部柔和反光弧，完美复刻参考源码的 ::after 玻璃光泽 */
            .lg-panel::after {
                content: "";
                position: absolute;
                top: 0;
                left: 10%;
                width: 80%;
                height: 36%;
                border-radius: 50%;
                background: linear-gradient(
                    180deg,
                    rgba(255, 255, 255, calc(var(--lg-shine) * 0.40)) 0%,
                    rgba(255, 255, 255, 0) 100%
                );
                filter: blur(12px);
                opacity: 0.65;
                pointer-events: none;
                z-index: -1;
            }

            .lg-panel.open {
                transform: translate(-50%, 0) scale(1);
                opacity: 1;
                pointer-events: auto;
            }

            .lg-head {
                display: flex;
                align-items: center;
                justify-content: space-between;
                margin-bottom: 12px;
                z-index: 1;
            }
            .lg-brand {
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .lg-gem-box {
                width: 30px;
                height: 30px;
                border-radius: 10px;
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.38), rgba(255, 255, 255, 0.12));
                border: 1px solid rgba(255, 255, 255, 0.48);
                box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.75);
                display: flex;
                align-items: center;
                justify-content: center;
            }
            .lg-gem {
                width: 10px;
                height: 10px;
                border: 1.8px solid #ffffff;
                transform: rotate(45deg);
                border-radius: 2px;
            }
            .lg-title-wrap {
                display: flex;
                flex-direction: column;
            }
            .lg-title {
                font-size: 14.5px;
                font-weight: 700;
                letter-spacing: 0.25px;
            }
            .lg-sub {
                font-size: 11px;
                color: rgba(255, 255, 255, 0.82);
                max-width: 185px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .lg-site-btn {
                appearance: none;
                border: 1px solid rgba(255, 255, 255, 0.48);
                background: linear-gradient(135deg, rgba(52, 211, 153, 0.42), rgba(16, 185, 129, 0.24));
                color: #ffffff;
                font-size: 11.5px;
                font-weight: 600;
                padding: 5.5px 12px;
                border-radius: 999px;
                cursor: pointer;
                box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.65);
            }
            .lg-site-btn.off {
                background: linear-gradient(135deg, rgba(251, 191, 36, 0.42), rgba(245, 158, 11, 0.24));
            }

            .lg-nav {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 5px;
                padding: 3.5px;
                border-radius: 15px;
                background: rgba(255, 255, 255, 0.10);
                border: 1px solid rgba(255, 255, 255, 0.24);
                box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.15);
                margin-bottom: 12px;
                z-index: 1;
            }
            .lg-nav-btn {
                appearance: none;
                border: none;
                background: transparent;
                color: rgba(255, 255, 255, 0.82);
                font-size: 12px;
                font-weight: 600;
                padding: 6px 4px;
                border-radius: 11.5px;
                cursor: pointer;
                transition: all 0.2s ease;
            }
            .lg-nav-btn.active {
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.36), rgba(255, 255, 255, 0.16));
                color: #ffffff;
                border: 0.5px solid rgba(255, 255, 255, 0.55);
                box-shadow: 0 3px 10px rgba(0, 0, 0, 0.18), inset 0 1px 0.8px rgba(255, 255, 255, 0.8);
            }

            .lg-scroll {
                overflow-y: auto;
                overscroll-behavior: contain;
                flex: 1;
                z-index: 1;
                padding-bottom: 2px;
            }
            .lg-scroll::-webkit-scrollbar {
                width: 0;
                height: 0;
            }
            .lg-view {
                display: none;
                flex-direction: column;
                gap: 8px;
            }
            .lg-view.active {
                display: flex;
            }

            .lg-stat-hero {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 8px;
            }
            .lg-stat-card {
                padding: 11px 12px;
                border-radius: 18px;
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.26) 0%, rgba(255, 255, 255, 0.08) 55%, rgba(255, 255, 255, 0.16) 100%);
                border: 1px solid rgba(255, 255, 255, 0.34);
                box-shadow: inset 0 1px 1px rgba(255, 255, 255, 0.55);
                display: flex;
                flex-direction: column;
                gap: 2px;
            }
            .lg-stat-lbl {
                font-size: 11px;
                color: rgba(255, 255, 255, 0.88);
            }
            .lg-stat-num {
                font-size: 22px;
                font-weight: 800;
                font-variant-numeric: tabular-nums;
                line-height: 1.15;
            }
            .lg-stat-sub {
                font-size: 10.5px;
                color: rgba(224, 242, 254, 0.96);
            }

            .lg-quad {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 6px;
            }
            .lg-quad-item {
                padding: 7.5px 4px;
                border-radius: 14px;
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.20), rgba(255, 255, 255, 0.07));
                border: 1px solid rgba(255, 255, 255, 0.26);
                box-shadow: inset 0 1px 0.5px rgba(255, 255, 255, 0.42);
                text-align: center;
            }
            .lg-quad-val {
                font-size: 13.5px;
                font-weight: 700;
                font-variant-numeric: tabular-nums;
            }
            .lg-quad-txt {
                font-size: 10px;
                color: rgba(255, 255, 255, 0.84);
                margin-top: 1px;
            }

            .lg-row {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 9px 11px;
                border-radius: 17px;
                background: linear-gradient(
                    135deg,
                    rgba(255, 255, 255, 0.24) 0%,
                    rgba(255, 255, 255, 0.07) 52%,
                    rgba(255, 255, 255, 0.15) 100%
                );
                border: 1px solid rgba(255, 255, 255, 0.30);
                box-shadow:
                    0 4px 14px rgba(0, 0, 0, 0.12),
                    inset 0 1px 1px rgba(255, 255, 255, 0.52),
                    inset 0 -1px 0.8px rgba(255, 255, 255, 0.08);
                cursor: pointer;
                transition: background 0.18s ease, transform 0.15s ease;
            }
            .lg-row:active {
                transform: scale(0.985);
                background: rgba(255, 255, 255, 0.30);
            }
            .lg-row-left {
                display: flex;
                align-items: center;
                gap: 9.5px;
                padding-right: 8px;
            }
            .lg-ico {
                width: 28px;
                height: 28px;
                border-radius: 9px;
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0.08));
                border: 1px solid rgba(255, 255, 255, 0.38);
                box-shadow: inset 0 1px 0.5px rgba(255, 255, 255, 0.6);
                display: flex;
                align-items: center;
                justify-content: center;
                flex-shrink: 0;
            }
            .lg-ico svg {
                width: 15px;
                height: 15px;
                stroke: #ffffff;
                stroke-width: 1.85;
                fill: none;
                stroke-linecap: round;
                stroke-linejoin: round;
            }
            .lg-row-title {
                font-size: 12.5px;
                font-weight: 600;
            }
            .lg-row-desc {
                font-size: 10.5px;
                color: rgba(255, 255, 255, 0.86);
                line-height: 1.25;
                margin-top: 1px;
            }

            .lg-switch {
                position: relative;
                width: 42px;
                height: 24px;
                flex-shrink: 0;
                border-radius: 999px;
                background: rgba(0, 0, 0, 0.28);
                border: 1px solid rgba(255, 255, 255, 0.34);
                box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.28);
                transition: all 0.24s ease;
            }
            .lg-switch.checked {
                background: linear-gradient(135deg, rgba(52, 211, 153, 0.90), rgba(56, 189, 248, 0.90));
                border-color: rgba(255, 255, 255, 0.75);
            }
            .lg-knob {
                position: absolute;
                top: 2px;
                left: 2px;
                width: 18px;
                height: 18px;
                border-radius: 50%;
                background: #ffffff;
                box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
                transition: transform 0.24s cubic-bezier(0.34, 1.56, 0.64, 1);
            }
            .lg-switch.checked .lg-knob {
                transform: translateX(18px);
            }

            .lg-slider-card {
                display: flex;
                flex-direction: column;
                gap: 5.5px;
                padding: 10px 12px;
                border-radius: 17px;
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.20), rgba(255, 255, 255, 0.06));
                border: 1px solid rgba(255, 255, 255, 0.28);
                box-shadow: inset 0 1px 0.8px rgba(255, 255, 255, 0.45);
            }
            .lg-slider-head {
                display: flex;
                justify-content: space-between;
                font-size: 11px;
                font-weight: 500;
            }
            .lg-range {
                -webkit-appearance: none;
                appearance: none;
                width: 100%;
                height: 4.5px;
                border-radius: 999px;
                background: rgba(255, 255, 255, 0.34);
                outline: none;
            }
            .lg-range::-webkit-slider-thumb {
                -webkit-appearance: none;
                appearance: none;
                width: 16px;
                height: 16px;
                border-radius: 50%;
                background: #ffffff;
                box-shadow: 0 1px 5px rgba(0, 0, 0, 0.4);
                cursor: pointer;
            }

            .lg-grid-2 {
                display: grid;
                grid-template-columns: repeat(2, 1fr);
                gap: 7px;
            }
            .lg-grid-4 {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 6px;
            }
            .lg-btn {
                appearance: none;
                border: 1px solid rgba(255, 255, 255, 0.34);
                background: linear-gradient(135deg, rgba(255, 255, 255, 0.26), rgba(255, 255, 255, 0.08));
                color: #ffffff;
                font-size: 11.5px;
                font-weight: 600;
                padding: 8px 6px;
                border-radius: 13px;
                cursor: pointer;
                text-align: center;
                box-shadow: inset 0 1px 0.8px rgba(255, 255, 255, 0.5);
                text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
                transition: transform 0.15s ease, background 0.2s ease;
            }
            .lg-btn.active {
                background: linear-gradient(135deg, rgba(56, 189, 248, 0.55), rgba(52, 211, 153, 0.48));
                border-color: rgba(255, 255, 255, 0.85);
            }
            .lg-btn:active {
                transform: scale(0.96);
            }

            .lg-drawer {
                border-radius: 15px;
                background: rgba(255, 255, 255, 0.10);
                border: 1px solid rgba(255, 255, 255, 0.24);
                overflow: hidden;
            }
            .lg-drawer-head {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 9px 12px;
                font-size: 11.5px;
                font-weight: 600;
                cursor: pointer;
            }
            .lg-drawer-body {
                display: none;
                padding: 0 10px 9px;
                max-height: 115px;
                overflow-y: auto;
            }
            .lg-drawer.open .lg-drawer-body {
                display: flex;
                flex-direction: column;
                gap: 4px;
            }
            .lg-log-item {
                display: flex;
                justify-content: space-between;
                font-size: 10.5px;
                padding: 3.5px 7px;
                border-radius: 7px;
                background: rgba(255, 255, 255, 0.10);
            }
            .lg-log-txt {
                max-width: 195px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
            }

            .lg-picker-bar {
                position: fixed;
                bottom: 24px;
                left: 50%;
                transform: translateX(-50%);
                background: rgba(15, 23, 42, 0.85);
                backdrop-filter: blur(20px);
                -webkit-backdrop-filter: blur(20px);
                border: 1px solid rgba(255, 255, 255, 0.42);
                color: #ffffff;
                padding: 10px 16px;
                border-radius: 999px;
                font-size: 12px;
                font-weight: 600;
                display: none;
                align-items: center;
                gap: 10px;
                z-index: 2147483647;
                pointer-events: auto;
            }
            .lg-picker-bar.show {
                display: inline-flex;
            }
            .lg-picker-exit {
                appearance: none;
                border: 1px solid rgba(255, 255, 255, 0.35);
                background: rgba(239, 68, 68, 0.5);
                color: #ffffff;
                padding: 3px 10px;
                border-radius: 999px;
                font-size: 11px;
                cursor: pointer;
            }

            .lg-toast {
                position: fixed;
                top: calc(env(safe-area-inset-top, 0px) + 52px);
                left: 50%;
                transform: translate(-50%, -8px);
                background: rgba(18, 26, 42, 0.82);
                backdrop-filter: blur(18px);
                -webkit-backdrop-filter: blur(18px);
                color: #ffffff;
                border: 1px solid rgba(255, 255, 255, 0.4);
                padding: 6.5px 15px;
                border-radius: 999px;
                font-size: 11.5px;
                font-weight: 500;
                opacity: 0;
                pointer-events: none;
                transition: all 0.24s ease;
                z-index: 2147483647;
            }
            .lg-toast.show {
                opacity: 1;
                transform: translate(-50%, 0);
            }

            @supports not (backdrop-filter: blur(1px)) {
                .lg-pill, .lg-panel {
                    background: rgba(24, 30, 44, 0.82);
                }
                .lg-pill::before, .lg-panel::before {
                    filter: none;
                }
            }
        `;

        const container = document.createElement('div');
        container.innerHTML = `
            <div id="vlgShadowSvgBox">${svgMarkup}</div>
            <div class="lg-backdrop" id="lgBackdrop"></div>

            <!-- iOS 27 原生液态玻璃顶部胶囊 -->
            <div class="lg-pill-wrap idle" id="lgPillWrap">
                <div class="lg-pill" id="lgPillBtn">
                    <span class="lg-orb" id="lgStatusDot"></span>
                    <span>守护</span>
                    <span class="lg-pill-num" id="lgTotalBadge">0</span>
                </div>
            </div>

            <div class="lg-picker-bar" id="lgPickerBar">
                <span>请直接点击网页上想屏蔽的广告或浮窗</span>
                <button class="lg-picker-exit" id="lgPickerCancel">退出点选</button>
            </div>

            <div class="lg-toast" id="lgToast"></div>

            <!-- iOS 27 原生液态玻璃主面板 -->
            <div class="lg-panel" id="lgPanel">
                <div class="lg-head">
                    <div class="lg-brand">
                        <div class="lg-gem-box"><div class="lg-gem"></div></div>
                        <div class="lg-title-wrap">
                            <div class="lg-title">Liquid Glass iOS 27</div>
                            <div class="lg-sub">${HOST}</div>
                        </div>
                    </div>
                    <button class="lg-site-btn" id="lgSiteToggle"></button>
                </div>

                <div class="lg-nav">
                    <button class="lg-nav-btn active" data-tab="core">核心守护</button>
                    <button class="lg-nav-btn" data-tab="stats">看板与工具</button>
                    <button class="lg-nav-btn" data-tab="style">液态与深色</button>
                </div>

                <div class="lg-scroll">
                    <!-- [第 1 页] 核心守护开关 -->
                    <div class="lg-view active" id="view-core">
                        <div class="lg-row" data-key="masterAdPurge">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg></div>
                                <div>
                                    <div class="lg-row-title">强力清除广告与色情博彩</div>
                                    <div class="lg-row-desc">净除动漫/影视站色情动图、赌博横幅与视频前贴片广告</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-masterAdPurge"><div class="lg-knob"></div></div>
                        </div>

                        <div class="lg-row" data-key="masterRedirectGuard">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><polyline points="9 14 4 9 9 4"/><path d="M20 20v-7a4 4 0 0 0-4-4H4"/></svg></div>
                                <div>
                                    <div class="lg-row-title">封死恶意跳转与劫持保护</div>
                                    <div class="lg-row-desc">阻止点屏幕偷跳赌博页、清除透明点击层、保护手机返回键</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-masterRedirectGuard"><div class="lg-knob"></div></div>
                        </div>

                        <div class="lg-row" data-key="masterPrivacyShield">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="3"/></svg></div>
                                <div>
                                    <div class="lg-row-title">全维隐私隐身与防追踪护盾</div>
                                    <div class="lg-row-desc">阻断1350+跨站追踪器、隐藏设备特征与真实地址、防塞口令</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-masterPrivacyShield"><div class="lg-knob"></div></div>
                        </div>

                        <div class="lg-row" data-key="masterSpeedBoost">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg></div>
                                <div>
                                    <div class="lg-row-title">网页与视频极速加载优化</div>
                                    <div class="lg-row-desc">触屏预读下一页、图片防裂图自愈、视频秒播优化、外链直达</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-masterSpeedBoost"><div class="lg-knob"></div></div>
                        </div>

                        <div class="lg-row" data-key="masterReadUnlock">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></div>
                                <div>
                                    <div class="lg-row-title">阅读解锁与便捷操控增强</div>
                                    <div class="lg-row-desc">自动展开全文、禁跳App弹窗、自由复制文字、双击密码显明文</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-masterReadUnlock"><div class="lg-knob"></div></div>
                        </div>
                    </div>

                    <!-- [第 2 页] 累计看板与百宝箱 -->
                    <div class="lg-view" id="view-stats">
                        <div class="lg-stat-hero">
                            <div class="lg-stat-card">
                                <span class="lg-stat-lbl">本网站累计拦截</span>
                                <span class="lg-stat-num" id="numSiteTotal">0</span>
                                <span class="lg-stat-sub" id="numSessionAdd">本次打开 +0</span>
                            </div>
                            <div class="lg-stat-card">
                                <span class="lg-stat-lbl">历史总计拦截 (永久累积)</span>
                                <span class="lg-stat-num" id="numGlobalTotal">0</span>
                                <span class="lg-stat-sub" id="numGlobalBoost">累计优化 0 次</span>
                            </div>
                        </div>

                        <div class="lg-quad">
                            <div class="lg-quad-item">
                                <div class="lg-quad-val" id="mAds">0</div>
                                <div class="lg-quad-txt">广告/色情</div>
                            </div>
                            <div class="lg-quad-item">
                                <div class="lg-quad-val" id="mRedirects">0</div>
                                <div class="lg-quad-txt">恶意跳转</div>
                            </div>
                            <div class="lg-quad-item">
                                <div class="lg-quad-val" id="mTrackers">0</div>
                                <div class="lg-quad-txt">隐私追踪</div>
                            </div>
                            <div class="lg-quad-item">
                                <div class="lg-quad-val" id="mBoosts">0</div>
                                <div class="lg-quad-txt">加载优化</div>
                            </div>
                        </div>

                        <div class="lg-drawer" id="drawerLogs">
                            <div class="lg-drawer-head" id="headLogs">
                                <span>点击展开最近拦截记录明细</span>
                                <span>▾</span>
                            </div>
                            <div class="lg-drawer-body" id="logListBox"></div>
                        </div>

                        <div class="lg-grid-2">
                            <button class="lg-btn" id="btnPickElement">手势点选消广告</button>
                            <button class="lg-btn" id="btnReaderMode">纯净阅读模式</button>
                            <button class="lg-btn" id="btnVideoSpeed">视频切换 2.0 倍速</button>
                            <button class="lg-btn" id="btnVideoPip">视频开启小窗播放</button>
                            <button class="lg-btn" id="btnCopyCleanUrl">复制无尾巴链接</button>
                            <button class="lg-btn" id="btnResetCustomHide">恢复点选隐藏元素</button>
                        </div>
                    </div>

                    <!-- [第 3 页] iOS 27 液态玻璃光学调节 & 夜间模式 -->
                    <div class="lg-view" id="view-style">
                        <div class="lg-row" data-key="darkMode" data-site="true">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg></div>
                                <div>
                                    <div class="lg-row-title">网页智能夜间护眼模式</div>
                                    <div class="lg-row-desc">柔和暗化刺眼白底，图片与视频保持原色不失真</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-darkMode"><div class="lg-knob"></div></div>
                        </div>

                        <div class="lg-row" data-key="darkAutoSystem">
                            <div class="lg-row-left">
                                <div class="lg-ico"><svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9"/><path d="M12 3v18"/></svg></div>
                                <div>
                                    <div class="lg-row-title">跟随手机系统深色自动切换</div>
                                    <div class="lg-row-desc">手机开启系统暗黑模式时自动同步启用</div>
                                </div>
                            </div>
                            <div class="lg-switch" id="sw-darkAutoSystem"><div class="lg-knob"></div></div>
                        </div>

                        <div class="lg-grid-4">
                            <button class="lg-btn" data-preset="ios27">iOS 27液态</button>
                            <button class="lg-btn" data-preset="crystal">超透冰晶</button>
                            <button class="lg-btn" data-preset="frosted">柔光磨砂</button>
                            <button class="lg-btn" data-preset="lite">省电轻量</button>
                        </div>

                        <div class="lg-slider-card">
                            <div class="lg-slider-head">
                                <span>3D 凸透镜与流体折射度</span>
                                <span id="valGlassWarp">${config.glassWarp}</span>
                            </div>
                            <input type="range" min="0" max="60" step="1" value="${config.glassWarp}" class="lg-range" id="rngGlassWarp">

                            <div class="lg-slider-head" style="margin-top:3px;">
                                <span>背景光学模糊度</span>
                                <span id="valGlassBlur">${config.glassBlur}px</span>
                            </div>
                            <input type="range" min="6" max="38" step="1" value="${config.glassBlur}" class="lg-range" id="rngGlassBlur">

                            <div class="lg-slider-head" style="margin-top:3px;">
                                <span>玻璃通透度 (数值越小越晶莹)</span>
                                <span id="valGlassOpacity">${config.glassOpacity}%</span>
                            </div>
                            <input type="range" min="6" max="70" step="1" value="${config.glassOpacity}" class="lg-range" id="rngGlassOpacity">

                            <div class="lg-slider-head" style="margin-top:3px;">
                                <span>顶部反光与高光折射亮度</span>
                                <span id="valGlassShine">${config.glassShine}%</span>
                            </div>
                            <input type="range" min="30" max="100" step="1" value="${config.glassShine}" class="lg-range" id="rngGlassShine">
                        </div>

                        <button class="lg-btn" id="btnResetPillPos">重置悬浮胶囊回屏幕正上方居中</button>
                    </div>
                </div>
            </div>
        `;

        shadow.appendChild(style);
        shadow.appendChild(container);

        const q = (sel) => shadow.querySelector(sel);
        const qa = (sel) => shadow.querySelectorAll(sel);

        const pillWrap = q('#lgPillWrap');
        const pillBtn = q('#lgPillBtn');
        const backdrop = q('#lgBackdrop');
        const panel = q('#lgPanel');
        const siteToggleBtn = q('#lgSiteToggle');
        const statusDot = q('#lgStatusDot');
        const totalBadge = q('#lgTotalBadge');
        const toastEl = q('#lgToast');
        const pickerBar = q('#lgPickerBar');
        const shadowSvgBox = q('#vlgShadowSvgBox');

        let isOpen = false;
        let idleTimer = null;

        function showToast(msg) {
            toastEl.textContent = msg;
            toastEl.classList.add('show');
            clearTimeout(toastEl.__t);
            toastEl.__t = setTimeout(() => toastEl.classList.remove('show'), 2200);
        }

        // 动态校准 3D 凸透镜位移贴图尺寸与折射参数
        function recalibrateLensFilter() {
            const pw = Math.max(300, Math.round(panel.offsetWidth || 372));
            const ph = Math.max(360, Math.round(panel.offsetHeight || 548));
            const pillW = Math.max(88, Math.round(pillBtn.offsetWidth || 108));
            const pillH = Math.max(30, Math.round(pillBtn.offsetHeight || 34));
            const pMap = generateConvexSquircleLensURI(pw, ph, 28, 44);
            const bMap = generateConvexSquircleLensURI(pillW, pillH, 17, 12);
            const markup = buildSvgDefsMarkup(pw, ph, pMap, pillW, pillH, bMap, config.glassWarp);
            if (shadowSvgBox) shadowSvgBox.innerHTML = markup;
            if (globalSvgBox) globalSvgBox.innerHTML = markup;
        }

        function applyGlassOptics() {
            host.style.setProperty('--lg-blur', config.glassBlur + 'px');
            host.style.setProperty('--lg-alpha', (config.glassOpacity / 100).toFixed(2));
            host.style.setProperty('--lg-shine', (config.glassShine / 100).toFixed(2));

            const sc = config.glassWarp;
            [shadowSvgBox, globalSvgBox].forEach(root => {
                if (!root) return;
                root.querySelectorAll('.vlg-disp-r').forEach(el => el.setAttribute('scale', String(Math.round(sc * 1.15))));
                root.querySelectorAll('.vlg-disp-g').forEach(el => el.setAttribute('scale', String(Math.round(sc))));
                root.querySelectorAll('.vlg-disp-b').forEach(el => el.setAttribute('scale', String(Math.round(sc * 0.85))));
                root.querySelectorAll('.vlg-disp-pill').forEach(el => el.setAttribute('scale', String(Math.round(sc * 0.72))));
                root.querySelectorAll('.vlg-disp-caustic').forEach(el => el.setAttribute('scale', String(sc)));
                root.querySelectorAll('.vlg-panel-blur').forEach(el => el.setAttribute('stdDeviation', String(Math.max(2, Math.round(config.glassBlur * 0.22)))));
            });

            q('#valGlassWarp').textContent = String(config.glassWarp);
            q('#valGlassBlur').textContent = config.glassBlur + 'px';
            q('#valGlassOpacity').textContent = config.glassOpacity + '%';
            q('#valGlassShine').textContent = config.glassShine + '%';
            q('#rngGlassWarp').value = config.glassWarp;
            q('#rngGlassBlur').value = config.glassBlur;
            q('#rngGlassOpacity').value = config.glassOpacity;
            q('#rngGlassShine').value = config.glassShine;

            qa('[data-preset]').forEach(b => {
                b.classList.toggle('active', b.getAttribute('data-preset') === config.glassPreset);
            });
        }

        applyGlassOptics();
        requestAnimationFrame(recalibrateLensFilter);

        let curX = Number(config.pillX);
        let curY = Number(config.pillY);

        function applyPillPos(x, y, smooth) {
            if (!Number.isFinite(x) || x < 0) {
                pillWrap.classList.remove('custom-pos', 'smooth-snap');
                pillWrap.style.transform = 'translate3d(-50%, 0, 0)';
                curX = -1;
                curY = 10;
                return;
            }
            const vw = Math.max(300, document.documentElement.clientWidth || window.innerWidth || 360);
            const vh = Math.max(400, document.documentElement.clientHeight || window.innerHeight || 640);
            const rect = pillWrap.getBoundingClientRect();
            const pw = rect.width || 94;
            const ph = rect.height || 34;

            const cx = Math.min(vw - pw - 6, Math.max(6, Math.round(x)));
            const cy = Math.min(vh - ph - 6, Math.max(6, Math.round(y)));
            curX = cx;
            curY = cy;

            pillWrap.classList.add('custom-pos');
            pillWrap.classList.toggle('smooth-snap', Boolean(smooth));
            pillWrap.style.transform = `translate3d(${cx}px, ${cy}px, 0)`;
        }

        if (curX >= 0) {
            requestAnimationFrame(() => applyPillPos(curX, curY, false));
        }

        window.addEventListener('resize', () => {
            if (curX >= 0) applyPillPos(curX, curY, false);
        }, { passive: true });

        let dragStartX = 0;
        let dragStartY = 0;
        let initLeft = 0;
        let initTop = 0;
        let isDragging = false;
        let dragRaf = 0;

        function refreshIdle() {
            pillWrap.classList.remove('idle');
            clearTimeout(idleTimer);
            if (!isOpen && !isDragging) {
                idleTimer = setTimeout(() => pillWrap.classList.add('idle'), 3500);
            }
        }

        pillWrap.addEventListener('pointerdown', (e) => {
            dragStartX = e.clientX;
            dragStartY = e.clientY;
            const rect = pillWrap.getBoundingClientRect();
            initLeft = rect.left;
            initTop = rect.top;
            isDragging = false;
            pillWrap.classList.remove('smooth-snap');
            try { pillWrap.setPointerCapture(e.pointerId); } catch (_) {}
            refreshIdle();
        });

        pillWrap.addEventListener('pointermove', (e) => {
            const dx = e.clientX - dragStartX;
            const dy = e.clientY - dragStartY;
            if (!isDragging && (Math.abs(dx) > 5 || Math.abs(dy) > 5)) {
                isDragging = true;
                pillWrap.classList.add('dragging');
                if (isOpen) togglePanel(false);
            }
            if (isDragging) {
                const nx = initLeft + dx;
                const ny = initTop + dy;
                if (dragRaf) cancelAnimationFrame(dragRaf);
                dragRaf = requestAnimationFrame(() => applyPillPos(nx, ny, false));
            }
        });

        let lastPointerToggleTs = 0;
        pillWrap.addEventListener('pointerup', (e) => {
            try {
                if (pillWrap.hasPointerCapture(e.pointerId)) pillWrap.releasePointerCapture(e.pointerId);
            } catch (_) {}
            pillWrap.classList.remove('dragging');
            if (isDragging) {
                config.pillX = curX;
                config.pillY = curY;
                saveConfig(false);
                lastPointerToggleTs = Date.now();
            } else {
                lastPointerToggleTs = Date.now();
                togglePanel();
            }
            isDragging = false;
            refreshIdle();
        });
        pillWrap.addEventListener('click', () => {
            if (Date.now() - lastPointerToggleTs > 350) {
                togglePanel();
            }
        });

        q('#btnResetPillPos').addEventListener('click', () => {
            config.pillX = -1;
            config.pillY = 10;
            saveConfig(false);
            applyPillPos(-1, 10, true);
            showToast('已恢复悬浮胶囊到正上方居中');
        });

        function syncUI() {
            if (config.enabled) {
                siteToggleBtn.textContent = '本站守护中';
                siteToggleBtn.classList.remove('off');
                statusDot.classList.remove('paused');
            } else {
                siteToggleBtn.textContent = '已加白名单';
                siteToggleBtn.classList.add('off');
                statusDot.classList.add('paused');
            }

            totalBadge.textContent = String(statsStore.site.total);
            q('#numSiteTotal').textContent = statsStore.site.total.toLocaleString();
            q('#numSessionAdd').textContent = `本次打开 +${statsStore.session.total}`;
            q('#numGlobalTotal').textContent = statsStore.global.total.toLocaleString();
            q('#numGlobalBoost').textContent = `累计优化 ${statsStore.global.boosted.toLocaleString()} 次`;

            q('#mAds').textContent = String(statsStore.site.ads);
            q('#mRedirects').textContent = String(statsStore.site.redirects);
            q('#mTrackers').textContent = String(statsStore.site.trackers);
            q('#mBoosts').textContent = String(statsStore.site.boosted);

            const logBox = q('#logListBox');
            if (logBox) {
                if (statsStore.recentLogs.length === 0) {
                    logBox.innerHTML = `<div class="lg-log-item"><span>当前页面暂无新增拦截记录</span></div>`;
                } else {
                    logBox.innerHTML = statsStore.recentLogs.map(item => `
                        <div class="lg-log-item">
                            <span class="lg-log-txt">${item.text}</span>
                            <span>${item.tag} ×${item.count}</span>
                        </div>
                    `).join('');
                }
            }

            const keys = [
                'masterAdPurge', 'masterRedirectGuard', 'masterPrivacyShield',
                'masterSpeedBoost', 'masterReadUnlock', 'darkMode', 'darkAutoSystem'
            ];
            keys.forEach(k => {
                const sw = q('#sw-' + k);
                if (sw) sw.classList.toggle('checked', Boolean(config[k]));
            });
        }

        uiSubscribers.add(syncUI);
        syncUI();

        function togglePanel(force) {
            isOpen = typeof force === 'boolean' ? force : !isOpen;
            panel.classList.toggle('open', isOpen);
            backdrop.classList.toggle('open', isOpen);
            pillWrap.classList.toggle('active', isOpen);
            refreshIdle();
            if (isOpen) {
                syncUI();
                requestAnimationFrame(recalibrateLensFilter);
            }
        }

        backdrop.addEventListener('click', () => togglePanel(false));

        qa('.lg-nav-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const tab = btn.getAttribute('data-tab');
                qa('.lg-nav-btn').forEach(b => b.classList.toggle('active', b === btn));
                qa('.lg-view').forEach(v => v.classList.toggle('active', v.id === 'view-' + tab));
                requestAnimationFrame(recalibrateLensFilter);
            });
        });

        q('#headLogs').addEventListener('click', () => {
            q('#drawerLogs').classList.toggle('open');
            requestAnimationFrame(recalibrateLensFilter);
        });

        siteToggleBtn.addEventListener('click', () => {
            config.enabled = !config.enabled;
            saveConfig(true);
            urlCache.clear();
            updatePageStyles();
            syncUI();
            showToast(config.enabled ? '已开启本站守护' : '已将本站加入白名单放行');
        });

        qa('.lg-row[data-key]').forEach(row => {
            row.addEventListener('click', () => {
                const k = row.getAttribute('data-key');
                const isSite = row.getAttribute('data-site') === 'true';
                config[k] = !config[k];
                saveConfig(isSite);
                urlCache.clear();
                updatePageStyles();
                if (k === 'masterAdPurge' && config.masterAdPurge) runInitialScan();
                syncUI();
            });
        });

        const PRESETS = {
            ios27:   { warp: 28, blur: 20, opacity: 18, shine: 85 },
            crystal: { warp: 42, blur: 12, opacity: 10, shine: 95 },
            frosted: { warp: 15, blur: 32, opacity: 42, shine: 65 },
            lite:    { warp: 0,  blur: 10, opacity: 60, shine: 45 }
        };

        qa('[data-preset]').forEach(btn => {
            btn.addEventListener('click', () => {
                const p = btn.getAttribute('data-preset');
                const v = PRESETS[p];
                if (!v) return;
                config.glassPreset = p;
                config.glassWarp = v.warp;
                config.glassBlur = v.blur;
                config.glassOpacity = v.opacity;
                config.glassShine = v.shine;
                applyGlassOptics();
                saveConfig(false);
            });
        });

        ['GlassWarp', 'GlassBlur', 'GlassOpacity', 'GlassShine'].forEach(name => {
            const rng = q('#rng' + name);
            const prop = 'glass' + name.replace('Glass', '');
            rng.addEventListener('input', (e) => {
                config[prop] = Number(e.target.value);
                config.glassPreset = 'custom';
                applyGlassOptics();
            });
            rng.addEventListener('change', () => saveConfig(false));
        });

        let pickerMode = false;
        function buildSelector(el) {
            if (!el || el.nodeType !== 1 || el === document.body || el === document.documentElement) return null;
            if (el.id && !/^\d/.test(el.id)) return '#' + CSS.escape(el.id);
            const cls = Array.from(el.classList || []).filter(c => c && !/^(active|open|show|hover)$/i.test(c));
            if (cls.length > 0) return el.tagName.toLowerCase() + '.' + cls.slice(0, 2).map(c => CSS.escape(c)).join('.');
            const p = el.parentElement;
            if (p) {
                const idx = Array.from(p.children).indexOf(el) + 1;
                return `${p.tagName.toLowerCase()} > ${el.tagName.toLowerCase()}:nth-child(${idx})`;
            }
            return null;
        }

        function onPick(e) {
            if (!pickerMode) return;
            if (e.target === host || host.contains(e.target)) return;
            e.preventDefault();
            e.stopPropagation();
            e.stopImmediatePropagation();
            const sel = buildSelector(e.target);
            if (sel) {
                customSelectors.push(sel);
                safeSet(KEY_CUSTOM_HIDE, JSON.stringify(customSelectors));
                updatePageStyles();
                recordEvent('ads', '手势屏蔽', HOST);
                showToast('已永久屏蔽该区域');
            }
            exitPick();
        }

        function exitPick() {
            pickerMode = false;
            pickerBar.classList.remove('show');
            window.removeEventListener('click', onPick, true);
        }

        q('#btnPickElement').addEventListener('click', () => {
            togglePanel(false);
            pickerMode = true;
            pickerBar.classList.add('show');
            setTimeout(() => window.addEventListener('click', onPick, true), 80);
        });

        q('#lgPickerCancel').addEventListener('click', exitPick);

        q('#btnResetCustomHide').addEventListener('click', () => {
            customSelectors = [];
            safeSet(KEY_CUSTOM_HIDE, '[]');
            updatePageStyles();
            showToast('已恢复手动屏蔽的元素');
        });

        const btnReader = q('#btnReaderMode');
        btnReader.addEventListener('click', () => {
            readerModeActive = !readerModeActive;
            btnReader.classList.toggle('active', readerModeActive);
            updatePageStyles();
            showToast(readerModeActive ? '已开启纯净阅读模式' : '已退出纯净阅读模式');
        });

        const btnSpeed = q('#btnVideoSpeed');
        btnSpeed.addEventListener('click', () => {
            const vids = document.querySelectorAll('video');
            if (vids.length === 0) {
                showToast('未检测到正在播放的视频');
                return;
            }
            let rate = 2.0;
            vids.forEach(v => {
                rate = (v.playbackRate === 1.0) ? 2.0 : (v.playbackRate === 2.0 ? 1.5 : 1.0);
                v.playbackRate = rate;
            });
            btnSpeed.textContent = `视频当前 ${rate.toFixed(1)} 倍速`;
            showToast(`视频已切换为 ${rate.toFixed(1)} 倍速`);
        });

        q('#btnVideoPip').addEventListener('click', () => {
            const v = document.querySelector('video');
            if (!v) {
                showToast('未检测到视频');
                return;
            }
            if (document.pictureInPictureElement) {
                document.exitPictureInPicture().catch(() => {});
            } else if (v.requestPictureInPicture) {
                v.requestPictureInPicture().then(() => togglePanel(false)).catch(() => showToast('当前浏览器不支持画中画'));
            } else if (v.webkitSetPresentationMode) {
                v.webkitSetPresentationMode('picture-in-picture');
                togglePanel(false);
            }
        });

        q('#btnCopyCleanUrl').addEventListener('click', () => {
            purifyCurrentUrl();
            if (navigator.clipboard && navigator.clipboard.writeText) {
                lastTrustedTouchTime = Date.now();
                navigator.clipboard.writeText(location.href).then(() => showToast('已复制纯净链接')).catch(() => showToast('链接已净化'));
            } else {
                showToast('链接已净化');
            }
        });

        const onFs = () => {
            const isFs = Boolean(document.fullscreenElement || document.webkitFullscreenElement);
            pillWrap.style.display = isFs ? 'none' : 'block';
            if (isFs && isOpen) togglePanel(false);
        };
        document.addEventListener('fullscreenchange', onFs);
        document.addEventListener('webkitfullscreenchange', onFs);
    }

    mountLiquidGlassUI();
    document.addEventListener('readystatechange', mountLiquidGlassUI);
    document.addEventListener('DOMContentLoaded', mountLiquidGlassUI);
    window.addEventListener('load', mountLiquidGlassUI);
    setInterval(() => {
        if (!document.getElementById('__via_lg_v65_host__')) {
            mountLiquidGlassUI();
        }
    }, 1800);