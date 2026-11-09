---
translation-of: submodule-sdk-wechat
language: en
status: draft
title: wechat · WeChat host composition
---

# WeChat host composition

[中文](SPEC-ch.md)

## Responsibility and entry

`WechatHost` composes common provider facets and WeChat models; `WechatPlatformApi` holds the login, page-stack, device, file, transfer, payment and experimental BLE escape hatch. Both are internal. Consumers currently use the concrete host through [[submodule-sdk-export]], not a public Host construction/injection entry.

Location reuses the same permission/privacy service instances as those capabilities, avoiding a second state owner. Host construction does not eagerly read `wx`. A common contract does not justify moving unrelated platform concepts into common code.

## Evidence and unconfirmed intent

[WechatHost.kt](WechatHost.kt) establishes current composition; [ADR 0003](../../../../../../../../../../docs/decisions/0003-miniapp-host-boundary-en.md) explains WeChat as a Host Adapter rather than SDK core. Page stacks/WeChat login state stay behind the escape hatch, preserving host meaning without promising it on other platforms.

**Unconfirmed design motive:** the maintainer has not established why `WechatHost` / `WechatPlatformApi` are consolidated as internal. No public constructor is an implementation fact, proving neither permanent intentional closure nor a promised new Kotlin Host API. This spec must not decide that unresolved intent.

## Submodule dependencies and boundary

[[submodule-sdk-wechat-interop]] supplies raw contracts, presence and decoding; [[submodule-sdk-wechat-adapter]] uses them with common models and internal async mechanics; [[submodule-sdk-wechat-runtime]] uses common models and adapter inspection ports for gating/lifecycle. Host composes these, not a strict chain of directories.

Platform request/result models stay in this namespace, never turning successful callbacks into backend identity/order facts. A host's native interface is the capability's interaction target, not an SDK renderer. [[architecture]] owns system constraints.

## Verification and limits

`WechatHostTest` checks composition/provider relationships. Runtime, adapter and interop fake-port suites run through `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Test constructors accepting fake ports are not public consumer APIs.

Only WeChat is implemented. Toast is interop-only; virtual payment, WebSocket and Presentation Core are unimplemented; BLE remains Experimental. [[submodule-wechat-evidence]] indexes incomplete host/backend evidence.
