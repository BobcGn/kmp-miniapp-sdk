# kmp-miniapp-sdk

[中文](README-ch.md)

A Kotlin Multiplatform Mini App platform: a Gradle plugin that gives an ordinary Kotlin Multiplatform
project a Mini App source set, a test source set and a host-ready bundle, plus the Kotlin runtime
those sources compile against. Mini App is the platform; **WeChat is the current host**, and the host
owns rendering.

The workflow and every command below come from, and are executed against, the repository's real
consumer build: [`fixtures/miniapp-consumer`](fixtures/miniapp-consumer). The snippets are shortened
where the text says so, but do not introduce an unverified API or build step.

## Status

Experimental / pre-alpha. The Consumer Bridge, Storage, `wx.login` client authentication bootstrap,
`wx.request` HTTP transport, App lifecycle, WeChat page-stack navigation, runtime capability
detection, the permission lifecycle, the WeChat session check, WeChat clipboard and vibration, basic
WeChat file-system access, one-shot on-demand WeChat location, WeChat scanning, and WeChat media
selection are implemented and have the required real-host evidence. Privacy authorization is
implemented but still depends on the mini program's backend privacy configuration and on the account,
so its real-host acceptance remains outstanding. WeChat subscription-message requests are implemented
and covered by automated checks; accepting them needs a configured template. The WeChat network
extensions are implemented and covered by automated checks; accepting upload and download needs a
controlled HTTPS test service, while observing a real network change needs a device. WeChat standard
payment is a typed forwarder of backend-produced parameters whose acceptance needs a legal merchant
environment, so it stays `Partial`. The
[WeChat capability matrix](docs/platforms/wechat/WECHAT_CAPABILITIES-en.md) has the exact status of
every capability, and a `Planned` entry there is not an implementation fact.

**The plugin and the runtime SDK are not published yet.** See
[Before publication](#before-publication-what-you-do-today) for what that means in practice.

## What this project is

- A Kotlin Multiplatform library for shared client behaviour and host capabilities
- A Gradle plugin that builds that library into a distribution a Mini App host can load
- A typed Kotlin/JavaScript boundary, currently targeting the WeChat Mini Program runtime

## What this project is not

- A UI framework or a Kuikly replacement
- A Compose renderer, a Virtual DOM, or a WXML generator
- A replacement for WXML or WXSS
- A renderer of any kind: the backend owns business truth, Kotlin owns client behaviour, and the host
  owns rendering. See [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-en.md).

## Who owns what

| Layer | Owns | Does not own |
| --- | --- | --- |
| **Gradle plugin** `io.github.bobcgn.miniapp` | The `miniapp` target and the `miniappMain` / `miniappTest` source sets, the runtime dependency, the Node test run, `assembleMiniAppBundle`, the renderer boundary check, the `miniapp { }` extension | Any runtime API, any rendering, any business configuration |
| **Runtime SDK** `io.github.bobcgn:kmp-miniapp-sdk` | The Kotlin API your `miniappMain` compiles against: shared contracts, host capability abstraction, the WeChat adapter, the error and async model | Rendering, WXML/WXSS, view trees, business truth, authentication or payment confirmation |
| **WeChat host** (yours) | `project.config.json`, `app.json`, pages, WXML, WXSS, the thin JavaScript that requires the bundle and calls it, `setData` and event forwarding | Nothing inside this SDK |
| **Presentation Runtime** | `UiState`, `Action`, `Store`, `Effect`, state machines, presentation logic — **not implemented, and this document shows no API for it** | It is P2 scope, described in the [roadmap](docs/ROADMAP-en.md) |

## Quick start

The following produces a working Mini App from an ordinary Kotlin Multiplatform project. It is the
flow [`fixtures/miniapp-consumer`](fixtures/miniapp-consumer) runs on every verification pass.

These are the fixture's own files, with two adaptations this page has to make: the fixture lives inside
this repository, so its `includeBuild` path is `../..` and its project name is `miniapp-consumer`, and
it selects the host bundle directory through a Gradle property so the same build can target both the
default and the host location. The API calls, the source, and the script shape are the fixture's.

### 0. What you need

- A JDK the Kotlin Gradle Plugin supports (the plugin is built and tested on JDK 17)
- This repository checked out next to your project, until the plugin and the SDK are published
- The Gradle Wrapper in your own project — a consumer never needs the wrapper from this repository

### 1. `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // Until publication, the plugin comes from a composite build of this repository.
    includeBuild("../kmp-miniapp-sdk")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// A plugin-management composite resolves plugins only. Dependency substitution — the runtime SDK
// arriving as `io.github.bobcgn:kmp-miniapp-sdk` — needs the build included at settings level too.
includeBuild("../kmp-miniapp-sdk")

rootProject.name = "my-miniapp"
```

### 2. `build.gradle.kts`

```kotlin
plugins {
    kotlin("multiplatform") version "2.4.20"
    id("io.github.bobcgn.miniapp")
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

That is the whole build script. There is no Kotlin/JS target to declare, no source set to create, no
`dependsOn` to wire, no runtime artifact to name, and no output to copy. `miniappMain`, `miniappTest`,
their edges to `commonMain` / `commonTest`, the runtime dependency and the bundle task all come from
the plugin. Kotlin 2.4.20 is the version this plugin is built and tested against; compatibility with
other Kotlin Gradle Plugin versions has not been established yet.

### 3. Shared behaviour in `commonMain`

```kotlin
package consumer

public fun greeting(name: String): String = "Hello, $name, from commonMain"

public class Counter(private var value: Int = 0) {
    public fun increment(): Int {
        value += 1
        return value
    }

    public fun current(): Int = value
}
```

This file knows nothing about WeChat, JavaScript, or rendering. It is the code a Compose client would
use too.

### 4. Host entry points in `miniappMain`

```kotlin
package consumer

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.js.JsExport

@JsExport
public fun hostGreeting(name: String): String = greeting(name)

@JsExport
public fun countUpTo(limit: Int): Int {
    val counter = Counter()
    repeat(limit) { counter.increment() }
    return counter.current()
}

@JsExport
public fun sdkVersion(): String = MiniAppSdk.VERSION
```

Only `@JsExport` declarations reach the host. This file calls `commonMain` and the runtime SDK's
public API (which compiles because the plugin wired the SDK in) and does nothing else — no view, no
layout, no `wx` call, no markup.

### 5. Tests

`commonTest` holds the shared tests:

```kotlin
package consumer

import kotlin.test.Test
import kotlin.test.assertEquals

public class SharedTest {

    @Test
    public fun greetingIsShared() {
        assertEquals("Hello, Ada, from commonMain", greeting("Ada"))
    }
}
```

`miniappTest` holds tests that need the Mini App compilation:

```kotlin
package consumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class MiniAppTest {

    @Test
    public fun miniAppMainReusesTheSharedBehaviour() {
        assertEquals("Hello, Ada, from commonMain", hostGreeting("Ada"))
    }

    @Test
    public fun theRuntimeSdkIsOnTheMiniAppClasspath() {
        assertTrue(sdkVersion().isNotBlank(), "the runtime SDK must report a version")
    }
}
```

### 6. Run the tests

```shell
./gradlew miniappTest
```

`miniappTest` runs both halves: the tests that live in `miniappTest`, and the ones declared in
`commonTest`. In the fixture that is `consumer.MiniAppTest` (3 tests) plus `consumer.SharedTest`
(2 tests), and the executions are recorded under `build/test-results/miniappNodeTest/`.

### 7. Assemble the bundle

```shell
./gradlew assembleMiniAppBundle
```

The bundle lands in the directory configured in step 2 — `host/miniprogram/libs` there, and
`build/miniapp/bundle` if you configure nothing. It is a compiler-managed distribution and contains
14 files in the fixture, including:

| File | What it is |
| --- | --- |
| `miniapp-consumer-miniapp.js` | your compiled module, the only thing the host requires |
| `miniapp-consumer-miniapp.d.ts` | its TypeScript declaration, for a TypeScript host |
| `kmp-miniapp-sdk-kotlin.js` | the runtime SDK, as its own module |
| `kotlin-kotlin-stdlib.js`, `kotlinx-coroutines-core.js`, `kotlinx-atomicfu.js` | the Kotlin runtime the compilation resolved |
| `package.json` | the module metadata that ties them together |

There is no WXML or WXSS in it. Markup is the host's, and the plugin generates none.

### 8. Load it in the WeChat host

The host is an ordinary mini program that owns its own UI. [`fixtures/miniapp-consumer/host`](fixtures/miniapp-consumer/host)
is a minimal one:

```text
host/
├── project.config.json          miniprogramRoot: "miniprogram/"
└── miniprogram/
    ├── app.json                 pages
    └── pages/index/
        ├── index.wxml           the view the user sees
        └── index.js              thin JavaScript: require the bundle, call it, setData
```

The page requires the bundle by relative path and binds the result:

```javascript
const consumer = require('../../libs/miniapp-consumer-miniapp.js').consumer;

Page({
  onLoad() {
    const greeting = consumer.hostGreeting('WeChat');
    const counted = consumer.countUpTo(5);
    const version = consumer.sdkVersion();

    this.setData({ greeting, counted: String(counted), version });
  }
});
```

Shortened for this page: the fixture's own handler also logs each value and rendering a failure
instead of throwing.

Point `miniprogramRoot` at your own mini program directory, run `assembleMiniAppBundle` with
`bundleDirectory` aimed at a directory inside it, and open that directory in WeChat Developer Tools.
Use your own AppID in `project.config.json`, or Developer Tools' test AppID.

### 9. What you should see

The fixture's host page renders the shared greeting, the counter, and the SDK version, and its Console
reports:

```text
fixture.greeting=Hello, WeChat, from commonMain
fixture.counted=5
fixture.sdkVersion=0.1.0-SNAPSHOT
fixture.result=PASS
```

That output is real-host evidence: the fixture bundle was loaded in WeChat Developer Tools at base
library **3.17.3** and produced exactly these lines. It is the acceptance for *this bundle shape on
this host*, not a statement about your mini program, other base libraries, other WeChat capabilities,
or other hosts.

`host/scripts/host-smoke.cjs` runs the same `require()` on Node so module wiring can be checked
without Developer Tools. That is a wiring check, not host acceptance, and the two are recorded
separately.

## Before publication: what you do today

Neither the plugin nor the runtime SDK has been published to the Gradle Plugin Portal, Maven Central
or npm, so today a consumer needs this repository's source. The only thing that changes is where
Gradle resolves two coordinates from:

| | Today (source checkout) | After publication |
| --- | --- | --- |
| Plugin `io.github.bobcgn.miniapp` | resolved by id from `includeBuild("../kmp-miniapp-sdk")` in `pluginManagement` | resolved by id from the plugin repository, with a version |
| Runtime `io.github.bobcgn:kmp-miniapp-sdk` | substituted by the settings-level `includeBuild` | resolved from the repository, with a version |

Your `build.gradle.kts`, your sources and your host are unaffected: the build script already applies
the plugin by id and never names the runtime artifact, and nothing in it points at a module path, a
`build/` directory or a generated file. This document does not give you a published version to use,
because none exists.

## What the plugin deliberately does not do

- It does not render, and it rejects a build whose runtime classpath carries Compose, Skiko or a
  browser UI runtime, because such a bundle would build successfully and still be unusable in a host.
- It does not generate WXML or WXSS.
- It does not publish anything, to any repository.
- It does not put business configuration — AppIDs, secrets, merchant material, template identifiers —
  into Gradle. That belongs to your backend and your host console, not to a build script.
- It does not give you a Presentation Runtime. `UiState`, `Action`, `Store` and `Effect` do not exist
  in this SDK yet; what exists is described in [PROJECT_FACTS-en.md](docs/PROJECT_FACTS-en.md), and
  what is planned is in [ROADMAP-en.md](docs/ROADMAP-en.md).

## Repository layout

The repository builds the SDK and the plugin that the Quick Start consumes, and verifies them:

```shell
./gradlew projects
./gradlew clean check
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :miniapp-gradle-plugin:test
./gradlew verifyMiniAppGradlePluginIntegration
```

`buildMiniAppSdk` is a repository-internal task that republishes the SDK into
`examples/wechat-miniprogram`; that example predates the plugin and is not the consumer workflow.
See the [development guide](docs/DEVELOPMENT-en.md) for the maintainer commands, the consumer fixture
and why its verification is not part of `check`.

## Documentation

- [Current project facts](docs/PROJECT_FACTS-en.md)
- [Architecture](docs/ARCHITECTURE-en.md)
- [Development guide](docs/DEVELOPMENT-en.md)
- [Testing and WeChat host verification](docs/TESTING-en.md)
- [WeChat capability matrix](docs/platforms/wechat/WECHAT_CAPABILITIES-en.md)
- [Roadmap](docs/ROADMAP-en.md)
- [Architecture decisions](docs/decisions/README-en.md)

Executable Gradle configuration and source code take precedence if documentation becomes stale.

## License

TBD.
