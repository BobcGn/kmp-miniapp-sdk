# kmp-miniapp-sdk

[中文](README-ch.md)

A Kotlin Multiplatform SDK for bridging shared Kotlin logic into WeChat Mini Programs through Kotlin/JS.

## Status

Experimental / pre-alpha. The Consumer Bridge, Storage, `wx.login` client authentication bootstrap, `wx.request` HTTP transport, App lifecycle, part of WeChat page-stack navigation, runtime capability detection, the permission lifecycle, the WeChat session check, and WeChat clipboard and vibration are implemented and have the required real-host evidence. Privacy authorization is implemented but still depends on the mini program's backend privacy configuration and on the account, so its real-host acceptance remains outstanding. `NotRequested` could not be produced on the account used for the permission run, because it already holds a decision for the one mapped permission; that state is covered by automated tests. See the [WeChat capability matrix](docs/platforms/wechat/WECHAT_CAPABILITIES-en.md) for the exact status of every other capability.

## What this project is

- A Kotlin Multiplatform library for shared Kotlin logic
- A Kotlin/JS library intended for a WeChat Mini Program host
- A foundation for a typed JavaScript and TypeScript runtime bridge

## What this project is not

- A UI framework or Kuikly replacement
- A Compose renderer or Virtual DOM
- A replacement for WXML

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

The repository currently provides one `:sdk` module and CommonJS and TypeScript consumer artifacts. Platform-neutral contracts live in `commonMain`; WeChat interop, adapter, runtime, and export boundaries live in `jsMain`. Only capabilities explicitly listed as implemented in the matrix are currently supported; planned entries are not implementation facts.

## Build

Use the checked-in Gradle Wrapper:

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
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
