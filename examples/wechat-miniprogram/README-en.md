# WeChat Mini Program example host

[中文](README-ch.md)

This directory is the minimal WeChat Mini Program integration host for the SDK Consumer Bridge.

The integration loop is:

```text
Gradle SDK build
      ↓
JavaScript artifact
      ↓
miniprogram/libs/
      ↓
TypeScript require()
      ↓
WeChat Developer Tools
```

The example has four pages, two of which are declared in `app.json`'s `tabBar`. The index page imports the normalized CommonJS SDK, calls `sdkVersion()`, exercises Storage, requests a short-lived WeChat login code, performs an HTTPS request through the SDK HTTP transport, and shows the app lifecycle state, the current page route, and what the runtime reports about itself together with the support state of each gated capability. The second and third pages exist so the page-stack bridge can be exercised: the second is opened with `wx.navigateTo` and replaces itself with the third using `wx.redirectTo`, and the third goes back with `wx.navigateBack`. The tabtarget page is the second `tabBar` entry, and it exists so `wx.switchTab` can be exercised: switching to it moves the tab bar's highlight, hides the index page rather than unloading it, and switching to a route with no tab is refused by the host and reported as a closed category. The login code itself is never logged or rendered.

`app.ts` forwards WeChat's App hooks, because WeChat reports lifecycle only to the registration the consumer owns. Each page forwards its own Page hooks and supplies its own `this.route`.

## Local verification

```shell
npm install
npm run smoke
npm run typecheck
```

## WeChat Developer Tools

Import this directory as a Mini Program project and compile it. The clipboard and vibration card needs a device for the vibration checks, and the clipboard checks need clipboard access. The index page must display `0.1.0-SNAPSHOT`, a `FOREGROUND` lifecycle state with a page route, the base-library version, and a `PASS` status for Runtime detection, Storage, the client login code, and the network check. The console must contain:

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
[kmp-miniapp-sdk] switch tab: PASS wx.switchTab /pages/tabtarget/index
[kmp-miniapp-sdk] runtime detection: PASS baseLibrary=…, platform=…, runtime-detection=…, storage=Supported, ungated=Unsupported
[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null
[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<positive integer>
[kmp-miniapp-sdk] network: PASS status=200, bytes=<positive integer>
[kmp-miniapp-sdk] session check: PASS check #N, state=Valid
[kmp-miniapp-sdk] clipboard write: PASS
[kmp-miniapp-sdk] clipboard read: PASS matched=true
[kmp-miniapp-sdk] haptics short: PASS
[kmp-miniapp-sdk] haptics long: PASS
[kmp-miniapp-sdk] filesystem write: PASS
[kmp-miniapp-sdk] filesystem access: PASS exists=true
[kmp-miniapp-sdk] filesystem read: PASS matched=true
[kmp-miniapp-sdk] filesystem remove: PASS
[kmp-miniapp-sdk] filesystem access: PASS exists=false
[kmp-miniapp-sdk] location capability: PASS wechat.location=Supported
[kmp-miniapp-sdk] location permission query: PASS state=Granted
[kmp-miniapp-sdk] location privacy query: PASS requirement=NOT_REQUIRED
[kmp-miniapp-sdk] location: PASS coordinatesValid=true, accuracyValid=true
[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=Supported
[kmp-miniapp-sdk] scan: PASS resultPresent=true, typeRecognized=true
[kmp-miniapp-sdk] scan: INTERRUPTED cause=indeterminate
[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=Supported
[kmp-miniapp-sdk] media choose: PASS count=1, typesValid=true, metadataValid=true
[kmp-miniapp-sdk] media choose: INTERRUPTED cause=indeterminate
[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=Supported
[kmp-miniapp-sdk] subscription request: NOT CONFIGURED templateCount=0
[kmp-miniapp-sdk] subscription request: PASS accepted=1, otherStatuses=0, signals=accept
[kmp-miniapp-sdk] subscription request: FAIL reason=HostFailure, signal=fail-cancel
[kmp-miniapp-sdk] network capabilities: PASS query=Supported, listener=Supported, upload=Supported, download=Supported
[kmp-miniapp-sdk] network type: PASS connected=true, type=WIFI
[kmp-miniapp-sdk] network observation: PASS events=2, last=CELLULAR_4G
[kmp-miniapp-sdk] upload check: NOT CONFIGURED
[kmp-miniapp-sdk] download check: NOT CONFIGURED
[kmp-miniapp-sdk] payment capability: PASS wechat.request-payment=Supported
[kmp-miniapp-sdk] payment request: NOT CONFIGURED
[kmp-miniapp-sdk] payment request: PASS interactionCompleted=true
[kmp-miniapp-sdk] payment request: CANCELLED cause=indeterminate
[kmp-miniapp-sdk] payment request: FAIL reason=host-failure
```

The Network Extensions card covers five host APIs, gated one at a time: the network type query, the network status listener, upload, and download. `Check network capabilities` reports each answer; `Get current network type` performs a query that registers nothing; `Start`/`Stop network status observation` register one host listener and remove it, and the stop line reports how many changes arrived and the last connection kind. The upload and download checks do nothing at all until you configure a controlled HTTPS endpoint locally — the card reports `NOT CONFIGURED` and makes no host call — and their results report a status, a byte count, and whether the host reported progress, never a URL, header, file path, or response body. `Cancel upload` and `Cancel download` stop an in-flight transfer and report whether a host abort was actually invoked.

The observation half needs a device: a simulator has no connection to switch. The transfer half is `BackendRequired`: it needs a controlled HTTPS service listed in the request domain, so `https://example.com/` cannot stand in for one. See the network extensions section of [../../docs/DEVELOPMENT-en.md](../../docs/DEVELOPMENT-en.md).

The Standard Payment card forwards parameters a trusted backend produced to WeChat's own payment interface. The page commits an empty placeholder, so `Request payment` reports `NOT CONFIGURED` and makes no host call at all; the card only reaches the host if you fill that placeholder in locally, from your own backend, in a legal merchant environment. Nothing about a payment is printed — not a timestamp, a nonce, a package, a signature, or the host's own failure text — so a screenshot of the page carries nothing from a payment interaction, and a failure is reduced to a closed label such as `reason=host-failure`. `PASS interactionCompleted=true` means the host reported that the interaction completed; it is not an order, and the card says so. Confirm the order with your backend, which learns the truth from WeChat Pay's server API, its asynchronous notification, or an order query. A real payment needs a legal merchant account, a real order, and a signing backend, which is `BackendRequired`.

The network check issues a `GET` to `https://example.com/`. WeChat requires that host to be listed in the request domain whitelist, or the project must be compiled with domain checking disabled. Change the `networkUrl` constant to verify a different endpoint.

Navigation needs interaction rather than a single page load. Tapping through the second and third pages prints `[kmp-miniapp-sdk] navigation: PASS <action>` for `wx.navigateTo`, `wx.redirectTo`, and `wx.navigateBack`; the tap sequence is in the checklist. The Tab Switching card switches to the tabtarget tab with `wx.switchTab` and prints `[kmp-miniapp-sdk] switch tab: PASS`; its second button asks for the third page, which has no tab, and prints only the closed category the SDK reported, never the host's own failure text.

The File System card writes a fixed, non-sensitive string to a fixed file name in the mini program sandbox, reads it back, checks that it is there, removes it, and checks again that it is gone. Nothing touches the file system while the page loads. Only UTF-8 text is supported, only inside the sandbox, and the sandbox root itself is never displayed or logged. Removing a file that is not there fails, following WeChat's contract, so the remove and the final check are meant to be run once each, in order.

The Clipboard and Haptics card writes a fixed, non-sensitive test string, reads the clipboard back to compare, and triggers the short and long vibrations. Nothing touches the clipboard or the vibrator while the page loads; every action follows a tap. The read only ever compares against the string this page wrote, and the page never displays or logs whatever else the clipboard holds. A successful vibration line means WeChat accepted the call, not that a vibration was felt — that can only be checked on a real device. `getClipboardData` is not an allowed `app.json.requiredPrivateInfos` entry, so the example does not declare it there.

The WeChat Session Check card asks WeChat whether its own client login state is still usable. It runs once while the page loads, deliberately before the login bootstrap: acquiring a code refreshes the client login state and would hide an expired session, so the startup order is check first, acquire later. A valid answer is not an authenticated user or a backend session, and an invalid one acquires no code by itself.

The Privacy Authorization card queries what the host requires for its own privacy contract and, only from a button, asks the host for the user's acceptance. The query runs while the page loads because it has no side effect; the request never does. WeChat requires the mini program to declare its collection in the MP backend privacy guideline before it will prompt at all, and a reading of `NOT_REQUIRED` does not prove the user agreed. See the privacy configuration section of [../../docs/DEVELOPMENT-en.md](../../docs/DEVELOPMENT-en.md).

The Permission lifecycle card queries, requests, and opens settings for one permission; none of it happens while the page loads, so every step follows a tap. The permission state is read from the host each time rather than remembered, and a refusal is reported as denied rather than as a host failure. The required sequence is in the checklist. The flow has been executed in WeChat Developer Tools at base library 3.17.2 and on an Android device; `NotRequested` could not be produced on the account used, because it already holds a decision for the mapped permission.

The Runtime detection card reads its expected state from the host rather than from a fixed table. An unregistered capability always reads `ungated=Unsupported`, and a registered one reads whichever state its host answers. `VersionDependent` cannot be produced here: the lowest debug base library Developer Tools offers is 2.21.4, which is above the 2.20.1 boundary that capability records, so that state is covered by automated tests instead. The card also shows the base-library version and platform, which the verification record needs.

The Location card is the only card whose capability enforces a precondition of its own. `Check location capability` reports how the gate answers for `wechat.location`; the permission and the privacy requirement are reported separately, because whether the API exists is a different question from whether the user has allowed it. Nothing reads a position while the page loads. A read attempted before the privacy contract is accepted fails with the SDK's privacy-required error and never reaches the host, and a read attempted before the permission is granted is refused rather than prompting on the consumer's behalf — driving that prompt needs a user gesture, so it stays behind `Request location permission`. The page validates the shape of the answer and prints `coordinatesValid` and `accuracyValid`; it never displays or logs a coordinate. Both `app.json` declarations must be in place and the interface must be enabled in the MP backend, or the host refuses before the user sees anything. Developer Tools derives its position from IP rather than from a device receiver, and only for `gcj02`, so the flow can be walked there but only a device proves a real position. See the location configuration section of [../../docs/DEVELOPMENT-en.md](../../docs/DEVELOPMENT-en.md).

The Scanner card opens WeChat's own scanning interface and reports what came back without showing scanned content. `Check scanner capability` reports the gate; `Scan code` permits an album entry, while `Scan from camera only` sets `onlyFromCamera=true`. A device proved that dismissal and system camera access preventing launch produce the same signal, so both print `INTERRUPTED cause=indeterminate` without claiming user cancellation or permission denial; other failures print `FAIL`. The page never displays or logs decoded content, `rawData`, character set, or image path, and it establishes no unverified SDK permission precondition for scanning.

The Media card opens WeChat's own picker and reports what came back without ever showing what was picked. `Check media capability` reports how the gate answers for `wechat.choose-media`; the four choose buttons open the picker for an image, a video, either kind, or the camera alone. Nothing opens the picker while the page loads. The console prints only how many files came back, whether every kind is recognized, and whether metadata is usable; it never logs the selected media, base64, file name, complete temporary path, or raw failure object. The exact Developer Tools dismissal signal (`chooseMedia:cancel`) and Android dismissal signal (`chooseMedia:fail cancel`) print `INTERRUPTED cause=indeterminate`; other failures print only a safe SDK error name and diagnostic category. Media selection asks for no SDK permission. Returned paths are temporary, so copy the media to storage you own if it must outlive the run.

The Subscription Message card asks WeChat to put message templates in front of the user. `Check subscription capability` reports how the gate answers for `wechat.request-subscribe-message`; `Request subscription` runs **only from that button**, because WeChat requires a user gesture, and only when a test template has been configured locally. Template ids belong to this mini program's account, so none is committed: the card's list is empty in the repository, the page reports `NOT CONFIGURED` and makes no host call until you paste your own test ids locally, and the page never displays or logs an id. The console prints counts plus closed status labels (`accept`, `reject`, `ban`, `filter`, or `other`), never a raw answer. This SDK recognizes just `accept`, because no offline source establishes the rest; every other non-blank status is preserved in `hostStatus`. Missing, unexpected, blank, or non-text answers are invalid. Until a real run establishes an exact dismissal signal, failures remain `HostFailure` and the page prints only a closed diagnostic label, never the raw error. Accepting a subscription is a subscription state and never means a message was sent or delivered. See the subscription section of [../../docs/DEVELOPMENT-en.md](../../docs/DEVELOPMENT-en.md).

Node and TypeScript checks do not replace this real-host verification. [../../docs/TESTING-en.md](../../docs/TESTING-en.md) holds the authoritative checklist and the two-layer testing model.

The login code is not an authenticated user or session. A production consumer must send it to its trusted backend for exchange and must not log or persist it as identity.

The files under `miniprogram/libs/` are the consumer-facing distribution copy. Refresh all compiler-managed JavaScript, TypeScript declaration, runtime, and source-map files from the repository root with:

```shell
./gradlew buildMiniAppSdk
```

The task preserves the hand-maintained `kmp-miniapp-sdk.js` and `kmp-miniapp-sdk.d.ts` normalization files. External source maps are copied for local debugging and ignored by Git.
