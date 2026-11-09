---
translation-of: submodule-sdk-async
language: en
status: draft
title: async · single-terminal callback adaptation
---

# Single-terminal callback adaptation

[中文](SPEC-ch.md)

## Responsibility and internal entry

`awaitHostCallback` turns one callback operation into a cancellable suspension; `HostOperationAborter` represents a real optional host abort hook. Both are internal, not a public general async framework.

## Why mechanics are shared, not business outcomes

A host can call twice, reply after cancellation or complete synchronously before registration returns. Per-adapter continuation handling would duplicate the same resource bugs. [HostCallbackAdapter.kt](HostCallbackAdapter.kt) shares terminal mechanics while adapters retain validation/error meaning, as ADR 0004 records.

Stopping a wait cannot always stop the host, so abort hooks remain optional rather than invented for uniformity. Internal visibility limits public promises about these mechanics, not a generic Promise/event framework or an arbitrary multithreaded-host scheduling guarantee.

## Terminal and cancellation contract

Only the first success/failure settles the continuation; contradictory or late callbacks are ignored. Cancellation stops result delivery and invokes a supplied abort hook at most once, including synchronous cancellation during registration before the aborter returns.

Without a hook, only waiting stops: the underlying operation is not claimed stopped. A registration exception maps to `InternalFailure` while non-terminal. This layer interprets no WeChat words and invents no permission refusal or user cancellation.

## Boundary and verification

Depend only on common errors and coroutines, not platform interop, business code, Promise DTOs or UI. New adapters reuse terminal mechanics rather than copy continuation state machines.

`HostCallbackAdapterTest` covers double completion, cancellation, registration exceptions, optional abort and synchronous-cancellation paths. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`; real abortability remains a concrete adapter/host contract.
