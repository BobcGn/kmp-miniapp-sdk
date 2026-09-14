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

## Dependency Direction

- `commonMain` must not depend on WeChat code.
- `interop` must not depend on business implementation logic.
- Platform code may implement contracts defined by platform-neutral code; the reverse dependency is forbidden.
