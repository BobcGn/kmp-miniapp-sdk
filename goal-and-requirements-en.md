---
translation-of: goal-and-requirements
language: en
status: draft
title: Project goal and scope
---

# Project goal and scope

[中文](goal-and-requirements-ch.md)

## Goal and audience

Let Kotlin Multiplatform consumers bring existing shared client behaviour to Mini Apps while each host owns final rendering. The project delivers a runtime SDK and its Gradle plugin, not an application or UI framework.

Its users are client developers writing shared Kotlin and WeChat host developers consuming artifacts through JavaScript / TypeScript. Shared code should not duplicate business logic to integrate WeChat, and the host should not take over SDK capability adaptation.

## Current capabilities

- The runtime supplies host-neutral contracts, semantic errors and asynchronous adaptation, together with the current WeChat implementation; common contracts remain distinct from platform-specific APIs.
- The plugin integrates Mini App compilation, tests, runtime dependencies, host initialization and standalone distribution into consumer builds. Module-local integration and root selection of a logic project are configuration entry points, not different runtimes.
- The compiler generates CommonJS and TypeScript declarations; consumers invoke exported entries in the host, while Kotlin consumers use the corresponding APIs and source documentation.
- The repository checks these boundaries with isolated automated tests, real consumer fixtures and host examples. Implementation presence, Node wiring, WeChat execution, real-device acceptance and backend acceptance are different conclusions.

[Project Facts](docs/PROJECT_FACTS-en.md) and the [WeChat capability matrix](docs/platforms/wechat/WECHAT_CAPABILITIES-en.md) index exact implementation status and existing acceptance limits; this document does not promote `Partial`, `Experimental` or unimplemented entries into complete capabilities.

## Durable boundaries

The backend owns trusted identity, orders and other business facts; the SDK owns client behaviour and host capabilities; the host owns rendering. Sharing presentation state has a recorded responsibility, but there is no Presentation Core implementation today, and ownership does not establish capability.

[[architecture]] owns system topology, dependency direction and concrete constraints. Module specs describe local responsibilities rather than copying the same system rules.

## Non-goals

- A renderer, virtual DOM, layout engine, Kotlin UI DSL or replacement for WXML/WXSS.
- Client-side signing, holding merchant keys, or confirming identity, payment or order facts from host callbacks.
- Mirroring every WeChat API or presenting WeChat-specific semantics as universal host contracts.
- Consumer-plugin uploads, deployment, or management of AppID, templates, payment or backend business configuration.

## Specs and evidence

Chinese files are the spec tree's sole nodes; English files are synchronized translations. `draft` means user review is still pending. Specs describe current responsibilities and explicit decisions, not a roadmap or task list. Recorded verification retains its original environment and date; importing specs is not fresh acceptance.
