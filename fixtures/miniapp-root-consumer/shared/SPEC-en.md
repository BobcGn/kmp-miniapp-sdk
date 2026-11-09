---
translation-of: submodule-fixture-root-logic
language: en
status: draft
title: shared · root-fixture logic artifact
---

# Root-fixture logic artifact

[中文](SPEC-ch.md)

## Responsibility and entry

As the explicitly selected KMP project, commonMain owns greeting while miniappMain exports host entries and reads SDK runtime information. The plugin supplies Mini App compilation/public SDK dependency; this module owns its generated bundle.

## Boundary and dependencies

Only shared behaviour and host binding belong here, not host UI/renderers. Root entries do not transfer Kotlin compilation ownership. The root uniquely manages host paths without silent module overrides. [[module-fixture-root]] owns the host relationship.

## Verification

Parent assembleMiniApp/verifyMiniApp compiles/consumes this module; Node smoke invokes actual generated functions. Build/wiring establishes neither all real-device probes nor new SDK targets.
