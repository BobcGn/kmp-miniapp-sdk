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

Capability support currently has two states: `Supported` and `Unsupported`. Version- and permission-dependent states are an explicit evolution direction, but will be introduced only when a concrete capability establishes their required data and behavior.

### Runtime families

Mini-app hosts may belong to different runtime families. WeChat-like DSL runtimes and WebView/web runtimes do not share universal assumptions about `window`, DOM, CommonJS, or WXML. CommonJS is the current WeChat distribution strategy, not the permanent ABI of every future host.

### Lifecycle and navigation

App-level lifecycle is a capability. Every mini-app host can say whether the application is in front of the user, so `MiniAppLifecycle` exposes the current state and a stream of changes, and the host adapter republishes the hooks its runtime hands the consumer.

Page-level lifecycle and page-stack navigation are not capabilities. A page, a page route, and a page stack belong to a DSL mini-program runtime, and a WebView host has none of them. `WechatPageLifecycle` and `WechatNavigation` therefore live under `host/wechat` and are reachable only through the platform escape hatch. Describing them as neutral contracts would present WeChat semantics as universal ones.

Lifecycle is forwarded rather than intercepted: WeChat reports lifecycle only to the `App(...)` and `Page(...)` registrations the consumer owns, so the SDK exposes entry points the consumer forwards its hooks into.

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
