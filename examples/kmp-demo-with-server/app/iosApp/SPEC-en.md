---
translation-of: submodule-server-demo-ios
language: en
status: draft
title: app/iosApp · iOS entry
---

# Server-demo iOS entry

[中文](SPEC-ch.md)

## Responsibility and entry

SwiftUI App/Xcode owns iOS launch. ContentView bridges UIKit to Shared MainViewController. app/shared compiles Kotlin UI/resources; Swift does not copy shared behaviour.

## Boundary

iOS views/signing stay in the consumer app, not runtime SDK or core's Mini App compilation. This is neither an SDK iOS target nor a Node-verifiable host. Local Team/provisioning data stays untracked.

## Verification

Use the example Wrapper for `:app:shared:iosSimulatorArm64Test`, then open `iosApp.xcodeproj` in Xcode for linking/execution. Kotlin tests, Android builds or WeChat navigation records prove no execution of this app; unexecuted checks remain unaccepted.
