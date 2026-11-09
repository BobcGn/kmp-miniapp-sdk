---
translation-of: submodule-sdk-export
language: en
status: draft
title: export · JS/TS ABI and observation sessions
---

# JS/TS ABI and observation sessions

[中文](SPEC-ch.md)

## Responsibility and public entry

`@JsExport MiniAppExports` and exported DTOs form the JavaScript/TypeScript surface, with compiler-generated declarations and Promise exposure of suspend operations. They adapt Kotlin collections, enums and objects into host-constructible shapes, not host behaviour implementations.

The namespace is outside `host/wechat`, but the current facade holds `WechatHost`, not public arbitrary-host injection. It calls common contracts and the WeChat escape hatch without direct `wx`. The example's handwritten CommonJS wrapper normalizes namespace, argument and result shapes only.

## Why exports have a separate boundary

JS callers must not need to construct Kotlin Map or collect Kotlin Flow, nor receive a second implementation of WeChat behaviour. This layer adapts existing Kotlin behaviour into host-usable arguments, Promises, DTOs and explicit observation entries; capabilities/adapters retain semantics. [MiniAppExports.kt](MiniAppExports.kt) and session classes establish implementation; ADR 0005 records the headers-shape trade-off.

The cost is non-identical Kotlin/JS signatures: declarations and normalization wrappers must match the same artifact. A separate namespace does not establish arbitrary Host replacement. [[submodule-sdk-wechat]] owns the unconfirmed internal-entry motive, not an additional prohibition imposed here.

## Sessions and resource boundary

JS cannot collect Flow or cancel a Promise, so `NetworkStatusObservation` and `BleObservationSession` own collectors between explicit start/stop calls. Closing a BLE adapter also ends discovery/connection observation. Each snapshot retains the latest 32 items, not a complete log or exhaustive scan.

`JsNetworkTransfer` exposes Promise result, abort and latest progress. Cleanup belongs to delegated adapters, not another host implementation. Bounded snapshots do not bound the adapter's device-deduplication set.

## Public contract limits

Host success establishes no identity, order or delivery fact. Unknown vocabulary preserves host information; logs must not expose credentials. TypeScript declarations, Kotlin source/KDoc and IDE navigation are separate chains with no identical default-click promise across editors.

## Verification

`NetworkStatusObservationTest` and `BleObservationSessionTest` run through `./gradlew :kmp-miniapp-sdk:jsNodeTest` for session ownership/stop. CommonJS smoke and strict TypeScript run separately after refreshing example artifacts. See [[submodule-sdk-js-tests]] and [[module-examples]], never equating Node success with host acceptance.
