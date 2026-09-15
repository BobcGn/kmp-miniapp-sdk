# ADR 0007: Capability Support States and Version Gating

[中文](0007-capability-support-and-version-gating-ch.md)

- Status: Accepted
- Date: 2026-09-15

## Context

ADR 0003 limited capability support to `Supported` and `Unsupported` and explicitly deferred anything richer: "Future capability-support states may express version or permission requirements when concrete use cases establish their required data. They are not added speculatively now."

BOB-70 is that use case. A capability that works in WeChat Developer Tools can be absent from an older base library, and until now the SDK answered from a hardcoded list, so a compatibility problem surfaced as an unexpected host failure instead of a clear "not available here". WeChat exposes `wx.canIUse` and the base-library version, so the question can be answered from the host itself rather than from a table.

## Decision

- **`CapabilitySupport` gains `VersionDependent` and `PermissionDependent`.** `Supported` and `Unsupported` keep their meaning. `VersionDependent(requiredVersion, currentVersion)` says the host provides the capability from a later runtime version; `PermissionDependent(permission)` says the capability depends on a permission. Both mean the capability is not usable now.
- **A host answers from its runtime, not from a list.** `WechatHost` delegates to a gate that reads `wx.canIUse` and the base-library version, so one SDK build can answer differently on two hosts. That is the point of the capability model rather than a side effect.
- **Version comparison lives in `commonMain` as `HostVersion`.** Parsing and ordering dotted numeric versions is platform-neutral logic, so a future host reuses it instead of reimplementing it in its own tests. Only numeric segments are understood; anything else is undeterminable, because a wrong comparison would silently misreport support.
- **Version data is recorded only where it is known.** A catalog entry may leave its minimum version unset and rely on `wx.canIUse`, which answers for the host actually running rather than for a figure copied from a table. WeChat's official documentation confirms `wx.canIUse` from 1.1.1, `wx.getAppBaseInfo` and `wx.getDeviceInfo` from 2.20.1, and that `wx.getSystemInfoSync` became unmaintained from 2.20.1. The runtime-detection capability's 2.20.1 minimum names the modern split-API boundary; the legacy fallback still reads the version needed to report `VersionDependent`. The entry pages for the current Storage APIs and `wx.request` do not state an introduction version for the API itself, so they remain live `wx.canIUse` decisions.
- **`PermissionDependent` is modelled but not yet produced by WeChat.** Nothing in the WeChat catalog declares a permission, because the permission lifecycle capability owns the NotRequested/Granted/Denied model. The state exists so that model has somewhere to report into, and until then it is exercised through the FakeHost contract checks.
- **`requireSupported` is the single path that produces `UnsupportedCapability`.** A caller that cannot proceed without a capability calls it and receives an SDK error instead of inventing its own. Every state other than `Supported` fails. The exception still carries only the key; a caller that needs the reason reads the support state.
- **Scalar runtime reads are deferred and cached independently.** The export facade constructs the host while the module is loading and a Node process has no `wx`, so nothing is probed during construction. Version and platform are each retained after their first read. `wx.canIUse` remains a live capability query and does not use that cache.
- The catalog is WeChat-only. No multi-host capability registry is introduced.

## Consequences

- Shared code can tell "this host will never provide it" from "this host cannot provide it here", which is the difference between giving up and degrading.
- Capability answers become a runtime probe, so they are no longer constant for a build. Tests and documentation that assumed a static answer had to be restated.
- A single sealed state can name only one blocking reason, so a capability that is both version- and permission-gated cannot report both at once. This limitation is recorded rather than solved speculatively.
- A host that cannot be probed at all fails closed: a capability the SDK cannot confirm is reported `Unsupported` rather than assumed present.
- The capability matrix can record where a version boundary is known and state that everything else is resolved at runtime, instead of leaving every row pending.
