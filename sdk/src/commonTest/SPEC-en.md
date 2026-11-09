---
translation-of: submodule-sdk-common-tests
language: en
status: draft
title: commonTest · reference implementations and shared contract tests
---

# Reference implementations and shared contract tests

[中文](SPEC-ch.md)

## Responsibility and entry

Verify common Kotlin semantics, not WeChat behaviour. Scope includes Host/version comparison, errors, single-terminal callbacks, the generated-version relationship, and common storage, HTTP, lifecycle, permission and privacy contracts.

`testing/` owns `FakeMiniAppHost` and in-memory/recording reference capabilities. `*ContractChecks` accept an implementation so the same guarantees also run against WeChat adapters in [[submodule-sdk-js-tests]]. A reference implementation is not a production SDK fallback.

## Why contract checks are host-independent

Running one guarantee against reference and WeChat implementations separates a wrong common contract from wrong platform mapping. Otherwise fakes/adapters could each pass different assertions. Existing shared checks and [TESTING](../../../docs/TESTING-en.md) establish this layering.

Reference implementations are contract baselines, not production fallbacks merely because tests can store/grant things. The boundary permits replaceable implementations without invented success for callers.

## Boundary and dependencies

Use common code, internal test-visible primitives, Kotlin test and coroutines-test only. No `wx`, `external`, `dynamic`, `js()`, DOM or Node API belongs here. A common contract must not encode a WeChat error word or callback shape.

New cross-implementation guarantees belong in shared checks; WeChat-only behaviour stays in platform tests. Do not widen production public APIs for testing convenience or publish test fakes as consumer capabilities.

## Verification

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

The current JS test compilation executes both common and platform tests; there is no independent JVM test target. Representative entries are `HostCallbackAdapterTest`, `MiniAppHostTest` and the `MiniApp*ContractTest` suites. Test-file presence does not prove actual execution.

## Evidence limits

These checks establish common semantics and reference behaviour, not real `wx`, device permission or backend behaviour. Tests alone cannot close host-acceptance gaps in [[submodule-wechat-evidence]].
