---
translation-of: submodule-demo-ui
language: en
status: draft
title: shared · Compose UI and resources
---

# Serverless-demo client UI

[中文](SPEC-ch.md)

## Responsibility and entry

Own Android/iOS Compose App, resources and the Swift-facing MainViewController. It api-consumes core for shared greeting. iOS produces the Shared framework; Android retains resource configuration.

## Boundary

This is a legitimate client-renderer home, not SDK runtime. Compose/resources must not enter core's Mini App compilation through common parents. This module applies no Mini App plugin, produces no WeChat bundle and pushes no UI dependency back into core.

## Verification

From the example root, `./gradlew :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug` checks representative compilation; source/README own other host tests. Framework/APK generation proves no device execution or WeChat resource compatibility.
