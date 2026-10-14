# KMP consumer demo with server

[中文](README-ch.md)

This independent Gradle build contains Android/iOS clients and a Ktor server. It consumes the SDK plugin and runtime from this checkout using `includeBuild("../..")`, including unreleased root-host support; it does not verify a published plugin version.

## Layout

- `core/`: shared logic for Android, iOS and JVM. The Mini App plugin adds `core/src/miniappMain/kotlin` and `core/src/miniappTest/kotlin` here. The SDK runtime is injected only into the Mini App source set.
- `app/shared/`: Compose UI and resources; it depends on `core` and does not apply the Mini App plugin.
- `app/androidApp/` and `app/iosApp/`: mobile application entry points.
- `app/miniApp/`: WeChat host source, initialized explicitly and maintained by the consumer. Generated libraries belong in `miniprogram/libs/` and are ignored.
- `server/`: Ktor JVM application depending on `core`; Mini App initialization does not require starting this server.
- `core/build/miniapp/bundle/`: generated Kotlin/JS bundle; `build/miniapp-dist/`: generated standalone WeChat distribution. Both are build output, not host source.

## Commands

Run from this directory using its Gradle Wrapper:

```bash
./gradlew --no-daemon --no-configuration-cache --console=plain initMiniApp :core:checkMiniAppHostBoundary
./gradlew syncMiniApp assembleMiniApp verifyMiniApp
./gradlew :core:miniappTest
node app/miniApp/scripts/smoke.cjs
node build/miniapp-dist/scripts/smoke.cjs
./gradlew :app:androidApp:assembleDebug
./gradlew :app:shared:testAndroidHostTest
./gradlew :app:shared:iosSimulatorArm64Test
./gradlew :server:run
./gradlew :server:test
```

For iOS, open `app/iosApp/iosApp.xcodeproj` in Xcode. Import `app/miniApp/` into WeChat Developer Tools after successful bundle synchronization.

## Current validation and limits

On 2026-10-14, `initMiniApp :core:checkMiniAppHostBoundary` passed with BUILD SUCCESSFUL: the host was created at `app/miniApp/` and the resolved Mini App dependency graph passed the renderer-boundary check. Initialization creates missing files without overwriting existing files.

The Mini App now exports `org.example.kmp_demo_with_server.platformGreeting()` and shares `core/commonMain`'s `sayHello()` with the mobile clients. The native WeChat page displays only the greeting, reading the platform and base-library version through the SDK. No server or network request is needed; absent host information is displayed as unknown. Compose UI and resources remain in `app/shared`.

On 2026-10-14 the Wrapper command `:core:miniappTest :core:jvmTest syncMiniApp assembleMiniApp verifyMiniApp` passed: 2 Mini App tests passed and the distribution reported `verified=true` (24 files). `jvmTest` was NO-SOURCE, not a passing JVM test suite. Node smoke checks the real compiled page/module under isolated mock devtools, Android and missing-host scenarios; it is not WeChat runtime proof. Android `:app:androidApp:assembleDebug` passed, and its APK retains the Compose resource. `core`'s `miniappRuntimeClasspath` and generated libraries contain no Compose/Skiko renderer or Compose resources. This proves separation for this demo, not general Mini App compatibility with shared `Res`.

Developer Tools runtime acceptance remains pending: the user confirmed they will import this demo with a test account and perform acceptance manually. The CLI service port was disabled during this run; no security setting was changed. iOS, server and real-device runtime acceptance are not claimed. Initialization remains idempotent and preserves existing host files.

## Developer Tools acceptance

After synchronization, import `app/miniApp/` with a local test configuration. The first screen must show `Hello, WeChat · devtools · 基础库 <actual base-library version>!`, matching the selected base library. Console must report `demo.result=PASS` with no application exception. Recompile/reopen to verify repeat loading. The screenshot and runtime result must be recorded separately from Node/build results. Do not upload or publish during this demo acceptance.

The committed configuration must use only `touristappid`; local private configuration and credentials must stay out of version control. Source host files remain outside build output so clean does not remove them. No SDK renderer or server dependency has been added.

## JS/TS declaration navigation

The host includes `miniprogram/jsconfig.json` and JSDoc type annotations on the bundle import. Open the standalone `miniprogram` directory in the IDE, then Cmd/Ctrl-click `platformGreeting()` and `MiniAppExports.sdkVersion()` in `pages/index/index.js`. Each should resolve to the corresponding generated declaration under `libs/`; actual IntelliJ IDEA 2026.2.3 navigation is pending retest, not established by the TypeScript checks. The page also logs `demo.sdkVersion`. See [DEVELOPMENT](../../docs/DEVELOPMENT-en.md#jsts-consumer-declarations-and-navigation-current-source-unreleased) for executable declaration checks and evidence requirements. No Kotlin navigation claim is made.
