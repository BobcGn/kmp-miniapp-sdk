---
translation-of: module-example-kmp
language: en
status: draft
title: kmp-demo · experimental serverless consumer
---

# Serverless client composition

[中文](SPEC-ch.md)

## Responsibility and root configuration

As the experimental reference consumer defined by [[module-examples]], this independent build observes shared logic consumed by mobile UI/WeChat, not a generic project template. Root logicProject selects `:core` without root KMP compilation. includeBuild consumes current repository source, not a claim that unreleased root selection is a public-release contract.

## Why core and UI are separate

Mini App compilation inherits common source-set dependencies. When shared also owns Compose UI/resources, adding a target alone does not make those dependencies fit WeChat. Manual consumer separation preserves behaviour/mobile UI while observing independent core Mini App delivery. [README](README-en.md) records the original problem/verification scope.

Root logicProject is this case's current shape, not mandatory consumer layout. The experiment establishes no template stability, all-platform execution or automatic migration; the parent owns both demos' role/trade-off.

## Module dependencies

[[submodule-demo-core]] owns greeting/platform information/Mini App compilation. [[submodule-demo-ui]] api-consumes core and owns Compose/resources. [[submodule-demo-android]] and [[submodule-demo-ios]] consume UI; [[submodule-demo-miniapp]] consumes core bundle only, not UI. Core never depends back on hosts.

Consumers manually performed this separation; the plugin does not migrate source. It demonstrates dependency isolation, not universal Compose Res compilation into Mini Apps.

## Verification and limits

Here use its own Wrapper: `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp`. [README](README-en.md) records mobile compilation and both Node smoke commands. Build/test records are not post-migration WeChat or manual IDE acceptance; server-demo results cannot substitute.
