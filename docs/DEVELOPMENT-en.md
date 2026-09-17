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

Before starting or closing WeChat capability work, review the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md). It distinguishes implemented, partial, planned, unsupported, and P3 presentation capabilities and records the verification levels required for each entry.

When running WeChat host acceptance, use the [WeChat real-host verification matrix](platforms/wechat/WECHAT_HOST_VERIFICATION-en.md) to select the minimum environment and complete its evidence template. A DeveloperTools result must not replace real-device, permission, privacy, or backend-chain evidence.

## Commands

```shell
./gradlew projects
./gradlew clean build
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :kmp-miniapp-sdk:jsTest
./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries
./gradlew :miniapp-gradle-plugin:test
./gradlew buildMiniAppSdk
```

These commands are valid and were verified on 2026-09-15, except `:miniapp-gradle-plugin:test`, which was verified on 2026-09-17.

The `:kmp-miniapp-sdk:jsTest` suite includes host-boundary, error-model, callback adaptation, cancellation, double-completion, optional abort, WeChat error-mapping, HTTP transport adaptation including host abort on cancellation, lifecycle state transitions, navigation adaptation, capability-support states and version gating, permission lifecycle behaviour including concurrent request merging, privacy authorization including the refusal classification, WeChat session checking including the expiry classification, the WeChat clipboard and vibration adapters including their per-capability gating, the WeChat file-system adapter including its sandbox and text boundaries, the WeChat location adapter including its coordinate validation and privacy precondition, the WeChat scan adapter including indeterminate-interruption classification and result validation, the WeChat media adapter including its request boundaries, result validation, and interruption classification, the WeChat subscription adapter including its template validation and per-template result policy, the network status adapter including its per-collector listener pairing, the WeChat upload and download adapters including their abort, progress, and timeout behaviour, the WeChat payment adapter including its parameter forwarding, blank-parameter refusal, exact interruption classification, and single-terminal-state behaviour, and interop object-construction coverage. These tests use fake callbacks and do not claim access to a real `wx` runtime.

## Architecture boundaries

The runtime SDK shares client behaviour, host capabilities and presentation state; it never renders. [ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-en.md) fixes the four layers, and [ARCHITECTURE-en.md](ARCHITECTURE-en.md) carries the responsibility matrix.

```shell
./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries
```

This task fails when anything under `sdk/**` imports a UI framework namespace or declares a UI framework dependency, and it runs as part of `:kmp-miniapp-sdk:check`. It is a build failure rather than a review note on purpose: "the SDK does not depend on Compose" has to survive contributors who have not read the ADR.

A dependency is matched by group when the group exists only to serve a UI framework, and by exact module coordinate when the group also carries a dependency this SDK legitimately uses. `org.jetbrains.kotlinx` carries `kotlinx-coroutines-core`, so the whole group cannot be forbidden; `kotlinx-html` and its `-js` / `-jvm` variants are forbidden as modules instead. Before the task trusts the classifier on the real model, it runs it against a fixed table of expected verdicts and fails if any coordinate is classified against expectation, so a later edit cannot silently widen or narrow the rule.

What the check does not do: it cannot detect a renderer that imports no UI framework. A WXML generator or a view tree assembled from the SDK's own types would pass it. That class of mistake is still caught by review and by the rules in `sdk/AGENTS.md`, not by the build.

## Mini App Gradle plugin

`:miniapp-gradle-plugin` publishes the `io.github.bobcgn.miniapp` plugin from the implementation class `io.github.bobcgn.miniapp.gradle.MiniAppGradlePlugin`. Applying it to a Kotlin Multiplatform project does three things:

- registers one Kotlin/JS target named `miniapp`, which is what makes `miniappMain` and `miniappTest` real, compilation-owned source sets with `commonMain` and `commonTest` as their parents;
- registers that target's Node.js test run, which is what gives `miniappTest` a task that actually executes;
- adds the runtime SDK to `miniappMain`'s api configuration, which is what lets consumer code compile against the public API without declaring an artifact.

The runtime is a **public module coordinate**, `io.github.bobcgn:kmp-miniapp-sdk:<version>`, so the plugin never names this repository's project paths — a consumer build has no access to them, and `project(":kmp-miniapp-sdk")` is not available to it. The runtime's directory is `sdk/`; its project name is `kmp-miniapp-sdk`, because that name is the artifact id composite-build substitution matches and a publication would publish. `miniappTest` inherits the runtime through the source-set hierarchy; no other source set and no other target receives it.

### The `miniapp { }` extension

The plugin registers one extension named `miniapp`. Its configuration is the current host, and the host's only setting is where its bundle is written:

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.bobcgn.miniapp")
}

miniapp {
    wechat {
        bundleDirectory.set(layout.buildDirectory.dir("miniapp/bundle"))
    }
}
```

| Field | Default | What it changes |
| --- | --- | --- |
| `wechat.bundleDirectory` | `build/miniapp/bundle` | where `assembleMiniAppBundle` writes the host bundle |

Both the block and the field are optional. `bundleDirectory` defaults to `build/miniapp/bundle`, so a consumer that configures nothing gets exactly the behaviour the plugin had before this extension existed.

**Why `wechat { }` exists at all.** A Mini App platform is not a host: the platform is what the sources and the runtime SDK are written against, and a host is the runtime that loads the bundle. `MiniAppExtension` is the platform; `WeChatHostConfiguration` is a host. Adding a second host means adding another `MiniAppHostConfiguration` subtype and another accessor on the extension — it does not mean widening the WeChat one, and it does not mean root-level `wechatXxx` properties that would make the platform look WeChat-shaped.

**Why so little is configurable.** Every field in a Gradle DSL is a decision a consumer has to make, so this one carries only what a build genuinely needs to agree with its host about. The Kotlin/JS output shape, CommonJS, TypeScript declarations and the renderer boundary are not consumer choices — they are what the plugin exists to decide, and exposing them would put the wiring back into the consumer's build script. The extension changes neither `checkMiniAppHostBoundary`, which is a platform-level guarantee, nor the host output shape.

**What is deliberately not here.** Application identifiers and secrets, merchant and payment material, order or signature data, API tokens, user identity, subscription template identifiers, permission state, presentation state, page routes, markup, view definitions, and any other business or backend configuration. Those are not build inputs. A Gradle extension is not a second copy of a host's console, and the SDK does not model them at build time for the same reason it does not model them at runtime.

**The one validation.** `bundleDirectory` must be a directory inside the project. `assembleMiniAppBundle` is a `Sync`, which deletes files in its destination that the bundle does not contain, so a directory outside the project — or the project directory itself — would be deleted rather than assembled into. The failure names the field, both paths and the expected value. Writing into a mini program folder inside the project is allowed; relaxing the rule needs a real external case.

### The host bundle

The plugin configures the Mini App target's Kotlin/JS output for a host: a CommonJS library binary with TypeScript declarations, on top of the Node.js test run. The consumer writes none of `useCommonJs()`, `binaries.library()`, `generateTypeScriptDefinitions()` or `nodejs()`.

```shell
./gradlew assembleMiniAppBundle
```

The task republishes the Kotlin/JS production library at `build/miniapp/bundle/`. It is a `Sync`, so a runtime module that leaves the compilation also leaves the bundle, and it copies the distribution task's *declared output* rather than a hand-written path, so a Kotlin Gradle Plugin layout change moves the bundle instead of quietly emptying it. It reads and writes nothing outside the Mini App target.

For a project whose `miniappMain` uses the runtime SDK, the bundle looks like this:

```text
build/miniapp/bundle/
  <project>-miniapp.js        the consumer's entry module: its compiled code and its @JsExport
                              declarations, re-exporting the runtime's namespace and requiring
                              the runtime module below
  <project>-miniapp.d.ts      the declaration the thin host code compiles against
  kmp-miniapp-sdk-kotlin.js   the runtime SDK, as its own module
  kotlin-kotlin-stdlib.js     the Kotlin runtime its compilation resolved
  kotlinx-coroutines-core.js
  kotlinx-atomicfu.js
  kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js
  package.json
  *.js.map                    external source maps, for local host-side debugging
```

What the contract promises: the consumer's entry module, its TypeScript declaration, the runtime SDK module, the Kotlin runtime modules the compilation resolved, and `package.json`. What it deliberately does not promise: a fixed list of runtime modules. That set is whatever the compilation resolves, so it moves when the SDK's own dependencies move; a host loads the entry module and whatever that module requires.

Two consequences are worth stating because they surprise people:

- **A Kotlin/JS library exports only `@JsExport` declarations, and eliminates everything else.** A consumer's host-facing surface is therefore its own `@JsExport` declarations in `miniappMain`; code nothing exports never reaches the bundle. This is why the fixtures export what they assert on.
- **The runtime SDK is a separate module, not inlined** into the consumer's module, because the runtime declares its own Kotlin/JS output module name. The bundle therefore has more than one JavaScript file a host must load, and the entry module requires the rest.

### The Mini App source set's architecture precondition

`miniappMain` inherits `commonMain`, so whatever a common source set carries reaches the Mini App compilation. It may inherit shared behaviour, models and state. It may **not** inherit Compose UI, Skiko, or a browser renderer: Compose UI belongs in a client module or source set that is not a parent of `miniappMain`, such as a Compose client application's own UI source set.

The plugin enforces that instead of only documenting it:

```shell
./gradlew checkMiniAppHostBoundary
```

The check resolves `miniappRuntimeClasspath` — the exact set the distribution is built from — and fails when it carries Compose, Skiko, a Compose-specific AndroidX integration artifact, `kotlinx-browser` or `kotlinx-html`. Non-Compose lifecycle, saved-state and navigation primitives are allowed. It runs before `assembleMiniAppBundle`, so a distribution no host could run is never produced. It is a **dependency** rule, not a file rule: the files in a Kotlin/JS distribution reach one another through `require()`, so deleting the ones that look like a renderer would turn a build failure into a load failure.

A consumer that already had a Kotlin/JS target before applying the plugin may need to run `./gradlew kotlinUpgradeYarnLock` once. Applying the plugin adds a target, which changes the npm dependency set, and the Kotlin Gradle Plugin will not overwrite an existing yarn lock on its own.

**What none of this establishes by itself:** that a host can load the distribution. Neither task success nor the boundary check is host evidence. That evidence is recorded separately: the consumer fixture distribution loaded in WeChat Developer Tools at base library 3.17.3 and reported `fixture.result=PASS`.

### Where the version lives

`gradle/libs.versions.toml` holds the one version the runtime SDK and this plugin share. The plugin's build generates `miniapp-plugin-metadata.properties` from it, and the plugin reads the coordinate from that resource at runtime, so neither the plugin's source nor its tests contain the version as a literal.

The SDK is not published yet, so the coordinate resolves through a composite build today and from a repository after publication, with the same group, name and version either way. A consumer that cannot resolve it fails with Gradle's dependency-resolution error naming the coordinate; the plugin deliberately does not fall back to another version, another artifact, or a project path.

The plugin creates no source set and no `dependsOn` edge of its own: hand-built ones are the unused-source-set model [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) rejected. Applying it to a project that does not apply the Kotlin Multiplatform plugin fails with a message naming the missing plugin.

```shell
./gradlew :miniapp-gradle-plugin:test
```

The plugin's tests are layered: a contract test for the descriptor, the public coordinate, the
extension's shape and the renderer classifier; TestKit fixtures for everything that only exists
inside a real build; and the consumer fixture for the path an outside project takes. The single entry
point that runs all of it is [described in TESTING](TESTING-en.md#gradle-plugin-integration-suite).

```shell
./gradlew --no-daemon --console=plain verifyMiniAppGradlePluginIntegration
```

A consumer whose `commonMain` depends on another Kotlin Multiplatform project must apply this plugin to that project as well. The dependency resolves through a Mini App variant, so a project that offers none fails variant resolution; the plugin provides no fallback, and moving the dependency out of `commonMain` is not a supported workaround.

The tests are Gradle TestKit fixtures plus a contract test. The fixtures put the Kotlin Gradle Plugin and the plugin under test on one buildscript classpath deliberately: `withPluginClasspath()` injects the plugin under test into the plugin-resolution classpath only, so a fixture that resolves a second plugin separately cannot reproduce the classpath a real consumer build gives the plugin. One fixture carries `commonMain`, `miniappMain`, `commonTest` and `miniappTest` sources and asserts the executed test report, so the source-set wiring and the test execution are both proved by running tests rather than by reading the model. The fixtures consume the runtime through a composite build (`includeBuild` of this repository), which is how the unpublished coordinate resolves; the plugin itself never sees that path.

The extension carries one setting — where the host's bundle is written. Business and host configuration stays outside Gradle by design, and the reasons are in the [mini app extension section](#the-miniapp--extension) above.

## Mini App Gradle plugin model PoC

`poc/kgp-model` is a standalone Gradle build that compares the Kotlin Gradle Plugin models for `miniappMain` / `miniappTest`. The root build does not include it, so it never runs as part of the SDK build. Run it from its own directory with the repository root's checked-in Wrapper:

```shell
cd poc/kgp-model
../../gradlew --no-daemon --console=plain clean check
../../gradlew --no-daemon --console=plain :model-c:miniappTest
../../gradlew --no-daemon --console=plain :model-c:checkMiniAppModel
../../gradlew --no-daemon --console=plain :model-a:reportKgpModel
```

The modules are:

| Module | What it is |
| --- | --- |
| `model-a` | A named Kotlin/JS target declared in the consumer build script. Control experiment; rejected as a delivery model. |
| `model-b` | Custom source sets with nothing behind them. Reproduces the unused-source-set failure. |
| `model-c` | A PoC plugin (`io.github.bobcgn.miniapp.poc`) that owns the Kotlin/JS target. The chosen model. |
| `model-c2` | A PoC plugin that keeps an internal `js` target and still tries to expose `miniappMain`. Reproduces the rejected hidden-target variant. |

`reportKgpModel` prints a module's model shape; `checkMiniAppModel` asserts ten model requirements and fails when any is not met. `model-b` and `model-c2` are expected to fail that check — they exist as the executed evidence for the rejected models, not as passing modules. `model-a` fails the one requirement that the consumer build script contain no manual Kotlin/JS wiring.

The decision, the rejected alternatives, and the Kotlin Gradle Plugin limitations are recorded in [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md). The PoC is experimental tooling: it does not ship in the SDK and it defines no SDK public API.

## Consumer integration fixture

`fixtures/miniapp-consumer` is an ordinary Gradle build beside this one, not a module of it. It exists to prove the consumer path the way an outside project experiences it: the plugin is resolved by id through `pluginManagement { includeBuild }`, the runtime arrives as the public coordinate `io.github.bobcgn:kmp-miniapp-sdk`, and nothing in it names an internal.

```shell
cd fixtures/miniapp-consumer
../../gradlew --no-daemon --console=plain clean miniappTest assembleMiniAppBundle verifyConsumerContract
```

What it proves by running:

| Evidence | Where it comes from |
| --- | --- |
| the plugin resolves by id, with no version | `plugins { id("io.github.bobcgn.miniapp") }`, in a build that has never seen this repository |
| `miniappMain` and `miniappTest` exist and compile | `src/miniappMain`, `src/miniappTest` |
| `miniappMain` reuses `commonMain` | `hostGreeting()` calls `greeting()` from `src/commonMain` |
| `miniappTest` runs its own tests *and* the shared ones | `build/test-results/miniappNodeTest/` holds `consumer.SharedTest` (2 tests) and `consumer.MiniAppTest` (3) |
| the runtime SDK resolves automatically | `sdkVersion()` calls `MiniAppSdk.VERSION` |
| the bundle satisfies its contract | the `verifyConsumerContract` task |
| the DSL takes effect | the same task asserts the configured directory, and the host run passes `-PminiappBundleDirectory=host/miniprogram/libs` |

The consumer's build script contains no source-set declaration, no `dependsOn`, no `useCommonJs()`, no `generateTypeScriptDefinitions()`, no runtime artifact coordinate and no copy step. Besides the two plugins it declares exactly one thing: `kotlin-test` for `commonTest`, which every Kotlin Multiplatform project does.

### Running it from the repository

```shell
./gradlew --no-daemon --console=plain verifyMiniAppConsumerFixture
```

That runs the fixture three ways: the default bundle directory, the host's directory, and the host's own consumption path. These tasks are not wired into `check`, because they compile Kotlin/JS and install npm dependencies.

### The WeChat host

`fixtures/miniapp-consumer/host` is a minimal WeChat mini program: WXML, WXSS and thin JavaScript that calls the assembled bundle. It is Host UI, so markup stays here and never enters the SDK or the fixture's shared Kotlin.

Its `miniprogram/libs/` is build output and is git-ignored; run `verifyMiniAppConsumerHostBundle` before opening the project in WeChat Developer Tools. The fixture completed Developer Tools acceptance at base library 3.17.3: the page rendered `Hello, WeChat, from commonMain`, `5`, and `0.1.0-SNAPSHOT`, and the Console reported `fixture.result=PASS`. The host also has a Node smoke check that loads the bundle exactly the way the page does:

```shell
cd fixtures/miniapp-consumer/host
node scripts/host-smoke.cjs
```

That is a module-wiring check on Node, not host acceptance; the two are recorded separately.

## Consumer Bridge

Build and prepare every artifact required by the WeChat integration host:

```shell
./gradlew buildMiniAppSdk
```

This task builds the Kotlin/JS production library and TypeScript declaration, then synchronizes the required modules into the example. The stable compiler output names are:

```text
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-kotlin.js
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-kotlin.d.ts
sdk/build/dist/js/productionLibrary/kotlin-kotlin-stdlib.js
sdk/build/dist/js/productionLibrary/kotlinx-coroutines-core.js
sdk/build/dist/js/productionLibrary/kotlinx-atomicfu.js
```

The example consumes a separate, consumer-facing copy under:

```text
examples/wechat-miniprogram/miniprogram/libs/
```

The directory contains two hand-maintained normalization files, `kmp-miniapp-sdk.js` and `kmp-miniapp-sdk.d.ts`. `buildMiniAppSdk` preserves those files and synchronizes the compiler module, its declaration, and the three runtime modules around them. Treat every other file in this directory as task-owned output; do not add unrelated files there.

External `.js.map` files are generated and copied beside each distributed JavaScript module for local WeChat debugging. They embed source content, are reproducible, and are ignored by Git. The JavaScript and `.d.ts` distribution files remain checked in so the example can be opened directly, but `buildMiniAppSdk` is the authoritative refresh path.

Run the local consumer checks:

```shell
cd examples/wechat-miniprogram
npm install
npm run smoke
npm run typecheck
```

The smoke test loads the consumer-facing CommonJS module, calls `sdkVersion()`, and installs a fake global `wx` to verify Storage, the WeChat login bootstrap, the HTTP transport, lifecycle forwarding, all three navigation calls, runtime capability detection, the permission lifecycle, privacy authorization, the WeChat session check, the clipboard and vibration capabilities, the file system, location, scanning, media selection, subscription requests, the network extensions (network status, upload, and download), and standard payment. It also asserts what the HTTP transport handed to the fake host, including the raw-text response mode, the absolute page paths received by navigation, and the support state reported for each gated capability, that a scan, a media selection, a subscription request, and each network extension query and request no permission, how an ended interaction is told apart from a similar-looking failure, that a payment reaches the host with exactly the five fields it accepts and that its resolved value carries no order field, that the host's own status line is never read as a template id, that observing network status registers one listener and removes it, and that aborting a transfer stops the host task once and cleans up its progress listener. This validates module and adapter behavior but is not real-host evidence. TypeScript runs in strict mode and validates the Promise-based Storage, typed `WeChatLoginResult`, HTTP transport, lifecycle, navigation, capability-support, permission, privacy, session-check, clipboard, vibration, file-system, location, scan, media-selection, subscription-request, network-status, transfer, and payment exports without `any`.

Real-host verification follows the checklist in [TESTING-en.md](TESTING-en.md), which is the authoritative list of required page values, console lines, and preparation steps. A capability is recorded as host-verified in [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) only after that run.

The Consumer Bridge and Storage capability were verified on 2026-09-14 in WeChat Developer Tools Stable 2.01.2510290 using user-provided visual evidence. The page displayed `0.1.0-SNAPSHOT`, reported Storage `PASS`, and showed `first=first, overwritten=second, missing=null`.

Authentication real-host acceptance was completed on 2026-09-14 using user-provided WeChat Developer Tools evidence. The page displayed `Client login code: PASS`, `codeReceived=true`, and a code length of 32 without showing the credential itself. The example intentionally never logs or renders the raw credential. A `wx.login` code is short-lived and must be sent to a trusted consumer backend for WeChat exchange; receiving it does not authenticate a user, create an SDK session, or authorize requests.

The HTTP transport capability was accepted in a real host on 2026-09-14 using user-provided WeChat Developer Tools confirmation. Its adapter, error mapping, and cancellation behavior remain covered by automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks; the real-host run confirms that the network card on the index page reaches `wx.request` successfully.

The lifecycle and navigation bridges completed WeChat Developer Tools acceptance through user confirmation on 2026-09-15. The run covered the foreground lifecycle state, the current page route, and the complete page-stack path: `navigateTo` opened the second page, `redirectTo` replaced it with the third page, and `navigateBack` returned directly to the index page. The background-state transition still requires placing the mini program in the background on a real device and is not covered by the Developer Tools simulator.

A passing Node/CommonJS smoke test or TypeScript check does not establish a passing WeChat Mini Program integration. Record the two results separately.

Runtime capability detection completed Developer Tools (base library 3.17.2) and Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76) acceptance on 2026-09-15, both reporting `runtime-detection=Supported`, `storage=Supported`, and `ungated=Unsupported`. The lowest debug base library WeChat Developer Tools currently offers is 2.21.4, so a host below 2.20.1 cannot be constructed; `VersionDependent` is therefore covered by automated tests rather than a real-host screenshot. The checklist in [TESTING-en.md](TESTING-en.md) describes how to produce each case.

### WeChat privacy configuration

WeChat gates the personal-data APIs a mini program declares behind its own privacy contract, separately from system permissions. Two things belong to the mini program's backend and cannot be done or replaced by this SDK:

- The collection types must be declared in the MP backend under 设置 → 服务内容声明 → 用户隐私保护指引. A mini program that declares nothing is reported as needing no authorization, so a `NOT_REQUIRED` reading does not prove that a user ever agreed.
- From base library 2.32.3 the host intercepts privacy-gated calls. Below that version it does not intercept them at all, which is why this capability reports `UnsupportedCapability` rather than pretending the condition does not exist.

The SDK informs and gates; it does not generate a privacy policy, decide whether a business is compliant, or provide legal advice.

To observe privacy state in WeChat Developer Tools, open the index page and use the Privacy Authorization card: `Refresh privacy status` queries the host without any side effect, and only the `Request privacy authorization` button starts a prompt. The debug base library must be 2.32.3 or later for the card to reach the host at all.

A recorded answer belongs to the account, the device, the backend configuration, and the account's history with the mini program, so the same build can report `REQUIRED` on one account and `NOT_REQUIRED` on another. Clear the account's acceptance in the Developer Tools cache, or use another account, to see `REQUIRED` again; do not reset a device or an account destructively to produce a state.

Privacy and permission acceptance are recorded separately in the verification matrix, because the host tracks them separately and a result for one says nothing about the other.

The permission lifecycle completed Developer Tools and Android-device acceptance for both microphone and location on 2026-09-15. The microphone run covered `Granted`, `Denied`, the settings return, and a repeated request after refusal; the location run additionally produced `NotRequested`, `Granted` after an explicit request, and `Denied` after refusal. The automated suite still never requests a permission, because a real prompt requires a user gesture.

The WeChat session check passed Android real-device acceptance on 2026-09-15: OnePlus PLQ110, Android 36, WeChat 8.0.76, base library 3.17.3 [1641]. The console reported `check #1, state=Invalid` before the login bootstrap acquired a new code and repeatedly reported `state=Valid` after `wx.login` succeeded. This result is still not evidence of identity, and session-check acceptance remains separate from login-bootstrap acceptance.

The WeChat clipboard and vibration capabilities completed real-host acceptance on 2026-09-15. Developer Tools at base library 3.17.2 and an Android device both verified clipboard write, read-back, and `matched=true`; the OnePlus PLQ110 running Android 36, WeChat 8.0.76, and base library 3.17.3 [1641] reported both vibration calls as PASS, and the tester confirmed feeling both the short and long vibrations. `getClipboardData` is not an allowed `app.json.requiredPrivateInfos` entry, so the example does not declare it there.

### WeChat location configuration

`wx.getLocation` needs more backend preparation than any other capability here, and none of it can be done or replaced by this SDK:

- The mini program's category must be one WeChat accepts for location, and the interface must be activated under 开发 → 开发管理 → 接口设置.
- For releases after 2022-07-14, `app.json` must list `getLocation` in `requiredPrivateInfos` and must state a purpose in `permission.scope.userLocation.desc`; the example does both.
- The user must grant `scope.userLocation`.

WeChat's own simulator locates by IP and supports `gcj02` only, so a simulator result is not evidence about a device's real position. Its call-frequency rules also differ between build kinds, so a second call in the same session may return the first position rather than a fresh one.

Reading a position is not something the page may do on its own: the example's Location card calls it only from a tap, and never displays or logs the coordinates.

WeChat location completed Developer Tools and Android-device acceptance on 2026-09-15. The runs covered a `Supported` capability, permission moving from `NotRequested` to `Granted`, blocking with `DENIED` before `getLocation` after refusal, and successful location again after permission recovery; success logs report only coordinate and accuracy validity and never actual coordinates. The current scope implements only one-shot on-demand `getLocation`, not `chooseLocation` or `openLocation`.

### Indeterminate WeChat scan interruption

`wx.scanCode` opens WeChat's own scanning interface, so the SDK implements no interface and models no camera. The example's Scanner card calls it only from a tap and never displays or logs the scanned content, `rawData`, the character set, or the image path.

A device run proved that dismissing the scan interface and system camera access preventing it from opening both produce the host's cancel signal. The SDK therefore no longer interprets that signal as user intent or permission denial; `HostInteractionInterrupted` reports that the interaction produced no result and its cause is indeterminate. Only `scanCode:cancel` and `scanCode:fail cancel` match; similar but different messages remain `HostFailure`.

Developer Tools is not a device: its scan implementation has the user pick an image and then decodes it, so the camera path, what `onlyFromCamera` actually does, and the device's dismissal text all have to be confirmed on hardware.

The capability queries and requests no permission. `scope.camera` does exist on this host, but nothing citable ties it to `wx.scanCode`, so the SDK neither prompts for it nor registers it as a permission precondition, and the example adds nothing to `app.json.requiredPrivateInfos`.

A page must not read the scanned content itself: the example reports only whether content was present, whether the format the host named is one the SDK recognizes, and whether the outcome was a dismissal or a failure.

WeChat scanning has Developer Tools and Android-device coverage for `Supported` and successful scanning. The device also proved that dismissal and camera restriction produce the same indeterminate interruption, so the capability does not claim to separate them. It is recorded `Stable` in the capability matrix: implementation, automated tests, and the required host-verification evidence are all in place, and the host's ambiguity is preserved explicitly in the error model instead of being presented as a distinction the host does not make.

### WeChat media selection

`wx.chooseMedia` opens WeChat's own picker, so the SDK implements no picker and models no camera. The example's Media card calls it only from a tap and never displays or logs what was selected or the temporary paths it arrived in.

The SDK validates what WeChat states exactly and refuses the rest: `mediaType` is required because WeChat's own schema marks it so, `maxDurationSeconds` must fall inside the 3-to-60 seconds WeChat documents, a byte count must be a whole non-negative number, and a temporary path must be a non-empty string. The host's own limit on `count` is deliberately not duplicated in the SDK, because it depends on the base library, so the SDK asks for what the caller asked for and reports what comes back.

The installed Developer Tools base library reports its simulated picker dismissal as `chooseMedia:cancel`, while an Android device reports a manual dismissal as `chooseMedia:fail cancel`; neither carries a structured cause. Those two exact signals map to `HostInteractionInterrupted` rather than inferred user intent. Every other failure remains `HostFailure`. On the verified Android host, restricting media access produces the same indeterminate interruption as manual dismissal, so the SDK does not invent a permission classification the host cannot support.

The capability queries and requests no permission: WeChat's own picker needs none, and the host's scope list holds no scope for reading the media library. Writing to the album is a different capability with its own APIs (`saveImageToPhotosAlbum` and `saveVideoToPhotosAlbum`); it is not implemented here, because this work has no requirement for it and nothing citable ties `scope.writePhotosAlbum` to it.

The paths a selection returns are host temporary resources. The SDK keeps no copy and makes no claim about their lifetime, so a caller that needs the media later must copy it to storage it owns.

WeChat media selection completed real-host acceptance on 2026-09-16. Automated checks, Developer Tools, and an Android device cover `wechat.choose-media=Supported`, image, video, mixed, camera, manual-dismissal, and restricted-media-access paths without logging media content or complete temporary paths. Manual dismissal and restricted access produce the same indeterminate interruption on that host; the capability is `Stable` because this ambiguity is preserved explicitly.

### WeChat subscription requests

`wx.requestSubscribeMessage` puts WeChat's own prompt in front of the user, so the SDK implements no prompt and sends no message. The example's Subscription Message card asks the host only from a button, and only when a test template has been configured locally; with none configured it reports `NOT CONFIGURED` and makes no host call at all. Template ids are host identifiers belonging to the mini program's account, so none is committed: the card's list is empty in the repository and the page never displays or logs an id.

**This capability has the thinnest evidence in the project, and the code is shaped by that.** The installed Developer Tools base library declares the API in its metadata table — option `tmplIds`, and a success result keyed by template id with `errMsg` beside it — but ships no implementation of it and no documentation schema. Nothing in the offline sources therefore names this API's status vocabulary or its dismissal message. The only status string observed anywhere in that bundle is `accept`, in the simulator's subscription-prompt preview payload, which is prompt-rendering data rather than an API result. The SDK recognizes exactly that one status and preserves every other non-blank status verbatim. It classifies no dismissal signal yet: cancel-looking failures remain `HostFailure` until this API produces real-host evidence. The example logs only a closed diagnostic label, never the raw message or a template id.

The SDK validates the templates and response correlation rather than guessing the answer vocabulary: an id must be non-blank, duplicates are collapsed with the caller's order kept, and no count limit is enforced because no offline source states one. The response must contain exactly the requested template keys, each with a non-blank text status; missing, unexpected, blank, or non-text entries are `InvalidResponse`.

The caller owns the user gesture, and the requirement is a real one: WeChat refuses the request without one. Nothing in the SDK or the example triggers it on load, and nothing retries a refusal.

The capability queries and requests no permission, because no offline source ties a scope or a privacy condition to this API. That is a conclusion about the evidence, not a finding that none exists, so the manual run watches for a prompt rather than assuming either way; BOB-60's privacy work being unfinished neither implies nor rules out a condition here. No `app.json` declaration is added either.

Acceptance is a subscription state, never delivery. End-to-end delivery needs a WeChat backend template and a trusted backend, which is `BackendRequired` and outside what any adapter test can establish.

WeChat subscription requests have automated coverage plus partial real-device evidence. An Android run on 2026-09-16 verified runtime support and the `templateCount=0` guard. That guard intentionally makes no host call, so the absence of a consent prompt is the expected result, not a permission failure. Acceptance, refusal, dismissal, multi-template correlation, and delivery remain **blocked** on a valid test template configured for the same AppID. The capability is `Partial`.

### WeChat network extensions

Five host APIs are adapted here: the network type query, the network status change listener, and the file upload and download. The contract comes from the base library shipped with the installed Developer Tools, which enumerates the connection kinds for both the query and the event, the option and result fields for both transfers, the members of both task types, and the timeout message its own error mapping produces.

Network status became a common capability and the transfers did not, and the difference is the model rather than the name. A host can always say whether it is online and over what link, and saying so needs nothing host-specific; an upload or a download is built on a path in the host's own file system, and this SDK deliberately has no portable file reference, so promoting them would mean inventing one for a single implementation. The portable half of a transfer — method, URL, headers, status, text body — is already the transport capability.

The query and the listener are gated separately, because a host may answer the question without offering events, and the listener requires both of its halves: registering without being able to remove would leak the listener for the lifetime of the mini program, so a host that has only `on` is reported unsupported. Each collector of `changes` registers its own host listener and removes it when it ends, which makes the pairing exact on every termination path — including a malformed event, which ends that collector with `InvalidResponse` rather than making the stream lie about the host. Nothing polls, and no reading is synthesized on collection; a consumer that wants a starting point calls the query.

The transfer adapters own three things a plain suspend call could not: abort, progress, and cleanup. `abort()` stops the host task at most once and reports whether a host abort was actually invoked, so a host that returned no task is never described as stopped. Cancelling a coroutine that is waiting on a transfer stops the host task too, because a caller that stopped waiting did not ask for it to keep running. The progress listener is registered at most once — and not at all when the task cannot report progress, since a registration that could never be paired with a removal is a leak — and it is removed on success, failure, and abort alike. Progress is advisory: a figure the SDK cannot read is ignored rather than failing a transfer that is working, and a transfer that reports none has no progress value rather than zero per cent.

A completed transfer is a completed exchange, so a non-2xx status resolves, exactly as it does for the transport capability. An expired timeout is the host's exact `<api>:fail timeout` message; a message that merely mentions a timeout stays a `HostFailure`.

None of these APIs asks for a permission. Reaching a host requires the consumer to list it in the request domain, which is a configuration constraint on the mini program rather than something the SDK could request, and no offline source ties a privacy condition to any of them either.

The network extensions have automated coverage only. Real-host acceptance is outstanding, and two halves of it cannot be produced by automation: a real transfer needs a controlled HTTPS service in the request domain list, which is `BackendRequired` and is why the example reports `NOT CONFIGURED` until one is supplied, and a real network change needs a device whose connection can actually be switched, which no simulator stands in for. WebSocket is not implemented and stays `Planned` in the capability matrix.


### WeChat standard payment

Payment is adapted as a typed forwarder rather than as a capability the SDK reasons about. The contract comes from the base library shipped with the installed Developer Tools: `requestPayment` takes exactly `timeStamp`, `nonceStr`, `package`, `signType`, and `paySign`, constrains `signType` to `MD5` and `HMAC-SHA256`, and declares an empty success shape, while that library's own payment flow reports an ended interaction as `requestPayment:cancel`.

Everything the host is given comes from the consumer's trusted backend, and nothing in this SDK produces any of it. There is no signing, no merchant key, no prepay lookup, and no order model — not as an unimplemented feature but as the boundary itself: an order is money, and its state belongs to the backend that holds the key and receives WeChat Pay's notification. The exported result therefore says one thing, `interactionCompleted`, and no result is ever named `paid`, `settled`, or `confirmed`. Nothing about a payment is logged, cached, or persisted, and the request model refuses a blank field before the host is called, because a blank parameter names nothing the host could verify.

The failure vocabulary is deliberately narrow. Only the exact message `requestPayment:cancel` becomes `HostInteractionInterrupted`; `requestPayment:fail cancel` and everything else stay a `HostFailure`. The host's other interfaces use the `:fail cancel` form, but no evidence covers this API, and a guess there would decide whether a payment was dismissed or broken. The interruption does not claim the user cancelled, for the same reason.

Real-host acceptance is outstanding and is `BackendRequired`. A real payment needs a legal merchant account bound to this mini program's AppID, a real order, a trusted backend that signs, and a device. The simulator's payment flow is not a merchant, so nothing it produces can be recorded as a payment result. The example reports `NOT CONFIGURED` and makes no host call until parameters are supplied locally, and the capability stays `Partial` until that run happens.

The WeChat file system completed real-host acceptance on 2026-09-15. WeChat Developer Tools and an Android device both verified, at base library 3.17.2, writing the fixed test file, reading matching content, checking that it existed, removing it, and checking that it no longer existed; neither the sandbox root nor file contents were displayed or logged. Automated Kotlin/JS, fake-host, CommonJS, and TypeScript checks also pass.

Privacy authorization has automated coverage only. Its real-host acceptance is outstanding, and it depends on two things this repository cannot arrange: the mini program's declared collection in the MP backend, and the account's own answer to the host prompt. The evidence the verification matrix requires is a `REQUIRED` reading with the host's contract name, a successful acceptance that leaves the host reporting `NOT_REQUIRED`, and a refusal that is reported as `REFUSED` while the requirement stays in place.

### Planned virtual payment (not implemented)

No virtual-payment code exists, and none is planned for P1: the deliverable is the boundary, which lives in [ARCHITECTURE-en.md](ARCHITECTURE-en.md). The notes below are what an implementation would have to establish first, recorded here so that the work does not start from the standard-payment files.

Before any code, these questions must be answered from official sources, because each one changes the design:

- the parameter table and result shape of `wx.requestVirtualPayment`;
- its minimum base library version, or whether `wx.canIUse` is the only available answer;
- whether the surface is a mini program or a mini game, and which platforms expose it (Android, iOS, HarmonyOS, and the Developer Tools simulator);
- the account, subject and category eligibility rules, and whether any staged rollout applies;
- whether a permission or a backend interface permission is required;
- whether an ended interaction produces a stable, exact signal distinct from other failures;
- what the success callback establishes, and which party confirms the final transaction.

A future implementation would follow the same path as every other WeChat capability — typed `interop` declarations and presence guards, an adapter port with a production implementation, a catalog entry gated through `canIUse`, and a strongly typed export — with two additions specific to payment: its own capability key, and a request model that is never merged with `WeChatPaymentRequest`.

Independent test plan, if it is implemented:

| Level | What it must cover |
| --- | --- |
| Interop | The option bag's exact fields, with no field the official table does not prove; callback initialisation and assignment; the presence guard answering false when the API or `wx` is absent; no raw JavaScript object leaving the interop package. |
| Adapter | A complete legal request reaching the host unchanged; each required field refused when blank; success, ended interaction and failure each settling once; repeated and late callbacks not changing the first terminal answer; an ineligible account or platform distinguished from a missing API; whether the host offers a task or abort handle, decided from the real contract rather than assumed. |
| Capability | A key distinct from `wechat.request-payment`, never answering for both; API presence distinguished from account or platform eligibility; version gating through `canIUse`; a host without the API reporting `UnsupportedCapability`. |
| Consumer | The example defaulting to `NOT CONFIGURED`; no host call while nothing is configured; no sensitive parameter, credential or raw host message printed or displayed; a resolved call shown as an interaction result rather than a completed transaction; the final state verified separately against the backend. |
| Real host | Per-platform runs (Android, iOS, HarmonyOS, Developer Tools), with no result from one platform recorded for another; a legally eligible account; a real but disposable product and order; the actual signal an ended interaction produces; failure scenarios; the server-side final transaction confirmation; screenshots and Console records free of sensitive data. |

Automation cannot stand in for the eligibility or backend halves: they need a qualifying account and a real order, exactly as standard payment does.

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
