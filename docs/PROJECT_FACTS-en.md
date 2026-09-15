# Project Facts

[中文](PROJECT_FACTS-ch.md)

This file is the human-readable source of truth for the current state of kmp-miniapp-sdk.

If this file conflicts with executable configuration or source code, the executable configuration and source code take precedence.

See the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md) for each capability's implementation status, host API, permission prerequisites, and verification level. `Planned`, `Partial`, `Experimental`, `Unsupported`, and `P3-Presentation` entries must not be interpreted as implemented capabilities.

## 1. Project Identity

- Project: `kmp-miniapp-sdk`
- Purpose: use official Kotlin Multiplatform and Kotlin/JS so shared Kotlin logic can be consumed by a WeChat Mini Program JavaScript or TypeScript runtime.
- Kind: SDK / library.

The project is not a WeChat Mini Program UI framework, Kuikly replacement, Compose renderer, Virtual DOM, or WXML replacement.

## 2. Current Status

- Experimental
- Pre-alpha
- Bootstrap completed
- Consumer Bridge completed and verified in WeChat Developer Tools
- Typed WeChat interop foundation implemented
- Host and capability boundary contracts implemented
- Common error model and internal callback-to-coroutine primitive implemented
- Storage capability implemented and verified in WeChat Developer Tools
- WeChat client authentication bootstrap implemented and verified in WeChat Developer Tools
- HTTP transport capability implemented and verified in WeChat Developer Tools
- App-level lifecycle capability implemented, with foreground state and page route verified in WeChat Developer Tools
- WeChat page-stack navigation bridge implemented and verified in WeChat Developer Tools
- Runtime capability detection and version gating implemented and verified in WeChat Developer Tools and on a real device
- Permission lifecycle implemented and verified in WeChat Developer Tools and on a real device

The build baseline and first end-to-end consumer bridge both pass verification. The common layer contains minimal host identity, capability support, and typed platform escape-hatch contracts. Production code invokes the typed WeChat Storage contracts through the Host and adapter boundaries, and the complete Storage path has passed real-host verification.

The first Storage capability implementation passes automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks. User-provided WeChat Developer Tools evidence also verifies read, overwrite, removal, and missing-key behavior in the real runtime.

The WeChat-specific authentication adapter invokes typed `wx.login`, returns `WeChatLoginResult`, and maps host failures through the common error model. It does not establish an authenticated user or session. Automated Kotlin/JS, CommonJS, and TypeScript checks pass, and user-provided WeChat Developer Tools evidence verifies that the real runtime obtains a non-empty login code without exposing it.

The HTTP transport capability defines a host-neutral request and response contract, adapts typed `wx.request` through the WeChat adapter, and maps transport failures and timeouts into the common error model. `wx.request` also returns an abort handle, so this is the first capability whose coroutine cancellation aborts the host operation. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. User-provided WeChat Developer Tools acceptance confirms that the index page's network check runs against the real runtime.

The app-level lifecycle capability models whether the application is in front of the user. It is the only lifecycle concept modelled as common; page-level lifecycle and page-stack navigation are WeChat semantics and stay behind the platform escape hatch. The WeChat adapter republishes the App hooks the consumer forwards. Automated checks pass; on 2026-09-15 the user confirmed that the foreground state and page route passed acceptance in WeChat Developer Tools. The background transition still cannot be triggered from the Developer Tools simulator.

The WeChat navigation bridge adapts `wx.navigateTo`, `wx.redirectTo`, and `wx.navigateBack`, and maps a bounded stack, an unknown route, and going back from the first page to `MiniAppException.HostFailure` rather than reporting success. Automated checks pass; on 2026-09-15 the user confirmed that all three navigation operations passed acceptance in WeChat Developer Tools.

Capability support is answered from the running host rather than from a hardcoded list. `MiniAppHost.capabilitySupport` reports `Supported`, `Unsupported`, `VersionDependent`, or `PermissionDependent`, and `requireSupported` turns every state other than `Supported` into `MiniAppException.UnsupportedCapability`. The WeChat gate reads `wx.canIUse` and the base-library version, with `wx.getAppBaseInfo` preferred and the unmaintained `wx.getSystemInfoSync` as the fallback, so the same build can answer differently on two hosts. Version and platform reads are deferred until their respective first use and cached independently; `wx.canIUse` remains a live query. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. WeChat Developer Tools (base library 3.17.2) and an Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) both verified the `Supported` and `Unsupported` states.

## 3. Current Gradle Modules

Verified with `./gradlew projects`:

```text
Root project 'kmp-miniapp-sdk'
└── Project ':sdk'
```

`examples/` is an integration host directory, not a Gradle module.

## 4. Current Targets

```text
Kotlin Multiplatform
└── JavaScript
```

Node.js is configured only as the local Kotlin/JS build and test environment. It is not the target host. The intended production host is the WeChat Mini Program JavaScript runtime.

No Android, JVM, iOS, Wasm, or browser application target is configured.

## 5. Current JS Strategy

- Kotlin/JS IR compilation through Kotlin 2.4.20
- CommonJS module output via `useCommonJs()`
- Library output via `binaries.library()`
- TypeScript definition generation via `generateTypeScriptDefinitions()`
- Stable compiler module name `kmp-miniapp-sdk-kotlin`
- A minimal `@JsExport` facade under `jsMain/.../export`
- A thin CommonJS normalization wrapper for consumers
- Root `buildMiniAppSdk` task for consumer distribution

The compiler-generated TypeScript declaration contains the version, Promise-based Storage, and Promise-based HTTP transport exports. The compiler CommonJS shape is namespaced, so the consumer-facing wrapper exposes stable flat functions without business logic. `buildMiniAppSdk` synchronizes the compiler module, declaration, runtime dependencies, and external source maps into the WeChat example while preserving that wrapper.

## 6. Current Dependencies

| Use | Dependency | Version source | Version |
| --- | --- | --- | --- |
| Build plugin and Kotlin libraries | Kotlin Multiplatform / Kotlin | `gradle/libs.versions.toml` | 2.4.20 |
| Production | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `gradle/libs.versions.toml` | 1.11.0 |
| Test | `kotlin-test` | Kotlin plugin version | 2.4.20 |
| Test | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `gradle/libs.versions.toml` | 1.11.0 |

Kotlin/JS platform variants and their transitive dependencies are selected by Gradle during dependency resolution.

## 7. Current Package Namespace

The source namespace is `io.github.bobcgn.miniapp`. The shared metadata API is in `io.github.bobcgn.miniapp.api`, the JavaScript export facade is in `io.github.bobcgn.miniapp.export`, and raw WeChat declarations are in `io.github.bobcgn.miniapp.host.wechat.interop`.

## 8. Source Set Responsibilities

### `commonMain`

Allowed responsibilities:

- Platform-neutral APIs and interfaces
- Models and errors
- Shared logic

Forbidden dependencies and constructs:

- `wx`
- `window` or `document`
- DOM APIs
- `dynamic`, `external`, or `js()`
- Node-specific APIs

### `jsMain`

`jsMain` owns JavaScript and WeChat Mini Program integration. Its intended internal boundaries are:

- `host/wechat/interop`: raw JavaScript and `wx` declarations
- `host/wechat/adapter`: platform API to Kotlin API adaptation
- `host/wechat/runtime`: host lifecycle and runtime integration
- `export`: host-neutral Kotlin to JavaScript and TypeScript public boundary

The `export` boundary contains the version facade, Promise-based Storage functions, the WeChat-specific login bootstrap, the Promise-based HTTP transport function, the App and Page lifecycle entry points the consumer forwards into, the WeChat navigation functions, and the capability-support and runtime-inspection queries, and the permission lifecycle functions. The interop boundary contains internal contracts for `login`, `showToast`, Storage, `request`, the three page-stack navigation methods, the runtime-inspection members with their presence guards, and the three permission methods with the raw authorization-map reader; production adapters invoke login, Storage, the HTTP transport, navigation, runtime inspection, and the permission lifecycle through `WechatHost`, while `showToast` remains an interop-only contract. The `runtime` boundary contains `WechatAppLifecycle`, which implements the common lifecycle capability, `WechatPageLifecycle`, which tracks the page the runtime reports as shown, and the capability catalog and gate that decide support from what the runtime reports. The `adapter` boundary additionally holds `WechatPermissionScopes`, the single place a WeChat scope string exists.

## 9. Current Capabilities

- The Kotlin Multiplatform project configures and builds.
- The `:sdk` JavaScript library target compiles.
- Production CommonJS library files are generated.
- TypeScript definition generation and validation are configured.
- `./gradlew buildMiniAppSdk` prepares all compiler-managed artifacts required by the WeChat integration host without manual copying.
- The compiler module has the stable name `kmp-miniapp-sdk-kotlin`; external source maps are copied for local debugging and ignored by Git.
- `MiniAppExports.sdkVersion()` exports the shared `MiniAppSdk.VERSION` value.
- A consumer-facing CommonJS wrapper exposes `sdkVersion(): string` from `examples/wechat-miniprogram/miniprogram/libs`.
- A strict TypeScript consumer and a Node/CommonJS smoke test both pass without using `any` as the consumer contract.
- `examples/wechat-miniprogram` contains a minimal index page that loads the same consumer artifact and displays the version value.
- WeChat Developer Tools loads the artifact, logs `0.1.0-SNAPSHOT`, and displays the same value on the index page.
- A common test runs on the Node.js Kotlin/JS test environment.
- Explicit API mode is enabled.
- `commonMain` defines `MiniAppHost`, `HostPlatformApi`, `HostVersion`, `CapabilityKey`, and the `Supported` / `Unsupported` / `VersionDependent` / `PermissionDependent` capability states, plus the `requireSupported` guard that turns any non-supported state into `MiniAppException.UnsupportedCapability`.
- `jsMain/.../host/wechat/interop` defines the internal global `wx` contract, typed callback results, and typed option bags for the initial toast and storage methods.
- Plain-object factories for those option bags are tested in the Kotlin/JS Node test environment without requiring a real `wx` runtime.
- `commonMain` defines the semantic `MiniAppException` hierarchy without raw JavaScript values.
- The internal `awaitHostCallback` primitive enforces one terminal callback, ignores callbacks after cancellation, and invokes an optional host abort hook at most once.
- The WeChat adapter boundary maps typed raw callback failures to `MiniAppException.HostFailure` while preserving scalar diagnostics.
- `MiniAppStorage` defines string get/set/remove semantics, including overwrite, idempotent removal, and `null` for an absent key.
- `WechatHost` provides `MiniAppStorage` through `StorageCapabilityProvider`; `WechatStorage` connects it to typed WeChat storage interop and coroutine adaptation.
- The JavaScript and TypeScript facade exposes Promise-based `storageGet`, `storageSet`, and `storageRemove` functions.
- Automated tests cover Storage semantics, WeChat adaptation, invalid responses, host errors, cancellation, and CommonJS consumption.
- WeChat Developer Tools verifies Storage read, overwrite, removal, and missing-key behavior; the page reports `PASS` with `first=first, overwritten=second, missing=null`.
- Typed `wx.login` interop covers the option bag, success `code`/`errMsg`, and failure `errMsg`/`errno` shapes.
- `WechatAuth` adapts callbacks into a coroutine result, rejects blank codes, and maps host failures without exposing raw JavaScript values.
- The TypeScript facade exposes `wechatLogin(): Promise<WeChatLoginResult>` without `any`; the example never logs or renders the raw code.
- `MiniAppHttpTransport` defines a host-neutral request and response contract carrying text bodies and string-valued headers, with no payload serialization or retry policy.
- A completed exchange returns a response for every HTTP status, including `4xx` and `5xx`; only a transport-level failure becomes a `MiniAppException`.
- `MiniAppException.Timeout` is distinct from `MiniAppException.HostFailure` and is reported when the host signals that an exchange exhausted its timeout.
- The adapter requests raw response text (`dataType: 'text'`) so the host cannot hand back a parsed object the string contract cannot represent; a non-text body is an `InvalidResponse`.
- `WechatHost` provides `MiniAppHttpTransport` through `NetworkCapabilityProvider`; `WechatNetwork` connects it to typed `wx.request` interop and coroutine adaptation.
- `wx.request` returns an abort handle, so this is the first capability whose coroutine cancellation also aborts the underlying host operation, at most once.
- The typed `wx.request` interop covers the option bag, the success result with `statusCode`/`header`/`data`, the failure result with `errMsg`/`errno`, and the returned task handle.
- The JavaScript and TypeScript facade exposes `networkRequest(url, init): Promise<NetworkResponse>` without `any`.
- Automated tests cover request forwarding, response mapping, header conversion, invalid responses, timeout mapping, host-failure mapping, and host abort on cancellation.
- `MiniAppLifecycle` defines the app-level lifecycle capability: the current `MiniAppLifecycleState` and a `Flow` of later changes. It is the only event-shaped capability and the only lifecycle concept modelled as common.
- `WechatAppLifecycle` implements that contract from `App.onLaunch` and `App.onShow` reporting the foreground and `App.onHide` reporting the background.
- `WechatPageLifecycle` tracks the route the runtime reports as shown. Page-level lifecycle is not a common capability and is reachable only through `WechatPlatformApi`.
- `WechatNavigation` adapts `wx.navigateTo`, `wx.redirectTo`, and `wx.navigateBack`, is reachable only through `WechatPlatformApi`, and reports WeChat's failure callback as `MiniAppException.HostFailure`.
- The JavaScript and TypeScript facade exposes the App and Page lifecycle entry points, the lifecycle state and page route, and the three navigation functions.
- `WechatCapabilityGate` decides support from `wx.canIUse` and the base-library version, preferring `wx.getAppBaseInfo` and falling back to the unmaintained `wx.getSystemInfoSync` on base libraries that predate it. Every runtime-inspection read is guarded, so an absent `wx` yields `Unsupported` rather than a crash.
- `WechatRuntimeInfo` reports the base-library version, the runtime platform, and whether the runtime is Developer Tools. Version and platform are each read and retained on first use; `canIUse` remains live, so constructing the SDK never touches `wx` and support answers are not frozen by the scalar cache.
- The WeChat capability catalog records what each gated capability requires. Storage and network name the schemas `wx.canIUse` must confirm and set no minimum version; the app lifecycle has no host API to probe; runtime detection records the documented `2.20.1` boundary, which is what makes a version-dependent answer reproducible.
- The JavaScript and TypeScript facade exposes `capabilitySupport`, `requireCapability`, `wechatRuntimeInfo`, and `wechatCanIUse` without `any`.
- `MiniAppPermissions` defines the host-neutral permission lifecycle: `PermissionKey` names what a permission is for, `PermissionState` is `NotRequested`, `Granted`, or `Denied`, and the contract offers query, request, and open-settings. `WechatHost` provides it through `PermissionCapabilityProvider`.
- `WechatPermissionScopes` is the single place a WeChat scope string exists. The one mapped permission is the microphone; a key the adapter does not map fails before any host call.
- Nothing is cached: every query and every settings return asks the host, because the user can change a permission in the host's own settings at any time.
- A refusal is `MiniAppException.PermissionDenied` and a host that cannot answer is `HostFailure`; only a failure message that reports a refused authorization becomes a denial.
- Concurrent requests for one permission share a single host call owned by the service, and an already decided permission is answered from the host's state instead of being prompted again.
- The JavaScript and TypeScript facade exposes `permissionState`, `requestPermission`, and `openPermissionSettings` with a stable string state union and no `any`.
- Automated tests cover version comparison and parsing, all four support states, the version boundary itself, fallback when the version is unreadable, a host that cannot be probed at all, the single runtime read, and the `UnsupportedCapability` guard.
- WeChat Developer Tools verifies the foreground lifecycle state, page route, and the complete `navigateTo` → `redirectTo` → `navigateBack` page-stack path.
- [TESTING-en.md](TESTING-en.md) records the two testing layers, the fakes each may use, and the reproducible real-host checklist.
- `commonTest` defines a host-neutral FakeHost boundary: `FakeMiniAppHost`, `InMemoryStorage`, `RecordingHttpTransport`, and `FakeMiniAppLifecycle`.
- `jsTest` defines a FakeAdapter boundary for the raw WeChat callback ports, so a WeChat adapter is driven without a WeChat runtime and without `wx`.
- The storage, transport, and app-lifecycle contracts are each stated once as shared checks and run against both the host-neutral reference implementation and the WeChat adapter.
- No automated test claims access to a real `wx` runtime, and no automated check substitutes for real-host verification.

## 10. Explicit Non-Capabilities

The project currently has no:

- Router or navigation-stack framework
- UI component lifecycle abstraction
- WeChat privacy authorization (`getPrivacySetting`) and the device capabilities it would gate
- Request or response body serialization, cookie handling, redirect policy, streaming, upload, or download
- Server-side code exchange, authenticated user/session management, and token refresh
- A public generic callback-to-coroutine API
- Compose integration, UI DSL, renderer, or Virtual DOM
- npm publication
- Maven publication
- Gradle plugin
- Alipay or Telegram host implementations

## 11. Verified Commands

Verified on 2026-09-15:

| Command | Result |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :sdk:jsNodeTest` | VERIFIED |
| `./gradlew :sdk:jsNodeProductionLibraryDistribution` | VERIFIED |
| `./gradlew buildMiniAppSdk` | VERIFIED — repeat run reused the configuration cache and was up to date |
| `npm run smoke` in `examples/wechat-miniprogram` | VERIFIED |
| `npm run typecheck` in `examples/wechat-miniprogram` | VERIFIED |
| WeChat Developer Tools load, console, page rendering, lifecycle, and navigation | VERIFIED — user confirmed the foreground state, page route, and all three navigation operations |
| WeChat real-device debugging (Android) | VERIFIED — OnePlus PLQ110 / Android 36 / WeChat 8.0.76; Runtime Detection, Storage, Authentication Bootstrap, HTTP Request, and all three navigation operations PASS with `platform=android` |
| Permission in WeChat Developer Tools | VERIFIED — base library 3.17.2; the card showed `Granted` and `Denied`, and the settings visit reported the host's decision |
| Permission on a real device | VERIFIED — the full `Granted` → `Denied` → `DENIED` → `Granted` transition, with no second prompt after the refusal |

Earlier WeChat Developer Tools evidence covers the version, Storage, and authentication checks; the network check was accepted separately on 2026-09-14. On 2026-09-15 the user confirmed real-host acceptance of the lifecycle foreground state, page route, and `navigateTo`, `redirectTo`, and `navigateBack`. The background-state transition is outside what the Developer Tools simulator can verify. The permission lifecycle defines a host-neutral three-state model — `NotRequested`, `Granted`, `Denied` — for a `PermissionKey` that names what a permission is for, and adapts `wx.getSetting`, `wx.authorize`, and `wx.openSetting` through the WeChat adapter. Nothing is cached, a refusal is `MiniAppException.PermissionDenied` rather than a host failure, and requesting a permission or opening settings never happens without a user gesture. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. WeChat Developer Tools (base library 3.17.2) and an Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) verified that the host reports `Granted` and `Denied`, that a settings visit reports the host's decision rather than assuming a grant, and that requesting a refused permission reports `DENIED` without a second prompt. `NotRequested` could not be produced on the account used, because it already holds a decision for the mapped permission; that state is covered by automated tests instead.

Runtime capability detection completed Developer Tools and real-device acceptance on 2026-09-15. Developer Tools reported `baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported` at base library 3.17.2; the Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) reported the same states with `platform=android`. The lowest debug base library WeChat Developer Tools currently offers is 2.21.4, so a host below 2.20.1 cannot be constructed there and `VersionDependent` has no real-host screenshot; that boundary is covered by the `HostVersion` unit tests, the Fake Host contract checks, the boundary test, and the executed mutation probe. 3.17.2 is the current primary compatibility verification version, not a proven minimum supported version. The permission lifecycle completed Developer Tools (base library 3.17.2) and Android real-device acceptance on 2026-09-15, covering `Granted`, `Denied`, the settings return, and a refused request. `NotRequested` was not reproducible on the account used and is covered by automated tests. Only the microphone permission is mapped.

The verified environment used Gradle 9.3.1 from the checked-in Wrapper and JDK 25.0.2. The build does not currently pin a JDK toolchain.

## 12. Current Consumer Bridge Status

The verified end-to-end path is:

```text
Kotlin @JsExport
→ TypeScript declaration
→ CommonJS
→ TypeScript require
→ Node smoke test
→ WeChat Mini Program runtime
```

The same consumer-facing artifact was verified in WeChat Developer Tools. The console and rendered page both show `0.1.0-SNAPSHOT`. The Consumer Bridge milestone is complete.
