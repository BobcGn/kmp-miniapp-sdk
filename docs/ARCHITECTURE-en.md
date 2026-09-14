# Architecture

[中文](ARCHITECTURE-ch.md)

This document records stable architectural boundaries. It does not imply that every described integration has been implemented. See [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) for current capabilities.

## 1. System Boundary

```text
commonMain
    ↓
jsMain/export facade
    ↓
Kotlin/JS artifact
    ↓
thin CommonJS export normalization
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program runtime
```

## 2. Dependency Direction

Platform-neutral interfaces, models, errors, and shared logic belong in `commonMain`. Platform implementations belong in `jsMain` and depend on the neutral contracts.

```text
jsMain platform implementation
            ↓ depends on
commonMain interfaces and models
```

`commonMain` must never depend on WeChat implementation details. WeChat-specific code must not leak upward into the shared layer.

## 3. Host Boundary

The WeChat Mini Program is the host runtime. The SDK does not own:

- WXML or WXSS rendering
- Page rendering
- Component trees
- Application UI

The SDK is limited to shared logic, runtime integration, and a typed platform bridge.

## 4. JS Interop Boundary

JavaScript-specific constructs such as `external`, `dynamic`, and `js()` may exist only in `jsMain`. Raw platform contracts are restricted to `jsMain/.../host/wechat/interop`.

Business and shared layers must not manipulate `dynamic` values directly. Conversion to typed Kotlin values belongs at the adapter boundary.

The WeChat interop layer models the global `wx` object, option bags, result shapes, and callbacks only. Its plain-object factories exist solely because external interfaces have no Kotlin constructors; they do not invoke host APIs or contain error mapping, capability policy, or business logic.

`host/wechat` is the platform-specific namespace beneath the host boundary. This namespace preserves room for future hosts without introducing additional host implementations or Gradle modules now.

## 5. Async Boundary

The architectural target for asynchronous platform APIs is:

```text
wx callback
    ↓
interop
    ↓
adapter
    ↓
coroutine-friendly Kotlin API
```

This is an architecture target, not a current capability. No callback-to-coroutine adapter is implemented.

## 6. Public Boundary

The project distinguishes two API surfaces:

- Kotlin public API: Kotlin-idiomatic interfaces, models, and behavior used by Kotlin code.
- JavaScript / TypeScript export API: a consumer-facing boundary designed for JavaScript and TypeScript ergonomics and interoperability constraints.

The export surface may adapt the Kotlin API; it must not become the business implementation layer.

The host-neutral export facade belongs under `jsMain/.../export` and may call shared Kotlin APIs from `commonMain`. It must not contain `wx` calls or platform capability implementations. When the compiler-generated CommonJS namespace is unsuitable as the stable consumer ABI, a thin JavaScript wrapper may normalize module and export shape only; it must not contain business logic, host error mapping, or platform behavior.

## 7. Non-goals

- Reimplementing Kuikly
- Building a Compose Mini Program renderer
- Replacing WXML
- Building a Virtual DOM
- Mirroring the complete `wx` API
