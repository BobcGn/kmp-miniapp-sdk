# kmp-miniapp-sdk runtime

[中文](README-ch.md)

The runtime module of `kmp-miniapp-sdk` provides Kotlin APIs for shared Mini App client behaviour,
host capabilities and the current WeChat adapter. It is a Kotlin Multiplatform library, but its
published platform implementation currently targets Kotlin/JS Mini App hosts.

Version `0.1.0` is available from Maven Central:

```kotlin
io.github.bobcgn:kmp-miniapp-sdk:0.1.0
```

This is an experimental release. Check the
[WeChat capability matrix](../docs/platforms/wechat/WECHAT_CAPABILITIES-en.md) before relying on a
specific capability in production.

## Recommended KMP integration

The intended consumer experience uses the companion Gradle plugin. It creates the `miniapp` target,
adds `miniappMain` and `miniappTest`, wires this runtime dependency, configures Node tests and
provides `assembleMiniAppBundle`.

> **Current publication status:** the runtime artifact above is live on Maven Central. The first
> `io.github.bobcgn.miniapp:0.1.0` plugin submission is still waiting for Gradle Plugin Portal
> approval. The configuration below is the supported final form, but online plugin resolution will
> work only after that approval is complete. Until then, use the
> [source-checkout workflow](../docs/CONSUMER_SETUP-en.md#current-source-checkout-workflow) or the
> runtime-only option below.

### 1. Configure repositories

In `settings.gradle.kts`:

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

### 2. Apply the plugin

In the KMP module's `build.gradle.kts`:

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

Do not manually create `miniappMain`, configure CommonJS, or add the runtime dependency. The plugin
owns those build details.

### 3. Put code in the right source sets

```text
src/
├── commonMain/kotlin/     portable behaviour; no host API or rendering
├── commonTest/kotlin/     portable tests
├── miniappMain/kotlin/    SDK calls, JS exports and host binding
└── miniappTest/kotlin/    Mini App / Node tests
```

For example, shared behaviour can stay in `commonMain`:

```kotlin
package example

public fun greeting(name: String): String = "Hello, $name"
```

The Mini App boundary belongs in `miniappMain`:

```kotlin
package example

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.js.JsExport

@JsExport
public fun hostGreeting(name: String): String = greeting(name)

@JsExport
public fun sdkVersion(): String = MiniAppSdk.VERSION
```

Only declarations exported at this boundary are visible to the host. WXML, WXSS and the thin host
JavaScript remain in the Mini App project; the SDK does not render UI.

### 4. Test and assemble

```bash
./gradlew miniappTest
./gradlew assembleMiniAppBundle
```

Open the configured host directory in WeChat Developer Tools and verify the host console and page.
Node tests prove Kotlin/JS behaviour; they do not replace real-host or real-device verification.

## Runtime-only integration for an existing Kotlin/JS target

If the project already owns a Kotlin/JS target and does not need the plugin-managed Mini App target,
add the runtime only to that target's main source set. Do not add this JS-only artifact to a
multi-platform `commonMain`, because Android and iOS cannot resolve a matching runtime variant.

```kotlin
kotlin {
    js {
        nodejs()
        useCommonJs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        jsMain.dependencies {
            implementation("io.github.bobcgn:kmp-miniapp-sdk:0.1.0")
        }
    }
}
```

This option provides the runtime API only. The consumer owns target configuration, tests, bundle
assembly, host dependency copying and the rendering boundary.

## Architecture boundary

- `commonMain` owns portable contracts, models, errors and behaviour.
- `miniappMain` or `jsMain` owns SDK calls, JavaScript exports and host binding.
- The host owns pages, layout, WXML/WXSS, rendering and native components.
- The backend owns authentication, payment, inventory and other business truth.
- The runtime SDK does not depend on Compose and is not a renderer, virtual DOM or WXML generator.

## Further documentation

- [Complete consumer setup](../docs/CONSUMER_SETUP-en.md)
- [Architecture](../docs/ARCHITECTURE-en.md)
- [Project facts](../docs/PROJECT_FACTS-en.md)
- [Testing](../docs/TESTING-en.md)
- [WeChat capability matrix](../docs/platforms/wechat/WECHAT_CAPABILITIES-en.md)
- [Host verification matrix](../docs/platforms/wechat/WECHAT_HOST_VERIFICATION-en.md)

## License

Licensed under the [Apache License 2.0](../LICENSE).
