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

## Transport contract

Requests carry method, URL, string headers, encoded text and optional host timeout. Completed 4xx/5xx exchanges are responses; only transport failures throw. Adapters may connect cancellation to a real task abort, without the common interface inventing universal abortability.

There is no payload serialization, cookie handling, redirect policy, streaming or file reference. WeChat upload/download stays behind the platform escape hatch, not host paths added to this contract.

## Connectivity and unresolved discrepancy

State retains both a recognized SDK type and the host word. Unknown vocabulary makes `networkType` null without discarding the original. Query/listener gating is separate; observation neither polls nor synthesizes a starting reading, and each collector owns its listener.

Public KDoc promises every host state, but the WeChat implementation uses `conflate()`, so slow consumers can skip intermediate states. This verified contract gap remains undecided: no lossless-event guarantee or API/implementation change is introduced by spec import.

## Verification

`HttpTransportContractChecks` is shared by reference and WeChat implementations; `WechatNetworkStatusTest` checks querying, listener pairing and errors. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Real connection switching and controlled HTTPS transfer require separate environments; see [[submodule-wechat-evidence]].
