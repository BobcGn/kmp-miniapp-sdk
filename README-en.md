# kmp-miniapp-sdk

[中文](README-ch.md)

A Kotlin Multiplatform client runtime for sharing client behaviour, host capabilities, and presentation state across Mini App platforms, while keeping rendering host-native. Kotlin/JS carries the current WeChat bridge.

## Status

Experimental / pre-alpha. The Consumer Bridge, Storage, `wx.login` client authentication bootstrap, `wx.request` HTTP transport, App lifecycle, part of WeChat page-stack navigation, runtime capability detection, the permission lifecycle, the WeChat session check, WeChat clipboard and vibration, basic WeChat file-system access, one-shot on-demand WeChat location, WeChat scanning, and WeChat media selection are implemented and have the required real-host evidence. Privacy authorization is implemented but still depends on the mini program's backend privacy configuration and on the account, so its real-host acceptance remains outstanding. The location-permission host run covers `NotRequested`, `Granted`, and `Denied`. WeChat subscription-message requests are implemented and covered by automated checks; their real-host acceptance is outstanding. The WeChat network extensions — the network type query, the network status listener, upload, and download — are implemented and covered by automated checks; accepting a real upload or download needs a controlled HTTPS test service, and observing a real network change needs a device. WeChat standard payment is implemented as a typed forwarder of backend-produced parameters and covered by automated checks; it computes no signature and holds no merchant key, and its real-host acceptance needs a legal merchant environment, so the capability stays `Partial`. See the [WeChat capability matrix](docs/platforms/wechat/WECHAT_CAPABILITIES-en.md) for the exact status of every other capability.

## What this project is

- A Kotlin Multiplatform library for shared client behaviour and host capabilities
- A Kotlin/JS library intended for a WeChat Mini Program host
- A foundation for a typed JavaScript and TypeScript runtime bridge

## What this project is not

- A UI framework or Kuikly replacement
- A Compose renderer or Virtual DOM
- A replacement for WXML
- A renderer of any kind: the backend owns business truth, Kotlin owns client behaviour, and the host owns rendering. See [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-en.md).

## Architecture

```text
commonMain
    ↓
jsMain
    ↓
Kotlin/JS
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program
```

## Current scope

The repository currently provides the `:kmp-miniapp-sdk` module with its CommonJS and TypeScript consumer artifacts, plus a `:miniapp-gradle-plugin` plugin that gives a Kotlin Multiplatform project `miniappMain` / `miniappTest`, the runtime SDK, and an `assembleMiniAppBundle` task that emits the compiler-managed Mini App distribution. Platform-neutral contracts live in `commonMain`; WeChat interop, adapter, runtime, and export boundaries live in `jsMain`. Only capabilities explicitly listed as implemented in the matrix are currently supported; planned entries are not implementation facts.

## Build

Use the checked-in Gradle Wrapper:

```shell
./gradlew projects
./gradlew clean build
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :miniapp-gradle-plugin:test
./gradlew buildMiniAppSdk
```

## Documentation

- [Current project facts](docs/PROJECT_FACTS-en.md)
- [Architecture](docs/ARCHITECTURE-en.md)
- [Development guide](docs/DEVELOPMENT-en.md)
- [WeChat capability matrix](docs/platforms/wechat/WECHAT_CAPABILITIES-en.md)
- [Testing and WeChat host verification](docs/TESTING-en.md)
- [Roadmap](docs/ROADMAP-en.md)
- [Architecture decisions](docs/decisions/README-en.md)

## Project facts

For the current project state, see [docs/PROJECT_FACTS-en.md](docs/PROJECT_FACTS-en.md). Executable Gradle configuration and source code take precedence if documentation becomes stale.

## License

TBD.
