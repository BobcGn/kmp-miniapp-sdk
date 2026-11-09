---
translation-of: submodule-demo-miniapp
language: en
status: draft
title: miniApp · serverless WeChat host
---

# Serverless-demo WeChat host

[中文](SPEC-ch.md)

## Responsibility and entry

Native pages require the generated core module, call platformGreeting/SDK version and bind WXML/WXSS. jsconfig/JSDoc point to the same bundle's declarations. Kotlin generates no UI; no backend is called.

## File and dependency boundary

Consumers maintain pages/configuration; sync owns ignored libs; the standalone distribution is under example-root build. Pages reference actual core, retaining no stale shared output. Init does not rewrite handwritten pages. Test accounts/private settings stay untracked.

## Verification and limits

After sync from the example root, run `node scripts/smoke.cjs` here and its independent distribution copy. Mock devtools/Android/absent-host results are Node wiring only. WeChat execution, reopening and this demo's manual IDE navigation need their own records, not server-demo screenshots.
