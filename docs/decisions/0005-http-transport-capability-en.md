# ADR 0005: HTTP Transport Capability

[中文](0005-http-transport-capability-ch.md)

- Status: Accepted
- Date: 2026-09-14

## Context

The SDK needs a minimal platform-neutral HTTP transport and a WeChat adapter for it. That raised several boundary questions that ADR 0003 and ADR 0004 did not settle:

- Is HTTP transport a genuinely shared capability, or a WeChat-specific escape hatch?
- Should an HTTP error status be an exception or a response?
- Is a host timeout a distinct SDK failure or a form of host failure?
- Headers are key-value data, but a Kotlin/JS collection cannot cross an `@JsExport` boundary, because a JavaScript caller has no way to construct one.
- `wx.request` is the first host API in this SDK that returns a real abort handle.

ADR 0003 established that only genuinely shared semantics become common capabilities, and ADR 0004 established the error and cancellation model without implementing any capability.

## Decision

- HTTP transport is a common capability, `MiniAppHttpTransport`, identified by `CapabilityKey("network")`. Any host able to perform an HTTP exchange can honour its semantics, so it is not a WeChat-specific escape hatch.
- The contract carries text only: URL, method, headers, an already-encoded body, and an optional timeout in; status code, headers, and a decoded body out. Payload serialization, cookies, redirect policy, streaming, upload, and download are excluded.
- A completed exchange is a response for every HTTP status. `4xx` and `5xx` describe the exchange rather than the SDK's ability to perform it, so they do not throw. Only a transport-level failure becomes a `MiniAppException`.
- `MiniAppException.Timeout` is added as a variant distinct from `HostFailure`, because a timeout means the host stopped waiting rather than that the operation was defective. This extends the variant list recorded in ADR 0004 without superseding that decision.
- `MiniAppHttpResponse.headers` carries single string values only. A host header reported in another shape, such as the array WeChat reports for `Set-Cookie`, is left unrepresented rather than lossily stringified.
- The adapter requests raw response text and reports a non-text body as `InvalidResponse`. The string contract must not depend on the host's JSON auto-parsing.
- `wx.request` returns an abort handle, so the WeChat adapter supplies the first real abort hook to `awaitHostCallback`. Cancelling the coroutine aborts the host exchange at most once, and any failure callback WeChat delivers for the aborted exchange is discarded. This implements, rather than changes, the cancellation rule from ADR 0004.
- The export facade cannot carry a Kotlin map across `@JsExport`. Headers therefore cross that boundary as a flat sequence of alternating name and value strings, and the hand-maintained CommonJS wrapper restores an object shape. The Kotlin API keeps `Map<String, String>`.
- No Ktor, Retrofit-like API, JSON serialization framework, cache, or retry framework is introduced.

## Consequences

- Shared code can perform an HTTP exchange without depending on WeChat declarations.
- Consumers distinguish an HTTP error response from a transport failure, and a timeout from a generic host failure, without parsing host messages themselves.
- How a host represents an exchange is decided by its adapter, not by the common contract.
- The JavaScript boundary needs an explicit representation rule for key-value data, which later capabilities that export maps must follow.
- A small amount of header conversion lives in the JavaScript wrapper. It is representation only; host access and error mapping stay in the adapter.
- Header values a host reports in a non-string shape remain unavailable to consumers until a concrete use case establishes the representation it needs.
