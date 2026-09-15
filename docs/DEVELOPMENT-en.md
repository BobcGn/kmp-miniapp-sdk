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

## Commands

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
./gradlew :sdk:jsTest
./gradlew buildMiniAppSdk
```

These commands are valid and were verified on 2026-09-15.

The `:sdk:jsTest` suite includes host-boundary, error-model, callback adaptation, cancellation, double-completion, optional abort, WeChat error-mapping, HTTP transport adaptation including host abort on cancellation, lifecycle state transitions, navigation adaptation, and interop object-construction coverage. These tests use fake callbacks and do not claim access to a real `wx` runtime.

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

The smoke test loads the consumer-facing CommonJS module, calls `sdkVersion()`, and installs a fake global `wx` to verify Storage, the WeChat login bootstrap, the HTTP transport, lifecycle forwarding, and all three navigation calls. It also asserts what the HTTP transport handed to the fake host, including the raw-text response mode, and the absolute page paths received by navigation. This validates module and adapter behavior but is not real-host evidence. TypeScript runs in strict mode and validates the Promise-based Storage, typed `WeChatLoginResult`, HTTP transport, lifecycle, and navigation exports without `any`.

Real-host verification follows the checklist in [TESTING-en.md](TESTING-en.md), which is the authoritative list of required page values, console lines, and preparation steps. A capability is recorded as host-verified in [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) only after that run.

The Consumer Bridge and Storage capability were verified on 2026-09-14 in WeChat Developer Tools Stable 2.01.2510290 using user-provided visual evidence. The page displayed `0.1.0-SNAPSHOT`, reported Storage `PASS`, and showed `first=first, overwritten=second, missing=null`.

Authentication real-host acceptance was completed on 2026-09-14 using user-provided WeChat Developer Tools evidence. The page displayed `Client login code: PASS`, `codeReceived=true`, and a code length of 32 without showing the credential itself. The example intentionally never logs or renders the raw credential. A `wx.login` code is short-lived and must be sent to a trusted consumer backend for WeChat exchange; receiving it does not authenticate a user, create an SDK session, or authorize requests.

The HTTP transport capability was accepted in a real host on 2026-09-14 using user-provided WeChat Developer Tools confirmation. Its adapter, error mapping, and cancellation behavior remain covered by automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks; the real-host run confirms that the network card on the index page reaches `wx.request` successfully.

The lifecycle and navigation bridges completed WeChat Developer Tools acceptance through user confirmation on 2026-09-15. The run covered the foreground lifecycle state, the current page route, and the complete page-stack path: `navigateTo` opened the second page, `redirectTo` replaced it with the third page, and `navigateBack` returned directly to the index page. The background-state transition still requires placing the mini program in the background on a real device and is not covered by the Developer Tools simulator.

A passing Node/CommonJS smoke test or TypeScript check does not establish a passing WeChat Mini Program integration. Record the two results separately.

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
