# Testing

[中文](TESTING-ch.md)

This document describes the two testing layers, the fakes each layer may use, and the reproducible checklist for a real WeChat Mini Program run.

The two layers are the top-level automation and real-host categories. Release evidence is further classified as Unit, Contract, Node, DeveloperTools, RealDevice, and BackendRequired. See the [WeChat real-host verification matrix](platforms/wechat/WECHAT_HOST_VERIFICATION-en.md) for the minimum level per capability, evidence retention, and regression timing.

## 1. Two layers, two different claims

| Layer | Runs on | Establishes | Does not establish |
| --- | --- | --- | --- |
| Automated | Node.js, through the Gradle Wrapper and the example's npm scripts | Shared Kotlin logic, adapter behavior driven through fake ports, module and declaration shape, TypeScript contract | Anything about the WeChat Mini Program runtime |
| Real host | WeChat Developer Tools | That the generated artifact compiles, loads, and runs in the intended host | Nothing further, and no regression in the host's own behavior |

**A passing Kotlin or Node.js test does not establish a passing WeChat Mini Program integration.** A passing `:kmp-miniapp-sdk:jsNodeTest`, `npm run smoke`, or `npm run typecheck` result and a real-host result are always recorded separately. Node.js is only the local build and test environment; the production host is the WeChat Mini Program JavaScript runtime.

## 2. Automated layer

### Source-set boundaries

- `commonTest` holds host-neutral expectations and must not reference `wx`, `external`, or `js()`.
- `jsTest` holds WeChat-specific expectations and may use raw JavaScript to build host result shapes.

### FakeHost boundary — `commonTest/.../testing`

Host-neutral stand-ins for shared code:

- `FakeMiniAppHost` — a `MiniAppHost` backed entirely by in-memory implementations, with configurable capability support.
- `InMemoryStorage` — the reference `MiniAppStorage`.
- `RecordingHttpTransport` — the reference `MiniAppHttpTransport`; it records requests and replies with one scripted response.

Use these when testing shared logic, so a failure identifies the contract rather than the active runtime.

### FakeAdapter boundary — `jsTest/.../host/wechat/testing`

Stand-ins for the raw WeChat callback ports, so a WeChat adapter can be driven without a WeChat runtime:

- `FakeWechatStorageHost`, `FakeWechatAuthHost`, `FakeWechatNetworkHost` — each replies immediately with one scripted outcome and can reproduce host behavior the SDK contract does not allow, such as a storage read that returns a non-string value.
- `fakeWxLoginSuccess`, `fakeWxRequestSuccess`, `fakeWxFailure`, and their siblings — builders for the raw result objects, which have no Kotlin constructors.
- `fakeAbortableTask` — the task handle `wx.request` returns, counting invocations of `abort`.

A port that must stay silent, such as one used for a cancellation scenario, belongs with that scenario rather than in this shared boundary.

### Contract checks

A contract check states one guarantee of a capability as a function that takes the implementation under test. The same check object therefore runs against the host-neutral reference implementation and against a real host adapter.

- `StorageContractChecks` — the full storage contract: absent key, write, overwrite, empty value versus absent key, removal, idempotent removal, key independence, and cross-key isolation.
- `HttpTransportContractChecks` — the guarantees that hold for every transport: a completed exchange is reported unchanged, the request is handed over unchanged, and an HTTP error status is an outcome rather than a failure.

Storage and network checks each run twice: once against the neutral implementation (`MiniAppStorageContractTest`, `MiniAppHttpTransportContractTest`) and once against the WeChat adapter (`WechatStorageContractTest`, `WechatNetworkContractTest`). A failure in the first identifies the contract; a failure in the second identifies the adapter.

WeChat client login is deliberately not a common capability, so it has no shared check object in `commonTest`. Its contract is stated in `WechatAuthContractTest` in terms of the SDK-facing adapter and driven through the FakeAdapter boundary.

Add a new capability guarantee to the shared check object rather than to one adapter's test class. Behavior that is specific to one host — which callback it uses, how its error vocabulary maps, whether an exchange can be aborted — cannot be a shared guarantee and belongs in that adapter's own suite instead.

### Commands

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

```shell
cd examples/wechat-miniprogram && npm run smoke && npm run typecheck
```

### Consumer integration fixture

`fixtures/miniapp-consumer` is a real consumer build rather than a temporary fixture: an ordinary Gradle build beside this repository that consumes the plugin by id and the runtime by its public coordinate. It is the layer that catches what a TestKit fixture in a temporary directory cannot — a consumer-shaped project that stays in the repository and is reviewed like source.

| Check | Evidence |
| --- | --- |
| The plugin resolves by id, and the runtime by its published coordinate | the fixture's `plugins { }` block and `settings.gradle.kts` |
| `miniappMain` / `miniappTest` exist and the test compilation inherits `commonTest` | the executed test report under `build/test-results/miniappNodeTest/`, which holds both `consumer.SharedTest` and `consumer.MiniAppTest` |
| The runtime SDK resolves without a consumer declaration | `sdkVersion()`, which calls `MiniAppSdk.VERSION` |
| The bundle satisfies its contract | the fixture's own `verifyConsumerContract` task |
| The DSL changes real output | the same task, run with and without `-PminiappBundleDirectory` |
| The host's consumption path works | `host/scripts/host-smoke.cjs`, which loads the bundle the way the page does |

That last check runs on Node and establishes module wiring only. **It is not WeChat host acceptance**, and the two are recorded separately. The separate accepted Developer Tools run used base library 3.17.3, rendered the shared greeting, count and SDK version, and reported `fixture.result=PASS` in the Console.

### Gradle plugin suite

`:miniapp-gradle-plugin:test` drives the plugin through Gradle TestKit fixtures in temporary consumer projects. It covers plugin application, the missing-Kotlin-Multiplatform failure, source-set provisioning and compilation ownership, test execution, runtime dependency wiring, the `assembleMiniAppBundle` contract, the renderer rejection that guards that contract, repeat-run incrementality and configuration-cache compatibility. It also covers the `miniapp { }` extension: that the canonical `miniapp { wechat { ... } }` block compiles and executes, that a configured bundle directory is where the bundle is actually written, that a directory outside the project is rejected with an actionable message, that applying the plugin twice creates no second extension, and that WeChat is a host configuration rather than the platform extension itself.

The error paths are asserted on the failure's own words, not on the build merely failing: a
project without the Kotlin Multiplatform plugin, a target that already uses `miniapp` for another
platform (Kotlin's own diagnostic, raised during plugin application rather than a silent
replacement), a shared Kotlin Multiplatform dependency that offers no Mini App variant, a runtime
coordinate that cannot be resolved, a bundle directory outside the project, and a runtime classpath
carrying Compose. Each names what the consumer has to change.

The fixtures resolve the runtime SDK through a composite build of this repository, because nothing is published yet. They assert on real output — the executed test report, and the files a bundle actually contains — rather than on task names alone: a task existing is not evidence that it produced anything. The bundle tests keep two conclusions apart: that the plugin generates no host markup, and that the distribution carries no renderer. The first is a statement about generated files; the second is a statement about the dependency graph and is asserted by the host-boundary check.

These tests establish that a distribution is produced and what it contains. They do not establish that any host can load it; that needs a real host run.

#### What the plugin suite refuses to guess

`checkMiniAppHostBoundary` reads the resolved dependency graph, and Gradle's resolution result lists
only the dependencies that resolved. A classpath that did not fully resolve would therefore pass the
check for the wrong reason — "no renderer" instead of "never seen" — so the check refuses it and
names the coordinates it could not resolve. A renderer hidden behind an unresolvable coordinate
fails the build either way; the difference is that the failure says what happened.

#### Test layers

| Layer | What it proves | Where |
| --- | --- | --- |
| Contract / unit | Plugin descriptor to implementation class, the public runtime coordinate, the extension's platform-versus-host shape, the renderer classifier's allow-list and deny-list, the bundle-directory rule | `miniapp-gradle-plugin/src/test/.../MiniAppPluginContractTest.kt` |
| TestKit | Plugin application, the missing-Kotlin-Multiplatform failure, source sets and their owning compilations, `miniappTest` execution, runtime wiring, task registration, the DSL, every error path below, the bundle contract, configuration-cache compatibility | `miniapp-gradle-plugin/src/test/.../MiniAppGradlePluginTest.kt` |
| Persistent consumer fixture | An ordinary consumer build that stays in the repository: clean build, `commonMain` reuse, both bundle directories, the bundle's contents, the host's CommonJS consumption path | `fixtures/miniapp-consumer` |
| Real host | That WeChat Developer Tools loads the bundle and runs it | manual, recorded in section 3 |

The first three layers are automated and isolated: TestKit fixtures live in temporary directories,
the persistent fixture runs from clean, and neither reads a developer's own project or an absolute
path. The fourth is not automated, and Node output is never reported as a host result.

### Gradle plugin integration suite

```shell
./gradlew verifyMiniAppGradlePluginIntegration
```

One entry point runs the plugin's contract and TestKit suites, the architecture boundary that keeps a
renderer out of the SDK, and the consumer fixture from clean. It is the command CI should call.

It is deliberately **not** wired into `check`. The fixture drives a nested Gradle build that compiles
Kotlin/JS and installs npm dependencies, so `check` depending on it would put several minutes and a
network dependency in front of every ordinary build and would make `check` re-enter Gradle. The
plugin's own suite is already part of `check` through that project's `check` task; this entry adds
the fixture and the SDK architecture check, which `check` does not run.

## 3. Real-host layer

### Reproducible checklist

The following procedure is the DeveloperTools checklist for currently implemented capabilities. It does not replace any RealDevice or BackendRequired checks in the verification matrix. After execution, use the matrix evidence template to record the commit, tool version, base-library version, result, and evidence reference.

1. Prepare the artifacts from the repository root.

   ```shell
   ./gradlew buildMiniAppSdk
   ```

2. Install the example's local tooling.

   ```shell
   cd examples/wechat-miniprogram && npm install
   ```

3. Import `examples/wechat-miniprogram` into WeChat Developer Tools as a Mini Program project and compile it.
4. Open the index page. The network check issues a real HTTPS request, so the target host must be listed in the request domain whitelist or the project must run with domain checking disabled.
5. Confirm the page and the console.

| Check | On the page | In the console |
| --- | --- | --- |
| Version | `0.1.0-SNAPSHOT` | `[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT` |
| Runtime lifecycle | The `Runtime Lifecycle` card shows `FOREGROUND` and a page route | No dedicated line; the card is the evidence |
| Runtime detection | The `Runtime Detection and Version Gate` card shows `PASS`, the base-library version, and support states | `[kmp-miniapp-sdk] runtime detection: PASS baseLibrary=…, platform=…, runtime-detection=…, storage=Supported, ungated=Unsupported` |
| Permission | The `Permission Lifecycle` card shows the permission name and its state after the steps below | `[kmp-miniapp-sdk] permission query: PASS permission=microphone, state=…` |
| Privacy | The `Privacy Authorization` card shows the host's requirement and its contract name | `[kmp-miniapp-sdk] privacy query: PASS requirement=…, contract=…` |
| Session check | The `WeChat Session Check` card shows `VALID`, `INVALID`, or `FAIL` | `[kmp-miniapp-sdk] session check: PASS check #N, state=Valid\|Invalid` |
| Clipboard and haptics | The `Clipboard and Haptics` card shows `PASS` for write, read, short vibration, and long vibration | `[kmp-miniapp-sdk] clipboard write: PASS`, `clipboard read: PASS matched=true`, `haptics short: PASS`, `haptics long: PASS` |
| File system | The `File System` card shows `PASS` for write, access, read, and remove | `[kmp-miniapp-sdk] filesystem write: PASS`, `filesystem access: PASS exists=true`, `filesystem read: PASS matched=true`, `filesystem remove: PASS`, `filesystem access: PASS exists=false` |
| Location | The `Location` card shows the capability answer, the permission, the privacy requirement, and `PASS` for a position | `[kmp-miniapp-sdk] location capability: PASS wechat.location=…`, `location permission query: PASS state=…`, `location privacy query: PASS requirement=…`, `location: PASS coordinatesValid=true, accuracyValid=true` |
| Scanner | The `Scanner` card shows the capability answer and `PASS`, `INTERRUPTED`, or `FAIL` for a scan | `[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=…`, `scan: PASS resultPresent=true, typeRecognized=true`, `scan: INTERRUPTED cause=indeterminate` |
| Media | The `Media` card shows the capability answer and `PASS`, `INTERRUPTED`, or `FAIL` for a selection | `[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=…`, `media choose: PASS count=1, typesValid=true, metadataValid=true`, `media choose: INTERRUPTED cause=indeterminate` |
| Subscription Message | The `Subscription Message` card shows the capability answer and the request's `PASS`, `NOT CONFIGURED`, or `FAIL` | `[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=…`, `subscription request: PASS accepted=N, otherStatuses=N, signals=…`, `subscription request: FAIL reason=HostFailure, signal=…` |
| Network extensions | The `Network Extensions` card shows each capability answer, the current network, and the observation, upload, and download states | `[kmp-miniapp-sdk] network capabilities: PASS query=…, listener=…, upload=…, download=…`, `network type: PASS connected=true, type=WIFI`, `network observation: PASS events=N, last=…`, `upload check: PASS\|NOT CONFIGURED`, `download check: PASS\|NOT CONFIGURED` |
| Standard payment | The `Standard Payment` card shows the capability answer and the request's `PASS`, `NOT CONFIGURED`, `CANCELLED`, or `FAIL` | `[kmp-miniapp-sdk] payment capability: PASS wechat.request-payment=…`, `payment request: NOT CONFIGURED`, `payment request: PASS interactionCompleted=true`, `payment request: CANCELLED cause=indeterminate`, `payment request: FAIL reason=<closed label>` |
| Storage | `Storage verification: PASS` | `[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null` |
| Client login code | `Client login code: PASS` | `[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<positive integer>` |
| Network | `Network verification: PASS` | `[kmp-miniapp-sdk] network: PASS status=200, bytes=<positive integer>` |

6. Verify the page-stack bridge by tapping through it. Navigation cannot be checked from one page, because every action changes which page is on screen.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Open second page (wx.navigateTo)` on the index page | The second page appears. Console: `[kmp-miniapp-sdk] second page: SHOWN pages/second/index`, then `[kmp-miniapp-sdk] navigation: PASS wx.navigateTo /pages/second/index` |
| 2 | `Replace with third page (wx.redirectTo)` on the second page | The third page appears. Console: `[kmp-miniapp-sdk] second page: UNLOADED pages/second/index`, then `[kmp-miniapp-sdk] navigation: PASS wx.redirectTo /pages/third/index` |
| 3 | `Go back (wx.navigateBack)` on the third page | The index page reappears with `FOREGROUND` and `pages/index/index`. Console: `[kmp-miniapp-sdk] navigation: PASS wx.navigateBack` |

Step 2 is what makes `redirectTo` observable: the second page is replaced rather than covered, so step 3 reveals the index page instead of the second one.

7. Confirm the version-gate states. The Runtime Detection card reads its expected state from the host, so it shows them without a code change:

| Case | How to produce it | Expected |
| --- | --- | --- |
| `Supported` | Open the page at a supported base library version | `storage=Supported`, `runtime-detection=Supported` |
| `Unsupported` | Open the same page at any base-library version | The ungated capability reads `ungated=Unsupported` |
| `VersionDependent` | Not constructible in Developer Tools | See below |

`VersionDependent` cannot be reproduced in Developer Tools: the lowest debug base library it offers is 2.21.4, which is above the 2.20.1 boundary this capability records, so no lower host can be constructed. Do not change the boundary to suit the tool, do not fabricate old-version evidence, and do not install an unsupported older Developer Tools build. The state is covered by automated tests instead:

- `HostVersionTest` verifies version parsing and numeric segment comparison.
- `WechatCapabilityGateTest` covers all four states, the `2.20.1` boundary itself, fallback when the version is unreadable, and a host that cannot be probed at all.
- `CapabilitySupportContractChecks` runs the same assertions against the Fake Host and the real `WechatHost`.
- A mutation probe was executed and reverted: inverting the version comparison failed three tests, covering both the direction and the boundary.

The base library version in the card must match the one selected in Developer Tools. Record it: it is part of the evidence the verification matrix requires. This capability has completed Developer Tools and real-device acceptance; the record is in the [WeChat real-host verification matrix](../platforms/wechat/WECHAT_HOST_VERIFICATION-en.md).

8. Verify the permission lifecycle. Nothing is requested while the page loads, so every step below follows a tap. The card starts at whatever state the host already holds: `NotRequested` on a fresh install or after clearing the mini program's authorization data.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Refresh permission state` | `[kmp-miniapp-sdk] permission query: PASS permission=microphone, state=NotRequested` on a host that has never been asked |
| 2 | `Request permission` and allow it | `[kmp-miniapp-sdk] permission request: PASS permission=microphone, state=Granted` |
| 3 | `Refresh permission state` | The state is still `Granted`: it is read from the host, not remembered by the SDK |
| 4 | Disable the permission in the host's own settings, then `Refresh permission state` | `state=Denied` |
| 5 | `Request permission` again | `[kmp-miniapp-sdk] permission request: DENIED permission=microphone, state=Denied`. A refusal is not a host failure, and no second prompt appears |
| 6 | `Open settings` and re-enable the permission | `[kmp-miniapp-sdk] permission settings: PASS permission=microphone, state=Granted` after the page closes |

A prompt may only ever follow a tap. If the smoke test or page load produces one, that is a defect, not a configuration problem. Permission state belongs to the user, so a Developer Tools run does not replace the real-device run in the verification matrix.

This flow has been executed in WeChat Developer Tools at base library 3.17.2 and on an Android device (OnePlus PLQ110, Android 36, WeChat 8.0.76), including the full `Granted` → `Denied` → `DENIED` → `Granted` transition with no second prompt after the refusal. Step 1 is the exception: the account used already holds a decision, so `NotRequested` could not be produced. That state is instead covered by automated tests — the Fake Host contract checks, the adapter tests for a missing authorization entry and for `true`/`false`/missing conversion, and the first-prompt success and refusal paths — and reproducing it on a host would require a different account or device, or clearing the mini program's authorization history.

9. Verify the privacy authorization flow. It is a different condition from the permission above, and the two are recorded separately. The debug base library must be 2.32.3 or later, and the mini program must declare its collection in the MP backend privacy guideline, or the host reports nothing to authorize.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | Read the card after the page loads | `[kmp-miniapp-sdk] privacy query: PASS requirement=REQUIRED, contract=…`. The query has no side effect, so nothing was prompted |
| 2 | `Request privacy authorization` and accept | `[kmp-miniapp-sdk] privacy request: PASS result=Authorized`, and the card then shows `NOT_REQUIRED` |
| 3 | `Refresh privacy status` | Still `NOT_REQUIRED`, read from the host rather than remembered |
| 4 | Clear the account's acceptance in the Developer Tools cache and reload | `REQUIRED` again |
| 5 | `Request privacy authorization` and decline or dismiss | `[kmp-miniapp-sdk] privacy request: REFUSED result=Refused`, and the requirement stays `REQUIRED` |

Step 5 is one state, not two: WeChat publishes no field that distinguishes declining from dismissing. The SDK reports a recognizable refusal as `Refused`; an unknown failure is shown as `FAIL` and remains a `HostFailure`. `NOT_REQUIRED` never means the user agreed, because the host also reports it when the mini program declares no collection.

10. Verify the WeChat session check. It is recorded separately from the login bootstrap above, because a valid session is not identity and only the login-code path establishes one. The check runs before the bootstrap while the page loads, on purpose: acquiring a code refreshes the client login state and would hide an expired session.

| Step | Action | Expected |
| --- | --- | --- |
| 1 | Clear the mini program's login state, then open the page | The card shows `INVALID` for check #1, before the bootstrap acquires a code. Console: `[kmp-miniapp-sdk] session check: PASS check #1, state=Invalid` |
| 2 | `Check WeChat session` again | Still `INVALID` while no new code has been acquired |
| 3 | Let the auth bootstrap run, or call `wechatLogin()` from the Auth card | `codeReceived=true` and a positive length; the code itself is never displayed or logged |
| 4 | `Check WeChat session` | `VALID`. Console: `… check #N, state=Valid` |

Under the host contract, the `wx.checkSession` failure callback means the login state is invalid, so the page reports `INVALID` without depending on the language or exact text of `errMsg`. Only a missing API or a call that cannot be registered rejects the Promise rather than masquerading as `VALID`.

11. Verify the clipboard and haptics. Nothing touches the clipboard or the vibrator while the page loads, so every step follows a tap. The clipboard steps only ever compare against the fixed test string this page wrote; the page never displays or logs whatever else the clipboard holds, because that is the user's.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Write test text` | `[kmp-miniapp-sdk] clipboard write: PASS`, and the card shows `Clipboard write: PASS` |
| 2 | `Read clipboard` | `[kmp-miniapp-sdk] clipboard read: PASS matched=true`, and the card shows `Clipboard read: PASS`. A mismatch prints `matched=false` without revealing either value |
| 3 | `Short vibration` | `[kmp-miniapp-sdk] haptics short: PASS` |
| 4 | `Long vibration` | `[kmp-miniapp-sdk] haptics long: PASS` |

Steps 3 and 4 must be run on a real device. The console line records only that WeChat accepted the call; whether a vibration was actually felt is for the person holding the device to confirm, and no automated check can show it. `getClipboardData` is not an allowed `app.json.requiredPrivateInfos` entry and must not be declared in that array.

12. Verify the file system. Nothing touches the file system while the page loads, so every step follows a tap. The page only ever writes a fixed, non-sensitive string to a fixed file name; it never displays or logs the sandbox root or any other file's contents.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Write test file` | `[kmp-miniapp-sdk] filesystem write: PASS` |
| 2 | `Check file exists` | `[kmp-miniapp-sdk] filesystem access: PASS exists=true` |
| 3 | `Read test file` | `[kmp-miniapp-sdk] filesystem read: PASS matched=true` |
| 4 | `Remove test file` | `[kmp-miniapp-sdk] filesystem remove: PASS` |
| 5 | `Check removed file` | `[kmp-miniapp-sdk] filesystem access: PASS exists=false` |

The order matters: step 5 only reads `exists=false` when step 4 removed the file. Removing a file that is not there fails, following WeChat's `unlink` contract, so steps 4 and 5 must not be run twice in a row.

13. Verify location. It is gated by three separate conditions, so the steps below walk the capability question, then the privacy contract, then the permission. Nothing reads a position while the page loads, so every step follows a tap.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Check location capability` | `[kmp-miniapp-sdk] location capability: PASS wechat.location=Supported`, and the card shows the same state. `Unsupported` is a correct answer on a host without the API |
| 2 | `Refresh privacy status` | `[kmp-miniapp-sdk] location privacy query: PASS requirement=REQUIRED`. The query has no side effect, so nothing was prompted |
| 3 | `Get current location` before the contract is accepted | `[kmp-miniapp-sdk] location: PRIVACY_REQUIRED`. The call is refused before the host is asked, and no prompt appears |
| 4 | `Request privacy authorization` and accept | `[kmp-miniapp-sdk] location privacy request: result=Authorized`, and the card then shows `NOT_REQUIRED` |
| 5 | `Refresh location permission` | `[kmp-miniapp-sdk] location permission query: PASS state=NotRequested` on a host that has never been asked |
| 6 | `Get current location` before the permission is granted | `[kmp-miniapp-sdk] location: DENIED permissionState=NotRequested`; the capability does not prompt for the permission on the consumer's behalf |
| 7 | `Request location permission` and allow | `[kmp-miniapp-sdk] location permission request: PASS state=Granted` |
| 8 | `Get current location` | `[kmp-miniapp-sdk] location: PASS coordinatesValid=true, accuracyValid=true` |
| 9 | Deny the permission in the host's settings, then `Get current location` again | `[kmp-miniapp-sdk] location: DENIED permissionState=Denied`, distinct from the privacy precondition failure in step 3 |

The page validates the shape of the answer — a finite latitude in `-90..90`, a finite longitude in `-180..180`, and a non-negative accuracy — and never renders or logs a coordinate, so step 8's line is the only evidence it prints. Steps 3 and 6 must not be reachable by accident: if the page load or the smoke test produces a prompt or a position read, that is a defect, not a configuration problem.

Steps 1 and 8 only mean what they say. A supported capability says the host exposes the API, not that any position was read; a successful read says the host answered with a position the contract can carry, not that the position is accurate or current. The simulator derives its answer from IP rather than from a device receiver, so a Developer Tools run can exercise the whole flow but proves nothing about a real position; only a device can. On a device the operating system may also gate WeChat's own access to location, independently of `scope.userLocation`, so a granted scope can still fail until that is allowed.

Three configuration requirements decide whether this works at all, and all three are outside the SDK: `getLocation` must be listed in `app.json.requiredPrivateInfos`, `scope.userLocation` must be declared in `app.json.permission` with a description, and the mini program must declare the location interface in the MP backend's interface settings. Without the last one the host refuses before the user sees anything, which is a configuration failure rather than an SDK defect.

A mutation probe was executed and reverted for this capability: inverting the coordinate range check failed nine tests across the interop, adapter, and contract levels, so the shape validation is genuinely asserted rather than incidentally passing.

14. Verify scanning. It opens WeChat's own scanning interface, so nothing opens while the page loads and every step below follows a tap. The page reports only whether content was present, whether the format the host named is recognized, and whether the outcome was success, an indeterminate interruption, or another failure; it **never displays or logs the scanned content, `rawData`, the character set, or the image path**.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Check scanner capability` | `[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=Supported`, and the card shows the same state. `Unsupported` is a correct answer on a host without the API |
| 2 | After the page loads, check the console and the card | No scanning interface appeared and there is no `scan:` line; the card still reads `NOT RUN` |
| 3 | `Scan code` and scan an ordinary test QR code | `[kmp-miniapp-sdk] scan: PASS resultPresent=true, typeRecognized=true`, and no scanned content appears anywhere in the console |
| 4 | `Scan code` and dismiss the interface | `[kmp-miniapp-sdk] scan: INTERRUPTED cause=indeterminate` |
| 5 | Restrict WeChat's camera access in system settings, then tap `Scan from camera only` | The same `scan: INTERRUPTED cause=indeterminate`; this proves the host provides too little information to distinguish dismissal from camera restriction |

Step 3 proves only that the SDK handed over a result the contract can carry, not that anything parsed it — this capability does no business parsing. Steps 4 and 5 together prove that the host collapses user dismissal and camera restriction into one signal; the SDK must preserve that uncertainty instead of reporting `UserCancelled` or `PermissionDenied`.

The interruption classification matches exactly two host messages (`scanCode:cancel` and `scanCode:fail cancel`); all other failures stay `HostFailure`. This conclusion follows the observed real-host behavior rather than guessing what the text means.

Developer Tools is not a device: its scan implementation has the user pick an image and then decodes it, so the camera path and what `onlyFromCamera` actually does can only be settled on hardware. `Scan from camera only` only passes `onlyFromCamera=true` to WeChat to prevent an album fallback; it neither queries nor requests nor assumes `scope.camera`. The example adds no scan field to `app.json.requiredPrivateInfos`. The system or WeChat may handle camera access inside the scanning interface, which does not mean the SDK established a permission precondition.

15. Verify media selection. It opens WeChat's own picker, so nothing opens while the page loads and every step below follows a tap. The page reports only how many files came back, whether every kind the host named is one the SDK recognizes, and whether the metadata is usable; it **never displays or logs the selected media, a base64 encoding, a file name, or a complete temporary path**.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Check media capability` | `[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=Supported`, and the card shows the same state. `Unsupported` is a correct answer on a host without the API |
| 2 | After the page loads, check the console and the card | No picker appeared and there is no `media choose:` line; the card still reads `NOT RUN` |
| 3 | `Choose image` and select a test image | `[kmp-miniapp-sdk] media choose: PASS count=1, typesValid=true, metadataValid=true` |
| 4 | `Choose video` and select a test video | The same line with `count=1`; on a device, record whether `durationSeconds`, `width`, and `height` were reported or absent |
| 5 | `Choose image or video` | A selection containing either kind still reports `typesValid=true` |
| 6 | `Choose from camera only` and take a photo or video | The picker opens the camera rather than the album |
| 7 | `Choose image` and dismiss the picker | `[kmp-miniapp-sdk] media choose: INTERRUPTED cause=indeterminate` |
| 8 | Restrict WeChat's access to photos, then `Choose image` | A failure, or the same `INTERRUPTED` line — **record which**, because this observation settles whether the device distinguishes a restriction from a dismissal |

Developer Tools reports a simulated dismissal as `chooseMedia:cancel`, while the accepted Android run reports a manual dismissal as `chooseMedia:fail cancel`; both exact signals are classified as an interruption. The accepted restricted-access run produces the same indeterminate interruption on that Android host, so the SDK does not claim it can distinguish the cause. Any other message remains a host failure until host evidence justifies changing the classification.

Step 3 proves only that the SDK handed over files the contract can carry, not that anything decoded or uploaded them — nothing in this capability does either. A success callback with no selected files is an invalid host response and never produces a vacuous `count=0` PASS.

The page reads no media itself, and nothing in the example copies the temporary files anywhere. The paths the host returns belong to the session that produced them, so treat them as short-lived: copy the media to storage you own if it has to outlive the run.

16. Verify subscription requests. WeChat requires a user gesture, so nothing asks the host while the page loads and every step below follows a tap. The page prints counts and never a template id: which templates a user subscribed to is between the user and the mini program, and a screenshot must not carry it.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Check subscription capability` | `[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=Supported` |
| 2 | After the page loads, check the console and the card | No prompt appeared and there is no `subscription request:` line; the card still reads `NOT RUN` |
| 3 | `Request subscription` with no test template configured locally | `[kmp-miniapp-sdk] subscription request: NOT CONFIGURED templateCount=0`, and no prompt: the page makes no host call at all |
| 4 | Configure one test template id locally (do not commit it), then `Request subscription` and allow it | `[kmp-miniapp-sdk] subscription request: PASS accepted=1, otherStatuses=0, signals=accept` if the host answers `accept` |
| 5 | `Request subscription` and refuse it | The same summary with different counts. **Record the status string the host used for a refusal**, because this SDK has no evidence for it and preserves it verbatim |
| 6 | `Request subscription` and dismiss the prompt | `subscription request: FAIL reason=HostFailure, signal=…`; report the closed `signal` label (`cancel`, `fail-cancel`, `cancel-like`, or `other`) so an exact mapping can be added only from evidence |
| 7 | Configure two test templates, allow one and refuse the other, then `Request subscription` | `PASS` with counts summing to 2, and one entry per requested template in the order given |

`templateCount=0` is not a host refusal and does not exercise `wx.requestSubscribeMessage`; consequently no consent prompt can appear in step 3. To run steps 4–7, first add a subscription-message template under the exact AppID used for the test, then place that template id only in the example's local `subscriptionTemplateIds` fixture. Never invent an id, reuse one from another AppID, or commit it. If the account cannot provide a valid template, record steps 4–7 as `NOT RUN — blocked by AppID template configuration`.

Step 5 and step 6 are the ones that carry information this repository does not have. This capability's status vocabulary could not be verified from the offline sources, so the SDK recognizes only `accept` and preserves every other non-blank status verbatim. Step 6 deliberately remains `HostFailure`; its safe label tells us whether an exact interruption mapping is justified without exposing raw host data. Report both rather than recording the step as passing.

Step 4 proves only that the SDK handed over the host's per-template answers, not that any message exists. Accepting a subscription is a subscription state; whether a message is ever sent or delivered is a `BackendRequired` question with a WeChat backend template and a trusted backend behind it.

The adapter requires exact correlation: missing or unexpected template keys and blank or non-text statuses are `InvalidResponse`, never a successful “silent” answer.

17. Verify the network extensions. Nothing is queried, registered, uploaded, or downloaded while the page loads, so every step follows a tap. The page never prints a URL, a header, a file path, or a response body; it reports statuses, byte counts, event counts, and whether an abort was invoked.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Check network capabilities` | `[kmp-miniapp-sdk] network capabilities: PASS query=Supported, listener=Supported, upload=Supported, download=Supported`, or `Unsupported` for whichever API the host lacks |
| 2 | `Get current network type` | `[kmp-miniapp-sdk] network type: PASS connected=true, type=<recognized kind or unrecognized>` |
| 3 | `Start network status observation`, switch the device's connection (Wi-Fi to cellular, or turn connectivity off and on), then `Stop network status observation` | `[kmp-miniapp-sdk] network observation: PASS events=<count above zero>, last=<the kind you switched to>`. **This needs a device**: a simulator has no connection to switch |
| 4 | `Stop network status observation` again without starting it | `[kmp-miniapp-sdk] network observation: PASS events=0, last=none` |
| 5 | `Run upload check` with no endpoint configured | `[kmp-miniapp-sdk] upload check: NOT CONFIGURED`, and no request leaves the device |
| 6 | `Run download check` with no endpoint configured | `[kmp-miniapp-sdk] download check: NOT CONFIGURED` |
| 7 | Configure a controlled HTTPS endpoint locally (do not commit it), whitelist it in the request domain, then `Run upload check` | `[kmp-miniapp-sdk] upload check: PASS status=<2xx or the status the service returns>, bytes=<length>, progressSeen=<true\|false>` |
| 8 | `Run download check` against the same service | `[kmp-miniapp-sdk] download check: PASS status=…, fileReported=true, progressSeen=…` |
| 9 | Start a check against a large or slow endpoint, then `Cancel upload` (or `Cancel download`) while it runs | `[kmp-miniapp-sdk] upload cancel: PASS abortInvoked=true` and the card shows `CANCELLED`; the transfer reports no answer |

Steps 3, 7, 8, and 9 are the ones automation cannot produce. Step 3 needs a device whose connection can actually be switched; steps 7 to 9 need a controlled HTTPS service, which is `BackendRequired` — a public endpoint is not acceptable evidence, and `https://example.com/` is not an upload service.

Step 5 is the guard that the example's default state is honest: with nothing configured the page makes no host call at all, so no upload can be mistaken for a passing check. A `progressSeen=false` in step 7 or 8 is not a failure: a small transfer can complete before the host reports anything.

18. Verify standard payment. Nothing touches the payment interface while the page loads, so every step follows a tap. The page's log lines never contain a payment parameter, a signature, a merchant identifier, or the host's own failure text; a failure is reduced to a closed label instead.

| Step | Tap | Expected |
| --- | --- | --- |
| 1 | `Check payment capability` | `[kmp-miniapp-sdk] payment capability: PASS wechat.request-payment=Supported`, or `Unsupported` on a host without the API |
| 2 | `Request payment` with nothing configured | `[kmp-miniapp-sdk] payment request: NOT CONFIGURED`, and no host call at all: the page inspects its placeholder before it calls the SDK |
| 3 | With a legal merchant environment, put the backend's parameters into the page's local placeholder (never commit them), then `Request payment` and complete the payment | `[kmp-miniapp-sdk] payment request: PASS interactionCompleted=true`. **This is not an order.** Confirm it with the trusted backend, which learns the truth from WeChat Pay's server API, its asynchronous notification, or an order query, and record that server-side result as the evidence |
| 4 | `Request payment` and dismiss the payment interface | `[kmp-miniapp-sdk] payment request: CANCELLED cause=indeterminate`, and the card does not claim that the user cancelled. Which message a real dismissal produces — `requestPayment:cancel` or `requestPayment:fail cancel` — is the open item this run exists to settle |
| 5 | `Request payment` with parameters the host refuses, such as an expired or already-used prepay package | `[kmp-miniapp-sdk] payment request: FAIL reason=host-failure`, with no host text on the page or in the console |

Steps 3 to 5 cannot be produced here at all: they need a legal WeChat Pay merchant account bound to this mini program's AppID, a real order, and a trusted backend that signs, which is `BackendRequired`. The Developer Tools simulator is not a merchant, so nothing it shows may be recorded as a payment result. Step 2 is the guard that the committed default is honest, and it is the only one of these steps that runs without that environment.

19. Record which checks you observed and which you did not. A capability is recorded as host-verified in [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) only after a real-host run for that capability.

The Storage check writes a dedicated test key, verifies overwrite, removes it, and confirms that the missing key reads as `null`. The network check issues a `GET` to `https://example.com/` and reports the status code and body length; point the example's `networkUrl` constant at any reachable HTTPS endpoint when verifying a different host.

The `BACKGROUND` state of the app-level lifecycle cannot be triggered from the Developer Tools simulator. Observing it requires backgrounding the mini program on a real device, so a simulator run can confirm the foreground state and the page route but not the background transition.

The login code is a short-lived credential and is never logged or rendered. A `wx.login` code must be sent to a trusted consumer backend for WeChat exchange; receiving it does not authenticate a user and does not authorize requests.

### Out of scope

Driving WeChat Developer Tools automatically, full end-to-end automation, and payment testing are not part of this layer. No automated test in this repository claims access to a real `wx` runtime.
