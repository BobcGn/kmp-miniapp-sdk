---
translation-of: module-example-server
language: en
status: draft
title: kmp-demo-with-server · experimental multi-client consumer
---

# Clients and server composition

[中文](SPEC-ch.md)

## Responsibility and root configuration

As the experimental reference consumer defined by [[module-examples]], this includes mobile clients, WeChat and Ktor server with current SDK source via includeBuild, not a generic project template. Unlike the serverless demo, core applies the plugin with rootHost and sourceDirectory app/miniApp; root logicProject does not migrate modules here.

## Why server and clients are different consumers

Ktor can consume core's JVM variant while WeChat consumes its Mini App variant. This observes shared logic/platform-dependency isolation, not a requirement for a WeChat backend or SDK backend ownership. Greeting makes no network call; server is another consumption environment, evidenced by [README](README-en.md) and module source.

Core-local rootHost and the other demo's root logicProject are existing experimental paths, not a promise to maintain two templates. Mobile UI/resources/server remain outside Mini App compilation. Other cases' success cannot fill this case's unaccepted paths.

## Module dependencies

[[submodule-server-demo-core]] supplies shared logic/platform variants; [[submodule-server-demo-ui]] consumes core and owns Compose/resources. [[submodule-server-demo-android]] and [[submodule-server-demo-ios]] consume UI. [[submodule-server-demo-miniapp]] consumes only core's Mini App bundle; [[submodule-server-demo-backend]] consumes its JVM variant.

WeChat greeting makes no server request; initialization needs no running server. A server's presence gives the SDK no backend identity/payment capability. Core does not depend on UI and Mini App carries no Compose.

## Verification and limits

Here `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp` checks Mini App. [README](README-en.md) records mobile/server commands and historical results. NO-SOURCE is not a passed test suite; IDEA/VS Code navigation proves no WeChat, iOS, server or device execution.
