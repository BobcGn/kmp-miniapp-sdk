# Testing

[中文](TESTING-ch.md)

This document describes the two testing layers, the fakes each layer may use, and the reproducible checklist for a real WeChat Mini Program run.

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

7. Record which checks you observed and which you did not. A capability is recorded as host-verified in [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) only after a real-host run for that capability.

The Storage check writes a dedicated test key, verifies overwrite, removes it, and confirms that the missing key reads as `null`. The network check issues a `GET` to `https://example.com/` and reports the status code and body length; point the example's `networkUrl` constant at any reachable HTTPS endpoint when verifying a different host.

The `BACKGROUND` state of the app-level lifecycle cannot be triggered from the Developer Tools simulator. Observing it requires backgrounding the mini program on a real device, so a simulator run can confirm the foreground state and the page route but not the background transition.

The login code is a short-lived credential and is never logged or rendered. A `wx.login` code must be sent to a trusted consumer backend for WeChat exchange; receiving it does not authenticate a user and does not authorize requests.

### Out of scope

Driving WeChat Developer Tools automatically, full end-to-end automation, and payment testing are not part of this layer. No automated test in this repository claims access to a real `wx` runtime.
