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

The `:sdk:jsTest` suite includes host-boundary, error-model, callback adaptation, cancellation, double-completion, optional abort, WeChat error-mapping, HTTP transport adaptation including host abort on cancellation, lifecycle state transitions, navigation adaptation, capability-support states and version gating, permission lifecycle behaviour including concurrent request merging, privacy authorization including the refusal classification, WeChat session checking including the expiry classification, the WeChat clipboard and vibration adapters including their per-capability gating, and interop object-construction coverage. These tests use fake callbacks and do not claim access to a real `wx` runtime.

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

The smoke test loads the consumer-facing CommonJS module, calls `sdkVersion()`, and installs a fake global `wx` to verify Storage, the WeChat login bootstrap, the HTTP transport, lifecycle forwarding, all three navigation calls, runtime capability detection, the permission lifecycle, privacy authorization, the WeChat session check, and the clipboard and vibration capabilities. It also asserts what the HTTP transport handed to the fake host, including the raw-text response mode, the absolute page paths received by navigation, and the support state reported for each gated capability. This validates module and adapter behavior but is not real-host evidence. TypeScript runs in strict mode and validates the Promise-based Storage, typed `WeChatLoginResult`, HTTP transport, lifecycle, navigation, capability-support, permission, privacy, session-check, clipboard, and vibration exports without `any`.

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

The permission lifecycle completed Developer Tools (base library 3.17.2) and Android device acceptance on 2026-09-15: the host reported `Granted` and `Denied`, a settings visit reported the host's decision, and requesting a refused permission reported `DENIED` without a second prompt. `NotRequested` could not be produced on the account used, because it already holds a decision for the mapped permission, so that state is covered by automated tests. The automated suite still never requests a permission, because a real prompt requires a user gesture. Only the microphone permission is mapped.

The WeChat session check passed Android real-device acceptance on 2026-09-15: OnePlus PLQ110, Android 36, WeChat 8.0.76, base library 3.17.3 [1641]. The console reported `check #1, state=Invalid` before the login bootstrap acquired a new code and repeatedly reported `state=Valid` after `wx.login` succeeded. This result is still not evidence of identity, and session-check acceptance remains separate from login-bootstrap acceptance.

The WeChat clipboard and vibration capabilities completed real-host acceptance on 2026-09-15. Developer Tools at base library 3.17.2 and an Android device both verified clipboard write, read-back, and `matched=true`; the OnePlus PLQ110 running Android 36, WeChat 8.0.76, and base library 3.17.3 [1641] reported both vibration calls as PASS, and the tester confirmed feeling both the short and long vibrations. `getClipboardData` is not an allowed `app.json.requiredPrivateInfos` entry, so the example does not declare it there.

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
