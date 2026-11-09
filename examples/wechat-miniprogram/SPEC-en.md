---
translation-of: module-example-wechat
language: en
status: draft
title: wechat-miniprogram · capability acceptance host
---

# SDK capability acceptance host

[中文](SPEC-ch.md)

## Responsibility and entries

Native App/Page, WXML/WXSS and thin TypeScript display SDK capability checks and forward consumer-owned lifecycle hooks. `miniprogram/libs/kmp-miniapp-sdk.js` and `.d.ts` are handwritten normalization; root buildMiniAppSdk refreshes other compiler artifacts. Pages do not copy adapters.

## Consumption and safety boundary

Pages bind state/events/capability calls, not rendering on the SDK's behalf or confirming identity/payment/delivery from callbacks. Gesture-requiring calls come from explicit actions. Missing template, payment or controlled-transfer configuration preserves no-host-call guards.

Report closed diagnostics/result shapes, not login codes, coordinates, scan contents, media paths, templates or payment credentials. Real AppID/private settings stay untracked. This project differs from both KMP greeting demos and shares no automatic acceptance conclusions with them.

## Verification and limits

First run `./gradlew buildMiniAppSdk` at the SDK root; here run `npm ci && npm run smoke && npm run typecheck`. Smoke installs fake wx; typecheck checks generated declarations. [[submodule-wechat-evidence]] indexes real-host steps and incomplete permission/backend evidence.
