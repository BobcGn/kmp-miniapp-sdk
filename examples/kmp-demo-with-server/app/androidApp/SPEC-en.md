---
translation-of: submodule-server-demo-android
language: en
status: draft
title: app/androidApp · Android entry
---

# Server-demo Android entry

[中文](SPEC-ch.md)

## Responsibility and entry

MainActivity/application configuration hosts shared Compose UI and owns manifest, packaging and Android lifecycle. The app consumes core through shared rather than copying server/SDK implementations.

## Boundary

Client renderer, Android APIs and local signing stay outside Mini App dependencies. A server module does not establish that greeting UI needs a backend; no network-business requirement follows merely from the example name.

## Verification

At the example root run `./gradlew :app:androidApp:assembleDebug`, then separately install/run. APK evidence establishes no device UI, server chain or SDK Android-target acceptance. This import commits no signing material.
