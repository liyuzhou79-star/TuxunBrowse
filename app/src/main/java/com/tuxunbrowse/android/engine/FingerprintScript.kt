package com.tuxunbrowse.android.engine

/**
 * Builds the document-start script that makes a WebView tab present
 * a [ResolvedFingerprint] (v0.144.0). Pure string assembly — pinned
 * by FingerprintScriptTest — injected through the same
 * WebViewCompat document-start channel as GuardScript and
 * PrivacyScript, registered BEFORE both so the profile's surface is
 * in place before any page (or guard) code wraps it.
 *
 * Covered, all driven by the profile: navigator identity (UA,
 * platform, language/languages, hardwareConcurrency, deviceMemory,
 * userAgentData with the profile's model), screen geometry and
 * devicePixelRatio, the time zone (Intl default zone + default
 * locale, Date.getTimezoneOffset, synthesised Date strings with a
 * daylight-aware zone name, locale date formatting, and — since
 * v0.207.0 — the local component face too: the component getters
 * and setters, toDateString/toTimeString, multi-field construction
 * and offset-less ISO parsing all read the profile zone instead of
 * the host's),
 * geolocation answers at the zone's city, canvas readback noise
 * (seeded, deterministic), WebGL unmasked vendor/renderer strings
 * AND the getParameter capability table for the profile's GPU,
 * AudioBuffer noise, ClientRects jitter, a fixed battery story,
 * and a leak-proofed live WebRTC (v0.160.0: the API stays
 * present like a real device's, connections are forced to
 * relay-only ICE and candidate events are filtered, so no host
 * or public address can surface; getUserMedia answers with a
 * permission refusal instead of being absent).
 * WebGPU adapter identity joined in v0.159.0: requestAdapter's
 * info answers vendor/architecture from the profile's GPU
 * family, so the third graphics API tells the same story as
 * the WebGL strings and capability table.
 *
 * v0.145.0 additions, same profile-driven rule: the region's
 * speech-synthesis voice list, matchMedia color-gamut /
 * dynamic-range answers per the device's panel, a navigator
 * .connection story, a sanitised mediaDevices.enumerateDevices
 * list (empty labels, no real device names), and a per-profile
 * fixed scale on canvas measureText widths so font metrics no
 * longer report the host device's raw values. The noise seed is
 * the profile's own constant, so one profile presents the SAME
 * fingerprint across sessions — a real fingerprint browser's
 * profile is stable, and per-visit churn would itself be a tell.
 *
 * v0.177.0: a stealth pass over every wrapper below — each
 * installed function is registered with the string a native
 * function would produce and Function.prototype.toString
 * answers from that registry; name/length are copied from the
 * originals; prototype methods are installed non-enumerable
 * like the natives they replace; the def() getters carry
 * native-style names. This lowers the tamper-detection
 * surface (the -10%/-5% "tampered function" grades) — it does
 * NOT erase it: a fresh realm (an iframe's unhooked
 * Function.prototype.toString) still shows real wrapper
 * sources, and the hook itself can be cross-examined.
 * Recorded honestly in the checkpoint.
 *
 * v0.208.0: the half-covered faces audit (the v0.207.0 lesson —
 * a surface covered in one component and host-voiced in the
 * next is worse than an honestly open one). Geolocation answers
 * are built on the real GeolocationPosition /
 * GeolocationCoordinates prototypes now (a plain object literal
 * failed instanceof and the [object …] tags), watchPosition
 * hands out unique ids and clearWatch really cancels;
 * permissions.query({name: 'geolocation'}) answers 'granted'
 * so it agrees with the positions the page receives (the
 * engine's own state never can: the app disables geolocation
 * at the settings level); matchMedia also answers the two
 * stock-preference queries (prefers-reduced-motion,
 * forced-colors) for the stock persona instead of passing the
 * host OS's accessibility settings through.
 *
 * v0.210.0: the cold-surface sweep. The UA-CH brand list is
 * computed by Chromium's own seeded algorithm now (GREASE
 * spelling, GREASE version and list order all follow the
 * claimed major version — the old frozen triple matched no
 * real Chrome); navigator.connection also pins downlinkMax
 * (the one NetworkInformation field the v0.145.0 story left
 * to the prototype, where it read as an exception/undefined
 * instead of a number); the sensor stream also serves
 * deviceorientationabsolute — the one orientation event
 * type the v0.190.0 interception never claimed, so it used
 * to deliver the HOST's live compass stream — with
 * absolute=true events over the same held-phone story (the
 * plain deviceorientation event stays absolute=false,
 * Chrome's post-M50 relative default, verified); and
 * navigator.getGamepads answers four empty slots, so a
 * controller paired to the host cannot name itself to the
 * page. Surfaces inventoried and deliberately NOT touched
 * (engine gaps with no interface-grade JS answer, recorded
 * in the checkpoint): navigator.xr, SpeechRecognition,
 * navigator.share/canShare; navigator.keyboard is absent
 * on Android Chrome too, so its absence here agrees.
 *
 * v0.174.0: the font ENUMERATION surface joins the covered set —
 * document.fonts.check() answers known system families from the
 * profile device's stock font set (see FingerprintProfiles),
 * borrowed as a coverage idea from the open-source anti-detect
 * projects' patch lists (Camoufox's font surface, the Chromium
 * fingerprint patches' font item) and re-implemented here in the
 * injection layer. The font files themselves still render on
 * the host: enumeration now follows the profile and measureText
 * scaling blurs measurement, but glyphs are not swapped.
 *
 * NOT covered, on purpose and recorded in the checkpoint: the HTTP
 * Accept-Language header (the WebView offers no per-request header
 * rewrite; the JS-visible language is the profile's), and
 * anything on the Gecko engine (no per-session surface — see
 * the checkpoint's Gecko findings; fingerprint tabs are
 * WebView-only). WebGL readPixels noise joined the covered set
 * in v0.149.0.
 */
object FingerprintScript {

    // Single-entry memo (v0.165.0 startup audit): the payload is
    // a large template assembly, and the tab layer used to re-run
    // it on the main thread at EVERY page start (syncFingerprint-
    // Script compares the fresh payload against the registered
    // one) plus at every tab creation. The assembly is a pure
    // function of the resolved profile, so the last built payload
    // is kept and handed back while the profile is unchanged —
    // the same per-process caching GuardScript has had since
    // v0.91.0. A profile switch misses once, rebuilds, and the
    // bytes are exactly what an uncached build would produce.
    @Volatile
    private var cachedFor: Pair<ResolvedFingerprint, FingerprintProtection>? =
        null

    @Volatile
    private var cachedPayload: String? = null

    fun payload(
        resolved: ResolvedFingerprint,
        protection: FingerprintProtection = FingerprintProtection()
    ): String {
        val key = resolved to protection
        cachedPayload?.let { if (cachedFor == key) return it }
        return synchronized(this) {
            cachedPayload?.let {
                if (cachedFor == key) return it
            }
            val built = buildPayload(resolved, protection)
            cachedFor = key
            cachedPayload = built
            built
        }
    }

    private fun buildPayload(
        resolved: ResolvedFingerprint,
        protection: FingerprintProtection
    ): String {
        val p = resolved.profile
        val d = resolved.device
        val tz = resolved.timezone
        val languages = resolved.languages.joinToString(",") { jsStr(it) }
        val voices = p.voices.joinToString(",") { v ->
            "{ name: ${jsStr(v.name)}, lang: ${jsStr(v.lang)}," +
                " isDefault: ${v.isDefault} }"
        }
        val daylight = tz.zoneDaylightName?.let { jsStr(it) } ?: "null"
        // Font sets as JS lookup objects, lower-cased family
        // names (v0.174.0). Names are plain ASCII, so direct
        // quoting is safe here.
        val fpFonts = d.fonts.joinToString(", ") {
            "'" + it.lowercase() + "': 1"
        }
        val knownFonts = FingerprintProfiles.KNOWN_FONT_FAMILIES
            .joinToString(", ") { "'" + it.lowercase() + "': 1" }
        // UA-CH brand lists for the claimed version, computed
        // by Chromium's own seeded algorithm (v0.210.0, see
        // FingerprintProfiles.uaBrandList) — spelling, GREASE
        // version and list order all follow the major version
        // instead of one frozen triple no real Chrome ships.
        val majorInt = resolved.chromeMajor.toIntOrNull()
            ?: FingerprintProfiles.CHROME_MAJOR.toInt()
        fun brandLiteral(
            list: List<Pair<String, String>>
        ): String = list.joinToString(", ") {
            "{ brand: ${jsStr(it.first)}," +
                " version: ${jsStr(it.second)} }"
        }
        val brandsJs = brandLiteral(
            FingerprintProfiles.uaBrandList(majorInt)
        )
        val fullBrandsJs = brandLiteral(
            FingerprintProfiles.uaFullVersionList(
                majorInt, resolved.chromeFullVersion
            )
        )
        return """
(function () {
  'use strict';
  var SEED = ${resolved.seed};
  // Per-surface protection switches (v0.190.0): each section
  // below checks its own flag and installs nothing when the
  // user turned that surface off — the page then reads the
  // host's real values for that one surface only.
  var FP_OPT_CANVAS = ${protection.canvasNoise};
  var FP_OPT_WEBGL = ${protection.webglDisguise};
  var FP_OPT_AUDIO = ${protection.audioNoise};
  var FP_OPT_FONTS = ${protection.fontSet};
  var FP_OPT_WEBRTC = ${protection.webrtcRelay};
  var FP_OPT_SENSORS = ${protection.sensors};
  var FP_OPT_BATTERY = ${protection.battery};
  var FP_OPT_CHROME = ${protection.chromeObject};
  var FP_OPT_TIMEZONE = ${protection.timezone};
  var FP_OPT_LANGUAGE = ${protection.language};
  var FP_OPT_GEO = ${protection.geolocation};
  var FP_OPT_SCREEN = ${protection.screen};
  var FP_OPT_IDENTITY = ${protection.deviceIdentity};
  var FP_OPT_CONNECTION = ${protection.connection};
  var LANG = ${jsStr(resolved.language)};
  var TZ = ${jsStr(tz.id)};
  var ZONE_NAME = ${jsStr(tz.zoneName)};
  var DAYLIGHT_NAME = $daylight;
  function hash(x) {
    x = (x ^ 61) ^ (x >>> 16); x = x + (x << 3); x = x ^ (x >>> 4);
    x = x * 0x27d4eb2d; x = x ^ (x >>> 15); return x >>> 0;
  }
  // Shared canvas noise (v0.182.0): ONE rule for every pixel
  // path — 2D getImageData, toDataURL / toBlob (which funnel
  // through getImageData on a copy), OffscreenCanvas and
  // WebGL readPixels. Same seed, same buffer position, same
  // delta, so a checker comparing the paths against each
  // other finds one story instead of two noise signatures.
  // (readPixels used to run a different rule — every 499th
  // byte, a different hash — with a visibly different
  // density.) The rule is a pure function of (seed, buffer
  // position): repeated reads of the same pixels agree.
  // Content-keyed (v0.220.0): the hash also mixes the pixel's
  // own RGB, so the deviation lattice is a function of the
  // IMAGE, not a fixed grid. A position-only lattice can be
  // mapped with one flat calibration image and then verified
  // against any second image — proof of synthetic noise.
  // Keyed by content, a calibration on one colour predicts
  // nothing about another, while identical pixels at the
  // same position still draw the identical delta, so repeat
  // reads and cross-path reads keep agreeing.
  function noiseBytes(d) {
    if (!FP_OPT_CANVAS) return;
    for (var i = 0; i + 3 < d.length; i += 4) {
      var h = hash(SEED * 1000003 + i +
        d[i] * 7 + d[i + 1] * 13 + d[i + 2] * 17);
      if (h % 211 === 0) {
        var delta = (h % 2 === 0) ? 1 : -1;
        d[i] = Math.max(0, Math.min(255, d[i] + delta));
        d[i + 1] = Math.max(0, Math.min(255, d[i + 1] + delta));
      }
    }
  }
  // Stealth registry (v0.177.0): wrapper function -> the
  // toString a native function would produce. Installed
  // before everything else so def() and every section below
  // can register what they install.
  var FP_TOSTRING = new WeakMap();
  function fpNativeString(name) {
    return 'function ' + name + '() { [native code] }';
  }
  function fixFn(fn, orig) {
    try {
      if (orig) {
        Object.defineProperty(fn, 'name',
          { value: orig.name, configurable: true });
        Object.defineProperty(fn, 'length',
          { value: orig.length, configurable: true });
        FP_TOSTRING.set(fn, fpNativeString(orig.name));
      }
    } catch (e) {}
    return fn;
  }
  function fixFnAs(fn, name, length) {
    try {
      Object.defineProperty(fn, 'name',
        { value: name, configurable: true });
      Object.defineProperty(fn, 'length',
        { value: length, configurable: true });
      FP_TOSTRING.set(fn, fpNativeString(name));
    } catch (e) {}
    return fn;
  }
  var origFnToString = Function.prototype.toString;
  var fpFnToString = function () {
    var fake = FP_TOSTRING.get(this);
    return fake || origFnToString.call(this);
  };
  fixFnAs(fpFnToString, 'toString', 0);
  Function.prototype.toString = fpFnToString;
  // Install like a native prototype method: non-enumerable,
  // writable, configurable — plain assignment would leave an
  // enumerable own property where natives have none.
  function setMethod(obj, name, fn) {
    try {
      Object.defineProperty(obj, name, {
        value: fn, writable: true,
        enumerable: false, configurable: true
      });
    } catch (e) {
      try { obj[name] = fn; } catch (e2) {}
    }
  }
  function def(obj, prop, value) {
    try {
      var getter = function () { return value; };
      // Native accessors stringify as
      // "function get <prop>() { [native code] }" and carry
      // that name; an anonymous getter is a tamper tell.
      Object.defineProperty(getter, 'name',
        { value: 'get ' + prop, configurable: true });
      FP_TOSTRING.set(getter,
        'function get ' + prop + '() { [native code] }');
      Object.defineProperty(obj, prop, {
        get: getter, configurable: true
      });
    } catch (e) {}
  }
  // Install a wrapped method where the native one lives: on
  // the PROTOTYPE behind the instance the API hangs off
  // (v0.220.0). setMethod(instance, ...) leaves an own data
  // property where a descriptor sweep expects none — the
  // native is inherited, so the wrapper is installed on the
  // prototype and read back through the instance; any failure
  // falls back to the instance install rather than losing
  // the wrapper.
  function setMethodOn(instance, name, fn) {
    try {
      var proto = Object.getPrototypeOf(instance);
      if (proto && proto !== Object.prototype) {
        Object.defineProperty(proto, name, {
          value: fn, writable: true,
          enumerable: false, configurable: true
        });
        if (instance[name] === fn) return;
      }
    } catch (e) {}
    setMethod(instance, name, fn);
  }
  // An on* event-handler attribute with a real backing store
  // (v0.220.0): fabricated interface objects (battery,
  // permission status, media query list) need handler props
  // a page can set and read back, shaped like the native
  // get/set pair instead of a plain data slot.
  function defHandler(obj, prop) {
    try {
      var store = { fn: null };
      var get = function () { return store.fn; };
      Object.defineProperty(get, 'name',
        { value: 'get ' + prop, configurable: true });
      FP_TOSTRING.set(get,
        'function get ' + prop + '() { [native code] }');
      var set = function (fn) {
        store.fn = (typeof fn === 'function') ? fn : null;
      };
      Object.defineProperty(set, 'name',
        { value: 'set ' + prop, configurable: true });
      FP_TOSTRING.set(set,
        'function set ' + prop + '() { [native code] }');
      Object.defineProperty(obj, prop, {
        get: get, set: set, configurable: true
      });
    } catch (e) {}
  }
  // A method on a fabricated object, shaped like a native
  // prototype method: non-enumerable data property with the
  // name/length/toString registry applied (v0.220.0). Plain
  // assignment would leave an enumerable own property where
  // natives have none.
  function stubMethod(obj, name, len, impl) {
    try {
      Object.defineProperty(obj, name, {
        value: fixFnAs(impl, name, len),
        writable: true, configurable: true
      });
    } catch (e) {}
  }
  function section(fn) { try { fn(); } catch (e) {} }

  section(function () {
    var nav = window.Navigator ? Navigator.prototype : navigator;
    // Device identity (v0.224.0): the UA string, the platform,
    // UA-CH and the hardware numbers are ONE switch — half
    // profile, half host inside a single read would contradict
    // itself, so they install together or not at all. Language
    // rides its own switch beside them.
    if (FP_OPT_IDENTITY) {
    def(nav, 'userAgent', ${jsStr(d.userAgent)});
    def(nav, 'platform', ${jsStr(FingerprintProfiles.PLATFORM)});
    }
    if (FP_OPT_LANGUAGE) {
    def(nav, 'language', LANG);
    def(nav, 'languages', Object.freeze([$languages]));
    }
    // Belt and braces (v0.145.0): some detectors read the values
    // off the navigator instance in ways that bypass the prototype
    // getter, so pin the same answers on the instance itself.
    if (FP_OPT_LANGUAGE) {
    def(navigator, 'language', LANG);
    def(navigator, 'languages', Object.freeze([$languages]));
    }
    // Platform joins the instance pinning (v0.192.0): the
    // prototype def above already covers ordinary reads,
    // but a detector that grabbed the instance early (or a
    // realm where the prototype def was refused) should
    // still meet the profile's answer, not the host's.
    if (FP_OPT_IDENTITY) {
    def(navigator, 'platform', ${jsStr(FingerprintProfiles.PLATFORM)});
    }
    if (FP_OPT_IDENTITY) {
    def(nav, 'hardwareConcurrency', ${d.hardwareConcurrency});
    def(nav, 'deviceMemory', ${d.deviceMemory});
    var brands = [$brandsJs];
    var fullBrands = [$fullBrandsJs];
    // Native brand lists are frozen solid (v0.220.0): the
    // array and its entries refuse writes, so a page that
    // pokes at them and reads back a changed value would
    // know the object was script-made.
    brands.forEach(function (b) { Object.freeze(b); });
    Object.freeze(brands);
    fullBrands.forEach(function (b) { Object.freeze(b); });
    Object.freeze(fullBrands);
    // Built on the real NavigatorUAData prototype
    // (v0.220.0): a plain literal failed instanceof and
    // Object.prototype.toString, and its five enumerable
    // keys were a sweep-visible tell — the fabricated-object
    // lesson the geolocation section learned in v0.208.0,
    // applied here. Attributes ride def() getters and the
    // two methods are non-enumerable stubMethod slots, so
    // the object carries no own enumerable keys.
    var uaData = window.NavigatorUAData
      ? Object.create(NavigatorUAData.prototype) : {};
    def(uaData, 'brands', brands);
    def(uaData, 'mobile', true);
    def(uaData, 'platform', '${FingerprintProfiles.UA_PLATFORM}');
    stubMethod(uaData, 'getHighEntropyValues', 1,
      function (hints) {
        var all = {
          architecture: '${FingerprintProfiles.UA_ARCHITECTURE}', bitness: '${FingerprintProfiles.UA_BITNESS}', brands: brands,
          fullVersionList: fullBrands,
          mobile: true, model: ${jsStr(d.uaModel)}, platform: '${FingerprintProfiles.UA_PLATFORM}',
          platformVersion: '14.0.0', uaFullVersion: '${resolved.chromeFullVersion}',
          formFactors: ['Mobile'], wow64: false
        };
        var out = { brands: brands, mobile: true, platform: '${FingerprintProfiles.UA_PLATFORM}' };
        (hints || []).forEach(function (h) { if (h in all) out[h] = all[h]; });
        return Promise.resolve(out);
      });
    stubMethod(uaData, 'toJSON', 0, function () {
      return { brands: brands, mobile: true, platform: '${FingerprintProfiles.UA_PLATFORM}' };
    });
    def(nav, 'userAgentData', uaData);
    }
  });

  section(function () {
    if (!FP_OPT_SCREEN) return;
    var scr = window.Screen ? Screen.prototype : screen;
    // Screen geometry follows posture (v0.192.0,
    // borrowed-table item 7): the stored pair is the
    // portrait shape; in landscape the pair swaps, in
    // step with screen.orientation — which follows the
    // host's real posture by design (v0.183.0). A static
    // portrait pair sitting under a landscape
    // orientation.type was a contradiction a cross-check
    // could read at a glance (and in a floating window
    // the mismatch is worse). The getters are shaped
    // like the native ones, name and toString included.
    var PW = ${d.screenWidth}, PH = ${d.screenHeight};
    function fpLandscape() {
      try {
        if (screen.orientation && screen.orientation.type) {
          return screen.orientation.type
            .indexOf('landscape') === 0;
        }
      } catch (e) {}
      try {
        return window.innerWidth > window.innerHeight;
      } catch (e) { return false; }
    }
    function defDyn(obj, prop, fn) {
      try {
        var getter = function () { return fn(); };
        Object.defineProperty(getter, 'name',
          { value: 'get ' + prop, configurable: true });
        FP_TOSTRING.set(getter,
          'function get ' + prop + '() { [native code] }');
        Object.defineProperty(obj, prop, {
          get: getter, configurable: true
        });
      } catch (e) {}
    }
    defDyn(scr, 'width',
      function () { return fpLandscape() ? PH : PW; });
    defDyn(scr, 'height',
      function () { return fpLandscape() ? PW : PH; });
    defDyn(scr, 'availWidth',
      function () { return fpLandscape() ? PH : PW; });
    defDyn(scr, 'availHeight',
      function () { return fpLandscape() ? PW : PH; });
    // Colour depth (v0.150.0): every Android Chrome reports 24;
    // pinning it keeps the answer the profile's even on a host
    // panel pipeline that reports otherwise.
    def(scr, 'colorDepth', 24);
    def(scr, 'pixelDepth', 24);
    // devicePixelRatio lives on the Window prototype as a
    // WebIDL attribute (v0.220.0): pinning it on the window
    // object itself left an own accessor where the native
    // has none. Install on the prototype with the native
    // enumerable flag; fall back to the plain def() on the
    // window when the prototype cannot be reached.
    (function () {
      var ratio = ${d.devicePixelRatio};
      try {
        var wp = Object.getPrototypeOf(window);
        if (!wp) throw new Error('no proto');
        var getter = function () { return ratio; };
        Object.defineProperty(getter, 'name',
          { value: 'get devicePixelRatio',
            configurable: true });
        FP_TOSTRING.set(getter,
          'function get devicePixelRatio()' +
          ' { [native code] }');
        Object.defineProperty(wp, 'devicePixelRatio', {
          get: getter, enumerable: true, configurable: true
        });
        if (window.devicePixelRatio === ratio) return;
      } catch (e) {}
      def(window, 'devicePixelRatio', ratio);
    })();
    // Touch points (v0.150.0, behaviour surface): Android Chrome
    // reports 5 on the profile devices and on phones generally.
    // The host value would usually agree — but "usually" is how
    // a surface leaks, so it is pinned like everything else.
    var nav2 = window.Navigator ? Navigator.prototype : navigator;
    def(nav2, 'maxTouchPoints', 5);
  });

  var OrigDTF = Intl.DateTimeFormat;
  function tzOffsetMinutes(date) {
    var dtf = new OrigDTF('en-US', {
      timeZone: TZ, hourCycle: 'h23', year: 'numeric', month: '2-digit',
      day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit'
    });
    var map = {};
    dtf.formatToParts(date).forEach(function (part) {
      map[part.type] = part.value;
    });
    var asUTC = Date.UTC(+map.year, +map.month - 1, +map.day,
      +map.hour, +map.minute, +map.second);
    return Math.round(
      (asUTC - Math.floor(date.getTime() / 1000) * 1000) / 60000);
  }
  section(function () {
    if (!FP_OPT_TIMEZONE && !FP_OPT_LANGUAGE) return;
    // supportedLocalesOf without .bind (v0.220.0): a bound
    // function reports name 'bound supportedLocalesOf' and
    // stringifies as 'function () { [native code] }' — the
    // missing name is a one-line tell on every Intl wrapper.
    // A named wrapper registered with fixFnAs carries the
    // native name, length and toString instead.
    function fpSLO(orig) {
      return fixFnAs(function () {
        return orig.supportedLocalesOf.apply(orig, arguments);
      }, 'supportedLocalesOf', 1);
    }
    function FpDTF(locales, options) {
      var opts = options ? Object.assign({}, options) : {};
      if (!opts.timeZone && FP_OPT_TIMEZONE) opts.timeZone = TZ;
      var loc = (FP_OPT_LANGUAGE &&
        (locales === undefined || locales === null ||
        (Array.isArray(locales) && locales.length === 0)))
        ? LANG : locales;
      return new OrigDTF(loc, opts);
    }
    FpDTF.prototype = OrigDTF.prototype;
    // Non-enumerable like the native static (v0.220.0):
    // plain assignment would leave the name enumerable on
    // the constructor.
    Object.defineProperty(FpDTF, 'supportedLocalesOf', {
      value: fpSLO(OrigDTF), writable: true,
      enumerable: false, configurable: true
    });
    fixFn(FpDTF, OrigDTF);
    setMethod(Intl, 'DateTimeFormat', FpDTF);
    if (FP_OPT_LANGUAGE) {
    var OrigNF = Intl.NumberFormat;
    function FpNF(locales, options) {
      var loc = (locales === undefined || locales === null ||
        (Array.isArray(locales) && locales.length === 0)) ? LANG : locales;
      return new OrigNF(loc, options);
    }
    FpNF.prototype = OrigNF.prototype;
    Object.defineProperty(FpNF, 'supportedLocalesOf', {
      value: fpSLO(OrigNF), writable: true,
      enumerable: false, configurable: true
    });
    fixFn(FpNF, OrigNF);
    setMethod(Intl, 'NumberFormat', FpNF);
    // The rest of the Intl family (v0.163.0): the two wrappers
    // above covered the formats pages use daily, but the
    // DEFAULT LOCALE of every other Intl constructor still
    // resolved to the host's — a cross-check asking
    // new Intl.Collator().resolvedOptions().locale (or
    // PluralRules, or DisplayNames) read the real device's
    // language while DateTimeFormat claimed the profile's.
    // Same rule for the whole family: no explicit locales
    // means the profile's language. String.prototype
    // .localeCompare rides on Collator internally, so it
    // follows without its own hook.
    ['Collator', 'PluralRules', 'RelativeTimeFormat',
      'ListFormat', 'DisplayNames', 'Segmenter'].forEach(
      function (name) {
        var Orig = Intl[name];
        if (!Orig) return;
        function FpIntlCtor(locales, options) {
          var loc = (locales === undefined || locales === null ||
            (Array.isArray(locales) && locales.length === 0))
            ? LANG : locales;
          return new Orig(loc, options);
        }
        FpIntlCtor.prototype = Orig.prototype;
        if (Orig.supportedLocalesOf) {
          Object.defineProperty(FpIntlCtor,
            'supportedLocalesOf', {
              value: fpSLO(Orig), writable: true,
              enumerable: false, configurable: true
            });
        }
        fixFn(FpIntlCtor, Orig);
        setMethod(Intl, name, FpIntlCtor);
      });
    }
  });
  section(function () {
    if (!FP_OPT_TIMEZONE) return;
    setMethod(Date.prototype, 'getTimezoneOffset',
      fixFn(function () {
        return -tzOffsetMinutes(this);
      }, Date.prototype.getTimezoneOffset));
    setMethod(Date.prototype, 'toLocaleString',
      fixFn(function (locales, options) {
        return new Intl.DateTimeFormat(locales, options)
          .format(this);
      }, Date.prototype.toLocaleString));
    setMethod(Date.prototype, 'toLocaleDateString',
      fixFn(function (locales, options) {
        return new Intl.DateTimeFormat(locales, options)
          .format(this);
      }, Date.prototype.toLocaleDateString));
    setMethod(Date.prototype, 'toLocaleTimeString',
      fixFn(function (locales, options) {
        return new Intl.DateTimeFormat(locales, options)
          .format(this);
      }, Date.prototype.toLocaleTimeString));
    // String forms from one shared parts builder: toString
    // keeps its long-standing synthesis; toDateString and
    // toTimeString used to fall through to the host-zone
    // natives (toTimeString printed the host's GMT offset and
    // zone name outright) and are built from the same parts
    // now (v0.207.0).
    function fpStringParts(date) {
      var dtf = new OrigDTF('en-US', {
        timeZone: TZ, hourCycle: 'h23', weekday: 'short',
        year: 'numeric', month: 'short', day: '2-digit',
        hour: '2-digit', minute: '2-digit', second: '2-digit'
      });
      var map = {};
      dtf.formatToParts(date).forEach(function (part) {
        map[part.type] = part.value;
      });
      var off = tzOffsetMinutes(date);
      var sign = off < 0 ? '-' : '+';
      var abs = Math.abs(off);
      // Daylight-aware zone name (v0.145.0): the standard
      // offset is the smaller (more negative, for the
      // northern-hemisphere zones in the profiles) of the
      // January/July offsets; a date whose offset sits above
      // it is in daylight time and takes the daylight name.
      // Zones without DST keep the one name.
      var name = ZONE_NAME;
      if (DAYLIGHT_NAME) {
        var jan = tzOffsetMinutes(new OrigDate(
          OrigDate.UTC(+map.year, 0, 1)));
        var jul = tzOffsetMinutes(new OrigDate(
          OrigDate.UTC(+map.year, 6, 1)));
        if (off > Math.min(jan, jul)) name = DAYLIGHT_NAME;
      }
      function pad2(n) { return (n < 10 ? '0' : '') + n; }
      return {
        date: map.weekday + ' ' + map.month + ' ' + map.day +
          ' ' + map.year,
        time: pad2(+map.hour) + ':' + map.minute + ':' +
          map.second + ' GMT' + sign +
          pad2(Math.floor(abs / 60)) + pad2(abs % 60) +
          ' (' + name + ')'
      };
    }
    setMethod(Date.prototype, 'toString',
      fixFn(function () {
        if (isNaN(this.getTime())) return 'Invalid Date';
        var p = fpStringParts(this);
        return p.date + ' ' + p.time;
      }, Date.prototype.toString));
    setMethod(Date.prototype, 'toDateString',
      fixFn(function () {
        if (isNaN(this.getTime())) return 'Invalid Date';
        return fpStringParts(this).date;
      }, Date.prototype.toDateString));
    setMethod(Date.prototype, 'toTimeString',
      fixFn(function () {
        if (isNaN(this.getTime())) return 'Invalid Date';
        return fpStringParts(this).time;
      }, Date.prototype.toTimeString));
    // Local component face (v0.207.0): until now only the
    // offset and the string forms spoke for the profile zone
    // — the local getters still answered for the HOST zone,
    // so getHours() contradicted getTimezoneOffset() whenever
    // the two differed. Everything below reads (and writes)
    // the instant through the profile zone: shift by the
    // zone's offset, then use the UTC fields.
    var OrigDate = Date;
    function fpLocalDate(date) {
      return new OrigDate(date.getTime() +
        tzOffsetMinutes(date) * 60000);
    }
    // Inverse of fpLocalDate: the instant whose profile-zone
    // fields equal the given components. The offset depends
    // on the instant itself, so solve once, then re-solve
    // with the offset at the solution (converges everywhere
    // except inside the spring-forward gap, where native
    // behaviour is implementation-defined anyway).
    function fpFromLocal(y, mo, d, h, mi, s, ms) {
      var guess = OrigDate.UTC(y, mo, d, h, mi, s, ms);
      if (isNaN(guess)) return NaN;
      var t = guess - tzOffsetMinutes(new OrigDate(guess)) * 60000;
      return guess - tzOffsetMinutes(new OrigDate(t)) * 60000;
    }
    function fpLocalParts(date) {
      var s = fpLocalDate(date);
      return [s.getUTCFullYear(), s.getUTCMonth(), s.getUTCDate(),
        s.getUTCHours(), s.getUTCMinutes(), s.getUTCSeconds(),
        s.getUTCMilliseconds()];
    }
    function fpApplyLocal(date, parts) {
      return date.setTime(fpFromLocal(parts[0], parts[1],
        parts[2], parts[3], parts[4], parts[5], parts[6]));
    }
    [['getFullYear', 'getUTCFullYear'],
     ['getMonth', 'getUTCMonth'], ['getDate', 'getUTCDate'],
     ['getDay', 'getUTCDay'], ['getHours', 'getUTCHours'],
     ['getMinutes', 'getUTCMinutes'],
     ['getSeconds', 'getUTCSeconds']].forEach(function (pair) {
      setMethod(Date.prototype, pair[0], fixFn(function () {
        if (isNaN(this.getTime())) return NaN;
        return fpLocalDate(this)[pair[1]]();
      }, Date.prototype[pair[0]]));
    });
    setMethod(Date.prototype, 'getYear',
      fixFn(function () {
        if (isNaN(this.getTime())) return NaN;
        return this.getFullYear() - 1900;
      }, Date.prototype.getYear));
    // Setters write through the same lens: the components
    // are profile-zone wall time. (setMilliseconds and
    // setTime are offset-independent and stay native.)
    // Native quirk kept: setFullYear alone revives an
    // invalid date, from New Year midnight; the other
    // setters return NaN on one.
    setMethod(Date.prototype, 'setFullYear',
      fixFn(function (y, mo, d) {
        var parts = isNaN(this.getTime())
          ? [0, 0, 1, 0, 0, 0, 0] : fpLocalParts(this);
        parts[0] = +y;
        if (mo !== undefined) parts[1] = +mo;
        if (d !== undefined) parts[2] = +d;
        return fpApplyLocal(this, parts);
      }, Date.prototype.setFullYear));
    setMethod(Date.prototype, 'setMonth',
      fixFn(function (mo, d) {
        if (isNaN(this.getTime())) return NaN;
        var parts = fpLocalParts(this);
        parts[1] = +mo;
        if (d !== undefined) parts[2] = +d;
        return fpApplyLocal(this, parts);
      }, Date.prototype.setMonth));
    setMethod(Date.prototype, 'setDate',
      fixFn(function (d) {
        if (isNaN(this.getTime())) return NaN;
        var parts = fpLocalParts(this);
        parts[2] = +d;
        return fpApplyLocal(this, parts);
      }, Date.prototype.setDate));
    setMethod(Date.prototype, 'setHours',
      fixFn(function (h, mi, s, ms) {
        if (isNaN(this.getTime())) return NaN;
        var parts = fpLocalParts(this);
        parts[3] = +h;
        if (mi !== undefined) parts[4] = +mi;
        if (s !== undefined) parts[5] = +s;
        if (ms !== undefined) parts[6] = +ms;
        return fpApplyLocal(this, parts);
      }, Date.prototype.setHours));
    setMethod(Date.prototype, 'setMinutes',
      fixFn(function (mi, s, ms) {
        if (isNaN(this.getTime())) return NaN;
        var parts = fpLocalParts(this);
        parts[4] = +mi;
        if (s !== undefined) parts[5] = +s;
        if (ms !== undefined) parts[6] = +ms;
        return fpApplyLocal(this, parts);
      }, Date.prototype.setMinutes));
    setMethod(Date.prototype, 'setSeconds',
      fixFn(function (s, ms) {
        if (isNaN(this.getTime())) return NaN;
        var parts = fpLocalParts(this);
        parts[5] = +s;
        if (ms !== undefined) parts[6] = +ms;
        return fpApplyLocal(this, parts);
      }, Date.prototype.setSeconds));
    setMethod(Date.prototype, 'setYear',
      fixFn(function (y) {
        y = +y;
        if (y >= 0 && y <= 99) y += 1900;
        return this.setFullYear(y);
      }, Date.prototype.setYear));
    // Constructor and Date.parse: multi-field construction
    // and offset-less ISO strings name profile-zone wall
    // time, not the host's. Date-only strings stay with the
    // native parser (the spec reads those as UTC), as does
    // anything carrying an explicit offset or a non-ISO
    // shape.
    function fpParse(str) {
      if (typeof str === 'string') {
        var m = /^\s*(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})(?::(\d{2})(?:\.(\d+))?)?\s*$/
          .exec(str);
        if (m) {
          var ms = m[7] ? +((m[7] + '000').slice(0, 3)) : 0;
          return fpFromLocal(+m[1], +m[2] - 1, +m[3], +m[4],
            +m[5], +(m[6] || 0), ms);
        }
      }
      return OrigDate.parse(str);
    }
    function FpDate() {
      if (!new.target) return new OrigDate().toString();
      if (arguments.length === 0) return new OrigDate();
      if (arguments.length === 1) {
        var v = arguments[0];
        return typeof v === 'string'
          ? new OrigDate(fpParse(v)) : new OrigDate(v);
      }
      var a = arguments;
      function num(i, dflt) {
        return a.length > i ? +a[i] : dflt;
      }
      return new OrigDate(fpFromLocal(+a[0], +a[1],
        num(2, 1), num(3, 0), num(4, 0), num(5, 0),
        num(6, 0)));
    }
    FpDate.prototype = OrigDate.prototype;
    // Statics as non-enumerable data properties (v0.220.0):
    // plain assignment left now/UTC/parse enumerable on the
    // constructor, where the native's are not —
    // Object.keys(Date) is empty on a real engine.
    function fpDateStatic(name, fn) {
      Object.defineProperty(FpDate, name, {
        value: fixFn(fn, OrigDate[name]),
        writable: true, enumerable: false, configurable: true
      });
    }
    fpDateStatic('now', function () { return OrigDate.now(); });
    fpDateStatic('UTC', function () {
      return OrigDate.UTC.apply(null, arguments);
    });
    fpDateStatic('parse', function (s) { return fpParse(s); });
    fixFn(FpDate, OrigDate);
    setMethod(window, 'Date', FpDate);
  });

  section(function () {
    if (!FP_OPT_GEO) return;
    if (!navigator.geolocation) return;
    var proto = Object.getPrototypeOf(navigator.geolocation);
    // Position shape (v0.208.0): the answer used to be a plain
    // object literal — the right numbers on the wrong object.
    // Real Chrome hands out GeolocationPosition /
    // GeolocationCoordinates instances, and a cross-check
    // reading instanceof or the [object …] string tags met
    // "[object Object]" here while the voices, devices and
    // battery sections already build on the real prototypes.
    // Same rule now. altitude/heading/speed stay null: a
    // stationary phone indoors reports exactly that, and the
    // accuracy is a plain plausible fix value.
    function position() {
      var coords = Object.create(
        window.GeolocationCoordinates
          ? GeolocationCoordinates.prototype
          : Object.prototype);
      def(coords, 'latitude', ${tz.latitude});
      def(coords, 'longitude', ${tz.longitude});
      def(coords, 'accuracy', 35);
      def(coords, 'altitude', null);
      def(coords, 'altitudeAccuracy', null);
      def(coords, 'heading', null);
      def(coords, 'speed', null);
      var pos = Object.create(
        window.GeolocationPosition
          ? GeolocationPosition.prototype : Object.prototype);
      def(pos, 'coords', coords);
      def(pos, 'timestamp', Date.now());
      if (!window.GeolocationPosition) {
        // The real prototype carries toJSON; the plain-object
        // fallback has to bring its own or the shape differs
        // by one more member.
        pos.toJSON = fixFnAs(function () {
          return {
            coords: {
              latitude: ${tz.latitude},
              longitude: ${tz.longitude},
              accuracy: 35, altitude: null,
              altitudeAccuracy: null, heading: null, speed: null
            },
            timestamp: this.timestamp
          };
        }, 'toJSON', 0);
      }
      return pos;
    }
    setMethod(proto, 'getCurrentPosition',
      fixFn(function (ok, err, opts) {
        setTimeout(function () { if (ok) ok(position()); }, 60);
      }, proto.getCurrentPosition));
    // Watch bookkeeping (v0.208.0): every watch used to return
    // id 1 and clearWatch was a no-op, so two watchers shared
    // an id and a "cleared" watch still delivered. Ids are
    // unique now and clearing really cancels the pending
    // delivery. A stationary phone gets its one initial fix
    // per watch and then silence, which is what a real watch
    // on a motionless device looks like.
    var nextWatchId = 0;
    var watches = {};
    setMethod(proto, 'watchPosition',
      fixFn(function (ok, err, opts) {
        var id = ++nextWatchId;
        watches[id] = setTimeout(function () {
          if (watches[id] !== undefined && ok) ok(position());
        }, 60);
        return id;
      }, proto.watchPosition));
    setMethod(proto, 'clearWatch',
      fixFn(function (id) {
        if (watches[id] !== undefined) {
          clearTimeout(watches[id]);
          delete watches[id];
        }
      }, proto.clearWatch));
  });

  section(function () {
    function noiseData(img) {
      noiseBytes(img.data);
      return img;
    }
    var origGID = CanvasRenderingContext2D.prototype.getImageData;
    setMethod(CanvasRenderingContext2D.prototype, 'getImageData',
      fixFn(function () {
        return noiseData(origGID.apply(this, arguments));
      }, origGID));
    function noisedCopy(canvas) {
      var t = document.createElement('canvas');
      t.width = canvas.width; t.height = canvas.height;
      var cx = t.getContext('2d');
      cx.drawImage(canvas, 0, 0);
      cx.putImageData(cx.getImageData(0, 0, t.width, t.height), 0, 0);
      return t;
    }
    // Font metrics (v0.145.0): measureText reports the HOST
    // device's real font widths, identical across profiles — the
    // leak the v0.144.0 real-device report showed. Scale widths
    // by a fixed per-profile factor (seed-derived, in the
    // ±0.44% band real devices differ by) so the numbers move off
    // the raw values while staying stable for the profile. This
    // is a mitigation, not a font swap: the glyphs themselves are
    // still the host's, recorded in the checkpoint.
    if (FP_OPT_FONTS &&
      CanvasRenderingContext2D.prototype.measureText) {
      var scale = 1 + ((hash(SEED * 97 + 13) % 89) - 44) / 10000;
      var origMT = CanvasRenderingContext2D.prototype.measureText;
      setMethod(CanvasRenderingContext2D.prototype, 'measureText',
        fixFn(function () {
          var m = origMT.apply(this, arguments);
          try {
            var w = m.width * scale;
            return new Proxy(m, {
              get: function (t, prop) {
                return prop === 'width' ? w : t[prop];
              }
            });
          } catch (e) { return m; }
        }, origMT));
    }
    var origTDU = HTMLCanvasElement.prototype.toDataURL;
    setMethod(HTMLCanvasElement.prototype, 'toDataURL',
      fixFn(function () {
        try {
          return origTDU.apply(noisedCopy(this), arguments);
        } catch (e) {
          return origTDU.apply(this, arguments);
        }
      }, origTDU));
    if (HTMLCanvasElement.prototype.toBlob) {
      var origTB = HTMLCanvasElement.prototype.toBlob;
      setMethod(HTMLCanvasElement.prototype, 'toBlob',
        fixFn(function () {
          try {
            return origTB.apply(noisedCopy(this), arguments);
          } catch (e) {
            return origTB.apply(this, arguments);
          }
        }, origTB));
    }
    // OffscreenCanvas (v0.157.0): the offscreen 2D context is a
    // DIFFERENT prototype from CanvasRenderingContext2D, so none
    // of the hooks above reach it — a checker drawing offscreen
    // would read the host's raw canvas fingerprint and raw font
    // widths. Same noise, same measureText scale, and the export
    // path (convertToBlob) goes through a noised copy just like
    // toBlob. (Offscreen WebGL contexts share the regular WebGL
    // prototypes, so that half was already covered.)
    if (window.OffscreenCanvasRenderingContext2D) {
      var octx = OffscreenCanvasRenderingContext2D.prototype;
      if (octx.getImageData) {
        var origOGID = octx.getImageData;
        setMethod(octx, 'getImageData',
          fixFn(function () {
            return noiseData(origOGID.apply(this, arguments));
          }, origOGID));
      }
      if (FP_OPT_FONTS && octx.measureText) {
        var origOMT = octx.measureText;
        setMethod(octx, 'measureText',
          fixFn(function () {
            var m = origOMT.apply(this, arguments);
            try {
              var w = m.width * scale;
              return new Proxy(m, {
                get: function (t, prop) {
                  return prop === 'width' ? w : t[prop];
                }
              });
            } catch (e) { return m; }
          }, origOMT));
      }
    }
    if (window.OffscreenCanvas &&
      OffscreenCanvas.prototype.convertToBlob) {
      var origOCTB = OffscreenCanvas.prototype.convertToBlob;
      setMethod(OffscreenCanvas.prototype, 'convertToBlob',
        fixFn(function () {
          try {
            var t = new OffscreenCanvas(this.width, this.height);
            var cx = t.getContext('2d');
            cx.drawImage(this, 0, 0);
            cx.putImageData(
              cx.getImageData(0, 0, t.width, t.height), 0, 0);
            return origOCTB.apply(t, arguments);
          } catch (e) {
            return origOCTB.apply(this, arguments);
          }
        }, origOCTB));
    }
  });

  section(function () {
    if (!FP_OPT_FONTS) return;
    // Font list surface (v0.174.0): document.fonts.check() is
    // the enumeration probe — check('16px "SamsungOne"') asks
    // "is this family on this device?" without loading a thing.
    // It used to answer with the HOST's shelf: a Galaxy profile
    // on a host lacking SamsungOne said no, while the host
    // vendor's own UI font said yes. Known system families now
    // answer from the PROFILE device's stock set. Generic
    // families resolve on every device, and anything unknown —
    // above all a page's own webfonts, whose check() result
    // pages legitimately wait on — falls through to the real
    // check untouched. FontFaceSet.load() is deliberately NOT
    // wrapped: it has loading side effects pages depend on.
    if (!window.FontFaceSet || !document.fonts ||
        !document.fonts.check) return;
    var FP_FONT_SET = { $fpFonts };
    var KNOWN_FONT_SET = { $knownFonts };
    function familiesOf(font) {
      var out = [];
      var sawSyntax = false;
      String(font).split(',').forEach(function (part) {
        var p = part.trim();
        if (!p) return;
        var q = /"([^"]+)"|'([^']+)'/.exec(p);
        if (q) { sawSyntax = true; out.push(q[1] || q[2]); return; }
        var tokens = p.split(/\s+/);
        for (var i = 0; i < tokens.length; i++) {
          if (/^\d+(\.\d+)?(px|pt|pc|em|rem|ex|ch|vw|vh|%|cm|mm|in)(\/\S+)?$/i
                .test(tokens[i]) ||
              /^(xx-small|x-small|small|medium|large|x-large|xx-large|smaller|larger)$/i
                .test(tokens[i])) {
            sawSyntax = true;
            if (i + 1 < tokens.length) {
              out.push(tokens.slice(i + 1).join(' '));
            }
            return;
          }
        }
        out.push(p);
      });
      return { families: out, sawSyntax: sawSyntax };
    }
    var origFontCheck = document.fonts.check.bind(document.fonts);
    setMethodOn(document.fonts, 'check',
      fixFnAs(function (font, text) {
      try {
        var parsed = familiesOf(font);
        if (parsed.sawSyntax) {
          var sawKnown = false;
          var allInProfile = true;
          parsed.families.forEach(function (fam) {
            var key = fam.toLowerCase();
            if (KNOWN_FONT_SET[key]) {
              sawKnown = true;
              if (!FP_FONT_SET[key]) allInProfile = false;
            }
          });
          if (sawKnown) return allInProfile;
        }
      } catch (e) {}
      return origFontCheck(font, text);
      }, 'check', 1));
  });

  section(function () {
    if (!FP_OPT_WEBGL) return;
    // The profile GPU's capability table (v0.145.0): a renderer
    // string that disagrees with MAX_TEXTURE_SIZE & friends is a
    // cross-check failure, so the commonly probed parameters
    // answer from the profile too. Arrays are copied per call —
    // real drivers hand out a fresh typed array each time.
    // Keys are the DECIMAL WebGL enum values. Four of them
    // were wrong from this table's first version until the
    // v0.221.0 audit: cube-map size sat on 34076 instead of
    // 34067 (0x8513), and the uniform/varying trio sat on
    // 35801-35803 instead of 36347-36349 (0x8DFB-0x8DFD) —
    // none of which are the enums pages actually query, so
    // those four parameters silently fell through to the
    // HOST GPU's real answers (a persona reporting the host
    // chip's own varying-vector count instead of its own). The audit harness
    // caught it by giving the host different values and
    // watching them leak through; FpLieDetectionTest pins
    // the key set now.
    var GLP = {
      3379: ${d.webGl.maxTextureSize},
      34067: ${d.webGl.maxCubeMapTextureSize},
      34024: ${d.webGl.maxRenderbufferSize},
      3386: new Int32Array([${d.webGl.maxViewportDim}, ${d.webGl.maxViewportDim}]),
      33901: new Float32Array([1, ${d.webGl.aliasedPointSizeMax}]),
      33902: new Float32Array([1, ${d.webGl.aliasedLineWidthMax}]),
      34921: ${d.webGl.maxVertexAttribs},
      34930: ${d.webGl.maxTextureImageUnits},
      35660: ${d.webGl.maxVertexTextureImageUnits},
      35661: ${d.webGl.maxCombinedTextureImageUnits},
      36347: ${d.webGl.maxVertexUniformVectors},
      36348: ${d.webGl.maxVaryingVectors},
      36349: ${d.webGl.maxFragmentUniformVectors},
      34047: 16
    };
    [window.WebGLRenderingContext, window.WebGL2RenderingContext]
      .forEach(function (ctor) {
        if (!ctor || !ctor.prototype.getParameter) return;
        var orig = ctor.prototype.getParameter;
        setMethod(ctor.prototype, 'getParameter',
          fixFn(function (param) {
            if (param === 0x9245) return ${jsStr(d.webGlVendor)};
            if (param === 0x9246) return ${jsStr(d.webGlRenderer)};
            if (Object.prototype.hasOwnProperty.call(GLP, param)) {
              var v = GLP[param];
              return (v && v.slice) ? v.slice() : v;
            }
            return orig.apply(this, arguments);
          }, orig));
        // readPixels (v0.149.0): the canvas noise is pointless if
        // a checker can read the framebuffer back raw — readback
        // is the other half of every WebGL fingerprint. Same
        // sparse ±1 treatment, keyed by SEED + byte position, so
        // repeated reads of the same scene agree with each other
        // and with the canvas story. Only the plain Uint8Array
        // form is touched (the WebGL2 pixel-pack-offset form
        // passes a number, not an array). ±1 on roughly 1-in-500
        // bytes is invisible to pages rendering with readPixels;
        // it only moves the hash.
        if (ctor.prototype.readPixels) {
          var origRead = ctor.prototype.readPixels;
          setMethod(ctor.prototype, 'readPixels',
            fixFn(function () {
              var r = origRead.apply(this, arguments);
              try {
                var pixels = arguments[6];
                if (pixels && pixels instanceof Uint8Array) {
                  noiseBytes(pixels);
                }
              } catch (e) {}
              return r;
            }, origRead));
        }
        // Shader precision (v0.181.0): pin ONLY the entries
        // public data agrees on for BOTH GPU families in the
        // profile library — vertex float {127,127,23} and int
        // {31,30,0} at every level; fragment HIGH float/int
        // the same; fragment medium/low float precision = 10
        // (fp16) with the range left as the host's own.
        // Fragment medium/low INT genuinely differs between
        // GPU families and no per-family values could be
        // verified from public sources, so those pass through
        // untouched rather than being pinned to an invented
        // number. Pinned answers are built on the real
        // object's prototype so instanceof/shape checks hold.
        var origSPF = ctor.prototype.getShaderPrecisionFormat;
        if (origSPF) {
          setMethod(ctor.prototype, 'getShaderPrecisionFormat',
            fixFn(function (shaderType, precisionType) {
              var real = origSPF.apply(this, arguments);
              var pin = null;
              var isFloat = precisionType >= 0x8DF0 &&
                precisionType <= 0x8DF2;
              var isHigh = precisionType === 0x8DF2 ||
                precisionType === 0x8DF5;
              if (shaderType === 0x8B31) {
                pin = isFloat
                  ? { rangeMin: 127, rangeMax: 127, precision: 23 }
                  : { rangeMin: 31, rangeMax: 30, precision: 0 };
              } else if (shaderType === 0x8B30 && isHigh) {
                pin = isFloat
                  ? { rangeMin: 127, rangeMax: 127, precision: 23 }
                  : { rangeMin: 31, rangeMax: 30, precision: 0 };
              } else if (shaderType === 0x8B30 && isFloat) {
                pin = { rangeMin: real.rangeMin,
                  rangeMax: real.rangeMax, precision: 10 };
              }
              if (!pin) return real;
              // def() getters, not assignments (v0.220.0):
              // the real object's prototype carries READONLY
              // WebIDL attributes, so plain assignment in this
              // strict-mode payload THREW on every pinned
              // answer — the wrapper itself crashed on the
              // exact probe it exists to answer. defineProperty
              // shadows the prototype cleanly and leaves no
              // enumerable own keys.
              var fmt = Object.create(
                Object.getPrototypeOf(real) || Object.prototype);
              def(fmt, 'rangeMin', pin.rangeMin);
              def(fmt, 'rangeMax', pin.rangeMax);
              def(fmt, 'precision', pin.precision);
              return fmt;
            }, origSPF));
        }
      });
  });

  section(function () {
    if (!FP_OPT_WEBGL) return;
    // WebGPU (v0.159.0): navigator.gpu was the one graphics API
    // still answering with the HOST GPU. A page asking
    // requestAdapter() gets an adapter whose .info names the
    // real chip family — Qualcomm on a host wearing a Mali
    // profile (or the reverse) is a flat contradiction with
    // the WebGL renderer string one probe earlier. Chrome only
    // exposes vendor/architecture (plus subgroup sizes and the
    // fallback flag) on GPUAdapterInfo without developer
    // features, so those two fields answer from the profile;
    // everything else on the info object — and everything else
    // on the adapter — passes through to the real object, with
    // methods bound to it so requestDevice & friends keep
    // working. The legacy requestAdapterInfo() is wrapped the
    // same way when the engine still has it.
    if (!navigator.gpu || !navigator.gpu.requestAdapter) return;
    var gpuObj = navigator.gpu;
    var origRequestAdapter = gpuObj.requestAdapter.bind(gpuObj);
    function wrapGpuInfo(info) {
      if (!info) return info;
      return new Proxy(info, {
        get: function (t, prop) {
          if (prop === 'vendor') return ${jsStr(d.webGpuVendor)};
          if (prop === 'architecture') {
            return ${jsStr(d.webGpuArchitecture)};
          }
          var v = t[prop];
          return (typeof v === 'function') ? v.bind(t) : v;
        }
      });
    }
    function wrapGpuAdapter(adapter) {
      if (!adapter) return adapter;
      return new Proxy(adapter, {
        get: function (t, prop) {
          if (prop === 'info') {
            try { return wrapGpuInfo(t.info); } catch (e) {
              return t.info;
            }
          }
          if (prop === 'requestAdapterInfo' &&
              typeof t[prop] === 'function') {
            return function () {
              return t.requestAdapterInfo.apply(t, arguments)
                .then(wrapGpuInfo);
            };
          }
          var v = t[prop];
          return (typeof v === 'function') ? v.bind(t) : v;
        }
      });
    }
    setMethod(gpuObj, 'requestAdapter',
      fixFnAs(function () {
        return origRequestAdapter.apply(null, arguments)
          .then(wrapGpuAdapter);
      }, 'requestAdapter', 0));
  });

  section(function () {
    // Speech-synthesis voices (v0.145.0): the list comes from
    // the device TTS engine, so it must name the PROFILE region's
    // engine, not the host's. Voice objects are built on the real
    // SpeechSynthesisVoice prototype so instanceof checks hold.
    // The interface guard changed in v0.224.0: a real-device
    // CreepJS report showed the whole Speech block empty, and
    // the section's own guard was the suspect — on a WebView
    // build that does not expose the SpeechSynthesisVoice
    // interface the section died silently and pages read the
    // engine's own (still-empty) list inside CreepJS's 300 ms
    // patience, reported as blocked. The geolocation section
    // learned this shape in v0.208.0: build on the real
    // prototype when it exists, on Object.prototype when it
    // does not — a page cannot instanceof against an interface
    // its own engine never exposed either.
    if (!FP_OPT_LANGUAGE) return;
    if (!window.speechSynthesis) return;
    var defs = [$voices];
    var list = defs.map(function (d) {
      var v = Object.create(window.SpeechSynthesisVoice
        ? SpeechSynthesisVoice.prototype : Object.prototype);
      def(v, 'name', d.name);
      def(v, 'voiceURI', d.name);
      def(v, 'lang', d.lang);
      def(v, 'localService', true);
      def(v, 'default', d.isDefault);
      return v;
    });
    var origGetVoices = speechSynthesis.getVoices;
    // Delivery form (v0.222.0): Chrome does not hand the
    // catalogue to the first synchronous call — the list is
    // owned by the browser process, a cold page's first
    // getVoices() returns [], and the voiceschanged event
    // is what delivers it. Serving the full list from the
    // first instant was a shape no real Chrome cold page
    // produces, so the list stays gated until the same
    // timer that fires the event flips it ready; a handler
    // calling getVoices() inside voiceschanged already
    // sees the list, exactly like the real delivery.
    var voicesReady = false;
    setMethodOn(speechSynthesis, 'getVoices',
      fixFn(function () {
        return voicesReady ? list.slice() : [];
      }, origGetVoices));
    setTimeout(function () {
      voicesReady = true;
      try {
        var evt = new Event('voiceschanged');
        speechSynthesis.dispatchEvent(evt);
        if (typeof speechSynthesis.onvoiceschanged === 'function') {
          speechSynthesis.onvoiceschanged(evt);
        }
      } catch (e) {}
    }, 50);
  });

  section(function () {
    // Panel story (v0.145.0): color-gamut and dynamic-range
    // media queries answer for the PROFILE device's screen.
    // Only whole single-feature queries are intercepted — the
    // shape detectors probe with; compound queries fall through
    // to the real matchMedia untouched.
    if (!FP_OPT_SCREEN) return;
    if (!window.matchMedia) return;
    var GAMUT = ${jsStr(p.colorGamut)};
    var HDR = ${p.dynamicRangeHigh};
    var DPR = ${d.devicePixelRatio};
    var order = { srgb: 0, p3: 1, rec2020: 2 };
    var origMM = window.matchMedia.bind(window);
    // The fake MediaQueryList is built on the real
    // prototype (v0.220.0): a plain literal failed
    // instanceof and Object.prototype.toString and carried
    // eight enumerable keys where a native MQL carries
    // none — on the very queries (color-gamut) fingerprint
    // probes ask first. Attributes ride def() getters, the
    // onchange pair has a backing store, and the listener
    // methods are non-enumerable stubs (the answers are
    // static per profile, so no change event ever fires,
    // exactly like a native gamut query on a fixed panel).
    function fakeMQL(query, matches) {
      var mql = window.MediaQueryList
        ? Object.create(MediaQueryList.prototype) : {};
      def(mql, 'matches', matches);
      def(mql, 'media', String(query));
      defHandler(mql, 'onchange');
      stubMethod(mql, 'addListener', 1, function () {});
      stubMethod(mql, 'removeListener', 1, function () {});
      stubMethod(mql, 'addEventListener', 2, function () {});
      stubMethod(mql, 'removeEventListener', 2, function () {});
      stubMethod(mql, 'dispatchEvent', 1,
        function () { return true; });
      return mql;
    }
    var fpMatchMedia = fixFnAs(function (query) {
        var q = String(query).replace(/\s+/g, '');
        var g = /^\(color-gamut:(srgb|p3|rec2020)\)$/.exec(q);
        if (g) return fakeMQL(query, order[g[1]] <= order[GAMUT]);
        var d = /^\(dynamic-range:(standard|high)\)$/.exec(q);
        if (d) return fakeMQL(query, d[1] === 'high' ? HDR : !HDR);
        // Stock-preference queries (v0.208.0): these two used
        // to fall through to the HOST OS — a host with
        // reduce-motion switched on in its accessibility
        // settings answered "reduce" for a persona that is a
        // stock device off the shelf. The stock answers are
        // no-preference and forced-colors: none.
        var rm = /^\(prefers-reduced-motion:(reduce|no-preference)\)$/
          .exec(q);
        if (rm) return fakeMQL(query, rm[1] === 'no-preference');
        var fc = /^\(forced-colors:(active|none)\)$/.exec(q);
        if (fc) return fakeMQL(query, fc[1] === 'none');
        // Resolution (v0.221.0): devicePixelRatio is pinned
        // to the profile, but resolution queries fell through
        // to the host's real ratio — two JS answers for one
        // fact, contradicting each other inside the JS
        // surface alone. Exact single-value dppx queries now
        // answer from the profile ratio, like the panel
        // queries above. Stylesheet @media evaluation is the
        // engine's own and still answers for the host panel
        // (no JS surface reaches it) — that boundary is
        // structural, recorded in the checkpoint, and the
        // self-check's media-agreement row reports it live
        // instead of hiding it.
        var res = /^\(resolution:([0-9]*\.?[0-9]+)dppx\)$/
          .exec(q);
        if (res) return fakeMQL(query,
          Math.abs(parseFloat(res[1]) - DPR) < 0.001);
        return origMM(query);
      }, 'matchMedia', 1);
    // matchMedia is a Window-prototype method (v0.220.0):
    // installing on the window object left an own property
    // where the native is inherited. Install on the
    // prototype and read back; fall back to the window
    // install if the prototype refuses.
    (function () {
      try {
        var wp = Object.getPrototypeOf(window);
        if (wp) {
          Object.defineProperty(wp, 'matchMedia', {
            value: fpMatchMedia, writable: true,
            enumerable: false, configurable: true
          });
          if (window.matchMedia === fpMatchMedia) return;
        }
      } catch (e) {}
      setMethod(window, 'matchMedia', fpMatchMedia);
    })();
  });

  section(function () {
    // Network story (v0.145.0): a healthy 4g link in the
    // profile's region, on the real NetworkInformation
    // prototype, instead of whatever radio the host is on.
    if (!FP_OPT_CONNECTION) return;
    var proto = window.NetworkInformation
      ? NetworkInformation.prototype : null;
    var conn = proto ? Object.create(proto) : {};
    def(conn, 'effectiveType', '4g');
    def(conn, 'downlink', ${p.connectionDownlink});
    def(conn, 'rtt', ${p.connectionRtt});
    def(conn, 'saveData', false);
    def(conn, 'type', 'wifi');
    // downlinkMax (v0.210.0): the one field of the story
    // left unpinned — read on this prototype-built object
    // it falls to the native getter, which has no real
    // instance behind it and answers an exception or
    // undefined where real Chrome always answers a number.
    // For the claimed wifi / 4g story that number is
    // Infinity (the spec's 4g row caps downlink at ∞, and
    // Chromium reports Infinity as the max for wifi).
    def(conn, 'downlinkMax', Infinity);
    // The accessor itself moves to Navigator.prototype
    // (v0.220.0): the native connection attribute lives
    // there, and def() on the existing prototype attribute
    // keeps its native enumerable flag through the
    // defineProperty merge. Pinning it on the instance left
    // an own accessor a descriptor sweep could find.
    var navT = window.Navigator ? Navigator.prototype
      : navigator;
    def(navT, 'connection', conn);
  });

  section(function () {
    // Media devices (v0.145.0): without a permission grant real
    // Chrome reports empty labels, so the profile does the same
    // — the important part is that no HOST device name can leak
    // through a granted page either. One mic, one camera, one
    // speaker, generic ids, on the real MediaDeviceInfo
    // prototype.
    if (!navigator.mediaDevices ||
        !navigator.mediaDevices.enumerateDevices) return;
    function device(kind, id, group) {
      var d = window.MediaDeviceInfo
        ? Object.create(MediaDeviceInfo.prototype) : {};
      def(d, 'deviceId', id);
      def(d, 'kind', kind);
      def(d, 'label', '');
      def(d, 'groupId', group);
      // Non-enumerable method slot (v0.220.0): plain
      // assignment left 'toJSON' in Object.keys(device)
      // where a native device carries no own keys at all.
      stubMethod(d, 'toJSON', 0, function () {
        return { deviceId: id, kind: kind, label: '', groupId: group };
      });
      return d;
    }
    var origEnumDevices = navigator.mediaDevices.enumerateDevices;
    setMethodOn(navigator.mediaDevices, 'enumerateDevices',
      fixFn(function () {
        return Promise.resolve([
          device('audioinput', 'default', 'fp-audio'),
          device('videoinput', '', 'fp-video'),
          device('audiooutput', 'default', 'fp-audio')
        ]);
      }, origEnumDevices));
  });

  section(function () {
    // Gamepads (v0.210.0): the Gamepad API exists in
    // WebView exactly as in Chrome for Android, so removing
    // it is not an option — but a controller paired to the
    // HOST would otherwise answer getGamepads() with the
    // real accessory's id string ("Xbox Wireless
    // Controller", a DualSense, …), an identity the stock
    // persona does not have. The persona's answer is the
    // one real Chrome gives on any phone with no controller
    // in play: four empty slots, a fresh array per call.
    // Recorded residual: gamepadconnected /
    // gamepaddisconnected listeners still register
    // natively, so a controller paired AND used mid-session
    // can fire an event the poll then contradicts — that
    // needs a second addEventListener wrapper swallowing
    // into a dead registry, more cross-examination surface
    // than the narrow case is worth.
    if (typeof navigator.getGamepads !== 'function') return;
    var nav = window.Navigator ? Navigator.prototype : navigator;
    setMethod(nav, 'getGamepads',
      fixFnAs(function () {
        return [null, null, null, null];
      }, 'getGamepads', 0));
  });

  // AudioContext static parameters (v0.181.0): the section
  // after this one shapes sample VALUES; this one aligns the
  // static story. sampleRate: the profile devices' platform
  // rate is 48000, so when a page constructs a context
  // WITHOUT asking for a rate we pass 48000 explicitly — the
  // context really runs at the profile rate, nothing is
  // faked; an explicit ask is honoured as asked.
  // destination.maxChannelCount: Android Chrome reports 2,
  // pinned per constructed context. baseLatency /
  // outputLatency are deliberately NOT pinned: on a real
  // device they follow the audio route (speaker, Bluetooth,
  // USB), so any fixed value would contradict the route the
  // user actually hears.
  section(function () {
    if (!FP_OPT_AUDIO) return;
    var OrigAC = window.AudioContext ||
      window.webkitAudioContext;
    if (!OrigAC) return;
    function FpAudioContext(options) {
      var opts = (options && typeof options === 'object')
        ? options : {};
      if (opts.sampleRate === undefined) {
        opts = Object.assign({}, opts, { sampleRate: 48000 });
      }
      var ctx = Reflect.construct(OrigAC, [opts],
        FpAudioContext);
      try {
        Object.defineProperty(ctx.destination,
          'maxChannelCount', {
            value: 2, writable: false, enumerable: true,
            configurable: true
          });
      } catch (e) {}
      return ctx;
    }
    FpAudioContext.prototype = OrigAC.prototype;
    fixFn(FpAudioContext, OrigAC);
    function installAC(name) {
      try {
        Object.defineProperty(window, name, {
          value: FpAudioContext, writable: true,
          enumerable: false, configurable: true
        });
      } catch (e) {
        def(window, name, FpAudioContext);
      }
    }
    installAC('AudioContext');
    if (window.webkitAudioContext &&
        window.webkitAudioContext === OrigAC) {
      try {
        Object.defineProperty(window, 'webkitAudioContext', {
          value: FpAudioContext, writable: true,
          enumerable: false, configurable: true
        });
      } catch (e) {}
    }
  });

  section(function () {
    if (!FP_OPT_AUDIO) return;
    if (!window.AudioBuffer || !AudioBuffer.prototype.getChannelData) return;
    var orig = AudioBuffer.prototype.getChannelData;
    // Noise ONCE per (buffer, channel) (v0.152.0): getChannelData
    // hands out the LIVE buffer, so the old apply-on-every-call
    // shape stacked another ±1e-7 on every read — a page reading
    // the same buffer repeatedly would watch the values drift,
    // which is itself a tell. A WeakMap of noised channels keeps
    // the first read's story final.
    var noised = new WeakMap();
    function noiseAudioChannel(buf, channel) {
      var data = orig.call(buf, channel);
      try {
        var seen = noised.get(buf);
        if (!seen) { seen = {}; noised.set(buf, seen); }
        if (seen[channel]) return data;
        seen[channel] = true;
        for (var i = 0; i < data.length; i += 997) {
          // Exact zeros stay zero (v0.221.0): a silent render —
          // an oscillator at frequency 0, a compressor's
          // lookahead prefix — is EXACTLY zero on a real engine,
          // and lie detectors render silence precisely to catch
          // injectors that perturb it. Skipping blanks costs
          // the story nothing on real signals.
          if (data[i] === 0) continue;
          // Content-keyed like the canvas rule (v0.220.0):
          // the hash mixes a coarse grid of the sample's own
          // value, and it decides BOTH whether this stride
          // point is touched and the delta's sign — so two
          // DIFFERENT signals share neither a deviation index
          // lattice nor a sign pattern a calibration render
          // could map and re-verify. Identical renders still
          // draw identical treatment (and the WeakMap above
          // pins the first read regardless).
          var h = hash(SEED * 7919 + i + channel * 104729 +
            Math.round(data[i] * 32768));
          if ((h >>> 16) % 3 === 0) continue;
          data[i] = data[i] + ((h % 2 === 0) ? 1e-7 : -1e-7);
        }
      } catch (e) {}
      return data;
    }
    setMethod(AudioBuffer.prototype, 'getChannelData',
      fixFn(function (channel) {
        return noiseAudioChannel(this, channel);
      }, orig));
    // copyFromChannel is the second door to the same samples
    // (v0.221.0): it reads the buffer's store WITHOUT going
    // through getChannelData, so a checker copying first and
    // reading second (or the reverse) watched the two doors
    // disagree at every touched stride point. Run the same
    // once-only noise before the copy, so both doors tell the
    // one story whichever opens first.
    if (AudioBuffer.prototype.copyFromChannel) {
      var origCopy = AudioBuffer.prototype.copyFromChannel;
      setMethod(AudioBuffer.prototype, 'copyFromChannel',
        fixFn(function (destination, channelNumber) {
          try {
            noiseAudioChannel(this, channelNumber);
          } catch (e) {}
          return origCopy.apply(this, arguments);
        }, origCopy));
    }
  });

  section(function () {
    function jitter(rect) {
      try {
        var h = hash(SEED * 31 +
          Math.round(rect.x * 10) + Math.round(rect.y * 10) * 131 +
          Math.round(rect.width * 10) * 137 +
          Math.round(rect.height * 10) * 139);
        var jx = ((h % 100) / 1000) - 0.05;
        var jy = (((h >>> 8) % 100) / 1000) - 0.05;
        return new DOMRect(rect.x + jx, rect.y + jy,
          rect.width, rect.height);
      } catch (e) { return rect; }
    }
    if (window.Element) {
      var origEl = Element.prototype.getBoundingClientRect;
      setMethod(Element.prototype, 'getBoundingClientRect',
        fixFn(function () {
          return jitter(origEl.call(this));
        }, origEl));
    }
    if (window.Range) {
      var origRg = Range.prototype.getBoundingClientRect;
      setMethod(Range.prototype, 'getBoundingClientRect',
        fixFn(function () {
          return jitter(origRg.call(this));
        }, origRg));
    }
  });

  section(function () {
    if (!FP_OPT_BATTERY) return;
    if (!('getBattery' in navigator)) return;
    // One manager per page (v0.220.0): the spec's promise
    // resolves to THE battery manager, so two calls must
    // hand back the identical object — a fresh object per
    // call failed an === check any page can run.
    var cachedBattery = null;
    setMethodOn(navigator, 'getBattery',
      fixFnAs(function () {
      if (cachedBattery) return Promise.resolve(cachedBattery);
      // The story must agree with itself (v0.158.0): chargingTime
      // is "seconds until full", so 0 is only true at level 1 —
      // a charging profile at 0.91 claiming 0 contradicts itself.
      // Derive it from the level instead ((1 - level) hours).
      // The object is built on the real BatteryManager prototype
      // when the engine exposes one, like the voices/devices
      // objects, so instanceof does not give it away.
      // Attributes ride def() getters (v0.220.0): plain
      // assignment THREW here — the prototype's WebIDL
      // attributes are read-only, and this payload is strict
      // mode, so assigning a level or a charging flag raised
      // TypeError and getBattery() itself crashed on call.
      // defineProperty shadows cleanly, the on* handlers get
      // real get/set pairs, and the listener methods are
      // non-enumerable stubs, so the object carries no own
      // enumerable keys.
      var b = Object.create(
        window.BatteryManager ? BatteryManager.prototype
          : Object.prototype);
      // Slow drift (v0.225.0): a level pinned to one number
      // forever is its own tell — a page reading getBattery
      // twice, hours apart, must not see the identical value,
      // yet the drift has to stay a pure function of (seed,
      // hour bucket): every read inside one bucket, on any
      // page, returns the identical level (the manager above
      // is a per-page singleton on top of that), and the step
      // between neighbouring buckets stays within a few points
      // (the three-bucket average bounds the raw drift to two
      // points; percent rounding can add one more in the worst
      // corner — the usual step is a single point). The story
      // around the profile's base level: ±3 points while
      // discharging; while charging, the level never reads
      // BELOW the base — the charge only ever adds — and
      // chargingTime derives from the drifted level, so the
      // pair can never contradict itself. Date.now here is
      // epoch time (FpDate delegates it to the real clock),
      // so the bucket is the true hour, not a zone artefact.
      var battBucket = Math.floor(Date.now() / 3600000);
      function battHash01(bucket) {
        return (hash(SEED ^ (bucket * 2654435761)) % 10000) /
          10000;
      }
      var battSmooth = (battHash01(battBucket - 1) +
        battHash01(battBucket) + battHash01(battBucket + 1)) / 3;
      var battBase = ${p.batteryLevel};
      var battLevel = ${p.batteryCharging}
        ? Math.min(1,
            Math.round((battBase + 0.03 * battSmooth) * 100) /
              100)
        : Math.max(0.05, Math.min(1,
            Math.round(
              (battBase + 0.03 * (2 * battSmooth - 1)) * 100) /
              100));
      def(b, 'charging', ${p.batteryCharging});
      def(b, 'chargingTime', ${p.batteryCharging}
        ? Math.round((1 - battLevel) * 3600) : Infinity);
      def(b, 'dischargingTime', Infinity);
      def(b, 'level', battLevel);
      defHandler(b, 'onchargingchange');
      defHandler(b, 'onchargingtimechange');
      defHandler(b, 'ondischargingtimechange');
      defHandler(b, 'onlevelchange');
      stubMethod(b, 'addEventListener', 2, function () {});
      stubMethod(b, 'removeEventListener', 2, function () {});
      stubMethod(b, 'dispatchEvent', 1,
        function () { return true; });
      cachedBattery = b;
      return Promise.resolve(b);
      }, 'getBattery', 0));
  });

  section(function () {
    if (!FP_OPT_WEBRTC) return;
    // WebRTC (v0.160.0): until now the mode deleted
    // RTCPeerConnection outright — but a real Chrome phone HAS
    // the API, so its absence is itself a tell, at odds with
    // the ordinary-device story every other section tells.
    // The API now stays, fully real, with the leak paths
    // closed instead:
    //  - every connection is forced to iceTransportPolicy
    //    'relay', so the engine only ever gathers TURN relay
    //    candidates; host and srflx candidates — the ones
    //    carrying the device's LAN address and the exit's
    //    public address — are never produced at all;
    //  - any candidate event that still surfaces is filtered
    //    on both delivery paths (the onicecandidate property,
    //    forwarded through the real prototype setter, and
    //    addEventListener): host/srflx/prflx candidates are
    //    dropped, so even an engine that ignored the policy
    //    cannot leak through the event stream.
    // getUserMedia likewise stays present but answers the way
    // an ungranted page experiences a real device: a
    // NotAllowedError rejection. (Fingerprint mode never
    // grants camera or microphone — v0.144.0 policy,
    // unchanged; only the SHAPE of the refusal changed.)
    var OrigRTC = window.RTCPeerConnection;
    if (OrigRTC) {
      function candidateLeaks(candidate) {
        if (!candidate) return false;
        return /typ (host|srflx|prflx)\b/
          .test(candidate.candidate || '');
      }
      function FpRTCPeerConnection(config) {
        var merged = {};
        if (config) {
          for (var k in config) merged[k] = config[k];
        }
        merged.iceTransportPolicy = 'relay';
        var pc = new OrigRTC(merged);
        var origAdd = pc.addEventListener.bind(pc);
        var fpAddEventListener = function (type, listener, opts) {
          if (type === 'icecandidate' &&
              typeof listener === 'function') {
            var wrappedListener = function (ev) {
              if (ev && candidateLeaks(ev.candidate)) return;
              return listener.apply(this, arguments);
            };
            return origAdd(type, wrappedListener, opts);
          }
          return origAdd(type, listener, opts);
        };
        fixFnAs(fpAddEventListener, 'addEventListener', 2);
        // Non-enumerable own slot (v0.220.0): plain
        // assignment put 'addEventListener' into
        // Object.keys(pc) where a native connection object
        // has no own enumerable keys at all.
        Object.defineProperty(pc, 'addEventListener', {
          value: fpAddEventListener, writable: true,
          enumerable: false, configurable: true
        });
        var iceDesc = Object.getOwnPropertyDescriptor(
          Object.getPrototypeOf(pc), 'onicecandidate');
        if (iceDesc && iceDesc.set) {
          var userHandler = null;
          Object.defineProperty(pc, 'onicecandidate', {
            configurable: true, enumerable: false,
            get: function () { return userHandler; },
            set: function (fn) {
              userHandler = fn;
              iceDesc.set.call(pc,
                (typeof fn === 'function')
                  ? function (ev) {
                      if (ev && candidateLeaks(ev.candidate)) {
                        return;
                      }
                      return fn.apply(this, arguments);
                    }
                  : fn);
            }
          });
        }
        return pc;
      }
      FpRTCPeerConnection.prototype = OrigRTC.prototype;
      if (OrigRTC.generateCertificate) {
        // Named wrapper, not .bind (v0.220.0): the bound
        // copy reported name 'bound generateCertificate'
        // and an anonymous native-code string. Installed
        // non-enumerable like the native static.
        Object.defineProperty(FpRTCPeerConnection,
          'generateCertificate', {
            value: fixFnAs(function () {
              return OrigRTC.generateCertificate
                .apply(OrigRTC, arguments);
            }, 'generateCertificate', 1),
            writable: true, enumerable: false,
            configurable: true
          });
      }
      fixFn(FpRTCPeerConnection, OrigRTC);
      // Native constructors sit on the global as writable,
      // non-enumerable DATA properties; def() would leave a
      // getter-only accessor instead, so install as data,
      // with def() as the fallback.
      function installRTC(name) {
        try {
          Object.defineProperty(window, name, {
            value: FpRTCPeerConnection, writable: true,
            enumerable: false, configurable: true
          });
        } catch (e) {
          def(window, name, FpRTCPeerConnection);
        }
      }
      installRTC('RTCPeerConnection');
      if (window.webkitRTCPeerConnection) {
        installRTC('webkitRTCPeerConnection');
      }
    }
    if (navigator.mediaDevices) {
      // A real prototype METHOD (v0.220.0): this used to be
      // a def() accessor on the mediaDevices instance
      // returning a length-0 function — the native is a
      // writable data property on MediaDevices.prototype
      // with length 1, and a descriptor read on either the
      // instance or the prototype could tell. fixFn copies
      // the native name and length from the original.
      var origGUM = navigator.mediaDevices.getUserMedia;
      if (origGUM) {
        setMethodOn(navigator.mediaDevices, 'getUserMedia',
          fixFn(function () {
            return Promise.reject(new DOMException(
              'Permission denied', 'NotAllowedError'));
          }, origGUM));
      }
    }
  });

  section(function () {
    if (!FP_OPT_SENSORS) return;
    // Sensors (v0.190.0): a real phone HAS motion sensors and
    // Chrome fires deviceorientation / devicemotion events;
    // the host's real stream would report the host's own
    // motion, and a phone lying untouched reports a
    // suspiciously perfect zero. The profile instead presents
    // a quietly held phone — a seeded, slowly wobbling
    // gravity story (tilted as if in a hand) — and page
    // listeners for the two event types are served from this
    // stream instead of the host's. Recorded limits:
    // synthetic events carry isTrusted=false like every
    // script-made event, so a checker that insists on trusted
    // events can still tell; the alternative (no events at
    // all) reads as a sensor-less device, which a phone
    // profile is not. The gravity vector's magnitude stays at
    // 9.81 and its components follow the same beta/gamma the
    // orientation events report.
    // v0.210.0: the THIRD orientation type joins —
    // deviceorientationabsolute was never claimed by the
    // interception, so its listeners registered natively
    // and received the HOST's live compass stream while the
    // other two types told the held-phone story (a classic
    // half-covered face). Since Chrome 50 the plain
    // deviceorientation event is RELATIVE (absolute=false —
    // the magnetometer left that event), and the absolute
    // story lives on this separate type, whose events
    // guarantee absolute=true; the stream below serves it
    // with the same alpha/beta/gamma and absolute=true.
    if (!window.DeviceOrientationEvent &&
      !window.DeviceMotionEvent) return;
    var listeners = {
      deviceorientation: [], devicemotion: [],
      deviceorientationabsolute: []
    };
    var propHandlers = {
      deviceorientation: null, devicemotion: null,
      deviceorientationabsolute: null
    };
    var origAdd = window.addEventListener;
    var origRemove = window.removeEventListener;
    function isSensorType(t) {
      return t === 'deviceorientation' ||
        t === 'devicemotion' ||
        t === 'deviceorientationabsolute';
    }
    var fpWinAdd = fixFn(function (t, fn) {
        if (isSensorType(t) && typeof fn === 'function') {
          if (listeners[t].indexOf(fn) < 0) listeners[t].push(fn);
          return;
        }
        return origAdd.apply(window, arguments);
      }, window.addEventListener);
    var fpWinRemove = fixFn(function (t, fn) {
        if (isSensorType(t)) {
          var i = listeners[t].indexOf(fn);
          if (i >= 0) listeners[t].splice(i, 1);
          return;
        }
        return origRemove.apply(window, arguments);
      }, window.removeEventListener);
    // Window-prototype placement (v0.220.0): addEventListener
    // & friends are EventTarget-prototype methods the window
    // INHERITS, and the on* handler attributes are Window-
    // prototype accessors — installing either on the window
    // object itself left own properties where natives have
    // none. Install on the prototype with readback, and fall
    // back to the window only if the prototype refuses.
    var winProto = null;
    try { winProto = Object.getPrototypeOf(window); }
    catch (e) {}
    function onWinProto(name, descriptor) {
      if (!winProto) return false;
      try {
        Object.defineProperty(winProto, name, descriptor);
        return !!Object.getOwnPropertyDescriptor(
          winProto, name);
      } catch (e) { return false; }
    }
    if (!onWinProto('addEventListener', {
      value: fpWinAdd, writable: true,
      enumerable: false, configurable: true
    }) || window.addEventListener !== fpWinAdd) {
      setMethod(window, 'addEventListener', fpWinAdd);
    }
    if (!onWinProto('removeEventListener', {
      value: fpWinRemove, writable: true,
      enumerable: false, configurable: true
    }) || window.removeEventListener !== fpWinRemove) {
      setMethod(window, 'removeEventListener', fpWinRemove);
    }
    ['deviceorientation', 'devicemotion',
      'deviceorientationabsolute'].forEach(function (t) {
      var descriptor = {
        get: function () { return propHandlers[t]; },
        set: function (fn) {
          propHandlers[t] =
            (typeof fn === 'function') ? fn : null;
        },
        configurable: true, enumerable: true
      };
      if (!onWinProto('on' + t, descriptor)) {
        try {
          Object.defineProperty(window, 'on' + t, descriptor);
        } catch (e) {}
      }
    });
    var betaBase = 62 + (hash(SEED) % 10);
    var gammaBase = (hash(SEED * 3) % 7) - 3;
    var alphaBase = hash(SEED * 7) % 360;
    var tick = 0;
    function dispatch(type, ev) {
      var list = listeners[type].slice();
      for (var i = 0; i < list.length; i++) {
        try { list[i].call(window, ev); } catch (e) {}
      }
      var ph = propHandlers[type];
      if (ph) { try { ph.call(window, ev); } catch (e) {} }
    }
    // Cadence (v0.222.0): the stream ticks at Chromium's
    // own nominal sensor rate — kInertialSensorInterval
    // Millis = 50 in the Chromium source (the browser-side
    // minimal interval between sensor data updates, with
    // the renderer pump delay matched to it). The stream
    // used to tick at 100 ms, twice the engine's nominal
    // rate, and the devicemotion interval field said the
    // same 100; both now say 50. The wobble divisors and
    // the compass drift step are halved with the tick so
    // the motion story's wall-clock shape is unchanged —
    // only the cadence moved. Recorded residual: the tick
    // is perfectly periodic and the drift perfectly
    // linear, where a real sensor stream jitters; no
    // public dataset describes that jitter's distribution,
    // and inventing one would be fabrication, so the
    // periodicity stays as the known limit of this face.
    setInterval(function () {
      tick++;
      var beta = betaBase + Math.sin(tick / 18) * 0.6;
      var gamma = gammaBase + Math.cos(tick / 26) * 0.5;
      if (window.DeviceOrientationEvent) {
        var alphaNow = (alphaBase + tick * 0.01) % 360;
        dispatch('deviceorientation',
          new DeviceOrientationEvent('deviceorientation', {
            alpha: alphaNow,
            beta: beta, gamma: gamma, absolute: false
          }));
        // The absolute twin (v0.210.0): same held phone,
        // same angles — only the reference frame differs,
        // exactly as Chrome serves the pair on Android.
        dispatch('deviceorientationabsolute',
          new DeviceOrientationEvent(
            'deviceorientationabsolute', {
              alpha: alphaNow,
              beta: beta, gamma: gamma, absolute: true
            }));
      }
      if (window.DeviceMotionEvent) {
        var br = beta * Math.PI / 180;
        var gr = gamma * Math.PI / 180;
        dispatch('devicemotion',
          new DeviceMotionEvent('devicemotion', {
            acceleration: { x: 0, y: 0, z: 0 },
            accelerationIncludingGravity: {
              x: -Math.sin(gr) * Math.cos(br) * 9.81,
              y: Math.sin(br) * 9.81,
              z: Math.cos(br) * Math.cos(gr) * 9.81
            },
            rotationRate: { alpha: 0, beta: 0, gamma: 0 },
            interval: 50
          }));
      }
    }, 50);
  });

  section(function () {
    if (!FP_OPT_CHROME) return;
    // window.chrome (v0.190.0): real Chrome carries a
    // window.chrome object (app / loadTimes / csi); the
    // WebView ships none, so its ABSENCE marked every profile
    // page as not-Chrome. The object follows Chrome's public
    // shape, and the timing answers are computed from THIS
    // page's own performance.timing, so they agree with what
    // the page itself can measure — no canned constants for
    // another read to contradict.
    try {
      if (window.chrome && window.chrome.loadTimes) return;
      var timing =
        (window.performance && performance.timing) || {};
      var navStart = timing.navigationStart || Date.now();
      function secs(ms) { return ms ? ms / 1000 : 0; }
      function loadTimes() {
        return {
          requestTime: secs(navStart),
          startLoadTime: secs(navStart),
          commitLoadTime: secs(timing.responseStart || navStart),
          finishDocumentLoadTime:
            secs(timing.domContentLoadedEventEnd),
          finishLoadTime: secs(timing.loadEventEnd),
          firstPaintTime: secs(timing.msFirstPaint),
          firstPaintAfterLoadTime: 0,
          navigationType: 'Other',
          wasFetchedViaSpdy: true,
          wasNpnNegotiated: true,
          npnNegotiatedProtocol: 'h2',
          wasAlternateProtocolAvailable: false,
          connectionInfo: 'h2'
        };
      }
      function csi() {
        return {
          startE: navStart,
          onloadT: timing.loadEventEnd || undefined,
          pageT: timing.loadEventEnd
            ? (timing.loadEventEnd - navStart) / 1000 : 0,
          tran: 15
        };
      }
      var chromeObj = {
        app: {
          isInstalled: false,
          InstallState: {
            DISABLED: 'disabled',
            INSTALLED: 'installed',
            NOT_INSTALLED: 'not_installed'
          },
          RunningState: {
            CANNOT_RUN: 'cannot_run',
            READY_TO_RUN: 'ready_to_run',
            RUNNING: 'running'
          },
          getDetails: fixFnAs(
            function () { return null; }, 'getDetails', 0),
          getIsInstalled: fixFnAs(
            function () { return false; }, 'getIsInstalled', 0)
        },
        loadTimes: fixFnAs(loadTimes, 'loadTimes', 0),
        csi: fixFnAs(csi, 'csi', 0)
      };
      Object.defineProperty(window, 'chrome', {
        value: chromeObj, writable: true,
        enumerable: true, configurable: true
      });
    } catch (e) {}
  });

  section(function () {
    // Permissions consistency (v0.190.0): Chrome answers
    // permissions.query({name: 'notifications'}) from the
    // same state Notification.permission reports; engines
    // that answer the two independently can be caught
    // disagreeing (a standard detector cross-check). Map the
    // query onto the Notification answer when both exist.
    // Always on: this is a consistency fix, not a disguise
    // surface, so it has no switch.
    if (!navigator.permissions || !window.Notification) return;
    var permsProto = Object.getPrototypeOf(navigator.permissions);
    if (!permsProto || !permsProto.query) return;
    var origQuery = permsProto.query;
    function fpPermissionStatus(name, state) {
      // Built on the real PermissionStatus prototype
      // (v0.220.0): the plain literal failed instanceof and
      // carried six enumerable keys where a native status
      // carries none — on the exact object the consistency
      // section exists to make believable.
      var st = window.PermissionStatus
        ? Object.create(PermissionStatus.prototype) : {};
      def(st, 'name', name);
      def(st, 'state', state);
      defHandler(st, 'onchange');
      stubMethod(st, 'addEventListener', 2, function () {});
      stubMethod(st, 'removeEventListener', 2, function () {});
      stubMethod(st, 'dispatchEvent', 1,
        function () { return true; });
      return Promise.resolve(st);
    }
    setMethod(permsProto, 'query', fixFn(function (desc) {
      if (desc && desc.name === 'notifications') {
        var state = Notification.permission === 'default'
          ? 'prompt' : Notification.permission;
        return fpPermissionStatus('notifications', state);
      }
      if (desc && desc.name === 'geolocation' && FP_OPT_GEO) {
        // Agreement with the geolocation section (v0.208.0):
        // the engine's own state can never tell this story —
        // the app disables geolocation at the settings level
        // (it holds no location permission and shows no
        // prompt), so the engine reads denied/prompt while
        // the page DOES receive positions, the profile's
        // city, from the section above. A cross-check of the
        // two caught exactly that. The persona's story is a
        // site the user granted, so the query says granted.
        return fpPermissionStatus('geolocation', 'granted');
      }
      return origQuery.call(navigator.permissions, desc);
    }, permsProto.query));
  });
})();
""".trimIndent()
    }

    /** Quote a string as a JS single-quoted literal. */
    private fun jsStr(value: String): String = buildString {
        append('\'')
        for (c in value) {
            when (c) {
                '\\' -> append("\\\\")
                '\'' -> append("\\'")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '<' -> append("\\u003c")
                else -> append(c)
            }
        }
        append('\'')
    }
}
