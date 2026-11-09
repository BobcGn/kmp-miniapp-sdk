---
translation-of: submodule-demo-ios
language: en
status: draft
title: iosApp · SwiftUI application entry
---

# Serverless-demo iOS entry

[中文](SPEC-ch.md)

## Responsibility and entry

The Xcode project/SwiftUI App owns iOS lifecycle. ContentView embeds Shared framework `MainViewController()` through UIViewControllerRepresentable. Swift bridges the client rather than reimplementing core greeting.

## Boundary

UIKit/SwiftUI/signing stay in this app/shared UI, not SDK or Mini App. Spec maintenance adds no real TEAM_ID or machine credentials. This Xcode host is not a new Gradle project.

## Verification

From the example root use `./gradlew :shared:compileKotlinIosSimulatorArm64` for Kotlin, then open `iosApp.xcodeproj` in Xcode. Framework compilation replaces neither Xcode linking nor simulator/device execution.
