---
translation-of: submodule-server-demo-core
language: en
status: draft
title: core · mobile, JVM and Mini App logic
---

# Server-demo shared logic

[中文](SPEC-ch.md)

## Responsibility and entry

commonMain supplies sayHello shared by Android/iOS/JVM. Mini App platformGreeting formats SDK runtime information, retaining unknown for absent fields, then calls sayHello without contacting server.

## Build and dependency boundary

This module applies Mini App with rootHost and source app/miniApp, retaining core compilation/bundle ownership. SDK runtime enters Mini App only; Compose/resources belong to client UI. [[module-example-server]] owns module relationships; UI/Ktor server dependencies never flow back into core.

## Verification and limits

At the example root, `./gradlew :core:miniappTest :core:checkMiniAppHostBoundary` checks Mini App logic/dependencies. Other platforms need separate compilation/tests. README's jvmTest NO-SOURCE proves no JVM suite, and a JVM target establishes no backend business chain.
