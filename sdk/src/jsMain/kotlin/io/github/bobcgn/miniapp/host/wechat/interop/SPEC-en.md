---
translation-of: submodule-sdk-wechat-interop
language: en
status: draft
title: interop · raw wx contracts and safe decoding
---

# Raw wx contracts and safe decoding

[中文](SPEC-ch.md)

## Responsibility and internal surface

Declare global `wx`, option bags, callbacks, task handles and raw result shapes. Plain-object factories compensate for external interfaces lacking Kotlin constructors; presence helpers safely report absent host objects/methods.

Raw readers may check types, finite numbers and ranges, distinguishing absent from malformed values. They neither produce business answers nor classify SDK errors. Checks in decoders such as `WxLocation.kt` do not move platform policy into common code.

## Boundary

Keep `dynamic`, `external` and `js()` narrow at this raw boundary; no raw JS object enters common APIs. Factories do not call the host; adapter ports perform concrete callback calls. Interop depends on no business implementation, export or host UI and adds no Kotlin renderer.

Contracts need evidence. A `showToast` declaration proves no production adapter/export. Without a discoverable virtual-payment contract, no speculative request or capability registration is added.

## Verification

`WxOptionsTest`, `WxRuntimeInfoTest` and `Wx*Test` suites construct/check raw shapes, missing methods and invalid values in Node. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Fake objects establish no real base-library availability or permission behaviour.
