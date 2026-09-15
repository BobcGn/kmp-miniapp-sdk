# Testing

[中文](TESTING-ch.md)

This document describes the two testing layers, the fakes each layer may use, and the reproducible checklist for a real WeChat Mini Program run.

The two layers are the top-level automation and real-host categories. Release evidence is further classified as Unit, Contract, Node, DeveloperTools, RealDevice, and BackendRequired. See the [WeChat real-host verification matrix](platforms/wechat/WECHAT_HOST_VERIFICATION-en.md) for the minimum level per capability, evidence retention, and regression timing.

## 1. Two layers, two different claims

| Layer | Runs on | Establishes | Does not establish |
| --- | --- | --- | --- |
| Automated | Node.js, through the Gradle Wrapper and the example's npm scripts | Shared Kotlin logic, adapter behavior driven through fake ports, module and declaration shape, TypeScript contract | Anything about the WeChat Mini Program runtime |
| Real host | WeChat Developer Tools | That the generated artifact compiles, loads, and runs in the intended host | Nothing further, and no regression in the host's own behavior |

**A passing Kotlin or Node.js test does not establish a passing WeChat Mini Program integration.** A passing `:sdk:jsNodeTest`, `npm run smoke`, or `npm run typecheck` result and a real-host result are always recorded separately. Node.js is only the local build and test environment; the production host is the WeChat Mini Program JavaScript runtime.

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
./gradlew :sdk:jsNodeTest
```

```shell
cd examples/wechat-miniprogram && npm run smoke && npm run typecheck
```

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

9. Record which checks you observed and which you did not. A capability is recorded as host-verified in [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) only after a real-host run for that capability.

The Storage check writes a dedicated test key, verifies overwrite, removes it, and confirms that the missing key reads as `null`. The network check issues a `GET` to `https://example.com/` and reports the status code and body length; point the example's `networkUrl` constant at any reachable HTTPS endpoint when verifying a different host.

The `BACKGROUND` state of the app-level lifecycle cannot be triggered from the Developer Tools simulator. Observing it requires backgrounding the mini program on a real device, so a simulator run can confirm the foreground state and the page route but not the background transition.

The login code is a short-lived credential and is never logged or rendered. A `wx.login` code must be sent to a trusted consumer backend for WeChat exchange; receiving it does not authenticate a user and does not authorize requests.

### Out of scope

Driving WeChat Developer Tools automatically, full end-to-end automation, and payment testing are not part of this layer. No automated test in this repository claims access to a real `wx` runtime.
