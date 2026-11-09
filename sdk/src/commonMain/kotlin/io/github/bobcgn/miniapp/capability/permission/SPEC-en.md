---
translation-of: submodule-sdk-permissions
language: en
status: draft
title: permission · permission lifecycle
---

# Permission lifecycle

[中文](SPEC-ch.md)

## Responsibility and public entries

`MiniAppPermissions` supplies stateOf/request/openSettings for `PermissionKey`; `PermissionState` is NotRequested, Granted or Denied, with provider key `permission`. Keys describe purpose; WeChat `scope.*` is not common vocabulary.

## Why state reports host decisions rather than owns authorization truth

[MiniAppPermissions.kt](MiniAppPermissions.kt) and ADR 0008 leave state with the host. Users change settings outside SDK control, so caching would disguise old decisions as current grants. Denied differs from host failure so callers distinguish visiting settings from retrying later, not blindly prompting again.

Purpose keys separate shared code from WeChat `scope.*` vocabulary, at the cost of adapters supporting only explicit mappings. Permission, capability availability and privacy requirements answer different questions rather than merging types for convenience.

## State and interaction contract

Each query asks the host without caching decisions, because users can change host settings at any time. Consumers trigger requests/settings from user gestures; the SDK never prompts at startup or as a side effect of another capability. Closing settings still requires reading real state, not assuming a grant.

Refusal is `PermissionDenied`; inability to answer is HostFailure. They must not be conflated. The current WeChat adapter coalesces concurrent requests for one permission into a service-owned call; this does not impose host-specific implementation details on every host. Permission state differs from CapabilitySupport.PermissionDependent and privacy consent.

## Verification and limits

`PermissionContractChecks`, `MiniAppPermissionsContractTest` and `WechatPermissionsTest` check states, live queries, refusal, settings returns and concurrency. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Real gestures, prompts and account initial states require host acceptance; see [[submodule-wechat-evidence]].
