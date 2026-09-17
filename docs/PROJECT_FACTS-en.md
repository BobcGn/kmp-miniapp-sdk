# Project Facts

[中文](PROJECT_FACTS-ch.md)

This file is the human-readable source of truth for the current state of kmp-miniapp-sdk.

If this file conflicts with executable configuration or source code, the executable configuration and source code take precedence.

See the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md) for each capability's implementation status, host API, permission prerequisites, and verification level. `Planned`, `Partial`, `Experimental`, `Unsupported`, and `P3-Presentation` entries must not be interpreted as implemented capabilities.

## 1. Project Identity

- Project: `kmp-miniapp-sdk`
- Purpose: a Kotlin Multiplatform client runtime for sharing client behaviour, host capabilities, and presentation state across Mini App platforms, while keeping rendering host-native. It uses official Kotlin Multiplatform and Kotlin/JS so shared Kotlin logic can be consumed by a WeChat Mini Program JavaScript or TypeScript runtime.
- Kind: SDK / library.

The backend owns business truth, Kotlin owns client behaviour, and the host owns rendering. [ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-en.md) fixes the four layers — backend, KMP client runtime, host integration, platform UI — and the responsibility matrix across them; [ARCHITECTURE-en.md](ARCHITECTURE-en.md) carries the matrix.

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
- Privacy authorization implemented and covered by automated checks
- WeChat session check implemented and verified by automated checks and an Android real-device run
- WeChat clipboard read/write and short/long vibration implemented and verified by automated checks, Developer Tools, and an Android real-device run
- WeChat file-system read, write, access, and remove implemented and verified by automated checks, Developer Tools, and an Android device
- One-shot on-demand WeChat location implemented and verified by automated checks, WeChat Developer Tools, and an Android device
- WeChat scanning implemented and verified by automated checks, WeChat Developer Tools, and an Android device
- WeChat media selection implemented and verified by automated checks, Developer Tools, and an Android real-device run
- WeChat subscription-message requests implemented and covered by automated checks; an Android real-device run verified runtime support and the zero-template guard, while prompt outcomes remain blocked on a valid template for the current AppID
- WeChat standard payment implemented and covered by automated checks as a typed forwarder of backend-produced parameters; an Android real-device run verified runtime support and the no-parameters guard, while real payment acceptance is blocked on a legal merchant environment and a trusted backend
- Virtual payment is not implemented: no request, outcome, capability entry, or export exists, and the installed Developer Tools base library contains no discoverable virtual-payment contract. Its boundary is recorded in ARCHITECTURE and its status is `Planned` in the capability matrix
- The `io.github.bobcgn.miniapp` Gradle plugin is implemented and covered by automated checks. Applying it to a Kotlin Multiplatform project provisions `miniappMain` and `miniappTest` as compilation-owned source sets with `commonMain` and `commonTest` as their parents, binds a Node.js test run so `miniappTest` executes tests, adds the runtime SDK to `miniappMain` so the consumer compiles against the public API without declaring an artifact, and exposes `assembleMiniAppBundle`, which republishes the Kotlin/JS production library — the consumer's module and declaration plus the runtime modules it needs — at `build/miniapp/bundle` as a compiler-managed Mini App distribution. It rejects a project that does not apply the Kotlin Multiplatform plugin, and it rejects a Mini App runtime classpath carrying a client renderer. It is configured through a `miniapp { wechat { } }` extension whose only setting is the current host's bundle directory, which defaults to `build/miniapp/bundle`
- A consumer whose `commonMain` depends on another Kotlin Multiplatform project must apply the Mini App plugin to that project as well: the dependency is resolved through a Mini App variant, so a project that offers none produces variant-resolution errors and there is no automatic fallback. Keeping a dependency out of `commonMain` to avoid this is not a supported workaround
- The client/runtime architecture boundary is recorded and enforced: the backend owns business truth, this SDK owns client behaviour and host capabilities, and the host owns rendering. `:kmp-miniapp-sdk:checkArchitectureBoundaries` fails the build when the runtime SDK imports or declares a UI framework namespace or Artifact, and it runs as part of `:kmp-miniapp-sdk:check`

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
+--- Project ':miniapp-gradle-plugin'
\--- Project ':kmp-miniapp-sdk'
```

`:kmp-miniapp-sdk` is the Kotlin/JS runtime SDK described by the rest of this document; its sources live in `sdk/`, while its Gradle project name — and therefore its published artifact id — is `kmp-miniapp-sdk`, which is what composite-build substitution matches. `:miniapp-gradle-plugin` is the `io.github.bobcgn.miniapp` Gradle plugin. It owns build integration and developer experience only: it detects the Kotlin Multiplatform plugin, registers the `miniapp` platform target chosen in [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) together with that target's Node.js test run, adds the runtime SDK to `miniappMain`, and fails with a named error when the Kotlin Multiplatform plugin is absent. Registering the target is what makes `miniappMain` and `miniappTest` real compilation-owned source sets with the `commonMain` / `commonTest` edges; the test run is what gives `miniappTest` a task that executes; the dependency is what lets a consumer write `miniappMain` code against the published API without naming an artifact. It also exposes `assembleMiniAppBundle`, which republishes the Kotlin/JS production library at a stable path a host integration can depend on, and a `miniapp { wechat { } }` extension that configures that path. The plugin contains no WeChat runtime code.

The runtime coordinate the plugin adds is `io.github.bobcgn:kmp-miniapp-sdk:<version>`. `gradle/libs.versions.toml` is the single source of that version: the plugin's coordinate is generated into it as a resource, and the SDK's own `MiniAppSdk.VERSION` is generated from the same place, so neither can drift away from the published version. The plugin adds it to `miniappMain` only, and `miniappTest` inherits it through the source-set hierarchy. The SDK is not published to any repository yet, so today that coordinate resolves through a composite build; publication would resolve the same coordinate from a repository.

`examples/` is an integration host directory, not a Gradle module.

`fixtures/miniapp-consumer` is the consumer integration fixture: an ordinary Gradle build beside this one, not a module of it, which consumes the plugin and the runtime the way an external project does. It has its own WeChat host under `host/`. Its `miniappMain` / `miniappTest`, its runtime dependency and its bundle all come from the plugin.

`poc/kgp-model` is the BOB-83 Kotlin Gradle Plugin model PoC. It is a standalone Gradle build that the root build does not include, so it contributes no module, target, or dependency to the SDK. Its decision is recorded in [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md).

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

The `export` boundary contains the version facade, Promise-based Storage functions, the WeChat-specific login bootstrap, the Promise-based HTTP transport function, the App and Page lifecycle entry points the consumer forwards into, the WeChat navigation functions, and the capability-support and runtime-inspection queries, the permission lifecycle functions, the WeChat-specific session check, the four WeChat clipboard and haptic functions, the five WeChat file-system functions, the WeChat location function, the WeChat scan function, the WeChat media-selection function, the WeChat subscription-request function, the network status functions, and the two WeChat transfer functions. The interop boundary contains internal contracts for `login`, `showToast`, Storage, `request`, the three page-stack navigation methods, the runtime-inspection members with their presence guards, and the three permission methods with the raw authorization-map reader; production adapters invoke login, Storage, the HTTP transport, navigation, runtime inspection, and the permission lifecycle through `WechatHost`, while `showToast` remains an interop-only contract. The `runtime` boundary contains `WechatAppLifecycle`, which implements the common lifecycle capability, `WechatPageLifecycle`, which tracks the page the runtime reports as shown, and the capability catalog and gate that decide support from what the runtime reports. The `adapter` boundary additionally holds `WechatPermissionScopes`, the single place a WeChat scope string exists.

## 9. Current Capabilities

- The Kotlin Multiplatform project configures and builds.
- The `:kmp-miniapp-sdk` JavaScript library target compiles.
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
- `WechatPermissionScopes` is the single place a WeChat scope string exists. Microphone and location are mapped; a key the adapter does not map fails before any host call.
- Nothing is cached: every query and every settings return asks the host, because the user can change a permission in the host's own settings at any time.
- A refusal is `MiniAppException.PermissionDenied` and a host that cannot answer is `HostFailure`; only a failure message that reports a refused authorization becomes a denial.
- Concurrent requests for one permission share a single host call owned by the service, and an already decided permission is answered from the host's state instead of being prompted again.
- The JavaScript and TypeScript facade exposes `permissionState`, `requestPermission`, and `openPermissionSettings` with a stable string state union and no `any`.
- `WeChatSessionState` reports `VALID` or `INVALID` for `wx.checkSession`. It is WeChat-specific and namespaced (`wechat.check-session`) rather than a host-neutral authentication capability, because no other host is known to share these semantics.
- The `wx.checkSession` success/fail callbacks map directly to `VALID`/`INVALID`; the adapter does not parse an `errMsg` that can vary by language or base library.
- The session check is a query only: an `INVALID` result acquires no code, exchanges no session, refreshes no token, and retries nothing. A valid result means WeChat's client login state is intact, not that a user is authenticated or a backend session is valid.
- The JavaScript and TypeScript facade exposes `wechatCheckSession` with a `Valid` / `Invalid` union and no `any`.
- `WeChatDeviceCapabilities` names four WeChat-specific device capabilities: `wechat.clipboard-read`, `wechat.clipboard-write`, `wechat.vibrate-short`, and `wechat.vibrate-long`. Each is gated separately, because a host may expose one clipboard direction or one vibration length without the other.
- `WechatClipboard` reads and writes system clipboard text. An empty clipboard reads as an empty string; a missing or non-string answer is `InvalidResponse`; neither direction stores what it reads.
- `WechatHaptics` performs the short and long vibrations as two independent calls. Neither reports an intensity, and neither claims a device vibrated.
- The clipboard APIs are gated at base library 1.1.0 and the vibration APIs at 1.2.0, each also probed through `wx.canIUse`. `wx.vibrateShort`'s optional `type` field (heavy / medium / light, base library 2.13.0) is deliberately not modelled.
- The JavaScript and TypeScript facade exposes `wechatGetClipboardText`, `wechatSetClipboardText`, `wechatVibrateShort`, and `wechatVibrateLong` without `any`.
- `WechatFileSystem` reads and writes UTF-8 text in the mini program file sandbox and tests and removes sandbox paths. It offers no directory, stream, descriptor, database, or secure-storage operation.
- The five file-system keys (`wechat.filesystem-read`, `-write`, `-access`, `-remove`, `-sandbox-path`) are gated separately. The manager is a host object, so each operation is reported supported only when that method is present.
- The file-system APIs are gated at base library 1.9.9, and the capability catalogue carries a presence probe for host-object members that `wx.canIUse` cannot answer for.
- `access` returns `false` only for the failure text WeChat documents for a missing path; every other failure raises, so a permission error is never reported as a missing file.
- The JavaScript and TypeScript facade exposes `wechatUserDataPath`, `wechatReadTextFile`, `wechatWriteTextFile`, `wechatFileExists`, and `wechatRemoveFile` without `any`.
- `WechatLocation` reads the device's position once; it does not watch, cache, or upload one. It is reachable only through `WechatPlatformApi`.
- `WeChatCoordinateSystem` names the two systems WeChat can answer in. The SDK always sets one and defaults to `gcj02`, because that is what WeChat's own map views accept; it never takes the host default silently.
- `PermissionKey.Location` is the host-neutral permission, mapped to `scope.userLocation` inside the single adapter mapping point.
- The adapter queries and enforces both the host privacy contract and location permission before calling `getLocation`; neither check presents UI, accepts a contract, or requests permission on the user's behalf.
- The location API records no minimum base library because WeChat's page states none; `wx.canIUse` is the authority.
- The JavaScript and TypeScript facade exposes `wechatGetCurrentLocation` with a `GeoPosition` and a `'wgs84' | 'gcj02'` union, without `any`.
- `WechatScanCode` turns one user gesture into one scan of WeChat's own interface; it does not retry, parse, cache, or upload what was scanned. It is reachable only through `WechatPlatformApi`.
- `WeChatScanCategory` names the four coarse categories a request may ask for (`barCode`, `qrCode`, `datamatrix`, `pdf417`), while `WeChatScanFormat` names the specific format the host reports in its answer. The vocabularies differ and the type names are deliberately not interchangeable; an empty category list means "no restriction" rather than "none".
- A device run proved that WeChat uses the same signal for dismissing the scan interface and for system camera access preventing it from opening. The two exact host messages therefore map to `MiniAppException.HostInteractionInterrupted` without inferring user cancellation; other messages remain `HostFailure`.
- A scan answer's `result` must be present and a string or it maps to `InvalidResponse`; a descriptive field the host omitted stays absent rather than becoming an empty string, and one it reported with the wrong type is not dropped either.
- A scan asks for no permission at all: `wx.scanCode` drives WeChat's own interface, no permission precondition for it could be established from the host contract, so the SDK neither prompts for one nor maps it to `scope.camera`.
- `WechatChooseMedia` turns one user gesture into one selection through WeChat's own picker; it does not retry, parse, store, or upload what was chosen, and it is reachable only through `WechatPlatformApi`.
- The request validates what the SDK can state exactly and refuses the rest: `mediaType` is required because WeChat's own schema marks it so, `count` must be at least 1, and `maxDurationSeconds` must be within the 3-to-60 range WeChat documents. Invalid input is rejected rather than quietly corrected, and the host's own count limit is left to the host because it depends on its base library.
- `WeChatMediaType` (`image`, `video`, `mix`), `WeChatMediaSource` (`album`, `camera`), `WeChatMediaSizeType`, and `WeChatCameraPosition` name the request vocabularies, while `WeChatMediaFileType` names the kind the host reports back. `sizeType` is documented as effective only for images, and `camera` only when the camera is among the requested sources; the SDK forwards both rather than second-guessing the host.
- A selection's `tempFilePath` must be a non-empty string and its `size` a whole, non-negative number of bytes, or the answer is `InvalidResponse`. A video's `duration`, `width`, `height`, and `thumbTempFilePath` stay absent when the host did not report them, because fabricating zero would claim an image is zero seconds long and has no pixels.
- A kind the SDK does not recognize is reported rather than failing the selection, and the host's own name is kept alongside it.
- Media selection asks for no permission at all: WeChat's own picker needs none, and the host's scope list holds no scope for reading the media library, so there is nothing the SDK could map even if it tried.
- The temporary paths a selection returns belong to the host and to the session that produced them. The SDK keeps no copy, makes no claim about their lifetime, and does not copy the media anywhere on the caller's behalf.
- `WechatRequestSubscribeMessage` turns one user gesture into one request about a list of message templates; it never sends a message, never manages a template, and is reachable only through `WechatPlatformApi`. It is deliberately not a general push-notification capability: subscribing to a WeChat template is a WeChat concept no other host has been shown to share.
- The caller owns the user gesture. WeChat requires it, so the adapter never requests on load, never supplies a gesture, and never retries a refusal that followed none.
- A template id must be non-blank, and duplicates are collapsed with the caller's order kept. No count limit is enforced, because no offline source for this API states one and this repository does not encode a number it cannot cite.
- The answer is read by template id, not by position, and the host's own `errMsg` status line is excluded before anything else is treated as a template: reading it as one would invent a template the caller never asked about.
- A valid result holds exactly one entry per requested template, in caller order. Missing, unexpected, blank, or non-text entries are `InvalidResponse`, because accepting them would lose correlation between the request and its answer.
- `WeChatSubscriptionStatus` names only `accept`, and deliberately little else: the offline sources for this API name no status vocabulary at all. The one status string observed anywhere in the installed base library is `accept`, in the simulator's prompt-preview payload, which is prompt-rendering data rather than an API result. Every other status is preserved verbatim in `hostStatus` with `status` left `null`, so a caller acts on what the host actually said and a real-device run can extend the set from evidence.
- Acceptance is a subscription state, not delivery. Nothing in the SDK reports a message as sent, and end-to-end delivery needs a WeChat backend template and a trusted backend, which is a `BackendRequired` concern outside this capability.
- A subscription request queries and requests no permission: the offline sources for this API name no scope for it. That is a statement about the evidence, and the SDK neither assumes BOB-60's unfinished privacy work applies nor assumes it does not.
- Network status is a common capability, because every host can say whether it is online and over what kind of link and the model needs nothing host-specific to say it. The vocabulary is not common, so `NetworkType` names what the SDK recognizes and `hostNetworkType` always carries the host's own word; a kind this SDK has never seen is reported rather than mapped to `UNKNOWN`.
- The query and the listener are separate capability keys (`network-status-query`, `network-status-listener`), because a host may answer the question without offering change events.
- `MiniAppNetworkStatus.changes` forwards what the host pushes. Nothing polls, no reading is synthesized on collection, each collector registers its own host listener and removes it exactly once when it ends, and an event arriving after that end is not delivered.
- Upload and download stay behind the WeChat escape hatch under `wechat.upload-file` and `wechat.download-file`. What is portable about them — method, URL, headers, status, text body — is already the common HTTP transport capability; what is not portable is the file, which is a host path, and this SDK deliberately has no portable file reference. Each direction is gated on its own API.
- A transfer reports the host's own task: `abort()` stops it (at most once, and it says whether a host abort was actually invoked), `lastProgress()` reports the last figure the host gave rather than an estimate, and cancelling the wait stops the host operation too. The progress listener is registered at most once and removed on every terminal path, including an abort.
- A completed transfer is a completed exchange: a non-2xx status resolves, exactly as it does for the HTTP transport capability, and only a transport-level failure rejects. An expired timeout is the exact host message `uploadFile:fail timeout` or `downloadFile:fail timeout`, which the base library's own error mapping produces; a message that merely mentions a timeout stays a `HostFailure`.
- The network extensions query and request no permission. Reaching a host requires it to be in the consumer's request domain list, which is a configuration constraint on the mini program rather than a permission the SDK could ask for, and no offline source ties a privacy condition to any of these APIs either.
- `wechatRequestPayment()` forwards exactly the five parameters a trusted backend produced — `timeStamp`, `nonceStr`, `package`, `signType`, and `paySign` — to WeChat's own payment interface and reports only that the host said the interaction completed. The SDK computes no signature, holds no merchant key, obtains no prepay identifier, and has no order model. A blank field is refused before the host is called, and `signType` is a closed set of the two values the base library accepts.
- Nothing about a payment is logged, cached, or persisted by the SDK: `nonceStr`, `package`, and `paySign` are credentials for one interaction. The exported result carries only `interactionCompleted`, so a resolved call cannot be misread as a paid order, and the SDK never names an outcome `paid`, `settled`, or `confirmed`.
- A payment asks for no permission: the offline sources name no scope and no privacy condition for this API. Whether a merchant is configured, an order exists, or the parameters are acceptable are answers the host gives when it is called, so none of them is reported as an unsupported host.
- The exact message `requestPayment:cancel` — the form the installed base library's own payment flow produces — maps to `HostInteractionInterrupted`, and the SDK does not claim the user cancelled. `requestPayment:fail cancel` and every other message stay a `HostFailure` until a legal merchant environment provides evidence for a real dismissal.
- Virtual payment is not implemented: no request, outcome, capability entry, or export exists for it. Probing the same offline base library finds `requestPayment` seven times — including its `canIUse` metadata entry — and `requestVirtualPayment` zero times anywhere in the file, so that source cannot provide positive capability evidence. The probe did not execute a runtime capability check. These are facts about one offline source, not about real WeChat clients: the parameter table, minimum base library version, platform availability, and account or category eligibility are all unestablished, and the capability stays `Planned` in the capability matrix with nothing registered in the SDK. [ARCHITECTURE-en.md](ARCHITECTURE-en.md) fixes the boundary a future implementation must respect — its own key, its own request and outcome models, no fallback between the two payment products, and no client-side signing, key, session, or order.

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
- A map SDK, map UI, background or continuous location, `startLocationUpdate`, `onLocationChange`, geofencing, track recording, reverse geocoding, third-party map services, the native `map` component, position caching, position upload, or `chooseLocation` and `openLocation`
- A file system beyond UTF-8 text in the mini program sandbox: no directories, directory traversal, recursive removal, streams, file descriptors, random access, file watching, databases, secure storage, or binary and base64 file contents
- Node `fs` or any POSIX file abstraction
- Device capabilities gated by privacy authorization that this SDK does not implement, such as Media and Bluetooth
- A native camera component, continuous visual recognition, a self-built QR or barcode parser, a general camera UI, or any business parsing, persistence, or upload of what was scanned
- A player, editor, compressor, or transcoder for the selected media; image recognition; upload, download, or backend storage; long-term management of the host's temporary files; a complete wrapper of WeChat's media APIs; or the media picker's own interface
- A general push-notification abstraction; sending subscription messages from a backend; template management; silent or load-time subscription requests; or any claim that an accepted subscription means a message was or will be delivered
- A general HTTP client with retry, caching, or authentication; a resume, cache, or download manager; reading, moving, unpacking, or parsing downloaded content; a WebSocket client; or any claim that a download result outlives the session
- Request or response body serialization, cookie handling, redirect policy, streaming, upload, or download
- Client-side signing, merchant keys or certificates, order creation or any order model, refunds, reconciliation, or the `requestVirtualPayment` boundary; and any client-side claim that a completed payment interaction means an order is paid
- Server-side code exchange, authenticated user/session management, and token refresh
- A public generic callback-to-coroutine API
- Compose integration, UI DSL, renderer, or Virtual DOM
- A Presentation Core: `UiState`, `Action`, `Store`, `Effect` and state machines have a defined owner (a future `presentation` module, P2) and no implementation, module, or placeholder type exists
- A Mini App renderer, view tree, layout engine, Kotlin view DSL or WXML generator; `miniappMain` and `jsMain` bind host state and host events, and never render
- A Mini App host bundle that can carry a client renderer: `checkMiniAppHostBoundary` fails the build when the `miniappRuntimeClasspath` carries Compose, Skiko, a Compose-specific AndroidX integration artifact, `kotlinx-browser` or `kotlinx-html`. Non-Compose lifecycle, saved-state and navigation primitives remain allowed shared behaviour. A consumer's `miniappMain` may inherit shared behaviour and state, but the Compose UI must live in a client module or source set that is not a parent of `miniappMain`
- npm publication
- Maven publication
- A Gradle DSL that carries host business configuration. The `miniapp { }` extension exists and carries exactly one build setting — the WeChat host's bundle directory — and deliberately nothing else: no application identifiers or secrets, merchant or payment material, order or signature data, API tokens, template identifiers, presentation state, markup or view definitions. The plugin assembles a compiler-managed Mini App distribution but performs no upload, publication or host deployment. Its consumer fixture bundle has completed WeChat Developer Tools acceptance at base library 3.17.3; that result does not turn deployment into a plugin responsibility
- A second Mini App host. Only WeChat exists; the extension is shaped so that another host is an added `MiniAppHostConfiguration` subtype and an added accessor, not a widened WeChat type
- Alipay or Telegram host implementations

## 11. Verified Commands

Verified on 2026-09-15:

| Command | Result |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :kmp-miniapp-sdk:jsNodeTest` | VERIFIED |
| `./gradlew :kmp-miniapp-sdk:jsNodeProductionLibraryDistribution` | VERIFIED |
| `./gradlew buildMiniAppSdk` | VERIFIED — repeat run reused the configuration cache and was up to date |
| `npm run smoke` in `examples/wechat-miniprogram` | VERIFIED |
| `npm run typecheck` in `examples/wechat-miniprogram` | VERIFIED |
| WeChat Developer Tools load, console, page rendering, lifecycle, and navigation | VERIFIED — user confirmed the foreground state, page route, and all three navigation operations |
| WeChat real-device debugging (Android) | VERIFIED — OnePlus PLQ110 / Android 36 / WeChat 8.0.76 / base library 3.17.3 [1641]; Runtime Detection, Storage, Authentication Bootstrap, HTTP Request, Clipboard read/write, and short/long vibration PASS with `platform=android` |
| Clipboard in WeChat Developer Tools | VERIFIED — base library 3.17.2; write PASS and read-back `matched=true` |
| Permission in WeChat Developer Tools | VERIFIED — base library 3.17.2; the card showed `Granted` and `Denied`, and the settings visit reported the host's decision |
| Permission on a real device | VERIFIED — the full `Granted` → `Denied` → `DENIED` → `Granted` transition, with no second prompt after the refusal |

Verified on 2026-09-17 for the BOB-83 Kotlin Gradle Plugin model PoC, from `poc/kgp-model`:

| Command | Result |
| --- | --- |
| `../../gradlew --no-daemon --console=plain clean check` | VERIFIED — BUILD SUCCESSFUL across all four PoC modules |
| `../../gradlew --no-daemon --console=plain :model-c:miniappTest` | VERIFIED — `miniappNodeTest` executed 4 tests with no failures |
| `../../gradlew --no-daemon --console=plain :model-c:checkMiniAppModel` | VERIFIED — all 10 model requirements pass |
| `../../gradlew --no-daemon --console=plain --configuration-cache :model-c:check` | VERIFIED — configuration cache entry stored, then reused |
| `./gradlew --no-daemon --console=plain clean check` at commit `6812814` | VERIFIED — `:kmp-miniapp-sdk:jsNodeTest` ran 598 tests with no failures |

Verified on 2026-09-17 for the Mini App Gradle plugin skeleton:

| Command | Result |
| --- | --- |
| `./gradlew --no-daemon --console=plain clean check` | VERIFIED — BUILD SUCCESSFUL across `:kmp-miniapp-sdk` and `:miniapp-gradle-plugin` |
| `./gradlew --no-daemon --console=plain :miniapp-gradle-plugin:test` | VERIFIED — 29 tests, no failures (20 Gradle TestKit, 9 contract) |
| `./gradlew --no-daemon --console=plain verifyMiniAppGradlePluginIntegration` | VERIFIED — the plugin suite, `:kmp-miniapp-sdk:checkArchitectureBoundaries` and the consumer fixture pass, and a second run reuses its configuration cache |
| `./gradlew --no-daemon --console=plain :kmp-miniapp-sdk:checkArchitectureBoundaries` | VERIFIED — the SDK imports and declares no UI framework |

Earlier WeChat Developer Tools evidence covers the version, Storage, and authentication checks; the network check was accepted separately on 2026-09-14. On 2026-09-15 the user confirmed real-host acceptance of the lifecycle foreground state, page route, and `navigateTo`, `redirectTo`, and `navigateBack`. The background-state transition is outside what the Developer Tools simulator can verify. The permission lifecycle defines a host-neutral three-state model — `NotRequested`, `Granted`, `Denied` — for a `PermissionKey` that names what a permission is for, and adapts `wx.getSetting`, `wx.authorize`, and `wx.openSetting` through the WeChat adapter. Nothing is cached, a refusal is `MiniAppException.PermissionDenied` rather than a host failure, and requesting a permission or opening settings never happens without a user gesture. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. WeChat Developer Tools (base library 3.17.2) and an Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) verified that the host reports `Granted` and `Denied`, that a settings visit reports the host's decision rather than assuming a grant, and that requesting a refused permission reports `DENIED` without a second prompt. `NotRequested` could not be produced on the account used, because it already holds a decision for the mapped permission; that state is covered by automated tests instead.

The privacy authorization capability adapts typed `wx.getPrivacySetting` and `wx.requirePrivacyAuthorize` through the WeChat adapter and reports what the host requires for its own privacy contract. It is deliberately separate from permissions: the two share no state, and a privacy refusal is `MiniAppException.PrivacyAuthorizationRequired` rather than a `PermissionDenied`. The queryable requirement, the result of one attempt, and SDK errors are modelled separately. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. Real-host privacy acceptance has not been performed: the requirement depends on the mini program's backend privacy configuration and on the user, so only a Developer Tools and real-device run can settle it. `NotRequested`-style proof of consent is not available in either case, because the host reports no requirement both when the user accepted the contract and when the mini program declares no collection.

The WeChat session check adapts typed `wx.checkSession` through the auth chain and reports `WeChatSessionState.VALID` or `INVALID`. It is deliberately WeChat-specific rather than a host-neutral authentication concept, and it is a query only: an invalid result acquires no code, exchanges no session, and refreshes no token. Its result says only that WeChat's own client login state is intact — not that a user is authenticated, not that a consumer backend session is valid, and not that any credential is still accepted. The success/fail callbacks map directly to `VALID`/`INVALID` under the WeChat contract; raw `errMsg` text is not parsed. Automated checks pass; an Android real-device run verified `INVALID` before a new code was acquired and `VALID` after `wx.login` succeeded.

The WeChat clipboard and vibration capabilities adapt typed `wx.getClipboardData`, `wx.setClipboardData`, `wx.vibrateShort`, and `wx.vibrateLong`. They are WeChat-specific device capabilities rather than host-neutral ones, so each is gated separately under a namespaced key (`wechat.clipboard-read`, `wechat.clipboard-write`, `wechat.vibrate-short`, `wechat.vibrate-long`) and reachable through the platform escape hatch. A clipboard read returns the text as-is, including an empty string, and reports a missing or non-string answer as `InvalidResponse` rather than coercing it. A resolved vibration call means only that WeChat accepted it; the tester holding the device confirms physical vibration. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. Developer Tools and an Android device verified clipboard read/write, and the Android device verified both short and long vibration.

The WeChat file-system capability adapts typed `wx.getFileSystemManager` and its `readFile`, `writeFile`, `access`, and `unlink` methods, working only on UTF-8 text inside the mini program file sandbox. It is WeChat-specific, so each operation is gated separately under a namespaced key (`wechat.filesystem-read`, `-write`, `-access`, `-remove`), together with the sandbox root (`wechat.filesystem-sandbox-path`) that the caller builds paths from. The manager is a host object, so a capability is reported supported only when that method is present, not merely when the manager is. An empty file reads as an empty string; binary content is `InvalidResponse`; a path the host reports as missing makes `exists` return `false`, while any other failure raises so a permission error is never reported as a missing file. Removing a file that is not there fails, because that is what WeChat's `unlink` does. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. WeChat Developer Tools and Android-device evidence from 2026-09-15 verified the full write, matching read, exists, remove, and absent-after-remove path at base library 3.17.2.

The WeChat location capability adapts typed `wx.getLocation` and reports `latitude`, `longitude`, and `accuracyMeters` in the coordinate system the caller asked for. It is WeChat-specific, because the coordinate system (`wgs84` or `gcj02`) is a local mapping concept, so it lives behind the platform escape hatch under `wechat.location` and is exported as `wechatGetCurrentLocation`. Only `getLocation` is implemented; `chooseLocation` and `openLocation` are not. The host also reports altitude, vertical and horizontal accuracy, and speed, and those are deliberately not modelled: Android reports `0` for vertical accuracy when it cannot obtain one, which is indistinguishable from a real zero, and nothing in the SDK needs them. An answer whose fields are missing, not numbers, not finite, or outside the valid coordinate range is `InvalidResponse` rather than a defaulted zero, which would be a real but wrong position. Location availability, the `scope.userLocation` permission, and the host's privacy contract are three separate questions: the gate answers only the first and the permission lifecycle answers the second; the adapter queries and enforces the latter two before calling the host but never starts either prompt itself. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. The 2026-09-15 Developer Tools and Android-device runs covered `Supported`, `NotRequested`, `Granted` after an explicit request, blocking while `Denied`, and successful location again after permission recovery; logs report only coordinate and accuracy validity and never the coordinates themselves.

The WeChat scan capability adapts typed `wx.scanCode`, turning one user gesture into one scan of WeChat's own interface. It is WeChat-specific, lives behind the platform escape hatch under `wechat.scan-code`, and is exported as `wechatScanCode`. The request models camera-only operation and the documented category set; the result preserves decoded content and descriptive host fields, rejects invalid shapes, and preserves unknown format names. An Android device showed that dismissing the interface and system camera access preventing it from opening both produce the same cancel signal. The two exact observed messages therefore map to `HostInteractionInterrupted`, not `UserCancelled` or `PermissionDenied`; other messages remain `HostFailure`. A scan queries and requests no SDK permission because no reliable contract ties `scope.camera` to `wx.scanCode`. Automated checks pass and real-host runs cover Supported, success, and an interruption whose cause cannot be attributed. The capability is recorded `Stable` because the matrix requires implementation, automated tests, and the required host-verification evidence, all of which it has; the host's ambiguity is preserved explicitly in the error model rather than presented as a distinction the host does not make.

The WeChat media capability adapts typed `wx.chooseMedia`, turning one user gesture into one selection through WeChat's own picker. It is WeChat-specific, lives behind the platform escape hatch under `wechat.choose-media`, and is exported as `wechatChooseMedia`. The request models only the options there is reason to expose — media type, count, source, longest recording, compression, and camera — and validates what it can state exactly: WeChat's own schema marks `mediaType` required and documents `maxDurationSeconds` as 3 to 60 seconds, so neither an empty media-type list nor an out-of-range duration is sent. It does not invent the host's count limit, because that depends on the host's base library. The result reports each file's temporary path, byte count, required host kind, and whatever video metadata the host reported; a kind the SDK does not know is preserved under the host's own name rather than failing the selection, a descriptive field the host omitted stays absent rather than becoming zero, and an empty success result, a missing required field, or a byte count that is not a non-negative JavaScript safe integer is `InvalidResponse`. Developer Tools reports a simulated dismissal as `chooseMedia:cancel`, while an Android device reports a manual dismissal as `chooseMedia:fail cancel`; both exact signals map to `HostInteractionInterrupted` without inferring user intent, and every other failure remains `HostFailure`. Media selection queries and requests no SDK permission. The paths a selection returns are host temporary resources: the SDK keeps no copy, makes no claim about their lifetime, and does not copy the media anywhere on the caller's behalf. Automated checks pass, and real-host runs cover capability support plus image, video, mixed, camera, manual-dismissal, and restricted-media-access paths. On the verified Android host, manual dismissal and restricted access produce the same indeterminate interruption, so the SDK explicitly preserves that host ambiguity. The capability is `Stable`.

The WeChat subscription capability adapts typed `wx.requestSubscribeMessage`, turning one user gesture into one request about a list of message templates. It is WeChat-specific, lives behind the platform escape hatch under `wechat.request-subscribe-message`, and is exported as `wechatRequestSubscribeMessage`. **Its evidence is the thinnest in the project.** The base library shipped with the installed Developer Tools declares the API in its metadata table — option `tmplIds`, and a success result keyed by template id with `errMsg` beside it — but ships no implementation of it and no documentation schema, so nothing in the offline sources names a status vocabulary or the dismissal message for this API. The only status string observed anywhere in that bundle is `accept`, from the simulator's subscription-prompt preview payload, which is prompt-rendering data rather than an API result. The SDK therefore recognizes exactly that one status and preserves every other non-blank string verbatim. It does not classify any dismissal signal until one is observed for this API; conventional cancel-looking failures remain `HostFailure`. A valid result must contain exactly the requested template keys, each with a non-blank text status, or it is `InvalidResponse`. Acceptance is a subscription state, never delivery, and end-to-end delivery is `BackendRequired`. No permission is mapped and no `app.json` declaration is added, because no citable source ties a scope or a privacy condition to this API — which is a conclusion about the evidence, not a finding that none exists. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. On 2026-09-16 an Android real-device run (OnePlus PLQ110, Android 36, WeChat 8.0.76, runtime base library 3.17.2) verified `wechat.request-subscribe-message=Supported` and `NOT CONFIGURED templateCount=0`; this deliberately makes no host call, so no consent prompt is expected. Prompt acceptance, refusal, dismissal, multi-template correlation, and delivery remain unverified and blocked on a valid subscription template configured for the same AppID, so the capability remains `Partial`.

The WeChat network extensions adapt five host APIs. Network status is the one that became a common capability, because the question it answers is universal and its model needs no host-specific part; the two transfers stayed WeChat-specific, because their request and result are built on host file paths and this SDK deliberately has no portable file reference — the portable half of an HTTP exchange is already the transport capability. The contract was read from the base library shipped with the installed Developer Tools: `getNetworkType` enumerates `wifi`, `2g`, `3g`, `4g`, `5g`, `unknown`, and `none`; the change event enumerates the same set without `5g`; `uploadFile` takes `url`, `filePath`, `name`, `header`, `formData`, and `timeout` and answers with `data` and `statusCode`; `downloadFile` takes `url`, `header`, `timeout`, and `filePath` and answers with `tempFilePath`, `filePath`, and `statusCode`; both tasks carry `abort`, `onProgressUpdate`, and `offProgressUpdate` (which `RequestTask` does not); and the same library's error mapping produces `<api>:fail timeout`. No introduction base library version is stated for any of them, so each relies on `wx.canIUse`. The four capabilities are gated one API at a time, and the listener needs both of its halves so a registration can always be paired with a removal. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks pass. Developer Tools and real-device acceptance is outstanding: a real upload or download needs a controlled HTTPS service in the request domain list, which is `BackendRequired`, and a real network change needs a device whose connection can actually be switched, which no simulator can stand in for. WebSocket is not implemented and remains `Planned` in the capability matrix.

Runtime capability detection completed Developer Tools and real-device acceptance on 2026-09-15. Developer Tools reported `baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported` at base library 3.17.2; the Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) reported the same states with `platform=android`. The lowest debug base library WeChat Developer Tools currently offers is 2.21.4, so a host below 2.20.1 cannot be constructed there and `VersionDependent` has no real-host screenshot; that boundary is covered by the `HostVersion` unit tests, the Fake Host contract checks, the boundary test, and the executed mutation probe. 3.17.2 is the current primary compatibility verification version, not a proven minimum supported version. The permission lifecycle completed Developer Tools and Android-device acceptance for microphone and location on 2026-09-15; the location run additionally covered `NotRequested`, `Granted`, and `Denied`.

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
