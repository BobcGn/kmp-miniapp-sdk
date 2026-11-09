---
translation-of: submodule-server-demo-backend
language: en
status: draft
title: server · Ktor JVM example service
---

# Ktor JVM example service

[中文](SPEC-ch.md)

## Responsibility and entry

`ApplicationKt.main` starts Netty; Application.module GET `/` returns core sayHello("Ktor"). This is a minimal JVM consumer of shared logic, not an SDK backend, authentication or payment service.

## Dependency and runtime boundary

Server depends on core's JVM variant and Ktor/logging. Core, runtime SDK and Mini App greeting do not depend on server. Current source binds 0.0.0.0:8080; startup listens on the network, not a read-only compilation check. Invoking run must account for that listening side effect.

## Verification

From the example root use `./gradlew :server:test` for tests; `./gradlew :server:run` is separate execution. Source/tests or successful shared-core compilation establish no server, real backend-business or client end-to-end acceptance.
