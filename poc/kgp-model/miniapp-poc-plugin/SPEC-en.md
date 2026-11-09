---
translation-of: module-poc-plugin
language: en
status: draft
title: miniapp-poc-plugin · experimental model provider
---

# Experimental model provider

[中文](SPEC-ch.md)

## Responsibility and internal entries

An independent included build supplies `io.github.bobcgn.miniapp.poc` and `.poc.hidden` for model-c/model-c2. It removes handwritten JS wiring from consumer scripts to compare real KGP models.

## Boundary

This is not production `io.github.bobcgn.miniapp`; it supplies no SDK module, publication, capability or distribution pipeline. Named/hidden target differences are experiments, not production fallbacks using rejected attachment models.

## Verification

PoC settings resolves it through includeBuild; parent model reports/checkMiniAppModel verify results. Actual compilation ownership/source sets/test execution matter more than descriptor presence. Model results prove no host execution.
