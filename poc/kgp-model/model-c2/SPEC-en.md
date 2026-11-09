---
translation-of: submodule-poc-model-c2
language: en
status: draft
title: model-c2 · hidden-js-target counterexample
---

# Hidden-js-target counterexample

[中文](SPEC-ch.md)

## Responsibility and entry

Apply `.poc.hidden`, retaining internal target name js while exposing miniappMain/miniappTest as consumer entries. This reproduces the rejected approach of hiding the old target without real Mini App compilation ownership.

## Boundary

Manual directories, aliases or apparently present test tasks cannot disguise compilation mismatch. This is no production compatibility path. The parent defines shared comparison criteria without relaxing them for this model.

## Verification

At the PoC root run `../../gradlew :model-c2:reportKgpModel :model-c2:checkMiniAppModel`. Explicit model checks are expected to fail. Ordinary compilation is neither ten-point acceptance nor support for the hidden-target approach.
