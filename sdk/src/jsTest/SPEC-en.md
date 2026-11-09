---
translation-of: submodule-sdk-js-tests
language: en
status: draft
title: jsTest · WeChat adaptation and export tests
---

# WeChat adaptation and export tests

[中文](SPEC-ch.md)

## Responsibility and test surface

Use controllable raw callback ports in Kotlin/JS to verify real adapters, interop objects, runtime gates and export observation sessions. `host/wechat/testing` supplies fake raw results and ports, not replacements for adapter behaviour under test.

Storage, HTTP, lifecycle, permission and privacy suites reuse common checks against reference and WeChat implementations. WeChat-specific checks cover failure classification, decoding, version boundaries, abort and listener-identity pairing.

## Boundary and dependencies

Depend on production JS implementations, shared checks from [[submodule-sdk-common-tests]], Kotlin test and coroutines-test. Raw JavaScript may construct malformed host results, but tests do not call real `wx`, show permission UI or read developer-account credentials.

Keep scenario-specific silent ports local rather than expanding production public APIs. Export tests check explicit start/stop, resource ownership and snapshot windows; example smoke/typecheck separately covers CommonJS and TypeScript host consumption.

## Verification

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :kmp-miniapp-sdk:jsNodeTest --tests "*Bluetooth*" --tests "*Ble*"
```

Representative suites are `WechatCapabilityGateTest`, `WechatNetworkTest`, `WechatBluetoothTest`, `NetworkStatusObservationTest` and `BleObservationSessionTest`. A focused filter covers matching tests only, not execution of the full suite.

## Evidence limits

A fake host can reproduce late/double callbacks, missing methods and refused cleanup; success does not establish that real WeChat gives the same answers. Node wiring, Developer Tools, device and backend evidence are reported through separate entries; see [[submodule-wechat-evidence]].
