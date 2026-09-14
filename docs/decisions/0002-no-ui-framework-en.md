# ADR 0002: No UI Framework

[中文](0002-no-ui-framework-ch.md)

- Status: Accepted
- Date: 2026-09-14

## Context

The current product boundary is shared Kotlin logic plus a typed bridge to the WeChat Mini Program runtime. Building a rendering system would introduce a separate product, architecture, and maintenance surface before the basic SDK bridge is proven.

## Decision

The project does not build a UI framework, Virtual DOM, Compose renderer, or WXML replacement. It does not attempt to reproduce or replace Kuikly.

## Consequences

- WXML, WXSS, page rendering, and component trees remain owned by the host application.
- SDK work focuses on shared logic, runtime integration, and typed platform bridges.
- UI experiments are not part of current SDK implementation scope.
- A future reversal requires a new ADR that supersedes this decision; this historical record must not be silently rewritten.
