# Development

[中文](DEVELOPMENT-ch.md)

## Requirements

- A JDK compatible with the checked-in Gradle 9.3.1 Wrapper. The project does not pin a JDK toolchain; JDK 25.0.2 is the currently verified environment.
- The checked-in Gradle Wrapper. A system Gradle installation is not required or authoritative.
- Kotlin 2.4.20 is resolved by Gradle from the version catalog.

Always invoke Gradle as `./gradlew` on Unix-like systems or `gradlew.bat` on Windows.

## IDE

IntelliJ IDEA is recommended for Kotlin Multiplatform, Gradle Kotlin DSL, and Kotlin/JS development.

VS Code may be useful for files under `examples/wechat-miniprogram`. WeChat Developer Tools is required for the real-host Consumer Bridge validation.

Before starting or closing WeChat capability work, review the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md). It distinguishes implemented, partial, planned, unsupported, and P3 presentation capabilities and records the verification levels required for each entry.

When running WeChat host acceptance, use the [WeChat real-host verification matrix](platforms/wechat/WECHAT_HOST_VERIFICATION-en.md) to select the minimum environment and complete its evidence template. A DeveloperTools result must not replace real-device, permission, privacy, or backend-chain evidence.

## Commands

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
./gradlew :sdk:jsTest
./gradlew buildMiniAppSdk
```

These commands are valid and were verified on 2026-09-15.

The `:sdk:jsTest` suite includes host-boundary, error-model, callback adaptation, cancellation, double-completion, optional abort, WeChat error-mapping, HTTP transport adaptation including host abort on cancellation, lifecycle state transitions, navigation adaptation, capability-support states and version gating, permission lifecycle behaviour including concurrent request merging, privacy authorization including the refusal classification, WeChat session checking including the expiry classification, the WeChat clipboard and vibration adapters including their per-capability gating, the WeChat file-system adapter including its sandbox and text boundaries, the WeChat location adapter including its coordinate validation and privacy precondition, the WeChat scan adapter including indeterminate-interruption classification and result validation, the WeChat media adapter including its request boundaries, result validation, and interruption classification, the WeChat subscription adapter including its template validation and per-template result policy, and interop object-construction coverage. These tests use fake callbacks and do not claim access to a real `wx` runtime.

## Consumer Bridge

Build and prepare every artifact required by the WeChat integration host:

```shell
./gradlew buildMiniAppSdk
```

This task builds the Kotlin/JS production library and TypeScript declaration, then synchronizes the required modules into the example. The stable compiler output names are:

```text
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-kotlin.js
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-kotlin.d.ts
sdk/build/dist/js/productionLibrary/kotlin-kotlin-stdlib.js
sdk/build/dist/js/productionLibrary/kotlinx-coroutines-core.js
sdk/build/dist/js/productionLibrary/kotlinx-atomicfu.js
```

The example consumes a separate, consumer-facing copy under:

```text
examples/wechat-miniprogram/miniprogram/libs/
```

The directory contains two hand-maintained normalization files, `kmp-miniapp-sdk.js` and `kmp-miniapp-sdk.d.ts`. `buildMiniAppSdk` preserves those files and synchronizes the compiler module, its declaration, and the three runtime modules around them. Treat every other file in this directory as task-owned output; do not add unrelated files there.

External `.js.map` files are generated and copied beside each distributed JavaScript module for local WeChat debugging. They embed source content, are reproducible, and are ignored by Git. The JavaScript and `.d.ts` distribution files remain checked in so the example can be opened directly, but `buildMiniAppSdk` is the authoritative refresh path.

Run the local consumer checks:

```shell
cd examples/wechat-miniprogram
npm install
npm run smoke
npm run typecheck
```

The smoke test loads the consumer-facing CommonJS module, calls `sdkVersion()`, and installs a fake global `wx` to verify Storage, the WeChat login bootstrap, the HTTP transport, lifecycle forwarding, all three navigation calls, runtime capability detection, the permission lifecycle, privacy authorization, the WeChat session check, the clipboard and vibration capabilities, the file system, location, scanning, media selection, and subscription requests. It also asserts what the HTTP transport handed to the fake host, including the raw-text response mode, the absolute page paths received by navigation, and the support state reported for each gated capability, that a scan, a media selection, and a subscription request each query and request no permission, how an ended interaction is told apart from a similar-looking failure, and that the host's own status line is never read as a template id. This validates module and adapter behavior but is not real-host evidence. TypeScript runs in strict mode and validates the Promise-based Storage, typed `WeChatLoginResult`, HTTP transport, lifecycle, navigation, capability-support, permission, privacy, session-check, clipboard, vibration, file-system, location, scan, media-selection, and subscription-request exports without `any`.

Real-host verification follows the checklist in [TESTING-en.md](TESTING-en.md), which is the authoritative list of required page values, console lines, and preparation steps. A capability is recorded as host-verified in [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) only after that run.

The Consumer Bridge and Storage capability were verified on 2026-09-14 in WeChat Developer Tools Stable 2.01.2510290 using user-provided visual evidence. The page displayed `0.1.0-SNAPSHOT`, reported Storage `PASS`, and showed `first=first, overwritten=second, missing=null`.

Authentication real-host acceptance was completed on 2026-09-14 using user-provided WeChat Developer Tools evidence. The page displayed `Client login code: PASS`, `codeReceived=true`, and a code length of 32 without showing the credential itself. The example intentionally never logs or renders the raw credential. A `wx.login` code is short-lived and must be sent to a trusted consumer backend for WeChat exchange; receiving it does not authenticate a user, create an SDK session, or authorize requests.

The HTTP transport capability was accepted in a real host on 2026-09-14 using user-provided WeChat Developer Tools confirmation. Its adapter, error mapping, and cancellation behavior remain covered by automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks; the real-host run confirms that the network card on the index page reaches `wx.request` successfully.

The lifecycle and navigation bridges completed WeChat Developer Tools acceptance through user confirmation on 2026-09-15. The run covered the foreground lifecycle state, the current page route, and the complete page-stack path: `navigateTo` opened the second page, `redirectTo` replaced it with the third page, and `navigateBack` returned directly to the index page. The background-state transition still requires placing the mini program in the background on a real device and is not covered by the Developer Tools simulator.

A passing Node/CommonJS smoke test or TypeScript check does not establish a passing WeChat Mini Program integration. Record the two results separately.

Runtime capability detection completed Developer Tools (base library 3.17.2) and Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) acceptance on 2026-09-15, both reporting `runtime-detection=Supported`, `storage=Supported`, and `ungated=Unsupported`. The lowest debug base library WeChat Developer Tools currently offers is 2.21.4, so a host below 2.20.1 cannot be constructed; `VersionDependent` is therefore covered by automated tests rather than a real-host screenshot. The checklist in [TESTING-en.md](TESTING-en.md) describes how to produce each case.

### WeChat privacy configuration

WeChat gates the personal-data APIs a mini program declares behind its own privacy contract, separately from system permissions. Two things belong to the mini program's backend and cannot be done or replaced by this SDK:

- The collection types must be declared in the MP backend under 设置 → 服务内容声明 → 用户隐私保护指引. A mini program that declares nothing is reported as needing no authorization, so a `NOT_REQUIRED` reading does not prove that a user ever agreed.
- From base library 2.32.3 the host intercepts privacy-gated calls. Below that version it does not intercept them at all, which is why this capability reports `UnsupportedCapability` rather than pretending the condition does not exist.

The SDK informs and gates; it does not generate a privacy policy, decide whether a business is compliant, or provide legal advice.

To observe privacy state in WeChat Developer Tools, open the index page and use the Privacy Authorization card: `Refresh privacy status` queries the host without any side effect, and only the `Request privacy authorization` button starts a prompt. The debug base library must be 2.32.3 or later for the card to reach the host at all.

A recorded answer belongs to the account, the device, the backend configuration, and the account's history with the mini program, so the same build can report `REQUIRED` on one account and `NOT_REQUIRED` on another. Clear the account's acceptance in the Developer Tools cache, or use another account, to see `REQUIRED` again; do not reset a device or an account destructively to produce a state.

Privacy and permission acceptance are recorded separately in the verification matrix, because the host tracks them separately and a result for one says nothing about the other.

The permission lifecycle completed Developer Tools and Android-device acceptance for both microphone and location on 2026-09-15. The microphone run covered `Granted`, `Denied`, the settings return, and a repeated request after refusal; the location run additionally produced `NotRequested`, `Granted` after an explicit request, and `Denied` after refusal. The automated suite still never requests a permission, because a real prompt requires a user gesture.

The WeChat session check passed Android real-device acceptance on 2026-09-15: OnePlus PLQ110, Android 36, WeChat 8.0.76, base library 3.17.3 [1641]. The console reported `check #1, state=Invalid` before the login bootstrap acquired a new code and repeatedly reported `state=Valid` after `wx.login` succeeded. This result is still not evidence of identity, and session-check acceptance remains separate from login-bootstrap acceptance.

The WeChat clipboard and vibration capabilities completed real-host acceptance on 2026-09-15. Developer Tools at base library 3.17.2 and an Android device both verified clipboard write, read-back, and `matched=true`; the OnePlus PLQ110 running Android 36, WeChat 8.0.76, and base library 3.17.3 [1641] reported both vibration calls as PASS, and the tester confirmed feeling both the short and long vibrations. `getClipboardData` is not an allowed `app.json.requiredPrivateInfos` entry, so the example does not declare it there.

### WeChat location configuration

`wx.getLocation` needs more backend preparation than any other capability here, and none of it can be done or replaced by this SDK:

- The mini program's category must be one WeChat accepts for location, and the interface must be activated under 开发 → 开发管理 → 接口设置.
- For releases after 2022-07-14, `app.json` must list `getLocation` in `requiredPrivateInfos` and must state a purpose in `permission.scope.userLocation.desc`; the example does both.
- The user must grant `scope.userLocation`.

WeChat's own simulator locates by IP and supports `gcj02` only, so a simulator result is not evidence about a device's real position. Its call-frequency rules also differ between build kinds, so a second call in the same session may return the first position rather than a fresh one.

Reading a position is not something the page may do on its own: the example's Location card calls it only from a tap, and never displays or logs the coordinates.

WeChat location completed Developer Tools and Android-device acceptance on 2026-09-15. The runs covered a `Supported` capability, permission moving from `NotRequested` to `Granted`, blocking with `DENIED` before `getLocation` after refusal, and successful location again after permission recovery; success logs report only coordinate and accuracy validity and never actual coordinates. The current scope implements only one-shot on-demand `getLocation`, not `chooseLocation` or `openLocation`.

### Indeterminate WeChat scan interruption

`wx.scanCode` opens WeChat's own scanning interface, so the SDK implements no interface and models no camera. The example's Scanner card calls it only from a tap and never displays or logs the scanned content, `rawData`, the character set, or the image path.

A device run proved that dismissing the scan interface and system camera access preventing it from opening both produce the host's cancel signal. The SDK therefore no longer interprets that signal as user intent or permission denial; `HostInteractionInterrupted` reports that the interaction produced no result and its cause is indeterminate. Only `scanCode:cancel` and `scanCode:fail cancel` match; similar but different messages remain `HostFailure`.

Developer Tools is not a device: its scan implementation has the user pick an image and then decodes it, so the camera path, what `onlyFromCamera` actually does, and the device's dismissal text all have to be confirmed on hardware.

The capability queries and requests no permission. `scope.camera` does exist on this host, but nothing citable ties it to `wx.scanCode`, so the SDK neither prompts for it nor registers it as a permission precondition, and the example adds nothing to `app.json.requiredPrivateInfos`.

A page must not read the scanned content itself: the example reports only whether content was present, whether the format the host named is one the SDK recognizes, and whether the outcome was a dismissal or a failure.

WeChat scanning has Developer Tools and Android-device coverage for `Supported` and successful scanning. The device also proved that dismissal and camera restriction produce the same indeterminate interruption, so the capability does not claim to separate them. It is recorded `Stable` in the capability matrix: implementation, automated tests, and the required host-verification evidence are all in place, and the host's ambiguity is preserved explicitly in the error model instead of being presented as a distinction the host does not make.

### WeChat media selection

`wx.chooseMedia` opens WeChat's own picker, so the SDK implements no picker and models no camera. The example's Media card calls it only from a tap and never displays or logs what was selected or the temporary paths it arrived in.

The SDK validates what WeChat states exactly and refuses the rest: `mediaType` is required because WeChat's own schema marks it so, `maxDurationSeconds` must fall inside the 3-to-60 seconds WeChat documents, a byte count must be a whole non-negative number, and a temporary path must be a non-empty string. The host's own limit on `count` is deliberately not duplicated in the SDK, because it depends on the base library, so the SDK asks for what the caller asked for and reports what comes back.

The installed Developer Tools base library reports its simulated picker dismissal as `chooseMedia:cancel`, while an Android device reports a manual dismissal as `chooseMedia:fail cancel`; neither carries a structured cause. Those two exact signals map to `HostInteractionInterrupted` rather than inferred user intent. Every other failure remains `HostFailure`. On the verified Android host, restricting media access produces the same indeterminate interruption as manual dismissal, so the SDK does not invent a permission classification the host cannot support.

The capability queries and requests no permission: WeChat's own picker needs none, and the host's scope list holds no scope for reading the media library. Writing to the album is a different capability with its own APIs (`saveImageToPhotosAlbum` and `saveVideoToPhotosAlbum`); it is not implemented here, because this work has no requirement for it and nothing citable ties `scope.writePhotosAlbum` to it.

The paths a selection returns are host temporary resources. The SDK keeps no copy and makes no claim about their lifetime, so a caller that needs the media later must copy it to storage it owns.

WeChat media selection completed real-host acceptance on 2026-09-16. Automated checks, Developer Tools, and an Android device cover `wechat.choose-media=Supported`, image, video, mixed, camera, manual-dismissal, and restricted-media-access paths without logging media content or complete temporary paths. Manual dismissal and restricted access produce the same indeterminate interruption on that host; the capability is `Stable` because this ambiguity is preserved explicitly.

### WeChat subscription requests

`wx.requestSubscribeMessage` puts WeChat's own prompt in front of the user, so the SDK implements no prompt and sends no message. The example's Subscription Message card asks the host only from a button, and only when a test template has been configured locally; with none configured it reports `NOT CONFIGURED` and makes no host call at all. Template ids are host identifiers belonging to the mini program's account, so none is committed: the card's list is empty in the repository and the page never displays or logs an id.

**This capability has the thinnest evidence in the project, and the code is shaped by that.** The installed Developer Tools base library declares the API in its metadata table — option `tmplIds`, and a success result keyed by template id with `errMsg` beside it — but ships no implementation of it and no documentation schema. Nothing in the offline sources therefore names this API's status vocabulary or its dismissal message. The only status string observed anywhere in that bundle is `accept`, in the simulator's subscription-prompt preview payload, which is prompt-rendering data rather than an API result. The SDK recognizes exactly that one status and preserves every other non-blank status verbatim. It classifies no dismissal signal yet: cancel-looking failures remain `HostFailure` until this API produces real-host evidence. The example logs only a closed diagnostic label, never the raw message or a template id.

The SDK validates the templates and response correlation rather than guessing the answer vocabulary: an id must be non-blank, duplicates are collapsed with the caller's order kept, and no count limit is enforced because no offline source states one. The response must contain exactly the requested template keys, each with a non-blank text status; missing, unexpected, blank, or non-text entries are `InvalidResponse`.

The caller owns the user gesture, and the requirement is a real one: WeChat refuses the request without one. Nothing in the SDK or the example triggers it on load, and nothing retries a refusal.

The capability queries and requests no permission, because no offline source ties a scope or a privacy condition to this API. That is a conclusion about the evidence, not a finding that none exists, so the manual run watches for a prompt rather than assuming either way; BOB-60's privacy work being unfinished neither implies nor rules out a condition here. No `app.json` declaration is added either.

Acceptance is a subscription state, never delivery. End-to-end delivery needs a WeChat backend template and a trusted backend, which is `BackendRequired` and outside what any adapter test can establish.

WeChat subscription requests have automated coverage plus partial real-device evidence. An Android run on 2026-09-16 verified runtime support and the `templateCount=0` guard. That guard intentionally makes no host call, so the absence of a consent prompt is the expected result, not a permission failure. Acceptance, refusal, dismissal, multi-template correlation, and delivery remain **blocked** on a valid test template configured for the same AppID. The capability is `Partial`.


The WeChat file system completed real-host acceptance on 2026-09-15. WeChat Developer Tools and an Android device both verified, at base library 3.17.2, writing the fixed test file, reading matching content, checking that it existed, removing it, and checking that it no longer existed; neither the sandbox root nor file contents were displayed or logged. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks also pass.

Privacy authorization has automated coverage only. Its real-host acceptance is outstanding, and it depends on two things this repository cannot arrange: the mini program's declared collection in the MP backend, and the account's own answer to the host prompt. The evidence the verification matrix requires is a `REQUIRED` reading with the host's contract name, a successful acceptance that leaves the host reporting `NOT_REQUIRED`, and a refusal that is reported as `REFUSED` while the requirement stays in place.

## Development Principle

IntelliJ IDEA provides code intelligence. The Gradle Wrapper is the build authority. An IDE success indicator does not prove that the project builds or that tests pass.

## Adding Dependencies

Declare dependency and plugin versions in `gradle/libs.versions.toml`. Do not hard-code the same version across multiple `build.gradle.kts` files without a documented reason.

Add dependencies only when required by an implemented change. Do not add speculative infrastructure.

## Definition of Done

After an SDK change:

- The Gradle build passes.
- Relevant tests pass.
- `docs/PROJECT_FACTS-en.md` and `docs/PROJECT_FACTS-ch.md` remain accurate and synchronized.
- No JavaScript, WeChat, DOM, or Node-specific implementation leaks into `commonMain`.
