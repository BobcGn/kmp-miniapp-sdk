# kmp-miniapp-sdk

[中文](README-ch.md)

A Kotlin Multiplatform SDK for bridging shared Kotlin logic into WeChat Mini Programs through Kotlin/JS.

## Status

Experimental / pre-alpha. The project bootstrap builds and tests, but no `wx` API bridge is implemented.

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

The repository currently provides one `:sdk` module, a JavaScript library target, CommonJS output, TypeScript definition generation, and Node.js-based Kotlin/JS tests. It does not yet expose a JavaScript consumer API or implement a WeChat platform API.

## Build

Use the checked-in Gradle Wrapper:

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
```

## Documentation

- [Current project facts](docs/PROJECT_FACTS-en.md)
- [Architecture](docs/ARCHITECTURE-en.md)
- [Development guide](docs/DEVELOPMENT-en.md)
- [Roadmap](docs/ROADMAP-en.md)
- [Architecture decisions](docs/decisions/README-en.md)

## Project facts

For the current project state, see [docs/PROJECT_FACTS-en.md](docs/PROJECT_FACTS-en.md). Executable Gradle configuration and source code take precedence if documentation becomes stale.

## License

TBD.
