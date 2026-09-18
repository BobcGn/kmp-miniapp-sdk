# WeChat Real-Host Verification Matrix

[中文](WECHAT_HOST_VERIFICATION-ch.md)

This document defines real-host acceptance environments, evidence format, regression frequency, and closing conditions for WeChat capabilities. See the [WeChat capability matrix](WECHAT_CAPABILITIES-en.md) for implementation status.

## 1. Conclusion boundaries

| Level | Environment | Proves | Does not prove |
| --- | --- | --- | --- |
| `Unit` | Kotlin/JS tests | An individual type, mapper, or adapter behavior | WeChat runtime availability |
| `Contract` | Reference implementation and WeChat adapter | Common semantics agree across both implementations | WeChat API, permission, or hardware behavior |
| `Node` | CommonJS smoke and TypeScript | Distribution modules and consumer types work | WeChat Mini Program compatibility |
| `DeveloperTools` | WeChat Developer Tools | Artifacts compile, load, and run under the recorded base-library configuration | Real hardware, system permissions, background behavior, or payment outcome |
| `RealDevice` | A real Mini Program in the WeChat client | Host behavior for the recorded device, OS, WeChat, and base-library combination | Compatibility with every other device or version |
| `BackendRequired` | Real device plus controlled backend | A complete signature, code-exchange, transfer, or payment chain | Unrecorded production reliability |

**Unit, Contract, or Node PASS is not DeveloperTools PASS. DeveloperTools PASS is not RealDevice or BackendRequired PASS.**

## 2. Common preparation

1. Record the Git commit under test and working-tree state.
2. At the repository root run:

   ```shell
   ./gradlew clean build
   ./gradlew buildMiniAppSdk
   ```

3. In `examples/wechat-miniprogram` run:

   ```shell
   npm install
   npm run smoke
   npm run typecheck
   ```

4. Import `examples/wechat-miniprogram` into WeChat Developer Tools. Record tool version, debug base-library version, AppID class, and domain-checking setting.
5. For RealDevice evidence also record device model, OS, WeChat version, network type, and initial privacy/permission state.
6. BackendRequired evidence must record the test backend environment, a non-sensitive correlation ID, and final server result. Credentials, login codes, payment signatures, and personal data must not enter screenshots or logs.

## 3. Current capability verification matrix

| Capability | Required Environment | Preconditions | Reproducible Steps | Expected Result | Current Evidence | Regression Frequency |
| --- | --- | --- | --- | --- | --- | --- |
| Consumer Bridge | Node + DeveloperTools | `buildMiniAppSdk` completed | Run smoke/typecheck; import the example and open index | Node prints the version; page and Console show `0.1.0-SNAPSHOT` | `WDT-2026-09-14-A`; see PROJECT_FACTS | Every export, Gradle, or distribution change |
| Storage | Unit + Contract + Node + DeveloperTools | Use the dedicated example key | Run tests; open index and wait for Storage card | `PASS`; `first=first, overwritten=second, missing=null`; test key removed | `WDT-2026-09-14-A` | Every Storage contract, adapter, or export change |
| HTTP Request | Unit + Contract + Node + DeveloperTools | Legal HTTPS request domain, or explicitly disabled local domain checks | Run tests; open index and wait for Network card | Request succeeds; page says PASS; Console has status and positive bytes | `WDT-2026-09-14-C` | Every transport, error, timeout, abort, or wrapper change |
| Authentication Bootstrap | Unit + Node + DeveloperTools | Test AppID can call `wx.login` | Open index and wait for Login card | `codeReceived=true`, positive length, code never displayed or logged | `WDT-2026-09-14-B` | Every auth interop, adapter, error, or export change |
| App Lifecycle — Foreground | Unit + Contract + Node + DeveloperTools | Consumer forwards App hooks | Compile and open index | Card shows `FOREGROUND` | `WDT-2026-09-15-A` | Every lifecycle-state or hook change |
| App Lifecycle — Background | RealDevice | Open example on device; consumer forwards App hooks | Background the Mini Program, then return | BACKGROUND → FOREGROUND without duplicate or lost terminal state | Pending | Every lifecycle change; at least once per release candidate |
| Page Lifecycle | Unit + Node + DeveloperTools | Consumer forwards Page hooks | Visit second and third pages, then return | route and show/hide/unload agree with visible page | `WDT-2026-09-15-A` | Every Page-hook or navigation change |
| Navigation | Unit + Node + DeveloperTools + RealDevice for `switchTab` | Example has index, second, third, and tabtarget pages, and a two-entry `tabBar` | navigateTo second; redirectTo third; navigateBack; switchTab to the tabtarget tab; switchTab to a route with no tab | Third replaces second; return reveals index; Console order matches docs; the tab target page appears and the index page is hidden rather than unloaded; a route with no tab keeps the current tab and prints a closed category | `WDT-2026-09-15-A` covers the three page-stack operations; `WDT-2026-09-18-A` and `DEVICE-2026-09-18-A` cover `switchTab` | Every navigation interop, adapter, or page-config change |
| Runtime Detection | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; Developer Tools debug base library recorded; one real device available | Run tests; open index and read the Runtime Detection card; repeat on a real device | Card shows the base-library version and platform; `storage` reads `Supported`; `wechat.runtime-detection` reads `Supported`; an ungated capability reads `Unsupported` | `WDT-2026-09-15-C`; `DEVICE-2026-09-15-A`; see PROJECT_FACTS | Every catalog, gate, or export change |
| Permission | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a host whose permission decision can be changed by hand | Run tests; open index and use the Permission Lifecycle card: refresh, request, disable in the host's own settings, refresh, request again, then open settings and re-enable | `Granted` after allowing; `Denied` after disabling; requesting again reports `DENIED` with no second prompt; `Granted` again after a settings visit | `WDT-2026-09-15-D`; `DEVICE-2026-09-15-B`; see PROJECT_FACTS | Every permission interop, adapter, scope-mapping, or export change |
| Privacy | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; the mini program's collection declared in the MP backend privacy guideline | Run tests; open index and use the Privacy Authorization card: refresh, then request; repeat after clearing the account's acceptance | The card shows `REQUIRED` with the host's contract name, then `NOT_REQUIRED` after acceptance; a refusal is reported as `REFUSED` and leaves the requirement in place | Pending | Every privacy interop, adapter, or export change |
| Check Session | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a host whose login state can be cleared | Run tests; open index and read the WeChat Session Check card; clear the login state and reload to see `Invalid`; acquire a new login code, then check again for `Valid` | The card reports `INVALID` before a code is acquired and `VALID` afterwards, and the console line matches | Verified 2026-09-15 — Android, OnePlus PLQ110, WeChat 8.0.76, base library 3.17.3 [1641] | Every session-check interop, adapter, or export change |
| Clipboard | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; clipboard access available | Run tests; open index and use the Clipboard and Haptics card: write the test text, then read it back | `clipboard write: PASS` and `clipboard read: PASS matched=true`; the page reports both as `PASS` | Verified 2026-09-15 — Developer Tools 3.17.2; Android, OnePlus PLQ110, WeChat 8.0.76, base library 3.17.3 [1641] | Every clipboard interop, adapter, or export change |
| Haptics | Unit + Contract + Node + RealDevice | A device with vibration hardware | Run tests; open index and tap `Short vibration`, then `Long vibration` | `haptics short: PASS` and `haptics long: PASS` in the console, and the tester feels each vibration | Verified 2026-09-15 — Android, OnePlus PLQ110, WeChat 8.0.76, base library 3.17.3 [1641]; both vibrations felt | Every haptics interop, adapter, or export change |
| File System | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; the mini program file sandbox is available | Run tests; open index and use the File System card in order: write, check exists, read, remove, check removed | `filesystem write: PASS`, `filesystem access: PASS exists=true`, `filesystem read: PASS matched=true`, `filesystem remove: PASS`, `filesystem access: PASS exists=false` | `WDT-2026-09-15-E`; `DEVICE-2026-09-15-C` | Every file-system interop, adapter, or export change |
| Location | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; the location interface enabled in the MP backend; a host whose permission decision can be changed by hand | Run tests; open index, inspect capability and privacy, then cover `NotRequested`, explicit grant, successful location, refusal in settings, a read while refused, and a successful read after recovery | `wechat.location=Supported`; permission reports `NotRequested`, `Granted`, and `Denied`; refusal reports `location: DENIED permissionState=Denied`; recovery reports `PASS coordinatesValid=true, accuracyValid=true`; no coordinate is displayed or logged | `WDT-2026-09-15-F`; `DEVICE-2026-09-15-D` | Every location interop, adapter, catalog, or export change |
| Scanner | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a reachable test QR code; a device whose camera access can be restricted | Check capability, scan a test code, dismiss deliberately, then restrict camera access and run both regular and camera-only scans | `Supported`; success is `PASS resultPresent=true, typeRecognized=true`; dismissal and camera restriction are both `INTERRUPTED cause=indeterminate`; no scanned content appears | Verified 2026-09-15 — Android, OnePlus PLQ110, WeChat 8.0.76, runtime base library 3.17.2; the host ambiguity is preserved as `HostInteractionInterrupted` | Every scan interop, adapter, catalog, or export change |
| Media | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a test image and a test video on the device; a device whose access to photos can be restricted | Run tests; open index and use the Media card: check the capability, choose an image, choose a video, choose either, take one from the camera, then dismiss the picker, then restrict access to photos and choose again | `wechat.choose-media=Supported`; an image selection reports `media choose: PASS count=1, typesValid=true, metadataValid=true`; a video selection reports its duration and dimensions, or reports them absent if the host did not send them; dismissal and restricted access report `media choose: INTERRUPTED cause=indeterminate`; no media content and no complete temporary path appear on the page or in the console | Verified 2026-09-16 — Developer Tools and Android OnePlus PLQ110, WeChat 8.0.76, base library 3.17.2 cover support, image, video, mixed, camera, dismissal, and restricted access; the host ambiguity is preserved as `HostInteractionInterrupted` | Every media interop, adapter, catalog, or export change |
| Bluetooth / BLE (Experimental) | DeveloperTools for the adapter and discovery; RealDevice for connection | `buildMiniAppSdk` completed; Developer Tools on macOS, which refuses every connection call; for connection, a real device and a reachable peripheral | Run tests; open index and use the Bluetooth card: check capability, open adapter, start discovery, watch the device count, stop discovery, close adapter. On a device, connect to a peripheral and then disconnect | Console: `ble capability: PASS wechat.bluetooth-adapter=…, wechat.bluetooth-discovery=…, wechat.bluetooth-connection=…`; `ble adapter: PASS opened=true`; `ble discovery: STARTED`; `ble device: EVENT count=N duplicatePolicy=deviceId-dedup`; `ble discovery: STOPPED`; `ble cleanup: PASS listeners=0`; on a device, `ble connection: PASS connected=true device=<masked>`. No device identifier, MAC address, advertisement payload or raw host message appears anywhere. | Verified 2026-09-18 on an Android real device for the adapter and discovery only: the three capability keys read `Supported`, adapter open and close both succeeded, discovery started and stopped, 32 devices were reported, and the session's listener count fell 2 → 1 → 0 across stop discovery and close adapter. **Connection and disconnection were not run**: no connectable peripheral was available, so they remain unverified and this record is not a complete BLE acceptance. | Every BLE interop, adapter, or listener change |
| Platform Escape Hatch | Unit + Node + host level required by each consumer | Use `WechatPlatformApi` | Run relevant tests and verify each specific WeChat capability | WeChat APIs remain reachable without being universal capabilities | Indirectly covered by Auth, Page Lifecycle, and Navigation | Every platform API surface change |
| Subscription Message | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a test template that exists under the same AppID, configured locally and never committed; a trusted backend and its template conditions for any delivery claim | Run tests; open index and use the Subscription Message card: check the capability, request with no template configured, then configure one locally and request, allowing and refusing, then dismiss the prompt, then repeat with two templates | `wechat.request-subscribe-message=Supported`; an unconfigured request reports `NOT CONFIGURED` with no prompt; a valid response has exactly one non-blank text status per requested template; `accept` is recognized and **every other non-blank status is preserved verbatim**; dismissal remains `HostFailure` and reports only a closed signal label until its exact host message is evidenced; no template id or raw failure appears | Partially verified by `DEVICE-2026-09-16-A`: capability support and the zero-template no-call guard pass. Prompt outcomes are **blocked**, not failed, until the current AppID has a valid template. Acceptance means the subscription state only; delivery is `BackendRequired` and is not claimed here | Every subscription interop, adapter, catalog, or export change |
| Network Status | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a device whose connection can actually be switched | Run tests; open index, check the capabilities, read the current network, then start observation, switch the connection (Wi-Fi to cellular, or off and on), and stop observation | `network-status-query=Supported` and `network-status-listener=Supported`; the query reports the connection kind and `connected=true`; stopping reports `network observation: PASS events=<above zero>, last=<the kind switched to>`; a host with only `on` reports the listener unsupported | Pending — a simulator has no connection to switch, so this needs a device | Every network status interop, adapter, catalog, or export change |
| Upload | Unit + Contract + Node + DeveloperTools + RealDevice + BackendRequired | `buildMiniAppSdk` completed; a controlled HTTPS endpoint listed in the request domain and a file in the sandbox; the example reports `NOT CONFIGURED` until one is supplied | Run tests; open index and use the Network Extensions card: check the capability, run the upload check, then repeat against a slow endpoint and cancel mid-flight | `wechat.upload-file=Supported`; `upload check: PASS status=…, bytes=…, progressSeen=<true\|false>`; cancelling reports `upload cancel: PASS abortInvoked=true` and the card reads `CANCELLED`; no URL, header, path, or response body appears | Pending — `BackendRequired`: a public endpoint is not evidence, and `https://example.com/` is not an upload service | Every upload interop, adapter, catalog, or export change |
| Download | Unit + Contract + Node + DeveloperTools + RealDevice + BackendRequired | `buildMiniAppSdk` completed; a controlled HTTPS endpoint listed in the request domain | Run tests; open index and use the card: check the capability, run the download check, then repeat against a slow endpoint and cancel mid-flight | `wechat.download-file=Supported`; `download check: PASS status=…, fileReported=true, progressSeen=<true\|false>`; cancelling reports `download cancel: PASS abortInvoked=true`; no path or body appears | Pending — `BackendRequired`, as for upload | Every download interop, adapter, catalog, or export change |
| Standard Payment | Unit + Contract + Node + DeveloperTools + RealDevice + BackendRequired | `buildMiniAppSdk` completed; a legal WeChat Pay merchant account bound to this mini program's AppID; a trusted backend that creates the order and produces the signature; a device with the WeChat client | Run tests; open index and use the Standard Payment card: check the capability, request with nothing configured, then paste parameters from the trusted backend locally and request again, completing the payment, dismissing the interface, and reproducing a declined payment | `wechat.request-payment=Supported`; an unconfigured request reports `NOT CONFIGURED` and makes no host call; completing reports `payment request: PASS interactionCompleted=true`, which is not an order fact; ending the interaction reports `payment request: CANCELLED cause=indeterminate`, preserved rather than attributed to the user; a failure reports `payment request: FAIL reason=<closed label>` with no host text; no payment parameter, signature, or merchant identifier appears on the page or in the console; the final order result is recorded from the trusted backend, not from the client | Pending — `BackendRequired`: the automated half passes; the real-host half needs a legal merchant environment | Every payment interop, adapter, catalog, error-mapping, or export change |

`VersionDependent` cannot be produced in Developer Tools: the lowest debug base library it offers is 2.21.4, which is above the 2.20.1 boundary Runtime Detection records. That state is covered by automated tests only. This is a verification-environment limit, not an unimplemented feature.

The microphone run could not produce `NotRequested`, but the later location run produced it on a host and completed the `NotRequested` → `Granted` → `Denied` → successful read after recovery loop. A page that has not queried yet shows `UNKNOWN`, which is an example-page placeholder and deliberately not a member of the public `PermissionState`.

The upload and download checks have a `BackendRequired` half for the same kind of reason: a real transfer needs a controlled HTTPS service the consumer lists in the request domain, so no committed endpoint can stand in. A network transition is different — it needs no service, but it does need a device whose connection can actually be switched, which a simulator cannot provide.

Subscription-message acceptance has a `BackendRequired` half: a subscription state is all the client can establish, and whether a message is ever sent or delivered depends on a WeChat backend template and a trusted backend. That half is recorded separately and is never inferred from a successful client request.

Standard payment has a `BackendRequired` half that is larger than the others, because the client is the wrong place to look for most of the answer. The parameters must come from a trusted backend that holds the merchant key and signs with it, and the order result must come from the same backend, from WeChat Pay's asynchronous notification, or from an order query. A real-host run therefore needs a legal merchant account bound to this mini program's AppID, a real order, and a controlled backend. Without them the automated contract still holds and the capability stays `Partial`: a client callback is never recorded as a payment or an order, and no results are inferred from the simulator, whose payment flow is not a merchant.

A real dismissal signal is the specific open item. The installed base library's own payment flow reports an ended interaction as `requestPayment:cancel`, and only that exact message is classified as an interruption; the `requestPayment:fail cancel` form its other interfaces use is deliberately left as a `HostFailure` until a legal merchant environment shows what a real dismissal actually produces. Nothing in the SDK claims the user cancelled either way.

Virtual payment has unresolved eligibility questions that standard payment does not establish, and that is why the capability stays `Planned` rather than implemented: an implementation could pass every automated check and still be unusable for a given account. A future run would need an account and category confirmed eligible by the then-current official rules, a real but disposable virtual product and order, a separate pass on each platform claimed, the exact signal an ended interaction produces, and the server-side final transaction confirmation. None of that is established offline: the installed Developer Tools base library defines no virtual-payment API, so this simulator cannot provide positive capability evidence, and the official page could not be retrieved from this environment. Until such a run exists, no platform, account or version claim is recorded, and the capability is not registered in the SDK.

Detailed page and Console assertions remain authoritative in [TESTING-en.md](../../TESTING-en.md). This matrix selects environments and manages evidence; it does not duplicate the full UI procedure.

## 4. Release-blocker closing conditions

| Issue | Capability | Required Evidence Before Done |
| --- | --- | --- |
| BOB-70 | Runtime Detection | Unit; DeveloperTools supported/unsupported/version-dependent cases; at least one RealDevice version sample; base-library version recorded. |
| BOB-64 | Permission | Unit + Contract; RealDevice NotRequested, Granted, Denied, and openSetting return; simulator-only evidence is insufficient. |
| BOB-60 | Privacy | Unit + Contract; RealDevice authorization-required, authorized, denied/cancelled; prove separation from Permission. |
| BOB-59 | Standard Payment | Unit + Contract + RealDevice + BackendRequired; success, cancellation, failure; final server order result recorded. |
| BOB-67 | Capability Matrix | Both languages structurally agree; every status cross-checked with source, tests, and host evidence. |
| BOB-71 | Host Verification Matrix | Matrix, evidence template, and documentation entry points complete; an independent executor reruns the current stable checklist and submits a record. |

## 5. Future capability environment requirements

| Tracking | Capability | Minimum Verification |
| --- | --- | --- |
| BOB-74 | Virtual Payment Boundary | Architecture and documentation review — this issue's deliverable, which leaves the capability `Planned`. If it is ever implemented: per-platform RealDevice runs (Android, iOS, HarmonyOS, Developer Tools) with no result carried from one platform to another, a legally eligible account and category, a real but disposable virtual product and order, the actual signal an ended interaction produces, and BackendRequired evidence for the server-side final transaction. |
| BOB-69 | BLE PoC | Unit + Contract + RealDevice; discovery, connect, cancellation, stop, and listener cleanup; remain Experimental. |
| BOB-62 | Bundle Baseline | Automated size report + DeveloperTools startup observation; commit and build mode recorded. |

## 6. Evidence record template

Copy this template for each verification run. One record may cover multiple capabilities, but each needs its own result. Failed or unexecuted checks must remain visible.

```markdown
# WeChat Host Verification Record

- Record ID: WDT|DEVICE|BACKEND-YYYY-MM-DD-sequence
- Git commit:
- SDK version:
- Executor:
- Date and timezone:
- Environment: DeveloperTools | RealDevice | BackendRequired
- Developer Tools version:
- Debug base-library version:
- AppID class: test | development | production-like
- Domain checking: enabled | disabled
- Device model / OS version: N/A for DeveloperTools
- WeChat version: N/A for DeveloperTools
- Network type:
- Backend environment and non-sensitive correlation ID: N/A when not required
- Initial permission/privacy state:

| Capability | Steps Performed | Expected | Actual | Result | Evidence Reference |
| --- | --- | --- | --- | --- | --- |
| Example | ... | ... | ... | PASS / FAIL / NOT RUN | screenshot/log/video path |

## Redaction Check

- Login code absent from artifacts: YES / NO / N/A
- Payment signature absent from artifacts: YES / NO / N/A
- Personal data absent or redacted: YES / NO / N/A

## Deviations and Known Limitations

- ...
```

## 7. Existing evidence registry

| Record ID | Date | Environment | Covered Capabilities | Evidence Source | Limitations |
| --- | --- | --- | --- | --- | --- |
| `WDT-2026-09-14-A` | 2026-09-14 | WeChat Developer Tools Stable 2.01.2510290 | Consumer Bridge, Storage | User-provided visual evidence; recorded in PROJECT_FACTS/DEVELOPMENT | No real-device, permission, or background behavior. |
| `WDT-2026-09-14-B` | 2026-09-14 | WeChat Developer Tools | Authentication Bootstrap | User-provided visual evidence; page showed received and length only | No Backend code exchange; raw code absent. |
| `WDT-2026-09-14-C` | 2026-09-14 | WeChat Developer Tools | HTTP Request | User confirmed Network card passed in real runtime | Domain-check setting and original screenshot are not committed. |
| `WDT-2026-09-15-A` | 2026-09-15 | WeChat Developer Tools | App foreground, Page Lifecycle, Navigation | User confirmed foreground, route, and three navigation operations | No App-background device transition; `switchTab` not implemented. |
| `WDT-2026-09-15-B` | 2026-09-15 | WeChat Developer Tools, debug base library 3.17.3 | Consumer Bridge, Storage, Authentication Bootstrap, HTTP Request, App foreground, Page Lifecycle, Navigation | Independent user rerun screenshots: all baseline page checks passed; Console recorded `navigateTo`, second-page unload, third-page show, `redirectTo`, and `navigateBack` PASS | Documentation baseline commit `59c7db6`; full tool version follows the existing Stable 2.01.2510290 project record; excludes App-background device transition and `switchTab`. |
| `WDT-2026-09-18-A` | 2026-09-18 | WeChat Developer Tools, debug base library 3.17.2 | Navigation `switchTab`, Page Lifecycle regression | Console recorded tab target `SHOWN`, `switch tab: PASS`, tab target `HIDDEN`, and non-tab route `REFUSED HostFailure`; the three page-stack operations also passed regression | Console evidence does not directly show the visual tab highlight; the target route and lifecycle sequence establish the completed switch. |
| `DEVICE-2026-09-18-A` | 2026-09-18 | RealDevice: OnePlus PLQ110, Android 36, WeChat 8.0.76 | Navigation, `switchTab`, Page Lifecycle regression | Console recorded all three page-stack operations PASS, tab target `SHOWN`, `switch tab: PASS wx.switchTab /pages/tabtarget/index`, tab target `HIDDEN`, and non-tab route `REFUSED HostFailure` | Runtime reported base library 3.17.2 while the debug panel showed 3.17.3 `[1641]`; the two unrelated `[wxapplib]` privacy/ad errors are not SDK navigation failures. |
| `WDT-2026-09-15-C` | 2026-09-15 | WeChat Developer Tools, debug base library 3.17.2 | Runtime Detection, Storage, Authentication Bootstrap, HTTP Request, Navigation | Console recorded `runtime detection: PASS baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported`, plus Storage, Authentication Bootstrap, and HTTP Request PASS | `VersionDependent` is not constructible here: Developer Tools' lowest debug base library is 2.21.4, above the 2.20.1 boundary. |
| `WDT-2026-09-15-D` | 2026-09-15 | WeChat Developer Tools Stable 2.01.2510290, debug base library 3.17.2 | Permission | Console recorded `permission query: PASS permission=microphone, state=Denied`, `permission settings: PASS permission=microphone, state=Granted`, and `permission query: PASS permission=microphone, state=Granted`; the card displayed `Granted` and `Denied` | `NotRequested` was not reproducible on this account. The card starts at the example's `UNKNOWN` placeholder and no prompt appears while the page loads. |
| `DEVICE-2026-09-15-B` | 2026-09-15 | RealDevice: OnePlus PLQ110, Android 36, WeChat 8.0.76 | Permission, Runtime Detection, Storage, Authentication Bootstrap, HTTP Request | Console recorded `permission query: PASS … state=Granted`, `permission request: PASS … state=Granted`, `permission settings: PASS … state=Denied`, `permission request: DENIED permission=microphone, state=Denied`, `permission settings: PASS … state=Granted`, and `permission query: PASS … state=Granted`, alongside the Runtime Detection, Storage, Authentication Bootstrap, and HTTP Request results. Full transition: `Granted` → settings → `Denied` → request → `DENIED` → settings → `Granted` → query → `Granted`, with no second prompt after the refusal | The SDK's runtime API reported base library 3.17.2 while the Developer Tools debug panel showed device base library 3.17.3 `[1641]`; recorded as an observed difference, not an SDK failure. `NotRequested` was not reproducible on this account. An unrelated `[wxapplib]` ad-optimisation error also appeared. |
| `DEVICE-2026-09-15-A` | 2026-09-15 | RealDevice: OnePlus PLQ110, Android 36, WeChat 8.0.76 | Runtime Detection, Storage, Authentication Bootstrap, HTTP Request, Navigation | Console recorded `runtime detection: PASS baseLibrary=3.17.2, platform=android, runtime-detection=Supported, storage=Supported, ungated=Unsupported`, plus Storage, Authentication Bootstrap, HTTP Request, and all three navigation operations PASS | The SDK's runtime API reported base library 3.17.2 while the Developer Tools debug panel showed device base library 3.17.3 `[1641]`. Recorded as an observed difference between the device's own base library and the debug runtime, not an SDK failure. An unrelated `[wxapplib]` ad-optimisation error also appeared. |
| `WDT-2026-09-15-E` | 2026-09-15 | WeChat Developer Tools, debug base library 3.17.2 | File System Read, Write, Access, Remove | Console recorded write PASS, read-back `matched=true`, `exists=true` before removal, removal PASS, and `exists=false` afterwards | The user ran write, read, access, remove, access; this still covers the complete state loop. The disabled-domain-check notice is unrelated to file-system behavior. |
| `DEVICE-2026-09-15-C` | 2026-09-15 | RealDevice: Android, runtime base library 3.17.2 | File System Read, Write, Access, Remove | Console recorded `filesystem write: PASS`, `filesystem read: PASS matched=true`, `filesystem access: PASS exists=true`, `filesystem remove: PASS`, and `filesystem access: PASS exists=false` | The device model and WeChat version were not repeated in this evidence text. Console `[wxapplib]` privacy/ad errors came from the WeChat runtime and are unrelated to the file-system path. |
| `WDT-2026-09-15-F` | 2026-09-15 | WeChat Developer Tools Stable 2.01.2510290, debug base library 3.17.2 | Location capability, permission, privacy precondition | Console recorded `wechat.location=Supported`, `state=NotRequested`, `state=Granted` after an explicit request, and a valid result shape; the page requested only after a tap and displayed or logged no coordinates | This AppID reported privacy `NOT_REQUIRED`, so this run does not claim to verify a privacy prompt; BOB-60 continues to track that condition separately. |
| `DEVICE-2026-09-15-D` | 2026-09-15 | RealDevice: Android, runtime base library 3.17.2 | Location permission refusal, guard, and recovery | Console recorded `state=Denied` and `location: DENIED permissionState=Denied`, followed by restored permission and `location: PASS coordinatesValid=true, accuracyValid=true` | The adapter blocked before `getLocation` while refused; success logs contain no coordinates. Completed scope is `getLocation` only and excludes `chooseLocation`, `openLocation`, and location-selection cancellation classification. |
| `DEVICE-2026-09-16-A` | 2026-09-16 | RealDevice: OnePlus PLQ110, Android 36, WeChat 8.0.76, runtime base library 3.17.2 | Subscription capability and unconfigured-request guard | Screenshot and console recorded `subscription capability: PASS wechat.request-subscribe-message=Supported` and `subscription request: NOT CONFIGURED templateCount=0`; no template id was exposed | This path intentionally makes no `wx.requestSubscribeMessage` call, so no consent prompt is expected. Acceptance, refusal, dismissal, multi-template correlation, and delivery remain NOT RUN because the current AppID has no configured valid test template. The visible `[wxapplib]` background-fetch and ad-optimisation errors are runtime noise unrelated to this capability. |

These rows index existing facts without inventing missing fields. The next run must use the full template rather than citing this table alone.

## 8. Regression policy

- Single-capability change: rerun that capability's Unit, Contract, Node, and minimum host level.
- Export, wrapper, Kotlin/JS, or distribution change: rerun the DeveloperTools checklist for all current `Stable` and `Partial` capabilities.
- Permission, privacy, lifecycle, listener, or hardware change: rerun the related RealDevice checks.
- Payment, login exchange, or upload/download test-backend change: rerun BackendRequired checks.
- Every release candidate: run the complete matrix. Record unexecuted checks as `NOT RUN` with a reason; never default them to PASS.
