---
translation-of: module-example-server
language: en
status: draft
title: kmp-demo-with-server · clients and server composition
---

# Clients and server composition

[中文](SPEC-ch.md)

## Responsibility and root configuration

An independent KMP consumer contains mobile clients, WeChat and a Ktor server, consuming current SDK source through includeBuild. Unlike the serverless demo, core applies the plugin with rootHost and sourceDirectory app/miniApp; root logicProject does not migrate modules here.

## Module dependencies

[[submodule-server-demo-core]] supplies shared logic/platform variants; [[submodule-server-demo-ui]] consumes core and owns Compose/resources. [[submodule-server-demo-android]] and [[submodule-server-demo-ios]] consume UI. [[submodule-server-demo-miniapp]] consumes only core's Mini App bundle; [[submodule-server-demo-backend]] consumes its JVM variant.

WeChat greeting makes no server request; initialization needs no running server. A server's presence gives the SDK no backend identity/payment capability. Core does not depend on UI and Mini App carries no Compose.

## Verification and limits

Here `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp` checks Mini App. [README](README-en.md) records mobile/server commands and historical results. NO-SOURCE is not a passed test suite; IDEA/VS Code navigation proves no WeChat, iOS, server or device execution.
