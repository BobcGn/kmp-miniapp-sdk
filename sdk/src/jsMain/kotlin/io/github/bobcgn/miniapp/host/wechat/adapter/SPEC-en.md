---
translation-of: submodule-sdk-wechat-adapter
language: en
status: draft
title: adapter · host behaviour and failure adaptation
---

# Host behaviour and failure adaptation

[中文](SPEC-ch.md)

## Responsibility and internal entries

Convert typed callback ports into Kotlin capabilities or WeChat results, owning conversion, semantic validation, failure classification and resource ownership. Production `Wx*Host` ports call interop; tests inject fakes. Adapters own neither UI nor consumer business logic.

## Why adaptation is not function-by-function forwarding

A successful callback need not carry data satisfying SDK contracts; failure text need not establish user cancellation. Adapters must validate, retain unknown information and classify only with evidence, not casts, zero-filled fields or fuzzy text matches. Otherwise shared logic receives convenient but semantically false values.

[WechatErrorMapper.kt](WechatErrorMapper.kt) centralizes interpretation; ports let tests drive real adapters without wx. Mapping/resource mechanics are shared, not promotion of every platform API into common capabilities. Each domain retains its contract; [[submodule-sdk-wechat]] owns cross-layer direction.

## Behaviour domains

- Storage/HTTP meet common contracts; HTTP requests raw text and connects real task abort to cancellation. Network query/listener probes are separate with per-collector listener pairing; conflation means no lossless-event guarantee.
- Auth obtains short-lived codes or checks WeChat login state, never exchanges backend sessions. `WechatPermissionScopes` uniquely maps permission scopes; permission/privacy queries stay separate. Location queries both prerequisites without prompting.
- Filesystem adapts host-sandbox UTF-8 text, existence/removal, with no SDK path-normalization or traversal-prevention guarantee. Clipboard content is not cached; vibration success proves no human perception.
- Scanning/media start host-owned interactions. Exact ambiguous cancel signals become unattributable interruption, not inferred user intent. Media temporary resources are not SDK-managed long-term; unknown host vocabulary retains original names.
- Subscription associates template answers without sending messages. Standard payment forwards backend parameters and reports interactionCompleted, without signing, order confirmation or virtual-payment fallback.
- Upload/download retain host paths/tasks, abort at most once, report host progress and clean listeners on termination; non-2xx remains a completed exchange.

## Event resources and known limits

BLE is Experimental: adapter/discovery/connection collectors own listeners and attempt identity-preserving removal on termination. Refused removal retains counts, not a zero-leak guarantee. Channels are bounded/drop-oldest, but device-deduplication `seen` has no capacity limit; not all state is bounded.

`WechatErrorMapper` centralizes classification; unknown failures remain HostFailure and diagnostic strings can enter public errors. Some adapters directly construct UnsupportedCapability, differing from the historical unified-guard requirement in [[architecture]]. That gap remains unresolved, not a guarantee that every call is uniformly gated.

## Verification boundary

`Wechat*Test`, shared contract suites and raw-port fakes run through `./gradlew :kmp-miniapp-sdk:jsNodeTest`, covering late/double callbacks, malformed results, abort, progress and listener identity. Real cancellation text, hardware, templates, payment and backend results still need separate [[submodule-wechat-evidence]] evidence.
