---
translation-of: submodule-sdk-host
language: en
status: draft
title: host · host, versions and capability guard
---

# Host, versions and capability guard

[中文](SPEC-ch.md)

## Responsibility and public entries

`MiniAppHost<P>` exposes runtime `capabilitySupport` and the typed `platform` escape hatch; `HostPlatformApi` marks host-specific APIs. Adding a host adds an implementation, not a central platform-enum branch.

`HostVersion` owns common parsing/comparison. WeChat versions required by particular capabilities are host data, not constants here. `requireSupported` accepts only `Supported`; all other states throw `UnsupportedCapability`. Callers needing a reason query support directly.

## Boundary and known discrepancy

This layer probes no `wx`, manages no permission state and offers no generic service locator. Capability-provider facets belong to their domains rather than an ever-growing Host base.

The guard is a public checking entry, not currently the only exception path: adapters also construct that exception, and some operations are not pre-gated. The difference from ADR 0007's single-path requirement remains explicit in [[architecture]]; this spec decides no refactoring approach.

## Verification

`HostVersionTest` checks parsing/comparison/boundaries; `MiniAppHostTest` and `CapabilitySupportContractChecks` cover support and the guard. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`. These common-semantic checks do not establish real-host availability.
