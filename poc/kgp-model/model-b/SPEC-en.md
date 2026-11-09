---
translation-of: submodule-poc-model-b
language: en
status: draft
title: model-b · unused-source-set counterexample
---

# Unused-source-set counterexample

[中文](SPEC-ch.md)

## Responsibility and entry

Retain the default js target while manually creating miniappMain/miniappTest and common parents, reproducing source-set/directory presence without consuming compilations.

## Model boundary

Names/dependsOn edges alone create no platform compilation or real Mini App test run. This retained counterexample is no production fallback; IDE directory visibility is not compilability evidence.

## Verification

From the PoC root `../../gradlew :model-b:reportKgpModel :model-b:checkMiniAppModel` reports facts/failures. Explicit checks are expected not to meet the chosen requirements; ordinary root-build success cannot overwrite that conclusion.
