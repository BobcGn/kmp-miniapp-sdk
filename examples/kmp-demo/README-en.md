# KMP consumer demo

[中文](README-ch.md)

This independent consumer build uses the SDK and Gradle plugin from this checkout through `includeBuild("../..")`. It is not a module of the SDK root build and does not demonstrate a published plugin release.

## Layout

- `androidApp/` and `iosApp/`: native application entry points; Compose UI resides in the Android/iOS source sets of `shared`.
- `shared/src/commonMain` and `commonTest`: shared greeting logic and tests.
- `shared/src/miniappMain` and `miniappTest`: Mini App logic, SDK runtime inspection and tests; no renderer.
- `miniApp/`: root-level WeChat host source, created by `initMiniApp` and maintained as source thereafter. `miniprogram/libs/` is generated and ignored.
- `build/miniapp-dist/`: generated standalone distribution, ignored by Git.

## Current state and limitations

The consumer selects `miniapp { wechat { rootHost.set(true) } }`. Root host initialization has passed. The Mini App Kotlin entry exports `platformGreeting()`, using shared greeting logic and the SDK-reported platform and WeChat base-library version; it needs no backend.

The shared Compose resource dependency remains in `commonMain` by user decision. It currently introduces renderer dependencies into the Mini App dependency graph and blocks bundle synchronization at the SDK host-boundary check. This demo has not passed the complete Mini App build or runtime acceptance. The WeChat page is still the initialization template that lists module exports; it does not yet display the intended greeting screen. Android/iOS build and runtime acceptance are not established by committing these files. BOB-122 remains pending; the independent root-host fixture provides the BOB-126 runtime evidence.

`project.config.json` contains only `touristappid`. Configure a local test environment in WeChat Developer Tools; keep private tool settings and credentials out of commits. The iOS `TEAM_ID` is empty and must be supplied locally for signing.

## Commands

Run from this directory using its Gradle Wrapper:

```bash
./gradlew --no-daemon --no-configuration-cache initMiniApp
./gradlew --no-daemon --no-configuration-cache syncMiniApp assembleMiniApp verifyMiniApp
./gradlew :shared:miniappTest
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

The first command creates missing host files without overwriting existing files. The next two Mini App commands remain subject to the resource dependency blocker above. Import `miniApp/` in WeChat Developer Tools only after successful synchronization. For iOS, open `iosApp/iosApp.xcodeproj` in Xcode. These commands describe the workflow, not a claim that every command has passed.

## Version-control boundary

Commit Kotlin, host UI, platform assets, Gradle configuration, Wrapper files, Xcode project files and `kotlin-js-store/yarn.lock`. Ignore build output, generated host libraries, machine-specific SDK paths, IDE/cache files, Xcode user state and WeChat private configuration. Ignore rules preserve source files and the dependency lockfile.
