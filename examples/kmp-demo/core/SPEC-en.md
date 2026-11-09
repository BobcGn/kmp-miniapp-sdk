---
translation-of: submodule-demo-core
language: en
status: draft
title: core · shared logic and Mini App artifact
---

# Serverless-demo shared logic

[中文](SPEC-ch.md)

## Responsibility and entry

Own `Greeting`, sayHello, platform expect/actual and logic tests. miniappMain `platformGreeting()` exports the same `Greeting().greet()`. WeChat reads SDK runtime information, retaining unknown when absent. Mobile and WeChat share behaviour, not views.

## Boundary

Only KMP/platform-library configuration belongs here; root logicProject integrates Mini App in this module. Common logic depends on no Compose/shared UI; SDK runtime enters Mini App only. Exports create no view, call no server and establish no business truth. [[module-example-kmp]] owns module relationships.

## Verification

From the example root run `./gradlew :core:miniappTest`. Common/Mini App tests check greeting and host-information shapes. Node smoke uses real artifacts with isolated hosts, not devices or Developer Tools. Resource isolation is not universal Compose Res support.
