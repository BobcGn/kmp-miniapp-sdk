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

The TypeScript index page imports the normalized CommonJS SDK, calls `sdkVersion()`, logs the returned value, and renders it on the page.

## Local verification

```shell
npm install
npm run smoke
npm run typecheck
```

## WeChat Developer Tools

Import this directory as a Mini Program project and compile it. The index page must display `0.1.0-SNAPSHOT`, and the console must contain:

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
```

Node and TypeScript checks do not replace this real-host verification.

The files under `miniprogram/libs/` are the consumer-facing distribution copy. Refreshing compiler artifacts is manual in this phase; build automation belongs to a later task.
