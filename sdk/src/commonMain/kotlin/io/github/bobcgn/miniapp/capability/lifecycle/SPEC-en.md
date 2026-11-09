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

## Rationale

[MiniAppLifecycle.kt](MiniAppLifecycle.kt) and ADR 0006 choose shareable app visibility rather than WeChat page vocabulary: WebView-like runtimes may lack equivalent pages/stacks. Promoting page navigation would force shared logic to recognize one host.

Consumers own host registration, so SDK accepts forwarded hooks rather than taking App/Page entries. Consumers must perform that wiring. Initial BACKGROUND means no visible-state report yet, not an invented foreground event.

## Events and boundary

`state` is the latest value; `stateChanges` emits the current value to a new collector and subsequent changes, suppressing repeated identical states. Cancelling collection stops observation without a public subscription handle.

Hosts deliver lifecycle to consumer-owned entries, so adapters accept forwarded hooks rather than intercept registrations. Page routes, stacks, component lifecycle and navigation stay outside this common model. The implementation's StateFlow is not an event log retaining every intermediate transition.

## Verification and limits

`LifecycleContractChecks` is shared by `FakeMiniAppLifecycle` and the WeChat app implementation; `WechatLifecycleTest` checks hook forwarding. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Node or foreground display in Developer Tools proves no real background transition; [[submodule-wechat-evidence]] records remaining evidence.
