---
translation-of: module-examples
language: en
status: draft
title: examples · hosts and client examples
---

# Hosts and client examples

[中文](SPEC-ch.md)

## Responsibility and modules

[[module-example-wechat]] is an independent npm/WeChat capability-acceptance host. [[module-example-kmp]] and [[module-example-server]] are independent KMP consumers with their own Wrappers, modules and mobile clients. examples is neither one app nor an SDK root-build Gradle module.

## Why the cases are distinct

The confirmed role of both KMP demos is experimental reference consumption, not generic templates. They expose shared logic, client UI and WeChat integration in real project shapes; the server case also supplies a JVM consumer without creating SDK backend responsibilities. SDK verification/development gaps remain: a running case does not turn them into supported capabilities.

They promise no stable project layout, complete platform compatibility or generic template. The two integration styles are current experimental facts, not a decision to retain both forever. Each case owns its wiring/results; directory organization is not public SDK API. Non-template status applies to these two KMP demos, not removal of the plugin's minimal WeChat source-host initialization-template contract.

## Dependencies and responsibility boundary

The capability host consumes SDK/thin wrapper. Each demo's UI and WeChat host consume its core, with WeChat using the Mini App artifact. The server example's server consumes core's JVM variant, not a dependency of SDK or WeChat greeting.

Compose/resources stay in client UI, WXML/WXSS in WeChat projects. Example convenience must not push UI dependencies into SDK or portray manual core/UI separation as automatic plugin migration. [[architecture]] owns system boundaries.

## Verification

Each README records commands/existing results. Root check does not execute all independent consumers. Compilation, Node smoke, IDE navigation, Developer Tools and devices are separate evidence; one demo's screenshots prove nothing about another. Commit placeholders, not secrets or machine-private settings.
