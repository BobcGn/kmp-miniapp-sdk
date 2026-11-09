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

## Behaviour domains

- Storage/HTTP meet common contracts; HTTP requests raw text and connects real task abort to cancellation. Network query/listener probes are separate with per-collector listener pairing; conflation means no lossless-event guarantee.
- Auth obtains short-lived codes or checks WeChat login state, never exchanges backend sessions. `WechatPermissionScopes` uniquely maps permission scopes; permission/privacy queries stay separate. Location queries both prerequisites without prompting.
- Filesystem adapts host-sandbox UTF-8 text, existence/removal, with no SDK path-normalization or traversal-prevention guarantee. Clipboard content is not cached; vibration success proves no human perception.
- Scanning/media start host-owned interactions. Exact ambiguous cancel signals become unattributable interruption, not inferred user intent. Media temporary resources are not SDK-managed long-term; unknown host vocabulary retains original names.
- Subscription associates template answers without sending messages. Standard payment forwards backend parameters and reports interactionCompleted, without signing, order confirmation or virtual-payment fallback.
- Upload/download retain host paths/tasks, abort at most once, report host progress and clean listeners on termination; non-2xx remains a completed exchange.

## Event resources and known limits

BLE is Experimental: adapter/discovery/connection collectors own listeners and attempt identity-preserving removal on termination. Refused removal retains counts, not a zero-leak guarantee. Channels are bounded/drop-oldest, but device-deduplication `seen` has no capacity limit; not all state is bounded.

`WechatErrorMapper` centralizes classification; unknown failures remain HostFailure and diagnostic strings can enter public errors. Some adapters directly construct UnsupportedCapability, differing from the historical unified-guard requirement in [[architecture]]. Import chooses no refactor.

## Verification boundary

`Wechat*Test`, shared contract suites and raw-port fakes run through `./gradlew :kmp-miniapp-sdk:jsNodeTest`, covering late/double callbacks, malformed results, abort, progress and listener identity. Real cancellation text, hardware, templates, payment and backend results still need separate [[submodule-wechat-evidence]] evidence.
