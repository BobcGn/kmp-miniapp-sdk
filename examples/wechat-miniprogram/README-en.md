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

The example has three pages. The index page imports the normalized CommonJS SDK, calls `sdkVersion()`, exercises Storage, requests a short-lived WeChat login code, performs an HTTPS request through the SDK HTTP transport, and shows the app lifecycle state and current page route. The second and third pages exist so the page-stack bridge can be exercised: the second is opened with `wx.navigateTo` and replaces itself with the third using `wx.redirectTo`, and the third goes back with `wx.navigateBack`. The login code itself is never logged or rendered.

`app.ts` forwards WeChat's App hooks, because WeChat reports lifecycle only to the registration the consumer owns. Each page forwards its own Page hooks and supplies its own `this.route`.

## Local verification

```shell
npm install
npm run smoke
npm run typecheck
```

## WeChat Developer Tools

Import this directory as a Mini Program project and compile it. The index page must display `0.1.0-SNAPSHOT`, a `FOREGROUND` lifecycle state with a page route, and a `PASS` status for Storage, the client login code, and the network check. The console must contain:

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null
[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<positive integer>
[kmp-miniapp-sdk] network: PASS status=200, bytes=<positive integer>
```

The network check issues a `GET` to `https://example.com/`. WeChat requires that host to be listed in the request domain whitelist, or the project must be compiled with domain checking disabled. Change the `networkUrl` constant to verify a different endpoint.

Navigation needs interaction rather than a single page load. Tapping through the second and third pages prints `[kmp-miniapp-sdk] navigation: PASS <action>` for `wx.navigateTo`, `wx.redirectTo`, and `wx.navigateBack`; the tap sequence is in the checklist.

Node and TypeScript checks do not replace this real-host verification. [../../docs/TESTING-en.md](../../docs/TESTING-en.md) holds the authoritative checklist and the two-layer testing model.

The login code is not an authenticated user or session. A production consumer must send it to its trusted backend for exchange and must not log or persist it as identity.

The files under `miniprogram/libs/` are the consumer-facing distribution copy. Refresh all compiler-managed JavaScript, TypeScript declaration, runtime, and source-map files from the repository root with:

```shell
./gradlew buildMiniAppSdk
```

The task preserves the hand-maintained `kmp-miniapp-sdk.js` and `kmp-miniapp-sdk.d.ts` normalization files. External source maps are copied for local debugging and ignored by Git.
