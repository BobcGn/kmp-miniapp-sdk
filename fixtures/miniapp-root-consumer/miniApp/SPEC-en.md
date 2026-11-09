---
translation-of: submodule-fixture-root-host
language: en
status: draft
title: miniApp · root-fixture host
---

# Root-fixture host

[中文](SPEC-ch.md)

## Responsibility and entry

The root WeChat project owns native pages/thin JS consuming shared-module greeting/SDK information, not SDK capability behaviour. It demonstrates root-host layout while compilation stays in a logic submodule.

## File and evidence boundary

Consumers maintain source; sync owns generated libs. build/miniapp-dist is an independent copy with no project back-reference. Only placeholder AppID is committed; private test configuration belongs in neither source nor specs.

## Verification

After synchronization, run `node scripts/smoke.cjs` here and independently in distribution. Node checks wiring only. TESTING's recorded Developer Tools/base-library run covers this fixture, not phones, backends or other examples.
