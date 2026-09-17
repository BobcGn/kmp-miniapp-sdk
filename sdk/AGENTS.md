# SDK Agent Rules

These rules apply to `sdk/**` and extend the repository root `AGENTS.md`.

## `commonMain`

Allowed content:

- Shared APIs and interfaces
- Models and errors
- Platform-neutral logic

Forbidden content:

- `wx`
- `external`, `dynamic`, or `js()`
- DOM APIs
- Node APIs

## `jsMain`

JavaScript interop is allowed, but preserve these layers:

- `interop`
- `adapter`
- `runtime`
- `export`

### `interop`

Describe low-level JavaScript and `wx` contracts only. Do not place business logic here.

### `adapter`

Own type conversion, error mapping, and asynchronous adaptation. Do not implement UI responsibilities.

### `runtime`

Own host runtime and lifecycle integration only.

### `export`

Own Kotlin-to-JavaScript and TypeScript boundaries. Do not turn the export layer into the business implementation layer.

## UI and Rendering

This module is client runtime and host capability. It does not render.

- No Compose, Material, Android view, UIKit, SwiftUI or host-markup dependency or import, anywhere in `sdk/**`. `./gradlew :sdk:checkArchitectureBoundaries` enforces this.
- `jsMain` binds host state and host events; it does not contain a renderer, virtual DOM, view tree, layout engine, Kotlin UI DSL or WXML generator.
- Presentation state and presentation logic have a defined home — a future `presentation` module, P2 — and no implementation exists today. Do not create placeholder modules or types for them.
- The backend owns business truth. Nothing in this module may present a cache, a `UiState`, or a host success callback as the authoritative answer for authentication, payment, inventory, or any other business rule.

## Dependency Direction

- `commonMain` must not depend on WeChat code.
- `interop` must not depend on business implementation logic.
- Platform code may implement contracts defined by platform-neutral code; the reverse dependency is forbidden.
