# Use kmp-miniapp-sdk 0.1.1 in a new KMP project

[中文](CONSUMER_SETUP-ch.md)

This guide describes the online consumer path for 0.1.1 once that version passes its release gate and
is published. Until then, 0.1.0 remains the latest public version and the 0.1.1 workflow is exercised
through this repository's composite-build fixture. The published path does not use a checkout of this
repository, `includeBuild`, an internal project dependency, or a copied SDK artifact.

## Requirements

- JDK 17 or newer
- A Kotlin Multiplatform project using Kotlin 2.4.20 (the compatibility baseline tested for 0.1.1)
- Maven Central and the Gradle Plugin Portal reachable from Gradle
- A native Mini App host; WeChat is the host currently verified by this repository

## 1. Configure repositories

Keep the standard public repositories in `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

Do not add this SDK with `includeBuild`: that form is only for repository development.

## 2. Apply the plugin

In the KMP module that should gain Mini App sources, add the released plugin version:

```kotlin
plugins {
    kotlin("multiplatform") version "2.4.20"
    id("io.github.bobcgn.miniapp") version "0.1.1"
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
```

The plugin automatically adds the released runtime coordinate
`io.github.bobcgn:kmp-miniapp-sdk:0.1.1` to `miniappMain`. Do not declare that dependency again.

If `commonMain` depends on another KMP project, apply the same Mini App plugin to that project too;
the dependency must publish a `miniapp` variant.

## 3. Add sources

After Gradle sync, the applying module contains:

```text
src/
├── commonMain/kotlin/
├── commonTest/kotlin/
├── miniappMain/kotlin/
└── miniappTest/kotlin/
```

Put shared behaviour in `commonMain`. Put host binding and JavaScript exports in `miniappMain`.
Rendering remains in the host's WXML/WXSS and thin JavaScript or TypeScript layer; the SDK does not
render.

## 4. Create the source host and develop

```shell
./gradlew miniappTest
./gradlew initMiniApp
./gradlew syncMiniApp
```

`initMiniApp` writes a minimal WeChat project at `miniApp/` and is safe to rerun: it creates only
files that are missing and never rewrites one you edited. `syncMiniApp` copies the compiled modules
into `miniApp/miniprogram/libs/`, which is the plugin's area inside your project; the rest of
`miniApp/` is yours. Open `miniApp/` in WeChat Developer Tools, set your own AppID in
`project.config.json`, and after each Kotlin change run `syncMiniApp` and recompile there.

## 5. Assemble and verify the distribution

```shell
./gradlew assembleMiniApp
./gradlew verifyMiniApp
```

`assembleMiniApp` writes a complete, standalone WeChat project to `build/miniapp-dist/`: your source
host plus the current compiled modules, with machine-local and IDE files excluded. It does not need
`syncMiniApp` to have run and it never modifies the source host. `verifyMiniApp` then reads that
directory on its own and fails with the specific missing file or unresolved `require()` if it is not a
complete host project. Open `build/miniapp-dist/` in WeChat Developer Tools to accept the artifact;
a Node smoke test is useful but is not host acceptance.

## Troubleshooting

- **Plugin cannot be found:** confirm `gradlePluginPortal()` is in `pluginManagement.repositories`
  and version `0.1.1` is specified on the plugin declaration.
- **Runtime cannot be resolved:** confirm `mavenCentral()` is in
  `dependencyResolutionManagement.repositories`; do not add an internal project dependency.
- **A common dependency has no Mini App variant:** apply `io.github.bobcgn.miniapp` to that KMP
  dependency rather than moving shared code out of `commonMain`.
- **Existing Yarn lock changes:** run `./gradlew kotlinUpgradeYarnLock` once, review the lock change,
  then rerun the build.
- **A bundle carries Compose or another renderer:** remove that dependency from the Mini App runtime
  graph. The plugin rejects renderers by design.
- **`assembleMiniApp` says the source host does not exist:** run `./gradlew initMiniApp` once, or
  point `miniapp { wechat { sourceDirectory } }` at your own mini program project.
- **A configured path is rejected:** the plugin refuses a source host outside your project, a
  generated library area outside the source host, and a distribution outside the project, because
  each of them is written to with `Sync`. The error names the path and what would have been deleted.

## Scope of 0.1.1

Version 0.1.0 was the first public, experimental release; 0.1.1 adds the distribution pipeline
described above and changes nothing 0.1.0 introduced. Capability status and real-host evidence are
recorded in the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md). A successful
host callback is not backend business truth, and capabilities marked `Partial`, `Experimental`, or
`Planned` must not be treated as fully verified production features.

0.1.0 was also the first publication of this plugin id, so the Gradle Plugin Portal held that first
upload for its own review and Maven Central took a short while to sync. A later version is not subject
to that first-upload delay, but if the plugin cannot be resolved shortly after a release, that is the
reason rather than a fault in your build configuration. How the release itself is performed is a
maintainer concern and is recorded in [RELEASING-en.md](RELEASING-en.md).
