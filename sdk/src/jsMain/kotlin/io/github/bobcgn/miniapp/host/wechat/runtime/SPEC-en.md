---
translation-of: submodule-sdk-wechat-runtime
language: en
status: draft
title: runtime · gating, host information and lifecycle
---

# Gating, host information and lifecycle

[中文](SPEC-ch.md)

## Responsibility and entries

`WechatCapabilityGate` answers from a catalog, runtime version and probes, failing closed to Unsupported for unknown keys or unconfirmed APIs. Support describes the current runtime, not a fixed SDK inventory. The WeChat implementation does not produce PermissionDependent.

`WechatRuntimeInfo` lazily reads/caches version and platform separately, preferring modern APIs with an older-library fallback; `canIUse` stays live. A known base-library version does not establish every API's availability.

## Lifecycle and boundary

`WechatAppLifecycle` accepts consumer-forwarded launch/show/hide and supplies common app state; `WechatPageLifecycle` tracks shown-page routes/state behind the WeChat escape hatch. Neither intercepts App/Page registration nor owns a page tree or navigation framework.

Runtime may use adapter inspection ports; adapters are not required to be strictly downstream. It owns no payment/authentication truth, cached permission decision or automatic prompt. [[submodule-sdk-wechat]] owns sibling dependency relationships.

## Verification and limits

`WechatCapabilityGateTest` checks version boundaries, unknown keys, absent probes and construction without host access. `WechatLifecycleTest` and `WechatPageLifecycleTest` check forwarded hooks. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`.

Version/platform caching and live probing are different guarantees. Real minimum support, app-background transitions and platform differences need concrete environments recorded by [[submodule-wechat-evidence]].
