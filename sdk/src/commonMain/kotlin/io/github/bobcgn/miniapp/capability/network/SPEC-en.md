---
translation-of: submodule-sdk-network
language: en
status: draft
title: network · text HTTP and network status
---

# Text HTTP and network status

[中文](SPEC-ch.md)

## Responsibility and public entries

`MiniAppHttpTransport` performs one text HTTP exchange under key `network`; `MiniAppNetworkStatus` queries/observes connectivity with separate query/listener keys. They answer different questions, not a full authenticated, retrying, caching HTTP client.

## Why this owns exchanges and connectivity observation only

[MiniAppHttpTransport.kt](MiniAppHttpTransport.kt) and ADR 0005 distinguish completed HTTP exchanges from transport inability. Callers interpret statuses; otherwise adapters would misreport business responses as host failures. Text keeps payload formats with existing shared logic, at the cost of consumer serialization/authentication/retry policy.

[MiniAppNetworkStatus.kt](MiniAppNetworkStatus.kt) asks a shareable connectivity question without unifying host vocabulary, so original words survive. Querying proves no ability to register/remove a listener. Transfer paths are host-specific and do not enter the common exchange contract merely because transport is HTTP.

## Transport contract

Requests carry method, URL, string headers, encoded text and optional host timeout. Completed 4xx/5xx exchanges are responses; only transport failures throw. Adapters may connect cancellation to a real task abort, without the common interface inventing universal abortability.

There is no payload serialization, cookie handling, redirect policy, streaming or file reference. WeChat upload/download stays behind the platform escape hatch, not host paths added to this contract.

## Connectivity and unresolved discrepancy

State retains both a recognized SDK type and the host word. Unknown vocabulary makes `networkType` null without discarding the original. Query/listener gating is separate; observation neither polls nor synthesizes a starting reading, and each collector owns its listener.

Public KDoc promises every host state, but the WeChat implementation uses `conflate()`, so slow consumers can skip intermediate states. This verified contract gap remains undecided: current implementation promises no lossless events, and recording the gap establishes no API/implementation change.

## Verification

`HttpTransportContractChecks` is shared by reference and WeChat implementations; `WechatNetworkStatusTest` checks querying, listener pairing and errors. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Real connection switching and controlled HTTPS transfer require separate environments; see [[submodule-wechat-evidence]].
