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
| Platform Escape Hatch | Unit + Node + host level required by each consumer | Use `WechatPlatformApi` | Run relevant tests and verify each specific WeChat capability | WeChat APIs remain reachable without being universal capabilities | Indirectly covered by Auth, Page Lifecycle, and Navigation | Every platform API surface change |

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
| BOB-58 | Check Session | Unit + Node + DeveloperTools; RealDevice/OpenAPI sample; no claim of backend identity verification. |
| BOB-65 | Clipboard | Unit + Contract + DeveloperTools; real-device read/write sample. |
| BOB-65 | Haptics | Unit + RealDevice; short and long vibration separately. |
| BOB-72 | File System | Unit + Contract + DeveloperTools; real-device sandbox path, encoding, and removal sample. |
| BOB-66 | Location | Unit + Contract + RealDevice; permission, privacy, success, and denial. |
| BOB-61 | Scanner | Unit + Contract + RealDevice; QR/barcode, cancellation, permission/privacy. |
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

These rows index existing facts without inventing missing fields. The next run must use the full template rather than citing this table alone.

## 8. Regression policy

- Single-capability change: rerun that capability's Unit, Contract, Node, and minimum host level.
- Export, wrapper, Kotlin/JS, or distribution change: rerun the DeveloperTools checklist for all current `Stable` and `Partial` capabilities.
- Permission, privacy, lifecycle, listener, or hardware change: rerun the related RealDevice checks.
- Payment, login exchange, or upload/download test-backend change: rerun BackendRequired checks.
- Every release candidate: run the complete matrix. Record unexecuted checks as `NOT RUN` with a reason; never default them to PASS.
