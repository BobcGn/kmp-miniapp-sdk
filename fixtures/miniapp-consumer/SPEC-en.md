---
translation-of: module-fixture-consumer
language: en
status: draft
title: miniapp-consumer · single-project source consumption
---

# Single-project source consumer

[中文](SPEC-ch.md)

## Responsibility and entry

An ordinary independent KMP build applies the plugin by id and composite-substitutes the public runtime coordinate with current source. miniappMain exports common greeting/counter logic and SDK version. Consumers hand-wire no target, source-set hierarchy or runtime dependency.

## Verification intent and trade-off

This is the minimal ordinary-consumer path. If it still hand-names JS targets, declares runtime artifacts or copies generated files, it has not tested removal of wiring. Public calls/real test reports are stronger than task names. Root README integration can therefore point to one executable project rather than separate untested snippets.

Settings includes the SDK for both plugin resolution and dependency substitution: a current-source test mechanism, not a public requirement to check out SDK. [[module-fixture-published]] owns public-dependency identity rather than simulating it here.

## Module and artifact boundary

Compilation, Mini App tests and bundle belong to this project; [[submodule-fixture-consumer-host]] consumes the bundle. Public plugins own distribution tasks without production knowledge of fixture paths. Source host, generated libs and standalone distribution retain different owners.

`verifyConsumerContract` checks actual bundle output; `verifyConsumerSourceHost` checks committed host source. Neither is WeChat acceptance, and file presence proves no test execution.

## Verification

From this directory use the repository Wrapper: `../../gradlew --no-daemon --console=plain clean miniappTest assembleMiniAppBundle verifyConsumerContract`. The aggregate `./gradlew verifyMiniAppConsumerFixture` runs from the SDK root only. TESTING retains existing Developer Tools evidence, not a replacement for independent execution of current artifacts.
