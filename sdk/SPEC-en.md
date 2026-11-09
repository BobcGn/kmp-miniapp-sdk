---
translation-of: module-sdk
language: en
status: draft
title: sdk · Mini App runtime
---

# Mini App runtime

[中文](SPEC-ch.md)

## Responsibility and public surface

This directory is root-build project `:kmp-miniapp-sdk`, published as `io.github.bobcgn:kmp-miniapp-sdk`. It supplies Kotlin contracts and the current WeChat implementation for consumer shared logic, not consumer builds or final views. [[architecture]] owns system responsibilities.

The Kotlin API and JS/TS ABI are distinct consumption surfaces: interfaces, models, errors and coroutines on one side, `MiniAppExports` and compiler declarations on the other. `WechatHost` / `WechatPlatformApi` are internal; there is no public WeChat Host constructor or arbitrary-host injection entry.

## Module structure and dependencies

- [[submodule-sdk-common]] owns common contracts, version metadata, errors and internal async primitives, with separate Host and capability-domain specs.
- [[submodule-sdk-wechat]] implements common contracts and retains WeChat-specific models and the escape hatch; its subtree describes interop, adapters and runtime.
- [[submodule-sdk-export]] depends on common contracts and the WeChat implementation, adapting host-consumable shapes without taking over host operations.
- [[submodule-sdk-common-tests]] depends only on common logic and reference implementations; [[submodule-sdk-js-tests]] reuses shared checks and drives WeChat implementations and export sessions.

These are source-set/package boundaries in one SDK artifact, not new independent Gradle modules. Common code never depends upward on platform implementations, and production never depends on test source sets, examples or the consumer plugin.

## Build boundary

`build.gradle.kts` configures the single JS library target, CommonJS and TypeScript declarations. Node is the local test environment, not the production host. `explicitApi()` constrains public declarations; the catalog generates the version value rather than a second source literal.

Production dependencies are Kotlin and coroutines. Architecture owns prohibitions on UI frameworks, host markup and business truth; `checkArchitectureBoundaries` checks only declared coordinates and Kotlin namespace classification lists, not arbitrary renderers.

## Verification and limits

```shell
./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

These are SDK automation entries, not proof of WeChat, device or backend success. [[submodule-wechat-evidence]] indexes exact capability status, exclusions and existing acceptance. Importing this spec neither runs nor invents that acceptance.
