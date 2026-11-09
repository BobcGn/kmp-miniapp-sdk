---
translation-of: module-miniapp-gradle-plugin
language: en
status: draft
title: miniapp-gradle-plugin · consumer build integration
---

# Consumer build integration

[中文](SPEC-ch.md)

## Responsibility and public surface

`MiniAppGradlePlugin` implements `io.github.bobcgn.miniapp`. Public configuration is `MiniAppExtension` and typed `wechat`; task contracts are miniappTest, assembleMiniAppBundle, initMiniApp, syncMiniApp, assembleMiniApp, verifyMiniApp and checkMiniAppHostBoundary. It injects the public SDK coordinate into consumers without linking runtime implementation.

## Why the plugin owns wiring instead of consumer scripts

[ADR 0010](../docs/decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) compares manual named targets, unconsumed custom source sets and plugin-owned targets. Names without compilations silently ignore consumer source; correct wiring handwritten by each consumer still preserves the integration burden. The plugin therefore creates a target, KGP owns its model, and consumers keep business source.

[MiniAppPlatformSupport.kt](src/main/kotlin/io/github/bobcgn/miniapp/gradle/MiniAppPlatformSupport.kt) establishes current implementation. The cost is KGP naming/variant/compatibility constraints, not an independent Mini App compiler. Public runtime coordinates also avoid consumer dependence on SDK-internal project paths.

## Platform and configuration ownership

A Kotlin/JS target named `miniapp` lets KGP derive real source sets, compilations and common parents. The plugin does not hand-build dependsOn models. Configuration materializes conventional Kotlin directories, not WeChat hosts. Node, CommonJS libraries and declarations describe the current host build shape.

Module mode is owned by the compilation project. rootHost moves host lifecycle to the root while retaining module compilation/bundle ownership. A non-KMP root may select an existing KMP subproject through logicProject before evaluation: no name inference, module creation, source migration, dependency removal or dependent-project auto-configuration.

## Distribution and path boundary

Init explicitly fills missing files; sync owns only generated libraries. Assemble combines source and current KGP output into a standalone distribution, without requiring prior sync or assembleMiniAppBundle. Verify reads only that distribution. Bundle, source, generated and distribution paths have distinct ownership.

Canonical checks keep source/distribution disjoint and generated strictly inside source. Root mode additionally keeps source outside root build and distribution inside it, permits one producer and rejects task replacement. Module mode does not fully forbid source under build; bundle checks only require containment in the compilation project. Handwritten-file protection is heuristic, not universal deletion safety.

## Dependencies and forbidden reaches

KGP is compileOnly; version/coordinate resources come from the catalog. Before assembly, checkMiniAppHostBoundary checks the fully resolved runtime classpath, rejects known renderer coordinates/unresolved dependencies, and never deletes JS files to disguise bad dependencies. It detects no arbitrary handwritten renderer.

No AppID, secret, merchant, template, business state, markup or deployment belongs here. Build paths/selection are not runtime APIs; build success is not host acceptance.

## Verification and limits

[[submodule-plugin-tests]] owns contract, TestKit and declaration checks. `./gradlew :miniapp-gradle-plugin:test` and root `verifyMiniAppGradlePluginIntegration` have different scopes. Independent consumers still need compatible Mini App variants; current representative checks establish no universal compatibility with other pre-existing named JS targets.
