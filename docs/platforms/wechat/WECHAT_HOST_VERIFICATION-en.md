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
| Navigation | Unit + Node + DeveloperTools | Example has index, second, and third pages | navigateTo second; redirectTo third; navigateBack | Third replaces second; return reveals index; Console order matches docs | `WDT-2026-09-15-A` | Every navigation interop, adapter, or page-config change |
| Runtime Detection | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; Developer Tools debug base library recorded; one real device available | Run tests; open index and read the Runtime Detection card; repeat on a real device | Card shows the base-library version and platform; `storage` reads `Supported`; `wechat.runtime-detection` reads `Supported`; an ungated capability reads `Unsupported` | `WDT-2026-09-15-C`; `DEVICE-2026-09-15-A`; see PROJECT_FACTS | Every catalog, gate, or export change |
| Permission | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a host whose permission decision can be changed by hand | Run tests; open index and use the Permission Lifecycle card: refresh, request, disable in the host's own settings, refresh, request again, then open settings and re-enable | `Granted` after allowing; `Denied` after disabling; requesting again reports `DENIED` with no second prompt; `Granted` again after a settings visit | `WDT-2026-09-15-D`; `DEVICE-2026-09-15-B`; see PROJECT_FACTS | Every permission interop, adapter, scope-mapping, or export change |
| Privacy | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; the mini program's collection declared in the MP backend privacy guideline | Run tests; open index and use the Privacy Authorization card: refresh, then request; repeat after clearing the account's acceptance | The card shows `REQUIRED` with the host's contract name, then `NOT_REQUIRED` after acceptance; a refusal is reported as `REFUSED` and leaves the requirement in place | Pending | Every privacy interop, adapter, or export change |
| Check Session | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a host whose login state can be cleared | Run tests; open index and read the WeChat Session Check card; clear the login state and reload to see `Invalid`; acquire a new login code, then check again for `Valid` | The card reports `INVALID` before a code is acquired and `VALID` afterwards, and the console line matches | Verified 2026-09-15 — Android, OnePlus PLQ110, WeChat 8.0.76, base library 3.17.3 [1641] | Every session-check interop, adapter, or export change |
| Clipboard | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; clipboard access available | Run tests; open index and use the Clipboard and Haptics card: write the test text, then read it back | `clipboard write: PASS` and `clipboard read: PASS matched=true`; the page reports both as `PASS` | Verified 2026-09-15 — Developer Tools 3.17.2; Android, OnePlus PLQ110, WeChat 8.0.76, base library 3.17.3 [1641] | Every clipboard interop, adapter, or export change |
| Haptics | Unit + Contract + Node + RealDevice | A device with vibration hardware | Run tests; open index and tap `Short vibration`, then `Long vibration` | `haptics short: PASS` and `haptics long: PASS` in the console, and the tester feels each vibration | Verified 2026-09-15 — Android, OnePlus PLQ110, WeChat 8.0.76, base library 3.17.3 [1641]; both vibrations felt | Every haptics interop, adapter, or export change |
| File System | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; the mini program file sandbox is available | Run tests; open index and use the File System card in order: write, check exists, read, remove, check removed | `filesystem write: PASS`, `filesystem access: PASS exists=true`, `filesystem read: PASS matched=true`, `filesystem remove: PASS`, `filesystem access: PASS exists=false` | `WDT-2026-09-15-E`; `DEVICE-2026-09-15-C` | Every file-system interop, adapter, or export change |
| Location | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; the location interface enabled in the MP backend; a host whose permission decision can be changed by hand | Run tests; open index, inspect capability and privacy, then cover `NotRequested`, explicit grant, successful location, refusal in settings, a read while refused, and a successful read after recovery | `wechat.location=Supported`; permission reports `NotRequested`, `Granted`, and `Denied`; refusal reports `location: DENIED permissionState=Denied`; recovery reports `PASS coordinatesValid=true, accuracyValid=true`; no coordinate is displayed or logged | `WDT-2026-09-15-F`; `DEVICE-2026-09-15-D` | Every location interop, adapter, catalog, or export change |
| Scanner | Unit + Contract + Node + DeveloperTools + RealDevice | `buildMiniAppSdk` completed; a reachable test QR code; a device whose camera access can be restricted | Check capability, scan a test code, dismiss deliberately, then restrict camera access and run both regular and camera-only scans | `Supported`; success is `PASS resultPresent=true, typeRecognized=true`; dismissal and camera restriction are both `INTERRUPTED cause=indeterminate`; no scanned content appears | Verified 2026-09-15 — Android, OnePlus PLQ110, WeChat 8.0.76, runtime base library 3.17.2; the host ambiguity is preserved as `HostInteractionInterrupted` | Every scan interop, adapter, catalog, or export change |
| Platform Escape Hatch | Unit + Node + host level required by each consumer | Use `WechatPlatformApi` | Run relevant tests and verify each specific WeChat capability | WeChat APIs remain reachable without being universal capabilities | Indirectly covered by Auth, Page Lifecycle, and Navigation | Every platform API surface change |

`VersionDependent` cannot be produced in Developer Tools: the lowest debug base library it offers is 2.21.4, which is above the 2.20.1 boundary Runtime Detection records. That state is covered by automated tests only. This is a verification-environment limit, not an unimplemented feature.

The microphone run could not produce `NotRequested`, but the later location run produced it on a host and completed the `NotRequested` → `Granted` → `Denied` → successful read after recovery loop. A page that has not queried yet shows `UNKNOWN`, which is an example-page placeholder and deliberately not a member of the public `PermissionState`.

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
| BOB-63 | Media | Unit + Contract + RealDevice; selection, cancellation, temporary file, permission/privacy. |
| BOB-73 | Subscription Message | Unit + Contract + RealDevice; user gesture; BackendRequired for end-to-end delivery. |
| BOB-68 | Upload / Download | Unit + Contract + RealDevice + BackendRequired; progress, success, failure, abort, and file result. |
| BOB-68 | Network Status | Unit + Contract + RealDevice; network transition; no events after unsubscribe. |
| BOB-74 | Virtual Payment Boundary | Architecture and documentation review; RealDevice + BackendRequired if implemented. |
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
| `WDT-2026-09-15-C` | 2026-09-15 | WeChat Developer Tools, debug base library 3.17.2 | Runtime Detection, Storage, Authentication Bootstrap, HTTP Request, Navigation | Console recorded `runtime detection: PASS baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported`, plus Storage, Authentication Bootstrap, and HTTP Request PASS | `VersionDependent` is not constructible here: Developer Tools' lowest debug base library is 2.21.4, above the 2.20.1 boundary. |
| `WDT-2026-09-15-D` | 2026-09-15 | WeChat Developer Tools Stable 2.01.2510290, debug base library 3.17.2 | Permission | Console recorded `permission query: PASS permission=microphone, state=Denied`, `permission settings: PASS permission=microphone, state=Granted`, and `permission query: PASS permission=microphone, state=Granted`; the card displayed `Granted` and `Denied` | `NotRequested` was not reproducible on this account. The card starts at the example's `UNKNOWN` placeholder and no prompt appears while the page loads. |
| `DEVICE-2026-09-15-B` | 2026-09-15 | RealDevice: OnePlus PLQ110, Android 36, WeChat 8.0.76 | Permission, Runtime Detection, Storage, Authentication Bootstrap, HTTP Request | Console recorded `permission query: PASS … state=Granted`, `permission request: PASS … state=Granted`, `permission settings: PASS … state=Denied`, `permission request: DENIED permission=microphone, state=Denied`, `permission settings: PASS … state=Granted`, and `permission query: PASS … state=Granted`, alongside the Runtime Detection, Storage, Authentication Bootstrap, and HTTP Request results. Full transition: `Granted` → settings → `Denied` → request → `DENIED` → settings → `Granted` → query → `Granted`, with no second prompt after the refusal | The SDK's runtime API reported base library 3.17.2 while the Developer Tools debug panel showed device base library 3.17.3 `[1641]`; recorded as an observed difference, not an SDK failure. `NotRequested` was not reproducible on this account. An unrelated `[wxapplib]` ad-optimisation error also appeared. |
| `DEVICE-2026-09-15-A` | 2026-09-15 | RealDevice: OnePlus PLQ110, Android 36, WeChat 8.0.76 | Runtime Detection, Storage, Authentication Bootstrap, HTTP Request, Navigation | Console recorded `runtime detection: PASS baseLibrary=3.17.2, platform=android, runtime-detection=Supported, storage=Supported, ungated=Unsupported`, plus Storage, Authentication Bootstrap, HTTP Request, and all three navigation operations PASS | The SDK's runtime API reported base library 3.17.2 while the Developer Tools debug panel showed device base library 3.17.3 `[1641]`. Recorded as an observed difference between the device's own base library and the debug runtime, not an SDK failure. An unrelated `[wxapplib]` ad-optimisation error also appeared. |
| `WDT-2026-09-15-E` | 2026-09-15 | WeChat Developer Tools, debug base library 3.17.2 | File System Read, Write, Access, Remove | Console recorded write PASS, read-back `matched=true`, `exists=true` before removal, removal PASS, and `exists=false` afterwards | The user ran write, read, access, remove, access; this still covers the complete state loop. The disabled-domain-check notice is unrelated to file-system behavior. |
| `DEVICE-2026-09-15-C` | 2026-09-15 | RealDevice: Android, runtime base library 3.17.2 | File System Read, Write, Access, Remove | Console recorded `filesystem write: PASS`, `filesystem read: PASS matched=true`, `filesystem access: PASS exists=true`, `filesystem remove: PASS`, and `filesystem access: PASS exists=false` | The device model and WeChat version were not repeated in this evidence text. Console `[wxapplib]` privacy/ad errors came from the WeChat runtime and are unrelated to the file-system path. |
| `WDT-2026-09-15-F` | 2026-09-15 | WeChat Developer Tools Stable 2.01.2510290, debug base library 3.17.2 | Location capability, permission, privacy precondition | Console recorded `wechat.location=Supported`, `state=NotRequested`, `state=Granted` after an explicit request, and a valid result shape; the page requested only after a tap and displayed or logged no coordinates | This AppID reported privacy `NOT_REQUIRED`, so this run does not claim to verify a privacy prompt; BOB-60 continues to track that condition separately. |
| `DEVICE-2026-09-15-D` | 2026-09-15 | RealDevice: Android, runtime base library 3.17.2 | Location permission refusal, guard, and recovery | Console recorded `state=Denied` and `location: DENIED permissionState=Denied`, followed by restored permission and `location: PASS coordinatesValid=true, accuracyValid=true` | The adapter blocked before `getLocation` while refused; success logs contain no coordinates. Completed scope is `getLocation` only and excludes `chooseLocation`, `openLocation`, and location-selection cancellation classification. |

These rows index existing facts without inventing missing fields. The next run must use the full template rather than citing this table alone.

## 8. Regression policy

- Single-capability change: rerun that capability's Unit, Contract, Node, and minimum host level.
- Export, wrapper, Kotlin/JS, or distribution change: rerun the DeveloperTools checklist for all current `Stable` and `Partial` capabilities.
- Permission, privacy, lifecycle, listener, or hardware change: rerun the related RealDevice checks.
- Payment, login exchange, or upload/download test-backend change: rerun BackendRequired checks.
- Every release candidate: run the complete matrix. Record unexecuted checks as `NOT RUN` with a reason; never default them to PASS.
