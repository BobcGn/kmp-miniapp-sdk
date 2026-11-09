---
translation-of: module-example-kmp
language: en
status: draft
title: kmp-demo · serverless client composition
---

# Serverless client composition

[中文](SPEC-ch.md)

## Responsibility and root configuration

An independent build demonstrates shared logic consumed by mobile UI and WeChat. Root logicProject selects `:core` without root KMP compilation. includeBuild consumes current repository source, not a claim that unreleased root selection is a public-release contract.

## Module dependencies

[[submodule-demo-core]] owns greeting/platform information/Mini App compilation. [[submodule-demo-ui]] api-consumes core and owns Compose/resources. [[submodule-demo-android]] and [[submodule-demo-ios]] consume UI; [[submodule-demo-miniapp]] consumes core bundle only, not UI. Core never depends back on hosts.

Consumers manually performed this separation; the plugin does not migrate source. It demonstrates dependency isolation, not universal Compose Res compilation into Mini Apps.

## Verification and limits

Here use its own Wrapper: `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp`. [README](README-en.md) records mobile compilation and both Node smoke commands. Build/test records are not post-migration WeChat or manual IDE acceptance; server-demo results cannot substitute.
