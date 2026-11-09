---
translation-of: submodule-demo-android
language: en
status: draft
title: androidApp · Android application entry
---

# Serverless-demo Android entry

[中文](SPEC-ch.md)

## Responsibility and entry

This Android application owns MainActivity, manifest, app configuration and packaging, displaying core behaviour through shared UI. It adds no Android target to the SDK.

## Boundary

Android/Compose dependencies stop in the client, outside Mini App runtime classpaths. Entries copy no SDK adapters and treat no WeChat generated library/WXML as Android resources. Core owns behaviour; the client owns display.

## Verification

Run `./gradlew :androidApp:assembleDebug` at the example root. APK generation proves build only; installation, startup, UI and device behaviour need execution. Spec import creates/commits no signing or machine-local configuration.
