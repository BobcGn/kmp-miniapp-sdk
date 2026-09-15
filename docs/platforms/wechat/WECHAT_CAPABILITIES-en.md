# WeChat Capability Matrix

[中文](WECHAT_CAPABILITIES-ch.md)

This document is a factual index of WeChat host capability status, not a roadmap. Source code and executable Gradle configuration take precedence. A capability may be marked `Stable` only when implementation, automated tests, and the required host-verification evidence exist.

## Status and verification levels

| Status | Meaning |
| --- | --- |
| `Stable` | The stated scope is implemented and has applicable automated and WeChat Developer Tools verification. |
| `Experimental` | A runnable implementation or proof of concept exists, but its API or behavior is not stable. |
| `Partial` | Only the listed APIs, semantics, or verification levels are complete. |
| `Planned` | Not implemented; an issue or plan is not implementation evidence. |
| `Unsupported` | Intentionally not provided by the current SDK. |
| `P3-Presentation` | A native view or presentation concern outside P1 Host Capability scope. |

| Test Level | Meaning |
| --- | --- |
| `Unit` | Kotlin/JS unit tests or focused adapter tests. |
| `Contract` | The same semantic contract is verified against a reference implementation and WeChat adapter. |
| `Node` | CommonJS smoke or TypeScript checks; this does not prove WeChat runtime compatibility. |
| `DeveloperTools` | Compiled, loaded, and run in WeChat Developer Tools. |
| `RealDevice` | Must be or has been verified on a real WeChat device runtime. |
| `BackendRequired` | Complete verification requires a consumer backend or controlled test service. |

`Minimum Host Version` is consistently “Pending BOB-70.” The repository does not implement `wx.canIUse` or base-library version gating, so versions must not be inferred from a development machine.

## Evidence sources

This matrix was established on 2026-09-15 against the following sources of fact:

- [PROJECT_FACTS-en.md](../../PROJECT_FACTS-en.md): current implementation and host-verification facts.
- [ARCHITECTURE-en.md](../../ARCHITECTURE-en.md): Host, Capability, platform escape hatch, async, and error boundaries.
- [TESTING-en.md](../../TESTING-en.md): Unit, Contract, Node, and DeveloperTools evidence and limitations.
- [`commonMain` capability contracts](../../../sdk/src/commonMain/kotlin/io/github/bobcgn/miniapp/capability): public Storage, HTTP transport, and App lifecycle contracts.
- [`jsMain` WeChat host](../../../sdk/src/jsMain/kotlin/io/github/bobcgn/miniapp/host/wechat): WeChat interop, adapters, runtime, and platform API.
- [`jsMain` export facade](../../../sdk/src/jsMain/kotlin/io/github/bobcgn/miniapp/export): JavaScript / TypeScript consumer surface.
- [`commonTest`](../../../sdk/src/commonTest) and [`jsTest`](../../../sdk/src/jsTest): shared contracts and WeChat adapter tests.
- [ADR-0006](../../decisions/0006-lifecycle-and-navigation-boundary-en.md): App, Page, and navigation boundaries.

Entries marked `Planned` have no implementation in those production sources or exports. Their Tracking value records a Multica gap only; it is not evidence that the capability exists.

## Core and WeChat ecosystem capabilities

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Consumer Bridge | `Stable` | Kotlin/JS CommonJS artifact | None | Not applicable | Unit, Node, DeveloperTools | `buildMiniAppSdk`, `.d.ts`, smoke, and the WeChat example are verified. | BOB-45 Done |
| Runtime Detection | `Planned` | `wx.canIUse`, runtime info | None | Pending BOB-70 | None | Only static `Supported` / `Unsupported` states exist. | BOB-70 |
| Permission | `Planned` | `getSetting`, `authorize`, `openSetting` | User action and API-specific scope | Pending BOB-70 | None | No NotRequested / Granted / Denied lifecycle exists. | BOB-64 |
| Privacy | `Planned` | `getPrivacySetting` | Privacy policy and user authorization | Pending BOB-70 | None | Privacy must remain separate from Permission. | BOB-60 |
| Storage | `Stable` | `getStorage`, `setStorage`, `removeStorage` | None | Pending BOB-70 | Unit, Contract, Node, DeveloperTools | String get/set/overwrite/remove, missing keys, and idempotent removal are covered. | BOB-50 Done |
| Storage Clear | `Unsupported` | `clearStorage` | None | Pending BOB-70 | None | The public contract intentionally does not clear all consumer data. | Matrix record |
| HTTP Request | `Stable` | `request` | Legal HTTPS request domain | Pending BOB-70 | Unit, Contract, Node, DeveloperTools | Method, URL, headers, text body, status, timeout, failure, and abort on cancellation are covered. | BOB-48 Done |
| Upload | `Planned` | `uploadFile` | Legal upload domain and readable file | Pending BOB-70 | None | No UploadTask, progress, or abort adapter exists. | BOB-68 |
| Download | `Planned` | `downloadFile` | Legal download domain and file sandbox | Pending BOB-70 | None | No DownloadTask, progress, or file-result adapter exists. | BOB-68 |
| Network Status | `Planned` | `getNetworkType`, `on/offNetworkStatusChange` | None | Pending BOB-70 | None | Paired on/off cleanup must be verified. | BOB-68 |
| WebSocket | `Planned` | WebSocket APIs | Legal socket domain | Pending BOB-70 | None | Production support is outside BOB-68. | Matrix record |
| Authentication Bootstrap | `Partial` | `login` | Backend performs code exchange | Pending BOB-70 | Unit, Node, DeveloperTools | `wechatLogin()` returns a short-lived code, not identity, SDK session, or access token. | BOB-47 Done |
| Check Session | `Planned` | `checkSession` | Backend identity verification still required | Pending BOB-70 | None | Login-state validity cannot currently be queried. | BOB-58 |
| Standard Payment | `Planned` | `requestPayment` | Valid merchant, Backend order/signature, real device | Pending BOB-70 | None | Client success must not be final order truth. | BOB-59 |
| Virtual Payment | `Planned` | `requestVirtualPayment` | Platform eligibility and Backend | Pending BOB-70 | None | Must remain separate from Standard Payment. | BOB-74 |
| Subscription Message | `Planned` | `requestSubscribeMessage` | User gesture, template, and Backend | Pending BOB-70 | None | WeChat-specific, not universal push notification. | BOB-73 |
| Navigation | `Partial` | `navigateTo`, `redirectTo`, `navigateBack` | Valid page route | Pending BOB-70 | Unit, Node, DeveloperTools | Three operations are verified; `switchTab` is not implemented. | BOB-49 |
| App Lifecycle | `Partial` | Consumer-forwarded launch/show/hide | Consumer forwards host hooks | Pending BOB-70 | Unit, Contract, Node, DeveloperTools | Foreground is verified; background transition lacks RealDevice evidence. | BOB-49 |
| Page Lifecycle | `Partial` | Consumer-forwarded show/hide/unload | Consumer forwards host hooks | Pending BOB-70 | Unit, Node, DeveloperTools | WeChat escape hatch; Page load is not public, per ADR-0006. | BOB-49 |
| Platform Escape Hatch | `Stable` | `WechatPlatformApi` | WeChat host only | Not applicable | Unit, Node | WeChat Auth, Navigation, and Page Lifecycle are not represented as universal. | BOB-46 Done |
| Toast | `Partial` | `showToast` | None | Pending BOB-70 | Unit | Typed interop only; no production adapter or export. | Matrix record |

## Device and system capabilities

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Location | `Planned` | `getLocation`; assess choose/open location as needed | Location permission + Privacy | Pending BOB-70 | None | No interop, adapter, export, or real-device evidence. | BOB-66 |
| Scanner | `Planned` | `scanCode` | Camera/Privacy | Pending BOB-70 | None | Cancellation must differ from permission denial and host failure. | BOB-61 |
| Clipboard | `Planned` | Clipboard get/set | Host-specific rules | Pending BOB-70 | None | Not implemented. | BOB-65 |
| Haptics | `Planned` | Short/long vibration | Real-device hardware | Pending BOB-70 | None | RealDevice evidence is required. | BOB-65 |
| Media | `Planned` | `chooseMedia` | Album/Camera permission + Privacy | Pending BOB-70 | None | Excludes Camera and Video native components. | BOB-63 |
| File System | `Planned` | `getFileSystemManager` read/write/access/remove | Mini Program file sandbox | Pending BOB-70 | None | P1 requires only basic file operations. | BOB-72 |
| Bluetooth / BLE | `Planned` | Adapter/discovery/event/connect APIs | Bluetooth permission + Privacy + RealDevice | Pending BOB-70 | None | A PoC will validate Flow, cancellation, and listener cleanup; no implementation exists, so it is not `Experimental`. | BOB-69 |
| Sensors | `Planned` | Accelerometer, gyroscope, compass, beacon, and others | API-specific and real device | Pending BOB-70 | None | P1 does not implement sensors in bulk; status only. | Matrix record |

## Native UI and presentation

| Capability | Status | Component | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Camera UI | `P3-Presentation` | `camera` | Camera permission + Privacy | Outside P1 | None | Native view, not a P1 Host Capability. | P3 |
| Map UI | `P3-Presentation` | `map` | Location permission + Privacy | Outside P1 | None | Location is P1; the map view is presentation. | P3 |
| Video UI | `P3-Presentation` | `video`, `live-player` | Component-specific | Outside P1 | None | Outside the current SDK renderer scope. | P3 |
| Canvas | `P3-Presentation` | `canvas` | Component-specific | Outside P1 | None | Does not start Renderer, Virtual DOM, or WXML work. | P3 |
| Web View / Editor / Other Native UI | `P3-Presentation` | `web-view`, `editor`, others | Component-specific | Outside P1 | None | Deferred to a Presentation phase. | P3 |

## Maintenance rules

1. When a WeChat capability issue is added or closed, review both language versions in the same change.
2. `Stable` requires implementation and specified verification evidence. A class, interface, roadmap entry, or issue alone is only `Planned`.
3. DeveloperTools evidence does not replace RealDevice or BackendRequired evidence.
4. Do not record speculative minimum base-library versions before BOB-70 is complete.
5. Native UI remains `P3-Presentation`; this matrix must not start Renderer, Compose, Virtual DOM, or WXML work.
