---
translation-of: submodule-wechat-evidence
language: en
status: draft
title: wechat · capability status and acceptance evidence
---

# WeChat capability status and acceptance evidence

[中文](SPEC-ch.md)

## Responsibility and entries

The [capability matrix](WECHAT_CAPABILITIES-en.md) indexes current implementation and status; the [host-verification matrix](WECHAT_HOST_VERIFICATION-en.md) owns minimum environments, regression and evidence format. Neither defines new runtime APIs or promotes plans into capabilities.

## Status boundary

`Stable` requires implementation, automation and the required host-level evidence. `Partial` preserves semantic or verification gaps; `Experimental` promises no stability. `Planned`, `Unsupported` and presentation entries are not existing interfaces.

Minimum versions need citable contracts. Where no version is evidenced but an implemented probe exists, record runtime resolution instead of inferring a number from a development machine. API presence, permission, privacy conditions and account eligibility are separate answers.

## Evidence boundary

Unit, Contract, Node, DeveloperTools, RealDevice and BackendRequired prove only their exercised scopes. Original records preserve commits, device, WeChat and base-library versions, and unexecuted checks; historical version values are not replaced with current ones.

Outstanding permission, privacy, app-background, subscription-template, controlled-transfer, real-payment and BLE-connection acceptance does not close when specs are imported. A zero cleanup count is an observation in a run, not a guarantee when a host rejects removal.

## Maintenance and verification

Capability or export changes update both matrix languages and cross-check `PROJECT_FACTS-*`, `ARCHITECTURE-*`, `TESTING-*` and source. Matrix entries and historical Issue chains are indexes, not choices of new development direction.
