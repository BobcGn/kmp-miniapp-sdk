---
translation-of: submodule-sdk-lifecycle
language: en
status: draft
title: lifecycle · app visibility
---

# App visibility

[中文](SPEC-ch.md)

## Responsibility and entry

`MiniAppLifecycle` reports whether the app is in front of the user; `LifecycleCapabilityProvider` exposes it under key `lifecycle`. States are FOREGROUND/BACKGROUND. Before a host report, state is BACKGROUND rather than an invented visible app.

## Events and boundary

`state` is the latest value; `stateChanges` emits the current value to a new collector and subsequent changes, suppressing repeated identical states. Cancelling collection stops observation without a public subscription handle.

Hosts deliver lifecycle to consumer-owned entries, so adapters accept forwarded hooks rather than intercept registrations. Page routes, stacks, component lifecycle and navigation stay outside this common model. The implementation's StateFlow is not an event log retaining every intermediate transition.

## Verification and limits

`LifecycleContractChecks` is shared by `FakeMiniAppLifecycle` and the WeChat app implementation; `WechatLifecycleTest` checks hook forwarding. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Node or foreground display in Developer Tools proves no real background transition; [[submodule-wechat-evidence]] records remaining evidence.
