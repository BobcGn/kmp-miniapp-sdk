---
translation-of: submodule-poc-model-a
language: en
status: draft
title: model-a · manual named-target control
---

# Manual named-target control

[中文](SPEC-ch.md)

## Responsibility and entry

The consumer directly declares `js("miniapp")`, configuring Node/CommonJS library/TypeScript declarations. This is a valid control for KGP-derived miniappMain/miniappTest without an experimental plugin.

## Model boundary

It creates compilations/source sets but fails the delivery requirement of no manual consumer JS wiring. Compilability makes it neither the production plugin contract nor the chosen approach. [[module-kgp-model-poc]] owns comparison relationships.

## Verification

From the PoC root run `../../gradlew :model-a:reportKgpModel :model-a:checkMiniAppModel`. The manual-wiring requirement is expected to fail; reports establish other model facts. Removing that requirement must not turn a control into falsely complete acceptance.
