# kmp-miniapp-sdk

[中文](README-ch.md)

A Kotlin Multiplatform Mini App platform: a Gradle plugin that gives an ordinary Kotlin Multiplatform
project a Mini App source set, a test source set, a WeChat source host, a host-ready bundle and a
standalone distribution, plus the Kotlin runtime those sources compile against. Mini App is the
platform; **WeChat is the current host**, and the host owns rendering.

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

Version **0.1.1** is the latest public, experimental release. It adds the Mini App
distribution pipeline: `initMiniApp` creates the WeChat source host, `syncMiniApp` drives the
development loop, and `assembleMiniApp` plus `verifyMiniApp` produce and check a standalone project.
Everything 0.1.0 introduced — the source sets, the runtime dependency, the test run and
`assembleMiniAppBundle` — is unchanged. See the dedicated
[consumer setup guide](docs/CONSUMER_SETUP-en.md) for the online-resolution path, or the
[runtime SDK README](sdk/README-en.md) for SDK-focused KMP integration and dependency guidance.

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
| **Gradle plugin** `io.github.bobcgn.miniapp` | The `miniapp` target and the `miniappMain` / `miniappTest` source sets, the runtime dependency, the Node test run, `assembleMiniAppBundle`, `initMiniApp`, `syncMiniApp`, `assembleMiniApp`, `verifyMiniApp`, the generated library area inside your source host, the renderer boundary check, the `miniapp { }` extension | Any runtime API, any rendering, any business configuration |
| **Runtime SDK** `io.github.bobcgn:kmp-miniapp-sdk` | The Kotlin API your `miniappMain` compiles against: shared contracts, host capability abstraction, the WeChat adapter, the error and async model | Rendering, WXML/WXSS, view trees, business truth, authentication or payment confirmation |
| **WeChat host** (yours, scaffolded by `initMiniApp`) | `project.config.json`, `app.json`, pages, WXML, WXSS, the thin JavaScript that requires the bundle and calls it, `setData` and event forwarding | Nothing inside this SDK |
| **Presentation Runtime** | `UiState`, `Action`, `Store`, `Effect`, state machines, presentation logic — **not implemented, and this document shows no API for it** | It is P2 scope, described in the [roadmap](docs/ROADMAP-en.md) |

## Quick start

The following produces a working Mini App from an ordinary Kotlin Multiplatform project. It is the
flow [`fixtures/miniapp-consumer`](fixtures/miniapp-consumer) runs on every verification pass.

These are the fixture's own APIs and script shape, published as the 0.1.1 consumer workflow.

### 0. What you need

- A JDK the Kotlin Gradle Plugin supports (the plugin is built and tested on JDK 17)
- Access to the Gradle Plugin Portal and Maven Central
- The Gradle Wrapper in your own project — a consumer never needs the wrapper from this repository

### 1. `settings.gradle.kts`

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

rootProject.name = "my-miniapp"
```

### 2. `build.gradle.kts`

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

That is the whole build script. There is no Kotlin/JS target to declare, no source set to create, no
`dependsOn` to wire, no runtime artifact to name, and no output to copy. On the first Gradle import,
the plugin creates `src/miniappMain/kotlin` and `src/miniappTest/kotlin` beside the existing common
source directories. `miniappMain`, `miniappTest`, their edges to `commonMain` / `commonTest`, the
runtime dependency and the host tasks all come from
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

### 7. Create the Mini App source host

```shell
./gradlew initMiniApp
```

This writes `miniApp/` beside your build script: a minimal WeChat project. It is **source**, and it is
yours — the plugin writes a file only when it is missing and never rewrites one that exists, so a
second run changes nothing and your edits are safe. Nothing creates it implicitly: not applying the
plugin, not Gradle configuration, not an IDE import.

```text
miniApp/
├── project.config.json          miniprogramRoot: "miniprogram/"
├── .gitignore                   keeps the generated library area out of version control
└── miniprogram/
    ├── app.js, app.json, app.wxss, sitemap.json
    └── pages/index/
        └── index.js, index.json, index.wxml, index.wxss
```

Set your own AppID in `project.config.json` — the template ships an empty one — or use Developer
Tools' test AppID.

### 8. The development loop

```shell
./gradlew syncMiniApp
```

`syncMiniApp` compiles your Kotlin and copies the result into `miniApp/miniprogram/libs/`, so the loop
is: edit Kotlin → `./gradlew syncMiniApp` → recompile in WeChat Developer Tools → look at the
simulator. It is a `Sync`, not a `Copy`, because the generated library area is the plugin's: a module
that left the bundle has to leave your source host too, or the host keeps loading code the build no
longer produces. It never writes anywhere else in `miniApp/`.

The page requires the compiled module by relative path, exactly as it does in the distribution:

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

Shortened for this page: the fixture's own handler also logs each value and renders a failure instead
of throwing. The `require()` is static, so that file name is part of your page: `initMiniApp` writes
the plugin's default module name into it, and a build that sets a different `outputModuleName` edits
that one line.

### 9. The acceptance artifact

```shell
./gradlew assembleMiniApp
./gradlew verifyMiniApp
```

`assembleMiniApp` produces a complete, standalone WeChat project at `build/miniapp-dist/`: your source
host, minus everything local to a machine, plus the compiler's current output placed where the source
host keeps it. It does not need `syncMiniApp` to have run, and it never writes into the source host,
so the artifact you accept with is not the project you develop in.

```text
build/miniapp-dist/
├── project.config.json
└── miniprogram/
    ├── app.js, app.json, app.wxss, sitemap.json
    ├── pages/index/...            your handwritten pages
    └── libs/                      the compiler's output, injected here
```

Left out on the way: `project.private.config.json`, `node_modules/`, `build/`, `.gradle/`, `.idea/`,
`.git/`, `*.iml`, `.DS_Store` and the distribution's own `.gitignore`. Kept: everything WeChat
packages — the project configuration, the pages, the WXML and WXSS, the assets, the compiled module,
the runtime SDK, the Kotlin runtime, `package.json` and the `.d.ts`.

`verifyMiniApp` reads the assembled distribution and nothing else. It checks the project and app
configuration, resolves `miniprogramRoot`, checks that every declared page has its entry files, checks
that the library carries one module, one declaration and its npm manifest, resolves every `require()`
in the distribution, and rejects absolute paths, `node_modules` and references back to the project. It
fails with the specific missing file or broken reference.

Then open `build/miniapp-dist/` in WeChat Developer Tools.

### 10. What you should see

The fixture's host page renders the shared greeting, the counter, and the SDK version, and its Console
reports:

```text
fixture.greeting=Hello, WeChat, from commonMain
fixture.counted=5
fixture.sdkVersion=0.1.0-SNAPSHOT
fixture.result=PASS
```

That output is real-host evidence: the fixture bundle was loaded in WeChat Developer Tools at base
library **3.17.3** and produced exactly these lines. The version line is the historical one —
`0.1.0-SNAPSHOT` was the version catalog value at that run, and a build from this tree prints
`0.1.1` instead; the other three lines are unchanged. It is the acceptance for *this bundle shape on
this host*, not a statement about your mini program, other base libraries, other WeChat capabilities,
or other hosts.

`miniApp/scripts/host-smoke.cjs` runs the same `require()` on Node so module wiring can be checked
without Developer Tools, and `assembleMiniApp` copies it into the distribution so the same check can
be run there. That is module wiring on Node, not host acceptance, and the two are recorded separately.

## What is source and what is output

Four directories are involved and confusing two of them is the mistake this layout exists to prevent:

| Path | Kind | Who owns it | `./gradlew clean` |
| --- | --- | --- | --- |
| `miniApp/` | source | you | does not remove it |
| `miniApp/miniprogram/libs/` | generated | `syncMiniApp` | does not remove it; the next sync refills it |
| `build/miniapp/bundle/` | build output | `assembleMiniAppBundle` | removes it |
| `build/miniapp-dist/` | build output | `assembleMiniApp` | removes it |

Every path is configurable and every one has the default above:

```kotlin
miniapp {
    wechat {
        bundleDirectory.set(layout.buildDirectory.dir("miniapp/bundle"))
        sourceDirectory.set(layout.projectDirectory.dir("miniApp"))
        generatedLibraryDirectory.set(sourceDirectory.dir("miniprogram/libs"))
        distributionDirectory.set(layout.buildDirectory.dir("miniapp-dist"))
    }
}
```

Each of them is validated before it is written to. A `Sync` deletes what its destination holds and
its sources do not, so the source host must stay inside your project, the generated library area must
stay strictly inside the source host, and the distribution must stay inside your project. A rejected
configuration fails before anything is copied or deleted.

```shell
./gradlew assembleMiniAppBundle
```

It is unchanged since 0.1.0 and still writes the compiler-managed bundle to `build/miniapp/bundle`. It
is the lower-level contract `syncMiniApp` and `assembleMiniApp` are built on: both republish exactly
the Kotlin/JS production library — the modules the compiler declares as its output — rather than a
path written into the plugin, so a Kotlin Gradle Plugin layout change moves the bundle with it.

## The module contract between the host and the plugin

- **CommonJS.** The compiled output is CommonJS (`useCommonJs()`). Your page requires the module by
  relative path, the module requires its runtime siblings the same way, and `package.json` names the
  entry file. `verifyMiniApp` resolves every `require()` in the distribution and rejects anything that
  is not a relative path or that points outside it, because a distribution ships no `node_modules`.
- **TypeScript is the host's choice, and Developer Tools is the only TypeScript compiler.** The
  generated project is plain JavaScript; a host that wants TypeScript adds `.ts` pages and turns on
  Developer Tools' `typescript` compiler plug-in. Nothing in the plugin compiles TypeScript, so there
  is exactly one compiler and no second build step to keep in step.
- **`.d.ts`.** The distribution carries exactly one declaration — your module's — and it is
  self-contained: the runtime SDK's exported types are inlined into it. It is what a TypeScript host
  page type-checks against, and `verifyMiniApp` requires it to be present and to match the module.
- **Source maps.** `*.js.map` files are part of the compiler's declared output, and both the generated
  area and the distribution carry them, because the `.js` files reference them by
  `sourceMappingURL`. They are debug metadata: their `sources` entries name the original Kotlin files
  by relative path, so they do not resolve outside your project. `verifyMiniApp` rejects an absolute
  path in one — that would pin the artifact to the machine that built it — but accepts the relative
  source paths, which are inert at runtime.
- **Node smoke is not host acceptance.** Running the bundle on Node proves the module graph resolves;
  it runs on a different runtime from the one that will load your mini program. Only WeChat Developer
  Tools, and then a real device where a capability needs one, is host acceptance.

## Multi-module root WeChat host (current source, not released)

A multi-module consumer can select its one final artifact module (for example `shared`) as the root host owner:

```kotlin
miniapp {
    wechat {
        rootHost.set(true)
    }
}
```

This retains Kotlin sources and the bundle in the module, provides `miniApp/` at the consumer root, and places the standalone acceptance project in root `build/miniapp-dist/`. Root entry points delegate to the selected module:

```shell
./gradlew initMiniApp
./gradlew syncMiniApp
./gradlew assembleMiniApp verifyMiniApp
```

Initialization is explicit and creates missing files only; synchronization owns only `miniApp/miniprogram/libs/`; `clean` preserves handwritten host source. This configuration is not yet in the public 0.1.1 release. The repository's [multi-module consumer](fixtures/miniapp-root-consumer) uses current source; see the [development guide](docs/DEVELOPMENT-en.md) for complete safety and compatibility rules. Renderer checks still apply; root paths do not solve CMP resource compatibility.

## Online resolution

Gradle resolves plugin `io.github.bobcgn.miniapp:0.1.1` from the Gradle
Plugin Portal. The plugin then adds runtime `io.github.bobcgn:kmp-miniapp-sdk:0.1.1`, which Gradle
resolves from Maven Central. A consumer does not need this repository's source, `includeBuild`, an internal module path, or a copied
artifact. See [CONSUMER_SETUP-en.md](docs/CONSUMER_SETUP-en.md) for the complete standalone-project
instructions.

## What the plugin deliberately does not do

- It does not render, and it rejects a build whose runtime classpath carries Compose, Skiko or a
  browser UI runtime, because such a bundle would build successfully and still be unusable in a host.
- It does not generate WXML or WXSS.
- It does not compile TypeScript, start WeChat Developer Tools, or upload a mini program.
- It does not publish a consumer project or its host artifacts; repository release automation is a
  maintainer concern, not plugin behaviour.
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
./gradlew verifyMiniAppBundleSize
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
