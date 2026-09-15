# ADR 0004: Error and Async Adaptation

[中文](0004-error-and-async-adaptation-ch.md)

- Status: Accepted
- Date: 2026-09-14

## Context

Callback-based host APIs report platform-specific result objects, messages, codes, and cancellation behavior. Passing those values directly to shared consumers would leak host contracts into `commonMain`. Treating coroutine cancellation as host-operation cancellation would also be incorrect because many host APIs provide no abort mechanism.

Host callbacks may be late or contradictory. An adapter must tolerate success followed by completion, failure followed by completion, a callback after coroutine cancellation, and defective hosts that invoke more than one terminal callback.

## Decision

- `MiniAppException` is the common semantic error boundary. Its minimal variants are `UnsupportedCapability`, `PermissionDenied`, `UserCancelled`, `HostFailure`, `InvalidResponse`, and `InternalFailure`.
- Raw host result objects remain inside the host-specific interop and adapter layers. A host adapter maps typed scalar diagnostics into `HostFailure`, including host, optional code, message, optional cause, and sanitized string metadata.
- `awaitHostCallback` is the internal callback-to-coroutine primitive. It accepts already-mapped `MiniAppException` failures and permits one terminal result only.
- Cancelling a coroutine always stops it from consuming later callbacks. It does not imply that the underlying host operation stopped.
- If a host operation supplies a real abort hook, cancellation invokes it at most once. With no hook, the host operation may continue in the background and its eventual callback is ignored.
- Exceptions thrown while registering callbacks are converted to `InternalFailure`.
- Promise and event adapters are not generalized until a concrete host integration requires them.

## Consequences

- Common consumers receive stable SDK semantics instead of raw JavaScript values.
- Host-specific error classification remains the responsibility of each host adapter.
- Cancellation behavior is truthful for both abortable and non-abortable APIs.
- Capability adapters can reuse the primitive, but no Storage, Auth, Network, or other capability is implemented by this decision.
