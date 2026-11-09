---
translation-of: module-repository-build
language: en
status: draft
title: root build · artifact and verification wiring
---

# Root-build artifact and verification wiring

[中文](SPEC-ch.md)

## Responsibility and entries

Root `settings.gradle.kts` includes the runtime and consumer plugin projects plus repository-only build-logic. Independent examples, fixtures and PoCs do not automatically become modules of this build. Root scripts orchestrate repository validation, not the consumer plugin's job.

`buildMiniAppSdk` depends on SDK production-library distribution and synchronizes compiler modules, declarations and source maps to the WeChat capability example while retaining its handwritten CommonJS wrapper. It is not a consumer initialization/distribution entry.

## Verification tasks and ownership

`verifyMiniAppConsumerDocs` checks root README heading/fence structure, fixture paths and consumer commands and joins check. `verifyMiniAppGradlePluginIntegration` aggregates plugin suites, SDK guard and real consumer fixtures without joining check, avoiding nested-build reentry.

Fixture runs are ordered so clean operations and concurrent artifact writes cannot undo each other. They remain ordinary independent builds consuming public coordinates. [[module-build-logic]] supplies size checks; [[submodule-gradle-toolchain]] owns versions/coordinates; [[module-ci-release]] owns CI/publication.

## Boundary and verification

Root wiring adds no renderer, business configuration, source migration or host deployment. `check` establishes no execution of independent demos, devices or backend chains.

```shell
./gradlew projects
./gradlew --no-daemon --console=plain verifyMiniAppConsumerDocs
./gradlew verifyMiniAppGradlePluginIntegration
```

These commands have different scopes. Running only documentation checks establishes no full build. Actual Gradle tasks/logs are evidence; historical results are not relabelled as this run.
