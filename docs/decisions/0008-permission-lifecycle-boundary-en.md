# ADR 0008: Permission Lifecycle Boundary

[中文](0008-permission-lifecycle-boundary-ch.md)

- Status: Accepted
- Date: 2026-09-15

## Context

Every device capability the SDK is expected to add — location, scanner, media, microphone, Bluetooth — is gated by a user permission, and WeChat exposes that gate through `wx.getSetting`, `wx.authorize`, and `wx.openSetting`. Without one shared lifecycle, each of those capabilities would invent its own, and the same refusal would be reported differently depending on which capability was asked.

Three things about permissions make them unlike the other capabilities:

- The state belongs to the user, not to the SDK or the host build. It can change while the mini program is running, from outside it.
- A refusal is not a malfunction. The host worked correctly and the answer was no.
- A prompt is only legitimate in response to a user gesture. An SDK that prompts on its own is hostile.

## Decision

- **Permission lifecycle is a common capability.** `MiniAppPermissions` reports `NotRequested`, `Granted`, or `Denied`, identified by `CapabilityKey("permission")`. It is host-neutral: `PermissionKey` names what a permission is *for*, and a `scope.*` string exists only inside the WeChat adapter's mapping.
- **`Denied` is not a host failure.** A refusal is `MiniAppException.PermissionDenied`; a host that cannot answer is `MiniAppException.HostFailure`. Collapsing the two would tell a caller to retry something that retrying cannot fix.
- **Nothing is cached.** Every call asks the host, because the user can change a permission in the host's own settings at any time and a remembered answer becomes wrong the moment they do.
- **The SDK never prompts on its own.** `request` and `openSettings` require a user gesture, and neither is called during startup or as a side effect of another operation.
- **A settings visit is not a grant.** `openSettings` resolves when the settings page closes and reports the state the host gives afterwards. A successful return never becomes `Granted` by assumption.
- **A decision the host already holds is answered from the host's state.** An already granted permission is not requested again, and an already refused one is reported as denied without a second prompt. The state is authoritative; the shape of a failure message is not, which is why only a message that says the authorization was denied becomes `PermissionDenied` and everything else stays a host failure.
- **An unmapped permission is a caller mistake.** A `PermissionKey` the adapter does not map fails before any host call, so an arbitrary string is never handed to `wx.authorize`.
- **Concurrent requests for one permission share one host call.** The call is owned by the permission service, not by whichever caller started it, so cancelling one caller cannot strand the others and cannot be reported as cancelling a host operation the host offers no way to cancel.
- **`PermissionState` and `CapabilitySupport.PermissionDependent` are different things.** The first describes a permission; the second describes a capability that a permission currently blocks. This capability reports `Supported` from its three host APIs and is never itself permission-dependent.
- **Permission does not include privacy.** WeChat's privacy authorization is a separate lifecycle with its own APIs and is not implemented here.
- No prompt framework, no automatic setting change, and no complete enumeration of WeChat scopes is introduced. Only the permissions a capability actually needs are mapped.

## Consequences

- Capabilities that arrive later reuse one lifecycle instead of restating the same three states with their own wording.
- Callers can tell "the user said no" from "the host could not answer", which are different instructions to a consumer: ask the user to visit settings, or retry later.
- Consumers must trigger permission requests themselves; the SDK will not do it for them, and a consumer that never asks simply sees `NotRequested`.
- Because nothing is cached, a permission query costs a host call. That is the price of an answer that cannot go stale.
- `Denied` requires the consumer to send the user to settings, because a host does not re-prompt after a refusal. That is the host's rule, not an SDK choice.
