# Third-party notices

## GeckoView

This app bundles GeckoView (org.mozilla.geckoview), the browser engine from
Mozilla, licensed under the Mozilla Public License 2.0 (MPL-2.0).
Source code: https://hg.mozilla.org/mozilla-central/

GeckoView is distributed as a prebuilt library; the MPL-2.0 applies to the
GeckoView code itself. This app's own code is GPL-3.0, which is compatible
with redistributing MPL-2.0 libraries alongside it.

## Built-in page-guard script

Since 0.91.0 the app bundles one user script, "Via Liquid Glass Pro"
(7.0.0), stored verbatim as `app/src/main/assets/page_guard.js` and
injected into system-WebView pages when the "内置净化脚本" switch is
on. The script was supplied by the project maintainer for inclusion;
its floating in-page UI is suppressed by the app, and the Firefox
(GeckoView) engine does not run it — GeckoView offers no equivalent
injection path, so Firefox tabs rely on the native block list and
tracking protection instead.

## AndroidX / Jetpack Compose

AndroidX libraries, including Jetpack Compose and Material 3, are licensed
under the Apache License 2.0.

## Fingerprint-mode design references

The fingerprint mode's coverage and consistency approach was
informed by studying the public documentation, issue trackers
and published data of several open-source projects. No code
from these projects is included; the implementation is
original, and parameters come from public device
specifications and public measurement data.

- Camoufox (MPL-2.0) — coverage checklist ideas for the
  fingerprint surface (fonts, WebGL, audio, voices).
- Apify fingerprint-suite (Apache-2.0) — the idea of encoding
  cross-field consistency rules so generated profiles cannot
  contradict themselves.
- adryfish/fingerprint-chromium (BSD-3-Clause) — the
  single-seed, multi-surface noise discipline and the
  practice of pinning real per-GPU parameter sets.
- Brave's farbling write-ups — the per-profile stable-noise
  approach.
- CreepJS (MIT) — its public lie-detection checklist informed
  the self-audit of the wrapper functions' stealth and the
  cross-checks (for example permissions versus Notification)
  that the payload now keeps in agreement.
- Cromite (GPL-3.0) — its published Android patch list was
  used as a coverage reference for Android-specific surfaces
  (sensors among them); no patches are included.
- The Chromium project (BSD-style license) — the UA client
  hints brand-list algorithm (GREASE brand derivation and
  the seeded list ordering in user_agent_utils.cc) was
  re-implemented from the public source so the claimed
  version's brand list matches what a real Chrome of that
  version sends; the sensor event pump's nominal 50 ms
  interval constant was likewise read from the public
  source so the synthetic sensor cadence matches the real
  engine's; no Chromium code is included.
- FingerprintJS (MIT) — its public audio-fingerprinting
  method (offline render slice, absolute-sum aggregation)
  served as the yardstick for auditing that the payload's
  audio noise stays orders of magnitude below the real
  cross-device spread; no code is included.
