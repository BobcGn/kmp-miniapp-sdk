---
translation-of: module-build-logic
language: en
status: draft
title: build-logic · bundle measurement and size gate
---

# Bundle measurement and size gate

[中文](SPEC-ch.md)

## Responsibility and entries

This independent included build supplies repository-only plugin `io.github.bobcgn.miniapp.size`, registering verifyMiniAppBundleSize and explicit updateMiniAppSizeBaseline. It is neither published nor applied by consumers and links no implementation into runtime.

The default input is SDK production distribution; miniappSizeDirectory can select another already-built bundle. Filename-based sdk/runtime/consumer/development categories measure raw/gzip bytes per file. Source maps, declarations and manifests participate in full-distribution totals; markup is rejected rather than counted as SDK cost.

## Why artifacts, not source directories, are measured

Consumers pay for compiled modules, runtimes and auxiliary metadata, not source line counts or checkout size. Tasks therefore read actual distributions and report categories/totals. Including markup in SDK cost would assign host UI cost to the wrong owner.

[PERFORMANCE_BASELINE](../docs/PERFORMANCE_BASELINE-en.md) explains deterministic gzip and dual thresholds: small compiler drift should not raise noise, while understood material growth needs review rather than automatic rebaselining. This soft gate promises no SDK startup cost or production reliability.

## Determinism and thresholds

Identical files sorted by relative path produce the same measurement under fixed gzip rules. Recompilation can change inputs slightly; compiler byte-reproducibility is not promised. `docs/performance-baseline.json` records machine-readable version/date/commit/gzip context.

The gate compares raw growth: each category and total must reach or exceed both 4,096 bytes and 5% to fail. Gzip is reported, not separately gated. Shrinkage does not fail; development participates. Baseline updates are explicit maintainer decisions, not automatic acceptance to turn a check green.

## Boundary and verification

This measures no isolated SDK startup time, actual transfer size, memory or WeChat performance. Developer Tools Launch Time is a manual whole-host observation. Size tasks do not join ordinary check, avoiding mandatory production-JS distribution on every validation.

`./gradlew :build-logic:test` checks classification/measurement/baselines/thresholds; `./gradlew verifyMiniAppBundleSize` measures real artifacts. PERFORMANCE_BASELINE records historical context, not actual measurement of current artifacts.
