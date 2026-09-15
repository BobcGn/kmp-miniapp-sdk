# WeChat Capability Matrix

[中文](WECHAT_CAPABILITIES-ch.md)

This document is a factual index of WeChat host capability status, not a roadmap. Source code and executable Gradle configuration take precedence. A capability may be marked `Stable` only when implementation, automated tests, and the required host-verification evidence exist. See the [WeChat real-host verification matrix](WECHAT_HOST_VERIFICATION-en.md) for environments, procedures, evidence format, and regression frequency.

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

`Minimum Host Version` records the oldest base library that provides the capability, and has three values:

- `Resolved at runtime` — the SDK gates the capability and asks the host through `wx.canIUse` rather than comparing against a recorded figure. The official entry pages for the current Storage APIs and `wx.request` do not state an introduction version for the API itself, so the repository does not infer one from a development machine. This is a live answer for the host actually running, not a missing value.
- A version number — a boundary WeChat documents. The repository records it only where it can cite the source.
- `Not established` — no minimum has been established, either because the capability is not implemented or because the SDK does not gate it.

The SDK reads the base-library version from `wx.getAppBaseInfo` and falls back to the unmaintained `wx.getSystemInfoSync` on base libraries that predate it.

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
- [ADR-0008](../../decisions/0008-permission-lifecycle-boundary-en.md): the permission lifecycle, its user-gesture rule, and why it stays separate from privacy.
- [ADR-0009](../../decisions/0009-privacy-authorization-boundary-en.md): the privacy authorization boundary, including why a refusal is an outcome and why no `Cancelled` state exists.
- [WeChat `wx.getPrivacySetting` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/privacy/wx.getPrivacySetting.html) and [`wx.requirePrivacyAuthorize` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/privacy/wx.requirePrivacyAuthorize.html): supported from base library 2.32.3; below it the host does not intercept privacy-gated calls.
- [WeChat `wx.canIUse` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/base/wx.canIUse.html): supported from base library 1.1.1.
- [WeChat `wx.getAppBaseInfo` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getAppBaseInfo.html) and [`wx.getDeviceInfo` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getDeviceInfo.html): supported from base library 2.20.1.
- [WeChat `wx.getSystemInfoSync` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getSystemInfoSync.html): unmaintained from base library 2.20.1 and retained as the older base-library fallback for version and platform.
- [WeChat `RequestTask.abort` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/network/request/RequestTask.abort.html): supported from base library 1.4.0.
- [WeChat `wx.getSetting` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/setting/wx.getSetting.html) and [`wx.authorize` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/authorize/wx.authorize.html): supported from base library 1.2.0.
- [WeChat `wx.openSetting` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/setting/wx.openSetting.html): supported from base library 1.1.0, and callable only from a user gesture from base library 2.3.0.
- [WeChat `wx.getFileSystemManager` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/file/wx.getFileSystemManager.html) with [`FileSystemManager.readFile`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.readFile.html), [`writeFile`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.writeFile.html), [`access`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.access.html), and [`unlink`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.unlink.html): supported from base library 1.9.9. The `access` and `unlink` pages document `no such file or directory` as the failure text for a missing path, which is what `wechatFileExists` classifies.
- [WeChat `wx.getClipboardData` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.getClipboardData.html) and [`wx.setClipboardData` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.setClipboardData.html): supported from base library 1.1.0.
- [WeChat `wx.vibrateShort` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/device/vibrate/wx.vibrateShort.html) and [`wx.vibrateLong` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/device/vibrate/wx.vibrateLong.html): supported from base library 1.2.0. The `type` field `wx.vibrateShort` documents (heavy / medium / light) is supported from base library 2.13.0 and is deliberately not modelled.
- [WeChat `wx.checkSession` documentation](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.checkSession.html): states no minimum base library, so the entry relies on `wx.canIUse` rather than a recorded figure. It defines success/fail as valid/expired login state, so the adapter uses the callback itself rather than parsing `errMsg`.

Entries marked `Planned` have no implementation in those production sources or exports. Their Tracking value records a Multica gap only; it is not evidence that the capability exists.

## Core and WeChat ecosystem capabilities

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Consumer Bridge | `Stable` | Kotlin/JS CommonJS artifact | None | Not applicable | Unit, Node, DeveloperTools | `buildMiniAppSdk`, `.d.ts`, smoke, and the WeChat example are verified. | BOB-45 Done |
| Runtime Detection | `Stable` | `canIUse`, `getAppBaseInfo`, with legacy `getSystemInfoSync` fallback | None | 2.20.1 | Unit, Contract, Node, DeveloperTools, RealDevice | 2.20.1 is the documented boundary of the modern `getAppBaseInfo` path; the legacy path can still read the version and report `VersionDependent`. `requireSupported` produces `UnsupportedCapability`. Developer Tools (base library 3.17.2) and an Android device both verified `Supported` and `Unsupported`; `VersionDependent` cannot be constructed in Developer Tools because its lowest debug base library, 2.21.4, is above the boundary, so automated tests cover it. 3.17.2 is the current verification version, not a proven minimum supported version. | BOB-70 Done |
| Permission | `Stable` | `getSetting`, `authorize`, `openSetting` | User gesture for request and settings; a host does not re-prompt after a refusal | 1.2.0 | Unit, Contract, Node, DeveloperTools, RealDevice | Three-state lifecycle with no caching, and a refusal that is a distinct SDK error rather than a host failure. `openSetting` is documented from base library 1.1.0 and `getSetting` and `authorize` from 1.2.0, so 1.2.0 is the oldest base library providing all three; the capability gate still probes all three at runtime. Only the microphone permission is mapped. `NotRequested` is covered by automated tests because a host that has already recorded a decision does not report it again. 3.17.2 is the current verification version, not a proven minimum supported version. | BOB-64 Done |
| Privacy | `Partial` | `getPrivacySetting`, `requirePrivacyAuthorize` | The mini program must declare its collection in the MP backend privacy guideline; the host presents the prompt | 2.32.3 | Unit, Contract, Node | Three-state-free: the requirement, the result of one attempt, and errors are separate models, and privacy shares no state with permission. WeChat publishes no field that distinguishes declining from dismissing, so no `Cancelled` outcome exists; a recognizable refusal maps to `Refused`, while an unknown failure remains a `HostFailure`. DeveloperTools and RealDevice evidence is still required. | BOB-60 |
| Storage | `Stable` | `getStorage`, `setStorage`, `removeStorage` | None | Resolved at runtime | Unit, Contract, Node, DeveloperTools | String get/set/overwrite/remove, missing keys, and idempotent removal are covered. | BOB-50 Done |
| Storage Clear | `Unsupported` | `clearStorage` | None | Not established | None | The public contract intentionally does not clear all consumer data. | Matrix record |
| HTTP Request | `Stable` | `request` | Legal HTTPS request domain | Resolved at runtime | Unit, Contract, Node, DeveloperTools | Method, URL, headers, text body, status, timeout, failure, and abort on cancellation are covered. `RequestTask.abort` requires base library 1.4.0; below it a request still completes but cancellation cannot abort it. | BOB-48 Done |
| Upload | `Planned` | `uploadFile` | Legal upload domain and readable file | Not established | None | No UploadTask, progress, or abort adapter exists. | BOB-68 |
| Download | `Planned` | `downloadFile` | Legal download domain and file sandbox | Not established | None | No DownloadTask, progress, or file-result adapter exists. | BOB-68 |
| Network Status | `Planned` | `getNetworkType`, `on/offNetworkStatusChange` | None | Not established | None | Paired on/off cleanup must be verified. | BOB-68 |
| WebSocket | `Planned` | WebSocket APIs | Legal socket domain | Not established | None | Production support is outside BOB-68. | Matrix record |
| Authentication Bootstrap | `Partial` | `login` | Backend performs code exchange | Not established | Unit, Node, DeveloperTools | `wechatLogin()` returns a short-lived code, not identity, SDK session, or access token. | BOB-47 Done |
| Check Session | `Stable` | `checkSession` | None | Resolved at runtime | Unit, Contract, Node, RealDevice | Reports whether WeChat's client login state is still intact. WeChat-specific and namespaced; a valid answer is not an authenticated user, a backend session, or a token. The success/fail callbacks map to `Valid`/`Invalid` without parsing raw `errMsg`. WeChat's API page records no minimum base library, so the capability gate probes instead. An Android real-device run verified `Invalid → wx.login → Valid`. | BOB-58 |
| Standard Payment | `Planned` | `requestPayment` | Valid merchant, Backend order/signature, real device | Not established | None | Client success must not be final order truth. | BOB-59 |
| Virtual Payment | `Planned` | `requestVirtualPayment` | Platform eligibility and Backend | Not established | None | Must remain separate from Standard Payment. | BOB-74 |
| Subscription Message | `Planned` | `requestSubscribeMessage` | User gesture, template, and Backend | Not established | None | WeChat-specific, not universal push notification. | BOB-73 |
| Navigation | `Partial` | `navigateTo`, `redirectTo`, `navigateBack` | Valid page route | Not established | Unit, Node, DeveloperTools | Three operations are verified; `switchTab` is not implemented. | BOB-49 |
| App Lifecycle | `Partial` | Consumer-forwarded launch/show/hide | Consumer forwards host hooks | Resolved at runtime | Unit, Contract, Node, DeveloperTools | Foreground is verified; background transition lacks RealDevice evidence. | BOB-49 |
| Page Lifecycle | `Partial` | Consumer-forwarded show/hide/unload | Consumer forwards host hooks | Not applicable | Unit, Node, DeveloperTools | WeChat escape hatch; Page load is not public, per ADR-0006. | BOB-49 |
| Platform Escape Hatch | `Stable` | `WechatPlatformApi` | WeChat host only | Not applicable | Unit, Node | WeChat Auth, Navigation, and Page Lifecycle are not represented as universal. | BOB-46 Done |
| Toast | `Partial` | `showToast` | None | Not established | Unit | Typed interop only; no production adapter or export. | Matrix record |

## Device and system capabilities

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Location | `Planned` | `getLocation`; assess choose/open location as needed | Location permission + Privacy | Not established | None | No interop, adapter, export, or real-device evidence. | BOB-66 |
| Scanner | `Planned` | `scanCode` | Camera/Privacy | Not established | None | Cancellation must differ from permission denial and host failure. | BOB-61 |
| Clipboard Read | `Stable` | `getClipboardData` | User gesture; WeChat Tools must have clipboard access | 1.1.0 | Unit, Contract, Node, DeveloperTools, RealDevice | Returns the clipboard text as-is, including an empty string; a missing or non-string answer is `InvalidResponse`. The SDK does not store or log what it reads. `getClipboardData` is not an allowed `requiredPrivateInfos` entry. | BOB-65 |
| Clipboard Write | `Stable` | `setClipboardData` | User gesture; WeChat Tools must have clipboard access | 1.1.0 | Unit, Contract, Node, DeveloperTools, RealDevice | Gated separately from the read, because a host may expose one direction without the other. Real hosts verified a write followed by a matching read. | BOB-65 |
| Short Vibration | `Stable` | `vibrateShort` | User gesture; device with vibration hardware | 1.2.0 | Unit, Contract, Node, RealDevice | The Android device reported PASS and the tester confirmed feeling the short vibration. The optional `type` field (2.13.0) is not modelled. | BOB-65 |
| Long Vibration | `Stable` | `vibrateLong` | User gesture; device with vibration hardware | 1.2.0 | Unit, Contract, Node, RealDevice | A separate host API from the short vibration. The Android device reported PASS and the tester confirmed feeling the long vibration. | BOB-65 |
| Media | `Planned` | `chooseMedia` | Album/Camera permission + Privacy | Not established | None | Excludes Camera and Video native components. | BOB-63 |
| File System Read | `Stable` | `getFileSystemManager().readFile` | Mini Program file sandbox; `encoding` is always UTF-8 | 1.9.9 | Unit, Contract, Node, DeveloperTools, RealDevice | Reads text; an empty file is an empty string and binary content is `InvalidResponse`. Developer Tools and an Android device at base library 3.17.2 both verified matching read-back. | BOB-72 |
| File System Write | `Stable` | `getFileSystemManager().writeFile` | Mini Program file sandbox; the parent directory must exist | 1.9.9 | Unit, Contract, Node, DeveloperTools, RealDevice | Writes UTF-8 text, replacing what is there; both real-host environments at base library 3.17.2 verified success. | BOB-72 |
| File System Access | `Stable` | `getFileSystemManager().access` | Mini Program file sandbox | 1.9.9 | Unit, Contract, Node, DeveloperTools, RealDevice | Reports `false` only for the failure text WeChat documents for a missing path; any other failure raises. Real-host evidence covers `true` before removal and `false` afterwards. | BOB-72 |
| File System Remove | `Stable` | `getFileSystemManager().unlink` | Mini Program file sandbox | 1.9.9 | Unit, Contract, Node, DeveloperTools, RealDevice | Removing a missing file fails, following WeChat's contract. The sandbox root is gated as `wechat.filesystem-sandbox-path`; real-host evidence covers the removal loop. | BOB-72 |
| Bluetooth / BLE | `Planned` | Adapter/discovery/event/connect APIs | Bluetooth permission + Privacy + RealDevice | Not established | None | A PoC will validate Flow, cancellation, and listener cleanup; no implementation exists, so it is not `Experimental`. | BOB-69 |
| Sensors | `Planned` | Accelerometer, gyroscope, compass, beacon, and others | API-specific and real device | Not established | None | P1 does not implement sensors in bulk; status only. | Matrix record |

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
4. Record a `Minimum Host Version` only from a citable WeChat source, or as `Resolved at runtime` when the SDK gates the capability by asking the host. Never infer a version from a development machine, and never leave a version number without its source.
5. Native UI remains `P3-Presentation`; this matrix must not start Renderer, Compose, Virtual DOM, or WXML work.
