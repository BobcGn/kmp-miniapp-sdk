# Use kmp-miniapp-sdk 0.1.0 in a new KMP project

[中文](CONSUMER_SETUP-ch.md)

This guide is for a consumer project that resolves the released Gradle plugin and runtime online. It
does not use a checkout of this repository, `includeBuild`, an internal project dependency, or a
copied SDK artifact.

## Requirements

- JDK 17 or newer
- A Kotlin Multiplatform project using Kotlin 2.4.20 (the compatibility baseline tested for 0.1.0)
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
    id("io.github.bobcgn.miniapp") version "0.1.0"
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

miniapp {
    wechat {
        bundleDirectory.set(layout.projectDirectory.dir("host/miniprogram/libs"))
    }
}
```

The plugin automatically adds the released runtime coordinate
`io.github.bobcgn:kmp-miniapp-sdk:0.1.0` to `miniappMain`. Do not declare that dependency again.

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

## 4. Test and assemble

Run from the consumer project's root:

```shell
./gradlew miniappTest
./gradlew assembleMiniAppBundle
```

The configured bundle directory receives the consumer module, its TypeScript declaration, the SDK
runtime, Kotlin runtime dependencies and `package.json`. Open the host project in WeChat Developer
Tools and verify the bundle there; a Node smoke test is useful but is not host acceptance.

## Troubleshooting

- **Plugin cannot be found:** confirm `gradlePluginPortal()` is in `pluginManagement.repositories`
  and version `0.1.0` is specified on the plugin declaration.
- **Runtime cannot be resolved:** confirm `mavenCentral()` is in
  `dependencyResolutionManagement.repositories`; do not add an internal project dependency.
- **A common dependency has no Mini App variant:** apply `io.github.bobcgn.miniapp` to that KMP
  dependency rather than moving shared code out of `commonMain`.
- **Existing Yarn lock changes:** run `./gradlew kotlinUpgradeYarnLock` once, review the lock change,
  then rerun the build.
- **A bundle carries Compose or another renderer:** remove that dependency from the Mini App runtime
  graph. The plugin rejects renderers by design.

## Scope of 0.1.0

Version 0.1.0 is the first public, experimental release. Capability status and real-host evidence are
recorded in the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md). A successful
host callback is not backend business truth, and capabilities marked `Partial`, `Experimental`, or
`Planned` must not be treated as fully verified production features.

It is also the first publication of this plugin id, so the Gradle Plugin Portal may hold that first
upload for its own review, and Maven Central takes a short while to sync after a release. If the plugin
cannot be resolved shortly after the release, that is the reason rather than a fault in your build
configuration. How the release itself is performed is a maintainer concern and is recorded in
[RELEASING-en.md](RELEASING-en.md).
