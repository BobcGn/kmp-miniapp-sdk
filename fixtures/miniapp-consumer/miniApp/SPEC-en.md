---
translation-of: submodule-fixture-consumer-host
language: en
status: draft
title: miniApp · single-project WeChat host
---

# Single-project WeChat host

[中文](SPEC-ch.md)

## Responsibility and consumption entry

Keep the consumer-owned WeChat project after initialization. Pages require the generated consumer module and show shared greeting, count and SDK version. WXML/WXSS/setData belong here, not copied SDK behaviour.

## Source and generated boundary

syncMiniApp owns ignored `miniprogram/libs/`; it contains no handwritten UI or deliberately retained stale modules. Parent build/miniapp-dist is the independent acceptance project. Test-account/private settings stay untracked; default clean preservation is not universal custom-path safety.

## Verification and limits

Run syncMiniApp from the parent first, then `node scripts/host-smoke.cjs` here; the assembled distribution can run its copy. This checks relative require wiring, not WeChat execution. Developer Tools must import the real project and record output/environment separately.
