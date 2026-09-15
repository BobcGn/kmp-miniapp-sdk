# ADR 0009: Privacy Authorization Boundary

[中文](0009-privacy-authorization-boundary-ch.md)

- Status: Accepted
- Date: 2026-09-15

## Context

ADR 0008 established a permission lifecycle for the SDK. WeChat also gates a separate condition on top of system permissions: from base library 2.32.3 it asks the user to accept the mini program's privacy contract before the personal-data APIs the mini program has declared may be called. Below that base library the host does not intercept those calls at all.

The two conditions look similar from a distance and are not: a system permission is granted to the application by the user through the operating system, while a privacy contract is the host's own condition over the data a mini program collects. They are prompted separately, stored separately, and cleared separately. Modelling one as the other would tell a consumer to ask the user for the wrong thing.

## Decision

- **Privacy is a common capability, separate from permission.** `MiniAppPrivacy` is identified by `CapabilityKey("privacy")` and has its own contract, adapter, and provider facet. It shares no state with `MiniAppPermissions`; neither adapter may stand in for the other.
- **Three things are modelled separately**, because WeChat reports them separately: the queryable requirement (`PrivacyStatus`), the result of one attempt (`PrivacyAuthorizationOutcome`), and SDK errors. Collapsing them into one enum would make a momentary answer look like a lasting state.
- **`NOT_REQUIRED` does not mean the user agreed.** WeChat reports it both when the user has accepted the contract and when the mini program declares no collection at all, so the state is named for the host's requirement rather than for a consent the SDK cannot prove.
- **A refusal is an outcome, not an exception.** Declining the contract is a correct host result, so `requestAuthorization` returns `Refused` and only a host-level problem raises. This differs from the permission lifecycle, where a refusal raises, because there the caller asked for something it cannot proceed without.
- **The SDK reports only a recognizable WeChat refusal.** The host sends a declined contract and a dismissed prompt through the same failure callback and publishes no field that separates them, so there is no `Cancelled` outcome. Only a recognizable refusal message maps to `Refused`; an unknown failure remains a `HostFailure`.
- **The precondition point never prompts.** `requireSatisfied` queries the host and fails with `PrivacyAuthorizationRequired` while the host requires acceptance. Showing a prompt is the consumer's job, from a user gesture, at startup never.
- **Privacy has its own error variant.** `MiniAppException.PrivacyAuthorizationRequired` is not a `PermissionDenied`: the two send a consumer to different remedies. A host without the privacy APIs reports `UnsupportedCapability` rather than letting a missing function throw.
- **Nothing is cached.** The host's requirement changes when the user answers and when the mini program's declared collection changes, so every call asks the host.
- **The host's failure text is interpreted in exactly one place.** WeChat publishes no stable code for a privacy refusal, so one internal mapper classifies the failure and is covered by tests; the raw message never reaches a public type.
- Concurrent requests share one host call, because WeChat owns a single privacy prompt. The call is owned by the adapter rather than a caller, and no abort hook is invented: the host offers none.
- Location, scanner, media, microphone, and Bluetooth are not implemented, and only capabilities that genuinely need the contract should call the precondition point.

## Consequences

- Shared code can ask "may I proceed?" without knowing whether the obstacle is a system permission or the host's privacy contract, and a consumer is told which one to resolve.
- Privacy state cannot be mistaken for a permission state, so a future device capability cannot accidentally satisfy one by checking the other.
- Consumers are responsible for both the mini program's backend privacy configuration and the prompt itself; the SDK informs and gates, and never presents anything or claims compliance.
- Because the host does not distinguish a decline from a dismissal, a consumer that needs to react differently to those two cannot; that limitation is the host's, not the SDK's.
