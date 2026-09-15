# Architecture

[中文](ARCHITECTURE-ch.md)

This document records stable architectural boundaries. It does not imply that every described integration has been implemented. See [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) for current capabilities.

## 1. System Boundary

```text
commonMain
    ↓
jsMain/export facade
    ↓
Kotlin/JS artifact
    ↓
thin CommonJS export normalization
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program runtime
```

## 2. Dependency Direction

Platform-neutral interfaces, models, errors, and shared logic belong in `commonMain`. Platform implementations belong in `jsMain` and depend on the neutral contracts.

```text
jsMain platform implementation
            ↓ depends on
commonMain interfaces and models
```

`commonMain` must never depend on WeChat implementation details. WeChat-specific code must not leak upward into the shared layer.

## 3. Host Boundary

The WeChat Mini Program is the host runtime. The SDK does not own:

- WXML or WXSS rendering
- Page rendering
- Component trees
- Application UI

The SDK is limited to shared logic, runtime integration, and a typed platform bridge.

`MiniAppHost` is the platform adapter boundary. Shared code identifies semantic capabilities with `CapabilityKey` and observes their current `CapabilitySupport`; it does not select a host with a platform enum. A new host is added as a new implementation rather than as branches in a central `when` statement.

Concrete common capabilities are exposed through small capability-provider facets. The first example is `StorageCapabilityProvider`, which lets `WechatHost` provide `MiniAppStorage` without adding a generic service locator or bypassing the host boundary.

WeChat client login intentionally does not become a common Auth capability. `WechatHost.platform` exposes the WeChat-specific authentication adapter through `WechatPlatformApi`, preserving the meaning of `wx.login` without inventing a misleading cross-host `login(): String` contract.

WeChat is Host Adapter #1. Its platform-specific code is isolated under `jsMain/.../host/wechat`. No Alipay or Telegram implementation currently exists.

### Capability and platform escape hatch

Only semantics that are genuinely common across hosts should become common capabilities. The SDK does not hide the underlying platform or force unrelated APIs into a lowest-common-denominator abstraction. `MiniAppHost.platform` provides a typed `HostPlatformApi` escape hatch for platform-specific APIs; concrete platform APIs will be introduced only with real consumers.

Capability support has four states: `Supported`, `Unsupported`, `VersionDependent`, and `PermissionDependent`. The last two describe what the host would need in order to help, and both mean the capability is not usable now. A host answers from its own runtime rather than from a list, so one SDK build can answer differently on two hosts. Version and platform reads are deferred until each is first needed and cached independently because the export facade builds the host while the module is still loading; `wx.canIUse` remains a live query.

Version comparison is platform-neutral logic and lives in `commonMain` as `HostVersion`. Which base library provides a capability is host data, so it lives in the WeChat catalog, and an entry may leave its minimum version unset and rely on `wx.canIUse` instead — a live answer for the host actually running rather than a figure copied from a table. What the WeChat runtime reports about itself, including the base-library version, is host-specific and reachable only through the escape hatch.

`requireSupported` turns any state other than `Supported` into `MiniAppException.UnsupportedCapability`, and is the only path that produces it. A host that cannot be probed at all fails closed: a capability the SDK cannot confirm is reported `Unsupported` rather than assumed present.

### Runtime families

Mini-app hosts may belong to different runtime families. WeChat-like DSL runtimes and WebView/web runtimes do not share universal assumptions about `window`, DOM, CommonJS, or WXML. CommonJS is the current WeChat distribution strategy, not the permanent ABI of every future host.

### Lifecycle and navigation

App-level lifecycle is a capability. Every mini-app host can say whether the application is in front of the user, so `MiniAppLifecycle` exposes the current state and a stream of changes, and the host adapter republishes the hooks its runtime hands the consumer.

Page-level lifecycle and page-stack navigation are not capabilities. A page, a page route, and a page stack belong to a DSL mini-program runtime, and a WebView host has none of them. `WechatPageLifecycle` and `WechatNavigation` therefore live under `host/wechat` and are reachable only through the platform escape hatch. Describing them as neutral contracts would present WeChat semantics as universal ones.

Lifecycle is forwarded rather than intercepted: WeChat reports lifecycle only to the `App(...)` and `Page(...)` registrations the consumer owns, so the SDK exposes entry points the consumer forwards its hooks into.

### Permission

Permission is a capability because every host that gates device access on the user's decision has the same three answers. `MiniAppPermissions` reports `NotRequested`, `Granted`, or `Denied` for a host-neutral `PermissionKey`, and never keeps an answer: the user can change a permission in the host's own settings at any time, so a remembered state would go stale without notice.

A refusal is `MiniAppException.PermissionDenied` and a host that cannot answer is `HostFailure`. They are not the same instruction to a consumer — one says ask the user to visit settings, the other says try again. Requesting and opening settings need a user gesture, so the SDK never prompts on its own, and a settings visit reports the state the host gives afterwards rather than assuming a grant.

`PermissionKey` names what a permission is for. The `scope.*` string a host uses for it exists only inside the WeChat adapter's mapping, and a key that adapter does not map fails before any host call instead of being forwarded.

`PermissionState` and `CapabilitySupport.PermissionDependent` answer different questions: the first is a permission's own state, the second says a capability is blocked by one. The permission capability reports `Supported` from its host APIs and is never itself permission-dependent.

Permission is not privacy. WeChat's privacy authorization is a separate lifecycle with its own APIs, its own capability, and its own state; sharing either model with the other would misreport one as the other.

### Privacy

Privacy is a capability because every host that collects personal data on the user's behalf has some form of consent condition, and WeChat states it explicitly. `MiniAppPrivacy` reports what the host currently requires for its own privacy contract, asks the host to obtain the user's acceptance, and exposes the precondition point a gated capability calls. It shares no state with `MiniAppPermissions`: the two are prompted, stored, and cleared separately by the host.

Three things are modelled separately, because the host reports them separately: the queryable requirement, the result of one attempt, and SDK errors. A requirement of `NOT_REQUIRED` is a statement about the host, not proof that the user agreed, because WeChat also reports it when the mini program declares no collection at all.

A refusal is an outcome rather than an error. Declining and dismissing enter the host failure callback, but only a recognizable refusal message maps to `Refused`; every unknown failure conservatively remains a `HostFailure`. The SDK does not invent a decline/cancel distinction the host does not provide. A failed privacy precondition is `MiniAppException.PrivacyAuthorizationRequired`, never a `PermissionDenied`, and a host without the privacy APIs reports `UnsupportedCapability`.

The precondition point queries the host and never shows anything: only a consumer can prompt, and only from a user gesture.

### Device capabilities

A device capability reaches the user through the platform escape hatch, not through a common capability, unless its semantics are genuinely shared across hosts. The clipboard and the two vibration lengths follow that rule: `WechatClipboard` and `WechatHaptics` live under `host/wechat`, and the capability catalogue gates them individually under namespaced keys, because a host may expose one clipboard direction or one vibration length without the other.

A host object is not `wx`: `wx.canIUse` answers for `wx` APIs and some component objects, but not for every object the SDK reaches. A capability entry may therefore carry its own presence probe, which the gate runs in addition to the version and schema checks. The file manager is the first such case, and it is why a capability is reported supported only when the method itself is there rather than when its manager is.

The file system reaches the user the same way: `WechatFileSystem` lives under `host/wechat`, works only on UTF-8 text inside the mini program sandbox, and is gated per operation. It does not stand in for a portable file API, and it adds no directory, stream, descriptor, or traversal operation that WeChat does not offer.

A device call resolving means the host accepted and performed it. It never means more than that: a vibration in particular can only be confirmed by someone holding the device, so nothing in the SDK reports one as having been felt.

## 4. JS Interop Boundary

JavaScript-specific constructs such as `external`, `dynamic`, and `js()` may exist only in `jsMain`. Raw platform contracts are restricted to `jsMain/.../host/wechat/interop`.

Business and shared layers must not manipulate `dynamic` values directly. Conversion to typed Kotlin values belongs at the adapter boundary.

The WeChat interop layer models the global `wx` object, option bags, result shapes, and callbacks only. Its plain-object factories exist solely because external interfaces have no Kotlin constructors; they do not invoke host APIs or contain error mapping, capability policy, or business logic.

`host/wechat` is the platform-specific namespace beneath the host boundary. This namespace preserves room for future hosts without introducing additional host implementations or Gradle modules now.

## 5. Async Boundary

The architectural target for asynchronous platform APIs is:

```text
wx callback
    ↓
interop
    ↓
adapter
    ↓
coroutine-friendly Kotlin API
```

`MiniAppException` is the platform-neutral semantic error boundary. Raw host failures stay in interop and host adapter code; adapters preserve useful scalar diagnostics while mapping them to SDK errors. Raw JavaScript objects never enter the common error model.

The internal `awaitHostCallback` primitive implements the callback-to-coroutine mechanics without implementing a capability. It accepts one success or failure terminal result and ignores every later callback, including callbacks received after cancellation.

Coroutine cancellation and host cancellation are distinct:

- Without an abort hook, cancellation stops result consumption but the host operation may continue.
- With a real abort hook, cancellation invokes that hook at most once.

Promise and event adaptation remain future, use-case-driven work, except for the one event-shaped capability that has a use case: app lifecycle state. `MiniAppLifecycle` publishes the current state and a `Flow` of later changes, which is the only stream abstraction the SDK introduces.

The first concrete capability follows this path:

```text
MiniAppStorage
    ↓ provided by
WechatHost
    ↓
WechatStorage adapter
    ↓
awaitHostCallback + WeChat error mapping
    ↓
typed storage interop
    ↓
wx.getStorage / wx.setStorage / wx.removeStorage
```

The common contract intentionally stores strings only. Object serialization, secure storage, cloud storage, and host-specific options are outside this capability.

The WeChat-specific authentication bootstrap uses a separate path:

```text
WeChatLoginResult
    ↑ returned by
WechatAuth adapter
    ↓
awaitHostCallback + WeChat error mapping
    ↓
typed login interop
    ↓
wx.login
```

WeChat also exposes `wx.checkSession`, which reports whether the client login state WeChat issued is still within the lifetime WeChat defines. It is modelled as a WeChat-specific result rather than a common authentication capability, because no other host has been shown to share those semantics and a generic `isAuthenticated()` would promise a guarantee the API cannot give. It is a query: an invalid answer acquires no code, exchanges no session, and refreshes no token, and a consumer decides for itself whether to call the existing login flow again.

A valid session check is not identity. It does not mean a user is authenticated, that a consumer backend session is valid, that a `session_key` is still accepted by a consumer backend, or that an access token is valid. Only the login-code path through the consumer's backend establishes a trusted identity, exactly as it did before this capability existed.

The login code is a short-lived client credential, not a trusted user identity, SDK session, or access token. Exchanging it with WeChat and establishing a trusted application session are consumer-backend responsibilities. The SDK does not log, persist, exchange, or refresh the code.

The first common capability that sends data to the host follows the shared path:

```text
MiniAppHttpTransport
    ↓ provided by
WechatHost
    ↓
WechatNetwork adapter
    ↓
awaitHostCallback + WeChat error mapping
    ↓
typed request interop
    ↓
wx.request
```

An exchange that completes is a response for every status, including `4xx` and `5xx`; an HTTP status describes the exchange rather than the SDK's ability to perform it. Only a transport-level failure becomes an exception: a host timeout maps to `MiniAppException.Timeout`, and every other transport failure maps to `MiniAppException.HostFailure`. `wx.request` returns an abort handle, so this is the first capability where coroutine cancellation also aborts the host operation instead of merely stopping consumption of its callbacks.

The contract carries text only. The adapter therefore requests raw response text and reports a non-text body as `MiniAppException.InvalidResponse` rather than letting the host hand back a parsed object the contract cannot represent. Response headers cross as single string values; a host header reported in any other shape is not represented. Payload serialization, cookies, redirects, streaming, and retry policy remain outside the capability.

## 6. Public Boundary

The project distinguishes two API surfaces:

- Kotlin public API: Kotlin-idiomatic interfaces, models, and behavior used by Kotlin code.
- JavaScript / TypeScript export API: a consumer-facing boundary designed for JavaScript and TypeScript ergonomics and interoperability constraints.

The export surface may adapt the Kotlin API; it must not become the business implementation layer.

The host-neutral export facade belongs under `jsMain/.../export` and may call shared Kotlin APIs from `commonMain`. It must not contain `wx` calls or platform capability implementations. When the compiler-generated CommonJS namespace is unsuitable as the stable consumer ABI, a thin JavaScript wrapper may normalize module and export shape only; it must not contain business logic, host error mapping, or platform behavior.

Kotlin collections cannot cross the `@JsExport` boundary, because a JavaScript caller has no way to construct a Kotlin map. An export that needs key-value data therefore carries it in a primitively representable shape and lets the wrapper restore an object shape; the Kotlin API keeps its idiomatic collection types. The HTTP transport export follows this rule for headers, representing them as a flat sequence of alternating name and value strings.

## 7. Non-goals

- Reimplementing Kuikly
- Building a Compose Mini Program renderer
- Replacing WXML
- Building a Virtual DOM
- Mirroring the complete `wx` API
- Selecting hosts through a central platform enum
- Hiding platform-specific APIs behind misleading common abstractions
