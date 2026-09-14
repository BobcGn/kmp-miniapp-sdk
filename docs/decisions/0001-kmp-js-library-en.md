# ADR 0001: Kotlin Multiplatform JavaScript Library

[中文](0001-kmp-js-library-ch.md)

- Status: Accepted
- Date: 2026-09-14

## Context

The project needs to share Kotlin logic with a WeChat Mini Program. A WeChat Mini Program is a JavaScript host runtime, not a conventional browser application. The project is an SDK and must produce a consumable library rather than an executable web application.

## Decision

Use official Kotlin Multiplatform with a Kotlin/JS target to build the SDK. Configure the JavaScript target as a CommonJS library. Use Node.js only for local build and test execution; treat the WeChat Mini Program JavaScript runtime as the intended production host.

The project is not a Kotlin/JS browser application and does not use a browser application or webpack executable model.

## Consequences

- Platform-neutral code remains in `commonMain`.
- JavaScript and WeChat-specific integration remains in `jsMain`.
- The build produces a library artifact rather than an application bundle.
- Browser globals and DOM APIs are not architectural dependencies.
- Host compatibility must ultimately be validated in a WeChat Mini Program environment; Node.js tests alone are insufficient.
