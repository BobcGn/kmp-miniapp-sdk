---
translation-of: submodule-server-demo-ui
language: en
status: draft
title: app/shared · Compose client UI
---

# Server-demo Compose client UI

[中文](SPEC-ch.md)

## Responsibility and entry

Own Compose App, mobile resources and MainViewController, api-consuming core logic. Shared framework is the Swift entry. Client rendering belongs here, not in SDK.

## Boundary

Compose, Material, client lifecycle integrations/resources stay in this module. It applies no Mini App plugin and is no parent source set of core. A platform source directory proves neither configured targets nor executed tests; Gradle is the authority.

## Verification

From the example root, `./gradlew :app:shared:testAndroidHostTest :app:shared:iosSimulatorArm64Test` checks host tests; mobile apps build separately. Tests/framework compilation prove no iOS launch, device behaviour or WeChat Compose-resource support. README/run records own actual results.
