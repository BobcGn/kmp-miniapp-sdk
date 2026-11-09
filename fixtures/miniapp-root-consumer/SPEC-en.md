---
translation-of: module-fixture-root
language: en
status: draft
title: miniapp-root-consumer · root logic selection
---

# Root logic-selection consumer

[中文](SPEC-ch.md)

## Responsibility and entry

A non-KMP root explicitly selects existing `:shared` through logicProject. The module owns compilation/bundle; the root owns host tasks and source/distribution configuration. Composite consumption checks current source, not availability of this entry in a public release.

## Why root coordination needs its own fixture

Single-project integration cannot expose ownership conflicts between root host and submodule compilation. This fixture separates them so wrong project selection, multiple producers, path overrides and configuration interference become observable consumer failures, not merely present extension fields.

Existing [settings](settings.gradle.kts) and root/shared scripts establish that setup. The absence of client UI narrows the observed scope to coordination; it cannot cover demo renderer/resource combinations or establish host compatibility.

## Module dependencies and boundary

[[submodule-fixture-root-logic]] supplies greeting, Mini App exports and SDK calls; [[submodule-fixture-root-host]] consumes its generated module only. There is no Compose UI, so this establishes no shared-CMP-resource compatibility.

Selection creates no core module, migrates no code and auto-configures no arbitrary dependency projects. Multiple producers, overlapping paths and overrides must be rejected by plugin checks, not fixture workarounds.

## Verification

Here run `../../gradlew initMiniApp syncMiniApp assembleMiniApp verifyMiniApp`, then `node scripts/smoke.cjs` separately inside source/distribution. Existing Developer Tools evidence covers this pure-logic fixture only, not the demos, devices or backend acceptance.
