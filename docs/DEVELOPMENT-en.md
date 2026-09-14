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

## Commands

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
```

These commands are valid and were verified on 2026-09-14.

## Consumer Bridge

Generate the Kotlin/JS production library:

```shell
./gradlew :sdk:jsNodeProductionLibraryDistribution
```

The compiler output is located at:

```text
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-sdk.js
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-sdk.d.ts
sdk/build/dist/js/productionLibrary/kotlin-kotlin-stdlib.js
```

The example consumes a separate, consumer-facing copy under:

```text
examples/wechat-miniprogram/miniprogram/libs/
```

The current copy is intentional and manual. Automating artifact copying belongs to the later tooling task. When refreshing it, copy the compiler module as `kmp-miniapp-sdk-kotlin.js`, its declaration as `kmp-miniapp-sdk-kotlin.d.ts`, and the required standard library module without changing the thin `kmp-miniapp-sdk.js` / `kmp-miniapp-sdk.d.ts` normalization wrapper.

Run the local consumer checks:

```shell
cd examples/wechat-miniprogram
npm install
npm run smoke
npm run typecheck
```

The smoke test loads the consumer-facing CommonJS module and calls `sdkVersion()`. TypeScript runs in strict mode and resolves the same function as returning `string`.

For real-host verification, import `examples/wechat-miniprogram` into WeChat Developer Tools, compile the project, and open the index page. The page must display `0.1.0-SNAPSHOT`, and the console must contain `[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT`.

This exact result was verified on 2026-09-14 in WeChat Developer Tools Stable 2.01.2510290 using user-provided visual evidence.

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
