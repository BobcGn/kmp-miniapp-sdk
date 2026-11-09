---
translation-of: submodule-sdk-common
language: en
status: draft
title: commonMain · common Kotlin contracts
---

# Common Kotlin contracts

[中文](SPEC-ch.md)

## Responsibility and entry

Own shareable Host/capability contracts, models, errors and internal async primitives. `api/MiniAppSdk.kt` supplies version metadata generated from the catalog by Gradle, not a second source literal. This layer neither creates concrete hosts nor implements consumer business logic.

## Submodules and dependencies

[[submodule-sdk-host]] defines hosts and versions; [[submodule-sdk-capabilities]] defines support and capability domains. They share common types within one compilation, not completely independent published packages. Capability declarations use identity keys; Host uses support and errors; [[submodule-sdk-errors]] uses keys; [[submodule-sdk-async]] uses errors and coroutines.

Types depend only on Kotlin and coroutines, never upward on WeChat, exports, test fakes or app UI. No `wx`, DOM, Node, `external`, `dynamic` or `js()` enters this source set. New common contracts must justify genuinely shared semantics; host-specific options remain with their host.

## Public surface and verification

Public declarations have explicit visibility and types; the internal callback primitive is not consumer API. [[submodule-sdk-common-tests]] checks guarantees with reference implementations and shared checks, reused by platform tests. The current execution entry is `./gradlew :kmp-miniapp-sdk:jsNodeTest`.

Contracts are not acceptance status: implemented capabilities with incomplete verification do not become Stable through this layer. [[architecture]] owns system responsibilities.
