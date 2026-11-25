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
 * locale, Date.getTimezoneOffset, synthesised Date strings and
 * locale date formatting), geolocation answers at the zone's city,
 * canvas readback noise (seeded, deterministic), WebGL unmasked
 * vendor/renderer strings, AudioBuffer noise, ClientRects jitter,
 * a fixed battery story, and a hard WebRTC block (stricter than the
 * opt-in normal-mode switch: fingerprint mode never negotiates
 * real ICE candidates).
 *
 * NOT covered, on purpose and recorded in the checkpoint: the HTTP
 * Accept-Language header (the WebView offers no per-request header
 * rewrite; the JS-visible language is the profile's), the device's
 * real font set (only blurred through the canvas/rect noise), and
 * anything on the Gecko engine (no per-session surface — see the
 * checkpoint's Gecko findings; fingerprint tabs are WebView-only).
 */
object FingerprintScript {

    fun payload(resolved: ResolvedFingerprint): String {
        val p = resolved.profile
        val tz = resolved.timezone
        val languages = p.languages.joinToString(",") { jsStr(it) }
        return """
(function () {
  'use strict';
  var SEED = ${p.noiseSeed};
  var LANG = ${jsStr(p.language)};
  var TZ = ${jsStr(tz.id)};
  var ZONE_NAME = ${jsStr(tz.zoneName)};
  function hash(x) {
    x = (x ^ 61) ^ (x >>> 16); x = x + (x << 3); x = x ^ (x >>> 4);
    x = x * 0x27d4eb2d; x = x ^ (x >>> 15); return x >>> 0;
  }
  function def(obj, prop, value) {
    try {
      Object.defineProperty(obj, prop, {
        get: function () { return value; }, configurable: true
      });
    } catch (e) {}
  }
  function section(fn) { try { fn(); } catch (e) {} }

  section(function () {
    var nav = window.Navigator ? Navigator.prototype : navigator;
    def(nav, 'userAgent', ${jsStr(p.userAgent)});
    def(nav, 'platform', ${jsStr(p.platform)});
    def(nav, 'language', LANG);
    def(nav, 'languages', Object.freeze([$languages]));
    def(nav, 'hardwareConcurrency', ${p.hardwareConcurrency});
    def(nav, 'deviceMemory', ${p.deviceMemory});
    var brands = [
      { brand: 'Chromium', version: '131' },
      { brand: 'Google Chrome', version: '131' },
      { brand: 'Not=A?Brand', version: '24' }
    ];
    var uaData = {
      brands: brands, mobile: true, platform: 'Android',
      getHighEntropyValues: function (hints) {
        var all = {
          architecture: 'arm', bitness: '64', brands: brands,
          fullVersionList: [
            { brand: 'Chromium', version: '131.0.6778.104' },
            { brand: 'Google Chrome', version: '131.0.6778.104' },
            { brand: 'Not=A?Brand', version: '24.0.0.0' }
          ],
          mobile: true, model: ${jsStr(p.uaModel)}, platform: 'Android',
          platformVersion: '14.0.0', uaFullVersion: '131.0.6778.104',
          wow64: false
        };
        var out = { brands: brands, mobile: true, platform: 'Android' };
        (hints || []).forEach(function (h) { if (h in all) out[h] = all[h]; });
        return Promise.resolve(out);
      },
      toJSON: function () {
        return { brands: brands, mobile: true, platform: 'Android' };
      }
    };
    def(nav, 'userAgentData', uaData);
  });

  section(function () {
    var scr = window.Screen ? Screen.prototype : screen;
    def(scr, 'width', ${p.screenWidth});
    def(scr, 'height', ${p.screenHeight});
    def(scr, 'availWidth', ${p.screenWidth});
    def(scr, 'availHeight', ${p.screenHeight});
    def(window, 'devicePixelRatio', ${p.devicePixelRatio});
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
    function FpDTF(locales, options) {
      var opts = options ? Object.assign({}, options) : {};
      if (!opts.timeZone) opts.timeZone = TZ;
      var loc = (locales === undefined || locales === null ||
        (Array.isArray(locales) && locales.length === 0)) ? LANG : locales;
      return new OrigDTF(loc, opts);
    }
    FpDTF.prototype = OrigDTF.prototype;
    FpDTF.supportedLocalesOf = OrigDTF.supportedLocalesOf.bind(OrigDTF);
    Intl.DateTimeFormat = FpDTF;
    var OrigNF = Intl.NumberFormat;
    function FpNF(locales, options) {
      var loc = (locales === undefined || locales === null ||
        (Array.isArray(locales) && locales.length === 0)) ? LANG : locales;
      return new OrigNF(loc, options);
    }
    FpNF.prototype = OrigNF.prototype;
    FpNF.supportedLocalesOf = OrigNF.supportedLocalesOf.bind(OrigNF);
    Intl.NumberFormat = FpNF;
  });
  section(function () {
    Date.prototype.getTimezoneOffset = function () {
      return -tzOffsetMinutes(this);
    };
    Date.prototype.toLocaleString = function (locales, options) {
      return new Intl.DateTimeFormat(locales, options).format(this);
    };
    Date.prototype.toLocaleDateString = function (locales, options) {
      return new Intl.DateTimeFormat(locales, options).format(this);
    };
    Date.prototype.toLocaleTimeString = function (locales, options) {
      return new Intl.DateTimeFormat(locales, options).format(this);
    };
    function pad2(n) { return (n < 10 ? '0' : '') + n; }
    Date.prototype.toString = function () {
      if (isNaN(this.getTime())) return 'Invalid Date';
      var dtf = new OrigDTF('en-US', {
        timeZone: TZ, hourCycle: 'h23', weekday: 'short', year: 'numeric',
        month: 'short', day: '2-digit', hour: '2-digit',
        minute: '2-digit', second: '2-digit'
      });
      var map = {};
      dtf.formatToParts(this).forEach(function (part) {
        map[part.type] = part.value;
      });
      var off = tzOffsetMinutes(this);
      var sign = off < 0 ? '-' : '+';
      var abs = Math.abs(off);
      return map.weekday + ' ' + map.month + ' ' + map.day + ' ' +
        map.year + ' ' + pad2(+map.hour) + ':' + map.minute + ':' +
        map.second + ' GMT' + sign + pad2(Math.floor(abs / 60)) +
        pad2(abs % 60) + ' (' + ZONE_NAME + ')';
    };
  });

  section(function () {
    if (!navigator.geolocation) return;
    var proto = Object.getPrototypeOf(navigator.geolocation);
    function position() {
      return {
        coords: {
          latitude: ${tz.latitude}, longitude: ${tz.longitude},
          accuracy: 35, altitude: null, altitudeAccuracy: null,
          heading: null, speed: null
        },
        timestamp: Date.now()
      };
    }
    proto.getCurrentPosition = function (ok, err, opts) {
      setTimeout(function () { if (ok) ok(position()); }, 60);
    };
    proto.watchPosition = function (ok, err, opts) {
      setTimeout(function () { if (ok) ok(position()); }, 60);
      return 1;
    };
    proto.clearWatch = function (id) {};
  });

  section(function () {
    function noiseData(img) {
      var d = img.data;
      for (var i = 0; i + 3 < d.length; i += 4) {
        var h = hash(SEED * 1000003 + i);
        if (h % 211 === 0) {
          var delta = (h % 2 === 0) ? 1 : -1;
          d[i] = Math.max(0, Math.min(255, d[i] + delta));
          d[i + 1] = Math.max(0, Math.min(255, d[i + 1] + delta));
        }
      }
      return img;
    }
    var origGID = CanvasRenderingContext2D.prototype.getImageData;
    CanvasRenderingContext2D.prototype.getImageData = function () {
      return noiseData(origGID.apply(this, arguments));
    };
    function noisedCopy(canvas) {
      var t = document.createElement('canvas');
      t.width = canvas.width; t.height = canvas.height;
      var cx = t.getContext('2d');
      cx.drawImage(canvas, 0, 0);
      cx.putImageData(cx.getImageData(0, 0, t.width, t.height), 0, 0);
      return t;
    }
    var origTDU = HTMLCanvasElement.prototype.toDataURL;
    HTMLCanvasElement.prototype.toDataURL = function () {
      try {
        return origTDU.apply(noisedCopy(this), arguments);
      } catch (e) {
        return origTDU.apply(this, arguments);
      }
    };
    if (HTMLCanvasElement.prototype.toBlob) {
      var origTB = HTMLCanvasElement.prototype.toBlob;
      HTMLCanvasElement.prototype.toBlob = function () {
        try {
          return origTB.apply(noisedCopy(this), arguments);
        } catch (e) {
          return origTB.apply(this, arguments);
        }
      };
    }
  });

  section(function () {
    [window.WebGLRenderingContext, window.WebGL2RenderingContext]
      .forEach(function (ctor) {
        if (!ctor || !ctor.prototype.getParameter) return;
        var orig = ctor.prototype.getParameter;
        ctor.prototype.getParameter = function (param) {
          if (param === 0x9245) return ${jsStr(p.webGlVendor)};
          if (param === 0x9246) return ${jsStr(p.webGlRenderer)};
          return orig.apply(this, arguments);
        };
      });
  });

  section(function () {
    if (!window.AudioBuffer || !AudioBuffer.prototype.getChannelData) return;
    var orig = AudioBuffer.prototype.getChannelData;
    AudioBuffer.prototype.getChannelData = function (channel) {
      var data = orig.call(this, channel);
      try {
        for (var i = 0; i < data.length; i += 997) {
          var h = hash(SEED * 7919 + i + channel * 104729);
          data[i] = data[i] + ((h % 2 === 0) ? 1e-7 : -1e-7);
        }
      } catch (e) {}
      return data;
    };
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
      Element.prototype.getBoundingClientRect = function () {
        return jitter(origEl.call(this));
      };
    }
    if (window.Range) {
      var origRg = Range.prototype.getBoundingClientRect;
      Range.prototype.getBoundingClientRect = function () {
        return jitter(origRg.call(this));
      };
    }
  });

  section(function () {
    if (!('getBattery' in navigator)) return;
    navigator.getBattery = function () {
      return Promise.resolve({
        charging: ${p.batteryCharging},
        chargingTime: ${if (p.batteryCharging) "0" else "Infinity"},
        dischargingTime: Infinity,
        level: ${p.batteryLevel},
        onchargingchange: null, onchargingtimechange: null,
        ondischargingtimechange: null, onlevelchange: null,
        addEventListener: function () {},
        removeEventListener: function () {},
        dispatchEvent: function () { return false; }
      });
    };
  });

  section(function () {
    def(window, 'RTCPeerConnection', undefined);
    def(window, 'webkitRTCPeerConnection', undefined);
    def(window, 'RTCDataChannel', undefined);
    if (navigator.mediaDevices) {
      def(navigator.mediaDevices, 'getUserMedia', undefined);
    }
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
