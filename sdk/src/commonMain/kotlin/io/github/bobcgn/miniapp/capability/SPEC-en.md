---
translation-of: submodule-sdk-capabilities
language: en
status: draft
title: capability · identity and support state
---

# Capability identity and support state

[中文](SPEC-ch.md)

## Responsibility and public surface

`CapabilityKey` identifies semantic capabilities; `CapabilitySupport` reports Supported, Unsupported, VersionDependent or PermissionDependent for the current host. The latter two still mean unavailable now, not permission to proceed. Support is neither a build constant nor proof of granted permission.

Common keys express host-neutral meaning; WeChat owns namespaced platform keys, not raw `wx` methods disguised as universal capabilities. The current WeChat gate does not produce PermissionDependent; model presence proves no implemented dynamic permission gating.

## Why identity and support are separate

In [Capability.kt](Capability.kt), a key is stable semantic identity; support is the current host's answer. Combining them into a build-time boolean loses conditions under which another version/state could help. Four states let callers explain unavailability, at the cost that model declarations alone prove no adapter produces/implements every state.

Provider facets leave service provision with domains rather than expanding Host into a service locator. Common-capability promotion needs shared semantics, not merely a same-named WeChat API. [[architecture]] owns that decision principle.

## Domains and dependencies

[[submodule-sdk-storage]] owns string storage; [[submodule-sdk-network]] owns text HTTP and network status; [[submodule-sdk-lifecycle]] owns app visibility; [[submodule-sdk-permissions]] and [[submodule-sdk-privacy]] own separate lifecycles. Domains use small provider facets, not sibling implementations or a central service locator.

Support uses common `HostVersion`, while Host uses keys/support. These are shared types in one common compilation, not independently published one-way modules inferred from directories. Probes, host error text and concrete precondition policy do not belong in model declarations.

## Verification

`CapabilitySupportContractChecks`, `MiniAppHostTest` and domain contract checks run through `./gradlew :kmp-miniapp-sdk:jsNodeTest`. Test models do not replace a live host's capability answers.
