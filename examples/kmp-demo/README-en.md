# KMP consumer demo without a server

[中文](README-ch.md)

This independent consumer uses the current SDK/plugin checkout through `includeBuild("../..")`. Root producer selection is not in published 0.1.1. It requires no backend.

## Layout and dependency boundary

- `core/`: `Greeting`, `sayHello`, platform identities/implementations, Mini App exports and logical tests. It applies Kotlin Multiplatform and has no Compose dependency.
- `shared/`: Android/iOS Compose UI, Compose resources and the iOS controller. It consumes `core` through `api(project(":core"))`; core never depends on shared.
- `androidApp/` and `iosApp/`: mobile app entry points.
- `miniApp/`: root WeChat source host. It renders the greeting through native WXML/WXSS. Generated libraries in `miniprogram/libs/` are ignored.
- `core/build/miniapp/bundle/` and root `build/miniapp-dist/`: generated build outputs.

## Explicit root selection

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("io.github.bobcgn.miniapp")
}
miniapp {
    logicProject.set(project(":core"))
}
```

The selected core module applies KMP as usual; the root applies Mini App integration there. The module name is a consumer decision, not a plugin requirement. This repository manually migrated its older mixed shared module into core and shared UI; the plugin does not infer names, create modules, move source files or exclude dependencies. Root path configuration must not be overridden in the selected module. Existing module-local integration remains supported, as shown by the server demo.

The migration preserves mobile UI/resources and moves the existing greeting and expect/actual platform logic without changing its package. `platformGreeting()` calls the same `Greeting().greet()` as mobile UI; WeChat platform/base-library info comes from the SDK, with unknown displayed when unavailable. The old shared-module require path was replaced with the actual core module path; initialization never rewrites handwritten pages.

## Build and checks

Run from this directory:

```bash
./gradlew --no-daemon --no-configuration-cache --console=plain \
  :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp \
  :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64
node miniApp/scripts/smoke.cjs
node build/miniapp-dist/scripts/smoke.cjs
```

Open `iosApp/iosApp.xcodeproj` in Xcode for the iOS application. The commands define acceptance steps; actual results and limits are recorded below. Node smoke tests isolated mock devtools, Android and missing-host scenarios, not real WeChat execution.

## Validation and limits

The previous shared/commonMain resource dependency caused renderer-boundary rejection. It now belongs exclusively to the shared UI module; Mini App compiles core. This is dependency isolation, not support for sharing Compose Res with Mini Apps. On 2026-10-14 the documented Wrapper command passed: 3 Mini App tests, sync/assemble/verify (24 files, verified=true), Android assembleDebug and iOS Simulator Kotlin compilation. Source-host and distribution Node smoke both passed all 3 mock-host scenarios. Android retains the Compose resource while Mini App contains no Compose/Skiko modules; stale shared-module output is absent. These are build/wiring results, not device execution.

Import `miniApp/` with a local test configuration after successful sync. Expected screen: `Hello, WeChat · devtools · 基础库 <actual base-library version>!`; Console `demo.result=PASS`. Check recompile/reopen and the standalone distribution separately. This migrated no-server demo requires its own user acceptance; the server-demo screenshot does not validate it. No upload, publication or merge is performed automatically.

## Version control

Keep source, platform assets, Wrapper, project configuration and the dependency lockfile. Generated libraries/build outputs, private WeChat config and machine-local settings are ignored. Only touristappid is retained in project config; iOS TEAM_ID remains empty.

## JS/TS declaration navigation

The host includes `miniprogram/jsconfig.json` and JSDoc type annotations on the bundle import. Its `platformGreeting()` and `MiniAppExports.sdkVersion()` calls are covered by automated declaration checks, but this no-server demo still requires its own manual IDE acceptance. The server demo's VS Code results and the IDEA interaction difference accepted on 2026-10-15 do not establish this demo's behavior. SDK integration promises matching declaration/runtime identity, not identical default click targets across editors. See [DEVELOPMENT](../../docs/DEVELOPMENT-en.md#jsts-consumer-declarations-and-navigation-current-source-unreleased) for checks and limits. The page also logs `demo.sdkVersion`; no manual Kotlin navigation claim is made for this demo.
