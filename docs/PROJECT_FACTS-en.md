# Project Facts

[中文](PROJECT_FACTS-ch.md)

This file is the human-readable source of truth for the current state of kmp-miniapp-sdk.

If this file conflicts with executable configuration or source code, the executable configuration and source code take precedence.

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

The build baseline and first end-to-end consumer bridge both pass verification. The first raw, callback-style WeChat contracts now compile and have option-construction tests; no platform API is invoked by production code yet.

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
- A minimal `@JsExport` facade under `jsMain/.../export`
- A thin CommonJS normalization wrapper for consumers

The compiler-generated TypeScript declaration contains `MiniAppExports.sdkVersion(): string`. The compiler CommonJS shape is namespaced, so the consumer-facing wrapper exposes the stable `sdkVersion(): string` entry point without business logic.

## 6. Current Dependencies

| Use | Dependency | Version source | Version |
| --- | --- | --- | --- |
| Build plugin and Kotlin libraries | Kotlin Multiplatform / Kotlin | `gradle/libs.versions.toml` | 2.4.20 |
| Production | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `gradle/libs.versions.toml` | 1.11.0 |
| Test | `kotlin-test` | Kotlin plugin version | 2.4.20 |

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

The `export` boundary currently contains only the version facade. The interop boundary contains internal contracts for `showToast`, `getStorage`, `setStorage`, and `removeStorage`; no WeChat platform API implementation invokes them yet.

## 9. Current Capabilities

- The Kotlin Multiplatform project configures and builds.
- The `:sdk` JavaScript library target compiles.
- Production CommonJS library files are generated.
- TypeScript definition generation and validation are configured.
- `MiniAppExports.sdkVersion()` exports the shared `MiniAppSdk.VERSION` value.
- A consumer-facing CommonJS wrapper exposes `sdkVersion(): string` from `examples/wechat-miniprogram/miniprogram/libs`.
- A strict TypeScript consumer and a Node/CommonJS smoke test both pass without using `any` as the consumer contract.
- `examples/wechat-miniprogram` contains a minimal index page that loads the same consumer artifact and displays the version value.
- WeChat Developer Tools loads the artifact, logs `0.1.0-SNAPSHOT`, and displays the same value on the index page.
- A common test runs on the Node.js Kotlin/JS test environment.
- Explicit API mode is enabled.
- `jsMain/.../host/wechat/interop` defines the internal global `wx` contract, typed callback results, and typed option bags for the initial toast and storage methods.
- Plain-object factories for those option bags are tested in the Kotlin/JS Node test environment without requiring a real `wx` runtime.

## 10. Explicit Non-Capabilities

The project currently has no:

- `wx` API wrappers
- Runtime invocation of the declared `wx` contracts
- Mini Program lifecycle adapter
- Network, storage, or authentication adapter
- Callback-to-coroutine bridge
- Compose integration, UI DSL, renderer, or Virtual DOM
- npm publication
- Maven publication
- Gradle plugin

## 11. Verified Commands

Verified on 2026-09-14:

| Command | Result |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :sdk:jsNodeTest` | VERIFIED |
| `./gradlew :sdk:jsNodeProductionLibraryDistribution` | VERIFIED |
| `npm run smoke` in `examples/wechat-miniprogram` | VERIFIED |
| `npm run typecheck` in `examples/wechat-miniprogram` | VERIFIED |
| WeChat Developer Tools load, console, and page rendering | VERIFIED — user-provided screenshot shows the index page and matching console output |

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
