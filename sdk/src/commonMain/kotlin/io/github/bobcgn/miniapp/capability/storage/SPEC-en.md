---
translation-of: submodule-sdk-storage
language: en
status: draft
title: storage · string key-value contract
---

# String key-value storage contract

[中文](SPEC-ch.md)

## Responsibility and entry

`MiniAppStorage` supplies string get/set/remove; `StorageCapabilityProvider` exposes implementations under key `storage`. These are client storage primitives, not cache-lifetime policy or backend business truth.

## Boundary rationale and trade-off

[MiniAppStorage.kt](MiniAppStorage.kt) explicitly limits the contract to string primitives, not a business-object encoding. Common models consequently bind no serializer, credential policy or cache lifetime. Consumers choose encoding/cache semantics rather than adapters guessing them.

Distinguishing missing from empty and making remove idempotent gives shared logic absence semantics without WeChat failure text. Shared contract checks own this common guarantee, not host method names or permission to clear all consumer data.

## Semantics and boundary

A missing key returns `null`; an empty string is a value. Set overwrites, removal of an absent key succeeds, and keys remain isolated. There is no clear-all operation, object serialization, expiration, secure storage or cloud storage.

Common code uses capability identity and Kotlin types only, not WeChat option bags, raw JS values or platform options. An adapter rejects data that cannot satisfy the string contract rather than coercing it in common models.

## Verification

`StorageContractChecks` drives both `InMemoryStorage` and the WeChat adapter through `MiniAppStorageContractTest` and `WechatStorageContractTest`. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. [[submodule-wechat-evidence]] records real-storage acceptance separately, never inferred from in-memory fakes.
