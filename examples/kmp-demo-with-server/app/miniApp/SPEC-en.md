---
translation-of: submodule-server-demo-miniapp
language: en
status: draft
title: app/miniApp · server-demo WeChat host
---

# Server-demo WeChat host

[中文](SPEC-ch.md)

## Responsibility and entry

Native pages consume core platformGreeting and MiniAppExports.sdkVersion to display runtime context. They call no server and compile no Compose UI into WXML. JSDoc/jsconfig uses current bundle declarations while WeChat executes JS.

## Source and generated boundary

Consumers own pages/configuration; sync owns libs; distribution is independent under root build. Commit touristappid placeholders, not private test settings. Initialization fills gaps without migrating existing pages.

## Verification and limits

After sync from the example root, run `node scripts/smoke.cjs` here and independently in distribution. This is isolated-host wiring. DEVELOPMENT records VS Code declarations, accepted IDEA JS-target differences and local Kotlin/KDoc evidence, not WeChat execution, reopening persistence, all IDEs or the serverless demo. Actual host acceptance follows this README separately.
