# ADR 0003: Mini App Host Boundary

[中文](0003-miniapp-host-boundary-ch.md)

- Status: Accepted
- Date: 2026-09-14

## Context

WeChat Mini Program is the first real host of the SDK, but WeChat is not the SDK core. Future hosts may expose different capabilities and may belong to different runtime families. WeChat and Alipay resemble DSL mini-program runtimes, while a host such as Telegram Mini Apps uses a WebView/web runtime. A host abstraction therefore cannot assume that `window` exists, that the DOM is absent, that CommonJS exists, or that WXML exists.

Forcing every host API into one common surface would create a lowest-common-denominator API. Selecting behavior through an ever-growing platform enum and central `when` statements would also require existing core code to change whenever a host is added.

## Decision

- A host is a platform adapter boundary represented by `MiniAppHost`, not a platform enum value.
- WeChat is Host Adapter #1 and its implementation code remains under `jsMain/.../host/wechat`.
- The common layer defines host-neutral contracts and never depends on a concrete host.
- A `CapabilityKey` identifies a semantic SDK capability, and `CapabilitySupport` initially expresses `Supported` or `Unsupported`.
- Capability abstractions are created only when their semantics are genuinely shared. Platform-specific APIs remain available through the typed `HostPlatformApi` escape hatch.
- New hosts are added through new implementations. The core architecture does not dispatch through a central `Platform` enum and `when` branches.
- CommonJS is the current WeChat distribution strategy, not a permanent ABI shared by every future runtime family.
- No host registry, dependency-injection framework, additional Gradle module, or speculative Alipay/Telegram implementation is introduced by this decision.

Future capability-support states may express version or permission requirements when concrete use cases establish their required data. They are not added speculatively now.

## Consequences

- Shared code can reason about semantic capability availability without importing WeChat declarations.
- Consumers can reach host-specific functionality without pretending that it is portable.
- A future host can use a distribution format appropriate to its runtime family.
- Capability interfaces, concrete host implementations, and richer availability states remain future work and must be backed by actual use cases.
