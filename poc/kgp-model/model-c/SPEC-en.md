---
translation-of: submodule-poc-model-c
language: en
status: draft
title: model-c · chosen plugin-owned named target
---

# Chosen plugin-owned named target

[中文](SPEC-ch.md)

## Responsibility and entry

Consumers apply only KMP and `io.github.bobcgn.miniapp.poc`, no JS DSL. The experimental plugin creates the named miniapp target; KGP derives source sets, parents and compilations. ADR 0010 chose this model, continued by production.

## Boundary

miniappTest must actually execute common/platform tests, not merely exist as a lifecycle task. Chosen model ownership makes this PoC neither the released plugin, a host distribution nor SDK API. It owns no backend/UI.

## Verification

At the PoC root run `../../gradlew :model-c:miniappTest :model-c:checkMiniAppModel`. Ten assertions/execution reports are evidence. Cache compatibility needs repeated runs; Node-model success replaces no WeChat acceptance.
