---
translation-of: submodule-sdk-errors
language: en
status: draft
title: error · semantic failure boundary
---

# Semantic failure boundary

[中文](SPEC-ch.md)

## Responsibility and public surface

`MiniAppException` is the semantic boundary for host failures entering common Kotlin. It distinguishes unavailable capability, permission denial, privacy preconditions, timeout, invalid result, ordinary host failure and internal failure. Errors are not backend business conclusions.

`UserCancelled` requires evidence of user intent; `HostInteractionInterrupted` means the host interaction ended without an attributable cause. Coroutine cancellation is neither host error. HTTP error statuses remain responses rather than exceptions caused by status alone.

## Data and dependency boundary

Capability keys, diagnostic strings, sanitized string metadata and Kotlin causes may cross; raw JavaScript objects and host result objects may not. Fields such as `HostFailure.hostMessage` can still carry original diagnostic strings: not every string is promised redacted. Consumer logging needs separate filtering, not whole-error printing.

This layer neither parses WeChat words nor owns retry, identity or order policy. Adapters classify host text. The wording gap between ADR 0009's prohibition on raw messages and current scalar `hostMessage` remains unresolved; import neither removes fields nor changes error behaviour.

## Verification

`MiniAppExceptionTest` checks the model; platform `WechatErrorMapperTest` checks exact classification and unknown-failure retention. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Fake-host checks establish neither real-device cancellation messages nor payment facts.
